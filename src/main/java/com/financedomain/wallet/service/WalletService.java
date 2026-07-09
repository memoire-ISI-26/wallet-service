package com.financedomain.wallet.service;

import com.financedomain.wallet.bean.Account;
import com.financedomain.wallet.bean.Transaction;
import com.financedomain.wallet.enums.TransactionType;
import com.financedomain.wallet.dto.TrackingEvent;
import com.financedomain.wallet.proxy.TrackingProxy;
import com.financedomain.wallet.exception.InsufficentAmountException;
import com.financedomain.wallet.exception.UnknownAccountException;
import com.financedomain.wallet.repository.AccountRepository;
import com.financedomain.wallet.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TrackingProxy trackingProxy;

    public Account createAccount(Account account) {
        if (accountRepository.existsByNumber(account.getNumber())) {
            throw new IllegalArgumentException("Le numéro de téléphone '" + account.getNumber() + "' est déjà associé à un compte.");
        }
        account.setBalance(0.0);
        account.setCallCredit(0.0);
        if (account.getCurrency() == null || account.getCurrency().isEmpty()) {
            account.setCurrency("XOF");
        }
        return accountRepository.save(account);
    }

    public List<Account> getAccountsByUserId(long idUser) {
        return accountRepository.findByIdUser(idUser);
    }

    public Optional<Account> getAccountByNumber(String number) {
        return accountRepository.findByNumber(number);
    }

    public double getBalance(String number) {
        return getAccountByNumber(number)
                .map(Account::getBalance)
                .orElseThrow(() -> new UnknownAccountException("Compte introuvable pour le numéro : " + number));
    }

    @Transactional
    public Transaction transfer(String senderNumber, String receiverNumber, double amount) {
        if (amount <= 0) {
            throw new InsufficentAmountException("Le montant du transfert doit être supérieur à 0.");
        }

        Account sender = getAccountByNumber(senderNumber)
                .orElseThrow(() -> new UnknownAccountException("Compte expéditeur introuvable : " + senderNumber));

        Account receiver = getAccountByNumber(receiverNumber)
                .orElseThrow(() -> new UnknownAccountException("Compte receveur introuvable : " + receiverNumber));

        if (sender.getBalance() < amount) {
            throw new InsufficentAmountException("Solde insuffisant sur le compte expéditeur.");
        }

        // Débiter/Créditer
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Enregistrer la transaction
        Transaction txn = new Transaction();
        txn.setSender(senderNumber);
        txn.setReceiver(receiverNumber);
        txn.setAmount(amount);
        txn.setType(TransactionType.TRANSFERT);
        txn.setCreatedAt(LocalDateTime.now());

        Transaction savedTxn = transactionRepository.save(txn);

        // Tracking
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", savedTxn.getId());
        payload.put("amount", amount);
        payload.put("receiverNumber", receiverNumber);
        sendTrackingEvent("TRANSFER", senderNumber, payload);

        return savedTxn;
    }

    @Transactional
    public Transaction deposit(String number, double amount) {
        if (amount <= 0) {
            throw new InsufficentAmountException("Le montant du dépôt doit être supérieur à 0.");
        }

        Account account = getAccountByNumber(number)
                .orElseThrow(() -> new UnknownAccountException("Compte introuvable : " + number));

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setSender(number);
        txn.setReceiver(number);
        txn.setAmount(amount);
        txn.setType(TransactionType.DEPOT);
        txn.setCreatedAt(LocalDateTime.now());

        Transaction savedTxn = transactionRepository.save(txn);

        // Tracking
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", savedTxn.getId());
        payload.put("amount", amount);
        sendTrackingEvent("DEPOSIT", number, payload);

        return savedTxn;
    }

    @Transactional
    public Transaction withdraw(String number, double amount) {
        if (amount <= 0) {
            throw new InsufficentAmountException("Le montant du retrait doit être supérieur à 0.");
        }

        Account account = getAccountByNumber(number)
                .orElseThrow(() -> new UnknownAccountException("Compte introuvable : " + number));

        if (account.getBalance() < amount) {
            throw new InsufficentAmountException("Solde insuffisant.");
        }

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setSender(number);
        txn.setReceiver(number);
        txn.setAmount(amount);
        txn.setType(TransactionType.RETRAIT);
        txn.setCreatedAt(LocalDateTime.now());

        Transaction savedTxn = transactionRepository.save(txn);

        // Tracking
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", savedTxn.getId());
        payload.put("amount", amount);
        sendTrackingEvent("WITHDRAW", number, payload);

        return savedTxn;
    }

    @Transactional
    public Transaction purchase(String senderNumber, String receiverNumber, double amount, TransactionType type, String paymentMethod) {
        if (amount <= 0) {
            throw new InsufficentAmountException("Le montant de l'achat doit être supérieur à 0.");
        }

        Account sender = getAccountByNumber(senderNumber)
                .orElseThrow(() -> new UnknownAccountException("Compte acheteur introuvable : " + senderNumber));

        if (type == TransactionType.ACHAT_CREDIT) {
            if (sender.getBalance() < amount) {
                throw new InsufficentAmountException("Solde principal insuffisant pour acheter du crédit.");
            }
            sender.setBalance(sender.getBalance() - amount);
            accountRepository.save(sender);

            // Créditer le destinataire
            Account receiver = getAccountByNumber(receiverNumber != null ? receiverNumber : senderNumber)
                    .orElseThrow(() -> new UnknownAccountException("Compte destinataire du crédit introuvable."));
            if (receiver.getCallCredit() == null) {
                receiver.setCallCredit(0.0);
            }
            receiver.setCallCredit(receiver.getCallCredit() + amount);
            accountRepository.save(receiver);
        } else {
            // Achat de pass ou autre paiement
            if ("CREDIT".equalsIgnoreCase(paymentMethod)) {
                if (sender.getCallCredit() == null || sender.getCallCredit() < amount) {
                    throw new InsufficentAmountException("Crédit téléphonique insuffisant pour cet achat.");
                }
                sender.setCallCredit(sender.getCallCredit() - amount);
            } else {
                // Par défaut, payer avec le portefeuille principal (WALLET)
                if (sender.getBalance() < amount) {
                    throw new InsufficentAmountException("Solde principal insuffisant pour cet achat.");
                }
                sender.setBalance(sender.getBalance() - amount);
            }
            accountRepository.save(sender);
        }

        Transaction txn = new Transaction();
        txn.setSender(senderNumber);
        txn.setReceiver(receiverNumber != null ? receiverNumber : senderNumber);
        txn.setAmount(amount);
        txn.setType(type);
        txn.setCreatedAt(LocalDateTime.now());

        Transaction savedTxn = transactionRepository.save(txn);

        // Tracking
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", savedTxn.getId());
        payload.put("amount", amount);
        payload.put("purchaseType", type.toString());
        payload.put("receiverNumber", receiverNumber);
        sendTrackingEvent("PURCHASE", senderNumber, payload);

        return savedTxn;
    }

    public List<Transaction> getTransactionHistory(String number) {
        return transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(number, number);
    }

    private void sendTrackingEvent(String eventType, String msisdn, Object payload) {
        String xUserId = "unknown";
        String xUserRole = "INTERNAL"; // fallback for internal server calls
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String headerId = request.getHeader("X-User-Id");
                if (headerId != null) xUserId = headerId;
                String headerRole = request.getHeader("X-User-Role");
                if (headerRole != null) xUserRole = headerRole;
            }
        } catch (Exception e) {
            // Ignore context issues
        }

        try {
            TrackingEvent event = TrackingEvent.builder()
                    .eventType(eventType)
                    .msisdn(msisdn)
                    .userId(xUserId)
                    .userRole(xUserRole)
                    .sourceService("wallet-service")
                    .payload(payload)
                    .timestamp(java.time.Instant.now())
                    .build();
            trackingProxy.collectEvent(event, "INTERNAL");
        } catch (Exception e) {
            System.err.println("Erreur de tracking wallet: " + e.getMessage());
        }
    }
}

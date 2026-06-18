package com.financedomain.wallet.service;

import com.financedomain.wallet.bean.Account;
import com.financedomain.wallet.bean.Transaction;
import com.financedomain.wallet.enums.TransactionType;
import com.financedomain.wallet.exception.InsufficentAmountException;
import com.financedomain.wallet.exception.UnknownAccountException;
import com.financedomain.wallet.repository.AccountRepository;
import com.financedomain.wallet.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Account createAccount(Account account) {
        if (accountRepository.existsByNumber(account.getNumber())) {
            throw new IllegalArgumentException("Le numéro de téléphone '" + account.getNumber() + "' est déjà associé à un compte.");
        }
        account.setBalance(0.0);
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

        return transactionRepository.save(txn);
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

        return transactionRepository.save(txn);
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

        return transactionRepository.save(txn);
    }

    @Transactional
    public Transaction purchase(String senderNumber, String receiverNumber, double amount, TransactionType type) {
        if (amount <= 0) {
            throw new InsufficentAmountException("Le montant de l'achat doit être supérieur à 0.");
        }

        Account sender = getAccountByNumber(senderNumber)
                .orElseThrow(() -> new UnknownAccountException("Compte acheteur introuvable : " + senderNumber));

        if (sender.getBalance() < amount) {
            throw new InsufficentAmountException("Solde insuffisant pour effectuer l'achat.");
        }

        sender.setBalance(sender.getBalance() - amount);
        accountRepository.save(sender);

        Transaction txn = new Transaction();
        txn.setSender(senderNumber);
        txn.setReceiver(receiverNumber != null ? receiverNumber : senderNumber);
        txn.setAmount(amount);
        txn.setType(type);
        txn.setCreatedAt(LocalDateTime.now());

        return transactionRepository.save(txn);
    }

    public List<Transaction> getTransactionHistory(String number) {
        return transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(number, number);
    }
}

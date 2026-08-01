package com.financedomain.wallet.service;

import com.financedomain.wallet.bean.Account;
import com.financedomain.wallet.bean.Transaction;
import com.financedomain.wallet.enums.TransactionType;
import com.financedomain.wallet.exception.InsufficentAmountException;
import com.financedomain.wallet.exception.UnknownAccountException;
import com.financedomain.wallet.proxy.TrackingProxy;
import com.financedomain.wallet.repository.AccountRepository;
import com.financedomain.wallet.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TrackingProxy trackingProxy;

    @InjectMocks
    private WalletService walletService;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setIdUser(10L);
        senderAccount.setNumber("771000001");
        senderAccount.setBalance(5000.0);
        senderAccount.setCallCredit(1000.0);
        senderAccount.setCurrency("XOF");

        receiverAccount = new Account();
        receiverAccount.setId(2L);
        receiverAccount.setIdUser(20L);
        receiverAccount.setNumber("772000002");
        receiverAccount.setBalance(2000.0);
        receiverAccount.setCallCredit(0.0);
        receiverAccount.setCurrency("XOF");
    }

    @Test
    @DisplayName("Devrait créer un compte avec solde initial à 0 et devise XOF par défaut")
    void shouldCreateAccountSuccessfullyWithDefaultValues() {
        Account newAcc = new Account();
        newAcc.setNumber("773000003");
        newAcc.setIdUser(30L);

        when(accountRepository.existsByNumber("773000003")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account created = walletService.createAccount(newAcc);

        assertNotNull(created);
        assertEquals(0.0, created.getBalance());
        assertEquals(0.0, created.getCallCredit());
        assertEquals("XOF", created.getCurrency());
    }

    @Test
    @DisplayName("Devrait lever IllegalArgumentException si le numéro existe déjà")
    void shouldThrowIllegalArgumentExceptionWhenNumberAlreadyExists() {
        Account newAcc = new Account();
        newAcc.setNumber("771000001");

        when(accountRepository.existsByNumber("771000001")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> walletService.createAccount(newAcc));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("Devrait retourner le solde du compte")
    void shouldGetBalance() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));

        double balance = walletService.getBalance("771000001");

        assertEquals(5000.0, balance);
    }

    @Test
    @DisplayName("Devrait lever UnknownAccountException si le compte n'existe pas lors du getBalance")
    void shouldThrowUnknownAccountExceptionWhenGettingBalanceForNonExistentNumber() {
        when(accountRepository.findByNumber("999999999")).thenReturn(Optional.empty());

        assertThrows(UnknownAccountException.class, () -> walletService.getBalance("999999999"));
    }

    @Test
    @DisplayName("Devrait effectuer un transfert entre deux comptes avec succès")
    void shouldTransferSuccessfully() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByNumber("772000002")).thenReturn(Optional.of(receiverAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        Transaction txn = walletService.transfer("771000001", "772000002", 1500.0);

        assertNotNull(txn);
        assertEquals(3500.0, senderAccount.getBalance());
        assertEquals(3500.0, receiverAccount.getBalance());
        assertEquals(TransactionType.TRANSFERT, txn.getType());
        verify(accountRepository).save(senderAccount);
        verify(accountRepository).save(receiverAccount);
    }

    @Test
    @DisplayName("Devrait lever InsufficentAmountException si le solde de l'expéditeur est insuffisant")
    void shouldThrowInsufficentAmountExceptionWhenSenderBalanceIsLow() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByNumber("772000002")).thenReturn(Optional.of(receiverAccount));

        assertThrows(InsufficentAmountException.class, () -> walletService.transfer("771000001", "772000002", 10000.0));
    }

    @Test
    @DisplayName("Devrait effectuer un dépôt sur un compte avec succès")
    void shouldDepositSuccessfully() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(101L);
            return t;
        });

        Transaction txn = walletService.deposit("771000001", 2000.0);

        assertNotNull(txn);
        assertEquals(7000.0, senderAccount.getBalance());
        assertEquals(TransactionType.DEPOT, txn.getType());
    }

    @Test
    @DisplayName("Devrait effectuer un retrait sur un compte avec succès")
    void shouldWithdrawSuccessfully() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(102L);
            return t;
        });

        Transaction txn = walletService.withdraw("771000001", 1000.0);

        assertNotNull(txn);
        assertEquals(4000.0, senderAccount.getBalance());
        assertEquals(TransactionType.RETRAIT, txn.getType());
    }

    @Test
    @DisplayName("Devrait acheter du crédit téléphonique et créditer le destinataire")
    void shouldPurchaseCreditSuccessfully() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findByNumber("772000002")).thenReturn(Optional.of(receiverAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction txn = walletService.purchase("771000001", "772000002", 1000.0, TransactionType.ACHAT_CREDIT, "WALLET");

        assertNotNull(txn);
        assertEquals(4000.0, senderAccount.getBalance());
        assertEquals(1000.0, receiverAccount.getCallCredit());
    }

    @Test
    @DisplayName("Devrait acheter un pass avec le crédit téléphonique (CREDIT)")
    void shouldPurchasePassUsingPhoneCreditSuccessfully() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction txn = walletService.purchase("771000001", "771000001", 500.0, TransactionType.ACHAT_ILLIMIX, "CREDIT");

        assertNotNull(txn);
        assertEquals(500.0, senderAccount.getCallCredit());
        assertEquals(5000.0, senderAccount.getBalance());
    }

    @Test
    @DisplayName("Devrait acheter un pass avec le solde principal (WALLET)")
    void shouldPurchasePassUsingMainWalletBalanceSuccessfully() {
        when(accountRepository.findByNumber("771000001")).thenReturn(Optional.of(senderAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction txn = walletService.purchase("771000001", "771000001", 2000.0, TransactionType.ACHAT_INTERNET, "WALLET");

        assertNotNull(txn);
        assertEquals(3000.0, senderAccount.getBalance());
    }

    @Test
    @DisplayName("Devrait retourner l'historique des transactions pour un numéro")
    void shouldGetTransactionHistory() {
        Transaction t = new Transaction();
        t.setSender("771000001");
        t.setReceiver("772000002");
        t.setAmount(1000.0);

        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc("771000001", "771000001"))
                .thenReturn(List.of(t));

        List<Transaction> history = walletService.getTransactionHistory("771000001");

        assertEquals(1, history.size());
        assertEquals(1000.0, history.get(0).getAmount());
    }
}

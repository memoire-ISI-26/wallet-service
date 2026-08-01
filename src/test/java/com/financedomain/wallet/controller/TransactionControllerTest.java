package com.financedomain.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financedomain.wallet.bean.Transaction;
import com.financedomain.wallet.dto.OperationRequest;
import com.financedomain.wallet.dto.PurchaseRequest;
import com.financedomain.wallet.dto.TransferRequest;
import com.financedomain.wallet.enums.TransactionType;
import com.financedomain.wallet.exception.InsufficentAmountException;
import com.financedomain.wallet.exception.UnknownAccountException;
import com.financedomain.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();

        sampleTransaction = new Transaction();
        sampleTransaction.setId(100L);
        sampleTransaction.setSender("771000001");
        sampleTransaction.setReceiver("772000002");
        sampleTransaction.setAmount(1500.0);
        sampleTransaction.setType(TransactionType.TRANSFERT);
        sampleTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Devrait effectuer un transfert (201 Created)")
    void shouldTransferAndReturn201() throws Exception {
        TransferRequest req = new TransferRequest("771000001", "772000002", 1500.0);
        when(walletService.transfer("771000001", "772000002", 1500.0)).thenReturn(sampleTransaction);

        mockMvc.perform(post("/transactions/transfer")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.amount").value(1500.0));
    }

    @Test
    @DisplayName("Devrait retourner 403 Forbidden si un CLIENT initie un transfert depuis un autre numéro")
    void shouldReturn403WhenClientTransfersFromOtherNumber() throws Exception {
        TransferRequest req = new TransferRequest("779999999", "772000002", 1500.0);

        mockMvc.perform(post("/transactions/transfer")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait effectuer un dépôt (201 Created)")
    void shouldDepositAndReturn201() throws Exception {
        OperationRequest req = new OperationRequest("771000001", 2000.0);
        sampleTransaction.setType(TransactionType.DEPOT);
        when(walletService.deposit("771000001", 2000.0)).thenReturn(sampleTransaction);

        mockMvc.perform(post("/transactions/deposit")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Devrait effectuer un retrait (201 Created)")
    void shouldWithdrawAndReturn201() throws Exception {
        OperationRequest req = new OperationRequest("771000001", 1000.0);
        sampleTransaction.setType(TransactionType.RETRAIT);
        when(walletService.withdraw("771000001", 1000.0)).thenReturn(sampleTransaction);

        mockMvc.perform(post("/transactions/withdraw")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Devrait retourner l'historique des transactions (200 OK)")
    void shouldGetTransactionHistory() throws Exception {
        when(walletService.getTransactionHistory("771000001")).thenReturn(List.of(sampleTransaction));

        mockMvc.perform(get("/transactions/history/771000001")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Devrait effectuer un achat de pass/crédit (201 Created)")
    void shouldPurchaseAndReturn201() throws Exception {
        PurchaseRequest req = new PurchaseRequest("771000001", "772000002", 1000.0, TransactionType.ACHAT_CREDIT, "WALLET");
        sampleTransaction.setType(TransactionType.ACHAT_CREDIT);

        when(walletService.purchase(eq("771000001"), eq("772000002"), eq(1000.0), eq(TransactionType.ACHAT_CREDIT), eq("WALLET")))
                .thenReturn(sampleTransaction);

        mockMvc.perform(post("/transactions/purchase")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Devrait retourner 400 Bad Request si le solde est insuffisant lors de l'achat")
    void shouldReturn400WhenInsufficientAmountForPurchase() throws Exception {
        PurchaseRequest req = new PurchaseRequest("771000001", "772000002", 50000.0, TransactionType.ACHAT_CREDIT, "WALLET");

        when(walletService.purchase(anyString(), anyString(), anyDouble(), any(), anyString()))
                .thenThrow(new InsufficentAmountException("Solde insuffisant"));

        mockMvc.perform(post("/transactions/purchase")
                        .header("X-User-Phone", "771000001")
                        .header("X-User-Role", "CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

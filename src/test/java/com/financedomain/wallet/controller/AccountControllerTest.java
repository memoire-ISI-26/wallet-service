package com.financedomain.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financedomain.wallet.bean.Account;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private AccountController accountController;

    private ObjectMapper objectMapper;
    private Account sampleAccount;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();

        sampleAccount = new Account();
        sampleAccount.setId(1L);
        sampleAccount.setIdUser(10L);
        sampleAccount.setNumber("771234567");
        sampleAccount.setBalance(5000.0);
        sampleAccount.setCallCredit(1000.0);
        sampleAccount.setCurrency("XOF");
    }

    @Test
    @DisplayName("Devrait créer un compte (201 Created) pour les appels Feign internes")
    void shouldCreateAccountAndReturn201() throws Exception {
        when(walletService.createAccount(any(Account.class))).thenReturn(sampleAccount);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.number").value("771234567"));
    }

    @Test
    @DisplayName("Devrait retourner 409 Conflict si le numéro de téléphone est déjà utilisé")
    void shouldReturn409ConflictWhenAccountExists() throws Exception {
        when(walletService.createAccount(any(Account.class)))
                .thenThrow(new IllegalArgumentException("Compte déjà associé"));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleAccount)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Devrait retourner 401 Unauthorized pour getAccountsByUserId sans rôle")
    void shouldReturn401WhenRoleIsNullForUserAccounts() throws Exception {
        mockMvc.perform(get("/accounts/user/10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Devrait retourner 403 Forbidden si un CLIENT demande les comptes d'un autre utilisateur")
    void shouldReturn403WhenClientRequestsOtherUserAccounts() throws Exception {
        mockMvc.perform(get("/accounts/user/99")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devrait retourner 200 OK si un CLIENT demande ses propres comptes")
    void shouldReturn200OKWhenClientRequestsOwnAccounts() throws Exception {
        when(walletService.getAccountsByUserId(10L)).thenReturn(List.of(sampleAccount));

        mockMvc.perform(get("/accounts/user/10")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Devrait récupérer un compte par numéro de téléphone (200 OK)")
    void shouldGetAccountByNumber() throws Exception {
        when(walletService.getAccountByNumber("771234567")).thenReturn(Optional.of(sampleAccount));

        mockMvc.perform(get("/accounts/number/771234567")
                        .header("X-User-Phone", "771234567")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("771234567"));
    }

    @Test
    @DisplayName("Devrait retourner le solde d'un compte (200 OK)")
    void shouldGetBalance() throws Exception {
        when(walletService.getBalance("771234567")).thenReturn(5000.0);

        mockMvc.perform(get("/accounts/number/771234567/balance")
                        .header("X-User-Phone", "771234567")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(content().string("5000.0"));
    }

    @Test
    @DisplayName("Devrait retourner 404 Not Found si le compte est introuvable lors de la demande de solde")
    void shouldReturn404WhenAccountNotFoundForBalance() throws Exception {
        when(walletService.getBalance("779999999")).thenThrow(new UnknownAccountException("Compte introuvable"));

        mockMvc.perform(get("/accounts/number/779999999/balance")
                        .header("X-User-Phone", "779999999")
                        .header("X-User-Role", "CLIENT"))
                .andExpect(status().isNotFound());
    }
}

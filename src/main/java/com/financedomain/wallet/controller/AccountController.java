package com.financedomain.wallet.controller;

import com.financedomain.wallet.bean.Account;
import com.financedomain.wallet.exception.NullBalanceDataException;
import com.financedomain.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final static String Unauthorized = "Unauthorized";
    private final static String AccessDenied = "Access Denied";
    private final static String CLIENT ="CLIENT";

    @Autowired
    private WalletService walletService;

    /**
     * Endpoint interne : création d'un compte lors de l'inscription d'un client.
     * Appelé par user-service via Feign (directement via Eureka, sans passer par le gateway).
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody Account account) {
        try {
            Account created = walletService.createAccount(account);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<?> getAccountsByUserId(
            @PathVariable long idUser,
            @RequestHeader(value = "X-User-Id", required = false) String xUserId,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (CLIENT.equals(xUserRole) && !String.valueOf(idUser).equals(xUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return ResponseEntity.ok(walletService.getAccountsByUserId(idUser));
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<?> getAccountByNumber(
            @PathVariable String number,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (CLIENT.equals(xUserRole) && !number.equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        return walletService.getAccountByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}/balance")
    public ResponseEntity<?> getBalance(
            @PathVariable String number,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Unauthorized);
        }
        if (CLIENT.equals(xUserRole) && !number.equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(AccessDenied);
        }
        try {
            double balance = walletService.getBalance(number);
            return ResponseEntity.ok(balance);
        } catch (NullBalanceDataException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

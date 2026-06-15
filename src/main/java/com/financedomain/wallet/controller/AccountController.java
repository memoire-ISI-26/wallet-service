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

    @Autowired
    private WalletService walletService;

    @GetMapping("/user/{idUser}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable long idUser) {
        return ResponseEntity.ok(walletService.getAccountsByUserId(idUser));
    }

    @GetMapping("/number/{number}")
    public ResponseEntity<?> getAccountByNumber(@PathVariable String number) {
        return walletService.getAccountByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{number}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String number) {
        try {
            double balance = walletService.getBalance(number);
            return ResponseEntity.ok(balance);
        } catch (NullBalanceDataException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

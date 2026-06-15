package com.financedomain.wallet.controller;

import com.financedomain.wallet.bean.Transaction;
import com.financedomain.wallet.dto.OperationRequest;
import com.financedomain.wallet.dto.TransferRequest;
import com.financedomain.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        try {
            Transaction txn = walletService.transfer(request.getSender(), request.getReceiver(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody OperationRequest request) {
        try {
            Transaction txn = walletService.deposit(request.getNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody OperationRequest request) {
        try {
            Transaction txn = walletService.withdraw(request.getNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(@PathVariable String number) {
        return ResponseEntity.ok(walletService.getTransactionHistory(number));
    }

}

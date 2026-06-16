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
    public ResponseEntity<?> transfer(
            @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if ("CLIENT".equals(xUserRole) && !request.getSender().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        try {
            Transaction txn = walletService.transfer(request.getSender(), request.getReceiver(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestBody OperationRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if ("CLIENT".equals(xUserRole) && !request.getNumber().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        try {
            Transaction txn = walletService.deposit(request.getNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody OperationRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if ("CLIENT".equals(xUserRole) && !request.getNumber().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        try {
            Transaction txn = walletService.withdraw(request.getNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<?> getTransactionHistory(
            @PathVariable String number,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if ("CLIENT".equals(xUserRole) && !number.equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
        }
        return ResponseEntity.ok(walletService.getTransactionHistory(number));
    }

}

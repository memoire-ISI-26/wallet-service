package com.financedomain.wallet.controller;

import com.financedomain.wallet.bean.Transaction;
import com.financedomain.wallet.dto.OperationRequest;
import com.financedomain.wallet.dto.TransferRequest;
import com.financedomain.wallet.dto.PurchaseRequest;
import com.financedomain.wallet.service.WalletService;
import com.financedomain.wallet.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String ACCESSDENIED = "Access Denied";
    private static final String CLIENT ="CLIENT";

    @Autowired
    private WalletService walletService;

    @PostMapping("/transfer")
    public ResponseEntity<Object> transfer(
            @RequestBody TransferRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !request.getSender().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            Transaction txn = walletService.transfer(request.getSender(), request.getReceiver(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (UnknownAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficentAmountException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Object> deposit(
            @RequestBody OperationRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !request.getNumber().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            Transaction txn = walletService.deposit(request.getNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (UnknownAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficentAmountException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Object> withdraw(
            @RequestBody OperationRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !request.getNumber().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            Transaction txn = walletService.withdraw(request.getNumber(), request.getAmount());
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (UnknownAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficentAmountException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<Object> getTransactionHistory(
            @PathVariable String number,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !number.equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        return ResponseEntity.ok(walletService.getTransactionHistory(number));
    }

    @PostMapping("/purchase")
    public ResponseEntity<Object> purchase(
            @RequestBody PurchaseRequest request,
            @RequestHeader(value = "X-User-Phone", required = false) String xUserPhone,
            @RequestHeader(value = "X-User-Role", required = false) String xUserRole) {
        if (xUserRole == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED);
        }
        if (CLIENT.equals(xUserRole) && !request.getSender().equals(xUserPhone)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ACCESSDENIED);
        }
        try {
            Transaction txn = walletService.purchase(
                    request.getSender(),
                    request.getReceiver(),
                    request.getAmount(),
                    request.getType(),
                    request.getPaymentMethod()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(txn);
        } catch (UnknownAccountException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficentAmountException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

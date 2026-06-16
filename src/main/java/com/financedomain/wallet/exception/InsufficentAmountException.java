package com.financedomain.wallet.exception;

public class InsufficentAmountException extends RuntimeException {
    public InsufficentAmountException(String message) {
        super(message);
    }
}

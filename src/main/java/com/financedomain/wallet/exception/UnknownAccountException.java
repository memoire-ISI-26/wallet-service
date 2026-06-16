package com.financedomain.wallet.exception;

public class UnknownAccountException extends RuntimeException {
    public UnknownAccountException(String message) {
        super(message);
    }
}

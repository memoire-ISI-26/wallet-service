package com.financedomain.wallet.exception;

public class NullBalanceDataException extends RuntimeException {
    public NullBalanceDataException(String message) {
        super(message);
    }
}

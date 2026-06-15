package com.financedomain.wallet.dto;

import lombok.*;

@Getter
@Setter
public class OperationRequest {

    private String number;
    private double amount;
}

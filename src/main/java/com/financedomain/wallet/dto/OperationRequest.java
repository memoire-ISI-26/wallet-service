package com.financedomain.wallet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationRequest {

    private String number;
    private double amount;
}

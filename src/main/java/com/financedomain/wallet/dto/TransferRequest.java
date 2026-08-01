package com.financedomain.wallet.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    private String sender;
    private String receiver;
    private double amount;
}

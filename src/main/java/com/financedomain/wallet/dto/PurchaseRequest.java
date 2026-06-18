package com.financedomain.wallet.dto;

import com.financedomain.wallet.enums.TransactionType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseRequest {

    private String sender;
    private String receiver;
    private double amount;
    private TransactionType type;
}

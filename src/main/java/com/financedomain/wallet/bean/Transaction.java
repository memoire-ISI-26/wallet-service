package com.financedomain.wallet.bean;

import com.financedomain.wallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaction")
    private long id;

    @Column(name = "expediteur", nullable = false)
    private String sender;

    @Column(name = "receveur", nullable = false)
    private String receiver;

    @Column(name = "montant")
    private double amount;

    @Column(name = "transaction")
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "cree a")
    private LocalDateTime createdAt;
}

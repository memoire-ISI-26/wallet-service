package com.financedomain.wallet.bean;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compte")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id_account;

    private long id_user;

    @Column(name = "telephone", unique = true, nullable = false, length = 12)
    private String number;

    private double balance;

    @Column(name = "devise")
    private String currency;
}

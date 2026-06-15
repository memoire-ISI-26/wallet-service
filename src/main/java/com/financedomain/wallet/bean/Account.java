package com.financedomain.wallet.bean;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    private Long id;

    @JsonProperty("id_user")
    @Column(name = "id_user")
    private Long idUser;

    @Column(name = "telephone", unique = true, nullable = false, length = 12)
    private String number;

    private Double balance;

    @Column(name = "devise")
    private String currency;
}

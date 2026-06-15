package com.financedomain.wallet.repository;

import com.financedomain.wallet.bean.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByNumber(String number);
    
    boolean existsByNumber(String number);

    List<Account> findByIdUser(Long idUser);
}

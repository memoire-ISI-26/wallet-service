package com.financedomain.wallet.repository;

import com.financedomain.wallet.bean.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByNumber(String number);
    
    boolean existsByNumber(String number);

    @Query("SELECT a FROM Account a WHERE a.id_user = :idUser")
    List<Account> findByIdUser(@Param("idUser") long idUser);
}

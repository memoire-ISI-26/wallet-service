package com.financedomain.wallet.repository;

import com.financedomain.wallet.bean.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findBySenderOrReceiverOrderByCreatedAtDesc(String sender, String receiver);
}

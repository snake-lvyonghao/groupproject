package com.comp5348.bank.repository;

import com.comp5348.bank.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, String> {
    Optional<TransactionRecord> findById(Long id);

}

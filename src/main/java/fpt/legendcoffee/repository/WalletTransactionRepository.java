package fpt.legendcoffee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fpt.legendcoffee.entity.WalletTransaction;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
}
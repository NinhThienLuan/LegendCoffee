package fpt.legendcoffee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fpt.legendcoffee.entity.WalletTransaction;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
}
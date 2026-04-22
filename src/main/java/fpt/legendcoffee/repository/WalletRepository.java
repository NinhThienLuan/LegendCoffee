package fpt.legendcoffee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fpt.legendcoffee.entity.Wallet;
import jakarta.persistence.LockModeType;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // 1. Truy vấn cơ bản (Không khóa) - Dùng để xem thông tin ví thông thường
    Optional<Wallet> findByUserId(Long userId);

    // 2. Truy vấn có khóa cho Ví của User - Dùng khi thực hiện CỘNG/TRỪ tiền
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserIdWithLock(@Param("userId") Long userId);

    // 3. Truy vấn có khóa cho Ví Admin (Tìm theo ID của ví) - Dùng khi CỘNG/TRỪ tiền Admin
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") Long id);

}
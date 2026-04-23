package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.WithdrawalRequest;
import fpt.legendcoffee.entity.enumeration.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<WithdrawalRequest> findByStatusOrderByCreatedAtAsc(WithdrawalStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT wr FROM WithdrawalRequest wr JOIN FETCH wr.user ORDER BY wr.createdAt DESC")
    List<WithdrawalRequest> findAllWithUserOrderByCreatedAtDesc();

    List<WithdrawalRequest> findAllByOrderByCreatedAtDesc();

    /** Kiểm tra user có yêu cầu PENDING nào chưa xử lý không */
    Optional<WithdrawalRequest> findByUserIdAndStatus(Long userId, WithdrawalStatus status);

    boolean existsByUserIdAndStatus(Long userId, WithdrawalStatus status);
}

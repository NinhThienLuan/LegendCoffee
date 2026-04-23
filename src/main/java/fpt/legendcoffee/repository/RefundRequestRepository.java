//package fpt.legendcoffee.repository;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import fpt.legendcoffee.entity.RefundRequest;
//import fpt.legendcoffee.entity.enumeration.RefundStatus;
//
//public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
//    List<RefundRequest> findByStatus(RefundStatus status);
//    boolean existsByOrderIdAndStatus(Long orderId, RefundStatus status);
//}
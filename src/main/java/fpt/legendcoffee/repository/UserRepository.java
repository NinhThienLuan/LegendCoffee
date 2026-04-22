package fpt.legendcoffee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fpt.legendcoffee.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersSince(@org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate);
}

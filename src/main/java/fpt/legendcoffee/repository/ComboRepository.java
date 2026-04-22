package fpt.legendcoffee.repository;

import fpt.legendcoffee.entity.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<Combo> findByNameIgnoreCase(String name);
}


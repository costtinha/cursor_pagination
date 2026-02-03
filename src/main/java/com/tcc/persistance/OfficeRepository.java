package com.tcc.persistance;

import com.tcc.entity.Office;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("OfficeJpaRepository")
public interface OfficeRepository extends JpaRepository<Office,Integer> {
    Office findOfficeByEmail(String email);
    Boolean existsByEmail(String email);
    List<Office> findByIdGreaterThanOrderByIdAsc(
            Integer id,
            Pageable pageable
    );
    List<Office> findByIdLessThanOrderByIdDesc(Integer id, Pageable pageable);

}

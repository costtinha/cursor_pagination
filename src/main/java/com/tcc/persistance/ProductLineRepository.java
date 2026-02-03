package com.tcc.persistance;

import com.tcc.entity.ProductLine;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ProductLineJpaRepository")
public interface ProductLineRepository extends JpaRepository<ProductLine, Integer> {
    List<ProductLine> findByIdGreaterThanOrderByIdAsc(Integer lastId, Pageable pageable);
    List<ProductLine> findByIdLessThanOrderByIdDesc(Integer lastId, Pageable pageable);
}

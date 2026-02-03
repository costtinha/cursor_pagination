package com.tcc.persistance;

import com.tcc.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ProductJpaRepository")
public interface ProductRepository extends JpaRepository<Product,Integer> {
    List<Product> findByIdGreaterThanOrderByIdAsc(Integer lastId, Pageable pageable);
    List<Product> findByIdLessThanOrderByIdDesc(Integer lastId, Pageable pageable);
}

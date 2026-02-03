package com.tcc.persistance;

import com.tcc.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository("OrderJpaRepository")
public interface OrderRepository extends JpaRepository<Order,Integer> {
    List<Order> findByIdGreaterThanOrderByIdAsc(Integer lastId, Pageable pageable);
    List<Order> findByIdLessThanOrderByIdDesc(Integer lastId, Pageable pageable);
}

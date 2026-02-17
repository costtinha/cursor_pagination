package com.tcc.persistance;

import com.tcc.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository("OrderJpaRepository")
public interface OrderRepository extends JpaRepository<Order,Integer> {
    List<Order> findByOrderIdGreaterThanOrderByOrderIdAsc(Integer lastId, Pageable pageable);
    List<Order> findByOrderIdLessThanOrderByOrderIdDesc(Integer lastId, Pageable pageable);
}

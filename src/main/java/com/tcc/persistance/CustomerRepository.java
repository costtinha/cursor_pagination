package com.tcc.persistance;

import com.tcc.entity.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("CustomerJpaRepository")
public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    List<Customer> findByIdGreaterThanOrderByIdAsc(Integer lastId,
                                                 Pageable pageable);
    List<Customer> findByIdLessThanOrderByIdDesc(Integer lastId, Pageable pageable);
}

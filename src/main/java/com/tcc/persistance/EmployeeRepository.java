package com.tcc.persistance;

import com.tcc.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("EmployeeJpaRepository")
public interface EmployeeRepository extends JpaRepository<Employee,Integer> {
    Boolean existsByEmail(String email);
    List<Employee> findByEmployeeIdGreaterThanOrderByEmployeeIdAsc(Integer lastId, Pageable pageable);
    List<Employee> findByEmployeeIdLessThanOrderByEmployeeIdDesc(Integer id, Pageable pageable);
}

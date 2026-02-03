package com.tcc.cache;

import com.tcc.entity.EmployeeCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("EmployeeRedisRepository")
public interface EmployeeCacheRepository extends CrudRepository<EmployeeCache,Integer> {
}

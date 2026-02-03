package com.tcc.cache;

import com.tcc.entity.CustomerCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("CustomerRedisRepository")
public interface CustomerCacheRepository extends CrudRepository<CustomerCache,Integer> {
}

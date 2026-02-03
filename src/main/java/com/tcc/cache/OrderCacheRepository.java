package com.tcc.cache;

import com.tcc.entity.OrderCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("OrderRedisRepository")
public interface OrderCacheRepository extends CrudRepository<OrderCache,Integer> {
}

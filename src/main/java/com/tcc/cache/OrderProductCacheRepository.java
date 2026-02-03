package com.tcc.cache;

import com.tcc.entity.OrderProductCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("OrderProductCacheRepository")
public interface OrderProductCacheRepository extends CrudRepository<OrderProductCache,String> {
}

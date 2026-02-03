package com.tcc.cache;

import com.tcc.entity.ProductCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("ProductCacheRepository")
public interface ProductCacheRepository extends CrudRepository<ProductCache,Integer> {
}

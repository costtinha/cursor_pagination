package com.tcc.cache;

import com.tcc.entity.ProductLine;
import com.tcc.entity.ProductLineCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("productLineRedisRepository")
public interface ProductLineCacheRepository extends CrudRepository<ProductLineCache,Integer> {
}

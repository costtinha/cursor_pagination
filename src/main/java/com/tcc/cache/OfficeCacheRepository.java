package com.tcc.cache;

import com.tcc.entity.OfficeCache;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository("OfficeRedisRepository")
public interface OfficeCacheRepository extends CrudRepository<OfficeCache,Integer> {
}

package com.tcc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.tcc.persistance")
@EnableRedisRepositories(basePackages = "com.tcc.cache")
public class RepositoryConfig {
}

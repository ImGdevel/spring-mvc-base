package com.spring.mvc.base.infra.redis.repository;

import com.spring.mvc.base.infra.redis.domain.RedisExampleEntity;
import org.springframework.data.repository.CrudRepository;

public interface RedisExampleRepository extends CrudRepository<RedisExampleEntity, String> {

}

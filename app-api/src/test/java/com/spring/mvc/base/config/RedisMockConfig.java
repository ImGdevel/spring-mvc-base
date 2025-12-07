package com.spring.mvc.base.config;

import com.spring.mvc.base.fake.FakeRedisService;
import com.spring.mvc.base.infra.redis.adapter.RedisService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class RedisMockConfig {

    @Bean
    @Primary
    public RedisService testRedisService(){
        return new FakeRedisService();
    }

}

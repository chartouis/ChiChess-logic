package com.chitas.chesslogic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Configuration
@Log4j2
public class JedisConfig {

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public Jedis jedis() {
        return new Jedis("localhost", 6379);
    }

    // @Bean
    // public JedisPool jedisPool() {
    //     return new JedisPool("localhost", 6379);
    // }
}

package com.chitas.chesslogic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.Jedis;
@Configuration
public class JedisConfig {

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("server", 6379);
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

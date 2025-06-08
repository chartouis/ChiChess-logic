package com.chitas.chesslogic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.chitas.chesslogic.grpc.RoomGrpcController;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import redis.clients.jedis.Jedis;

@Configuration
@Log4j2
public class Config {

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("server", 6379);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public Jedis jedis() {
        return new Jedis("localhost", 6379);
    }

    @Autowired
    private RoomGrpcController roomGrpcController;

    @Bean
    public Server grpcServer() {
        return ServerBuilder.forPort(4967)
            .addService(roomGrpcController)
            .addService(ProtoReflectionService.newInstance())
            .build();
    }

    @Bean
    public CommandLineRunner startGrpcServer(Server grpcServer) {
        return _ -> {
            grpcServer.start();
            log.info("gRPC Server started on port 4967");
        };
    }

    @Bean
    public GrpcAuthenticationReader grpcAuthenticationReader() {
        return new BasicGrpcAuthenticationReader();
    }
}

package com.chitas.chesslogic.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chitas.chesslogic.grpc.RoomGrpcController;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.security.authentication.BasicGrpcAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;

@Configuration
@Log4j2
public class GrpcConfig {

    private RoomGrpcController roomGrpcController;

    public GrpcConfig(RoomGrpcController roomGrpcController) {
        this.roomGrpcController = roomGrpcController;
    }

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

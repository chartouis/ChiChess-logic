package com.chitas.chesslogic.grpc;

import com.chitas.chesslogic.model.RoomState;
import com.chitas.chesslogic.service.ChessService;
import com.chitas.grpc.CreateRoomRequest;
import com.chitas.grpc.JoinRoomRequest;
import com.chitas.grpc.RoomResponse;
import com.chitas.grpc.RoomServiceGrpc.RoomServiceImplBase;

import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Log4j2
public class RoomGrpcController extends RoomServiceImplBase {

    private final ChessService service;

    public RoomGrpcController(ChessService service) {
        this.service = service;
    }

    @Override
    public void createRoom(CreateRoomRequest request, StreamObserver<RoomResponse> responseObserver) {
        log.info("gRPC: createRoom");
        RoomState room = service.createRoom(
                request.getCreator(), request.getWhite(), request.getBlack());

        RoomResponse response = RoomResponse.newBuilder()
                .setRoomId(room.getId())
                .setStatus("created")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void joinRoom(JoinRoomRequest request, StreamObserver<RoomResponse> responseObserver) {
        log.info("gRPC: joinRoom");
        RoomState room = service.joinRoom(request.getRoomId(), request.getVisitor());

        RoomResponse response = RoomResponse.newBuilder()
                .setRoomId(room.getId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

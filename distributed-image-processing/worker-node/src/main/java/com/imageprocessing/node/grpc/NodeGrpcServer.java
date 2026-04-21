package com.imageprocessing.node.grpc;

import com.imageprocessing.node.config.NodeProperties;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeGrpcServer {

    private final NodeServiceImpl nodeService;
    private final NodeProperties props;
    private Server server;

    public void start() throws IOException {
        server = ServerBuilder.forPort(props.getGrpcPort())
            .addService(nodeService)
            .build()
            .start();
        log.info("gRPC Server escuchando en puerto {}", props.getGrpcPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Deteniendo servidor gRPC...");
            if (server != null) server.shutdown();
        }));
    }
}

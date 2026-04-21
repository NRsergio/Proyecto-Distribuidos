package com.imageprocessing.node;

import com.imageprocessing.node.grpc.NodeGrpcServer;
import com.imageprocessing.node.config.NodeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@EnableConfigurationProperties(NodeProperties.class)
public class WorkerNodeApplication implements CommandLineRunner {

    private final NodeGrpcServer grpcServer;
    private final NodeProperties props;

    public static void main(String[] args) {
        SpringApplication.run(WorkerNodeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Iniciar servidor gRPC
        grpcServer.start();
        log.info("Nodo {} iniciado en puerto gRPC {}", props.getId(), props.getGrpcPort());

        // Registrarse en el servidor de aplicacion
        registrarseEnServidor();
    }

    private void registrarseEnServidor() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> body = Map.of(
                "nombre", props.getId(),
                "direccionRed", "localhost:" + props.getGrpcPort()
            );
            restTemplate.postForObject(
                props.getAppServerUrl() + "/api/nodos/registrar",
                body, Map.class
            );
            log.info("Nodo registrado exitosamente en {}", props.getAppServerUrl());
        } catch (Exception e) {
            log.warn("No se pudo registrar en el servidor: {}", e.getMessage());
        }
    }
}

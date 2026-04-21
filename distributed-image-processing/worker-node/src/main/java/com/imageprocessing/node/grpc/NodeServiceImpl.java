package com.imageprocessing.node.grpc;

import com.imageprocessing.grpc.*;
import com.imageprocessing.node.processing.ImageProcessor;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NodeServiceImpl extends NodeServiceGrpc.NodeServiceImplBase {

    private final ImageProcessor imageProcessor;

    @Override
    public void processImage(ImageJobRequest request,
                             StreamObserver<ImageJobResponse> responseObserver) {
        log.info("Trabajo recibido: imagen={}, lote={}", request.getIdImagen(), request.getIdLote());

        // Aceptar inmediatamente y procesar de forma asincrona con hilos
        imageProcessor.procesarAsync(request);

        responseObserver.onNext(ImageJobResponse.newBuilder()
            .setAceptado(true)
            .setMensaje("Trabajo aceptado y en cola de procesamiento")
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void heartbeat(HeartbeatRequest request,
                          StreamObserver<HeartbeatResponse> responseObserver) {
        responseObserver.onNext(HeartbeatResponse.newBuilder()
            .setActivo(true)
            .setCargaActual(imageProcessor.getCargaActual())
            .build());
        responseObserver.onCompleted();
    }
}

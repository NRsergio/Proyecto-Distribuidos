package com.imageprocessing.server.grpc;

import com.imageprocessing.grpc.*;
import com.imageprocessing.server.model.entity.ImagenSolicitud;
import com.imageprocessing.server.model.entity.NodoTrabajador;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NodeGrpcClient {

    public void enviarTrabajoANodo(NodoTrabajador nodo, ImagenSolicitud imagen, String callbackUrl) {
        String[] partes = nodo.getDireccionRed().split(":");
        String host = partes[0];
        int port = partes.length > 1 ? Integer.parseInt(partes[1]) : 9090;

        ManagedChannel channel = ManagedChannelBuilder
            .forAddress(host, port)
            .usePlaintext()
            .build();

        try {
            NodeServiceGrpc.NodeServiceBlockingStub stub = NodeServiceGrpc.newBlockingStub(channel);

            List<TransformacionConfig> transformaciones = imagen.getTransformaciones().stream()
                .map(t -> TransformacionConfig.newBuilder()
                    .setTipo(t.getTipo())
                    .setOrden(t.getOrden())
                    .setParametros(t.getParametros() != null ? t.getParametros() : "{}")
                    .build())
                .collect(Collectors.toList());

            ImageJobRequest request = ImageJobRequest.newBuilder()
                .setIdImagen(imagen.getIdImagen())
                .setIdLote(imagen.getLote().getIdLote())
                .setRutaOriginal(imagen.getRutaOriginal())
                .setFormatoSalida("PNG")
                .addAllTransformaciones(transformaciones)
                .setCallbackUrl(callbackUrl + "/api/callback/imagen")
                .build();

            ImageJobResponse response = stub
                .withDeadlineAfter(30, TimeUnit.SECONDS)
                .processImage(request);

            if (!response.getAceptado()) {
                log.error("Nodo {} rechazo la imagen {}: {}",
                    nodo.getNombre(), imagen.getIdImagen(), response.getMensaje());
            }
        } catch (Exception e) {
            log.error("Error gRPC al enviar imagen {} al nodo {}: {}",
                imagen.getIdImagen(), nodo.getNombre(), e.getMessage());
        } finally {
            channel.shutdown();
        }
    }
}

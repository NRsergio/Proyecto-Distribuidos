package com.imageprocessing.node.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final RestTemplate restTemplate;

    public void notificarExito(String callbackUrl, long idImagen, String rutaResultado, long tiempoMs) {
        Map<String, Object> body = new HashMap<>();
        body.put("idImagen", idImagen);
        body.put("exitoso", true);
        body.put("rutaResultado", rutaResultado);
        body.put("tiempoEjecucionMs", tiempoMs);

        enviarCallback(callbackUrl, body);
        log.info("Callback exito enviado para imagen {}", idImagen);
    }

    public void notificarError(String callbackUrl, long idImagen, String mensajeError) {
        Map<String, Object> body = new HashMap<>();
        body.put("idImagen", idImagen);
        body.put("exitoso", false);
        body.put("mensajeError", mensajeError);

        enviarCallback(callbackUrl, body);
        log.warn("Callback error enviado para imagen {}: {}", idImagen, mensajeError);
    }

    private void enviarCallback(String url, Map<String, Object> body) {
        try {
            restTemplate.postForObject(url, body, Void.class);
        } catch (Exception e) {
            log.error("Error enviando callback a {}: {}", url, e.getMessage());
        }
    }
}

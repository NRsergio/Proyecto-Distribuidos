package com.imageprocessing.node.processing.transformations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imgscalr.Scalr;
import java.awt.image.BufferedImage;

public class RedimensionarTransformation {

    private static final ObjectMapper mapper = new ObjectMapper();

    // parametros JSON esperado: {"ancho": 800, "alto": 600}
    public static BufferedImage apply(BufferedImage original, String parametrosJson) {
        try {
            JsonNode params = mapper.readTree(parametrosJson);
            int ancho = params.get("ancho").asInt();
            int alto = params.get("alto").asInt();
            return Scalr.resize(original, Scalr.Method.QUALITY, Scalr.Mode.FIT_EXACT, ancho, alto);
        } catch (Exception e) {
            throw new RuntimeException("Error al redimensionar: " + e.getMessage(), e);
        }
    }
}

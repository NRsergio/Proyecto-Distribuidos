package com.imageprocessing.node.processing.transformations;

import java.awt.image.BufferedImage;

/**
 * TODO: Implementar logica de transformacion.
 * parametros: JSON con configuracion especifica.
 */
public class NitidezTransformation {

    public static BufferedImage apply(BufferedImage original, String parametrosJson) {
        // TODO: implementar
        return original;
    }

    // Sobrecarga sin parametros
    public static BufferedImage apply(BufferedImage original) {
        return apply(original, "{}");
    }
}

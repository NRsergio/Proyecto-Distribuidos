package com.imageprocessing.node.processing.transformations;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EscalaGrisesTransformation {

    public static BufferedImage apply(BufferedImage original) {
        BufferedImage resultado = new BufferedImage(
            original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resultado.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return resultado;
    }
}

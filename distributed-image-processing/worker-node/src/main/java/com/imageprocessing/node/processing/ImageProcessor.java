package com.imageprocessing.node.processing;

import com.imageprocessing.grpc.ImageJobRequest;
import com.imageprocessing.grpc.TransformacionConfig;
import com.imageprocessing.node.callback.CallbackService;
import com.imageprocessing.node.config.NodeProperties;
import com.imageprocessing.node.processing.transformations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class ImageProcessor {

    // ── Pool de hilos para procesamiento paralelo (requisito del proyecto) ──
    private final ExecutorService executorService;
    private final AtomicInteger cargaActual = new AtomicInteger(0);
    private final CallbackService callbackService;
    private final NodeProperties props;

    public ImageProcessor(NodeProperties props, CallbackService callbackService) {
        this.props = props;
        this.callbackService = callbackService;
        this.executorService = Executors.newFixedThreadPool(props.getThreadPoolSize());
    }

    public void procesarAsync(ImageJobRequest request) {
        executorService.submit(() -> procesarImagen(request));
    }

    public int getCargaActual() {
        return cargaActual.get();
    }

    private void procesarImagen(ImageJobRequest request) {
        cargaActual.incrementAndGet();
        long inicio = System.currentTimeMillis();

        try {
            BufferedImage imagen = ImageIO.read(new File(request.getRutaOriginal()));

            // Aplicar transformaciones en orden definido
            List<TransformacionConfig> transformaciones = request.getTransformacionesList()
                .stream()
                .sorted(Comparator.comparingInt(TransformacionConfig::getOrden))
                .toList();

            for (TransformacionConfig t : transformaciones) {
                imagen = aplicarTransformacion(imagen, t);
                log.debug("Transformacion {} aplicada a imagen {}", t.getTipo(), request.getIdImagen());
            }

            // Guardar resultado
            String rutaResultado = guardarResultado(imagen, request);
            long tiempoMs = System.currentTimeMillis() - inicio;

            callbackService.notificarExito(request.getCallbackUrl(), request.getIdImagen(),
                rutaResultado, tiempoMs);

        } catch (Exception e) {
            log.error("Error procesando imagen {}: {}", request.getIdImagen(), e.getMessage());
            callbackService.notificarError(request.getCallbackUrl(), request.getIdImagen(),
                e.getMessage());
        } finally {
            cargaActual.decrementAndGet();
        }
    }

    private BufferedImage aplicarTransformacion(BufferedImage imagen, TransformacionConfig config) {
        return switch (config.getTipo().toUpperCase()) {
            case "ESCALA_GRISES"      -> EscalaGrisesTransformation.apply(imagen);
            case "REDIMENSIONAR"      -> RedimensionarTransformation.apply(imagen, config.getParametros());
            case "RECORTAR"           -> RecortarTransformation.apply(imagen, config.getParametros());
            case "ROTAR"              -> RotarTransformation.apply(imagen, config.getParametros());
            case "REFLEJAR"           -> ReflejarTransformation.apply(imagen, config.getParametros());
            case "DESENFOCAR"         -> DesenfocatTransformation.apply(imagen, config.getParametros());
            case "NITIDEZ"            -> NitidezTransformation.apply(imagen);
            case "BRILLO_CONTRASTE"   -> BrilloContrasteTransformation.apply(imagen, config.getParametros());
            case "MARCA_DE_AGUA"      -> MarcaDeAguaTransformation.apply(imagen, config.getParametros());
            case "CONVERSION_FORMATO" -> imagen; // formato se maneja al guardar
            default -> throw new IllegalArgumentException("Transformacion desconocida: " + config.getTipo());
        };
    }

    private String guardarResultado(BufferedImage imagen, ImageJobRequest request) throws Exception {
        File directorio = new File(props.getStoragePath() + "/resultados/" + request.getIdLote());
        directorio.mkdirs();

        String formato = request.getFormatoSalida().toUpperCase();
        String ruta = directorio.getAbsolutePath() + "/" + request.getIdImagen() + "." + formato.toLowerCase();

        ImageIO.write(imagen, formato, new File(ruta));
        return ruta;
    }
}

package com.imageprocessing.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        List<Map<String, String>> resultado = new ArrayList<>();

        try {
            File dir = new File(uploadDir);
            dir.mkdirs();

            for (MultipartFile file : files) {
                String extension = "";
                String originalName = file.getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    extension = originalName.substring(originalName.lastIndexOf("."));
                }

                // Nombre unico para evitar colisiones
                String storedName = UUID.randomUUID() + extension;
                Path destino = Paths.get(uploadDir, storedName);
                Files.copy(file.getInputStream(), destino);

                log.info("Archivo guardado: {}", destino);
                resultado.add(Map.of(
                    "nombreOriginal", originalName != null ? originalName : storedName,
                    "rutaAlmacenada", destino.toAbsolutePath().toString(),
                    "storedName", storedName
                ));
            }

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al subir archivos: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error al guardar los archivos: " + e.getMessage()));
        }
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("ruta") String rutaResultado) {
        try {
            Path path = Paths.get(rutaResultado);
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            byte[] data = Files.readAllBytes(path);
            String filename = path.getFileName().toString();
            String contentType = filename.endsWith(".png") ? "image/png"
                : filename.endsWith(".jpg") || filename.endsWith(".jpeg") ? "image/jpeg"
                : "application/octet-stream";

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", contentType)
                .body(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

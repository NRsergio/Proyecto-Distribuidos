package com.imageprocessing.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación Spring Boot
 * App-Server v2.0 - Procesamiento distribuido de imágenes con RMI y HTTP REST
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.imageprocessing.server")
@EnableScheduling
public class AppServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppServerApplication.class, args);
    }
}

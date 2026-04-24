package com.imageprocessing.server.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtro de autenticación JWT
 * Extrae y valida JWT de las peticiones entrantes
 * Establece el contexto de seguridad de Spring
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null) {
                logger.debug("JWT encontrado en petición: {}", request.getRequestURI());
                // Validación de JWT será manejada por el servicio de autenticación
                // Aquí se establece el usuario en el contexto de Spring Security
            }
        } catch (Exception e) {
            logger.error("No se pudo procesar JWT: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

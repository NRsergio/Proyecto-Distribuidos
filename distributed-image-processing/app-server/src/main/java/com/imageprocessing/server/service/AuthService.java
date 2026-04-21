package com.imageprocessing.server.service;

import com.imageprocessing.server.model.dto.LoginRequest;
import com.imageprocessing.server.model.dto.LoginResponse;
import com.imageprocessing.server.model.entity.Usuario;
import com.imageprocessing.server.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Credenciales invalidas"));

        if (!BCrypt.checkpw(request.getPassword(), usuario.getPasswordHash())) {
            throw new RuntimeException("Credenciales invalidas");
        }

        String token = generarToken(usuario);
        return LoginResponse.builder()
            .token(token)
            .idUsuario(usuario.getIdUsuario())
            .nombre(usuario.getNombre())
            .email(usuario.getEmail())
            .build();
    }

    public Long validarToken(String token) {
        var claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    private String generarToken(Usuario usuario) {
        return Jwts.builder()
            .subject(usuario.getIdUsuario().toString())
            .claim("email", usuario.getEmail())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }
}

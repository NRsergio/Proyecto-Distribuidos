package com.imageprocessing.server.service;

import com.imageprocessing.server.model.dto.AuthResponse;
import com.imageprocessing.server.model.dto.LoginRequest;
import com.imageprocessing.server.model.dto.RegisterRequest;
import com.imageprocessing.server.model.entity.Sesion;
import com.imageprocessing.server.model.entity.Usuario;
import com.imageprocessing.server.repository.SesionRepository;
import com.imageprocessing.server.repository.UsuarioRepository;
import com.imageprocessing.server.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Servicio de Autenticación
 * Maneja login, registro y validación de sesiones
 * Utiliza BD para persistencia de sesiones
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_ROLE = "USER";
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Login de usuario
     * @param request datos de login
     * @return AuthResponse con token y datos del usuario
     */
    public AuthResponse login(LoginRequest request) {
        String identificador = obtenerIdentificadorLogin(request);
        logger.info("Intento de login para usuario: {}", identificador);
        
        // Buscar usuario por username o email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(identificador);
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.findByEmail(identificador);
        }
        
        if (usuarioOpt.isEmpty()) {
            logger.warn("Usuario no encontrado: {}", identificador);
            throw new RuntimeException("Usuario o contraseña inválidos");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            logger.warn("Contraseña incorrecta para usuario: {}", usuario.getUsername());
            throw new RuntimeException("Usuario o contraseña inválidos");
        }
        
        // Generar JWT
        String token = jwtTokenProvider.generateToken(usuario.getUsername(), usuario.getIdUsuario().toString());
        
        // Crear sesión en BD
        Sesion sesion = Sesion.builder()
            .usuario(usuario)
            .token(token)
            .fechaExpiracion(LocalDateTime.now().plusHours(24))
            .dispositivoInfo("app-server")
            .build();
        
        sesionRepository.save(sesion);
        logger.info("Login exitoso para usuario: {}", usuario.getUsername());
        
        return AuthResponse.builder()
            .token(token)
            .idUsuario(usuario.getIdUsuario())
            .username(usuario.getUsername())
            .email(usuario.getEmail())
                .rol(DEFAULT_ROLE)
            .build();
    }
    
    /**
     * Registro de nuevo usuario
     * @param request datos de registro
     * @return AuthResponse con token y datos del usuario creado
     */
    public AuthResponse register(RegisterRequest request) {
        String username = obtenerUsernameRegistro(request);
        logger.info("Intento de registro para usuario: {}", username);
        
        // Validar que el usuario no exista
        if (usuarioRepository.existsByUsername(username)) {
            logger.warn("Username ya existe: {}", username);
            throw new RuntimeException("El username ya existe");
        }
        
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email ya existe: {}", request.getEmail());
            throw new RuntimeException("El email ya existe");
        }
        
        // Crear nuevo usuario
        Usuario usuario = Usuario.builder()
            .username(username)
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fechaRegistro(LocalDateTime.now())
            .build();
        
        usuario = usuarioRepository.save(usuario);
        logger.info("Usuario registrado exitosamente: {}", usuario.getUsername());
        
        // Generar JWT y crear sesión
        String token = jwtTokenProvider.generateToken(usuario.getUsername(), usuario.getIdUsuario().toString());
        
        Sesion sesion = Sesion.builder()
            .usuario(usuario)
            .token(token)
            .fechaExpiracion(LocalDateTime.now().plusHours(24))
            .dispositivoInfo("app-server")
            .build();
        
        sesionRepository.save(sesion);
        
        return AuthResponse.builder()
            .token(token)
            .idUsuario(usuario.getIdUsuario())
            .username(usuario.getUsername())
            .email(usuario.getEmail())
                .rol(DEFAULT_ROLE)
            .build();
    }
    
    /**
     * Valida un token JWT y verifica que exista una sesión activa en BD
     * @param token token JWT
     * @return true si el token es válido y la sesión está activa
     */
    public boolean validarToken(String token) {
        try {
            // Validar firma JWT
            if (!jwtTokenProvider.validateToken(token)) {
                logger.warn("Token JWT inválido");
                return false;
            }
            
            // Validar que la sesión existe en BD
            Optional<Sesion> sesion = sesionRepository.findByToken(token);
            if (sesion.isEmpty()) {
                logger.warn("Sesión no encontrada en BD");
                return false;
            }
            
            Sesion s = sesion.get();

            // Validar que no haya expirado
            if (LocalDateTime.now().isAfter(s.getFechaExpiracion())) {
                logger.warn("Sesión expirada");
                sesionRepository.delete(s);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene el usuario asociado a un token
     * @param token token JWT
     * @return Usuario si existe, optionalvacío si no
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerUsuarioDelToken(String token) {
        Optional<Sesion> sesion = sesionRepository.findByToken(token);
        return sesion.map(Sesion::getUsuario);
    }
    
    /**
     * Cierra la sesión del usuario
     * @param token token JWT
     */
    public void logout(String token) {
        Optional<Sesion> sesion = sesionRepository.findByToken(token);
        sesion.ifPresent(s -> {
            sesionRepository.delete(s);
            logger.info("Logout exitoso");
        });
    }

    private String obtenerIdentificadorLogin(LoginRequest request) {
        String identificador = request.getUsername();
        if (identificador == null || identificador.isBlank()) {
            identificador = request.getEmail();
        }

        if (identificador == null || identificador.isBlank()) {
            throw new RuntimeException("Debe enviar username o email");
        }

        return identificador.trim();
    }

    private String obtenerUsernameRegistro(RegisterRequest request) {
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return request.getUsername().trim();
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            return request.getNombre().trim();
        }

        if (request.getEmail() != null && request.getEmail().contains("@")) {
            return request.getEmail().substring(0, request.getEmail().indexOf('@')).trim();
        }

        throw new RuntimeException("Debe enviar username, nombre o email válido");
    }
}

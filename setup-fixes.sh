#!/usr/bin/env bash
# =============================================================================
# FIXES CRITICOS PARA AVANCE 2
# Ejecutar desde la raiz del repo (donde está distributed-image-processing/)
# =============================================================================
set -e

ROOT="distributed-image-processing"
SERVER_SRC="$ROOT/app-server/src/main/java/com/imageprocessing/server"

echo "Aplicando fixes para Avance 2..."

# ─── FIX 1: Agregar spring-security-crypto al pom del app-server ─────────────
# Busca la linea del PostgreSQL dependency y agrega security-crypto antes
python3 - << 'PYEOF'
import re, sys

path = "distributed-image-processing/app-server/pom.xml"
with open(path) as f:
    content = f.read()

new_dep = """
        <!-- BCrypt para hashing de contrasenas -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
"""

# Insertar antes de la dependencia de PostgreSQL
marker = "        <!-- PostgreSQL -->"
if "spring-boot-starter-security" not in content:
    content = content.replace(marker, new_dep + marker)
    with open(path, 'w') as f:
        f.write(content)
    print("[OK] spring-boot-starter-security agregado")
else:
    print("[SKIP] security ya estaba")
PYEOF

# ─── FIX 2: application.yml — ddl-auto update + CORS ─────────────────────────
cat > "$ROOT/app-server/src/main/resources/application.yml" << 'EOF'
server:
  port: 8080

spring:
  application:
    name: app-server
  datasource:
    url: jdbc:postgresql://localhost:5432/imageprocessing_db
    username: imageuser
    password: imagepass
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update          # update para avance 2; cambiar a validate en produccion
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # Subida de archivos
  servlet:
    multipart:
      enabled: true
      max-file-size: 20MB
      max-request-size: 100MB

# SOAP (Apache CXF)
cxf:
  path: /ws
  servlet.init:
    service-list-path: /services

app:
  jwt:
    secret: "C4mbI4r_P0r_S3cr3t0_S3gur0_d3_256_b1ts_m1n1m0!!"
    expiration-ms: 86400000

  callback-base-url: "http://localhost:8080"
  heartbeat-interval-ms: 30000

  # Directorio donde se guardan las imagenes subidas
  upload-dir: "/tmp/imageprocessing/uploads"

logging:
  level:
    com.imageprocessing: DEBUG
    org.apache.cxf: WARN
    org.springframework.security: WARN
EOF
echo "[OK] application.yml actualizado"

# ─── FIX 3: AppServerApplication con @EnableAsync ────────────────────────────
cat > "$SERVER_SRC/AppServerApplication.java" << 'EOF'
package com.imageprocessing.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AppServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppServerApplication.class, args);
    }
}
EOF
echo "[OK] @EnableAsync agregado"

# ─── FIX 4: SecurityConfig — permite todo para el avance ─────────────────────
mkdir -p "$SERVER_SRC/config"
cat > "$SERVER_SRC/config/SecurityConfig.java" << 'EOF'
package com.imageprocessing.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()   // Para avance 2: autenticacion manejada por JWT en capa SOAP/REST
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        return new UrlBasedCorsConfigurationSource() {{
            registerCorsConfiguration("/**", config);
        }};
    }
}
EOF
echo "[OK] SecurityConfig creado"

# ─── FIX 5: AuthService — usar PasswordEncoder de Spring en vez de BCrypt raw ─
cat > "$SERVER_SRC/service/AuthService.java" << 'EOF'
package com.imageprocessing.server.service;

import com.imageprocessing.server.model.dto.LoginRequest;
import com.imageprocessing.server.model.dto.LoginResponse;
import com.imageprocessing.server.model.dto.RegisterRequest;
import com.imageprocessing.server.model.entity.Usuario;
import com.imageprocessing.server.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
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

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
            .nombre(request.getNombre())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .rol("USER")
            .build();

        usuarioRepository.save(usuario);

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
            .claim("nombre", usuario.getNombre())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }
}
EOF
echo "[OK] AuthService actualizado (usa PasswordEncoder de Spring)"

# ─── FIX 6: DTO RegisterRequest ──────────────────────────────────────────────
cat > "$SERVER_SRC/model/dto/RegisterRequest.java" << 'EOF'
package com.imageprocessing.server.model.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
}
EOF
echo "[OK] RegisterRequest DTO creado"

# ─── FIX 7: AuthController REST (register + login REST para el frontend) ──────
mkdir -p "$SERVER_SRC/controller"
cat > "$SERVER_SRC/controller/AuthController.java" << 'EOF'
package com.imageprocessing.server.controller;

import com.imageprocessing.server.model.dto.LoginRequest;
import com.imageprocessing.server.model.dto.LoginResponse;
import com.imageprocessing.server.model.dto.RegisterRequest;
import com.imageprocessing.server.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Credenciales invalidas"));
        }
    }
}
EOF
echo "[OK] AuthController REST creado (/api/auth/register y /api/auth/login)"

# ─── FIX 8: FileUploadController — para que el frontend pueda subir archivos ──
cat > "$SERVER_SRC/controller/FileUploadController.java" << 'EOF'
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
EOF
echo "[OK] FileUploadController creado"

# ─── FIX 9: SOAP — agregar operacion register ─────────────────────────────────
cat > "$SERVER_SRC/soap/ImageProcessingServiceSoap.java" << 'EOF'
package com.imageprocessing.server.soap;

import com.imageprocessing.server.model.dto.*;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService(name = "ImageProcessingService",
            targetNamespace = "http://imageprocessing.com/soap")
public interface ImageProcessingServiceSoap {

    @WebMethod(operationName = "register")
    LoginResponse register(@WebParam(name = "request") RegisterRequest request);

    @WebMethod(operationName = "login")
    LoginResponse login(@WebParam(name = "request") LoginRequest request);

    @WebMethod(operationName = "enviarLote")
    EnviarLoteResponse enviarLote(@WebParam(name = "request") EnviarLoteRequest request);

    @WebMethod(operationName = "consultarProgreso")
    ProgresoBatchResponse consultarProgreso(@WebParam(name = "idLote") Long idLote,
                                             @WebParam(name = "token") String token);
}
EOF

cat > "$SERVER_SRC/soap/ImageProcessingServiceImpl.java" << 'EOF'
package com.imageprocessing.server.soap;

import com.imageprocessing.server.model.dto.*;
import com.imageprocessing.server.service.AuthService;
import com.imageprocessing.server.service.BatchService;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@WebService(endpointInterface = "com.imageprocessing.server.soap.ImageProcessingServiceSoap",
            serviceName = "ImageProcessingService",
            portName = "ImageProcessingPort",
            targetNamespace = "http://imageprocessing.com/soap")
@RequiredArgsConstructor
public class ImageProcessingServiceImpl implements ImageProcessingServiceSoap {

    private final AuthService authService;
    private final BatchService batchService;

    @Override
    public LoginResponse register(RegisterRequest request) {
        log.info("SOAP register: {}", request.getEmail());
        return authService.register(request);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("SOAP login: {}", request.getEmail());
        return authService.login(request);
    }

    @Override
    public EnviarLoteResponse enviarLote(EnviarLoteRequest request) {
        Long idUsuario = authService.validarToken(request.getToken());
        log.info("SOAP enviarLote: usuario={}, imagenes={}", idUsuario, request.getImagenes().size());
        return batchService.procesarEnvioLote(idUsuario, request);
    }

    @Override
    public ProgresoBatchResponse consultarProgreso(Long idLote, String token) {
        authService.validarToken(token);
        return batchService.consultarProgreso(idLote);
    }
}
EOF
echo "[OK] SOAP actualizado con operacion register"

# ─── FIX 10: Frontend — Register.jsx con llamada REST real ────────────────────
cat > "$ROOT/client-web/src/pages/Register.jsx" << 'EOF'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Register() {
  const [form, setForm] = useState({ nombre: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (form.password !== form.confirm) {
      setError('Las contraseñas no coinciden'); return
    }
    setLoading(true); setError('')
    try {
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nombre: form.nombre, email: form.email, password: form.password })
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error || 'Error en el registro')
      authLogin(data)        // auto-login tras registro exitoso
      navigate('/')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <h2 style={styles.title}>Crear Cuenta</h2>
        <p style={styles.subtitle}>Sistema de Procesamiento de Imágenes</p>
        {error && <div style={styles.error}>{error}</div>}
        <input style={styles.input} placeholder="Nombre completo" value={form.nombre}
          onChange={e => setForm({...form, nombre: e.target.value})} required />
        <input style={styles.input} type="email" placeholder="Email" value={form.email}
          onChange={e => setForm({...form, email: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Contraseña" value={form.password}
          onChange={e => setForm({...form, password: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Confirmar contraseña" value={form.confirm}
          onChange={e => setForm({...form, confirm: e.target.value})} required />
        <button style={styles.btn} type="submit" disabled={loading}>
          {loading ? 'Registrando...' : 'Registrarse'}
        </button>
        <p style={{textAlign:'center', marginTop:12}}>
          ¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link>
        </p>
      </form>
    </div>
  )
}

const styles = {
  container: { display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', background:'#f0f2f5' },
  card: { background:'#fff', padding:40, borderRadius:12, boxShadow:'0 4px 20px rgba(0,0,0,.1)', width:400 },
  title: { fontSize:24, fontWeight:700, marginBottom:4 },
  subtitle: { color:'#666', marginBottom:24, fontSize:14 },
  input: { display:'block', width:'100%', padding:'10px 14px', marginBottom:16, border:'1px solid #ddd', borderRadius:8, fontSize:14 },
  btn: { width:'100%', padding:'12px', background:'#4f46e5', color:'#fff', border:'none', borderRadius:8, fontSize:16, cursor:'pointer' },
  error: { background:'#fee2e2', color:'#991b1b', padding:10, borderRadius:8, marginBottom:16, fontSize:14 }
}
EOF
echo "[OK] Register.jsx con endpoint REST real"

# ─── FIX 11: Login.jsx — usar REST en vez de SOAP para el flujo de login ──────
cat > "$ROOT/client-web/src/pages/Login.jsx" << 'EOF'
import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login: authLogin } = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true); setError('')
    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      })
      const data = await res.json()
      if (!res.ok) throw new Error(data.error || 'Credenciales invalidas')
      authLogin(data)
      navigate('/')
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={styles.container}>
      <form onSubmit={handleSubmit} style={styles.card}>
        <h2 style={styles.title}>Iniciar Sesión</h2>
        <p style={styles.subtitle}>Sistema de Procesamiento de Imágenes</p>
        {error && <div style={styles.error}>{error}</div>}
        <input style={styles.input} type="email" placeholder="Email"
          value={form.email} onChange={e => setForm({...form, email: e.target.value})} required />
        <input style={styles.input} type="password" placeholder="Contraseña"
          value={form.password} onChange={e => setForm({...form, password: e.target.value})} required />
        <button style={styles.btn} type="submit" disabled={loading}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
        <p style={{textAlign:'center', marginTop:12}}>
          ¿No tienes cuenta? <Link to="/register">Regístrate</Link>
        </p>
      </form>
    </div>
  )
}

const styles = {
  container: { display:'flex', justifyContent:'center', alignItems:'center', minHeight:'100vh', background:'#f0f2f5' },
  card: { background:'#fff', padding:40, borderRadius:12, boxShadow:'0 4px 20px rgba(0,0,0,.1)', width:380 },
  title: { fontSize:24, fontWeight:700, marginBottom:4 },
  subtitle: { color:'#666', marginBottom:24, fontSize:14 },
  input: { display:'block', width:'100%', padding:'10px 14px', marginBottom:16, border:'1px solid #ddd', borderRadius:8, fontSize:14 },
  btn: { width:'100%', padding:'12px', background:'#4f46e5', color:'#fff', border:'none', borderRadius:8, fontSize:16, cursor:'pointer' },
  error: { background:'#fee2e2', color:'#991b1b', padding:10, borderRadius:8, marginBottom:16, fontSize:14 }
}
EOF
echo "[OK] Login.jsx usa REST directo (más simple y confiable)"

# ─── FIX 12: BatchUpload — subir archivos reales antes de enviar el lote ──────
cat > "$ROOT/client-web/src/pages/BatchUpload.jsx" << 'EOF'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { enviarLote } from '../services/soapClient.js'

const TRANSFORMACIONES = [
  { valor: 'ESCALA_GRISES', label: 'Escala de grises' },
  { valor: 'REDIMENSIONAR', label: 'Redimensionar', params: { ancho: 800, alto: 600 } },
  { valor: 'ROTAR', label: 'Rotar', params: { grados: 90 } },
  { valor: 'RECORTAR', label: 'Recortar', params: { x: 0, y: 0, ancho: 400, alto: 300 } },
  { valor: 'REFLEJAR', label: 'Reflejar', params: { eje: 'HORIZONTAL' } },
  { valor: 'DESENFOCAR', label: 'Desenfocar', params: { radio: 3 } },
  { valor: 'NITIDEZ', label: 'Nitidez' },
  { valor: 'BRILLO_CONTRASTE', label: 'Brillo y contraste', params: { brillo: 10, contraste: 10 } },
  { valor: 'MARCA_DE_AGUA', label: 'Marca de agua', params: { texto: 'CONFIDENCIAL' } },
  { valor: 'CONVERSION_FORMATO', label: 'Convertir formato', params: { formato: 'PNG' } },
]

export default function BatchUpload() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [archivos, setArchivos] = useState([])   // { file, transformaciones: [] }
  const [loading, setLoading] = useState(false)
  const [fase, setFase] = useState('')
  const [error, setError] = useState('')

  const onFilesChange = (e) => {
    const files = Array.from(e.target.files)
    setArchivos(files.map(f => ({ file: f, transformaciones: [] })))
    setError('')
  }

  const agregarTransformacion = (idx) => {
    if (archivos[idx].transformaciones.length >= 5) {
      setError('Máximo 5 transformaciones por imagen'); return
    }
    setArchivos(prev => prev.map((a, i) => i === idx ? {
      ...a,
      transformaciones: [...a.transformaciones, {
        tipo: 'ESCALA_GRISES',
        orden: a.transformaciones.length + 1,
        parametros: '{}'
      }]
    } : a))
  }

  const cambiarTipoTransformacion = (idxImg, idxTrans, nuevoTipo) => {
    const trans = TRANSFORMACIONES.find(t => t.valor === nuevoTipo)
    const params = trans?.params ? JSON.stringify(trans.params) : '{}'
    setArchivos(prev => prev.map((a, i) => i === idxImg ? {
      ...a,
      transformaciones: a.transformaciones.map((t, j) => j === idxTrans
        ? { ...t, tipo: nuevoTipo, parametros: params } : t)
    } : a))
  }

  const eliminarTransformacion = (idxImg, idxTrans) => {
    setArchivos(prev => prev.map((a, i) => i === idxImg ? {
      ...a,
      transformaciones: a.transformaciones
        .filter((_, j) => j !== idxTrans)
        .map((t, j) => ({ ...t, orden: j + 1 }))
    } : a))
  }

  const handleSubmit = async () => {
    if (archivos.length === 0) { setError('Selecciona al menos una imagen'); return }
    const sinTransf = archivos.find(a => a.transformaciones.length === 0)
    if (sinTransf) { setError(`"${sinTransf.file.name}" no tiene transformaciones`); return }

    setLoading(true); setError('')

    try {
      // FASE 1: Subir archivos al servidor
      setFase('Subiendo archivos...')
      const formData = new FormData()
      archivos.forEach(a => formData.append('files', a.file))

      const uploadRes = await fetch('/api/files/upload', {
        method: 'POST', body: formData
      })
      if (!uploadRes.ok) throw new Error('Error al subir los archivos')
      const uploaded = await uploadRes.json()

      // FASE 2: Enviar lote via SOAP
      setFase('Enviando lote al servidor...')
      const imagenesPayload = archivos.map((a, i) => ({
        nombreArchivo: a.file.name,
        rutaOriginal: uploaded[i].rutaAlmacenada,
        transformaciones: a.transformaciones
      }))

      const { idLote } = await enviarLote(user.token, imagenesPayload)
      navigate(`/batch/${idLote}`)
    } catch (e) {
      setError('Error: ' + e.message)
    } finally {
      setLoading(false); setFase('')
    }
  }

  return (
    <div style={styles.page}>
      <div style={styles.container}>
        <h2 style={styles.h2}>📤 Enviar Lote de Imágenes</h2>
        {error && <div style={styles.error}>{error}</div>}

        <label style={styles.dropzone}>
          <input type="file" multiple accept="image/jpeg,image/png,image/tiff"
            onChange={onFilesChange} style={{display:'none'}} />
          <span style={{fontSize:40}}>📁</span>
          <p style={{fontWeight:600}}>Haz clic para seleccionar imágenes</p>
          <small style={{color:'#888'}}>JPG, PNG, TIF — máx. 20 MB por archivo</small>
        </label>

        {archivos.map((a, i) => (
          <div key={i} style={styles.imgCard}>
            <div style={styles.imgHeader}>
              <div>
                <strong>🖼 {a.file.name}</strong>
                <span style={styles.fileSize}> ({(a.file.size / 1024).toFixed(0)} KB)</span>
              </div>
              <button style={styles.btnAdd} onClick={() => agregarTransformacion(i)}
                disabled={a.transformaciones.length >= 5}>
                + Transformación
              </button>
            </div>

            {a.transformaciones.length === 0 && (
              <p style={{color:'#f59e0b', fontSize:13}}>⚠ Agrega al menos una transformación</p>
            )}

            {a.transformaciones.map((t, j) => (
              <div key={j} style={styles.transRow}>
                <span style={styles.ordenBadge}>{t.orden}</span>
                <select style={styles.select} value={t.tipo}
                  onChange={e => cambiarTipoTransformacion(i, j, e.target.value)}>
                  {TRANSFORMACIONES.map(op =>
                    <option key={op.valor} value={op.valor}>{op.label}</option>)}
                </select>
                <code style={styles.params}>{t.parametros}</code>
                <button style={styles.btnDel} onClick={() => eliminarTransformacion(i, j)}>✕</button>
              </div>
            ))}
          </div>
        ))}

        {archivos.length > 0 && (
          <button style={{...styles.btnEnviar, opacity: loading ? .7 : 1}}
            onClick={handleSubmit} disabled={loading}>
            {loading ? `⏳ ${fase}` : `📤 Enviar ${archivos.length} imagen(es)`}
          </button>
        )}
      </div>
    </div>
  )
}

const styles = {
  page: { minHeight:'100vh', background:'#f0f2f5', padding:32 },
  container: { maxWidth:760, margin:'0 auto' },
  h2: { fontSize:22, fontWeight:700, marginBottom:24 },
  dropzone: { display:'flex', flexDirection:'column', alignItems:'center', gap:8,
              border:'2px dashed #c7d2fe', borderRadius:14, padding:40, marginBottom:24,
              cursor:'pointer', background:'#fafbff', textAlign:'center' },
  imgCard: { background:'#fff', borderRadius:12, padding:20, marginBottom:16,
             boxShadow:'0 2px 10px rgba(0,0,0,.07)' },
  imgHeader: { display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:12 },
  fileSize: { color:'#888', fontSize:12 },
  transRow: { display:'flex', alignItems:'center', gap:10, marginBottom:8 },
  ordenBadge: { background:'#e0e7ff', color:'#4f46e5', borderRadius:20, padding:'2px 10px', fontSize:12, fontWeight:700 },
  select: { padding:'6px 10px', borderRadius:6, border:'1px solid #e5e7eb', fontSize:13, flexShrink:0 },
  params: { fontSize:11, color:'#6b7280', background:'#f3f4f6', padding:'3px 8px', borderRadius:4, overflow:'hidden', maxWidth:200 },
  btnDel: { background:'#fee2e2', color:'#dc2626', border:'none', borderRadius:6, padding:'4px 10px', cursor:'pointer', fontSize:12 },
  btnAdd: { padding:'6px 14px', background:'#e0e7ff', border:'none', borderRadius:8, cursor:'pointer', fontSize:13, color:'#4f46e5', fontWeight:600 },
  btnEnviar: { width:'100%', padding:14, background:'#4f46e5', color:'#fff', border:'none',
               borderRadius:12, fontSize:16, cursor:'pointer', marginTop:8, fontWeight:600 },
  error: { background:'#fee2e2', color:'#991b1b', padding:12, borderRadius:8, marginBottom:16, fontSize:14 }
}
EOF
echo "[OK] BatchUpload.jsx con upload real de archivos"

echo ""
echo "============================================================"
echo "  ✅ Todos los fixes aplicados"
echo "============================================================"
echo ""
echo "  Secuencia de arranque para mañana:"
echo ""
echo "  1. docker-compose up -d"
echo "     (esperar ~10s a que postgres inicie)"
echo ""
echo "  2. cd distributed-image-processing"
echo "     mvn clean install -DskipTests"
echo ""
echo "  3. Terminal A — App Server:"
echo "     cd app-server && mvn spring-boot:run"
echo ""
echo "  4. Terminal B — Worker Node:"
echo "     cd worker-node && mvn spring-boot:run"
echo ""
echo "  5. Terminal C — Frontend:"
echo "     cd client-web && npm install && npm run dev"
echo ""
echo "  Verificar que funciona:"
echo "  - http://localhost:3000  → Login/Register funcionando"
echo "  - http://localhost:8080/ws/services  → WSDL listado"
echo "  - http://localhost:8080/ws/ImageProcessingService?wsdl  → WSDL completo"
echo ""

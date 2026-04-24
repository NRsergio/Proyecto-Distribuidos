# 📂 Estructura del App-Server v2.0

Documento que describe la organización y responsabilidades de cada carpeta del proyecto.

## 📋 Índice

1. [Estructura General](#estructura-general)
2. [Descripción de Carpetas](#descripción-de-carpetas)
3. [Flujo de Peticiones](#flujo-de-peticiones)
4. [Principios de Diseño](#principios-de-diseño)

---

## 📐 Estructura General

```
app-server2/
├── pom.xml                                  # Dependencias Maven
├── README.md                                # Documentación principal
├── ESTRUCTURA.md                            # Este archivo
├── .env.example                             # Variables de entorno
├── .gitignore                               # Git ignore
│
└── src/
    ├── main/
    │   ├── java/com/imageprocessing/server/
    │   │   ├── AppServerApplication.java
    │   │   │
    │   │   ├── config/                      # 🔧 Configuración
    │   │   │   └── SecurityConfig.java
    │   │   │
    │   │   ├── controller/                  # 🌐 Endpoints REST
    │   │   │   ├── AuthRestController.java
    │   │   │   ├── BatchRestController.java
    │   │   │   ├── NodoRestController.java
    │   │   │   └── HealthController.java
    │   │   │
    │   │   ├── middleware/                  # 🔐 Filtros
    │   │   │   └── JwtAuthenticationFilter.java
    │   │   │
    │   │   ├── service/                     # 💼 Lógica de negocio
    │   │   │   ├── AuthService.java
    │   │   │   ├── BatchService.java
    │   │   │   └── NodoService.java
    │   │   │
    │   │   ├── repository/                  # 💾 Acceso a datos
    │   │   │   ├── UsuarioRepository.java
    │   │   │   ├── SesionRepository.java
    │   │   │   ├── SolicitudLoteRepository.java
    │   │   │   ├── ImagenSolicitudRepository.java
    │   │   │   ├── TransformacionRepository.java
    │   │   │   ├── NodoTrabajadorRepository.java
    │   │   │   └── LogTrabajoRepository.java
    │   │   │
    │   │   ├── model/                       # 📊 Modelos de datos
    │   │   │   ├── entity/                  # Entidades JPA
    │   │   │   │   ├── Usuario.java
    │   │   │   │   ├── Sesion.java
    │   │   │   │   ├── SolicitudLote.java
    │   │   │   │   ├── ImagenSolicitud.java
    │   │   │   │   ├── Transformacion.java
    │   │   │   │   ├── NodoTrabajador.java
    │   │   │   │   └── LogTrabajo.java
    │   │   │   ├── dto/                     # DTOs
    │   │   │   │   ├── LoginRequest.java
    │   │   │   │   ├── RegisterRequest.java
    │   │   │   │   ├── AuthResponse.java
    │   │   │   │   ├── BatchRequest.java
    │   │   │   │   ├── BatchResponse.java
    │   │   │   │   ├── ProgressResponse.java
    │   │   │   │   └── TransformacionDTO.java
    │   │   │   └── enums/                   # Enumeraciones
    │   │   │
    │   │   ├── rmi/                         # 🔌 Comunicación RMI
    │   │   │   └── RmiClient.java
    │   │   │
    │   │   └── utils/                       # 🛠️ Utilidades
    │   │       └── JwtTokenProvider.java
    │   │
    │   └── resources/
    │       └── application.yml              # Config de Spring
    │
    └── test/
        └── java/com/imageprocessing/server/
            └── (tests aquí)
```

---

## 📝 Descripción de Carpetas

### `config/` - Configuración

**Responsabilidad**: Configuración de Spring Boot y seguridad

- **SecurityConfig.java**
  - Configuración de Spring Security
  - CORS para comunicación con frontend
  - Password encoding BCrypt
  - Configuración de CSRF

**Cuándo modificar**:
- Cambios en políticas de seguridad
- Agregar nuevas fuentes CORS
- Modificar algoritmos de encriptación

---

### `controller/` - Controladores REST

**Responsabilidad**: Exponer endpoints HTTP REST y orquestar peticiones

**Controladores**:

| Clase | Ruta | Responsabilidad |
|-------|------|-----------------|
| `AuthRestController` | `/api/auth/*` | Login, registro, validación |
| `BatchRestController` | `/api/batch/*` | Crear y trackear lotes |
| `NodoRestController` | `/api/nodos/*` | Registrar y monitorear nodos |
| `HealthController` | `/api/health` | Health check |

**Flujo típico**:
```
Petición HTTP → Controller → Service → Repository → BD → Response JSON
```

**Responsabilidades del Controller**:
- ✅ Recibir peticiones HTTP
- ✅ Validar token JWT
- ✅ Parsear parámetros
- ✅ Llamar servicio apropiado
- ✅ Convertir respuesta a JSON
- ✅ Manejar errores HTTP

**Cuándo modificar**:
- Agregar nuevos endpoints
- Cambiar estructura de request/response
- Modificar validaciones de input

---

### `middleware/` - Filtros e Interceptores

**Responsabilidad**: Procesar peticiones globalmente antes de llegar a controllers

**Componentes**:

- **JwtAuthenticationFilter.java**
  - Extrae JWT del header `Authorization`
  - Valida firma del token
  - Establece contexto de seguridad de Spring
  - Se ejecuta en CADA petición

**Cuándo modificar**:
- Cambios en estrategia de autenticación
- Agregar nuevos filtros (logging, CORS, etc.)

---

### `service/` - Lógica de Negocio

**Responsabilidad**: Implementar la lógica de negocio pura y orquestar operaciones

**Servicios**:

| Clase | Responsabilidad |
|-------|-----------------|
| `AuthService` | Login, registro, validación de sesiones |
| `BatchService` | Creación, tracking y progreso de lotes |
| `NodoService` | Registro, monitoreo y métricas de nodos |

**Principios**:
- ✅ Separados de HTTP (podría usarse en otros contextos)
- ✅ Reutilizables
- ✅ Testables en aislamiento
- ✅ Contienen transacciones

**AuthService**:
```java
public AuthResponse login(LoginRequest request) {
    // 1. Buscar usuario
    // 2. Validar contraseña (BCrypt)
    // 3. Generar JWT
    // 4. Crear sesión en BD
    // 5. Retornar AuthResponse
}
```

**Cuándo modificar**:
- Cambios en reglas de negocio
- Modificar flujo de autenticación
- Cambiar algoritmos de procesamiento

---

### `repository/` - Acceso a Datos

**Responsabilidad**: Interfaz entre la aplicación y la base de datos

**Características**:
- Extienden `JpaRepository<Entity, ID>`
- Spring Data genera implementación automáticamente
- Métodos customizados para queries específicas

**Ejemplo**:
```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Boolean existsByEmail(String email);
}
```

**Cuándo modificar**:
- Necesitar queries más complejas
- Agregar índices de BD
- Cambiar estrategia de caching

---

### `model/` - Modelos de Datos

#### `entity/` - Entidades JPA

Clases mapeadas a tablas de BD usando anotaciones JPA:

| Entidad | Tabla | Schema |
|---------|-------|--------|
| `Usuario` | usuarios | auth |
| `Sesion` | sesiones | auth |
| `SolicitudLote` | solicitud_lote | public |
| `ImagenSolicitud` | imagen_solicitud | public |
| `Transformacion` | transformacion | public |
| `NodoTrabajador` | nodo_trabajador | public |
| `LogTrabajo` | log_trabajo | public |

**Anotaciones principales**:
- `@Entity` - Mapeo a tabla
- `@Table` - Nombre de tabla y schema
- `@Column` - Mapeo de columna
- `@ManyToOne` / `@OneToMany` - Relaciones
- `@Index` - Índices de BD

**Cuándo modificar**:
- Agregar nuevos campos
- Cambiar relaciones
- Modificar restricciones

#### `dto/` - Data Transfer Objects

Clases para entrada/salida HTTP, desacopladas de entidades:

| DTO | Propósito |
|-----|-----------|
| `LoginRequest` | Entrada: credenciales de login |
| `RegisterRequest` | Entrada: datos de nuevo usuario |
| `AuthResponse` | Salida: token y datos de usuario |
| `BatchRequest` | Entrada: crear nuevo lote |
| `BatchResponse` | Salida: confirmación de lote |
| `ProgressResponse` | Salida: progreso de lote |
| `TransformacionDTO` | Transformación dentro de lote |

**Ventajas**:
- ✅ Desacoplamiento entre capa HTTP y BD
- ✅ Validación específica de entrada
- ✅ Oculta campos innecesarios
- ✅ Versioning de API más fácil

**Cuándo modificar**:
- Cambiar estructura de API
- Agregar validaciones
- Versionar endpoints

#### `enums/` - Enumeraciones

Valores constantes del sistema (vacío actualmente, expandir según necesidad):

```java
// Ejemplo de uso futuro:
public enum RolUsuario {
    ADMIN, USER, GUEST
}

public enum EstadoLote {
    PENDIENTE, PROCESANDO, COMPLETADO, ERROR
}
```

---

### `rmi/` - Comunicación RMI

**Responsabilidad**: Cliente RMI para comunicación remota con nodos workers

**RmiClient.java**:
- Gestiona conexiones a registries RMI remotos
- Cachea referencias de servicios para performance
- Maneja excepciones de desconexión

**Uso**:
```java
Object servicioRemoto = rmiClient.obtenerServicioRemoto("192.168.1.100", 9090);
// Usar servicioRemoto para llamadas remotas
```

**Cuándo modificar**:
- Cambiar estrategia de comunicación
- Agregar retry logic
- Implementar load balancing

---

### `utils/` - Utilidades

**Responsabilidad**: Helpers y utilidades reutilizables

**JwtTokenProvider.java**:
- Generación de JWT con firma HMAC-SHA512
- Validación de tokens
- Extracción de claims

**Potenciales utilidades futuras**:
- `FileUtil.java` - Manejo de archivos
- `DateUtil.java` - Operaciones de fechas
- `ValidationUtil.java` - Validadores custom
- `EncryptionUtil.java` - Encriptación

---

## 🔄 Flujo de Peticiones

### Login Flow

```
POST /api/auth/login
    ↓
AuthRestController.login()
    ↓
AuthService.login()
    ├─ buscarUsuario()
    ├─ validarContraseña() [BCrypt]
    ├─ generarJWT() [JwtTokenProvider]
    ├─ crearSesion() [SesionRepository]
    └─ retornando AuthResponse
    ↓
JSON Response con token
```

### Validar Token

```
GET /api/auth/validar
    ↓
JwtAuthenticationFilter
    ├─ extraer JWT del header
    └─ validar firma
    ↓
AuthService.validarToken()
    ├─ verificar JWT [JwtTokenProvider]
    ├─ buscar en BD [SesionRepository]
    ├─ verificar fecha expiración
    └─ retornar boolean
    ↓
HTTP 200 OK o 400 Bad Request
```

### Crear Lote

```
POST /api/batch/enviar
    ↓
Middleware: validar JWT
    ↓
BatchRestController.enviarLote()
    ├─ obtener usuario del token
    └─ validar request
    ↓
BatchService.crearLote()
    ├─ crear SolicitudLote [SolicitudLoteRepository]
    └─ crear transformaciones [TransformacionRepository]
    ↓
JSON Response con BatchResponse
```

### Registrar Nodo

```
POST /api/nodos/registrar
    ↓
NodoRestController.registrarNodo()
    ↓
NodoService.registrarNodo()
    ├─ validar que no existe [NodoTrabajadorRepository]
    ├─ crear NodoTrabajador
    └─ guardar en BD
    ↓
JSON Response con datos del nodo
```

---

## 🎯 Principios de Diseño

### 1. Separación de Responsabilidades

```
Controller  → Orquestación HTTP
Service     → Lógica de negocio
Repository  → Acceso a datos
Entity      → Representación de datos
DTO         → Contratos de API
```

### 2. Inyección de Dependencias

Utilizar `@RequiredArgsConstructor` de Lombok + `@Autowired`:

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    // Constructor generado automáticamente
}
```

### 3. Transacciones

Los servicios manejan transacciones automáticamente:

```java
@Service
@Transactional  // Abre transacción para cada método
public class BatchService {
    public void crearLote(Long idUsuario, BatchRequest request) {
        // Automáticamente commit/rollback
    }
}
```

### 4. Logging

Usar SLF4J + Logback:

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);

logger.info("Mensaje informativo");
logger.warn("Advertencia");
logger.error("Error", exception);
```

### 5. Validación

```java
// En entidades
@NotNull(message = "Campo no puede ser nulo")
@NotBlank(message = "Campo no puede estar en blanco")
private String username;

// En DTOs
@Valid
@NotNull(message = "Request no puede ser nulo")
private LoginRequest request;
```

---

## 📊 Patrones de Arquitectura

### MVC (Model-View-Controller)

Adaptado para REST:

- **Model**: `model/entity/` + `model/dto/`
- **View**: JSON responses
- **Controller**: `controller/`

### Service Layer

Capa intermedia de lógica:

```
controller → service → repository → database
```

### Data Access Object (DAO)

Implementado via `JpaRepository`:

```java
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Implementación generada automáticamente
}
```

### Builder Pattern

Para constructores complejos:

```java
Usuario usuario = Usuario.builder()
    .username("juan")
    .email("juan@example.com")
    .passwordHash(encoded)
    .rol("USER")
    .build();
```

---

## 🔄 Extensión de la Estructura

### Agregar Nuevo Feature

1. **Crear Entidad**
   ```
   model/entity/MiEntidad.java
   ```

2. **Crear Repository**
   ```
   repository/MiEntidadRepository.java
   ```

3. **Crear Service**
   ```
   service/MiEntidadService.java
   ```

4. **Crear DTOs**
   ```
   model/dto/MiEntidadRequest.java
   model/dto/MiEntidadResponse.java
   ```

5. **Crear Controller**
   ```
   controller/MiEntidadController.java
   ```

---

## 📝 Convenciones de Nombres

```
Clases de Entidad:         Usuario.java
Clases de DTO:             UsuarioRequest.java, UsuarioResponse.java
Clases de Repository:      UsuarioRepository.java
Clases de Service:         UsuarioService.java
Clases de Controller:      UsuarioRestController.java
Métodos GET:               obtener...(), buscar...()
Métodos POST/PUT:          crear...(), actualizar...()
Métodos DELETE:            eliminar...(), borrar...()
Variables privadas:        private String nombre;
Constantes:                private static final int MAX_SIZE = 100;
```

---

## 🚀 Próximos Pasos Recomendados

- [ ] Agregar tests unitarios (`src/test/`)
- [ ] Implementar paginación en listados
- [ ] Agregar auditoría de cambios
- [ ] Implementar caché (Redis)
- [ ] Agregar rate limiting
- [ ] Implementar versionado de API
- [ ] Agregar documentación OpenAPI/Swagger
- [ ] Mejorar manejo de errores global


# 📱 App-Server v2.0 - Servidor de Aplicación Distribuido

Servidor de aplicación para procesamiento distribuido de imágenes. Actúa como intermediario entre el cliente web (via HTTP REST) y los nodos trabajadores (via RMI).

## 🏗️ Arquitectura

```
┌──────────────────┐
│   Cliente Web    │ (React/Vite - puerto 3000)
│  (Browser)       │
└────────┬─────────┘
         │ HTTP REST
┌────────▼─────────┐
│ Backend Express  │ (Node.js - puerto 3001)
│   (Intermediario)│
└────────┬─────────┘
         │ HTTP REST
┌────────▼─────────────────────┐
│   App-Server v2.0            │ (Spring Boot - puerto 8080)
│  (Este proyecto)             │
│                              │
│ ┌──────────────────────────┐ │
│ │ Controllers (HTTP REST)  │ │
│ └──────────────────────────┘ │
│ ┌──────────────────────────┐ │
│ │ Services (Lógica)        │ │
│ └──────────────────────────┘ │
│ ┌──────────────────────────┐ │
│ │ RMI Client               │ │
│ └──────────────────────────┘ │
└────────┬─────────────────────┘
         │ RMI
    ┌────┴────┬──────────┐
    │          │          │
┌───▼──┐  ┌───▼──┐  ┌───▼──┐
│Node-1│  │Node-2│  │Node-N│ (Worker Nodes - puerto 9090+)
└──────┘  └──────┘  └──────┘
```

## 🚀 Características

- ✅ **Autenticación JWT** con sesiones persistidas en BD
- ✅ **API REST** completa para cliente web
- ✅ **Comunicación RMI** con nodos trabajadores paralelos
- ✅ **Bases de datos**:
  - Schema `auth` para autenticación
  - Schema `public` para procesamiento de imágenes
- ✅ **Gestión de lotes** de procesamiento
- ✅ **Monitoreo de nodos** trabajadores
- ✅ **Arquitectura limpia** y escalable

## 📋 Requisitos

- **Java**: 17 o superior
- **Maven**: 3.8.0 o superior
- **PostgreSQL**: 12 o superior
- **Base de datos**: `imageprocessing_db` con esquemas `auth` y `public`

## 🔧 Configuración

### 1. Variables de Entorno

Crear `.env` basado en `.env.example`:

```bash
# Database
DB_HOST=localhost
DB_PORT=5416
DB_NAME=imageprocessing_db
DB_USER=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# RMI
RMI_REGISTRY_PORT=1099
RMI_SERVICE_NAME=ImageProcessingWorker

# File Upload
FILE_UPLOAD_DIR=./uploads
FILE_MAX_SIZE=104857600
```

### 2. Base de Datos

La base de datos debe tener:

```sql
-- Schema para autenticación
CREATE SCHEMA auth;

CREATE TABLE auth.usuarios (
    id_usuario SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL DEFAULT 'USER',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT true
);

CREATE TABLE auth.sesiones (
    id_sesion SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES auth.usuarios(id_usuario),
    token VARCHAR(500) UNIQUE NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    dispositivo_info VARCHAR(255),
    activa BOOLEAN DEFAULT true
);

-- Schema para procesamiento (ya debe existir)
-- Ver database/01_schema.sql del proyecto
```

## 🚀 Inicio Rápido

### Compilar

```bash
mvn clean package
```

### Ejecutar

```bash
# Con Maven
mvn spring-boot:run

# Con Java directo
java -jar target/app-server-2.0.0.jar

# La aplicación escucha en http://localhost:8080
```

## 📚 API Endpoints

### Autenticación

```
POST   /api/auth/login      - Login de usuario
POST   /api/auth/register   - Registro de nuevo usuario
GET    /api/auth/validar    - Validar token JWT
POST   /api/auth/logout     - Cerrar sesión
```

### Procesamiento de Lotes

```
POST   /api/batch/enviar                 - Crear nuevo lote
GET    /api/batch/progreso/{idLote}      - Obtener progreso
```

### Gestión de Nodos

```
POST   /api/nodos/registrar              - Registrar nuevo nodo
GET    /api/nodos/activos                - Listar nodos activos
PUT    /api/nodos/{idNodo}/estado        - Actualizar estado
```

### Health Check

```
GET    /api/health                       - Verificar servidor activo
```

## 📊 Estructura del Proyecto

```
app-server2/
├── pom.xml                          # Dependencias Maven
├── README.md                        # Este archivo
├── ESTRUCTURA.md                    # Documentación de carpetas
├── .env.example                     # Template de env vars
├── .gitignore                       # Git config
│
└── src/
    ├── main/
    │   ├── java/com/imageprocessing/server/
    │   │   ├── AppServerApplication.java      # Punto de entrada
    │   │   ├── config/
    │   │   │   └── SecurityConfig.java        # Config de seguridad
    │   │   ├── controller/
    │   │   │   ├── AuthRestController.java    # Endpoints de auth
    │   │   │   ├── BatchRestController.java   # Endpoints de batch
    │   │   │   ├── NodoRestController.java    # Endpoints de nodos
    │   │   │   └── HealthController.java      # Health check
    │   │   ├── middleware/
    │   │   │   └── JwtAuthenticationFilter.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java           # Lógica de auth
    │   │   │   ├── BatchService.java          # Lógica de batch
    │   │   │   └── NodoService.java           # Gestión de nodos
    │   │   ├── repository/
    │   │   │   ├── UsuarioRepository.java
    │   │   │   ├── SesionRepository.java
    │   │   │   ├── SolicitudLoteRepository.java
    │   │   │   ├── ImagenSolicitudRepository.java
    │   │   │   ├── TransformacionRepository.java
    │   │   │   ├── NodoTrabajadorRepository.java
    │   │   │   └── LogTrabajoRepository.java
    │   │   ├── model/
    │   │   │   ├── entity/                    # Entidades JPA
    │   │   │   ├── dto/                       # Data Transfer Objects
    │   │   │   └── enums/                     # Enumeraciones
    │   │   ├── rmi/
    │   │   │   └── RmiClient.java             # Cliente RMI para workers
    │   │   └── utils/
    │   │       └── JwtTokenProvider.java      # JWT utilities
    │   │
    │   └── resources/
    │       └── application.yml                # Config de Spring
    │
    └── test/
        └── java/com/imageprocessing/server/
            └── (tests aquí)
```

## 🔐 Seguridad

### Autenticación
- JWT (JSON Web Tokens) con firma HMAC-SHA512
- Sesiones persistidas en BD para validación
- Contraseñas hasheadas con BCrypt

### Autorización
- Control de acceso por rol (ADMIN, USER)
- Validación de token en cada petición
- CORS configurado para Frontend

### Mejores Prácticas
- Variables sensibles en `.env`
- Contraseñas nunca en logs
- SQL injection prevention (JPA parameterizado)
- CSRF protection

## 🔌 Integración RMI

El cliente RMI (`RmiClient.java`) gestiona la conexión con nodos trabajadores:

```java
// Obtener servicio remoto
Object servicioRemoto = rmiClient.obtenerServicioRemoto("localhost", 9090);

// Usar método remoto del worker
// (Implementación específica según interfaz del worker)
```

## 📈 Escalamiento

### Agregar más nodos workers
1. Iniciar nuevo worker-node en puerto diferente
2. Registrar nodo via `POST /api/nodos/registrar`
3. El app-server automáticamente lo usa para procesamiento paralelo

### Aumentar capacidad
- Aumentar pool de conexiones PostgreSQL
- Usar balanceador de carga frente al app-server
- Replicar app-server backends

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Con cobertura
mvn test jacoco:report
```

## 📝 Ejemplos de Uso

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario",
    "password": "contraseña"
  }'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "idUsuario": 1,
  "username": "usuario",
  "email": "usuario@example.com",
  "rol": "USER"
}
```

### 2. Crear Lote

```bash
curl -X POST http://localhost:8080/api/batch/enviar \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Redimensionar imágenes",
    "descripcion": "Redimensionar a 800x600",
    "transformaciones": [
      {
        "tipo": "RESIZE",
        "parametros": "{\"width\": 800, \"height\": 600}"
      }
    ]
  }'
```

### 3. Registrar Nodo

```bash
curl -X POST http://localhost:8080/api/nodos/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "worker-1",
    "host": "192.168.1.100",
    "puertoRmi": 9090
  }'
```

## 🐛 Troubleshooting

### "Error conectando a nodo RMI"
- Verificar que el nodo está activo
- Verificar puerto RMI del nodo
- Revisar firewall

### "Token inválido"
- Token expirado (24 horas)
- Sesión no encontrada en BD
- Re-hacer login

### "Database connection error"
- Verificar PostgreSQL está activo
- Verificar credenciales en `.env`
- Verificar que BD existe

## 📞 Contacto y Support

Para preguntas o problemas, consultar la documentación o crear issue en el repositorio.

## 📄 Licencia

MIT

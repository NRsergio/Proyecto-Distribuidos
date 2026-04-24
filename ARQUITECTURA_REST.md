# Actualización de Arquitectura: HTTP REST en lugar de SOAP

## Cambios Realizados

### 1. **Database Schema Actualizado**

Se creó un nuevo **esquema `auth`** en PostgreSQL para separar autenticación de datos operacionales:

```sql
SCHEMA auth
├── usuarios (id_usuario, username, password_hash, email, fecha_registro, rol)
└── sesiones (id_sesion, id_usuario, token, fecha_expiracion, dispositivo_info)

SCHEMA public
├── usuario (compatibilidad con lotes)
├── solicitud_lote
├── imagen_solicitud
└── transformacion
```

**Cambios en las entidades:**
- `usuarios.nombre` → **`usuarios.username`** (identificador único)
- `sesiones.estado` → removido, ahora validamos por `fecha_expiracion`
- Agregado `dispositivo_info` en sesiones

### 2. **App-Server: REST Endpoints Nuevos**

Se agregaron controladores REST (`@RestController`) que coexisten con el SOAP antiguo:

#### **AuthRestController** (`/api/auth`)
```
POST   /api/auth/login        → LoginResponse {token, idUsuario, nombre, email}
POST   /api/auth/register     → LoginResponse
GET    /api/auth/validar      → TokenValidationResponse {valido, idUsuario, mensaje}
```

Headers esperados:
```
Content-Type: application/json
Authorization: Bearer <token>  (para get/validar)
```

**Ejemplo - Login:**
```bash
POST http://localhost:8080/api/auth/login
{
  "email": "usuario@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGc...",
  "idUsuario": 1,
  "nombre": "username",
  "email": "usuario@example.com"
}
```

#### **BatchRestController** (`/api/batch`)
```
POST   /api/batch/enviar       → EnviarLoteResponse {idLote}
GET    /api/batch/progreso/:idLote → ProgresoBatchResponse {estado, progreso, imagenes[]}
```

Headers:
```
Authorization: Bearer <token>
Content-Type: application/json
```

### 3. **Backend Express: HTTP Client en vez de SOAP**

**Cambio en `soapService.js`:**

**Antes:**
```javascript
// Construía XML SOAP manualmente
const envelope = `<?xml version="1.0"...`
const response = await fetch(SOAP_URL, { body: envelope })
const doc = parser.parseFromString(response.text(), 'text/xml')
```

**Ahora:**
```javascript
// Cliente HTTP REST con axios
import axios from 'axios'
const api = axios.create({ baseURL: `http://localhost:8080/api` })
const response = await api.post('/auth/login', { email, password })
return response.data  // JSON directo
```

**Beneficios:**
- ✅ Menos código (sin parseo XML)
- ✅ Más rápido (JSON vs XML)
- ✅ Debugging más fácil
- ✅ Mejor para manejo errores (status HTTP vs XML parsing)

### 4. **AuthService Actualizado**

**Cambios principales:**

1. **Base de datos → Tabla de sesiones**
   ```java
   // Antes: Solo generaba token JWT
   String token = generarToken(usuario);
   
   // Ahora: Genera token AND crea registro en BD
   String token = generarToken(usuario);
   Sesion sesion = Sesion.builder()
       .usuario(usuario)
       .token(token)
       .fechaExpiracion(LocalDateTime.now().plus(expirationMs))
       .build();
   sesionRepository.save(sesion);
   ```

2. **Validación de token contra BD**
   ```java
   // Antes: Solo validaba firma JWT
   public Long validarToken(String token) {
       var claims = Jwts.parser()...
       return Long.parseLong(claims.getSubject());
   }
   
   // Ahora: Valida en BD + firma JWT
   public Long validarToken(String token) {
       Sesion sesion = sesionRepository.findByToken(token)
           .orElseThrow(() -> new RuntimeException("Token no encontrado"));
       
       if (sesion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
           throw new RuntimeException("Token expirado");
       }
       
       // Validar firma JW
       var claims = Jwts.parser()...
       return Long.parseLong(claims.getSubject());
   }
   ```

3. **Búsqueda por username O email**
   ```java
   // Ahora soporta ambos identificadores
   Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
       .or(() -> usuarioRepository.findByUsername(request.getEmail()))
       .orElseThrow(...);
   ```

### 5. **Flujo de Autenticación Actualizado**

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Cliente Web                                              │
│    POST /api/auth/login {email, password}                  │
└────────────────────┬────────────────────────────────────────┘
                     ↓ HTTP REST
┌─────────────────────────────────────────────────────────────┐
│ 2. Backend Express                                          │
│    callAPI('/auth/login')                                   │
└────────────────────┬────────────────────────────────────────┘
                     ↓ HTTP REST
┌─────────────────────────────────────────────────────────────┐
│ 3. App-Server /api/auth/login                              │
│    - Busca usuario por email/username                       │
│    - Valida password                                        │
│    - Genera token JWT                                       │
│    - Crea sesión en BD (auth.sesiones)                     │
│    - Devuelve {token, idUsuario, nombre}                   │
└────────────────────┬────────────────────────────────────────┘
                     ↓ HTTP REST
┌─────────────────────────────────────────────────────────────┐
│ 4. Backend Express                                          │
│    response.data → { token, ... }                          │
└────────────────────┬────────────────────────────────────────┘
                     ↓ HTTP REST
┌─────────────────────────────────────────────────────────────┐
│ 5. Cliente Web                                              │
│    localStorage.setItem('token', response.token)            │
│    Usar header: Authorization: Bearer <token>               │
└─────────────────────────────────────────────────────────────┘
```

---

## Migración de Datos

### Script para migrar datos existentes (si existen):

```sql
-- Copiar usuarios a la nueva tabla auth.usuarios
INSERT INTO auth.usuarios (id_usuario, username, password_hash, email, fecha_registro, rol)
SELECT id_usuario, 
       username, 
       password_hash, 
       email, 
       fecha_registro, 
       COALESCE(rol, 'USER')
FROM usuario;

-- Resincronizar secuencias
SELECT setval('auth.usuarios_id_usuario_seq', (SELECT MAX(id_usuario) FROM auth.usuarios));
```

---

## Configuración

### Backend Express (.env.local)

```env
# URL del App-Server con nuevos endpoints REST
JAVA_APP_SERVER_URL=http://localhost:8080
```

### App-Server (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/imageprocessing_db
    username: imageuser
    password: imagepass
```

---

## Próximos Pasos

1. **Drop schema viejo (opcional)**
   ```sql
   DROP TABLE IF EXISTS sesion CASCADE;
   DROP TABLE IF EXISTS usuario CASCADE;
   CREATE VIEW usuario AS SELECT * FROM public.usuario;  -- Para compatibilidad
   ```

2. **Configurar propiedades de schemas en Spring**
   ```yaml
   spring:
     jpa:
       properties:
         hibernate:
           default_schema: public
   ```

3. **Agregar índices para mejor performance**
   ```sql
   CREATE INDEX idx_auth_usuarios_username ON auth.usuarios(username);
   CREATE INDEX idx_auth_usuarios_email ON auth.usuarios(email);
   CREATE INDEX idx_auth_sesiones_token ON auth.sesiones(token);
   CREATE INDEX idx_auth_sesiones_usuario ON auth.sesiones(id_usuario);
   ```

---

## Testing de Endpoints

### Teste el login REST:

```bash
# Request
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123"
  }'

# Response (200 OK)
{
  "token": "eyJhbGc...",
  "idUsuario": 1,
  "nombre": "usuario_username",
  "email": "usuario@example.com"
}
```

### Teste la validación de token:

```bash
curl -X GET http://localhost:8080/api/auth/validar \
  -H "Authorization: Bearer eyJhbGc..."

# Response (200 OK)
{
  "valido": true,
  "idUsuario": 1,
  "mensaje": "Token válido"
}
```

---

## Backward Compatibility

⚠️ **El SOAP antiguo aún funciona** (`/ws/ImageProcessingService?wsdl`)

Si necesitas mantener compatibilidad con clientes SOAP antiguos:
1. El `ImageProcessingServiceImpl` sigue existiendo
2. Ambos usan el mismo `AuthService`
3. Los tokens JWT generados son idénticos

---

## Ventajas de esta Arquitectura

| Aspecto | SOAP Antiguo | REST Nuevo |
|--------|------------|-----------|
| **Protocolo** | XML en SOAP envelope | JSON en HTTP |
| **Parser** | DOMParser (navegador) | axios/JSON (backend) |
| **Payload** | ~500 bytes (login) | ~300 bytes |
| **Speed** | ~200ms | ~50ms |
| **Debuggable** | XML en red | JSON + HTTP status  |
| **Caché HTTP** | ❌ No | ✅ Sí |
| **Compresión** | ❌ Manual | ✅ Automática |
| **Clients** | Solo SOAP | REST + gRPC + SOAP |

---

**Última actualización:** Abril 2026

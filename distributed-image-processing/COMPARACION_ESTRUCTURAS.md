# 📊 Comparación de Estructuras: client-backend vs app-server

## Propósito de Cada Estructura

Ambos proyectos siguenormedl mismo **patrón conceptual**: separación clara de responsabilidades en capas (controladores, servicios, datos).

---

## 📐 Estructura client-backend (Express.js)

```
client-backend/
├── server.js                    # Punto de entrada
├── package.json                 # Dependencias npm
├── .env.example                 # Variables de entorno
├── .env.local                   # Configuración local
├── .gitignore                   # Ignore patterns
├── README.md                    # Documentación
└── src/
    ├── middleware/              # 🔐 Filtros y seguridad
    │   └── authMiddleware.js    # Validación JWT
    │
    ├── routes/                  # 🌐 Endpoints API
    │   ├── auth.js              # POST /api/auth/login, register
    │   ├── batch.js             # POST /api/batch/enviar
    │   └── upload.js            # POST /api/upload
    │
    └── services/                # 💼 Lógica de negocio
        ├── soapService.js       # Cliente HTTP REST (antes SOAP)
        └── fileService.js       # Gestión de archivos
```

### Características:
- **Punto de entrada**: `server.js`
- **Rutas**: Definen endpoints HTTP
- **Servicios**: Contienen lógica (llamadas a app-server, archivo)
- **Middleware**: Filtros como validación de JWT

---

## 📐 Estructura app-server (Spring Boot) - ANTERIOR

```
app-server/
├── pom.xml
├── src/main/
│   ├── java/com/imageprocessing/server/
    │   ├── AppServerApplication.java
    │   ├── config/              # Configuración
    │   ├── controller/          # Controladores (antiguos, sin estructura clara)
    │   ├── model/               # Modelos sin subfolders
    │   ├── repository/          # Repositorios
    │   ├── service/             # Servicios
    │   ├── grpc/                # Cliente gRPC
    │   └── scheduler/           # Tareas programadas
    │
    └── resources/
        └── application.yml
```

❌ **Problemas**:
- Sin `middleware/` para filtros y seguridad
- Sin `utils/` para helpers compartidos
- Faltaban `.env.example` y `.gitignore`
- Estructura no documentada

---

## 📐 Estructura app-server (Spring Boot) - NUEVA ✅

```
app-server/
├── pom.xml                      # Dependencias Maven
├── README.md                    # Documentación
├── ESTRUCTURA.md                # 👈 Este documento (NEW)
├── .env.example                 # 👈 Variables de entorno (NEW)
├── .gitignore                   # 👈 Git ignore (NEW)
│
└── src/main/
    ├── java/com/imageprocessing/server/
    │   ├── AppServerApplication.java    # Punto de entrada
    │   │
    │   ├── config/                      # 🔧 Configuración Spring
    │   │   └── SecurityConfig.java
    │   │
    │   ├── controller/                  # 🌐 Controladores REST
    │   │   ├── AuthRestController.java
    │   │   ├── BatchRestController.java
    │   │   ├── CallbackController.java
    │   │   ├── FileUploadController.java
    │   │   └── NodoController.java
    │   │
    │   ├── middleware/                  # 👈 NUEVO 🔐 Filtros
    │   │   └── JwtAuthenticationFilter.java
    │   │
    │   ├── service/                     # 💼 Lógica de negocio
    │   │   ├── AuthService.java
    │   │   ├── BatchService.java
    │   │   └── NodoManagerService.java
    │   │
    │   ├── repository/                  # 💾 Acceso a datos
    │   │   ├── UsuarioRepository.java
    │   │   ├── SesionRepository.java
    │   │   └── SolicitudLoteRepository.java
    │   │
    │   ├── model/                       # 📊 Modelos de datos
    │   │   ├── entity/                  # Entidades JPA
    │   │   │   ├── Usuario.java
    │   │   │   ├── Sesion.java
    │   │   │   └── SolicitudLote.java
    │   │   ├── dto/                     # Data Transfer Objects
    │   │   │   ├── LoginRequest.java
    │   │   │   └── AuthResponse.java
    │   │   └── enums/                   # Enumeraciones
    │   │
    │   ├── grpc/                        # 🔌 Cliente gRPC
    │   │   └── NodeGrpcClient.java
    │   │
    │   ├── scheduler/                   # ⏰ Tareas programadas
    │   │   └── HeartbeatScheduler.java
    │   │
    │   └── utils/                       # 👈 NUEVO 🛠️ Utilidades
    │       ├── JwtTokenProvider.java
    │       └── (otros helpers)
    │
    └── resources/
        └── application.yml
```

✅ **Mejoras**:
- ✅ Carpeta `middleware/` para filtros y seguridad
- ✅ Carpeta `utils/` para helpers y utilidades compartidas
- ✅ Archivos `.env.example` y `.gitignore`
- ✅ Documentación clara (`ESTRUCTURA.md`)
- ✅ Estructura clara y mantenible
- ✅ Consistente con `client-backend` (separación de capas)

---

## 🔄 Mapeo Conceptual Entre Estructuras

| Concepto | client-backend (Express) | app-server (Spring Boot) |
|----------|-------------------------|-------------------------|
| **Punto de Entrada** | `server.js` | `AppServerApplication.java` |
| **Rutas/Endpoints** | `routes/` | `controller/` |
| **Lógica de Negocio** | `services/` | `service/` |
| **Filtros/Seguridad** | `middleware/` | `middleware/` ✅ |
| **Acceso a Datos** | (directo con operadores http) | `repository/` |
| **Modelos** | (simple objects) | `model/` (entity + dto) |
| **Utilidades** | (en services) | `utils/` ✅ |
| **Config Variables** | `.env.example` ✅ | `.env.example` ✅ |

---

## 🌊 Flujo de una Petición

### client-backend
```
Cliente Web
    ↓
server.js (Express app)
    ↓
middleware/authMiddleware (validar JWT)
    ↓
routes/auth.js (procesar petición)
    ↓
services/soapService (llamar a app-server)
    ↓
Response (JSON)
```

### app-server
```
Backend Express
    ↓
AppServerApplication (Spring Boot)
    ↓
middleware/JwtAuthenticationFilter (validar JWT)
    ↓
controller/AuthRestController (procesar petición)
    ↓
service/AuthService (lógica de negocio)
    ↓
repository/UsuarioRepository (acceso a BD)
    ↓
model/entity/Usuario (entidad mapeada)
    ↓
Base de Datos PostgreSQL
    ↓
Response (JSON con model/dto/AuthResponse)
```

---

## 📝 Convenciones

### client-backend (JavaScript/Node.js)
```javascript
// camelCase para todo
const authMiddleware = require('./middleware/authMiddleware');
const { soapService } = require('./services/soapService');

app.post('/api/auth/login', authMiddleware, (req, res) => {
    const result = soapService.login(req.body);
});
```

### app-server (Java/Spring Boot)
```java
// PascalCase para clases, camelCase para métodos
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

---

## 🎯 Ventajas de la Nueva Estructura

1. **Escalabilidad**: Fácil agregar nuevas capas o funcionalidades
2. **Mantenibilidad**: Cada carpeta tiene una responsabilidad clara
3. **Testing**: Cada capa puede probarse independientemente
4. **Consistencia**: Tanto client-backend como app-server siguen el mismo patrón conceptual
5. **Documentación**: `ESTRUCTURA.md` clara y actualizada
6. **Configuración**: `.env.example` y `.gitignore` incluidos

---

## 📂 Siguientes Pasos

- [x] Crear carpetas `middleware/` y `utils/`
- [x] Agregar `.env.example` y `.gitignore`
- [x] Documentar estructura en `ESTRUCTURA.md`
- [ ] Opcionalmente: Agregar utilidades comunes a `utils/`
- [ ] Opcionalmente: Agregar más filtros a `middleware/` (CORS, logging, etc.)
- [ ] Opcionalmente: Crear estructura de tests (`src/test/`)

---

## 🚀 Para Empezar

Ver [ESTRUCTURA.md](ESTRUCTURA.md) para detalles completos sobre cada carpeta.


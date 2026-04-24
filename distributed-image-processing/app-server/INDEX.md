# 📖 Índice de Navegación - App-Server v2.0

**Bienvenido al App-Server v2.0** - Servidor de aplicación para procesamiento distribuido de imágenes.

Este índice te ayuda a navegar por el proyecto y encontrar rápidamente lo que necesitas.

---

## 🚀 ¿Por Dónde Empiezo?

### Si eres nuevo en el proyecto
1. **Primero**: Lee [README.md](README.md) - Introducción general (5 min)
2. **Luego**: Sigue [QUICKSTART.md](QUICKSTART.md) - Guía de 5 pasos para empezar (5 min)
3. **Finalmente**: Consulta [ESTRUCTURA.md](ESTRUCTURA.md) - Detalles arquitectura (10 min)

### Si quieres entender la arquitectura
- [ESTRUCTURA.md](ESTRUCTURA.md) - Explicación de carpetas y patrones (muy detallado)
- Diagrama de flujo en [README.md](README.md#-arquitectura)

### Si necesitas integrar con Workers
- [RMI_INTEGRATION.md](RMI_INTEGRATION.md) - Guía completa de RMI

### Si buscas ayuda rápida  
- [QUICKSTART.md](QUICKSTART.md) - Troubleshooting al final

### Si quieres un resumen
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - Visión general del proyecto

---

## 📁 Estructura de Carpetas

```
app-server2/
│
├── 📄 README.md                 ← START HERE (5 min)
├── 📄 QUICKSTART.md             ← Start the server (5 min)
├── 📄 ESTRUCTURA.md             ← Understand architecture (10 min)
├── 📄 RMI_INTEGRATION.md        ← Worker integration guide
├── 📄 PROJECT_SUMMARY.md        ← Project overview
├── 📄 .env.example              ← Environment template
├── 📄 pom.xml                   ← Maven dependencies
│
└── src/main/java/com/imageprocessing/server/
    │
    ├── 🔧 config/
    │   └── SecurityConfig.java          → Spring Security setup
    │
    ├── 🌐 controller/                   → REST Endpoints
    │   ├── AuthRestController.java      → /api/auth/* endpoints
    │   ├── BatchRestController.java     → /api/batch/* endpoints
    │   ├── NodoRestController.java      → /api/nodos/* endpoints
    │   └── HealthController.java        → /api/health endpoint
    │
    ├── 🔐 middleware/
    │   └── JwtAuthenticationFilter.java → JWT validation for all requests
    │
    ├── 💼 service/                      → Business Logic
    │   ├── AuthService.java             → User authentication & sessions
    │   ├── BatchService.java            → Batch processing logic
    │   └── NodoService.java             → Worker node management
    │
    ├── 💾 repository/                   → Database Access (JPA)
    │   ├── UsuarioRepository.java
    │   ├── SesionRepository.java
    │   ├── SolicitudLoteRepository.java
    │   ├── ImagenSolicitudRepository.java
    │   ├── TransformacionRepository.java
    │   ├── NodoTrabajadorRepository.java
    │   └── LogTrabajoRepository.java
    │
    ├── 📊 model/                        → Data Models
    │   ├── entity/                      → JPA Entities (7 total)
    │   │   ├── Usuario.java
    │   │   ├── Sesion.java
    │   │   ├── SolicitudLote.java
    │   │   ├── ImagenSolicitud.java
    │   │   ├── Transformacion.java
    │   │   ├── NodoTrabajador.java
    │   │   └── LogTrabajo.java
    │   │
    │   ├── dto/                         → DTOs (7 total)
    │   │   ├── LoginRequest.java
    │   │   ├── RegisterRequest.java
    │   │   ├── AuthResponse.java
    │   │   ├── BatchRequest.java
    │   │   ├── BatchResponse.java
    │   │   ├── ProgressResponse.java
    │   │   └── TransformacionDTO.java
    │   │
    │   └── enums/                       → Enumerations (expandible)
    │
    ├── 🔌 rmi/
    │   └── RmiClient.java               → RMI client for worker nodes
    │
    ├── 🛠️ utils/
    │   └── JwtTokenProvider.java        → JWT generation & validation
    │
    └── AppServerApplication.java        ← Main Spring Boot application
```

---

## 📚 Documentos y Sus Propósitos

| Archivo | Propósito | Tiempo |
|---------|-----------|--------|
| **README.md** | Introducción general, requisitos, inicio | 5 min |
| **QUICKSTART.md** | Guía rápida de 5 pasos + testing | 5 min |
| **ESTRUCTURA.md** | Arquitectura detallada, patrones, flujos | 15 min |
| **RMI_INTEGRATION.md** | Integración RMI con worker-nodes | 10 min |
| **PROJECT_SUMMARY.md** | Resumen ejecutivo del proyecto | 5 min |
| **.env.example** | Template de configuración | Referencia |
| **pom.xml** | Dependencias Maven | Referencia |

---

## 🎯 Casos de Uso Comunes

### "Quiero iniciar el servidor"
→ [QUICKSTART.md](QUICKSTART.md) paso 4-5

### "No entiendo la estructura"
→ [ESTRUCTURA.md](ESTRUCTURA.md)

### "¿Cómo agrego un endpoint nuevo?"
→ [ESTRUCTURA.md](ESTRUCTURA.md#extensión-de-la-estructura)

### "¿Cómo integro con workers?"
→ [RMI_INTEGRATION.md](RMI_INTEGRATION.md)

### "¿Qué hace cada carpeta?"
→ [ESTRUCTURA.md](ESTRUCTURA.md#descripción-de-carpetas)

### "¿Quiero probar los endpoints?"
→ [QUICKSTART.md](QUICKSTART.md#-pruebas-rápidas)

### "Tengo un error"
→ [QUICKSTART.md](QUICKSTART.md#-troubleshooting-rápido)

### "Necesito entender la arquitectura general"
→ [README.md](README.md#-arquitectura) + [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

---

## 🔍 Búsqueda Rápida por Funcionalidad

### Autenticación
- **Lógica**: [service/AuthService.java](src/main/java/com/imageprocessing/server/service/AuthService.java)
- **Endpoints**: [controller/AuthRestController.java](src/main/java/com/imageprocessing/server/controller/AuthRestController.java)
- **Datos**: [model/entity/Usuario.java](src/main/java/com/imageprocessing/server/model/entity/Usuario.java)
- **DTOs**: [model/dto/AuthResponse.java](src/main/java/com/imageprocessing/server/model/dto/AuthResponse.java)

### Procesamiento de Lotes
- **Lógica**: [service/BatchService.java](src/main/java/com/imageprocessing/server/service/BatchService.java)
- **Endpoints**: [controller/BatchRestController.java](src/main/java/com/imageprocessing/server/controller/BatchRestController.java)
- **Datos**: [model/entity/SolicitudLote.java](src/main/java/com/imageprocessing/server/model/entity/SolicitudLote.java)
- **DTOs**: [model/dto/BatchResponse.java](src/main/java/com/imageprocessing/server/model/dto/BatchResponse.java)

### Gestión de Nodos
- **Lógica**: [service/NodoService.java](src/main/java/com/imageprocessing/server/service/NodoService.java)
- **Endpoints**: [controller/NodoRestController.java](src/main/java/com/imageprocessing/server/controller/NodoRestController.java)
- **Datos**: [model/entity/NodoTrabajador.java](src/main/java/com/imageprocessing/server/model/entity/NodoTrabajador.java)
- **RMI Client**: [rmi/RmiClient.java](src/main/java/com/imageprocessing/server/rmi/RmiClient.java)

### Seguridad
- **Configuración**: [config/SecurityConfig.java](src/main/java/com/imageprocessing/server/config/SecurityConfig.java)
- **Filtro JWT**: [middleware/JwtAuthenticationFilter.java](src/main/java/com/imageprocessing/server/middleware/JwtAuthenticationFilter.java)
- **JWT Utils**: [utils/JwtTokenProvider.java](src/main/java/com/imageprocessing/server/utils/JwtTokenProvider.java)

### Base de Datos
- **Usuario**: [repository/UsuarioRepository.java](src/main/java/com/imageprocessing/server/repository/UsuarioRepository.java)
- **Sesión**: [repository/SesionRepository.java](src/main/java/com/imageprocessing/server/repository/SesionRepository.java)
- **Lotes**: [repository/SolicitudLoteRepository.java](src/main/java/com/imageprocessing/server/repository/SolicitudLoteRepository.java)

---

## 🔗 Relación Entre Archivos

### Flujo de Autenticación
```
1. AuthRestController.login()
   ↓
2. AuthService.login()
   ↓
3. UsuarioRepository.findByUsername()
   ↓
4. JwtTokenProvider.generateToken()
   ↓
5. SesionRepository.save()
   ↓
6. AuthResponse (DTO)
```

### Flujo de Batch Processing
```
1. BatchRestController.enviarLote()
   ↓
2. AuthService.validarToken()
   ↓
3. BatchService.crearLote()
   ↓
4. SolicitudLoteRepository.save()
   ↓
5. TransformacionRepository.save()
   ↓
6. BatchResponse (DTO)
```

### Flujo de Nodos RMI
```
1. NodoRestController.registrarNodo()
   ↓
2. NodoService.registrarNodo()
   ↓
3. NodoTrabajadorRepository.save()
   ↓
4. RmiClient.obtenerServicioRemoto()
   ↓
5. Worker procesamiento
```

---

## 📊 Vista de Capas

```
┌─────────────────┐
│     HTTP        │  ← REST API (puerto 8080)
├─────────────────┤
│  Controllers    │  ← AuthRestController, BatchRestController, etc.
├─────────────────┤
│  Middleware     │  ← JwtAuthenticationFilter
├─────────────────┤
│  Services       │  ← AuthService, BatchService, NodoService
├─────────────────┤
│  Repositories   │  ← UsuarioRepository, SesionRepository, etc.
├─────────────────┤
│  Models (JPA)   │  ← Usuario, Sesion, SolicitudLote, etc.
├─────────────────┤
│  PostgreSQL     │  ← Base de datos (imageprocessing_db)
└─────────────────┘

Side: RMI ↔ Workers
      (RmiClient.java)
```

---

## 🎓 Patrones Encontrados

### 1. Repository Pattern
- `UsuarioRepository`, `SesionRepository`, etc.
- Abstrae acceso a BD

### 2. Service Layer Pattern
- `AuthService`, `BatchService`, `NodoService`
- Contiene lógica de negocio

### 3. DTO Pattern
- `LoginRequest`, `AuthResponse`, `BatchRequest`, etc.
- Desacopla API de BD

### 4. Dependency Injection
- `@RequiredArgsConstructor` + `@Autowired`
- Inyección automática

### 5. Builder Pattern
- `Usuario.builder()`, `Sesion.builder()`, etc.
- Constructores fluidos

---

## ✅ Checklist de Lectura Recomendada

Para comprender completamente el proyecto:

- [ ] Leer README.md (5 min)
- [ ] Ejecutar QUICKSTART.md (10 min)
- [ ] Revisar ESTRUCTURA.md (15 min)
- [ ] Explorar cada carpeta source (30 min)
- [ ] Entender flujos en PROJECT_SUMMARY.md (10 min)
- [ ] (Opcional) Leer RMI_INTEGRATION.md si trabajas con workers

---

## 🚀 Comienza Ahora

### Opción 1: Rápido (15 min)
1. README.md
2. QUICKSTART.md
3. Ejecutar servidor

### Opción 2: Profundo (1 hora)
1. README.md
2. ESTRUCTURA.md
3. Explorar código fuente
4. QUICKSTART.md
5. Ejecutar y probar

### Opción 3: Solo quiero hacerlo andar
1. QUICKSTART.md paso por paso
2. `mvn spring-boot:run`
3. ¡Listo!

---

## 📞 Ayuda Rápida

| Pregunta | Respuesta |
|----------|-----------|
| "¿Cómo inicio?" | [QUICKSTART.md](QUICKSTART.md) |
| "¿Cómo agrego un endpoint?" | [ESTRUCTURA.md](ESTRUCTURA.md#extensión-de-la-estructura) |
| "¿Qué hace X clase?" | Ver comentarios JavaDoc en el archivo |
| "¿Dónde está Y funcionalidad?" | Usa tabla "Búsqueda Rápida" arriba |
| "Tengo un error" | [QUICKSTART.md](QUICKSTART.md#-troubleshooting-rápido) |
| "¿Cómo integro workers?" | [RMI_INTEGRATION.md](RMI_INTEGRATION.md) |

---

## 📈 Navegación Recomendada

**Semana 1: Comprensión**
- [ ] Leer todos los .md
- [ ] Explorar estructura
- [ ] Ejecutar servidor
- [ ] Probar endpoints

**Semana 2: Integración**
- [ ] Integrar con client-backend
- [ ] Integrar con client-web
- [ ] Crear tests unitarios
- [ ] Documentar cambios

**Semana 3+: Expansión**
- [ ] Agregar nuevos endpoints
- [ ] Integrar workers RMI
- [ ] Optimizaciones
- [ ] Deployment

---

## 🎯 Objetivo Final

Entender y poder:
1. ✅ Compilar el proyecto
2. ✅ Ejecutar sin errores
3. ✅ Probar los endpoints
4. ✅ Entender la arquitectura
5. ✅ Agregar nuevas funcionalidades
6. ✅ Integrar con otros componentes

---

**Bienvenido al desarrollo del Sistema Distribuido de Procesamiento de Imágenes** 🚀

---

**Última actualización**: 2026-04-21  
**Versión**: 2.0.0  
**Estado**: ✅ Completado y Documentado

# 🚀 Quick Start - App-Server v2.0

Guía rápida para poner en funcionamiento el app-server en 5 minutos.

## ⚡ Pasos Rápidos

### 1. Preparar Ambiente (1 min)

```bash
# Ir a carpeta del proyecto
cd app-server2

# Crear archivo .env desde template
cp .env.example .env
```

Editar `.env` con valores correctos:
```bash
DB_HOST=localhost
DB_PORT=5416
DB_NAME=imageprocessing_db
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=tu-clave-secreta-segura
```

### 2. Base de Datos (1 min)

Asegurar que PostgreSQL está activo:

```bash
# Ver estado (en una terminal aparte)
psql -U postgres -c "SELECT version();"
```

La BD `imageprocessing_db` debe existir con esquemas `auth` y `public`.

### 3. Compilar (2 min)

```bash
mvn clean package
```

Si hay errores, verificar:
- Java 17 o superior: `java -version`
- Maven 3.8.0+: `mvn -version`

### 4. Ejecutar (1 min)

```bash
# Opción A: Con Maven
mvn spring-boot:run

# Opción B: Con Java directo (después de compilar)
java -jar target/app-server-2.0.0.jar
```

Verás logs de Spring Boot. Cuando veas:
```
Started AppServerApplication in X.XXX seconds
```

¡El servidor está activo!

### 5. Verificar (Sin tiempo)

En otra terminal:

```bash
# Health check
curl http://localhost:8080/api/health

# Respuesta esperada:
# {"status":"UP","message":"App-Server is running"}
```

✅ **¡Listo en 5 minutos!**

---

## 🧪 Pruebas Rápidas

### Test 1: Registrar Usuario

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

Respuesta esperada (guardar el token):
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "idUsuario": 1,
  "username": "testuser",
  "email": "test@example.com",
  "rol": "USER"
}
```

### Test 2: Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### Test 3: Validar Token

```bash
# Reemplazar TOKEN con el obtenido en registrar
curl -X GET http://localhost:8080/api/auth/validar \
  -H "Authorization: Bearer TOKEN"
```

Respuesta:
```json
{"message":"Token válido"}
```

### Test 4: Registrar Nodo

```bash
curl -X POST http://localhost:8080/api/nodos/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "worker-1",
    "host": "localhost",
    "puertoRmi": 9090
  }'
```

### Test 5: Crear Lote

```bash
# Reemplazar TOKEN con el obtenido
curl -X POST http://localhost:8080/api/batch/enviar \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Test Batch",
    "descripcion": "Batch de prueba",
    "transformaciones": [
      {
        "tipo": "RESIZE",
        "parametros": "{\"width\": 800, \"height\": 600}"
      }
    ]
  }'
```

Respuesta:
```json
{
  "idLote": 1,
  "titulo": "Test Batch",
  "estado": "PENDIENTE",
  "cantidadImagenes": 0,
  "progreso": 0
}
```

---

## 🔧 Configuración Avanzada

### Cambiar Puerto

En `.env`:
```bash
SERVER_PORT=8090
```

O en `application.yml`:
```yaml
server:
  port: 8090
```

### Aumentar Timeout

En `application.yml`:
```yaml
server:
  servlet:
    session:
      timeout: 1h
```

### Debug Logging

En `application.yml`:
```yaml
logging:
  level:
    com.imageprocessing: DEBUG
    org.springframework.security: DEBUG
```

---

## 🐛 Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| `Port 8080 already in use` | Cambiar puerto en `.env` o `.gitignore` |
| `Connection refused` | PostgreSQL no está activo, iniciar `docker-compose` |
| `Database does not exist` | Crear BD: `createdb imageprocessing_db` |
| `java.lang.NoClassDefFoundError` | Falta compilar: `mvn clean compile` |
| `Token invalid` | Token expirado (24h), hacer nuevo login |
| `CORS error` | Frontend debe estar en `http://localhost:3000` o `http://localhost:3001` |

---

## 📂 Estructura de Carpetas

Después de compilar, verás:

```
app-server2/
├── target/                          # Compilado
│   ├── app-server-2.0.0.jar        # JAR ejecutable
│   └── classes/                     # Clases compiladas
├── .env                             # Configuración local
├── .env.example                     # Template
└── src/                             # Código fuente
```

---

## 📚 Documentación Completa

Para más detalles, ver:
- `README.md` - Introducción general
- `ESTRUCTURA.md` - Arquitectura y carpetas
- `pom.xml` - Dependencias Maven

---

## 🎯 Próximos Pasos

1. ✅ App-Server activo
2. → Integrar con cliente web (frontend)
3. → Integrar con backend express (cliente-backend)
4. → Configurar worker-nodes (RMI)
5. → Pruebas end-to-end

---

## 💬 Tips

- Mantener terminal con logs visible mientras desarrollas
- Usar `curl` o Postman para probar endpoints
- Guardar tokens en variable: `TOKEN=$(curl ... | jq '.token')`
- Ver error detallado: `tail -f target/app-server.log`

---

**¡Ahora sí, a desarrollo!** 🚀


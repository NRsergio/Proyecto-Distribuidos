# 🚀 Quick Start - Sistema con HTTP REST

## ⚡ Inicio Rápido (5 minutos)

### Paso 1: Preparar la Base de Datos
```bash
cd distributed-image-processing
docker-compose down -v  # Limpia volúmenes anteriores si existen
docker-compose up -d

# Verificar que PostgreSQL está corriendo
docker ps | grep imageprocessing_db
```

✅ **PostgreSQL corriendo en puerto 5432**

---

### Paso 2: Compilar App-Server
```bash
cd app-server
mvn clean install -DskipTests
```

✅ **Archivos Java compilados**

---

### Paso 3: Levantar App-Server REST HTTP
```bash
cd app-server
mvn spring-boot:run
```

**Esperado en consola:**
```
- Started AppServerApplication
- Tomcat started on port 8080
```

✅ **App-Server corriendo en :8080 con endpoints REST**

**Verificar:**
```bash
curl http://localhost:8080/api/auth/validar
# Debería responder 401 (sin token)
```

---

### Paso 4: Levantar Worker-Node (en otra terminal)
```bash
cd worker-node
mvn spring-boot:run
```

✅ **Worker-Node corriendo en :9090 (gRPC)**

---

### Paso 5: Levantar Backend Express (en otra terminal)
```bash
cd client-backend

# Primera vez
npm install

# Siempre
npm run dev
```

**Esperado:**
```
✅ Servidor escuchando en http://localhost:3001
📡 API disponible en http://localhost:3001/api
🏥 Health check en http://localhost:3001/health
```

✅ **Backend Express corriendo en :3001**

**Verificar:**
```bash
curl http://localhost:3001/health
# Response: { "status": "ok", "javaAppServer": "..." }
```

---

### Paso 6: Levantar Cliente Web (en otra terminal)
```bash
cd client-web
npm install  # si es primera vez
npm run dev
```

**Esperado:**
```
➜  VITE v4.x.x  ready in XXX ms
➜  Local:   http://localhost:3000
```

✅ **Cliente Web corriendo en :3000**

---

## ✅ Verificación Rápida

### 1. Health Checks
```bash
# Backend Express
curl http://localhost:3001/health
# ✅ Responde: { "status": "ok" }

# App-Server
curl http://localhost:8080/api/auth/validar  
# ✅ Responde 401 (sin token - esperado)
```

### 2. Login REST
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "password123"
  }'

# Si error "Credenciales invalidas": crear usuario primero ↓
```

### 3. Registrar Usuario (primero)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "password123",
    "nombre": "Test User"
  }'

# ✅ Response: { "token": "...", "idUsuario": 1, ... }
```

### 4. Luego Login
```bash
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "password123"
  }' | jq -r '.token')

echo "Token: $TOKEN"

# ✅ Debería imprimir el token JWT
```

### 5. Validar Token
```bash
curl -X GET http://localhost:8080/api/auth/validar \
  -H "Authorization: Bearer $TOKEN"

# ✅ Response: { "valido": true, "idUsuario": 1, "mensaje": "Token válido" }
```

---

## 🎯 En el Navegador

1. Abre **http://localhost:3000**
2. Haz clic en **Register** (primera vez)
3. Completa formulario:
   - Username/Email: `test@example.com`
   - Password: `password123`
   - Name: `Test User`
4. Click en **Register**
5. Deberías estar authenticado y ver **Dashboard**

✅ **Sistema operativo**

---

## 📊 Puertos en Uso

| Servicio | Puerto | URL | Status |
|----------|--------|-----|--------|
| Cliente Web | 3000 | http://localhost:3000 | ✅ |
| Backend Express | 3001 | http://localhost:3001/api | ✅ |
| App-Server REST | 8080 | http://localhost:8080/api | ✨ NUEVO |
| App-Server SOAP | 8080 | http://localhost:8080/ws | ✅ Legacy |
| Worker-Node gRPC | 9090 | localhost:9090 | ✅ |
| PostgreSQL | 5432 | localhost:5432 | ✅ |

---

## 🔄 Diferencia clave: SOAP → REST

### SOAP (Antiguo)
```javascript
// Cliente construía XML
const envelope = `<?xml version="1.0"...`
const response = await fetch('/ws/ImageProcessingService', { body: envelope })
const doc = parser.parseFromString(response.text(), 'text/xml')
```

### REST (Nuevo)
```javascript
// Cliente encía JSON
const response = await axios.post('/api/auth/login', { email, password })
const data = response.data  // Ya es JSON
```

**Resultado:** Código más simple, más rápido, fácil de debuggar.

---

## 🐛 Troubleshoot

### Error: "Connection refused" cuando login
```
❌ Error: Cannot connect to localhost:8080
```
**Solución:** Asegurate que App-Server está corriendo
```bash
# Terminal dedicada
cd app-server
mvn spring-boot:run
```

### Error: "Table auth.usuarios not found"
```
❌ ERROR: relation "auth.usuarios" does not exist
```
**Solución:** Recrear BD con compose
```bash
docker-compose down -v
docker-compose up -d
# Espera a que PostgreSQL inicie completamente (~10s)
```

### Error: "Cannot POST /api/auth/login" en Express
```
❌ Error: Cannot POST /api/auth/login
```
**Solución:** Backend Express no puede conectar al App-Server
```bash
# Verificar App-Server está corriendo en :8080
curl http://localhost:8080/api/auth/validar

# Si no responde, iniciar App-Server
```

### Error: "SyntaxError: Unexpected token < in JSON"
```
❌ SyntaxError: Unexpected token < in JSON at position 0
```
**Causa:** Response es XML (SOAP) en lugar de JSON
**Solución:** Asegurate usar endpoints `.../api/...` NO `.../ws/...`

---

## 📚 Documentación Detallada

Si quieres más información:

1. **Arquitectura técnica**: [ARQUITECTURA_REST.md](ARQUITECTURA_REST.md)
2. **Guía completa de instalación**: [SETUP.md](SETUP.md)
3. **API Endpoints detallados**: [client-backend/README.md](client-backend/README.md)
4. **Guía de migración SOAP→REST**: [MIGRACION_SOAP_REST.md](MIGRACION_SOAP_REST.md)
5. **Resumen de cambios**: [RESUMEN_CAMBIOS.md](RESUMEN_CAMBIOS.md)

---

## 🎉 ¡Listo!

Has completado:
- ✅ Backend Express intermediario
- ✅ App-Server con endpoints REST HTTP
- ✅ BD con schema `auth` (usuarios + sesiones)
- ✅ Sistema operativo de extremo a extremo

**Próximas características:**
- [ ] Upload de imágenes
- [ ] Procesamiento de lotes
- [ ] Consulta de progreso
- [ ] Worker-nodes distribuidos

---

## 💡 Tips

**Para debuggear requests:**
```bash
# Instalar httpie (más legible que curl)
pip install httpie

# Usar así
http POST localhost:8080/api/auth/login email=test@example.com password=pass123
```

**Ver logs de la BD:**
```bash
docker logs imageprocessing_db
```

**Resetear todo y empezar de cero:**
```bash
docker-compose down -v
rm -rf client-backend/uploads
rm -f client-backend/node_modules
docker-compose up -d
```

---

**¡Éxito! 🚀**

Última actualización: Abril 21, 2026

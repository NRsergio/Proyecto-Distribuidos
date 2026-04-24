# Guía de Instalación y Ejecución

## Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│ Cliente Web (React/Vite)     :3000                              │
│ localhost:3000                                                  │
└────────────────┬────────────────────────────────────────────────┘
                 │ HTTP REST
                 ↓
┌─────────────────────────────────────────────────────────────────┐
│ Backend Intermedio (Express.js)  :3001                          │
│ localhost:3001/api                                              │
│ ├─ /auth/login                                                  │
│ ├─ /auth/register                                               │
│ ├─ /upload                                                      │
│ ├─ /batch/enviar                                                │
│ └─ /batch/progreso/:idLote                                      │
└────────────────┬────────────────────────────────────────────────┘
                 │ SOAP XML
                 ↓
┌─────────────────────────────────────────────────────────────────┐
│ App-Server (Spring Boot JAVA)  :8080                            │
│ Coordinador + Autenticación (BD relacional)                     │
│ └─ /ws/ImageProcessingService?wsdl                              │
└────────────────┬────────────────────────────────────────────────┘
                 │ gRPC
                 ↓
┌─────────────────────────────────────────────────────────────────┐
│ Worker-Nodes (Spring Boot JAVA)  :9090+ (gRPC)                 │
│                                   :8090+ (HTTP) Health/Admin    │
│ Procesamiento paralelo de imágenes                              │
└─────────────────────────────────────────────────────────────────┘
```

## Prerequisitos

- **Java 17+** (para app-server y worker-nodes)
- **Maven 3.9+** (compilar módulos Java)
- **Node.js 20+** (para client-web y client-backend)
- **npm** (gestor de paquetes Node)
- **PostgreSQL 14+** (o Docker)

## Paso 1: Levantar la Base de Datos

```bash
# En la raíz del proyecto
docker-compose up -d

# Verificar que está corriendo
docker ps | grep imageprocessing_db
```

**Credenciales:**
- Host: `localhost:5432`
- Database: `imageprocessing_db`
- User: `imageuser`
- Password: `imagepass`

## Paso 2: Compilar módulos Java

```bash
# Desde la raíz
cd distributed-image-processing

# Compilar todo (grpc-contracts, app-server, worker-node)
mvn clean install -DskipTests

# O compilar individualmente:
# cd grpc-contracts && mvn install
# cd app-server && mvn install
# cd worker-node && mvn install
```

## Paso 3: Levantar el App-Server

```bash
cd app-server
mvn spring-boot:run

# Debería mostrar:
# - Listening on http://localhost:8080
# - SOAP WSDL: http://localhost:8080/ws/ImageProcessingService?wsdl
```

## Paso 4: Levantar Worker-Nodes

En **terminals diferentes**:

```bash
# Terminal 1 - Nodo 01
cd worker-node
mvn spring-boot:run

# Terminal 2 - Nodo 02 (opcional)
cd worker-node
mvn spring-boot:run -Dnode.id=nodo-02 -Dnode.grpc-port=9091 -Dserver.port=8091
```

## Paso 5: Levantar Backend Intermedio Express

```bash
cd client-backend

# Instalar dependencias (primera vez)
npm install

# O solo copiar .env.local si ya existe
cp .env.example .env.local

# Ejecutar
npm run dev

# Debería mostrar:
# ✅ Servidor escuchando en http://localhost:3001
# 📡 API disponible en http://localhost:3001/api
```

## Paso 6: Levantar Cliente Web

```bash
cd client-web

# Instalar dependencias (primera vez)
npm install

# Copiar configuración (si no existe)
cp .env.example .env.local

# Ejecutar
npm run dev

# Debería mostrar:
# ➜  Local:   http://localhost:3000
# ➜  Press h to show help
```

---

## Verificación de Servicios

Una vez todo levantado, verificar que funciona:

```bash
# Health check del backend
curl http://localhost:3001/health

# WSDL del app-server
curl http://localhost:8080/ws/ImageProcessingService?wsdl

# Estado del cliente web
curl http://localhost:3000
```

---

## Orden de Levantamiento Recomendado

1. **PostgreSQL** (Docker)
   ```bash
   docker-compose up -d
   ```

2. **App-Server** (Spring Boot)
   ```bash
   cd app-server && mvn spring-boot:run
   ```

3. **Worker-Node(s)** (Spring Boot)
   ```bash
   cd worker-node && mvn spring-boot:run
   ```

4. **Backend Express** (Node.js)
   ```bash
   cd client-backend && npm run dev
   ```

5. **Cliente Web** (React/Vite)
   ```bash
   cd client-web && npm run dev
   ```

---

## Puertos Utilizados

| Servicio | Puerto | Protocolo | URL |
|----------|--------|-----------|-----|
| Cliente Web (React) | 3000 | HTTP | http://localhost:3000 |
| Backend Express | 3001 | HTTP REST | http://localhost:3001/api |
| App-Server | 8080 | HTTP/SOAP | http://localhost:8080 |
| Worker-Node 01 (gRPC) | 9090 | gRPC | localhost:9090 |
| Worker-Node 01 (HTTP) | 8090 | HTTP | http://localhost:8090 |
| Worker-Node 02 (gRPC) | 9091 | gRPC | localhost:9091 |
| Worker-Node 02 (HTTP) | 8091 | HTTP | http://localhost:8091 |
| PostgreSQL | 5432 | SQL | localhost:5432 |

---

## Troubleshooting

### Error: "Cannot find @vitejs/plugin-react"

```bash
cd client-web
npm install
```

### Error: "Cannot find express"

```bash
cd client-backend
npm install
```

### Error: "Address already in use"

El puerto está ocupado. Cambiar en `.env.local` o terminar el proceso anterior:

```bash
# Windows
netstat -ano | findstr :3001
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3001
kill -9 <PID>
```

### Error: "Connection refused" en Backend

Verificar que:
1. App-Server está corriendo en `http://localhost:8080`
2. Backend tiene variable `JAVA_APP_SERVER_URL=http://localhost:8080` en `.env.local`

### Error: "Cannot connect to database"

Verificar PostgreSQL:

```bash
docker ps | grep imageprocessing_db
docker logs imageprocessing_db

# O reconectar
docker-compose restart postgres
```

---

## Flujo de Uso

1. Abrir http://localhost:3000
2. Registrar usuario o logs in
3. Subir imágenes
4. Seleccionar transformaciones
5. Enviar lote
6. Ver progreso en tiempo real
7. Descargar resultados

---

## Estructura de Carpetas

```
distributed-image-processing/
├── docker-compose.yml           # PostgreSQL
├── grpc-contracts/              # Contratos .proto compartidos
├── app-server/                  # Servidor coordinador (Spring Boot)
├── worker-node/                 # Nodos procesadores (Spring Boot)
├── client-backend/              # NEW: Backend Express (intermediario)
├── client-web/                  # Frontend React/Vite
└── database/                    # Scripts SQL
```

---

## Documentación Adicional

- [README Backend Express](client-backend/README.md)
- [README App-Server](app-server/README.md)
- [README Client Web](client-web/README.md)
- [Proto Contracts](grpc-contracts/src/main/proto/node_service.proto)

---

## Notas Importantes

✅ **Cambios implementados:**
- Nuevo backend Express como intermediario
- Cliente web ahora usa REST en lugar de SOAP directo
- Mejor separación de responsabilidades
- Facilita futuras integraciones (móvil, etc.)

⚠️ **Verificar antes de producción:**
- Cambiar contraseña PostgreSQL
- Configurar JWT secret robusto
- Habilitar HTTPS
- Configurar CORS apropiadamente
- Implementar reintentos en gRPC
- Monitoreo y logging

---

**Última actualización:** Abril 2026

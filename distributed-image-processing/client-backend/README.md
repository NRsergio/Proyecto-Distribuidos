# Backend Intermedio - Express.js

Backend que actúa como intermediario entre el Cliente Web (React) y el Servidor de Aplicación JAVA.

## Arquitectura

```
Cliente Web (React) 
    ↓ HTTP REST
Backend Express (Puerto 3001)
    ↓ SOAP XML
App-Server JAVA (Puerto 8080)
    ↓ gRPC
Worker-Nodes (Puerto 9090+)
```

## Instalación

```bash
npm install
```

## Configuración

Copiar `.env.example` a `.env` y ajustar valores:

```bash
cp .env.example .env
```

### Variables de entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `BACKEND_PORT` | Puerto donde escucha el backend | 3001 |
| `JAVA_APP_SERVER_URL` | URL del App-Server JAVA | http://localhost:8080 |
| `CALLBACK_BASE_URL` | URL base para callbacks | http://localhost:3001 |
| `UPLOAD_DIR` | Directorio para archivos | ./uploads |

## Ejecutar

**Desarrollo:**
```bash
npm run dev
```

**Producción:**
```bash
npm start
```

## API Endpoints

### Autenticación

#### POST `/api/auth/login`
Login de usuario.

**Body:**
```json
{
  "email": "usuario@example.com",
  "password": "contraseña"
}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGc...",
    "idUsuario": 1,
    "nombre": "Juan Pérez",
    "email": "usuario@example.com"
  }
}
```

#### POST `/api/auth/register`
Registrar nuevo usuario.

**Body:**
```json
{
  "email": "nuevo@example.com",
  "password": "contraseña",
  "nombre": "Nombre Completo"
}
```

### Subida de Archivos

#### POST `/api/upload`
Sube imágenes al servidor.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: multipart/form-data
```

**Form Data:**
- `imagenes` - Array de archivos (máx 10, máx 20MB cada uno)

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "cantidad": 2,
    "archivos": [
      {
        "nombreArchivo": "foto.jpg",
        "rutaOrigen": "/path/to/foto.jpg",
        "nombreGuardado": "1623456789-foto.jpg",
        "size": 2048576
      }
    ]
  }
}
```

### Procesamiento de Lotes

#### POST `/api/batch/enviar`
Envía lote de imágenes con transformaciones.

**Headers:**
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Body:**
```json
{
  "token": "eyJhbGc...",
  "imagenes": [
    {
      "nombreArchivo": "foto.jpg",
      "rutaOriginal": "/tmp/uploads/1623456789-foto.jpg",
      "transformaciones": [
        {
          "tipo": "ESCALA_GRISES",
          "orden": 1,
          "parametros": "{}"
        },
        {
          "tipo": "REDIMENSIONAR",
          "orden": 2,
          "parametros": "{\"ancho\": 800, \"alto\": 600}"
        }
      ]
    }
  ]
}
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "idLote": 42
  }
}
```

#### GET `/api/batch/progreso/:idLote`
Consulta el progreso de un lote.

**Headers:**
```
Authorization: Bearer <token>
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "idLote": 42,
    "estadoLote": "EN_PROCESO",
    "porcentajeProgreso": 65.5,
    "totalImagenes": 3,
    "imagenesCompletadas": 2,
    "imagenesError": 0,
    "imagenes": [
      {
        "idImagen": 1,
        "nombreArchivo": "foto.jpg",
        "estado": "COMPLETADO",
        "rutaResultado": "/tmp/resultados/1/1.PNG"
      }
    ]
  }
}
```

## Flujo de Uso Completo

### 1. Registrar usuario
```bash
curl -X POST http://localhost:3001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123",
    "nombre": "Juan Pérez"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "password123"
  }'
```

Guardar `token` de la respuesta.

### 3. Subir imágenes
```bash
curl -X POST http://localhost:3001/api/upload \
  -H "Authorization: Bearer <token>" \
  -F "imagenes=@/path/to/image1.jpg" \
  -F "imagenes=@/path/to/image2.jpg"
```

Guardar `rutaOrigen` o `nombreGuardado` de cada imagen.

### 4. Enviar lote de procesamiento
```bash
curl -X POST http://localhost:3001/api/batch/enviar \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "token": "<token>",
    "imagenes": [
      {
        "nombreArchivo": "imagen1.jpg",
        "rutaOriginal": "/tmp/imageprocessing/uploads/1623456789-imagen1.jpg",
        "transformaciones": [
          {"tipo": "ESCALA_GRISES", "orden": 1, "parametros": "{}"}
        ]
      }
    ]
  }'
```

Guardar `idLote` de la respuesta.

### 5. Consultar progreso
```bash
curl -X GET http://localhost:3001/api/batch/progreso/42 \
  -H "Authorization: Bearer <token>"
```

Repetir hasta que `estadoLote` sea `COMPLETADO` o `ERROR`.

## Estructura de Carpetas

```
client-backend/
├── server.js                 # Servidor principal Express
├── package.json             # Dependencias
├── .env.example             # Plantilla de configuración
├── .gitignore
├── README.md
└── src/
    ├── routes/
    │   ├── auth.js          # Login/Register
    │   ├── batch.js         # Enviar lote/Progreso
    │   └── upload.js        # Subida de archivos
    ├── services/
    │   ├── soapService.js   # Comunica con app-server JAVA
    │   └── fileService.js   # Manejo de archivos
    └── middleware/
        └── authMiddleware.js # Verificación de tokens
```

## Comunicación con App-Server JAVA

El backend utiliza **SOAP** para comunicarse con el app-server:

- **URL:** `http://localhost:8080/ws/ImageProcessingService`
- **Namespace:** `http://imageprocessing.com/soap`
- **Operaciones:**
  - `login(email, password)` → token
  - `register(email, password, nombre)` → token
  - `enviarLote(token, imagenes)` → idLote
  - `consultarProgreso(token, idLote)` → estado

Ver [src/services/soapService.js](src/services/soapService.js) para detalles técnicos.

## Transformaciones Disponibles

| Tipo | Parámetros | Ejemplo |
|------|-----------|---------|
| `ESCALA_GRISES` | `{}` | `{}` |
| `REDIMENSIONAR` | `{ancho, alto}` | `{"ancho": 800, "alto": 600}` |
| `ROTAR` | `{grados}` | `{"grados": 90}` |
| `RECORTAR` | `{x, y, ancho, alto}` | `{"x": 0, "y": 0, "ancho": 400, "alto": 300}` |
| `REFLEJAR` | `{eje}` | `{"eje": "HORIZONTAL"}` |
| `DESENFOCAR` | `{radio}` | `{"radio": 3}` |
| `NITIDEZ` | `{}` | `{}` |
| `BRILLO_CONTRASTE` | `{brillo, contraste}` | `{"brillo": 10, "contraste": 10}` |
| `MARCA_DE_AGUA` | `{texto}` | `{"texto": "CONFIDENCIAL"}` |
| `CONVERSION_FORMATO` | `{formato}` | `{"formato": "PNG"}` |

## Notas

- El backend almacena archivos en `./uploads/` (configurable)
- No implementa persistencia propia (delega al App-Server JAVA)
- Los tokens JWT son validados en el App-Server JAVA
- El timeout para SOAP es de 30 segundos

# 🔌 Integración RMI - Comunicación con Nodos Workers

Guía de integración de RMI para comunicación con los nodos trabajadores.

## 📋 Visión General

El app-server se comunica con los nodos workers mediante **RMI (Remote Method Invocation)**, que es un mecanismo de Java para invocar métodos en objetos remotos.

```
App-Server (puerto 8080)
    ↓ RMI
    ├─ Worker-1 (puerto 9090)
    ├─ Worker-2 (puerto 9091)
    └─ Worker-N (puerto 909N)
```

## 🏗️ Arquitectura RMI

### Interfaz Remota (en nodo worker)

```java
// En el nodo worker - DEBE ser interfaz remota
public interface ImageProcessingWorker extends Remote {
    ProcessingResult processImage(byte[] imageData, List<Transformation> transforms) 
        throws RemoteException;
    
    WorkerStatus getStatus() throws RemoteException;
}

// Implementación en worker
public class ImageProcessingWorkerImpl extends UnicastRemoteObject 
    implements ImageProcessingWorker {
    
    @Override
    public ProcessingResult processImage(byte[] imageData, List<Transformation> transforms) 
        throws RemoteException {
        // Lógica de procesamiento
    }
}
```

### Cliente RMI (app-server)

```java
// En app-server
@Component
public class RmiClient {
    
    public Object obtenerServicioRemoto(String host, Integer puerto) {
        Registry registry = LocateRegistry.getRegistry(host, puerto);
        return registry.lookup("ImageProcessingWorker");
    }
}
```

## 🔧 Configuración RMI

### 1. Archivo application.yml

```yaml
rmi:
  registry:
    port: 1099                    # Puerto por defecto del registry
  server:
    port: 1099                    # Puede ser diferente por nodo
  service:
    name: ImageProcessingWorker   # Nombre del servicio remoto
```

### 2. Nodo Worker - Registrar Servicio RMI

```java
// En worker-node
public class WorkerNodeApplication {
    
    public static void main(String[] args) {
        try {
            // Crear registry en puerto 9090
            Registry registry = LocateRegistry.createRegistry(9090);
            
            // Crear instancia del servicio
            ImageProcessingWorker service = new ImageProcessingWorkerImpl();
            
            // Registrar en el registry
            registry.rebind("ImageProcessingWorker", service);
            
            System.out.println("Worker registrado en puerto 9090");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
```

### 3. App-Server - Usar Cliente RMI

```java
@Service
public class NodoService {
    
    private final RmiClient rmiClient;
    
    public void enviarTrabajo(Long idImagen, String host, Integer puerto) {
        try {
            // Obtener referencia remota
            ImageProcessingWorker worker = 
                (ImageProcessingWorker) rmiClient.obtenerServicioRemoto(host, puerto);
            
            // Llamar método remoto (como si fuera local)
            ProcessingResult resultado = worker.processImage(imageData, transformations);
            
            // Procesar resultado
            guardarResultado(idImagen, resultado);
            
        } catch (RemoteException e) {
            logger.error("Error comunicando con worker: {}", e.getMessage());
            marcarError(idImagen);
        }
    }
}
```

## 📡 Flujo de Procesamiento

```
1. Usuario crea lote (POST /api/batch/enviar)
        ↓
2. Controller recibe y llama BatchService
        ↓
3. BatchService crea SolicitudLote en BD
        ↓
4. Sistema busca nodos disponibles (NodoService)
        ↓
5. Para cada imagen:
    a. Obtener referencia RMI (RmiClient)
    b. Llamar método remoto processImage()
    c. Worker procesa la imagen
    d. Retorna resultado
    e. Guardar en BD
        ↓
6. Cliente consulta progreso (GET /api/batch/progreso)
        ↓
7. App-server retorna estado desde BD
```

## 🔐 Serialización RMI

Todos los objetos pasados por RMI DEBEN implementar `Serializable`:

```java
// Objetos pasados entre app-server y workers

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transformation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String tipo;
    private String parametros;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private byte[] processedImage;
    private String status;
    private String errorMessage;
}
```

## 🚀 Iniciar RMI

### En el Worker-Node

```bash
# Terminal 1: Iniciar worker-node
mvn spring-boot:run  # En worker-node/

# Salida esperada:
# Worker registrado en puerto 9090
# Registry listening on port 9090
```

### En el App-Server

```bash
# Terminal 2: Iniciar app-server
mvn spring-boot:run  # En app-server2/

# El app-server automáticamente se conectará cuando sea necesario
```

### Registrar el Nodo

```bash
# Terminal 3: Registrar nodo en BD
curl -X POST http://localhost:8080/api/nodos/registrar \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "worker-1",
    "host": "localhost",
    "puertoRmi": 9090
  }'
```

## 🔄 Implementación Completa en App-Server

### 1. Servicio para Enviar Trabajo

```java
@Service
@RequiredArgsConstructor
public class ProcessingService {
    
    private final RmiClient rmiClient;
    private final NodoTrabajadorRepository nodoRepository;
    private final LogTrabajoRepository logRepository;
    
    public void procesarImagen(Long idLote, Long idImagen, Long idNodo) {
        try {
            // Obtener nodo
            Optional<NodoTrabajador> nodoOpt = nodoRepository.findById(idNodo);
            if (nodoOpt.isEmpty()) throw new RuntimeException("Nodo no encontrado");
            
            NodoTrabajador nodo = nodoOpt.get();
            
            // Obtener referencia remota
            ImageProcessingWorker worker = 
                (ImageProcessingWorker) rmiClient.obtenerServicioRemoto(
                    nodo.getHost(), 
                    nodo.getPuertoRmi()
                );
            
            // Registrar inicio
            LogTrabajo log = LogTrabajo.builder()
                .idLote(idLote)
                .idImagen(idImagen)
                .idNodo(idNodo)
                .estado("INICIADO")
                .fechaInicio(LocalDateTime.now())
                .build();
            logRepository.save(log);
            
            // Llamar método remoto
            ProcessingResult resultado = worker.processImage(imageData, transformations);
            
            // Actualizar LogTrabajo
            log.setEstado("COMPLETADO");
            log.setFechaFin(LocalDateTime.now());
            log.setMensaje(resultado.getStatus());
            logRepository.save(log);
            
        } catch (RemoteException e) {
            logger.error("Error en RMI: {}", e.getMessage());
            registrarError(idLote, idImagen, e.getMessage());
        }
    }
}
```

### 2. Scheduler para Procesamiento Automático

```java
@Component
public class ProcessingScheduler {
    
    private final BatchService batchService;
    private final ProcessingService processingService;
    private final NodoService nodoService;
    
    /**
     * Cada 10 segundos, buscar lotes pendientes y procesarlos
     */
    @Scheduled(fixedDelay = 10000)
    public void procesarLotesPendientes() {
        List<SolicitudLote> lotes = batchService.obtenerLotesPendientes();
        
        for (SolicitudLote lote : lotes) {
            // Obtener nodos disponibles
            List<NodoTrabajador> nodos = nodoService.obtenerNodosActivos();
            
            if (nodos.isEmpty()) {
                logger.warn("No hay nodos disponibles para lote: {}", lote.getIdLote());
                continue;
            }
            
            // Obtener imágenes pendientes
            List<ImagenSolicitud> imagenes = 
                batchService.obtenerImagenesPendientes(lote.getIdLote());
            
            // Procesar imágenes en paralelo con workers
            int workerIndex = 0;
            for (ImagenSolicitud imagen : imagenes) {
                NodoTrabajador nodo = nodos.get(workerIndex % nodos.size());
                
                // Enviar a procesar (asincrónico)
                processingService.procesarImagen(
                    lote.getIdLote(), 
                    imagen.getIdImagen(), 
                    nodo.getIdNodo()
                );
                
                workerIndex++;
            }
        }
    }
}
```

## 🧪 Testing RMI

### Test Local

```bash
# Terminal 1: Iniciar mock worker RMI
java -cp ".:lib/*" MockWorkerNode 9090

# Terminal 2: Ejecutar app-server
mvn spring-boot:run

# Terminal 3: Enviar petición
curl -X POST http://localhost:8080/api/batch/enviar \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Test","transformaciones":[]}'
```

### Monitorear Conexión RMI

```bash
# Ver conexiones activas
jps -l

# O con rmiregistry
rmiregistry 9090 &

# Ver servicios registrados
rmi registry lookup
```

## 📊 Monitoreo RMI

### Health Check de Nodos

```java
@Scheduled(fixedDelay = 30000)  // Cada 30 segundos
public void verificarSaludNodos() {
    List<NodoTrabajador> nodos = nodoRepository.findByEstado("CONECTADO");
    
    for (NodoTrabajador nodo : nodos) {
        try {
            ImageProcessingWorker worker = 
                (ImageProcessingWorker) rmiClient.obtenerServicioRemoto(
                    nodo.getHost(), 
                    nodo.getPuertoRmi()
                );
            
            WorkerStatus status = worker.getStatus();
            
            nodoService.actualizarMetricas(
                nodo.getIdNodo(),
                status.getCpuUsage(),
                status.getMemoryUsage()
            );
        } catch (RemoteException e) {
            nodoService.desconectarNodo(nodo.getIdNodo());
            rmiClient.invalidarConexion(nodo.getHost(), nodo.getPuertoRmi());
        }
    }
}
```

## 🔧 Troubleshooting RMI

| Problema | Causa | Solución |
|----------|-------|----------|
| `RemoteException: Connection refused` | Worker no activo | Iniciar worker-node |
| `NotBoundException` | Servicio no registrado | Verificar nombre en registry |
| `ClassNotFoundException` | Clase no en classpath | Agregar a `pom.xml` |
| `MarshalException: error marshalling arguments` | Objeto no serializable | Implementar `Serializable` |
| `Connection timeout` | Firewall bloqueando puerto | Abrir puerto RMI |

## 🚀 Siguiente Fase

Una vez RMI funcionando:
1. ✅ Lotes creados vía HTTP
2. ✅ Imágenes procesadas vía RMI
3. → Agregar callbacks de progreso
4. → Implementar queue de trabajos
5. → Agregar balanceador de carga

---

**Documentación versión 2.0** - Abril 2026


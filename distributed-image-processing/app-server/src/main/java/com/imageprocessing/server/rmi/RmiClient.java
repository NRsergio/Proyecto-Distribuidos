package com.imageprocessing.server.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente RMI para comunicación con nodos trabajadores
 * Maneja la comunicación remota con los nodos via RMI
 * 
 * Nota: Esta es una estructura base que necesita detallarse
 * según la interfaz RMI específica de los worker-nodes
 */
@Component
public class RmiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RmiClient.class);
    
    @Value("${rmi.registry.port:1099}")
    private int rmiRegistryPort;
    
    @Value("${rmi.service.name:ImageProcessingWorker}")
    private String rmiServiceName;
    
    private final Map<String, Object> serviceCache = new HashMap<>();
    
    /**
     * Obtiene una referencia remota a un servicio RMI en un nodo específico
     * @param host dirección IP del nodo
     * @param puerto puerto RMI del nodo
     * @return referencia remota al servicio
     */
    public Object obtenerServicioRemoto(String host, Integer puerto) {
        String cacheKey = String.format("%s:%d", host, puerto);
        
        // Verificar cache
        if (serviceCache.containsKey(cacheKey)) {
            logger.debug("Servicio remoto obtenido del cache: {}", cacheKey);
            return serviceCache.get(cacheKey);
        }
        
        try {
            logger.info("Conectando a servicio RMI en {}:{}", host, puerto);
            
            // Obtener registry del nodo remoto
            Registry registry = LocateRegistry.getRegistry(host, puerto);
            
            // Buscar el servicio
            Object servicio = registry.lookup(rmiServiceName);
            
            // Guardar en cache
            serviceCache.put(cacheKey, servicio);
            
            logger.info("Servicio RMI conectado exitosamente: {}", cacheKey);
            return servicio;
        } catch (RemoteException | NotBoundException e) {
            logger.error("Error conectando a servicio RMI {}:{}: {}", host, puerto, e.getMessage());
            // Limpiar del cache si existe
            serviceCache.remove(cacheKey);
            throw new RuntimeException("No se pudo conectar al servicio RMI: " + e.getMessage());
        }
    }
    
    /**
     * Invalida la conexión en cache para un nodo específico
     * (útil cuando el nodo se desconecta)
     * @param host dirección IP del nodo
     * @param puerto puerto RMI del nodo
     */
    public void invalidarConexion(String host, Integer puerto) {
        String cacheKey = String.format("%s:%d", host, puerto);
        if (serviceCache.containsKey(cacheKey)) {
            serviceCache.remove(cacheKey);
            logger.info("Conexión invalidada: {}", cacheKey);
        }
    }
    
    /**
     * Limpia todo el cache de conexiones
     */
    public void limpiarCache() {
        serviceCache.clear();
        logger.info("Cache de conexiones RMI limpiado");
    }
}

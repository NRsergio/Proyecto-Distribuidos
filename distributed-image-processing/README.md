# Sistema Distribuido — Procesamiento de Imágenes en Paralelo

## Arquitectura del Proyecto

```
distributed-image-processing/
├── grpc-contracts/   # Contratos .proto compartidos (gRPC)
├── app-server/       # Servidor de aplicacion (SOAP + coordinador)
├── worker-node/      # Nodo trabajador (gRPC + hilos)
├── client-web/       # Frontend React
├── database/         # Scripts SQL (PostgreSQL)
└── docker-compose.yml
```

## Requisitos

- Java 17+
- Maven 3.9+
- Node.js 20+
- PostgreSQL 14+ (o Docker)

## Levantar la Base de Datos

```bash
docker-compose up -d
```

## Compilar todos los modulos Java

```bash
mvn clean install
```

## Levantar el Servidor de Aplicacion

```bash
cd app-server
mvn spring-boot:run
# SOAP disponible en: http://localhost:8080/ws/ImageProcessingService?wsdl
```

## Levantar un Nodo Trabajador

```bash
cd worker-node
mvn spring-boot:run
# En otro terminal, nodo-02:
mvn spring-boot:run -Dnode.id=nodo-02 -Dnode.grpc-port=9091 -Dserver.port=8091
```

## Levantar el Frontend

```bash
cd client-web
npm install
npm run dev
# Disponible en: http://localhost:3000
```

## WSDL del Servicio SOAP

```
http://localhost:8080/ws/ImageProcessingService?wsdl
```

## Puertos

| Servicio           | Puerto |
|--------------------|--------|
| App Server (HTTP)  | 8080   |
| Nodo-01 (gRPC)     | 9090   |
| Nodo-01 (HTTP)     | 8090   |
| Nodo-02 (gRPC)     | 9091   |
| Frontend React     | 3000   |
| PostgreSQL         | 5432   |

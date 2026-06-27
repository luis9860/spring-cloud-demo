# Comandas de Restaurantes

**Proyecto:** Comandas de Restaurantes (Luis Alberto Arias Ledesma — Cibertec DSW2)  
**Implementación técnica:** demo Spring Cloud del curso (`spring-cloud-demo`) ampliado con el MVP de comandas.

Documentación completa: [`docs/00-Indice-Documentacion.md`](docs/00-Indice-Documentacion.md)

## Cómo se nombra (según documentación)

| Nivel | Nombre | Dónde aparece |
|-------|--------|----------------|
| **Proyecto / producto** | **Comandas de Restaurantes** | Portada, índice DOC-01…DOC-10, presentación |
| **Repositorio / carpeta** | `spring-cloud-demo` | Base del profesor (Eureka, Config, Gateway) |
| **Microservicios** | `producto-service`, `pedido-service`, `auth-service`, `api-gateway`, `eureka-server`, `config-server` | Plan técnico §4, Eureka, Feign, Gateway |

El nombre del **negocio** va en la documentación y en textos de la app (ej. restaurante piloto). Los microservicios mantienen nombres **por función**, como en el plan técnico y la plantilla del curso — no se renombran todos a “comandas” en Eureka.

## Stack

- Java 17 | Spring Boot 3.5.14 | Spring Cloud 2025.0.2
- Maven (cada módulo es un proyecto Spring Boot independiente)

## Módulos y puertos

| Carpeta | `spring.application.name` | Puerto |
|---------|---------------------------|--------|
| `01-eureka-server` | eureka-server | 8761 |
| `02-config-server` | config-server | 8888 |
| `03-producto-service` | producto-service | 8081 |
| `04-pedido-service` | pedido-service | 8082 |
| `06-auth-service` | auth-service | 8083 |
| `05-api-gateway` | api-gateway | 8080 |
| `07-frontend-comandas` | (Angular) | 4200 |

## Base de datos MySQL

Los servicios `auth-service`, `producto-service` y `pedido-service` usan **MySQL** (antes H2 en memoria).

1. Instala MySQL y crea las bases con [`db/init-comandas-mysql.sql`](db/init-comandas-mysql.sql).
2. Credenciales por defecto en `application.yml`: usuario `root`, contraseña `mysql`.
3. Las tablas y datos demo se crean al arrancar (`ddl-auto=update` + `DataLoader`).

Ver [`db/README.md`](db/README.md).

## Swagger / OpenAPI

| Servicio | URL directa | Vía Gateway |
|----------|-------------|-------------|
| Gateway | http://localhost:8080/swagger-ui.html | — |
| Auth | http://localhost:8083/swagger-ui.html | http://localhost:8080/api/auth/... |
| Producto | http://localhost:8081/swagger-ui.html | http://localhost:8080/api/productos/... |
| Pedido | http://localhost:8082/swagger-ui.html | http://localhost:8080/api/mesas/... |

Colección Postman: [`docs/postman/Comandas-API.postman_collection.json`](docs/postman/Comandas-API.postman_collection.json).

## VPS con MySQL

Tras clonar en el servidor (`/opt/comandas/spring-cloud-demo`):

```bash
./scripts/setup-vps-production.sh   # primera vez: Java, nginx, systemd
./scripts/setup-mysql-vps.sh        # primera vez: MySQL + /etc/comandas/comandas.env
./scripts/deploy-vps.sh             # compilar y publicar
```

Cada `git push` a `main` puede relanzar el deploy automático si `VPS_DEPLOY_ENABLED=true`. El script `deploy-vps.sh` exige MySQL activo y el archivo de entorno.

## Arranque local

```bash
cd 01-eureka-server && mvn spring-boot:run
cd 02-config-server && mvn spring-boot:run
cd 06-auth-service && mvn spring-boot:run
cd 03-producto-service && mvn spring-boot:run
cd 04-pedido-service && mvn spring-boot:run
cd 05-api-gateway && mvn spring-boot:run
```

## Pruebas vía Gateway

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
curl http://localhost:8080/api/productos
curl http://localhost:8080/api/mesas
curl "http://localhost:8080/api/pedidos/simular/1?cantidad=2"
```

Usuarios demo: `admin`/`admin123`, `mozo1`/`mozo123`, `cocina1`/`cocina123`.

## Frontend Angular

```bash
cd 07-frontend-comandas
npm start
```

UI en http://localhost:4200 (mozo, cocina, login, QR). Ver `07-frontend-comandas/README.md`.

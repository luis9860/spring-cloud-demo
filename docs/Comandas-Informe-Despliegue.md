# Comandas de Restaurantes — Informe de despliegue
## DOC-08 | Operaciones e infraestructura

**Autor:** Luis Alberto Arias Ledesma  
**Entorno:** VPS Ubuntu (OVH)  
**Versión:** 1.0

---

# 1. Resumen ejecutivo

El ecosistema **Spring Cloud** del proyecto está desplegado en un **VPS Ubuntu** con **Java 17**, ejecutándose como servicios **systemd** independientes. La entrada pública recomendada es el **API Gateway** en el puerto **8080**. Este informe documenta la arquitectura desplegada, el orden de arranque, la verificación y las recomendaciones de producción.

---

# 2. Especificación del servidor

| Parámetro | Valor |
|-----------|-------|
| SO | Ubuntu Server (64-bit) |
| Java | OpenJDK 17 |
| Build | Maven en PC de desarrollo → JAR en VPS |
| Usuario despliegue | `ubuntu` |
| Ruta aplicación | `/home/ubuntu/spring-cloud-demo/` |
| Gestión procesos | systemd |

---

# 3. Servicios desplegados

| Servicio systemd | JAR | Puerto | Estado esperado |
|------------------|-----|--------|-----------------|
| eureka | eureka-server-1.0.0.jar | 8761 | active |
| config-server | config-server-1.0.0.jar | 8888 | active |
| producto-service | producto-service-1.0.0.jar | 8081 | active |
| pedido-service | pedido-service-1.0.0.jar | 8082 | active |
| api-gateway | api-gateway-1.0.0.jar | 8080 | active |
| auth-service | auth-service-1.0.0.jar | 8083 | active |

**Orden de arranque:** eureka → config-server → producto → pedido → gateway (esperar 20–30 s entre cada uno en primer despliegue).

---

# 4. Comandos de operación

## 4.1 Ver estado
```bash
sudo systemctl status eureka config-server producto pedido gateway --no-pager
```

## 4.2 Reiniciar un servicio
```bash
sudo systemctl restart pedido
```

## 4.3 Ver logs
```bash
sudo journalctl -u gateway -n 100 --no-pager
```

## 4.4 Verificar puertos
```bash
ss -tlnp | grep -E '8761|8888|8080|8081|8082'
```

---

# 5. Pruebas de humo post-despliegue

| # | Prueba | Comando / URL | Criterio éxito |
|---|--------|---------------|----------------|
| 1 | Eureka dashboard | `http://IP:8761` | 3 instancias UP |
| 2 | Productos vía Gateway | `curl http://localhost:8080/api/productos` | JSON |
| 3 | Simular pedido | `curl "http://localhost:8080/api/pedidos/simular/1?cantidad=2"` | JSON con total |
| 4 | Tras reboot VPS | Reinicio + esperar 3 min + repetir 1–3 | Servicios auto-start |

---

# 6. Firewall

| Puerto | Uso | Recomendación |
|--------|-----|---------------|
| 22 | SSH | Abierto (restringir IP si es posible) |
| 8080 | Gateway prueba | Abierto en prueba; cerrar cuando exista Nginx |
| 8761 | Eureka | Solo interno o VPN |
| 443 | HTTPS | Abrir con Nginx + certbot en producción |

---

# 7. Actualización de versión (procedimiento)

1. En PC: `mvn clean package -DskipTests` por módulo modificado.  
2. `scp` JAR nuevo a VPS (sin `.original`).  
3. `sudo systemctl stop {servicio}`  
4. Reemplazar JAR  
5. `sudo systemctl start {servicio}`  
6. Ejecutar pruebas de humo  

---

# 8. Componentes pendientes (MVP comandas)

| Componente | Estado |
|------------|--------|
| PostgreSQL en VPS | Pendiente instalación |
| auth-service :8083 | Pendiente desarrollo |
| Nginx + HTTPS | Recomendado |
| Front estático | Pendiente |

---

# 9. Diagrama de despliegue físico

```
Internet
    |
    v
[ Firewall ]
    |
    v
VPS :8080 (Gateway) ---> Eureka :8761
    |                      Config :8888
    +---> producto :8081
    +---> pedido :8082
    +---> (auth :8083 futuro)
    +---> PostgreSQL :5432 (localhost, futuro)
```

---

# 10. Conclusiones

El despliegue actual demuestra **viabilidad operativa** del stack Spring Cloud en VPS con reinicio automático vía systemd. Para el MVP de comandas se requiere añadir **base de datos**, **auth-service** y **frontend**, manteniendo la misma estrategia de despliegue por JAR.

---

*Fin DOC-08*

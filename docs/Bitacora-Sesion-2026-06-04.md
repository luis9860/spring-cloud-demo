# Bitácora de sesión — 4 de junio de 2026

**Proyecto:** Comandas de Restaurantes  
**Autor:** Luis Alberto Arias Ledesma  
**Repositorio GitHub:** [luis9860/spring-cloud-demo](https://github.com/luis9860/spring-cloud-demo)  
**Propósito:** Registro de lo implementado, corregido y acordado en esta jornada, para consulta futura.

---

## 1. Resumen ejecutivo

En esta sesión se corrigieron **bugs críticos del flujo mozo ↔ cocina**, se mejoró la **experiencia del comensal (QR)**, se preparó **despliegue con GitHub** y se documentaron estrategias **profesionales** (CI/CD, blue/green, nginx, cero corte).

---

## 2. Correcciones de software (mozo / cocina / QR)

### 2.1 Mozo no veía platos listos (cola QR sí los mostraba)

**Síntoma:** En `/qr` aparecían platos «Listo», pero en `/mozo` la sección «Listo en cocina» estaba vacía.

**Causa:**
- `notificacionesMozo` solo avisaba si la comanda entera estaba en `LISTA` (no `PARCIALMENTE_LISTA`).
- Cuando cocina marcaba **algunos** platos listos, la comanda quedaba parcial y el mozo no recibía tipo `LISTO`.
- El **pedido-service en ejecución** a veces era una versión antigua (sin el fix) hasta reiniciar el puerto 8082.

**Solución aplicada:**

| Archivo | Cambio |
|---------|--------|
| `04-pedido-service/.../ComandaService.java` | `notificacionesMozo` por **líneas** `LISTA`; recorre mesas con sesión activa (misma lógica que cola QR). |
| | Nuevo método `platosListosMozo()` y endpoint dedicado. |
| | `recalcularEstadoComanda`: `listaParaServirEn` también en parcial (modo A). |
| `04-pedido-service/.../MozoController.java` | `GET /mozo/platos-listos` |
| `07-frontend-comandas/.../mozo.component.ts` | Consulta notificaciones + platos-listos; polling cada 4 s; muestra **todas** las líneas de la sesión. |
| `07-frontend-comandas/.../comandas-api.service.ts` | `getPlatosListosMozo()` |

### 2.2 Al marcar «Servido al cliente» desaparecía todo el pedido

**Síntoma:** Cocina terminaba un plato; mozo aceptaba/servía y **desaparecían** también líneas aún en cocina.

**Causa:** `marcarEntregada` marcaba **toda la comanda** y **todas las líneas** como `ENTREGADA`, incluso las que seguían `ENVIADA` o `EN_PREPARACION`. El frontend ocultaba comandas `ENTREGADA`.

**Solución aplicada:**

| Archivo | Cambio |
|---------|--------|
| `ComandaService.marcarEntregada` | Solo líneas en estado `LISTA` → `ENTREGADA`. |
| | `recalcularEstadoComandaDespuesServir`: comanda sigue activa si quedan platos en cocina. |
| | Respuesta JSON: `platosServidos`, `platosPendientes`. |
| `ComandaController` | `PATCH /comandas/{id}/entregada` devuelve `Map` con resumen. |
| `mozo.component.ts` | Tras enviar a cocina **recarga** líneas (ya no borra la tabla). |
| | Confirmación de servido aclara que solo afecta platos listos. |

### 2.3 Otros ajustes mozo (sesiones previas, consolidados aquí)

- **Liberar mesa:** valida líneas en cocina/listas; anula `BORRADOR` al cerrar.
- **Quitar línea:** solo en carrito (`BORRADOR`).
- **Varias líneas del mismo producto** en una comanda: comportamiento esperado si el mozo agrega dos veces (no es duplicado automático del sistema).

### 2.4 QR comensal (contexto funcional)

- Un solo QR del local: `/qr` → cola en vivo por mesa.
- Backend público: `GET /api/publico/local`, `GET /api/publico/local/cola`.
- Token local: `comandas-local`.

---

## 3. Infraestructura y datos

| Tema | Detalle |
|------|---------|
| **H2 en memoria** (`pedido-service`) | Cada **reinicio** de `pedido-service` **borra** pedidos y mesas en curso. No es por cambiar de pantalla mozo/cocina. |
| **Orden de arranque local** | Eureka 8761 → Config 8888 → auth 8083 → producto 8081 → pedido 8082 → gateway 8080 → `ng serve` 4200 |
| **Usuarios demo** | `admin/admin123`, `mozo1/mozo123`, `cocina1/cocina123` |

---

## 4. GitHub y despliegue

### 4.1 Repositorio creado y primer push

- URL: **https://github.com/luis9860/spring-cloud-demo**
- Rama: `main`
- Commit inicial: *Comandas de Restaurantes - MVP Spring Cloud y Angular* (144 archivos).
- `.gitignore` excluye `node_modules`, `target/`, secretos.

### 4.2 Archivos de despliegue añadidos en el repo

| Archivo | Función |
|---------|---------|
| `.github/workflows/deploy-vps.yml` | Deploy automático al hacer `push` a `main` (SSH al VPS). |
| `scripts/deploy-vps.sh` | Compila y reinicia en el VPS (enfoque inicial). |
| `docs/Despliegue-GitHub-VPS.md` | Guía paso a paso (Secrets, clone, systemd). |

**Pendiente:** configurar Secrets en GitHub (`VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`) cuando el VPS esté listo. Hasta entonces el workflow de Actions puede fallar (es normal).

### 4.3 Decisiones de arquitectura de despliegue (acordadas en sesión)

| Enfoque | Descripción |
|---------|-------------|
| **Código en GitHub, no JAR** | El repo solo lleva fuente; `.gitignore` excluye artefactos. |
| **Corporativo (preferido)** | CI en GitHub Actions compila JAR/imagen Docker; VPS solo descarga y reinicia (sin Maven/Node en producción). |
| **Blue / Green en un VPS** | Instancia **Blue** = producción diaria; **Green** = solo encendida al actualizar; nginx hace el *switch* de tráfico. |
| **Cero corte** | Requiere 2 instancias o blue/green + `nginx reload`; un solo JAR con `restart` siempre tiene corte breve. |
| **nginx** | Puerta de entrada (HTTPS, Angular estático, proxy `/api` al Gateway). |

---

## 5. Pruebas recomendadas tras cada cambio

1. Reiniciar **pedido-service** (8082) si se modificó Java del mozo/cocina.
2. Flujo E2E: abrir mesa → agregar → enviar → cocina acepta → Listo → mozo ve «Listo en cocina» → Servido (solo listos) → verificar que el resto sigue visible.
3. Comparar **misma mesa** en QR y mozo (ej. M02 en ambas pantallas).
4. Tras `git push`, revisar pestaña **Actions** en GitHub cuando el VPS esté configurado.

---

## 6. Archivos tocados (referencia rápida)

### Backend
- `04-pedido-service/src/main/java/com/cibertec/pedido/service/ComandaService.java`
- `04-pedido-service/src/main/java/com/cibertec/pedido/web/MozoController.java`
- `04-pedido-service/src/main/java/com/cibertec/pedido/web/ComandaController.java`

### Frontend
- `07-frontend-comandas/src/app/pages/mozo/mozo.component.ts`
- `07-frontend-comandas/src/app/pages/mozo/mozo.component.html`
- `07-frontend-comandas/src/app/core/comandas-api.service.ts`

### DevOps / docs
- `.gitignore`
- `.github/workflows/deploy-vps.yml`
- `scripts/deploy-vps.sh`
- `docs/Despliegue-GitHub-VPS.md`
- `docs/00-Indice-Documentacion.md`

---

## 7. Trabajo futuro (no hecho hoy)

- [ ] `docker-compose.yml` + imágenes construidas en GitHub Actions (enfoque corporativo).
- [ ] Blue/Green con `docker-compose.blue.yml` / `green.yml` y scripts de switch nginx.
- [ ] PostgreSQL en producción (reemplazar H2).
- [ ] Secrets de GitHub y primer deploy real al VPS.
- [ ] Documentar units **systemd** por microservicio.

---

## 8. Control de esta bitácora

| Versión | Fecha | Notas |
|---------|-------|-------|
| 1.0 | 04/06/2026 | Sesión mozo/cocina, GitHub, despliegue y blue/green |

---

*Mantener este documento al cerrar sesiones importantes de desarrollo; añadir entradas nuevas como `Bitacora-Sesion-AAAA-MM-DD.md` o ampliar una sección «Historial» al final.*

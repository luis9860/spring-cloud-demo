# Comandas de Restaurantes — Plan de pruebas
## DOC-05 | Aseguramiento de calidad

**Referencia:** DOC-02 §6, DOC-03  
**Autor:** Luis Alberto Arias Ledesma  
**Versión:** 1.0

---

# 1. Objetivo

Validar que el **MVP** cumple los requisitos funcionales críticos: autenticación, gestión de mesas (límite 15 FREE), flujo mozo–cocina, modos de servicio A/B y consulta pública QR.

---

# 2. Alcance de pruebas

| Incluye | Excluye (fase 3) |
|---------|------------------|
| APIs REST vía Gateway | App comensal marketplace completa |
| Reglas plan FREE | Pasarela real producción |
| Eureka + 5–6 microservicios | Carga masiva > 100 RPS |
| Front web manual | Penetration testing profesional |

---

# 3. Estrategia

| Nivel | Tipo | Herramienta | Responsable |
|-------|------|-------------|-------------|
| L1 | Unitarias | JUnit 5 + Mockito | Desarrollador |
| L2 | Integración API | Postman / Newman | Desarrollador |
| L3 | Sistema E2E | Postman + front manual | Desarrollador / QA |
| L4 | Aceptación | Checklist con dueño piloto | Product owner |

---

# 4. Criterios de entrada y salida

**Entrada:** Build exitoso; servicios UP en Eureka; BD con datos semilla.  
**Salida:** 100 % casos críticos PASS; 0 defectos bloqueantes abiertos.

---

# 5. Casos de prueba — Críticos (MVP)

| ID | Módulo | Descripción | Precondición | Pasos | Resultado esperado | Prioridad |
|----|--------|-------------|--------------|-------|-------------------|-----------|
| TP-01 | Auth | Login admin válido | Usuario semilla | POST /api/auth/login | 200 + JWT | Alta |
| TP-02 | Auth | Login password incorrecta | — | POST login mal password | 401 | Alta |
| TP-03 | Mesa | Crear mesa 1–15 plan FREE | Admin JWT | POST /api/mesas x15 | 201 | Alta |
| TP-04 | Mesa | Bloquear mesa 16 plan FREE | 15 mesas | POST mesa 16 | 403 + mensaje upgrade | Alta |
| TP-05 | Pedido | Abrir sesión mesa | Mesa LIBRE | POST abrir | OCUPADA | Alta |
| TP-06 | Pedido | Agregar línea silla 2 | Sesión abierta | POST linea | BORRADOR | Alta |
| TP-07 | Pedido | Enviar comanda | Líneas borrador | POST enviar | ENVIADA en cocina | Alta |
| TP-08 | Cocina | Marcar ítem LISTA | Comanda enviada | PATCH estado | LISTA | Alta |
| TP-09 | Mozo | Modo A notificación | Modo A, todos ítems listos | GET notificaciones | 1 aviso comanda completa | Alta |
| TP-10 | Mozo | Modo B notificación | Modo B, 1 ítem listo | GET notificaciones | 1 aviso por ítem | Alta |
| TP-11 | Producto | Listar menú | JWT mozo | GET /api/productos | JSON lista | Media |
| TP-12 | Público | QR sin auth | qr_token válido | GET /api/publico/pedido/{token} | 200 estados | Alta |
| TP-13 | Gateway | Ruta producto legacy | Eureka UP | GET /api/productos | 200 | Media |
| TP-14 | Infra | Eureka 3+ servicios | Todos arrancados | GET :8761 | INSTANCAS registradas | Alta |

---

# 6. Casos de prueba — Seguridad

| ID | Descripción | Esperado |
|----|-------------|----------|
| TS-01 | Mozo accede POST /api/mesas (crear) | 403 |
| TS-02 | Request sin JWT a /api/mesas | 401 |
| TS-03 | JWT expirado | 401 |
| TS-04 | Usuario restaurante A accede mesa restaurante B | 403 |

---

# 7. Datos de prueba (semilla)

| Entidad | Valor |
|---------|-------|
| Restaurante | id=1, plan=FREE, limite_mesas=15 |
| Admin | admin / (password en entorno dev) |
| Mozo | mozo1 |
| Cocinero | cocina1 |
| Productos | 5 COCINA, 3 BARRA |

---

# 8. Matriz trazabilidad RF → TP

| RF | Casos TP |
|----|----------|
| RF-01, RF-01b | TP-03, TP-04 |
| RF-02 | TP-01 |
| RF-03–04 | TP-05–07 |
| RF-05–06 | TP-08 |
| RF-07, RF-07b | TP-09, TP-10 |
| RF-08 | TP-12 |

---

# 9. Registro de ejecución (plantilla)

| ID | Fecha | Build | Entorno | Ejecutor | Resultado | Defecto |
|----|-------|-------|---------|----------|-----------|---------|
| TP-01 | | | VPS / Local | | PASS / FAIL | |

---

# 10. Defectos — Clasificación

| Severidad | Definición |
|-----------|------------|
| Bloqueante | No se puede operar comanda en piloto |
| Mayor | Función incorrecta con workaround |
| Menor | UI o mensaje |
| Cosmético | Estético |

---

*Fin DOC-05*

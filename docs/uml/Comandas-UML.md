# Comandas de Restaurantes — Modelado UML
## DOC-03 | Análisis y diseño

**Referencia:** DOC-01 Emprendimiento, DOC-02 Plan técnico  
**Autor:** Luis Alberto Arias Ledesma  
**Versión:** 1.0

---

# 1. Actores del sistema (diagrama de contexto)

```mermaid
flowchart LR
    Admin[Administrador / Dueño]
    Mozo[Mozo]
    Cocina[Cocinero]
    Barra[Barra]
    Comensal[Comensal]
    Sistema[Sistema Comandas]
    Admin --> Sistema
    Mozo --> Sistema
    Cocina --> Sistema
    Barra --> Sistema
    Comensal --> Sistema
```

| Actor | Descripción breve |
|-------|-------------------|
| Administrador | Configura restaurante, usuarios, menú, mesas, planes |
| Mozo | Opera mesas, comandas, recibe notificaciones |
| Cocinero | Cola estación COCINA |
| Barra | Cola estación BARRA |
| Comensal | QR en mesa (MVP); app marketplace (fase 3) |

---

# 2. Diagrama de casos de uso (MVP)

```mermaid
flowchart TB
    subgraph Sistema["Sistema Comandas - MVP"]
        UC1[UC-01 Configurar local]
        UC2[UC-02 Gestionar menú]
        UC3[UC-03 Gestionar usuarios]
        UC4[UC-04 Tomar pedido]
        UC5[UC-05 Enviar comanda a cocina]
        UC6[UC-06 Preparar pedido]
        UC7[UC-07 Recibir notificación listo]
        UC8[UC-08 Seguimiento QR comensal]
    end
    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Mozo --> UC4
    Mozo --> UC5
    Mozo --> UC7
    Cocina --> UC6
    Barra --> UC6
    Comensal --> UC8
    Admin --> UC2
```

## 2.1 Matriz actor — caso de uso

| Caso de uso | Admin | Mozo | Cocina | Barra | Comensal |
|-------------|:-----:|:----:|:------:|:-----:|:--------:|
| UC-01 Configurar local | X | | | | |
| UC-02 Gestionar menú | X | | | | |
| UC-03 Gestionar usuarios | X | | | | |
| UC-04 Tomar pedido | | X | | | |
| UC-05 Enviar comanda | | X | | | |
| UC-06 Preparar pedido | | | X | X | |
| UC-07 Notificación listo | | X | | | |
| UC-08 Seguimiento QR | | | | | X |

---

# 3. Caso de uso detallado — UC-05 Enviar comanda a cocina

| Campo | Descripción |
|-------|-------------|
| **ID** | UC-05 |
| **Actor principal** | Mozo |
| **Precondiciones** | Mozo autenticado; mesa con sesión abierta; al menos una línea en BORRADOR |
| **Postcondiciones** | Comanda en estado ENVIADA; líneas visibles en cocina/barra |
| **Flujo principal** | 1. Mozo selecciona mesa y silla → 2. Agrega ítems → 3. Confirma envío → 4. Sistema valida y notifica cocina |
| **Flujo alternativo A** | Sin conexión: mensaje de error; reintento |
| **Flujo alternativo B** | Modo A: cocina marca ítems; al completar comanda, notifica mozo |
| **Reglas de negocio** | RN-01, RN-02, RN-07 |

---

# 4. Diagrama de secuencia — Tomar pedido y enviar a cocina (MVP)

```mermaid
sequenceDiagram
    actor Mozo
    participant Front as Front Mozo
    participant GW as API Gateway
    participant Auth as auth-service
    participant Ped as pedido-service
    participant Prod as producto-service
    actor Cocina as Cocinero

    Mozo->>Front: Login
    Front->>GW: POST /api/auth/login
    GW->>Auth: login
    Auth-->>Front: JWT

    Mozo->>Front: Abrir mesa 5
    Front->>GW: POST /api/mesas/5/abrir
    GW->>Ped: abrir sesión
    Ped-->>Front: sesionId

    Mozo->>Front: Agregar línea silla 2
    Front->>GW: POST .../lineas
    GW->>Ped: agregar línea
    Ped->>Prod: GET producto (Feign)
    Prod-->>Ped: precio, estacion
    Ped-->>Front: lineaId

    Mozo->>Front: Enviar comanda
    Front->>GW: POST /api/comandas/{id}/enviar
    GW->>Ped: enviar
    Ped-->>Cocina: cola actualizada

    Cocina->>GW: PATCH línea LISTA
    GW->>Ped: actualizar estado
    Ped-->>Front: notificación mozo (modo A/B)
```

---

# 5. Diagrama de secuencia — Autoatención al llegar (fase 3)

```mermaid
sequenceDiagram
    actor Comensal
    participant App as App Comensal
    participant GW as API Gateway
    participant Com as comensal-service
    participant Ped as pedido-service
    participant Pay as Pasarela pago

    Comensal->>App: Elige restaurante
    App->>GW: GET mesas/disponibilidad
    GW->>Com: consultar
    Com->>Ped: estados mesas
    Ped-->>App: mesa 7 LIBRE

    Comensal->>App: Pedido + pago
    App->>GW: POST autoatencion
    GW->>Pay: cobrar
    Pay-->>GW: OK
    GW->>Ped: crear comanda APP_AUTOATENCION
    Ped-->>App: confirmación + seguimiento
```

---

# 6. Diagrama de clases (dominio simplificado)

```mermaid
classDiagram
    class Restaurante {
        +Long id
        +String nombre
        +Plan plan
        +int limiteMesas
        +boolean autoatencionHabilitada
    }
    class Usuario {
        +Long id
        +String username
        +Rol rol
    }
    class Mesa {
        +Long id
        +int numero
        +EstadoMesa estado
        +String qrToken
    }
    class SesionMesa {
        +Long id
        +LocalDateTime abiertaEn
    }
    class Comanda {
        +Long id
        +Origen origen
        +EstadoComanda estado
        +char modoServicio
    }
    class LineaPedido {
        +Long id
        +int numeroSilla
        +int cantidad
        +EstadoLinea estado
    }
    class Producto {
        +Long id
        +String nombre
        +BigDecimal precio
        +Estacion estacion
    }
    Restaurante "1" --> "*" Usuario
    Restaurante "1" --> "*" Mesa
    Restaurante "1" --> "*" Producto
    Mesa "1" --> "*" SesionMesa
    SesionMesa "1" --> "*" Comanda
    Comanda "1" --> "*" LineaPedido
    LineaPedido --> Producto
```

---

# 7. Diagrama de estados — Línea de pedido

```mermaid
stateDiagram-v2
    [*] --> BORRADOR
    BORRADOR --> ENVIADA: Mozo envía comanda
    ENVIADA --> EN_PREPARACION: Cocina inicia
    EN_PREPARACION --> LISTA: Cocina termina ítem
    LISTA --> ENTREGADA: Mozo sirve
    BORRADOR --> ANULADA: Mozo cancela
    ENVIADA --> ANULADA: Admin autoriza
```

---

# 8. Diagrama de despliegue (VPS)

```mermaid
flowchart TB
    subgraph VPS[VPS Ubuntu]
        NGINX[Nginx :443]
        GW[api-gateway :8080]
        EU[eureka :8761]
        CFG[config :8888]
        AUTH[auth :8083]
        PROD[producto :8081]
        PED[pedido :8082]
        DB[(PostgreSQL)]
    end
    Client[Cliente HTTPS] --> NGINX --> GW
    GW --> AUTH & PROD & PED
    AUTH & PROD & PED --> DB
    PROD & PED & AUTH & GW --> EU
    PROD & PED & AUTH --> CFG
```

---

# 9. Trazabilidad requisitos — casos de uso

| RF (DOC-02) | Caso de uso |
|-------------|-------------|
| RF-01, RF-01b | UC-01 |
| RF-02 | UC-03 |
| Producto CRUD | UC-02 |
| RF-03, RF-04 | UC-04, UC-05 |
| RF-05, RF-06 | UC-06 |
| RF-07 | UC-07 |
| RF-08 | UC-08 |

---

*Fin DOC-03 — Exportar diagramas mermaid a imagen si el informe Word no admite mermaid nativo.*

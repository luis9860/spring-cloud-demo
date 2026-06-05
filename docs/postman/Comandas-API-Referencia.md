# Comandas de Restaurantes — Especificación de APIs
## DOC-07 | Referencia REST (Postman)

**Base URL:** `http://{host}:8080`  
**Formato:** JSON  
**Autenticación:** Bearer JWT (salvo `/api/publico/**`)

---

# 1. Variables de entorno Postman

| Variable | Ejemplo | Descripción |
|----------|---------|-------------|
| `baseUrl` | `http://localhost:8080` | Gateway |
| `token` | (auto login) | JWT |
| `mesaId` | `1` | Pruebas |
| `comandaId` | `1` | Pruebas |
| `qrToken` | `abc123...` | QR mesa |

---

# 2. Autenticación

## POST {{baseUrl}}/api/auth/login
**Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```
**200:**
```json
{
  "token": "eyJhbG...",
  "rol": "ADMIN",
  "restauranteId": 1,
  "nombreRestaurante": "Restaurante Piloto"
}
```

## POST {{baseUrl}}/api/auth/usuarios
**Headers:** `Authorization: Bearer {{token}}` (ADMIN)  
**Body:**
```json
{
  "username": "mozo2",
  "password": "mozo123",
  "rol": "MOZO"
}
```

---

# 3. Productos

## GET {{baseUrl}}/api/productos
**Headers:** Bearer  
**Query:** `?estacion=COCINA`

## POST {{baseUrl}}/api/productos
```json
{
  "nombre": "Lomo saltado",
  "precio": 28.50,
  "categoriaId": 1,
  "estacion": "COCINA"
}
```

---

# 4. Mesas y pedidos

## GET {{baseUrl}}/api/mesas

## POST {{baseUrl}}/api/mesas
**Headers:** `X-Rol: ADMIN`  
El **número** de mesa (1, 2, 3…) se asigna automáticamente en orden de creación.
```json
{
  "capacidadSillas": 4,
  "codigo": "VIP01"
}
```
Si `codigo` se omite → `M01`, `M02`, … según el siguiente número.

## PUT {{baseUrl}}/api/mesas/{{mesaId}}
```json
{ "codigo": "M10", "capacidadSillas": 6 }
```

## DELETE {{baseUrl}}/api/mesas/{{mesaId}}
Solo si la mesa está LIBRE.

## POST {{baseUrl}}/api/mesas/{{mesaId}}/sillas
Body vacío `{}` o sin body → siguiente silla (1, 2, 3…).  
Código silla = `M10-S01`, `M10-S02`, …

## DELETE {{baseUrl}}/api/mesas/sillas/{{sillaId}}

## POST {{baseUrl}}/api/mesas/{{mesaId}}/abrir

## POST {{baseUrl}}/api/mesas/{{mesaId}}/sillas/2/lineas
```json
{
  "productoId": 1,
  "cantidad": 2,
  "notas": "Sin cebolla"
}
```

## POST {{baseUrl}}/api/comandas/{{comandaId}}/enviar

## GET {{baseUrl}}/api/comandas/pendientes?estacion=COCINA

## PATCH {{baseUrl}}/api/lineas/{{lineaId}}/estado
```json
{
  "estado": "LISTA"
}
```

## GET {{baseUrl}}/api/mozo/notificaciones

---

# 5. Público (QR)

## GET {{baseUrl}}/api/publico/pedido/{{qrToken}}
**Sin Authorization**

**200 ejemplo:**
```json
{
  "mesa": 5,
  "restaurante": "Restaurante Piloto",
  "lineas": [
    { "nombre": "Chicha morada", "estado": "LISTA" },
    { "nombre": "Lomo saltado", "estado": "EN_PREPARACION" }
  ]
}
```

---

# 6. Demo legacy (compatibilidad curso)

## GET {{baseUrl}}/api/pedidos/simular/1?cantidad=2

---

# 7. Códigos HTTP estándar

| Código | Uso |
|--------|-----|
| 200 | OK lectura |
| 201 | Creado |
| 204 | Sin contenido |
| 400 | Validación |
| 401 | No autenticado |
| 403 | Sin permiso / límite plan |
| 404 | No encontrado |
| 500 | Error servidor |

---

# 8. Colección Postman (estructura de carpetas)

```
Comandas API/
  Auth/
    Login
    Crear usuario
  Productos/
    Listar
    Crear
  Mesas/
    Listar
    Crear
    Abrir sesión
  Comandas/
    Agregar línea
    Enviar
    Pendientes cocina
    Marcar lista
    Notificaciones mozo
  Publico/
    QR pedido
  Demo/
    Simular pedido
```

*Importar manualmente o exportar JSON cuando exista implementación.*

---

*Fin DOC-07*

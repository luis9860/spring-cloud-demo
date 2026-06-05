# Comandas de Restaurantes — Frontend Angular

Angular 19 · consume el **API Gateway** en `http://localhost:8080/api`.

## Requisitos

- Node 20+ y npm
- Backend: Eureka, Config, auth, producto, pedido, **gateway** (puerto 8080)

## Arranque

```bash
cd 07-frontend-comandas
npm start
```

Abrir http://localhost:4200

## Rutas

| Ruta | Rol | Descripción |
|------|-----|-------------|
| `/login` | — | Inicio de sesión |
| `/mozo` | MOZO, ADMIN | Mesas, pedido, enviar a cocina |
| `/cocina` | COCINERO, ADMIN | Cola y marcar LISTA |
| `/qr?token=mesa-demo-1` | Público | Seguimiento comensal |

## Usuarios demo

- `mozo1` / `mozo123`
- `cocina1` / `cocina123`
- `admin` / `admin123`

## CORS

El Gateway debe permitir `http://localhost:4200` (ya configurado en `05-api-gateway`).

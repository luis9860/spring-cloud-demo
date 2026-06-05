# Comandas de Restaurantes — Manual de usuario
## DOC-06 | Guía de operación

**Versión software:** MVP 1.0  
**Autor:** Luis Alberto Arias Ledesma  
**Audiencia:** Administrador, mozo, cocinero, comensal (QR)

---

# 1. Introducción

**Comandas de Restaurantes** digitaliza la comunicación entre salón y cocina. Este manual describe el uso del **MVP**: panel administrador, aplicación mozo, pantalla cocina/barra y consulta del comensal por QR.

**Requisitos del usuario:**
- Dispositivo con navegador actualizado (Chrome, Safari, Edge)
- Conexión WiFi del restaurante
- Credenciales proporcionadas por el administrador (personal)

---

# 2. Roles y accesos

| Rol | URL sugerida | Funciones principales |
|-----|--------------|----------------------|
| Administrador | `/admin/` | Mesas, menú, usuarios, configuración |
| Mozo | `/mozo/` | Mesas, pedidos, notificaciones |
| Cocinero | `/cocina/` | Cola pedidos estación COCINA |
| Barra | `/barra/` | Cola pedidos estación BARRA |
| Comensal | QR en mesa | Solo consulta estado (sin login) |

---

# 3. Administrador / dueño

## 3.1 Iniciar sesión
1. Abra la URL del sistema en el navegador.
2. Ingrese **usuario** y **contraseña**.
3. Pulse **Ingresar**.

## 3.2 Configurar mesas (plan gratis: máximo 15)
1. Menú **Mesas** → **Nueva mesa**.
2. Indique **número** y **cantidad de sillas** (hasta 8).
3. Guarde. Si alcanzó 15 mesas, el sistema mostrará mensaje para plan premium.

## 3.3 Gestionar menú
1. Menú **Productos** → **Agregar**.
2. Complete nombre, precio, categoría y **estación** (Cocina o Barra).
3. Los cambios se reflejan de inmediato en la app del mozo.

## 3.4 Crear usuarios (mozo, cocinero)
1. Menú **Usuarios** → **Nuevo**.
2. Elija **rol**, usuario y contraseña temporal.
3. Entregue las credenciales al empleado (cambio de clave recomendado en futuras versiones).

## 3.5 Modo de servicio (A o B)
- **Modo A:** el mozo recibe un aviso cuando **toda** la comanda de la mesa/silla está lista.
- **Modo B:** el mozo recibe aviso **por cada plato** listo.

Configure en **Ajustes** → **Modo de servicio**.

---

# 4. Mozo

## 4.1 Flujo diario
1. Inicie sesión.
2. Seleccione **mesa** (verde = libre, rojo = ocupada).
3. Pulse **Abrir mesa** si está libre.
4. Elija **número de silla** del comensal.
5. Agregue productos del menú (cantidad y notas: “sin hielo”, etc.).
6. Pulse **Enviar a cocina** cuando el cliente confirme el pedido.
7. Puede cambiar a otra mesa y repetir (varias mesas en paralelo).
8. Al recibir **notificación**, lleve los platos a la mesa indicada.

## 4.2 Buenas prácticas
- Envíe a cocina por **tandas** (entradas, fondos, postres) sin cerrar la mesa.
- Verifique la **silla** correcta en mesas grandes.

---

# 5. Cocinero y barra

## 5.1 Pantalla de cola
1. Inicie sesión (cocina o barra).
2. Vea comandas ordenadas por hora.
3. Cada tarjeta muestra: **Mesa**, **Silla**, ítems y **notas**.

## 5.2 Actualizar estado
1. Pulse **En preparación** al iniciar un ítem.
2. Pulse **Listo** al terminar.
3. El mozo recibe aviso según modo A o B configurado en el local.

---

# 6. Comensal (QR en mesa)

1. Escanee el **código QR** en la mesa con la cámara del celular.
2. Se abre una página con el estado de su pedido.
3. Verá: **En preparación**, **Listo**, **Servido** por cada ítem.
4. No necesita instalar aplicación ni crear cuenta (MVP).

*Nota: La app comensal con mapa y pedido anticipado estará disponible en una versión futura.*

---

# 7. Mensajes de error frecuentes

| Mensaje | Causa | Acción |
|---------|-------|--------|
| Sesión expirada | Token vencido | Volver a iniciar sesión |
| Límite de mesas alcanzado | Plan gratis, 15 mesas | Contratar premium o eliminar mesa no usada |
| Mesa ocupada | Otra sesión abierta | Verificar con mozo o admin |
| Sin conexión | WiFi caído | Reintentar; avisar a administrador |

---

# 8. Soporte

Contacto del implementador o administrador del local para incidencias en horario de servicio.

---

*Fin DOC-06 — Insertar capturas de pantalla al implementar el frontend.*

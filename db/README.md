# Base de datos MySQL

Este proyecto usa MySQL local para los servicios con persistencia.

Credenciales configuradas:

- Usuario: `root`
- Password: `mysql`
- Host: `localhost`
- Puerto: `3306`

Bases de datos creadas por el script:

- `comandas_auth`
- `comandas_producto`
- `comandas_pedido`

## Inicializar

Ejecuta en MySQL Workbench o consola (ajusta la ruta a tu proyecto):

```sql
source db/init-comandas-mysql.sql;
```

O desde consola en la raíz del repo:

```bash
mysql -u root -pmysql < db/init-comandas-mysql.sql
```

Las tablas y datos demo se crean al iniciar los microservicios porque JPA esta configurado con `ddl-auto=update` y los servicios tienen `DataLoader`.

## VPS (producción)

En el servidor Ubuntu usa el script automatizado (usuario dedicado `comandas`, no `root`):

```bash
cd /opt/comandas/spring-cloud-demo
chmod +x scripts/setup-mysql-vps.sh
./scripts/setup-mysql-vps.sh
```

Esto instala MySQL, crea las 3 bases, genera `/etc/comandas/comandas.env` con `DB_USER`, `DB_PASSWORD` y `JWT_SECRET`, y configura los servicios systemd para leerlo.

Variables opcionales antes de ejecutar:

```bash
export COMANDAS_DB_PASSWORD='tu_password_seguro'
export JWT_SECRET='clave-jwt-minimo-32-caracteres'
./scripts/setup-mysql-vps.sh
```

Plantilla: [`scripts/comandas.env.example`](../scripts/comandas.env.example).

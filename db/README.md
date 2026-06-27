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

Ejecuta en MySQL Workbench o consola:

```sql
source D:/DAW II/spring-cloud-demo/db/init-comandas-mysql.sql;
```

O desde consola:

```bash
mysql -u root -pmysql < "D:/DAW II/spring-cloud-demo/db/init-comandas-mysql.sql"
```

Las tablas y datos demo se crean al iniciar los microservicios porque JPA esta configurado con `ddl-auto=update` y los servicios tienen `DataLoader`.

-- Comandas de Restaurantes — MySQL en VPS (usuario dedicado, no root)
-- Lo ejecuta scripts/setup-mysql-vps.sh automaticamente.
-- Solo usar manualmente si necesitas repetir el alta de bases:

CREATE DATABASE IF NOT EXISTS comandas_auth
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS comandas_producto
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS comandas_pedido
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usuario y GRANT: ver scripts/setup-mysql-vps.sh (password en /etc/comandas/comandas.env)

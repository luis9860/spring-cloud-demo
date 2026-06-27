-- Comandas de Restaurantes - MySQL local
-- Usuario esperado por los servicios: root
-- Password esperado por los servicios: mysql

CREATE DATABASE IF NOT EXISTS comandas_auth
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS comandas_producto
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS comandas_pedido
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Las tablas se crean/actualizan automaticamente al iniciar cada microservicio
-- porque los application.yml usan spring.jpa.hibernate.ddl-auto=update.

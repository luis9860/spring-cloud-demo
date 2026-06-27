#!/usr/bin/env bash
# Instala MySQL en Ubuntu VPS, crea bases/usuario y genera /etc/comandas/comandas.env
# Ejecutar UNA VEZ en el servidor (como usuario ubuntu):
#   cd /opt/comandas/spring-cloud-demo && chmod +x scripts/setup-mysql-vps.sh && ./scripts/setup-mysql-vps.sh
#
# Opcional antes de ejecutar:
#   export COMANDAS_DB_USER=comandas
#   export COMANDAS_DB_PASSWORD='tu_password_seguro'
#   export JWT_SECRET='clave-jwt-minimo-32-caracteres-produccion'
set -euo pipefail

ENV_FILE="/etc/comandas/comandas.env"
DB_USER="${COMANDAS_DB_USER:-comandas}"

if [ "$(id -u)" -eq 0 ]; then
  echo "Ejecute como usuario ubuntu (el script usa sudo internamente)."
  exit 1
fi

echo "==> Instalar MySQL Server..."
sudo DEBIAN_FRONTEND=noninteractive apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq mysql-server

echo "==> Habilitar MySQL..."
sudo systemctl enable mysql
sudo systemctl start mysql

if [ -f "$ENV_FILE" ] && [ -z "${COMANDAS_DB_PASSWORD:-}" ]; then
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  DB_PASS="${DB_PASSWORD:?No se pudo leer DB_PASSWORD de $ENV_FILE}"
  JWT_SECRET="${JWT_SECRET:-comandas-cibertec-mvp-secret-key-2026-min-32-chars}"
  echo "==> Reutilizando credenciales existentes de $ENV_FILE"
else
  DB_PASS="${COMANDAS_DB_PASSWORD:-$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-24)}"
  JWT_SECRET="${JWT_SECRET:-$(openssl rand -base64 36 | tr -d '/+=' | cut -c1-48)}"
fi

echo "==> Crear bases de datos y usuario MySQL '$DB_USER'..."
sudo mysql <<EOF
CREATE DATABASE IF NOT EXISTS comandas_auth
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS comandas_producto
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS comandas_pedido
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
ALTER USER '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';

GRANT ALL PRIVILEGES ON comandas_auth.* TO '${DB_USER}'@'localhost';
GRANT ALL PRIVILEGES ON comandas_producto.* TO '${DB_USER}'@'localhost';
GRANT ALL PRIVILEGES ON comandas_pedido.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
EOF

echo "==> Escribir variables de entorno en $ENV_FILE ..."
sudo mkdir -p /etc/comandas
sudo tee "$ENV_FILE" > /dev/null <<EOF
# Comandas VPS — generado por scripts/setup-mysql-vps.sh
# NO subir este archivo a GitHub
DB_HOST=127.0.0.1
DB_PORT=3306
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASS}
JWT_SECRET=${JWT_SECRET}
EOF
sudo chmod 640 "$ENV_FILE"
sudo chown root:ubuntu "$ENV_FILE"

echo "==> Verificar conexion..."
mysql -h 127.0.0.1 -u "$DB_USER" -p"$DB_PASS" -e "SHOW DATABASES LIKE 'comandas_%';" > /dev/null

echo ""
echo "==> MySQL listo para Comandas."
echo "    Bases: comandas_auth, comandas_producto, comandas_pedido"
echo "    Usuario: $DB_USER"
echo "    Config:  $ENV_FILE"
echo ""
echo "Siguiente paso: ./scripts/deploy-vps.sh"

#!/usr/bin/env bash
# Primera instalacion en Ubuntu VPS (nginx + systemd). Ejecutar una vez en el servidor.
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/comandas/spring-cloud-demo}"
cd "$APP_DIR"

echo "==> Paquetes..."
sudo DEBIAN_FRONTEND=noninteractive apt-get update -qq
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq git openjdk-17-jdk maven nodejs npm nginx rsync

echo "==> systemd units..."
for f in scripts/systemd/comandas-*.service; do
  sudo cp "$f" /etc/systemd/system/
  sudo systemctl enable "$(basename "$f")"
done

LEGACY_UNITS=(eureka.service config-server.service gateway.service producto.service pedido.service)
echo "==> Desactivar unidades legacy duplicadas..."
for unit in "${LEGACY_UNITS[@]}"; do
  sudo systemctl disable --now "$unit" 2>/dev/null || true
done

sudo systemctl daemon-reload

echo "==> nginx..."
sudo cp scripts/nginx/comandas.conf /etc/nginx/sites-available/comandas
sudo ln -sf /etc/nginx/sites-available/comandas /etc/nginx/sites-enabled/comandas
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl enable nginx

echo "==> Directorio web..."
sudo mkdir -p /var/www/comandas
sudo chown -R ubuntu:www-data /var/www/comandas

echo "==> Firewall (si ufw esta activo)..."
if command -v ufw >/dev/null && sudo ufw status | grep -q "Status: active"; then
  sudo ufw allow OpenSSH
  sudo ufw allow 80/tcp
  sudo ufw allow 443/tcp
fi

echo "==> Setup base listo. Ejecute: ./scripts/deploy-vps.sh"

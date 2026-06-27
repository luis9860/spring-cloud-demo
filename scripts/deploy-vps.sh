#!/usr/bin/env bash
# Ejecutar EN EL VPS después de git pull (ver docs/Despliegue-GitHub-VPS.md)
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/comandas/spring-cloud-demo}"
cd "$APP_DIR"

if ! systemctl is-active --quiet mysql 2>/dev/null; then
  echo "ERROR: MySQL no esta activo. Ejecute primero: ./scripts/setup-mysql-vps.sh"
  exit 1
fi

if [ ! -f /etc/comandas/comandas.env ]; then
  echo "ERROR: Falta /etc/comandas/comandas.env. Ejecute: ./scripts/setup-mysql-vps.sh"
  exit 1
fi

echo "==> Compilar microservicios (sin tests)..."
for dir in 01-eureka-server 02-config-server 03-producto-service 04-pedido-service 06-auth-service 05-api-gateway; do
  echo "    mvn -q package -DskipTests -f $dir/pom.xml"
  mvn -q package -DskipTests -f "$dir/pom.xml"
done

echo "==> Frontend Angular (produccion, apiUrl=/api)..."
cd 07-frontend-comandas
npm ci
npm run build -- --configuration production
cd ..

FRONT_DIST="07-frontend-comandas/dist/07-frontend-comandas/browser"
if [ ! -d "$FRONT_DIST" ]; then
  FRONT_DIST="07-frontend-comandas/dist/07-frontend-comandas"
fi

echo "==> Publicar frontend en /var/www/comandas..."
sudo mkdir -p /var/www/comandas
sudo rsync -a --delete "$FRONT_DIST/" /var/www/comandas/
sudo chown -R ubuntu:www-data /var/www/comandas

echo "==> nginx..."
if [ -f scripts/nginx/comandas.conf ]; then
  sudo cp scripts/nginx/comandas.conf /etc/nginx/sites-available/comandas
  sudo ln -sf /etc/nginx/sites-available/comandas /etc/nginx/sites-enabled/comandas
  sudo rm -f /etc/nginx/sites-enabled/default
  sudo nginx -t
  sudo systemctl reload nginx || sudo systemctl restart nginx
fi

echo "==> Actualizar units systemd..."
for f in scripts/systemd/comandas-*.service; do
  sudo cp "$f" /etc/systemd/system/
done
sudo systemctl daemon-reload

echo "==> Reiniciar microservicios..."
chmod +x scripts/restart-services.sh
./scripts/restart-services.sh

echo "==> Despliegue terminado."
echo "    Web: http://$(curl -s ifconfig.me 2>/dev/null || hostname -I | awk '{print $1}')/"

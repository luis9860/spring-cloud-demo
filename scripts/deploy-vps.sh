#!/usr/bin/env bash
# Ejecutar EN EL VPS después de git pull (ver docs/Despliegue-GitHub-VPS.md)
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/comandas/spring-cloud-demo}"
cd "$APP_DIR"

echo "==> Compilar microservicios (sin tests)..."
for dir in 01-eureka-server 02-config-server 03-producto-service 04-pedido-service 06-auth-service 05-api-gateway; do
  echo "    mvn -q package -DskipTests -f $dir/pom.xml"
  mvn -q package -DskipTests -f "$dir/pom.xml"
done

echo "==> Frontend Angular..."
cd 07-frontend-comandas
npm ci
npm run build -- --configuration production
cd ..

echo "==> Reiniciar servicios (systemd)..."
# Ajusta nombres si creaste otros units en el VPS
for svc in comandas-eureka comandas-config comandas-auth comandas-producto comandas-pedido comandas-gateway comandas-frontend; do
  if systemctl is-enabled "$svc" &>/dev/null; then
    sudo systemctl restart "$svc"
    echo "    restarted $svc"
  fi
done

echo "==> Despliegue terminado."

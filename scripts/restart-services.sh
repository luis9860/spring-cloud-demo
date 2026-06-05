#!/usr/bin/env bash
set -euo pipefail

wait_port() {
  local port="$1"
  local name="$2"
  echo "    esperando $name (puerto $port)..."
  for _ in $(seq 1 90); do
    if ss -ltn | awk '{print $4}' | grep -q ":${port}$"; then
      echo "    $name listo"
      return 0
    fi
    sleep 2
  done
  echo "    ERROR: $name no respondio en puerto $port"
  return 1
}

restart_unit() {
  local unit="$1"
  sudo systemctl restart "$unit"
  sudo systemctl is-active --quiet "$unit"
}

echo "==> Reiniciar stack Comandas (orden Eureka -> Gateway)..."

sudo systemctl daemon-reload

restart_unit comandas-eureka.service
wait_port 8761 "Eureka"

restart_unit comandas-config.service
wait_port 8888 "Config Server"

restart_unit comandas-auth.service
restart_unit comandas-producto.service
restart_unit comandas-pedido.service
wait_port 8081 "Producto"
wait_port 8082 "Pedido"
wait_port 8083 "Auth"

restart_unit comandas-gateway.service
wait_port 8080 "Gateway"

echo "==> Stack Java activo."

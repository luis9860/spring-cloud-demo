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
  if ! sudo systemctl is-active --quiet "$unit"; then
    echo "    ERROR: $unit no quedo activo"
    sudo systemctl status "$unit" --no-pager || true
    sudo journalctl -u "$unit" -n 40 --no-pager || true
    return 1
  fi
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "==> Reiniciar stack Comandas (orden Eureka -> Gateway)..."

if [ -f "$SCRIPT_DIR/cleanup-stale-java.sh" ]; then
  bash "$SCRIPT_DIR/cleanup-stale-java.sh"
fi

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

echo "==> Health check API (login via gateway)..."
for _ in $(seq 1 30); do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST http://127.0.0.1:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}' || echo "000")
  if [ "$code" = "200" ]; then
    echo "    API login OK (HTTP 200)"
    break
  fi
  sleep 3
done
if [ "${code:-000}" != "200" ]; then
  echo "    ERROR: gateway activo pero login devolvio HTTP ${code:-000}"
  sudo journalctl -u comandas-gateway -n 30 --no-pager || true
  sudo journalctl -u comandas-auth -n 30 --no-pager || true
  exit 1
fi

echo "==> Stack Java activo."

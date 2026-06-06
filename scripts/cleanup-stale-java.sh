#!/usr/bin/env bash
# Mata procesos Java huérfanos que ocupan puertos del stack Comandas
# (p. ej. jars viejos en ~/spring-cloud-demo/ antes de systemd)
set -euo pipefail

PORTS=(8761 8888 8080 8081 8082 8083)

LEGACY_UNITS=(eureka.service config-server.service gateway.service producto.service pedido.service)
COMANDAS_UNITS=(comandas-gateway comandas-auth comandas-producto comandas-pedido comandas-config comandas-eureka)

echo "==> Desactivar unidades legacy (~/spring-cloud-demo)..."
for unit in "${LEGACY_UNITS[@]}"; do
  sudo systemctl disable --now "$unit" 2>/dev/null || true
done

echo "==> Detener unidades systemd Comandas..."
for unit in "${COMANDAS_UNITS[@]}"; do
  sudo systemctl stop "$unit" 2>/dev/null || true
done
sleep 2

echo "==> Matar JARs legacy en /home/ubuntu/spring-cloud-demo..."
legacy_pids=$(pgrep -f '/home/ubuntu/spring-cloud-demo/.*\.jar' || true)
for pid in $legacy_pids; do
  echo "    kill $pid"
  sudo kill "$pid" 2>/dev/null || true
done
sleep 2

echo "==> Liberar puertos ${PORTS[*]}..."
for port in "${PORTS[@]}"; do
  pids=$(sudo lsof -t -i:"$port" 2>/dev/null || true)
  if [ -n "$pids" ]; then
    echo "    puerto $port -> kill $pids"
    echo "$pids" | xargs -r sudo kill 2>/dev/null || true
  fi
done
sleep 2

echo "==> Procesos Java restantes:"
pgrep -af java || echo "    (ninguno)"

#!/usr/bin/env bash
# Diagnostico rapido en el VPS cuando nginx devuelve 502 en /api/
set -uo pipefail

echo "==> Servicios systemd Comandas"
for u in comandas-eureka comandas-config comandas-auth comandas-producto comandas-pedido comandas-gateway mysql nginx; do
  printf "  %-22s " "$u:"
  systemctl is-active "$u" 2>/dev/null || echo "no instalado"
done

echo
echo "==> Puertos Java (8080 gateway, 8083 auth, 8761 eureka)"
ss -ltn | grep -E ':8080|:8083|:8761|:8888|:8081|:8082' || echo "  (ninguno escuchando)"

echo
echo "==> Prueba login local via gateway"
code=$(curl -s -o /dev/null -w "%{http_code}" \
  -X POST http://127.0.0.1:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' || echo "000")
echo "  HTTP $code (esperado 200)"

echo
echo "==> Ultimas lineas gateway (si falla)"
journalctl -u comandas-gateway -n 25 --no-pager 2>/dev/null || true

echo
echo "Para reiniciar todo: cd /opt/comandas/spring-cloud-demo && ./scripts/restart-services.sh"

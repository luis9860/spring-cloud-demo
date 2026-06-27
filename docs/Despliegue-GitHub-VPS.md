# Subir a GitHub y desplegar automático en VPS

## Resumen

1. Creas un repositorio en **GitHub** (vacío).
2. Subes este proyecto con **git push**.
3. En el **VPS** clonas el repo una vez y preparas Java 17, Maven, Node y (opcional) systemd.
4. En GitHub configuras **Secrets** para que, en cada `push` a `main`, un workflow se conecte por SSH al VPS, haga `git pull` y ejecute `scripts/deploy-vps.sh`.

No hace falta entrar manualmente al VPS en cada cambio si el workflow está bien configurado.

---

## Paso 1 — GitHub (en tu PC)

```powershell
cd c:\Users\USUARIO.DESKTOP-5UKTARA\Pictures\DAW2\spring-cloud-demo

git init
git add .
git commit -m "Comandas Restaurantes - MVP Spring Cloud"

# Crea el repo en github.com (ej. tu-usuario/spring-cloud-demo), luego:
git branch -M main
git remote add origin https://github.com/TU_USUARIO/spring-cloud-demo.git
git push -u origin main
```

**No subas** contraseñas, `.env` con claves JWT reales ni `application-prod.yml` con secretos. Usa variables de entorno en el VPS.

---

## Paso 2 — VPS (una sola vez)

Requisitos típicos (Ubuntu/Debian):

- Java 17, Maven 3.9+
- Node 20+ y npm (para Angular)
- Git
- Puertos abiertos: 80/443 (nginx), 8080 (gateway), etc.

```bash
sudo mkdir -p /opt/comandas
sudo chown $USER:$USER /opt/comandas
cd /opt/comandas
git clone https://github.com/TU_USUARIO/spring-cloud-demo.git
cd spring-cloud-demo
chmod +x scripts/deploy-vps.sh
```

Genera clave SSH **solo para deploy** (en el VPS o en tu PC):

```bash
ssh-keygen -t ed25519 -f ~/.ssh/github_deploy -N ""
cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys   # en el VPS
```

Copia el contenido de `github_deploy` (privada) para el Secret de GitHub.

---

## Paso 3 — Actions (ya están en el repo; no hay que crearlos a mano)

GitHub los activa solo al detectar `.github/workflows/`:

| Workflow | Archivo | ¿Necesita VPS? |
|----------|---------|----------------|
| **CI Build** | `ci-build.yml` | **No** — compila Java + Angular en cada `push` a `main` |
| **Deploy to VPS** | `deploy-vps.yml` | **Sí** — SSH al servidor |

### Secrets (solo tú puedes ponerlos en la web de GitHub)

**Settings → Secrets and variables → Actions → Secrets → New repository secret**

| Secret | Ejemplo |
|--------|---------|
| `VPS_HOST` | `123.45.67.89` o `mi-dominio.com` |
| `VPS_USER` | `ubuntu` |
| `VPS_SSH_KEY` | contenido completo de la clave **privada** |
| `VPS_PORT` | `22` (opcional) |
| `VPS_APP_DIR` | `/opt/comandas/spring-cloud-demo` (opcional) |

### Variable para deploy automático en cada push

**Settings → Secrets and variables → Actions → Variables → New repository variable**

| Variable | Valor |
|----------|--------|
| `VPS_DEPLOY_ENABLED` | `true` (cuando el VPS y los Secrets ya estén listos) |

Sin esa variable, el deploy solo se lanza **manual**: pestaña **Actions → Deploy to VPS → Run workflow**.

Los workflows están en [`.github/workflows/`](../.github/workflows/).

---

## Paso 4 — Servicios en el VPS (systemd, recomendado)

El script `scripts/deploy-vps.sh` reinicia units si existen:

- `comandas-eureka`, `comandas-config`, `comandas-auth`
- `comandas-producto`, `comandas-pedido`, `comandas-gateway`
- `comandas-frontend` (nginx sirviendo `07-frontend-comandas/dist`)

Cada unit puede ejecutar el JAR:

```bash
java -jar /opt/comandas/spring-cloud-demo/04-pedido-service/target/pedido-service-*.jar
```

Orden de arranque: Eureka → Config → auth, producto, pedido → Gateway → nginx con el build de Angular.

**Producción:** los servicios usan **MySQL** (datos persistentes). En el VPS ejecuta una vez:

```bash
cd /opt/comandas/spring-cloud-demo
chmod +x scripts/setup-mysql-vps.sh
./scripts/setup-mysql-vps.sh
```

Eso instala MySQL, crea las bases `comandas_auth`, `comandas_producto`, `comandas_pedido` y el archivo `/etc/comandas/comandas.env` con credenciales. Los units systemd (`comandas-auth`, `comandas-producto`, `comandas-pedido`, `comandas-gateway`) leen ese archivo.

Luego cada deploy con `deploy-vps.sh` compila, publica el frontend y reinicia los servicios.

Ver también [`db/README.md`](../db/README.md).

---

## Paso 5 — Probar el pipeline

```powershell
git add .
git commit -m "fix: mozo parcial servido"
git push origin main
```

En GitHub: pestaña **Actions** → workflow **Deploy to VPS** → debe quedar en verde.

---

## Alternativas

| Opción | Cuándo usarla |
|--------|----------------|
| **GitHub Actions + SSH** (este repo) | VPS propio, control total |
| **Docker Compose** en VPS | Más portable; habría que añadir `docker-compose.yml` |
| **Railway / Render / Fly.io** | Menos configuración; otro modelo de precios |

---

## Checklist rápido

- [ ] Repo en GitHub con rama `main`
- [ ] `.gitignore` evita `node_modules` y `target/`
- [ ] VPS con clone en `VPS_APP_DIR`
- [ ] Secrets `VPS_HOST`, `VPS_USER`, `VPS_SSH_KEY`
- [ ] `deploy-vps.sh` probado manualmente una vez en el VPS
- [ ] Firewall: solo 80/443 públicos; microservicios detrás de nginx/gateway

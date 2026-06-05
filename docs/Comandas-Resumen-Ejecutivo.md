# Comandas de Restaurantes — Resumen ejecutivo

**Autor:** Luis Alberto Arias Ledesma  
**Institución:** Cibertec — Desarrollo de Servicios Web 2  
**Fecha:** 4 de junio de 2026

---

## Problema

Los restaurantes de menú en Lima operan con **papel y comunicación verbal** entre mozo y cocina, generando errores, demoras y mala experiencia del comensal.

## Solución

**Comandas de Restaurantes**: plataforma digital con app para **mozo y cocina** (móvil/tablet), **seguimiento del comensal** por QR, planes **gratis hasta 15 mesas** y evolución **premium** (más mesas, branding). Fase futura: **app comensal** tipo marketplace (mapa, pedido anticipado, autoatención con mesas en tiempo real).

## Modelo de negocio

- **Gratis:** operación completa hasta 15 mesas, sin personalización visual.  
- **Premium:** más mesas, logo, colores, dashboard, inventario.  
- **Ingresos adicionales:** publicidad y comisión en app comensal.

## Tecnología

Java 17, Spring Boot 3.5.14, Spring Cloud (Eureka, Config, Gateway), microservicios **producto** y **pedido**, PostgreSQL, despliegue en **VPS** con systemd. Alineado al curso DSW y demo Cibertec.

## Estado actual

- Documentación completa (negocio, técnica, UML, DER, pruebas, manual, APIs, despliegue).  
- Demo Spring Cloud **operativo en VPS**.  
- MVP de comandas en **desarrollo** (BD, auth, fronts).

## Próximos pasos

1. Implementar MVP mozo–cocina–QR.  
2. Piloto en restaurante Lima.  
3. Fases premium y app comensal.

---

*Documento de una página para portada de informe integrado — ver `00-Indice-Documentacion.md`.*

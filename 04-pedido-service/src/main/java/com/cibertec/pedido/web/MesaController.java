package com.cibertec.pedido.web;

import com.cibertec.pedido.dto.AgregarLineaRequest;
import com.cibertec.pedido.dto.ActualizarMesaRequest;
import com.cibertec.pedido.dto.CrearMesaRequest;
import com.cibertec.pedido.dto.CrearSillaRequest;
import com.cibertec.pedido.service.ComandaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mesas")
public class MesaController {

    private final ComandaService comandaService;

    public MesaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping
    public List<Map<String, Object>> listar(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.listarMesas(restauranteId);
    }

    @PostMapping
    public Map<String, Object> crear(
            @RequestBody CrearMesaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        return comandaService.crearMesa(restauranteId, request, rol);
    }

    @PutMapping("/{mesaId}")
    public Map<String, Object> actualizar(
            @PathVariable Long mesaId,
            @RequestBody ActualizarMesaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        return comandaService.actualizarMesa(mesaId, restauranteId, request, rol);
    }

    @DeleteMapping("/{mesaId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        comandaService.eliminarMesa(mesaId, restauranteId, rol);
    }

    @GetMapping("/{mesaId}/sillas")
    public List<Map<String, Object>> listarSillas(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.listarSillas(mesaId, restauranteId);
    }

    @PostMapping("/{mesaId}/sillas")
    public Map<String, Object> crearSilla(
            @PathVariable Long mesaId,
            @RequestBody(required = false) CrearSillaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        return comandaService.crearSilla(mesaId, restauranteId, request, rol);
    }

    @DeleteMapping("/sillas/{sillaId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void eliminarSilla(
            @PathVariable Long sillaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        comandaService.eliminarSilla(sillaId, restauranteId, rol);
    }

    @PostMapping("/{mesaId}/abrir")
    public Map<String, Object> abrir(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.abrirMesa(mesaId, restauranteId);
    }

    @PostMapping("/{mesaId}/cerrar")
    public void cerrar(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        comandaService.cerrarMesa(mesaId, restauranteId);
    }

    @GetMapping("/{mesaId}/sesion-activa")
    public Map<String, Object> sesionActiva(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.sesionActiva(mesaId, restauranteId);
    }

    @PostMapping("/{mesaId}/sillas/{silla}/lineas")
    public Map<String, Object> agregarLinea(
            @PathVariable Long mesaId,
            @PathVariable int silla,
            @RequestBody AgregarLineaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.agregarLinea(mesaId, silla, request, restauranteId);
    }
}

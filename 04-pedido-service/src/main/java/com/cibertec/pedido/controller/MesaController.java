package com.cibertec.pedido.controller;

import com.cibertec.pedido.dto.AgregarLineaRequest;
import com.cibertec.pedido.dto.ActualizarMesaRequest;
import com.cibertec.pedido.dto.CrearMesaRequest;
import com.cibertec.pedido.dto.CrearSillaRequest;
import com.cibertec.pedido.service.ComandaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mesas")
@Tag(name = "Mesas", description = "Gestión de mesas y sillas")
public class MesaController {

    private final ComandaService comandaService;

    public MesaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @GetMapping
    @Operation(summary = "Listar mesas del restaurante")
    public List<Map<String, Object>> listar(
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.listarMesas(restauranteId);
    }

    @PostMapping
    @Operation(summary = "Crear mesa (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mesa creada"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public Map<String, Object> crear(
            @RequestBody CrearMesaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        return comandaService.crearMesa(restauranteId, request, rol);
    }

    @PutMapping("/{mesaId}")
    @Operation(summary = "Actualizar mesa (ADMIN)")
    public Map<String, Object> actualizar(
            @PathVariable Long mesaId,
            @RequestBody ActualizarMesaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        return comandaService.actualizarMesa(mesaId, restauranteId, request, rol);
    }

    @DeleteMapping("/{mesaId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar mesa (ADMIN)")
    public void eliminar(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        comandaService.eliminarMesa(mesaId, restauranteId, rol);
    }

    @GetMapping("/{mesaId}/sillas")
    @Operation(summary = "Listar sillas de una mesa")
    public List<Map<String, Object>> listarSillas(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.listarSillas(mesaId, restauranteId);
    }

    @PostMapping("/{mesaId}/sillas")
    @Operation(summary = "Agregar silla a mesa (ADMIN)")
    public Map<String, Object> crearSilla(
            @PathVariable Long mesaId,
            @RequestBody(required = false) CrearSillaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        return comandaService.crearSilla(mesaId, restauranteId, request, rol);
    }

    @DeleteMapping("/sillas/{sillaId}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar silla (ADMIN)")
    public void eliminarSilla(
            @PathVariable Long sillaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId,
            @RequestHeader(value = "X-Rol", defaultValue = "") String rol) {
        comandaService.eliminarSilla(sillaId, restauranteId, rol);
    }

    @PostMapping("/{mesaId}/abrir")
    @Operation(summary = "Abrir sesión de mesa")
    public Map<String, Object> abrir(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.abrirMesa(mesaId, restauranteId);
    }

    @PostMapping("/{mesaId}/cerrar")
    @Operation(summary = "Cerrar sesión de mesa")
    public void cerrar(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        comandaService.cerrarMesa(mesaId, restauranteId);
    }

    @GetMapping("/{mesaId}/sesion-activa")
    @Operation(summary = "Obtener sesión activa y comandas")
    public Map<String, Object> sesionActiva(
            @PathVariable Long mesaId,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.sesionActiva(mesaId, restauranteId);
    }

    @PostMapping("/{mesaId}/sillas/{silla}/lineas")
    @Operation(summary = "Agregar línea de pedido a silla")
    public Map<String, Object> agregarLinea(
            @PathVariable Long mesaId,
            @PathVariable int silla,
            @RequestBody AgregarLineaRequest request,
            @RequestHeader(value = "X-Restaurante-Id", defaultValue = "1") Long restauranteId) {
        return comandaService.agregarLinea(mesaId, silla, request, restauranteId);
    }
}

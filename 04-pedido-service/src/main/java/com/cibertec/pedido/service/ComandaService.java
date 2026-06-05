package com.cibertec.pedido.service;

import com.cibertec.pedido.client.ProductoClient;
import com.cibertec.pedido.domain.CodigoMesaSilla;
import com.cibertec.pedido.domain.Estados;
import com.cibertec.pedido.dto.AgregarLineaRequest;
import com.cibertec.pedido.dto.ActualizarMesaRequest;
import com.cibertec.pedido.dto.CrearMesaRequest;
import com.cibertec.pedido.dto.CrearSillaRequest;
import com.cibertec.pedido.dto.IdentificarComensalRequest;
import com.cibertec.pedido.dto.Producto;
import com.cibertec.pedido.entity.*;
import com.cibertec.pedido.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class ComandaService {

    public static final String QR_LOCAL = "comandas-local";
    private static final long RESTAURANTE_PUBLICO_ID = 1L;

    private final MesaRepository mesaRepository;
    private final SillaRepository sillaRepository;
    private final SesionMesaRepository sesionMesaRepository;
    private final ComandaRepository comandaRepository;
    private final LineaPedidoRepository lineaPedidoRepository;
    private final ComensalSillaRepository comensalSillaRepository;
    private final ProductoClient productoClient;

    @Value("${pedido.plan.mesas-max-free:15}")
    private int mesasMaxFree;

    @Value("${pedido.modo-servicio-default:A}")
    private String modoServicioDefault;

    public ComandaService(MesaRepository mesaRepository,
                          SillaRepository sillaRepository,
                          SesionMesaRepository sesionMesaRepository,
                          ComandaRepository comandaRepository,
                          LineaPedidoRepository lineaPedidoRepository,
                          ComensalSillaRepository comensalSillaRepository,
                          ProductoClient productoClient) {
        this.mesaRepository = mesaRepository;
        this.sillaRepository = sillaRepository;
        this.sesionMesaRepository = sesionMesaRepository;
        this.comandaRepository = comandaRepository;
        this.lineaPedidoRepository = lineaPedidoRepository;
        this.comensalSillaRepository = comensalSillaRepository;
        this.productoClient = productoClient;
    }

    public List<Map<String, Object>> listarMesas(Long restauranteId) {
        return mesaRepository.findByRestauranteIdOrderByNumeroAsc(restauranteId).stream()
                .map(this::mesaToMap)
                .toList();
    }

    public List<Map<String, Object>> listarSillas(Long mesaId, Long restauranteId) {
        Mesa mesa = mesa(mesaId, restauranteId);
        return sillasDeMesa(mesa.getId());
    }

    @Transactional
    public Map<String, Object> crearMesa(Long restauranteId, CrearMesaRequest request, String rol) {
        requireAdmin(rol);
        if (mesaRepository.countByRestauranteId(restauranteId) >= mesasMaxFree) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Limite de mesas alcanzado (plan FREE: " + mesasMaxFree + ")");
        }
        int numero = mesaRepository.maxNumero(restauranteId) + 1;
        String codigo;
        if (request.codigo() != null && !request.codigo().isBlank()) {
            codigo = CodigoMesaSilla.codigoMesaNormalizado(request.codigo());
            if (mesaRepository.existsByRestauranteIdAndCodigo(restauranteId, codigo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Codigo de mesa ya existe: " + codigo);
            }
        } else {
            do {
                codigo = CodigoMesaSilla.codigoMesaPorNumero(numero);
                if (!mesaRepository.existsByRestauranteIdAndCodigo(restauranteId, codigo)
                        && !mesaRepository.existsByRestauranteIdAndNumero(restauranteId, numero)) {
                    break;
                }
                numero++;
            } while (numero < 1000);
            if (mesaRepository.existsByRestauranteIdAndNumero(restauranteId, numero)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay numero de mesa disponible; elimine mesas duplicadas o reinicie datos");
            }
        }
        Mesa m = new Mesa();
        m.setRestauranteId(restauranteId);
        m.setNumero(numero);
        m.setCodigo(codigo);
        m.setCapacidadSillas(request.capacidadSillas() > 0 ? request.capacidadSillas() : 4);
        m.setEstado(Estados.MESA_LIBRE);
        m.setQrToken(codigo.toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 8));
        mesaRepository.save(m);
        asegurarSillasHastaCapacidad(m);
        return mesaToMap(m);
    }

    @Transactional
    public Map<String, Object> actualizarMesa(Long mesaId, Long restauranteId, ActualizarMesaRequest request, String rol) {
        requireAdmin(rol);
        Mesa mesa = mesa(mesaId, restauranteId);
        if (Estados.MESA_OCUPADA.equals(mesa.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede editar una mesa ocupada");
        }
        if (request.capacidadSillas() != null && request.capacidadSillas() > 0) {
            long sillasActuales = sillaRepository.countByMesaIdAndActivaTrue(mesaId);
            if (request.capacidadSillas() < sillasActuales) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Capacidad no puede ser menor que sillas registradas (" + sillasActuales + ")");
            }
            mesa.setCapacidadSillas(request.capacidadSillas());
        }
        if (request.codigo() != null && !request.codigo().isBlank()) {
            String nuevoCodigo = CodigoMesaSilla.codigoMesaNormalizado(request.codigo());
            if (!nuevoCodigo.equals(mesa.getCodigo())
                    && mesaRepository.existsByRestauranteIdAndCodigo(restauranteId, nuevoCodigo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Codigo ya en uso: " + nuevoCodigo);
            }
            mesa.setCodigo(nuevoCodigo);
            recodificarSillas(mesa);
        }
        mesaRepository.save(mesa);
        asegurarSillasHastaCapacidad(mesa);
        return mesaToMap(mesa);
    }

    @Transactional
    public void eliminarMesa(Long mesaId, Long restauranteId, String rol) {
        requireAdmin(rol);
        Mesa mesa = mesa(mesaId, restauranteId);
        if (Estados.MESA_OCUPADA.equals(mesa.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede eliminar una mesa ocupada");
        }
        if (sesionMesaRepository.findByMesaIdAndActivaTrue(mesaId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mesa con sesion activa");
        }
        sillaRepository.findByMesaIdAndActivaTrueOrderByNumeroAsc(mesaId)
                .forEach(s -> sillaRepository.delete(s));
        mesaRepository.delete(mesa);
    }

    @Transactional
    public Map<String, Object> crearSilla(Long mesaId, Long restauranteId, CrearSillaRequest request, String rol) {
        requireAdmin(rol);
        Mesa mesa = mesa(mesaId, restauranteId);
        int numeroSilla = request != null && request.numero() != null && request.numero() > 0
                ? request.numero()
                : sillaRepository.maxNumero(mesaId) + 1;
        if (sillaRepository.existsByMesaIdAndNumero(mesaId, numeroSilla)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La silla ya existe en esta mesa");
        }
        if (sillaRepository.countByMesaIdAndActivaTrue(mesaId) >= mesa.getCapacidadSillas()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Capacidad maxima de sillas: " + mesa.getCapacidadSillas());
        }
        Silla silla = new Silla();
        silla.setMesaId(mesaId);
        silla.setNumero(numeroSilla);
        silla.setCodigo(CodigoMesaSilla.codigoSilla(mesa.getCodigo(), numeroSilla));
        sillaRepository.save(silla);
        return Map.of(
                "id", silla.getId(),
                "numero", silla.getNumero(),
                "codigo", silla.getCodigo(),
                "mesaCodigo", mesa.getCodigo(),
                "mesaNumero", mesa.getNumero());
    }

    @Transactional
    public void eliminarSilla(Long sillaId, Long restauranteId, String rol) {
        requireAdmin(rol);
        Silla silla = sillaRepository.findById(sillaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Silla no encontrada"));
        Mesa mesa = mesa(silla.getMesaId(), restauranteId);
        if (Estados.MESA_OCUPADA.equals(mesa.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mesa ocupada; no elimine sillas con pedido activo");
        }
        sillaRepository.delete(silla);
    }

    /** Crea sillas 1..capacidad que falten (mismo criterio que PedidoDataLoader). */
    private void asegurarSillasHastaCapacidad(Mesa mesa) {
        for (int s = 1; s <= mesa.getCapacidadSillas(); s++) {
            if (sillaRepository.findByMesaIdAndNumeroAndActivaTrue(mesa.getId(), s).isPresent()) {
                continue;
            }
            Silla silla = new Silla();
            silla.setMesaId(mesa.getId());
            silla.setNumero(s);
            silla.setCodigo(CodigoMesaSilla.codigoSilla(mesa.getCodigo(), s));
            sillaRepository.save(silla);
        }
    }

    private void recodificarSillas(Mesa mesa) {
        for (Silla s : sillaRepository.findByMesaIdAndActivaTrueOrderByNumeroAsc(mesa.getId())) {
            s.setCodigo(CodigoMesaSilla.codigoSilla(mesa.getCodigo(), s.getNumero()));
            sillaRepository.save(s);
        }
    }

    @Transactional
    public Map<String, Object> abrirMesa(Long mesaId, Long restauranteId) {
        Mesa mesa = mesa(mesaId, restauranteId);
        if (Estados.MESA_OCUPADA.equals(mesa.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mesa ya ocupada");
        }
        SesionMesa sesion = new SesionMesa();
        sesion.setMesaId(mesa.getId());
        sesion.setRestauranteId(restauranteId);
        sesion.setActiva(true);
        sesionMesaRepository.save(sesion);

        Comanda comanda = new Comanda();
        comanda.setSesionMesaId(sesion.getId());
        comanda.setNumeroTurno(1);
        comanda.setOrigen("MOZO");
        comanda.setEstado(Estados.COMANDA_BORRADOR);
        comanda.setModoServicio(modoServicioDefault);
        comandaRepository.save(comanda);

        mesa.setEstado(Estados.MESA_OCUPADA);
        mesaRepository.save(mesa);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("sesionId", sesion.getId());
        res.put("comandaId", comanda.getId());
        res.put("mesaId", mesa.getId());
        res.put("mesaCodigo", mesa.getCodigo());
        res.put("modoServicio", comanda.getModoServicio());
        return res;
    }

    @Transactional
    public void cerrarMesa(Long mesaId, Long restauranteId) {
        Mesa mesa = mesa(mesaId, restauranteId);
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay sesion activa"));

        for (Comanda comanda : comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId())) {
            for (LineaPedido linea : lineaPedidoRepository.findByComandaId(comanda.getId())) {
                if (Estados.LINEA_BORRADOR.equals(linea.getEstado())) {
                    linea.setEstado(Estados.LINEA_ANULADA);
                    lineaPedidoRepository.save(linea);
                }
            }
        }

        boolean platosPendientes = comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId()).stream()
                .flatMap(c -> lineaPedidoRepository.findByComandaId(c.getId()).stream())
                .anyMatch(l -> Estados.LINEA_ENVIADA.equals(l.getEstado())
                        || Estados.LINEA_EN_PREPARACION.equals(l.getEstado())
                        || Estados.LINEA_LISTA.equals(l.getEstado()));
        if (platosPendientes) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Hay platos en cocina o listos sin servir. Marque «Servido al cliente» o espere a que cocina termine.");
        }

        comensalSillaRepository.deleteBySesionMesaId(sesion.getId());
        sesion.setActiva(false);
        sesion.setCerradaEn(Instant.now());
        sesionMesaRepository.save(sesion);
        mesa.setEstado(Estados.MESA_LIBRE);
        mesaRepository.save(mesa);
    }

    public Map<String, Object> sesionActiva(Long mesaId, Long restauranteId) {
        Mesa mesa = mesa(mesaId, restauranteId);
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sin sesion activa"));
        List<Map<String, Object>> comandas = comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId())
                .stream()
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(),
                        "estado", c.getEstado(),
                        "numeroTurno", c.getNumeroTurno(),
                        "lineas", lineasResumen(c.getId(), mesa.getId())))
                .toList();
        return Map.of("sesionId", sesion.getId(), "mesaNumero", mesa.getNumero(), "mesaCodigo", mesa.getCodigo(), "comandas", comandas);
    }

    @Transactional
    public Map<String, Object> agregarLinea(Long mesaId, int silla, AgregarLineaRequest req, Long restauranteId) {
        Mesa mesa = mesa(mesaId, restauranteId);
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Abra la mesa primero"));
        Comanda comanda = comandaRepository.findBySesionMesaIdAndEstado(sesion.getId(), Estados.COMANDA_BORRADOR)
                .orElseGet(() -> nuevaComandaBorrador(sesion));

        Silla sillaEnt = sillaRepository.findByMesaIdAndNumeroAndActivaTrue(mesa.getId(), silla)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Silla no registrada. Codigo esperado: "
                                + CodigoMesaSilla.codigoSilla(mesa.getCodigo(), silla)));

        Producto producto = productoClient.obtenerProducto(req.productoId());
        LineaPedido linea = new LineaPedido();
        linea.setComandaId(comanda.getId());
        linea.setProductoId(producto.id());
        linea.setNumeroSilla(sillaEnt.getNumero());
        linea.setCantidad(req.cantidad() > 0 ? req.cantidad() : 1);
        linea.setNotas(req.notas());
        linea.setPrecioUnitario(BigDecimal.valueOf(producto.precio()));
        linea.setProductoNombreSnapshot(producto.nombre());
        linea.setEstacionProducto(producto.estacion() != null ? producto.estacion() : "COCINA");
        linea.setEstado(Estados.LINEA_BORRADOR);
        lineaPedidoRepository.save(linea);

        return Map.of(
                "lineaId", linea.getId(),
                "comandaId", comanda.getId(),
                "producto", producto.nombre(),
                "codigoSilla", sillaEnt.getCodigo());
    }

    @Transactional
    public Map<String, Object> enviarComanda(Long comandaId, Long restauranteId) {
        Comanda comanda = comanda(comandaId, restauranteId);
        if (!Estados.COMANDA_BORRADOR.equals(comanda.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comanda no esta en BORRADOR");
        }
        List<LineaPedido> lineas = lineaPedidoRepository.findByComandaId(comanda.getId());
        if (lineas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comanda sin lineas");
        }
        for (LineaPedido l : lineas) {
            if (Estados.LINEA_BORRADOR.equals(l.getEstado())) {
                l.setEstado(Estados.LINEA_ENVIADA);
                lineaPedidoRepository.save(l);
            }
        }
        comanda.setEstado(Estados.COMANDA_ENVIADA);
        comanda.setEnviadaEn(Instant.now());
        comandaRepository.save(comanda);
        return Map.of("comandaId", comanda.getId(), "estado", comanda.getEstado());
    }

    public List<Map<String, Object>> pendientesCocina(String estacion) {
        List<String> estados = List.of(Estados.LINEA_ENVIADA, Estados.LINEA_EN_PREPARACION);
        List<LineaPedido> lineas = estacion != null && !estacion.isBlank()
                ? lineaPedidoRepository.findByEstadoInAndEstacionProducto(estados, estacion)
                : lineaPedidoRepository.findByEstadoIn(estados);
        return lineas.stream().map(this::lineaDetalle).toList();
    }

    @Transactional
    public Map<String, Object> aceptarComandaEnCocina(Long comandaId, Long restauranteId) {
        Comanda comanda = comanda(comandaId, restauranteId);
        if (!Estados.COMANDA_ENVIADA.equals(comanda.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se aceptan comandas recien enviadas (estado: " + comanda.getEstado() + ")");
        }
        List<LineaPedido> lineas = lineaPedidoRepository.findByComandaId(comanda.getId());
        boolean alguna = false;
        for (LineaPedido l : lineas) {
            if (Estados.LINEA_ENVIADA.equals(l.getEstado())) {
                l.setEstado(Estados.LINEA_EN_PREPARACION);
                lineaPedidoRepository.save(l);
                alguna = true;
            }
        }
        if (!alguna) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay lineas nuevas para aceptar");
        }
        comanda.setEstado(Estados.COMANDA_EN_PREPARACION);
        comandaRepository.save(comanda);
        return Map.of("comandaId", comanda.getId(), "estado", comanda.getEstado());
    }

    @Transactional
    public Map<String, Object> actualizarEstadoLinea(Long lineaId, String nuevoEstado, Long restauranteId) {
        LineaPedido linea = lineaPedidoRepository.findById(lineaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linea no encontrada"));
        Comanda comanda = comanda(linea.getComandaId(), restauranteId);

        if (Estados.LINEA_EN_PREPARACION.equals(nuevoEstado)) {
            if (!Estados.LINEA_ENVIADA.equals(linea.getEstado())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo lineas ENVIADA pasan a preparacion");
            }
        } else if (Estados.LINEA_LISTA.equals(nuevoEstado)) {
            if (!Estados.LINEA_EN_PREPARACION.equals(linea.getEstado())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo lineas EN_PREPARACION pasan a LISTA");
            }
        } else if (Estados.LINEA_ANULADA.equals(nuevoEstado)) {
            if (!Estados.LINEA_BORRADOR.equals(linea.getEstado())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Solo se puede quitar lineas en carrito (antes de enviar a cocina)");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no permitido: " + nuevoEstado);
        }
        linea.setEstado(nuevoEstado);
        lineaPedidoRepository.save(linea);

        if (Estados.LINEA_ANULADA.equals(nuevoEstado)) {
            return Map.of("lineaId", linea.getId(), "estado", linea.getEstado(), "comandaEstado", comanda.getEstado());
        }

        if (Estados.LINEA_LISTA.equals(nuevoEstado)) {
            recalcularEstadoComanda(comanda);
        } else {
            comanda.setEstado(Estados.COMANDA_EN_PREPARACION);
            comandaRepository.save(comanda);
        }
        return Map.of("lineaId", linea.getId(), "estado", linea.getEstado(), "comandaEstado", comanda.getEstado());
    }

    public List<Map<String, Object>> notificacionesMozo(Long restauranteId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Mesa mesa : mesaRepository.findByRestauranteIdOrderByNumeroAsc(restauranteId)) {
            SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId()).orElse(null);
            if (sesion == null) {
                continue;
            }
            for (Comanda c : comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId())) {
                out.addAll(notificacionesMozoComanda(c, mesa));
            }
        }
        return out;
    }

    /** Platos con línea LISTA en mesas con sesión activa (misma fuente que la cola QR). */
    public List<Map<String, Object>> platosListosMozo(Long restauranteId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Mesa mesa : mesaRepository.findByRestauranteIdOrderByNumeroAsc(restauranteId)) {
            SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId()).orElse(null);
            if (sesion == null) {
                continue;
            }
            for (Comanda c : comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId())) {
                if (Estados.COMANDA_ENTREGADA.equals(c.getEstado())) {
                    continue;
                }
                List<String> prodLista = lineasActivasComanda(c.getId()).stream()
                        .filter(l -> Estados.LINEA_LISTA.equals(l.getEstado()))
                        .map(LineaPedido::getProductoNombreSnapshot)
                        .toList();
                if (!prodLista.isEmpty()) {
                    out.add(notificacionMozoRow(c, mesa, "LISTO", prodLista,
                            "Platos listos — llevar a la mesa"));
                }
            }
        }
        return out;
    }

    private List<Map<String, Object>> notificacionesMozoComanda(Comanda c, Mesa mesa) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (Estados.COMANDA_ENTREGADA.equals(c.getEstado())) {
            return out;
        }
        List<LineaPedido> lineasActivas = lineasActivasComanda(c.getId());
        if (lineasActivas.isEmpty()) {
            return out;
        }

        List<String> prodEnviada = nombresPorEstadoLinea(lineasActivas, Estados.LINEA_ENVIADA);
        List<String> prodPreparacion = nombresPorEstadoLinea(lineasActivas, Estados.LINEA_EN_PREPARACION);
        List<String> prodLista = nombresPorEstadoLinea(lineasActivas, Estados.LINEA_LISTA);

        if (Estados.COMANDA_ENVIADA.equals(c.getEstado()) && !prodEnviada.isEmpty()) {
            out.add(notificacionMozoRow(c, mesa, "RECIBIDO", prodEnviada,
                    "Pedido en cocina — pendiente de aceptar"));
        }
        if (!prodPreparacion.isEmpty()) {
            out.add(notificacionMozoRow(c, mesa, "PREPARANDO", prodPreparacion,
                    prodLista.isEmpty()
                            ? "Cocina preparando su pedido"
                            : "Cocina aun preparando otros platos"));
        }
        if (!prodLista.isEmpty()) {
            out.add(notificacionMozoRow(c, mesa, "LISTO", prodLista,
                    "Platos listos — llevar a la mesa"));
        }
        return out;
    }

    private List<LineaPedido> lineasActivasComanda(Long comandaId) {
        return lineaPedidoRepository.findByComandaId(comandaId).stream()
                .filter(l -> !Estados.LINEA_BORRADOR.equals(l.getEstado())
                        && !Estados.LINEA_ANULADA.equals(l.getEstado()))
                .toList();
    }

    private static List<String> nombresPorEstadoLinea(List<LineaPedido> lineas, String estadoLinea) {
        return lineas.stream()
                .filter(l -> estadoLinea.equals(l.getEstado()))
                .map(LineaPedido::getProductoNombreSnapshot)
                .toList();
    }

    private Map<String, Object> notificacionMozoRow(Comanda c, Mesa mesa, String tipo,
                                                    List<String> productos, String mensaje) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("tipo", tipo);
        row.put("comandaId", c.getId());
        row.put("mesaId", mesa.getId());
        row.put("mesa", mesa.getNumero());
        row.put("mesaCodigo", mesa.getCodigo());
        row.put("mensaje", mensaje);
        row.put("productos", productos);
        row.put("modoServicio", c.getModoServicio());
        return row;
    }

    @Transactional
    public Map<String, Object> marcarEntregada(Long comandaId, Long restauranteId) {
        Comanda comanda = comanda(comandaId, restauranteId);
        List<LineaPedido> listas = lineaPedidoRepository.findByComandaId(comanda.getId()).stream()
                .filter(l -> Estados.LINEA_LISTA.equals(l.getEstado()))
                .toList();
        if (listas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No hay platos listos para servir. Espere a que cocina los marque como listos.");
        }
        for (LineaPedido l : listas) {
            l.setEstado(Estados.LINEA_ENTREGADA);
            lineaPedidoRepository.save(l);
        }
        recalcularEstadoComandaDespuesServir(comanda);
        long pendientes = lineasActivasComanda(comanda.getId()).stream()
                .filter(l -> !Estados.LINEA_ENTREGADA.equals(l.getEstado()))
                .count();
        return Map.of(
                "comandaId", comanda.getId(),
                "comandaEstado", comanda.getEstado(),
                "platosServidos", listas.size(),
                "platosPendientes", pendientes);
    }

    /** Tras servir solo líneas LISTA, mantiene en cocina el resto (ENVIADA / EN_PREPARACION). */
    private void recalcularEstadoComandaDespuesServir(Comanda comanda) {
        List<LineaPedido> lineas = lineasActivasComanda(comanda.getId());
        if (lineas.isEmpty()) {
            comanda.setEstado(Estados.COMANDA_ENTREGADA);
            comandaRepository.save(comanda);
            return;
        }
        boolean hayLista = lineas.stream().anyMatch(l -> Estados.LINEA_LISTA.equals(l.getEstado()));
        boolean hayPreparacion = lineas.stream().anyMatch(l -> Estados.LINEA_EN_PREPARACION.equals(l.getEstado()));
        boolean hayEnviada = lineas.stream().anyMatch(l -> Estados.LINEA_ENVIADA.equals(l.getEstado()));
        boolean todasEntregadas = lineas.stream().allMatch(l -> Estados.LINEA_ENTREGADA.equals(l.getEstado()));

        if (todasEntregadas) {
            comanda.setEstado(Estados.COMANDA_ENTREGADA);
        } else if (hayLista) {
            comanda.setEstado(Estados.COMANDA_PARCIAL);
        } else if (hayPreparacion) {
            comanda.setEstado(Estados.COMANDA_EN_PREPARACION);
        } else if (hayEnviada) {
            comanda.setEstado(Estados.COMANDA_ENVIADA);
        }
        comandaRepository.save(comanda);
    }

    public Map<String, Object> infoLocalPublico() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("qrToken", QR_LOCAL);
        res.put("restaurante", "Restaurante Piloto Lima");
        List<Map<String, Object>> mesas = new ArrayList<>();
        for (Mesa m : mesaRepository.findByRestauranteIdOrderByNumeroAsc(RESTAURANTE_PUBLICO_ID)) {
            Map<String, Object> mesaMap = new LinkedHashMap<>();
            mesaMap.put("id", m.getId());
            mesaMap.put("numero", m.getNumero());
            mesaMap.put("codigo", m.getCodigo());
            mesaMap.put("estado", m.getEstado());
            mesaMap.put("sesionActiva",
                    sesionMesaRepository.findByMesaIdAndActivaTrue(m.getId()).isPresent());
            mesaMap.put("sillas", sillasDeMesa(m.getId()));
            mesas.add(mesaMap);
        }
        res.put("mesas", mesas);
        return res;
    }

    public Map<String, Object> colaLocalPublica() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("restaurante", "Restaurante Piloto Lima");
        List<Map<String, Object>> mesasCola = new ArrayList<>();
        List<Map<String, Object>> colaGlobal = new ArrayList<>();

        for (Mesa mesa : mesaRepository.findByRestauranteIdOrderByNumeroAsc(RESTAURANTE_PUBLICO_ID)) {
            SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId()).orElse(null);
            if (sesion == null) {
                continue;
            }
            List<Map<String, Object>> lineasMesa = lineasPublicasMesa(mesa, sesion);
            if (lineasMesa.isEmpty()) {
                continue;
            }
            Map<String, Object> bloque = new LinkedHashMap<>();
            bloque.put("mesaNumero", mesa.getNumero());
            bloque.put("mesaCodigo", mesa.getCodigo());
            bloque.put("lineas", lineasMesa);
            mesasCola.add(bloque);
            colaGlobal.addAll(lineasMesa);
        }

        colaGlobal.sort(Comparator
                .comparingInt((Map<String, Object> row) -> prioridadCola((String) row.get("estado")))
                .thenComparing(row -> (String) row.get("mesaCodigo"))
                .thenComparing(row -> (String) row.get("codigoSilla")));
        int posCola = 1;
        for (Map<String, Object> row : colaGlobal) {
            String est = (String) row.get("estado");
            boolean enCola = Estados.LINEA_ENVIADA.equals(est) || Estados.LINEA_EN_PREPARACION.equals(est);
            row.put("enCola", enCola);
            if (enCola) {
                row.put("posicionCola", posCola++);
            }
        }
        res.put("mesas", mesasCola);
        res.put("cola", colaGlobal);
        return res;
    }

    private List<Map<String, Object>> lineasPublicasMesa(Mesa mesa, SesionMesa sesion) {
        List<Map<String, Object>> lineas = new ArrayList<>();
        for (Comanda comanda : comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId())) {
            if (Estados.COMANDA_ENTREGADA.equals(comanda.getEstado())) {
                continue;
            }
            for (LineaPedido l : lineaPedidoRepository.findByComandaId(comanda.getId())) {
                if (Estados.LINEA_ANULADA.equals(l.getEstado())) {
                    continue;
                }
                String codigoSilla = sillaRepository.findByMesaIdAndNumeroAndActivaTrue(mesa.getId(), l.getNumeroSilla())
                        .map(Silla::getCodigo)
                        .orElse(CodigoMesaSilla.codigoSilla(mesa.getCodigo(), l.getNumeroSilla()));
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("mesaNumero", mesa.getNumero());
                row.put("mesaCodigo", mesa.getCodigo());
                row.put("codigoSilla", codigoSilla);
                row.put("etiquetaComensal", codigoSilla);
                row.put("producto", l.getProductoNombreSnapshot());
                row.put("estado", l.getEstado());
                row.put("estadoLabel", etiquetaEstadoLinea(l.getEstado()));
                row.put("cantidad", l.getCantidad());
                lineas.add(row);
            }
        }
        lineas.sort(Comparator
                .comparingInt((Map<String, Object> row) -> prioridadCola((String) row.get("estado")))
                .thenComparing(row -> (String) row.get("codigoSilla")));
        return lineas;
    }

    public Map<String, Object> infoMesaPublica(String qrToken) {
        Mesa mesa = mesaPublica(qrToken, null);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("qrToken", mesa.getQrToken());
        res.put("mesa", mesa.getNumero());
        res.put("mesaCodigo", mesa.getCodigo());
        res.put("restaurante", "Restaurante Piloto Lima");
        res.put("sillas", sillasDeMesa(mesa.getId()));
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId()).orElse(null);
        res.put("sesionActiva", sesion != null);
        return res;
    }

    @Transactional
    public Map<String, Object> identificarComensal(String qrToken, IdentificarComensalRequest request) {
        if (request.codigoSilla() == null || request.codigoSilla().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique su silla");
        }
        Mesa mesa = mesaPublica(qrToken, request.mesaCodigo());
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId()).orElse(null);
        if (sesion == null) {
            if (Boolean.TRUE.equals(request.abrirMesaSiLibre()) && Estados.MESA_LIBRE.equals(mesa.getEstado())) {
                abrirMesa(mesa.getId(), mesa.getRestauranteId());
                sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "No se pudo abrir la mesa"));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La mesa aun no tiene pedido activo. Espere a que el mozo abra la mesa.");
            }
        }
        String codigoSilla = request.codigoSilla().trim().toUpperCase();
        sillaRepository.findByCodigoAndActivaTrue(codigoSilla)
                .filter(s -> s.getMesaId().equals(mesa.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Silla no valida para esta mesa: " + codigoSilla));

        ComensalSilla comensal = comensalSillaRepository
                .findBySesionMesaIdAndCodigoSilla(sesion.getId(), codigoSilla)
                .orElseGet(ComensalSilla::new);
        comensal.setSesionMesaId(sesion.getId());
        comensal.setCodigoSilla(codigoSilla);
        comensal.setNombre(codigoSilla);
        comensal.setApellido(mesa.getCodigo());
        comensalSillaRepository.save(comensal);

        return Map.of(
                "codigoSilla", codigoSilla,
                "mesaCodigo", mesa.getCodigo(),
                "mesaNumero", mesa.getNumero());
    }

    public Map<String, Object> consultaPublicaQr(String qrToken, String mesaCodigo, String miCodigoSilla) {
        if (miCodigoSilla == null || miCodigoSilla.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indique su silla (parametro miSilla) tras identificarse");
        }
        Mesa mesa = mesaPublica(qrToken, mesaCodigo);
        String miSilla = miCodigoSilla.trim().toUpperCase();
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId()).orElse(null);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("mesa", mesa.getNumero());
        res.put("mesaCodigo", mesa.getCodigo());
        res.put("restaurante", "Restaurante Piloto Lima");
        res.put("miSilla", miSilla);

        if (sesion == null) {
            res.put("lineas", List.of());
            res.put("comandaEstado", "SIN_SESION");
            return res;
        }

        List<Map<String, Object>> lineas = new ArrayList<>();
        String comandaEstado = "LIBRE";
        for (Comanda comanda : comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId())) {
            if (Estados.COMANDA_ENTREGADA.equals(comanda.getEstado())) {
                continue;
            }
            comandaEstado = comanda.getEstado();
            for (LineaPedido l : lineaPedidoRepository.findByComandaId(comanda.getId())) {
                if (Estados.LINEA_ANULADA.equals(l.getEstado())) {
                    continue;
                }
                String codigoSilla = sillaRepository.findByMesaIdAndNumeroAndActivaTrue(mesa.getId(), l.getNumeroSilla())
                        .map(Silla::getCodigo)
                        .orElse(CodigoMesaSilla.codigoSilla(mesa.getCodigo(), l.getNumeroSilla()));
                boolean esTuyo = miSilla.equals(codigoSilla);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("esTuyo", esTuyo);
                row.put("etiquetaComensal", esTuyo ? "Tu" : codigoSilla);
                row.put("producto", l.getProductoNombreSnapshot());
                row.put("estado", l.getEstado());
                row.put("estadoLabel", etiquetaEstadoLinea(l.getEstado()));
                row.put("cantidad", l.getCantidad());
                row.put("codigoSilla", codigoSilla);
                lineas.add(row);
            }
        }
        lineas.sort(Comparator
                .comparingInt((Map<String, Object> row) -> prioridadCola((String) row.get("estado")))
                .thenComparing(row -> (String) row.get("codigoSilla")));
        int posCola = 1;
        for (Map<String, Object> row : lineas) {
            String est = (String) row.get("estado");
            boolean enCola = Estados.LINEA_ENVIADA.equals(est) || Estados.LINEA_EN_PREPARACION.equals(est);
            row.put("enCola", enCola);
            if (enCola) {
                row.put("posicionCola", posCola++);
            }
        }
        res.put("lineas", lineas);
        res.put("comandaEstado", comandaEstado);
        return res;
    }

    public Map<String, Object> menuPublico(String qrToken, String mesaCodigo) {
        Mesa mesa = mesaPublica(qrToken, mesaCodigo);
        List<Map<String, Object>> productos = productoClient.listar(mesa.getRestauranteId()).stream()
                .map(p -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", p.id());
                    row.put("nombre", p.nombre());
                    row.put("precio", p.precio());
                    row.put("estacion", p.estacion());
                    return row;
                })
                .toList();
        return Map.of(
                "mesaCodigo", mesa.getCodigo(),
                "productos", productos);
    }

    @Transactional
    public Map<String, Object> agregarLineaPublica(String qrToken, String mesaCodigo, String miCodigoSilla,
                                                   AgregarLineaRequest req) {
        if (miCodigoSilla == null || miCodigoSilla.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique su silla (miSilla)");
        }
        Mesa mesa = mesaPublica(qrToken, mesaCodigo);
        String codigoSilla = miCodigoSilla.trim().toUpperCase();
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Abra la mesa primero"));
        comensalSillaRepository.findBySesionMesaIdAndCodigoSilla(sesion.getId(), codigoSilla)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Identifiquese primero en esta mesa"));
        Silla sillaEnt = sillaRepository.findByCodigoAndActivaTrue(codigoSilla)
                .filter(s -> s.getMesaId().equals(mesa.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Silla no valida"));
        return agregarLinea(mesa.getId(), sillaEnt.getNumero(), req, mesa.getRestauranteId());
    }

    @Transactional
    public Map<String, Object> enviarPedidoPublico(String qrToken, String mesaCodigo, String miCodigoSilla) {
        if (miCodigoSilla == null || miCodigoSilla.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique su silla (miSilla)");
        }
        Mesa mesa = mesaPublica(qrToken, mesaCodigo);
        String codigoSilla = miCodigoSilla.trim().toUpperCase();
        SesionMesa sesion = sesionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay sesion activa"));
        comensalSillaRepository.findBySesionMesaIdAndCodigoSilla(sesion.getId(), codigoSilla)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Identifiquese primero en esta mesa"));
        Comanda comanda = comandaRepository.findBySesionMesaIdAndEstado(sesion.getId(), Estados.COMANDA_BORRADOR)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No hay platos pendientes de enviar"));
        long lineasPropias = lineaPedidoRepository.findByComandaId(comanda.getId()).stream()
                .filter(l -> Estados.LINEA_BORRADOR.equals(l.getEstado()))
                .filter(l -> {
                    String cs = sillaRepository.findByMesaIdAndNumeroAndActivaTrue(mesa.getId(), l.getNumeroSilla())
                            .map(Silla::getCodigo)
                            .orElse(CodigoMesaSilla.codigoSilla(mesa.getCodigo(), l.getNumeroSilla()));
                    return codigoSilla.equals(cs);
                })
                .count();
        if (lineasPropias == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Agregue al menos un plato antes de enviar");
        }
        return enviarComanda(comanda.getId(), mesa.getRestauranteId());
    }

    private boolean esQrLocal(String qrToken) {
        return qrToken != null
                && (QR_LOCAL.equalsIgnoreCase(qrToken) || "local".equalsIgnoreCase(qrToken));
    }

    private Mesa mesaPublica(String qrToken, String mesaCodigo) {
        if (esQrLocal(qrToken)) {
            if (mesaCodigo == null || mesaCodigo.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indique codigo de mesa (mesaCodigo)");
            }
            return mesaRepository.findByRestauranteIdAndCodigo(RESTAURANTE_PUBLICO_ID, mesaCodigo.trim().toUpperCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Mesa no encontrada: " + mesaCodigo));
        }
        return mesaPorQr(qrToken);
    }

    private int prioridadCola(String estado) {
        if (estado == null) {
            return 99;
        }
        return switch (estado) {
            case Estados.LINEA_EN_PREPARACION -> 1;
            case Estados.LINEA_ENVIADA -> 2;
            case Estados.LINEA_LISTA -> 3;
            case Estados.LINEA_BORRADOR -> 4;
            default -> 5;
        };
    }

    private Mesa mesaPorQr(String qrToken) {
        return mesaRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QR invalido"));
    }

    private String etiquetaEstadoLinea(String estado) {
        return switch (estado) {
            case Estados.LINEA_BORRADOR -> "En carrito";
            case Estados.LINEA_ENVIADA -> "Enviado a cocina";
            case Estados.LINEA_EN_PREPARACION -> "Preparando";
            case Estados.LINEA_LISTA -> "Listo";
            case Estados.LINEA_ENTREGADA -> "Servido";
            default -> estado;
        };
    }

    private void recalcularEstadoComanda(Comanda comanda) {
        List<LineaPedido> lineas = lineaPedidoRepository.findByComandaId(comanda.getId()).stream()
                .filter(l -> !Estados.LINEA_BORRADOR.equals(l.getEstado()) && !Estados.LINEA_ANULADA.equals(l.getEstado()))
                .toList();
        boolean todasLista = lineas.stream().allMatch(l -> Estados.LINEA_LISTA.equals(l.getEstado()) || Estados.LINEA_ENTREGADA.equals(l.getEstado()));
        boolean algunaLista = lineas.stream().anyMatch(l -> Estados.LINEA_LISTA.equals(l.getEstado()));

        if ("B".equals(comanda.getModoServicio()) && algunaLista) {
            comanda.setEstado(Estados.COMANDA_PARCIAL);
            comanda.setListaParaServirEn(Instant.now());
        }
        if (todasLista) {
            comanda.setEstado(Estados.COMANDA_LISTA);
            comanda.setListaParaServirEn(Instant.now());
        } else if (algunaLista) {
            comanda.setEstado(Estados.COMANDA_PARCIAL);
            if (comanda.getListaParaServirEn() == null) {
                comanda.setListaParaServirEn(Instant.now());
            }
        }
        comandaRepository.save(comanda);
    }

    private Comanda nuevaComandaBorrador(SesionMesa sesion) {
        int turno = (int) comandaRepository.findBySesionMesaIdOrderByNumeroTurnoAsc(sesion.getId()).size() + 1;
        Comanda c = new Comanda();
        c.setSesionMesaId(sesion.getId());
        c.setNumeroTurno(turno);
        c.setEstado(Estados.COMANDA_BORRADOR);
        c.setModoServicio(modoServicioDefault);
        return comandaRepository.save(c);
    }

    private Mesa mesa(Long mesaId, Long restauranteId) {
        return mesaRepository.findByIdAndRestauranteId(mesaId, restauranteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesa no encontrada"));
    }

    private Comanda comanda(Long comandaId, Long restauranteId) {
        Comanda c = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comanda no encontrada"));
        SesionMesa s = sesionMesaRepository.findById(c.getSesionMesaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesion no encontrada"));
        if (!s.getRestauranteId().equals(restauranteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Comanda de otro restaurante");
        }
        return c;
    }

    private void requireAdmin(String rol) {
        if (!"ADMIN".equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede administrar mesas y sillas");
        }
    }

    private Map<String, Object> mesaToMap(Mesa m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("numero", m.getNumero());
        map.put("codigo", m.getCodigo());
        map.put("capacidadSillas", m.getCapacidadSillas());
        map.put("estado", m.getEstado());
        map.put("qrToken", m.getQrToken());
        map.put("sillas", sillasDeMesa(m.getId()));
        return map;
    }

    private List<Map<String, Object>> sillasDeMesa(Long mesaId) {
        return sillaRepository.findByMesaIdAndActivaTrueOrderByNumeroAsc(mesaId).stream()
                .map(s -> Map.<String, Object>of(
                        "id", s.getId(),
                        "numero", s.getNumero(),
                        "codigo", s.getCodigo()))
                .toList();
    }

    private List<Map<String, Object>> lineasResumen(Long comandaId, Long mesaId) {
        return lineaPedidoRepository.findByComandaId(comandaId).stream()
                .filter(l -> !Estados.LINEA_ANULADA.equals(l.getEstado()))
                .map(l -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", l.getId());
                    row.put("producto", l.getProductoNombreSnapshot());
                    row.put("cantidad", l.getCantidad());
                    row.put("estado", l.getEstado());
                    row.put("silla", l.getNumeroSilla());
                    row.put("notas", l.getNotas() != null ? l.getNotas() : "");
                    sillaRepository.findByMesaIdAndNumeroAndActivaTrue(mesaId, l.getNumeroSilla())
                            .ifPresent(s -> row.put("codigoSilla", s.getCodigo()));
                    return row;
                })
                .toList();
    }

    private Map<String, Object> lineaDetalle(LineaPedido l) {
        Comanda c = comandaRepository.findById(l.getComandaId()).orElseThrow();
        SesionMesa s = sesionMesaRepository.findById(c.getSesionMesaId()).orElseThrow();
        Mesa m = mesaRepository.findById(s.getMesaId()).orElseThrow();
        return Map.of(
                "lineaId", l.getId(),
                "comandaId", l.getComandaId(),
                "mesa", m.getNumero(),
                "mesaCodigo", m.getCodigo(),
                "producto", l.getProductoNombreSnapshot(),
                "cantidad", l.getCantidad(),
                "estado", l.getEstado(),
                "estacion", l.getEstacionProducto(),
                "notas", l.getNotas() != null ? l.getNotas() : "");
    }
}

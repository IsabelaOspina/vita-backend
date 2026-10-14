package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.OrdenSintoma;
import org.example.vitabackend.dto.request.SintomaActualizarRequest;
import org.example.vitabackend.dto.request.SintomaFiltro;
import org.example.vitabackend.dto.request.SintomaRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.SintomaEvolucionResponse;
import org.example.vitabackend.dto.response.SintomaResponse;
import org.example.vitabackend.dto.response.SintomaResumenResponse;
import org.example.vitabackend.dto.response.TipoSintomaResponse;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.SintomaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Seguimiento de síntomas del paciente.
 * Cada paciente solo ve y modifica SUS síntomas: el id sale del token, nunca de la URL.
 */
@RestController
@RequestMapping("/api/sintomas")
@RequiredArgsConstructor
public class SintomaController {

    private final SintomaService sintomaService;
    private final UsuarioActual usuarioActual;

    @PostMapping
    public ResponseEntity<SintomaResponse> registrar(@Valid @RequestBody SintomaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sintomaService.registrar(usuarioActual.id(), request));
    }

    /** Historial con filtros: ?desde=2026-10-01&hasta=2026-10-14&tipo=cabeza&intensidadMin=5&orden=RECIENTES&pagina=0&tamano=20 */
    @GetMapping
    public PaginaResponse<SintomaResponse> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer intensidadMin,
            @RequestParam(required = false) Integer intensidadMax,
            @RequestParam(defaultValue = "RECIENTES") OrdenSintoma orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        SintomaFiltro filtro = new SintomaFiltro(desde, hasta, tipo, intensidadMin, intensidadMax);
        return sintomaService.listar(usuarioActual.id(), filtro, orden, pagina, tamano);
    }

    @GetMapping("/{id}")
    public SintomaResponse obtener(@PathVariable Long id) {
        return sintomaService.obtener(usuarioActual.id(), id);
    }

    @PutMapping("/{id}")
    public SintomaResponse actualizar(@PathVariable Long id, @Valid @RequestBody SintomaActualizarRequest request) {
        return sintomaService.actualizar(usuarioActual.id(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sintomaService.eliminar(usuarioActual.id(), id);
        return ResponseEntity.noContent().build();
    }

    /** Indicadores del periodo (por defecto, últimos 30 días). */
    @GetMapping("/resumen")
    public SintomaResumenResponse resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return sintomaService.resumen(usuarioActual.id(), desde, hasta);
    }

    /** Evolución de los síntomas */
    @GetMapping("/evolucion")
    public SintomaEvolucionResponse evolucion(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") AgrupacionTiempo agrupacion) {
        return sintomaService.evolucion(usuarioActual.id(), tipo, desde, hasta, agrupacion);
    }

    /** Tipos que ya registró (para sugerirlos al escribir o dictar). */
    @GetMapping("/tipos")
    public List<TipoSintomaResponse> tipos() {
        return sintomaService.tiposRegistrados(usuarioActual.id());
    }
}

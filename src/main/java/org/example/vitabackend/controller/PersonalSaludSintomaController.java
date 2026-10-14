package org.example.vitabackend.controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.OrdenSintoma;
import org.example.vitabackend.dto.request.SintomaFiltro;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.SintomaEvolucionResponse;
import org.example.vitabackend.dto.response.SintomaResponse;
import org.example.vitabackend.dto.response.SintomaResumenResponse;
import org.example.vitabackend.security.AccesoPaciente;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.SintomaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/**
 * Consulta (solo lectura) de los síntomas de un paciente por parte de su profesional de salud.
 * SecurityConfig exige rol PERSONAL_SALUD en /api/personal-salud/**, y cada método comprueba
 * además que el paciente esté asignado a ese profesional (si no, 403).
 */
@RestController
@RequestMapping("/api/personal-salud/pacientes/{pacienteId}/sintomas")
@RequiredArgsConstructor
public class PersonalSaludSintomaController {

    private final SintomaService sintomaService;
    private final UsuarioActual usuarioActual;
    private final AccesoPaciente accesoPaciente;

    @GetMapping
    public PaginaResponse<SintomaResponse> listar(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer intensidadMin,
            @RequestParam(required = false) Integer intensidadMax,
            @RequestParam(defaultValue = "RECIENTES") OrdenSintoma orden,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        accesoPaciente.verificarAsignado(usuarioActual.id(), pacienteId);
        SintomaFiltro filtro = new SintomaFiltro(desde, hasta, tipo, intensidadMin, intensidadMax);
        return sintomaService.listar(pacienteId, filtro, orden, pagina, tamano);
    }

    @GetMapping("/{sintomaId}")
    public SintomaResponse obtener(@PathVariable Long pacienteId, @PathVariable Long sintomaId) {
        accesoPaciente.verificarAsignado(usuarioActual.id(), pacienteId);
        return sintomaService.obtener(pacienteId, sintomaId);
    }

    @GetMapping("/resumen")
    public SintomaResumenResponse resumen(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        accesoPaciente.verificarAsignado(usuarioActual.id(), pacienteId);
        return sintomaService.resumen(pacienteId, desde, hasta);
    }

    @GetMapping("/evolucion")
    public SintomaEvolucionResponse evolucion(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "DIA") AgrupacionTiempo agrupacion) {
        accesoPaciente.verificarAsignado(usuarioActual.id(), pacienteId);
        return sintomaService.evolucion(pacienteId, tipo, desde, hasta, agrupacion);
    }
}

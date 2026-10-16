package org.example.vitabackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.response.CantidadResponse;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.RecomendacionResponse;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.RecomendacionService;
import org.springframework.web.bind.annotation.*;

/**
 * El paciente ve las recomendaciones que le dejaron sus profesionales
 * (SecurityConfig exige rol USUARIO). Cada paciente solo ve las suyas.
 */
@RestController
@RequestMapping("/api/recomendaciones")
@RequiredArgsConstructor
public class RecomendacionController {

    private final RecomendacionService recomendacionService;
    private final UsuarioActual usuarioActual;

    /** ?soloNoLeidas=true para ver únicamente las pendientes. */
    @GetMapping
    public PaginaResponse<RecomendacionResponse> listar(@RequestParam(defaultValue = "false") boolean soloNoLeidas,
                                                        @RequestParam(defaultValue = "0") int pagina,
                                                        @RequestParam(defaultValue = "20") int tamano) {
        return recomendacionService.listarDelPaciente(usuarioActual.id(), soloNoLeidas, pagina, tamano);
    }

    /** Cuántas recomendaciones nuevas tiene (para mostrar un aviso en la app). */
    @GetMapping("/no-leidas/cantidad")
    public CantidadResponse cantidadNoLeidas() {
        return new CantidadResponse(recomendacionService.contarNoLeidas(usuarioActual.id()));
    }

    @PatchMapping("/{id}/leida")
    public RecomendacionResponse marcarLeida(@PathVariable Long id) {
        return recomendacionService.marcarLeida(usuarioActual.id(), id);
    }
}

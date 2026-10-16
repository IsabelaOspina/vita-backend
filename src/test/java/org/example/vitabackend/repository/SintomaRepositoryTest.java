package org.example.vitabackend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.model.Sintoma;
import org.example.vitabackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Prueba las consultas JPQL de los síntomas contra una base H2 en memoria
 */
@DataJpaTest
class SintomaRepositoryTest {

    private static final LocalDateTime MUY_ANTIGUO = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MUY_FUTURO = LocalDateTime.of(3000, 1, 1, 0, 0);

    @Autowired
    private SintomaRepository sintomaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void buscarDevuelveSoloLosSintomasDelPacienteOrdenadosPorFecha() {
        Usuario ana = guardarUsuario("200000001", "ana.sintomas@vita.test");
        Usuario juan = guardarUsuario("200000002", "juan.sintomas@vita.test");
        guardarSintoma(ana, "mareo", 4, LocalDateTime.of(2026, 10, 1, 8, 0));
        guardarSintoma(ana, "fiebre", 6, LocalDateTime.of(2026, 10, 3, 8, 0));
        guardarSintoma(juan, "tos", 2, LocalDateTime.of(2026, 10, 2, 8, 0));

        Page<Sintoma> pagina = sintomaRepository.buscar(ana.getId(), MUY_ANTIGUO, MUY_FUTURO, "%", 1, 10,
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("fechaHora"))));

        assertEquals(2, pagina.getTotalElements());
        assertEquals("fiebre", pagina.getContent().get(0).getTipo());
        assertEquals("mareo", pagina.getContent().get(1).getTipo());
        assertTrue(pagina.getContent().stream().allMatch(s -> s.getUsuario().getId().equals(ana.getId())));
    }

    @Test
    void buscarIncluyeElInicioYExcluyeElFinDelRango() {
        Usuario ana = guardarUsuario("200000003", "ana2.sintomas@vita.test");
        guardarSintoma(ana, "antes", 3, LocalDateTime.of(2026, 9, 30, 23, 59));
        guardarSintoma(ana, "al inicio", 3, LocalDateTime.of(2026, 10, 1, 0, 0));
        guardarSintoma(ana, "ultimo minuto del 14", 3, LocalDateTime.of(2026, 10, 14, 23, 59));
        guardarSintoma(ana, "dia 15", 3, LocalDateTime.of(2026, 10, 15, 0, 0));

        // "hasta el 14" se consulta como < 15 a medianoche
        Page<Sintoma> pagina = sintomaRepository.buscar(ana.getId(),
                LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 15, 0, 0), "%", 1, 10,
                PageRequest.of(0, 10, Sort.by("fechaHora")));

        assertEquals(List.of("al inicio", "ultimo minuto del 14"),
                pagina.getContent().stream().map(Sintoma::getTipo).toList());
    }

    @Test
    void buscarFiltraPorTipoSinDistinguirMayusculasYPorIntensidad() {
        Usuario ana = guardarUsuario("200000004", "ana3.sintomas@vita.test");
        guardarSintoma(ana, "Dolor de cabeza", 3, LocalDateTime.of(2026, 10, 1, 8, 0));
        guardarSintoma(ana, "dolor de cabeza", 8, LocalDateTime.of(2026, 10, 2, 8, 0));
        guardarSintoma(ana, "dolor de espalda", 9, LocalDateTime.of(2026, 10, 3, 8, 0));

        Page<Sintoma> porTipo = sintomaRepository.buscar(ana.getId(), MUY_ANTIGUO, MUY_FUTURO, "%cabeza%", 1, 10,
                PageRequest.of(0, 10));
        Page<Sintoma> porIntensidad = sintomaRepository.buscar(ana.getId(), MUY_ANTIGUO, MUY_FUTURO, "%", 8, 10,
                PageRequest.of(0, 10, Sort.by(Sort.Order.desc("intensidad"))));

        assertEquals(2, porTipo.getTotalElements());
        assertEquals(2, porIntensidad.getTotalElements());
        assertEquals("dolor de espalda", porIntensidad.getContent().get(0).getTipo());
    }

    @Test
    void buscarPagina() {
        Usuario ana = guardarUsuario("200000005", "ana4.sintomas@vita.test");
        for (int i = 1; i <= 5; i++) {
            guardarSintoma(ana, "sintoma " + i, 3, LocalDateTime.of(2026, 10, i, 8, 0));
        }

        Page<Sintoma> segunda = sintomaRepository.buscar(ana.getId(), MUY_ANTIGUO, MUY_FUTURO, "%", 1, 10,
                PageRequest.of(1, 2, Sort.by("fechaHora")));

        assertEquals(5, segunda.getTotalElements());
        assertEquals(3, segunda.getTotalPages());
        assertEquals("sintoma 3", segunda.getContent().get(0).getTipo());
        assertFalse(segunda.isLast());
    }

    @Test
    void buscarEnRangoOrdenaCronologicamenteYFiltraPorPatron() {
        Usuario ana = guardarUsuario("200000006", "ana5.sintomas@vita.test");
        guardarSintoma(ana, "mareo", 4, LocalDateTime.of(2026, 10, 3, 8, 0));
        guardarSintoma(ana, "Dolor de cabeza", 5, LocalDateTime.of(2026, 10, 1, 8, 0));
        guardarSintoma(ana, "dolor de cabeza", 6, LocalDateTime.of(2026, 10, 2, 8, 0));

        List<Sintoma> todos = sintomaRepository.buscarEnRango(ana.getId(), MUY_ANTIGUO, MUY_FUTURO, "%");
        List<Sintoma> cabeza = sintomaRepository.buscarEnRango(ana.getId(), MUY_ANTIGUO, MUY_FUTURO, "%dolor de cabeza%");

        assertEquals(3, todos.size());
        assertEquals("Dolor de cabeza", todos.get(0).getTipo()); // el más antiguo primero
        assertEquals(2, cabeza.size());
    }

    @Test
    void buscarPorIdYPropietarioNoDevuelveSintomasAjenos() {
        Usuario ana = guardarUsuario("200000007", "ana6.sintomas@vita.test");
        Usuario juan = guardarUsuario("200000008", "juan6.sintomas@vita.test");
        Sintoma sintoma = guardarSintoma(ana, "mareo", 4, LocalDateTime.of(2026, 10, 1, 8, 0));

        assertTrue(sintomaRepository.findByIdAndUsuario_Id(sintoma.getId(), ana.getId()).isPresent());
        assertFalse(sintomaRepository.findByIdAndUsuario_Id(sintoma.getId(), juan.getId()).isPresent());
    }

    @Test
    void listarPorUsuarioDevuelveLosMasRecientesPrimero() {
        Usuario ana = guardarUsuario("200000009", "ana7.sintomas@vita.test");
        guardarSintoma(ana, "viejo", 3, LocalDateTime.of(2026, 10, 1, 8, 0));
        guardarSintoma(ana, "nuevo", 3, LocalDateTime.of(2026, 10, 5, 8, 0));

        List<Sintoma> sintomas = sintomaRepository.findByUsuario_IdOrderByFechaHoraDesc(ana.getId());

        assertEquals("nuevo", sintomas.get(0).getTipo());
        assertEquals("viejo", sintomas.get(1).getTipo());
    }

    private Usuario guardarUsuario(String cedula, String correo) {
        return usuarioRepository.save(Usuario.builder()
                .cedula(cedula)
                .nombre("Usuario de prueba")
                .correo(correo)
                .contrasena("hash-no-utilizable-en-pruebas")
                .activo(true)
                .rol(Rol.USUARIO)
                .build());
    }

    private Sintoma guardarSintoma(Usuario usuario, String tipo, int intensidad, LocalDateTime fechaHora) {
        return sintomaRepository.save(Sintoma.builder()
                .tipo(tipo)
                .intensidad(intensidad)
                .fechaHora(fechaHora)
                .fechaRegistro(fechaHora)
                .usuario(usuario)
                .build());
    }
}

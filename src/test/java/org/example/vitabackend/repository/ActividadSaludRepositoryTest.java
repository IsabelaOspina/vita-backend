package org.example.vitabackend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.enums.TipoActividad;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.domain.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ActividadSaludRepositoryTest {

    @Autowired
    private ActividadSaludRepository actividadSaludRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test 
    void listarPorUsuarioOrdenaPorFechaYNoIncluyeActividadesDeOtroUsuario() {
        Usuario ana = guardarUsuario("100000001", "ana@vita.test");
        Usuario juan = guardarUsuario("100000002", "juan@vita.test");

        guardarActividad(ana, "Actividad tarde", LocalDateTime.of(2026, 10, 22, 16, 0));
        guardarActividad(ana, "Actividad mañana", LocalDateTime.of(2026, 10, 22, 9, 0));
        guardarActividad(juan, "Actividad de otro usuario", LocalDateTime.of(2026, 10, 22, 8, 0));

        List<ActividadSalud> actividades = actividadSaludRepository
                .findByUsuario_IdOrderByFechaHoraAscIdAsc(ana.getId());

        assertEquals(2, actividades.size());
        assertEquals("Actividad mañana", actividades.get(0).getNombre());
        assertEquals("Actividad tarde", actividades.get(1).getNombre());
        assertTrue(actividades.stream().allMatch(a -> a.getUsuario().getId().equals(ana.getId())));
    }

    @Test
    void buscarPorIdYPropietarioNoDevuelveActividadAjena() {
        Usuario ana = guardarUsuario("100000003", "ana2@vita.test");
        Usuario juan = guardarUsuario("100000004", "juan2@vita.test");
        ActividadSalud actividad = guardarActividad(
                ana, "Cita", LocalDateTime.of(2026, 10, 23, 10, 0));

        assertTrue(actividadSaludRepository.findByIdAndUsuario_Id(actividad.getId(), ana.getId()).isPresent());
        assertFalse(actividadSaludRepository.findByIdAndUsuario_Id(actividad.getId(), juan.getId()).isPresent());
    }

    @Test
    void consultaPorRangoIncluyeLimitesYOrdenaCronologicamente() {
        Usuario ana = guardarUsuario("100000005", "ana3@vita.test");
        LocalDateTime desde = LocalDateTime.of(2026, 10, 24, 9, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 10, 24, 12, 0);
        guardarActividad(ana, "Al límite final", hasta);
        guardarActividad(ana, "Al límite inicial", desde);
        guardarActividad(ana, "Fuera del rango", hasta.plusMinutes(1));

        List<ActividadSalud> resultado = actividadSaludRepository
                .findByUsuario_IdAndFechaHoraBetweenOrderByFechaHoraAscIdAsc(ana.getId(), desde, hasta);

        assertEquals(2, resultado.size());
        assertEquals("Al límite inicial", resultado.get(0).getNombre());
        assertEquals("Al límite final", resultado.get(1).getNombre());
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

    private ActividadSalud guardarActividad(Usuario usuario, String nombre, LocalDateTime fechaHora) {
        return actividadSaludRepository.save(ActividadSalud.builder()
                .nombre(nombre)
                .tipo(TipoActividad.CITA_MEDICA)
                .fechaHora(fechaHora)
                .estado(EstadoActividad.PENDIENTE)
                .usuario(usuario)
                .build());
    }
}

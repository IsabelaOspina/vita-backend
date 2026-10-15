package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.enums.TipoActividad;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.request.HorarioMedicamentoRequest;
import org.example.vitabackend.dto.request.RecordatorioRequest;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.ActividadSaludRepository;
import org.example.vitabackend.repository.MedicamentoRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.ActividadSaludService;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.example.vitabackend.service.RecordatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de integración de las reglas de seguridad del calendario.
 * Validan aislamiento entre usuarios usando servicios, repositorios y H2.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CalendarioPropiedadYSeguridadIntegrationTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ActividadSaludRepository actividadRepository;
    @Autowired private RecordatorioRepository recordatorioRepository;
    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private ActividadSaludService actividadService;
    @Autowired private RecordatorioService recordatorioService;
    @Autowired private HorarioMedicamentoService horarioService;

    @Test
    void usuarioNoPuedeConsultarActualizarCancelarNiProgramarAvisosDeActividadAjena() {
        Usuario propietario = crearUsuario();
        Usuario tercero = crearUsuario();
        LocalDateTime ahora = LocalDateTime.now().plusDays(6).withNano(0);

        var actividad = actividadService.crear(propietario.getCorreo(), new ActividadSaludRequest(
                "Cita protegida", TipoActividad.CITA_MEDICA, ahora, "Hospital", "Control"));

        assertThrows(ResourceNotFoundException.class,
                () -> actividadService.obtener(tercero.getCorreo(), actividad.id()));
        org.junit.jupiter.api.Assertions.assertTrue(actividadService.listar(tercero.getCorreo()).isEmpty());
        assertThrows(ResourceNotFoundException.class,
                () -> actividadService.actualizar(tercero.getCorreo(), actividad.id(),
                        new ActividadSaludRequest("Cambio ajeno", TipoActividad.CITA_MEDICA,
                                ahora.plusHours(1), "Hospital", "No autorizado")));
        assertThrows(ResourceNotFoundException.class,
                () -> actividadService.cancelar(tercero.getCorreo(), actividad.id()));

        RecordatorioRequest recordatorio = new RecordatorioRequest(
                LocalDateTime.now().plusDays(2).withNano(0),
                "Aviso de prueba", TipoRecordatorio.VISUAL);
        assertThrows(ResourceNotFoundException.class,
                () -> recordatorioService.crearParaActividad(tercero.getCorreo(), actividad.id(), recordatorio));
        assertThrows(ResourceNotFoundException.class,
                () -> recordatorioService.listarDeActividad(tercero.getCorreo(), actividad.id()));

        // La operación legítima del propietario sigue funcionando.
        assertEquals(actividad.id(), actividadService.obtener(propietario.getCorreo(), actividad.id()).id());
    }

    @Test
    void usuarioNoPuedeCrearHorarioParaMedicamentoDeOtroUsuario() {
        Usuario propietario = crearUsuario();
        Usuario tercero = crearUsuario();
        Medicamento medicamento = medicamentoRepository.save(Medicamento.builder()
                .nombre("Medicamento privado")
                .dosis("500 mg")
                .usuario(propietario)
                .activo(true)
                .build());

        HorarioMedicamentoRequest request = new HorarioMedicamentoRequest(
                TipoFrecuenciaMedicamento.DIARIO,
                LocalDateTime.now().plusDays(1).withNano(0),
                null,
                null,
                LocalTime.of(19, 0),
                Set.of(),
                TipoRecordatorio.VOCAL);

        assertThrows(ResourceNotFoundException.class,
                () -> horarioService.crear(tercero.getCorreo(), medicamento.getId(), request));
    }

    @Test
    void completarActividadCancelaAvisosPendientesFuturosYConservaTrazabilidad() {
        Usuario usuario = crearUsuario();
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        var actividadResponse = actividadService.crear(usuario.getCorreo(), new ActividadSaludRequest(
                "Control médico", TipoActividad.CITA_MEDICA, ahora.plusDays(5),
                "Hospital", "Control de seguimiento"));

        var recordatorioResponse = recordatorioService.crearParaActividad(usuario.getCorreo(),
                actividadResponse.id(), new RecordatorioRequest(
                        ahora.plusDays(4), "Prepararse para el control", TipoRecordatorio.VISUAL));

        actividadService.completar(usuario.getCorreo(), actividadResponse.id());

        ActividadSalud actividad = actividadRepository.findById(actividadResponse.id()).orElseThrow();
        Recordatorio recordatorio = recordatorioRepository.findById(recordatorioResponse.id()).orElseThrow();

        assertEquals(EstadoActividad.COMPLETADA, actividad.getEstado());
        assertEquals(EstadoRecordatorio.CANCELADO, recordatorio.getEstado());
    }

    private Usuario crearUsuario() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return usuarioRepository.saveAndFlush(Usuario.builder()
                .cedula(suffix.substring(0, 12))
                .nombre("Usuario de pruebas")
                .correo(suffix + "@vita.test")
                .contrasena("hash-de-prueba")
                .activo(true)
                .rol(Rol.USUARIO)
                .build());
    }
}

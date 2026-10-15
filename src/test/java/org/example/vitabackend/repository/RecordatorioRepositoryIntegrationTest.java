package org.example.vitabackend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.enums.TipoActividad;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.TomaMedicamento;
import org.example.vitabackend.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class RecordatorioRepositoryIntegrationTest {
    @Autowired private RecordatorioRepository recordatorioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ActividadSaludRepository actividadRepository;
    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private TomaMedicamentoRepository tomaRepository;

    @org.junit.jupiter.api.Test
    void agendaIncluyeRecordatoriosDeActividadYMedicamentoSoloDelUsuarioPropietario() {
        Usuario ana = guardarUsuario("310000001", "ana-recordatorios@vita.test");
        Usuario juan = guardarUsuario("310000002", "juan-recordatorios@vita.test");
        LocalDateTime desde = LocalDateTime.of(2026, 10, 20, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 10, 21, 0, 0);

        ActividadSalud actividadAna = actividadRepository.save(ActividadSalud.builder()
                .nombre("Cita").tipo(TipoActividad.CITA_MEDICA).fechaHora(desde.plusHours(8))
                .estado(EstadoActividad.PENDIENTE).usuario(ana).build());
        recordatorioRepository.save(Recordatorio.builder().fechaHora(desde.plusHours(7))
                .mensaje("Cita a las 8").tipoRecordatorio(TipoRecordatorio.VISUAL)
                .estado(EstadoRecordatorio.PENDIENTE).actividadSalud(actividadAna).build());

        Medicamento medicamentoAna = medicamentoRepository.save(Medicamento.builder()
                .nombre("Medicamento A").usuario(ana).build());
        TomaMedicamento tomaAna = tomaRepository.save(TomaMedicamento.builder()
                .medicamento(medicamentoAna).fechaHora(desde.plusHours(9)).estado(EstadoTomaMed.PENDIENTE).build());
        recordatorioRepository.save(Recordatorio.builder().fechaHora(desde.plusHours(9))
                .mensaje("Tomar medicamento A").tipoRecordatorio(TipoRecordatorio.SONORA)
                .estado(EstadoRecordatorio.PENDIENTE).tomaMedicamento(tomaAna).build());

        ActividadSalud actividadJuan = actividadRepository.save(ActividadSalud.builder()
                .nombre("Cita de Juan").tipo(TipoActividad.CITA_MEDICA).fechaHora(desde.plusHours(10))
                .estado(EstadoActividad.PENDIENTE).usuario(juan).build());
        recordatorioRepository.save(Recordatorio.builder().fechaHora(desde.plusHours(9))
                .mensaje("Cita ajena").tipoRecordatorio(TipoRecordatorio.VISUAL)
                .estado(EstadoRecordatorio.PENDIENTE).actividadSalud(actividadJuan).build());

        List<Recordatorio> resultado = recordatorioRepository
                .findAgendaByUsuarioIdAndIntervalo(ana.getId(), desde, hasta);

        assertEquals(2, resultado.size());
        assertEquals("Cita a las 8", resultado.get(0).getMensaje());
        assertEquals("Tomar medicamento A", resultado.get(1).getMensaje());
    }

    private Usuario guardarUsuario(String cedula, String correo) {
        return usuarioRepository.save(Usuario.builder().cedula(cedula).nombre("Usuario de prueba")
                .correo(correo).contrasena("hash-solo-test").activo(true).rol(Rol.USUARIO).build());
    }
}

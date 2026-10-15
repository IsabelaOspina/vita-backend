package org.example.vitabackend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.enums.TipoActividad;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.request.RecordatorioRequest;
import org.example.vitabackend.exception.GlobalExceptionHandler;
import org.example.vitabackend.repository.ActividadSaludRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.ActividadSaludService;
import org.example.vitabackend.service.RecordatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de aceptación de los casos formales del proceso Gestión del Calendario de Salud.
 *
 * Cubre la parte que pertenece al backend. Las interacciones exclusivas de React Native/Expo
 * (lector de pantalla, permisos del SO, aviso vocal, pantalla bloqueada y apertura de ajustes)
 * se documentan como pruebas manuales de frontend y no se simulan artificialmente aquí.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CalendarioSaludFormalAcceptanceTest {

    private static final ZoneId ZONA_VITA = ZoneId.of("America/Bogota");

    @Autowired private ActividadSaludController actividadController;
    @Autowired private RecordatorioController recordatorioController;
    @Autowired private ActividadSaludService actividadService;
    @Autowired private RecordatorioService recordatorioService;
    @Autowired private ActividadSaludRepository actividadRepository;
    @Autowired private RecordatorioRepository recordatorioRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private MockMvc mvc;
    private Usuario usuario;
    private Authentication autenticacion;
    private LocalDateTime ahora;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(actividadController, recordatorioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        String suffix = UUID.randomUUID().toString().replace("-", "");
        usuario = usuarioRepository.saveAndFlush(Usuario.builder()
                .cedula(suffix.substring(0, 12))
                .nombre("Usuario de aceptación")
                .correo(suffix + "@vita.test")
                .contrasena("hash-de-prueba")
                .activo(true)
                .rol(Rol.USUARIO)
                .build());
        autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                usuario.getCorreo(), "", List.of());
        ahora = LocalDateTime.now(ZONA_VITA).withNano(0);
    }

    /** C3 — Registro de actividad de salud correcto. */
    @Test
    void C3_registroActividad_retorna201YPersisteLaActividad() throws Exception {
        String fecha = ahora.plusDays(5).withHour(8).toString();

        // El documento formal usa "Medicamento" como tipo, pero el enum vigente de VITA
        // solo permite CITA_MEDICA, TERAPIA y OTRO. Se usa OTRO sin modificar el contrato acordado.
        mvc.perform(post("/api/actividades-salud")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadJson("Losartán 50 mg", "OTRO", fecha)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Losartán 50 mg"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        var guardadas = actividadRepository.findByUsuario_IdOrderByFechaHoraAscIdAsc(usuario.getId());
        assertEquals(1, guardadas.size());
        assertEquals("Losartán 50 mg", guardadas.getFirst().getNombre());
    }

    /** C24 — Registro con campos obligatorios vacíos. */
    @Test
    void C24_registroConCamposObligatoriosVacios_retorna400YNoPersiste() throws Exception {
        mvc.perform(post("/api/actividades-salud")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":" ",
                                  "tipo":"CITA_MEDICA"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.fechaHora").exists());

        assertEquals(0, actividadRepository.findByUsuario_IdOrderByFechaHoraAscIdAsc(usuario.getId()).size());
    }

    /** C25 — Fechas pasadas son rechazadas; una fecha futura es aceptada. */
    @Test
    void C25_fechaAnteriorNoSeGuardaYFechaFuturaSi() throws Exception {
        LocalDateTime ayer = ahora.minusDays(1).withHour(10);
        LocalDateTime minutoPasado = ahora.minusMinutes(1);
        LocalDateTime minutoFuturo = ahora.plusMinutes(1);

        mvc.perform(post("/api/actividades-salud")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadJson("Control de tensión - ayer", "CITA_MEDICA", ayer.toString())))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/actividades-salud")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadJson("Control de tensión - minuto pasado", "CITA_MEDICA", minutoPasado.toString())))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/actividades-salud")
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadJson("Control de tensión - minuto futuro", "CITA_MEDICA", minutoFuturo.toString())))
                .andExpect(status().isCreated());

        assertEquals(1, actividadRepository.findByUsuario_IdOrderByFechaHoraAscIdAsc(usuario.getId()).size());
    }

    /** C10 — Edición y reprogramación de recordatorio asociado. */
    @Test
    void C10_editarActividad_actualizaHoraYReprogramaRecordatorioSinDuplicar() throws Exception {
        var creada = crearActividad("Terapia física", ahora.plusDays(5).withHour(15));
        LocalDateTime recordatorioInicial = creada.getFechaHora().minusMinutes(10);
        var recordatorio = recordatorioService.crearParaActividad(usuario.getCorreo(), creada.getId(),
                new RecordatorioRequest(recordatorioInicial, "Recordatorio terapia", TipoRecordatorio.VOCAL));

        LocalDateTime nuevaFecha = creada.getFechaHora().plusHours(1).plusMinutes(30);

        mvc.perform(put("/api/actividades-salud/{id}", creada.getId())
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actividadJson("Terapia física", "TERAPIA", nuevaFecha.toString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaHora").value(nuevaFecha.toString()));

        ActividadSalud guardada = actividadRepository.findById(creada.getId()).orElseThrow();
        assertEquals(nuevaFecha, guardada.getFechaHora());

        Recordatorio reprogramado = recordatorioRepository.findById(recordatorio.id()).orElseThrow();
        assertEquals(EstadoRecordatorio.PENDIENTE, reprogramado.getEstado());
        assertEquals(nuevaFecha.minusMinutes(10), reprogramado.getFechaHora());
        assertEquals(1, recordatorioRepository
                .findByActividadSalud_IdAndActividadSalud_Usuario_IdOrderByFechaHoraAscIdAsc(
                        creada.getId(), usuario.getId()).size());
    }

    /** C6 — Confirmación antes de eliminación y cancelación lógica en backend. */
    @Test
    void C6_eliminarActividad_cancelaActividadYRecordatoriosPendientes() throws Exception {
        var actividad = crearActividad("Caminata 30 minutos", ahora.plusDays(8).withHour(7));
        var recordatorio = recordatorioService.crearParaActividad(usuario.getCorreo(), actividad.getId(),
                new RecordatorioRequest(actividad.getFechaHora().minusMinutes(5), "Aviso caminata", TipoRecordatorio.VISUAL));

        // La confirmación del diálogo es responsabilidad del frontend. El backend ejecuta la operación
        // únicamente después de que el cliente haya confirmado.
        mvc.perform(delete("/api/actividades-salud/{id}", actividad.getId())
                        .principal(autenticacion))
                .andExpect(status().isNoContent());

        assertEquals(EstadoActividad.CANCELADA,
                actividadRepository.findById(actividad.getId()).orElseThrow().getEstado());
        assertEquals(EstadoRecordatorio.CANCELADO,
                recordatorioRepository.findById(recordatorio.id()).orElseThrow().getEstado());
    }

    /** C4 — Consulta general cronológica. */
    @Test
    void C4_consultarActividades_listaSoloLasPropiasEnOrdenCronologico() throws Exception {
        crearActividad("Actividad de tarde", ahora.plusDays(3).withHour(16));
        crearActividad("Actividad de mañana", ahora.plusDays(3).withHour(9));
        crearActividad("Actividad de noche", ahora.plusDays(3).withHour(20));

        mvc.perform(get("/api/actividades-salud").principal(autenticacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Actividad de mañana"))
                .andExpect(jsonPath("$[1].nombre").value("Actividad de tarde"))
                .andExpect(jsonPath("$[2].nombre").value("Actividad de noche"));
    }

    /** C4/RF04 + RF05 — Consulta y filtro por fecha. */
    @Test
    void C4_consultaYFiltroPorFecha_devuelveSoloLasActividadesEsperadas() throws Exception {
        LocalDateTime fecha20 = ahora.plusDays(5).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fecha25 = fecha20.plusDays(5);
        crearActividad("Actividad 20 - mañana", fecha20.withHour(8));
        crearActividad("Actividad 20 - tarde", fecha20.withHour(15));
        crearActividad("Actividad 21", fecha20.plusDays(1).withHour(10));

        mvc.perform(get("/api/actividades-salud/dia")
                        .principal(autenticacion)
                        .param("fecha", fecha20.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Actividad 20 - mañana"))
                .andExpect(jsonPath("$[1].nombre").value("Actividad 20 - tarde"));

        mvc.perform(get("/api/actividades-salud/dia")
                        .principal(autenticacion)
                        .param("fecha", fecha25.toLocalDate().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /** C5 — Programación de recordatorio; prueba inmediata del dispositivo es frontend. */
    @Test
    void C5_programarRecordatorio_guardaAvisoDiezMinutosAntes() throws Exception {
        LocalDateTime actividadFecha = ahora.plusDays(5).withHour(8).withMinute(0);
        var actividad = crearActividad("Losartán 50 mg", actividadFecha);
        LocalDateTime fechaAviso = actividadFecha.minusMinutes(10);

        mvc.perform(post("/api/actividades-salud/{actividadId}/recordatorios", actividad.getId())
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordatorioJson(fechaAviso, "Es hora de tomar Losartán 50 mg", "VOCAL")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fechaHora").value(fechaAviso.toString()))
                .andExpect(jsonPath("$.actividadSaludId").value(actividad.getId()));
    }

    /** C9 — Confirmación mediante botón/API; voz y respuesta hablada son frontend. */
    @Test
    void C9_confirmarActividad_realizadaMarcaCompletadaRegistraHoraYCancelaAvisosFuturos() throws Exception {
        var actividad = crearActividad("Losartán 50 mg", ahora.plusDays(5).withHour(8));
        var recordatorio = recordatorioService.crearParaActividad(usuario.getCorreo(), actividad.getId(),
                new RecordatorioRequest(actividad.getFechaHora().minusMinutes(10), "Tomar medicamento", TipoRecordatorio.VOCAL));

        LocalDateTime antes = LocalDateTime.now(ZONA_VITA);
        mvc.perform(patch("/api/actividades-salud/{id}/completar", actividad.getId())
                        .principal(autenticacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.fechaHoraCumplimiento").exists());
        LocalDateTime despues = LocalDateTime.now(ZONA_VITA);

        ActividadSalud guardada = actividadRepository.findById(actividad.getId()).orElseThrow();
        assertEquals(EstadoActividad.COMPLETADA, guardada.getEstado());
        assertNotNull(guardada.getFechaHoraCumplimiento());
        assertEquals(EstadoRecordatorio.CANCELADO,
                recordatorioRepository.findById(recordatorio.id()).orElseThrow().getEstado());
        assertEquals(true, !guardada.getFechaHoraCumplimiento().isBefore(antes)
                && !guardada.getFechaHoraCumplimiento().isAfter(despues));
    }

    /** C7 — El móvil reporta que la notificación local fue presentada. */
    @Test
    void C7_verificarEnvioRecordatorio_actualizaEstadoAEnviado() throws Exception {
        var actividad = crearActividad("Losartán 50 mg", ahora.plusDays(5).withHour(8));
        var recordatorio = recordatorioService.crearParaActividad(usuario.getCorreo(), actividad.getId(),
                new RecordatorioRequest(actividad.getFechaHora().minusMinutes(1), "Es hora de tomar Losartán 50 mg", TipoRecordatorio.VOCAL));

        mvc.perform(patch("/api/recordatorios/{id}/enviado", recordatorio.id())
                        .principal(autenticacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENVIADO"));

        assertEquals(EstadoRecordatorio.ENVIADO,
                recordatorioRepository.findById(recordatorio.id()).orElseThrow().getEstado());
    }

    /**
     * C26 — El backend puede guardar actividad y recordatorio aunque el permiso del SO esté desactivado.
     * La detección del permiso, el mensaje accesible y la apertura de Ajustes pertenecen al móvil.
     */
    @Test
    void C26_permisoNotificacionesDesactivado_noImpidePersistirActividadYRecordatorio() throws Exception {
        LocalDateTime actividadFecha = ahora.plusDays(6).withHour(13);
        var actividad = crearActividad("Metformina 850 mg", actividadFecha);
        var fechaAviso = actividadFecha.minusMinutes(5);

        mvc.perform(post("/api/actividades-salud/{actividadId}/recordatorios", actividad.getId())
                        .principal(autenticacion)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recordatorioJson(fechaAviso, "Tomar Metformina 850 mg", "VISUAL")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));

        assertEquals(1, recordatorioRepository
                .findByActividadSalud_IdAndActividadSalud_Usuario_IdOrderByFechaHoraAscIdAsc(
                        actividad.getId(), usuario.getId()).size());
    }

    private ActividadSalud crearActividad(String nombre, LocalDateTime fechaHora) {
        var creada = actividadService.crear(usuario.getCorreo(),
                new ActividadSaludRequest(nombre, TipoActividad.CITA_MEDICA,
                        fechaHora, "Hospital de pruebas", "Prueba formal"));
        assertNotNull(creada.id());
        return actividadRepository.findById(creada.id()).orElseThrow();
    }

    private String actividadJson(String nombre, String tipo, String fechaHora) {
        return """
                {
                  "nombre":"%s",
                  "tipo":"%s",
                  "fechaHora":"%s",
                  "ubicacion":"Hospital de pruebas",
                  "descripcion":"Prueba formal"
                }
                """.formatted(nombre, tipo, fechaHora);
    }

    private String recordatorioJson(LocalDateTime fechaHora, String mensaje, String tipo) {
        return """
                {
                  "fechaHora":"%s",
                  "mensaje":"%s",
                  "tipoRecordatorio":"%s"
                }
                """.formatted(fechaHora, mensaje, tipo);
    }
}

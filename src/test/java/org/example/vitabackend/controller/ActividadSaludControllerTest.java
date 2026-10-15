package org.example.vitabackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.TipoActividad;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.response.ActividadSaludResponse;
import org.example.vitabackend.exception.GlobalExceptionHandler;
import org.example.vitabackend.service.ActividadSaludService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ActividadSaludControllerTest {

    private static final String CORREO = "usuario@vita.test";

    @Mock
    private ActividadSaludService actividadSaludService;

    private MockMvc mockMvc;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        ActividadSaludController controller = new ActividadSaludController(actividadSaludService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authentication = UsernamePasswordAuthenticationToken.authenticated(CORREO, "", List.of());
    }

    @Test
    void listar_DevuelveActividadesDelServicio() throws Exception {
        ActividadSaludResponse response = new ActividadSaludResponse(
                3L, "Cita de control", TipoActividad.CITA_MEDICA,
                LocalDateTime.of(2026, 10, 20, 9, 0), "Hospital", "Control anual",
                EstadoActividad.PENDIENTE);
        when(actividadSaludService.listar(CORREO)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/actividades-salud").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].nombre").value("Cita de control"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));

        verify(actividadSaludService).listar(CORREO);
    }

    @Test
    void crear_ValidaSolicitudYDevuelve201() throws Exception {
        ActividadSaludResponse response = new ActividadSaludResponse(
                8L, "Terapia", TipoActividad.TERAPIA,
                LocalDateTime.of(2026, 10, 20, 9, 0), "Centro médico", "Sesión",
                EstadoActividad.PENDIENTE);
        when(actividadSaludService.crear(org.mockito.ArgumentMatchers.eq(CORREO), any(ActividadSaludRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/actividades-salud")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":"Terapia",
                                  "tipo":"TERAPIA",
                                  "fechaHora":"2026-10-20T09:00:00",
                                  "ubicacion":"Centro médico",
                                  "descripcion":"Sesión"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.tipo").value("TERAPIA"));
    }

    @Test
    void crear_RechazaNombreVacio() throws Exception {
        mockMvc.perform(post("/api/actividades-salud")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre":" ",
                                  "tipo":"TERAPIA",
                                  "fechaHora":"2026-10-20T09:00:00",
                                  "ubicacion":"Centro médico",
                                  "descripcion":"Sesión"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}

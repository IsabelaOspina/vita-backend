package org.example.vitabackend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.enums.NivelIntensidad;
import org.example.vitabackend.domain.enums.OrdenSintoma;
import org.example.vitabackend.dto.request.SintomaFiltro;
import org.example.vitabackend.dto.request.SintomaRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.SintomaResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.GlobalExceptionHandler;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.SintomaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class SintomaControllerTest {

    private static final Long PACIENTE_ID = 7L;

    @Mock
    private SintomaService sintomaService;
    @Mock
    private UsuarioActual usuarioActual;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // algunas pruebas fallan en la validación y nunca llegan a pedir el id del usuario
        lenient().when(usuarioActual.id()).thenReturn(PACIENTE_ID);
        mockMvc = MockMvcBuilders.standaloneSetup(new SintomaController(sintomaService, usuarioActual))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private SintomaResponse respuesta(Long id, String tipo, int intensidad) {
        return new SintomaResponse(id, tipo, intensidad, NivelIntensidad.SEVERA,
                LocalDateTime.of(2026, 10, 14, 15, 30), null,
                LocalDateTime.of(2026, 10, 14, 15, 31), null, tipo + ", intensidad " + intensidad + " de 10");
    }

    // Registro con datos válidos
    @Test
    void registrar_ConDatosValidosDevuelve201YElSintomaCreado() throws Exception {
        when(sintomaService.registrar(eq(PACIENTE_ID), any(SintomaRequest.class)))
                .thenReturn(respuesta(12L, "dolor de cabeza", 7));

        mockMvc.perform(post("/api/sintomas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"dolor de cabeza","intensidad":7,"fechaHora":"2026-10-14T15:30:00"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.tipo").value("dolor de cabeza"))
                .andExpect(jsonPath("$.nivelIntensidad").value("SEVERA"));

        ArgumentCaptor<SintomaRequest> captor = ArgumentCaptor.forClass(SintomaRequest.class);
        verify(sintomaService).registrar(eq(PACIENTE_ID), captor.capture());
        assertEquals(7, captor.getValue().intensidad());
        assertEquals(LocalDateTime.of(2026, 10, 14, 15, 30), captor.getValue().fechaHora());
    }

    // Registro con campos obligatorios vacíos
    @Test
    void registrar_ConCamposObligatoriosVaciosDevuelve400ConElDetalleDeCadaCampo() throws Exception {
        mockMvc.perform(post("/api/sintomas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"","observaciones":"malestar general"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"))
                .andExpect(jsonPath("$.errores.tipo").value("El tipo de síntoma es obligatorio"))
                .andExpect(jsonPath("$.errores.intensidad").value("La intensidad es obligatoria"));

        verify(sintomaService, never()).registrar(anyLong(), any());
    }

    // Registro con intensidad fuera del rango determinado (1-10)
    @Test
    void registrar_ConIntensidadFueraDeRangoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/sintomas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"Dolor de espalda","intensidad":11}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.intensidad").value("La intensidad máxima es 10"));

        mockMvc.perform(post("/api/sintomas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"Dolor de espalda","intensidad":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.intensidad").value("La intensidad mínima es 1"));

        verify(sintomaService, never()).registrar(anyLong(), any());
    }

    // Registro con fecha ilegible
    @Test
    void registrar_ConFechaIlegibleDevuelve400ConMensajeEntendible() throws Exception {
        mockMvc.perform(post("/api/sintomas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"mareo","intensidad":3,"fechaHora":"hoy"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(sintomaService, never()).registrar(anyLong(), any());
    }

    // Registro con fecha en el futuro
    @Test
    void registrar_ConReglaDeNegocioIncumplidaDevuelve400ConElMensajeDelServicio() throws Exception {
        when(sintomaService.registrar(eq(PACIENTE_ID), any(SintomaRequest.class)))
                .thenThrow(new BusinessException("La fecha y hora del síntoma no puede estar en el futuro"));

        mockMvc.perform(post("/api/sintomas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"mareo","intensidad":3,"fechaHora":"2030-01-01T10:00:00"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha y hora del síntoma no puede estar en el futuro"));
    }


    // Consultar con valores por defecto
    @Test
    void listar_PasaLosFiltrosAlServicioConLosValoresPorDefecto() throws Exception {
        when(sintomaService.listar(eq(PACIENTE_ID), any(SintomaFiltro.class), eq(OrdenSintoma.RECIENTES), anyInt(), anyInt()))
                .thenReturn(new PaginaResponse<>(List.of(respuesta(1L, "mareo", 4)), 0, 20, 1, 1, true));

        mockMvc.perform(get("/api/sintomas")
                        .param("desde", "2026-10-01")
                        .param("hasta", "2026-10-14")
                        .param("tipo", "mareo")
                        .param("intensidadMin", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].tipo").value("mareo"))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.ultima").value(true));

        ArgumentCaptor<SintomaFiltro> filtro = ArgumentCaptor.forClass(SintomaFiltro.class);
        verify(sintomaService).listar(eq(PACIENTE_ID), filtro.capture(), eq(OrdenSintoma.RECIENTES), eq(0), eq(20));
        assertEquals(LocalDate.of(2026, 10, 1), filtro.getValue().desde());
        assertEquals(LocalDate.of(2026, 10, 14), filtro.getValue().hasta());
        assertEquals("mareo", filtro.getValue().tipo());
        assertEquals(3, filtro.getValue().intensidadMin());
        assertNull(filtro.getValue().intensidadMax());
    }

    // Consultar con fecha incorrecta
    @Test
    void listar_ConFechaEnFormatoIncorrectoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/sintomas").param("desde", "14/10/2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro 'desde'"));

        verify(sintomaService, never()).listar(anyLong(), any(), any(), anyInt(), anyInt());
    }

    // Consultar con orden que no existe
    @Test
    void listar_ConOrdenInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/sintomas").param("orden", "OTRO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro 'orden'"));
    }

    // Connsulta con sintoma no encontrado
    @Test
    void obtener_SiNoExisteDevuelve404() throws Exception {
        when(sintomaService.obtener(PACIENTE_ID, 99L)).thenThrow(new ResourceNotFoundException("Síntoma no encontrado"));

        mockMvc.perform(get("/api/sintomas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Síntoma no encontrado"));
    }

    // Actualizar con datos válidos
    @Test
    void actualizar_ConDatosValidosDevuelve200() throws Exception {
        when(sintomaService.actualizar(eq(PACIENTE_ID), eq(12L), any()))
                .thenReturn(respuesta(12L, "vértigo", 6));

        mockMvc.perform(put("/api/sintomas/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"vértigo","intensidad":6}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("vértigo"));
    }

    // Actualizar con intensidad inválida
    @Test
    void actualizar_ConIntensidadInvalidaDevuelve400() throws Exception {
        mockMvc.perform(put("/api/sintomas/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"vértigo","intensidad":15}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.intensidad").exists());
    }

    // Evolución con rango invertido
    @Test
    void evolucion_ConRangoInvertidoDevuelve400ConElMensajeDelServicio() throws Exception {
        when(sintomaService.evolucion(eq(PACIENTE_ID), any(), any(), any(), any()))
                .thenThrow(new BusinessException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'"));

        mockMvc.perform(get("/api/sintomas/evolucion")
                        .param("desde", "2026-10-14")
                        .param("hasta", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha 'desde' no puede ser posterior a la fecha 'hasta'"));
    }

    // Evolución con valor inválido
    @Test
    void evolucion_ConAgrupacionInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/sintomas/evolucion").param("agrupacion", "ANIO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro 'agrupacion'"));
    }
}

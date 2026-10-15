package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.mapper.ActividadSaludMapper;
import org.example.vitabackend.repository.ActividadSaludRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActividadCalendarioConsultasTest {

    @Mock private ActividadSaludRepository actividadRepository;
    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ActividadSaludMapper mapper;
    @InjectMocks private ActividadSaludServiceImpl service;

    private final Usuario usuario = Usuario.builder().id(14L).correo("calendario@vita.test").build();

    void prepararUsuario() {
        when(usuarioRepository.findByCorreo("calendario@vita.test")).thenReturn(Optional.of(usuario));
    }

    @Test
    void listarPorDia_UsaInicioYFinExclusivoDelDia() {
        prepararUsuario();

        service.listarPorDia("calendario@vita.test", LocalDate.of(2026, 10, 14));
        verify(actividadRepository)
                .findByUsuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
                        eq(14L), eq(LocalDateTime.of(2026, 10, 14, 0, 0)),
                        eq(LocalDateTime.of(2026, 10, 15, 0, 0)));
    }

    @Test
    void listarPorSemana_UsaSemanaDeLunesALunes() {
        prepararUsuario();

        service.listarPorSemana("calendario@vita.test", LocalDate.of(2026, 10, 14));
        verify(actividadRepository)
                .findByUsuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
                        eq(14L), eq(LocalDateTime.of(2026, 10, 12, 0, 0)),
                        eq(LocalDateTime.of(2026, 10, 19, 0, 0)));
    }

    @Test
    void listarPorMes_UsaPrimerDiaDelMesYPrimerDiaDelMesSiguiente() {
        prepararUsuario();

        service.listarPorMes("calendario@vita.test", 2026, 10);
        verify(actividadRepository)
                .findByUsuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
                        eq(14L), eq(LocalDateTime.of(2026, 10, 1, 0, 0)),
                        eq(LocalDateTime.of(2026, 11, 1, 0, 0)));
    }

   @Test
    void listarPorMes_RechazaMesInvalido() {
        assertThrows(BusinessException.class,
                () -> service.listarPorMes("calendario@vita.test", 2026, 13));
    }
}
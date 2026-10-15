package org.example.vitabackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.config.Reloj;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.OrdenSintoma;
import org.example.vitabackend.domain.model.Sintoma;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.SintomaActualizarRequest;
import org.example.vitabackend.dto.request.SintomaFiltro;
import org.example.vitabackend.dto.request.SintomaRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.SintomaEvolucionResponse;
import org.example.vitabackend.dto.response.SintomaResponse;
import org.example.vitabackend.dto.response.SintomaResumenResponse;
import org.example.vitabackend.dto.response.TipoSintomaResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.SintomaMapper;
import org.example.vitabackend.repository.SintomaRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.SintomaAnalisis;
import org.example.vitabackend.service.SintomaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SintomaServiceImpl implements SintomaService {

    private static final LocalDateTime MUY_ANTIGUO = LocalDateTime.of(1900, 1, 1, 0, 0);
    private static final LocalDateTime MUY_FUTURO = LocalDateTime.of(3000, 1, 1, 0, 0);
    private static final int TAMANO_MAXIMO = 100;
    private static final int DIAS_POR_DEFECTO = 30;
    private static final int MAX_DIAS_AGRUPADOS_POR_DIA = 366;
    /** Tolerancia por si el reloj del celular va adelantado unos minutos. */
    private static final int TOLERANCIA_FUTURO_MINUTOS = 5;

    private final SintomaRepository sintomaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Reloj reloj;

    @Override
    @Transactional
    public SintomaResponse registrar(Long pacienteId, SintomaRequest request) {
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        LocalDateTime ahora = reloj.ahora();

        Sintoma sintoma = Sintoma.builder()
                .tipo(tipoValido(request.tipo()))
                .intensidad(request.intensidad())
                .fechaHora(fechaValida(request.fechaHora(), ahora))
                .observaciones(limpiarObservaciones(request.observaciones()))
                .fechaRegistro(ahora)
                .usuario(paciente)
                .build();
        return SintomaMapper.aRespuesta(sintomaRepository.save(sintoma));
    }

    @Override
    @Transactional(readOnly = true)
    public SintomaResponse obtener(Long pacienteId, Long sintomaId) {
        return SintomaMapper.aRespuesta(buscar(pacienteId, sintomaId));
    }

    @Override
    @Transactional
    public SintomaResponse actualizar(Long pacienteId, Long sintomaId, SintomaActualizarRequest request) {
        Sintoma sintoma = buscar(pacienteId, sintomaId);
        LocalDateTime ahora = reloj.ahora();

        sintoma.setTipo(tipoValido(request.tipo()));
        sintoma.setIntensidad(request.intensidad());
        if (request.fechaHora() != null) {
            sintoma.setFechaHora(fechaValida(request.fechaHora(), ahora));
        }
        sintoma.setObservaciones(limpiarObservaciones(request.observaciones()));
        sintoma.setFechaActualizacion(ahora);
        return SintomaMapper.aRespuesta(sintomaRepository.save(sintoma));
    }

    @Override
    @Transactional
    public void eliminar(Long pacienteId, Long sintomaId) {
        sintomaRepository.delete(buscar(pacienteId, sintomaId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<SintomaResponse> listar(Long pacienteId, SintomaFiltro filtro, OrdenSintoma orden,
                                                  int pagina, int tamano) {
        if (pagina < 0) {
            throw new BusinessException("La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new BusinessException("El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        validarRango(filtro.desde(), filtro.hasta());

        int minimo = filtro.intensidadMin() != null ? filtro.intensidadMin() : 1;
        int maximo = filtro.intensidadMax() != null ? filtro.intensidadMax() : 10;
        if (minimo < 1 || maximo > 10 || minimo > maximo) {
            throw new BusinessException("El rango de intensidad debe estar entre 1 y 10 (mínimo no mayor al máximo)");
        }

        Page<Sintoma> resultado = sintomaRepository.buscar(
                pacienteId,
                inicio(filtro.desde()),
                finExclusivo(filtro.hasta()),
                patron(filtro.tipo()),
                minimo, maximo,
                PageRequest.of(pagina, tamano, ordenar(orden)));

        return new PaginaResponse<>(
                resultado.getContent().stream().map(SintomaMapper::aRespuesta).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages(), resultado.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public SintomaResumenResponse resumen(Long pacienteId, LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : reloj.ahora().toLocalDate();
        LocalDate ini = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1L);
        validarRango(ini, fin);

        List<SintomaAnalisis.Registro> registros = sintomaRepository
                .buscarEnRango(pacienteId, inicio(ini), finExclusivo(fin), "%")
                .stream().map(SintomaMapper::aRegistro).toList();
        return SintomaAnalisis.resumen(ini, fin, registros);
    }

    @Override
    @Transactional(readOnly = true)
    public SintomaEvolucionResponse evolucion(Long pacienteId, String tipo, LocalDate desde, LocalDate hasta,
                                              AgrupacionTiempo agrupacion) {
        AgrupacionTiempo agrup = agrupacion != null ? agrupacion : AgrupacionTiempo.DIA;
        LocalDate fin = hasta != null ? hasta : reloj.ahora().toLocalDate();
        LocalDate ini = desde != null ? desde : fin.minusDays(DIAS_POR_DEFECTO - 1L);
        validarRango(ini, fin);
        if (agrup == AgrupacionTiempo.DIA && ini.plusDays(MAX_DIAS_AGRUPADOS_POR_DIA).isBefore(fin)) {
            throw new BusinessException("Para agrupar por día el rango máximo es de " + MAX_DIAS_AGRUPADOS_POR_DIA
                    + " días; use SEMANA o MES para rangos más largos");
        }

        String tipoNormalizado = SintomaAnalisis.normalizarTipo(tipo);
        List<SintomaAnalisis.Registro> registros = sintomaRepository
                .buscarEnRango(pacienteId, inicio(ini), finExclusivo(fin), patron(tipoNormalizado))
                .stream().map(SintomaMapper::aRegistro).toList();
        return SintomaAnalisis.evolucion(tipoNormalizado, ini, fin, agrup, registros);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoSintomaResponse> tiposRegistrados(Long pacienteId) {
        // De más reciente a más antiguo: la primera escritura de cada tipo es la más reciente
        Map<String, TipoSintomaResponse> porClave = new LinkedHashMap<>();
        for (Sintoma s : sintomaRepository.findByUsuario_IdOrderByFechaHoraDesc(pacienteId)) {
            String clave = SintomaAnalisis.claveTipo(s.getTipo());
            TipoSintomaResponse actual = porClave.get(clave);
            porClave.put(clave, actual == null
                    ? new TipoSintomaResponse(SintomaAnalisis.normalizarTipo(s.getTipo()), 1, s.getFechaHora())
                    : new TipoSintomaResponse(actual.tipo(), actual.cantidad() + 1, actual.ultimoRegistro()));
        }
        List<TipoSintomaResponse> tipos = new ArrayList<>(porClave.values());
        tipos.sort(Comparator.comparingLong(TipoSintomaResponse::cantidad).reversed()
                .thenComparing(TipoSintomaResponse::tipo));
        return tipos;
    }

    // ---------- ayudas privadas ----------

    /** 404 también cuando el síntoma es de otro usuario: no se revela que existe. */
    private Sintoma buscar(Long pacienteId, Long sintomaId) {
        return sintomaRepository.findByIdAndUsuario_Id(sintomaId, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Síntoma no encontrado"));
    }

    private String tipoValido(String tipo) {
        String normalizado = SintomaAnalisis.normalizarTipo(tipo);
        if (normalizado.isEmpty()) {
            throw new BusinessException("El tipo de síntoma es obligatorio");
        }
        return normalizado;
    }

    private String limpiarObservaciones(String observaciones) {
        return observaciones == null || observaciones.isBlank() ? null : observaciones.trim();
    }

    private LocalDateTime fechaValida(LocalDateTime fechaHora, LocalDateTime ahora) {
        if (fechaHora == null) {
            return ahora;
        }
        if (fechaHora.isAfter(ahora.plusMinutes(TOLERANCIA_FUTURO_MINUTOS))) {
            throw new BusinessException("La fecha y hora del síntoma no puede estar en el futuro");
        }
        return fechaHora;
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
    }

    private LocalDateTime inicio(LocalDate desde) {
        return desde == null ? MUY_ANTIGUO : desde.atStartOfDay();
    }

    /** "hasta" es un día completo inclusivo: se consulta hasta el inicio del día siguiente (exclusivo). */
    private LocalDateTime finExclusivo(LocalDate hasta) {
        return hasta == null ? MUY_FUTURO : hasta.plusDays(1).atStartOfDay();
    }

    private String patron(String tipo) {
        String normalizado = SintomaAnalisis.claveTipo(tipo);
        return normalizado.isEmpty() ? "%" : "%" + normalizado + "%";
    }

    private Sort ordenar(OrdenSintoma orden) {
        OrdenSintoma o = orden != null ? orden : OrdenSintoma.RECIENTES;
        return switch (o) {
            case RECIENTES -> Sort.by(Sort.Order.desc("fechaHora"), Sort.Order.desc("id"));
            case ANTIGUOS -> Sort.by(Sort.Order.asc("fechaHora"), Sort.Order.asc("id"));
            case INTENSIDAD -> Sort.by(Sort.Order.desc("intensidad"), Sort.Order.desc("fechaHora"));
        };
    }
}

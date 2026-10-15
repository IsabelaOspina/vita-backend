package org.example.vitabackend.dto.response;

import java.util.List;

/** Una página de resultados. pagina empieza en 0. */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean ultima) {
}

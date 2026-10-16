package org.example.vitabackend.mapper;

import org.example.vitabackend.domain.model.PersonalSalud;
import org.example.vitabackend.domain.model.Recomendacion;
import org.example.vitabackend.dto.response.RecomendacionResponse;

/** Conversión manual Recomendacion -> respuesta (incluye el texto de lectura). */
public final class RecomendacionMapper {

    private RecomendacionMapper() {
    }

    public static RecomendacionResponse aRespuesta(Recomendacion r) {
        PersonalSalud datos = r.getProfesional().getPersonalSalud();
        String especialidad = datos != null ? datos.getEspecialidad() : null;
        String nombre = r.getProfesional().getNombre();

        StringBuilder texto = new StringBuilder("Recomendación de ").append(nombre);
        if (especialidad != null) {
            texto.append(" (").append(especialidad).append(")");
        }
        texto.append(". ").append(r.getMensaje());
        if (r.getSintomaReferencia() != null) {
            texto.append(" Sobre su síntoma: ").append(r.getSintomaReferencia()).append(".");
        }

        return new RecomendacionResponse(
                r.getId(),
                r.getMensaje(),
                r.getProfesional().getId(),
                nombre,
                especialidad,
                r.getPaciente().getId(),
                r.getSintomaId(),
                r.getSintomaReferencia(),
                r.getFechaCreacion(),
                r.getLeida(),
                r.getFechaLectura(),
                texto.toString());
    }
}

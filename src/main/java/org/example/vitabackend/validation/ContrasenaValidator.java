package org.example.vitabackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContrasenaValidator implements ConstraintValidator<ContrasenaValida, String> {

    @Override
    public boolean isValid(
            String contrasena,
            ConstraintValidatorContext context) {

        // La obligatoriedad se valida por separado con @NotBlank.
        if (contrasena == null) {
            return true;
        }

        return PoliticaContrasena.cumple(contrasena);
    }
}

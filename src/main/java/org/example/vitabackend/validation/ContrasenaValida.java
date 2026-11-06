package org.example.vitabackend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ContrasenaValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.RECORD_COMPONENT
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ContrasenaValida {

    String message() default "La contraseña debe tener mínimo 8 caracteres, "
            + "una mayúscula, una minúscula, un número y un carácter especial.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

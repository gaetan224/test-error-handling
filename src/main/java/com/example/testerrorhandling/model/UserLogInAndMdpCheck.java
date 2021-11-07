package com.example.testerrorhandling.model;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = UserLogInAndMdpCheckValidator.class)
public @interface UserLogInAndMdpCheck {
    String message() default "mot de passe et login n'ont pas la bone taille";
    String errorType() default "erreurMotDePasseEtLogin"; // le type d'error sinon present le nom de l'annotation sera utiliser

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}

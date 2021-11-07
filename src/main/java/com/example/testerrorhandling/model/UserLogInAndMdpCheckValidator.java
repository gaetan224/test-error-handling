package com.example.testerrorhandling.model;

import jdk.jshell.execution.Util;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserLogInAndMdpCheckValidator implements ConstraintValidator<UserLogInAndMdpCheck, Utilisateur> {
    @Override
    public void initialize(UserLogInAndMdpCheck constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Utilisateur value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        // pour les annotation qui sont sur un object, rajouter le champs en erreur en question
        // et sur la validation portent sur plusieur champs mettre tout c'est champs ou laisser le sur le nom de l'objet
        Map<String,String> map = Map.of("key1","PAYLOADLogin1", "key2", "PAYLOADLogin2");
        context.unwrap(HibernateConstraintValidatorContext.class)
                .withDynamicPayload(map);

        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("login").addConstraintViolation();

        List list = Arrays.asList("a", "b", "c");
        context.unwrap(HibernateConstraintValidatorContext.class)
                .withDynamicPayload(list);

        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("mdp").addConstraintViolation();

        return false;
    }
}

package org.nic.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.nic.auth.EnumAuthValidateLevel.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
    String value();

    String[] validateFields() default {};

    EnumAuthValidateLevel level() default Ultimate;

    boolean validateLogin() default false;
}
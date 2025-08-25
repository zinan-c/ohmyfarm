package org.nic.auth.config.base;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigAttr {
    Class<?> type() default String.class;
    String aliasName() default "";
    String defaltValue() default "";
    int maxValue() default -1;
    int minValue() default -1;
    String desc();
}

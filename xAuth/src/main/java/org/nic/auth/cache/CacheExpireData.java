package org.nic.auth.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheExpireData {
    int seconds() default 86400;

    ExpireDataByConfig expireDataByConfig() default @ExpireDataByConfig(configName = "", configClazz = Void.class);

    String aliasName() default "";

    String desc() default "";

    boolean isDistributed() default false;

    boolean excludePrefixName() default false;
}


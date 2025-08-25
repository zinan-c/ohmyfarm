package org.nic.auth.config.base;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ConfigKeyAttributeInfo {
    public ConfigKeyAttributeInfo(String key, ConfigAttr configAttr) {
        this.key = key;
        this.configAttr = configAttr;
    }

    private ConfigAttr configAttr;
    private String key;

    public ConfigAttr getConfigAttr() {
        return configAttr;
    }

    public void setConfigAttr(ConfigAttr configAttr) {
        this.configAttr = configAttr;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /*获取ConfigKeyInfo*/
    public static ConfigKeyAttributeInfo getAttribute(IConfigurationKeyType key) {
        String fieldName = key.toString();
        HashMap<String, Field> dicFields = ReflectBase.getMappingFields(key.getClass());
        Field field = dicFields.get(fieldName.toLowerCase());
        ConfigAttr configAttr = null;
        if (field != null) {
            configAttr = AnnotationUtils.findAnnotation(field, ConfigAttr.class);
            if (configAttr != null) {
                if (!DataConvert.isNullOrEmpty(configAttr.aliasName())) {
                    fieldName = configAttr.aliasName();
                }
            }
        }
        ConfigKeyAttributeInfo configKeyInfo = new ConfigKeyAttributeInfo(fieldName, configAttr);
        return configKeyInfo;
    }
}

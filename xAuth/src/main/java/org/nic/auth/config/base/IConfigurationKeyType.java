package org.nic.auth.config.base;

import java.util.HashMap;

public interface IConfigurationKeyType {
    HashMap<String, ConfigKeyAttributeInfo> DIC_CONFIGKEY_ATTRIBUTE_INFO = new HashMap<>();

    default ConfigKeyAttributeInfo attribute() {
        String key = this.toString();
        key = this.getClass().getName() + "." + key + ".@ConfigAttr";

        if (DIC_CONFIGKEY_ATTRIBUTE_INFO.containsKey(key)) {
            return DIC_CONFIGKEY_ATTRIBUTE_INFO.get(key);
        } else {
            try {
                ConfigKeyAttributeInfo attr = ConfigKeyAttributeInfo.getAttribute(this);
                DIC_CONFIGKEY_ATTRIBUTE_INFO.put(key, attr);
                return attr;
            } catch (Exception ex) {
            }
        }
        return null;
    }
}

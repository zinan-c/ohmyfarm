package org.nic.auth.config.base;

public interface IConfigReader {
    <T> T get(IConfigurationKeyType key, Class<T> clazz);

    <T> T get(IConfigurationKeyType key);

    <T> T getByYml(IConfigurationKeyType key);

    String get(IConfigurationKeyType... keys);

    String get(String seprator, IConfigurationKeyType... keys);
}


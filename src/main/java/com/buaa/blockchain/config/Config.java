package com.buaa.blockchain.config;

import com.buaa.blockchain.config.exceptions.ConfigException;
import com.buaa.blockchain.config.model.CryptoType;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


public class Config {
    protected static String propertiesDirectory;

    public static ConfigOption load(String app_properties) throws ConfigException {
        return load(app_properties, CryptoType.ECDSA_TYPE);
    }

    public static ConfigOption load(String app_properties, int cryptoType) throws ConfigException {
        try {
            app_properties = Config.class.getResource("/").getPath() + app_properties;
            Properties properties = new Properties();
            if (new File(app_properties).isFile()) {
                try (FileInputStream file = new FileInputStream(app_properties)) {
                    properties.load(file);
                }
            }
            ConfigOption configOption = new ConfigOption(properties, cryptoType);
            return configOption;
        } catch (Exception e) {
            throw new ConfigException(
                    "parse Config " + app_properties + " failed, error info: " + e.getMessage(), e);
        }
    }
}

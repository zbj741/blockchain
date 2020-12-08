package com.buaa.blockchain.config;

import com.buaa.blockchain.config.exceptions.ConfigException;
import com.buaa.blockchain.config.model.AccountConfig;
import com.buaa.blockchain.config.model.CryptoType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Properties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigOption {
    private AccountConfig accountConfig;
    private Properties configProperty;

    public ConfigOption(Properties configProperty) throws ConfigException {
        this(configProperty, CryptoType.ECDSA_TYPE);
    }

    public ConfigOption(Properties configProperty, int cryptoType) throws ConfigException {
        accountConfig = new AccountConfig(configProperty);
        this.configProperty = configProperty;
    }

    public AccountConfig getAccountConfig() {
        return accountConfig;
    }

    public void setAccountConfig(AccountConfig accountConfig) {
        this.accountConfig = accountConfig;
    }

}

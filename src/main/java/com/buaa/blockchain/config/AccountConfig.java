package com.buaa.blockchain.config;

import com.buaa.blockchain.config.exceptions.ConfigException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class AccountConfig {
    @Value("${keyStoreDir}")
    private String keyStoreDir;
    @Value("${accountAddress}")
    private String accountAddress;
    @Value("${accountFileFormat}")
    private String accountFileFormat;
    @Value("${accountPassword}")
    private String accountPassword;
    @Value("${accountFilePath}")
    private String accountFilePath;

    private void checkAccountConfig() throws ConfigException {
        if (this.accountAddress.equals("")) {
            return;
        }
        if ("pem".compareToIgnoreCase(accountFileFormat) != 0
                && "p12".compareToIgnoreCase(accountFileFormat) != 0) {
            throw new ConfigException(
                    "load account failed, only support pem and p12 account file format, current configurated account file format is "
                            + accountFileFormat);
        }
    }

    public String getAccountFilePath() {
        return accountFilePath;
    }

    public void setAccountFilePath(String accountFilePath) {
        this.accountFilePath = accountFilePath;
    }

    public String getKeyStoreDir() {
        return keyStoreDir;
    }

    public void setKeyStoreDir(String keyStoreDir) {
        this.keyStoreDir = keyStoreDir;
    }

    public String getAccountAddress() {
        return accountAddress;
    }

    public void setAccountAddress(String accountAddress) {
        this.accountAddress = accountAddress;
    }

    public String getAccountFileFormat() {
        return accountFileFormat;
    }

    public void setAccountFileFormat(String accountFileFormat) {
        this.accountFileFormat = accountFileFormat;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    @Override
    public String toString() {
        return "AccountConfig{"
                + "keyStoreDir='"
                + keyStoreDir
                + '\''
                + ", accountAddress='"
                + accountAddress
                + '\''
                + ", accountFileFormat='"
                + accountFileFormat
                + '\''
                + ", accountPassword='"
                + accountPassword
                + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountConfig that = (AccountConfig) o;
        return Objects.equals(keyStoreDir, that.keyStoreDir)
                && Objects.equals(accountAddress, that.accountAddress)
                && Objects.equals(accountFileFormat, that.accountFileFormat)
                && Objects.equals(accountPassword, that.accountPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyStoreDir, accountAddress, accountFileFormat, accountPassword);
    }

    public void clearAccount() {
        this.accountFilePath = "";
        this.accountAddress = "";
        this.accountPassword = "";
    }

    public boolean isAccountConfigured() {
        if (accountFilePath != null && !accountFilePath.equals("")) {
            return true;
        }
        if (accountAddress != null && !accountAddress.equals("")) {
            return true;
        }
        return false;
    }
}

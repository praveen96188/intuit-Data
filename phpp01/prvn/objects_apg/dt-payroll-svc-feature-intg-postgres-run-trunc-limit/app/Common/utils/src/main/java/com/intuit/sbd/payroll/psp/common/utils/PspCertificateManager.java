package com.intuit.sbd.payroll.psp.common.utils;


import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * @author Jeff Jones
 */
public class PspCertificateManager {

    private static String keyStorePath;
    private static KeyStore pspKeyStore;
    private static String keyStorePassword;

    private static final Logger logger = LoggerFactory.getLogger(PspCertificateManager.class);

    static {
        try {
            keyStorePath = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-keystore");
            keyStorePassword = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-keystorepassword");

            //Load global trustStore from file
            System.setProperty("javax.net.ssl.trustStore", ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-truststore"));
            System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);

            // set SSL properties for weblogic
            System.setProperty("weblogic.security.SSL.ignoreHostnameVerification", "true");
            System.setProperty("java.protocol.handler.pkgs", "weblogic.net");
            System.setProperty("weblogic.security.TrustKeyStore", "CustomTrust");
            System.setProperty("weblogic.security.CustomTrustKeyStoreFileName", System.getProperty("javax.net.ssl.trustStore"));
            System.setProperty("weblogic.security.CustomTrustKeyStorePassPhrase", keyStorePassword);
            System.setProperty("weblogic.security.CustomTrustKeyStoreType", "JKS");

            //Load keyStore from file
            pspKeyStore = KeyStore.getInstance("JKS");
            File keyStoreFile = new File(keyStorePath);
            pspKeyStore.load(new FileInputStream(keyStoreFile), keyStorePassword.toCharArray());
        } catch (Exception e) {
            logger.error("exception in getting configuration " +e);
            throw new RuntimeException(e);
        }
    }

    public static SSLSocketFactory getSSLSocketFactory(String pAlias) {
        return getSSLSocketFactory(pAlias, keyStorePassword);
    }

    public static SSLSocketFactory getSSLSocketFactory(String pAlias, String pCertificatePassword) {
        SSLContext sslContext = getSSLContext(pAlias, pCertificatePassword);
        return  sslContext.getSocketFactory();
    }

    public static SSLContext getSSLContext(String pAlias, String pCertificatePassword) {
        try {
            //Extract the IOP privateKey
            KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) pspKeyStore.getEntry
                    (pAlias, new KeyStore.PasswordProtection(pCertificatePassword.toCharArray()));

            //Create new in memory keyStore and add IOP privateKey to it
            KeyStore iopKeyStore = KeyStore.getInstance("JKS");
            iopKeyStore.load(null, keyStorePassword.toCharArray());
            iopKeyStore.setKeyEntry(pAlias, entry.getPrivateKey(), pCertificatePassword.toCharArray(), entry.getCertificateChain());
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(iopKeyStore, pCertificatePassword.toCharArray());

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyFactory.getKeyManagers(), null, new SecureRandom());

            return context;
        } catch (Exception e) {
            logger.warn("exception in getSSLContext() " +e);
            throw new RuntimeException(e);
        }
    }
}

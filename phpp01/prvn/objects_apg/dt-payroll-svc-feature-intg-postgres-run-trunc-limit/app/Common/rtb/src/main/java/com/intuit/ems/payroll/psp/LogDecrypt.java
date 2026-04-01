package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

public class LogDecrypt {

    private static final String KEY_NAME = "-key";
    private static final String CIPHER_TEXT = "-cipherText";

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    private String cipherText;
    private String keyName;

    public static void main(String[] args) {
        LogDecrypt logDecrypt = new LogDecrypt();
        logDecrypt.parseArgs(args);

        String decryptMsg = EncryptionUtils.probabilisticDecrypt(logDecrypt.getKeyName(),logDecrypt.getCipherText());

        System.out.println("Decrypted Message: " + decryptMsg);
    }

    private void parseArgs(String[] args) {

        final String usage = "LogDecrypt -key=<keyName> -cipherText=<cipherText>";

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(KEY_NAME)) {
                    keyName = argParts[1];
                    System.out.println("-key: " + keyName);
                } else if (argParts[0].equals(CIPHER_TEXT)) {
                    cipherText = argParts[1];
                    System.out.println("-cipherText: " + cipherText);
                }
            } else {
                System.out.println("ERROR: Invalid Arguments: Required" + usage);
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }

        if(keyName.isEmpty() || cipherText.isEmpty()){
            System.out.println("ERROR: Invalid Arguments: Required" + usage);
            System.exit(-1);
        }
    }
}

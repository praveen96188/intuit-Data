package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

/**
 * Created by rvl on Aug/28/2016.
 */
public class ServerConfigDetail {

    private String host = null;
    private int port = 0;
    private String user = null;
    private String password = null;
    private String privateKey = null;
    private String destDir = null;
    private int timeout = 0;
    private int maxRetries = 0;

    public ServerConfigDetail() {}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDestDir() {
        return destDir;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setDestDir(String destDir) {
        this.destDir = destDir;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}

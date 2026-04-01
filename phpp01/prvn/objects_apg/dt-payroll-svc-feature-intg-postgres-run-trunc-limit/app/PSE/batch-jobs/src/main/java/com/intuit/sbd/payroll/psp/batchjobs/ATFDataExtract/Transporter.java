package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import java.io.File;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

/**
 * @author not attributable
 *
 * Description:	This class is to send the data to the Remote system. This uses secure FTP to send the data.
 *
 */

public class Transporter {

    /**
     * Constructor for Transporter.
     */
    public Transporter() {
        super();
    }

    private static final String SFTP_PORT = "22";
    private static final String SFTP_CHANNEL_TYPE_VALUE = "sftp";

    private Session session = null;
    private String host = null;
    private String port = null;
    private String user = null;
    private String password = null;

    public Transporter(String host, String port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public Transporter(String host, String user, String password) {
        this(host, SFTP_PORT, user, password);
    }

    public void send(String sourcePath, String targetDirPath) throws Throwable {
        boolean returnValue = false;

        File sourceFile = new File(sourcePath);
        if (sourceFile.exists() == false) {
            throw new Exception("Failed " + sourcePath + " does not exist");
        }

        Channel channel = this.openChannel();
        if (channel != null) {
            ChannelSftp channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.put(sourcePath, targetDirPath,
                                ChannelSftp.OVERWRITE);
                returnValue = true;
            } catch (SftpException sftpException) {
                sftpException.printStackTrace();
                throw sftpException;
            }

            this.closeChannel(channel);
        }

        // disconnect the session
        disconnect();
    }

    // To Rename the file
    public void rename(String oldPath, String newPath) throws Throwable {
        String sourceDirPath = ".";
        int lastSeparatorIndex = oldPath.lastIndexOf("/");
        if (lastSeparatorIndex > -1) {
            sourceDirPath = oldPath.substring(0, lastSeparatorIndex);
        }

        if (newPath.indexOf("/") == -1) {
            newPath = sourceDirPath + "/" + newPath;
        }

        if (oldPath.equals(newPath) == false) {
            Channel channel = this.openChannel();
            if (channel != null) {
                ChannelSftp channelSftp = (ChannelSftp) channel;

                try {
                    if (channelSftp.ls(newPath).isEmpty() == false) {
                        channelSftp.rm(newPath);
                    }
                    channelSftp.rename(oldPath, newPath);
                } catch (SftpException sftpException) {
                    throw sftpException;
                }

                this.closeChannel(channel);
            }
        }

        // disconnect the session
        disconnect();
    }


	// To Remove file
	public void remove(String pFilePath) throws Throwable {
		
		Channel channel = openChannel();
		ChannelSftp channelSftp = (ChannelSftp) channel;
		
		if (!channelSftp.ls(pFilePath).isEmpty()) {
				channelSftp.rm(pFilePath);
		}
		
	}

    public void connect() throws Throwable {
        Session session = this.getSession();
        if (session != null || session.isConnected() == false) {
            session.connect();
        }
    }

    public void disconnect() throws Throwable {
        Session session = this.getSession();
        if (session != null && session.isConnected() == true) {
            session.disconnect();
        }
    }

    protected Session getSession() throws Throwable {
        if (this.session == null) {
            JSch jSch = new JSch();

            String host = this.getHost();
            String port = this.getPort();
            String user = this.getUser();
            String password = this.getPassword();

            int portInt = Integer.parseInt(port);

            try {
                this.session = jSch.getSession(user, host, portInt);
            } catch (JSchException jSchException) {
                throw jSchException;
            }

            UserInfo userInfo = new SFTPUserInfo(password);
            this.session.setUserInfo(userInfo);
            this.session.setPassword(password);
        }

        return this.session;
    }

    protected Channel openChannel() throws Throwable {
        Channel channel = null;

        Session session = this.getSession();
        if (session != null) {
            if (session.isConnected() == false) {
                try {
                    session.connect();
                } catch (JSchException jSchException) {
                    throw jSchException;
                }
            }

            if (session.isConnected() == true) {
                try {
                    channel = session.openChannel(SFTP_CHANNEL_TYPE_VALUE);
                    channel.connect();
                } catch (JSchException jSchException) {
                    throw jSchException;
                }
            }
        }

        return channel;
    }

    protected void closeChannel(Channel channel) throws Throwable {
        if (channel != null) {
            channel.disconnect();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected class SFTPUserInfo implements UserInfo {

        private String password = null;

        public SFTPUserInfo(String password) {
            this.password = password;
        }

        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return this.password;
        }

        public boolean promptPassword(String message) {
            return false;
        }

        public boolean promptPassphrase(String message) {
            return false;
        }

        public boolean promptYesNo(String message) {
            return true;
        }

        public void showMessage(String message) {
        }
    }

}
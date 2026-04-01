package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.jscape.inet.sftp.Sftp;
import com.jscape.inet.sftp.SftpException;
import com.jscape.inet.sftp.SftpFile;
import com.jscape.inet.sftp.events.SftpAdapter;
import com.jscape.inet.sftp.events.SftpChangeDirEvent;
import com.jscape.inet.sftp.events.SftpConnectedEvent;
import com.jscape.inet.sftp.events.SftpCreateDirEvent;
import com.jscape.inet.sftp.events.SftpDeleteDirEvent;
import com.jscape.inet.sftp.events.SftpDeleteFileEvent;
import com.jscape.inet.sftp.events.SftpDisconnectedEvent;
import com.jscape.inet.sftp.events.SftpDownloadEvent;
import com.jscape.inet.sftp.events.SftpListener;
import com.jscape.inet.sftp.events.SftpListingEvent;
import com.jscape.inet.sftp.events.SftpProgressEvent;
import com.jscape.inet.sftp.events.SftpRenameFileEvent;
import com.jscape.inet.sftp.events.SftpUploadEvent;
import com.jscape.inet.ssh.util.SshParameters;

import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 13, 2009
 * Time: 4:26:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleSftpFile {
    protected final SimpleSftpFileEventListener mEventListener = new SimpleSftpFileEventListener();

    protected SpcfLogger mLogger = null;
    protected Sftp mSftp = null;

    /**
     * Construct a new SimpleSftpFile object using the specified host, user name and password.
     *
     * @param pHost
     * @param pUsername
     * @param pPassword
     */
    public SimpleSftpFile(String pHost, String pUsername, String pPassword) {
        mSftp = new Sftp(new SshParameters(pHost, pUsername, pPassword));
        addListener(mEventListener);
    }

    /**
     * Construct a new SimpleSftpFile object using the specified host, user name and private key file
     *
     * @param pHost
     * @param pUsername
     * @param pPrivateKeyFile
     */
    public SimpleSftpFile(String pHost, String pUsername, File pPrivateKeyFile) {
        mSftp = new Sftp(new SshParameters(pHost, pUsername, pPrivateKeyFile));
    }

    /**
     * Set the logger to which you want SFTP session events and errors logged.  If no logger is specified, the default
     * class logger will be used.
     *
     * @param pLogger
     */
    public void setLogger(SpcfLogger pLogger) {
        mLogger = pLogger;
    }

    protected SpcfLogger getLogger() {
        if (mLogger == null) {
            mLogger = Application.getLogger(this.getClass());
        }

        return mLogger;
    }

    /**
     * Register the SFTP events for which you want log information. By default, no events are logged.
     * <br>
     * For example, if you want connect and disconnect events logged to your log file, call this method as follows:
     * <br>
     * logEvents(SftpLogEvent.CONNECT, SftpLogEvent.DISCONNECT);
     * <br>
     * This will cause all SFTP session connect and disconnect events to be logged to your log file.
     * All other events will be ignored for logging purposes.
     * <br>
     * The following events may be enabled/disabled for logging:
     * <br>
     * - CONNECT        log sftp connect events
     * - DISCONNECT     log sftp disconnect events
     * - DOWNLOAD       log sftp (successful) file download events
     * - UPLOAD         log sftp (successful) file upload events
     * - PROGRESS       log sftp file upload/download progress events
     * - DIRLISTING     log sftp directory listing events
     * - DELETEDIR      log sftp directory delete events
     * - DELETEFILE     log sftp file delete events
     * - RENAMEFILE     log sftp file rename events
     * - CREATEDIR      log sftp directory create events
     * - CHANGEDIR      log sftp directory change events
     * <br>
     *
     * @param pEvents
     */
    public void setLogEvents(SftpLogEvent... pEvents) {
        mEventListener.setLogEvents(pEvents);
    }

    /**
     * Add a specific log event to the list of active log events.
     *
     * @param pEvent The event you wish to add.
     */
    public void addLogEvent(SftpLogEvent pEvent) {
        mEventListener.addLogEvent(pEvent);
    }

    /**
     * Remove a specific log event from the list of active log events.
     *
     * @param pEvent The event you wish to remove.
     */
    public void removeLogEvent(SftpLogEvent pEvent) {
        mEventListener.removeLogEvent(pEvent);
    }

    /**
     * A convenience method to add all log events to list of active log events.
     */
    public void setDebugMode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String debugFileName = "sftpdebug-" + sdf.format(new Date()) + ".log";
        File sftpDebugFile = new File(System.getProperty("user.home", "."), debugFileName);

        try {
            // if we fail to set the debug stream to our debug file, it defaults to stdout
            mSftp.setDebugStream(new PrintStream(sftpDebugFile));
        } catch (Exception e) {
            getLogger().warn("Error setting SFTP debug stream to file, using stdout.", e);
        } finally {
            mSftp.setDebug(true);
            mEventListener.setDebugMode();
        }
    }

    /**
     * Set the host url and port to use for proxying this sftp connection.
     *
     * @param pProxyHostname The proxy host url.
     * @param pProxyPort     The proxy host port.
     */
    public void setProxyHost(String pProxyHostname, int pProxyPort) {
        mSftp.setProxyHost(pProxyHostname, pProxyPort);
    }

    /**
     * Set the proxy host type.
     *
     * @param pProxyType The proxy type (valid values are HTTP and SOCKS5).
     */
    public void setProxyType(String pProxyType) {
        mSftp.setProxyType(pProxyType);
    }

    /**
     * Set the username and password for the proxy host (optional).
     *
     * @param pProxyUsername The proxy host username.
     * @param pProxyPassword The proxy host password.
     */
    public void setProxyAuthentication(String pProxyUsername, String pProxyPassword) {
        mSftp.setProxyAuthentication(pProxyUsername, pProxyPassword);
    }

    /**
     * Clear all proxy settings.
     * <p/>
     * Note that the JScape Sftp class stores some proxy settings statically.
     * (the details of just what is stored statically is unknown since their code base is proprietary)
     */
    public void clearProxySettings() {
        Sftp.clearProxySettings();
    }

    /**
     * Set the amount of time to wait for a connection to the remote SFTP host before giving up.
     *
     * @param pTimeoutMillis The amount of time to wait in milliseconds.
     */
    public void setConnectionTimeout(int pTimeoutMillis) {
        mSftp.setTimeout(pTimeoutMillis);
    }

    public void setAscii() {
        mSftp.setAscii();
    }

    public void setBinary() {
        mSftp.setBinary();
    }

    /**
     * Determines if there is an active connection to the remote SFTP host.
     *
     * @return True if there is an active connection.
     */
    public boolean isConnected() {
        return mSftp.isConnected();
    }

    /**
     * Add an SftpListener to the session. Listener's are used to listen for SFTP events during an SFTP session.
     * <br>
     * The following events are reported:
     * <br>
     * - CONNECT        listen for sftp connect events
     * - DISCONNECT     listen for sftp disconnect events
     * - DOWNLOAD       listen for sftp (successful) file download events
     * - UPLOAD         listen for sftp (successful) file upload events
     * - PROGRESS       listen for sftp file upload/download progress events
     * - DIRLISTING     listen for sftp directory listing events
     * - DELETEDIR      listen for sftp directory delete events
     * - DELETEFILE     listen for sftp file delete events
     * - RENAMEFILE     listen for sftp file rename events
     * - CREATEDIR      listen for sftp directory create events
     * - CHANGEDIR      listen for sftp directory change events
     * <br>
     * See logEvents() for information on enabling or disabling the default logging events.
     * <br>
     *
     * @param pListener A Class that extends SftpAdapter or implements the SftpListener interface.
     */
    public void addListener(SftpListener pListener) {
        if (pListener != null) {
            mSftp.addSftpListener(pListener);
        }
    }

    /**
     * Connect to the remote SFTP host.
     */
    public void connect() {
        disconnect();

        try {
            mSftp.connect();
        } catch (SftpException e) {
            throw new RuntimeException("Failed to connect to SFTP host. ", e);
        }
    }

    /**
     * Disconnect from the remote SFTP host.
     */
    public void disconnect() {
        if (isConnected()) {
            mSftp.disconnect();
        }
    }

    /**
     * Change the current directory on the local system.
     *
     * @param pDir
     */
    public void changeLocalDir(String pDir) {
        if (pDir != null) {
            mSftp.setLocalDir(new File(pDir));
        }
    }

    /**
     * Change the current directory on the remote host.
     *
     * @param pDir
     */
    public void changeRemoteDir(String pDir) {
        if (pDir != null) {
            try {
                mSftp.setDir(pDir);
            } catch (SftpException e) {
                throw new RuntimeException("Error changing remote directory on host: " + pDir, e);
            }
        }
    }

    /**
     * Uploads the given file to the remote host. Uploads are performed relative to the current local directory.
     *
     * @param pFile
     */
    public void uploadFile(File pFile) {
        if (pFile != null) {
            try {
                mSftp.upload(pFile);
            } catch (SftpException e) {
                throw new RuntimeException("Error uploading file to host: " + pFile, e);
            }
        }
    }

    /**
     * Uploads the given file to the remote host. Uploads are performed relative to the current local directory.
     *
     * @param pFile
     */
    public void uploadFile(String pFile) {
        if (pFile != null) {
            try {
                mSftp.upload(pFile);
            } catch (SftpException e) {
                throw new RuntimeException("Error uploading file to host=" + pFile, e);
            }
        }
    }

    /**
     * Uploads a given file to the remote host. Uploads are performed relative to the current local directory.
     *
     * @param pLocalFile
     * @param pRemoteFile
     */
    public void uploadFile(String pLocalFile, String pRemoteFile) {
        if (pRemoteFile == null) {
            uploadFile(pLocalFile);
        }
        if (pLocalFile != null) {
            try {
                mSftp.upload(pLocalFile, pRemoteFile);
            } catch (SftpException e) {
                throw new RuntimeException("Error uploading file to host=" + pLocalFile + " remote file=" + pRemoteFile, e);
            }
        }
    }

    /**
     * Uploads a given file to the remote host. Uploads are performed relative to the current local directory.
     *
     * @param pLocalFile
     * @param pRemoteFile
     */
    public void uploadFile(File pLocalFile, String pRemoteFile) {
        if (pRemoteFile == null) {
            uploadFile(pLocalFile);
        }
        if (pLocalFile != null) {
            try {
                mSftp.upload(pLocalFile, pRemoteFile);
            } catch (SftpException e) {
                throw new RuntimeException("Error uploading file to host= " + pLocalFile + " remote file= " + pRemoteFile, e);
            }
        }
    }

    /**
     * Downloads the given file from the remote host.
     *
     * @param pFile
     * @return A File object representing the downloaded file.
     */
    public File downloadFile(String pFile) {
        File result = null;

        if (pFile != null) {
            try {
                result = mSftp.download(pFile);
            } catch (SftpException e) {
                throw new RuntimeException("Error downloading file from host= " + pFile, e);
            }
        }

        return result;
    }

    /**
     * Uploads all of the files in the list to the remote host.
     *
     * @param pFileList A Collection<String> of files to upload.
     */
    public void uploadFiles(Collection<String> pFileList) {
        uploadFiles(pFileList, 0);
    }

    /**
     * Downloads all of the files in the list from the remote host.
     *
     * @param pFileList A Collection<String> of files to download.
     * @return A Collection<String> representing the files downloaded from the remote host.
     */
    public Collection<File> downloadFiles(Collection<String> pFileList) {
        return downloadFiles(pFileList, 0);
    }

    /**
     * Uploads all of the files in the list to the remote host.
     *
     * @param pFileList                A Collection<String> of files to upload.
     * @param pMillisDelayBetweenFiles Millisecond delay between files uploaded to the remote host.
     */
    public void uploadFiles(Collection<String> pFileList, int pMillisDelayBetweenFiles) {
        if (pFileList != null) {
            if (pMillisDelayBetweenFiles < 0) {
                pMillisDelayBetweenFiles = 0;
            }

            for (String file : pFileList) {
                uploadFile(file);

                if (pMillisDelayBetweenFiles > 0) {
                    try {
                        Thread.sleep(pMillisDelayBetweenFiles);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("File upload delay interrupted. ", e);
                    }
                }
            }
        }
    }

    /**
     * Downloads all of the files in the list from the remote host.
     *
     * @param pFileList                A Collection<String> of files to download.
     * @param pMillisDelayBetweenFiles Millisecond delay between files downloaded from the remote host.
     * @return A Collection<String> representing the files downloaded from the remote host.
     */
    public Collection<File> downloadFiles(Collection<String> pFileList, int pMillisDelayBetweenFiles) {
        Collection<File> files = new Vector<File>();

        if (pFileList != null) {
            if (pMillisDelayBetweenFiles < 0) {
                pMillisDelayBetweenFiles = 0;
            }

            for (String file : pFileList) {
                File dFile = downloadFile(file);

                if (dFile != null) {
                    files.add(dFile);
                }

                if (pMillisDelayBetweenFiles > 0) {
                    try {
                        Thread.sleep(pMillisDelayBetweenFiles);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("File download delay interrupted. ", e);
                    }
                }
            }
        }

        return files;
    }

    /**
     * Retrieves the current working directory on the remote host.
     *
     * @return
     */
    public String getRemoteDir() {
        return mSftp.getDir();
    }

    /**
     * Retrieves a directory listing of all files (in the current directory) on the remote host.
     *
     * @return An Enumeration of SftpFile objects representing files on the remote host.
     */
    public Enumeration getRemoteDirListing() {
        Enumeration listing = null;

        try {
            listing = mSftp.getDirListing();
        } catch (SftpException e) {
            throw new RuntimeException("Error retrieving directory listing from host. ", e);
        }

        return listing;
    }

    /**
     * Retrieves a directory listing of all files (in the current directory) matching the given regular expression on
     * the remote host.
     *
     * @param pRegex Example: To list all files with extension .java or .class the regex whould be ".+\\.(?:java|class)"
     * @return An Enumeration of SftpFile objects representing files on the remote host matching the regular expression.
     */
    public Enumeration getRemoteDirListing(String pRegex) {
        Enumeration listing = null;

        try {
            listing = mSftp.getDirListing(pRegex);
        } catch (SftpException e) {
            throw new RuntimeException("Error retrieving directory listing from host (" + Matcher.quoteReplacement(pRegex) + ")", e);
        }

        return listing;
    }

    /**
     * Deletes a directory on the remote host.
     *
     * @param pDir
     */
    public void deleteRemoteDir(String pDir) {
        try {
            mSftp.deleteDir(pDir);
        } catch (SftpException e) {
            throw new RuntimeException("Error deleting directory on host: " + pDir, e);
        }
    }

    /**
     * Deletes a directory on the remote host, recursing as necessary.
     *
     * @param pDir
     * @param pRecurse If true, recursively deletes sub-directories as necessary.
     */
    public void deleteRemoteDir(String pDir, boolean pRecurse) {
        try {
            mSftp.deleteDir(pDir, pRecurse);
        } catch (SftpException e) {
            throw new RuntimeException("Error deleting directory on host: " + pDir, e);
        }
    }

    /**
     * Deletes a file on the remote host.
     *
     * @param pFile
     */
    public void deleteRemoteFile(String pFile) {
        try {
            mSftp.deleteFile(pFile);
        } catch (SftpException e) {
            throw new RuntimeException("Error deleting file on host: " + pFile, e);
        }
    }

    /**
     * Renames a file on the remote host.
     *
     * @param pOldFile
     * @param pNewFile
     */
    public void renameRemoteFile(String pOldFile, String pNewFile) {
        try {
            mSftp.renameFile(pOldFile, pNewFile);
        } catch (SftpException e) {
            throw new RuntimeException("Error renaming file on host (from: " + pOldFile + ", to: " + pNewFile + ")", e);
        }
    }

    /**
     * Creates a directory on the remote host.
     *
     * @param pDir
     */
    public void createRemoteDir(String pDir) {
        try {
            mSftp.makeDir(pDir);
        } catch (SftpException e) {
            throw new RuntimeException("Error creating directory on host: " + pDir, e);
        }
    }

    /**
     * Creates a directory on the remote host, creating any subdirectories along the way.
     *
     * @param pDir
     */
    public void createRemoteDirs(String pDir) {
        try {
            mSftp.makeDirRecursive(pDir);
        } catch (SftpException e) {
            throw new RuntimeException("Error creating directory on host: " + pDir, e);
        }
    }

    /**
     * Creates a directory on the local system.
     *
     * @param pDir
     */
    public void createLocalDir(String pDir) {
        mSftp.makeLocalDir(pDir);
    }

    /**
     * Checks to see if the specified dir is a valid directory on the remote host.
     *
     * @param pDir
     * @return
     */
    public boolean isValidRemoteDir(String pDir) {
        try {
            return mSftp.isDirectory(pDir);
        } catch (SftpException e) {
            throw new RuntimeException("Error validating directory on host: " + pDir, e);
        }
    }

    /**
     * Checks to see if the specified path is a valid directory or file on the remote host. Note: You must specify
     * the fully qualified path (i.e. does not assume current working directory on remote host.)
     *
     * @param pPath The fully qualified path name to check.
     * @return
     */
    public boolean isValidRemotePath(String pPath) {
        try {
            return mSftp.isValidPath(pPath);
        } catch (SftpException e) {
            throw new RuntimeException("Error validating path on host: " + pPath, e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following are for SFTP session event logging
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Use these enumerations values to determine which sftp events to log.
     */
    public enum SftpLogEvent {
        CONNECT,    // log sftp connect events
        DISCONNECT, // log sftp disconnect events
        DOWNLOAD,   // log sftp (successful) file download events
        UPLOAD,     // log sftp (successful) file upload events
        PROGRESS,   // log sftp file upload/download progress events
        DIRLISTING, // log sftp directory listing events
        DELETEDIR,  // log sftp directory delete events
        DELETEFILE, // log sftp file delete events
        RENAMEFILE, // log sftp file rename events
        CREATEDIR,  // log sftp directory create events
        CHANGEDIR   // log sftp directory change events
    }

    /**
     * This is the default listener class to facilitate logging of SFTP session events.
     */
    private class SimpleSftpFileEventListener extends SftpAdapter {
        private final String OFFSET = ">  ";
        private final String NEWLINE = System.getProperty("line.separator");
        private final BigDecimal bd100 = new BigDecimal(String.valueOf(100));
        private final List<SftpLogEvent> mEventList = new Vector<SftpLogEvent>();

        /**
         * Add bulk log events to the list of active log events. Clears the list prior to setting the new events to log.
         *
         * @param pEvents
         */
        public void setLogEvents(SftpLogEvent... pEvents) {
            mEventList.clear();

            if (pEvents != null) {
                mEventList.addAll(Arrays.asList(pEvents));
            }
        }

        /**
         * Add a specific log event to the list of active log events.
         *
         * @param pEvent The event you wish to add.
         */
        public void addLogEvent(SftpLogEvent pEvent) {
            if ((pEvent != null) && !mEventList.contains(pEvent)) {
                mEventList.add(pEvent);
            }
        }

        /**
         * Remove a specific log event from the list of active log events.
         *
         * @param pEvent The event you wish to remove.
         */
        public void removeLogEvent(SftpLogEvent pEvent) {
            if ((pEvent != null) && mEventList.contains(pEvent)) {
                mEventList.remove(pEvent);
            }
        }

        /**
         * A convenience method to add all log events to list of active log events.
         */
        public void setDebugMode() {
            setLogEvents(SftpLogEvent.values());
        }

        /**
         * Log connect events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.CONNECT) to enable or removeLogEvent(SftpLogEvent.CONNECT) to disable.
         * <br>
         *
         * @param event
         */
        public void connected(SftpConnectedEvent event) {
            if (!mEventList.contains(SftpLogEvent.CONNECT)) {
                return;
            }

            getLogger().info("Connection established with SFTP host: " + event.getHostname());
        }

        /**
         * Log disconnect events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.DISCONNECT) to enable or removeLogEvent(SftpLogEvent.DISCONNECT) to disable.
         * <br>
         *
         * @param event
         */
        public void disconnected(SftpDisconnectedEvent event) {
            if (!mEventList.contains(SftpLogEvent.DISCONNECT)) {
                return;
            }

            getLogger().info("Disconnected from SFTP host: " + event.getHostname());
        }

        /**
         * Log successful file download events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.DOWNLOAD) to enable or removeLogEvent(SftpLogEvent.DOWNLOAD) to disable.
         * <br>
         *
         * @param event
         */
        public void download(SftpDownloadEvent event) {
            if (!mEventList.contains(SftpLogEvent.DOWNLOAD)) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Successfully downloaded file from SFTP host:").append(NEWLINE);
            buf.append(OFFSET).append("File Location: ").append(event.getPath()).append(NEWLINE);
            buf.append(OFFSET).append("File Name:     ").append(event.getFilename()).append(NEWLINE);
            buf.append(OFFSET).append("File Size:     ").append(event.getFilesize()).append(NEWLINE);
            buf.append(OFFSET).append("Download Time: ").append(event.getTime());

            getLogger().info(buf.toString());
        }

        /**
         * Log successful file upload events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.UPLOAD) to enable or removeLogEvent(SftpLogEvent.UPLOAD) to disable.
         * <br>
         *
         * @param event
         */
        public void upload(SftpUploadEvent event) {
            if (!mEventList.contains(SftpLogEvent.UPLOAD)) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Successfully uploaded file to SFTP host:").append(NEWLINE);
            buf.append(OFFSET).append("File Location: ").append(event.getPath()).append(NEWLINE);
            buf.append(OFFSET).append("File Name:     ").append(event.getFilename()).append(NEWLINE);
            buf.append(OFFSET).append("File Size:     ").append(event.getFilesize()).append(NEWLINE);
            buf.append(OFFSET).append("Upload Time:   ").append(event.getTime());

            getLogger().info(buf.toString());
        }

        /**
         * Log file transfer (upload/download) progress events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.PROGRESS) to enable or removeLogEvent(SftpLogEvent.PROGRESS) to disable.
         * <br>
         *
         * @param event
         */
        public void progress(SftpProgressEvent event) {
            if (!mEventList.contains(SftpLogEvent.PROGRESS)) {
                return;
            }

            // calculate the percent complete of the file transfer
            BigDecimal bdDividend = new BigDecimal(String.valueOf(event.getBytes()));
            BigDecimal bdDivisor = new BigDecimal(String.valueOf(event.getTotalBytes()));
            BigDecimal result = bdDividend.divide(bdDivisor, 3, RoundingMode.FLOOR).multiply(bd100);
            String percentComplete = result.toBigInteger().toString() + "%";

            StringBuffer buf = new StringBuffer();

            buf.append(event.getMode() == SftpProgressEvent.UPLOAD ? "Uploading: " : "Downloading: ");
            buf.append(event.getFilename());
            buf.append(" [ ");
            buf.append(event.getBytes());
            buf.append(" of ");
            buf.append(event.getTotalBytes());
            buf.append(" bytes (");
            buf.append(percentComplete);
            buf.append(") ]");

            getLogger().info(buf.toString());
        }

        /**
         * Log directory listing events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.DIRLISTING) to enable or removeLogEvent(SftpLogEvent.DIRLISTING) to disable.
         * <br>
         *
         * @param event
         */
        public void dirListing(SftpListingEvent event) {
            if (!mEventList.contains(SftpLogEvent.DIRLISTING)) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Directory listing on server:").append(NEWLINE);

            Enumeration fileList = event.getFiles();

            if (!fileList.hasMoreElements()) {
                buf.append(OFFSET).append("[ no files found ]").append(NEWLINE);
            } else {
                while (fileList.hasMoreElements()) {
                    SftpFile file = (SftpFile) fileList.nextElement();

                    try {
                        buf.append(OFFSET);
                        buf.append(file.getFilename());

                        if (file.isDirectory()) {
                            buf.append(" <dir>");
                        } else {
                            buf.append(" (");
                            buf.append(file.getFilesize());
                            buf.append(" bytes)");
                        }

                        buf.append(NEWLINE);
                    } catch (SftpException e) {
                        buf.append(OFFSET).append("<error> ").append(e.getMessage()).append(NEWLINE);
                    }
                }
            }

            getLogger().info(buf.toString().trim());
        }

        /**
         * Log directory deletion events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.DELETEDIR) to enable or removeLogEvent(SftpLogEvent.DELETEDIR) to disable.
         * <br>
         *
         * @param event
         */
        public void deleteDir(SftpDeleteDirEvent event) {
            if (!mEventList.contains(SftpLogEvent.DELETEDIR)) {
                return;
            }

            getLogger().info("Deleted directory on server: " + event.getDirectory() + " (parent directory was: " + event.getPath() + ")");
        }

        /**
         * Log file deletion events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.DELETEFILE) to enable or removeLogEvent(SftpLogEvent.DELETEFILE) to disable.
         * <br>
         *
         * @param event
         */
        public void deleteFile(SftpDeleteFileEvent event) {
            if (!mEventList.contains(SftpLogEvent.DELETEFILE)) {
                return;
            }

            String dir = event.getPath().replaceAll("\\\\", "/");

            if (!dir.endsWith("/")) {
                dir += "/";
            }

            getLogger().info("Deleted file on server: " + dir + event.getFile());
        }

        /**
         * Log file rename events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.RENAMEFILE) to enable or removeLogEvent(SftpLogEvent.RENAMEFILE) to disable.
         * <br>
         *
         * @param event
         */
        public void renameFile(SftpRenameFileEvent event) {
            if (!mEventList.contains(SftpLogEvent.RENAMEFILE)) {
                return;
            }

            String dir = event.getPath().replaceAll("\\\\", "/");

            if (!dir.endsWith("/")) {
                dir += "/";
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Renamed file on server:").append(NEWLINE);
            buf.append(OFFSET).append("From: ").append(dir).append(event.getOldFile()).append(NEWLINE);
            buf.append(OFFSET).append("To:   ").append(dir).append(event.getNewFile());

            getLogger().info(buf.toString());
        }

        /**
         * Log directory creation events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.CREATEDIR) to enable or removeLogEvent(SftpLogEvent.CREATEDIR) to disable.
         * <br>
         *
         * @param event
         */
        public void createDir(SftpCreateDirEvent event) {
            if (!mEventList.contains(SftpLogEvent.CREATEDIR)) {
                return;
            }

            String dir = event.getPath().replaceAll("\\\\", "/");

            if (!dir.endsWith("/")) {
                dir += "/";
            }

            getLogger().info("Created directory on server: " + dir + event.getDirectory());
        }

        /**
         * Log directory change events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.CHANGEDIR) to enable or removeLogEvent(SftpLogEvent.CHANGEDIR) to disable.
         * <br>
         *
         * @param event
         */
        public void changeDir(SftpChangeDirEvent event) {
            if (!mEventList.contains(SftpLogEvent.CHANGEDIR)) {
                return;
            }

            getLogger().info("Changed directory on server to: " + event.getDirectory());
        }
    }
}

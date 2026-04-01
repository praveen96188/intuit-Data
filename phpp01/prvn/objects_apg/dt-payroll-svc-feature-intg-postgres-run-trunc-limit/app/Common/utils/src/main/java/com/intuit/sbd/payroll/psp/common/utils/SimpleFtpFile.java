package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.jscape.inet.ftp.Ftp;
import com.jscape.inet.ftp.FtpAdapter;
import com.jscape.inet.ftp.FtpChangeDirEvent;
import com.jscape.inet.ftp.FtpConnectedEvent;
import com.jscape.inet.ftp.FtpCreateDirEvent;
import com.jscape.inet.ftp.FtpDeleteDirEvent;
import com.jscape.inet.ftp.FtpDeleteFileEvent;
import com.jscape.inet.ftp.FtpDisconnectedEvent;
import com.jscape.inet.ftp.FtpDownloadEvent;
import com.jscape.inet.ftp.FtpException;
import com.jscape.inet.ftp.FtpFile;
import com.jscape.inet.ftp.FtpListener;
import com.jscape.inet.ftp.FtpListingEvent;
import com.jscape.inet.ftp.FtpProgressEvent;
import com.jscape.inet.ftp.FtpRenameFileEvent;
import com.jscape.inet.ftp.FtpUploadEvent;

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
 * User: svenkata
 * Date: Dec 9, 2010
 * Time: 11:23:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleFtpFile {
    protected final SimpleFtpFileEventListener mEventListener = new SimpleFtpFileEventListener();

    protected SpcfLogger mLogger = null;
    protected Ftp mFtp = null;

    /**
     * Construct a new SimpleFtpFile object using the specified host, user name and password.
     *
     * @param pHost
     * @param pUsername
     * @param pPassword
     */
    public SimpleFtpFile(String pHost, String pUsername, String pPassword) {
        mFtp = new Ftp(pHost, pUsername, pPassword);
        addListener(mEventListener);
    }

    /**
     * Set the logger to which you want FTP session events and errors logged.  If no logger is specified, the default
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
     * Register the FTP events for which you want log information. By default, no events are logged.
     * <br>
     * For example, if you want connect and disconnect events logged to your log file, call this method as follows:
     * <br>
     * logEvents(FtpLogEvent.CONNECT, FtpLogEvent.DISCONNECT);
     * <br>
     * This will cause all FTP session connect and disconnect events to be logged to your log file.
     * All other events will be ignored for logging purposes.
     * <br>
     * The following events may be enabled/disabled for logging:
     * <br>
     * - CONNECT        log ftp connect events
     * - DISCONNECT     log ftp disconnect events
     * - DOWNLOAD       log ftp (successful) file download events
     * - UPLOAD         log ftp (successful) file upload events
     * - PROGRESS       log ftp file upload/download progress events
     * - DIRLISTING     log ftp directory listing events
     * - DELETEDIR      log ftp directory delete events
     * - DELETEFILE     log ftp file delete events
     * - RENAMEFILE     log ftp file rename events
     * - CREATEDIR      log ftp directory create events
     * - CHANGEDIR      log ftp directory change events
     * <br>
     *
     * @param pEvents
     */
    public void setLogEvents(FtpLogEvent... pEvents) {
        mEventListener.setLogEvents(pEvents);
    }

    /**
     * Add a specific log event to the list of active log events.
     *
     * @param pEvent The event you wish to add.
     */
    public void addLogEvent(FtpLogEvent pEvent) {
        mEventListener.addLogEvent(pEvent);
    }

    /**
     * Remove a specific log event from the list of active log events.
     *
     * @param pEvent The event you wish to remove.
     */
    public void removeLogEvent(FtpLogEvent pEvent) {
        mEventListener.removeLogEvent(pEvent);
    }

    /**
     * A convenience method to add all log events to list of active log events.
     */
    public void setDebugMode() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String debugFileName = "ftpdebug-" + sdf.format(new Date()) + ".log";
        File ftpDebugFile = new File(System.getProperty("user.home", "."), debugFileName);

        try {
            // if we fail to set the debug stream to our debug file, it defaults to stdout
            mFtp.setDebugStream(new PrintStream(ftpDebugFile));
        } catch (Exception e) {
            getLogger().warn("Error setting FTP debug stream to file, using stdout.", e);
        } finally {
            mFtp.setDebug(true);
            mEventListener.setDebugMode();
        }
    }

    /**
     * Set the host url and port to use for proxying this ftp connection.
     *
     * @param pProxyHostname The proxy host url.
     * @param pProxyPort     The proxy host port.
     */
    public void setProxyHost(String pProxyHostname, int pProxyPort) {
        mFtp.setProxyHost(pProxyHostname, pProxyPort);
    }

    /**
     * Set the proxy host type.
     *
     * @param pProxyType The proxy type (valid values are HTTP and SOCKS5).
     */
    public void setProxyType(String pProxyType) {
        mFtp.setProxyType(pProxyType);
    }

    /**
     * Set the username and password for the proxy host (optional).
     *
     * @param pProxyUsername The proxy host username.
     * @param pProxyPassword The proxy host password.
     */
    public void setProxyAuthentication(String pProxyUsername, String pProxyPassword) {
        mFtp.setProxyAuthentication(pProxyUsername, pProxyPassword);
    }

    /**
     * Clear all proxy settings.
     * <p/>
     * Note that the JScape Ftp class stores some proxy settings statically.
     * (the details of just what is stored statically is unknown since their code base is proprietary)
     */
    public void clearProxySettings() {
        mFtp.clearProxySettings();
    }

    /**
     * Set the amount of time to wait for a connection to the remote FTP host before giving up.
     *
     * @param pTimeoutMillis The amount of time to wait in milliseconds.
     */
    public void setConnectionTimeout(int pTimeoutMillis) {
        mFtp.setTimeout(pTimeoutMillis);
    }

    /**
     * Determines if there is an active connection to the remote FTP host.
     *
     * @return True if there is an active connection.
     */
    public boolean isConnected() {
        return mFtp.isConnected();
    }

    /**
     * Add an FtpListener to the session. Listener's are used to listen for FTP events during an FTP session.
     * <br>
     * The following events are reported:
     * <br>
     * - CONNECT        listen for ftp connect events
     * - DISCONNECT     listen for ftp disconnect events
     * - DOWNLOAD       listen for ftp (successful) file download events
     * - UPLOAD         listen for ftp (successful) file upload events
     * - PROGRESS       listen for ftp file upload/download progress events
     * - DIRLISTING     listen for ftp directory listing events
     * - DELETEDIR      listen for ftp directory delete events
     * - DELETEFILE     listen for ftp file delete events
     * - RENAMEFILE     listen for ftp file rename events
     * - CREATEDIR      listen for ftp directory create events
     * - CHANGEDIR      listen for ftp directory change events
     * <br>
     * See logEvents() for information on enabling or disabling the default logging events.
     * <br>
     *
     * @param pListener A Class that extends FtpAdapter or implements the FtpListener interface.
     */
    public void addListener(FtpListener pListener) {
        if (pListener != null) {
            mFtp.addFtpListener(pListener);
        }
    }

    public int getPort() {
        return mFtp.getPort();
    }

    public void setPort(int pPort) {
        mFtp.setPort(pPort);
    }

    /**
     * Connect to the remote FTP host.
     */
    public void connect() {
        disconnect();
        try {
            mFtp.connect();
        } catch (FtpException e) {
            throw new RuntimeException("Failed to connect to FTP host. ", e);
        }
    }

    /**
     * Disconnect from the remote FTP host.
     */
    public void disconnect() {
        if (isConnected()) {
            mFtp.disconnect();
        }
    }

    /**
     * Change the current directory on the local system.
     *
     * @param pDir
     */
    public void changeLocalDir(String pDir) {
        if (pDir != null) {
            try {
                mFtp.setLocalDir(new File(pDir));
            } catch (Exception e) {
                throw new RuntimeException("Error changing local directory on host: " + pDir, e);
            }
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
                mFtp.setDir(pDir);
            } catch (FtpException e) {
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
                mFtp.upload(pFile);
            } catch (FtpException e) {
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
                mFtp.upload(pFile);
            } catch (FtpException e) {
                throw new RuntimeException("Error uploading file to host: " + pFile, e);
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
                mFtp.upload(pLocalFile, pRemoteFile);
            } catch (FtpException e) {
                throw new RuntimeException("Error uploading file to host: " + pLocalFile + " remote file: " + pRemoteFile, e);
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
                mFtp.upload(pLocalFile, pRemoteFile);
            } catch (FtpException e) {
                throw new RuntimeException("Error uploading file to host: " + pLocalFile + " remote file: " + pRemoteFile, e);
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
                result = mFtp.download(pFile);
            } catch (FtpException e) {
                throw new RuntimeException("Error downloading file from host: " + pFile, e);
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
     * @return remoteDir Name
     */
    public String getRemoteDir() {
        try {
            return mFtp.getDir();
        } catch (FtpException e) {
            throw new RuntimeException("Error connecting remote directory. ", e);
        }
    }

    /**
     * Retrieves a directory listing of all files (in the current directory) on the remote host.
     *
     * @return An Enumeration of FtpFile objects representing files on the remote host.
     */
    public Enumeration getRemoteDirListing() {
        Enumeration listing = null;

        try {
            listing = mFtp.getDirListing();
        } catch (FtpException e) {
            throw new RuntimeException("Error retrieving directory listing from host. ", e);
        }

        return listing;
    }

    /**
     * Retrieves a directory listing of all files (in the current directory) matching the given regular expression on
     * the remote host.
     *
     * @param pRegex Example: To list all files with extension .java or .class the regex whould be ".+\\.(?:java|class)"
     * @return An Enumeration of FtpFile objects representing files on the remote host matching the regular expression.
     */
    public Enumeration getRemoteDirListing(String pRegex) {
        Enumeration listing = null;

        try {
            listing = mFtp.getDirListing(pRegex);
        } catch (FtpException e) {
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
            mFtp.deleteDir(pDir);
        } catch (FtpException e) {
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
            mFtp.deleteDir(pDir, pRecurse);
        } catch (FtpException e) {
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
            mFtp.deleteFile(pFile);
        } catch (FtpException e) {
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
            mFtp.renameFile(pOldFile, pNewFile);
        } catch (FtpException e) {
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
            mFtp.makeDir(pDir);
        } catch (FtpException e) {
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
            mFtp.makeDirRecursive(pDir);
        } catch (FtpException e) {
            throw new RuntimeException("Error creating directory on host: " + pDir, e);
        }
    }

    /**
     * Creates a directory on the local system.
     *
     * @param pDir
     */
    public void createLocalDir(String pDir) {
        mFtp.makeLocalDir(pDir);
    }

    /*
     * Checks to see if the specified dir is a valid directory on the remote host.
     * @param pDir
     * @return
     */

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // The following are for FTP session event logging
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Use these enumerations values to determine which ftp events to log.
     */
    public enum FtpLogEvent {
        CONNECT,    // log ftp connect events
        DISCONNECT, // log ftp disconnect events
        DOWNLOAD,   // log ftp (successful) file download events
        UPLOAD,     // log ftp (successful) file upload events
        PROGRESS,   // log ftp file upload/download progress events
        DIRLISTING, // log ftp directory listing events
        DELETEDIR,  // log ftp directory delete events
        DELETEFILE, // log ftp file delete events
        RENAMEFILE, // log ftp file rename events
        CREATEDIR,  // log ftp directory create events
        CHANGEDIR   // log ftp directory change events
    }

    /**
     * This is the default listener class to facilitate logging of FTP session events.
     */
    private class SimpleFtpFileEventListener extends FtpAdapter {
        private final String OFFSET = ">  ";
        private final String NEWLINE = System.getProperty("line.separator");
        private final BigDecimal bd100 = new BigDecimal(String.valueOf(100));
        private final List<FtpLogEvent> mEventList = new Vector<FtpLogEvent>();

        /**
         * Add bulk log events to the list of active log events. Clears the list prior to setting the new events to log.
         *
         * @param pEvents
         */
        public void setLogEvents(FtpLogEvent... pEvents) {
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
        public void addLogEvent(FtpLogEvent pEvent) {
            if ((pEvent != null) && !mEventList.contains(pEvent)) {
                mEventList.add(pEvent);
            }
        }

        /**
         * Remove a specific log event from the list of active log events.
         *
         * @param pEvent The event you wish to remove.
         */
        public void removeLogEvent(FtpLogEvent pEvent) {
            if ((pEvent != null) && mEventList.contains(pEvent)) {
                mEventList.remove(pEvent);
            }
        }

        /**
         * A convenience method to add all log events to list of active log events.
         */
        public void setDebugMode() {
            setLogEvents(FtpLogEvent.values());
        }

        /**
         * Log connect events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.CONNECT) to enable or removeLogEvent(FtpLogEvent.CONNECT) to disable.
         * <br>
         *
         * @param event
         */
        public void connected(FtpConnectedEvent event) {
            if (!mEventList.contains(FtpLogEvent.CONNECT)) {
                return;
            }

            getLogger().info("Connection established with FTP host: " + event.getHostname());
        }

        /**
         * Log disconnect events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.DISCONNECT) to enable or removeLogEvent(FtpLogEvent.DISCONNECT) to disable.
         * <br>
         *
         * @param event
         */
        public void disconnected(FtpDisconnectedEvent event) {
            if (!mEventList.contains(FtpLogEvent.DISCONNECT)) {
                return;
            }

            getLogger().info("Disconnected from FTP host: " + event.getHostname());
        }

        /**
         * Log successful file download events for the SFTP session. Disabled by default.
         * <br>
         * Call addLogEvent(SftpLogEvent.DOWNLOAD) to enable or removeLogEvent(SftpLogEvent.DOWNLOAD) to disable.
         * <br>
         *
         * @param event
         */
        public void download(FtpDownloadEvent event) {
            if (!mEventList.contains(FtpLogEvent.DOWNLOAD)) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Successfully downloaded file from FTP host:").append(NEWLINE);
            buf.append(OFFSET).append("File Location: ").append(event.getPath()).append(NEWLINE);
            buf.append(OFFSET).append("File Name:     ").append(event.getFilename()).append(NEWLINE);
            buf.append(OFFSET).append("File Size:     ").append(event.getSize()).append(NEWLINE);
            buf.append(OFFSET).append("Download Time: ").append(event.getTime());

            getLogger().info(buf.toString());
        }

        /**
         * Log successful file upload events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.UPLOAD) to enable or removeLogEvent(FtpLogEvent.UPLOAD) to disable.
         * <br>
         *
         * @param event
         */
        public void upload(FtpUploadEvent event) {
            if (!mEventList.contains(FtpLogEvent.UPLOAD)) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Successfully uploaded file to FTP host:").append(NEWLINE);
            buf.append(OFFSET).append("File Location: ").append(event.getPath()).append(NEWLINE);
            buf.append(OFFSET).append("File Name:     ").append(event.getFilename()).append(NEWLINE);
            buf.append(OFFSET).append("File Size:     ").append(event.getSize()).append(NEWLINE);
            buf.append(OFFSET).append("Upload Time:   ").append(event.getTime());

            getLogger().info(buf.toString());
        }

        /**
         * Log file transfer (upload/download) progress events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.PROGRESS) to enable or removeLogEvent(FtpLogEvent.PROGRESS) to disable.
         * <br>
         *
         * @param event
         */
        public void progress(FtpProgressEvent event) {
            if (!mEventList.contains(FtpLogEvent.PROGRESS)) {
                return;
            }

            // calculate the percent complete of the file transfer
            BigDecimal bdDividend = new BigDecimal(String.valueOf(event.getBytes()));
            BigDecimal bdDivisor = new BigDecimal(String.valueOf(event.getTotalBytes()));
            BigDecimal result = bdDividend.divide(bdDivisor, 3, RoundingMode.FLOOR).multiply(bd100);
            String percentComplete = result.toBigInteger().toString() + "%";

            StringBuffer buf = new StringBuffer();

            buf.append(event.getMode() == FtpProgressEvent.UPLOAD ? "Uploading: " : "Downloading: ");
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
         * Log directory listing events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.DIRLISTING) to enable or removeLogEvent(FtpLogEvent.DIRLISTING) to disable.
         * <br>
         *
         * @param event
         */
        public void dirListing(FtpListingEvent event) {
            if (!mEventList.contains(FtpLogEvent.DIRLISTING)) {
                return;
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Directory listing on server:").append(NEWLINE);

            Enumeration fileList = event.getListing();

            if (!fileList.hasMoreElements()) {
                buf.append(OFFSET).append("[ no files found ]").append(NEWLINE);
            } else {
                while (fileList.hasMoreElements()) {
                    FtpFile file = (FtpFile) fileList.nextElement();

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
                }
            }

            getLogger().info(buf.toString().trim());
        }

        /**
         * Log directory deletion events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.DELETEDIR) to enable or removeLogEvent(FtpLogEvent.DELETEDIR) to disable.
         * <br>
         *
         * @param event
         */
        public void deleteDir(FtpDeleteDirEvent event) {
            if (!mEventList.contains(FtpLogEvent.DELETEDIR)) {
                return;
            }

            getLogger().info("Deleted directory on server: " + event.getDirectory() + " (parent directory was: " + event.getPath() + ")");
        }

        /**
         * Log file deletion events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.DELETEFILE) to enable or removeLogEvent(FtpLogEvent.DELETEFILE) to disable.
         * <br>
         *
         * @param event
         */
        public void deleteFile(FtpDeleteFileEvent event) {
            if (!mEventList.contains(FtpLogEvent.DELETEFILE)) {
                return;
            }

            String dir = event.getPath().replaceAll("\\\\", "/");

            if (!dir.endsWith("/")) {
                dir += "/";
            }

            getLogger().info("Deleted file on server: " + dir + event.getFile());
        }

        /**
         * Log file rename events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.RENAMEFILE) to enable or removeLogEvent(FtpLogEvent.RENAMEFILE) to disable.
         * <br>
         *
         * @param event
         */
        public void renameFile(FtpRenameFileEvent event) {
            if (!mEventList.contains(FtpLogEvent.RENAMEFILE)) {
                return;
            }

            String dir = event.getPath().replaceAll("\\\\", "/");

            if (!dir.endsWith("/")) {
                dir += "/";
            }

            StringBuffer buf = new StringBuffer();

            buf.append("Renamed file on server:").append(NEWLINE);
            buf.append(OFFSET).append("From: ").append(dir).append(event.getOldName()).append(NEWLINE);
            buf.append(OFFSET).append("To:   ").append(dir).append(event.getNewName());

            getLogger().info(buf.toString());
        }

        /**
         * Log directory creation events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.CREATEDIR) to enable or removeLogEvent(FtpLogEvent.CREATEDIR) to disable.
         * <br>
         *
         * @param event
         */
        public void createDir(FtpCreateDirEvent event) {
            if (!mEventList.contains(FtpLogEvent.CREATEDIR)) {
                return;
            }
            String dir = event.getPath().replaceAll("\\\\", "/");

            if (!dir.endsWith("/")) {
                dir += "/";
            }
            getLogger().info("Created directory on server: " + dir + event.getDirectory());
        }

        /**
         * Log directory change events for the FTP session. Disabled by default.
         * <br>
         * Call addLogEvent(FtpLogEvent.CHANGEDIR) to enable or removeLogEvent(FtpLogEvent.CHANGEDIR) to disable.
         * <br>
         *
         * @param event
         */
        public void changeDir(FtpChangeDirEvent event) {
            if (!mEventList.contains(FtpLogEvent.CHANGEDIR)) {
                return;
            }
            getLogger().info("Changed directory on server to: " + event.getDirectory());
        }
    }
}
package com.intuit.sbd.payroll.psp.common.utils.jsch;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.ParallelEnvJSSUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileInputStream;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchEvent.JSchEventType;
import com.intuit.spc.foundations.primary.logging.SpcfLevel;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import static java.nio.charset.StandardCharsets.UTF_8;


import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * @author not attributable
 *
 *
 *         Description: This class is to send the data to the Remote system.
 *         This uses secure FTP to send the data.
 * 
 *         Even though JSch allows multiple channels of various types to be
 *         associated with one session.
 * 
 *         <b>Transporter allows associating only one SFTP channel per
 *         session.</b>
 * 
 *         Directories should be changes appropriately on the local and remote
 *         host before the upload/download as these are done relative to the
 *         current directory
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
	public static final int TIME_OUT = 60000;  // Value in milliseconds

	private Session mSession = null;
	private ChannelSftp mChannelSftp = null;
	private String mHost = null;
	private String mPort = null;
	private String mUser = null;
	private String mPassword = null;
    private String mPrvKey = null;
    private boolean mUseKeyAuth = false;
    private String mKnownHostsPath = null;

	private int mTimeOut = TIME_OUT;

	private SpcfLogger mLogger = null;
	private List<JSchListener> mListener = null;
	private Proxy mProxy = null;
	private boolean mDebug;

	public Transporter(String pHost, String pPort, String pUser, String passKey, boolean useKeyAuth) {
		this.mHost = pHost;
		this.mPort = pPort;
		this.mUser = pUser;
        this.mUseKeyAuth = useKeyAuth;
        if(useKeyAuth)
		    this.mPrvKey = passKey;
        else
            this.mPassword = passKey;

		addListener(new JSchEventLogger());
	}

    public Transporter(String pHost, String pUser, String pPassword) {
        this(pHost, pUser, pPassword, false);
    }

	public Transporter(String pHost, String pUser, String passKey, boolean useKeyAuth) {
		this(pHost, SFTP_PORT, pUser, passKey, useKeyAuth);
	}

    public boolean isUseKeyAuth() {
        return mUseKeyAuth;
    }

    public void setUseKeyAuth(boolean useKeyAuth) {
        this.mUseKeyAuth = useKeyAuth;
    }

    public String getPrvKey() {
        return mPrvKey;
    }

    public void setPrvKey(String prvKey) {
        this.mPrvKey = prvKey;
    }

    public String getKnownHostsPath() {
        return mKnownHostsPath;
    }

    public void setKnownHostsPath(String knownHostsPath) {
        this.mKnownHostsPath = knownHostsPath;
    }

	public void changeLocalDir(String pDir) throws Exception {
		if (pDir == null) {
			throw new RuntimeException("Error changing local directory on host: " + pDir);
		}
		getChannelSftp().lcd(pDir);
	}

	public void changeRemoteDir(String pDir) throws Exception {
		if (pDir == null) {
			throw new RuntimeException("Error changing remote directory on host: " + pDir);
		}

		if (Application.isParallelEnv()) {
			pDir += "/" + ParallelEnvJSSUtils.getParallelEnvJSSSuffix();
			mLogger.info("Parallel Env changeRemoteDir pDir="+pDir);
		}
		getChannelSftp().cd(pDir);
	}

	/**
	 * Uploads the given file to the remote host. Uploads are performed relative
	 * to the current local directory.
	 *
	 * @param pLocalFile
	 * @param pRemoteFile
	 */
	public void uploadFile(String pLocalFile, String pRemoteFile) throws Exception {

        mLogger.info("Trying to Upload File=" + pLocalFile + " From=" + getLocalDir() + " To= " + getRemoteDir());

		if (pLocalFile == null) {
			throw new RuntimeException("Error locating file on local directory on host: " + pLocalFile);
		}

        if (pRemoteFile == null) {
            throw new RuntimeException("Error locating file on remote directory on host: " + pRemoteFile);
        }

        StopWatch sw = new StopWatch().start();
        try {
        	//add changes to handle
			File localFile = new File(getLocalDir(), pLocalFile);
			if(StreamUtil.isFileIDPSEncrypted(localFile))
			{
				mLogger.info("Trying to Upload Encrypted File=" + pLocalFile + " From=" + getLocalDir() + " To= " + getRemoteDir());
				Key key  = IDPSFileStreamManager.newKeyHandleLatest();
				IDPSFileInputStream encStream = new IDPSFileInputStream(localFile,key);
				InputStream decStream = StreamUtil.getDecryptedStream(encStream);
				getChannelSftp().put(decStream, pRemoteFile, ChannelSftp.OVERWRITE);
				decStream.close();
				encStream.close();
			}
			else {
				getChannelSftp().put(pLocalFile, pRemoteFile, ChannelSftp.OVERWRITE);
			}
           // getChannelSftp().put(pLocalFile, pRemoteFile, ChannelSftp.OVERWRITE);

            long fileSize = FileUtils.sizeOf(new File(getLocalDir(), pLocalFile));
            notifyListeners(new JSchEvent(JSchEventType.Upload), new FileBean(getRemoteDir(), pRemoteFile, fileSize,
                    sw.getElapsedTimeString()));
        } finally {
            sw.stop();
		}
	}

	public void uploadFile(String pDestinationFileName, InputStream pFileStream) throws Exception {
		mLogger.info("Trying to Upload File=" + pDestinationFileName + " To= " + getRemoteDir());

		StopWatch sw = new StopWatch().start();
		FileSizeProgressMonitor fileSizeProgressMonitor = new FileSizeProgressMonitor();
		try {
			getChannelSftp().put(pFileStream, pDestinationFileName, fileSizeProgressMonitor, ChannelSftp.OVERWRITE);
		} finally {
			sw.stop();
		}

		notifyListeners(new JSchEvent(JSchEventType.Upload),
				new FileBean(getRemoteDir(), pDestinationFileName, fileSizeProgressMonitor.getFileSize(), sw.getElapsedTimeString()));
	}

	/**
	 * Uploads the given file to the remote host. Uploads are performed relative
	 * to the current local directory.
	 *
	 * @param pLocalFile
	 */
	public void uploadFile(String pLocalFile) throws Exception {
        uploadFile(pLocalFile, pLocalFile);
	}
	/*public void uploadSecuredFile(String pLocalFile) throws Exception {
		uploadSecuredFile(pLocalFile, pLocalFile);
	}*/
    /**
     * Uploads all of the files in the list to the remote host.
     *
     * @param pFileList                A Collection<String> of files to upload.
     * @param pMillisDelayBetweenFiles Millisecond delay between files uploaded to the remote host.
     */
    public void uploadFiles(Collection<String> pFileList, int pMillisDelayBetweenFiles) throws Exception {
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
	 * Downloads the given file from the remote host. Downloads are performed
	 * relative to the current local directory.
	 *
	 * @param pFile
	 * @return A File object representing the downloaded file.
	 */
	public File downloadFile(String pFile) throws Exception {
		File result = null;
		if (pFile != null) {
			StopWatch sw = new StopWatch().start();
			try {
				InputStream inputStream = getChannelSftp().get(pFile);

				result = new File(getLocalDir(), pFile);
				FileUtils.copyInputStreamToFile(inputStream, result);

				long fileSize = FileUtils.sizeOf(new File(getLocalDir(), pFile));
				notifyListeners(new JSchEvent(JSchEventType.Download), new FileBean(getLocalDir(), pFile, fileSize,
						sw.getElapsedTimeString()));
			} finally {
				sw.stop();
			}
		}
		return result;
	}
	public File downloadFileSecurely(String pFile) throws Exception {
		File result = null;
		if (pFile != null) {
			StopWatch sw = new StopWatch().start();
			try {
				InputStream inputStream = getChannelSftp().get(pFile);

				result = new File(getLocalDir(), pFile);
				//FileUtils.copyInputStreamToFile(inputStream, result);
				Key key  = IDPSFileStreamManager.newKeyHandleLatest();
				StreamUtil.copyEncryptedBytes(inputStream,result,key);

				long fileSize = FileUtils.sizeOf(new File(getLocalDir(), pFile));
				notifyListeners(new JSchEvent(JSchEventType.Download), new FileBean(getLocalDir(), pFile, fileSize,
						sw.getElapsedTimeString()));
			} finally {
				sw.stop();
			}
		}
		return result;
	}

	// To Rename the file
	public void rename(String oldPath, String newPath) throws Exception {
		String sourceDirPath = ".";
		int lastSeparatorIndex = oldPath.lastIndexOf("/");
		if (lastSeparatorIndex > -1) {
			sourceDirPath = oldPath.substring(0, lastSeparatorIndex);
		}

		if (newPath.indexOf("/") == -1) {
			newPath = sourceDirPath + "/" + newPath;
		}

		if (oldPath.equals(newPath) == false) {
			if (getChannelSftp().ls(newPath).isEmpty() == false) {
				getChannelSftp().rm(newPath);
			}
			getChannelSftp().rename(oldPath, newPath);
		}
	}

	// To Remove file
	public void deleteRemoteFile(String pFile) throws Exception {

		if (pFile == null) {
			throw new RuntimeException("Error locating file on remote directory on host: " + pFile);
		}

        StopWatch sw = new StopWatch().start();
        try {
            if (!getChannelSftp().ls(pFile).isEmpty()) {
                getChannelSftp().rm(pFile);
            }

            notifyListeners(new JSchEvent(JSchEventType.DeleteFile), new FileBean(getRemoteDir(), pFile, 0,
                    sw.getElapsedTimeString()));
        } finally {
            sw.stop();
        }

	}

	public String getRemoteDir() throws Exception {
		return getChannelSftp().pwd();
	}

	public String getLocalDir() throws Exception {
		return getChannelSftp().lpwd();
	}

    /** Note : It will return all files & sub dirs including . & .. dirs */
	public List<String> getRemoteDirListing() throws Exception {
		List<String> files = new ArrayList<String>();

		Vector<LsEntry> vector = mChannelSftp.ls(getRemoteDir());
		for (LsEntry lsEntry : vector) {
			files.add(lsEntry.getFilename());
		}

		return files;
	}

	public List<String> getRemoteDirListing(final String filePattern) throws Exception {
		final List<String> files = new ArrayList<String>();

		mChannelSftp.ls(getRemoteDir(), new LsEntrySelector() {
			@Override
			public int select(LsEntry lsEntry) {
				if (lsEntry.getFilename().matches(filePattern)) {
					files.add(lsEntry.getFilename());
				}
				return 0;
			}
		});

		return files;
	}

	public void connect() throws Exception {
		Session session = this.getSession();
		if (session != null && !session.isConnected()) {
			session.connect(getTimeOut());
			openChannel();
            notifyListeners(new JSchEvent(JSchEventType.Connected), session.getHost());
		}
	}

    /* Closes STFP Channel & Session */
	public void disconnect() throws Exception {
		if (mSession != null && mSession.isConnected()) {
			closeChannel();
            mSession.disconnect();
            notifyListeners(new JSchEvent(JSchEventType.Disconnected), mSession.getHost());
		}
        mSession = null;
	}

	protected Session getSession() throws Exception {
		if (mSession == null) {
			JSch.setLogger(new JSchLogger(isDebug()));
			JSch jSch = new JSch();

			String host = this.getHost();
			String port = this.getPort();
			String user = this.getUser();
			String password = this.getPassword();

			int portInt = Integer.parseInt(port);

            // Disabling the StrictHostKeyChecking option will make the connection less secure & prone to man-in-the-middle attacks
            Properties config = new Properties();
            if (StringUtils.isNotEmpty(getKnownHostsPath())) {
                jSch.setKnownHosts(getKnownHostsPath());
                config.put("StrictHostKeyChecking", "yes");
            }

            if (isUseKeyAuth())
				jSch.addIdentity("", getPrvKey().getBytes(UTF_8) , null, null);

			mSession = jSch.getSession(user, host, portInt);
            mSession.setProxy(mProxy);

			UserInfo userInfo = new SFTPUserInfo(password);
			mSession.setUserInfo(userInfo);

            if (!isUseKeyAuth())
			    mSession.setPassword(password);

            mSession.setConfig(config);
		}

		return mSession;
	}

	protected void openChannel() throws Exception {
		Session session = this.getSession();
		if (session != null) {
			if (session.isConnected() && this.mChannelSftp == null) {
				try {
					Channel channel = session.openChannel(SFTP_CHANNEL_TYPE_VALUE);
					this.mChannelSftp = (ChannelSftp) channel;
					this.mChannelSftp.connect(getTimeOut());
				} catch (JSchException jSchException) {
					throw jSchException;
				}
			}
		}
	}

	protected void closeChannel() throws Exception {
		if (getChannelSftp() != null && getChannelSftp().isConnected()) {
			getChannelSftp().disconnect();
		}
        mChannelSftp = null;
	}

	private void notifyListeners(JSchEvent pEvent, Object val) {
		List<JSchListener> listeners = getListeners();
		if (listeners != null) {
			for (JSchListener jSchListener : listeners) {
				jSchListener.handleEvent(pEvent, val);
			}
		}
	}

	/**
	 * Set the host url and port to use for proxying this sftp connection.
	 *
	 * @param pProxyHostname
	 *            The proxy host url.
	 * @param pProxyPort
	 *            The proxy host port.
	 * @throws Exception
	 */
	public void setProxyHost(String pProxyHostname, int pProxyPort) throws Exception {
		mProxy = new ProxyHTTP(pProxyHostname, pProxyPort);
	}

	/**
	 * Set the proxy host type.
	 *
	 * @param pProxyType
	 *            The proxy type (valid values are HTTP and SOCKS5).
	 */
	public void setProxyType(String pProxyType) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set the username and password for the proxy host (optional).
	 *
	 * @param pProxyUsername
	 *            The proxy host username.
	 * @param pProxyPassword
	 *            The proxy host password.
	 */
	public void setProxyAuthentication(String pProxyUsername, String pProxyPassword) {
		if (mProxy == null) {
			throw new RuntimeException("Proxy Host and Port are not set");
		}

		if (mProxy instanceof ProxyHTTP) {
			ProxyHTTP proxyHTTP = (ProxyHTTP) mProxy;
			proxyHTTP.setUserPasswd(pProxyUsername, pProxyPassword);
		} else {
			throw new RuntimeException("Authenciation can't be set for proxies other than HTTP");
		}
	}

	/**
	 * Clear all proxy settings.
	 * <p/>
	 * Note that the JScape Sftp class stores some proxy settings statically.
	 * (the details of just what is stored statically is unknown since their
	 * code base is proprietary)
	 * 
	 * @throws Exception
	 */
	public void clearProxySettings() throws Exception {
		mProxy = null;
		getSession().setProxy(null);
	}

	public ChannelSftp getChannelSftp() {
		return mChannelSftp;
	}

	public String getHost() {
		return mHost;
	}

	public void setHost(String pHost) {
		this.mHost = pHost;
	}

	public String getPort() {
		return this.mPort;
	}

	public void setPort(String pPort) {
		this.mPort = pPort;
	}

	public String getUser() {
		return this.mUser;
	}

	public void setUser(String pUser) {
		this.mUser = pUser;
	}

	public String getPassword() {
		return this.mPassword;
	}

	public void setPassword(String pPassword) {
		this.mPassword = pPassword;
	}

	public int getTimeOut() {
		return mTimeOut;
	}

	public void setTimeOut(int pTimeOut) {
		this.mTimeOut = pTimeOut;
	}

	public SpcfLogger getLogger() {
		if (mLogger == null) {
			mLogger = Application.getLogger(this.getClass());
		}
		return mLogger;
	}

	public void setLogger(SpcfLogger pLogger) {
		this.mLogger = pLogger;
	}

	public void addListener(JSchListener listener) {
		if (mListener == null) {
			mListener = new ArrayList<JSchListener>();
		}

        if (listener != null)
		    mListener.add(listener);
	}

	public void removeAllListeners() {
		if (mListener != null) {
			mListener.clear();
		}
	}

	public List<JSchListener> getListeners() {
		return mListener;
	}

	public boolean isDebug() {
		return mDebug;
	}

	public void setDebug(boolean pDebug) {
		this.mDebug = pDebug;
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

	public class JSchLogger implements Logger {

		private boolean debug;

		private Map<Integer, SpcfLevel> name = new HashMap<Integer, SpcfLevel>() {
			{
				put(new Integer(DEBUG), SpcfLevel.Debug);
				put(new Integer(INFO), SpcfLevel.Info);
				put(new Integer(WARN), SpcfLevel.Warn);
				put(new Integer(ERROR), SpcfLevel.Error);
				put(new Integer(FATAL), SpcfLevel.Fatal);
			}
		};

		public JSchLogger() {
			super();
		}

		public JSchLogger(boolean debug) {
			super();
			this.debug = debug;
		}

		public boolean isEnabled(int level) {
			if (!debug && level == DEBUG) {
				return false;
			}
			return true;
		}

		public void log(int level, String message) {
			getLogger().log(name.get(level), message, null);
		}
	}

	public class JSchEventLogger extends JSchAdapter {

		private final String OFFSET = ">  ";
		private final String NEWLINE = System.getProperty("line.separator");

		@Override
		public void download(FileBean val) {
			StringBuffer buf = new StringBuffer();

			buf.append("Successfully downloaded file from SFTP host:").append(NEWLINE);
			buf.append(OFFSET).append("File Location: ").append(val.getPath()).append(NEWLINE);
			buf.append(OFFSET).append("File Name:     ").append(val.getFilename()).append(NEWLINE);
			buf.append(OFFSET).append("File Size:     ").append(val.getFilesize()).append(NEWLINE);
			buf.append(OFFSET).append("Download Time: ").append(val.getTime());

			getLogger().info(buf.toString());
		}

		@Override
		public void upload(FileBean val) {
			StringBuffer buf = new StringBuffer();

			buf.append("Successfully uploaded file to SFTP host:").append(NEWLINE);
			buf.append(OFFSET).append("File Location: ").append(val.getPath()).append(NEWLINE);
			buf.append(OFFSET).append("File Name:     ").append(val.getFilename()).append(NEWLINE);
			buf.append(OFFSET).append("File Size:     ").append(val.getFilesize()).append(NEWLINE);
			buf.append(OFFSET).append("Upload Time:   ").append(val.getTime());

			getLogger().info(buf.toString());
		}

	}

	public class FileSizeProgressMonitor implements com.jcraft.jsch.SftpProgressMonitor {
		private long fileSize = 0;

		@Override
		public void init(int op, String src, String dest, long max) {
			// no-op
		}

		@Override
		public boolean count(long count) {
			fileSize += count;
			// continue
			return true;
		}

		@Override
		public void end() {

		}

		public long getFileSize() {
			return fileSize;
		}
	}
}
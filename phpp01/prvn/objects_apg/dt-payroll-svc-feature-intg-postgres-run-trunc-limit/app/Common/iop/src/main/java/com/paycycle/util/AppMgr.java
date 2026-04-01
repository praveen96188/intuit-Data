/**
 * AppMgr.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.paycycle.util;

//import com.intuit.platform.client.PlatformClient;
//import com.intuit.platform.client.PlatformSessionContext;
//import com.intuit.platform.util.Config;
//import com.paycycle.abtest.ABTestType;
//import com.paycycle.abtest.PopulationManager;
//import com.paycycle.auth.AuthMgr;
//import com.paycycle.biz.Company;
//import com.paycycle.biz.CompanyBankAccountChangeListeners;
//import com.paycycle.biz.CompanyStatus;
//import com.paycycle.biz.Contractor;
//import com.paycycle.biz.EServicesMgr;
//import com.paycycle.biz.Employee;
//import com.paycycle.biz.FeatureSet.FeatureSetType;
//import com.paycycle.biz.Partner;
//import com.paycycle.biz.PartnerMgr;
//import com.paycycle.biz.Provider;
//import com.paycycle.changemgr.ChangeMgr;
//import com.paycycle.data.AppLock;
//import com.paycycle.data.SessionEventHandler;
//import com.paycycle.data.TOPLinkProject;
//import com.paycycle.data.Transaction;
//import com.paycycle.io.PartnerGatewayServlet;
//import com.paycycle.jni.TaxWrap;
//import com.paycycle.marketing.CodeMgr;
//import com.paycycle.partnerdata.PartnerAdapter;
//import com.paycycle.partnerdata.PartnerAdapterType;
//import com.paycycle.partnerdata.PartnerDataMgr;
//import com.paycycle.payment.TaxPaymentActivityMgr;
//import com.paycycle.payment.TaxPaymentAlertManager;
//import com.paycycle.payment.TaxPaymentMgr;
//import com.paycycle.payroll.PayrollWarningMgr;
//import com.paycycle.profile.ProfileFilter;
//import com.paycycle.profile.ProfileHelper;

import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
//import com.paycycle.profile.ProfileLong;
//import com.paycycle.security.Encryption;
//import com.paycycle.security.PhoneFactor;
//import com.paycycle.sso.ArcotManager;
//import com.paycycle.sso.SSOManager;
//import com.paycycle.sso.UserTransientData;
//import com.paycycle.sso.saml.SamlHelper;
//import com.paycycle.task.FlowMgr;
//import com.paycycle.task.PayrollTaskList;
//import com.paycycle.task.SubmitButtonActionManager;
//import com.paycycle.task.TaskMgrInstance;
//import com.paycycle.test.TestMgr;
//import com.paycycle.tx.CompanyTx;
//import com.paycycle.tx.UserTx;
//import com.paycycle.tx.VisitorTx;
//import com.paycycle.user.HostToPartnerMapping;
//import com.paycycle.user.Login;
//import com.paycycle.user.RoleID;
//import com.paycycle.user.User;
//import com.paycycle.user.UserEvent;
//import com.paycycle.user.UserException;
//import com.paycycle.user.UserExceptionErrorCode;
//import com.paycycle.user.UserType;
//import com.paycycle.user.VisitorConfigurationManager;
//import com.paycycle.user.VisitorInfoServlet;
//import com.paycycle.util.jmx.JMXMgr;
//import org.eclipse.persistence.descriptors.ClassDescriptor;
//import org.eclipse.persistence.expressions.Expression;
//import org.eclipse.persistence.expressions.ExpressionBuilder;
//import org.eclipse.persistence.logging.JavaLog;
//import org.eclipse.persistence.logging.SessionLog;
//import org.eclipse.persistence.sessions.DatabaseLogin;
//import org.eclipse.persistence.sessions.Project;
//import org.eclipse.persistence.sessions.Session;
//import org.eclipse.persistence.sessions.UnitOfWork;
//import org.eclipse.persistence.sessions.server.ConnectionPool;
//import org.eclipse.persistence.sessions.server.Server;
//
//import javax.servlet.RequestDispatcher;
//import javax.servlet.ServletContext;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import javax.servlet.jsp.PageContext;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Method;
//import java.net.Authenticator;
//import java.net.ProxySelector;
//import java.text.SimpleDateFormat;
//import java.util.CurrentTimeManager;
//import java.util.Date;
//import java.util.EmptyStackException;
//import java.util.HashMap;
//import java.util.Hashtable;
//import java.util.Iterator;
//import java.util.Properties;
//import java.util.Stack;
//import java.util.StringTokenizer;
//import java.util.Vector;
//import java.util.logging.FileHandler;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.LogManager;

/**
 * Application Manager.
 */
public class AppMgr {
    //	/** Keys/indexes to thread local application context */
    //	protected static final String APPNAME = "APPNAME";
    //	protected static final String DBCATALOG = "DBCATALOG";
    //	protected static final String USERID = "USERID";
    //	protected static final String COMPANYID = "COMPANYID";
    //	protected static final String DBTIME = "DBTIME";
    //
    //	public enum Catalog {
    //		PAYCYCLE, TEST, PROFILE, ALTERNATE, EMAIL
    //	}
    //
    //	/** constants */
    //	protected static final String PAYCYCLE = "paycycle";
    //	protected static final String OPS = "ops";
    //	protected static final String UNKNOWN_APPNAME = "unknown";
    //	protected static final String PCHISTORY = "com.paycycle.pchistory";
    //	protected static final String SAMPLE = "sample";
    //	protected static final String TOPLINKLOG = "toplink.log";
    //	protected static final String JVMID = "jvmid";
    //	protected static final String VISTORID = "visitorid";
    //	protected static final long SSO_LOGOUT_KEYS_EXPIRATION_TIME = 864000000; // 1 hour in milliseconds
    //	private static String BUILD_VERSION = "";
    //
    //	private static String CTX_ROOT;
    //	private static ServletContext servletContext;
    //
    //	/** Per-thread local storage */
    //	protected static ThreadLocal m_threadLocal = new ThreadLocal();
    //
    //	/** Per JVM properties */
    //	protected static PropertiesTable m_dbProperties;
    //	protected static EventMgr m_eventMgr = null;
    //	protected static UserActivityLogger m_userActivityLogger = null;
    //	protected static UserActivityNotifier m_userActivityNotifier = null;
    //	protected static TaxPaymentActivityMgr m_paymentActivityMgr;
    //	protected static PageHelpManager m_pageHelpManager;
    //	protected static CompanyBankAccountChangeListeners m_companyBankAccountChangeListeners;
    //	protected static TaxPaymentAlertManager m_taxPaymentAlertManager;
    //
    //	/** Per DB TOPLink Servers */
    //	protected static Hashtable<String, Server> m_dbServers = new Hashtable<String, Server>();
    //
    //	/** Currently only support for one query database **/
    //	protected static QueryDatabase m_queryDatabase;
    //
    //	// The resource bundles
    //	private static HashMap resourceBundles;
    //
    //	private static int businessDayMapSize = 1024;
    //	private static int businessDayRemoveSize = 100;
    //
    //	// Static types for resources
    //	public static final String RESOURCE_TYPE_SQL_STRING = "SQL_STRINGS";
    //	public static final String RESOURCE_TYPE_USER_MSGS = "USER_MESSAGES";
    //	public static final String RESOURCE_TYPE_PAYROLLWARNING_MSGS = "PAYROLLWARNING_MESSAGES";
    //
    //	public static ProfileHelper m_profileHelper;
    //	public static boolean m_profileDebug = false;
    //
    //    protected static ThreadLocal<HttpSession> sessionThreadLocal = new ThreadLocal<HttpSession>();
    //    protected static ThreadLocal<PageContext> pageContextThreadLocal = new ThreadLocal<PageContext>();
    //	protected static ThreadLocal<ChangeMgr> changeMgr = new ThreadLocal<ChangeMgr>();
    //
    //	protected static Class<? extends com.paycycle.payroll.PayrollWarningMgr> payrollWarningMgrClass;
    //
    //	/**
    //	 * Constructor.
    //	 */
    //	public AppMgr() {
    //	}
    //
    //	/**
    //	 * Initialize the Web Application.
    //	 */
    //	static boolean loggingInitialized = false;
    //
    //	public static ServletContext getServletContext() {
    //		return servletContext;
    //	}
    //
    //	public static boolean logEverythingToConsole() {
    //        //return isDevelopment();
    //        // hack for PD-6770 for now until logging is revamped
    //		return true;
    //	}
    //
    //        private static boolean isWindows() {
    //            final String OS_NAME = System.getProperty("os.name");
    //            return OS_NAME != null && OS_NAME.contains("Windows");
    //        }
    //
    //        private static String getLoggerBase() {
    //            String result = isWindows() ? "c:" : "/var/tmp";
    //            if (isDevelopment()) {
    //                result = new File(getResourcesPath()).getParent();
    //            }
    //            return result;
    //        }
    //
    //    public static void initBasicApp(ServletContext ctx) throws Exception {
    //			servletContext = ctx;
    //			CTX_ROOT = ctx.getRealPath("/");
    //			// Load top level properties
    //
    //            PropertiesManager.init(AppMgr.getContextRoot(), ctx.getInitParameter("properties"));
    //
    //            Logger.init(ctx, isDevelopment(), getContextPath(ctx), getLoggerBase());
    //            JMXMgr.init();
    //            try {
    //				InputStream is = new FileInputStream(getContextRoot() + "/WEB-INF/config/build.version");
    //				if (is != null) {
    //					Properties p = new Properties();
    //					p.load(is);
    //					BUILD_VERSION = p.getProperty("build.version");
    //				}
    //			} catch (Exception ex) {
    //			}
    //
    //			final String FULL_VERSION = getAppBaseVersion() + (BUILD_VERSION.length() > 0 ? "." + BUILD_VERSION : "");
    //			getLogger().info("===== booting Intuit Online Payroll version " + FULL_VERSION + " =====");
    //            PropertiesManager.setProperty("app.product.version", FULL_VERSION);
    //			final String JVM_VERSION = System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") +  " " + System.getProperty("java.runtime.version");
    //			getLogger().info("===== on jvm: " + JVM_VERSION + " =====");
    //			getLogger().info("===== for context: " + servletContext.getContextPath() + " =====");
    //
    //            initCrypto();
    //
    //            Config.configure(new Properties());
    //            Config.setProperty(Config.WORKPLACE_URL_KEY, PropertiesManager.getProperty(PropertiesManager.Category.SSO, "workplace.server"));
    //            Config.setProperty("ids.server", PropertiesManager.getProperty(PropertiesManager.Category.SSO, "ids.server"));
    //
    //        	payrollWarningMgrClass = com.paycycle.payroll.PayrollWarningMgr.class;
    //    }
    //
    //	public static String getAppBaseVersion() {
    //		return PropertiesManager.getProperty(PropertiesManager.Category.common, "app.product.version");
    //	}
    //
    //    public static void stopApp() {
    //        // shutdown profile subsystem
    //        getProfileHelper().stop();
    //    }
    //
    //    public static void initApp(ServletContext ctx) {
    //    public static void initApp() {
    //		try {
    //            initBasicApp(ctx);
    //
    //            // create instance of event handlers for activities related to any
    //			// User object
    //			// this must be created after the event manager has been created.
    //			initEventHandling();
    //			m_paymentActivityMgr = new TaxPaymentActivityMgr();
    //			m_pageHelpManager = new PageHelpManager(ctx);
    //			m_companyBankAccountChangeListeners = new CompanyBankAccountChangeListeners();
    //			m_taxPaymentAlertManager = new TaxPaymentAlertManager();
    //			EServicesMgr.addBillingHoldListener();
    //
    //			m_profileHelper = new ProfileHelper();
    //
    //            //initialize vertex
    //            initVertex();
    //
    //			// Set the resource bundles up so the applications can get the
    //			// properties as needed.
    //			setResourceBundles(getContextRoot() + getProperty("app.properties.resource"));
    //
    //			String strBusDayMapSize = getProperty("app.businessDay.mapSize");
    //
    //			if (strBusDayMapSize != null) {
    //				businessDayMapSize = Integer.parseInt(strBusDayMapSize);
    //			}
    //
    //			getLogger().info("Business Day Map Size: " + businessDayMapSize);
    //
    //			String strSizeToRemove = getProperty("app.businessDay.ifFull.removeSize");
    //			if (strSizeToRemove != null) {
    //				businessDayRemoveSize = Integer.parseInt(strSizeToRemove);
    //			}
    //
    //			getLogger().info("Business Day Remove Size: " + businessDayRemoveSize);
    //
    //			if (!AppMgr.isDevelopment() && !AppMgr.isOperations() && !AppMgr.isEmployeeSite()) {
    //				IndexManager.MakeIndex(ctx);
    //			}
    //
    //			m_dbProperties = new PropertiesTable("PayCycleProperties");
    //
    //			// In producton, initialize these things for the main context
    //			// Everywhere else, initialize these things for non-main
    //			// This is a big hack until we get rid of the dual context stuff
    //			if (isProduction() == ctx.equals(ctx.getContext("/")))
    //			{
    //				Authenticator.setDefault(new PayCycleAuthenticator());
    //				ProxySelector.setDefault(new PayCycleProxySelector(ProxySelector.getDefault()));
    //			}
    //
    //			// Initializes the PhoneFactor library for second factor authentication & opensaml library
    //			if (!isOperations() && !isEmployeeSite()) {
    //				//initPhoneFactor();
    //                initSAMLLibrary();			}
    //
    //            // Set up context paths from properties
    //            Hashtable paths = (Hashtable) ctx.getAttribute("paths");
    //            if (paths == null) {
    //                paths = new Hashtable();
    //                Hashtable ht = PropertiesManager.getProperties("app.context-path.");
    //                for (Iterator it = ht.keySet().iterator(); it.hasNext();) {
    //                    String name = (String) it.next();
    //                    paths.put(name, ctx.getContextPath() + ht.get(name));
    //                }
    //
    //				// Cache for later use
    //				ctx.setAttribute("paths", paths);
    //            }
    //
    //			// We have to store the class rather then just an object because the
    //			// PayrollWarningMgr isn't meant to be shared.  So we store the class
    //			// and instantiate a new one for each use.  Different apps will each
    //			// extend the PayrollWarningMgr for their own use.
    //			payrollWarningMgrClass = com.paycycle.payroll.PayrollWarningMgr.class;
    //
    //			// Release the application locks (if any) in case this app server
    //			// was stopped abruptly
    //			AppMgr.releaseAppLocks();
    //			TestMgr.runTest();
    //		} catch (Exception ex) {
    //			getLogger().fatal("Startup failed : " + ex, ex);
    //		}
    //	}
    //
    //    /**
    //     * Load the vertex path and initialize the library using TaxWrap.initVertex......
    //     * NOTE: This has to be invoked only once per app initialization....
    //     * @throws java.io.IOException
    //     */
    //    public static void initVertex () throws IOException {
    //			if (System.getProperty("vertexPath") == null) {
    //				System.setProperty("vertexPath", new File(getResourcesPath(),"../Library/Vertex/Payroll/bin").getCanonicalPath());
    //			}
    //			final String VERTEX_PATH = System.getProperty("vertexPath");
    //
    //			// Initialize the Vertex library
    //			getLogger().info("===== initializing Vertex JNI layer at: " + VERTEX_PATH + " =====");
    //            getLogger().info("===== Vertex database resides at " + getVertexDbPath());
    //			TaxWrap.initVertex(AppMgr.getVertexDbPath());
    //			getLogger().info("===== Vertex JNI layer successfully initialized =====");
    //    }
    //
    //
    //    public static String getKeystoreFileName() {
    //		return getContextRoot() + PropertiesManager.getProperty(PropertiesManager.Category.security, "keystore.root");
    //    }
    //
    //	private static void initCrypto() throws Exception {
    //		Encryption.init(getKeystoreFileName());
    //		getLogger().info("===== initialized crypto subsystem with " + getProperty("app.properties.security") + " =====");
    //	}
    //
    //    /**
    //     * Release any app locks held by this app server based on JVMID as app name.
    //     */
    //    public static void releaseAppLocks() {
    //        Catalog catalog = servletContext.getContextPath().equals("") ? Catalog.PAYCYCLE : Catalog.TEST;
    //        try {
    //            getLogger().info("Release DB App locks for context " + servletContext.getContextPath()
    //                    + " and catalog " + catalog);
    //            AppLock.releaseAppLockForApplication(catalog, AppMgr.getJVMID());
    //        } catch (Exception e) {
    //            getLogger().error("Error releasing the App locks", e);
    //        }
    //    }
    //
    //	/**
    //	 * This is broken out separately so that the various "init" functions, which
    //	 * are called in different situations, can all set up event handling
    //	 * properly. This takes care of building the event manager as well as any
    //	 * important, universally-needed event listeners.
    //	 */
    //	public static void initEventHandling() {
    //		if (m_eventMgr == null) {
    //			m_eventMgr = new EventMgr();
    //		}
    //		if (m_userActivityLogger == null) {
    //			m_userActivityLogger = new UserActivityLogger();
    //		}
    //		if (m_userActivityNotifier == null) {
    //			m_userActivityNotifier = new UserActivityNotifier();
    //		}
    //	}
    //
    //	protected static void setResourceBundles(String resourceCfgFile) throws IOException {
    //
    //		getLogger().debug("Reading resource config file: " + resourceCfgFile);
    //
    //		Properties resourceCfgProps = new Properties();
    //		resourceCfgProps.load(new FileInputStream(resourceCfgFile));
    //
    //		resourceBundles = new HashMap();
    //
    //		StringTokenizer tokens = new StringTokenizer(resourceCfgProps.getProperty("resource.names"), ", ");
    //
    //		while (tokens.hasMoreTokens()) {
    //			String token = tokens.nextToken().trim();
    //
    //			String filename = resourceCfgProps.getProperty(token + ".file");
    //			getLogger().debug("Properties file for " + token + ": " + filename);
    //
    //			Properties prop = new Properties();
    //			final String fname = getContextRoot() + filename;
    //			prop.load(new FileInputStream(fname));
    //			resourceBundles.put(token, prop);
    //		}
    //	}
    //
    //	/**
    //	 * A method to return the business day map size
    //	 */
    //	public static int getBusinessDayMapSize() {
    //		return businessDayMapSize;
    //	}
    //
    //	/**
    //	 * A method to return the business day remove size if the map is full.
    //	 */
    //	public static int getBusinessDayRemoveSize() {
    //		return businessDayRemoveSize;
    //	}
    //
    //	/**
    //	 * A method to get a property string based on the type of the property. The
    //	 * current supported property types are:
    //	 * <UL>
    //	 * <LI> SQL Strings (AppMgr.RESOURCE_TYPE_SQL_STRING), and </LI>
    //	 * <LI> User Messages (AppMgr.RESOURCE_TYPE_USER_MSGS) </LI>
    //	 * <LI> PayrollWarning Messages (AppMgr.RESOURCE_TYPE_PAYROLLWARNING_MSGS) </LI>
    //	 * </UL>
    //	 *
    //	 * @param propType
    //	 *            The type of the property.
    //	 * @param key
    //	 *            The key whose value you need.
    //	 *
    //	 * @return The value of the provided key.
    //	 */
    //	public static String getProperty(String propType, String key) {
    //		Properties prop = (Properties) resourceBundles.get(propType);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		return prop.getProperty(key);
    //
    //	}
    //
    //	/**
    //	 * A method to get a property string based on the type of the property. The
    //	 * method also takes an array of objects that will be used to format the
    //	 * given property string before returning.
    //	 *
    //	 * The current supported property types are:
    //	 * <UL>
    //	 * <LI> SQL Strings (AppMgr.RESOURCE_TYPE_SQL_STRING), and </LI>
    //	 * <LI> User Messages (AppMgr.RESOURCE_TYPE_USER_MSGS) </LI>
    //	 * </UL>
    //	 *
    //	 * @param propType
    //	 *            The type of the property.
    //	 * @param key
    //	 *            The key whose value you need.
    //	 *
    //	 * @return The value of the provided key.
    //	 */
    //	public static String getProperty(String propType, String key, Object[] args) {
    //		Properties prop = (Properties) resourceBundles.get(propType);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		String propStr = prop.getProperty(key);
    //
    //		if (args != null) {
    //			return java.text.MessageFormat.format(propStr, args);
    //		} else {
    //			return propStr;
    //		}
    //	}
    //
    //	/**
    //	 * A helper method to get the SQL Strings from the properties file.
    //	 *
    //	 * @param key
    //	 *            The key whose value you need.
    //	 */
    //	public static String getSQLString(String key) {
    //		Properties prop = (Properties) resourceBundles.get(AppMgr.RESOURCE_TYPE_SQL_STRING);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		return prop.getProperty(key);
    //	}
    //
    //	/**
    //	 * A helper method to get the SQL Strings from the properties file. The
    //	 * method also takes an array of objects that will be used to format the
    //	 * given property string before returning.
    //	 *
    //	 * @param key
    //	 *            The key whose value you need.
    //	 */
    //	public static String getSQLString(String key, Object[] args) {
    //		Properties prop = (Properties) resourceBundles.get(AppMgr.RESOURCE_TYPE_SQL_STRING);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		String propStr = prop.getProperty(key);
    //
    //		if (args != null) {
    //			return java.text.MessageFormat.format(propStr, args);
    //		} else {
    //			return propStr;
    //		}
    //	}
    //
    //	/**
    //	 * A helper method to get the UserMsg Strings from the properties file.
    //	 *
    //	 * @param key
    //	 *            The key whose value you need.
    //	 */
    //	public static String getUserMsg(String key) {
    //		Properties prop = (Properties) resourceBundles.get(AppMgr.RESOURCE_TYPE_USER_MSGS);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		return prop.getProperty(key);
    //	}
    //
    //	/**
    //	 * A helper method to get the UserMsg Strings from the properties file. The
    //	 * method also takes an array of objects that will be used to format the
    //	 * given property string before returning.
    //	 *
    //	 * @param key
    //	 *            The key whose value you need.
    //	 */
    //	public static String getUserMsg(String key, Object[] args) {
    //		Properties prop = (Properties) resourceBundles.get(AppMgr.RESOURCE_TYPE_USER_MSGS);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		String propStr = prop.getProperty(key);
    //
    //		if (args != null) {
    //			return java.text.MessageFormat.format(propStr, args);
    //		} else {
    //			return propStr;
    //		}
    //	}
    //
    //	/**
    //	 * A helper method to get the PayrollWarning msg Strings from the properties file.
    //	 *
    //	 * @param key
    //	 *            The key whose value you need.
    //	 */
    //	public static String getPayrollWarningMsg(String key) {
    //		Properties prop = (Properties) resourceBundles.get(AppMgr.RESOURCE_TYPE_PAYROLLWARNING_MSGS);
    //
    //		if (prop == null) {
    //			return null;
    //		}
    //
    //		return prop.getProperty(key);
    //	}
    //
    //	/**
    //	 * A helper method to get the PayrollWarning msg Strings from the properties file. The
    //	 * method also takes an array of objects that will be used to format the
    //	 * given property string before returning.
    //	 *
    //	 * There is also a utility class, PayrollWarningMsg, which has a method to
    //	 * generate the args as appropriate, given a PayrollWarning object, then call
    //	 * this method to get/format the message.
    //	 *
    //	 * @param key
    //	 *            The key whose value you need.
    //	 */
    //	public static String getPayrollWarningMsg(String key, Object[] args) {
    //		String propStr = getPayrollWarningMsg(key);
    //		if (args != null) {
    //			return java.text.MessageFormat.format(propStr, args);
    //		} else {
    //			return propStr;
    //		}
    //	}
    //
    //	/**
    //	 * Initialize application context based on CGI server name and script name
    //	 * variables. This method should be called for every HTTP request.
    //	 */
    //	protected static void initThreadContext(String server, String context) {
    //		/** Get threadlocal storage */
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		if (ctx == null) {
    //			ctx = new Hashtable();
    //		}
    //
    //		/** Decide on the application name based on request URL */
    //		String name = StringUtil.strip(context, "/");
    //		if (Helper.isEmpty(name)) {
    //			name = PAYCYCLE;
    //		}
    //
    //		if (isDevelopment()) {
    //			ctx.put(APPNAME, "test");
    //		} else {
    //			ctx.put(APPNAME, name);
    //		}
    //		m_threadLocal.set(ctx);
    //	}
    //
    //    public static InputStream getResourceAsStream(String path) {
    //        return getServletContext().getResourceAsStream(path);
    //    }
    //	/**
    //	 * Calls the workhorse initContext()
    //	 */
    //	public static void initContext(PageContext pageContext) {
    //		if (pageContextThreadLocal.get() == null) {
    //	        pageContextThreadLocal.set(pageContext);
    //			HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    //			initContext(request, pageContext.getServletContext());
    //		}
    //	}
    //
    //    public static PageContext getPageContext() {
    //        return pageContextThreadLocal.get();
    //    }
    //
    //    public static HttpSession getHttpSession() {
    //        return sessionThreadLocal.get();
    //    }
    //
    //    public static void clearHttpSession() {
    //        sessionThreadLocal.set(null);
    //        pageContextThreadLocal.set(null);
    //        clearCompanyToLog();
    //    }
    //
    //	/*
    //	 * initFakeDate
    //	 *
    //	 * If the fd parameter is used in the request, set the date offset in the
    //	 * CurrentTimeManager Also, register the current session ID with the time
    //	 * manager.
    //	 *
    //	 */
    //	protected static void initFakeDate(HttpServletRequest request) {
    //		// Tell the current time manager what the current session ID is
    //		try {
    //			CurrentTimeManager.setCurrentSessionId(request.getSession().getId());
    //		} catch (NoClassDefFoundError Ex) {
    //		} catch (SecurityException secEx) {
    //		} // can occur on weblogic when trying to load fake date support code
    //	}
    //
    //	/**
    //	 * Create the application level variables, initialize the AppMgr with
    //	 * context parameters.
    //	 */
    //	public static void initContext(HttpServletRequest request, ServletContext application) {
    //        if (sessionThreadLocal.get() == null) {
    //        	// Set the session.  It is necessary to set this before any managers
    //        	// (like partnerDataMgr) get created so they have access to the session.
    //            HttpSession httpSession = request.getSession();
    //            String sessionId = httpSession.getId();
    //            String URI = request.getRequestURL().toString() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    //            sessionThreadLocal.set(httpSession);
    //
    //            AppMgr.getLogger().debug(request.getRequestURL());
    //
    //            String disableEventHandling = PropertiesManager.getProperty("app.public.disableEventHandling");
    //            disableEventHandling = disableEventHandling == null ? "false" : disableEventHandling;
    //            if (!Boolean.parseBoolean(disableEventHandling)) {
    //                initEventHandling();
    //            }
    //
    //			// Create the application-level variables once
    //			String context = request.getContextPath();
    //			if (context.equals("/")) {	// suspected tomcat bug
    //				context = "";
    //				AppMgr.getLogger().warning("request.getContextPath() return / which is out of spec. "
    //						+ "corrected to emtpy string "
    //						+ "request URI was: " + request.getRequestURI());
    //			}
    //
    //            // Set up context paths from properties
    //            Hashtable paths = (Hashtable) application.getAttribute("paths");
    //            if (paths == null) {
    //                paths = new Hashtable();
    //                Hashtable ht = PropertiesManager.getProperties("app.context-path.");
    //                for (Iterator it = ht.keySet().iterator(); it.hasNext();) {
    //                    String name = (String) it.next();
    //                    paths.put(name, context + ht.get(name));
    //                }
    //
    //				// Cache for later use
    //				application.setAttribute("paths", paths);
    //			}
    //
    //			/* For each request, make sure we keep track of thread locals */
    //			initThreadContext(request.getServerName(), context);
    //
    //			/* Initialize the DB catalog */
    //			initDbCatalog(request.getSession());
    //
    //			// Load TaxPaymentGroups
    //			TaxPaymentMgr.loadTaxPaymentGroups();
    //
    //			 // Find the source code in the cookie and use it to determine the partner
    //			if (request.getSession().getAttribute("companyId") == null) {
    //				String sc = request.getParameter("SC");
    //				if (sc == null) {
    //					sc = ServletUtil.getCookieValue(request, "SC");
    //				}
    //
    //				Partner hostPartner = HostToPartnerMapping.getPartnerFromHost(request.getServerName());
    //	            String defaultSourceCode = "";
    //	            if (hostPartner != null) {
    //	                defaultSourceCode = hostPartner.getDefaultSourceCode().getSourceCode();
    //	            }
    //				Partner partner = PartnerMgr.getPartner(sc == null ? defaultSourceCode : sc, -1);
    //				HttpSession session = request.getSession();
    //				session.setAttribute("partnerId", String.valueOf(partner.getId()));
    //			}
    //
    //			setCompanyAndPartner(request);
    //
    //			// Do the fake date feature (only in development)
    //			if (!isProduction()) {
    //				initFakeDate(request);
    //			}
    //        }
    //	}
    //
    //	private static void setCompanyAndPartner(HttpServletRequest request) {
    //		String companyId = (String)request.getSession().getAttribute("companyId");
    //		if (companyId != null) {
    //			Company company = (Company) Helper.readObject(com.paycycle.biz.Company.class, Long.parseLong(companyId));
    //	        if (company == null) {
    //	        	getLogger().error("Session is for a nonexistent company (company ID=" + companyId + ")");
    //	        	// This seems like it ought to be an error yet employeesite sets companyId=-1 so we need to ignore it.
    ////	        	throw new IllegalArgumentException("Session is for a nonexistent company"); // Don't leak company IDs to client
    //	        	return;
    //	        }
    //
    //            // Set the partner company in the logger.
    //            setCompanyToLog(company);
    //
    //            // If there is a partner and that partner has a PartnerAdapter, create a
    //        	// PartnerDataMgr instance (which gets saved in the session) and register
    //        	// it to listen for changes.
    //            Partner partner = company.getPartner();
    //            if (partner.initializePartnerManager()) {
    //                // This manager instance is saved in the session by BaseServiceLayerManager.getInstance().
    //                PartnerDataMgr mgr = ServiceLayerHelper.getPartnerDataMgrInstance();
    //
    //                // Listen for DB changes - this must happen AFTER initEventMgr as that creates
    //                // the changeMgr and we cannot register until it is created.
    //                mgr.registerChangeListeners();
    //            } else {
    //            	ServiceLayerHelper.clearPartnerDataMgrInstance();
    //            }
    //            partner.initSessionData(request);
    //		} else {
    //			clearCompanyToLog();
    //		}
    //	}
    //
    //	/**
    //     * Sets the company id and partner id into the MDC of the logger so they log.  If the company
    //     * is null, then nothing happens and the logger's MDC values stay as is.
    //	 *
    //	 * @param company The company to pull the info from for logging (can be null).
    //	 */
    //	public static void setCompanyToLog(Company company) {
    //		// Set the QBO company into the logger's MDC so it prints in the logs.
    //		if (company != null) {
    //			setCompanyToLog(company.getPartner().getId(), company.getIdAsString(), company.getExternalPartnerDataId());
    //		}
    //	}
    //
    //    /**
    //     * Sets the company id and partner id into the MDC of the logger so they log.  If either
    //     * value is null, it is removed from the logger's MDC.
    //     *
    //     * @param partnerId			The partner id for this company.
    //     * @param companyId			The company id to log (can be null).
    //     * @param partnerCompanyId	The partner company id to log (can be null).
    //     */
    //	public static void setCompanyToLog(long partnerId, String companyId, String partnerCompanyId) {
    //        Logger.setMDC ("CoId", companyId == null ? null : "CoId:" + companyId);
    //    	Logger.setMDC("PartnerCoId", null);
    //        if (companyId != null && partnerCompanyId != null) {
    //        	Partner partner = PartnerMgr.getPartner(partnerId); // never returns null
    //	        PartnerAdapter partnerAdapter = partner.getPartnerAdapter();
    //	        if (partnerAdapter != null) {
    //	        	PartnerAdapterType partnerAdapterType = partnerAdapter.getPartnerAdapterType();
    //	        	Logger.setMDC("PartnerCoId", partnerAdapterType.toString() + "CoId:" + partnerCompanyId);
    //	        }
    //        }
    //	}
    //
    //	/**
    //	 * Clear the log MDC when there isn't a company associated with the request.
    //	 */
    //	public static void clearCompanyToLog() {
    //		Logger.setMDC("CoId", null);
    //		Logger.setMDC("PartnerCoId", null);
    //	}
    //
    //	/**
    //	 * Invalidate the existing session.
    //	 */
    //	public static void clearSession(HttpSession session) {
    //		if (session != null) {
    //			String sessionInvalidated = "Session invalidated [" + session.getId() + "]";
    //			getLogger().info(Helper.getCallerMessage(sessionInvalidated, 1));
    //			session.invalidate();
    //			clearHttpSession();
    //		}
    //	}
    //
    //	/**
    //	 * Invalidate the existing session and create a new one.
    //	 * If the session has a partnerId attribute it is preserved
    //	 * into the new session (the partner isn't changing during the call).
    //	 * @param request the current servlet request object.
    //	 */
    //	public static void createNewSession(HttpServletRequest request) {
    //		HttpSession session = request.getSession(false);
    //		String partnerId = null;
    //		if (session != null) {
    //			partnerId = (String)session.getAttribute("partnerId");
    //		}
    //		clearSession(session);
    //		session = request.getSession(true);
    //		sessionThreadLocal.set(session);
    //		if (partnerId != null) {
    //			session.setAttribute("partnerId", partnerId);
    //		}
    //	}
    //
    //	public static void initDbCatalog(HttpSession session) {
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		String catalog = (String) ctx.get(APPNAME);
    //		if (Helper.isNotEmpty(session.getAttribute("isSample"))) {
    //			catalog = SAMPLE;
    //		}
    //
    //		initDbCatalog(catalog);
    //	}
    //
    //	public static void initDbCatalog(String catalog) {
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		ctx.put(DBCATALOG, getProperty("app.db.catalog." + catalog));
    //	}
    //
    //	/**
    //	 * Initialize the context. Typically this is called by non UI client (for
    //	 * example, automatic tasks) to establish the necessary context.
    //	 * NOTE: Do not use this method to launch Sample Company since we
    //	 *       cannot check for the Sample DB catalog given a String-based context.
    //	 */
    //	public static void initContext(String context) {
    //		initThreadContext(null, context);
    //		initDbCatalog(getAppName());
    //	}
    //
    //	/**
    //	 * Initialize the PhoneFactor authentication object.
    //	 */
    //	public static void initPhoneFactor() {
    //		String configPath = AppMgr.getContextRoot() + "/WEB-INF/config/PhoneFactor/";
    //		PhoneFactor.getInstance().initialize(configPath, "R6GMKWROAONBIDO7");
    //	}
    //
    //	public static void initSAMLLibrary() {
    //		SamlHelper.init();
    //	}
    //
    //	public static QueryDatabase getQueryDatabase() {
    //		/**
    //		 * Construct singleton QueryDatabase
    //		 */
    //		if (m_queryDatabase == null) {
    //			m_queryDatabase = new QueryDatabase();
    //		}
    //
    //		return m_queryDatabase;
    //	}
    //
    //	// Returns the application-specific JVMID
    //	public static String getJVMID() {
    //		return System.getProperty(JVMID, "none");
    //	}
    //
    //	public static String getBuildVersion() {
    //		return BUILD_VERSION;
    //	}
    //
    //	/**
    //	 * Detect, based on the current JVM ID and session JVM ID, if failover
    //	 * happened. Failover will happen if the session's JVM ID is not the same as
    //	 * current JVM ID.
    //	 *
    //	 * @param session
    //	 *            the http session
    //	 * @return true if failover is detected, otherwise false.
    //	 */
    //	public static boolean detectFailover(HttpSession session) {
    //		String jvmId = getJVMID();
    //		String sessionJvmId = (String) session.getAttribute(JVMID);
    //
    //		if (sessionJvmId == null) {
    //			session.setAttribute(JVMID, jvmId);
    //			return false;
    //		}
    //
    //		if (!sessionJvmId.equals(jvmId)) {
    //			// The ID came from another JVM and got replicated on the current
    //			// server
    //			session.setAttribute(JVMID, jvmId);
    //			AppMgr.getLogger().info("Failover detected from JVMID = " + sessionJvmId + " to JVMID = " + jvmId + " for session " + session.getId());
    //			return true;
    //		}
    //
    //		return false;
    //	}
    //
    //	public static void validateLogin(PageContext pageContext) {
    //		// The following line of code is a painful hack.  Basically, this will create an instance of the task manager
    //		// and store it in the request.  This is needed because the validateLogin function below needs the task manager
    //		// to check on whether or not the user is set up, but since it doesn't have access to the pageContext, it isn't
    //		// able to properly do this.  So we set up it here.
    //        TaskMgrInstance.getPayrollTaskMgrInstance(pageContext);
    //		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    //		HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
    //		validateLogin(request, response);
    //	}
    //
    //	/**
    //	 * Check if the user is logged in. If not show login page. If login is
    //	 * submitted process the login.
    //	 */
    //	public static void validateLogin(HttpServletRequest request, HttpServletResponse response) {
    //		HttpSession session = request.getSession();
    //
    //		// If login is being attempted then try logging in now.
    //		RequestDispatcher rd = null;
    //
    //		try {
    //			// next if statement should be true only when trying to login
    //			if (!isLoggedIn(request)
    //					&& request.getParameter("loginNow") != null
    //					&& (session.getAttribute("ssoType") != null || (
    //							request.getParameter("userStrId") != null && request.getParameter("userStrId").trim().length() > 0 &&
    //							request.getParameter("password") != null && request.getParameter("password").trim().length() > 0))) {
    //				// Until we allow re-login to sample company, end sample company
    //				// now.
    //				//there is logic about welcome back message which depends on loginPage flag
    //				//For QBOSSO login, we currently don't want to show welcome back message
    //				//after user add Payroll, so donnotSetloginPageFlag is set in QBOSSO login process(IamAuthenticationReplay.jsp)
    //				if(!"true".equals(request.getAttribute("donnotSetloginPageFlag"))) {
    //					session.setAttribute("loginPage","yes");
    //				}
    //
    //				AppMgr.endSample(session);
    //				session.setAttribute("logLoginIPAddr", "1");
    //
    //				// for non-SSO, if false returned then do not continue with this
    //				// function, just return.
    //				if (!processLogin(request, response, true)) {
    //					return;
    //				}
    //			}
    //
    //			if (!ServletUtil.isSessionValid(session) || session.getAttribute("userId") == null) {
    //				if(request.getParameter("partner_integration") != null) {
    //					//set partner intergration flag into the session so it can be use to determine
    //					//if we need to push changes to the partner like QBO. When data is out of synch
    //					//between Paryoll and partner, this flag is set to off so Ops can fix the problem
    //					//in Payroll
    //					session.setAttribute("partner_integration", request.getParameter("partner_integration"));
    //				}
    //				// User is not logged in and is not attempting to login. Need to
    //				// send them to the appropriate login location.
    //				if (SSOManager.shouldRedirectToPartnerLogin(request)) {
    //					UserException exception = request.getAttribute("error") != null && request.getAttribute("error") instanceof UserException ? (UserException) request.getAttribute("error") : null;
    //					if (exception == null || exception.getMessage().indexOf("We are experiencing high seasonal volume") < 0) {
    //						//ideally, we want to rewrite the logic if/else to handling cases for ssotimeout vs ssoerror.
    //						//however, pnc ssoerror is handled in ssotimeout
    //						boolean handleError = false;
    //						if(exception != null && (exception.getErrorCode() == UserExceptionErrorCode.AuthMgr_Multiple_Users_Login
    //								|| exception.getErrorCode() == UserExceptionErrorCode.AuthMgr_Multiple_session_login)) {
    //							Partner partner = HostToPartnerMapping.getPartnerFromHost(request.getServerName());
    //							if(partner != null && partner.hasPartnerDataIntegration()) {
    //								rd = request.getRequestDispatcher("/" + SSOManager.SSO_ERROR_PAGE);
    //								handleError = true;
    //							}
    //						}
    //						if(!handleError) {
    //							PartnerGatewayServlet.sendRedirect(request, response, "timeout");
    //						}
    //
    //					} else {
    //						rd = request.getRequestDispatcher(SSOManager.SSO_ERROR_PAGE);
    //					}
    //				} else if (request.getAttribute("employeeSite") != null) {
    //					rd = request.getRequestDispatcher("/login.jsp");
    //				} else {
    //					rd = request.getRequestDispatcher("/login/login.jsp");
    //				}
    //
    //				if (rd != null) {
    //					rd.include(request, response);
    //				}
    //				return;
    //			}
    //
    //			if (session.getAttribute("userId") != null) {
    //				request.setAttribute("userId", session.getAttribute("userId"));
    //			}
    //
    //			Company company=null;
    //			if (session.getAttribute("companyId") != null) {
    //				String id = (String) session.getAttribute("companyId");
    //				request.setAttribute("companyId", id);
    //				company = (Company) Helper.readObject(com.paycycle.biz.Company.class, Long.parseLong(id));
    //
    //				/* Do necessary synchronization in case of fail-over */
    //				if (detectFailover(request.getSession())) {
    //					company = refreshCompany(company);
    //				}
    //			}
    //
    //			if (session.getAttribute("providerId") != null) {
    //				request.setAttribute("providerId", session.getAttribute("providerId"));
    //			}
    //			Object partnerId = session.getAttribute("partnerId");
    //			if (partnerId != null) {
    //				request.setAttribute("partnerId", partnerId);
    //			}
    //			if (Helper.isEmpty(ServletUtil.getCookie(request, "USR"))) {
    //				ServletUtil.setCookieValue(request, response, "USR", "0");
    //			}
    //
    //			if (request.getAttribute("parameterListExpirationError") != null) {
    //				try {
    //					company = refreshCompany(company);
    //					if (company.isProvider()) {
    //						redirectToSPTodoList(request, response, company);
    //					} else {
    //						response.resetBuffer();
    //						request.setAttribute("PageRedirect", "true");
    //						response.sendRedirect(request.getContextPath() + "/in/todo/default.jsp");
    //					}
    //				} catch (IOException ex) {
    //				}
    //			}
    //
    //			try {
    //				if (session.getAttribute("postLoginPagesToVisit") != null
    //						&& (SubmitButtonActionManager.anyActionHasBeenRequested(request) || request.getParameter("loginNow") != null))
    //				{
    //					String redirectURL = ((Stack<String>) session.getAttribute("postLoginPagesToVisit")).pop();
    //
    //					// Add the "skipInterviewCheck" parameter so that we don't redirect into Apollo for this page.
    //					if (redirectURL.indexOf('?') >= 0)
    //						redirectURL += "&skipInterviewCheck";
    //					else
    //						redirectURL += "?skipInterviewCheck";
    //
    //					// Do the redirect and return.
    //					response.sendRedirect(redirectURL);
    //					return;
    //				}
    //			} catch (EmptyStackException ex) {
    //			}
    //
    //            // handle assisted setup related requests only when company is available and setup assistance has been requested:
    //            if (company != null && company.getStatus().getStatus(CompanyStatus.NEED_ONBOARDING_ASSISTANCE, "false").equals("true")) {
    //            	String redirectURL = getAssistedSetupURL(company, request);
    //            	if (redirectURL != null) {
    //                    // being here meaning this is a ops publicLogin or a customer login:
    //            		if (redirectURL.equals("return")) {
    //                        ;// "return" meaning go to the desired url:
    //                    }
    //                    else {
    //                        // not "return" means go to the redirect setup service url:
    //            			response.sendRedirect(redirectURL);
    //                    }
    //            		return;
    //            	}
    //                else {
    //                    ;// not handled by getAssistedSetupURL(...)
    //                }
    //            }
    //
    //			// If the user attempts to access any page while not in the setup wizard and while setup is incomplete,
    //			// then we will automatically redirect them to the setup hub.
    //			if (company != null && !isSample(session) &&										// Not a sample company
    //				!FlowMgr.isInInterviewMode(request) &&                                          // Make sure not already in interview mode.
    //				!company.getStatus().getInitialSetupCompleted() &&                              // If setup ever completed, then they are allowed to be out of setup mode
    //				Helper.isEmpty(company.getPreTodoPage()) &&										// If there are NOT any pre-todo pages to be done
    //				request.getAttribute("skipInterviewCheck") == null &&							// If the page did not set "skipInterviewCheck" to skip this check (terminate service pages set this)
    //				request.getParameter("skipInterviewCheck") == null ) {							// If the page did not set get passed "skipInterviewCheck" on the URL
    //
    //                TaskMgrInstance tmgr = TaskMgrInstance.getPayrollTaskMgrInstance(request);
    //
    //                if (tmgr != null) {
    //                    tmgr.setCompany(company);
    //
    //                    boolean redirectToApolloBecauseSetupIsIncomplete = company.isInSetup(tmgr);
    //                    boolean showExitSetupLink = company.canAccountantNavigateTabWorld(tmgr, request, response);
    //
    //                    if (redirectToApolloBecauseSetupIsIncomplete && (!showExitSetupLink && !isTireKickingEnabled(tmgr, request, response))) {
    //                        request.setAttribute("PageRedirect", "true");
    //                        if (request.getParameter("a") != null && request.getParameter("a").trim().length() > 0 &&
    //                            request.getParameter("a").equalsIgnoreCase("inquiry") && company.getProviderID() == -1)
    //                        {
    //                            session.setAttribute("inquiry", "true");
    //                        }
    //
    //                        // If the client is terminated and are ending up at the hub, message them at the hub.
    //                        if (company.isTerminated())
    //                            session.setAttribute("isTerminated", "true");
    //
    //                        // Redirect to the (Apollo) Setup hub
    //                        if (company.chooseDIYOverSetupService())
    //                            response.sendRedirect(request.getContextPath() + "/signup/hubs/start.jsp?chooseDIYLogin=true");
    //                        else
    //                            response.sendRedirect(request.getContextPath() + "/signup/hubs/start.jsp");  // This is wrong.  Should ask the FlowMgr, but the flow manager is not accessible.
    //                    }
    //                }
    //            }
    //		} catch (Exception ex) {
    //			request.setAttribute("error", ex);
    //		}
    //	}
    //
    //	public static boolean isLoggedIn(PageContext pageContext) {
    //		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    //		return isLoggedIn(request);
    //	}
    //
    //	public static boolean isLoggedIn(HttpServletRequest request) {
    //		HttpSession session = request.getSession();
    //
    //		boolean returnVal = session.getAttribute("userId") != null && session.getAttribute("companyId") != null;
    //
    //		if (returnVal && !AuthMgr.isCurrentSession(request)) {
    //			returnVal = false;
    //			AuthMgr.logout(request, false);
    //		}
    //
    //		return returnVal;
    //	}
    //
    //	/*
    //	 * this method invoked only when company is available and setup assistance has been requested
    //	 */
    //	public static String getAssistedSetupURL(Company company, HttpServletRequest request)
    //	{
    //        String paramPublicLogin = (String) request.getParameter("publicLogin");
    //        if (Helper.isNotEmpty(paramPublicLogin) && paramPublicLogin.equals("true"))
    //            request.getSession().setAttribute("publicLogin", "true");
    //
    //        // if ops publicLogin, return "return":
    //        if (Helper.isNotEmpty(request.getSession().getAttribute("publicLogin")) && "true".equals(request.getSession().getAttribute("publicLogin"))) {
    //            if (Helper.isNotEmpty(paramPublicLogin) && paramPublicLogin.equals("true"))
    //                return (request.getContextPath() + "/in/assistedSetup/assistedSetup.jsp");
    //            else
    //                return "return";
    //        }
    //
    //        // if customer login:
    //        String requestURL = request.getRequestURL().toString();
    //        String inAssistedSetup = (String) request.getAttribute("inAssistedSetup");
    //        String inSetupReview = (String) request.getAttribute("inSetupReview");
    //
    //        // assisted setup is completed by clicking on createPaychecks, setupOverivew or employeeOverview buttons in final assisted setup page:
    //        String status = company.getStatus().getStatus(CompanyStatus.ONBOARDING_ASSISTANCE_STATUS, "");
    //        if (status != null && status.equals(CompanyStatus.ONBOARDING_ASSISTANCE_STATUS_COMPLETED))
    //            return "return";
    //
    //        // buttons on hub pages:
    //        if (requestURL.indexOf("contract.jsp") >= 0 // TaskProfileCSA page: even b4 step into to AssistedSetup, NEED_ONBOARDING_ASSISTANCE is set to 'true' already
    //            //|| requestURL.indexOf("importEE.jsp") >= 0 // go to SPOT
    //            || requestURL.indexOf("sendInquiry.jsp") >= 0 || requestURL.indexOf("emailUs.jsp") >= 0 // email us
    //            || requestURL.indexOf("spotSelection.jsp") >= 0 // return to spot selection
    //            || requestURL.indexOf("eSvcEditBankInfo.jsp") >= 0 // update bank info
    //            || requestURL.indexOf("eSvcVerifyBank.jsp") >= 0 // verify back info
    //            || requestURL.indexOf("eSvcBmrAuthForm.jsp") >= 0 // authorization form
    //            || requestURL.indexOf("batchReports.jsp") >= 0) { // Excel batch export
    //            return "return";
    //        }
    //
    //        // review all data:
    //        if (requestURL.indexOf("setupReview.jsp") >= 0) {
    //            if (Helper.isNotEmpty(inSetupReview) && inSetupReview.equals("true"))
    //                return "return"; //avoid login looping
    //            else
    //                return (request.getContextPath() + "/in/setupReview/setupReview.jsp");
    //        }
    //
    //        // anything else:
    //        if (Helper.isNotEmpty(inAssistedSetup) && inAssistedSetup.equals("true")) {
    //            if (requestURL.indexOf("assistedSetup.jsp") >= 0)
    //                return "return"; //avoid login looping
    //        }
    //        else
    //            return (request.getContextPath() + "/in/assistedSetup/assistedSetup.jsp");
    //
    //        return null;
    //	}
    //
    //	/**
    //	 * Login submit is processed. Handles login validation, forcing the users to
    //	 * change the passwords if needed. Takes cares of redirecting to provider
    //	 * page also.
    //	 */
    //	public static boolean processLogin(HttpServletRequest request, HttpServletResponse response, boolean validateLoginSalt) {
    //		HttpSession session = request.getSession();
    //		session.setAttribute("LoginRegistration", "false");
    //		long start = System.currentTimeMillis();
    //
    //		if (SSOManager.shouldProcessSSOLogin(request)) {
    //			if (!processSSOLogin(request, response, session, SSOManager.getSSOType(request))) {
    //				return false;
    //			}
    //		} else {
    //			if (!processStandardLogin(request, response, session, validateLoginSalt)) {
    //				return false;
    //			}
    //			session.setAttribute("LoginRegistration", "true");
    //		}
    //
    //		// Now that we're logged in, force the VCM to re-initialize
    //		VisitorConfigurationManager.invalidate(session);
    //
    //		// Profile logging
    //		HashMap pm = new HashMap();
    //		pm.put("companyId", request.getAttribute("companyId"));
    //		pm.put("userId", request.getAttribute("userId"));
    //		pm.put("employeeSiteLogin", request.getParameter("employeeSiteLogin"));
    //		long dbtime = 0;
    //		Long dbtimeLong = AppMgr.getDbTime("processLogin");
    //		if (dbtimeLong != null) {
    //			dbtime = dbtimeLong;
    //		}
    //		AppMgr.getProfileHelper().log(start, System.currentTimeMillis(), dbtime, pm);
    //		AppMgr.initDbTime("processLogin");
    //		return true;
    //	}
    //
    //	/**
    //	 * *D* todo: factor this into processStandardLogin and get rid of this
    //	 */
    //	private static boolean processSSOLogin(HttpServletRequest request, HttpServletResponse response, HttpSession session, Login.SSOType ssoType) {
    //		try {
    //			boolean isSP = false;
    //			User user = null;
    //			if (ssoType != null && ssoType != Login.SSOType.None) {
    //				UserTransientData td = UserTransientData.getUserTransientData(session);
    //
    //				if (td != null) {
    //					String externalUserId = Login.buildSSOExternalId(ssoType, td.getUserId(), td.getFederatedIdentity());
    //					user = AuthMgr.lookUpSsoUser(ssoType, externalUserId);
    //					if (user == null) {
    //						/*
    //						 * For SSO, we don't fail login if the user is unrecognized.
    //						 * If the login is for the Owner of the account, update the
    //						 * existing owner with the new user name info. If it's a
    //						 * maintenance user, add a new user on the fly and continue
    //						 * with login.
    //						 */
    //						if (td.getUserType() == UserTransientData.UserType.OWNER) {
    //							Company tempco = SSOManager.getCompanyBySsoExternalId(td.getFederatedIdentity(), ssoType);
    //							user = new UserTx().updateSsoUserId(tempco.getUser(), ssoType, externalUserId);
    //						} else {
    //							user = new CompanyTx().addNewSsoUser(ssoType, td);
    //						}
    //					} else {
    //                        if (AppMgr.hasIDSPlatformSessionContext() && Helper.isEmpty(user.getLogin().getExternalPartnerDataId())) {
    //                            try {
    //                                com.intuit.platform.client.User ippUser = new PlatformClient().getUserInfo(AppMgr.getIDSPlatformSessionContext());
    //                                Transaction tx = getDbTransaction();
    //                                User registeredUser = tx.registerExitingObject(user);
    //                                registeredUser.getLogin().setExternalPartnerDataId(ippUser.getId());
    //                                tx.commit();
    //                            } catch (Exception e) {
    //                                getLogger().error(e);
    //                            }
    //                        }
    //
    //                    }
    //				} else if (ssoType == Login.SSOType.MFA) {
    //					String ssoExternalID = (Helper.isNotEmpty(session.getAttribute("SSOExternalID"))) ? session.getAttribute("SSOExternalID").toString() : null;
    //					if (Helper.isNotEmpty(ssoExternalID)) user = AuthMgr.lookUpSsoUser(ssoType, ssoExternalID);
    //
    //					if (user == null) {
    //						//If its a migrating user associate the ssoexternalid with the user login information
    //						String action = Helper.isNotEmpty(request.getParameter("action")) ?
    //											request.getParameter("action").toString() : "";
    //						String userStrId = Helper.isNotEmpty(request.getParameter("userStrId")) ?
    //												request.getParameter("userStrId").toString() : "";
    //						if (action.equals(ArcotManager.Migration)) {
    //							user = AuthMgr.lookUpStandardUser(userStrId);
    //							Transaction tx = AppMgr.getDbTransaction();
    //							User uClone = (User)tx.registerObject(user);
    //							uClone.getLogin().setFormat(Login.FORMAT_SSO);
    //							uClone.getLogin().setSsoType(Login.SSOType.MFA);
    //							uClone.getLogin().setPassword(Login.generateRandomPassword(Login.FORMAT_SSO));
    //							uClone.getLogin().setArcotActionStatus(ArcotManager.Enroll, Login.SUCCESS);
    //							uClone.getLogin().setSsoExternalID(ssoExternalID);
    //							tx.commit();
    //						}
    //					}
    //
    //					if (user == null) {
    //						throw new UserException("Please enter a valid User ID");
    //					} else {
    //						AuthMgr.clearFailedPasswordAndSQInfo(user.getLogin().getUserId());
    //						ServletUtil.setCookieValue(request, response, "USERSTRID", user.getLogin().getUserId());
    //					}
    //				} else {
    //					return false;
    //				}
    //			} else {
    //				throw new RuntimeException("appmgr: unsupported sso: " + ssoType);
    //			}
    //
    //			AuthMgr.processMultipleUserLoginBlock(user, request, false);
    //
    //			Company company;
    //			if (user.isProviderAdmin()) {
    //				isSP = true;
    //				// handle accountants (but not their employees)
    //				company = refreshCompany(user.getProviderCompany());
    //				String id = String.valueOf(company.getId());
    //				session.setAttribute("providerId", id);
    //				request.setAttribute("providerId", id);
    //				session.setAttribute("companyId", id);
    //				request.setAttribute("companyId", id);
    //			} else if (request.getParameter("employeeSiteLogin") != null) {
    //				company = processEmployeeSiteLogin(user, session, request);
    //			} else {
    //				// regular, non-AC employees should never get here; they do not
    //				// use SSO.
    //				company = refreshCompany(user.getCompany());
    //				session.setAttribute("companyId", String.valueOf(company.getId()));
    //				request.setAttribute("companyId", String.valueOf(company.getId()));
    //			}
    //
    //            //reset the index in case this company is already cached on this server and a contractor could have been added on another
    //            company.resetContractorIndex(true);
    //
    //			session.setAttribute("userId", String.valueOf(user.getId()));
    //			request.setAttribute("userId", String.valueOf(user.getId()));
    //
    //			// encrypt the company's source code before finding the partner
    //			String sc = Encryption.encryptCode(company.getSourceCode());
    //			Partner p = PartnerMgr.getPartner(sc, company.getId());
    //
    //			// For Brandless companies, change the source code before we write
    //			// the cookie
    //			if (p.getId() == Partner.MANAGEPAYROLL) {
    //				sc = Encryption.encryptCode(Partner.MANAGEPAYROLL_SOURCECODE);
    //			}
    //
    //			session.setAttribute("partnerId", String.valueOf(p.getId()));
    //			request.setAttribute("partnerId", String.valueOf(p.getId()));
    //
    //            // Here the partner is determined based on source code.
    //            // But to determine whether to set HTTPOnly flag on cookie, I determine partner using HostToPartner mapping.
    //			ServletUtil.setCookieValue(request, response, "SC", sc);
    //
    //            if (company != null) {
    //                VisitorTx vtx = new VisitorTx();
    //                vtx.assignCompanyToVisitor(company.getId(), ServletUtil.getCookieValue(request, VisitorInfoServlet.VISITOR_ID_COOKIE));
    //            }
    //
    //			setLastLogin(user, request);
    //
    //			if (isSP) {
    //				// if the request is not within the sp realm or elink, default
    //				// to client page
    //				if (request.getRequestURI().indexOf("/sp/") < 0) {
    //					redirectToSPTodoList(request, response, company);
    //				}
    //			}
    //		} catch (Exception ex) {
    //			request.setAttribute("error", ex);
    //		}
    //		return true;
    //	}
    //
    //        static final int LOGIN_FAILED = 0;
    //        static final int LOGIN_SUCCEEDED = 1;
    //        static final int LOGIN_IGNORED = 2;
    //	private static boolean processStandardLogin(HttpServletRequest request, HttpServletResponse response, HttpSession session, boolean validateLoginSalt) {
    //                int loginSuccessful = LOGIN_FAILED;
    //		boolean isEmployeeSiteLogin = request.getParameter("employeeSiteLogin") != null;
    //		try {
    //			if (request.getParameter("userStrId") == null || request.getParameter("password") == null) {
    //				throw new UserException("Please start by entering your User ID and password.");
    //			}
    //
    //			String userStrId = request.getParameter("userStrId").trim();
    //			String password = request.getParameter("password").trim();
    //
    //			// the vConfig here is for determining whether we should block the user based on what URL they came from.
    //			VisitorConfigurationManager vConfig = VisitorConfigurationManager.getInstance(request);
    //			AuthMgr.validateLoginInfo(userStrId, password, isEmployeeSiteLogin, vConfig.getPartner(), request);
    //			User user = AuthMgr.lookUpStandardUser(userStrId);
    //
    //			if (validateLoginSalt && !isEmployeeSiteLogin && !isDevelopment()) { // prevent refresh of login using the back button by using a per-login-form salt that can only be used once
    //				String parameterLoginSalt = request.getParameter("loginSalt");
    //				if (Helper.isEmpty(parameterLoginSalt)) {
    //					throw new UserException("Please try again. (Error Code 1)");
    //				}
    //				Hashtable sessionSalts = (Hashtable) session.getAttribute("loginSalts");
    //				if (sessionSalts == null) {
    //					String queryString = request.getQueryString();
    //					String visitorID = ServletUtil.getCookieValue(request, "visitorid");
    //					visitorID = visitorID == null ? "" : visitorID;
    //					getLogger("com.paycycle.saltMismatches").error(
    //							"nosessionsalts: " + request.getRequestURL() + (queryString == null ? "" : "?" + queryString) + " sessionid "
    //									+ session.getId() + " requestSalt " + parameterLoginSalt + " visitorid " + visitorID);
    //					throw new UserException("Please try again. (Error Code 2)");
    //				}
    //				if (sessionSalts.containsKey(parameterLoginSalt)) {
    //					session.removeAttribute("loginSalts");
    //				} else {
    //					String queryString = request.getQueryString();
    //					String visitorID = ServletUtil.getCookieValue(request, "visitorid");
    //					visitorID = visitorID == null ? "" : visitorID;
    //					getLogger("com.paycycle.saltMismatches").error(
    //							"salt mismatch: " + request.getRequestURL() + (queryString == null ? "" : "?" + queryString) + " sessionid "
    //									+ session.getId() + " requestSalt " + parameterLoginSalt + " sessionSalt(s) " + sessionSalts + " visitorid "
    //									+ visitorID);
    //					throw new UserException("Please try again. (Error Code 3)");
    //				}
    //			}
    //
    //			// first we do an extra access check. This is done on every page in
    //			// app_globals, but we want the special login error message to
    //			// appear
    //			// here. This is strategically placed before the force-change check,
    //			// so that a disabled employee in force-change state will still get
    //			// the
    //			// same error message. This check also applies for those trying to
    //			// log
    //			// into the main site without an appropriate role.
    //			if ((isEmployeeSiteLogin && !user.hasRole(RoleID.EMPLOYEE))
    //					|| (!isEmployeeSiteLogin && !(user.hasRole(RoleID.PROVIDER_ADMIN) || user.hasAccessRole()))) {
    //				throw new UserException("The information you entered does not match our records. Please verify your information.");
    //			}
    //
    //			// Check if an SSO user is trying to login on the public site, and
    //			// disallow it
    //			if ((user.getLogin().getSsoType() != Login.SSOType.None)) {
    //				throw new UserException("The information you entered does not match our records. Please verify your information.");
    //			}
    //
    //			// if it turns out that the password must be changed, make them do
    //			// it before they
    //			// are considered logged in, but after we are sure they are a real
    //			// user
    //			if (user.getLogin().getForceChange()) {
    //				String url;
    //
    //				// To allow access to forcedlogin.jsp page only after logged in
    //				// with the temporary password,
    //				// we insert the user id into the session and verify it on the
    //				// forcedlogin page.
    //				Long id = new Long(user.getId());
    //				session.setAttribute("ForcedLogin", id);
    //
    //				if (isEmployeeSiteLogin) {
    //					url = "/forcedChange.jsp?userId=" + user.getId() + "&userStrId=" + userStrId;
    //				} else {
    //					url = "/login/forcedChange.jsp?userId=" + user.getId() + "&userStrId=" + userStrId;
    //				}
    //				response.resetBuffer();
    //				request.setAttribute("PageRedirect", "true");
    //				response.sendRedirect(request.getContextPath() + url);
    //				AuthMgr.releaseLoginSession(user.getCompany(), request);
    //                                loginSuccessful = LOGIN_IGNORED;
    //				return false;
    //			}
    //
    //			// Check if the user is admin of a provider company, if so get the
    //			// Provider company
    //			Company company = null; // if the user is an employee, this will
    //			// stay null.
    //			Company workCompany = null; // this will be null unless the user is
    //			// an employee.
    //			boolean isSP = false;
    //
    //			if (isEmployeeSiteLogin) {
    //				workCompany = processEmployeeSiteLogin(user, session, request);
    //			} else if (user.hasRole(RoleID.CUSTOMER_SUPPORT) && Helper.isNotEmpty(request.getParameter("id"))) {
    //				// For public login, replace this user's Company ID with that of
    //				// the company they're accessing.
    //				String id = Encryption.decryptCompanySecrets(request.getParameter("id"));
    //				session.setAttribute("companyId", id);
    //				request.setAttribute("companyId", id);
    //				company = (Company) Helper.readObject(com.paycycle.biz.Company.class, Long.parseLong(id));
    //				if (company.isProvider()) {
    //					session.setAttribute("providerId", id);
    //					request.setAttribute("providerId", id);
    //					isSP = true;
    //				}
    //
    //				UserTransientData utd = UserTransientData.getUserTransientDataForPublicLogin(company);
    //				if (utd != null) {
    //					session.setAttribute(UserTransientData.SESSION_ATTR_NAME, utd);
    //				}
    //
    //				// Tell any UserEvent listener that CS User is logging in.
    //				UserEvent event = new UserEvent(user, user, new Date(), company);
    //				AppMgr.getEventMgr().broadcastLoginEvent(event);
    //			} else if (user.hasRole(RoleID.PROVIDER_ADMIN)) {
    //				// handle accountants (but not their employees)
    //				isSP = true;
    //				company = refreshCompany(user.getProviderCompany());
    //				String id = String.valueOf(company.getId());
    //				session.setAttribute("providerId", id);
    //				request.setAttribute("providerId", id);
    //
    //				session.setAttribute("companyId", id);
    //				request.setAttribute("companyId", id);
    //			} else if (user.hasAccessRole()) {
    //				company = refreshCompany(user.getCompany());
    //				session.setAttribute("companyId", String.valueOf(company.getId()));
    //				request.setAttribute("companyId", String.valueOf(company.getId()));
    //			} else {
    //				AuthMgr.releaseLoginSession(company, request);
    //				throw new UserException("The information you entered does not match our records. Please verify your information.");
    //			}
    //            loginSuccessful = LOGIN_SUCCEEDED;
    //
    //            //reset the index in case this company is already cached on this server and a contractor could have been added on another
    //            if (company != null) company.resetContractorIndex(true);
    //
    //            session.setAttribute("userId", String.valueOf(user.getId()));
    //			request.setAttribute("userId", String.valueOf(user.getId()));
    //
    //			// encrypt the company's source code before finding the partner
    //			String sc;
    //			Partner p;
    //			if (!isEmployeeSiteLogin) {
    //				sc = Encryption.encryptCode(company.getSourceCode());
    //				p = PartnerMgr.getPartner(sc, company.getId());
    //			} else {
    //				sc = Encryption.encryptCode(workCompany.getSourceCode());
    //				p = PartnerMgr.getPartner(sc, workCompany.getId());
    //			}
    //			// For Brandless companies, change the source code before we write
    //			// the cookie
    //			if (p.getId() == Partner.MANAGEPAYROLL) {
    //				sc = Encryption.encryptCode(Partner.MANAGEPAYROLL_SOURCECODE);
    //			}
    //			session.setAttribute("partnerId", String.valueOf(p.getId()));
    //			request.setAttribute("partnerId", String.valueOf(p.getId()));
    //
    //            // Here the partner is determined based on source code.
    //            // But to determine whether to set HTTPOnly flag on cookie, I determine partner using HostToPartner mapping.
    //			ServletUtil.setCookieValue(request, response, "SC", sc);
    //
    //			// remember the last login name used in a cookie on the user's machine
    //			ServletUtil.setCookieValue(request, response, "USERSTRID", userStrId);
    //
    //            if (company != null) {
    //                VisitorTx vtx = new VisitorTx();
    //                vtx.assignCompanyToVisitor(company.getId(), ServletUtil.getCookieValue(request, VisitorInfoServlet.VISITOR_ID_COOKIE));
    //            }
    //
    //			// remember in the db when this person last logged in
    //			setLastLogin(user, request);
    //
    //			// Intercept companies that need to use PhoneFactor.  Unset their userId and
    //			// companyId attrs in the session so that they can't jump away from the
    //			// PhoneFactor auth page into the rest of the app (because the rest of the
    //			// app uses those attrs to determine if the user is logged in).  But save the
    //			// values so that we can put them back after PF Auth succeeds.
    //			if (user.getCompany().getSecondFactorType() == Login.SecondFactorType.PhoneFactor) {
    //				// For now we're only authenticating the primary contact.
    //				if (user.getId() == user.getCompany().getContact().getId()) {
    //					if (session.getAttribute("PhoneFactorLoggedIn") == null) {
    //						String url = "/login/phoneFactorAuth.jsp";
    //
    //						session.setAttribute("PhoneFactorUser", user);
    //						session.setAttribute("PhoneFactorCompanyId", user.getCompany().getId());
    //						session.setAttribute("PhoneFactorProviderId", session.getAttribute("providerId"));
    //						if (isSP) {
    //							session.setAttribute("PhoneFactorIsSP", "true");
    //						} else {
    //							session.setAttribute("PhoneFactorIsSP", null);
    //						}
    //						if (isEmployeeSiteLogin) {
    //							session.setAttribute("PhoneFactorIsEmployeeSiteLogin", "true");
    //						} else {
    //							session.setAttribute("PhoneFactorIsEmployeeSiteLogin", null);
    //						}
    //
    //						session.setAttribute("userId", null);
    //						session.setAttribute("companyId", null);
    //						session.setAttribute("providerId", null);
    //
    //						response.resetBuffer();
    //						request.setAttribute("PageRedirect", "true");
    //						response.sendRedirect(request.getContextPath() + url);
    //                                                loginSuccessful = LOGIN_IGNORED;
    //						return true;
    //					}
    //				}
    //			}
    //
    //			if (isSP && !isEmployeeSiteLogin) {
    //				boolean isFromElink = request.getRequestURI().toLowerCase().endsWith("elink.jsp");
    //
    //				// if the request is not within the sp realm or elink, default
    //				// to client page
    //				if (!isFromElink && request.getRequestURI().indexOf("/sp/") < 0) {
    //					redirectToSPTodoList(request, response, company);
    //				}
    //			}
    //
    //			// If there is an error set the session vars as appropriate.
    //		} catch (Exception ex) {
    //                        request.setAttribute("error", ex);
    //
    //		} finally {
    //                    if( request.getParameter("userStrId")!=null && request.getParameter("companyId")==null ) {
    //                        User user = AuthMgr.lookUpStandardUser(request.getParameter("userStrId"));
    //                        if( user!=null ) {
    //                            Helper.logIPAddressLogin(request, user, loginSuccessful==LOGIN_SUCCEEDED);      // Record bad login
    //                        }
    //                    }
    //                }
    //		return true;
    //	}
    //
    //    private static Company processEmployeeSiteLogin(User user, HttpSession session, HttpServletRequest request) {
    //        // Note that logging in to the employee site should be thought
    //        // of as
    //        // the same as logging in to the regular site, only you get more
    //        // session
    //        // variables. So logging in to either place will provide the
    //        // user with
    //        // everything they need to view the public site.
    //        // Everyone who logs into the employee site should have
    //        // an Employee object or Contractor object.
    //        // Employees and users have the same ID space, so we pull the
    //        // employee
    //        // out of the database using the user's ID.
    //        Company workCompany = null;
    //        if (user.getUserType() == UserType.EMPLOYEE) {
    //            Employee emp = (Employee) Helper.readObject(Employee.class, user.getId());
    //            session.setAttribute("employeeId", String.valueOf(emp.getId()));
    //            request.setAttribute("employeeId", String.valueOf(emp.getId()));
    //            workCompany = emp.getWorkCompany();
    //        } else if (user.getUserType() == UserType.CONTRACTOR) {
    //            Contractor contractor = Helper.readObject(Contractor.class, new ExpressionBuilder().get("m_contact").equal(user));
    //            session.setAttribute("contractorId", String.valueOf(contractor.getId()));
    //            request.setAttribute("contractorId", String.valueOf(contractor.getId()));
    //            workCompany = contractor.getWorkCompany();
    //        // For time tracking, the company's primary contact user can now be used to login
    //        // to the employee site for approving employees' time sheets.
    //        } else if (user.getUserType() == UserType.OTHER) {
    //        	workCompany = user.getCompany();
    //        }
    //        workCompany = (Company) Helper.refreshObject(workCompany); // force to get new company at each login.
    //        session.setAttribute("workCompanyId", String.valueOf(workCompany.getId()));
    //        request.setAttribute("workCompanyId", String.valueOf(workCompany.getId()));
    //        // we need a separate "companyId" and "workCompanyId" because
    //        // employees of
    //        // accountants have both. workCompanyId only applies to the
    //        // employee site,
    //        // and companyId only applies to the main site.
    //        session.setAttribute("companyId", "-1");
    //        request.setAttribute("companyId", "-1");
    //        return workCompany;
    //    }
    //
    //	public static void redirectToSPTodoList(HttpServletRequest request, HttpServletResponse response, Company company) {
    //		Provider sp = (Provider) company;
    //		response.resetBuffer();
    //		request.setAttribute("PageRedirect", "true");
    //		String path = "/sp/client/default.jsp";
    //		if (!sp.hasClients() &&                                                     // No clients
    //				!company.getPaycheckHistory().isNonHistoricalPaycheckAvailable() && // No regular paychecks
    //				(!company.getPaycheckHistory().isHistoryComplete() ||               // History incomplete
    //				Helper.isEmpty(company.getSettings().getPayTypes()) ||              // No paytypes
    //				(company.getNumEmployees() + company.getNumContractors()) <= 0      // No ees or contractors
    //				)) {
    //			path = "/in/welcome.jsp";
    //		}
    //		if (sp.getPreferences().isClientTodoListAsFirstPage()) {
    //			path = "/sp/client/clientTodo.jsp";
    //		}
    //
    //		try {
    //			response.sendRedirect(request.getContextPath() + path);
    //		} catch (IOException ex) {
    //		}
    //
    //	}
    //
    //	/**
    //	 * save to the db the info about when this user last logged in
    //	 */
    //	private static void setLastLogin(User user, HttpServletRequest request) {
    //		Transaction tx;
    //		User u;
    //                Date today = new Date();
    //
    //		tx = getDbTransaction();
    //		u = (User) tx.registerObject(user);
    //		u.getLogin().setLastLogin(today);
    //		u.getLogin().setLastBrowser(request.getHeader("User-Agent"));
    //		tx.commit();
    //
    //		Company company = user.getCompany();
    //		if (!AppMgr.isOperations() && !AppMgr.isEmployeeSite() && PartnerMgr.isBOA(company) &&
    //                    company.getStatus().getStatus(CompanyStatus.BOA_FIRST_LOGIN, "").length() == 0 &&
    //                    today.compareTo(DateUtil.getStartOfNextDay(company.getSignupDate())) >= 0) {
    //			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    //			String sqlInsert = "Insert into ObjectAttributes values (#P, #P, #P, #P, #P)";
    //			Helper.executeModifyQuery(sqlInsert, company.getId(), "C", "BOA_FIRST_LOGIN", sdf.format(today), today);
    //			company.getStatus().setStatus(CompanyStatus.BOA_FIRST_LOGIN, sdf.format(today));
    //		}
    //	}
    //
    //	/**
    //	 * Set up session info for sample company
    //	 */
    //	public static void startSample(HttpSession session) {
    //		session.setAttribute("isSample", "true");
    //		initDbCatalog(session);
    //	}
    //
    //	/**
    //	 * Remove session info for sample company
    //	 */
    //	public static void endSample(HttpSession session) {
    //		session.removeAttribute("isSample");
    //		initDbCatalog(session);
    //		ServiceLayerHelper.getSetupWebServiceInstance().clear();
    //	}
    //
    //	/**
    //	 * Return if it is the sample session
    //	 */
    //	public static boolean isSample(HttpSession session) {
    //		return Helper.isNotEmpty(session.getAttribute("isSample"));
    //	}
    //
    //    /**
    //     * return true if the sp_prepare and sp_execute should be turned off
    //     */
    //    public static boolean disablePreparedStatement() {
    //        final String PROP_NAME = "app.db.disablePreparedStatement";
    //        final String SYS_PROP = System.getProperty(PROP_NAME, "");
    //        return TypeUtil.toBoolean(getProperty(PROP_NAME)) || SYS_PROP.equals("true");
    //    }
    //
    //	/**
    //	 * Returns if the current context is pointing to the sample database.
    //	 */
    //	public static boolean isSampleDB() {
    //		if (isProduction() || TypeUtil.toBoolean(getProperty("app.QA.flipsampledb"))) {
    //			return getDbCatalog().equals(getProperty("app.db.catalog." + SAMPLE));
    //		} else {
    //			/*
    //			 * If not production, we may not be able to distinguish between the
    //			 * sample database vs. the main database. Currently, both are
    //			 * pointing to the same database in the development environment.
    //			 */
    //			return false;
    //		}
    //	}
    //
    //	/**
    //	 * Returns the property value defined in appglobals.properties
    //	 */
    //	public static String getProperty(String name) {
    //		return PropertiesManager.getProperty(name);
    //	}
    //
    //	/**
    //	 * Returns the property value defined in the PayCycleProperties database
    //	 * table
    //	 */
    //	public static String getDatabaseProperty(String name) {
    //		return m_dbProperties.getProperty(name);
    //	}
    //
    //	/**
    //	 * Returns the property value defined in the PayCycleProperties database
    //	 * table with always refreshed DB data
    //	 */
    //	public static String getDatabasePropertyNow(String name) {
    //		return m_dbProperties.getPropertyNow(name);
    //	}
    //
    //	/**
    //	 * Returns the property value defined in appglobals.properties
    //	 */
    //	public static String getLandingProperty(String name) {
    //		return PropertiesManager.getProperty(PropertiesManager.Category.landingPageTests, name);
    //	}
    //
    //	/**
    //	 * Based on the application context, determine the application name
    //	 */
    //	public static String getAppName() {
    //		/** Get threadlocal storage */
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		return (String) ctx.get(APPNAME);
    //	}
    //
    //    public static String getSystemType() {
    //        return PropertiesManager.getSystemType();
    //    }
    //
    //	public static String getAppNameForProfiler() {
    //		String appName;
    //		try {
    //			appName = AppMgr.getAppName();
    //		} catch (Exception ex) {
    //			appName = UNKNOWN_APPNAME;
    //		}
    //		if (AppMgr.isOperations()) {
    //			appName = OPS + appName;
    //		}
    //		return appName;
    //	}
    //
    //	/**
    //	 * Determine if the request hits production server.
    //	 */
    //	public static boolean isProduction() {
    //		return getSystemType().equals("prod");
    //	}
    //
    //	/**
    //	 * Determine if the request hits perf server.
    //	 */
    //	public static boolean isPerf() {
    //		return getSystemType().equals("perf");
    //	}
    //
    //	/**
    //	 * Determine if the request hits operations server.
    //	 */
    //	public static boolean isOperations() {
    //		return TypeUtil.toBoolean(PropertiesManager.getProperty("app.state.isOperations"));
    //	}
    //
    //	public static boolean isEmployeeSite() {
    //		return TypeUtil.toBoolean(PropertiesManager.getProperty("app.state.isEmployeeSite"));
    //	}
    //
    //	public static boolean isFullService() {
    //		return TypeUtil.toBoolean(PropertiesManager.getProperty("app.state.isFullService", "false"));
    //	}
    //
    //	/**
    //	 * Determine if the request hits development server.
    //	 */
    //	public static boolean isDevelopment() {
    //        return getSystemType().equals("dev");
    //	}
    //
    //	/**
    //	 * Determine if PDF forms are streamed to the client or a temp file is
    //	 * created...
    //	 */
    //	public static boolean isStreamPdfForm() {
    //		return TypeUtil.toBoolean(PropertiesManager.getProperty("app.state.isStreamPdfForm"));
    //	}
    //
    //	/**
    //	 * Determine if error would be auto logged
    //	 */
    //	public static boolean isErrorAutoLogged() {
    //		return TypeUtil.toBoolean(PropertiesManager.getProperty("app.error.autologged"));
    //	}
    //
    //	/**
    //	 * Determine if the request is of "main" context.
    //	 */
    //	public static boolean isMain() {
    //		return getAppName().equals(PAYCYCLE);
    //	}
    //
    //	/**
    //	 * Get the database driver name from configuration file
    //	 */
    //	public static String getDbDriver() {
    //		return PropertiesManager.getProperty("app.db.driver");
    //	}
    //
    //	/**
    //	 * Get the JDBC database URL from configuration file NOTE: If the sample
    //	 * context then url for sample db is returned.
    //	 */
    //	public static String getDbUrl() {
    //		boolean sampleCatalogIsTheSameAsTest = getProperty("app.db.catalog.sample") != null && getProperty("app.db.catalog.sample").equalsIgnoreCase(getProperty("app.db.catalog.test"));
    //		String catalog = getDbCatalog();
    //		return Helper.isNotEmpty(catalog) && catalog.equalsIgnoreCase(getProperty("app.db.catalog." + SAMPLE)) && !sampleCatalogIsTheSameAsTest ? getSampleDbUrl()
    //				: PropertiesManager.getProperty("app.db.url").trim();
    //	}
    //
    //	/**
    //	 * Get the JDBC sample database URL from configuration file
    //	 */
    //	public static String getSampleDbUrl() {
    //		// If sample url is not defined, use the normal db url
    //		return PropertiesManager.getProperty("app.db.sample.url") == null ? PropertiesManager.getProperty("app.db.url").trim() : PropertiesManager.getProperty("app.db.sample.url").trim();
    //	}
    //
    //	/**
    //	 * Get the database username from configuration file
    //	 */
    //	public static String getDbUsername() {
    //		return PropertiesManager.getProperty("app.db.username");
    //	}
    //
    //	/**
    //	 * Based on the application context, determine the database password
    //	 */
    //	public static String getDbPassword() {
    //		return Encryption.decryptProperty(PropertiesManager.getProperty("app.db.password"));
    //	}
    //
    //	/**
    //	 * Based on the application context, determine the document root path
    //	 */
    //	public static String getDocPath() {
    //		return getContextRoot();
    //	}
    //
    //    public static String getImageUploadPath() {
    //        return System.getProperty("java.io.tmpdir");
    //    }
    //
    //    public static String getLuceneRoot() {
    //        return isDevelopment() ? System.getProperty("java.io.tmpdir") : PropertiesManager.getProperty("app.paths.luceneRoot");
    //    }
    //
    //    public static String getMaxMindPath() {
    //        return getResourcesPath()+PropertiesManager.getProperty("app.paths.db.maxmind");
    //    }
    //
    //	/**
    //	 * Get the Vertex database path from configuration file
    //	 */
    //	public static String getVertexDbPath() {
    //        // some versions of vertex require strict windows pathing
    //        // ie c:\foo\bar, not c:/foo/bar.  therefore normalize via File.getCannonicalPath()
    //        try {
    //            final String systemPath  = new File(getResourcesPath(), "Vertex").getCanonicalPath();
    //            return systemPath;
    //        } catch (IOException ex) {
    //            return null;
    //        }
    //	}
    //
    //	/**
    //	 * Get the path for temporary pdf files from configuration file
    //	 */
    //	public static String getTempPdfPath() {
    //		if (isDevelopment()) {
    //			return System.getProperty("java.io.tmpdir");
    //		}
    //		return PropertiesManager.getProperty("app.paths.clientPDF");
    //	}
    //
    //    public static String getResourcesPath() {
    //        try {
    //        	String resourcePath = PropertiesManager.getProperty("app.paths.resources");
    //        	if (resourcePath.equals("relative") && isDevelopment()) {
    //        		return new File(getContextRoot(), "../../../Resources").getCanonicalPath().trim();
    //        	} else {
    //                    if ( ! isWindows() ) {
    //                        final String linuxResources = PropertiesManager.getProperty("app.paths.resources.linux");
    //                        if (linuxResources != null) {
    //                            resourcePath = linuxResources;
    //                        }
    //                    }
    //                    return resourcePath.trim();
    //        	}
    //        } catch (IOException ex) {
    //            return null;
    //        }
    //    }
    //	/**
    //	 * Get the forms path from configuration file
    //	 */
    //	public static String getFormsPath() {
    //		return getResourcesPath()+PropertiesManager.getProperty("app.paths.forms");
    //	}
    //
    //	/**
    //	 * Get the export path from configuration file
    //	 */
    //	public static String getExportPath() {
    //		return getContextRoot()+PropertiesManager.getProperty("app.paths.export");
    //	}
    //
    //	/**
    //	 * Get the import path from configuration file
    //	 */
    //	public static String getImportPath() {
    //		return getContextRoot()+PropertiesManager.getProperty("app.paths.import");
    //	}
    //
    //	public static String getHRContentResourcePath() {
    //		return getResourcesPath()+PropertiesManager.getProperty("app.paths.hrcontent.resources");
    //	}
    //
    //
    //
    //	public static String getFlashDemoPath(HttpServletRequest request, String demoPath, boolean secure) {
    //		String context = request.getContextPath();
    //		String server = request.getServerName();
    //		if (isDevelopment()) {
    //			String port = Integer.toString(request.getServerPort());
    //			return ((secure) ? "https://" : "http://") + server + ":" + port + context + "/" + demoPath;
    //		}
    //		else
    //			return ((secure) ? "https://" : "http://") + server + context + "/" + demoPath;
    //	}
    //
    //	public static String getSBFlashDemoPath(HttpServletRequest request) {
    //		return (isLoggedIn(request)) ? getFlashDemoPath(request, "help/videoLauncher.jsp?flashName=../demo/FlashDemo/SBInternalFlashDemo.swf", false)
    //				: getFlashDemoPath(request,	"help/videoLauncher.jsp?flashName=../demo/FlashDemo/SBExternalFlashDemo.swf", false);
    //	}
    //
    //
    //	public static String getACFlashDemoPath(HttpServletRequest request) {
    //		return (isLoggedIn(request)) ? getFlashDemoPath(request, "help/videoLauncher.jsp?flashName=../demo/FlashDemo/ACInternalFlashDemo.swf",	false)
    //				: getFlashDemoPath(request, "help/videoLauncher.jsp?flashName=../demo/FlashDemo/ACExternalFlashDemo.swf", false);
    //	}
    //
    //	public static String getSBFlashDemoPath(HttpServletRequest request, Partner partner) {
    //		String demoUrl = null;
    //		if (isLoggedIn(request)) {
    //			demoUrl = partner.getTextAttribute(Partner.ATTR_SB_INTERNAL_DEMO_PAGE);	// Partner.ATTR_SB_INTERNAL_DEMO_PAGE contains a leading '/' character so the string
    //
    //		} else {
    //			demoUrl = partner.getTextAttribute(Partner.ATTR_SB_EXTERNAL_DEMO_PAGE);	// Partner.ATTR_SB_INTERNAL_DEMO_PAGE contains a leading '/' character so the string
    //		}
    //
    //		if (partner.isBOAPartner()){ // BOA has its own demo url
    //			return demoUrl;
    //		}else if (demoUrl.startsWith("/")){											// can be used by GatewayServlet.java correctly.  Remove it since getFlashDemoPath()
    //			demoUrl = demoUrl.substring("/".length());							// concatenates with a leading '/' character
    //		}
    //
    //		return getFlashDemoPath(request, demoUrl, true);
    //	}
    //	public static String getSetupSBFlashDemoPath(HttpServletRequest request, Partner partner) {
    //			String demoUrl = null;
    //			demoUrl = partner.getTextAttribute(Partner.ATTR_SB_INTERNAL_DEMO_PAGE);	// Partner.ATTR_SB_INTERNAL_DEMO_PAGE contains a leading '/' character so the string
    //			if (demoUrl.startsWith("/"))											// can be used by GatewayServlet.java correctly.  Remove it since getFlashDemoPath()
    //				demoUrl = demoUrl.substring("/".length());							// concatenates with a leading '/' character
    //
    //			return getFlashDemoPath(request, demoUrl, false);
    //	}
    //	public static String getACFlashDemoPath(HttpServletRequest request, Partner partner) {
    //			String demoUrl = null;
    //        if (isLoggedIn(request)){
    //            demoUrl = getFlashDemoPath(request, partner.getTextAttribute(Partner.ATTR_AC_INTERNAL_DEMO_PAGE), false);
    //        }else{
    //            demoUrl = getFlashDemoPath(request, partner.getTextAttribute(Partner.ATTR_AC_EXTERNAL_DEMO_PAGE), false);
    //        }
    //		return demoUrl;
    //	}
    //
    //	public static String getMailURL() {
    //		return getProperty("app.ops.mailServletHost") + "/"
    //				+ ((AppMgr.isMain() || AppMgr.isDevelopment()) ? "" : AppMgr.getAppName() + "/") + getProperty("app.ops.mailUrl");
    //	}
    //
    //	public static String getBillingURL() {
    //		return getProperty("app.ops.billingServletHost") + "/"
    //				+ ((AppMgr.isMain() || AppMgr.isDevelopment()) ? "" : AppMgr.getAppName() + "/") + getProperty("app.ops.billingUrl");
    //	}
    //
    //	public static String getContactMsg() {
    //		return getProperty("app.contactMessage");
    //	}
    //
    //	public static String getPaycheckRecordURL(HttpServletRequest request, Partner partner) {
    //
    //		// Google is the only partner has its own paycheck record site
    //		if (partner.isGooglePartner()){
    //			String url = PropertiesManager.getProperty(PropertiesManager.Category.Google, "google.pcr.root");
    //			return url;
    //		}
    //
    //		// for local dev, we return http://localhost:8080/employeesite/
    //		StringBuffer eeUrl = new StringBuffer();
    //		eeUrl.append(isDevelopment() ? "http" : "https");
    //		eeUrl.append("://");
    //		eeUrl.append(getProperty("app.employeesitehost"));
    //		if (isDevelopment()){
    //			eeUrl.append(":");
    //			eeUrl.append(request.getServerPort());
    //			eeUrl.append("/employeesite/");
    //		}else{
    //			if(Helper.isNotEmpty(request.getContextPath())){
    //				eeUrl.append(request.getContextPath());
    //			}
    //			eeUrl.append("/");
    //		}
    //
    //		return eeUrl.toString();
    //	}
    //
    //	/**
    //	 * Prospect Conversion (2007R2.1): retrieve values from properties files for
    //	 * setting whether a new company gets the new sales phone number in left nav
    //	 * or not
    //	 *
    //	 */
    //	public static int getShowSalesPhoneNumberPercent() {
    //		return Integer.valueOf(getProperty("app.conversion.show.sales.phone.percent"));
    //	}
    //
    //	public static String getShowSalesPhoneNumberStatusYesValue() {
    //		return getProperty("app.conversion.show.sales.phone.yes.value");
    //	}
    //
    //	public static String getShowSalesPhoneNumberStatusNoValue() {
    //		return getProperty("app.conversion.show.sales.phone.no.value");
    //	}
    //
    //	public static String getSalesPhoneNumber() {
    //		return getProperty("app.conversion.sales.phone.number");
    //	}
    //
    //	public static String getCompanyDeletionBatchSize() {
    //		return getProperty("app.company_deletion.batchsize");
    //	}
    //
    //	/***************************************************************************
    //	 * **************************** Logging ***********************************
    //	 **************************************************************************/
    //
    //	/**
    //	 * Initialize Log Manager
    //	 */
    //	private static void addHandler(String name, Handler handler) {
    //		if (LogManager.getLogManager().getLogger(name) != null) {
    //			LogManager.getLogManager().getLogger(name).addHandler(handler);
    //		}
    //	}
    //
    //	/**
    //	 * Returns the application's logger instance
    //	 */

    public static SpcfLogger getLogger() {
        //KP - return Logger.getDefaultLogger();
        return SpcfLogManager.getLogger(AppMgr.class);
    }
    //
    //	public static Handler getHandler(String name) {
    //		LogManager mgr = LogManager.getLogManager();
    //		final String JVMID = getJVMID();
    //		String loggerDirectoryName = mgr.getProperty("root") + File.separator + JVMID;
    //		new File(loggerDirectoryName).mkdirs();
    //		String loggerPathName = loggerDirectoryName + "/" + mgr.getProperty(name + ".FileHandler.pattern");
    //
    //		Handler handler = null;
    //		try {
    //			// Set up the log handler, according to logging properties
    //			handler = new FileHandler(loggerPathName, Integer.parseInt(mgr.getProperty(name + ".FileHandler.limit")), Integer.parseInt(mgr
    //					.getProperty(name + ".FileHandler.count")), Boolean.parseBoolean(mgr.getProperty(name + ".FileHandler.append")));
    //		} catch (IOException ex) {
    //		}
    //		handler.setLevel(Level.parse(mgr.getProperty(name + ".FileHandler.level")));
    //
    //		try {
    //			handler.setFormatter((java.util.logging.Formatter) Class.forName(mgr.getProperty(name + ".FileHandler.formatter")).newInstance());
    //		} catch (InstantiationException ex) {
    //			handler = null;
    //		} catch (ClassNotFoundException ex) {
    //			handler = null;
    //		} catch (IllegalAccessException ex) {
    //			handler = null;
    //		}
    //
    //		return handler;
    //	}
    //
    //	public static Logger getLogger(Logger.LoggerInstance instance) {
    //		return getLogger(instance.value());
    //	}
    //

    public static SpcfLogger getLogger(String name) {
        return SpcfLogManager.getLogger(name);
    }

    /**
     * Returns Logger instance by class
     */
    public static SpcfLogger getLogger(Class clz) {
        return getLogger(clz.getName());
    }
    //
    //	/***************************************************************************
    //	 * **************************** Profiling
    //	 * ***********************************
    //	 **************************************************************************/
    //
    //	public static ProfileHelper getProfileHelper() {
    //		return m_profileHelper;
    //	}
    //
    //	/***************************************************************************
    //	 * ************************* DB functions *********************************
    //	 **************************************************************************/
    //
    //	/**
    //	 * Return the DB catalog assigned to this thread
    //	 */
    //	public static String getDbCatalog() {
    //		/** Get threadlocal storage */
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		if (ctx != null) {
    //			return (String) ctx.get(DBCATALOG);
    //		}
    //		return null;
    //	}
    //
    //	public static void initDbTime(String uri) {
    //		/** Get threadlocal storage */
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		if (ctx != null) {
    //			ctx.put(DBTIME, new ProfileLong());
    //			if (m_profileDebug) {
    //				ctx.put("URI", uri);
    //				getLogger().info("init DBTime: uri: " + uri + " " + m_threadLocal);
    //			}
    //		}
    //	}
    //
    //	public static Long getDbTime(String uri) {
    //		ProfileLong value = null;
    //		/** Get threadlocal storage */
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		if (ctx != null) {
    //			value = (ProfileLong) ctx.get(DBTIME);
    //			ctx.remove(DBTIME); // Clear the DBTIME
    //			if (m_profileDebug) {
    //				if (value != null) {
    //					getLogger().info("get DBTime: " + value.longValue() + " uri: " + uri + " ctx_uri: " + ctx.get("URI") + " " + m_threadLocal);
    //				} else {
    //					getLogger().info("get DBTime: 0 " + " uri: " + uri + " ctx_uri: " + ctx.get("URI") + " " + m_threadLocal);
    //				}
    //			}
    //		}
    //		if (value != null) {
    //			return value.getLongValue();
    //		} else {
    //			return (long) 0;
    //		}
    //	}
    //
    //	public static long updateDbTime(long value) {
    //		/** Get threadlocal storage */
    //		Hashtable ctx = (Hashtable) m_threadLocal.get();
    //		if (ctx != null) {
    //			ProfileLong curValue = (ProfileLong) ctx.get(DBTIME);
    //			if (curValue != null) {
    //				curValue.add(value);
    //			} else {
    //				curValue = new ProfileLong(value);
    //				ctx.put(DBTIME, curValue);
    //			}
    //			if (m_profileDebug) {
    //				getLogger().info("update DBTime (total): " + curValue + " uri: " + ctx.get("URI") + " " + m_threadLocal);
    //			}
    //		} else {
    //			if (m_profileDebug) {
    //				getLogger().info("update DBTime (none) " + value + " uri: null");
    //			}
    //		}
    //		return value;
    //	}
    //
    //	/**
    //	 * Return the number of DB write connection to be set in the write
    //	 * connection pool
    //	 */
    //	public static int getNumWriteConnections() {
    //		return Integer.parseInt(PropertiesManager.getProperty("app.db.numWriteConnections"));
    //	}
    //
    //	/**
    //	 * Return the number of DB write connection to be set in the write
    //	 * connection pool
    //	 */
    //	public static int getNumReadConnections() {
    //		return Integer.parseInt(PropertiesManager.getProperty("app.db.numReadConnections"));
    //	}
    //
    //	/**
    //	 * TOPLink - Login to database and get a UnitOfWork wrapper
    //	 */
    //	public static Transaction getDbTransaction() {
    //		Session session = getDbSession();
    //		Transaction tx = new Transaction(session);
    //		session.release();
    //		return tx;
    //	}
    //
    //	/**
    //	 * TOPLink - Login to database and get a session
    //	 */
    //	public static Session getDbSession() {
    //		long before = System.currentTimeMillis();
    //		Session session = getDBServer(getDbCatalog()).acquireClientSession();
    //		long after = System.currentTimeMillis();
    //		AppMgr.updateDbTime(after - before);
    //		return session;
    //	}
    //
    //	/**
    //	 * TOPLink - Login to database and get a session
    //	 */
    //	public static Session getDbSession(Catalog catalog) {
    //		String url = null;
    //
    //		if (catalog == Catalog.PROFILE) {
    //			url = LogManager.getLogManager().getProperty("com.paycycle.profile.ProfileHandler.dbUrl");
    //		}
    //
    //		if (catalog == Catalog.EMAIL) {
    //			url = getProperty("app.db.email.url");
    //		}
    //
    //		return getDbSession(url, getCatalogName(catalog));
    //	}
    //
    //	public static String getCatalogName(Catalog catalog) {
    //		return catalog == Catalog.PROFILE ? LogManager.getLogManager().getProperty("com.paycycle.profile.ProfileHandler.catalog") : AppMgr
    //				.getProperty("app.db.catalog." + catalog.toString().toLowerCase());
    //	}
    //
    //	public static Session getDbSession(String catalog) {
    //		return getDbSession(null, catalog);
    //	}
    //
    //	public static Session getDbSession(String dbUrl, String catalog) {
    //		long before = System.currentTimeMillis();
    //		Session session = getDBServer(dbUrl, catalog).acquireClientSession();
    //		long after = System.currentTimeMillis();
    //		AppMgr.updateDbTime(after - before);
    //		return session;
    //	}
    //
    //	/**
    //	 * TOPLink - Enable toplink logging
    //	 */
    //	public static boolean enableDbLogging(Transaction tx) {
    //		synchronized (tx) {
    //			if (tx != null) {
    //				UnitOfWork uow = tx.getUnitOfWork();
    //				TopLinkSessionLog log = new TopLinkSessionLog();
    //				uow.setSessionLog(log);
    //				log.setSession(uow.getActiveSession());
    //				uow.setLogLevel(SessionLog.FINE);
    //				return true;
    //			} else {
    //				return false;
    //			}
    //		}
    //	}
    //
    //	/**
    //	 * TOPLink - Disable toplink logging
    //	 */
    //	public static boolean disableDbLogging(Transaction tx) {
    //		synchronized (tx) {
    //			if (tx != null) {
    //				tx.getUnitOfWork().dontLogMessages();
    //				return true;
    //			} else {
    //				return false;
    //			}
    //		}
    //	}
    //
    //	/**
    //	 * Determines from the server session if the object identified by the
    //	 * specified id is already in the object cache
    //	 */
    //	static public boolean containsObjectInCache(String className, long id) {
    //		boolean result = false;
    //		try {
    //			Session session = getDbSession();
    //			Class clz = Class.forName(className);
    //			Vector keys = new Vector();
    //			keys.add(id);
    //			result = session.getIdentityMapAccessor().containsObjectInIdentityMap(keys, clz);
    //		} catch (Exception ex) {
    //			getLogger().error(ex);
    //		}
    //		return result;
    //	}
    //
    //	/**
    //	 * Determines from the server session if the object identified by the
    //	 * specified id is already in the object cache
    //	 */
    //	static public boolean containsObjectInCache(Object object) {
    //		boolean result = false;
    //		try {
    //			Session session = getDbSession();
    //			result = session.getIdentityMapAccessor().containsObjectInIdentityMap(object);
    //		} catch (Exception ex) {
    //			getLogger().error(ex);
    //		}
    //		return result;
    //	}
    //
    //	/**
    //	 * Refreshes the object identified by the specified id. This forces TOPLink
    //	 * to read object (including objects it references) from database.
    //	 *
    //	 * @return the refreshed object.
    //	 */
    //	static public Object refreshObject(Object o) {
    //		return Helper.refreshObject(o);
    //	}
    //
    //	/**
    //	 * Refreshes the company and its employees.
    //	 *
    //	 * @return the refreshed company.
    //	 */
    //	static public Company refreshCompany(Company o) {
    //		Company co = (Company) AppMgr.refreshObject(o);
    //		Vector emps = co.getEmployees();
    //		for (int i = 0; i < emps.size(); i++) {
    //			AppMgr.refreshObject(emps.get(i));
    //		}
    //
    //		return co;
    //	}
    //
    //	/**
    //	 * Return the Toplink database server associated with the applcation. Create
    //	 * the server if necessary.
    //	 */
    //	protected static synchronized Server getDBServer(String catalog) {
    //		return getDBServer(null, catalog);
    //	}
    //
    //	protected static synchronized Server getDBServer(String dbUrl, String catalog) {
    //		Server server = m_dbServers.get(catalog);
    //		if (server == null) {
    //
    //			boolean isProfileCatalog = Helper.isNotEmpty(catalog) && catalog.equalsIgnoreCase(AppMgr.getCatalogName(Catalog.PROFILE));
    //			boolean isMainCatalog = Helper.isNotEmpty(catalog) && catalog.equalsIgnoreCase(AppMgr.getCatalogName(Catalog.PAYCYCLE));
    //			boolean isEmailCatalog = Helper.isNotEmpty(catalog) && catalog.equalsIgnoreCase(AppMgr.getCatalogName(Catalog.EMAIL));
    //
    //			Project project = null;
    //			project = new TOPLinkProject();
    //
    //			DatabaseLogin login = project.getLogin();
    //
    //			// Connect to the database catalog
    //			if (dbUrl == null) {
    //				dbUrl = getDbUrl();
    //			}
    //			login.setDatabaseURL(dbUrl + "?database=" + catalog);
    //			login.setUserName(AppMgr.getDbUsername());
    //			login.setPassword(AppMgr.getDbPassword());
    //			if (disablePreparedStatement()) {
    //				login.setProperty("prepare", "false");
    //			}
    //
    //			// Make field names case insensitive
    //			DatabaseLogin.setShouldIgnoreCaseOnFieldComparisons(true);
    //
    //			// Create a server session with predefined number of write
    //			// connections
    //			server = project.createServerSession( isMainCatalog ? getNumWriteConnections() : 5, getNumWriteConnections());
    //
    //			SessionLog javaLog = new EclipseLinkLogger();
    //			javaLog.setLevel(JavaLog.FINE);
    //			server.setSessionLog(javaLog);
    //
    //			server.getEventManager().addListener(new SessionEventHandler());
    //
    //			// Set the predefined number of read connections
    //			if (TypeUtil.toBoolean(getProperty("app.toplink.exclusiveConnectionPool"))) {
    //				server.useExclusiveReadConnectionPool(getNumReadConnections(), getNumReadConnections());
    //				AppMgr.getLogger().info("Using exclusive connection pooling");
    //			} else {
    //				ConnectionPool readPool = server.getReadConnectionPool();
    //				readPool.setMinNumberOfConnections( isMainCatalog ? getNumReadConnections() : 5);
    //				readPool.setMaxNumberOfConnections(getNumReadConnections());
    //			}
    //
    //			login.setShouldBindAllParameters(true);
    //			login.setShouldCacheAllStatements(true);
    //
    //			// Login to TOPLink server session
    //			server.login();
    //			java.util.Map descriptors = server.getDescriptors();
    //      for ( Object key : descriptors.keySet())
    //      {
    //          ClassDescriptor desc = (ClassDescriptor)descriptors.get(key);
    //          desc.getDescriptorQueryManager().setExpressionQueryCacheMaxSize(0);
    //      }
    //
    //			// Handle selective database exception
    //			server.setExceptionHandler(new TOPLinkExceptionHandler());
    //			m_dbServers.put(catalog, server);
    //
    //			// it is important to acquire client session from the new server.
    //			// do not load banking holidays if connecting to customer service or
    //			// profile db
    //			if (!isProfileCatalog && !isEmailCatalog)
    //			{
    //				AppMgr.getLogger().info("Loading banking holidays for catalog:" + catalog);
    //				Helper.readAllObjects(server.acquireClientSession(), com.paycycle.biz.BankHoliday.class, (Expression) null);
    //
    //				AppMgr.getLogger().info("Loading partner holidays for catalog:" + catalog);
    //				Helper.readAllObjects(server.acquireClientSession(), com.paycycle.biz.PartnerHoliday.class, (Expression) null);
    //			}
    //			// Print out some statistics of this server session
    //			getLogger().info(
    //					"Default connection pool: min = " + server.getDefaultConnectionPool().getMinNumberOfConnections() + ", max = "
    //							+ server.getDefaultConnectionPool().getMaxNumberOfConnections());
    //
    //			getLogger().info(
    //					"Read connection pool: min = " + server.getReadConnectionPool().getMinNumberOfConnections() + ", max = "
    //							+ server.getReadConnectionPool().getMaxNumberOfConnections());
    //
    //			getLogger().info("Non pooled connection: max = " + server.getMaxNumberOfNonPooledConnections());
    //		}
    //
    //		return server;
    //	}
    //
    //	public static PageHelpManager getPageHelpManager() {
    //		return m_pageHelpManager;
    //	}
    //
    //	public static EventMgr getEventMgr() {
    //		return m_eventMgr;
    //	}
    //
    //	public static void createChangeMgr() {
    //		if (getChangeMgr() == null) {
    //			changeMgr.set(new ChangeMgr());
    //		}
    //	}
    //
    //	public static ChangeMgr getChangeMgr() {
    //		return changeMgr.get();
    //	}
    //
    //	public static void clearChangeMgr() {
    //        changeMgr.set(null);
    //    }
    //
    //	/**
    //	 * Based on the application name, determine the realtime CCard processing
    //	 * host, used by OPS
    //	 */
    //	public static String getCCardHost() {
    //		String host = PropertiesManager.getProperty("app.tx.ccard.host." + getAppName());
    //		if (Helper.isEmpty(host)) {
    //			host = PropertiesManager.getProperty("app.tx.ccard.host.test");
    //		}
    //		return host;
    //	}
    //
    //	static final int FORM1099_STARTDATE = 0;
    //	static final int FORM1099_FIRSTDEADLINE = 1;
    //	static final int FORM1099_SECONDDEADLINE = 2;
    //
    //	public static Date get1099StartDate() {
    //		return get1099Date(FORM1099_STARTDATE);
    //	}
    //
    //	public static Date get1099FirstDeadline() {
    //		return get1099Date(FORM1099_FIRSTDEADLINE);
    //	}
    //
    //	public static Date get1099SecondDeadline() {
    //		return get1099Date(FORM1099_SECONDDEADLINE);
    //	}
    //
    //	private static Date get1099Date(int dateType) {
    //		int year = DateUtil.getYear(new Date());
    //		int month;
    //		int day;
    //
    //		switch (dateType) {
    //
    //		case FORM1099_STARTDATE:
    //			month = Integer.parseInt(getProperty("app.form1099.start.month")) - 1;
    //			day = Integer.parseInt(getProperty("app.form1099.start.day"));
    //			break;
    //
    //		case FORM1099_FIRSTDEADLINE:
    //			month = Integer.parseInt(getProperty("app.form1099.deadline.month")) - 1;
    //			day = Integer.parseInt(getProperty("app.form1099.deadline.day"));
    //			break;
    //
    //		case FORM1099_SECONDDEADLINE:
    //			month = Integer.parseInt(getProperty("app.form1099.extendedDeadline.month")) - 1;
    //			day = Integer.parseInt(getProperty("app.form1099.extendedDeadline.day"));
    //			break;
    //
    //		default:
    //			throw new UserException("Unknown 1099 date");
    //		}
    //
    //		return DateUtil.createDate(year, month, day);
    //	}
    //
    //	public static int get1099FilingYear()
    //	{
    //		if (isProduction() || getProperty("app.form1099.filingYear") == null) {
    //			return DateUtil.getYear(DateUtil.today()) - 1;
    //		} else {
    //			return Integer.parseInt(getProperty("app.form1099.filingYear"));
    //		}
    //	}
    //
    //	/**
    //	 * Extracts search keywords from the request header and inserts them into a
    //	 * request attribute. This function is used to look for the keywords that
    //	 * are used in search engines (eg, google) and get those keywords into our
    //	 * profile database to aid in data analysis. It is normally only called when
    //	 * we are creating a vistor id. The ProfileHandler then picks them up by
    //	 * searching for the appropriate request attribute and inserts them into the
    //	 * profile db.
    //	 *
    //	 * @param req
    //	 *            The request.
    //	 */
    //	public static void checkSearchKeywords(HttpServletRequest req) {
    //		req.setAttribute(ProfileFilter.SEARCHKEYWORDS, (getSearchKeywordsFromUrl(req.getHeader("Referer"))));
    //	}
    //
    //	public static String getSearchKeywordsFromUrl(String url) {
    //		if (Helper.isEmpty(url)) return "";
    //
    //		// Check for search engine key word strings. Useful for eMarketing
    //		// tracking
    //		String[] searchParams = { "q", "p", "query" }; // These are the
    //		// parameter names used
    //		// by the various search
    //		// engines.
    //
    //		try {
    //			boolean breakout = false;
    //			int qmarkIx = url.indexOf('?');
    //			if (qmarkIx >= 0) {
    //				String referrerParams = url.substring(qmarkIx + 1);
    //				String[] params = referrerParams.split("&");
    //				for (String param : params) {
    //					String[] paramParts = param.split("=");
    //					for (String searchParam : searchParams) {
    //						if (paramParts[0].equalsIgnoreCase(searchParam)) {
    //							return paramParts[1];
    //						}
    //					}
    //					if (breakout) {
    //						break;
    //					}
    //				}
    //			}
    //		} catch (Exception e) {
    //			// Just ignore. This is purely about tracking. If something goes
    //			// wrong, ignore and move on.
    //		}
    //
    //		return "";
    //	}
    //
    //	/**
    //	 * return a unique visitor id for this user. If it can't be found in a
    //	 * cookie, then construct one from the jsessionid. If res is not null then
    //	 * set the cookie.
    //	 */
    //	public static String getVistorId(HttpServletRequest req, HttpServletResponse res) {
    //		String vCookie = ServletUtil.getCookieValue(req, AppMgr.VISTORID);
    //
    //		if (Helper.isEmpty(vCookie)) {
    //			vCookie = ServletUtil.getSessionIdUnique(req);
    //			if (res != null) {
    //				ServletUtil.setCookieValue(req, res, AppMgr.VISTORID, vCookie);
    //			}
    //
    //			checkSearchKeywords(req);
    //		}
    //
    //		return vCookie;
    //	}
    //
    //	static final long MINUTES_TILL_REFRESH = 5;
    //	static final long MILLIS_TILL_REFRESH = MINUTES_TILL_REFRESH * 60 * 1000;
    //	static Date lastRefresh = new Date();
    //	static int currentFuesTestId = -2;
    //
    //	/**
    //	 * This routine is used to cache the current FollowupEmailScheduleTest id.
    //	 * Since this requires reading the entire table index, we just use the
    //	 * AppMgr to cache the results and then invalidate the results after a set
    //	 * time period to allow an occational refresh.
    //	 */
    //	public static int getCurrentFuesTestId() {
    //		Date now = new Date();
    //		if (currentFuesTestId == -2 || now.getTime() - lastRefresh.getTime() > MILLIS_TILL_REFRESH) {
    //			lastRefresh = now;
    //
    //			String sqlQuery = "SELECT MAX(FUEScheduleTestID) AS LastID FROM FollowupEmailScheduleTests ";
    //			Integer id = (Integer) Helper.executeValueReadQuery(sqlQuery);
    //
    //			if (id != null) {
    //				currentFuesTestId = id;
    //			} else {
    //				currentFuesTestId = -1;
    //			}
    //		}
    //
    //		return currentFuesTestId;
    //	}
    //
    //	public static Date getZeroPaymentReleaseDate() {
    //		return DateUtil.createDate(getProperty("app.zeroPayments.releaseDate"));
    //	}
    //
    //	public static Date getZeroPaymentReleaseDateForMultistate() {
    //		return DateUtil.createDate(getProperty("app.zeroPayments.releaseDateForMultistate"));
    //	}
    //
    //	/**
    //	 * Determines if the image about our 866 phone line should be displayed on
    //	 * the home page.
    //	 */
    //	public static boolean get866ImagesHome() {
    //		return Helper.isEmpty(getProperty("app.866images.home"), "false").equals("true");
    //	}
    //
    //	/**
    //	 * Determines if the image about our 866 phone line should be displayed on
    //	 * the external pages.
    //	 */
    //	public static boolean get866ImagesExternal() {
    //		return Helper.isEmpty(getProperty("app.866images.external"), "false").equals("true");
    //	}
    //
    //	/**
    //	 * Determines if the image about our 866 phone line should be displayed on
    //	 * the internal pages.
    //	 */
    //	public static boolean get866ImagesInternal() {
    //		return Helper.isEmpty(getProperty("app.866images.internal"), "false").equals("true");
    //	}
    //
    //	public static boolean show866ImagesInternal(PageContext pageContext) {
    //		return AppMgr.get866ImagesInternal() && ((AppMgr.isLoggedIn(pageContext) && pageContext.getAttribute("co") == null) // Provider
    //																															// case
    //				|| (pageContext.getAttribute("co") != null && !((Company) pageContext.getAttribute("co")).isActive() && !((Company) pageContext
    //						.getAttribute("co")).isTerminated()));
    //	}
    //
    //	/**
    //	 * Determines if the image about our 866 phone line should be displayed
    //	 * (whether page is internal or external). This function makes use of the
    //	 * other get866 methods in AppMgr and most likely should be used instead of
    //	 * them.
    //	 *
    //	 * @param pageContext
    //	 * @return
    //	 */
    //	public static boolean show866Images(PageContext pageContext) {
    //		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    //		HttpSession session = request.getSession();
    //		boolean get866ImagesExternal = // this means we're on the external site
    //		pageContext.getAttribute("co") == null && AppMgr.get866ImagesExternal();
    //		boolean show866ImagesInternal = show866ImagesInternal(pageContext); // this
    //		// means
    //		// we're
    //		// on
    //		// the
    //		// internal
    //		// site
    //		// and
    //		// the
    //		// company
    //		// is
    //		// active
    //		boolean companyIsSBA = false;
    //		if (Helper.isNotEmpty(session.getAttribute("companyIsSBA"))) {
    //			if (session.getAttribute("companyIsSBA").equals("true")) {
    //				companyIsSBA = true;
    //			}
    //		}
    //		boolean doNotDisplayCondition = // this means we're on the internal site
    //		// and the company is an SBA.
    //		(pageContext.getAttribute("co") != null && companyIsSBA) || (TypeUtil.toInt(session.getAttribute("partnerId")) == Partner.MANAGEPAYROLL);
    //
    //		return (get866ImagesExternal || show866ImagesInternal) && !doNotDisplayCondition;
    //	}
    //
    //	/**
    //	 * Returns information about the href for the support link (location on the
    //	 * image, plus destination of link).
    //	 *
    //	 * @param pageContext
    //	 * @return Returns a string array, where element 0 is the href, and element
    //	 *         1 is the location on the image.
    //	 */
    //	public static String[] get866Href(PageContext pageContext) {
    //		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    //		HttpSession session = request.getSession();
    //		ServletContext application = pageContext.getServletContext();
    //		boolean show866ImagesInternal = show866ImagesInternal(pageContext); // this
    //		// means
    //		// we're
    //		// on
    //		// the
    //		// internal
    //		// site
    //		// and
    //		// the
    //		// company
    //		// is
    //		// active
    //		String supportHref = "mailto:OnlinePayrollSupport@intuit.com";
    //		String supportCoords = "0, 0, 0, 0"; // A little messy, but no nice
    //		// place to put this.
    //		if (show866ImagesInternal) {
    //			Hashtable basepaths = (Hashtable) application.getAttribute("paths");
    //			supportHref = basepaths.get("support") + "/sendInquiry.jsp?" + DestUrlMgr.getDestUrlPushAsParam(request, null);
    //			// if accountant employee, they can't go to
    //			// /in/support/sendInquiry.jsp
    //			if (session.getAttribute("companyId").equals(session.getAttribute("providerId")) && request.getAttribute("overrideSPcheck") == null) {
    //				User loggedInUser = (User) Helper.readObject(com.paycycle.user.User.class, TypeUtil.toLong(session.getAttribute("userId")));
    //				if (loggedInUser.hasRole(RoleID.PROVIDER_ADMIN)) {
    //					supportHref = basepaths.get("spSupport") + "/sendInquiry.jsp?" + DestUrlMgr.getDestUrlPushAsParam(request, null);
    //				}
    //			}
    //			supportCoords = "0, 0, 152, 115";
    //		}
    //		return new String[] { supportHref, supportCoords };
    //	}
    //
    //	/**
    //	 * Returns the path name that should be used for the given section (such as "support", "account", etc.).
    //	 * Note that the returned path does NOT end in a '/', so you have to add that on yourself, presuming you
    //	 * are using this to construct a url.
    //	 * @Returns null if the section is not found.
    //	 */
    //	public static String getBasePath(PageContext pageContext,String section) {
    //		Hashtable paths = (Hashtable)pageContext.getAttribute("paths", PageContext.APPLICATION_SCOPE);
    //		return (String) paths.get(section);
    //	}
    //
    //	/**
    //	 * Returns the path name that should be used for the given section (such as "support", "account", etc.).
    //	 * Note that the returned path does NOT end in a '/', so you have to add that on yourself, presuming you
    //	 * are using this to construct a url.
    //	 * @Returns null if the section is not found.
    //	 */
    //	public static String getBasePath(String section) {
    //		Hashtable paths = (Hashtable)servletContext.getAttribute("paths");
    //		return (String) paths.get(section);
    //	}
    //
    //	/**
    //	 * Determines if the export to CCH ProSystem is shown on the site.
    //	 */
    //	public static boolean getCCHExport() {
    //		return Helper.isEmpty(getProperty("app.cchexport"), "true").equals("true");
    //	}
    //
    //	/**
    //	 * Based on the application context, determine the file attachments path
    //	 */
    //	public static String getFileAttachmentPath() {
    //		return System.getProperty("java.io.tmpdir");
    //	}
    //
    //	/**
    //	 * Based on the application context, determine the template files archive
    //	 * path
    //	 */
    //	public static String getTemplatePath() {
    //		return getResourcesPath()+PropertiesManager.getProperty("app.paths.template");
    //	}
    //
    //	/**
    //	 * Determine the path to the template/auto directory
    //	 *
    //	 */
    //	public static String getAutoTemplatesPath() {
    //		return getResourcesPath()+PropertiesManager.getProperty("app.paths.template.auto");
    //	}
    //
    //	/**
    //	 * This method attempts to "guess" what section of the public site the user
    //	 * is in based on the url, is called if it is not specified by the
    //	 * <pc:page>'s "area" attribute
    //	 */
    //	public static String getPublicSiteTabArea(HttpServletRequest request) {
    //		String[][] areas = new String[][] { { "TO DO", "todo" }, {"EMPLOYEES","employees"},{ "PAY DAY", "pay" }, { "TAXES & FORMS", "taxesForms" },
    //				{ "SETUP", "account"}, { "REPORTS", "reports" }, { "HELP", "support" } };
    //		String url = request.getRequestURL().toString();
    //
    //		while (url.indexOf('/') > 0) {
    //			String last = url.substring(url.lastIndexOf('/') + 1);
    //			String first = url.substring(0, url.lastIndexOf('/'));
    //			url = first;
    //
    //			// look for first section that matches
    //			for (int i = 0; i < areas.length; i++) {
    //				for (int j = 1; j < areas[i].length; j++) {
    //					if (last.equalsIgnoreCase(areas[i][j])) {
    //						return areas[i][0];
    //					}
    //				}
    //			}
    //		}
    //
    //		return null;
    //	}
    //
    //	/**
    //	 * Enable the ops to search by recipient e-mail. Defaults to off.
    //	 */
    //	public static boolean enableRecipientSearching() {
    //		String enableRecipientSearching = PropertiesManager.getProperty("app.mail.enableRecipientFiltering");
    //		return enableRecipientSearching != null && enableRecipientSearching.equalsIgnoreCase("true");
    //	}
    //
    //	public static String getContextRoot() {
    //		return CTX_ROOT;
    //	}
    //
    //	public static String getContextPath(ServletContext ctx) {
    //		// this works on servlet 2.4 (tomcat 55) but not GF2.x
    //		String result = ctx.equals(ctx.getContext("/")) ? "main" : "test";
    //		try {
    //			// attempt call to servlet 2.5 api dynamically so the the build
    //			// won't fail when only tomcat55 is available
    //			Class noArgs[] = {};
    //			Method api = ctx.getClass().getMethod("getContextPath", noArgs);
    //			result = (String) api.invoke(ctx);
    //			if (result.equals("")) {
    //				// this is the root context case
    //				result = "main";
    //			} else {
    //				// otherwise trim off the leading '/' from the path
    //				result = result.substring(1);
    //			}
    //		} catch (Exception ex) {
    //			// give up and use the default value
    //		}
    //		return result;
    //	}
    //
    //	public static String getExportPropertiesPath() {
    //		return getContextRoot() + PropertiesManager.getProperty("app.properties.export");
    //	}
    //
    //
    //	public static boolean isPDFFilterEnabled() {
    //		String pdf_filter = getProperty("app.pdf.filter");
    //		return pdf_filter.equalsIgnoreCase("true");
    //	}
    //
    //	public static String getApplicationAttribute(String name) {
    //		return (String) servletContext.getAttribute(name);
    //	}
    //
    //	public static void setApplicationAttribute(String name, String value) {
    //		servletContext.setAttribute(name, value);
    //	}
    //
    //	public static boolean isLivePersonChatEnabled()
    //	{
    //		if (!Helper.isEmpty(getProperty("app.textChat.livePerson"))) {
    //			String onValue = isProduction() ? "P" : "T";
    //			if (getProperty("app.textChat.livePerson").equals(onValue)) {
    //				return true;
    //			}
    //		}
    //		return false;
    //	}
    //
    //	public static String getLivePersonAccountID(Partner partner)
    //	{
    //		String accountID, key;
    //
    //		if (!isProduction()) {
    //			key = "app.textChat.livePerson.test_"+partner.getId()+".id";
    //			accountID = (!Helper.isEmpty(getProperty(key))) ? getProperty(key)
    //							: getProperty("app.textChat.livePerson.test.id");
    //		} else {
    //			key = "app.textChat.livePerson.production_"+partner.getId()+".id";
    //			accountID = (!Helper.isEmpty(getProperty(key))) ? getProperty(key)
    //							: getProperty("app.textChat.livePerson.production.id");
    //		}
    //		return accountID;
    //	}
    //
    //	public static boolean isCoremetricsEnabled() {
    //		return true;
    //	}
    //	public static boolean isCoremetricsTracking (HttpServletRequest request)
    //	{
    //		// not tracking development build and performance build:
    //		if (isDevelopment() || isPerf())
    //			return false;
    //
    //		boolean isCoremetricsTracking = false;
    //		if (Helper.isNotEmpty(getProperty("app.coremetrics.enabled")) && getProperty("app.coremetrics.enabled").equalsIgnoreCase("true")) {
    //			String appName = UNKNOWN_APPNAME;
    //			try {
    //				appName = AppMgr.getAppName();
    //			}
    //			catch (Exception ex)
    //			{}
    //			// tracking paycycle main only, not test:
    //			if (appName.equals(PAYCYCLE)) {
    //				isCoremetricsTracking = true;
    //			} else if (!AppMgr.isProduction() && "true".equals(request.getParameter("app_coremetrics_enabled"))) {
    //				//for non-production environment, enable core metric if app_coremetrics_enabled parameter is sent in the request
    //				//this functionality is enable so QA can test coremetric tracking in QA environment if it's needed
    //				//core metric enable in non-production environment will send tracking traffic to coremetric test environment
    //				isCoremetricsTracking = true;
    //			} else {
    //				return false;
    //			}
    //		}
    //		else
    //			return false;
    //
    //		// not tracking www.managepayroll.com:
    //		if (request.getServerName().toLowerCase().indexOf("managepayroll") >= 0)
    //			return false;
    //
    //		// not tracking boa, capitalone and pnc partners:
    //		Company pageCompany = null;
    //		if (Helper.isNotEmpty(request.getSession().getAttribute("companyId"))) {
    //			pageCompany = Helper.readObject(Company.class, Long.parseLong((String) request.getSession().getAttribute("companyId")));
    //			if (pageCompany != null && PartnerMgr.getPartner(pageCompany).isWhiteLabel())
    //				return false;
    //		}
    //
    //		return isCoremetricsTracking;
    //	}
    //
    //	public static boolean isCoremetricsTrackingBofA (HttpServletRequest request)
    //	{
    //		// not tracking development build and performance build:
    //		if (isDevelopment() || isPerf())
    //			return false;
    //
    //		boolean isCoremetricsTracking = false;
    //		if (Helper.isNotEmpty(getProperty("app.coremetrics.enabled")) && getProperty("app.coremetrics.enabled").equalsIgnoreCase("true")) {
    //			String appName = UNKNOWN_APPNAME;
    //			try {
    //				appName = AppMgr.getAppName();
    //			}
    //			catch (Exception ex)
    //			{}
    //			// tracking paycycle main only, not test:
    //			if (appName.equals(PAYCYCLE))
    //				isCoremetricsTracking = true;
    //			else {
    //				if (Helper.isEmpty(getProperty("app.coremetrics.testbofa")) || !getProperty("app.coremetrics.testbofa").equalsIgnoreCase("true"))
    //					return false;
    //				else
    //					isCoremetricsTracking = true;
    //			}
    //		}
    //		else
    //			return false;
    //
    //		// not tracking www.managepayroll.com:
    //		if (request.getServerName().toLowerCase().indexOf("managepayroll") >= 0)
    //			return false;
    //
    //		return isCoremetricsTracking;
    //	}
    //
    //	public static Company getCompanyFromRequest(HttpServletRequest request) {
    //		if (Helper.isEmpty(request.getSession().getAttribute("companyId"))) {
    //			return null;
    //		}
    //		return Helper.readObject(Company.class, Long.parseLong((String) request.getSession().getAttribute("companyId")));
    //	}
    //
    //	public static boolean isTireKickingEnabled(PageContext pageContext) {
    //    	return isTireKickingEnabled(TaskMgrInstance.getPayrollTaskMgrInstance(pageContext), (HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse());
    //    }
    //
    //	public static boolean isTireKickingEnabled(TaskMgrInstance tmgr, HttpServletRequest request, HttpServletResponse response ) {
    //		Company co = getCompanyFromRequest(request);
    //
    //		// Do not allow tire kicking for non-PayCycle partners
    //		Partner p = PartnerMgr.getPartner(Encryption.encryptCode(co.getSourceCode()), co.getId());
    //		if (p.isKeyAccount())
    //			return false;
    //
    //		// Do not allow tire kicking if this is a SBA
    //		if (co == null || Helper.isNotEmpty(co.getProvider()))
    //			return false;
    //
    //		// Do not allow tire kicking if Initial interview task (Profile Setup) and Initial interview in Employee Setup (radio buttons questions) task are incomplete
    //		boolean initialSetupComplete = tmgr.isCompleted(PayrollTaskList.TaskInitialInterview.class, co) && tmgr.isCompleted(PayrollTaskList.TaskEEsInitialInterview.class, co);
    //		if (!initialSetupComplete)
    //			return false;
    //
    //		return PopulationManager.getTestChoiceNameForTestType(ABTestType.AllowExploration, request, response).equals("allow");
    //	}
    //
    //	public static boolean isPublicLogin() {
    //		String userIdStr = (String) AppMgr.getHttpSession().getAttribute("userId");
    //		if(userIdStr != null) {
    //			long userId = Long.parseLong(userIdStr);
    //			User user = (User) Helper.readObject(com.paycycle.user.User.class, userId);
    //			if(user != null) {
    //				return user.hasRole(RoleID.CUSTOMER_SUPPORT);
    //			}
    //		}
    //		return false;
    //	}
    //
    //	public static String getAppId(HttpSession session) {
    //		String tmp = session.getServletContext().toString();
    //		String[] s = tmp.split("[@]");
    //		if(s.length == 2) {
    //			return s[1];
    //		} else {
    //			return tmp;
    //		}
    //	}
    //
    //	public static final String PARTNER_INTEGRATION_OFF = "partner_integration_off";
    //	public static final String PARTNER_INTEGRATION = "partner_integration";
    //	/**
    //	 * Return true if it's currently in partner data integeration is off. This api
    //	 * only work for Ops or Public login. It always return false for Public since
    //	 * we never want intergation to be off in normal Public login.
    //	 * @param company instance of Company to check
    //	 * @return
    //	 */
    //	public static boolean isPartnerDataIntegrationOff(Company company) {
    //		boolean result = false;
    //		//first check to see if there is partner_intergration_off flag in the session
    //		HttpSession session = AppMgr.getHttpSession();
    //		if(session != null && company != null) {
    //			String externalPartnerDataId = (String)session.getAttribute(PARTNER_INTEGRATION_OFF);
    //			if(externalPartnerDataId != null && company.getExternalPartnerDataId() != null) {
    //				Partner partner = company.getPartner();
    //				if(partner != null && partner.hasPartnerDataIntegration()) {
    //					if(AppMgr.isOperations() || AppMgr.isPublicLogin()) {
    //						result = externalPartnerDataId.equals(company.getExternalPartnerDataId());
    //						if(!result) {
    //							session.removeAttribute(PARTNER_INTEGRATION_OFF);
    //						}
    //					}
    //				}
    //			}
    //		}
    //		return result;
    //	}
    //
    //	/**
    //	 * Return partner integration parameter flag that is passed in the url.
    //	 * This api is used to pass integration parameter flag from Ops to
    //	 * Public login
    //	 * @param company instance of Company object where partner data integration is checked
    //	 * @return
    //	 */
    //	public static String getPartnerIntegrationParameter(Company company) {
    //		String result = "";
    //		if(isPartnerDataIntegrationOff(company)) {
    //			result = "&" + PARTNER_INTEGRATION + "=off";
    //		}
    //		return result;
    //	}
    //
    //	public static boolean isFetchBackTrackingEnabled(HttpServletRequest request)
    //	{
    //		// not tracking development build and performance build:
    //		if (isDevelopment() || isPerf())
    //			return false;
    //
    //		boolean isFetchBackTracking = false;
    //		if (Helper.isNotEmpty(getProperty("app.fetchback.enabled")) && getProperty("app.fetchback.enabled").equalsIgnoreCase("true")) {
    //			String appName = UNKNOWN_APPNAME;
    //			try {
    //				appName = AppMgr.getAppName();
    //			}
    //			catch (Exception ex)
    //			{}
    //			// tracking paycycle main only, not test:
    //			if (appName.equals(PAYCYCLE))
    //				isFetchBackTracking = true;
    //			else
    //				return false;
    //		}
    //		else
    //			return false;
    //
    //		// not tracking for sample session
    //		if (AppMgr.isSample(request.getSession()))
    //			return false;
    //
    //		// not tracking www.managepayroll.com:
    //		if (request.getServerName().toLowerCase().indexOf("managepayroll") >= 0)
    //			return false;
    //
    //		// not tracking boa, capitalone and pnc partners:
    //		Company pageCompany = null;
    //		if (Helper.isNotEmpty(request.getSession().getAttribute("companyId"))) {
    //			pageCompany = Helper.readObject(Company.class, Long.parseLong((String) request.getSession().getAttribute("companyId")));
    //			if (pageCompany != null && PartnerMgr.getPartner(pageCompany).isWhiteLabel())
    //				return false;
    //		}
    //
    //		// not tracking for partners other than PAYCYCLE
    //		Partner partner = null;
    //		if (Helper.isNotEmpty(request.getSession().getAttribute("partnerId"))) {
    //			partner = PartnerMgr.getPartner(Long.parseLong((String)request.getSession().getAttribute("partnerId")));
    //			if (partner != null && partner.getId() != Partner.PAYCYCLE)
    //				return false;
    //		}
    //
    //		return isFetchBackTracking;
    //	}
    //
    //    public static boolean isSetupServicePartner(HttpServletRequest request, Company co)
    //    {
    //    	if (request == null || co == null)
    //    		return false;
    //
    //        if (isSourcecodeOverride(request))
    //            return true;
    //
    //        if (isSourcecodeExcluded(request))
    //            return false;
    //
    //        if (co.isProvider() || co.isHousehold() || co.isSBA()) // not AC, HH, SBA
    //            return false;
    //        if (!co.getFeatureSet().equals(FeatureSetType.SUPER) && !co.getFeatureSet().equals(FeatureSetType.Plus)) // has to be plus or super
    //            return false;
    //        if (co.getPartner().getId() != Partner.PAYCYCLE) // Paycycle customer only
    //            return false;
    //        return true;
    //    }
    //
    //    public static boolean isSetupServiceVisible(HttpServletRequest request, Company company)
    //    {
    //    	if (request == null || company == null)
    //    		return false;
    //
    //        if (isSourcecodeOverride(request))
    //            return true;
    //
    //        if (isSourcecodeExcluded(request))
    //            return false;
    //
    //        // get visiblity property: if property "app.setupService.pilot.everyX" not defined, defined as non-numberical or x<1, not visible
    //        String everyX = getProperty("app.setupService.pilot.everyX");
    //        if (Helper.isEmpty(everyX) || !StringUtil.isNumeric(everyX) || TypeUtil.toInteger(TypeUtil.toInt(everyX)) < 1)
    //            return false;
    //        // every one is visible:
    //        if (TypeUtil.toInteger(TypeUtil.toInt(everyX)) == 1)
    //            return true;
    //        // look up latest signups to check for visibility:
    //        String sqlQuery =   " select count(*) from objectattributes " +
    //                            " where objectType = 'C' and attributeName = 'SHOW_ONBOARDING_ASSISTANCE' and attributeValue = 'true' " +
    //                            "   and objectid in ( " +
    //                            "     select top " + everyX + " CompanyID from companies " +
    //                            "     where CompanyType = 'C' " + // not AC
    //                            "       and isHousehold = 0 " + // not HH
    //                            "       and BillingType = 'co' " + // not SBA
    //                            "       and featureSetId = 2 " + // PLUS
    //                            "       and providerid is NULL " + // not SBA
    //                            "       and partnerid = 1 " + //PayCycle branded
    //                            "     order by signupDate desc) " +
    //                            "   and objectid <> " + String.valueOf(company.getId());
    //        Integer count = (Integer) Helper.executeValueReadQuery(sqlQuery);
    //		if (count != null && count > 0)
    //            return false;
    //
    //        return true;
    //    }
    //
    //	public static boolean isAppUsingFakeDate() {
    //		long res = 0;
    //		if (!AppMgr.isProduction()) {
    //			try {
    //				Class c = Class.forName("java.util.CurrentTimeManager");
    //				Method m[] = c.getDeclaredMethods();
    //				for (int i = 0; i < m.length; i++) {
    //					if (m[i].getName().equals("getOffsetInMillisecondsForCurrentSession")) {
    //						Object[] args = new Object[]{};
    //						Object obj = m[i].invoke(null, args);
    //						if (obj != null && obj instanceof Long) {
    //							res = ((Long)obj).longValue();
    //						}
    //						break;
    //					}
    //				}
    //			} catch (Throwable e) {
    //				AppMgr.getLogger().info(e.getMessage());
    //			}
    //		}
    //		return res != 0;
    //	}
    //
    //    public static boolean isSourcecodeOverride(HttpServletRequest request)
    //    {
    //        boolean toOverride = false;
    //        String overrideSourcecode = getProperty("app.setupService.pilot.overrideSourcecode");
    //        String sc = ServletUtil.getCookieValue(request, "SC");
    //        if (Helper.isNotEmpty(overrideSourcecode) && Helper.isNotEmpty(sc)) { // override only if SC and overrideSourcecode are defined:
    //            String clearSC = CodeMgr.decryptSourceCode(sc).toLowerCase(); // sc might be encrypted, decrypt and lowercase it since public.properties contains lowcased sourcecodes
    //        	StringTokenizer st = new StringTokenizer(overrideSourcecode, ",");
    //        	while (st.hasMoreTokens()) {
    //                String overrider = st.nextToken();
    //                // override is an overriding sourcecode such as : payrollcomsetuptest and organicsetuptest
    //                // or the prefix of a group of sourcecodes.
    //                if (overrider.endsWith("*")) { // matching prefix of source code:
    //        			String prefix = overrider.substring(0, overrider.indexOf("*"));
    //        			if (clearSC.startsWith(prefix)) {
    //        				toOverride = true;
    //                    	break;
    //        			}
    //        		}
    //        		else { // matching entire source code:
    //        			if (clearSC.equals(overrider)) {
    //        				toOverride = true;
    //                    	break;
    //        			}
    //        		}
    //            }
    //        }
    //        return toOverride;
    //    }
    //
    //    public static boolean isSourcecodeExcluded(HttpServletRequest request)
    //    {
    //        boolean toExclude = false;
    //        String excludeSourcecode = getProperty("app.setupService.pilot.excludeSourcecode");
    //        String sc = ServletUtil.getCookieValue(request, "SC");
    //        if (Helper.isNotEmpty(excludeSourcecode) && Helper.isNotEmpty(sc)) { // exclude only if SC and excludeSourcecode are defined:
    //            String clearSC = CodeMgr.decryptSourceCode(sc).toLowerCase(); // sc might be encrypted, decrypt and lowercase it since public.properties contains lowcased sourcecodes
    //        	StringTokenizer st = new StringTokenizer(excludeSourcecode, ",");
    //        	while (st.hasMoreTokens()) {
    //                String excludee = st.nextToken();
    //                // excludee is an excluded sourcecode such as: payrollcomcontrol and organiccontrol
    //                // or a regular expression representing a group of sourcecodes such as: ppc\\S+paycycle\\S+
    //                if (clearSC.equals(excludee) || clearSC.matches(excludee)) {
    //                	toExclude = true;
    //                	break;
    //                }
    //            }
    //        }
    //        return toExclude;
    //    }
    //
    //	public static boolean hasIDSPlatformSessionContext() {
    //		return getIDSPlatformSessionContext() != null;
    //	}
    //
    //	public static PlatformSessionContext getIDSPlatformSessionContext() {
    //		return (PlatformSessionContext) getHttpSession().getAttribute("IDSPlatformSessionContext");
    //	}
    //
    //	public static void setIDSPlatformSessionContext(String ticket, String appToken) {
    //		PlatformSessionContext platformSessionContext = new PlatformSessionContext(ticket, appToken);
    //		getHttpSession().setAttribute("IDSPlatformSessionContext", platformSessionContext);
    //	}
    //
    //    public static boolean isIntegoEnabledForAll() {
    //		if (Boolean.valueOf(AppMgr.getProperty("app.integoForAll.enabled"))) {
    //			return true;
    //        }
    //        return false;
    //	}
    //
    //	public static PayrollWarningMgr getNewPayrollWarningMgr(Company co) {
    //		PayrollWarningMgr mgr;
    //		try {
    //			mgr = (PayrollWarningMgr) payrollWarningMgrClass.newInstance();
    //		} catch (InstantiationException ex) {
    //			throw new UserException("Can't get new PayrollWarningMgr " + payrollWarningMgrClass.getName(), ex);
    //		} catch (IllegalAccessException ex) {
    //			throw new UserException("Can't get new PayrollWarningMgr " + payrollWarningMgrClass.getName(), ex);
    //		}
    //		mgr.init(co);
    //		return mgr;
    //	}
    //
}

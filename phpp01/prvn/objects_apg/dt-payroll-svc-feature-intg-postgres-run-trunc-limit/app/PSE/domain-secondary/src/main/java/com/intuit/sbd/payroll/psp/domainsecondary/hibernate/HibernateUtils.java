package com.intuit.sbd.payroll.psp.domainsecondary.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.DomainEntityInterceptor;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.internal.SessionImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.ServiceRegistry;

import javax.persistence.OptimisticLockException;
import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: ssharma17
 * Date: Nov 16, 2018
 * Time: 4:38:35 PM
 */
public class HibernateUtils {
    private static Pattern ExtendsPattern = Pattern.compile(".*joined-subclass[^>]+extends=\"[^\"]+\\.([^\"\\.]+)\".*", Pattern.DOTALL);
    private static final SpcfLogger logger = Application.getLogger(HibernateUtils.class);
    private static final String DOMAIN_ENTITY_INSPECTOR = "com.intuit.sbd.payroll.psp.hibernate.DomainEntityInspector";

    private static SessionFactory sessionFactory;

    static {
        try {
            Configuration hibernateConfiguration = getConfiguration();
            ServiceRegistry serviceRegistry = createServiceRegistry(hibernateConfiguration);
            sessionFactory = hibernateConfiguration.buildSessionFactory(serviceRegistry);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
            logger.error(ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }

    public static void initialize() {
        getSessionFactory();
    }

    private static Configuration getConfiguration() throws IOException {
        Configuration config = new Configuration();

        addHibernateProperties(config);
        addConnectionProperties(config);
        addInterceptor(config);
        addInspector(config);
        addHbmFiles(config);

        return config;
    }

    private static ServiceRegistry createServiceRegistry(Configuration config) {
        // Create CustomIntegrator to attach EventListeners
        BootstrapServiceRegistry bootstrapServiceRegistry = new BootstrapServiceRegistryBuilder()
                .applyIntegrator(new CustomIntegrator()).build();

        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
                .applySettings(config.getProperties());

        ServiceRegistry serviceReg = registryBuilder.build();

        return serviceReg;
    }

    private static void addHbmFiles(Configuration config) {
        String hbmLocation = getJarfileName();

        if (hbmLocation.endsWith(".jar")) {
            config.addJar(new File(hbmLocation));
        }
        else {
            Set<String> addedEntities = new HashSet<String>();
            List<File> nextPassEntities = new ArrayList<File>();
            if (hbmLocation.endsWith("/")) {
                addDirectoryAlphabetical(config, addedEntities, nextPassEntities, new File(hbmLocation));
            } else {
                hbmLocation = hbmLocation.substring(0, hbmLocation.indexOf("com/intuit") - 1);
                SpcfLogManager.getLogger(HibernateUtils.class).info(hbmLocation);
                addDirectoryAlphabetical(config, addedEntities, nextPassEntities, new File(hbmLocation));
            }

            while (!nextPassEntities.isEmpty()) {
                List<File> sortedFilesToAdd = nextPassEntities;
                nextPassEntities = new ArrayList<File>();
                addHbmFiles(config, sortedFilesToAdd, addedEntities, nextPassEntities);
            }


        }

    }

    private static Configuration addHbmFiles(Configuration config, List<File> sortedFiles, Set<String> addedEntities, List<File> nextPassEntities) {
        for (File file : sortedFiles) {
            if ( file.isDirectory() ) {
                addDirectoryAlphabetical(config, addedEntities, nextPassEntities, file);
            }
            else if ( file.getName().endsWith( ".hbm.xml" ) ) {
                String mappingFileString;
                mappingFileString = readFile(file);

                boolean add;
                Matcher matcher = ExtendsPattern.matcher(mappingFileString);
                if (matcher.matches()) {
                    String extendedClass = matcher.group(1);
                    add = addedEntities.contains(extendedClass);
                } else {
                    add = true;
                }

                if (add) {
                    addedEntities.add(file.getName().replaceAll("\\.hbm\\.xml", ""));
                    config.addFile(file);
                } else {
                    nextPassEntities.add(file);
                }
            }
        }

        return config;
    }

    //todo use FileUtils--but needs to be added to artifacts
    private static String readFile(File file) {
        try {
            return new Scanner(file).useDelimiter("\\Z").next();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration addDirectoryAlphabetical(Configuration config, Set<String> addedEntities, List<File> nextPassEntities, File dir) throws MappingException {
        File[] files = dir.listFiles();
        SortedMap<String, File> fileMap = new TreeMap<String, File>();
        if (files != null) {
            for ( File file : files ) {
                fileMap.put(file.getName(), file);
            }
        }
        addHbmFiles(config, new ArrayList<File>(fileMap.values()), addedEntities, nextPassEntities);
        return config;
    }


    private static String getJarfileName() {
        // Get the location of the jar file and the jar file name
        java.net.URL outputURL = HibernateUtils.class.getProtectionDomain().getCodeSource().getLocation();
        String outputString = outputURL.toString();

        String[] parseString;
        int index1 = outputString.indexOf(":");
        int index2 = outputString.lastIndexOf(":");
        if (index1 != index2) // Windows/DOS uses C: naming convention
            parseString = outputString.split("file:/");
        else
            parseString = outputString.split("file:");
        String jarFilename = parseString[1];

        SpcfLogManager.getLogger(HibernateUtils.class).info("Hibernate mapping files location:" + jarFilename);

        return jarFilename;
    }

    /**
     * Gets external configuration settings for hibernate from the configuration manager, and injects them into
     * the specified hibernate configuration.
     *
     * @param config Hibernate configuration to add hibernate properties to.
     */
    private static void addHibernateProperties(Configuration config) {
        config.addProperties(ConfigurationManager.getConfigurationProperties(DatabaseConfigManager.AuditDbHibernateToken));
    }

    /**
     * Adds connection properties for hibernate to the specified hibernate configuration, based on Data Access
     * properties set elsewhere.
     *
     * @param config Hibernate configuration to add connection properties to.
     */
    private static void addConnectionProperties(Configuration config) {
        String dbConnectionToken = DatabaseConfigManager.AuditDbToken;
        String connectionProvider = ConfigurationManager.getSettingValue(dbConnectionToken, ConnectionProviderKey);

        // Either use connection from data source, or use JDBC directly.
        if (ConfigurationManager.containsKey(dbConnectionToken, ConnectionDataSourceKey)) {
            config.setProperty(Environment.DATASOURCE, ConfigurationManager.getSettingValue(dbConnectionToken, ConnectionDataSourceKey));
            config.setProperty(Environment.DIALECT, ConfigurationManager.getSettingValue(dbConnectionToken, DatabaseDialectKey));
            // set connection provider, only if one has been configured.
            if (connectionProvider != null && connectionProvider.length() > 0) {
                config.setProperty(Environment.CONNECTION_PROVIDER, connectionProvider);
            }
        }
        else {
            config.setProperty(Environment.USER, ConfigurationManager.getSettingValue(dbConnectionToken, UserIdKey));
            config.setProperty(Environment.PASS, ConfigurationManager.getSettingValue(dbConnectionToken, PasswordKey));
            config.setProperty(Environment.URL, ConfigurationManager.getSettingValue(dbConnectionToken, ConnectionUrlKey));
            config.setProperty(Environment.DRIVER, ConfigurationManager.getSettingValue(dbConnectionToken, ConnectionDriverKey));
            config.setProperty(Environment.DIALECT, ConfigurationManager.getSettingValue(dbConnectionToken, DatabaseDialectKey));
            // set connection provider, only if one has been configured.
            if (connectionProvider != null && connectionProvider.length() > 0) {
                config.setProperty(Environment.CONNECTION_PROVIDER, connectionProvider);
            }

        }

    }

    /**
     * Adds interceptor.
     *
     * @param cfg Hibernate configuration.
     */
    private static void addInterceptor(Configuration cfg) {
        cfg.setInterceptor(DomainEntityInterceptor.getInstance());
    }

    /**
     * Adds inspector.
     *
     * @param cfg Hibernate configuration.
     */
    private static void addInspector(Configuration cfg) {
        cfg.setProperty(Environment.STATEMENT_INSPECTOR, DOMAIN_ENTITY_INSPECTOR);
    }

    public static Object[] getCachedDatabaseSnapshot(Class entityClass, DomainEntity pEntity) {
        SessionImpl sessionImpl = (SessionImpl) ApplicationSecondary.getHibernateSession();
        EntityPersister persister = sessionImpl.getFactory().getEntityPersister(entityClass.getName());
        EntityKey entityKey = new EntityKey(pEntity.getId(), persister);
        return sessionImpl.getPersistenceContext().getCachedDatabaseSnapshot(entityKey);
    }

    /**
     * From Hibernate 5.6.10, OptimisticLockException is thrown instead of StaleObjectStateException
     * @param ole
     */
    public static void parseStaleObjectStateException(OptimisticLockException ole) {
        Throwable t = ole.getCause();
        if((!Objects.isNull(t)) && t instanceof StaleObjectStateException)
            parseStaleObjectStateException((StaleObjectStateException )t);
    }
                                                      /**
     * This method will parse a StaleObjectStateException, retrieving the stale and non-stale (i.e. DB) versions of the
     * domain object and save the exception and resulting object field values to a file.
     * @param pException The StaleObjectStateException to parse.
     */
    public static void parseStaleObjectStateException(StaleObjectStateException pException) {
        String entityClassName = pException.getEntityName();
        Serializable entityId = pException.getIdentifier();

        // PSRV001910
        // Check to ensure we have a valid entity name and entity id (if not, ignore the exception)
        // Check to see if this StaleObjectStateException originated with the test company (if so, ignore the exception)
        if ((entityClassName == null) || (entityId == null) ||
            (entityClassName.equals("com.intuit.sbd.payroll.psp.domain.Company") &&
             entityId.toString().equals("1adf4866-8548-42f2-97e5-f22ca2e1c6a0"))) {
            return;
        }

        StringBuffer msgBuffer = new StringBuffer();

        try {
            Class entityClass = Class.forName(entityClassName);

            if (DomainEntity.class.isAssignableFrom(entityClass)) {
                String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
                StringWriter stackTrace = new StringWriter();

                msgBuffer.append(String.format("**********************************************************************%n"));
                msgBuffer.append(String.format("* StaleObjectStateException Diagnostic Log (%s)%n", timestamp));
                msgBuffer.append(String.format("**********************************************************************%n"));
                msgBuffer.append(String.format("*%n"));
                msgBuffer.append(String.format("* Exception Stack Trace:%n"));
                msgBuffer.append(String.format("*%n%n"));

                pException.printStackTrace(new PrintWriter(stackTrace));

                msgBuffer.append(stackTrace.toString());

                msgBuffer.append(parseStaleDomainEntity(entityClass, entityId));
            }
        } catch (Throwable t) {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));

            msgBuffer.append(String.format("Error in parseStaleObjectStateException%n"));
            msgBuffer.append(stackTrace.toString());
        }

        if (msgBuffer.length() > 0) {
            try {
                File logFile = File.createTempFile("psp-stale-object-", ".log", new File(getLogDirectory()));
                FileOutputStream fos = new FileOutputStream(logFile);

                try {
                    OutputStreamWriter osw = new OutputStreamWriter(fos);

                    try {
                        osw.write(msgBuffer.toString());
                        osw.flush();
                    } finally {
                        osw.close();
                    }
                } finally {
                    fos.close();
                }

                // TODO: Encrypt log file since it may contain PII (or write string buffer to database??)
                // TODO: Can't use ZipAes here since including the package produces a circular reference
                // TODO: Move all crypto code to its own package when time allows...

                logger.error(String.format("A StaleObjectStateException has occurred. See file %s for details.%n" +
                                           "Message: %s%n", logFile.toString(), pException.getMessage()));
            } catch (Throwable t) {
                logger.info("Error creating log file for StaleObjectStateException.", t);
            }
        }
    }

    private static String getLogDirectory() {
        String directory = System.getProperty("user.dir");  // default

        if(System.getProperty("catalina.base") != null ) { // for tomcat
            directory = System.getProperty("catalina.base") + File.separator + "logs";
        } else if(System.getProperty("flux.home") != null ) { // for flux
            directory = System.getProperty("flux.home") + File.separator + "logs";
        }

        return directory;
    }

    public static <T extends DomainEntity> StringBuffer parseStaleDomainEntity(final Class<T> pDomainEntityClass,
                                                                               final Serializable pUniqueId) {
        StringBuffer msgBuffer = new StringBuffer();

        msgBuffer.append(String.format("%n*%n"));
        msgBuffer.append(String.format("* Snapshot of stale Domain Entity [%s#%s]%n", pDomainEntityClass.getCanonicalName(), pUniqueId));
        msgBuffer.append(String.format("*%n%n"));

        //
        // Print the stale domain object's field values
        //
        try {
            DomainEntity de = Application.findById(pDomainEntityClass, pUniqueId); // retrieve stale object from cache

            if (de == null) {
                msgBuffer.append(String.format("Unable to locate domain object in cache or database [%s#%s]%n",
                                               pDomainEntityClass.getCanonicalName(), pUniqueId));
            } else {
                T domainObject = pDomainEntityClass.cast(de); // cast DomainEntity to appropriate type
                Class clazz = pDomainEntityClass.getSuperclass();

                // Start from super class (Base) of given domain entity class (i.e. if Paycheck, start from BasePaycheck)
                while ((clazz != null) && DomainEntity.class.isAssignableFrom(clazz)) {
                    msgBuffer.append(printDomainEntityFields(clazz, domainObject));
                    clazz = clazz.getSuperclass();
                }
            }
        } catch (Throwable t) {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));

            msgBuffer.append(String.format("Error parsing stale domain object [%s#%s]%n", pDomainEntityClass.getCanonicalName(), pUniqueId));
            msgBuffer.append(stackTrace.toString());
        }

        msgBuffer.append(String.format("%n*%n"));
        msgBuffer.append(String.format("* Snapshot of fresh Domain Entity [%s#%s]%n", pDomainEntityClass.getCanonicalName(), pUniqueId));
        msgBuffer.append(String.format("*%n%n"));

        //
        // Print the fresh domain object's field values (in separate transaction to keep stale cached object intact)
        //
        StringBuffer threadMsgBuffer = Application.executeTransactionThread(new TransactionThread<StringBuffer>() {
            public StringBuffer transaction() {
                StringBuffer msgBuffer = new StringBuffer();

                try {
                    DomainEntity de = Application.findById(pDomainEntityClass, pUniqueId); // retrieve fresh object from db

                    if (de == null) {
                        msgBuffer.append(String.format("Unable to locate domain object in database [%s#%s]%n",
                                                       pDomainEntityClass.getCanonicalName(), pUniqueId));
                    } else {
                        T domainObject = pDomainEntityClass.cast(de); // cast DomainEntity to appropriate type
                        Class clazz = pDomainEntityClass.getSuperclass();

                        // Start from super class (Base) of given domain entity class (i.e. if Paycheck, start from BasePaycheck)
                        while ((clazz != null) && DomainEntity.class.isAssignableFrom(clazz)) {
                            msgBuffer.append(printDomainEntityFields(clazz, domainObject));
                            clazz = clazz.getSuperclass();
                        }
                    }
                } catch (Throwable t) {
                    StringWriter stackTrace = new StringWriter();
                    t.printStackTrace(new PrintWriter(stackTrace));

                    msgBuffer.append(String.format("Error parsing fresh domain object [%s#%s]%n", pDomainEntityClass.getCanonicalName(), pUniqueId));
                    msgBuffer.append(stackTrace.toString());
                }

                return msgBuffer;
            }
        });

        msgBuffer.append(threadMsgBuffer.toString());

        return msgBuffer;
    }

    public static <T extends DomainEntity> StringBuffer printDomainEntityFields(Class<T> pDomainEntityClass,
                                                                                T pDomainObject) {
        StringBuffer msgBuffer = new StringBuffer();

        try {
            if (pDomainObject == null) {
                msgBuffer.append(String.format("Referencing domain object is null (%s).", pDomainEntityClass.getCanonicalName()));
            } else {
                for (Field field : pDomainEntityClass.getDeclaredFields()) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                    Object fieldObj = field.get(pDomainObject);

                    // if the field is another DomainEntity, just pull the class name and unique id from the object
                    // else if the field is a DomainEntitySet, write the set's DomainEntity objects to the buffer.
                    if (DomainEntity.class.isInstance(fieldObj)) {
                        DomainEntity de = (DomainEntity) fieldObj;
                        fieldObj = String.format("[%s#%s]", de.getClass().getCanonicalName(), de.getId());
                    } else if (DomainEntitySet.class.isInstance(fieldObj)) {
                        DomainEntitySet des = (DomainEntitySet) fieldObj;
                        StringBuffer sb = new StringBuffer();

                        if (des.isEmpty()) {
                            sb.append("<empty>");
                        } else {
                            for (Object obj : des) {
                                if (sb.length() > 0) {
                                    sb.append(",");
                                }

                                if (DomainEntity.class.isInstance(obj)) {
                                    DomainEntity de = (DomainEntity) obj;
                                    sb.append(String.format("[%s#%s]", de.getClass().getCanonicalName(), de.getId()));
                                } else {
                                    sb.append(String.format("[%s#%s]", obj.getClass().getCanonicalName(), "<no-unique-id>"));
                                }
                            }
                        }

                        fieldObj = String.format("%s {%s}", fieldObj.getClass().getSimpleName(), sb.toString());
                    }

                    msgBuffer.append(String.format("%s = %s%n", field.getName(), fieldObj));
                }
            }
        } catch (Throwable t) {
            StringWriter stackTrace = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTrace));

            msgBuffer.append(String.format("Error printing domain entity fields (%s)%n", pDomainEntityClass.getCanonicalName()));
            msgBuffer.append(stackTrace.toString());
        }

        return msgBuffer;
    }

    /**
     * Key for user ID.
     */
    private static final String UserIdKey = "dataAccess.connection.username";

    /**
     * Key for password.
     */
    private static final String PasswordKey = "dataAccess.connection.password";

    /**
     * Key for connection URL.
     */
    private static final String ConnectionUrlKey = "dataAccess.connection.url";

    /**
     * Key for connection provider.
     */
    private static final String ConnectionProviderKey = "dataAccess.connection.provider";

    /**
     * Key for connection driver.
     */
    private static final String ConnectionDriverKey = "dataAccess.connection.driver_class";

    /**
     * Key for datasource provider.
     */
    private static final String ConnectionDataSourceKey = "dataAccess.connection.datasource";

    /**
     * Key for database dialect.
     */
    private static final String DatabaseDialectKey = "dataAccess.connection.dialect";
}

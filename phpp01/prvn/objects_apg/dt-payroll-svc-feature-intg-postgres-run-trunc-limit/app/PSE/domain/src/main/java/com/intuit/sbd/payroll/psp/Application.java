package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.c3p0.C3P0PoolManager;
import com.intuit.sbd.payroll.psp.cache.SessionCache;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConstants;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.entity.DGTransactionObserver;
import com.intuit.sbd.payroll.psp.entity.EntityEventTransactionObserver;
import com.intuit.sbd.payroll.psp.filter.CompanyFilterStrategy;
import com.intuit.sbd.payroll.psp.filter.factory.CompanyFilterStrategyFactory;
import com.intuit.sbd.payroll.psp.filter.constants.FilterStrategyType;
import com.intuit.sbd.payroll.psp.hibernate.HibernateUtils;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.hibernate.multitenancy.*;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.query.BuildHibernateCriteriaVisitor;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.transactionobserver.CompanyTransactionObserver;
import com.intuit.sbd.payroll.psp.transactionobserver.DateTransactionObserver;
import com.intuit.sbd.payroll.psp.util.DomainReflectionHelper;
import com.intuit.sbd.payroll.psp.util.IProcessObserver;
import com.intuit.sbd.payroll.psp.util.ITransactionObserver;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagUtil;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.application.context.PayrollApplicationContext;
import com.intuit.sbg.psp.proxyInjector.service.ProxyServerSetup;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.mchange.v2.c3p0.C3P0ProxyConnection;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.spi.StandardLevel;
import org.hibernate.*;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.procedure.internal.ProcedureParameterImpl;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.type.descriptor.sql.JdbcTypeJavaClassMappings;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TemporalType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.*;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


/**
 * This is the API that provides PSP with access to the underlying plumbing
 * . Logging
 * . Configuration Management
 * . Unit of work capability (begin/commit/rollback)
 * . Find data entities based on criteria/unique id
 * . Find data objects based on criteria/unique id
 * . Data Entities CRUD
 * . Data Objects CRUD
 * . Installation of database objects
 * . Load of test datasets
 * <p/>
 * Based on SPC-F code and Wiktor & Dawn's TransactionContext class
 *
 * @author Allen Chaves
 */
public final class Application {
    private volatile static Application mApplication = null;
    private static SpcfLogger mLogger;
    private static ThreadLocal<PspPrincipal> mPspPrincipalCache = new ThreadLocal<PspPrincipal>();
    private static ThreadLocal<SessionCache> mSessionCache = new ThreadLocal<SessionCache>();
    private static ThreadLocal<Session> mHibernateSessionCache = new ThreadLocal<Session>();
    private static ThreadLocal<FlushMode> mDefaultHibernateFlushMode = new ThreadLocal<FlushMode>();
    private static ThreadLocal<Boolean> mProcessValidatesOnly = new ThreadLocal<Boolean>();
    private static final String PSP_DATE_TIMEZONE_OFFSET = "PSP_DATE_TIMEZONE_OFFSET";
    public static final String APPLICATION_LOGGING_KEY_NAME = "Application_Logging";

    private static final String POSTGRES_SUFFIX = "_postgres";
    private static final WorkflowTypeAssessor workflowTypeAssessor = new FeatureFlagWorkflowTypeAssessor();
    private static CompanyFilterStrategyFactory companyFilterStrategyFactory;
    private static CompanyFilterStrategy<Criteria, Criteria> hibernateCriteriaStrategy;
    private static CompanyTransactionObserver companyTransactionObserver;

    private static DateTransactionObserver dateTransactionObserver;

    private static ThreadLocal<Map<String, ITransactionObserver>> mTransactionObservers =
            new ThreadLocal<Map<String, ITransactionObserver>>() {
                protected synchronized Map<String, ITransactionObserver> initialValue() {
                    return new Hashtable<String, ITransactionObserver>();
                }
            };

    private static ThreadLocal<Map<String, IProcessObserver>> mProcessObservers =
            new ThreadLocal<Map<String, IProcessObserver>>() {
                protected synchronized Map<String, IProcessObserver> initialValue() {
                    return new Hashtable<String, IProcessObserver>();
                }
            };

    /*
     * Application singleton - initializes the metadata and holds a reference to SPC-F singletons (Repository, TransactionManager)
     */
    private static Application getApplication() {
        if (mApplication == null) {
            synchronized (Application.class) {
                if (mApplication == null) {
                    try {
                        Application newApplication = new Application();
                        newApplication.startApplication();

                        mApplication = newApplication;
                    }
                    catch (Throwable ex) {
                        if (mLogger == null) {
                            mLogger = getLogger(Application.class);
                        }
                        mLogger.error("Error initializing in getApplication().", ex);
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return mApplication;
    }

    public static PspPrincipal getCurrentPrincipal() {
        return mPspPrincipalCache.get();
    }


    public static void setCurrentPrincipal(PspPrincipal principal) {
        mPspPrincipalCache.set(principal);

        // Set the logger parameters
        if (SpcfLogManager.getContext("architecture") == null) {
            // These are not thread specific
            SpcfLogManager.putContext("architecture", "PSP");
            // SpcfLogManager.putContext("environment", ConfigurationManager.getEnvironmentIdentifier());

            try {
                InetAddress addr = InetAddress.getLocalHost();

                // Get IP Address
                byte[] ipAddr = addr.getAddress();

                // Get hostname
                String hostname = addr.getHostName();
                SpcfLogManager.putContext("host", addr.getHostName());
            }
            catch (UnknownHostException e) {
                SpcfLogManager.putContext("host", "unknown");
            }
        }

        SpcfLogManager.putContext("application", principal.getLoggerApplicationName());
        //SpcfLogManager.putContext("Company","ThisIsTheCompany1");
        SpcfLogManager.putContext("UserId", principal.getName());
    }

    /**
     * Truncates all tables in database
     */
    public static void truncateTables() {
        // guard against calling truncateTables from a JUnit test against a non-LOCAL environment
        // i.e. don't accidentally wipe out testers data
        String callingMethod = getCallerMethod("org.junit.runner.JUnitCore.run");
        if (callingMethod.length() > 0 && !isIntegrationTestEnvironment()) {
            throw new RuntimeException("Attempting to run truncateTables() from " + getCallerMethod("truncateTables") + " for environment: " + getEnvironmentName());
        }

        validateConnectionURLForIntegrationTests();

        for (String sqlCommand : readStrings("resources/TruncateTables.sql")) {
            executeSqlCommand(sqlCommand, true);
        }
    }

    /**
     * Update tables in database before test case
     */
    public static void updateTables() {
        // guard against calling updateTables from a JUnit test against a non-LOCAL environment
        // i.e. don't accidentally update testers data
        String callingMethod = getCallerMethod("org.junit.runner.JUnitCore.run");
        if (callingMethod.length() > 0 && !isIntegrationTestEnvironment()) {
            throw new RuntimeException("Attempting to run updateTables() from " + getCallerMethod("updateTables") + " for environment: " + getEnvironmentName());
        }

        validateConnectionURLForIntegrationTests();

        for (String sqlCommand : readStrings("resources/UpdateTables.sql")) {
            executeSqlCommand(sqlCommand, true);
        }
    }

    public static void validateConnectionURLForIntegrationTests() {
        //PSRV004006: Create safeguard such that truncate/update tables cannot be run against a non-local environment
        boolean manageSession = !Application.hasActiveTransaction();
        if(manageSession) {
            Application.beginUnitOfWork();
        }
        try {
            Connection connection = Application.getConnection();
            String connectionURL = connection.getMetaData().getURL();

            if ( !(connectionURL.matches(".*(ds2|DS2|ds1|DS1|ds3|DS3|ltq(?!2)|LTQ(?!2)).*"))
                    && !(connectionURL.contains("orapbdqprd1.qcyf01.ie.intuit.net:1521/pbdprd"))
                    && !(connectionURL.contains("localhost:1521/ORCLCDB.localdomain"))
                    && !(connectionURL.contains("localhost:5432/psp"))
                    && (!connectionURL.contains("XE")
                    || connectionURL.matches(".*(qa|QA|prd|PRD|lt(?!2)|LT(?!2)|prf|PRF|perf|PERF).*"))) {

                throw new RuntimeException("cannot truncate/update tables against URL: " + connectionURL);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(manageSession) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    /**
     * Deletes a particular company info from the database
     */
    public static void deleteCompany(String uniqueId) throws Exception {
        executeSqlProcedure(StoredProcedures.PRC_REMOVE_COMPANY_FAST, true, Pair.of(String.class, uniqueId));
    }

    /**
     * Gets logger for passed class
     *
     * @return Initialized logger
     */
    public static SpcfLogger getLogger(Class c) {
        return SpcfLogManager.getLogger(c);
    }

    /**
     * Gets logger for passed name
     *
     * @return Initialized logger
     */
    public static SpcfLogger getLogger(String name) {
        return SpcfLogManager.getLogger(name);
    }

    /*
     * Saves a DataEntity
     *
     */
    public static <T extends DomainEntity> T save(T dataEntity) {
        if (!dataEntity.isNew() && hasActiveTransaction() && !getHibernateSession().contains(dataEntity)) {
            EntityEntry entry = ((SessionImpl) getHibernateSession()).getPersistenceContext().getEntry(dataEntity);
            String details = (entry != null) ? entry.toString() : "EntityEntry is null";

            rollbackUnitOfWork();

            throw new RuntimeException(String.format("Can't save a data entity that was retrieved/created in a different unit of work { entity details: %s }", details));
        }

        getHibernateSession().save(dataEntity);

        return dataEntity;
    }


    /*
     * Deletes the passed DataEntity
     */
    public static void delete(DomainEntity dataEntity) {
        getHibernateSession().delete(dataEntity);
    }

    /*
     * Saves a DataObject
     *
     */
    public static <T> void deleteObject(T dataObject) {
        getHibernateSession().delete(dataObject);
    }

    /*
     * Deletes the Data Entity identified by the passed UniqueId
     */
    public static void delete(Class c, SpcfUniqueId uniqueId) {
        //noinspection unchecked
        DomainEntity entity = findById(c, uniqueId);

        if (entity != null) {
            delete(entity);
        }
    }


    /**
     ************
     * This section contains methods that ultimately issue direct database calls, typically to find objects
     ************/

    //ID/Type
    /*
     * Find a Data Entity based on its UniqueId
     *
     */
    public static <T extends DomainEntity> T findById(Class<T> c, SpcfUniqueId uniqueId) {
        T obj = null;
        Boolean manageTransaction = !hasActiveTransaction();
        if (manageTransaction) {
            beginUnitOfWork();
        }
        try {
            SessionImpl sessionImpl = (SessionImpl) getHibernateSession();
            EntityPersister persister = ((SessionImpl) getHibernateSession()).getFactory().getEntityPersister(c.getName());
            if (!sessionImpl.getPersistenceContext().containsEntity(new EntityKey(uniqueId, persister))) {
                getSessionCache().trackSqlCall(c.getSimpleName());
            }
            //noinspection unchecked
            obj = getActualObject((T) getHibernateSession().get(c, uniqueId));
        } finally {
            if (manageTransaction) {
                commitUnitOfWork();
            }
        }

        return obj;

    }

    /*
    * Find a Data Object based on its key (defined in the modeling)
    */
    public static <T> T findById(Class<T> c, Object dataObjectUniqueId) {
        T obj = null;
        Boolean manageTransaction = !hasActiveTransaction();
        if (manageTransaction) {
            beginUnitOfWork();
        }
        try {

            SessionImpl sessionImpl = (SessionImpl) getHibernateSession();
            EntityPersister persister = ((SessionImpl) getHibernateSession()).getFactory().getEntityPersister(c.getName());
            if (!sessionImpl.getPersistenceContext().containsEntity(new EntityKey((Serializable) dataObjectUniqueId, persister))) {
                getSessionCache().trackSqlCall(c.getSimpleName());
            }
            //noinspection unchecked
            obj = getActualObject((T) getHibernateSession().get(c, (Serializable) dataObjectUniqueId));
        } finally {
            if (manageTransaction) {
                commitUnitOfWork();
            }
        }

        return obj;
    }

    /*
     * Find all Data Entities of a given type
     */
    public static <T extends DomainEntity> DomainEntitySet<T> find(Class<T> c) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();
            Session session = getHibernateSession();
            CriteriaQuery criteria = createCriteriaQuery(c, session);
            //noinspection unchecked
            DomainEntitySet<T> results = getUniqueActualObjects(session.createQuery(criteria).getResultList());

            checkResultSetTooLarge(c.getSimpleName(), results.size());
            getSessionCache().trackSqlCall(c.getSimpleName());

            return results;
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }

    /*
     * Find all Data Objects of a given type
     */
    public static <T extends DataObject> DomainEntitySet<T> findObjects(Class<T> c) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();

            DomainEntitySet<T> results = getSessionCache().getDataObjectCollection(c, "all");

            if (results == null) {
                Session session = getHibernateSession();
                CriteriaQuery criteria = createCriteriaQuery(c, session);
                //noinspection unchecked
                results = getActualObjects(session.createQuery(criteria).getResultList());

                checkResultSetTooLarge(c.getSimpleName(), results.size());
                getSessionCache().trackSqlCall(c.getSimpleName());

                getSessionCache().addDataObjectCollection(c, "all", results);
            }

            return results;
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }


    //Expression

    /*
     * Find all Data Entities that satisfy the passed expression
     */
    public static <T extends DataObject> DomainEntitySet<T> find(Class<T> c, Expression<? super T> expression) {
        Boolean manageTransaction = !hasActiveTransaction();
        if (manageTransaction) {
            beginUnitOfWork();
        }

        try {
            BuildHibernateCriteriaVisitor visitor = new BuildHibernateCriteriaVisitor(expression);
            Criteria hibernateCriteria = visitor.visit(c);

            if(isHibernateCriteriaChangeRequired()) {
                hibernateCriteria = hibernateCriteriaStrategy.applyFilter(hibernateCriteria);
            }

            String queryComment = getQueryComment();
            if(!ObjectUtils.isEmpty(queryComment)) {
                hibernateCriteria.setComment(queryComment);
            }

            if (!visitor.getEagerlyFilteredCollections().isEmpty()) {
                hibernateCriteria.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
            }

            List hibernateList = hibernateCriteria.list();

            DomainEntitySet<T> results;
            if (!visitor.getEagerlyFilteredCollections().isEmpty()) {
                //noinspection unchecked
                results = buildFromAliasMap(hibernateList, visitor.getEagerlyFilteredCollections(), visitor.getPropertyPathToAlias());
            } else {
                //noinspection unchecked
                results = getUniqueActualObjects(hibernateList);
            }


            checkResultSetTooLarge(c.getSimpleName(), results.size());
            getSessionCache().trackSqlCall(c.getSimpleName());

            return results;
        } finally {
            if (manageTransaction) {
                rollbackUnitOfWork();
            }
        }
    }


    /**
     * Executes a query and returns a list of objects (not necessarily data object or domain entities)
     * If domain entities are included, only the FK will be included in the query and subsequent access will
     * result in another database call.  This behavior cannot be avoided except by using HQL.
     */
    public static <T, Q> ArrayList<Q> executeQuery(Class<T> c, Expression<? super T> expression) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();
            BuildHibernateCriteriaVisitor visitor = new BuildHibernateCriteriaVisitor(expression);
            Criteria hibernateCriteria = visitor.visit(c);

            if(isHibernateCriteriaChangeRequired()) {
                hibernateCriteria = hibernateCriteriaStrategy.applyFilter(hibernateCriteria);
            }

            String queryComment = getQueryComment();
            if(!ObjectUtils.isEmpty(queryComment)) {
                hibernateCriteria.setComment(queryComment);
            }


            //noinspection unchecked
            ArrayList<Q> results = new ArrayList<Q>(hibernateCriteria.list());

            checkResultSetTooLarge(c.getSimpleName(), results.size());
            getSessionCache().trackSqlCall(c.getSimpleName());

            return results;
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }

    public static <T> Long executeScalarAggQuery(Class<T> c, Expression<? super T> expression) {
        //noinspection unchecked
        List scalarList = executeQuery(c, expression);
        return Long.parseLong(scalarList.get(0).toString());
    }

    public static <T, Q> Q executeObjectAggQuery(Class<T> c, Expression<? super T> expression) {
        List<Object> objectList = executeQuery(c, expression);
        //noinspection unchecked
        return (Q) objectList.get(0);
   }

    /*
    * Find all Data Entities that satisfy the passed expression and returns a scrollable result set
    */
    public static <T extends DomainEntity> ScrollableResults findScrollable(Class<T> c, Expression<T> expression) {
        Boolean manageTransaction = !hasActiveTransaction();
        if (manageTransaction) {
            beginUnitOfWork();
        }

        try {
            BuildHibernateCriteriaVisitor visitor = new BuildHibernateCriteriaVisitor(expression);
            Criteria hibernateCriteria = visitor.visit(c);

            if(isHibernateCriteriaChangeRequired()) {
                hibernateCriteria = hibernateCriteriaStrategy.applyFilter(hibernateCriteria);
            }

            return hibernateCriteria.scroll(ScrollMode.FORWARD_ONLY);
        } finally {
            if (manageTransaction) {
                rollbackUnitOfWork();
            }
        }
    }

    //Named Queries/HQL queries

    /*
    * Executes a named query and return a list of objects (not data object or domain entities)
    */
    public static <T> ArrayList<T> executeNamedQuery(String queryName, String[] paramNames, Object[] paramValues, boolean trackSqlCalls) {
        return executeNamedQuery(queryName, paramNames, paramValues, trackSqlCalls, -1, -1);
    }

    public static <T> ArrayList<T> executeNamedQuery(String queryName, String[] paramNames, Object[] paramValues, int firstResult, int maxResults) {
        return executeNamedQuery(queryName, paramNames, paramValues, true, firstResult, maxResults);
    }

    public static <T> ArrayList<T> executeNamedQuery(String queryName, String[] paramNames, Object[] paramValues) {
        return executeNamedQuery(queryName, paramNames, paramValues, true);
    }

    public static <T> ArrayList<T> executeNamedQuery(String queryName, String[] paramNames, Object[] paramValues, boolean trackSqlCalls, int firstResult, int maxResults) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();
            Query queryObject = getHibernateNamedQuery(queryName, paramNames, paramValues, firstResult, maxResults, null);
            //noinspection unchecked
            ArrayList<T> results = new ArrayList<T>(queryObject.list());
            if (trackSqlCalls) {
                checkResultSetTooLarge(queryName, results.size());
                getSessionCache().trackSqlCall(queryName);
            }

            return results;
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }


    /*
     * Find entities/data objects by using a named query  with parameters
     */
    public static <T extends DataObject> DomainEntitySet<T> findByNamedQuery(String queryName, String[] paramNames, Object[] paramValues) {
        return findByNamedQuery(queryName, paramNames, paramValues, -1, -1);
    }

    /*
     * Find entities/data objects by using a named query with parameters.  Use session cache.
     */
    public static <T extends DomainEntity> DomainEntitySet<T> findByNamedQueryUsingCache(Class<T> c, String queryName, String[] paramNames, Object[] paramValues) {
        StringBuilder cacheKey = new StringBuilder(queryName);
        for (int i = 0; i < paramNames.length; i++) {
            cacheKey.append(":");
            cacheKey.append(paramNames[i]);
            cacheKey.append("=");
            if (paramValues[i] != null) {
                if (paramValues[i] instanceof DomainEntity) {
                    cacheKey.append(((DomainEntity) paramValues[i]).getId());
                } else {
                    cacheKey.append(paramValues[i].toString());
                }
            } else {
                cacheKey.append("null");
            }
        }

        DomainEntitySet<T> entities = getSessionCache().getEntityCollection(c, cacheKey.toString());
        if (entities == null) {
            entities = findByNamedQuery(queryName, paramNames, paramValues);
            getSessionCache().addEntityCollection(c, cacheKey.toString(), entities);
        }

        return entities;
    }

    /*
     * Find entities/data objects by using a named query  with parameters
     * Return only a subset of records as specified by firstResult and maxResults
     *
     */
    public static <T extends DataObject> DomainEntitySet<T> findByNamedQuery(String queryName, String[] paramNames, Object[] paramValues, int firstResult, int maxResults) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();
            Query queryObject = getHibernateNamedQuery(queryName, paramNames, paramValues, firstResult, maxResults, null);
            //noinspection unchecked
            DomainEntitySet<T> results = getUniqueActualObjects(queryObject.list());

            checkResultSetTooLarge(queryName, results.size());
            getSessionCache().trackSqlCall(queryName);

            return results;
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }

    /*
     * Find entities/data objects by using a named query  with parameters
     * Return only a subset of records as specified by firstResult and maxResults
     * Optionally sets the readOnly flag
     */
    public static <T extends DataObject> DomainEntitySet<T> findByNamedQuery(String queryName, String[] paramNames, Object[] paramValues, int firstResult, int maxResults, Boolean readOnly) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();
            Query queryObject = getHibernateNamedQuery(queryName, paramNames, paramValues, firstResult, maxResults, readOnly);
            //noinspection unchecked
            DomainEntitySet<T> results = getUniqueActualObjects(queryObject.list());

            checkResultSetTooLarge(queryName, results.size());
            getSessionCache().trackSqlCall(queryName);

            return results;
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }

    private static Query getHibernateNamedQuery(String queryName, String[] paramNames, Object[] paramValues, int firstResult, int maxResults, Boolean readOnly) {
        Query queryObject = getHibernateSession().getNamedQuery(queryName);

        setHQLParameters(queryObject, paramNames, paramValues, firstResult, maxResults, readOnly);

        return queryObject;
    }

    @Deprecated
    /**
     * @deprecated use one of the other methods here or HQLBuilder
     */
    public static Query createReadOnlyQuery(String pHQLQuery) {
        Query query = Application.getHibernateSession().createQuery(pHQLQuery);
        query.setReadOnly(true);
        return query;
    }

    @Deprecated
    /**
     * @deprecated use one of the other methods here or HQLBuilder
     */
    public static Query createHibernateQuery(String queryString) {
        return Application.getHibernateSession().createQuery(queryString);
    }

    @Deprecated
    /**
     * @deprecated use one of the other methods here or HQLBuilder
     */
    public static Query getNamedQuery(String queryName) {
        return Application.getHibernateSession().getNamedQuery(queryName);
    }


    public static <T extends DomainEntity> DomainEntitySet<T> findByHQLQuery(String queryString, String[] paramNames, Object[] paramValues) {
        return findByHQLQuery(queryString, paramNames, paramValues, -1, -1, null);
    }

    public static <T extends DomainEntity> DomainEntitySet<T> findByHQLQuery(String queryString, String[] paramNames, Object[] paramValues, int firstResult, int maxResults) {
        return findByHQLQuery(queryString, paramNames, paramValues, firstResult, maxResults, null);
    }

    public static <T extends DomainEntity> DomainEntitySet<T> findByHQLQuery(String queryString, String[] paramNames, Object[] paramValues, int firstResult, int maxResults, Boolean readOnly) {
        Query queryObject = getHibernateSession().createQuery(queryString);

        setHQLParameters(queryObject, paramNames, paramValues, firstResult, maxResults, readOnly);

        //noinspection unchecked
        List<T> list = queryObject.list();

        checkResultSetTooLarge(queryString, list.size());
        getSessionCache().trackSqlCall(queryString);

        return getUniqueActualObjects(list);
    }

    public static <T> List<T> executeHQLQuery(String queryString, String[] paramNames, Object[] paramValues) {
        return executeHQLQuery(queryString, paramNames, paramValues, -1, -1, null);
    }

    public static <T> List<T> executeHQLQuery(String queryString, String[] paramNames, Object[] paramValues, int firstResult, int maxResults) {
        return executeHQLQuery(queryString, paramNames, paramValues, firstResult, maxResults, null);
    }

    public static <T> List<T> executeHQLQuery(String queryString, String[] paramNames, Object[] paramValues, int firstResult, int maxResults, Boolean readOnly) {
        Query queryObject = getHibernateSession().createQuery(queryString);

        setHQLParameters(queryObject, paramNames, paramValues, firstResult, maxResults, readOnly);

        //noinspection unchecked
        List<T> list = queryObject.list();

        checkResultSetTooLarge(queryString, list.size());
        getSessionCache().trackSqlCall(queryString);

        return list;
    }

    public static void executeHQLUpdate(String queryString, String[] paramNames, Object[] paramValues) {
        Query queryObject = getHibernateSession().createQuery(queryString);

        setHQLParameters(queryObject, paramNames, paramValues, -1, -1, null);

        queryObject.executeUpdate();

        getSessionCache().trackSqlCall(queryString);
    }

    private static void setHQLParameters(Query queryObject, String[] paramNames, Object[] paramValues, int firstResult, int maxResults, Boolean readOnly) {
        if (paramNames != null && paramValues != null) {
            for (int i = 0; i < paramValues.length; i++) {
                if (paramValues[i] instanceof Collection) {
                    queryObject.setParameterList(paramNames[i], (Collection) paramValues[i]);
                } else if (paramValues[i] instanceof Object[]) {
                    queryObject.setParameterList(paramNames[i], (Object[]) paramValues[i]);
                } else {
                    queryObject.setParameter(paramNames[i], paramValues[i]);
                }
            }
        }

        if (firstResult != -1) {
            queryObject.setFirstResult(firstResult);
        }

        if (maxResults != -1) {
            queryObject.setMaxResults(maxResults);
        }

        if ((firstResult != -1 || maxResults != -1) && queryContainsCollectionFetches(queryObject)) {
            //todo once all existing ones are eliminated, this should be a runtime exception
            mLogger.warn("firstResult/maxResults specified with collection fetch; applying in memory! ALL rows will be returned from database.  Query: " + queryObject.getQueryString());
        }

        if (readOnly != null) {
            queryObject.setReadOnly(readOnly);
        }
    }

    public static boolean queryContainsCollectionFetches(Query queryObject) {
        try {
            Method getHQLQueryPlan = AbstractSessionImpl.class.getDeclaredMethod("getHQLQueryPlan", String.class, Boolean.TYPE);
            getHQLQueryPlan.setAccessible(true);
            HQLQueryPlan HQLQueryPlan = (HQLQueryPlan) getHQLQueryPlan.invoke(getHibernateSession(), queryObject.getQueryString(), false);
            return HQLQueryPlan.getTranslators()[0].containsCollectionFetches();
        } catch (Throwable t) {
            //don't care!
            return false;
        }
    }


    /*
     * Returns a ScrollableResults for a named query that does not hold all data in memory
     * Note: the result NEEDS to be closed after use
     *
     */
    public static ScrollableResults scrollableResultsByNamedQuery(String queryName, String[] paramNames, Object[] paramValues, int firstResult, int maxResults) {
        return scrollableResultsByNamedQuery(queryName, paramNames, paramValues, firstResult, maxResults, null);
    }

    public static ScrollableResults scrollableResultsByNamedQuery(String queryName, String[] paramNames, Object[] paramValues, int firstResult, int maxResults, Boolean readOnly) {
        Boolean manageTransaction = !hasActiveTransaction();
        try {
            if (manageTransaction) beginUnitOfWork();
            Query queryObject = getHibernateNamedQuery(queryName, paramNames, paramValues, firstResult, maxResults, readOnly);
            return queryObject.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
        } finally {
            if (manageTransaction) rollbackUnitOfWork();
        }
    }


    public static void checkResultSetTooLarge(String queryName, int recordCount) {
        if (!queryName.equals("SystemParameter") && recordCount > SystemParameter.findIntValue(SystemParameter.Code.RESULTSET_SIZE_ALERT_THRESHOLD, 1000)) {
            String callerMethod = getCallerMethod("Application");
            if (!callerMethodIsInExclusionList(SystemParameter.Code.RESULTSET_SIZE_ALERT_EXCLUSION_LIST, callerMethod)) {
                getLogger(Application.class).warn("Large result set retrieved for " + queryName + " (" + recordCount + " rows)" + " Stack trace: " + getStackTrace());
            }
        }
    }

    private static boolean callerMethodIsInExclusionList(SystemParameter.Code systemParameterCode, String callerMethod) {
        String[] exclusionList = SystemParameter.findStringValue(systemParameterCode, "").split(";");
        Boolean matched = false;
        for (String exclusionPattern : exclusionList) {
            if (exclusionPattern.startsWith("*")) {
                // Allows to exclude by the last part of the full class name ("*findCompanyByEIN")
                exclusionPattern = exclusionPattern.substring(1);
                matched = callerMethod.endsWith(exclusionPattern);
            } else {
                // Allows to exclude entire namespaces ("com.intuit.sbd.payroll.batchjobs")
                matched = callerMethod.startsWith(exclusionPattern);
            }

            if (matched) return true;
        }
        return matched;
    }

    //SQL

    /**
     * Execute a sql command
     */
    public static int executeSqlCommand(String sqlCommand, boolean commit, Object... paramValues) {
        Session session = null;
        PreparedStatement statement = null;
        try {
            if (commit) {
                session = getNewHibernateSession();
            } else {
                session = getHibernateSession();
            }
            Connection connection = Application.getConnection(session);
            statement = connection.prepareStatement(sqlCommand);
            // PSP-16104-To Add timeout feature for sqlconsole
            statement.setQueryTimeout(SystemParameter.findIntValue(SystemParameter.Code.SQL_QUERY_TIMEOUT,300));
            // Bind the IN parameters
            int parameterCount = 1;
            for (Object parameterValue : paramValues) {
                if (parameterValue instanceof java.sql.Timestamp) {
                    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    statement.setTimestamp(parameterCount, (java.sql.Timestamp) parameterValue, utcCalendar);
                } else {
                    statement.setObject(parameterCount, parameterValue);
                }
                parameterCount++;
            }

            statement.execute();
            if (commit) {
                connection.commit();
            }
            return statement.getUpdateCount();
        } catch (SQLTimeoutException e){
            throw new RuntimeException("Query is taking too much time to execute, Aborting it");
        }
        catch (SQLException e) {
            throw new RuntimeException("Problem executing SQL: " + sqlCommand, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Problem closing statement: " + statement, e);
                }
            }

            if (session != null && commit) {
                session.close();
            }
        }
    }

    public static void executeSqlProcedure(StoredProcedures storedProcedure, boolean pCommit, Pair<Class<?>, Object>... inParameterObjectArray) {
        executeSqlProcedureWithOutParameter(storedProcedure, pCommit, NoOutParameter, inParameterObjectArray);
    }

    /**
     * Executes an anonymous block of sql procedure
     * @param sql
     * @param commit
     * @param <T>
     * @return
     */
    public static <T> T executeAnonymousSQl(String sql, boolean commit) {
        T outParameterValue = null;
        Session session = null;
        CallableStatement statement = null;
        boolean exceptionPending = false;

        try {
            if (commit) {
                session = getNewHibernateSession();
            } else {
                session = getHibernateSession();
            }
            Connection connection = Application.getConnection(session);
            statement = connection.prepareCall(sql);

            // Bind the IN parameters


            statement.executeQuery();
            if (commit) {
                connection.commit();
            }



        } catch (SQLException e) {
            exceptionPending = true;
            throw new RuntimeException("Problem executing SQL: " + sql, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    if (!exceptionPending) {
                        throw new RuntimeException("Problem closing statement: " + statement, e);
                    }
                }
            }

            if (session != null && commit) {
                session.close();
            }
        }

        return outParameterValue;
    }

    /**
     * Using Pair of Class Type and Parameter value,
     * because in case of Parameter being Null we would not know the Type of parameter
     * and Postgres DB needs to know the type of Parameter even though its null
     */
    public static <T> T executeSqlProcedureWithOutParameter(StoredProcedures storedProcedure, boolean commit, int outParameterReturnType, Pair<Class<?>, Object>... inParameterObjectArray) {
        Set<String> enabledProceduresSet = FeatureFlagUtil.getFeatureFlagStringSet(FeatureFlags.Key.JPA_PROCEDURE_LIST);
        mLogger.info("Action=Procedure_Execution procedureName="+storedProcedure.getStoredProcedureName()+" enabled="+enabledProceduresSet.contains(storedProcedure.getStoredProcedureName())+" enabledProceduresSet="+enabledProceduresSet);
         if (enabledProceduresSet.contains(storedProcedure.getStoredProcedureName())) {
            return executeSqlProcedureWithOutParameterUsingJPA(storedProcedure, commit, outParameterReturnType, inParameterObjectArray);
        }
        return executeSqlProcedureWithOutParameterUsingCallableStatement(storedProcedure, commit, outParameterReturnType, inParameterObjectArray);
    }

    /**
     * Execute a Sql procedure that takes 0..many IN parameters and has one OUT parameter
     *
     * @param storedProcedure          : Name of the database stored procedure to execute
     * @param outParameterReturnType : Specifies the type of the sole OUT parameter (java.sql.Types.*)
     * @param inParameterObjectArray      : Specifies the values for the IN parameters (if any)
     * @return : returns the value of the OUT parameter
     */
    public static <T> T executeSqlProcedureWithOutParameterUsingCallableStatement(StoredProcedures storedProcedure, boolean commit, int outParameterReturnType, Pair<Class<?>, Object>... inParameterObjectArray) {
        T outParameterValue = null;
        Session session = null;
        CallableStatement statement = null;
        boolean exceptionPending = false;

        try {
            if (commit) {
                session = getNewHibernateSession();
            } else {
                session = getHibernateSession();
            }
            Connection connection = Application.getConnection(session);
            statement = connection.prepareCall(BuildCallableStmt(storedProcedure.getStoredProcedureName(), inParameterObjectArray.length, outParameterReturnType != NoOutParameter));

            // Bind the IN parameters
            int parameterCount = 1;
            for (Pair<Class<?>, Object> inParameterData : inParameterObjectArray) {
                Class<?> inParameterDataType = inParameterData.getLeft();
                Object inParameterDataValue = inParameterData.getRight();
                if (inParameterDataType.equals(Timestamp.class)) {
                    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    statement.setTimestamp(parameterCount, (java.sql.Timestamp) inParameterDataValue, utcCalendar);
                } else {
                    statement.setObject(parameterCount, inParameterDataValue);
                }
                parameterCount++;
            }
            // Bind the OUT parameter (if there is one)
            if (outParameterReturnType != NoOutParameter) {
                statement.registerOutParameter(parameterCount, outParameterReturnType);
            }

            statement.executeQuery();
            if (commit) {
                connection.commit();
            }

            if (outParameterReturnType != NoOutParameter) {
                //noinspection unchecked
                outParameterValue = (T) statement.getObject(inParameterObjectArray.length + 1);
            }

        } catch (SQLException e) {
            exceptionPending = true;
            mLogger.error("Action=Procedure_Execution PEType=Procedure_Execution_OutParameter PECallType=CallableStatement PEOutParameterReturnType="+outParameterReturnType+" PEProcedureName="+storedProcedure+" PEStatus=failure", e);
            throw new RuntimeException("Problem executing SQL: " + storedProcedure, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    if (!exceptionPending) {
                        throw new RuntimeException("Problem closing statement: " + statement, e);
                    }
                }
            }

            if (session != null && commit) {
                session.close();
            }
        }
        Application.printStackTrace("Action=Procedure_Execution PEType=Procedure_Execution_OutParameter PECallType=CallableStatement PEOutParameterReturnType="+outParameterReturnType+" PEProcedureName="+storedProcedure+" PEStatus=success", StandardLevel.INFO);
        return outParameterValue;
    }

    public static <T> T executeSqlProcedureWithOutParameterUsingJPA(StoredProcedures storedProcedure, boolean commit, int outParameterReturnType, Pair<Class<?>, Object>... inParameterObjectArray) {
        T outParameterValue = null;
        Session session = null;
        boolean exceptionPending = false;
        StoredProcedureQuery query = null;

        try {
            if (commit) {
                session = getNewHibernateSession();
            } else {
                session = getHibernateSession();
            }
            query = session.createStoredProcedureQuery(storedProcedure.getStoredProcedureName());
            int parameterCount = 1;
            parameterCount = addParameter(query, parameterCount, inParameterObjectArray);
            if (outParameterReturnType != NoOutParameter) {
                query.registerStoredProcedureParameter(parameterCount, determineJavaClassForJdbcTypeCode(outParameterReturnType), ParameterMode.OUT);
            }
            query.execute();

            if (outParameterReturnType != NoOutParameter) {
                outParameterValue = (T) query.getOutputParameterValue(parameterCount);
            }

            if (commit) {
                Connection connection = Application.getConnection(session);
                connection.commit();
            }

        } catch (Exception exception) {
            exceptionPending = true;
            mLogger.error("Action=Procedure_Execution PEType=Procedure_Execution_OutParameter PECallType=JPAStoredProcedureQuery PEOutParameterReturnType="+outParameterReturnType+" PEProcedureName="+storedProcedure+" PEStatus=failure", exception);
            throw new RuntimeException("Problem executing SQL using JPA Stored procedureName=" + storedProcedure, exception);
        } finally {
            if (session != null && commit) {
                try{
                    session.close();
                } catch (HibernateException hibernateException) {
                    mLogger.error("Action=Procedure_Execution PEType=Procedure_Execution_OutParameter PECallType=JPAStoredProcedureQuery PEOutParameterReturnType="+outParameterReturnType+" PEProcedureName="+storedProcedure+" exception in closing session", hibernateException);
                    if (!exceptionPending) {
                        throw new RuntimeException("Problem closing session: " + session, hibernateException);
                    }
                }
            }
        }
        Application.printStackTrace("Action=Procedure_Execution PEType=Procedure_Execution_OutParameter PECallType=JPAStoredProcedureQuery PEOutParameterReturnType="+outParameterReturnType+" PEProcedureName="+storedProcedure+" PEStatus=success", StandardLevel.INFO);
        return outParameterValue;
    }

    public static int addParameter(StoredProcedureQuery query, int parameterCount, Pair<Class<?>, Object>... inParameterObjectArray) {
        for (Pair<Class<?>, Object> inParameterData : inParameterObjectArray) {
            Class<?> inParameterDataType = inParameterData.getLeft();
            Object inParameterDataValue = inParameterData.getRight();
            if (inParameterDataType.equals(Timestamp.class)) {
                query.registerStoredProcedureParameter(parameterCount, Timestamp.class, ParameterMode.IN);
                query.setParameter(parameterCount, getTimestampInUTC((Timestamp)inParameterDataValue), TemporalType.TIMESTAMP);
            } else if (Objects.nonNull(inParameterDataValue)) {
                query.registerStoredProcedureParameter(parameterCount, inParameterDataType, ParameterMode.IN);
                query.setParameter(parameterCount, inParameterDataValue);
            } else {
                Application.printStackTrace("Action=Procedure_Execution PECallType=JPAStoredProcedureQuery parameterCount="+parameterCount+" Parameter Value is null", StandardLevel.INFO);
                query.registerStoredProcedureParameter(parameterCount, inParameterDataType, ParameterMode.IN);
                // Set Enable Passing Null for current Registered Parameter before setting value
                ProcedureParameterImpl procedureParameterImpl = (ProcedureParameterImpl) query.getParameter(parameterCount);
                procedureParameterImpl.enablePassingNulls(true);
                query.setParameter(parameterCount, inParameterDataValue);
            }
            parameterCount++;
        }
        return parameterCount;
    }

    public static Class determineJavaClassForJdbcTypeCode(int javaSqlType) {
        return JdbcTypeJavaClassMappings.INSTANCE.determineJavaClassForJdbcTypeCode(javaSqlType);
    }

    private static Timestamp getTimestampInUTC(Timestamp timestamp) {
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        ZoneId utcZoneId = utcCalendar.getTimeZone().toZoneId();
        ZonedDateTime zdt = timestamp.toInstant().atZone(utcZoneId);
        return Timestamp.valueOf(zdt.toLocalDateTime());
    }

    /**
     * Using Pair of Class Type and Parameter value,
     * because in case of Parameter being Null we would not know the Type of parameter
     * and Postgres DB needs to know the type of Parameter even though its null
     */
    public static ResultSet executeSqlProcedure(StoredProcedures storedProcedure, int pResultSetType, int pResultSetConcurrency, Pair<Class<?>, Object>... inParameterObjectArray) {
        Set<String> enabledProceduresSet = FeatureFlagUtil.getFeatureFlagStringSet(FeatureFlags.Key.JPA_PROCEDURE_LIST);
        mLogger.info("Action=Procedure_Execution procedureName="+storedProcedure.getStoredProcedureName()+" enabled="+enabledProceduresSet.contains(storedProcedure.getStoredProcedureName())+" enabledProceduresSet="+enabledProceduresSet);
         if (enabledProceduresSet.contains(storedProcedure.getStoredProcedureName())) {
            return executeSqlProcedureUsingJPA(storedProcedure, inParameterObjectArray);
        }
        return executeSqlProcedureUsingCallableStatement(storedProcedure, pResultSetType, pResultSetConcurrency, inParameterObjectArray);
    }

    /**
     * Execute a Sql procedure that takes 0..many IN parameters and returns a result set.
     * <p/>
     * Note that the JDBC Statement object produced in this method cannot be closed since that would also close
     * the ResultSet; however, when the ResultSet is GC'd, the cursor will automatically be closed, at which time
     * the Statement will also be GC'd and subsequently closed.
     * <p/>
     * Note: For stored procedures, JDBC requires that the cursor being returned must be registered as an OUT parameter
     * only (i.e. not INOUT) and Oracle requires that it be the first parameter in the param list of the proc.
     *
     * @param storedProcedure         : Name of the database stored procedure to execute
     * @param pResultSetType        : ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, or ResultSet.TYPE_SCROLL_SENSITIVE
     * @param pResultSetConcurrency : ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
     * @param inParameterObjectArray     : Specifies the values for the IN parameters (if any)
     * @return : A ResultSet object (cursor) as returned by the stored procedure.
     */
    public static ResultSet executeSqlProcedureUsingCallableStatement(StoredProcedures storedProcedure,
                                                int pResultSetType,
                                                int pResultSetConcurrency,
                                                Pair<Class<?>, Object>... inParameterObjectArray) {
        ResultSet resultSet;

        try {
            Connection connection = Application.getConnection();

            CallableStatement statement = connection.prepareCall(
                    BuildCallableStmt(storedProcedure.getStoredProcedureName(), inParameterObjectArray.length, true),
                    pResultSetType, pResultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);

            // Bind the OUT parameter (always a cursor in this case)
            statement.registerOutParameter(1, OracleTypes.CURSOR);

            // Bind the IN parameters
            int parameterCount = 2;
            for (Pair<Class<?>, Object> inParameterData : inParameterObjectArray) {
                Class<?> inParameterDataType = inParameterData.getLeft();
                Object inParameterDataValue = inParameterData.getRight();
                if (inParameterDataType.equals(Timestamp.class)) {
                    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    statement.setTimestamp(parameterCount, (java.sql.Timestamp) inParameterDataValue, utcCalendar);
                } else {
                    statement.setObject(parameterCount, inParameterDataValue);
                }

                ++parameterCount;
            }

            statement.executeQuery();

            resultSet = (ResultSet) statement.getObject(1);
        } catch (SQLException e) {
            mLogger.error("Action=Procedure_Execution PEType=Procedure_Execution_RefCursor PECallType=CallableStatement PEProcedureName="+storedProcedure+" PEStatus=failure", e);
            throw new RuntimeException("Problem executing SQL: " + storedProcedure, e);
        }
        Application.printStackTrace("Action=Procedure_Execution PEType=Procedure_Execution_RefCursor PECallType=CallableStatement PEProcedureName="+storedProcedure+" PEStatus=success", StandardLevel.INFO);
        return resultSet;
    }

    public static ResultSet executeSqlProcedureUsingJPA(StoredProcedures storedProcedure, Pair<Class<?>, Object>... inParameterObjectArray) {
        ResultSet resultSet;
        Session session;
        try {
            session = getHibernateSession();
            StoredProcedureQuery query = session.createStoredProcedureQuery(storedProcedure.getStoredProcedureName());
            int parameterCount = 1;
            query.registerStoredProcedureParameter(parameterCount++, void.class, ParameterMode.REF_CURSOR);
            addParameter(query, parameterCount, inParameterObjectArray);
            query.execute();
            resultSet = (ResultSet) query.getOutputParameterValue(1);
        } catch (Exception exception) {
            mLogger.error("Action=Procedure_Execution PEType=Procedure_Execution_RefCursor PECallType=JPAStoredProcedureQuery PEProcedureName="+storedProcedure+" PEStatus=failure", exception);
            throw new RuntimeException("Problem executing SQL using JPA Stored procedureName=" + storedProcedure, exception);
        }
        Application.printStackTrace("Action=Procedure_Execution PEType=Procedure_Execution_RefCursor PECallType=JPAStoredProcedureQuery PEProcedureName="+storedProcedure+" PEStatus=success", StandardLevel.INFO);
        return resultSet;
    }

    /**
     * Returns list of unique actual objects. If an object in list is a proxy, returns the proxied object.
     * If the object is not proxied, returns the object. If the object is null, returns null.
     *
     * @param <T>               Type of objects to get.
     * @param possibleProxyList List of input object for which actual objects are required.
     * @return List of actual objects.
     */
    public static <T extends DataObject> DomainEntitySet<T> getUniqueActualObjects(List<T> possibleProxyList) {
        DomainEntitySet<T> objectList = new DomainEntitySet<T>();

        for (T possibleProxy : possibleProxyList) {
            T actualObject = getActualObject(possibleProxy);
            if (!objectList.contains(actualObject)) {
                objectList.add(actualObject);
            }
        }

        return objectList;
    }

    /**
     * Returns list of actual objects. If an object in list is a proxy, returns the proxied object.
     * If the object is not proxied, returns the object. If the object is null, returns null.
     *
     * @param <T>               Type of objects to get.
     * @param possibleProxyList List of input object for which actual objects are required.
     * @return List of actual objects.
     */
    private static <T extends DataObject> DomainEntitySet<T> getActualObjects(List<T> possibleProxyList) {
        DomainEntitySet<T> objectList = new DomainEntitySet<T>();

        for (T possibleProxy : possibleProxyList) {
            objectList.add(getActualObject(possibleProxy));
        }

        return objectList;
    }

    public static <T> T getActualObject(T obj) {
        if (obj instanceof HibernateProxy) {
            //noinspection unchecked
            return (T) ((HibernateProxy) obj).getHibernateLazyInitializer().getImplementation();
        } else {
            return obj;
        }
    }

    /*
       Hibernate will not hydrate filtered collections automatically, instead it returns a list of alias maps
       It's a little unclear, but if you summed the entry set in each map, it would be equal to the number of rows returned from the sql
       Example:
        from a left join b on b.x = a.x and b.y = :y
       Result (List<Map>):
       a->A1 b->B1
       a->A1 b->B2
       a->A1 b->B3
       a->A2 b->null
       a->A3 b->B1
    */
    private static <T extends DataObject> DomainEntitySet<T> buildFromAliasMap(List<Map> maps, Set<String> pEagerlyFilteredCollections, HashMap<String, String> pPropertyPathToAlias) {
        //TODO: CleanUp Required here
        if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_BUILD_FROM_ALIAS_MAP_FIX)) {
            return buildFromAliasMapNew(maps, pEagerlyFilteredCollections, pPropertyPathToAlias);
        }
        return buildFromAliasMapOld(maps, pEagerlyFilteredCollections, pPropertyPathToAlias);
    }

    private static <T extends DataObject> DomainEntitySet<T> buildFromAliasMapOld(List<Map> maps, Set<String> pEagerlyFilteredCollections, HashMap<String, String> pPropertyPathToAlias) {
        DomainEntitySet<T> set = new DomainEntitySet<T>();
        for (Map map : maps) {
            //noinspection unchecked
            T item = (T) map.get("this");
            if (!set.contains(item)) {
                set.add(item);
                for (String eagerlyFilteredCollection : pEagerlyFilteredCollections) {
                    boolean enableEagerLoadNestedCollection = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION);
                    if(enableEagerLoadNestedCollection) {
                        // for nested collections we'll need to identify the actual parent of the collection to re-initialize and populate.
                        // for eg. TransactionReturn.MoneyMovementTransaction.FinancialTransactionSet
                        // here the FTset is part of MMT and not TR.
                        // so reinitializeFTSet would be found inside MMT and not TR
                        // so we take the association path (t0.FTS)
                        // split by '.' and get the last 2 entries as collectionParent and collectionName.
                        //Eg: AssocPath      collectionParent      collectionName
                        //     FT ->             this                   FT
                        //     t0.FT->            t0                    FT
                        //     t0.t1.FT->         t1                    FT
                        Pair<String, String> aliasEntityPair = getAliasEntityPair(eagerlyFilteredCollection);
                        DataObject collectionParent = (DataObject) map.get(aliasEntityPair.getKey());
                        if(Objects.isNull(collectionParent)) {
                            continue;
                        }
                        String collectionName = aliasEntityPair.getValue();
                        DomainReflectionHelper.reinitializeCollection(collectionParent, collectionName);
                    } else{
                        DomainReflectionHelper.reinitializeCollection(item, eagerlyFilteredCollection);
                    }
                }
            }
            for (String eagerlyFilteredCollection : pEagerlyFilteredCollections) {
                Object collectionItem = map.get(pPropertyPathToAlias.get(eagerlyFilteredCollection));
                //will be null if outer join and no elements.  want to return empty collection instead.
                if (collectionItem != null) {
                    boolean enableEagerLoadNestedCollection = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION);
                    if(enableEagerLoadNestedCollection) {
                        Pair<String, String> aliasEntityPair = getAliasEntityPair(eagerlyFilteredCollection);
                        DataObject collectionParent = (DataObject) map.get(aliasEntityPair.getKey());
                        if(Objects.isNull(collectionParent)) {
                            continue;
                        }
                        String collectionName = aliasEntityPair.getValue();
                        DomainReflectionHelper.addItemToCollection(collectionParent, collectionName, (DataObject) collectionItem);
                    } else{
                        DomainReflectionHelper.addItemToCollection(item, eagerlyFilteredCollection, (DataObject) collectionItem);
                    }
                }
            }
        }

        return set;
    }
    private static <T extends DataObject> DomainEntitySet<T> buildFromAliasMapNew(List<Map> maps, Set<String> pEagerlyFilteredCollections, HashMap<String, String> pPropertyPathToAlias) {
        DomainEntitySet<T> set = new DomainEntitySet<T>();
        MultiValuedMap<Object, String> initMap = new ArrayListValuedHashMap<>();

        for (Map map : maps) {
            //noinspection unchecked
            T item = (T) map.get("this");
            if (!set.contains(item)) {
                set.add(item);
            }
            for (String eagerlyFilteredCollection : pEagerlyFilteredCollections) {
                boolean enableEagerLoadNestedCollection = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION);
                if(enableEagerLoadNestedCollection) {
                    // for nested collections we'll need to identify the actual parent of the collection to re-initialize and populate.
                    // for eg. TransactionReturn.MoneyMovementTransaction.FinancialTransactionSet
                    // here the FTset is part of MMT and not TR.
                    // so reinitializeFTSet would be found inside MMT and not TR
                    // so we take the association path (t0.FTS)
                    // split by '.' and get the last 2 entries as collectionParent and collectionName.
                    //Eg: AssocPath      collectionParent      collectionName
                    //     FT ->             this                   FT
                    //     t0.FT->            t0                    FT
                    //     t0.t1.FT->         t1                    FT
                    Pair<String, String> aliasEntityPair = getAliasEntityPair(eagerlyFilteredCollection);
                    DataObject collectionParent = (DataObject) map.get(aliasEntityPair.getKey());
                    if(Objects.isNull(collectionParent)) {
                        continue;
                    }
                    String collectionName = aliasEntityPair.getValue();
                    if(!initMap.containsKey(collectionParent) || !initMap.get(collectionParent).contains(aliasEntityPair.getValue())) {
                        DomainReflectionHelper.reinitializeCollection(collectionParent, collectionName);
                        initMap.put(collectionParent, aliasEntityPair.getValue());
                    }
                } else{
                    DomainReflectionHelper.reinitializeCollection(item, eagerlyFilteredCollection);
                }
            }
            for (String eagerlyFilteredCollection : pEagerlyFilteredCollections) {
                Object collectionItem = map.get(pPropertyPathToAlias.get(eagerlyFilteredCollection));
                //will be null if outer join and no elements.  want to return empty collection instead.
                if (collectionItem != null) {
                    boolean enableEagerLoadNestedCollection = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_CRITERIA_EAGER_LOAD_NESTED_COLLECTION);
                    if(enableEagerLoadNestedCollection) {
                        Pair<String, String> aliasEntityPair = getAliasEntityPair(eagerlyFilteredCollection);
                        DataObject collectionParent = (DataObject) map.get(aliasEntityPair.getKey());
                        if(Objects.isNull(collectionParent)) {
                            continue;
                        }
                        String collectionName = aliasEntityPair.getValue();
                        DomainReflectionHelper.addItemToCollection(collectionParent, collectionName, (DataObject) collectionItem);
                    } else{
                        DomainReflectionHelper.addItemToCollection(item, eagerlyFilteredCollection, (DataObject) collectionItem);
                    }
                }
            }
        }

        return set;
    }

    private static Pair<String, String> getAliasEntityPair(String collectionAssociationPath){
        String[] aliasPaths = StringUtils.split(collectionAssociationPath,".");
        String collectionName = aliasPaths[aliasPaths.length-1];
        String alias = (aliasPaths.length > 1) ? aliasPaths[aliasPaths.length-2] : "this";

        return new ImmutablePair<>(alias, collectionName);
    }




    /*
     * Returns current transaction status.
     *
     */
    public static Boolean hasActiveTransaction() {
        Session hibernateSession = getHibernateSession();
        return hibernateSession != null &&
                hibernateSession.isOpen() &&
                hibernateSession.getTransaction().isActive();
    }

    /**
     * ObjectCache access
     */
    public static SessionCache getSessionCache() {
        if (mSessionCache.get() == null) {
            mSessionCache.set(new SessionCache());
        }
        return mSessionCache.get();
    }

    public static void beginUnitOfWork() {
        Application.beginUnitOfWork(FlushMode.AUTO);
    }

    public static void beginUnitOfWork(FlushMode pFlushMode) {
        Application.beginUnitOfWork(pFlushMode, null);
    }

    /*
     * @param pReadOnly when true, objects returned out of UOW will by default be read only.  This can
     *                  improve performance as dirty checking will not be used.
     *                  It should be used in conjunction with a flush mode of Manual
     */
    public static void beginUnitOfWork(FlushMode pFlushMode, boolean pReadOnly) {
        Application.beginUnitOfWork(pFlushMode, null, pReadOnly);
    }

    public static void beginUnitOfWork(FlushMode pFlushMode, SpcfCalendar pPspDate) {
        Application.beginUnitOfWork(pFlushMode, pPspDate, false);
    }

    /**
     * Begin a unit of work
     * The second parameter can be passed only if you are sure that no direct database inserts/updates will be performed (because it is set in memory, not
     * stored in PSP_SYSTEM_PARAMETER as an offset as we normally do for testing
     */
    public static void beginUnitOfWork(FlushMode pFlushMode, SpcfCalendar pPspDate, boolean pReadOnly) {
        try {
            if (hasActiveTransaction()) {
                String errMsg = "beginUnitOfWork aborted, an ongoing transaction present. Current UOW was started at:\n" + getSessionCache().getOriginOfUnitOfWork();

                rollbackUnitOfWork();
                mLogger.error(errMsg);
                throw new RuntimeException(errMsg);
            }

            getSessionCache().clear();    // Make sure the thread-local session is clear
            resetProcessValidatesOnly();  // tie validation only behavior to unit of work

            Session session = getNewHibernateSession(pReadOnly);
            session.setFlushMode(pFlushMode);
            session.setDefaultReadOnly(pReadOnly);
            mHibernateSessionCache.set(session);

            if(isOracleDB()){
                setEndToEndMetrics(session);
            }

            registerTransactionObservers();

            // notify observers of transaction state
            for (ITransactionObserver observer : mTransactionObservers.get().values()) {
                observer.beforeTransactionBegin();
            }

            getHibernateSession().beginTransaction();

            if (!getHibernateSession().getTransaction().isActive()) {
                String errMsg = "beginUnitOfWork is not exiting with an active transaction";

                rollbackUnitOfWork();
                mLogger.error(errMsg);
                throw new RuntimeException(errMsg);
            }

            // notify observers of transaction state
            for (ITransactionObserver observer : mTransactionObservers.get().values()) {
                observer.afterTransactionBegin();
            }

            // Add PSP date to session cache if passed
            if (pPspDate != null) {
                Application.getSessionCache().addNonHibernateObject("PSPDate", pPspDate.toLocal());
            }

            // load PSP date
            PSPDate.getPSPTime();
        } catch (PersistenceException e){
            mLogger.error("Exception in beginUnitOfWork", e);
            resetPoolIfRequired(e);
            throw e;
        }
    }

    public static void resetPoolIfRequired(PersistenceException e){
        if(isExceptionEligibleForConnectionReset(e)) {
            try {
                ///08003 - oracle  08006 - postgres
                //connection does not exist
                C3P0PoolManager.resetAllDataSourcePools();
            } catch (SQLException ex) {
                mLogger.error("Exception in connection reset", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    public static Boolean isExceptionEligibleForConnectionReset(PersistenceException ex){
        List<Throwable> throwables = ExceptionUtils.getThrowableList(ex);
        SQLException sqle = (SQLException) throwables.stream().filter(t -> t instanceof SQLException).findFirst().orElse(null);

        if(Objects.isNull(sqle)){
            return false;
        }
        String sqlState = JdbcExceptionHelper.extractSqlState(sqle);
        mLogger.info(String.format("JDBC Exception, sql_state=%s sql_message=%s sql_error=%s" , sqlState, sqle.getMessage(), sqle.getErrorCode()));
        String errorCodes = FeatureFlags.get().stringValue(FeatureFlags.Key.C3P0_HARD_RESET_ELIGIBLE_CODES, String.format(",%s,", C3P0PoolManager.DEFAULT_HARD_RESET_ELIGIBLE_SQL_STATES));

        return (!StringUtils.isAllBlank(errorCodes) && errorCodes.contains(String.format(",%s,",sqlState)));
    }

    /*
    Sets metrics on session so more details available in OEM/toad
    Will be included on next request to database
     */
    private static void setEndToEndMetrics(Session pSession) {
        try {
            Connection connection = Application.getConnection(pSession)
                    .unwrap(OracleConnection.class);

            if (connection instanceof OracleConnection) {
                OracleConnection oracleConnection = (OracleConnection) connection;
                String[] metrics = oracleConnection.getEndToEndMetrics();
                if (metrics == null) {
                    metrics = new String[OracleConnection.END_TO_END_STATE_INDEX_MAX];
                }
                String module = null;
                if (Application.getCurrentPrincipal() != null) {
                    module = Application.getCurrentPrincipal().getName();
                }
                if (module != null) {
                    metrics[OracleConnection.END_TO_END_CLIENTID_INDEX] = module;
                }
                StackTraceElement callerMethod = getCallerStackTraceElement(".*(Application|PayrollServices|DirtyCheckProcessCache|SystemParameter|getCurrentOffset|getPSPTime).*");
                if (callerMethod != null) {
                    String action = callerMethod.getFileName().replaceAll("\\.java$", "") + ":" + callerMethod.getLineNumber();
                    metrics[OracleConnection.END_TO_END_MODULE_INDEX] = StringUtils.substring(action, 0, 32); //oracle limits to 32-bytes
                    metrics[OracleConnection.END_TO_END_ACTION_INDEX] = StringUtils.substring(callerMethod.getMethodName(), 0, 32); //oracle limits to 32-bytes
                }
                oracleConnection.setEndToEndMetrics(metrics, (short) 0);

            }
        } catch (Throwable t) {
            mLogger.info("Error setting Oracle metrics", t);
        }
    }

    /**
     * Commit a unit of work
     */
    public static void commitUnitOfWork() {
        checkTotalSqlCallsTooLarge(getSessionCache().getTotalSqlCalls());
        if (!hasActiveTransaction()) {
            String errMsg = "commitUnitOfWork aborted, no ongoing transaction present.";

            rollbackUnitOfWork();
            mLogger.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        // stats: before committing
        if (HibernateUtils.getSessionFactory().getStatistics().isStatisticsEnabled()) {
            getLogger(Application.class).debug("Before flush/commit " + SpcfCalendar.getNow().toString());
        }

        // notify observers of transaction state
        for (ITransactionObserver observer : mTransactionObservers.get().values()) {
            observer.beforeTransactionCommit();
        }

        // hibernate flush and commit
        if (getHibernateSession().getHibernateFlushMode() == FlushMode.MANUAL) {
            getHibernateSession().flush();
        }
        getHibernateSession().getTransaction().commit();

        // notify observers of transaction state
        for (ITransactionObserver observer : mTransactionObservers.get().values()) {
            observer.afterTransactionCommit();
        }

        // stats: after committing
        if (HibernateUtils.getSessionFactory().getStatistics().isStatisticsEnabled()) {
            getLogger(Application.class).debug("After flush/commit " + SpcfCalendar.getNow().toString() + " Inserts: " + HibernateUtils.getSessionFactory().getStatistics().getEntityInsertCount() + " Updates: " + HibernateUtils.getSessionFactory().getStatistics().getEntityUpdateCount() + " " + HibernateUtils.getSessionFactory().getStatistics().toString() + "\n");

            for (String entityName : HibernateUtils.getSessionFactory().getStatistics().getEntityNames()) {
                EntityStatistics es = HibernateUtils.getSessionFactory().getStatistics().getEntityStatistics(entityName);
                getLogger(Application.class).debug(entityName + ": " + es.toString());
            }
            HibernateUtils.getSessionFactory().getStatistics().clear();
        }

        // These can't be on a finally block - we only want to clean up thread local if execution was successful
        // If it was not, a rollback call will follow that will then rollback the transaction and clean up thread local
        getSessionCache().clear();
        mSessionCache.set(null);
        mTransactionObservers.get().clear();
        mHibernateSessionCache.set(null);
        TenantContext.clearTenantId();
    }

    /**
     * Rollback a unit of work
     */
    public static void rollbackUnitOfWork() {
        try {
            // if there is no active transaction, silently no-op
            if (hasActiveTransaction()) {
                // checkTotalSqlCallsTooLarge if session is active because it can in-turn begin a session
                checkTotalSqlCallsTooLarge(getSessionCache().getTotalSqlCalls());
                // notify observers of transaction state
                for (ITransactionObserver observer : mTransactionObservers.get().values()) {
                    observer.beforeTransactionRollback();
                }
                getHibernateSession().getTransaction().rollback();

                // notify observers of transaction state
                for (ITransactionObserver observer : mTransactionObservers.get().values()) {
                    observer.afterTransactionRollback();
                }
            }
        } catch (Throwable t) {
            mLogger.error("Exception in rollbackUnitOfWork()", t);
            throw t;
        } finally {
            getSessionCache().clear();
            mSessionCache.set(null);
            mTransactionObservers.get().clear();
            mHibernateSessionCache.set(null);
            TenantContext.clearTenantId();
        }
    }

    private static void checkTotalSqlCallsTooLarge(int totalSqlCalls) {
        int totalSqlCallsAlertThreshold = SystemParameter.findIntValue(SystemParameter.Code.TOTAL_SQL_CALLS_ALERT_THRESHOLD, 500);
        if (totalSqlCalls > totalSqlCallsAlertThreshold) {
            String callerMethod = getCallerMethod("Application.");
            if (!callerMethodIsInExclusionList(SystemParameter.Code.TOTAL_SQL_CALLS_ALERT_EXCLUSION_LIST, callerMethod)) {
                getLogger(Application.class).info("Large number of total sql calls (" + totalSqlCalls + " calls) " +
                        "Caller: " + callerMethod + " " +
                        "Top calls: " + getSessionCache().getSqlCalls(totalSqlCallsAlertThreshold / 10) + " Stack trace: " + getStackTrace());
            }
        }
    }

    /**
     * Mimics application startup.
     */
    private void startApplication() {
        ConfigurationManager.ensureInitialization();

        // logger initialization
        System.out.println("System - Log initialization, status=start");
        mLogger = getLogger(Application.class);
        System.out.println("System - Log initialization, status=complete");
        mLogger.info("Logger - Log initialization, status=complete");
    }


    /**
     * Searches for the file in the classpath.
     *
     * @param fileName fileName to find
     * @return String absolute path to the file
     */
    public static String findFileOnClassPath(String fileName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ResourceLoader resourceLoader = resolver.getResourceLoader();

        Resource resource = resourceLoader.getResource("classpath:/" + fileName);
        String path;
        if (resource.exists()) {
            try {
                path = resource.getFile().getAbsolutePath();
            }
            catch (IOException ex) {
                throw new RuntimeException("File not found: " + fileName);
            }
        } else {
            throw new RuntimeException("File not found: " + fileName);
        }
        return path;
    }

    //method to get FileObject on the class path
    public static File findFileObjectOnClassPath(String fileName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ResourceLoader resourceLoader = resolver.getResourceLoader();
        File file = new File(fileName.substring(fileName.lastIndexOf("/")+1));
        //File file = new File("abc");
        Resource resource = resourceLoader.getResource("classpath:/" + fileName);
        try{
            InputStream inputStream = resource.getInputStream();
            //File file = new File(fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + fileName);
        } catch (IOException e) {
            throw new RuntimeException("File not found: " + fileName);
        }
        return file;
    }

    /*
     * Read file and return array of strings
     *
     */
    private static String[] readStrings(String fileName) {
        BufferedReader reader;
        String filePath = findFileOnClassPath(fileName);
        try {
            reader = new BufferedReader(new FileReader(filePath));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(fileName + " not found");
        }

        String line;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.equals("/") && line.length() > 0) {
                    lines.add(line);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error while reading " + filePath);
        }

        return lines.toArray(new String[lines.size()]);
    }
    /**
     * Initialize SPC-F infrastructure
     */
    public static void initialize() {
        ProxyServerSetup.initialize();
        getApplication();
        initializeApplicationContext();
        HibernateUtils.initialize();
        FeatureFlags.get();
        postInitialize();
    }

    public static void uninitialize() {
        mApplication = null;
        HibernateUtils.shutdown();
        destroyApplicationContext();
    }

    public static void initializeApplicationContext(){
        if (PayrollApplicationContext.isInitialized()){
            return;
        }

        PayrollApplicationContext.init();
        mLogger.info("initializing spring beans");
    }

    public static void destroyApplicationContext(){
        if(isIntegrationTestEnvironment())
            return;

        PayrollApplicationContext.destroy();
        mLogger.info("destroying spring beans");
    }

    private static void postInitialize() {
        companyFilterStrategyFactory = PayrollApplicationBeanFactory.getBean(CompanyFilterStrategyFactory.class);
        hibernateCriteriaStrategy = companyFilterStrategyFactory.getCompanyFilterStrategy(FilterStrategyType.HIBERNATE_CRITERIA);
        FeatureFlagLazyLoader featureFlagLazyLoader = FeatureFlagLazyLoader.getInstance();
        featureFlagLazyLoader.lazyLoadFeatureFlags();
    }

    private static Session getNewHibernateSession() {
        return getNewHibernateSession(false);
    }

    private static Session getNewHibernateSession(boolean readOnly) {
        String tenantIdentifier = workflowTypeAssessor.isReadOnly(readOnly) ? TenantIdentifier.READ.name(): TenantIdentifier.READ_WRITE.name();
        TenantContext.setTenantId(tenantIdentifier);
        return HibernateUtils.getSessionFactory().withOptions().tenantIdentifier(tenantIdentifier).openSession();
    }

    public static Session getHibernateSession() {
        return mHibernateSessionCache.get();
    }

    public static PersistenceContext getHibernatePersistentContext() {
        if(!hasActiveTransaction()) {
            return null;
        }
        SessionImplementor sessionImpl = (SessionImplementor) Application.getHibernateSession();
        return sessionImpl.getPersistenceContext();
    }

    public static ClassMetadata getHibernateClassMetadata(Class entityClass) {
        return HibernateUtils.getSessionFactory().getClassMetadata(entityClass.getName());
    }

    public static boolean isOracleDB(){
        return DatabaseType.ORACLE == getDatabaseType();
    }

    public static boolean isPostgresDB(){
        return DatabaseType.POSTGRES == getDatabaseType();
    }

    public static DatabaseType getDatabaseType(){
        return DatabaseConfigManager.getDatabaseType(
                DatabaseConstants.MonolithDbKey, DatabaseType.ORACLE.toString());
    }

    public static Dialect getDialect() {
        Session session = getHibernateSession();
        if(Objects.isNull(session))
            throw new RuntimeException("Session is null. Cannot get Dialect");
        return ((SessionImpl) session).getJdbcServices().getDialect();
    }

    private static String BuildCallableStmt(String procedureName, int inParameterCount, boolean hasOutParameter) {
        int totalParameterCount = inParameterCount;
        if (hasOutParameter) totalParameterCount++;

        //
        // Build the string representing the jdbc procedure call
        //
        String CallableSqlStmt = "{call " + procedureName + "(";
        for (int i = 1; i <= totalParameterCount; i++) {
            CallableSqlStmt = CallableSqlStmt + "?";
            if (i < totalParameterCount) {
                CallableSqlStmt += ", ";
            }
        }
        CallableSqlStmt += ")}";

        return CallableSqlStmt;
    }

    private static final int NoOutParameter = -99999;

    public static <T extends DomainEntity> T refresh(T domainEntity) {
        if(isEntityEnhancedForLazyLoading(domainEntity) && isEntityExistsInPersistentContext(domainEntity)) {
            //Replicate the entity to the current session
            getHibernateSession().replicate(domainEntity, ReplicationMode.OVERWRITE);
        }
        getHibernateSession().refresh(domainEntity);
        domainEntity.onRefresh();
        //noinspection unchecked
        return (T) findById(domainEntity.getClass(), domainEntity.getId());
    }

    public static boolean isEntityExistsInPersistentContext(DomainEntity domainEntity) {
        SessionImpl session = (SessionImpl) Application.getHibernateSession();
        PersistenceContext persistenceContext = session.getPersistenceContext();
        EntityKey key = new EntityKey(domainEntity.getId(), getEntityPersister(domainEntity));
        return persistenceContext.containsEntity(key);
    }

    public static boolean isEntityEnhancedForLazyLoading(DomainEntity domainEntity) {
        return getEntityPersister(domainEntity).getBytecodeEnhancementMetadata().isEnhancedForLazyLoading();
    }

    public static EntityPersister getEntityPersister(DomainEntity domainEntity) {
        SessionImpl sessionImpl = (SessionImpl) Application.getHibernateSession();
        return sessionImpl.getEntityPersister(null, domainEntity);
    }



    /*
     * Executes transaction in a separate thread.
     *
     */
    public static <T> T executeTransactionThread(TransactionThread<T> pTransactionThread) {
        Thread thread = new Thread(pTransactionThread);

        thread.setDaemon(true);
        thread.start();

        try {
            thread.join();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        if (pTransactionThread.getException() != null) {
            throw new RuntimeException("Failed to execute transaction", pTransactionThread.getException());
        }

        return pTransactionThread.getProcessResult();
    }

    public static void setDefaultHibernateFlushMode(FlushMode pDefaultFlushMode) {
        if (pDefaultFlushMode == null) {
            mDefaultHibernateFlushMode.remove();
        } else {
            mDefaultHibernateFlushMode.set(pDefaultFlushMode);
        }
    }

    public static FlushMode getDefaultHibernateFlushMode() {
        return mDefaultHibernateFlushMode.get();
    }

    public static boolean getProcessValidatesOnly() {
        Boolean value = mProcessValidatesOnly.get();
        if (value == null) {
            value = false;
        }
        return value;
    }
    public static void setProcessValidatesOnly(boolean pValidateOnly) {
        mProcessValidatesOnly.set(pValidateOnly);
    }

    private static void resetProcessValidatesOnly() {
        mProcessValidatesOnly.set(false);
    }

    public static <T> T getTransactionObserver(String pObserverName) {
        //noinspection unchecked
        return (T) mTransactionObservers.get().get(pObserverName);
    }

    public static ITransactionObserver registerTransactionObserver(ITransactionObserver pObserver) {
        return registerTransactionObserver(pObserver.getObserverName(), pObserver);
    }

    public static ITransactionObserver registerTransactionObserver(String pObserverName, ITransactionObserver pObserver) {
        ITransactionObserver prevObserver = mTransactionObservers.get().put(pObserverName, pObserver);

        pObserver.registered();

        if (prevObserver != null) {
            prevObserver.unregistered();
        }

        return prevObserver;
    }

    public static void unregisterTransactionObserver(ITransactionObserver pObserver) {
        unregisterTransactionObserver(pObserver.getObserverName());
    }

    public static void unregisterTransactionObserver(String pObserverName) {
        if (mTransactionObservers.get().containsKey(pObserverName)) {
            mTransactionObservers.get().remove(pObserverName).unregistered();
        }
    }

    public static IProcessObserver getProcessObserver(String pObserverName) {
        return mProcessObservers.get().get(pObserverName);
    }

    public static boolean registerProcessObserver(IProcessObserver pObserver) {
        return registerProcessObserver(pObserver.getName(), pObserver);
    }

    public static boolean registerProcessObserver(String pObserverName, IProcessObserver pObserver) {
        if(!mProcessObservers.get().containsKey(pObserverName)) {
            mProcessObservers.get().put(pObserverName, pObserver);
            pObserver.registered();
            return true;
        }
        return false;
    }

    public static void unregisterProcessObserver(IProcessObserver pObserver) {
        unregisterProcessObserver(pObserver.getName());
    }

    public static void unregisterProcessObserver(String pObserverName) {
        if (mProcessObservers.get().containsKey(pObserverName)) {
            mProcessObservers.get().remove(pObserverName).unregistered();
        }
    }

    public static String getEnvironmentName() {
        return ConfigurationManager.getSettingValue(DatabaseConfigManager.MonolithDbToken, "dataAccess.env");
    }

    public static String getSpringProfile() {
        return System.getProperty("spring.profiles.active");
    }

    public static boolean isAWSEnvironment(){
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("AWS");
    }

    public static boolean isProdEnvironment() {
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("PROD");
    }

    public static boolean isStgEnvironment() {
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("STG");
    }

    public static boolean isDS2Environment() {
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("DS2");
    }

    public static boolean isStgSpringProfile() {
        String springProfile = getSpringProfile();
        return springProfile != null && springProfile.equalsIgnoreCase("stg");
    }

    public static boolean isDS2SpringProfile() {
        String springProfile = getSpringProfile();
        return springProfile != null && springProfile.equalsIgnoreCase("ds2");
    }

    public static boolean isDS2EnvironmentOrDS2SpringProfile() {
        return isDS2Environment() || isDS2SpringProfile();
    }

    public static boolean isSTGEnvironmentOrSTGSpringProfile() {
        return isStgEnvironment() || isStgSpringProfile();
    }

    public static boolean isParallelEnv() {
        return isDS2EnvironmentOrDS2SpringProfile() || isSTGEnvironmentOrSTGSpringProfile();
    }

    public static boolean isIntegrationTestEnvironment() {
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("LOCAL");
    }

    public static boolean isPerfEnvironment() {
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("PERF");
    }

    private static String getCallerMethod(String callingThis) {
        StackTraceElement st = getCallerStackTraceElement(callingThis);
        if (st == null) {
            return "";
        } else {
            return st.getClassName() + "." + st.getMethodName();
        }
    }

    private static StackTraceElement getCallerStackTraceElement(String callingThis) {
        boolean nextMethodIsACaller = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement st : stackTrace) {
            if (callingThis.startsWith(".*") && st.toString().matches(callingThis)) {
                nextMethodIsACaller = true;
            } else if (!callingThis.startsWith(".*") && st.toString().contains(callingThis)) {
                nextMethodIsACaller = true;
            } else {
                if (nextMethodIsACaller) {
                    return st;
                }
            }
        }
        return null;
    }

    private static String getStackTrace() {
        StringBuilder stackTraceString = new StringBuilder();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {  // Ignore first two elements
            StackTraceElement st = stackTrace[i];
            stackTraceString.append(" ");
            stackTraceString.append(st.getClassName()).append(".").append(st.getMethodName()).append(":").append(st.getLineNumber()).append(" -> ");
        }

        return stackTraceString.toString();
    }

    public static void printStackTrace(String log){
        StringBuffer logString= new StringBuffer(log);
        try {
            StackTraceElement[] stes = Thread.currentThread().getStackTrace();
            for (StackTraceElement ste : stes) {
                logString.append("\n"+ ste.toString());
            }
            mLogger.info(logString);
        }catch(Exception e){
            mLogger.error(log, e);
        }
    }

    public static void printStackTrace(String log, StandardLevel level) {

        String message = ExceptionUtils.getStackTrace(new RuntimeException(log));
        switch (level) {
            case INFO :
                mLogger.info(message);
                break;
            case WARN :
                mLogger.warn(message);
                break;
            case DEBUG :
                mLogger.debug(message);
                break;
            case ERROR:
                mLogger.error(message);
                break;
            default:
                mLogger.info(message);
        }
    }


    /*
       From Spring/http://code.google.com/p/aurora-project/source/browse/branches/LWAP2.0/org/lwap/database/c3p0/C3P0NativeJdbcExtractor.java?r=2543
    */
    public static class C3P0NativeJdbcExtractor {
            private final Method getRawConnectionMethod;

            public C3P0NativeJdbcExtractor() {
                    try {
                            this.getRawConnectionMethod = getClass().getMethod(
                                            "getRawConnection", new Class[] { Connection.class });
                    } catch (NoSuchMethodException ex) {
                            throw new IllegalStateException(
                                            "Internal error in C3P0NativeJdbcExtractor: "
                                                            + ex.getMessage());
                    }
            }

            @SuppressWarnings("UnusedDeclaration") //used via reflection
            public static Connection getRawConnection(Connection con) {
                    return con;
            }

            public Connection getNativeConnection(Connection con) throws Exception {
                    if (con instanceof C3P0ProxyConnection) {
                            C3P0ProxyConnection cpCon = (C3P0ProxyConnection) con;
                            return (Connection) cpCon.rawConnectionOperation(
                                            this.getRawConnectionMethod, null,
                                            new Object[] { C3P0ProxyConnection.RAW_CONNECTION });

                    }
                    return con;
            }
    }

    public static boolean testDBConnect(){

        SystemParameter sysParamTimezone = SystemParameter.findSystemParameter(PSP_DATE_TIMEZONE_OFFSET);
        if (sysParamTimezone == null)
            return false;
        else
            return true;
    }

    private static void registerTransactionObservers() {
        registerTransactionObserver(new EntityEventTransactionObserver());
        registerTransactionObserver(new DGTransactionObserver());
        boolean hibernateFilterEnabled = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_COMPANY_FILTER);
        if(hibernateFilterEnabled) {
            if(Objects.isNull(companyTransactionObserver)) {
                companyTransactionObserver = new CompanyTransactionObserver();
            }
            registerTransactionObserver(companyTransactionObserver);
        }
        boolean hibernateDateFilterEnabled = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_HIBERNATE_DATE_FILTER);
        if(hibernateDateFilterEnabled) {
            if(Objects.isNull(dateTransactionObserver)) {
                dateTransactionObserver = new DateTransactionObserver();
            }
            registerTransactionObserver(dateTransactionObserver);
        }
    }

    public static Class getDomainEntityType(Object sourceEntity) {
        if(sourceEntity instanceof HibernateProxy) {
            return getActualClassFromProxy((HibernateProxy) sourceEntity);
        }
        return sourceEntity.getClass();
    }

    public static Class getActualClassFromProxy(HibernateProxy hibernateProxy) {
        return hibernateProxy.getHibernateLazyInitializer().getPersistentClass();
    }

    public static void initializeLogger(final String loggerPath) {
        Configurator.initialize(null, loggerPath);
    }

    public static SpcfCalendar getPreviousYearDate() {
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        return SpcfCalendar.createInstance(currentDate.getYear() - 1, currentDate.getMonth(), currentDate.getDay());
    }

    /**
     * Utility method to get connection from given session
     * @param session
     * @return
     */
    public static Connection getConnection(Session session) {
        return Objects.isNull(session) ? ((SessionImpl) Application.getHibernateSession()).connection() : ((SessionImpl) session).connection();
    }

    /**
     * Utility method to get connection from Application.getHibernateSession()
     * @return
     */
    public static Connection getConnection() {
        return getConnection(null);
    }

    /**
     *
     * @param entity The entity to evict
     * @throws IllegalArgumentException
     */
    public static void evict(Object entity) throws IllegalArgumentException {
        evict(entity, null);
    }
    /**
     * @see Session#evict(Object)
     * @param entity The entity to evict
     * @param session Hibernate session
     */
    public static void evict(Object entity, Session session) throws IllegalArgumentException {
        /**
         * Workaround for hibernate bug HHH-9013
         * See: https://hibernate.atlassian.net/browse/HHH-9013
         */
        final String HHH_9013_ERROR_MESSAGE_PREFIX = "Non-entity object instance passed to evict";
        try {
            if (Objects.isNull(session)) {
                getHibernateSession().evict(entity);
            } else {
                session.evict(entity);
            }
        } catch (IllegalArgumentException e) {
            // drop exception if it contains HHH_9013_ERROR_MESSAGE_PREFIX
            final String errorMessage = e.getMessage() ;
            if( errorMessage != null && errorMessage.startsWith( HHH_9013_ERROR_MESSAGE_PREFIX ) ) {
                // drop it
                if(mLogger.isDebugEnabled())
                    mLogger.debug("Exception in evict", e);
            }
            else {
                // do not catch it: it is not caused by the bug
                throw e ;
            }
        } catch (NullPointerException npe) {
            // do nothing. evict() throws NPE if the entity is null
        }
    }

    /**
     * hibernateSession.createCriteria() has been deprecated for JPA's Criteria since Hibernate 5
     * This is utility method to create CriteriaQuery
     * @param entity
     * @param session
     * @return
     */
    public static CriteriaQuery createCriteriaQuery(Class entity, Session session){
        // Create CriteriaBuilder
        CriteriaBuilder builder = session.getCriteriaBuilder();
        // Create CriteriaQuery
        CriteriaQuery criteria = builder.createQuery(entity);
        // Specify criteria root
        criteria.from(entity);

        return criteria;
    }

    public static String getQueryComment() {
        String queryComment = "";
        try {
            boolean enableQueryComment = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_QUERY_COMMENT);
            if (!enableQueryComment)
                return queryComment;

            StackTraceElement callerMethod = getCallerStackTraceElement(".*(Application|PayrollServices|DirtyCheckProcessCache|SystemParameter|getCurrentOffset|getPSPTime).*");
            if (callerMethod != null) {
                String fileName = callerMethod.getFileName().replaceAll("\\.java$", "");
                queryComment = String.format("%s:%s:%s", fileName, callerMethod.getMethodName(), callerMethod.getLineNumber());
            }
            return queryComment;
        } catch (Exception e) {
            return queryComment;
        }
    }

    private static boolean isHibernateCriteriaChangeRequired() {
        if(Objects.isNull(hibernateCriteriaStrategy)) {
            if (mLogger == null) {
                mLogger = getLogger(Application.class);
            }
            mLogger.error("hibernateCriteriaStrategy is null");
            return false;
        }
        return true;
    }

    public static <T> T nextSequenceValue(SequenceId sequenceId, Class<T> type) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        if(Objects.isNull(sequenceId))
            throw new IllegalArgumentException("sequenceId cannot be null");

        Boolean manageTransaction = !hasActiveTransaction();
        try {
            String sequenceName = sequenceId.getName();
            if (manageTransaction) beginUnitOfWork();
            preparedStatement = getConnection().prepareStatement(getDialect().getSequenceNextValString(sequenceName));
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            T sequence = resultSet.getObject(1, type);
            //TODO: IMP: remove this log after testing
            printStackTrace("Action=nextSequenceValue, sequenceName=" + sequenceName + ", sequence=" + sequence + ", stackTrace=", StandardLevel.DEBUG);
            return sequence;
        } catch (Exception e) {
            mLogger.error("Error in nextSequenceValue()", e);
            throw new RuntimeException(e);
        } finally {
                try {
                    if(resultSet != null)
                        resultSet.close();
                    if(preparedStatement != null)
                        preparedStatement.close();
                } catch (SQLException e) {
                    mLogger.error("Error in closing resultSet or preparedStatement", e);
                }
            if (manageTransaction) rollbackUnitOfWork();
        }
    }

    public static String getQueryName(String queryBaseName) {
        if (StringUtils.isEmpty(queryBaseName)) {
            throw new IllegalArgumentException("Query Name cannot be empty");
        }
        return isPostgresDB() ? queryBaseName.concat(POSTGRES_SUFFIX) : queryBaseName;
    }

    public static String getTruncFunctionString(String sqlVariable) {
        if (isPostgresDB()) {
            return "date_trunc('day'," + sqlVariable + ")";
        }else {
            return "trunc(" + sqlVariable + ")";
        }
    }

    public static String getUTCTimeExtractString(String sqlVariable) {
        if (isPostgresDB()) {
            return "timezone('UTC', cast(" + sqlVariable + " AS timestamptz))";
        } else {
            return "SYS_EXTRACT_UTC(" + sqlVariable + ")";
        }
    }
}

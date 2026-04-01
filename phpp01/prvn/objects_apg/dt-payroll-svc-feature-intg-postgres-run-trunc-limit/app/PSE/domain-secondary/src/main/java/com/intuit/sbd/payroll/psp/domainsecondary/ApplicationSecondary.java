package com.intuit.sbd.payroll.psp.domainsecondary;

import com.intuit.sbd.payroll.psp.*;
import com.intuit.sbd.payroll.psp.c3p0.C3P0PoolManager;
import com.intuit.sbd.payroll.psp.cache.SessionCache;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseType;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConstants;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domainsecondary.hibernate.HibernateUtils;
import com.intuit.sbd.payroll.psp.domainsecondary.processes.TransactionThreadSecondary;
import com.intuit.sbd.payroll.psp.query.BuildHibernateCriteriaVisitorSecondary;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.util.DomainReflectionHelper;
import com.intuit.sbd.payroll.psp.util.IProcessObserver;
import com.intuit.sbd.payroll.psp.util.ITransactionObserver;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import org.apache.commons.lang.StringUtils;
import org.hibernate.*;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.internal.AbstractSessionImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.stat.EntityStatistics;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.*;
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
 * @author ssharma17
 */
public final class ApplicationSecondary {
    private volatile static ApplicationSecondary mApplication = null;
    private static SpcfLogger mLogger;
    private static ThreadLocal<SessionCache> mSessionCache = new ThreadLocal<SessionCache>();
    private static ThreadLocal<Session> mHibernateSessionCache = new ThreadLocal<Session>();
    private static ThreadLocal<FlushMode> mDefaultHibernateFlushMode = new ThreadLocal<FlushMode>();
    private static ThreadLocal<Boolean> mProcessValidatesOnly = new ThreadLocal<Boolean>();
    private static final String PSP_DATE_TIMEZONE_OFFSET = "PSP_DATE_TIMEZONE_OFFSET";

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
    private static ApplicationSecondary getApplication() {
        if (mApplication == null) {
            synchronized (ApplicationSecondary.class) {
                if (mApplication == null) {
                    try {
                        ApplicationSecondary newApplication = new ApplicationSecondary();
                        newApplication.startApplication();

                        mApplication = newApplication;
                    }
                    catch (Throwable ex) {
                        if (mLogger == null) {
                            mLogger = Application.getLogger(ApplicationSecondary.class);
                        }
                        mLogger.error("Error initializing in getApplication().", ex);
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return mApplication;
    }

    /**
     * Truncates all tables in database
     */
    public static void truncateTables() {
        // guard against calling truncateTables from a JUnit test against a non-LOCAL environment
        // i.e. don't accidentally wipe out testers data
        String callingMethod = getCallerMethod("org.junit.runner.JUnitCore.run");
        if (callingMethod.length() > 0 && !getEnvironmentName().equalsIgnoreCase("LOCAL")) {
            throw new RuntimeException("Attempting to run truncateTables() from " + getCallerMethod("truncateTables") + " for environment: " + getEnvironmentName());
        }

        //PSRV004006: Create safeguard such that truncate tables cannot be run against a non-local environment
        boolean manageSession = !ApplicationSecondary.hasActiveTransaction();
        if(manageSession) {
            ApplicationSecondary.beginUnitOfWork();
        }
        try {
            Connection connection = ApplicationSecondary.getConnection();
            String connectionURL = connection.getMetaData().getURL();

            if ( !(connectionURL.matches(".*(ds2|DS2|ds1|DS1|ds3|DS3|ltq(?!2)|LTQ(?!2)).*"))
                    && !(connectionURL.contains("orapbdqprd1.qcyf01.ie.intuit.net:1521/pbdprd"))
                    && !(connectionURL.contains("localhost:1521/ORCLCDB.localdomain"))
                    && !(connectionURL.contains("localhost:5432"))
                    && (!connectionURL.contains("XE")
                    || connectionURL.matches(".*(qa|QA|prd|PRD|lt(?!2)|LT(?!2)|prf|PRF|perf|PERF).*"))) {

                throw new RuntimeException("cannot truncate tables against URL: " + connectionURL);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(manageSession) {
                ApplicationSecondary.rollbackUnitOfWork();
            }
        }

        for (String sqlCommand : readStrings("resources/TruncateTablesSecondary.sql")) {
            executeSqlCommand(sqlCommand, true);
        }
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
            BuildHibernateCriteriaVisitorSecondary visitor = new BuildHibernateCriteriaVisitorSecondary(expression);
            Criteria hibernateCriteria = visitor.visit(c);

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
            BuildHibernateCriteriaVisitorSecondary visitor = new BuildHibernateCriteriaVisitorSecondary(expression);
            Criteria hibernateCriteria = visitor.visit(c);
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
            BuildHibernateCriteriaVisitorSecondary visitor = new BuildHibernateCriteriaVisitorSecondary<>(expression);
            Criteria hibernateCriteria = visitor.visit(c);

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
        Query query = ApplicationSecondary.getHibernateSession().createQuery(pHQLQuery);
        query.setReadOnly(true);
        return query;
    }

    @Deprecated
    /**
     * @deprecated use one of the other methods here or HQLBuilder
     */
    public static Query createHibernateQuery(String queryString) {
        return ApplicationSecondary.getHibernateSession().createQuery(queryString);
    }

    @Deprecated
    /**
     * @deprecated use one of the other methods here or HQLBuilder
     */
    public static Query getNamedQuery(String queryName) {
        return ApplicationSecondary.getHibernateSession().getNamedQuery(queryName);
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
            String callerMethod = getCallerMethod("ApplicationSecondary");
            if (!callerMethodIsInExclusionList(SystemParameter.Code.RESULTSET_SIZE_ALERT_EXCLUSION_LIST, callerMethod)) {
                Application.getLogger(ApplicationSecondary.class).warn("Large result set retrieved for " + queryName + " (" + recordCount + " rows)" + " Stack trace: " + getStackTrace());
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
            Connection connection = ApplicationSecondary.getConnection(session);
            statement = connection.prepareStatement(sqlCommand);
            // PSP-16104-To Add timeout feature for sqlconsole
            statement.setQueryTimeout(SystemParameter.findIntValue(SystemParameter.Code.SQL_QUERY_TIMEOUT,300));
            // Bind the IN parameters
            int parameterCount = 1;
            for (Object parameterValue : paramValues) {
                if (parameterValue instanceof Timestamp) {
                    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    statement.setTimestamp(parameterCount, (Timestamp) parameterValue, utcCalendar);
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

    public static void executeSqlProcedure(String procedureName, boolean pCommit, Object... inParameterValues) {
        executeSqlProcedureWithOutParameter(procedureName, pCommit, NoOutParameter, inParameterValues);
    }

    /**
     * Execute a Sql procedure that takes 0..many IN parameters and has one OUT parameter
     *
     * @param procedureName          : Name of the database stored procedure to execute
     * @param outParameterReturnType : Specifies the type of the sole OUT parameter (java.sql.Types.*)
     * @param inParameterValues      : Specifies the values for the IN parameters (if any)
     * @return : returns the value of the OUT parameter
     */
    public static <T> T executeSqlProcedureWithOutParameter(String procedureName, boolean commit, int outParameterReturnType, Object... inParameterValues) {
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
            Connection connection = ApplicationSecondary.getConnection(session);
            statement = connection.prepareCall(BuildCallableStmt(procedureName, inParameterValues.length, outParameterReturnType != NoOutParameter));

            // Bind the IN parameters
            int parameterCount = 1;
            for (Object parameterValue : inParameterValues) {
                if (parameterValue instanceof Timestamp) {
                    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    statement.setTimestamp(parameterCount, (Timestamp) parameterValue, utcCalendar);
                } else {
                    statement.setObject(parameterCount, parameterValue);
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
                outParameterValue = (T) statement.getObject(inParameterValues.length + 1);
            }

        } catch (SQLException e) {
            exceptionPending = true;
            throw new RuntimeException("Problem executing SQL: " + procedureName, e);
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
     * Execute a Sql procedure that takes 0..many IN parameters and returns a result set.
     * <p/>
     * Note that the JDBC Statement object produced in this method cannot be closed since that would also close
     * the ResultSet; however, when the ResultSet is GC'd, the cursor will automatically be closed, at which time
     * the Statement will also be GC'd and subsequently closed.
     * <p/>
     * Note: For stored procedures, JDBC requires that the cursor being returned must be registered as an OUT parameter
     * only (i.e. not INOUT) and Oracle requires that it be the first parameter in the param list of the proc.
     *
     * @param procedureName         : Name of the database stored procedure to execute
     * @param pResultSetType        : ResultSet.TYPE_FORWARD_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE, or ResultSet.TYPE_SCROLL_SENSITIVE
     * @param pResultSetConcurrency : ResultSet.CONCUR_READ_ONLY or ResultSet.CONCUR_UPDATABLE
     * @param inParameterValues     : Specifies the values for the IN parameters (if any)
     * @return : A ResultSet object (cursor) as returned by the stored procedure.
     */
    public static ResultSet executeSqlProcedure(String procedureName,
                                                int pResultSetType,
                                                int pResultSetConcurrency,
                                                Object... inParameterValues) {
        ResultSet resultSet;

        try {
            Connection connection = ApplicationSecondary.getConnection();

            CallableStatement statement = connection.prepareCall(
                    BuildCallableStmt(procedureName, inParameterValues.length, true),
                    pResultSetType, pResultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);

            // Bind the OUT parameter (always a cursor in this case)
            statement.registerOutParameter(1, OracleTypes.CURSOR);

            // Bind the IN parameters
            int parameterCount = 2;
            for (Object parameterValue : inParameterValues) {
                if (parameterValue instanceof Timestamp) {
                    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    statement.setTimestamp(parameterCount, (Timestamp) parameterValue, utcCalendar);
                } else {
                    statement.setObject(parameterCount, parameterValue);
                }

                ++parameterCount;
            }

            statement.executeQuery();

            resultSet = (ResultSet) statement.getObject(1);
        } catch (SQLException e) {
            throw new RuntimeException("Problem executing SQL: " + procedureName, e);
        }

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
        DomainEntitySet<T> set = new DomainEntitySet<T>();
        for (Map map : maps) {
            //noinspection unchecked
            T item = (T) map.get("this");
            if (!set.contains(item)) {
                set.add(item);
                for (String eagerlyFilteredCollection : pEagerlyFilteredCollections) {
                    DomainReflectionHelper.reinitializeCollection(item, eagerlyFilteredCollection);
                }
            }
            for (String eagerlyFilteredCollection : pEagerlyFilteredCollections) {
                Object collectionItem = map.get(pPropertyPathToAlias.get(eagerlyFilteredCollection));
                //will be null if outer join and no elements.  want to return empty collection instead.
                if (collectionItem != null) {
                    DomainReflectionHelper.addItemToCollection(item, eagerlyFilteredCollection, (DataObject) collectionItem);
                }
            }
        }

        return set;
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
        ApplicationSecondary.beginUnitOfWork(FlushMode.AUTO);
    }

    public static void beginUnitOfWork(FlushMode pFlushMode) {
        ApplicationSecondary.beginUnitOfWork(pFlushMode, null);
    }

    /*
     * @param pReadOnly when true, objects returned out of UOW will by default be read only.  This can
     *                  improve performance as dirty checking will not be used.
     *                  It should be used in conjunction with a flush mode of Manual
     */
    public static void beginUnitOfWork(FlushMode pFlushMode, boolean pReadOnly) {
        ApplicationSecondary.beginUnitOfWork(pFlushMode, null, pReadOnly);
    }

    public static void beginUnitOfWork(FlushMode pFlushMode, SpcfCalendar pPspDate) {
        ApplicationSecondary.beginUnitOfWork(pFlushMode, pPspDate, false);
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

            Session session = getNewHibernateSession();
            session.setFlushMode(pFlushMode);
            session.setDefaultReadOnly(pReadOnly);
            mHibernateSessionCache.set(session);

            if (isOracleDB()){
                setEndToEndMetrics(session);
            }

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
                ApplicationSecondary.getSessionCache().addNonHibernateObject("PSPDate", pPspDate.toLocal());
            }

            // load PSP date
            PSPDate.getPSPTime();
        } catch (PersistenceException e){
            mLogger.error("Exception in connection reset", e);
            Application.resetPoolIfRequired(e);
            throw e;
        }
    }

    public static DatabaseType getDatabaseType(){
        return DatabaseConfigManager.getDatabaseType(
                DatabaseConstants.AuditDbKey, DatabaseType.ORACLE.toString());
    }

    public static boolean isOracleDB(){
        return DatabaseType.ORACLE == getDatabaseType();
    }

    public static boolean isPostgresDB(){
        return DatabaseType.POSTGRES == getDatabaseType();
    }

    /*
    Sets metrics on session so more details available in OEM/toad
    Will be included on next request to database
     */
    private static void setEndToEndMetrics(Session pSession) {
        try {
            Connection connection = ApplicationSecondary.getConnection(pSession);

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
                StackTraceElement callerMethod = getCallerStackTraceElement(".*(ApplicationSecondary|PayrollServices|DirtyCheckProcessCache|SystemParameter|getCurrentOffset|getPSPTime).*");
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
            Application.getLogger(ApplicationSecondary.class).debug("Before flush/commit " + SpcfCalendar.getNow().toString());
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
            Application.getLogger(ApplicationSecondary.class).debug("After flush/commit " + SpcfCalendar.getNow().toString() + " Inserts: " + HibernateUtils.getSessionFactory().getStatistics().getEntityInsertCount() + " Updates: " + HibernateUtils.getSessionFactory().getStatistics().getEntityUpdateCount() + " " + HibernateUtils.getSessionFactory().getStatistics().toString() + "\n");

            for (String entityName : HibernateUtils.getSessionFactory().getStatistics().getEntityNames()) {
                EntityStatistics es = HibernateUtils.getSessionFactory().getStatistics().getEntityStatistics(entityName);
                Application.getLogger(ApplicationSecondary.class).debug(entityName + ": " + es.toString());
            }
            HibernateUtils.getSessionFactory().getStatistics().clear();
        }

        // These can't be on a finally block - we only want to clean up thread local if execution was successful
        // If it was not, a rollback call will follow that will then rollback the transaction and clean up thread local
        getSessionCache().clear();
        mSessionCache.set(null);
        mTransactionObservers.get().clear();
        mHibernateSessionCache.set(null);
    }

    /**
     * Rollback a unit of work
     */
    public static void rollbackUnitOfWork() {
        try {
            checkTotalSqlCallsTooLarge(getSessionCache().getTotalSqlCalls());

            // if there is no active transaction, silently no-op
            if (hasActiveTransaction()) {
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
        }
        finally {
            getSessionCache().clear();
            mSessionCache.set(null);
            mTransactionObservers.get().clear();
            mHibernateSessionCache.set(null);
        }
    }

    private static void checkTotalSqlCallsTooLarge(int totalSqlCalls) {
        int totalSqlCallsAlertThreshold = SystemParameter.findIntValue(SystemParameter.Code.TOTAL_SQL_CALLS_ALERT_THRESHOLD, 500);
        if (totalSqlCalls > totalSqlCallsAlertThreshold) {
            String callerMethod = getCallerMethod("ApplicationSecondary.");
            if (!callerMethodIsInExclusionList(SystemParameter.Code.TOTAL_SQL_CALLS_ALERT_EXCLUSION_LIST, callerMethod)) {
                Application.getLogger(ApplicationSecondary.class).info("Large number of total sql calls (" + totalSqlCalls + " calls) " +
                        "Caller: " + callerMethod + " " +
                        "Top calls: " + getSessionCache().getSqlCalls(totalSqlCallsAlertThreshold / 10) + " Stack trace: " + getStackTrace());
            }
        }
    }

    /**
     * Mimics application startup.
     * Using Domain Modules Logger Provider as in that is
     * the common logger provider for complete project
     */
    private void startApplication() {
        ConfigurationManager.ensureInitialization();
        mLogger = Application.getLogger(ApplicationSecondary.class);
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
        getApplication();
        HibernateUtils.initialize();
    }

    public static void uninitialize() {
        mApplication = null;
        HibernateUtils.shutdown();
    }

    private static Session getNewHibernateSession() {
        return HibernateUtils.getSessionFactory().openSession();
    }

    public static Session getHibernateSession() {
        return mHibernateSessionCache.get();
    }

    public static ClassMetadata getHibernateClassMetadata(Class entityClass) {
        return HibernateUtils.getSessionFactory().getClassMetadata(entityClass.getName());
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
        getHibernateSession().refresh(domainEntity);
        domainEntity.onRefresh();
        //noinspection unchecked
        return (T) findById(domainEntity.getClass(), domainEntity.getId());
    }




    /*
     * Executes transaction in a separate thread.
     *
     */
    public static <T> T executeTransactionThread(TransactionThreadSecondary<T> pTransactionThread) {
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
        return ConfigurationManager.getSettingValue(DatabaseConfigManager.AuditDbToken, "dataAccess.env");
    }

    public static boolean isProdEnvironment() {
        String environment = getEnvironmentName();
        return environment != null && environment.toUpperCase().contains("PROD");
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

    public static boolean testDBConnect(){

        SystemParameter sysParamTimezone = SystemParameter.findSystemParameter(PSP_DATE_TIMEZONE_OFFSET);
        if (sysParamTimezone == null)
            return false;
        else
            return true;
    }

    /**
     * Utility method to get connection from given session
     * @param session
     * @return
     */
    public static Connection getConnection(Session session) {
        return Objects.isNull(session) ? ((SessionImpl) ApplicationSecondary.getHibernateSession()).connection() : ((SessionImpl) session).connection();
    }

    /**
     * Utility method to get connection from ApplicationSecondary.getHibernateSession()
     * @return
     */
    public static Connection getConnection() {
        return getConnection(null);
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


}

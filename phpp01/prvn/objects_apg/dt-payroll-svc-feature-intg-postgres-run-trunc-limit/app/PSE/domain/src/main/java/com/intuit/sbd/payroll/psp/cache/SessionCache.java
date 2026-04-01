package com.intuit.sbd.payroll.psp.cache;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jun 2, 2008
 * Time: 12:36:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class SessionCache {
    /**
     * Constructor
     */
    public SessionCache() {
        // keep track where the UOW was originated for debugging purposes
        originOfUnitOfWork = null;
        boolean nextMethodIsACaller = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement st : stackTrace) {
            if (st.getMethodName().equals("beginUnitOfWork")) {
                nextMethodIsACaller = true;
            } else {
                if (nextMethodIsACaller) {
                    originOfUnitOfWork = String.format("%1$s(), %2$s:%3$s (%4$s)",
                            st.getMethodName(), st.getFileName(), st.getLineNumber(), st.getClassName());
                    break;
                }
            }
        }
    }

    /*
      Cache to map natural keys into primary keys.
    */
    private HashMap<NaturalKey, SpcfUniqueId> naturalKeyToPrimaryKeyMap = new HashMap<NaturalKey, SpcfUniqueId>();
    private String originOfUnitOfWork;

    public SpcfUniqueId getPrimaryKey(NaturalKey naturalKey) {
        return naturalKeyToPrimaryKeyMap.get(naturalKey);
    }

    public void addPrimaryKey(NaturalKey naturalKey, SpcfUniqueId primaryKey) {
        if (primaryKey != null) {
            naturalKeyToPrimaryKeyMap.put(naturalKey, primaryKey);
        }
    }

    public void removePrimaryKey(NaturalKey naturalKey) {
        naturalKeyToPrimaryKeyMap.remove(naturalKey);
    }

    public boolean isPrimaryKeyCacheInitialized() {
        return naturalKeyToPrimaryKeyMap.size() > 0;
    }


    /*
      Cache for collections. Note: we do not keep actual entities because of SPC-F behavior of returning a copy
      of an entity upon save
    */
    private String getEntityCollectionCacheKey(Class c, String externalKey) {
        return c.getName() + ":" + externalKey;
    }

    private HashMap<String, HashSet<SpcfUniqueId>> entityCollectionCache = new HashMap<String, HashSet<SpcfUniqueId>>();

    public <T extends DomainEntity> void addEntityCollection(Class c, String externalKey, DomainEntitySet<T> entityCollection) {
        HashSet<SpcfUniqueId> ids = new HashSet<SpcfUniqueId>();

        for (T entity : entityCollection) {
            if (Application.getHibernateSession().contains(entity)) {
                ids.add(entity.getId());
            }
        }

        entityCollectionCache.put(getEntityCollectionCacheKey(c, externalKey), ids);
    }

    public <T extends DomainEntity> void addEntity(Class c, String externalKey, T entity) {
        HashSet<SpcfUniqueId> ids = entityCollectionCache.get(getEntityCollectionCacheKey(c, externalKey));
        if (Application.getHibernateSession().contains(entity)) {
            ids.add(entity.getId());
        }
    }

    public <T extends DomainEntity> void removeEntity(Class c, String externalKey, T entity) {
        HashSet<SpcfUniqueId> ids = entityCollectionCache.get(getEntityCollectionCacheKey(c, externalKey));
        if (ids != null) {
            ids.remove(entity.getId());
        }
    }

    public boolean isEntityCollectionCached(Class c, String key) {
        return entityCollectionCache.get(getEntityCollectionCacheKey(c, key)) != null;
    }

    public <T extends DomainEntity> DomainEntitySet<T> getEntityCollection(Class<T> c, String key) {
        HashSet<SpcfUniqueId> ids = entityCollectionCache.get(getEntityCollectionCacheKey(c, key));

        if (ids != null) {
            DomainEntitySet<T> entities = new DomainEntitySet<T>();

            for (SpcfUniqueId id : ids) {
                T entity = Application.<T>findById(c, id);

                if (entity != null) {
                    entities.add(entity);
                }
            }

            return entities;
        }
        else {
            return null;
        }
    }

    /*
      Cache for data object collections. Note: we do not keep actual entities because of SPC-F behavior of returning a copy
      of an entity upon save
    */
    private HashMap<String, HashSet<Object>> dataObjectCollectionCache = new HashMap<String, HashSet<Object>>();

    public <T extends DataObject> void addDataObjectCollection(Class c, String externalKey, DomainEntitySet<T> entityCollection) {
        HashSet<Object> ids = new HashSet<Object>();

        for (T entity : entityCollection) {
            ids.add(Application.getHibernateClassMetadata(c).getIdentifier(entity, (SharedSessionContractImplementor) null));
        }

        dataObjectCollectionCache.put(getEntityCollectionCacheKey(c, externalKey), ids);
    }

    public boolean isDataObjectCollectionCached(Class c, String key) {
        return dataObjectCollectionCache.get(getEntityCollectionCacheKey(c, key)) != null;
    }

    public <T extends DataObject> DomainEntitySet<T> getDataObjectCollection(Class<T> c, String key) {
        HashSet<Object> ids = dataObjectCollectionCache.get(getEntityCollectionCacheKey(c, key));

        if (ids != null) {
            DomainEntitySet<T> dataObjects = new DomainEntitySet<T>();

            for (Object id : ids) {
                T dataObject = Application.<T>findById(c, id);
                if (dataObject != null) {
                    dataObjects.add(dataObject);
                }
            }

            return dataObjects;
        }
        else {
            return null;
        }
    }

    /*
      Cache for non-hibernate objects.
    */
    private HashMap<Object, Object> nonHibernateObjectCache = new HashMap<Object, Object>();

    public <T> void addNonHibernateObject(Object key, T nonSpcfObject) {
        nonHibernateObjectCache.put(key, nonSpcfObject);
    }

    public void removeNonHibernateObject(Object key) {
        nonHibernateObjectCache.remove(key);
    }

    public <T> T getNonHibernateObject(Object key) {
        return (T) nonHibernateObjectCache.get(key);
    }


    /**
     * Cache Clean-up
     */
    public void clear() {
        naturalKeyToPrimaryKeyMap.clear();
        entityCollectionCache.clear();
        dataObjectCollectionCache.clear();
        nonHibernateObjectCache.clear();
        sqlCalls.clear();
        totalSqlCalls = 0;
    }

    /**
     * getOriginOfUnitOfWork
     * @return
     */
    public String getOriginOfUnitOfWork() {
        return originOfUnitOfWork;
    }

    private HashMap<String, Long> sqlCalls = new HashMap<String, Long>();
    private int totalSqlCalls = 0;

    public void trackSqlCall(String sqlCall) {
        Long executions = sqlCalls.get(sqlCall);
        if (executions == null) {
            executions = 0L;
        }
        else {
            executions++;
        }
        totalSqlCalls++;
        sqlCalls.put(sqlCall, executions);
    }

    public int getTotalSqlCalls() {
        return totalSqlCalls;
    }

    public String getSqlCalls(int threshold) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Long> sqlCall : sqlCalls.entrySet()) {
            if (sqlCall.getValue() > threshold) {
                sb.append(sqlCall.getKey() + ": " + sqlCall.getValue() + " ");
            }
        }
        return sb.toString();
    }

}

package com.intuit.sbd.payroll.psp.cache;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: rnorian
 * Date: Jun 4, 2010
 * Time: 6:01:51 PM
 */
public class DirtyCheckProcessCache<Key extends NaturalKey, Entity extends DomainEntity> extends BaseProcessCache<Key, Entity> {
    private SpcfLogger logger = Application.getLogger(DirtyCheckProcessCache.class);

    private static AtomicInteger enabledDirtyCacheCount = new AtomicInteger(0);

    private Expression<Entity> query;

    private AtomicReference<HashMap<Key, Entity>> parameterCache = new AtomicReference<HashMap<Key, Entity>>(new HashMap<Key, Entity>());
    private AtomicBoolean requiresRefresh = new AtomicBoolean(false);
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private AtomicReference<String> cacheToken = new AtomicReference<String>("");

    public DirtyCheckProcessCache(Class pValueClazz, Expression<Entity> pQuery, ScalarProperty... pScalarProperties) {
        super(pValueClazz, pScalarProperties);

        enabledDirtyCacheCount.incrementAndGet();

        query = pQuery;
        if (query == null) {
            query = new Query<Entity>();
        }
    }

    public Entity get(Key key) {
        // check if cache check has occurred for this UoW
        if (Application.getSessionCache().getNonHibernateObject("ProcessDataCached") == null) {
            performDirtyCheck();
            Application.getSessionCache().addNonHibernateObject("ProcessDataCached", new Boolean(true));
        }

        Entity entity = null;

        // if cache requires update don't block and instead read single value from table below
        if (!isRefreshing.get() && requiresRefresh.get()) {
            // use AtomicBoolean to allow only 1 thread to perform the cache refresh
            if (isRefreshing.compareAndSet(false, true)) {
                try {
                    cacheToken.set(getCurrentDBCacheTokenValue());

                    HashMap<Key, Entity> newCache = new HashMap<Key, Entity>();
                    for (Entity value : Application.find(entityClazz, query)) {
                        newCache.put((Key)keyMethod.invoke(value), value);
                    }

                    parameterCache.set(newCache);

                    requiresRefresh.set(false);
                    isRefreshing.set(false);
                } catch (Throwable t) {
                    logger.error("could not update ExpiringCache for " + entityClazz.getSimpleName(), t);
                    return null;
                }
            }
        }

        if (!isRefreshing.get()) {
            entity = parameterCache.get().get(key);
        }

        if (entity == null) {
            // force non-cache pull from DB
            SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(key);

            if (primaryKey == null) {
                Criterion<Entity> whereClause = keyProperties[0].equalTo(key.getKeyValues()[0]);
                for (int i = 1; i < keyProperties.length; i++) {
                    ScalarProperty keyProperty = keyProperties[i];
                    whereClause = whereClause.And(keyProperty.equalTo(key.getKeyValues()[i]));
                }

                Expression<Entity> exp = new Query<Entity>().Where(whereClause);
                for (Entity fetchedEntity : Application.find(entityClazz, exp)) {
                    try { Application.getSessionCache().addPrimaryKey((Key)keyMethod.invoke(fetchedEntity), fetchedEntity.getId()); }
                    catch (Throwable t) { logger.error("could not reflect onto fetched entity"); }
                }
                primaryKey = Application.getSessionCache().getPrimaryKey(key);
            }

            if (primaryKey != null) {
                entity = Application.findById(entityClazz, primaryKey);
                if (entity != null) {
                    Application.getSessionCache().addPrimaryKey(key, entity.getId());
                }
            }
        }
        
        return entity;
    }

    private void performDirtyCheck() {
        if (isRefreshing.get() || requiresRefresh.get())
            return;

        String existingToken = cacheToken.get();
        String dbToken = getCurrentDBCacheTokenValue();
        if (dbToken.equals(existingToken))
            return;

        requiresRefresh.compareAndSet(false, true);
    }

    private String getCurrentDBCacheTokenValue() {
        ArrayList<String> results = Application.executeNamedQuery("findCurrentProcessCacheId", null, null, false);
        if (results.size() != 1) {
            throw new RuntimeException("could not read currval from sequence: SEQ_PROCESS_CACHE_TOKEN");
        }
        return results.get(0);
    }

    public static void updateDBCacheTokenValue() {
        if (enabledDirtyCacheCount.get() > 0) {
            //Application.executeNamedQuery("findNextProcessCacheId", null, null);
            Application.executeSqlCommand("UPDATE PSP_SYSTEM_PARAMETER SET SYSTEM_PARAMETER_VALUE = ? WHERE SYSTEM_PARAMETER_CD = 'PROCESS_CACHE_REFRESH_TOKEN'", true, UUID.randomUUID().toString());
        }
    }
}

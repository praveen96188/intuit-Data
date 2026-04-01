package com.intuit.sbd.payroll.psp.cache;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: rnorian
 * Date: Jun 4, 2010
 * Time: 6:01:51 PM
 */
public class ExpiringProcessCache<Key extends NaturalKey, Entity extends DomainEntity> extends BaseProcessCache<Key, Entity> {
    private SpcfLogger logger = Application.getLogger(ExpiringProcessCache.class);

    private Expression<Entity> query;
    private SystemParameter.Code cacheIntervalKey;


    private AtomicReference<HashMap<Key, Entity>> parameterCache = new AtomicReference<HashMap<Key, Entity>>(new HashMap<Key, Entity>());
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private long lastCacheRefresh = 0;
    private long cacheExpirationMillis = 0;

    public ExpiringProcessCache(Class pValueClazz, SystemParameter.Code pCacheIntervalKey, Expression<Entity> pQuery, ScalarProperty... pEntityKeyProperties) {
        super(pValueClazz, pEntityKeyProperties);
        cacheIntervalKey = pCacheIntervalKey;

        query = pQuery;
        if (query == null) {
            query = new Query<Entity>();
        }
    }

    private long getCacheRefreshInterval() {
        Expression<SystemParameter> exp = new Query<SystemParameter>().Where(SystemParameter.SystemParameterCd().equalTo(cacheIntervalKey.name()));
        DomainEntitySet<SystemParameter> results = Application.<SystemParameter>find(SystemParameter.class, exp);
        if (results.size() != 1) {
            throw new RuntimeException("Could not find cache interval key: " + cacheIntervalKey.name());
        }

        return Long.parseLong(results.get(0).getSystemParameterValue()) * 1000;
    }

    public Entity get(Key naturalKey) {
        // has cache expired?
        if (System.currentTimeMillis() - lastCacheRefresh > cacheExpirationMillis) {
            // use AtomicBoolean to allow only 1 thread to perform the cache refresh;
            // all other threads continue to use existing cache
            if (isRefreshing.compareAndSet(false, true)) {
                if (System.currentTimeMillis() - lastCacheRefresh > cacheExpirationMillis) {
                    try {
                        HashMap<Key, Entity> newCache = new HashMap<Key, Entity>();
                        for (Entity value : Application.find(entityClazz, query)) {
                            newCache.put((Key)keyMethod.invoke(value), value);
                        }

                        parameterCache.set(newCache);

                        lastCacheRefresh = System.currentTimeMillis();
                        cacheExpirationMillis = getCacheRefreshInterval();
                        isRefreshing.set(false);
                    } catch (Throwable t) {
                        logger.error("could not update ExpiringCache for " + entityClazz.getSimpleName(), t);
                    }
                }
            }
        }

        Entity cachedEntity = parameterCache.get().get(naturalKey);

        // non-cached volatile entry? read from DB, store in session cache
        if (cachedEntity == null) {
            SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

            if (primaryKey == null) {
                Criterion<Entity> whereClause = keyProperties[0].equalTo(naturalKey.getKeyValues()[0]);
                for (int i = 1; i < keyProperties.length; i++) {
                    ScalarProperty keyProperty = keyProperties[i];
                    whereClause = whereClause.And(keyProperty.equalTo(naturalKey.getKeyValues()[i]));
                }

                Expression<Entity> exp = new Query<Entity>().Where(whereClause);
                for (Entity entity : Application.find(entityClazz, exp)) {
                    try { Application.getSessionCache().addPrimaryKey((Key)keyMethod.invoke(entity), entity.getId()); }
                    catch (Throwable t) { t.printStackTrace(System.err); }
                }
                primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
            }

            if (primaryKey != null) {
                cachedEntity = Application.findById(entityClazz, primaryKey);
                if (cachedEntity != null) {
                    Application.getSessionCache().addPrimaryKey(naturalKey, cachedEntity.getId());
                }
            }
        }

        return cachedEntity;
    }
}

package com.intuit.sbd.payroll.psp.cache;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.lang.reflect.Method;

/**
 * User: rnorian
 * Date: Jun 9, 2010
 * Time: 10:26:00 PM
 */
public abstract class BaseProcessCache<Key extends NaturalKey, Entity extends DomainEntity> {

    private static SpcfLogger logger = Application.getLogger(BaseProcessCache.class);

    public static ProcessCacheType getProcessCacheType() {
        Expression<SystemParameter> queryCacheMode =
                new Query<SystemParameter>().Where(SystemParameter.SystemParameterCd().equalTo(SystemParameter.Code.PROCESS_CACHE_TYPE.name()));
        DomainEntitySet<SystemParameter> cacheModeResults = Application.find(SystemParameter.class, queryCacheMode);

        ProcessCacheType cacheType = ProcessCacheType.SessionCaching;
        if (cacheModeResults.size() == 1) {
            try {
                cacheType = ProcessCacheType.valueOf(cacheModeResults.get(0).getSystemParameterValue().trim());
            } catch (Throwable t) {
                logger.error("defaulting to SessionCache ProcessCacheType -- could not read/interpret system parameter: " + SystemParameter.Code.PROCESS_CACHE_TYPE);
            }
        }

        return cacheType;
    }


    protected Class<? extends Entity> entityClazz;
    protected Method keyMethod;
    protected ScalarProperty[] keyProperties;

    protected BaseProcessCache(Class<? extends Entity> pEntityClazz) {
        this(pEntityClazz, new ScalarProperty[]{});
    }

    public BaseProcessCache(Class<? extends Entity> pEntityClazz, ScalarProperty... pEntityKeyProperties) {
        entityClazz = pEntityClazz;
        keyProperties = pEntityKeyProperties;

        String getMethodName = "getNaturalKey";
        try {
            keyMethod = pEntityClazz.getMethod(getMethodName);
        } catch (Throwable t) {
            throw new RuntimeException("could not reflect on to getNaturalKey getter", t);
        }
    }

    abstract public Entity get(Key key);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("cache: ").append(getClass().getSimpleName()).append("\n");
        builder.append("classtype: ").append(entityClazz.getSimpleName()).append("\n");
        builder.append("\t").append("keyProperties: ");
        for (ScalarProperty keyProperty : keyProperties) {
            builder.append(keyProperty.getPropertyName()).append(",");
        }
        builder.setLength(builder.length() - 1);

        return builder.toString();
    }
}

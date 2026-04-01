package com.intuit.sbd.payroll.psp.cache;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * User: rnorian
 * Date: Jun 9, 2010
 * Time: 11:08:35 PM
 */
public class SessionCacheWrapper<Key extends NaturalKey, Entity extends DomainEntity> extends BaseProcessCache<Key, Entity> {

    public SessionCacheWrapper(Class pValueClazz) {
        super(pValueClazz);
    }

    @Override
    public Entity get(Key key) {
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(key);

        if (primaryKey == null) {
            for (Entity entity : Application.find(entityClazz)) {
                try { Application.getSessionCache().addPrimaryKey((Key)keyMethod.invoke(entity), entity.getId()); }
                catch (Throwable t) { t.printStackTrace(); }
            }
            primaryKey = Application.getSessionCache().getPrimaryKey(key);
        }

        if (primaryKey != null) {
            return Application.findById(entityClazz, primaryKey);
        } else {
            return null;
        }
    }
}

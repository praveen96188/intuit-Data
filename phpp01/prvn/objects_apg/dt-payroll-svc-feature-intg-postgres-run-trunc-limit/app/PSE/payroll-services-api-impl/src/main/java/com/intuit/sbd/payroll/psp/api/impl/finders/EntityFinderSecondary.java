package com.intuit.sbd.payroll.psp.api.impl.finders;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.finders.IEntityFinder;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * @author achaves
 *         Date: Nov 7, 2007
 *         Time: 10:19:33 PM
 */
class EntityFinderSecondary implements IEntityFinder {
    public <T extends DomainEntity> T findById(Class<T> c, SpcfUniqueId uniqueId) {
        return ApplicationSecondary.findById(c, uniqueId);
    }

    public <T extends DataObject> T findById(Class<T> c, Object dataObjectUniqueId) {
        return ApplicationSecondary.findById(c, dataObjectUniqueId);
    }

    public <T extends DataObject> DomainEntitySet<T> find(Class<T> c, Expression<? super T> expression) {
        return ApplicationSecondary.find(c, expression);
    }

    public <T extends DomainEntity> DomainEntitySet<T> find(Class c) {
        return ApplicationSecondary.find(c);
    }

    public <T extends DataObject> DomainEntitySet<T> findObjects(Class c) {
        return ApplicationSecondary.findObjects(c);
    }
}

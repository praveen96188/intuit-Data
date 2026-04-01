package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.spc.foundations.portability.SpcfUniqueId;


/**
 * This is the PSP service API that deals with finding information in PSP's object model
 * <p>The API is read-only and exposes ways to find single objects or collections of objects
 */

public interface IEntityFinder {
    <T extends DomainEntity> T findById(Class<T> c, SpcfUniqueId uniqueId);

    <T extends DataObject> T findById(Class<T> c, Object dataObjectUniqueId);

    <T extends DataObject> DomainEntitySet<T> find(Class<T> c, Expression<? super T> expression);

    <T extends DomainEntity> DomainEntitySet<T> find(Class c);

    <T extends DataObject> DomainEntitySet<T> findObjects(Class c);
}

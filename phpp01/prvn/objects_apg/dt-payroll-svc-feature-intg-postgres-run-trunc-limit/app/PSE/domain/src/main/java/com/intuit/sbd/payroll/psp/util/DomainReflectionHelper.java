package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * User: dweinberg
 * Date: 1/9/13
 * Time: 1:08 PM
 */
public class DomainReflectionHelper {

    /*
    It was a cold night but she made it colder.  She walked into my office and said her husband
    had been cheating on her.  She wasn't upset.  She wasn't hurt.  Just the fact of the matter
    was that her husband had been cheating on her.  I turned around to get some paperwork, and
    in the mirror I saw how steadily she was holding that cup of coffee.
    Yes, in the java reflection was as empty and detached as a collection is after calling this method.
    */
    public static <T extends DataObject> void reinitializeCollection(T entity, String setName) {
        try {
            entity.getClass().getMethod("reinitialize" + setName).invoke(entity);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

    }

    public static <T extends DataObject> DomainEntitySet getCollection(T entity, String setName) {
        try {
            return (DomainEntitySet) entity.getClass().getMethod("get" + setName.replaceAll("Set$", "Collection")).invoke(entity);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static <T extends DataObject> void addItemToCollection(T entity, String setName, DataObject item) {
        //noinspection unchecked
        getCollection(entity, setName).add(item);
    }
}

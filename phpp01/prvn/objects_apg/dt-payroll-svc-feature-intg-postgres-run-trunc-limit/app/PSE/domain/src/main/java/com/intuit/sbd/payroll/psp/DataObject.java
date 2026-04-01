package com.intuit.sbd.payroll.psp;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metadata.ClassMetadata;


/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jun 15, 2008
 * Time: 8:11:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataObject implements Comparable {
    /**
     * Version
     */
    private static final long NewVersion = -1;
    private long mVersion = NewVersion;
    private boolean mIsVersionSet = false;

    void setVersion(long version) {
        if (version < mVersion) {
            throw new RuntimeException(toString() + "- Version is less than existing version");
        }

        mVersion = version;
        mIsVersionSet = true;
    }

    public long getVersion() {
        return mVersion;
    }

    public boolean isNew() {
        return mVersion == NewVersion;
    }

    /**
     * Not used in PSP - legacy from SPC-F
     */
    long getRealmId() {
        return -1;
    }

    void setRealmId(long realmId) {
    }

    public int compareTo(Object o) {
        if (o == null) return -1;

        ClassMetadata classMetadata = Application.getHibernateClassMetadata(this.getClass());

        Comparable lValue = (Comparable) classMetadata.getIdentifier(this, (SharedSessionContractImplementor) null);
        Comparable rValue = (Comparable) classMetadata.getIdentifier(o, (SharedSessionContractImplementor) null);

        return lValue.compareTo(rValue);
    }
}

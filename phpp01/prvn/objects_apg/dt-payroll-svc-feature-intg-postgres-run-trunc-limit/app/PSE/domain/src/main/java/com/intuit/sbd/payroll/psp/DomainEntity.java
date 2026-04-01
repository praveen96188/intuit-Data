package com.intuit.sbd.payroll.psp;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jun 15, 2008
 * Time: 8:11:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class DomainEntity extends DataObject {

    // track if the entity has been committed to the db yet
    private boolean mCreatedInCurrentSession = false;

    public void setCreatedInCurrentSession(boolean pCreatedInCurrentSession) {
        mCreatedInCurrentSession = pCreatedInCurrentSession;
    }

    public boolean isCreatedInCurrentSession() {
        return mCreatedInCurrentSession;
    }

    /**
     * Unique ID.
     */
    private SpcfUniqueId mId = SpcfUniqueId.generateRandomUniqueId();

    void setId(SpcfUniqueId uniqueId) {
        mId = uniqueId;
    }

    public SpcfUniqueId getId() {
        if (mId == null) {
            mId = SpcfUniqueId.generateRandomUniqueId();
        }

        return mId;
    }


    /**
     * Creator ID.
     */
    private String mCreatorId = null;

    public void setCreatorId(String creatorId) {
        mCreatorId = creatorId;
    }

    public String getCreatorId() {
        return mCreatorId;
    }

    /**
     * Created date and time.
     */
    private SpcfCalendar mCreatedDate = null;

    public void setCreatedDate(SpcfCalendar createdDate) {
        mCreatedDate = createdDate;
    }

    public SpcfCalendar getCreatedDate() {
        return mCreatedDate;
    }

    /**
     * Modifier ID.
     */
    private String mModifierId = null;

    public void setModifierId(String modifierId) {
        mModifierId = modifierId;
    }

    public String getModifierId() {
        return mModifierId;
    }

    /**
     * Modified date and time.
     */
    private SpcfCalendar mModifiedDate = null;

    public void setModifiedDate(SpcfCalendar modifiedDate) {
        mModifiedDate = modifiedDate;
    }

    public SpcfCalendar getModifiedDate() {
        return mModifiedDate;
    }


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

    // PSP query support
    public static final <T extends DomainEntity> ScalarProperty<T, SpcfCalendar> CreatedDate() {return new ScalarProperty<T, SpcfCalendar>(null, "CreatedDate");};
    public static final <T extends DomainEntity> ScalarProperty<T, SpcfCalendar> ModifiedDate() {return new ScalarProperty<T, SpcfCalendar>(null, "ModifiedDate");};
    public static final <T extends DomainEntity> ScalarProperty<T, SpcfUniqueId> Id() {return new ScalarProperty<T, SpcfUniqueId>(null, "Id");};
    public static final <T extends DomainEntity> ScalarProperty<T, String> ModifierId() {return new ScalarProperty<T, String>(null, "ModifierId");};
    public static final <T extends DomainEntity> ScalarProperty<T, String> CreatorId() {return new ScalarProperty<T, String>(null, "CreatorId");};

    /**
     * Not used in PSP - legacy from SPC-F
     */
    long getRealmId() {
        return -1;
    }

    void setRealmId(long realmId) {
    }

    String mTempId = SpcfUniqueId.generateRandomUniqueId().toString();

    void setTempId(String id) {
        mTempId = id;
    }

    String getTempId() {
        if (mTempId == null) {
            mTempId = SpcfUniqueId.generateRandomUniqueId().toString();
        }

        return mTempId;
    }

    public void onRefresh() {

    }

    /////////////////////////////////////// EQUALS AND HASHCODE //////////////////////////////////////

    /**
     * Equality.
     *
     * @param obj Object to compare to.
     * @return True if the object is non-null and is this exact Entity, or is a different object instance but has
     *         the same unproxied type and the same unique ID.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        }

        Class thisClass = this.getClass();
        Class otherClass = obj.getClass();

        if (otherClass != thisClass) {
            // If the classes don't match, that might be because the other object is a proxy for this type of Entity,
            // or this is a proxy and the other object is a non-proxied Entity of the same type. This second check
            // covers those cases.

            if (!thisClass.isAssignableFrom(otherClass) && !otherClass.isAssignableFrom(thisClass)) {
                return false;
            }
        }

        SpcfUniqueId id = getId();

        if (id == null) {
            return false;
        }

        DomainEntity entity = (DomainEntity) obj;

        SpcfUniqueId otherId = entity.getId();

        if (otherId == null) {
            return false;
        }

        return id.equals(otherId);
    }

    /**
     * Hash code.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        SpcfUniqueId id = getId();

        if (id != null) {
            return id.hashCode();
        } else {
            return super.hashCode();
        }
    }

}    


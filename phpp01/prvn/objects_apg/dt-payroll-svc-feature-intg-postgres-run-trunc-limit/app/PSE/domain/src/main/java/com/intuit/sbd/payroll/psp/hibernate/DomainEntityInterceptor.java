package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * An interceptor that intercepts the persistence events for
 * {@link com.intuit.sbd.payroll.psp.DomainEntity
 */
public final class DomainEntityInterceptor extends EmptyInterceptor {
    /**
     * The default date type.
     */
    @SuppressWarnings("unchecked")
    private static final Class sDefaultDateType = CustomType.class;

    /**
     * Default realm id value
     */
    private static final long sDefaultRealmIdValue = -1;
    /**
     * The default date type name.
     */
    private static final String sSpcfCalendarUserType = SpcfCalendarUserType.class.getName();

    /**
     * Property name of the creator ID in Entity.
     */
    private static final String CreatorIdPropertyName = "CreatorId";

    /**
     * Property name of the created date in Entity.
     */
    private static final String CreatedDatePropertyName = "CreatedDate";

    /**
     * Property name of the modifier ID in Entity.
     */
    private static final String ModifierIdPropertyName = "ModifierId";

    /**
     * Property name of the modified date in Entity.
     */
    private static final String ModifiedDatePropertyName = "ModifiedDate";

    /**
     * Property name of the realm id in Entity
     */
    private static final String RealmIdPropertyName = "RealmId";

    /**
     * Singleton instance of the interceptor.
     */
    private static final DomainEntityInterceptor mInstance = new DomainEntityInterceptor();

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainEntityInterceptor.class);

    /**
     * Object monitor
     */
    // private static Object mLock = new Object();

    /**
     * Returns the singleton instance of the interceptor.
     *
     * @return Singleton instance of interceptor.
     */
    public static final DomainEntityInterceptor getInstance() {
        return mInstance;
    }

    /**
     * Private default constructor.
     */
    private DomainEntityInterceptor() {
        super();
    }

    /**
     * Handler to intercept the onSave event prior to saving a new
     * {@link com.intuit.sbd.payroll.psp.DomainEntity
     * to the database.
     * In the handler, the created date and modified date of the
     * {@link com.intuit.sbd.payroll.psp.DomainEntity
     * are set.
     *
     * @see com.intuit.spc.foundations.subsystem.dataAccess.objectServices.ISpcfDataAccessInterceptor#onSave(Object,Object,Object[],String[],Object[])
     */
    @Override
    public final boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        boolean stateModified = super.onSave(entity, id, state, propertyNames, types);
        PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
        String currentId = null;
        if (currentPrincipal != null) {
            currentId = currentPrincipal.getId();
        }

        if (entity instanceof DomainEntity) {
            boolean modified = false;

            // Can't call PSPDate.getPSPTime() for SystemParameter (recursive call)
            SpcfCalendar now = SpcfCalendar.getNow();
            if (!(entity instanceof SystemParameter)) {
                PSPDate.ensureCurrentOffsetIsCached();
                now = PSPDate.getPSPTime();
            }

            modified = setCreatedDate(state, propertyNames, types, now);

            if (!stateModified && modified) {
                stateModified = modified;
                setCreatorId(state, propertyNames, currentId);
                ((DomainEntity)entity).setCreatedInCurrentSession(true);
            }

            modified = setModifiedDate(state, propertyNames, types, now);

            if (!stateModified && modified) {
                stateModified = modified;
            }

            if (stateModified) {
                setModifierId(state, propertyNames, currentId);
            }
        }

        return stateModified;
    }

    /**
     * Sets created date if the object is an Entity.
     *
     * @param state         The property values of the Entity to be saved to the database.
     * @param propertyNames The property names of the Entity.
     * @param types         The types of the properties of the Entity.
     * @param value         The value to set the property to.
     * @return True if any state has been modified by the handler, false otherwise.
     */
    private static final boolean setCreatedDate(
            Object[] state, String[] propertyNames, Object[] types, SpcfCalendar value) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        boolean stateModified = false;

        int index = findPropertyIndex(propertyNames, CreatedDatePropertyName);

        if (index != -1) {
            if (isUtcTimestampType(types[index])) {
                if (state[index] == null) {
                    state[index] = value;
                    stateModified = true;
                }
            } else {
                // Must throw CallbackException because this is thrown in the Hibernate context.

                throw new CallbackException("Property " + CreatedDatePropertyName + " is not of type " +
                        sDefaultDateType.getSimpleName());
            }
        } else {
            // Must throw CallbackException because this is thrown in the Hibernate context.

            throw new CallbackException("Property " + CreatedDatePropertyName + " not found");
        }

        return stateModified;
    }

    /**
     * Sets creator ID if the object is an Entity.
     *
     * @param state         The property values of the Entity to be saved to the database.
     * @param propertyNames The property names of the Entity.
     * @param value         The value to set the property to.
     */
    private static final void setCreatorId(
            Object[] state, String[] propertyNames, String value) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.
        int index = findPropertyIndex(propertyNames, CreatorIdPropertyName);
        if (index != -1) {
            state[index] = value;
        } else {
            // Must throw CallbackException because this is thrown in the Hibernate context.
            throw new CallbackException("Property " + CreatorIdPropertyName + " not found");
        }
    }

    /**
     * Sets modifier ID if the object is an Entity.
     *
     * @param state         The property values of the Entity to be saved to the database.
     * @param propertyNames The property names of the Entity.
     * @param value         The value to set the property to.
     */
    private static final void setModifierId(Object[] state, String[] propertyNames, String value) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        int index = findPropertyIndex(propertyNames, ModifierIdPropertyName);
        if (index != -1) {
            state[index] = value;
        } else {
            // Must throw CallbackException because this is thrown in the Hibernate context.
            throw new CallbackException("Property " + CreatorIdPropertyName + " not found");
        }
    }

    /**
     * Handler to intercept the onUpdate event prior to saving an existing
     * {@link com.intuit.sbd.payroll.psp.DomainEntity
     * to the database.
     * In the handler, the modified date of the
     * {@link com.intuit.sbd.payroll.psp.DomainEntity
     * is set.
     *
     * @see com.intuit.spc.foundations.subsystem.dataAccess.objectServices.ISpcfDataAccessInterceptor#onUpdate(Object,Object,Object[],Object[],String[],Object[])
     */
    @Override
    public final boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
        String[] propertyNames, Type[] types) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        boolean stateModified = super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);

        PspPrincipal currentPrincipal = Application.getCurrentPrincipal();
        String currentId = null;
        if (currentPrincipal != null) {
            currentId = currentPrincipal.getId();
        }

        if (entity instanceof DomainEntity) {
            boolean modified = false;

            // Can't call PSPDate.getPSPTime() for SystemParameter (recursive call)            
            SpcfCalendar now = SpcfCalendar.getNow();
            if (!(entity instanceof SystemParameter)) {
                PSPDate.ensureCurrentOffsetIsCached();
                now = PSPDate.getPSPTime();
            }

            modified = setModifiedDate(currentState, propertyNames, types, now);

            if (!stateModified && modified) {
                stateModified = modified;
            }

            if (stateModified) {
                setModifierId(currentState, propertyNames, currentId);
            }

        }

        return stateModified;
    }

    /**
     * Sets modified date if the object is an Entity.
     *
     * @param states        The property values of the Entity to be saved to the database.
     * @param propertyNames The property names of the Entity.
     * @param types         The types of the properties of the Entity.
     * @param value         The value to set the property to.
     * @return True if any state has been modified by the handler, false otherwise.
     */
    private static final boolean setModifiedDate(
            Object[] states, String[] propertyNames, Object[] types, SpcfCalendar value) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        boolean stateModified = false;

        int index = findPropertyIndex(propertyNames, ModifiedDatePropertyName);

        if (index != -1) {
            if (isUtcTimestampType(types[index])) {
                states[index] = value;
                stateModified = true;
            } else {
                // Must throw CallbackException because this is thrown in the Hibernate context.

                throw new CallbackException("Property " + ModifiedDatePropertyName + " is not of type "
                        + sDefaultDateType.getSimpleName());
            }
        } else {
            // Must throw CallbackException because this is thrown in the Hibernate context.

            throw new CallbackException("Property " + ModifiedDatePropertyName + " not found");
        }

        return stateModified;
    }

    /**
     * Finds the location of the specified property in the array of properties.
     *
     * @param propertyNames The array of properties in which to find the property.
     * @param propertyName  The name of the property to find.
     * @return The 0-based index of the property if found, -1 otherwise.
     */
    private static final int findPropertyIndex(String[] propertyNames, String propertyName) {
        int index = -1;

        for (int i = 0; i < propertyNames.length; i++) {
            if (propertyNames[i].equals(propertyName)) {
                index = i;
                break;
            }
        }

        return index;
    }

    /**
     * Returns whether this is our date type.
     *
     * @param type The type to test.
     * @return Return True if the object is of type
     *         {@link com.intuit.spc.foundations.subsystemSpecific.dataAccess.objectServices.hibernate.SpcfCalendarUserType},
     *         False otherwise.
     */
    private static final boolean isUtcTimestampType(Object type) {
        return type.getClass().equals(sDefaultDateType) && ((CustomType) type).getName().equals(sSpcfCalendarUserType);
    }

    /**
     * @see com.intuit.spc.foundations.subsystem.dataAccess.objectServices.ISpcfDataAccessInterceptor#onLoad(Object, Object, Object[], String[], Object[])
     */
    @Override
	public final boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        boolean modified = super.onLoad(entity, id, state, propertyNames, types);

        return modified;
    }

    /**
     * @see com.intuit.spc.foundations.subsystem.dataAccess.objectServices.ISpcfDataAccessInterceptor#onDelete(Object, Object, Object[], String[], Object[])
     */
    @Override
	public final void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        // We don't check arguments for null because this is called by Hibernate, which presumably sends
        // in valid arguments.

        super.onDelete(entity, id, state, propertyNames, types);
    }
}

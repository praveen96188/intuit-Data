package com.intuit.sbd.payroll.psp.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.Application;

/**
 * This class implements the UserType to persist SpcfUniqueId to the database.
 */
public final class SpcfUniqueIdUserType implements UserType, Serializable
{
    private static final long serialVersionUID = -5421900602922035504L;
    /**
     * logger for logging SQL binding variable with the SQL. Use log4j logger
     * so we respect the same log level and log to where Hibernate logs.
     */
    private static final SpcfLogger sHibernateLogger = Application.getLogger("org.hibernate.type");

    /**
     * Types returned by sql-types method.
     */
    private static final int[] sSqlTypes = new int[] { Types.VARCHAR };

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public final int[] sqlTypes()
    {
        return sSqlTypes;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(Object, Object)
     */
    public final boolean equals(Object x, Object y)
    {
        return (x == null) ? (y == null) : x.equals(y);
    }

    /**
     * @see org.hibernate.usertype.UserType#hashCode(Object)
     */
    public final int hashCode(Object obj)
    {
       return obj.hashCode();
    }

    /**
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public final boolean isMutable()
    {
        return true;
    }

    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @SuppressWarnings("unchecked")
    public final Class returnedClass()
    {
        return SpcfUniqueId.class;
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(Object)
     */
    public final Object deepCopy(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof String)
        {
            return SpcfTypeConversionUtilities.convertStringToUniqueId((String)value);
        }
        else if (value instanceof SpcfUniqueId)
        {
            return value;
        }
        else
        {
            throw new HibernateException("Unsupported value type: " + value.getClass());
        }
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeGet(ResultSet, String[], SharedSessionContractImplementor, Object)
     */
    @Override
    public final Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor sharedSessionContractImplementor, Object owner) throws SQLException
    {
        String uniqueId = rs.getString(names[0]);

        return SpcfTypeConversionUtilities.convertStringToUniqueId(uniqueId);


    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(PreparedStatement, Object, int, SharedSessionContractImplementor)
     */
    @Override
    public final void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException
    {

        Object localValue = value;

        // convert from SpcfUniqueId to String
        if (value instanceof SpcfUniqueId)
        {
            localValue = SpcfTypeConversionUtilities.convertUniqueIdToString((SpcfUniqueId)value);
        }
        // If value is null or BigDecimal then simply save the value
        if (sHibernateLogger.isDebugEnabled())
        {
            sHibernateLogger.debug(this.getClass().getSimpleName() + " - binding '"
                    + value + "' to parameter: " + index);
        }

        st.setString(index, (String)localValue);

    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(Serializable, Object)
     */
    public final Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        return null;
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(Object)
     */
    public final Serializable disassemble(Object value) throws HibernateException
    {
        return null;
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(Object, Object, Object)
     */
    public final Object replace(Object original, Object target, Object owner) throws HibernateException
    {
        return deepCopy(original);
    }
}
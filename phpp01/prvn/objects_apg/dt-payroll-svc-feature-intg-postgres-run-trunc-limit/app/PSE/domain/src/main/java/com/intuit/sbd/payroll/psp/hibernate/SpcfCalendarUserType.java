package com.intuit.sbd.payroll.psp.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.Application;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * This class implements the UserType to persist SpcfCalendar to the database.
 */
public final class SpcfCalendarUserType implements UserType, Serializable
{
    private static final long serialVersionUID = -8575262630241601857L;

    /**
     * logger for logging SQL binding variable with the SQL. Use log4j logger
     * so we respect the same log level and log to where Hibernate logs.
     */
    private static final SpcfLogger sHibernateLogger = Application.getLogger("org.hibernate.type");

    /**
     * Types returned by sql-types method.
     */
    private static final int[] sSqlTypes = new int[] { Types.TIMESTAMP };

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
       return SpcfCalendar.class;
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
        else if (value instanceof Timestamp)
        {
            return SpcfTypeConversionUtilities.convertSqlTimestampToSpcfCalendar((Timestamp)value);
        }
        else if (value instanceof Date)
        {
            return new java.sql.Timestamp(((Date)value).getTime());
        }
        else if (value instanceof SpcfCalendar)
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
        //Change to support using SpcfCalendarUserType - instead of date property accessors
        Timestamp ts = rs.getTimestamp(names[0], Calendar.getInstance(TimeZone.getTimeZone("UTC")));

        if (sHibernateLogger.isDebugEnabled())
        {
            sHibernateLogger.debug(this.getClass().getSimpleName() + " - returning '"
                    + ts + " as column: " + names[0]);
        }

        return SpcfTypeConversionUtilities.convertSqlTimestampToSpcfCalendar(ts);

    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(PreparedStatement, Object, int, SharedSessionContractImplementor)
     */
    @Override
    public final void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException
    {
        Object localValue = value;
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        // The explicit check for null is really not necessary because null is not an instanceof anything.
        // But doing so helps to clarify the intent and also avoids having to call deepCopy() unnecessarily.
        if (localValue != null && ! (localValue instanceof Timestamp))
        {
            localValue = SpcfTypeConversionUtilities.convertSpcfCalendarToSqlTimestamp((SpcfCalendar)localValue);
        }

        if (sHibernateLogger.isDebugEnabled())
        {
            sHibernateLogger.debug(this.getClass().getSimpleName() + " - binding '"
                    + value + ", " + utcCalendar + "' to parameter: " + index);
        }

        st.setTimestamp(index, (Timestamp)localValue, utcCalendar);

    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(Serializable, Object)
     */
    public final Object assemble(Serializable cached, Object owner) throws HibernateException
    {
        if (cached == null) return null;
        GregorianCalendar calendar = (GregorianCalendar) cached;
        return new SpcfCalendarImpl(calendar);
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(Object)
     */
    public final Serializable disassemble(Object value) throws HibernateException
    {
        if (value == null) return null;
        SpcfCalendarImpl valueAsSpcfCalendar = (SpcfCalendarImpl) value;
        return valueAsSpcfCalendar.toSpecific();
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(Object, Object, Object)
     */
    public final Object replace(Object original, Object target, Object owner) throws HibernateException
    {
       return deepCopy(original);
    }
}
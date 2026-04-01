package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.slf4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.Application;


/**
 * This class implements the UserType to persist SpcfDecimal to the database.
 */
public final class SpcfDecimalUserType implements UserType, Serializable
{
    private static final long serialVersionUID = -2327070400729190753L;
    /**
     * logger for logging SQL binding variable with the SQL. Use log4j logger
     * so we respect the same log level and log to where Hibernate logs.
     */
    private static final SpcfLogger sHibernateLogger = Application.getLogger("org.hibernate.type");

    /**
     * SpcfDecimal wraps over BigDecimal which is wraps over
     * java.math.BigDecimal. According to hibernate documentation big_decimal
     * Type mappings from java.math.BigDecimal and java.math.BigInteger to NUMERIC
     * TYPES.DECIMAL is not supported by the Mysql dialect hence using NUMERIC
     */
    private static final int[] sSqlTypes = new int[] { Types.NUMERIC };

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
     * SpcfDecimal is not Mutable
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public final boolean isMutable()
    {
        return false;
    }

    /**
     * Return the wrapped class which is this
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @SuppressWarnings("unchecked")
    public final Class returnedClass()
    {
       return SpcfDecimal.class;
    }

    /**
     * Return the SpcfDecimal object from the value
     * @see org.hibernate.usertype.UserType#deepCopy(Object)
     */
    public final Object deepCopy(Object value)
    {
        if (value == null)
        {
            return null;
        }
        else if (value instanceof java.math.BigDecimal)
        {
        	//Need the DecimalImpl since it has constructor which takes BigDecimal
			return new SpcfDecimalImpl((java.math.BigDecimal) value);
        }
        else if (value instanceof SpcfDecimalImpl)
        {
            return value;
        }
        else
        {
            throw new HibernateException("Unsupported value type: " + value.getClass());
        }
    }

    /**
     * Get the BigDecimal column from the ResultSet and convert it to a SpcfDecimal object
     * @see org.hibernate.usertype.UserType#nullSafeGet(ResultSet, String[], SharedSessionContractImplementor, Object)
     */
    @Override
    public final Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor sharedSessionContractImplementor, Object owner) throws SQLException
    {
    	SpcfDecimalImpl spcfDecimal;

        //convert the data stored in the database from java.math.BigDecimal to SpcfDecimal
        java.math.BigDecimal bigDecimalDb = rs.getBigDecimal(names[0]);

        //If the property is not set then we may get a null value, but
        //we must not send null values to the SpcfDecimalImpl constructor
        if(bigDecimalDb == null)
        {
        	return null;
        }
        else
        {
        	spcfDecimal = new SpcfDecimalImpl(bigDecimalDb);
        }

        if (sHibernateLogger.isDebugEnabled())
        {
            sHibernateLogger.debug(this.getClass().getSimpleName() + " - unbinding '" + bigDecimalDb +
            		//"'with precision '" + bigDecimalDb.precision() + "'and scale '" + bigDecimalDb.scale() +
            		"' from parameter: " + names[0]);
        }

		return spcfDecimal;

    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(PreparedStatement, Object, int, SharedSessionContractImplementor)
     */
    @Override
    public final void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException
    {
        Object localValue = value;

        if (value instanceof SpcfDecimalImpl)
        {
        	//First get the DecimalImpl class
            SpcfDecimalImpl spcfDecimalImpl = (SpcfDecimalImpl)value;

            //SpcfMoney doesn't support NAN, +-Infinity and Scale over 2.
            //Scale is always fixed for SpcfMoney so need to check that
            if(spcfDecimalImpl.isNumber())
            {
            	java.math.BigDecimal bigDecimal = spcfDecimalImpl.toSpecific();
            	localValue = bigDecimal;
            }
            else
            {
            	throw new HibernateException("The value being set is not a valid SpcfDecimal range");
            }
        }
        // If value is null or BigDecimal then simply save the value
        if (sHibernateLogger.isDebugEnabled())
        {
            sHibernateLogger.debug(this.getClass().getSimpleName() + " - binding '"
                    + value + "' to parameter: " + index);
        }

        st.setBigDecimal(index, (java.math.BigDecimal)localValue);

    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, Object)
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
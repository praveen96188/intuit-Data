package com.intuit.sbd.payroll.psp.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;

/**
 * Class that maps an enum value to a String that can be used in Hibernate mappings.
 * @author dgoel
 */
public class SpcfStringEnumUserType implements EnhancedUserType, ParameterizedType, Serializable
{
    private static final long serialVersionUID = 3408940863767639650L;
	/**
	 * SQL Types of columns that can hold this type.
	 */
    private static final int[] SQL_TYPES = {Types.VARCHAR};

    /**
     * Param that can be passed to indicate the full enum class name that contains the actual values.
     */
    protected static final String ENUM_CLASSNAME_PARAM = "enumClassname";

    /**
     * Class for the enum that contains the actual values.
     */
    @SuppressWarnings("unchecked")
    private Class<Enum> mEnumClass = null;

    /**
     * @param parameters The values contained in the HBM file.
     *
     */
    @SuppressWarnings("unchecked")
    public final void setParameterValues(Properties parameters)
    {
        if (parameters != null)
        {
            String enumClassName = parameters.getProperty(ENUM_CLASSNAME_PARAM);
            if (enumClassName != null)
            {
                setEnumClassName(enumClassName);
            }
        }
    }

    /**
     * Allows a sub-class to set the value of enum type class name.
     * @param enumClassName Name of the Enum class.
     */
    @SuppressWarnings("unchecked")
    protected final void setEnumClassName(String enumClassName) {
        try
        {
        	mEnumClass = (Class<Enum>)Class.forName(enumClassName);
        }
        catch (ClassNotFoundException cnfe)
        {
            throw new RuntimeException("Enum class not found", cnfe);
        }
    }


    /**
     * @see org.hibernate.usertype.UserType#returnedClass()
     */
    @SuppressWarnings("unchecked")
    public final Class returnedClass()
    {
        return mEnumClass;
    }

    /**
     * @see org.hibernate.usertype.UserType#sqlTypes()
     */
    public final int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    /**
     * @see org.hibernate.usertype.UserType#isMutable()
     */
    public final boolean isMutable()
    {
        return false;
    }

    /**
     * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
     */
    public final Object deepCopy(Object value)
    {
        return value;
    }

    /**
     * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public final Serializable disassemble(Object value)
    {
        return (Enum) value;
    }

    /**
     * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public final Object replace(Object original, Object target, Object owner)
    {
        return original;
    }

    /**
     * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
     */
    public final Object assemble(Serializable cached, Object owner)
    {
        return cached;
    }

    /**
     * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
     */
    public final boolean equals(Object x, Object y)
    {
        return x == y;
    }

    /**
     * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
     */
    public final int hashCode(Object x)
    {
        return x.hashCode();
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#fromXMLString(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public final Object fromXMLString(String xmlValue)
    {
        return Enum.valueOf(mEnumClass, xmlValue);
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#objectToSQLString(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public final String objectToSQLString(Object value)
    {
        return '\'' + ((Enum)value ).name() + '\'';
    }

    /**
     * @see org.hibernate.usertype.EnhancedUserType#toXMLString(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public final String toXMLString(Object value)
    {
        return ((Enum)value ).name();
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeGet(ResultSet, String[], SharedSessionContractImplementor, Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor sharedSessionContractImplementor, Object owner) throws SQLException
    {
        String name = rs.getString(names[0]);

        return rs.wasNull() ? null : Enum.valueOf(mEnumClass, name);
    }

    /**
     * @see org.hibernate.usertype.UserType#nullSafeSet(PreparedStatement, Object, int, SharedSessionContractImplementor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor sharedSessionContractImplementor) throws SQLException
    {
        if (value==null)
        {
            st.setNull(index, Types.VARCHAR);
        }
        else
        {
            st.setString(index, ((Enum)value).name());
        }
    }
}

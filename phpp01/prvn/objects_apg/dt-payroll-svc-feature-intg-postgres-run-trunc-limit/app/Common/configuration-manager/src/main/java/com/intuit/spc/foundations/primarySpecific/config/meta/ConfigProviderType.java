/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/ConfigProviderType.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.ArrayList;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ConfigProviderType.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
public class ConfigProviderType implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * This is the required serialVersionUID for native serialization versioning.
     */
    private static final long serialVersionUID = 865393969884325212L;

    /**
     * Field _optional
     */
    private boolean _optional = false;

    /**
     * keeps track of state for field: _optional
     */
    private boolean _has_optional;

    /**
     * Field _providerClass
     */
    private java.lang.String _providerClass;

    /**
     * Field _propertyList
     */
    private java.util.ArrayList<com.intuit.spc.foundations.primarySpecific.config.meta.Property> _propertyList;


      //----------------/
     //- Constructors -/
    //----------------/
    /**
     * Constructor
     */
    public ConfigProviderType() {
        super();
        _propertyList = new ArrayList<com.intuit.spc.foundations.primarySpecific.config.meta.Property>();
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProviderType()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addProperty
     * 
     * 
     * 
     * @param vProperty
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void addProperty(com.intuit.spc.foundations.primarySpecific.config.meta.Property vProperty)
        throws java.lang.IndexOutOfBoundsException
    {
        _propertyList.add(vProperty);
    } //-- void addProperty(com.intuit.spc.foundations.primarySpecific.config.meta.Property) 

    /**
     * Method addProperty
     * 
     * 
     * 
     * @param index
     * @param vProperty
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void addProperty(int index, com.intuit.spc.foundations.primarySpecific.config.meta.Property vProperty)
        throws java.lang.IndexOutOfBoundsException
    {
        _propertyList.add(index, vProperty);
    } //-- void addProperty(int, com.intuit.spc.foundations.primarySpecific.config.meta.Property) 

    /**
     * Method clearProperty
     * 
     */
    public void clearProperty()
    {
        _propertyList.clear();
    } //-- void clearProperty() 

    /**
     * Method deleteOptional
     * 
     */
    public void deleteOptional()
    {
        this._has_optional= false;
    } //-- void deleteOptional() 

    /**
     * Method enumerateProperty
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateProperty()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_propertyList.iterator());
    } //-- java.util.Enumeration enumerateProperty() 

    /**
     * Returns the value of field 'optional'.
     * 
     * @return the value of field 'optional'.
     */
    public boolean getOptional()
    {
        return this._optional;
    } //-- boolean getOptional() 

    /**
     * Method getProperty
     * 
     * 
     * 
     * @param index
     * @return Property
     * @throws java.lang.IndexOutOfBoundsException
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.Property getProperty(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _propertyList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return _propertyList.get(index);
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Property getProperty(int) 

    /**
     * Method getProperty
     * 
     * 
     * 
     * @return Property
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.Property[] getProperty()
    {
        int size = _propertyList.size();
        com.intuit.spc.foundations.primarySpecific.config.meta.Property[] mArray = new com.intuit.spc.foundations.primarySpecific.config.meta.Property[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = _propertyList.get(index);
        }
        return mArray;
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Property[] getProperty() 

    /**
     * Method getPropertyCount
     * 
     * 
     * 
     * @return int
     */
    public int getPropertyCount()
    {
        return _propertyList.size();
    } //-- int getPropertyCount() 

    /**
     * Returns the value of field 'providerClass'.
     * 
     * @return the value of field 'providerClass'.
     */
    public java.lang.String getProviderClass()
    {
        return this._providerClass;
    } //-- java.lang.String getProviderClass() 

    /**
     * Method hasOptional
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasOptional()
    {
        return this._has_optional;
    } //-- boolean hasOptional() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    public boolean isValid()
    {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    } //-- boolean isValid() 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param out
     * @throws org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException
     * */    
    public void marshal(java.io.Writer out)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, out);
    } //-- void marshal(java.io.Writer) 

    /**
     * Method marshal
     * 
     * 
     * 
     * @param handler
     * @throws java.io.IOException
     * @throws org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException
     */
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Method removeProperty
     * 
     * 
     * 
     * @param vProperty
     * @return boolean
     */
    public boolean removeProperty(com.intuit.spc.foundations.primarySpecific.config.meta.Property vProperty)
    {
        boolean removed = _propertyList.remove(vProperty);
        return removed;
    } //-- boolean removeProperty(com.intuit.spc.foundations.primarySpecific.config.meta.Property) 

    /**
     * Sets the value of field 'optional'.
     * 
     * @param optional the value of field 'optional'.
     */
    public void setOptional(boolean optional)
    {
        this._optional = optional;
        this._has_optional = true;
    } //-- void setOptional(boolean) 

    /**
     * Method setProperty
     * 
     * 
     * 
     * @param index
     * @param vProperty
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void setProperty(int index, com.intuit.spc.foundations.primarySpecific.config.meta.Property vProperty)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _propertyList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _propertyList.set(index, vProperty);
    } //-- void setProperty(int, com.intuit.spc.foundations.primarySpecific.config.meta.Property) 

    /**
     * Method setProperty
     * 
     * 
     * 
     * @param propertyArray
     */
    public void setProperty(com.intuit.spc.foundations.primarySpecific.config.meta.Property[] propertyArray)
    {
        //-- copy array
        _propertyList.clear();
        for (int i = 0; i < propertyArray.length; i++) {
            _propertyList.add(propertyArray[i]);
        }
    } //-- void setProperty(com.intuit.spc.foundations.primarySpecific.config.meta.Property) 

    /**
     * Sets the value of field 'providerClass'.
     * 
     * @param providerClass the value of field 'providerClass'.
     */
    public void setProviderClass(java.lang.String providerClass)
    {
        this._providerClass = providerClass;
    } //-- void setProviderClass(java.lang.String) 

    /**
     * Method unmarshal
     * 
     * 
     * 
     * @param reader
     * @return Object
     * @throws org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProviderType) Unmarshaller.unmarshal(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProviderType.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * @throws org.exolab.castor.xml.ValidationException
     */
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}

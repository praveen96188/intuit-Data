/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/Constants.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.ArrayList;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Constants.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
public class Constants implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * This is the required serialVersionUID for native serialization versioning.
     */
    private static final long serialVersionUID = -1111038178912019280L;
    /**
     * Field _configProviderList
     */
    private java.util.ArrayList<ConfigProvider> _configProviderList;


      //----------------/
     //- Constructors -/
    //----------------/
    /**
     * Constructor
     */
    public Constants() {
        super();
        _configProviderList = new ArrayList<ConfigProvider>();
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Constants()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addConfigProvider
     * 
     * 
     * 
     * @param vConfigProvider
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void addConfigProvider(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider vConfigProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _configProviderList.add(vConfigProvider);
    } //-- void addConfigProvider(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) 

    /**
     * Method addConfigProvider
     * 
     * 
     * 
     * @param index
     * @param vConfigProvider
     * @throws java.lang.IndexOutOfBoundsException     
     */
    public void addConfigProvider(int index, com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider vConfigProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        _configProviderList.add(index, vConfigProvider);
    } //-- void addConfigProvider(int, com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) 

    /**
     * Method clearConfigProvider
     * 
     */
    public void clearConfigProvider()
    {
        _configProviderList.clear();
    } //-- void clearConfigProvider() 

    /**
     * Method enumerateConfigProvider
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateConfigProvider()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_configProviderList.iterator());
    } //-- java.util.Enumeration enumerateConfigProvider() 

    /**
     * Method getConfigProvider
     * 
     * 
     * 
     * @param index
     * @return ConfigProvider
     * @throws java.lang.IndexOutOfBoundsException
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider getConfigProvider(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _configProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return _configProviderList.get(index);
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider getConfigProvider(int) 

    /**
     * Method getConfigProvider
     * 
     * 
     * 
     * @return ConfigProvider
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider[] getConfigProvider()
    {
        int size = _configProviderList.size();
        com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider[] mArray = new com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = _configProviderList.get(index);
        }
        return mArray;
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider[] getConfigProvider() 

    /**
     * Method getConfigProviderCount
     * 
     * 
     * 
     * @return int
     */
    public int getConfigProviderCount()
    {
        return _configProviderList.size();
    } //-- int getConfigProviderCount() 

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
     */
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
     * Method removeConfigProvider
     * 
     * 
     * 
     * @param vConfigProvider
     * @return boolean
     */
    public boolean removeConfigProvider(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider vConfigProvider)
    {
        boolean removed = _configProviderList.remove(vConfigProvider);
        return removed;
    } //-- boolean removeConfigProvider(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) 

    /**
     * Method setConfigProvider
     * 
     * 
     * 
     * @param index
     * @param vConfigProvider
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void setConfigProvider(int index, com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider vConfigProvider)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _configProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _configProviderList.set(index, vConfigProvider);
    } //-- void setConfigProvider(int, com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) 

    /**
     * Method setConfigProvider
     * 
     * 
     * 
     * @param configProviderArray
     */
    public void setConfigProvider(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider[] configProviderArray)
    {
        //-- copy array
        _configProviderList.clear();
        for (int i = 0; i < configProviderArray.length; i++) {
            _configProviderList.add(configProviderArray[i]);
        }
    } //-- void setConfigProvider(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) 

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
        return (com.intuit.spc.foundations.primarySpecific.config.meta.Constants) Unmarshaller.unmarshal(com.intuit.spc.foundations.primarySpecific.config.meta.Constants.class, reader);
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

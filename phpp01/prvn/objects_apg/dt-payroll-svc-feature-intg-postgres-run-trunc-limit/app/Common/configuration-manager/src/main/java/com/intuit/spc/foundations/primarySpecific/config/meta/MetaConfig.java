/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/MetaConfig.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.util.ArrayList;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class MetaConfig.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
public class MetaConfig implements java.io.Serializable {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/


    /**
     * This is the required serialVersionUID for native serialization versioning.
     */
    private static final long serialVersionUID = 7324130269528296260L;

    /**
     * Field _constants
     */
    private com.intuit.spc.foundations.primarySpecific.config.meta.Constants _constants;

    /**
     * Field _moduleList
     */
    private java.util.ArrayList<com.intuit.spc.foundations.primarySpecific.config.meta.Module> _moduleList;


      //----------------/
     //- Constructors -/
    //----------------/
    /**
     * Constructor
     */
    public MetaConfig() {
        super();
        _moduleList = new ArrayList<com.intuit.spc.foundations.primarySpecific.config.meta.Module>();
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.MetaConfig()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method addModule
     * 
     * 
     * 
     * @param vModule
     * @throws java.lang.IndexOutOfBoundsException
     */    
    public void addModule(com.intuit.spc.foundations.primarySpecific.config.meta.Module vModule)
        throws java.lang.IndexOutOfBoundsException
    {
        _moduleList.add(vModule);
    } //-- void addModule(com.intuit.spc.foundations.primarySpecific.config.meta.Module) 

    /**
     * Method addModule
     * 
     * 
     * 
     * @param index
     * @param vModule
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void addModule(int index, com.intuit.spc.foundations.primarySpecific.config.meta.Module vModule)
        throws java.lang.IndexOutOfBoundsException
    {
        _moduleList.add(index, vModule);
    } //-- void addModule(int, com.intuit.spc.foundations.primarySpecific.config.meta.Module) 

    /**
     * Method clearModule
     * 
     */
    public void clearModule()
    {
        _moduleList.clear();
    } //-- void clearModule() 

    /**
     * Method enumerateModule
     * 
     * 
     * 
     * @return Enumeration
     */
    public java.util.Enumeration enumerateModule()
    {
        return new org.exolab.castor.util.IteratorEnumeration(_moduleList.iterator());
    } //-- java.util.Enumeration enumerateModule() 

    /**
     * Returns the value of field 'constants'.
     * 
     * @return the value of field 'constants'.
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.Constants getConstants()
    {
        return this._constants;
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Constants getConstants() 

    /**
     * Method getModule
     * 
     * 
     * 
     * @param index
     * @return Module
     * @throws java.lang.IndexOutOfBoundsException
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.Module getModule(int index)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _moduleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        
        return _moduleList.get(index);
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Module getModule(int) 

    /**
     * Method getModule
     * 
     * 
     * 
     * @return Module
     */
    public com.intuit.spc.foundations.primarySpecific.config.meta.Module[] getModule()
    {
        int size = _moduleList.size();
        com.intuit.spc.foundations.primarySpecific.config.meta.Module[] mArray = new com.intuit.spc.foundations.primarySpecific.config.meta.Module[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = _moduleList.get(index);
        }
        return mArray;
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Module[] getModule() 

    /**
     * Method getModuleCount
     * 
     * 
     * 
     * @return int
     */
    public int getModuleCount()
    {
        return _moduleList.size();
    } //-- int getModuleCount() 

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
     * Method removeModule
     * 
     * 
     * 
     * @param vModule
     * @return boolean
     */
    public boolean removeModule(com.intuit.spc.foundations.primarySpecific.config.meta.Module vModule)
    {
        boolean removed = _moduleList.remove(vModule);
        return removed;
    } //-- boolean removeModule(com.intuit.spc.foundations.primarySpecific.config.meta.Module) 

    /**
     * Sets the value of field 'constants'.
     * 
     * @param constants the value of field 'constants'.
     */
    public void setConstants(com.intuit.spc.foundations.primarySpecific.config.meta.Constants constants)
    {
        this._constants = constants;
    } //-- void setConstants(com.intuit.spc.foundations.primarySpecific.config.meta.Constants) 

    /**
     * Method setModule
     * 
     * 
     * 
     * @param index
     * @param vModule
     * @throws java.lang.IndexOutOfBoundsException
     */
    public void setModule(int index, com.intuit.spc.foundations.primarySpecific.config.meta.Module vModule)
        throws java.lang.IndexOutOfBoundsException
    {
        //-- check bounds for index
        if ((index < 0) || (index > _moduleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _moduleList.set(index, vModule);
    } //-- void setModule(int, com.intuit.spc.foundations.primarySpecific.config.meta.Module) 

    /**
     * Method setModule
     * 
     * 
     * 
     * @param moduleArray
     */
    public void setModule(com.intuit.spc.foundations.primarySpecific.config.meta.Module[] moduleArray)
    {
        //-- copy array
        _moduleList.clear();
        for (int i = 0; i < moduleArray.length; i++) {
            _moduleList.add(moduleArray[i]);
        }
    } //-- void setModule(com.intuit.spc.foundations.primarySpecific.config.meta.Module) 

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
        return (com.intuit.spc.foundations.primarySpecific.config.meta.MetaConfig) Unmarshaller.unmarshal(com.intuit.spc.foundations.primarySpecific.config.meta.MetaConfig.class, reader);
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

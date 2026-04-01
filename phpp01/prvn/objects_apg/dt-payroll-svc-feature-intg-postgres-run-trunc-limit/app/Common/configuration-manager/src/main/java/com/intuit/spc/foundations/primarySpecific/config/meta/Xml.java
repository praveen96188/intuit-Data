/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/Xml.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Xml.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
//Changed manually by barunachalam
public class Xml extends com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider 
implements java.io.Serializable
{


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/


    /**
     * This is the required serialVersionUID for native serialization versioning.
     */
    private static final long serialVersionUID = -6893561145749222525L;

    /**
     * Field _file
     */
    private java.lang.String _file;

    /**
     * Field _node
     */
    private java.lang.String _node;

    /**
     * Field _useClasspath
     */
    private boolean _useClasspath = false;

    /**
     * keeps track of state for field: _useClasspath
     */
    private boolean _has_useClasspath;

    /**
     * Field _returnCopy
     */
    private boolean _returnCopy = true;

    /**
     * keeps track of state for field: _returnCopy
     */
    private boolean _has_returnCopy;


      //----------------/
     //- Constructors -/
    //----------------/

    /**
     * Constructor
     */
    public Xml() {
        super();
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Xml()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method deleteUseClasspath
     * 
     */
    public void deleteUseClasspath()
    {
        this._has_useClasspath= false;
    } //-- void deleteUseClasspath() 

    /**
     * Method deleteReturnCopy
     * 
     */
    public void deleteReturnCopy()
    {
        this._has_returnCopy= false;
    } //-- void deleteReturnCopy() 

    /**
     * Returns the value of field 'file'.
     * 
     * @return the value of field 'file'.
     */
    public java.lang.String getFile()
    {
        return this._file;
    } //-- java.lang.String getFile() 

    /**
     * Returns the value of field 'node'.
     * 
     * @return the value of field 'node'.
     */
    public java.lang.String getNode()
    {
        return this._node;
    } //-- java.lang.String getNode() 

    /**
     * Returns the value of field 'useClasspath'.
     * 
     * @return the value of field 'useClasspath'.
     */
    public boolean getUseClasspath()
    {
        return this._useClasspath;
    } //-- boolean getUseClasspath() 

    /**
     * Returns the value of field 'returnCopy'.
     * 
     * @return the value of field 'returnCopy'.
     */
    public boolean getReturnCopy()
    {
        return this._returnCopy;
    } //-- boolean getReturnCopy() 

    /**
     * Method hasUseClasspath
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasUseClasspath()
    {
        return this._has_useClasspath;
    } //-- boolean hasUseClasspath() 

    /**
     * Method hasReturnCopy
     * 
     * 
     * 
     * @return boolean
     */
    public boolean hasReturnCopy()
    {
        return this._has_returnCopy;
    } //-- boolean hasReturnCopy() 

    /**
     * Method isValid
     * 
     * 
     * 
     * @return boolean
     */
    @Override
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
    @Override
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
     * 
     */
    @Override
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

    /**
     * Sets the value of field 'file'.
     * 
     * @param file the value of field 'file'.
     */
    public void setFile(java.lang.String file)
    {
        this._file = file;
    } //-- void setFile(java.lang.String) 

    /**
     * Sets the value of field 'node'.
     * 
     * @param node the value of field 'node'.
     */
    public void setNode(java.lang.String node)
    {
        this._node = node;
    } //-- void setNode(java.lang.String) 

    /**
     * Sets the value of field 'useClasspath'.
     * 
     * @param useClasspath the value of field 'useClasspath'.
     */
    public void setUseClasspath(boolean useClasspath)
    {
        this._useClasspath = useClasspath;
        this._has_useClasspath = true;
    } //-- void setUseClasspath(boolean) 

    /**
     * Sets the value of field 'returnCopy'.
     * 
     * @param returnCopy the value of field 'returnCopy'.
     */
    public void setReturnCopy(boolean returnCopy)
    {
        this._returnCopy = returnCopy;
        this._has_returnCopy = true;
    } //-- void setReturnCopy(boolean) 

    /**
     * Method unmarshal
     * 
     * @param reader reader
     * @throws org.exolab.castor.xml.MarshalException
     * @throws org.exolab.castor.xml.ValidationException
     * @return Object
     */
    public static java.lang.Object unmarshal(java.io.Reader reader)
        throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        return (com.intuit.spc.foundations.primarySpecific.config.meta.Xml) Unmarshaller.unmarshal(com.intuit.spc.foundations.primarySpecific.config.meta.Xml.class, reader);
    } //-- java.lang.Object unmarshal(java.io.Reader) 

    /**
     * Method validate
     * @throws org.exolab.castor.xml.ValidationException
     */
    @Override
    public void validate()
        throws org.exolab.castor.xml.ValidationException
    {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    } //-- void validate() 

}

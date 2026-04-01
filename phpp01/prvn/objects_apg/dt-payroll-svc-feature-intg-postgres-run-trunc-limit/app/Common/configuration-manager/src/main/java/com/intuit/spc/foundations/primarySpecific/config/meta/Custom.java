/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/Custom.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class Custom.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
//Changed manually by barunachalam
public class Custom extends ConfigProvider 
implements java.io.Serializable
{


      //----------------/
     //- Constructors -/
    //----------------/

    /**
     * This is the required serialVersionUID for native serialization versioning.
     */
    private static final long serialVersionUID = -2206446849267333213L;

    /**
     * Field _returnCopy
     */
    private boolean _returnCopy = true;

    /**
     * keeps track of state for field: _returnCopy
     */
    private boolean _has_returnCopy;
    
    /**
     * Constructor
     */
    public Custom() {
        super();
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.Custom()

    /**
     * Method deleteReturnCopy
     * 
     */
    public void deleteReturnCopy()
    {
        this._has_returnCopy= false;
    } //-- void deleteReturnCopy() 
    
    /**
     * Returns the value of field 'returnCopy'.
     * @return the value of field 'returnCopy'.
     */
    public boolean getReturnCopy()
    {
        return this._returnCopy;
    } //-- boolean getReturnCopy() 
    
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
     * Sets the value of field 'returnCopy'.
     * 
     * @param returnCopy the value of field 'returnCopy'.
     */
    public void setReturnCopy(boolean returnCopy)
    {
        this._returnCopy = returnCopy;
        this._has_returnCopy = true;
    } //-- void setReturnCopy(boolean) 
      //-----------/
     //- Methods -/
    //-----------/

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
     */
	@Override
    public void marshal(org.xml.sax.ContentHandler handler)
        throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException
    {
        
        Marshaller.marshal(this, handler);
    } //-- void marshal(org.xml.sax.ContentHandler) 

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
        return (com.intuit.spc.foundations.primarySpecific.config.meta.Custom) Unmarshaller.unmarshal(com.intuit.spc.foundations.primarySpecific.config.meta.Custom.class, reader);
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

/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/ConfigProvider.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Class ConfigProvider.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
public abstract class ConfigProvider extends ConfigProviderType 
implements java.io.Serializable
{
    //--------------------------/
   //- Class/Member Variables -/
  //--------------------------/

    /**
     * This is the required serialVersionUID for native serialization versioning.
     */
    private static final long serialVersionUID = 2717509876763494956L;
     
      //----------------/
     //- Constructors -/
    //----------------/
    /**
     * Constructor
     */
    public ConfigProvider() {
        super();
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider()


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
     * @throws java.io.IOException, org.exolab.castor.xml.MarshalException
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
        return (com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) Unmarshaller.unmarshal(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider.class, reader);
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

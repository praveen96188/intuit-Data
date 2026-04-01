/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.6</a>, using an XML
 * Schema.
 * $Id: //psp/dev/Common/ConfigurationManager/src/com/intuit/spc/foundations/primarySpecific/config/meta/ModuleDescriptor.java#1 $
 */

package com.intuit.spc.foundations.primarySpecific.config.meta;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.exolab.castor.xml.validators.StringValidator;

/**
 * Class ModuleDescriptor.
 * 
 * @version $Revision: #1 $ $Date: 2012/04/16 $
 */
public class ModuleDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field nsPrefix
     */
    private java.lang.String nsPrefix;

    /**
     * Field nsURI
     */
    private java.lang.String nsURI;

    /**
     * Field xmlName
     */
    private java.lang.String xmlName;

    /**
     * Field identity
     */
    private org.exolab.castor.xml.XMLFieldDescriptor identity;


      //----------------/
     //- Constructors -/
    //----------------/
    /**
     * Constructor
     */
    public ModuleDescriptor() {
        super();
        xmlName = "module";
        
        //-- set grouping compositor
        setCompositorAsSequence();
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl  desc           = null;
        org.exolab.castor.xml.XMLFieldHandler              handler        = null;
        org.exolab.castor.xml.FieldValidator               fieldValidator = null;
        //-- initialize attribute descriptors
        
        //-- _id
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_id", "id", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            @Override
        	public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Module target = (Module) object;
                return target.getId();
            }
            @Override
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Module target = (Module) object;
                    target.setId( (java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            @Override
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return null;
            }
        } );
        desc.setHandler(handler);
        desc.setRequired(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _id
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(1);
        { //-- local scope
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);
        //-- initialize element descriptors
        
        //-- _configProviderList
        // yzhang: manually modified to support substitutionGroup
        /*************************************************************************************
        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider.class, "_configProviderList", "configProvider", org.exolab.castor.xml.NodeType.Element);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Module target = (Module) object;
                return target.getConfigProvider();
            }
            public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Module target = (Module) object;
                    target.addConfigProvider( (com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
            public java.lang.Object newInstance( java.lang.Object parent ) {
                return new com.intuit.spc.foundations.primarySpecific.config.meta.ConfigProvider();
            }
        } );
        desc.setHandler(handler);
        desc.setMultivalued(true);
        addFieldDescriptor(desc);
        
        //-- validation code for: _configProviderList
        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
                *************************************************************************************/
        //      -- _configProviderList for xml
        addXmlDescriptor();
        
        //      -- _configProviderList for properties        
        addPropertiesDescriptor();
        
        //      -- _configProviderList for custom
        addCustomDescriptor();  
    } //-- com.intuit.spc.foundations.primarySpecific.config.meta.ModuleDescriptor()


      //-----------/
     //- Methods -/
    //-----------/
    /**
     * yzhang: manually added to support substitutionGroup
     */
    private void addXmlDescriptor()
    {
//      -- _configProviderList for xml
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(com.intuit.spc.foundations.primarySpecific.config.meta.Xml.class, "_configProviderList", "configProvider", org.exolab.castor.xml.NodeType.Element);
        org.exolab.castor.xml.XMLFieldHandler handler = (new org.exolab.castor.xml.XMLFieldHandler() {
        	@Override
        	public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Module target = (Module) object;
                return target.getConfigProvider();
            }
        	@Override
        	public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Module target = (Module) object;                    
                    target.addConfigProvider( (com.intuit.spc.foundations.primarySpecific.config.meta.Xml) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
        	@Override
        	public java.lang.Object newInstance( java.lang.Object parent ) {
                return new com.intuit.spc.foundations.primarySpecific.config.meta.Xml();
            }
        } );
        desc.setHandler(handler);
        desc.setMultivalued(true);        
        
        //-- validation code for: _configProviderList
        org.exolab.castor.xml.FieldValidator fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        
        addFieldDescriptor(desc);
    }
    
    /**
     * yzhang: manually added to support substitutionGroup
     */
    private void addPropertiesDescriptor()
    {
//      -- _configProviderList for xml
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(com.intuit.spc.foundations.primarySpecific.config.meta.Properties.class, "_configProviderList", "configProvider", org.exolab.castor.xml.NodeType.Element);
        org.exolab.castor.xml.XMLFieldHandler handler = (new org.exolab.castor.xml.XMLFieldHandler() {
        	@Override
        	public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Module target = (Module) object;
                return target.getConfigProvider();
            }
        	@Override
        	public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Module target = (Module) object;                    
                    target.addConfigProvider( (com.intuit.spc.foundations.primarySpecific.config.meta.Properties) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
        	@Override
        	public java.lang.Object newInstance( java.lang.Object parent ) {
                return new com.intuit.spc.foundations.primarySpecific.config.meta.Properties();
            }
        } );
        desc.setHandler(handler);
        desc.setMultivalued(true);        
        
        //-- validation code for: _configProviderList
        org.exolab.castor.xml.FieldValidator fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        
        addFieldDescriptor(desc);
    }
    
    /**
     * yzhang: manually added to support substitutionGroup
     */
    private void addCustomDescriptor()
    {
//      -- _configProviderList for xml
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(com.intuit.spc.foundations.primarySpecific.config.meta.Custom.class, "_configProviderList", "configProvider", org.exolab.castor.xml.NodeType.Element);
        org.exolab.castor.xml.XMLFieldHandler handler = (new org.exolab.castor.xml.XMLFieldHandler() {
        	@Override
        	public java.lang.Object getValue( java.lang.Object object ) 
                throws IllegalStateException
            {
                Module target = (Module) object;
                return target.getConfigProvider();
            }
        	@Override
        	public void setValue( java.lang.Object object, java.lang.Object value) 
                throws IllegalStateException, IllegalArgumentException
            {
                try {
                    Module target = (Module) object;                    
                    target.addConfigProvider( (com.intuit.spc.foundations.primarySpecific.config.meta.Custom) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }
        	@Override
        	public java.lang.Object newInstance( java.lang.Object parent ) {
                return new com.intuit.spc.foundations.primarySpecific.config.meta.Custom();
            }
        } );
        desc.setHandler(handler);
        desc.setMultivalued(true);        
        
        //-- validation code for: _configProviderList
        org.exolab.castor.xml.FieldValidator fieldValidator = new org.exolab.castor.xml.FieldValidator();
        fieldValidator.setMinOccurs(0);
        { //-- local scope
        }
        desc.setValidator(fieldValidator);
        
        addFieldDescriptor(desc);
    }
    /**
     * Method getAccessMode
     * 
     * 
     * 
     * @return AccessMode
     */
	@Override
    public org.exolab.castor.mapping.AccessMode getAccessMode()
    {
        return null;
    } //-- org.exolab.castor.mapping.AccessMode getAccessMode() 

    /**
     * Method getExtends
     * 
     * 
     * 
     * @return ClassDescriptor
     */
	@Override
	public org.exolab.castor.mapping.ClassDescriptor getExtends()
    {
        return null;
    } //-- org.exolab.castor.mapping.ClassDescriptor getExtends() 

    /**
     * Method getIdentity
     * 
     * 
     * 
     * @return FieldDescriptor
     */
	@Override
	public org.exolab.castor.mapping.FieldDescriptor getIdentity()
    {
        return identity;
    } //-- org.exolab.castor.mapping.FieldDescriptor getIdentity() 

    /**
     * Method getJavaClass
     * 
     * 
     * 
     * @return Class
     */
	@Override
	public java.lang.Class getJavaClass()
    {
        return com.intuit.spc.foundations.primarySpecific.config.meta.Module.class;
    } //-- java.lang.Class getJavaClass() 

    /**
     * Method getNameSpacePrefix
     * 
     * 
     * 
     * @return String
     */
	@Override
	public java.lang.String getNameSpacePrefix()
    {
        return nsPrefix;
    } //-- java.lang.String getNameSpacePrefix() 

    /**
     * Method getNameSpaceURI
     * 
     * 
     * 
     * @return String
     */
	@Override
	public java.lang.String getNameSpaceURI()
    {
        return nsURI;
    } //-- java.lang.String getNameSpaceURI() 

    /**
     * Method getValidator
     * 
     * 
     * 
     * @return TypeValidator
     */
	@Override
	public org.exolab.castor.xml.TypeValidator getValidator()
    {
        return this;
    } //-- org.exolab.castor.xml.TypeValidator getValidator() 

    /**
     * Method getXMLName
     * 
     * 
     * 
     * @return String
     */
	@Override
	public java.lang.String getXMLName()
    {
        return xmlName;
    } //-- java.lang.String getXMLName() 

}

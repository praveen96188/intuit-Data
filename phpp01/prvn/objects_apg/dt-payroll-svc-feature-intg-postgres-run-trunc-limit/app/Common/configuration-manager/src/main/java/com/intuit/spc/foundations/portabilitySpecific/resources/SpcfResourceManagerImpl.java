package com.intuit.spc.foundations.portabilitySpecific.resources;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfNullPointerException;
import com.intuit.spc.foundations.portability.collections.SpcfIterator;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfIteratorImpl;
import com.intuit.spc.foundations.portability.resources.SpcfMissingResourceException;
import com.intuit.spc.foundations.portability.resources.SpcfResourceManager;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import com.intuit.spc.foundations.portability.SpcfParamValidator;


/**
 * Java implementation of the SpcfResourceManager abstract portable class. This class provides an adaptation
 * of Java's ResourceBundle.
 */
public class SpcfResourceManagerImpl extends SpcfResourceManager
{
	private ResourceBundle mResourceBundle = null;
	
	/**
	 * A private default constructor to ensure that only the constructors below are used.
	 */
	private SpcfResourceManagerImpl()
	{
	}
	
	/**
	 * Constructor. Creates a private ResourceBundle based on passed params.
	 * 
	 * @param baseName Represents the default (non-localized) name of the resource file to load.
	 * @param assemblyFullName	Param ignored for Java
	 */
	public SpcfResourceManagerImpl(String baseName, String assemblyFullName)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(baseName, "baseName");
		
		try
		{
			mResourceBundle =  ResourceBundle.getBundle(baseName);	
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(MissingResourceException e)
		{
			throw new SpcfMissingResourceException(e);
		}
	}
	
	/**
	 * Constructor. Creates a private ResourceBundle based on passed params.
	 * 
	 * @param baseName Represents the default (non-localized) name of the resource file to load.
	 * @param locale	Represents the locale for the intended localized resource file.
	 * @param assemblyFullName Param ignored for Java
	 */
	public SpcfResourceManagerImpl(String baseName, SpcfLocaleInfo locale, String assemblyFullName)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(baseName, "baseName");
		SpcfParamValidator.checkIsNotNull(locale, "locale");
		
		try
		{
			// need to accommodate instantiation of Java Locale w/ and w/out country
			if (locale.getCountry() != null)
			{
				mResourceBundle =  ResourceBundle.getBundle(baseName, new Locale(locale.getLanguage(), locale.getCountry()));
			}
			else
			{
				mResourceBundle =  ResourceBundle.getBundle(baseName, new Locale(locale.getLanguage()));			
			}
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(MissingResourceException e)
		{
			throw new SpcfMissingResourceException(e);
		}
	}
	
	/**
	 * Constructor. Creates a private ResourceBundle based on passed native ResourceBundle.
	 * 
	 * @param nativeResourceBundle A Jave ResourceBundle object.
	 */
	public SpcfResourceManagerImpl(ResourceBundle nativeResourceBundle)
	{
		SpcfParamValidator.checkIsNotNull(nativeResourceBundle, "nativeResourceBundle");
		
		mResourceBundle = nativeResourceBundle;
	}
	
	/**
	 * Returns the native ResourceBundle associated with this instance.
	 * 
	 * @return The stored ResourceBundle object
	 */
	public ResourceBundle toSpecific()
	{
		if (mResourceBundle == null)
		{
			throw new SpcfNullPointerException("Private ResourceBundle object not instantiated.");
		}
		
		return mResourceBundle;
	}
	
	/**
	 * Returns an object from the resource repository associated with this instance.
	 * 
	 * @param key	A string representing the key for the intended resource.
	 * @return Object
	 */
	public Object getObject(String key)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(key, "key");
		
		try
		{
			return mResourceBundle.getObject(key);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(MissingResourceException e)
		{
			throw new SpcfMissingResourceException(e);
		}
	}
	
	/**
	 * Returns an object from the resource repository associated with this instance.
	 * 
	 * @param key	A string representing the key for the intended resource.
	 * @return A generic typed object
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTypedObject(String key)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(key, "key");
		
		try
		{
			return (T) mResourceBundle.getObject(key);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(MissingResourceException e)
		{
			throw new SpcfMissingResourceException(e);
		}
	}
	
	/**
	 * Returns a string from the resource file associated with this instance.
	 * 
	 * @param key	A string representing the key for the intended resource.
	 * @return Object
	 */
	public String getString(String key)
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(key, "key");
		
		try
		{
			return mResourceBundle.getString(key);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(MissingResourceException e)
		{
			throw new SpcfMissingResourceException(e);
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}

	}
	
	/**
	 * Returns an instance of SpcfIterator that references the set of keys (String) in the
	 * object.
	 * 
	 * @return An instance of SpcfIterator that references the set of keys (String) in the 
	 * SpcfResourceManager instance
	 */
	public SpcfIterator<String> getKeys()
	{
		// need to get from an Enumeration to an iterator
		Enumeration<String> keys = mResourceBundle.getKeys();
		ArrayList<String> keyList = java.util.Collections.list(keys);
		return new SpcfIteratorImpl<String>(keyList.iterator());
	}
}
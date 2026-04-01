package com.intuit.spc.foundations.portability.resources;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.collections.SpcfIterator;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;

/**
 * An abstract portable class used to encapsulate the functionality of the java ResourceBundle
 * class or the .NET ResourceManager class. 
 */
public abstract class SpcfResourceManager 
{
	/**
	 * Invokes the SpcfFactory to instantiate this class from the specified resource
	 * file.
	 * 
	 * @param baseName Represents the default (non-localized) name of the resource file to load.
	 * @param assemblyName The name of the assembly containing the resource -param ignored for Java
	 * @return SpcfResourceManager
	 * @throws SpcfArgumentNullException if null baseName argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty baseName argument is passed
	 * @throws SpcfMissingResourceException if no resource repository for the specified 
	 * base name can be found
	 */
	public static SpcfResourceManager createInstance(String baseName, String assemblyName)
	{
		return SpcfFactory.getInstance().createResourceManager(baseName, assemblyName);
	}

	/**
	 * Invokes the SpcfFactory to instantiate this class from the specified resource
	 * file.
	 * 
	 * @param baseName	Represents the default (non-localized) name of the resource file to load.
	 * @param locale	Represents the locale for the intended localized resource file.
	 * @param assemblyName The name of the assembly containing the resource -param ignored for Java
	 * @return SpcfResourceManager
	 * @throws SpcfArgumentNullException if null baseName or locale argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty baseName argument is passed
	 * @throws SpcfMissingResourceException if no resource repository for the specified 
	 * base name can be found
	 */
	public static SpcfResourceManager createInstance(String baseName, SpcfLocaleInfo locale, String assemblyName)
	{
		return SpcfFactory.getInstance().createResourceManager(baseName, locale, assemblyName);
	}
	
	/**
	 * Returns an object from the resource file associated with this instance.
	 * 
	 * @param key	A string representing the key for the intended resource.
	 * @return Object
	 * @throws SpcfArgumentNullException if null key argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty key argument is passed
	 * @throws SpcfMissingResourceException if no object for the specified key can be found
	 */
	public abstract Object getObject(String key);
	
	/**
	 * Returns an object from the resource repository associated with this instance.
	 * 
	 * @param key	A string representing the key for the intended resource.
	 * @return A generic typed object
	 * @throws SpcfArgumentNullException if null key argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty key argument is passed
	 * @throws SpcfMissingResourceException if no object for the specified key can be found
	 */
	public abstract <T> T getTypedObject(String key);
	
	/**
	 * Returns a string from the resource repository associated with this instance.
	 * 
	 * @param key	A string representing the key for the intended resource.
	 * @return Object
	 * @throws SpcfArgumentNullException if null key argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty key argument is passed
	 * @throws SpcfMissingResourceException if no object for the specified key can be found
	 */
	public abstract String getString(String key);
	
	/**
	 * Returns an instance of SpcfIterator that references the set of keys (String) in the
	 * object.
	 * 
	 * @return An instance of SpcfIterator that references the set of keys (String) in the 
	 * SpcfResourceManager instance
	 * 
	 */
	public abstract SpcfIterator<String> getKeys();
}
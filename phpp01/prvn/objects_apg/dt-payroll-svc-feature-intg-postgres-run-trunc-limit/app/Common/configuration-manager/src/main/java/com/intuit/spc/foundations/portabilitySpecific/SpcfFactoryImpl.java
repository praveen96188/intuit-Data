/*
 * author		RWG, RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-03-24   Initial Implementation
 * 2004-05-04   Javadoc comments and collections
 */
package com.intuit.spc.foundations.portabilitySpecific;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.portability.SpcfStringUtil;
import com.intuit.spc.foundations.portability.SpcfSystem;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.SpcfVersion;
import com.intuit.spc.foundations.portability.collections.ISpcfComparator;
import com.intuit.spc.foundations.portability.collections.SpcfArrayList;
import com.intuit.spc.foundations.portability.collections.SpcfArraysUtil;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfCollectionsUtil;
import com.intuit.spc.foundations.portability.collections.SpcfHashMap;
import com.intuit.spc.foundations.portability.collections.SpcfHashSet;
import com.intuit.spc.foundations.portability.collections.SpcfLinkedHashMap;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.portability.collections.SpcfQueue;
import com.intuit.spc.foundations.portability.collections.SpcfSet;
import com.intuit.spc.foundations.portability.collections.SpcfStack;
import com.intuit.spc.foundations.portability.collections.SpcfTreeMap;
import com.intuit.spc.foundations.portability.collections.SpcfTreeSet;
import com.intuit.spc.foundations.portability.io.SpcfBitConverter;
import com.intuit.spc.foundations.portability.io.SpcfByteOrderEnum;
import com.intuit.spc.foundations.portability.io.SpcfDirectory;
import com.intuit.spc.foundations.portability.io.SpcfFile;
import com.intuit.spc.foundations.portability.io.SpcfReader;
import com.intuit.spc.foundations.portability.io.SpcfStream;
import com.intuit.spc.foundations.portability.io.SpcfWriter;
import com.intuit.spc.foundations.portability.io.zip.SpcfCRC32;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipEntry;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipFile;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipReader;
import com.intuit.spc.foundations.portability.io.zip.SpcfZipWriter;
import com.intuit.spc.foundations.portability.net.SpcfUrl;
import com.intuit.spc.foundations.portability.net.SpcfUrlDecoder;
import com.intuit.spc.foundations.portability.net.SpcfUrlEncoder;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.reflect.SpcfPortabilityResolver;
import com.intuit.spc.foundations.portability.resources.SpcfResourceManager;
import com.intuit.spc.foundations.portability.security.SpcfCurrentPrincipalStrategy;
import com.intuit.spc.foundations.portability.security.SpcfPasswordDerivedKey;
import com.intuit.spc.foundations.portability.security.SpcfPrincipal;
import com.intuit.spc.foundations.portability.security.SpcfRealm;
import com.intuit.spc.foundations.portability.security.SpcfRole;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import com.intuit.spc.foundations.portability.text.SpcfNumberFormat;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfPattern;
import com.intuit.spc.foundations.portability.threading.ISpcfRunnable;
import com.intuit.spc.foundations.portability.threading.SpcfThread;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfItem;
import com.intuit.spc.foundations.portability.util.SpcfItemizableDecimal;
import com.intuit.spc.foundations.portability.util.SpcfMath;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfArrayListImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfArraysUtilImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfCollectionsUtilImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfEmptyListImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfEmptyMapImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfEmptySetImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfHashMapImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfHashSetImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfLinkedHashMapImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfQueueImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfStackImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfTreeMapImpl;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfTreeSetImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfBigEndianBitConverter;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfDirectoryImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfFileImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfLittleEndianBitConverter;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfReaderImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.SpcfWriterImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.zip.SpcfCRC32Impl;
import com.intuit.spc.foundations.portabilitySpecific.io.zip.SpcfZipEntryImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.zip.SpcfZipFileImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.zip.SpcfZipReaderImpl;
import com.intuit.spc.foundations.portabilitySpecific.io.zip.SpcfZipWriterImpl;
import com.intuit.spc.foundations.portabilitySpecific.net.SpcfUrlDecoderImpl;
import com.intuit.spc.foundations.portabilitySpecific.net.SpcfUrlEncoderImpl;
import com.intuit.spc.foundations.portabilitySpecific.net.SpcfUrlImpl;
import com.intuit.spc.foundations.portabilitySpecific.reflect.SpcfClassImpl;
import com.intuit.spc.foundations.portabilitySpecific.reflect.SpcfPortabilityResolverImpl;
import com.intuit.spc.foundations.portabilitySpecific.resources.SpcfResourceManagerImpl;
import com.intuit.spc.foundations.portabilitySpecific.security.SpcfCurrentPrincipalStrategyImpl;
import com.intuit.spc.foundations.portabilitySpecific.security.SpcfPasswordDerivedKeyImpl;
import com.intuit.spc.foundations.portabilitySpecific.security.SpcfPrincipalImpl;
import com.intuit.spc.foundations.portabilitySpecific.security.SpcfRoleImpl;
import com.intuit.spc.foundations.portabilitySpecific.text.SpcfDateFormatImpl;
import com.intuit.spc.foundations.portabilitySpecific.text.SpcfNumberFormatImpl;
import com.intuit.spc.foundations.portabilitySpecific.text.regularExpressions.SpcfPatternImpl;
import com.intuit.spc.foundations.portabilitySpecific.threading.SpcfThreadImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfMathImpl;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfTimeZoneImpl;


/**
 * A singleton portable factory implementation
 *
 */

public class SpcfFactoryImpl extends SpcfFactory
{
	/**
     * Initializes the platform specific portable factory singleton. This method
     * must to be called before {@link SpcfFactory#getInstance()} is called.
     *
     */
    public static void initialize()
    {
    	//Create a new portable factory if one has not already been created
    	if (sFactory == null)
    		sFactory = new SpcfFactoryImpl();
    }


    protected SpcfFactoryImpl()
    {
    }

	//com.intuit.spc.foundations.portabilitySpecific.collections

   public SpcfArraysUtil createArraysUtil()
   {
	   return new SpcfArraysUtilImpl();
   }

	public SpcfCollectionsUtil createCollectionsUtil()
	{
		return new SpcfCollectionsUtilImpl();
	}

	public <T> SpcfArrayList<T> createArrayList()
	{
		return new SpcfArrayListImpl<T>();
	}
	
	public <T> SpcfArrayList<T> createArrayList(SpcfClass typeParam)
	{
		return new SpcfArrayListImpl<T>(typeParam);
	}

	public <T> SpcfArrayList<T> createArrayList(int initialCapacity)
	{
		return new SpcfArrayListImpl<T>(initialCapacity);
	}
	
	public <T> SpcfArrayList<T> createArrayList(int initialCapacity, SpcfClass typeParam)
	{
		return new SpcfArrayListImpl<T>(initialCapacity,typeParam);
	}

	public <T> SpcfTreeSet<T> createTreeSet()
	{
		return new SpcfTreeSetImpl<T>();
	}
	
	public <T> SpcfTreeSet<T> createTreeSet(SpcfClass typeParam)
	{
		return new SpcfTreeSetImpl<T>(typeParam);
	}

	public <T> SpcfTreeSet<T> createTreeSet(ISpcfComparator<T> c)
	{
		return new SpcfTreeSetImpl<T>(c);
	}

	public <K,V> SpcfTreeMap<K,V> createTreeMap()
	{
		return new SpcfTreeMapImpl<K,V>();
	}
	
	public <K,V> SpcfTreeMap<K,V> createTreeMap(SpcfClass param1, SpcfClass param2)
	{
		return new SpcfTreeMapImpl<K,V>(param1,param2);
	}


	public <K,V> SpcfTreeMap<K,V> createTreeMap(ISpcfComparator<K> c)
	{
		return new SpcfTreeMapImpl<K,V>(c);
	}

	public <K,V> SpcfHashMap<K,V> createHashMap(int initialCapacity)
	{
		return new SpcfHashMapImpl<K,V>(initialCapacity);
	}

	public <K,V> SpcfHashMap<K,V> createHashMap()
	{
		return new SpcfHashMapImpl<K,V>();
	}
	
	public <K,V> SpcfHashMap<K,V> createHashMap(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		return new SpcfHashMapImpl<K,V>(initialCapacity, param1, param2);
	}

	public <K,V> SpcfHashMap<K,V> createHashMap(SpcfClass param1, SpcfClass param2)
	{
		return new SpcfHashMapImpl<K,V>(param1, param2);
	}

	public <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap(int initialCapacity)
	{
		return new SpcfLinkedHashMapImpl<K,V>(initialCapacity);
	}
	
	public <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		return new SpcfLinkedHashMapImpl<K,V>(initialCapacity, param1, param2);
	}

	public <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap()
	{
		return new SpcfLinkedHashMapImpl<K,V>();
	}

	public <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap(SpcfClass param1, SpcfClass param2)
	{
		return new SpcfLinkedHashMapImpl<K,V>(param1, param2);
	}

	public <T> SpcfQueue<T> createQueue()
	{
		return new SpcfQueueImpl<T>();
	}
	
	public <T> SpcfQueue<T> createQueue(SpcfClass typeParam)
	{
		return new SpcfQueueImpl<T>(typeParam);
	}

	@SuppressWarnings("unchecked")
	public <T> SpcfSet<T> createEmptySet()
	{
		return new SpcfEmptySetImpl<T>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> SpcfSet<T> createEmptySet(SpcfClass typeParam)
	{
		return new SpcfEmptySetImpl<T>(typeParam);
	}

	@SuppressWarnings("unchecked")
	public <T> SpcfList<T> createEmptyList()
	{
		return new SpcfEmptyListImpl<T>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> SpcfList<T> createEmptyList(SpcfClass typeParam)
	{
		return new SpcfEmptyListImpl<T>(typeParam);
	}

	@SuppressWarnings("unchecked")
	public <K,V> SpcfMap<K,V> createEmptyMap()
	{
		return new SpcfEmptyMapImpl<K,V>();
	}
	
	@SuppressWarnings("unchecked")
	public <K,V> SpcfMap<K,V> createEmptyMap(SpcfClass param1, SpcfClass param2)
	{
		return new SpcfEmptyMapImpl<K,V>(param1,param2);
	}

	public <T> SpcfHashSet<T> createHashSet()
	{
		return new SpcfHashSetImpl<T>();
	}
	
	public <T> SpcfHashSet<T> createHashSet(SpcfClass typeParam)
	{
		return new SpcfHashSetImpl<T>(typeParam);
	}

	public <T> SpcfHashSet<T> createHashSet(int initialCapacity)
	{
		return new SpcfHashSetImpl<T>(initialCapacity);
	}
	
	public <T> SpcfHashSet<T> createHashSet(int initialCapacity, SpcfClass typeParam)
	{
		return new SpcfHashSetImpl<T>(initialCapacity, typeParam);
	}

	public <T> SpcfHashSet<T> createHashSet(SpcfCollection<T> c)
	{
		return new SpcfHashSetImpl<T>(c);
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createStack()
	 */
	public <T> SpcfStack<T> createStack()
	{
		return new SpcfStackImpl<T>();
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createStack(SpcfClass)
	 */
	public <T> SpcfStack<T> createStack(SpcfClass typeParam)
	{
		return new SpcfStackImpl<T>(typeParam);
	}


	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createStack(int)
	 */
	public <T> SpcfStack<T> createStack(int initialCapacity)
	{
		return new SpcfStackImpl<T>(initialCapacity);
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createStack(int,SpcfClass)
	 */
	public <T> SpcfStack<T> createStack(int initialCapacity, SpcfClass typeParam)
	{
		return new SpcfStackImpl<T>(initialCapacity,typeParam);
	}

	public SpcfSystem createSystem()
	{
		return new SpcfSystemImpl();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar()
	 */
	public SpcfCalendar createCalendar()
	{
		return new SpcfCalendarImpl();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(SpcfTimeZone)
	 */
	public SpcfCalendar createCalendar(SpcfTimeZone zone)
	{
		return new SpcfCalendarImpl(zone);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int, int, int, int, int)
	 */
	public SpcfCalendar createCalendar(int year, int month, int day,
			int hour, int minute, int second, int millisecond)
	{
		return new SpcfCalendarImpl(year, month, day,
				hour, minute, second, millisecond);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int, int, int, int, int, SpcfTimeZone)
	 */
	public SpcfCalendar createCalendar(int year, int month, int day,
			int hour, int minute, int second, int millisecond, SpcfTimeZone zone)
	{
		return new SpcfCalendarImpl(year, month, day,
				hour, minute, second, millisecond, zone);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int)
	 */
	public SpcfCalendar createCalendar(int year, int month, int day)
	{
		return new SpcfCalendarImpl(year, month, day);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int, SpcfTimeZone)
	 */
	public SpcfCalendar createCalendar(int year, int month, int day, SpcfTimeZone zone)
	{
		return new SpcfCalendarImpl(year, month, day, zone);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(long)
	 */
	public SpcfCalendar createCalendar(long milliseconds)
	{
		return new SpcfCalendarImpl(milliseconds);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(long, SpcfTimeZone)
	 */
	public SpcfCalendar createCalendar(long milliseconds, SpcfTimeZone zone)
	{
		return new SpcfCalendarImpl(milliseconds, zone);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createMath()
	 */
	public SpcfMath createMath()
	{
		return new SpcfMathImpl();
	}

	/**
	 * @see SpcfFactory#createStringUtil
	 */
	public SpcfStringUtil createStringUtil()
	{
		return new SpcfStringUtilImpl();
	}

	/**
	 * @see SpcfFactory#createStringBuilder
	 */
    public SpcfStringBuilder createStringBuilder()
    {
    	return new SpcfStringBuilderImpl();
    }

    /**
     * @see SpcfFactory#createStringBuilder(String)
     */
    public SpcfStringBuilder createStringBuilder(String str)
    {
    	return new SpcfStringBuilderImpl(str);
    }

    /**
     * @see SpcfFactory#createStringBuilder(SpcfStringBuilder)
     */
	public SpcfStringBuilder createStringBuilder(SpcfStringBuilder str)
	{
    	return new SpcfStringBuilderImpl(str);
    }

	/**
     * @see SpcfFactory#createStringBuilder(char[])
     */
	public SpcfStringBuilder createStringBuilder(char[] buffer)
	{
    	return new SpcfStringBuilderImpl(buffer);
    }

	/**
     * @see SpcfFactory#createStringBuilder(int)
     */
	public SpcfStringBuilder createStringBuilder(int capacity)
	{
    	return new SpcfStringBuilderImpl(capacity);
    }

	/**
	 * @see SpcfFactory#createStringBuilder(byte[], int, int)
	 */
	public SpcfStringBuilder createStringBuilder(byte[] buffer,
			int offset, int length)
	{
		SpcfEncoding encoding = null;
		return createStringBuilder(buffer, offset, length, encoding);
	}

	/**
	 * @see SpcfFactory#createStringBuilder(byte[], int, int, SpcfEncoding)
	 */
	public SpcfStringBuilder createStringBuilder(byte[] buffer,
			int offset, int length, SpcfEncoding encoding)
	{
		return new SpcfStringBuilderImpl(buffer, offset, length, encoding);
    }

	//Date/Time Format

	public SpcfDateFormat createDateFormat()
	{
		return new SpcfDateFormatImpl();
	}

	public SpcfDateFormat createDateFormat(SpcfLocaleInfo localeInfo)
	{
		return new SpcfDateFormatImpl(localeInfo);
	}

	/*
	Removed code
	public SpcfDateFormat createDateFormat(String culture)
	{
		return new SpcfDateFormatImpl(culture);
	}

	public SpcfDateFormat createDateFormat(String language, String country)
	{
		return new SpcfDateFormatImpl(language, country);
	}
	*/

	//Number Format

	public SpcfNumberFormat createNumberFormat()
	{
		return new SpcfNumberFormatImpl();
	}

	public SpcfNumberFormat createNumberFormat(SpcfLocaleInfo localeInfo)
	{
		return new SpcfNumberFormatImpl(localeInfo);
	}

	/*
	Removed code
	public SpcfNumberFormat createNumberFormat(String culture)
	{
		return new SpcfNumberFormatImpl(culture);
	}
	*/

	/**
	 * @see SpcfFactory#createDecimal(String)
	 */
	public SpcfDecimal createDecimal(String decimalString)
	{
		return new SpcfDecimalImpl(decimalString);
	}

	/**
	 * @see SpcfFactory#createDecimal(int, long, int, int)
	 */
	public SpcfDecimal createDecimal(int sign, long integerPart, int fractionalPart, int scale)
	{
		return new SpcfDecimalImpl(sign, integerPart, fractionalPart, scale);
	}

	/**
	 * @see SpcfFactory#createDecimal(byte[])
	 */
	public SpcfDecimal createDecimal(byte[] bytes)
	{
		return new SpcfDecimalImpl(bytes);
	}

	/**
	 * @see SpcfFactory#createDecimal(double)
	 */
	public SpcfDecimal createDecimal(double decimalVar)
	{
		return new SpcfDecimalImpl(decimalVar);
	}

	/**
	 * @see SpcfFactory#createDecimal(long)
	 */
	public SpcfDecimal createDecimal(long decimalVar)
	{
		return new SpcfDecimalImpl(decimalVar);
	}

	/**
	 * @see SpcfFactory#createClass()
	 */
	public SpcfClass createClass()
	{
		return new SpcfClassImpl();
	}

	/**
	 * @see SpcfFactory#createVersion(String)
	 */
	public SpcfVersion createVersion(String ver)
	{
		return new SpcfVersionImpl(ver);
	}

	/**
	 * @see SpcfFactory#createPortabilityResolver()
	 */
	public SpcfPortabilityResolver  createPortabilityResolver()
	{
		return new SpcfPortabilityResolverImpl();
	}

	/**
	 * @see SpcfFactory#createClass(Class)
	 */
	
	@Override
	public SpcfClass createClass(Class c)
	{
		SpcfClass sc = new SpcfClassImpl(c);
		if (c != null && c.isArray()) 
		{
			Class elemType = c.getComponentType();
			SpcfClass spcfElemType = SpcfClass.createInstance(elemType);
			sc.setElementType(spcfElemType);
		}
		return sc;
	}

	/**
	 * @see SpcfFactory#createClassFromInstance(Object)
	 */
	public SpcfClass createClassFromInstance(Object classInstance)
	{
		//make sure classInstance is not null
		SpcfParamValidator.checkIsNotNull(classInstance,
				                          "class instance");
		SpcfClassImpl c = new SpcfClassImpl();
		c.setEncapsulatedClassType(classInstance.getClass());
		return c;
	}
	
	/**
	 * @see SpcfFactory#createClassFromInstance(Object, SpcfClass[])
	 */
	@Override
	public SpcfClass createClassFromInstance(Object classInstance,
			SpcfClass[] typeParams)
	{
		//make sure classInstance is not null
		SpcfParamValidator.checkIsNotNull(classInstance,
				                          "class instance");
		Class c = classInstance.getClass();
		return createClass(c, typeParams);
	}

	/**
	 * @see SpcfFactory#createClass(Class, SpcfClass[])
	 */
	
	@Override
	public SpcfClass createClass(Class c, SpcfClass[] typeParams)
	{
		//make sure classInstance is not null
		SpcfParamValidator.checkIsNotNull(c, "c");
		SpcfClassImpl sc = new SpcfClassImpl();		
		sc.setEncapsulatedClassType(c);
		if (c.isArray()) 
		{
			SpcfClass elementType = 
				SpcfClass.createInstance(c.getComponentType());
			elementType.setSpcfTypeParameters(typeParams);
			sc.setElementType(elementType);
			return sc;
		}
		
		sc.setSpcfTypeParameters(typeParams);
		return sc;
		
	}
	
	@Override
	public SpcfPattern createRegexPattern(String regex)
	{
		return new SpcfPatternImpl(regex);
	}

	@Override
	public SpcfPattern createRegexPattern(String regex, int options)
	{
		return new SpcfPatternImpl(regex, options);
	}

	@Override
	public SpcfZipFile createZipFile(SpcfFile file)
	{
		return new SpcfZipFileImpl(file);
	}

	@Override
	public SpcfZipFile createZipFile(String name)
	{
		return new SpcfZipFileImpl(name);
	}

	@Override
	public SpcfZipFile createZipFile(SpcfFile file, int mode)
	{
		return new SpcfZipFileImpl(file, mode);
	}

	@Override
	public SpcfZipReader createZipReader(SpcfStream zipIn)
	{
		return new SpcfZipReaderImpl(zipIn);
	}

	@Override
	public SpcfZipWriter createZipWriter(SpcfStream zipOut)
	{
		return new SpcfZipWriterImpl(zipOut);
	}

	@Override
	public SpcfZipEntry createZipEntry(String zipEntryName)
	{
		return new SpcfZipEntryImpl(zipEntryName);
	}

	@Override
	public SpcfCRC32 createCRC32()
	{
		return new SpcfCRC32Impl();
	}


	@Override
	public SpcfUrl createUrl(String spec)
	{
		return new SpcfUrlImpl(spec);
	}

	@Override
	public SpcfUrlDecoder createUrlDecoder()
	{
		return new SpcfUrlDecoderImpl();
	}

	@Override
	public SpcfUrlEncoder createUrlEncoder()
	{
		return new SpcfUrlEncoderImpl();
	}

	@Override
	public SpcfUrl createUrl(String protocol, String host, String file)
	{
		return new SpcfUrlImpl(protocol, host, file);
	}

	@Override
	public SpcfUrl createUrl(String protocol, String host, int port, String file)
	{
		return new SpcfUrlImpl(protocol, host, port, file);
	}



	@Override
	public SpcfFile createFile(String path)
	{
		return new SpcfFileImpl(path);
	}


	@Override
	public SpcfDirectory createDirectory(String path)
	{
		return new SpcfDirectoryImpl(path);
	}


	@Override
	public SpcfReader createReader(SpcfStream binaryStream, SpcfEncoding encoding)
	{
		return new SpcfReaderImpl(binaryStream, encoding);
	}


	@Override
	public SpcfWriter createWriter(SpcfStream binaryStream, SpcfEncoding encoding)
	{
		return new SpcfWriterImpl(binaryStream, encoding);
	}


	@Override
	public SpcfBitConverter createBitConverter(SpcfByteOrderEnum byteOrder)
	{
		if (byteOrder == SpcfByteOrderEnum.LittleEndian)
		{
			return new SpcfLittleEndianBitConverter();
		}
		else
		{
			return new SpcfBigEndianBitConverter();
		}
	}

	/**
	 * @see SpcfFactory#createThread(ISpcfRunnable)
	 */
	public SpcfThread createThread(ISpcfRunnable runnableObject)
	{
		return new SpcfThreadImpl(runnableObject);
	}

	/**
	 * @see SpcfFactory#createThread()
	 */
	public SpcfThread createThread()
	{
		return new SpcfThreadImpl();
	}


	/**
	 * @see SpcfFactory#createResourceManager(String, String)
	 */
	public SpcfResourceManager createResourceManager(String baseName, String assemblyName)
	{
		return new SpcfResourceManagerImpl(baseName, assemblyName);
	}


	/**
	 * @see SpcfFactory#createResourceManager(String, SpcfLocaleInfo, String)
	 */
	public SpcfResourceManager createResourceManager(String baseName, SpcfLocaleInfo locale, String assemblyName)
	{
		return new SpcfResourceManagerImpl(baseName, locale, assemblyName);
	}

    
    /**
     * @see SpcfFactory#createPasswordDerivedKey()
     */
    public SpcfPasswordDerivedKey createPasswordDerivedKey()
    {
    	return new SpcfPasswordDerivedKeyImpl();
    }

    /**
     * @see SpcfFactory#createUniqueId()
     */
 	public SpcfUniqueId createUniqueId()
 	{
 		return new SpcfUniqueIdImpl(false);
 	}

    /**
     * @see SpcfFactory#createUniqueId(boolean initializeToRandomValue)
     */
 	public SpcfUniqueId createUniqueId(boolean initializeToRandomValue)
 	{
 		return new SpcfUniqueIdImpl(initializeToRandomValue);
 	}

 	 /**
     * @see SpcfFactory#createUniqueId(String s)
     */
    public SpcfUniqueId createUniqueId(String s)
    {
        return new SpcfUniqueIdImpl(s);
    }

    /**
     * @see SpcfFactory#createPrincipal(String, SpcfRealm, String, SpcfList, SpcfList)
     */
    public SpcfPrincipal createPrincipal(String id, SpcfRealm realm, String name, SpcfList<SpcfRole> roles, SpcfList<SpcfRealm> effectiveRealms)
    {
        return new SpcfPrincipalImpl(id, realm, name, roles, effectiveRealms);
    }

	/**
     * Create a new SpcfRole instance with specified name.
     * @param name the name of role
     * @return new instance of SpcfRole
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	@Override
	public SpcfRole createRole(String name)
	{
		return new SpcfRoleImpl(name);
	}
	
	/**
     * Create a new SpcfRole instance with specified name and role type.
     * @param name the name of role
     * @param roleType the type of role
     * @return new instance of SpcfRole
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	@Override
	public SpcfRole createRole(String name, String roleType)
	{
		return new SpcfRoleImpl(name, roleType);
	}
		
    /**
     * @see SpcfFactory#createRealm(String)
     */
	@Override
	public SpcfRealm createRealm(String realmId)
	{
		return new SpcfRealm(realmId);
	}
	
    /*
     * @see SpcfFactory#createMessageDigest(SpcfHashAlgorithmEnum)
     *
    public SpcfMessageDigest createMessageDigest(SpcfHashAlgorithmEnum algo)
    {
        return new SpcfMessageDigestImpl(algo);
    }
	*/

    /**
     * @see SpcfFactory#createCurrentPrincipalStrategy()
     */
	public SpcfCurrentPrincipalStrategy createCurrentPrincipalStrategy()
	{
		return new SpcfCurrentPrincipalStrategyImpl();
	}

	/**
	 * This method creates the SpcfItemizableDecimal not Impl like others.
	 * SpcfItemizableDecimal is a portable concrete class, and doesn't have
	 * impl class.  The create method can exist in portable layer, however
	 * it is intentionally included in specific layer to be consistence
	 * with others types in portability package.
	 * @see SpcfFactory#createItemizableDecimal(SpcfDecimal)
	 */
	public SpcfItemizableDecimal createItemizableDecimal(SpcfDecimal dec) 
	{
		return new SpcfItemizableDecimal(dec);
	}

	/**
	 * This method creates the SpcfItem not Impl like others.
	 * SpcfItem is a portable concrete class, and doesn't have
	 * impl class.  The create method can exist in portable layer, however
	 * it is intentionally included in specific layer to be consistence 
	 * with others types in portability package.
	 * @see SpcfFactory#createItem(String, SpcfDecimal)
	 */
	public SpcfItem createItem(String description, SpcfDecimal amount) 
	{
		return new SpcfItem(description, amount);
	}

	/**
     * @see SpcfFactory#createLocalTimeZone()
     */
	public SpcfTimeZone createLocalTimeZone() 
	{
		return new SpcfTimeZoneImpl();
	}
	
	
}

/*
 * author		RWG, RGS
 * department	SPC Foundations
 * project  	Portability
 * 2004-03-24   Initial Implementation
 * 2004-05-03   Javadoc comments and collections
 */
package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.portability.io.*;
import com.intuit.spc.foundations.portability.io.zip.*;
import com.intuit.spc.foundations.portability.net.*;
import com.intuit.spc.foundations.portability.reflect.*;
import com.intuit.spc.foundations.portability.resources.*;
import com.intuit.spc.foundations.portability.text.*;
import com.intuit.spc.foundations.portability.text.regularExpressions.*;
import com.intuit.spc.foundations.portability.threading.*;
import com.intuit.spc.foundations.portability.util.*;
import com.intuit.spc.foundations.portability.security.*;

/**
 * An abstract factory used by portable code to create portable objects.
 * The abstract methods are overriden by platform specific implementations.
 */
public abstract class SpcfFactory
{
	/**
	 * protected constructor
	 */
	protected SpcfFactory()
	{
		// left intentially blank
	}

	/**
 	* The factory instance
 	*/
	protected static SpcfFactory sFactory = null;

    static
    {
    	SpcfPortabilityUtilNT.initializeFactory();
    }

	/**
	 * Provides a global point of access to the factory instance.
	 * @return the factory instance
	 */
    public static SpcfFactory getInstance()
    {
    	return sFactory;
    }

    //com.intuit.spc.foundations.portability.collections

    /**
      * Constructs a SpcfArraysUtil object.
      * @return SpcfArraysUtil implementation object.
      */
    public abstract SpcfArraysUtil createArraysUtil();

    /**
     * Constructs a SpcfCollectionsUtil object.
     * @return SpcfCollectionsUtil implementation object.
     */
    public abstract SpcfCollectionsUtil createCollectionsUtil();

	/**
	 * Returns an empty portable list with an initial capacity of ten.
	 * @return an SpcfArrayList&lt;T&gt; object
	 */
	public abstract <T> SpcfArrayList<T> createArrayList();
	
	/**
	 * Returns an empty portable list with an initial capacity of ten.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfArrayList&lt;T&gt; object
	 */
	public abstract <T> SpcfArrayList<T> createArrayList(SpcfClass typeParam);

	/**
	 * Returns a portable list.
	 * @param initialCapacity the initial capacity of the list.
	 * @return an SpcfArrayList&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <T> SpcfArrayList<T> createArrayList(int initialCapacity);
	
	/**
	 * Returns a portable list.
	 * @param initialCapacity the initial capacity of the list.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfArrayList&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <T> SpcfArrayList<T> createArrayList(int initialCapacity, SpcfClass typeParam);

	/**
	 * Returns an empty portable stack with an initial capacity of ten.
	 * @return an SpcfStack&lt;T&gt; object
	 */
	public abstract <T> SpcfStack<T> createStack();
	
	/**
	 * Returns an empty portable stack with an initial capacity of ten.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfStack&lt;T&gt; object
	 */
	public abstract <T> SpcfStack<T> createStack(SpcfClass typeParam);

	/**
	 * Returns a portable stack.
	 * @param initialCapacity the initial capacity of the stack.
	 * @return an SpcfStack&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <T> SpcfStack<T> createStack(int initialCapacity);
	
	/**
	 * Returns a portable stack.
	 * @param initialCapacity the initial capacity of the stack.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfStack&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <T> SpcfStack<T> createStack(int initialCapacity, SpcfClass typeParam);

	/**
	 * Returns an empty portable tree set.  The default comparator will be used.
	 * @return an SpcfTreeSet&lt;T&gt; object
	 */
	public abstract <T> SpcfTreeSet<T> createTreeSet();
	
	/**
	 * Returns an empty portable tree set.  The default comparator will be used.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfTreeSet&lt;T&gt; object
	 */
	public abstract <T> SpcfTreeSet<T> createTreeSet(SpcfClass typeParam);

	/**
	 * Returns an empty portable tree set with the specified comparator.
	 * @param c an instance of an ISpcfComparator (A null value indicates that the keys' natural ordering should be used.)
	 * @return an SpcfTreeSet&lt;T&gt; object
	 */
	public abstract <T> SpcfTreeSet<T> createTreeSet(ISpcfComparator<T> c);

	/**
	 * Returns an empty portable tree map.  The default comparator will be used.
	 * @return an empty SpcfTreeMap&lt;T&gt; object
	 */
	public abstract <K,V> SpcfTreeMap<K,V> createTreeMap();
	
	/**
	 * Returns an empty portable tree map.  The default comparator will be used.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an empty SpcfTreeMap&lt;T&gt; object
	 */
	public abstract <K,V> SpcfTreeMap<K,V> createTreeMap(SpcfClass param1, SpcfClass param2);

	/**
	 * Returns an empty portable tree map which will use the specified comparator.
	 * @param c an instance of an ISpcfComparator&lt;K&gt; (A null value indicates that the keys' natural ordering should be used.)
	 * @return an empty SpcfTreeMap&lt;K,V&gt; object
	 */
	public abstract <K,V> SpcfTreeMap<K,V> createTreeMap(ISpcfComparator<K> c);

	/**
	 * Constructs an empty hashMap with the default initial capacity (10)
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 */
	public abstract <K,V> SpcfHashMap<K,V> createHashMap(SpcfClass param1, SpcfClass param2);
	
	/**
	 * Constructs an empty hashMap with the default initial capacity (10)
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 */
	public abstract <K,V> SpcfHashMap<K,V> createHashMap();

	/**
	 * Returns an empty portable hashMap with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <K,V> SpcfHashMap<K,V> createHashMap(int initialCapacity);
	
	/**
	 * Returns an empty portable hashMap with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <K,V> SpcfHashMap<K,V> createHashMap(int initialCapacity, SpcfClass param1, SpcfClass param2);

	/**
	 * Constructs an empty linked hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 */
	public abstract <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap();
	
	/**
	 * Constructs an empty linked hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 */
	public abstract <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap(SpcfClass param1, SpcfClass param2);

	/**
	 * Returns an empty linked hashmap with the specified initial capacity
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap(int initialCapacity);
	
	/**
	 * Returns an empty linked hashmap with the specified initial capacity
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <K,V> SpcfLinkedHashMap<K,V> createLinkedHashMap(int initialCapacity, SpcfClass param1, SpcfClass param2);

	/**
	 * Constructs an empty SpcfHashSet with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfHashSet&lt;T&gt; object
	 */
	public abstract <T> SpcfHashSet<T> createHashSet();
	
	/**
	 * Constructs an empty SpcfHashSet with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfHashSet&lt;T&gt; object
	 */
	public abstract <T> SpcfHashSet<T> createHashSet(SpcfClass typeParam);

	/**
	 * Returns an empty SpcfHashSet with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfHashSet&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <T> SpcfHashSet<T> createHashSet(int initialCapacity);
	
	/**
	 * Returns an empty SpcfHashSet with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfHashSet&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public abstract <T> SpcfHashSet<T> createHashSet(int initialCapacity, SpcfClass typeParam);

	/**
	 * Constructs an SpcfHashSet with default load factor
	 * (1.0 for .NET and 0.75 for Java) and adds collection items into the set.
	 * @param c collection of T which is added into the set.
	 * @return an SpcfHashSet&lt;T&gt; object
	 * @throws SpcfArgumentNullException if collection is null.
	 */
	public abstract <T> SpcfHashSet<T> createHashSet(SpcfCollection<T> c);

	/**
	 * Creates and returns an empty SpcfQueue
	 * @return An SpcfQueue&lt;T&gt; object
	 */
	public abstract <T> SpcfQueue<T> createQueue();
	
	/**
	 * Creates and returns an empty SpcfQueue
	 * param typeParam the type parameter for this generic type
	 * @return An SpcfQueue&lt;T&gt; object
	 */
	public abstract <T> SpcfQueue<T> createQueue(SpcfClass typeParam);

	/**
	 * Constructs an immutable empty set.
	 * @return New instance of an immutable empty set.
	 */
	public abstract <T> SpcfSet<T> createEmptySet();
	
	/**
	 * Constructs an immutable empty set.
	 * @param typeParam the type parameter for this generic type
	 * @return New instance of an immutable empty set.
	 */
	public abstract <T> SpcfSet<T> createEmptySet(SpcfClass typeParam);

	/**
	 * Constructs an immutable empty list.
	 * @return New instance of an immutable empty list.
	 */
	public abstract <T> SpcfList<T> createEmptyList();
	
	/**
	 * Constructs an immutable empty list.
	 * @param typeParam the type parameter for this generic type
	 * @return New instance of an immutable empty list.
	 */
	public abstract <T> SpcfList<T> createEmptyList(SpcfClass typeParam);

	/**
	 * Constructs an immutable empty map.
	 * @return New instance of an immutable empty map.
	 */
	public abstract <K,V> SpcfMap<K,V> createEmptyMap();
	
	/**
	 * Constructs an immutable empty map.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return New instance of an immutable empty map.
	 */
	public abstract <K,V> SpcfMap<K,V> createEmptyMap(SpcfClass param1, SpcfClass param2);

	/**
	 * Creates an SpcfSystem object.   This method is used by the portability
	 * library code.  Since all of the methods on SpcfSystem are static, users
	 * of the SpcfSystem class do not need an instance of SpcfSystem and therefore
	 * should not use this method.
	 * @return A new SpcfSystem object.
	 */
	public abstract SpcfSystem createSystem();

	//com.intuit.spc.foundations.portability.util
	/**
	 * Constructs an empty GregorianCalendar object with the current
	 * UTC date/time.
	 *
	 * @return an SpcfCalendar implementation object
	 */
	public abstract SpcfCalendar createCalendar();
	
	/**
	 * Constructs a GregorianCalendar object with the current date and
	 * time with the given time-zone.
	 * @param zone the time-zone to be used.
	 * @return an SpcfCalendar implementation object
	 * @throws SpcfArgumentNullException if zone is null
	 * @throws SpcfIllegalArgumentException if the specified parameter is out of range.
	 */
	public abstract SpcfCalendar createCalendar(SpcfTimeZone zone);

	/**
	 * Constructs a GregorianCalendar object with the specified date and
	 * TimeZone of UTC.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 */
	public abstract SpcfCalendar createCalendar(int year, int month, int day,
			int hour, int minute, int second, int millisecond);
	
	/**
	 * Constructs a GregorianCalendar object with the specified date and
	 * time zone.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 * @param zone the time-zone.
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range.
     * @throws SpcfArgumentNullException if zone is null
	 */
	public abstract SpcfCalendar createCalendar(int year, int month, int day,
			int hour, int minute, int second, int millisecond, SpcfTimeZone zone);

	/**
	 * Constructs a GregorianCalendar object with the specified date and
	 * TimeZone of UTC.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range.
 	 */
	public abstract SpcfCalendar createCalendar(int year, int month, int day);
	
	/**
	 * Constructs a GregorianCalendar object with the specified date and time-zone.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param zone the time zone.
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range.
     * @throws SpcfArgumentNullException if zone is null
 	 */
	public abstract SpcfCalendar createCalendar(int year, int month, int day, SpcfTimeZone zone);

	/**
	 * Constructs a GregorianCalendar object with the specified milliseconds in the
	 * TimeZone of UTC.
	 *
	 * @param milliseconds the number of milliseconds since 1/1/1970, can be a negative number.
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if the specified parameter is out of range.
 	 */
	public abstract SpcfCalendar createCalendar(long milliseconds);
	
	/**
	 * Constructs a GregorianCalendar object with the specified milliseconds in the
	 * given time-zone.
	 * @param milliseconds the number of milliseconds since 1/1/1970, can be a negative number.
	 * @param zone the timezone to be used
	 * @return an SpcfCalendar implementation object
	 * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range.
     * @throws SpcfArgumentNullException if zone is null
	 */
	public abstract SpcfCalendar createCalendar(long milliseconds, SpcfTimeZone zone);

	/**
	 * Constructs a SpcfMath object. This method is used by the portability
	 * library code.  Since all of the methods on SpcfMath are static, users
	 * of the SpcfMath class do not need an instance of SpcfMath and therefore
	 * should not use this method.
	 *
	 * @return an SpcfMath implementation object
	 */
	public abstract SpcfMath createMath();

	/**
     * Constructs a SpcfStringUtil object.
     * @return SpcfStringUtil implementation object.
     */
   public abstract SpcfStringUtil createStringUtil();

	/**
	 * Create an empty SpcfStringBuilder instance with an initial capacity of 16 characters.
	 * @return an empty SpcfStringBuilder instance object
	 */
    public abstract SpcfStringBuilder createStringBuilder();

    /**
     * Create a new SpcfStringBuilder instance by copying the provided string
     * @param str a string to copy into this SpcfStringBuilder instance
	 * @return a SpcfStringBuilder instance object
     * @throws SpcfArgumentNullException if str is null
     */
	public abstract SpcfStringBuilder createStringBuilder(String str);

	/**
	 * Create a new SpcfStringBuilder instance by copying the provided SpcfStringBuilder
     * @param str a SpcfStringBuilder to copy into this SpcfStringBuilder instance
	 * @return a SpcfStringBuilder instance object
     * @throws SpcfArgumentNullException if str is null
     */
	public abstract SpcfStringBuilder createStringBuilder(SpcfStringBuilder str);

	/**
	 * Create a new SpcfStringBuilder instance by copying the provided buffer
	 * @param buffer the array of characters to copy into this instance
	 * @return a SpcfStringBuilder instance object
     * @throws SpcfArgumentNullException if buffer is null
     */
	public abstract SpcfStringBuilder createStringBuilder(char[] buffer);

	/**
	 * Create a new SpcfStringBuilder instance with the given capacity
	 * @param capacity The suggested starting size of this instance.
	 * @return a SpcfStringBuilder instance object
     * @throws SpcfIllegalArgumentException if capacity &lt; 0
     */
	public abstract SpcfStringBuilder createStringBuilder(int capacity);

	/**
	 * Constructs a new SpcfStringBuilder by decoding the specified subarray of
	 * bytes using the default charset.
	 *
	 * The result is undefined if there any bytes that are invalid for the
	 * default charset, or if there are not all the bytes necessary
	 * to produce whole characters. The result in these case may
	 * be different in C# than in Java.
	 *
	 * @param buffer the bytes to be decoded into characters
	 * @param offset the index of the first byte to decode
	 * @param length the number of bytes to decode
	 *
	 * @return a SpcfStringBuilder instance object
	 * @throws SpcfIndexOutOfBoundsException if the offset and length arguments index characters outside the bounds of the bytes array
	 * @throws SpcfArgumentNullException if buffer is null
	 */
	public abstract SpcfStringBuilder createStringBuilder(byte[] buffer,
			int offset, int length);

	/**
	 * Constructs a new SpcfStringBuilder by decoding the specified subarray of
	 * bytes using the specified charset. If null is passed for
	 * the charset, then the default encoding it is used.
	 *
	 * The result is undefined if there any bytes that are invalid for the
	 * given charset, or if there are not all the bytes necessary
	 * to produce whole characters. The result in these case may
	 * be different in C# than in Java.
	 *
	 * @param buffer the bytes to be decoded into characters
	 * @param offset the index of the first byte to decode
	 * @param length the number of bytes to decode
	 * @param encoding the name of a supported charset
	 *
	 * @return a SpcfStringBuilder instance object
	 * @throws SpcfUnsupportedEncodingException if the encoding is not supported
	 * @throws SpcfIndexOutOfBoundsException if the offset and length arguments index characters outside the bounds of the bytes array
	 * @throws SpcfArgumentNullException if buffer is null
	 */
	public abstract SpcfStringBuilder createStringBuilder(byte[] buffer,
			int offset, int length, SpcfEncoding encoding);

	//Date/Time format

	/**
	 * Creates a date/time format object using the default geographical and
	 * cultural conventions.
	 */
	public abstract SpcfDateFormat createDateFormat();

	/**
	 * Creates a date/time format object using the specified geographical and
	 * cultural conventions.
	 * @param localeInfo An instance of SpcfLocaleInfo or null to specify the
	 *  current culture in dotnet or default locale in java
	 */
	public abstract SpcfDateFormat createDateFormat(SpcfLocaleInfo localeInfo);

	//Number format

	/**
	 * Creates a number format object using the default geographical and
	 * cultural conventions.
	 */
	public abstract SpcfNumberFormat createNumberFormat();

	/**
	 * Creates a number format object using the specified geographical and
	 * cultural conventions.
	 * @param localeInfo An instance of SpcfLocaleInfo
	 */
	public abstract SpcfNumberFormat createNumberFormat(SpcfLocaleInfo localeInfo);

	//create SpcfDecimal

	/**
	 * Constructs from a decimal string. The format is optional +/-
	 * sign followed by 1 or more digits optionally separated by decimal point
	 * Alternativelly the string can be one of "Infinity", "-Infinity" or "NAN"
	 *
	 * If the string is an overflow, then the number becomes infinity.
	 * If necessary the number is rounded to max 8 fractional decimals
	 *
	 * @param decimalString decimal string representation
	 *
	 * @throws SpcfArgumentNullException if argument is null
	 * @throws SpcfIllegalArgumentException if argument contains invalid chars
	 */
	 public abstract SpcfDecimal createDecimal(String decimalString);

	/**
	 * Constructs from a byte[] array, as returned
	 * from SpcfDecimal.toBytes().
	 *
	 * @param bytes decimal byte representation
	 * @throws SpcfArgumentNullException if argument is null
	 * @throws SpcfIllegalArgumentException if invalid argument
	 */
	public abstract SpcfDecimal createDecimal(byte[] bytes);

	/**
	 * Constructs from a double
	 *
	 * @param decimalVar double value used to create a decimal
	 */
	public abstract SpcfDecimal createDecimal(double decimalVar);

	/**
	 * Constructs from a long
	 *
	 * @param decimalVar long value used to create a decimal
	 */
	public abstract SpcfDecimal createDecimal(long decimalVar);

	/**
	 * Constructs from an integer and fractional part with the specified scale.
	 *
	 * <p>
	 * The sign gives the sign of the decimal number.
	 *
	 * The fractional part is expressed as a value
	 * multiplied to 10 to the power of the provided scale.
	 * E.g.
	 * if fractionalPart = 1, scale 4, results into .0001;
	 * if fractionalPart = 1256, scale 4, results into .1256.
	 *
	 * <p>
	 * The sign of of integer and fractional part are ignored
	 * (i.e. they are used as absolute values).
	 *
	 * <p>
	 * If the fractional part has more decimal digits than scale, then
	 * the fractional part is rounded to fit the scale
	 * E.g.
	 *   fractionalPart = 12345, scale 4 results into .1235.
	 *
	 * If the resulting number is an overflow, then the 'this' number becomes +/-infinity.
	 * If the integer part represents the NAN integer part,
	 * then the 'this' number is also a NAN.
	 *
	 * @param sign positive, 0, or negative number that determines the sign.
	 * A zero sign can only be used to create a number representing zero or NAN.
	 * @param integerPart value representing the absolute integer decimal
	 * @param fractionalPart value representing the absolute fractional decimal
	 * @param scale the scale of the decimal
	 *
	 * @throws SpcfIllegalArgumentException if scale is out of [0,8] range or,
	 * if sign is 0 and the number is not representing zero or NAN
	 */
	public abstract SpcfDecimal createDecimal(
			int sign, long integerPart, int fractionalPart, int scale);

	/**
	 * Creates a new SpcfClass instance
	 * @param c an instance of Class to encapsulate
	 * @return an SpcfClass instance object
	 */
	public abstract SpcfClass createClass(Class c);

	/**
	 * Creates a new SpcfClass instance with the given type parameters. 
	 * 
	 * This method is for specifying a type's type parameters that are erased at 
	 * java runtime, but needed for portable serialization.
	 * 
	 * @param c an instance of Class to encapsulate.
	 * @param typeParams type parameters.
	 * @return an SpcfClass instance object.
	 * @throws SpcfArgumentNullException if c is null, or typeParams
	 * is null, or any element of typeParams is null.
	 */
	public abstract SpcfClass createClass(Class c, SpcfClass[] typeParams);
	
	/**
	 * Creates a new SpcfClass instance
	 * @return an SpcfClass instance object
	 */
	public abstract SpcfClass createClass();

	/**
	 * Creates a new SpcfVersion instance
	 * @param ver a version string.
	 * @return an SpcfVersion instance.
	 * @throws SpcfIllegalArgumentException if input version string doesn't
	 * start with "x.y." where x/y is the required major/minor number.
	 */
	public abstract SpcfVersion createVersion(String ver);

	/**
	 * Creates a new SpcfPortabilityResolve instance
	 * @return an SpcfPortabilityResolver instance object
	 */
	public abstract SpcfPortabilityResolver createPortabilityResolver();

	/**
	 * Creates a new SpcfClass instance.
	 *
	 * @param classInstance an instance of the type desired.
	 * @return an SpcfClass instance object.
	 * @throws SpcfArgumentNullException if classInstance is null.
	 */
	public abstract SpcfClass createClassFromInstance(Object classInstance);

	/**
	 * Creates a new SpcfClass instance. with the given type parameters. 
	 * 
	 * This method is for specifying a type's type parameters that are erased at 
	 * java runtime, but needed for portable serialization.
	 *
	 * @param classInstance an instance of the type desired.
	 * @param typeParams type parameters.
	 * @return an SpcfClass instance object.
	 * @throws SpcfArgumentNullException if classInstance is null, or typeParams
	 * is null, or any element of typeParams is null.
	 */
	public abstract SpcfClass createClassFromInstance(Object classInstance,
			SpcfClass[] typeParams);

	/**
	 * Compiles the given regular expression into a pattern.
	 * @param regex The expression to be compiled
	 * @return SpcfPattern compiled representation of regular expression
	 * @throws SpcfPatternSyntaxException If the regex's syntax is invalid
	 * @throws SpcfArgumentNullException If regex is null
	 */
	public abstract SpcfPattern createRegexPattern(String regex);

	/**
	 * Compiles the given regular expression into a pattern.
	 * @param regex The expression to be compiled
	 * @param options Match options
	 * @return SpcfPattern compiled representation of regular expression
	 * @throws SpcfPatternSyntaxException If the expression's syntax is invalid
	 * @throws SpcfIllegalArgumentException If bit values other than those corresponding to the defined match flags are set in flags
	 * @throws SpcfArgumentNullException If an argument is null
	 */
	public abstract SpcfPattern createRegexPattern(String regex, int options);

	/**
	 * Creates a new ZIP reader.
	 * @param zipIn binary file reader.
	 * @return zip file reader.
	 * @throws SpcfArgumentNullException if zipIn is null
	 */
	public abstract SpcfZipReader createZipReader(SpcfStream zipIn);

	/**
	 * Creates a new ZIP writer.
	 * @param zipOut binary file writer
	 * @return zip file writer.
	 * @throws SpcfArgumentNullException if zipOut is null
	 */
	public abstract SpcfZipWriter createZipWriter(SpcfStream zipOut);

	/**
	 * Creates a new ZIP entry.
	 * @param zipEntryName The name for this entry. Can include directory components. The convention for names is 'unix' style paths
	 * with no device names and path elements separated by '/' characters.
	 * @return zip entry
	 * @throws SpcfArgumentNullException if zipEntryName is null
	 * @throws SpcfArgumentOutOfRangeException if zipEntryName is an empty string
	 * @throws SpcfIllegalArgumentException if the entry name is longer than 0xFFFF bytes
	 */
	public abstract SpcfZipEntry createZipEntry(String zipEntryName);

	/**
	 * Opens a ZIP file for reading given the specified SpcfFile object.
	 * @param file the ZIP file to be opened for reading
	 * @throws SpcfNullPointerException if file is null
	 * @throws SpcfZipException if a ZIP error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred
	 */
	public abstract SpcfZipFile createZipFile(SpcfFile file);

	/**
	 * Opens a zip file for reading.
	 * @param name the name of the zip file
	 * @throws SpcfNullPointerException if file is null
	 * @throws SpcfZipException if a ZIP error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred
	 * @throws SpcfSecurityException if reading on the file is restricted
	 */
	public abstract SpcfZipFile createZipFile(String name);

	/**
	 * Opens a new ZipFile to read from the specified File object in the specified mode. The mode argument must be
	 * either OPEN_READ or OPEN_READ | OPEN_DELETE.
	 * @param file the ZIP file to be opened for reading
	 * @param mode the mode in which the file is to be opened
	 * @throws SpcfNullPointerException if file is null
	 * @throws SpcfIllegalArgumentException If the mode argument is invalid
	 * @throws SpcfZipException if a ZIP error has occurred
	 * @throws SpcfIOException  if an I/O error has occurred
	 * @throws SpcfSecurityException if reading on the file is restricted
	 */
	public abstract SpcfZipFile createZipFile(SpcfFile file, int mode);

	/**
	 * Creates CRC-32 checksum class object.
	 * @return CRC-32 checksum class object
	 */
	public abstract SpcfCRC32 createCRC32();


	/**
	 * Creates a URL object from the String representation.
	 * @param spec the String to parse as a URL.
	 * @return SpcfURL object
	 * @throws SpcfArgumentNullException If the argument is null
	 * @throws SpcfMalformedUrlException If an unknown protocol is specified
	 */
	public abstract SpcfUrl createUrl(String spec);

	/**
	 * Creates a URL Decoder object.
	 * @return SpcfURLDecoder object.
	 */
	public abstract SpcfUrlDecoder createUrlDecoder();

	/**
	 * Creates a URL Encoder object.
	 * @return SpcfURLEncoder object.
	 */
	public abstract SpcfUrlEncoder createUrlEncoder();

	/**
	 * Creates a URL from the specified protocol name, host name, and file name. The default port for the specified
	 * protocol is used. <p>
	 *
	 * This method is equivalent to calling the four-argument constructor with the arguments being protocol, host, -1, and file.
	 * No validation of the inputs is performed by this method.
	 * @param protocol the name of the protocol to use.
	 * @param host the name of the host.
	 * @param file  the file on the host
	 * @return SpcfURL object
	 * @throws SpcfMalformedUrlException if an unknown protocol is specified.
	 */
	public abstract SpcfUrl createUrl(String protocol, String host, String file);

	/**
	 * Creates a URL object from the specified protocol, host, port number, and file. <p>
	 *
	 * Host can be expressed as a host name or a literal IP address. <p>
	 *
	 * Specifying a port number of -1 indicates that the URL should use the default port for the protocol. <p>
	 *
	 * No validation of the inputs is performed by this method.
	 *
	 * @param protocol the name of the protocol to use.
	 * @param host the name of the host.
	 * @param port the port number on the host.
	 * @param file the file on the host
	 * @return SpcfURL object
	 * @throws SpcfMalformedUrlException if an unknown protocol is specified.
	 */
	public abstract SpcfUrl createUrl(String protocol, String host, int port, String file);


	/**
	 * Creates a new SpcfFile object that points to the file specified by path.
	 * @param path location of the file. Can be null. Does not have to exist.
	 * @return new SpcfFile that represents the file
	 */
	public abstract SpcfFile createFile(String path);

	/**
	 * Creates a new SpcfDirectory object that points to the directory specified by path.
	 * @param path location of the directory. Can be null. Does not have to exist.
	 * @return new SpcfDirectory that represents the directory
	 */
	public abstract SpcfDirectory createDirectory(String path);

	/**
	 * Creates a new SpcfMemoryStream. The buffer parameter can be null, and if so, the factory
	 * should create an SpcfMemoryStream with its own default buffer.
	 * @param buffer initial buffer that the memory stream will wrap or use. Can be null, and if so, a buffer will automatically be created.
	 * @return new SpcfMemoryStream representing a new in-memory binary stream
	 */
	public SpcfMemoryStream createMemoryStream(byte[] buffer)
	{
		if (buffer != null && buffer.length > 0) return new SpcfMemoryStream(buffer);
		return new SpcfMemoryStream();
	}

	/**
	 * Creates a new SpcfReader by wrapping an existing SpcfStream.
	 * @param binaryStream binary stream to be wrapped
	 * @param encoding how the character and strings should be encoded into bytes
	 * @return new SpcfReader that wraps the binary stream
	 * @throws SpcfArgumentNullException if either binaryStream or encoding is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not readable
	 * @throws SpcfSecurityException if a security exception is encountered
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 */
	public abstract SpcfReader createReader(SpcfStream binaryStream, SpcfEncoding encoding);


	/**
	 * Creates a new SpcfWriter by wrapping an existing SpcfStream.
	 * @param binaryStream binary stream to be wrapped
	 * @param encoding how the character and strings should be encoded into bytes
	 * @return new SpcfWriter that wraps the binary stream
	 * @throws SpcfArgumentNullException if either binaryStream or encoding is null
	 * @throws SpcfIllegalArgumentException if binaryStream is not writable
	 * @throws SpcfSecurityException if a security exception is encountered while trying to open the file
	 * @throws SpcfUnsupportedEncodingException if the selected encoding is not supported
	 */
	public abstract SpcfWriter createWriter(SpcfStream binaryStream, SpcfEncoding encoding);


	/**
	 * Creates a new SpcfBitConverter that uses the requested byte ordering.
	 * @param byteOrder the byte ordering to use: big endian or little endian
	 * @return new SpcfBitConverter
	 */
	public abstract SpcfBitConverter createBitConverter(SpcfByteOrderEnum byteOrder);

	/***
	 * Returns a new SpcfThread instance that is not started. The specified ISpcfRunnable
	 * object is bound to this SpcfThread instance. The ISpcfRunnable is called back
	 * once the SpcfThread has been started. The thread is initially set as a foreground
	 * regardless of the current thread's characteristics. You can change this attribute
	 * setting SpcfThread.setBackground(true), prior to calling SpcfThread.start.
	 * @param runnableObject Client implement instance of ISpcfRunnable object. Its "run" is
	 * called back and will execute on a new runtime thread.
	 * @return A new SpcfThread instance bound to the ISpcfRunnable object.
	 * @throws SpcfArgumentNullException if specified runnableObject is null.
	 */
	public abstract SpcfThread createThread(ISpcfRunnable runnableObject);


	/***
	 * Returns a new SpcfThread instance but no runtime thread was created. Use this method
	 * to support two-phase construction. Using this construction method, a call
	 * SpcfThread.create is required before the required call to SpcfThread.start.
	 * @see SpcfFactory#createThread(ISpcfRunnable) for more information.
	 * @return A new SpcfThread instance that must subsequently call SpcfThread.create.
	 */
	public abstract SpcfThread createThread();

	/**
	 * Returns a new instance of SpcfResourceManager when passed the name (baseName) of the resource
	 * file to be loaded. This operation will assume the default (or no) localization.
	 * @param baseName Represents the default (non-localized) name of the resource file to load.
	 * @param assemblyName The name of the assembly containing the resource -param ignored for Java
	 * @return A new instance of SpcfResourceManager
	 * @throws SpcfArgumentNullException if null baseName argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty baseName argument is passed
	 * @throws SpcfMissingResourceException if no resource repository for the specified
	 * base name can be found
	 */
	public abstract SpcfResourceManager createResourceManager(String baseName, String assemblyName);

	/**
	 * Returns a new instance of SpcfResourceManager when passed the name (baseName) of the resource
	 * file to be loaded and the specified localization information (locale). If the specified locale
	 * is not available, a resource set will attempt to be loaded based on fallback rules: 1. a resource
	 * set based on the specified locale (language and country) 2. a resource set based on the language
	 * in the specified locale. 3. The default resource set for the baseName.
	 * @param baseName Represents the default (non-localized) name of the resource file to load.
	 * @param locale Represents the locale for the intended localized resource file.
	 * @param assemblyName The name of the assembly containing the resource -param ignored for Java
	 * @return A new instance of SpcfResourceManager
	 * @throws SpcfArgumentNullException if null baseName or locale argument is passed
	 * @throws SpcfArgumentOutOfRangeException if empty baseName argument is passed
	 * @throws SpcfMissingResourceException if no resource repository for the specified
	 * base name can be found
	 */
	public abstract SpcfResourceManager createResourceManager(String baseName, SpcfLocaleInfo locale, String assemblyName);

     
     /**
      * Creates a new SpcfPasswordDerivedKey object.
      * @return A SpcfPasswordDerivedKey object.
      */
     public abstract SpcfPasswordDerivedKey createPasswordDerivedKey();



     /*
      * Creates an instance of message digest object which can be used to compute digest of any arbitrary length of
      * data.
      * @param algo algo hash algorithm to use to compute message digest
      * @return message digest object
      *
     public abstract SpcfMessageDigest createMessageDigest(SpcfHashAlgorithmEnum algo);
	*/
     
     /**
      * Create a new SpcfUniqueId instance with an empty value
      * @return new instance of the SpcfUniqueId
      */
     public abstract SpcfUniqueId createUniqueId();

     /**
      * Create a new SpcfUniqueId instance
      * @param initializeToRandomValue Initialize with a pseudo-random initial value if true
      * @return new instance of the SpcfUniqueId
      */
     public abstract SpcfUniqueId createUniqueId(boolean initializeToRandomValue);

     /**
      * Create a new SpcfUniqueId instance from the specified string.
      * @param s The string representation of the unique id value.
      * @return new instance of the SpcfUniqueId
      * @throws SpcfArgumentNullException - if the specified string is null
      * @throws SpcfIllegalArgumentException - if the specified string is empty or does
      * not have the expected format
      */
     public abstract SpcfUniqueId createUniqueId(String s);

 	/**
 	 * Creates a new SpcfPrincipal instance with specified values.
	 * @param id the unique id of identity
	 * @param realm the current realm of the user
	 * @param name the name of principal
	 * @param roles the roles associated with principal
	 * @param effectiveRealms the list of effective realms of the user
	 * @return new instance of SpcfPrincipal
	 * @throws SpcfArgumentNullException - if the specified id string is null
 	 */
    public abstract SpcfPrincipal createPrincipal(String id, SpcfRealm realm, String name, SpcfList<SpcfRole> roles, SpcfList<SpcfRealm> effectiveRealms);

	/**
	 * Create a new SpcfCurrentPrincpalStrategy instance
	 * @return new instance of SpcfCurrentPrincpalStrategy
	 */
	public abstract SpcfCurrentPrincipalStrategy createCurrentPrincipalStrategy();

	/**
     * Create a new SpcfRole instance with specified name.
     * @param name the name of role
     * @return new instance of SpcfRole
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	public abstract SpcfRole createRole(String name);
	
	/**
     * Create a new SpcfRole instance with specified name.
     * @param name the name of role
     * @param roleType the type of role
     * @return new instance of SpcfRole
     * @throws SpcfArgumentNullException - if specified name is null
     * @throws SpcfArgumentOutOfRangeException - if specified name is empty
	 */
	public abstract SpcfRole createRole(String name, String roleType);
	
	/**
     * Create a new SpcfRealm instance with specified realm id.
     * @param realmId the Id of realm
     * @return new instance of SpcfRealm
     * @throws SpcfArgumentNullException - if specified realmId is null
     * @throws SpcfArgumentOutOfRangeException - if specified realmId is empty
	 */
	public abstract SpcfRealm createRealm(String realmId);
	
	/**
	 * Create a new SpcfItemizableDeicmal instance with specified SpcfDecimal
	 * @param dec the decimal
	 * @return new instance of SpcfItemizableDecimal
	 * @throws SpcfArgumentNullException - if specified decimal is null
	 */
	public abstract SpcfItemizableDecimal createItemizableDecimal(SpcfDecimal dec);
	
	/**
	 * Create a new SpcfItem instance with specified description and amount.
	 * @param description the description of the item
	 * @param amount the amount of the item
	 * @return the new instance of SpcfItem
	 * @throws SpcfArgumentNullException - if specified descripiton/amount is null
	 * @throws SpcfArgumentOutOfRangeException - if specified description is empty
	 */
	public abstract SpcfItem createItem(String description, SpcfDecimal amount);
	
	/**
	 * Creates an instance of local systme time-zone.	
	 * @return local time-zone object
	 */
	public abstract SpcfTimeZone createLocalTimeZone();
}

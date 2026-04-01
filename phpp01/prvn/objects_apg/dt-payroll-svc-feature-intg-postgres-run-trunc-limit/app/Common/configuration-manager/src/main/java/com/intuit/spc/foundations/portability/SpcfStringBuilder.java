package com.intuit.spc.foundations.portability;
  
import com.intuit.spc.foundations.portability.text.*;

/**
 * Encapsulation class for StringBuilder methods
 */
public abstract class SpcfStringBuilder implements Comparable 
{
    /**
     * Create an empty SpcfStringBuilder instance with an initial capacity of 16 characters.
     * @return an empty SpcfStringBuilder instance object
     */
    public static SpcfStringBuilder createInstance()
    {
    	return SpcfFactory.getInstance().createStringBuilder();
    }

    /**
     * Create a new SpcfStringBuilder instance by copying the provided string
     * @param str a string to copy into this SpcfStringBuilder instance
     * @return a SpcfStringBuilder instance object
     * @throws SpcfArgumentNullException if str is null
     */
    public static SpcfStringBuilder createInstance(String str)
    {
    	return SpcfFactory.getInstance().createStringBuilder(str);
    }

    /**
     * Create a new SpcfStringBuilder instance by copying the provided SpcfStringBuilder
     * @param sb a SpcfStringBuilder to copy into this SpcfStringBuilder instance
     * @return a SpcfStringBuilder instance object
     * @throws SpcfArgumentNullException if sb is null
     */
    public static SpcfStringBuilder createInstance(SpcfStringBuilder sb)
    {
    	return SpcfFactory.getInstance().createStringBuilder(sb);
    }

    /**
     * Create a new SpcfStringBuilder instance by copying the provided buffer
     * @param buffer the array of characters to copy into this instance
     * @return a SpcfStringBuilder instance object
     * @throws SpcfArgumentNullException if buffer is null
     */
    public static SpcfStringBuilder createInstance(char[] buffer)
    {
    	return SpcfFactory.getInstance().createStringBuilder(buffer);
    }

    /**
     * Create a new SpcfStringBuilder instance with the given capacity
     * @param capacity The suggested starting size of this instance.
     * @return a SpcfStringBuilder instance object
     * @throws SpcfIllegalArgumentException if capacity &lt; 0
     */
    public static SpcfStringBuilder createInstance(int capacity)
    {
    	return SpcfFactory.getInstance().createStringBuilder(capacity);
    }

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
    public static SpcfStringBuilder createInstance(byte[] buffer,
            int offset, int length)
    {
    	return SpcfFactory.getInstance().createStringBuilder(buffer, offset, length);
    }

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
    public static SpcfStringBuilder createInstance(byte[] buffer,
            int offset, int length, SpcfEncoding encoding)
    {
    	return SpcfFactory.getInstance().createStringBuilder(buffer, offset, length, encoding);
    }
	
    /**
     * @throws SpcfClassCastException if o is not a SpcfStringBuilder 
     * @throws SpcfArgumentNullException if o is null
	 */
	public abstract int compareTo(Object o);
	
	/** 
	 * Compare lexicographically the this object's content with string s.
	 * @param s string to compare to
	 * @return negative integer, 0, or positive integer if 
	 * this is less, equal or bigger than s, respectively.
	 * @throws SpcfArgumentNullException if s is null
	 */
	public abstract int compareToString(String s);
 
    /**
     * Returns the length of this string.
	 * @return the length of the sequence of 
	 * characters represented by this object.
	 */
    public abstract int getLength();
    
    /**
     * Encodes this String into a sequence of 
     * bytes using the default encoding, 
     * storing the result into a new byte array
     * 
     * @return The resultant byte array
     */
    public abstract byte[] getBytes();

    /**
     * Returns the char found at the specified index
     *
     * @param index integer position
     * @return Char at the specified index
     * @throws SpcfIndexOutOfBoundsException 
     * if the index argument is negative or not 
     * less than the length of this string
     */
    public abstract char charAt(int index);
    
    /**
     * Sets the char at the specified index
     *
     * @param index integer position
     * @param ch character to set at the specified position
     * @return A reference to this object
     * @throws SpcfIndexOutOfBoundsException 
     * if the index argument is negative or not 
     * less than the length of this string
     */
    public abstract SpcfStringBuilder setCharAt(int index, char ch);
    
    /**
     * Converts all of the characters in this StringBuilder 
     * to upper case using the rules of the system default locale
     * 
     * @return this object after being converted to upper case
     */
    public abstract SpcfStringBuilder setToUpperCase();
    
    
    /**
     * Converts all of the characters in this StringBuilder 
     * to upper case using the rules of the given Locale
     * If locale is null, the system default is used
     * 
     * @param locale use the case transformation rules for this locale 
     * @return this object after being converted to upper case
     */
    public abstract SpcfStringBuilder setToUpperCase(SpcfLocaleInfo locale);

    /**
     * Converts all of the characters in this StringBuilder 
     * to lower case using the rules of the system default locale
     * 
     * @return this object after being converted to lower case
     */
    public abstract SpcfStringBuilder setToLowerCase();
    
    /**
     * Converts all of the characters in this StringBuilder 
     * to lower case using the rules of the given Locale
     * If locale is null, the system default is used
     * 
     * @param locale use the case transformation rules for this locale 
     * @return this object after being converted to lower case
     */
    public abstract SpcfStringBuilder setToLowerCase(SpcfLocaleInfo locale);
    
    /**
     * Replaces each substring of this string that matches the 
     * given regular expression with the given replacement.
     *  
     * @param regex the regular expression to which this string is to be matched
     * @param replacement the string that replaces the regular expression
     * @return this object after modification
     * @throws SpcfIllegalArgumentException  
     * if the regular expression's syntax is invalid
     * @throws SpcfArgumentNullException if any argument is null
     */
    public abstract SpcfStringBuilder replaceAll(String regex, String replacement);
    
    /**
     * This method is same as its overloaded couterpart- replaceAll(String, String). <p>
     * 
     * The only difference is that this method takes an extra boolean parameter- escapeMetaCharacters.
     * If this parameter is true, all metacharacters or escape sequences in the input sequence- regex
     * will be given no special meaning and regex will be treated as a literal pattern. If escapeMetaCharacters
     * is false, there will be no difference between this method and overloaded replaceAll(String, String) method.
     */
    public abstract SpcfStringBuilder replaceAll(String regex, String replacement, boolean escapeMetaCharacters);
    
    /**
     * Replaces first matches of the  
     * given regular expression with the given replacement.
     *  
     * @param regex the regular expression to which this string is to be matched
     * @param replacement the string that replaces the regular expression
     * @return this object after modification
     * @throws SpcfIllegalArgumentException 
     * if the regular expression's syntax is invalid
     * @throws SpcfArgumentNullException if any argument is null
     */
    public abstract SpcfStringBuilder replaceFirst(String regex, String replacement);
    
    /**
     * This method is same as its overloaded couterpart- replaceFirst(String, String). <p>
     * 
     * The only difference is that this method takes an extra boolean parameter- escapeMetaCharacters.
     * If this parameter is true, all metacharacters or escape sequences in the input sequence- regex
     * will be given no special meaning and regex will be treated as a literal pattern. If escapeMetaCharacters
     * is false, there will be no difference between this method and overloaded replaceFirst(String, String) method.
     */
    public abstract SpcfStringBuilder replaceFirst(String regex, String replacement, boolean escapeMetaCharacters);
    
    /**
     * Appends the string representation of the argument to this object
     * (i.e "true" and "false") 
     * @param b a boolean
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(boolean b);
    
    /**
     * Appends the string representation of the argument to this object
     * @param c a char
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(char c);
    
    /**
     * Appends the string representation of the argument to this object
     * @param i an integer
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(int i);
    
    /**
     * Appends the string representation of the argument to this object
     * @param l a long
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(long l);
    
    /**
     * Appends the string representation of the argument to this object
     * @param f a float
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(float f);
    
    /**
     * Appends the string representation of the argument to this object
     * @param d a double
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(double d);
    
    /**
     * Appends the string representation of the argument to this object
     * @param o an object
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(Object o);
    
    /**
     * Appends the string representation of the argument to this object
     * @param f a String
     * @return a reference to this object
     */
    public abstract SpcfStringBuilder append(String f);
    
    /**
     * Appends the string representation of the argument to this object
     * @param sb an SpcfStringBuilder
     * @return a reference to this object
    */
    public abstract SpcfStringBuilder append(SpcfStringBuilder sb);
    
    /**
     * Appends the string representation of the argument to this object
     * @param str an array of chars
     * @return a reference to this object
     * @throws SpcfArgumentNullException if null argument
    */
    public abstract SpcfStringBuilder append(char[] str);
    
    /**
     * Appends the string representation of the argument to this object
     * @param str an array of chars
     * @param offset the index of the first character to append.
     * @param len the number of characters to append. 
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException  
     * if offset is negative, or count is negative, 
     * or offset+count is larger than data.length.
     * @throws SpcfArgumentNullException if argument is null
    */
    public abstract SpcfStringBuilder append(char[] str, int offset, int len);
    
    /**
     * Inserts the string representation of the argument to this object
     * (i.e "true" and "false")
     * @param offset index where to insert
     * @param b a boolean
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, boolean b);
    
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param c a char
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, char c);
    
    /**
     * Inserts the string representation of the boolean argument to this object
     * @param offset index where to insert
     * @param i an integer
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, int i);
    
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param l a long
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, long l);
    
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param f a float
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, float f);
    
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param d a double
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, double d);
   
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param o an Object
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, Object o);
   
    
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param f a String
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     */
    public abstract SpcfStringBuilder insert(int offset, String f);
    
    /**
     * Appends the string representation of the argument to this object
     * @param offset index where to insert
     * @param sb an SpcfStringBuilder
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
    */
    public abstract SpcfStringBuilder insert(int offset, SpcfStringBuilder sb);
    
    /**
     * Inserts the string representation of the argument to this object
     * @param offset index where to insert
     * @param str an array of chars
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     * @throws SpcfArgumentNullException if null argument
     */
    public abstract SpcfStringBuilder insert(int offset, char[] str);
    
    /**
     * Inserts the string representation of the argument to this object
     * @param index the index of where to insert.
     * @param str an array of chars
     * @param offset the index of the first character 
     * in subarray to to be inserted.
     * @param len the number of characters in the subarray to to be inserted. 
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException if offset is less than zero or greater than the length
     * @throws SpcfArgumentNullException if null argument
     */
    public abstract SpcfStringBuilder insert(int index, char[] str, int offset, int len);
    
    /**
     * Removes a char at the specified index
     * @param index the index where to remove a char
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException 
     * if start is negative, or eq or greater than length()
     */
	public abstract SpcfStringBuilder deleteCharAt(int index);
    
    /**
     * Removes the characters in a substring of this object. 
     * The substring begins at the specified start and extends to 
     * the character at index end - 1 or to the end of the StringBuffer 
     * if no such character exists. If start is equal to end, 
     * no changes are made.
     * @param start The beginning index, inclusive.
     * @param end The ending index, exclusive
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException  
     * if start is negative, eq or greater than length(), or greater than end.
     */
    public abstract SpcfStringBuilder delete(int start, int end);

    /**
     * Replaces the characters in a substring of this object 
     * with characters in the specified String. 
     * This object will be lengthened to accommodate the 
     * specified String if necessary. 
     * @param start The beginning index, inclusive.
     * @param end The ending index, exclusive
     * @param str String that will replace previous contents.
     * @return a reference to this object
     * @throws SpcfIndexOutOfBoundsException 
     * if start is negative, eq or greater than length(), or greater than end.
     * @throws SpcfArgumentNullException if str is null
     */
    public abstract SpcfStringBuilder replace(int start, int end, String str);
    
    /**
     * Replaces all occurrences of the target String in this instance
     * with the characters in the specified replacement String. 
     * This object will be lengthened to accommodate the 
     * specified String if necessary. This method is similar to the
     * replaceAll() methods in this class except that the target String will
     * be replaced as is, rather than being evaluated as a regular expression.
     * Use this method when a regular expression is not required as it has a
     * much lower performance/memory profile. 
     * @param oldValue String that will be replaced.
     * @param newValue String that will replace previous contents.
     * @return a reference to this object
     * @throws SpcfArgumentNullException if any argument is null
     * @throws SpcfArgumentOutOfRangeException if oldValue is an empty string
     */
    public abstract SpcfStringBuilder replaceAllStrings(String oldValue, String newValue);
    
    /**
     * Replaces the first occurrence of the target String in this instance
     * with the characters in the specified replacement String. 
     * This object will be lengthened to accommodate the 
     * specified String if necessary. This method is similar to the
     * replaceFirst() methods in this class except that the target String will
     * be replaced as is, rather than being evaluated as a regular expression.
     * Use this method when a regular expression is not required as it has a
     * much lower performance/memory profile. 
     * @param oldValue String that will be replaced.
     * @param newValue String that will replace previous contents.
     * @return a reference to this object
     * @throws SpcfArgumentNullException if any argument is null
     * @throws SpcfArgumentOutOfRangeException if oldValue is an empty string
     */
    public abstract SpcfStringBuilder replaceFirstString(String oldValue, String newValue);
    
    /**
     * Retrieve capacity
     * @return current capacity of the internal buffer
     */
    public abstract int getCapacity();
    
    /**
     * Ensures that the capacity is set to the minimum specified
     * @param minimumCapacity the minimum size of the internal buffer
     * @throws SpcfIllegalArgumentException 
     * if minimumCapacity less than zero
     */
    public abstract void ensureCapacity(int minimumCapacity);
    
    /**
     * Truncates the string or pads it with null characters ('\0000') to the 
     * desired length (E.g. the string "Spcf" 
     * when set to a new length of 7 becomes "Spcf\0\0\0").
     * 
     * @param newLength new length
     * @throws SpcfIndexOutOfBoundsException 
     * if the newLength argument is negative.
     */
    public abstract void setLength(int newLength);
    
}

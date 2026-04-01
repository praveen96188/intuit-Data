package com.intuit.spc.foundations.portability.util;


import com.intuit.spc.foundations.portability.SpcfFactory;

import java.io.Serializable;

/**
 * This is an immutable fixed point decimal class
 * with maximum 13 digits for the
 * whole part and 8 digits for the fractional part, which
 * supports a variety of rounding modes
 * as required by financial calculations.
 *
 * This class supports positive or negative infinity and NAN (not a number).
 * A positive or negative infinity number results when the whole part
 * of the number needs more than 13 digits to be represented.
 *
 * This class is immutable, meaning that any arithmetic operation (e.g. add)
 * will return an object representing the result.
 * The 'this' object value cannot be changed by any method call.
 *
 */
abstract public class SpcfDecimal implements Comparable, Serializable
{
	private static final long serialVersionUID = 3161650585897546850L;

	/**
	 * A string that represents the positive infinity ("Infinity").
	 */
	public final static String PositiveInfinityString = "Infinity";

	/**
	 * A string that represents the negative infinity ("-Infinity").
	 */
	public final static String NegativeInfinityString = "-Infinity";

	/**
	 * A string that represents NAN ("NAN").
	 */
	public final static String NanString = "NAN";


	/**
	 * A long representing the whole part of
	 * positive infinity ("0x7ff0000000000000L").
	 */
	public static final long PositiveInfinityIntegerPart = 0x7ff0000000000000L;

	/**
	 * A long representing the whole part of
	 * negative infinity ("0xfff0000000000000L" in java and -4503599627370496 in .Net).
	 */
	public static final long NegativeInfinityIntegerPart = SpcfDecimal.createInstance(0).doGetPlatformSpecificNegativeInfinityIntegerPart();

	/**
	 * A long representing the whole part of
	 * NAN ("0x7ff8000000000000L").
	 */
	public static final long NanIntegerPart = 0x7ff8000000000000L; 

	/**
	 * An int representing maximum integer digits with value 13.
	 */
	public final static int   MaxIntDigits = 13;

	/**
	 * An int representing the maximum scale with value 8.
	 */
	public final static int   MaxScale = 8;

	/**
	 * Rounding Type Enum class
	 *
	 */
	public static class SpcfRoundingType implements Serializable
	{

		private static final long serialVersionUID = 3218171342263505831L;

		/**
		  * Rounding mode to round to nearest neighbor,
		  * where an equidistant value is rounded up.
		  * If the discarded digits represent greater than or equal to half
		  * (0.5 times) the value of a one in the next position then the result
		  * should be rounded up (away from zero). Otherwise the discarded
		  * digits are ignored.
		  */
		public static final SpcfRoundingType HalfUp = new SpcfRoundingType();

		/**
		  * Rounding mode to round to nearest neighbor, where an equidistant
		  * value is rounded to the nearest even neighbor.
		  * If the discarded digits represent greater than half (0.5 times)
		  * the value of a one in the next position then the result should be
		  * rounded up (away from zero). If they represent less than half,
		  * then the result should be rounded down.
		  * <p>Otherwise (they represent exactly half) the result is rounded
		  * down if its rightmost digit is even, or rounded up if its
		  * rightmost digit is odd (to make an even digit).</p>
		  */
		public static final SpcfRoundingType HalfEven = new SpcfRoundingType();

		/**
		  * Rounding mode to round to nearest neighbor, where an equidistant
		  * value is rounded down.
		  * If the discarded digits represent greater than half (0.5 times)
		  * the value of a one in the next position then the result should be
		  * rounded up (away from zero). Otherwise the discarded digits are
		  * ignored.
		  */
		public static final SpcfRoundingType HalfDown = new SpcfRoundingType();

		/**
		  * Rounding mode to round away from zero.
		  * <p>If any of the discarded digits are non-zero then the result will
		  * be rounded up (away from zero).
		  */
		public static final SpcfRoundingType Up = new SpcfRoundingType();

		/** Rounding mode to round towards zero.
		  * All discarded digits are ignored (truncated).
		  */
		public static final SpcfRoundingType Down = new SpcfRoundingType();

		/**
		  * Rounding mode to round to a more negative number.
		  * If any of the discarded digits are non-zero then the result
		  * should be rounded towards the next more negative digit.
		  */
		public static final SpcfRoundingType Floor = new SpcfRoundingType();

		/**
		  * Rounding mode to round to a more positive number.
		  * If any of the discarded digits are non-zero then the result
		  * should be rounded towards the next more positive digit.
		  */
		public static final SpcfRoundingType Ceiling = new SpcfRoundingType();
	}

	/**
	 * Default constructor, used by the derived classes.
	 * Any XML Serializable class requires a public default ctor.
	 */
	public SpcfDecimal(){}

	/**
	 * The implementation of this method returns an object
	 * whose value is numerically equal to this object's adjusted to the
	 * specified scale.
	 * <p>
	 * This method is used to either increase the scale (by padding with zeros)
	 * or to reduced it (using half-even-up rounding).
	 * <p>
	 * The result is positive or negative infinity if after
	 * the half-even-up rounding, it needs more than 13 digits for
	 * representing its whole part.
	 * <p>
	 * Note that since SpcfDecimal objects are immutable,
	 * calls of this method do not result in the original
	 * object being modified, contrary to the usual
	 * convention of having methods named setX mutate field X.
	 * <p>
	 * Instead, setScale returns an object with the proper scale;
	 * the returned object may or may not be newly allocated.
	 *
     * @param scale a number between 0 and 8 that
	 * represents the number of fractional digits
	 * @return a number with the specified scale
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if scale is
	 * out of [0-8] range
	 */
	public abstract SpcfDecimal setScale(int scale);

	/**
	 * The implementation of this method returns an object
	 * whose value is numerically equal to this object's adjusted to the
	 * specified scale.
	 * <p>
	 * This method is used to either increase the scale (by padding with zeros)
	 * or to reduced it (using the specified rounding).
	 * <p>
	 * The result is positive or negative infinity if after
	 * the rounding, it needs more than 13 digits for
	 * representing its whole part.
	 *     <p>
	 * Note that since SpcfDecimal objects are immutable,
	 * calls of this method do not result in the original
	 * object being modified, contrary to the usual
	 * convention of having methods named setX mutate field X.
	 * <p>
	 * Instead, setScale returns an object with the proper scale;
	 * the returned object may or may not be newly allocated.
	 *
     * @param scale a number between 0 and 8 that
	 * represents the number of fractional digits
	 * @param roundingType the rounding type if result
	 * must be rounded to the specified scale
	 *
	 * @return a number with the specified scale
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if scale is
	 * out of [0-8] range
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if roundingType is null
	 */
	public abstract SpcfDecimal setScale(
			int scale, SpcfRoundingType roundingType);

	/**
	 * Returns the sign of the number.
	 *
	 * @return returns 1 if the value of this number is positive or is positive infinity,
	 * returns -1 if the value is negative or is negative infinity,
	 * returns 0 if the value is zero or is NAN.
	 */
	public abstract int getSign();

	/**
	 * The implementation of this method returns an object
	 * whose value is (-this), and whose scale is this.scale().
	 *
	 * @return The negative value of the number.
	 *
	 * If the number is NAN, it returns an object whose value is NAN;
	 * in this case the returned object may or may not be newly allocated.
	 */
	public abstract SpcfDecimal negate();

	/**
	 * Returns the number of fractional digits.
	 * @return The number of digits, including zeros, of the fractional part.
	 * If the number is positive infinity, negative infinity or NAN, the
	 * returned value is -1.
	 */
	public abstract int getScale();

	/**
	 * Returns the value of the fractional part multiplied
	 * by 10 raised to the power of the number of fractional digits.
	 * E.g. for -9.450, the return is 450.
	 * <p>
	 * If the fractional value if made of zeros only, the return is 0.
	 * For example, for 1.00000, the return is 0.
	 *
	 * @return fractional value of the number.
	 * For positive infinity, negative infinity or NAN, the
	 * returned value is -1.
	 */
	public abstract int getFractionalPart();

	/**
	 * Returns the integer part of the number.
	 * E.g. for -9.450, the return is -9.
	 *
	 * The return is PositiveInfinityIntegerPart,
	 * NegativeInfinityIntegerPart or
	 * NaNIntegerPart
	 * if the number is positive infinity, negative infinity or NAN, respectively.
	 *
	 * @return integer part of the number.
	 */
	public abstract long getIntegerPart();


	/**
	 * The implementation of this method returns an object
	 * whose value is the result of (this + val),
	 * and whose scale is max(this.scale(), val.scale()).
	 *
	 * It returns NAN is either operand is NAN or
	 * when adding plus infinity with minus infinity.
	 *
	 * Otherwise, it returns positive or negative infinity if there a
	 * positive or negative overflow, respectively
	 * (i.e. the result needs more than 13 digits for the whole part).
	 * An overflow also occurs in the case when
	 * one of the operands is positive or negative infinity
	 * (e.g. adding -1 with infinity results into infinity).
	 *
	 * @param val value to be added to the this number
	 * @return number representing the result of the addition.
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract SpcfDecimal add(SpcfDecimal val);

	/**
	 * The implementation of this method returns an object
	 * whose value is the result of (this - val),
	 * and whose scale is max(this.scale(), val.scale()).
	 *
	 * It returns NAN is either operand is NAN or when
	 * substracting minus infinity from plus infinity.
	 *
	 * Otherwise, it returns positive or negative infinity if there a
	 * positive or negative overflow, respectively
	 * (i.e. the result needs more than 13 digits for the whole part).
	 * An overflow also occurs in the case when
	 * one of the operands is positive or negative infinity
	 * (e.g. substracting -1 from infinity results into infinity).
	 *
	 * @param val value to be substracted from this number
	 * @return number representing the result of the substraction.
	 *
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract SpcfDecimal subtract(SpcfDecimal val);

	/**
	 * The implementation of this method returns an object
	 * whose value is the result of (this * val),
	 * and whose scale is min(8, this.scale() + val.scale()).
	 * If (this.scale() + val.scale()) > 8, the result is
	 * rounded to 8 fractional digits using round-half-up rounding.
	 *
	 * It returns NAN if either operand is NAN or when
	 * multiplying 0 with positive or negative infinity.
	 * Otherwise, it returns positive or negative infinity if there a
	 * positive or negative overflow, respectively
	 * (i.e. the result needs more than 13 digits for the whole part).
	 * An overflow also occurs in the case when
	 * at least one of the operands is positive or negative infinity
	 * (but the other operand is not zero).
	 *
	 * @param val value to be multiplied to this number
	 * @return number representing the result of the multiplication.
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract SpcfDecimal multiply(SpcfDecimal val);

	/**
	 * The implementation of this method returns an object
	 * whose value is the result of (this * val) with the specified scale.
	 * The resulted number is either rounded
	 * (using the specified rounding type) or zero padded as needed
	 * to obtain the specified scale.
	 *
	 * It returns NAN is either operand is NAN or when
	 * multiplying 0 with positive or negative infinity.
	 * Otherwise, it returns positive or negative infinity if there a
	 * positive or negative overflow, respectively
	 * (i.e. the result needs more than 13 digits for the whole part).
	 * An overflow also occurs in the case when
	 * at least one of the operands is positive or negative infinity
	 * (but the other operand is not zero).
	 *
	 * @param val value to be multiplied to this number
	 * @param scale number of fractional digits for the result
	 * @param roundingType the rounding type if result
	 * must be rounded to the specified scale
	 * @return number representing the result of the multiplication.
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if scale is
	 * out of [0-8] range
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if any argument is null
	 */
	public abstract SpcfDecimal multiply(SpcfDecimal val,
			int scale, SpcfRoundingType roundingType);

	/**
	 * The implementation of this method returns an object
	 * whose value is the result of (this / val),
	 * and whose scale is this.scale().
	 * If needed, the result is rounded using round-half-up rounding.
	 *
	 * It returns NAN is either operand is NAN or when
	 * dividing infinity with infinity or when dividing zero by zero.
	 * Otherwise, it returns positive or negative infinity if there a
	 * positive or negative overflow, respectively
	 * (i.e. the result needs more than 13 digits for the whole part).
	 *
	 * @param val value divide this number
	 * @return the result of the division.
	 *
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract SpcfDecimal divide(SpcfDecimal val);

	/**
	 * The implementation of this method returns an object
	 * whose value is the result of (this /val) with the specified scale.
	 * The resulted number is either rounded
	 * (using the specified rounding type) or zero padded as needed
	 * to obtain the specified scale.
	 *
	 * It returns NAN is either operand is NAN or when
	 * dividing infinity with infinity or when dividing zero by zero.
	 * Otherwise, it returns positive or negative infinity if there a
	 * positive or negative overflow, respectively
	 * (i.e. the result needs more than 13 digits for the whole part).
	 *
	 * @param val the divisor
	 * @param scale the scale
	 * @param roundingType the RoundingType
	 * @return number representing the result of the division.
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if scale is
	 * out of [0-8] range
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if any arguments are null
	 */
	public abstract SpcfDecimal divide(SpcfDecimal val,
			int scale, SpcfRoundingType roundingType);

	/**
	 * The implementation of this method returns an object
	 * whose value is the remainder of division.
	 * By definition, this = N*val + R,
	 * where N is an positive or negative integer,
	 * R is the remainder, and abs(R) is less than abs(val).
	 *
	 * <br> The remainder has the same sign as this, and a scale that
	 * is either:
	 * <br> - max(this.scale(), val.scale), if val is less than the value of this
	 * <br> - this.scale, if val is equal or bigger than the value of this
	 *
	 * <br> E.g.
	 * <br>The remainder of 1 by 5 = 1
	 * <br>The remainder of 101.0 by 3 = 2.0
	 * <br>The remainder of 0.133 by 1 = 0.133
	 * <br>The remainder of -4.01 by 2 = -0.01
	 * <br>The remainder of -4.01 by -2 = -0.01
	 * <br>The remainder of 4.01 by 2 = 0.01
	 * <br>The remainder of 6.25 by 1.25 = 0.00
	 * <br>
	 *
	 * <br>
	 * It returns NAN if:
	 * <br>   -     either operand is NAN
	 * <br>   -     this is positive or negative infinity, or
	 * <br>   -     val is zero.
	 * <br>
	 * Else, if val is positive or negative infinity,
	 * it returns a value equal to the one set in this object;
	 * the returned object may or may not be newly allocated.
	 *
	 * @param val the divisor
	 * @return number representing the remainder after dividing this by val.
	 *
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract SpcfDecimal remainder(SpcfDecimal val);


	/**
	 * Returns the string representation of the number.
	 * The string returned by this method can be used to
	 * create an SpcfDecimal object.
	 *
	 * If this number represents positive infinity it returns PositiveInfinityString.
	 * If this number represents negative infinity it returns NegativeInfinityString.
	 * If this number represents NAN it returns NaNString.
	 *
	 * @return the string representation of the number.
	 */
	public abstract String toString();

	/**
	 * Compares this number with the specified value.
	 * Two numbers that are equal in value but have a different scale
	 * (like 2.0 and 2.00) are considered equal by this method.
	 *
	 * @param val value to be compared with
	 * @return -1, 0 or 1 as this is numerically less than, equal to,
	 * or greater than val, respectively.
	 * NAN is greater than positive infinity.
	 *
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract int compareTo(SpcfDecimal val);

    public abstract boolean isGreaterThan(SpcfDecimal val);

    public abstract boolean isLessThan(SpcfDecimal val);

    public abstract boolean isGreaterThanEqualTo(SpcfDecimal val);

    public abstract boolean isLessThanEqualTo(SpcfDecimal val);

    /**
     * Compare this object with $0.00
     * @return returns true if this is equal to zero
     */
    public abstract boolean isZero();

	/**
	 * Compares this number with the specified value.
	 * If the val is a SpcfDecimal derived class, this method behaves
	 * like {@link #compareTo(SpcfDecimal) compareTo(SpcfDecimal)}.
	 * Otherwise, it throws a SpcfClassCastException.
	 *
	 * @param val value to be compared with
	 * @return negative integer, 0 or positive as this number is numerically less than, equal to,
	 * or greater than val, respectively.
	 *
	 *@throws com.intuit.sbd.payroll.psp.spcf.SpcfClassCastException if val is not a SpcfDecimal derived class
	 *@throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 */
	public abstract int compareTo(Object val);


	/**
	 * Compares this number with the specified val for equality.
	 * Unlike compareTo, this method considers two numbers equal
	 * only if they are equal in value and scale
	 * (thus 2.0 is not equal to 2.00 when compared by this method).
	 *
	 * @param val Object to which this number is to be compared.
	 * @return true, only if the specified Object is a SpcfDecimal
	 * derived class whose value and scale are equal to this number's.
	 */
	public abstract boolean equals(Object val);

	/**
	 * Returns a hash code value for the object
	 * @return hash code value for this object.
	 */
	public abstract int hashCode();

	/**
	 * Returns a compact variable size byte array
	 * (which, for example, can be used to serialize this number to disk).
	 * @return compact variable size representation of the object.
	 */
	public abstract byte[] toBytes();

	/**
	 * Returns a positive number
	 * with the same absolute value as this object.
	 *
	 * If this number is positive or NAN then the
	 * returned object may or may not be newly allocated.
	 *
	 * @return a number that is the absolute value as this object
	 */
	public abstract SpcfDecimal abs();

	/**
	 * Returns true if this number represents a positive infinity.
	 * @return true, only if this number is positive infinity
	 */
	public abstract boolean isPositiveInfinity();

	/**
	 * Returns true if this number represents a negative infinity.
	 * @return true, only if this number is negative infinity
	 */
	public abstract boolean isNegativeInfinity();

	/**
	 * Returns true if this number represents a NAN (not a number).
	 * @return true, only if this number is NAN
	 */
	public abstract boolean isNAN();

	/**
	 * Returns true if this number is neither infinity nor NAN.
	 * @return true, only if this number
	 * is neither positive or negative infinity, nor NAN.
	 */
	public abstract boolean isNumber();

	/**
	 * Public callback interface that can be called when
	 * an operation results into a non-numeric value (a non-numeric value is
	 * negative or positive infinity, or NAN).
	 *
	 */
	public static interface ISpcfSignalError
	{
		/**
		 * Signals a operations that result
		 * into positive or negative infinity, or NAN
		 * when these results happen from these operations:
		 * constructors, setScale, add, subtract,
		 * multiply, divide and remainder.
         *
		 * @param type integer that specifies the type of the error:
		 * -1 for negative infinity, 1 for positive infinity and 0 for NAN,
		 * respectively
		 *
		 */
		void signal(int type);
	}

	/**
	 * The implementation of this method returns an object
	 * whose value is the midpoint of the interval to which this number belongs,
	 * where an interval is defined by consecutive multiples of the intervalSize.
	 *
	 * The scale of the result is same as this.scale().
	 * <br>
	 * In case the intervalSize is negative, its absolute value will be used.
	 * <br>
	 * Thus, for example, if 'this' is 33 and 'intervalSize' is 10,
	 * the returned value is 35.
	 *
	 * <br><br>
	 * A few more examples:
	 * <br>
	 * For an SpcdDecimal equal to "33"
	 * and the internalSize equal to "15", the midpoint is "38"
	 * <br>
     *     33 belongs to the interval [30-45],
     * <br>
     *     the midpoint of which is 37.5.
     * <br>
     *     'this' is 33, so its scale is 0, so round 37.5 to 38.
     * <br><br>
     * But
     * <br>
     * For an SpcdDecimal equal to "33.1"
	 * and the internalSize equal to "15", the midpoint is "37.5"
     * <br>
     *     33.1 belongs to the interval [30-35],
     * <br>
     *     the midpoint of which is 37.5.
     * <br>
     *     Since 'this' is 33.1, the scale in the result is maintained.
     * <br>
     * <br>
     * If 'this' is NAN, the return is NAN.
	 * Else if 'this' is positive or negative infinity
	 * the result is positive or negative infinity, respectively;
	 * the returned object may or may not be newly allocated.
	 *
	 * @param intervalSize interval size
	 * @return mid point of the interval to which this number belongs.
	 */
	abstract public SpcfDecimal midPoint (long intervalSize);

	/**
	 * Not for public use
	 */
	static protected ISpcfSignalError sSignal;

	/**
	 * Sets a callback interface that is called whenever the result is
	 * a positive or negative infinity, or NAN from the following operations:
	 * constructors, setScale, add, subtract,
	 * multiply, divide and remainder.
	 * @return true
	 */
	static public boolean setSignalError(ISpcfSignalError signal)
	{
		sSignal = signal;
		return true;
	}

    /**
     * Return the platform specific impl
     */
    public abstract Object toImpl();

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
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if argument contains invalid chars
	 */
	public static SpcfDecimal createInstance(String decimalString)
	{
		return SpcfFactory.getInstance().createDecimal(decimalString);
	}

	/**
	 * Constructs from a byte[] array, as returned
	 * from SpcfDecimal.toBytes().
	 *
	 * @param bytes decimal byte representation
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if invalid argument
	 */
	public static SpcfDecimal createInstance(byte[] bytes)
	{
		return SpcfFactory.getInstance().createDecimal(bytes);
	}

	/**
	 * Constructs from a double
	 *
	 * @param decimalVar double value used to create a decimal
	 */
	public static SpcfDecimal createInstance(double decimalVar)
	{
	    return SpcfFactory.getInstance().createDecimal(decimalVar);
	}

	/**
	 * Constructs from a long
	 *
	 * @param decimalVar long value used to create a decimal
	 */
	public static SpcfDecimal createInstance(long decimalVar)
	{
	    return SpcfFactory.getInstance().createDecimal(decimalVar);
	}

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
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if scale is out of [0,8] range or,
	 * if sign is 0 and the number is not representing zero or NAN
	 */
	public static SpcfDecimal createInstance(int sign, long integerPart, int fractionalPart, int scale)
    {
		return SpcfFactory.getInstance().createDecimal(sign, integerPart, fractionalPart, scale);
	}

	/**
	 * This is a protected abstract method to allow us to receive back the
	 * platform specific value for the negative infinity integer part.
	 * The java and c# platform have diffent values because the java value
	 * provides an out of range because the java and .net do not agree on using
	 * long and ulong types.
	 * @return 0xfff0000000000000L" in java and -4503599627370496 in .Net
	 */
	protected abstract long doGetPlatformSpecificNegativeInfinityIntegerPart();

	public SpcfDecimal max(SpcfDecimal val) {
		return (compareTo(val) >= 0 ? this : val);
	}

	public SpcfDecimal min(SpcfDecimal val) {
		return (compareTo(val) <= 0 ? this : val);
	}
}

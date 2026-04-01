package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.*;

/**
 * SpcfMath is the math class that provides useful methods
 * to provide constant and static methods for trignometric, logarithmic and
 * other common mathematical functions.
 * The .NET and Java versions calculations in
 * SpcfMath will be consistant with values produced from the Math class on .NET
 * and the StrictMath class in java on the respective platforms.
 * Note:  There can be differences in the results between the .NET
 * and Java versions beyond the 12th significant digit.  For example:
 * SpcfMath.Exp(180.34) returns in .NET - 2.0925067443119146E78
 * and in Java - 2.0925067443119348E78 and SpcfMath.Log(180.34) returns in
 * .NET - 5.19484395807176 and in Java - 5.194843958071761
 */
public strictfp abstract class SpcfMath
{

	/**
     * math instance that is used for static methods
     */
    protected static SpcfMath sMath;

    /**
     * Gets the double value that is closer than any other to e,
     * the base of the natural logarithms. 
     */
    public final static double E;

    /**
     * Instance implementation called by E.
     */
    protected abstract double doGetE();

    /**
     * Gets the double value that is closer than any other to pi,
     * the ratio of the circumference of a circle to its diameter.
     */
    public final static double Pi;

    static
    {
    	sMath = SpcfFactory.getInstance().createMath(); //instance for static methods
    	E = sMath.doGetE();
    	Pi = sMath.doGetPI();
    }
    /**
     * Instance implementation called byPI.
     */
    protected abstract double doGetPI();

    /**
     * Returns the absolute value of a <i>double</i> value.
     *
     * <br>If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is positive zero or negative zero, the result is positive zero.
     * <li>If the argument is infinite, the result is positive infinity.
     * <li>If the argument is NaN, the result is NaN.
     *  </ul>
	 * @param a the argument whose absolute value is to be determined
     */
    public static double abs(double a)
	{
    	return sMath.getAbs(a);
    }

    /**
     * Instance implementation called by abs.
     */
    protected abstract double getAbs(double a);

    /**
     * Returns the absolute value of a <i>float</i> value.
     *
     * <br>If the argument is not negative, the argument is returned.
     * If the argument is negative, the negation of the argument is returned.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is positive zero or negative zero, the result is positive zero.
     * <li>If the argument is infinite, the result is positive infinity.
     * <li>If the argument is NaN, the result is NaN.
     *  </ul>
	 * @param a the argument whose absolute value is to be determined
     */
    public static float abs(float a)
	{
    	return sMath.getAbs(a);
    }

    /**
     * Instance implementation called by abs.
     */
    protected abstract float getAbs(float a);

    /**
     * Returns the absolute value of a <i>int</i> value.
     * <br>If the argument is not negative, the argument is returned. If the argument is negative, the negation of the argument is returned.
     * <br>Note that if the argument is equal to the value of Integer.MIN_VALUE, the most negative representable int value, the result is that same value, which is negative.
	 * @param a the argument whose absolute value is to be determined
     */
    public static int abs(int a)
	{
    	return sMath.getAbs(a);
    }

    /**
     * Instance implementation called by abs.
     */
    protected abstract int getAbs(int a);

    /**
     * Returns the absolute value of a <i>long</i> value.
     * <br>If the argument is not negative, the argument is returned. If the argument is negative, the negation of the argument is returned.
     * <br>Note that if the argument is equal to the value of Long.MIN_VALUE, the most negative representable long value, the result is that same value, which is negative.
	 * @param a the argument whose absolute value is to be determined
     */
    public static long abs(long a)
	{
    	return sMath.getAbs(a);
    }

    /**
     * Instance implementation called by abs.
     */
    protected abstract long getAbs(long a);

    /**
     * Returns the arc cosine of an angle, in the range of 0.0 through pi.
     * <br>Special case:
     * <ul>
     * <li>If the argument is NaN or its absolute value is greater than 1, then the result is NaN.
     * </ul>
     * @param a the value whose arc cosine is to be returned
     */
    public static double acos(double a)
	{
    	return sMath.getAcos(a);
    }

    /**
     * Instance implementation called by acos.
     */
    protected abstract double getAcos(double a);

    /**
     * Returns the arc sine of an angle, in the range of -pi/2 through pi/2.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN or its absolute value is greater than 1, then the result is NaN.
     * <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     * @param a the value whose arc sine is to be returned
     */
    public static double asin(double a)
	{
    	return sMath.getAsin(a);
    }

    /**
     * Instance implementation called by asin.
     */
    protected abstract double getAsin(double a);

    /**
     * Returns the arc tangent of an angle, in the range of -pi/2 through pi/2.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN, then the result is NaN.
     * <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     * @param a the value whose arc tangent is to be returned
     */
    public static double atan(double a)
	{
    	return sMath.getAtan(a);
    }

    /**
     * Instance implementation called by atan.
     */
    protected abstract double getAtan(double a);

    /**
     * Converts rectangular coodinates (x, y) to polar (r, theta).
     * <br>This method computes the phase theta by computing an arc tangent of y/x in the range of -pi to pi.
     * <br>Special cases:
     * <ul>
     * <li>If either argument is NaN, then the result is NaN.
     * <li>If the first argument is positive zero and the second argument is positive, or the first argument is positive and finite and the second argument is positive infinity, then the result is positive zero.
     * <li>If the first argument is negative zero and the second argument is positive, or the first argument is negative and finite and the second argument is positive infinity, then the result is negative zero.
     * <li>If the first argument is positive zero and the second argument is negative, or the first argument is positive and finite and the second argument is negative infinity, then the result is the double value closest to pi.
     * <li>If the first argument is negative zero and the second argument is negative, or the first argument is negative and finite and the second argument is negative infinity, then the result is the double value closest to -pi.
     * <li>If the first argument is positive and the second argument is positive zero or negative zero, or the first argument is positive infinity and the second argument is finite, then the result is the double value closest to pi/2.
     * <li>If the first argument is negative and the second argument is positive zero or negative zero, or the first argument is negative infinity and the second argument is finite, then the result is the double value closest to -pi/2.
     * <li>If both arguments are positive infinity, then the result is the double value closest to pi/4.
     * <li>If the first argument is positive infinity and the second argument is negative infinity, then the result is the double value closest to 3*pi/4.
     * <li>If the first argument is negative infinity and the second argument is positive infinity, then the result is the double value closest to -pi/4.
     * <li>If both arguments are negative infinity, then the result is the double value closest to -3*pi/4.
     * </ul>
     * @param y the ordinate coordinate
     * @param x the abscissa coordinate
     */
    public static double atan2(double y, double x)
	{
    	return sMath.getAtan2(y, x);
    }

    /**
     * Instance implementation called by atan2.
     */
    protected abstract double getAtan2(double y, double x);

    /**
     * Returns the smallest (closest to negative infinity) double value that
     * is not less than the argument and is equal to a mathematical integer.
     * <br>Special cases:
     * <ul>
     * <li>If the argument value is already equal to a mathematical integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or negative zero, then the result is the same as the argument.
     * <li>If the argument value is less than zero but greater than -1.0, then the result is negative zero.
     * </ul>
     * <br>Note that the value of Math.ceil(x) is exactly the value of -Math.floor(-x).
     * @param a a value
     */
    public static double ceil(double a)
	{
    	return sMath.getCeil(a);
    }

    /**
     * Instance implementation called by ceil.
     */
    protected abstract double getCeil(double a);

    /**
     * Returns the trignometric cosine of an angle in radians.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN or an infinity, then the result is NaN.
     * </ul>
     * @param a an angle, in radians
     */
    public static double cos(double a)
	{
    	return sMath.getCos(a);
    }

    /**
     * Instance implementation called by cos.
     */
    protected abstract double getCos(double a);

    /**
     * Returns Euler's number e raised to the power of a double value.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN, the result is NaN.
     * <li>If the argument is positive infinity, then the result is positive infinity.
     * <li>If the argument is negative infinity, then the result is positive zero.
     * </ul>
     * @param a the exponent to raise e to
     */
    public static double exp(double a)
	{
    	return sMath.getExp(a);
    }

    /**
     * Instance implementation called by exp.
     */
    protected abstract double getExp(double a);

    /**
     * Returns the largest (closest to positive infinity) double value that is
     * not greater than the argument and is equal to a mathematical integer.
     * <br>Special cases:
     * <ul>
     * <li>If the argument value is already equal to a mathematical integer, then the result is the same as the argument.
     * <li>If the argument is NaN or an infinity or positive zero or negative zero, then the result is the same as the argument.
     * </ul>
     * @param a a value
     */
    public static double floor(double a)
	{
    	return sMath.getFloor(a);
    }

    /**
     * Instance implementation called by floor.
     */
    protected abstract double getFloor(double a);

    /**
     * Computes the remainder operation on two arguments as prescribed by
     * the IEEE 754 standard.
     * <br>The remainder value is mathematically equal to f1 - f2 × n, where n is the mathematical integer closest to the exact mathematical value of the quotient f1/f2, and if two mathematical integers are equally close to f1/f2, then n is the integer that is even. If the remainder is zero, its sign is the same as the sign of the first argument.
     * <br>Special cases:
     * <ul>
     * <li>If either argument is NaN, or the first argument is infinite, or the second argument is positive zero or negative zero, then the result is NaN.
     * <li>If the first argument is finite and the second argument is infinite, then the result is the same as the first argument.
     * </ul>
     * @param f1 the dividend.
     * @param f2 the divisor.
     */
    public static double getIEEERemainder(double f1, double f2)
	{
    	return sMath.doGetIEEERemainder(f1, f2);
    }

    /**
     * Instance implementation called by IEEEremainder.
     */
    protected abstract double doGetIEEERemainder(double f1, double f2);

    /**
     * Returns a bit representation of the specified floating-point value according to the IEEE 754
     * floating-point double format bit layout.  Does not distinguish between distinct values of NaN.
     * <br>Bit 63 (the bit that is selected by the mask 0x8000000000000000L) represents the sign of the floating-point
     * number. Bits 62-52 (the bits that are selected by the mask 0x7ff0000000000000L) represent the exponent.
     * Bits 51-0 (the bits that are selected by the mask 0x000fffffffffffffL) represent the significand (sometimes
     * called the mantissa) of the floating-point number.
     * @param d A double precision floating-point number to be converted.
     * @return A signed 64-bit Integer that represents the specified floating-point value according
     * to the IEEE 754 floating-point "double format" bit layout.
     */
    public static long doubleToLongBits(double d)
	{
    	return sMath.doDoubleToLongBits(d);
    }

    /**
     * Instance implementation called by DoubleToLongBits.
     * @param d A double precision floating-point number to be converted.
     * @return A signed 64-bit Integer that represents the specified floating-point value according
     * to the IEEE 754 floating-point double format bit layout.
     */
    protected abstract long doDoubleToLongBits(double d);

    /**
     * Returns the double value corresponding to a given bit representation of a floating-point value according to
     * the IEEE 754 floating-point double format bit layout. Does not distinguish between distinct values of NaN.
     * @param l A long integer to be converted.
     * @return The double precision floating-point number with the same bit pattern.
     */
    public static double longBitsToDouble(long l)
	{
    	return sMath.doLongBitsToDouble(l);
    }

    /**
     * Instance implementation called by LongBitsToDouble.
     * @param l A long integer to be converted.
     * @return The double precision floating-point number with the same bit pattern.
     */
    protected abstract double doLongBitsToDouble(long l);

    /**
     * Returns the string representation of the floating-point value that can accurately be parsed back
     * into the same numeric value.  For .NET, this means using the Round-trip Standard Numeric
     * Format Specifier.
     * @param d A double precision floating-point number.
     * @return String representation of the floating-point value that can accurately be parsed back
     * into the same numeric value.
     */
    public static String toStringUnrounded(Double d)
	{
    	return sMath.doToStringUnrounded(d);
    }

    /**
     * Instance implementation called by toStringUnrounded.
     * @param a A double precision floating-point number.
     * @return String representation of the floating-point value that can accurately be parsed back
     * into the same numeric value.
     */
    protected abstract String doToStringUnrounded(Double a);

    /**
     * Returns the natural logarithm (base e) of a double value.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN or less than zero, then the result is NaN.
     * <li>If the argument is positive infinity, then the result is positive infinity.
     * <li>If the argument is positive zero or negative zero, then the result is negative infinity.
     * </ul>
     * @param a a number greater than 0.0.
     */
    public static double log(double a)
	{
    	return sMath.getLog(a);
    }

    /**
     * Instance implementation called by log.
     */
    protected abstract double getLog(double a);

    /**
     * Returns the greater of two double values.
     * <br>That is, the result is the argument closer to positive infinity. If the arguments have the same value, the result is that same value.
     * <br>Special cases:
     * <ul>
     * <li>If either value is NaN, then the result is NaN. Unlike the the numerical comparison operators, this method considers negative zero to be strictly smaller than positive zero.
     * <li>If one argument is positive zero and the other negative zero, the result is positive zero.
     * </ul>
     * @param a an argument.
     * @param b another argument
     */
    public static double max(double a, double b)
	{
    	return sMath.getMax(a, b);
    }

    /**
     * Instance implementation called by max.
     */
    protected abstract double getMax(double a, double b);

    /**
     * Returns the greater of two float values.
     * <br>That is, the result is the argument closer to positive infinity. If the arguments have the same value, the result is that same value.
     * <br>Special cases:
     * <ul>
     * <li>If either value is NaN, then the result is NaN. Unlike the the numerical comparison operators, this method considers negative zero to be strictly smaller than positive zero.
     * <li>If one argument is positive zero and the other negative zero, the result is positive zero.
     * </ul>
     * @param a an argument.
     * @param b another argument
     */
    public static float max(float a, float b)
	{
    	return sMath.getMax(a, b);
    }

    /**
     * Instance implementation called by max.
     */
    protected abstract float getMax(float a, float b);

    /**
     * Returns the greater of two int values.
     * <br>That is, the result is the argument closer to the value of Integer.MAX_VALUE. If the arguments have the same value, the result is that same value.
     * @param a an argument.
     * @param b another argument
     */
    public static int max(int a, int b)
	{
    	return sMath.getMax(a, b);
    }

    /**
     * Instance implementation called by max.
     */
    protected abstract int getMax(int a, int b);

    /**
     * Returns the greater of two long values.
     * <br>That is, the result is the argument closer to the value of Long.MAX_VALUE. If the arguments have the same value, the result is that same value.
     * @param a an argument.
     * @param b another argument
     */
    public static long max(long a, long b)
	{
    	return sMath.getMax(a, b);
    }

    /**
     * Instance implementation called by max.
     */
    protected abstract long getMax(long a, long b);

    /**
     * Returns the smaller of two double values.
     * <br>That is, the result is the value closer to negative infinity.
     * <br>Special cases:
     * <ul>
     * <li>If the arguments have the same value, the result is that same value.
     * <li>If either value is NaN, then the result is NaN. Unlike the the numerical comparison operators, this method considers negative zero to be strictly smaller than positive zero.
     * <li>If one argument is positive zero and the other is negative zero, the result is negative zero.
     * </ul>
     * @param a an argument.
     * @param b another argument
     */
    public static double min(double a, double b)
	{
    	return sMath.getMin(a, b);
    }

    /**
     * Instance implementation called by min.
     */
    protected abstract double getMin(double a, double b);

    /**
     * Returns the smaller of two float values.
     * <br>That is, the result is the value closer to negative infinity.
     * <br>Special cases:
     * <ul>
     * <li>If the arguments have the same value, the result is that same value.
     * <li>If either value is NaN, then the result is NaN. Unlike the the numerical comparison operators, this method considers negative zero to be strictly smaller than positive zero.
     * <li>If one argument is positive zero and the other is negative zero, the result is negative zero.
     * </ul>
     * @param a an argument.
     * @param b another argument
     */
    public static float min(float a, float b)
	{
    	return sMath.getMin(a, b);
    }

    /**
     * Instance implementation called by min.
     */
    protected abstract float getMin(float a, float b);

    /**
     * Returns the smaller of two int values.
     * <br>That is, the result the argument closer to the value of Integer.MIN_VALUE. If the arguments have the same value, the result is that same value.
     * @param a an argument.
     * @param b another argument
     */
    public static int min(int a, int b)
	{
    	return sMath.getMin(a, b);
    }

    /**
     * Instance implementation called by min.
     */
    protected abstract int getMin(int a, int b);

    /**
     * Returns the smaller of two long values.
     * <br>That is, the result is the argument closer to the value of Long.MIN_VALUE. If the arguments have the same value, the result is that same value.
     * @param a an argument.
     * @param b another argument
     */
    public static long min(long a, long b)
	{
    	return sMath.getMin(a, b);
    }

    /**
     * Instance implementation called by min.
     */
    protected abstract long getMin(long a, long b);

    /**
     * Returns the value of the first argument raised to the power of the
     * second argument.
     * <br>Special cases:
     * <ul>
     * <li>If the second argument is positive or negative zero, then the result is 1.0.
     * <li>If the second argument is 1.0, then the result is the same as the first argument.
     * <li>If the second argument is NaN, then the result is NaN.
     * <li>If the first argument is NaN and the second argument is nonzero, then the result is NaN.
     * <li>If the absolute value of the first argument is greater than 1 and the second argument is positive infinity, then the result is positive infinity.
     * <li>If the absolute value of the first argument is less than 1 and the second argument is negative infinity, then the result is positive infinity.
     * <li>If the absolute value of the first argument is greater than 1 and the second argument is negative infinity, then the result is positive zero.
     * <li>If the absolute value of the first argument is less than 1 and the second argument is positive infinity, then the result is positive zero.
     * <li>If the absolute value of the first argument equals 1 and the second argument is infinite, then the result is NaN.
     * <li>If the first argument is positive zero and the second argument is greater than zero, then the result is positive zero.
     * <li>If the first argument is positive infinity and the second argument is less than zero, then the result is positive zero.
     * <li>If the first argument is positive zero and the second argument is less than zero, then the result is positive infinity.
     * <li>If the first argument is positive infinity and the second argument is greater than zero, then the result is positive infinity.
     * <li>If the first argument is negative zero and the second argument is greater than zero but not a finite odd integer, then the result is positive zero.
     * <li>If the first argument is negative infinity and the second argument is less than zero but not a finite odd integer, then the result is positive zero.
     * <li>If the first argument is negative zero and the second argument is a positive finite odd integer, then the result is negative zero.
     * <li>If the first argument is negative infinity and the second argument is a negative finite odd integer, then the result is negative zero.
     * <li>If the first argument is negative zero and the second argument is less than zero but not a finite odd integer, then the result is positive infinity.
     * <li>If the first argument is negative infinity and the second argument is greater than zero but not a finite odd integer, then the result is positive infinity.
     * <li>If the first argument is negative zero and the second argument is a negative finite odd integer, then the result is negative infinity.
     * <li>If the first argument is negative infinity and the second argument is a positive finite odd integer, then the result is negative infinity.
     * <li>If the first argument is finite and less than zero and if the second argument is a finite even integer, the result is equal to the result of raising the absolute value of the first argument to the power of the second argument
     * <li>If the first argument is finite and less than zero and if the second argument is a finite odd integer, the result is equal to the negative of the result of raising the absolute value of the first argument to the power of the second argument
     * <li>If the first argument is finite and less than zero and if the second argument is finite and not an integer, then the result is NaN.
     * <li>If both arguments are integers, then the result is exactly equal to the mathematical result of raising the first argument to the power of the second argument if that result can in fact be represented exactly as a double value.
     * </ul>
     * <p>(In the foregoing descriptions, a floating-point value is considered to be an integer if and only if it is finite and a fixed point of the method ceil or, equivalently, a fixed point of the method floor. A value is a fixed point of a one-argument method if and only if the result of applying the method to the value is equal to the value.)
     * @param a the base.
     * @param b the exponent
     */
    public static double pow(double a, double b)
	{
    	return sMath.getPow(a, b);
    }

    /**
     * Instance implementation called by pow.
     */
    protected abstract double getPow(double a, double b);

    /**
     * Returns the closest long to the argument.
     * The result is rounded to an integer by adding 1/2, taking the floor of the result, and casting the result to type int. In other words, the result is equal to the value of the expression:
     * <p>(int)Math.floor(a + 0.5f)
     * <p>Special cases:
     * <ul>
     * <li>If the argument is NaN, the result is 0.
     * <li>If the argument is negative infinity or any value less than or equal to the value of Long.MIN_VALUE(java) or Int32.MIN_VALUE(.NET), the result is equal to the value of Long.MIN_VALUE(java) or Int32.MIN_VALUE(.NET).
     * <li>If the argument is positive infinity or any value greater than or equal to the value of Long.MAX_VALUE(java) or Int32.MAX_VALUE(.NET), the result is equal to the value of Long.MAX_VALUE(java) or Int32.MAX_VALUE(.NET).
     * </ul>
     * @param a a floating-point value to be rounded to a long.
     */
    public static long round(double a)
	{
    	return sMath.getRound(a);
    }

    /**
     * Instance implementation called by round.
     */
    protected abstract long getRound(double a);

    /**
     * Returns the trigonometric sine of an angle.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN or an infinity, then the result is NaN.
     * <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     * @param a an angle, in radians.
     */
    public static double sin(double a)
	{
    	return sMath.getSin(a);
    }

    /**
     * Instance implementation called by sin.
     */
    protected abstract double getSin(double a);

    /**
     * Returns the correctly rounded positive square root of a double value.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN or less than zero, then the result is NaN.
     * <li>If the argument is positive infinity, then the result is positive infinity.
     * <li>If the argument is positive zero or negative zero, then the result is the same as the argument.
     * </ul>
     * Otherwise, the result is the double value closest to the true mathematical square root of the argument value.
     * @param a a value.
     */
    public static double sqrt(double a)
	{
    	return sMath.getSqrt(a);
    }

    /**
     * Instance implementation called by sqrt.
     */
    protected abstract double getSqrt(double a);

    /**
     * Returns the trignometric tangent of an angle.
     * <br>Special cases:
     * <ul>
     * <li>If the argument is NaN or an infinity, then the result is NaN.
     * <li>If the argument is zero, then the result is a zero with the same sign as the argument.
     * </ul>
     * @param a an angle, in radians.
     */
    public static double tan(double a)
	{
    	return sMath.getTan(a);
    }

    /**
     * Instance implementation called by tan.
     */
    protected abstract double getTan(double a);

    /**
     * Returns a random int value between lbound (inclusive) and
     * ubound (exclusive).
     * @param lbound The lower bound for the random int.
     * @param ubound The upper bound for the random int.
     * @return A random integer, such that lbound &lt;= randomInt &lt; ubound
     */
    public static int randomInt(int lbound, int ubound)
    {
    	if (lbound < 0) throw new SpcfIllegalArgumentException("SpcfMath.randomInt() " + lbound + " < 0");
    	if (lbound > ubound) throw new SpcfIllegalArgumentException("SpcfMath.randomInt() " + lbound + " > " + ubound);

    	return sMath.getRandomInt(lbound, ubound);
    }

    /**
     * Returns a random int value between 0 (inclusive) and
     * ubound (exclusive).
     * @param ubound The upper bound for the random int.
     * @return A random integer, such that 0 &lt;= randomInt &lt; ubound
     */
    public static int randomInt(int ubound)
    {
    	return SpcfMath.randomInt(0, ubound);
    }

    /**
     * Instance implementation called by randomInt
     */
    protected abstract int getRandomInt(int lbound, int ubound);

}

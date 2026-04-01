package com.intuit.spc.foundations.primary;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

import java.math.BigDecimal;

/**
 * This is a generic immutable Money class,
 * that represents valid numbers
 * (values other than positive infinity, negative infinity or NAN)
 * with exactly 2 fractional decimals
 * and maximum 13 decimals for the whole part.
 *
 * Any operation like add, subtract, etc. returns a SpcfMoney instance.
 */
public class SpcfMoney extends SpcfDecimal
{
    public static final SpcfMoney ZERO = new SpcfMoney("0.00");

	/**
	 * ID for native serialization
	 */
	private static final long serialVersionUID = -4387699858867861892L;

	/**
	 * internal decimal representation
	 */
	private SpcfDecimal mDecimal;
	/**
	 * Money scale
	 */
	final static private int MoneyScale = 2;

	/**
	 * Constructs from a decimal string. The format includes an optional +/-
	 * sign followed by one or more digits optionally separated by a single decimal point.
	 * Alternatively, the string can be one of "Infinity", "-Infinity", or "NAN".
	 *
	 * If the resulting number has more than 13 digits for its whole part (mantissa),
	 * then the number becomes infinity (either positive or negative infinity, depending
	 * on the sign). Otherwise, the result has exactly 2 fractional decimals (either rounding
	 * or zero padding occurs if necessary).
	 *
	 * @param decimalString decimal string representation
	 *
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if argument contains invalid characters
	 */
	 public SpcfMoney(String decimalString)
	 {
	 	mDecimal = SpcfFactory.getInstance().createDecimal(decimalString);
	 	mDecimal = mDecimal.setScale(MoneyScale);
	 }

	 /**
	  * Construct from a SpcfDecimal instance by setting its scale to 2
	  * (rounding or zero padding occurs if necessary). If the decimal is not a valid
	  * numeric value (for example, it is positive or negative infinity or NAN), then
	  * this instance becomes an invalid numeric value itself.
	  *
	  * Note: There is no signaling for positive or negative infinity (when set via
	  * setSignal method) even if the decimal parameter is positive or negative infinity or NAN.
	  *
	  * @param dec number to construct from
	  * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if argument is null
	  */
	 public SpcfMoney(SpcfDecimal dec)
	 {
		 SpcfParamValidator.checkIsNotNull(dec, "dec");
		 mDecimal = dec;
		 mDecimal = mDecimal.setScale(MoneyScale);
	 }

	 /**
	  * Constructs a new instance of SpcfMoney with value "0.00".
	  */
	 public SpcfMoney()
	 {
		 mDecimal = SpcfFactory.getInstance().createDecimal("0.00");
	 }

	/**
	 * Returns the same object as 'this' if scale is 2,
	 * otherwise it returns a new SpcfDecimal (not a SpcfMoney) with the
	 * specified scale.
	 *
	 * @see SpcfDecimal#setScale(int)
	 */
	@Override
	public SpcfDecimal setScale(int scale)
	{
		if (scale == MoneyScale)
		{
			return this;
		}
		return mDecimal.setScale(scale);
	}

	/**
	 * Returns the same object as 'this' if scale is 2.
	 * Otherwise it returns a SpcfDecimal (not a SpcfMoney) with the
	 * specified scale.
	 *
	 * @see SpcfDecimal#setScale(int, SpcfRoundingType)
	 */
	@Override
	public SpcfDecimal setScale(int scale, SpcfRoundingType roundingType)
	{
		if (scale == MoneyScale)
		{
			return this;
		}
		return mDecimal.setScale(scale, roundingType);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getSign()
	 */
	@Override
	public int getSign()
	{
		return mDecimal.getSign();
	}

	/**
	 * Returns a new SpcfMoney instance representing the negative value of 'this' object.
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#negate()
	 */
	@Override
	public SpcfDecimal negate()
	{
		return new SpcfMoney(mDecimal.negate());
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getScale()
	 */
	@Override
	public int getScale()
	{
		return mDecimal.getScale();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getFractionalPart()
	 */
	@Override
	public int getFractionalPart()
	{
		return mDecimal.getFractionalPart();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getIntegerPart()
	 */
	@Override
	public long getIntegerPart()
	{
		return mDecimal.getIntegerPart();
	}

	/**
	 * Returns a new SpcfMoney instance representing the (this + val).
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#add(SpcfDecimal)
	 */
	@Override
	public SpcfDecimal add(SpcfDecimal val)
	{
		return new SpcfMoney(mDecimal.add(val));
	}

	/**
	 * Returns a new SpcfMoney instance representing (this - val).
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#subtract(SpcfDecimal)
	 */
	@Override
	public SpcfDecimal subtract(SpcfDecimal val)
	{
		return new SpcfMoney(mDecimal.subtract(val));
	}

	/**
	 * Returns a new SpcfMoney instance representing (this multiplied by val).
	 * @see SpcfDecimal#multiply(SpcfDecimal)
	 */
	@Override
	public SpcfDecimal multiply(SpcfDecimal val)
	{
		return new SpcfMoney(mDecimal.multiply(val));
	}

	/**
	 * Returns a new SpcfMoney instance representing (this multiplied by val) using
	 * the specified rounding type. If the specified
	 * scale is not 2, then SpcfIllegalArgumentException is thrown.
	 *
	 * @see SpcfDecimal#multiply(SpcfDecimal, int, SpcfRoundingType)
	 * @throws SpcfIllegalArgumentException if scale is not 2
	 */
	@Override
	public SpcfDecimal multiply(SpcfDecimal val, int scale, SpcfRoundingType roundingType)
	{
		if (scale == MoneyScale)
		{
			return new SpcfMoney(mDecimal.multiply(val, scale, roundingType));
		}
		throw new SpcfIllegalArgumentException();
	}

	/**
	 * Returns a new SpcfMoney instance that represents (this / val).
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#divide(SpcfDecimal)
	 */
	@Override
	public SpcfDecimal divide(SpcfDecimal val)
	{
		return new SpcfMoney(mDecimal.divide(val));
	}

	/**
	 * Returns a new SpcfMoney instance representing (this / val) the specified rounding type.
	 * If the specified scale is not 2, then SpcfIllegalArgumentException is thrown.
	 *
	 * @see SpcfDecimal#divide(SpcfDecimal, int, SpcfRoundingType)
	 * @throws SpcfIllegalArgumentException if scale is not 2
	 */
	@Override
	public SpcfDecimal divide(SpcfDecimal val, int scale, SpcfRoundingType roundingType)
	{
		if (scale == MoneyScale)
		{
			return new SpcfMoney(mDecimal.divide(val, scale, roundingType));
		}
		throw new SpcfIllegalArgumentException();
	}

	/**
	 * Returns a new SpcfMoney instance representing the remainder of division of this by val.
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#remainder(SpcfDecimal)
	 */
	@Override
	public SpcfDecimal remainder(SpcfDecimal val)
	{
		return new SpcfMoney(mDecimal.remainder(val));
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#toString()
	 */
	@Override
	public String toString()
	{
		return mDecimal.toString();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#compareTo(SpcfDecimal)
	 */
	@Override
	public int compareTo(SpcfDecimal val)
	{
		return mDecimal.compareTo(val);
	}

    @Override
    public boolean isGreaterThan(SpcfDecimal val) {
        return mDecimal.isGreaterThan(val);
    }

    @Override
    public boolean isLessThan(SpcfDecimal val) {
        return mDecimal.isLessThan(val);
    }

    @Override
    public boolean isGreaterThanEqualTo(SpcfDecimal val) {
        return mDecimal.isGreaterThanEqualTo(val);
    }

    @Override
    public boolean isLessThanEqualTo(SpcfDecimal val) {
        return mDecimal.isLessThanEqualTo(val);
    }

    @Override
    public boolean isZero() {
        return this.compareTo(ZERO) == 0;
    }

    /**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#compareTo(Object)
	 */
	@Override
	public int compareTo(Object val)
	{
		return mDecimal.compareTo(val);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#equals(Object)
	 */
	@Override
	public boolean equals(Object val)
	{
		return mDecimal.equals(val);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return mDecimal.hashCode();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#toBytes()
	 */
	@Override
	public byte[] toBytes()
	{
		return mDecimal.toBytes();
	}

	/**
	 * Returns a new SpcfMoney instance representing the absolute value of this instance.
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#abs()
	 */
	@Override
	public SpcfDecimal abs()
	{
		return new SpcfMoney(mDecimal.abs());
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isPositiveInfinity()
	 */
	@Override
	public boolean isPositiveInfinity()
	{
		return mDecimal.isPositiveInfinity();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isNegativeInfinity()
	 */
	@Override
	public boolean isNegativeInfinity()
	{
		return mDecimal.isNegativeInfinity();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isNAN()
	 */
	@Override
	public boolean isNAN()
	{
		return mDecimal.isNAN();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isNumber()
	 */
	@Override
	public boolean isNumber()
	{
		return mDecimal.isNumber();
	}

	/**
	 * Returns a new SpcfMoney instance representing the mid point of the interval
	 * with the specified intervalSize to which this belongs.
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#midPoint(long)
	 */
	@Override
	public SpcfDecimal midPoint(long intervalSize)
	{
		return new SpcfMoney(mDecimal.midPoint(intervalSize));
	}

	/**
	 * Not for public use.
	 */
	@Override
    public Object toImpl()
    {
        return mDecimal.toImpl();
    }

    /**
     * This is a protected abstract method to allow us to receive back the platform specific value
     * for the negative infinity integer part.
     */
	@Override
	protected long doGetPlatformSpecificNegativeInfinityIntegerPart()
	{
		// The answer is already in SpcfDecimal. Just pass it along.
		return SpcfDecimal.NegativeInfinityIntegerPart;
	}

	/**
	 * Getter used only for serialization purposes.  Assumes that the serialized
	 * value of this consist of just the string representation of the internal
	 * decimal value.  Since the decimal scale is always represented by
	 * {@link #MoneyScale}
	 * @return the actual data that is to be serialized
	 */
	@SuppressWarnings("unused")
	private String getDecimalValue()
	{
		return mDecimal.toString();
	}

	/**
	 * Setter used only for serialization purposes.  Assumes that the serialized
	 * value of this consist of just the string representation of the internal
	 * decimal value.  Since the decimal scale is always represented by
	 * {@link #MoneyScale}
	 * @param dec the data to be deserialized in
	 */
	@SuppressWarnings("unused")
	private void setDecimalValue(String dec)
	{
		mDecimal = SpcfDecimal.createInstance(dec);
		mDecimal = mDecimal.setScale(MoneyScale);
	}

	/**
	 * convert SpcfMoney to BigDecimal
	 */
	public BigDecimal toBigDecimal() {
		return new BigDecimal(mDecimal.toString());
	}
}

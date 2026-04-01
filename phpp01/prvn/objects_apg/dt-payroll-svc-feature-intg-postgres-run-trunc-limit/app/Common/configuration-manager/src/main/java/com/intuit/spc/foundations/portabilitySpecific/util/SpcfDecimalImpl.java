package com.intuit.spc.foundations.portabilitySpecific.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

/**
 * Class implementation for SpcfDecimal (13.8 fixed point 
 * decimal representation, used for financial arithmetic) 
 */
public class SpcfDecimalImpl extends SpcfDecimal 
{

	private static final long serialVersionUID = -1509442750527124502L;

	protected BigDecimal mDecimal; 
		
	//some enums; do not change their values unless
	//also modifying the setSignalError implementation
	private final static int ValidNumberType = 2;
	private final static int PositiveInfinityType = 1;
	private final static int NegativeInfinityType = -1;
	private final static int NanType = 0;
	
	protected int mType = ValidNumberType; //mType can be one of the 4 values above
	
	//some consts
	//protected final static int   MAX_INT_DIGITS = 13;
	//protected final static int   MAX_SCALE = 8;
	protected final static long  MaxInt = 9999999999999L; //13 digits
	//protected final static int   MAX_FRACTION = 99999999; //8 digits
	
	private final static SpcfDecimal PositiveInfinity = new SpcfDecimalImpl(PositiveInfinityType);
	private final static SpcfDecimal NegativeInfinity = new SpcfDecimalImpl(NegativeInfinityType);
	private final static SpcfDecimal Nan = new SpcfDecimalImpl(NanType);
	private final static SpcfDecimal Zero = new SpcfDecimalImpl("0");
	private final static SpcfDecimal Two = new SpcfDecimalImpl("2");
	private final static BigDecimal ZeroDecimal = new BigDecimal("0");

	/**
	 * Required to support XML serialization.
	 */
	public SpcfDecimalImpl(){}
	
	/**
	 * Private ctor used internally
	 * @param decimalVar
	 */
	private SpcfDecimalImpl(BigDecimal decimalVar)
	{
		mDecimal = decimalVar;
	}
	
	/**
	 * public ctor used to match the toSpecific
	 * @param decimalVar
	 */
	public SpcfDecimalImpl(java.math.BigDecimal decimalVar)
	{
		mDecimal = new BigDecimal(decimalVar);
		
		//round to MaxScale
		if (mDecimal.scale() > MaxScale)
		{
			mDecimal = mDecimal.setScale(MaxScale, BigDecimal.ROUND_HALF_UP);
		}
	}
	
	/**
	 * Private ctor used internally to for creating infinities or NAN_TYPE
	 * 
	 */
	private SpcfDecimalImpl(int type)
	{
		mType = type;
	}
	
	/**
	 * Public ctor used to create SpcfDecimal from a double
	 * 
	 */
	public SpcfDecimalImpl(double decimalVar)
	{
		mDecimal = new BigDecimal(decimalVar);
		
		//round to MaxScale
		if (mDecimal.scale() > MaxScale)
		{
			mDecimal = mDecimal.setScale(MaxScale, BigDecimal.ROUND_HALF_UP);
		}
	}
	
	/**
	 * Public ctor used to create SpcfDecimal from a long
	 * 
	 */
	public SpcfDecimalImpl(long decimalVar)
	{
		mDecimal = new BigDecimal(decimalVar);
	}
	
	/**
	 * Constructs from a decimal string. The format is optional +/- 
	 * sign followed by 1 or more digits optionally separated by decimal point.
	 * Alternativelly the string can be one of "Infinity", "-Infinity" or "NAN".
	 * 
	 * If the string is an overflow, then the number becomes infinity.
	 * If necessary the number is rounded to max 8 fractional decimals.
	 * 
	 * @param decimalString - decimal string representation
	 * 
	 * @throws SpcfArgumentNullException if argument is null
	 * @throws SpcfIllegalArgumentException if argument contains invalid chars 
	 */
	public SpcfDecimalImpl(String decimalString)
	{
		init(decimalString);
	}

	static final byte ByteSerializationVersion = 0; 
	
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#toBytes()
	 */
	public byte[] toBytes() 
	{
		//the serialized to byte[] consists of the following:
		//byte[0] the 8 bits are as following: 
		//	2 for version
		//	1 for sign (0 for +, 1 for -)
		//	1 for type (0 = number, 1 = special type: NAN or infinity)
		//	4 for scale or for special type info (0000 = NAN, 0001 = Infinity)
		//byte[1] - byte[9] serialized long from the first 19 decimal digits
		//byte[10] - serialized last 2 decimal digits
		
		byte firstByte = 0;
		
		//set the version of the serialization
		firstByte |= (ByteSerializationVersion << 6); 
		
		//set sign	
		if (this.getSign() < 0)
		{
			byte negative = 1;
			firstByte |= (negative << 5);
		}
		
		if (mType == ValidNumberType)
		{
			//set scale
			byte scale = (byte)mDecimal.scale();
			firstByte += scale;
			
			//if zero, return
			if (mDecimal.compareTo(ZeroDecimal) == 0)
			{
				byte [] ret = {firstByte};
				return ret;
			}
			
			//serialize the mantissa
			byte[] man = mDecimal.getMantissa();
			int manLen = man.length;
			
			//first 19 digits are a packed as a long
			//either positive if represent <= long.MAX_VALUE (18 bits)
			//or negative if > long.MAX_VALUE (18 or 19 bits)
			long l19Man = 0;
			if (manLen >= 18)
			{
				//prepare an array of first 19 bytes from man
				int l19ManLen = (manLen == 18) ? 18 : 19;
				byte [] firstManBytes = new byte[l19ManLen];
				for (int i = 0; i< l19ManLen; ++i)
				{
					firstManBytes[i] = man[i];
				}
				BigDecimal d = new BigDecimal(0 /*scale*/, (byte)1 /*positive*/,firstManBytes);
				BigDecimal dLongMax = new BigDecimal(Long.MAX_VALUE);
				if (d.compareTo(dLongMax) > 0)
				{
					d = d.subtract(dLongMax);
					l19Man = -mantissaAsLong(d);
				}
				else
				{
					l19Man = mantissaAsLong(d);
				}
			}
			else
			{
				l19Man = mantissaAsLong(mDecimal);
			}
			
			//now pack the long into 8 bytes
			byte[] lB = new byte[8];
			for (int i = 0; i< 8; ++i)
			{
				lB [i] = (byte)(l19Man & 255L);
				l19Man >>= 8;
			}
			
			//if there are more than 19 digits, then the last 2 digits are packed
			//in an extra byte
			if (manLen > 19)
			{
				byte [] res = new byte[10];
				res[0] = firstByte;
				for (int i = 1; i< 9; ++i)
				{
					res[i] = lB[i-1];
				}
				
				if(manLen >20)
				{
					res[9] = (byte)(man[19]*10 + man[20]);
				}
				else
				{
					res[9] = man[19];	
				}
				return res;
			}
			else 
			//up to 19 digits, 
			//then remove all the zero bytes from lB
			{
				int lBCompactLen = lB.length;
				while (lBCompactLen >0)
				{
					if (lB[lBCompactLen -1] !=0)
					{
						break;
					}
					lBCompactLen--;
				}
				byte [] res = new byte[lBCompactLen + 1];
				res[0] = firstByte;
				for (int i = 1; i<= lBCompactLen; ++i)
				{
					res[i] = lB[i-1];
				}
				return res;
			}
		}
		else //serialize +/-infinte or NAN
		{
			//byte[0] the 8 bits are as following: 
			//	2 for version
			//	1 for sign (0 for +, 1 for -)
			//	1 for type (0 = number, 1 = special type: NAN or infinity)
			//	4 for scale or for special type info (0000 = NAN, 0001 = Infinity)
			
			byte specialType = 1;
			firstByte |= specialType << 4;
			byte specialTypeInfo = (byte)((this.mType == NanType) ? 0 : 1);
			firstByte |= specialTypeInfo; 
			
			byte [] ret = {firstByte};
			return ret;
		}
	}
	
	/**
	 * Returns the value of mantissa as a long.
	 * It assumes that it won't be an overflow.
	 */
	static private long mantissaAsLong(BigDecimal decimalVar)
	{
		long res = 0L;
		
		byte[] man = decimalVar.getMantissa();
		for (int i = 0; i < man.length; ++i)
		{
			if (i != 0)
				res *= 10;
			
			res += man[i];
		}
		return res;
	}
	
	/**
	 * Constructs from a byte[] array, as returned 
	 * from SpcfDecimal.toBytes().
	 *
	 * @param bytes byte array as returned from SpcfDecimal.toBytes()
	 * 
	 * @throws SpcfArgumentNullException if argument is null
	 * @throws SpcfIllegalArgumentException if invalid argument 
	 */
	public SpcfDecimalImpl(byte[] bytes)
	{
		SpcfParamValidator.checkIsNotNull(bytes, "bytes");
		
		int bytesLen = bytes.length;
		
		//verify the len and version
		if (bytesLen == 0 || bytesLen > 10 || 
				ByteSerializationVersion != (bytes[0] & 0xC0))
		{
			throw new SpcfIllegalArgumentException();	
		}
		
		//the serialized to byte[] consists of the following:
		//byte[0] the 8 bits are as following: 
		//	2 for version
		//	1 for sign (0 for +, 1 for -)
		//	1 for type (0 = number, 1 = special type: NAN or infinity)
		//	4 for scale or for special type info (0000 = NAN, 0001 = Infinity)
		//byte[1] - byte[9] serialized long from the first 19 decimal digits
		//byte[10] - serialized last 2 decimal digits
		
		//deserialize the 1st byte sign
		byte ind = (byte)((bytes[0] & 0x20) >> 5);
		//deserialize the 1st byte special type (NAN or infinity)
		byte specialType = (byte)((bytes[0] & 0x10) >> 4);
			
		if (specialType !=0) //NAN or infinity
		{
			if ((bytes[0] & 0x01) == 0)
			{
				this.mType = NanType;
				signalError(mType);
			}
			else if ((bytes[0] & 0x01) == 1)
			{
				this.mType = (ind == 1) ? NegativeInfinityType : PositiveInfinityType;
				signalError(mType);
			}
			else 
			{
				throw new SpcfIllegalArgumentException();
			}
			
		}
		else //this is 'normal' a numeric field
		{
			//the idea is to convert the compacted byte[] to
			//an array of digits (i.e. 0-9 value per byte)
			
			int scale = (bytes[0] & 0x0F);
			if (scale > MaxScale)
			{
				throw new SpcfIllegalArgumentException();
			}
			
			if (bytesLen == 1) //this is ZERO 
			{ 
				byte[] mant = {(byte)0};
				mDecimal = new BigDecimal(-scale, (byte)0, mant);
			}
			else 
			//deserialize the next 8 bytes as a long
			//and if it exists, the last byte as the byte 20 and 21 of the mantissa	
			{
				//reconstitute the long
				long lD = 0;
				int longLen = bytesLen - 1; //1 is for byte[0]
				if (longLen > 8)
					longLen = 8;
				
				for (int i = 0; i< longLen; ++i)
				{
					lD |= (bytes[longLen-i] & 0xFF);
					if (i != longLen-1)
						lD <<= 8;
				} 
				
				String s = Long.toString(Math.abs(lD));
				if (lD < 0)
				{
					s = new BigDecimal(s).add(new BigDecimal(Long.MAX_VALUE)).toString();
				}
				
				//add the last 2 digits
				if (bytesLen > 9)
				{
					s += Byte.toString(bytes[9]);
				}
				
				//convert from String s to a byte[] of digits
				byte[] mant = new byte[s.length()];
				for (int i=0; i< mant.length; ++i)
				{
					mant[i] = (byte)(s.charAt(i)-'0');
				}
				//ind is 0 when positive, and 1 when negative
				//we have to translate to 1 or -1 respectevly
				mDecimal = new BigDecimal(-scale, ((ind == 0)? (byte)1 : (byte)-1), mant);
			}
		}
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
	 * the fractional part is rounded to fit the scale.
	 * E.g. 
	 *   integerPart = 0; fractionalPart = 12345; scale 4; => results into 0.1235.
	 * 
	 * If the resulting number is an overflow, then the 'this' number becomes +/-infinity.
	 * If the integer part represents the NAN integer part, 
	 * then the 'this' number is also a NAN.
	
	 * @param sign positive, 0, or negative number that determines the sign.
	 * A zero sign can only be used to create a number representing zero or NAN.
	 *  
	 * @param integerPart value representing the absolute integer decimal
	 * @param fractionalPart value representing the absolute fractional decimal
	 * @param scale the scale of the decimal
	 * 
	 * @throws SpcfIllegalArgumentException 
	 * if scale is not in the [0-8] range, 
	 * or if sign is 0 and the number is not representing zero or NAN 
	 */
	public SpcfDecimalImpl(int sign, long integerPart, int fractionalPart, int scale)
	{
		long absIntegerPart = Math.abs(integerPart);
		
		if (absIntegerPart > SpcfDecimalImpl.MaxInt) //special case
		{
			if (integerPart == NanIntegerPart)
			{
				mType = NanType;
				signalError(mType);
			}
			else
			{
				if (sign == 0)
				{
					throw new SpcfIllegalArgumentException();
				}
				mType = (sign > 0) ? PositiveInfinityType : NegativeInfinityType;
				signalError(mType);
			}
		}
		else //it's a number
		{
			if (scale < 0 || scale > MaxScale)
			{
				throw new SpcfIllegalArgumentException();
			}
			
			fractionalPart = Math.abs(fractionalPart);
			
			String s = ((sign < 0) ? "-" : "") +  Long.toString(absIntegerPart) ;
			
			String fractionalString = Integer.toString(fractionalPart);
			while (fractionalString.length() < scale)
			{
				//prepend with zeros
				fractionalString = "0" + fractionalString;
			}
			
			s += "." + fractionalString;
			mDecimal = new BigDecimal(s).setScale(scale,BigDecimal.ROUND_HALF_UP); 
			
			//check if overflow
			if (isOverflow(mDecimal))
			{
				mType = (mDecimal.signum() < 0) ? NegativeInfinityType : PositiveInfinityType;
				mDecimal = null;
			}
			else if (sign == 0 && mDecimal.compareTo(ZeroDecimal) != 0)
			{
				throw new SpcfIllegalArgumentException();
			}
		}
	}
	
	static private void signalError(int type)
	{
		if (sSignal != null)
		{
			sSignal.signal(type);
		}
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#setScale
	 * (int)
	 */
	public SpcfDecimal setScale(int scale) 
	{
		if (scale < 0 || scale > MaxScale)
		{
			throw new SpcfIllegalArgumentException();
		}
		
		if (mType != ValidNumberType || scale == mDecimal.scale())
		{
			return this;
		}
		//calling setScale DOESN'T affect the mDecimal, a new one is created
		BigDecimal result = mDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
		if (isOverflow(result))
		{
			return (result.signum() > 0 ? PositiveInfinity : NegativeInfinity); 
		}
		return new SpcfDecimalImpl(result);

	}

	static private int roundToBigDecimalType(SpcfRoundingType rounding)
	{
	
		if (rounding == SpcfDecimal.SpcfRoundingType.HalfUp)
			return BigDecimal.ROUND_HALF_UP;
		
		if (rounding == SpcfDecimal.SpcfRoundingType.HalfEven)
			return BigDecimal.ROUND_HALF_EVEN;
		
		if (rounding == SpcfDecimal.SpcfRoundingType.HalfDown)
			return BigDecimal.ROUND_HALF_DOWN;
		
		if (rounding == SpcfDecimal.SpcfRoundingType.Down)
			return BigDecimal.ROUND_DOWN;
		
		if (rounding == SpcfDecimal.SpcfRoundingType.Up)
			return BigDecimal.ROUND_UP;
		
		if (rounding == SpcfDecimal.SpcfRoundingType.Floor)
			return BigDecimal.ROUND_FLOOR;
		
		if (rounding == SpcfDecimal.SpcfRoundingType.Ceiling)
			return BigDecimal.ROUND_CEILING;
		
		return BigDecimal.ROUND_HALF_UP; 
	}
	
	/** 
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#setScale(int, SpcfRoundingType)
	 */
	public SpcfDecimal setScale(int scale, SpcfRoundingType roundingType) 
	{
		SpcfParamValidator.checkIsNotNull(roundingType, "roundingType");
		if (scale < 0 || scale > MaxScale)
		{
			throw new SpcfIllegalArgumentException();
		}
		
		if (mType != ValidNumberType || scale == mDecimal.scale())
		{
			return this;
		}
		//calling setScale DOESN'T affect the mDecimal, a new BigDecimal is created
		BigDecimal result = mDecimal.setScale(scale, roundToBigDecimalType(roundingType));
		if (isOverflow(result))
		{
			return (result.signum() > 0 ? PositiveInfinity : NegativeInfinity); 
		}
		return new SpcfDecimalImpl(result);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getSign()
	 */
	public int getSign() 
	{
		if (mType == ValidNumberType)
		{
			return mDecimal.signum();
		} 
		else
		{
			switch (mType)
			{
			case PositiveInfinityType:
				return 1;
			case NegativeInfinityType:
				return -1;
			}
			return 0; //NAN_TYPE
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#negate()
	 */
	public SpcfDecimal negate() 
	{
		if (mType == ValidNumberType)
		{
			if (mDecimal.signum() != 0)
			{
				return new SpcfDecimalImpl(mDecimal.negate()); //doens't affect mDecimal
			}
		} 
		else
		{
			switch (mType)
			{
			case PositiveInfinityType:
				return NegativeInfinity;
			case NegativeInfinityType:
				return PositiveInfinity;
			}
		}
		return this;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getScale()
	 */
	public int getScale()
	{
		if (mType == ValidNumberType)
		{
			return mDecimal.scale();
		} 
		return -1;
	}

	private static final int[] FractionalPowers10 = 
	{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000};
		
	private static final long[] IntegerPowers10 = 
	{1, 10, 100, 1000, 10000, 100000, 1000000, 10000000,
			100000000, 1000000000, 10000000000L, 100000000000L,
			 1000000000000L};
	
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getFractionalPart()
	 */
	public int getFractionalPart() 
	{
		if (mType == ValidNumberType)
		{
			byte[] mant = mDecimal.getMantissa();
			int scale = mDecimal.scale();
			
			if (mant == null || scale == 0)
			{
				return 0;
			}
			
			int result = 0;
			
			//scale is < mant for normalized numbers 
			//that start with one or more zeros, e.g. "0.045"
			int imax = Math.min(scale, mant.length);
			for (int i = 0; i< imax; ++i)
			{
				result += mant[mant.length - 1 - i] * FractionalPowers10[i];
			}
			
			return result;
		} 
		
		return -1;
	}

	
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#getIntegerPart()
	 */
	public long getIntegerPart() 
	{
		if (mType == ValidNumberType)
		{
			byte[] mant = mDecimal.getMantissa();
			int scale = mDecimal.scale();
			if (mant == null || scale > mant.length)
			{
				return 0;
			}

			long result = 0;
			int imax = mant.length - scale;
			for (int i = 0; i< imax; ++i)
			{
				result += mant[imax - 1 - i] * IntegerPowers10[i];
			}
			
			if (getSign() < 0)
			{
				result = -result;
			}
			return result;
			
		}
		else
		{
			switch (mType)
			{
			case PositiveInfinityType:
				return PositiveInfinityIntegerPart;
			case NegativeInfinityType:
				return NegativeInfinityIntegerPart;
			}	
		}
		
		return NanIntegerPart;
	}
	
	static private boolean isOverflow(BigDecimal bigDecimal)
	{
		if ((bigDecimal.getMantissa().length - bigDecimal.scale()) > 
				MaxIntDigits)
		{
			SpcfDecimalImpl.signalError(bigDecimal.signum() > 0 ?
					PositiveInfinityType : NegativeInfinityType);
			return true;
		}
		return false;
	}
	
	/** 
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#add(com.intuit.spc.foundations.portability.util.SpcfDecimal)
	 */
	public SpcfDecimal add(SpcfDecimal val) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		
		SpcfDecimalImpl rightOperand = (SpcfDecimalImpl)val.toImpl();
		if (this.mType == ValidNumberType && 
			rightOperand.mType == ValidNumberType)
		{
			BigDecimal result = this.mDecimal.add(rightOperand.mDecimal);
			if (isOverflow(result))
			{
				return (result.signum() > 0 ? PositiveInfinity : NegativeInfinity); 
			}
			else
			{
				return new SpcfDecimalImpl(result);
			}
		}
		//it's NAN is either operand is NAN or adding +infinity with -infinity
		else if (this.mType == NanType || rightOperand.mType == NanType ||
			(this.mType == PositiveInfinityType && rightOperand.mType == NegativeInfinityType) ||
			(this.mType == NegativeInfinityType && rightOperand.mType == PositiveInfinityType))
		{
			signalError(NanType);
			return Nan; 
		}
		//it's +infinity if one is +infinity
		else if (this.mType == PositiveInfinityType || 
				rightOperand.mType == PositiveInfinityType)
		{
			signalError(PositiveInfinityType);
			return PositiveInfinity;
		}
		//else it's -infinity
		signalError(NegativeInfinityType);
		return NegativeInfinity;
		
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#subtract(com.intuit.spc.foundations.portability.util.SpcfDecimal)
	 */
	public SpcfDecimal subtract(SpcfDecimal val) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		
		SpcfDecimalImpl rightOperand = (SpcfDecimalImpl)val.toImpl();
		if (this.mType == ValidNumberType && 
			rightOperand.mType == ValidNumberType)
		{
			BigDecimal result = this.mDecimal.subtract(rightOperand.mDecimal);
			if (isOverflow(result))
			{
				return (result.signum() > 0 ? PositiveInfinity : NegativeInfinity);
			}
			else
			{
				return new SpcfDecimalImpl(result);
			}
		}
		//it's NAN is either operand is NAN or adding +infinity with -infinity
		else if (this.mType == NanType || rightOperand.mType == NanType ||
			(this.mType == PositiveInfinityType && rightOperand.mType == PositiveInfinityType) ||
			(this.mType == NegativeInfinityType && rightOperand.mType == NegativeInfinityType))
		{
			signalError(NanType);
			return Nan; 
		}
		//it's +infinity if one is +infinity
		else if (this.mType == PositiveInfinityType || 
				rightOperand.mType == NegativeInfinityType)
		{
			signalError(PositiveInfinityType);
			return PositiveInfinity;
		}
		//else it's -infinity
		signalError(NegativeInfinityType);
		return NegativeInfinity;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#multiply(com.intuit.spc.foundations.portability.util.SpcfDecimal)
	 */
	public SpcfDecimal multiply(SpcfDecimal val) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		
		return multiply( val, 
				SpcfMathImpl.abs(SpcfMathImpl.min(MaxScale, this.getScale()+val.getScale())),
				SpcfRoundingType.HalfUp); 
	}

	/** 
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#multiply(SpcfDecimal, int, SpcfRoundingType)
	 */
	public SpcfDecimal multiply(SpcfDecimal val, int scale,
			SpcfRoundingType roundingType) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		SpcfParamValidator.checkIsNotNull(roundingType, "roundingType");
		if (scale < 0 || scale > MaxScale)
		{
			throw new SpcfIllegalArgumentException();
		}
		
		SpcfDecimalImpl rightOperand = (SpcfDecimalImpl)val.toImpl();
		if (this.mType == ValidNumberType && 
			rightOperand.mType == ValidNumberType)
		{
			BigDecimal result = this.mDecimal.multiply(rightOperand.mDecimal);
			if (isOverflow(result))
			{
				return (result.signum() > 0 ? PositiveInfinity : NegativeInfinity); 
			}
			else
			{
				//round and return 
				return new SpcfDecimalImpl(result.setScale(scale, 
						roundToBigDecimalType(roundingType)));
			}
		}
		//it's NAN is either operand is NAN or multiplying 0 with +/-infinity
		else if (this.mType == NanType || rightOperand.mType == NanType ||
			((this.mType == PositiveInfinityType || this.mType == NegativeInfinityType) &&
					(rightOperand.compareTo(Zero) == 0)) ||
			((rightOperand.mType == PositiveInfinityType || rightOperand.mType == NegativeInfinityType) &&
					(this.compareTo(Zero) == 0)) )

		{
			signalError(NanType);
			return Nan; 
		}
		//else it's + or - infinity
		else if (this.getSign()*val.getSign() < 0)
		{
			signalError(NegativeInfinityType);
			return NegativeInfinity;
		}
		//else
		signalError(PositiveInfinityType);
		return PositiveInfinity;
	
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#divide(com.intuit.spc.foundations.portability.util.SpcfDecimal)
	 */
	public SpcfDecimal divide(SpcfDecimal val) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		
		return dividePrivate(val, this.getScale(), SpcfRoundingType.HalfUp); 
	
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#divide(SpcfDecimal, int, SpcfRoundingType)
	 */
	public SpcfDecimal divide(SpcfDecimal val, 
			int scale, SpcfRoundingType roundingType) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		SpcfParamValidator.checkIsNotNull(roundingType, "roundingType");
		
		if (scale < 0 || scale > MaxScale)
		{
			throw new SpcfIllegalArgumentException();
		}
		return dividePrivate(val, scale, roundingType);
	}
	
	private SpcfDecimal dividePrivate(SpcfDecimal val, int scale,
			SpcfRoundingType roundingType)
	{
		SpcfDecimalImpl rightOperand = (SpcfDecimalImpl)val.toImpl();
		if (this.mType == ValidNumberType && 
			rightOperand.mType == ValidNumberType)
		{
			//divide by 0
			if (rightOperand.compareTo(Zero) == 0)
			{
				if (this.compareTo(Zero) == 0)
				{
					signalError(NanType);
					return Nan;
				}
				
				if (this.getSign() > 0)
				{
					signalError(PositiveInfinityType);
					return PositiveInfinity;
				}
				else
				{
					signalError(NegativeInfinityType);
					return NegativeInfinity;
				}
			}
			
			BigDecimal result = this.mDecimal.divide(rightOperand.mDecimal,
					scale, roundToBigDecimalType(roundingType));
			if (isOverflow(result))
			{
				return (result.signum() > 0 ? PositiveInfinity : NegativeInfinity); 
			}
			return new SpcfDecimalImpl(result);
		}
		//it's NAN is either operand is NAN or dividing infinity with infinity
		else if (this.mType == NanType || rightOperand.mType == NanType ||
			((this.mType == PositiveInfinityType || this.mType == NegativeInfinityType) &&
			((rightOperand.mType == PositiveInfinityType || rightOperand.mType == NegativeInfinityType))))

		{
			signalError(NanType);
			return Nan; 
		}
		//else dividing infinity results into infinity
		else if (this.mType == PositiveInfinityType || this.mType == NegativeInfinityType)
		{
			if (this.getSign()*val.getSign() < 0)
			{
				signalError(NegativeInfinityType);
				return NegativeInfinity; 
			}
			else if (this.getSign()*val.getSign() >0)
			{
				signalError(PositiveInfinityType);
				return PositiveInfinity; 
			}
			else //val.getSign() == 0
			{
				signalError( this.getSign() > 0 ? PositiveInfinityType : NegativeInfinityType);
				return (this.getSign() > 0 ? PositiveInfinity : NegativeInfinity);
			}
		}
		//else divide by infinity results into zero 
		//(with as many padded zero as needed
		return Zero.setScale(scale, roundingType);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#remainder(com.intuit.spc.foundations.portability.util.SpcfDecimal)
	 */
	public SpcfDecimal remainder(SpcfDecimal val) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");

		SpcfDecimalImpl rightOperand = (SpcfDecimalImpl)val.toImpl();
		if (this.mType == ValidNumberType && 
			rightOperand.mType == ValidNumberType)
		{
			//right operand is zero -> return NAN
			if (rightOperand.compareTo(Zero) == 0)
			{
				signalError(NanType);
				return Nan;
			}
			
			BigDecimal result = this.mDecimal.remainder(rightOperand.mDecimal);
			if (result.compareTo(ZeroDecimal) == 0) //adjust scale if result is zero
			{
				result = result.setScale(this.getScale(),BigDecimal.ROUND_HALF_UP);
			}
			return new SpcfDecimalImpl (result);
		}
		//if either operand is NAN or this == +/-infinity 
		//return NAN
		else if (rightOperand.mType == NanType || !this.isNumber())
		{
			//else if any op is NAN or this is +/-infinity -> return NAN
			signalError(NanType);
			return Nan;
			
		}
		//else  It returns 'this' if operand is +/-infinity 
		if (!this.isNumber())
		{
			signalError(this.mType);
		}
		return this; 
	}
	

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() 
	{
		switch (this.mType)
		{
		case ValidNumberType:
			return this.mDecimal.toString();
		case PositiveInfinityType:
			return PositiveInfinityString;
		case NegativeInfinityType:
			return NegativeInfinityString;
		default:
			return NanString;
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#compareTo(com.intuit.spc.foundations.portability.util.SpcfDecimal)
	 */
	public int compareTo(SpcfDecimal val) 
	{
		SpcfParamValidator.checkIsNotNull(val, "val");
		
		SpcfDecimalImpl operand = (SpcfDecimalImpl)val.toImpl();
		
		if (this.mType == ValidNumberType && 
			operand.mType == ValidNumberType)
		{	
			return mDecimal.compareTo(operand.mDecimal);
		}
		
		//if values are valid and 'invalid numbers' 
		//we can compare the integer values
		long leftIntegerValue = this.getIntegerPart();
		long rightIntegerValue = val.getIntegerPart();
		if (leftIntegerValue == rightIntegerValue)
		{
			return 0;
		}
		else if (leftIntegerValue > rightIntegerValue)
		{
			return 1;
		}
		//else
		return -1;
	}

    @Override
    public boolean isGreaterThan(SpcfDecimal val) {
        return compareTo(val) > 0;
    }

    @Override
    public boolean isLessThan(SpcfDecimal val) {
        return compareTo(val) < 0;
    }

    @Override
    public boolean isGreaterThanEqualTo(SpcfDecimal val) {
        return compareTo(val) >= 0;
    }

    @Override
    public boolean isLessThanEqualTo(SpcfDecimal val) {
        return compareTo(val) <= 0;
    }

    @Override
    public boolean isZero() {
        return this.compareTo(Zero) == 0;
    }

    /**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#compareTo(java.lang.Object)
	 */
	public int compareTo(Object val) 
	{
		try
		{
			return compareTo((SpcfDecimal)val);
		}
		catch (ClassCastException ex)
		{
			throw new SpcfClassCastException(ex);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#equals(java.lang.Object)
	 */
	public boolean equals(Object x) 
	{
		if (x == null || !(x instanceof SpcfDecimal))
		{
			return false;
		}
		
		SpcfDecimal o = (SpcfDecimal)x;
		SpcfDecimalImpl operand = (SpcfDecimalImpl)o.toImpl();
		
		if (this.mType == ValidNumberType && 
			operand.mType == ValidNumberType)
		{	
			return mDecimal.equals(operand.mDecimal);
		}
		
		//if values are valid and 'invalid numbers' 
		//we can compare the integer values
		long leftIntegerValue = this.getIntegerPart();
		long rightIntegerValue = operand.getIntegerPart();
		if (leftIntegerValue == rightIntegerValue)
		{
			return true;
		}
		
		return false;	
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#hashCode()
	 */
	public int hashCode()
	{
		if (this.mType == ValidNumberType)
		{
			return mDecimal.hashCode();
		}
		return mType; 
	}


	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#abs()
	 */
	public SpcfDecimal abs() 
	{
		if (mType == ValidNumberType)
		{
			if (mDecimal.signum() < 0)
			{
				return new SpcfDecimalImpl(mDecimal.abs());
			}
		} 
		else
		{
			switch (mType)
			{
			case NegativeInfinityType:
				return PositiveInfinity;
			}
		}
		return this;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isPositiveInfinity()
	 */
	public boolean isPositiveInfinity() 
	{
		return (this.mType == PositiveInfinityType);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isNegativeInfinity()
	 */
	public boolean isNegativeInfinity() 
	{
		return (this.mType == NegativeInfinityType);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isNAN()
	 */
	public boolean isNAN() 
	{
		return (this.mType == NanType);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#isNumber()
	 */
	public boolean isNumber() 
	{
		return (this.mType == ValidNumberType);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfDecimal#midPoint(long)
	 */
	public SpcfDecimal midPoint (long intervalSize)
	{

		if (this.isNumber())
		{
			if (intervalSize == 0)
			{
				return this;
			}
			
			SpcfDecimal absInt = new SpcfDecimalImpl(new BigDecimal(Long.toString(intervalSize)));
			SpcfDecimal absThis = this.abs();

			SpcfDecimal low = absThis.divide(absInt, 0, SpcfDecimal.SpcfRoundingType.Floor);
			low = low.multiply(absInt);
			
			//midpoint
			SpcfDecimal middle = 
		    	absInt.divide(Two, getScale(), SpcfDecimal.SpcfRoundingType.Ceiling).add(low); 
			if (this.getSign() < 0)
			{
				middle = middle.negate();
			}
			return middle;
		}
		//if either operand is NAN -> NAN
		else if (this.isNAN())
		{
			return Nan;
		}
		//else 'this' is +/- infinity
		return this;
	}
	
	/**
	 * Gets the serialized representation of the decimal value of this object. 
	 * This property is added to support xml serialization. 
	 * @return serialized form of the value represented by this object.
	 */
	public String getSerialized()
	{	
		return toString();
	}
	
	/** 
	 * Initializes this object from its serialized representation. 
	 * This property is added to support xml serialization. 
	 * 
	 * @param decimalString - decimal string representation
	 * @throws SpcfArgumentNullException if argument is null
	 * @throws SpcfIllegalArgumentException if argument contains invalid chars 
	 */
	public void setSerialized(String decimalString)
	{	
		init(decimalString);
	}
	
	/**
	* Returns the encapsulated third party runtime object 
	* 
	* @return a java.math.BigDecimal instance
	*/
	public java.math.BigDecimal toSpecific()
	{
		return mDecimal.toBigDecimal();
	}
	
    /**
     * Return the platform specific impl
     */ 
    public Object toImpl()
    {
        return this;
    }
    
    /**
     * return the platform specific value for the 
     */
	protected long doGetPlatformSpecificNegativeInfinityIntegerPart()
	{
		return 0xfff0000000000000L;
	}
	
	private void init(String decimalString)
	{
		SpcfParamValidator.checkIsNotNull(decimalString, "decimalString");
		
		//no exponential notation please
		if (decimalString.indexOf('E') >= 0 ||
		    decimalString.indexOf('e') >= 0)
		{
			throw new SpcfIllegalArgumentException("E is not allowed");
		}
			
		try
		{
			this.mDecimal = new BigDecimal(decimalString);
		}
		catch (NumberFormatException ex)
		{
			if (decimalString.equals(PositiveInfinityString))
			{
				mType = PositiveInfinityType;
			}
			else if (decimalString.equals(NegativeInfinityString))
			{
				mType = NegativeInfinityType;
			}  
			else if (decimalString.equals(NanString))
			{
				mType = NanType;
			}
			else
			{
				throw new SpcfIllegalArgumentException(ex);
			}
			signalError(mType);
			return;
		}
		
		//round to MaxScale
		if (mDecimal.scale() > MaxScale)
		{
			mDecimal = mDecimal.setScale(MaxScale, BigDecimal.ROUND_HALF_UP);
		}
		
		//check if overflow
		if (isOverflow(mDecimal))
		{
			mType = (mDecimal.signum() < 0) ? NegativeInfinityType : PositiveInfinityType;
			mDecimal = null;
		}
	}
}

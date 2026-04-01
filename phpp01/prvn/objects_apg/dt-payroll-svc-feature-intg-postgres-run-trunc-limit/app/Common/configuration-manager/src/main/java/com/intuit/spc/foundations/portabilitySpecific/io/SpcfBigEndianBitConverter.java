package com.intuit.spc.foundations.portabilitySpecific.io;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.io.*;

public strictfp class SpcfBigEndianBitConverter extends SpcfBitConverter
{
	public SpcfBigEndianBitConverter()
	{
		super();
	}

	
	/**
	 * @inheritDoc
	 */
	@Override
	public SpcfByteOrderEnum getByteOrder()
	{
		return SpcfByteOrderEnum.BigEndian;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public boolean toBoolean(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 1);
		return (buffer[0] != 0);
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public char toChar(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 2);
		return (char)((buffer[0] << 8) | (buffer[1] & 0xff));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public double toDouble(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 8);
		return Double.longBitsToDouble(toLong(buffer));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public float toFloat(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 4);
		return Float.intBitsToFloat(toInt(buffer));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public int toInt(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 4);
		return (((buffer[0] & 0xff) << 24) | 
				((buffer[1] & 0xff) << 16) | 
				((buffer[2] & 0xff) <<  8) | 
				((buffer[3] & 0xff)));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public long toLong(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 8);
		return (((long)(buffer[0] & 0xff) << 56) | 
				((long)(buffer[1] & 0xff) << 48) | 
				((long)(buffer[2] & 0xff) << 40) | 
				((long)(buffer[3] & 0xff) << 32) | 
				((long)(buffer[4] & 0xff) << 24) | 
				((long)(buffer[5] & 0xff) << 16) | 
				((long)(buffer[6] & 0xff) <<  8) | 
				((long)(buffer[7] & 0xff)));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public short toShort(byte[] buffer)
	{
		SpcfParamValidator.checkArrayParams(buffer, 0, 2);
		return (short)((buffer[0] << 8) | (buffer[1] & 0xff));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromBoolean(boolean b)
	{
		byte[] buffer = new byte[1];
		buffer[0] = (b ? (byte)1 : (byte)0);
		return buffer;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromChar(char c)
	{
		byte[] buffer = new byte[2];
		buffer[0] = (byte)(0xff & (c >> 8));
		buffer[1] = (byte)(0xff & c);
		return buffer;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromDouble(double d)
	{
		return fromLong(Double.doubleToLongBits(d));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromFloat(float f)
	{
		return fromInt(Float.floatToIntBits(f));
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromInt(int i)
	{
		byte[] buffer = new byte[4];
		buffer[0] = (byte)(0xff & (i >> 24));
		buffer[1] = (byte)(0xff & (i >> 16));
		buffer[2] = (byte)(0xff & (i >> 8));
		buffer[3] = (byte)(0xff &  i);
		return buffer;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromLong(long l)
	{
		byte[] buffer = new byte[8];
		buffer[0] = (byte)(0xff & (l >> 56));
		buffer[1] = (byte)(0xff & (l >> 48));
		buffer[2] = (byte)(0xff & (l >> 40));
		buffer[3] = (byte)(0xff & (l >> 32));
		buffer[4] = (byte)(0xff & (l >> 24));
		buffer[5] = (byte)(0xff & (l >> 16));
		buffer[6] = (byte)(0xff & (l >>  8));
		buffer[7] = (byte)(0xff &  l);
		return buffer;
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public byte[] fromShort(short s)
	{
		byte[] buffer = new byte[2];
		buffer[0] = (byte)(0xff & (s >> 8));
		buffer[1] = (byte)(0xff &  s);
		return buffer;
	}
}

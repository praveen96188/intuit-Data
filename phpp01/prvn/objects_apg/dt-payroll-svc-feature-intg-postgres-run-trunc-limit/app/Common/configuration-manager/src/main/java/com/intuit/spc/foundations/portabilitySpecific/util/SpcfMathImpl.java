package com.intuit.spc.foundations.portabilitySpecific.util;

import java.util.Random;
import com.intuit.spc.foundations.portability.util.*;

/**
* A platform specific implementation of SpcfMath
*/
public strictfp class SpcfMathImpl extends SpcfMath
{

	private static final long serialVersionUID = -8204746625903068067L;
	
	/**
	 * 
	 *  We are keeping this value around so that we truely receive random
	 *  numbers since we will be getting the next random number for each
	 *  get random number call because we are not initializing the random
	 *  see for every call.
	 */
	private static Random rand = null;

	/**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#abs(double)
     */	
    protected double getAbs(double a)
    {
    	return StrictMath.abs(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#abs(float)
     */	
    protected float getAbs(float a)
    {
    	return StrictMath.abs(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#abs(int)
     */	
    protected int getAbs(int a)
    {
    	return StrictMath.abs(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#abs(long)
     */	
    protected long getAbs(long a)
    {
    	return StrictMath.abs(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#acos(double)
     */	
    protected double getAcos(double a)
    {
    	return StrictMath.acos(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#asin(double)
     */	
    protected double getAsin(double a)
    {
    	return StrictMath.asin(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#atan(double)
     */	
    protected double getAtan(double a)
    {
    	return StrictMath.atan(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#atan2(double, double)
     */	
    protected double getAtan2(double y, double x)
    {
    	return StrictMath.atan2(y, x);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#ceil(double)
     */	
    protected double getCeil(double a)
    {
    	return StrictMath.ceil(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#cos(double)
     */	
    protected double getCos(double a)
    {
    	return StrictMath.cos(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#E
     */	
    protected double doGetE()
	{
    	return StrictMath.E;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#exp(double)
     */	
    protected double getExp(double a)
    {
    	return StrictMath.exp(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#floor(double)
     */	
    protected double getFloor(double a)
    {
    	return StrictMath.floor(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#getIEEERemainder(double, double)
     */	
    protected double doGetIEEERemainder(double f1, double f2)
    {
    	return StrictMath.IEEEremainder(f1, f2);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#doubleToLongBits(double)
     */	
    protected long doDoubleToLongBits(double a)
    { 
    	return Double.doubleToLongBits(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#longBitsToDouble(long)
     */	
    protected double doLongBitsToDouble(long l)
    { 
    	return Double.longBitsToDouble(l);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#toStringUnrounded(double)
     */	
    protected String doToStringUnrounded(Double a)
    {
    	return a.toString();
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#log(double)
     */	
    protected double getLog(double a)
    {
    	return StrictMath.log(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#max(double, double)
     */	
    protected double getMax(double a, double b)
    {
    	return StrictMath.max(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#max(float, float)
     */	
    protected float getMax(float a, float b)
    {
    	return StrictMath.max(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#max(int, int)
     */	
    protected int getMax(int a, int b)
    {
    	return StrictMath.max(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#max(long, long)
     */	
    protected long getMax(long a, long b)
    {
    	return StrictMath.max(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#min(double, double)
     */	
    protected double getMin(double a, double b)
    {
    	return StrictMath.min(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#min(float, float)
     */	
    protected float getMin(float a, float b)
    {
    	return StrictMath.min(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#min(int, int)
     */	
    protected int getMin(int a, int b)
    {
    	return StrictMath.min(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#min(long, long)
     */	
    protected long getMin(long a, long b)
    {
    	return StrictMath.min(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#Pi
     */	
    protected double doGetPI()
    {
    	return StrictMath.PI;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#pow(double, double)
     */	
    protected double getPow(double a, double b)
    {
    	// to be consistant with dotnet, se want to return NaN whenever
    	// there is a computation using Nan.
		if (Double.isNaN(a) || Double.isNaN(b))
		{
			return Double.NaN;
		}
    	return StrictMath.pow(a, b);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#round(double)
     */	
    protected long getRound(double a)
    {
    	return StrictMath.round(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#sin(double)
     */	
    protected double getSin(double a)
    {
    	return StrictMath.sin(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#sqrt(double)
     */	
    protected double getSqrt(double a)
    {
    	return StrictMath.sqrt(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#tan(double)
     */	
    protected double getTan(double a)
    {
    	return StrictMath.tan(a);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfMath#randomInt(int, int)
     */
    protected int getRandomInt(int lbound, int ubound)
    {
    	if (rand == null)
    	{
    		rand = new Random(System.currentTimeMillis());
    	}
    	return (rand.nextInt(ubound - lbound) + lbound); 
    }
    
}

package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;

/**
 * We are using this class to provide the non-portable code necessary for Portability classes.
 * We have provided this class to hold the non-portable code for portability classes so that 
 * the portability classes can be translated.
 */
@SpcfNonPortableClass
class SpcfPortabilityUtilNT
{
	/**
	 * protected constructor
	 */
	protected SpcfPortabilityUtilNT()
	{
	}

	/**
	 * SpcfFactory cannot call the reflection methods on SpcfClass because SpcfClass
	 * will need to call SpcfFactory which creates an endless loop.
	 * This is the method which SpcfFactory will call to initialize it's static variable
	 * holding the singleton instance of it's SpcfFactoryImpl.
	 */
    static void initializeFactory()
    {
        try
        {
        	com.intuit.spc.foundations.portabilitySpecific.SpcfFactoryImpl.initialize();
        	
//            Class factoryImpl;
//            factoryImpl = Class.forName("com.intuit.spc.foundations.portabilitySpecific.SpcfFactoryImpl");
//            Method initializeMethod = factoryImpl.getMethod("initialize");
//            initializeMethod.invoke(null);

        }
        catch(Exception e)
        {
            throw new SpcfInvalidFactoryStateException(e);
        }
    }

	/**
	 *  Returns stack trace associated with the exception
	 *  @return returns the stack trace information from the exception as a string
	 *  @param e the exception to get the stack trace information about
	 */
	static String getStackTraceInfo(Exception e)
	{
		if (e == null)
		{
			return "";
		}
		
		// Create StringWriter and decorate with a PrintWriter
		// Then fill with the stack trace
		java.io.StringWriter strWriter = new java.io.StringWriter();
		e.printStackTrace( new java.io.PrintWriter(strWriter) );
		return strWriter.toString();
	}

}

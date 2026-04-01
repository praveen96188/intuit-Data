/**
 * --------------------------------------------------------------------------
 * Copyright (c) 2008 Intuit, Inc. All rights reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * --------------------------------------------------------------------------
 *
 * --------------------------------------------------------------------------
 *
 * Author	PSP CodeGen
 * Model Version	1.0
 *
 * --------------------------------------------------------------------------
 */

package com.intuit.sbd.payroll.psp.domainsecondary.entitybase;

import com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;
import com.intuit.sbd.payroll.psp.util.StringFormatter;

public class BaseSAPMethodCall extends com.intuit.sbd.payroll.psp.DomainEntity
{
    //
    // ScreenPath
    //
    private String mScreenPath = null;

    public void setScreenPath(String pScreenPath)
    {
        pScreenPath = StringFormatter.removeNonAsciiCharacters(pScreenPath);
        if (pScreenPath != null && pScreenPath.length() > 400)
        {
            String argStr = pScreenPath.length() > 30 ? pScreenPath.substring(0, 30) + "..." : pScreenPath;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (400) for pScreenPath");
        }

    	if (pScreenPath != null && pScreenPath.length() == 0)
    	{
    		pScreenPath = null;
    	}

        mScreenPath = pScreenPath;
    }

    public String getScreenPath()
    {
    	return mScreenPath;
    }

    //
    // ServiceName
    //
    private String mServiceName = null;

    public void setServiceName(String pServiceName)
    {
    	if (pServiceName != null && pServiceName.length() == 0)
    	{
    		pServiceName = null;
    	}

        mServiceName = pServiceName;
    }

    public String getServiceName()
    {
    	return mServiceName;
    }

    //
    // MethodName
    //
    private String mMethodName = null;

    public void setMethodName(String pMethodName)
    {
    	if (pMethodName != null && pMethodName.length() == 0)
    	{
    		pMethodName = null;
    	}

        mMethodName = pMethodName;
    }

    public String getMethodName()
    {
    	return mMethodName;
    }

    //
    // Parameters
    //
    private String mParameters = null;

    public void setParameters(String pParameters)
    {
        pParameters = StringFormatter.removeNonAsciiCharacters(pParameters);
        if (pParameters != null && pParameters.length() > 4000)
        {
            String argStr = pParameters.length() > 30 ? pParameters.substring(0, 30) + "..." : pParameters;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (4000) for pParameters");
        }

    	if (pParameters != null && pParameters.length() == 0)
    	{
    		pParameters = null;
    	}

        mParameters = pParameters;
    }

    public String getParameters()
    {
    	return mParameters;
    }

    //
    // ResultSize
    //
    private long mResultSize = 0L;

    public void setResultSize(long pResultSize)
    {
        mResultSize = pResultSize;
    }

    public long getResultSize()
    {
    	return mResultSize;
    }

    //
    // ElapsedMillis
    //
    private long mElapsedMillis = 0L;

    public void setElapsedMillis(long pElapsedMillis)
    {
        mElapsedMillis = pElapsedMillis;
    }

    public long getElapsedMillis()
    {
    	return mElapsedMillis;
    }

    //
    // ExceptionTrace
    //
    private String mExceptionTrace = null;

    public void setExceptionTrace(String pExceptionTrace)
    {
        pExceptionTrace = StringFormatter.removeNonAsciiCharacters(pExceptionTrace);
        if (pExceptionTrace != null && pExceptionTrace.length() > 4000)
        {
            String argStr = pExceptionTrace.length() > 30 ? pExceptionTrace.substring(0, 30) + "..." : pExceptionTrace;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (4000) for pExceptionTrace");
        }

    	if (pExceptionTrace != null && pExceptionTrace.length() == 0)
    	{
    		pExceptionTrace = null;
    	}

        mExceptionTrace = pExceptionTrace;
    }

    public String getExceptionTrace()
    {
    	return mExceptionTrace;
    }

    //
    // SecurityPrincipal
    //
    private String mSecurityPrincipal = null;

    public void setSecurityPrincipal(String pSecurityPrincipal)
    {
    	if (pSecurityPrincipal != null && pSecurityPrincipal.length() == 0)
    	{
    		pSecurityPrincipal = null;
    	}

        mSecurityPrincipal = pSecurityPrincipal;
    }

    public String getSecurityPrincipal()
    {
    	return mSecurityPrincipal;
    }

    //
    // SessionId
    //
    private String mSessionId = null;

    public void setSessionId(String pSessionId)
    {
    	if (pSessionId != null && pSessionId.length() == 0)
    	{
    		pSessionId = null;
    	}

        mSessionId = pSessionId;
    }

    public String getSessionId()
    {
    	return mSessionId;
    }

    //
    // Host
    //
    private String mHost = null;

    public void setHost(String pHost)
    {
        pHost = StringFormatter.removeNonAsciiCharacters(pHost);
        if (pHost != null && pHost.length() > 200)
        {
            String argStr = pHost.length() > 30 ? pHost.substring(0, 30) + "..." : pHost;
            throw new RuntimeException("String '" + argStr + "' is longer than allowed (200) for pHost");
        }

    	if (pHost != null && pHost.length() == 0)
    	{
    		pHost = null;
    	}

        mHost = pHost;
    }

    public String getHost()
    {
    	return mHost;
    }



    // PSP query support
    public static final ScalarProperty<SAPMethodCall, String> ScreenPath() {return new ScalarProperty<SAPMethodCall, String>(null, "ScreenPath");};
    public static final ScalarProperty<SAPMethodCall, String> ServiceName() {return new ScalarProperty<SAPMethodCall, String>(null, "ServiceName");};
    public static final ScalarProperty<SAPMethodCall, String> MethodName() {return new ScalarProperty<SAPMethodCall, String>(null, "MethodName");};
    public static final ScalarProperty<SAPMethodCall, String> Parameters() {return new ScalarProperty<SAPMethodCall, String>(null, "Parameters");};
    public static final ScalarProperty<SAPMethodCall, Long> ResultSize() {return new ScalarProperty<SAPMethodCall, Long>(null, "ResultSize");};
    public static final ScalarProperty<SAPMethodCall, Long> ElapsedMillis() {return new ScalarProperty<SAPMethodCall, Long>(null, "ElapsedMillis");};
    public static final ScalarProperty<SAPMethodCall, String> ExceptionTrace() {return new ScalarProperty<SAPMethodCall, String>(null, "ExceptionTrace");};
    public static final ScalarProperty<SAPMethodCall, String> SecurityPrincipal() {return new ScalarProperty<SAPMethodCall, String>(null, "SecurityPrincipal");};
    public static final ScalarProperty<SAPMethodCall, String> SessionId() {return new ScalarProperty<SAPMethodCall, String>(null, "SessionId");};
    public static final ScalarProperty<SAPMethodCall, String> Host() {return new ScalarProperty<SAPMethodCall, String>(null, "Host");};
}
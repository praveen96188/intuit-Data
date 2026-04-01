package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.cache.*;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * Hand-written business logic
 */
public class SourcePayrollParameter extends BaseSourcePayrollParameter {
    private static SpcfLogger logger = SpcfLogManager.getLogger(SourcePayrollParameter.class);

    private static BaseProcessCache<NaturalKey,SourcePayrollParameter> processCache;

    private static void initializeProcessCache() {
        if (processCache == null) {
            synchronized (SourcePayrollParameter.class) {
                if (processCache == null) {
                    try {
                        processCache = new SessionCacheWrapper<NaturalKey,SourcePayrollParameter>(SourcePayrollParameter.class);
                        switch (BaseProcessCache.getProcessCacheType()) {
                            case DirtyChecking:
                                processCache = new DirtyCheckProcessCache<NaturalKey,SourcePayrollParameter>(
                                                        SourcePayrollParameter.class,
                                                        new Query<SourcePayrollParameter>(),
                                                        SourceSystemCd(), ParameterCd());
                                break;
                            case Expiring:
                                processCache = new ExpiringProcessCache<NaturalKey,SourcePayrollParameter>(
                                                        SourcePayrollParameter.class,
                                                        SystemParameter.Code.SOURCE_PAYROLL_PARAMETER_REFRESH_INTERVAL,
                                                        new Query<SourcePayrollParameter>(),
                                                        SourceSystemCd(), ParameterCd());
                                break;
                            default:
                                processCache = new SessionCacheWrapper<NaturalKey,SourcePayrollParameter>(SourcePayrollParameter.class);
                        }
                    } catch (Throwable t) {
                        logger.error("error initializing source payroll parameter caching", t);
                    }
                    logger.info("system parameter cache: " + processCache);
                }
            }
        }
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(SourcePayrollParameter.class, getSourceSystemCd(), getParameterCd());
    }

    public static BaseProcessCache<NaturalKey,SourcePayrollParameter> getProcessCache() {
        if (processCache == null)
            initializeProcessCache();
        return processCache;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static SourcePayrollParameter findSourcePayrollParameter(SourceSystemCode pSourceSystemCd, String pParamCode) {
        return findSourcePayrollParameter(pSourceSystemCd, SourcePayrollParameterCode.valueOf(pParamCode));
    }

    public static SourcePayrollParameter findSourcePayrollParameter(SourceSystemCode pSourceSystemCd,
                                                                    SourcePayrollParameterCode pParamCode) {
        NaturalKey naturalKey = new NaturalKey(SourcePayrollParameter.class, pSourceSystemCd, pParamCode);

        boolean manageTransaction = false;
        if (!Application.hasActiveTransaction()) {
            manageTransaction = true;
            Application.beginUnitOfWork();
        }

        SourcePayrollParameter returnVal = getProcessCache().get(naturalKey);

        if (manageTransaction) {
            Application.rollbackUnitOfWork();
        }

        return returnVal;
    }

    public static String findValue(SourceSystemCode pSourceSystemCd, SourcePayrollParameterCode pParamCode) {
        return findStringValue(pSourceSystemCd, pParamCode);
    }

    public static String findValue(SourceSystemCode pSourceSystemCd, String pParamCode) {
        return findValue(pSourceSystemCd, SourcePayrollParameterCode.valueOf(pParamCode));
    }

    public static String findStringValue(SourceSystemCode pSourceSystemCd, SourcePayrollParameterCode pParamCode) {
        SourcePayrollParameter sourcePayrollParameter = findSourcePayrollParameter(pSourceSystemCd, pParamCode);
        if (sourcePayrollParameter == null) {
            throw new RuntimeException("could not locate system parameter for value: " + pSourceSystemCd + ":" + pParamCode);
        }

        return sourcePayrollParameter.getParameterValue();
    }

    public static String findStringValue(SourceSystemCode pSourceSystemCd, String pParamCode) {
        return findStringValue(pSourceSystemCd, SourcePayrollParameterCode.valueOf(pParamCode));
    }

    public static int findIntValue(SourceSystemCode pSourceSystemCd, SourcePayrollParameterCode pParamCode) {
        SourcePayrollParameter sourcePayrollParameter = findSourcePayrollParameter(pSourceSystemCd, pParamCode);
        if (sourcePayrollParameter == null) {
            throw new RuntimeException("could not locate system parameter for value: " + pSourceSystemCd + ":" + pParamCode);
        }

        String strValue = sourcePayrollParameter.getParameterValue();
        if (strValue == null || strValue.trim().length() == 0) {
            throw new RuntimeException("no value found for system parameter: " + pSourceSystemCd + ":" + pParamCode);
        }

        int value = Integer.MIN_VALUE;
        try {
            value = Integer.parseInt(strValue);
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("cannot convert source payroll parameter value to int: " + value);
        }

        return value;
    }

    public static int findIntValue(SourceSystemCode pSourceSystemCd, String pParamCode) {
        return findIntValue(pSourceSystemCd, SourcePayrollParameterCode.valueOf(pParamCode));
    }

    public static double findDoubleValue(SourceSystemCode pSourceSystemCd, SourcePayrollParameterCode pParamCode) {
        SourcePayrollParameter sourcePayrollParameter = findSourcePayrollParameter(pSourceSystemCd, pParamCode);
        if (sourcePayrollParameter == null) {
            throw new RuntimeException("could not locate system parameter for value: " + pSourceSystemCd + ":" + pParamCode);
        }

        String strValue = sourcePayrollParameter.getParameterValue();
        if (strValue == null || strValue.trim().length() == 0) {
            throw new RuntimeException("no value found for system parameter: " + pSourceSystemCd + ":" + pParamCode);
        }

        double value = Double.MIN_VALUE;
        try {
            value = Double.parseDouble(strValue);
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("cannot convert source payroll parameter value to double: " + value);
        }

        return value;
    }

    public static double findDoubleValue(SourceSystemCode pSourceSystemCd, String pParamCode) {
        return findDoubleValue(pSourceSystemCd, SourcePayrollParameterCode.valueOf(pParamCode));
    }

    public static SpcfMoney findMoneyValue(SourceSystemCode pSourceSystemCd, SourcePayrollParameterCode pParamCode) {
        SourcePayrollParameter sourcePayrollParameter = findSourcePayrollParameter(pSourceSystemCd, pParamCode);
        if (sourcePayrollParameter == null) {
            throw new RuntimeException("could not locate system parameter for value: " + pSourceSystemCd + ":" + pParamCode);
        }

        String strValue = sourcePayrollParameter.getParameterValue();
        if (strValue == null || strValue.trim().length() == 0) {
            throw new RuntimeException("no value found for system parameter: " + pSourceSystemCd + ":" + pParamCode);
        }

        SpcfMoney value = null;
        try {
            value = new SpcfMoney(strValue);
        }
        catch (NumberFormatException e) {
            throw new RuntimeException("cannot convert source payroll parameter value to double: " + value);
        }

        return value;
    }

    public static SpcfMoney findMoneyValue(SourceSystemCode pSourceSystemCd, String pParamCode) {
        return findMoneyValue(pSourceSystemCd, SourcePayrollParameterCode.valueOf(pParamCode));
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public SourcePayrollParameter()
	{
		super();
	}



}

package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.cache.DirtyCheckProcessCache;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class SystemParameterTestUtils {
    private static final Map<SystemParameter.Code, String> originalSystemParameterValues = new HashMap<>();

    public static Logger logger = LoggerFactory.getLogger(SystemParameterTestUtils.class);

    private SystemParameterTestUtils() {
    }

    public static void updateAndSavePrevious(SystemParameter.Code pParamCode, String pNewValue) {
        originalSystemParameterValues.computeIfAbsent(pParamCode, x -> SystemParameter.findStringValue(pParamCode, null));
        updateSystemParameter(pParamCode, pNewValue);
    }

    public static void restoreChangedSystemParameters() {
        for (Map.Entry<SystemParameter.Code, String> originalParam : originalSystemParameterValues.entrySet()) {
            updateSystemParameter(originalParam.getKey(), originalParam.getValue());
        }
        originalSystemParameterValues.clear();
    }

    private static void updateSystemParameter(SystemParameter.Code paramCode, String newValue) {
        try {
            Application.beginUnitOfWork();
            SystemParameter systemParameter = SystemParameter.findSystemParameter(paramCode);
            if (systemParameter == null) {
                systemParameter = new SystemParameter();
                systemParameter.setSystemParameterCd(paramCode.name());
                systemParameter.setSystemParameterDescription("Desc");
                systemParameter.setSystemParameterOrg("PSP");
                systemParameter.setSystemParameterValue(newValue);
                Application.save(systemParameter);
                DirtyCheckProcessCache.updateDBCacheTokenValue();
            } else {
                SystemParameter.update(paramCode, newValue);
            }
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
}

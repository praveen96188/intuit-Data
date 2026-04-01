package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.cache.DirtyCheckProcessCache;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * This core process allows for the udpate of the SYSTEM PARAMETER VALUE and shall
 * take only two values, the SYSTEM_PARAMETER_CD and SYSTEM_PARAMETER_VALUE
 */
public class UpdateSystemParameterCore extends Process implements IProcess {

    private String systemParameterCd;
    private String systemParameterValue;

    private SystemParameter systemParameter;    

    public UpdateSystemParameterCore(SystemParameter.Code pSystemParameterCd, String pSystemParameterValue) {
        if (pSystemParameterCd != null)
            systemParameterCd = pSystemParameterCd.name();
        systemParameterValue = pSystemParameterValue;
    }

    public UpdateSystemParameterCore(String pSystemParameterCd, String pSystemParameterValue) {
        systemParameterCd = pSystemParameterCd;
        systemParameterValue = pSystemParameterValue;
    }

    /**
     *
     */
    public ProcessResult validate() {
        ProcessResult processResult = new ProcessResult();

        if (systemParameterCd == null || !(Validator.isValidLength(systemParameterCd, 1, 400))) {
            processResult.getMessages().InvalidValue(EntityName.SystemParameter, "", "SystemParameterCd");
        }

        if (systemParameterValue == null || !(Validator.isValidLength(systemParameterValue, 1, 400))) {
            processResult.getMessages().InvalidValue(EntityName.SystemParameter, systemParameterValue, "System Parameter Value");
        }

        // will throw exception if parameter does not exist --> change to process result?
        systemParameter = SystemParameter.findSystemParameter(systemParameterCd);
        return processResult;
    }

    /**
     * Processing step.
     */
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        //update the _cached_ object
        systemParameter.setSystemParameterValue(systemParameterValue);

        //we must also get the persistent object
        systemParameter = Application.findById(SystemParameter.class, systemParameter.getId());

        //and update that
        systemParameter.setSystemParameterValue(systemParameterValue);

        Application.save(systemParameter);

        //update token so cache will be refreshed
        DirtyCheckProcessCache.updateDBCacheTokenValue();

        return processResult;
    }

    /**
     * Obtains the system parameter
     *
     * @return System Parameter
     */
    public SystemParameter getSystemParameter() {
        return systemParameter;
    }
}

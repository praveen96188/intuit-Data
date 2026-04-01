package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.DDAutoLimitIncreaseTierDTO;
import com.intuit.sbd.payroll.psp.domain.LimitRule;
import com.intuit.sbd.payroll.psp.domain.LimitValue;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: rnorian
 * Date: Sep 4, 2009
 * Time: 2:25:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateDDAutoLimitIncreaseTiersCore extends Process implements IProcess {
    private String mLimitRuleId;
    private DDAutoLimitIncreaseTierDTO[] mTiers;
    private LimitRule mLimitRule;

    public UpdateDDAutoLimitIncreaseTiersCore(String pLimitRuleId, DDAutoLimitIncreaseTierDTO[] pTiers) {
        mLimitRuleId = pLimitRuleId;
        this.mTiers = pTiers;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mTiers == null || mTiers.length == 0) {
            validationResult.getMessages().InvalidArgument(EntityName.DDAutoLimitTier, mLimitRuleId, "expected at least 1 tier.");
        }
                
        mLimitRule = Application.findById(LimitRule.class, SpcfUniqueId.createInstance(mLimitRuleId));
        if(mLimitRule == null) {
            validationResult.getMessages().InvalidArgument(EntityName.DDAutoLimitTier, mLimitRuleId, "Limit rule does not exist.");
        }

        for (DDAutoLimitIncreaseTierDTO autoLimitIncreaseTierDTO : mTiers) {
            validationResult.merge( autoLimitIncreaseTierDTO.validate() );
        }

        return validationResult;
    }

    public ProcessResult process() {
        for (DDAutoLimitIncreaseTierDTO dto : mTiers) {
            for (LimitValue limitValue : mLimitRule.getLimitValueCollection().find(LimitValue.Tier().equalTo(Integer.parseInt(dto.getLevel())))) {
                switch (limitValue.getName()) {
                    case AutoLimitIncreaseMaxCompanyLimit:
                        limitValue.setValue(dto.getCompanyCap());
                        Application.save(limitValue);
                        break;
                    case AutoLimitIncreaseMaxEmployeeLimit:
                        limitValue.setValue(dto.getEmployeeCap());
                        Application.save(limitValue);
                        break;
                    case AutoLimitIncreaseMinEarliestPayrollRunDays:
                        limitValue.setValue(dto.getDaysSinceFirstPayroll());
                        Application.save(limitValue);
                        break;
                    case AutoLimitIncreaseMinPayrolls:
                        limitValue.setValue(dto.getPayrollsRun());
                        Application.save(limitValue);
                        break;
                    case AutoLimitIncreaseIncreaseMultiplier:
                        limitValue.setValue(dto.getIncreaseMultiplier());
                        Application.save(limitValue);
                        break;
                }
            }
        }

        return new ProcessResult();
    }
}

package com.intuit.sbd.payroll.psp.mapper.guideline401k.payslips;

import com.intuit.sbd.payroll.psp.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.domain.Deduction;
import com.intuit.sbd.payroll.psp.domain.PaycheckStatusCode;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.common.Amount;
import com.intuit.v4.payroll.payslip.EmployeePayslip;
import org.apache.commons.lang3.shaded.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Objects;

/**
 * mapping psp deductions to v4 deduction - matching EmployeE deduction items
 */
@Component
public class PspDeductionsToV4DeductionTypeMapper extends BeanMapper<Deduction, EmployeePayslip.DeductionType> {

    private final Amount zeroAmount = new Amount(BigInteger.ZERO);

    @Override
    public EmployeePayslip.DeductionType mapToTarget(Deduction deduction, Class<EmployeePayslip.DeductionType> t) {
        String description = deduction.getCompanyPayrollItem().getSourceDescription();

        if(!isDeductionType(description)) {
            return null;
        }
        EmployeePayslip.DeductionType deductionType = new EmployeePayslip.DeductionType();

        switch(description){
            case Guideline401kConstants.GUIDELINE_401K_LOAN_EE:
                deductionType.setType(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_LOAN);
                deductionType.setDescription(Guideline401kConstants.GUIDELINE_401K_LOAN);
                break;
            case Guideline401kConstants.GUIDELINE_ROTH_401K_EE:
                deductionType.setType(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_ROTH401K);
                deductionType.setDescription(Guideline401kConstants.GUIDELINE_ROTH_401K);
                break;
            case Guideline401kConstants.GUIDELINE_TRADITIONAL_401K_EE:
                deductionType.setType(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);
                deductionType.setDescription(Guideline401kConstants.GUIDELINE_TRADITIONAL_401K);
                break;
        }
        if(deduction.getPaycheck().getStatus() == PaycheckStatusCode.Inactive ||
                deduction.getPaycheck().getStatus() == PaycheckStatusCode.Deleted){
            // in case the paycheck is Inactive (voided), amount is set to 0.
            deductionType.setAmount(zeroAmount);
            deductionType.setYtdAmount(zeroAmount);
            return deductionType;
        }
        deductionType.setAmount(Objects.nonNull(deduction.getDeductionAmount()) ?
                new Amount(deduction.getDeductionAmount().toString()) : null);
        deductionType.setYtdAmount(Objects.nonNull(deduction.getDeductionYTDAmount()) ?
                new Amount(deduction.getDeductionYTDAmount().toString()) : null);
        return deductionType;
    }

    private boolean isDeductionType(String description) {
        return StringUtils.equalsIgnoreCase(description, Guideline401kConstants.GUIDELINE_401K_LOAN_EE) ||
                StringUtils.equalsIgnoreCase(description, Guideline401kConstants.GUIDELINE_ROTH_401K_EE) ||
                StringUtils.equalsIgnoreCase(description, Guideline401kConstants.GUIDELINE_TRADITIONAL_401K_EE);
    }
}

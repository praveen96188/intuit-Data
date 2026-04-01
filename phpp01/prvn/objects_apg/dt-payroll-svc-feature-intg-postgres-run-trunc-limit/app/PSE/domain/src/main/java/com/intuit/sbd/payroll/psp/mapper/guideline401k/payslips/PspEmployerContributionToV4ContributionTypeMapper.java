package com.intuit.sbd.payroll.psp.mapper.guideline401k.payslips;

import com.intuit.sbd.payroll.psp.constants.Guideline401kConstants;
import com.intuit.sbd.payroll.psp.domain.EmployerContribution;
import com.intuit.sbd.payroll.psp.domain.PaycheckStatusCode;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.common.Amount;
import com.intuit.v4.payroll.payslip.EmployeePayslip;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Objects;

/**
 * EmployeR deductions from paychecks are considered as contributions.
 */
@Component
public class PspEmployerContributionToV4ContributionTypeMapper extends BeanMapper<EmployerContribution,
        EmployeePayslip.ContributionType> {

    private final Amount zeroAmount = new Amount(BigInteger.ZERO);

    @Override
    public EmployeePayslip.ContributionType mapToTarget(EmployerContribution contribution, Class<EmployeePayslip.ContributionType> t) {

        String description = contribution.getCompanyPayrollItem().getSourceDescription();

        if(!isContributionType(description)){
            return null;
        }
        EmployeePayslip.ContributionType contributionType = new EmployeePayslip.ContributionType();

        contributionType.setType(Guideline401kConstants.GUIDELINE_STATUTORY_POLICY_CUS_401K);
        contributionType.setDescription(Guideline401kConstants.GUIDELINE_TRADITIONAL_401K);
        if(contribution.getPaycheck().getStatus() == PaycheckStatusCode.Inactive ||
                contribution.getPaycheck().getStatus() == PaycheckStatusCode.Deleted){
            // in case the paycheck is Inactive (voided), amount is set to 0.
            contributionType.setAmount(zeroAmount);
            contributionType.setYtdAmount(zeroAmount);
            return contributionType;
        }
        contributionType.setAmount(Objects.nonNull(contribution.getContributionAmount()) ?
                new Amount(contribution.getContributionAmount().toString()) : null);
        contributionType.setYtdAmount(Objects.nonNull(contribution.getContributionYTDAmount()) ?
                new Amount(contribution.getContributionYTDAmount().toString()) : null);
        return contributionType;
    }

    private boolean isContributionType(String description) {
        return StringUtils.equalsIgnoreCase(description, Guideline401kConstants.GUIDELINE_TRADITIONAL_401K_ER);
    }
}

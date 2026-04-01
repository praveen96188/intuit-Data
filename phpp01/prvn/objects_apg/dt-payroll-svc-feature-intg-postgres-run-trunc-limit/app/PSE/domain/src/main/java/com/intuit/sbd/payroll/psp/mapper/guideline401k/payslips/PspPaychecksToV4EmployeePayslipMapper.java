package com.intuit.sbd.payroll.psp.mapper.guideline401k.payslips;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Deduction;
import com.intuit.sbd.payroll.psp.domain.EmployerContribution;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.payroll.payslip.EmployeePayslip;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class PspPaychecksToV4EmployeePayslipMapper extends BeanMapper<PspPaycheckToV4EmployeePayslipMapperObject, EmployeePayslip> {

    private final PspEmployerContributionToV4ContributionTypeMapper pspEmployerContributionToV4ContributionTypeMapper;
    private final PspDeductionsToV4DeductionTypeMapper pspDeductionsToV4DeductionTypeMapper;

    @Autowired
    public PspPaychecksToV4EmployeePayslipMapper(PspEmployerContributionToV4ContributionTypeMapper pspEmployerContributionToV4ContributionTypeMapper,
                                                 PspDeductionsToV4DeductionTypeMapper pspDeductionsToV4DeductionTypeMapper) {
        this.pspEmployerContributionToV4ContributionTypeMapper = pspEmployerContributionToV4ContributionTypeMapper;
        this.pspDeductionsToV4DeductionTypeMapper = pspDeductionsToV4DeductionTypeMapper;
    }

    @Override
    public EmployeePayslip mapToTarget(PspPaycheckToV4EmployeePayslipMapperObject mapperObject, Class<EmployeePayslip> t) {

        EmployeePayslip v4Payslip = new EmployeePayslip();

        v4Payslip.setId(GlobalId.create("", mapperObject.getPaycheck().getId().toString()));

        v4Payslip.setPayDate(getPayDate(mapperObject.getPaycheck()));

        v4Payslip.setApprovedDateTime(getApprovedDateTime(mapperObject.getPaycheck()));

        setContributionsAndDeductionsForV4Payslip(mapperObject.getPaycheck(), v4Payslip);

        v4Payslip.setGrossPay(new Amount(mapperObject.getGrossAmount().toString()));

        v4Payslip.setYtdGrossPay(new Amount(new SpcfMoney(mapperObject.getGrossYtdAmount()).toString()));

        return v4Payslip;
    }

    private LocalDate getPayDate(Paycheck paycheck) {
        return Objects.nonNull(paycheck.getPayrollRun().getPaycheckDate()) ?
                paycheck.getPayrollRun().getPaycheckDate().toLocalDate() : null;
    }

    private DateTime getApprovedDateTime(Paycheck paycheck) {
        return Objects.nonNull(paycheck.getPayrollRun().getPayrollRunDate()) ?
                paycheck.getPayrollRun().getPayrollRunDate().toDateTime() : null;
    }

    private void setContributionsAndDeductionsForV4Payslip(Paycheck paycheck, EmployeePayslip v4Payslip) {
        DomainEntitySet<Deduction> deductionCollection = paycheck.getDeductionCollection();
        DomainEntitySet<EmployerContribution> employerContributions = paycheck.getEmployerContributionCollection();
        List<EmployeePayslip.ContributionType> v4Contributions = new ArrayList<>();
        List<EmployeePayslip.DeductionType> v4Deductions = new ArrayList<>();

        for( Deduction deduction : deductionCollection) {
            EmployeePayslip.DeductionType deductionType = pspDeductionsToV4DeductionTypeMapper.mapToTarget(
                    deduction, EmployeePayslip.DeductionType.class);
            if(Objects.nonNull(deductionType)) v4Deductions.add(deductionType);
        }

        for(EmployerContribution contribution : employerContributions){

            EmployeePayslip.ContributionType contributionType = pspEmployerContributionToV4ContributionTypeMapper.mapToTarget(
                    contribution, EmployeePayslip.ContributionType.class);
            if(Objects.nonNull(contributionType)) v4Contributions.add(contributionType);

        }
        v4Payslip.setContributions(v4Contributions);
        v4Payslip.setDeductions(v4Deductions);
    }
}




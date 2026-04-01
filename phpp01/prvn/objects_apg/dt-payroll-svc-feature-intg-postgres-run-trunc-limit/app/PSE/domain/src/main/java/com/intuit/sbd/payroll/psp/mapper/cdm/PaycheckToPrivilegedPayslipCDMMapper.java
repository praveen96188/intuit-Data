package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.payslip.model.*;
import com.intuit.payroll.api.shared.model.BankAccountCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.intuit.payroll.api.payslip.model.MoneyDistributionLineType.*;
import static com.intuit.spc.foundations.portability.util.SpcfCalendar.parse;
import static com.intuit.spc.foundations.portability.util.SpcfCalendar.toDateTime;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Note: All PII data including TaxId and Bank Details, Address etc are NOT encrypted by mapper.
 * It is to be encrypted by calling SDK
 */
@Component("com.intuit.sbd.payroll.psp.mapper.cdm.PaycheckToPayslipCDMMapper")
@Slf4j
public class PaycheckToPrivilegedPayslipCDMMapper extends BeanMapper<Paycheck, PrivilegedPayslipCDM> {

    /**
     * Convert Paycheck Domain entity (PSP understands this entity) into a
     * Common Data Model (CDM) object (Online Payroll & downstream systems
     * understands this model)
     *
     * @param s          source object: Paycheck Entity Object
     * @param targetType target type object: PayslipCDM
     * @return PayslipCDM
     */
    @Override
    public PrivilegedPayslipCDM mapToTarget(Paycheck s, Class<PrivilegedPayslipCDM> targetType) {
        PrivilegedPayslipCDM t = new PrivilegedPayslipCDM();
        t.setId(isNull(s.getId()) ? null : s.getId().toString());
        DateTime paycheckCreateDate = toDateTime(s.getCreatedDate());
        t.setCreated(paycheckCreateDate);
        t.setApprovedDate(paycheckCreateDate);
        t.setUpdated(isNull(s.getModifiedDate()) ? null : s.getModifiedDate().toDateTime());
        t.setPayPeriodStartDate(isNull(s.getPayPeriodBeginDate()) ? null : s.getPayPeriodBeginDate().toLocalDate());
        t.setPayPeriodEndDate(isNull(s.getPayPeriodEndDate()) ? null : s.getPayPeriodEndDate().toLocalDate());
        t.setCheckNumber(isNull(s.getQbdtPaycheckInfo()) ? null : s.getQbdtPaycheckInfo().getCheckNumber());
        t.setTotalHours(getTotalHours(s));
        t.setCreatedBy(s.getCreatorId());


        // NET_AMOUNT is not populated in PSP_PAYCHECK table for DD Paychecks. Hence, accumulating amount from PaycheckSplit
        BigDecimal ddNetAmount = s.findTotalAmountPerPaycheck().toBigDecimal();
        t.setNetAmount(ddNetAmount);

        // GROSS_AMOUNT is not populated in PSP_PAYCHECK table. Hence fetch from Paystub / Compensation
        // If gross amount is not found, use ddNetAmount (temp fix)
        BigDecimal ddGrossAmount = s.findGrossAmountPerPaycheck().toBigDecimal();
        t.setGrossAmount((BigDecimal.ZERO.compareTo(ddGrossAmount) == 0) ? ddNetAmount : ddGrossAmount);

        String entityVersion = String.valueOf(s.getVersion());
        if (!isBlank(entityVersion)) {
            t.setEntityVersion(entityVersion);
        } else {
            log.warn("Status=MissingField, fieldName=entityVersion, PaycheckId=" + s.getId());
        }

        setDefaultValues(t);
        setPayrollRun(s, t);
        setEmployerInfo(s, t);
        setEmployeeInfo(s, t);
        setDistributionLine(s, t);
        setTaxLines(s, t);

        return t;
    }

    private BigDecimal getTotalHours(Paycheck s) {
        double totalHours = 0.0D;
        DomainEntitySet<Compensation> compensations = s.getCompensationCollection();
        for (Compensation compensation : compensations) {
            totalHours += compensation.getHoursWorked();
        }
        return BigDecimal.valueOf(totalHours);
    }

    private void setDefaultValues(PrivilegedPayslipCDM t) {
        t.setApprovedBy("NA");
        t.setApproved(true);
        t.setPayslipType(PayslipType.REGULAR);
        t.setHistory(false);
    }

    private void setPayrollRun(Paycheck s, PrivilegedPayslipCDM t) {
        PayrollRun payrollRun = s.getPayrollRun();
        if (payrollRun == null) {
            return;
        }

        t.setCheckDate(isNull(payrollRun.getPaycheckDate()) ? null : payrollRun.getPaycheckDate().toLocalDate());
        t.setPayrollId(isNull(payrollRun.getId()) ? null : payrollRun.getId().toString());

        SpcfCalendar settlementDate = payrollRun.getPaycheckSettlementDate();
        if (nonNull(settlementDate)) {
            LocalDate settlementDateLocal = settlementDate.toLocalDate();
            t.setDdSettlementDate(settlementDateLocal);

            // TODO fix DD date fields
            t.setDdDebitDate(settlementDateLocal);
            t.setDdInitiationDate(settlementDateLocal);
            if (nonNull(s.getCompany()) && nonNull(s.getCompany().getOffloadGroup())) {
                /* t.setDdCutoffTime(toDateTime(s.getCompany().getOffloadGroup().getCalendarForCutoffTime(
                        settlementDate))); */

                // cutoffTime is hard-coded as 1 AM UTC, as done for Kount
                SpcfCalendar cutoffDate = parse("yyyyMMddHH:mm",
                        settlementDate.format("yyyyMMdd") + "01:00");
                t.setDdCutoffTime(new DateTime(cutoffDate.getTimeInMilliseconds()));
            }
        }
    }

    private void setEmployerInfo(Paycheck s, PrivilegedPayslipCDM t) {
        PrivilegedEmployerInfoCDM employerInfoCDM = getMapper().mapToTarget(s, PrivilegedEmployerInfoCDM.class);
        validateEmployerInfo(employerInfoCDM, s);
        t.setPrivilegedEmployerInfo(employerInfoCDM);
    }

    private void validateEmployerInfo(PrivilegedEmployerInfoCDM employerInfoCDM, Paycheck s) {
        if (isNull(employerInfoCDM)) {
            throw new IllegalArgumentException("Paycheck_EmployerInfo=null, PaycheckId=" + s.getId());
        }
        if (!isBlank(employerInfoCDM.getBankAccountId()) && (
                isNull(employerInfoCDM.getBankAccount()) || !isValid(employerInfoCDM.getBankAccount())
        )) {
            throw new IllegalArgumentException("Paycheck_EmployerBankAccount_Not_Valid, PaycheckId=" + s.getId());
        }
    }

    private void setEmployeeInfo(Paycheck s, PrivilegedPayslipCDM t) {

        Employee employee = (nonNull(s.getDDEmployee())) ? s.getDDEmployee() : s.getSourceEmployee();

        if (isNull(employee)) {
            throw new IllegalArgumentException("Paycheck_Employee=null, PaycheckId=" + s.getId());
        }

        PrivilegedEmployeeInfoCDM employeeInfoCDM = getMapper().mapToTarget(employee, PrivilegedEmployeeInfoCDM.class);
        if (nonNull(employeeInfoCDM)) {
            t.setPrivilegedEmployeeInfo(employeeInfoCDM);
        } else {
            throw new IllegalArgumentException("Paycheck_EmployeeInfo=null, PaycheckId=" + s.getId());
        }
    }

    /**
     * Throw exception if ANY one Paycheck split line does not have bank account number or Principal details
     *
     * @param s source object - Paycheck
     * @param t target object - PrivilegedPayslipCDM
     */
    private void setDistributionLine(Paycheck s, PrivilegedPayslipCDM t) {
        List<PrivilegedMoneyDistributionLineCDM> lines = new ArrayList<>();

        for (PaycheckSplit split : s.getPaycheckSplitCollection()) {
            PrivilegedMoneyDistributionLineCDM line = getMapper()
                    .mapToTarget(split, PrivilegedMoneyDistributionLineCDM.class);
            String bankAccountId = line.getBankAccountId();
            line.setType(!isBlank(bankAccountId) ? DDService : CHECK);
            if (!isBlank(bankAccountId) && (
                    isNull(line.getBankAccount()) || !isValid(line.getBankAccount()) || isNull(line.getAmount())
            )) {
                throw new IllegalArgumentException(
                        "Action=mapMoneyDistributionLineCDM, Msg=Incomplete_Bank_Details, PaycheckId=" + s.getId());
            }
            lines.add(line);
        }
        t.setPrivilegedMoneyDistributionLines(lines);
    }

    private boolean isValid(BankAccountCDM bankAccount) {
        return !isBlank(bankAccount.getAccountNumber())
                && nonNull(bankAccount.getPrincipal());
    }

    private void setTaxLines(Paycheck s, PrivilegedPayslipCDM t) {
        DomainEntitySet<Tax> taxCollection = s.getTaxCollection();
        if (CollectionUtils.isEmpty(taxCollection)) {
            return;
        }
        /*List<TaxLineCDM> taxLineCDMs = new ArrayList<>();
        for (Tax tax : taxCollection) {
            taxLineCDMs.add(getMapper().mapToTarget(tax, TaxLineCDM.class));    // TODO create mapper
        }
        t.setTaxLines(taxLineCDMs);*/
    }
}
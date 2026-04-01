package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.payslip.model.PrivilegedEmployerInfoCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.payroll.api.shared.model.BankAccountVerification;
import com.intuit.payroll.api.shared.model.BankAccountVerificationStatus;
import com.intuit.payroll.api.shared.model.BankAccountVerificationType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.intuit.spc.foundations.portability.util.SpcfCalendar.toDateTime;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.PaycheckToPrivilegedEmployerInfoCDMMapper")
@Slf4j
public class PaycheckToPrivilegedEmployerInfoCDMMapper extends BeanMapper<Paycheck, PrivilegedEmployerInfoCDM> {

    /**
     * @param s          source object: Paycheck
     * @param targetType target type object: PrivilegedEmployerInfoCDM
     * @return PrivilegedEmployerInfoCDM
     */
    @Override
    public PrivilegedEmployerInfoCDM mapToTarget(Paycheck s, Class<PrivilegedEmployerInfoCDM> targetType) {
        Company company = s.getCompany();
        if (nonNull(company)) {
            PrivilegedEmployerInfoCDM t = getMapper().mapToTarget(company, PrivilegedEmployerInfoCDM.class);

            boolean directDepositEnabled = nonNull(s.getDDEmployee());
            // Set CompanyBankAccount only for DD enabled Paychecks
            if (directDepositEnabled) {
                setBankAccount(s, t);
            }
            return t;
        }
        throw new IllegalArgumentException("Paycheck_Company=null, PaycheckId=" + s.getId());
    }

    private void setBankAccount(Paycheck s, PrivilegedEmployerInfoCDM t) {
        if (nonNull(s.getPayrollRun())) {
            PayrollRun.PayrollDebitInfo payrollDebitInfo = s.getPayrollRun().getPayrollDebitInfo();
            if (nonNull(payrollDebitInfo)) {
                setBankAccount(s, t, payrollDebitInfo);
            } else {
                throw new IllegalArgumentException("Paycheck_PayrollDebitInfo=null, PaycheckId=" + s.getId());
            }
        } else {
            throw new IllegalArgumentException("Paycheck_PayrollRun=null, PaycheckId=" + s.getId());
        }
    }

    private void setBankAccount(Paycheck s, PrivilegedEmployerInfoCDM t, PayrollRun.PayrollDebitInfo payrollDebitInfo) {
        Company company = s.getCompany();
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccountIncludingExpired(
                company, payrollDebitInfo.bankAccount);

        if (isNull(companyBankAccount)) {
            log.error("CompanyBankAccount not found, PaycheckId=" + s.getId());
            return;
        }

        t.setBankAccountId(isNull(companyBankAccount.getId()) ? null : companyBankAccount.getId().toString());
        PrivilegedBankAccountCDM bankAccountCDM = getBankAccountCDM(company, companyBankAccount, s.getId().toString());
        t.setBankAccount(bankAccountCDM);
    }

    private PrivilegedBankAccountCDM getBankAccountCDM(Company company, CompanyBankAccount s, String paycheckId) {

        String sourceCompanyId = company.getSourceCompanyId();
        BankAccount bankAccount = s.getBankAccount();
        if (isNull(bankAccount)) {
            log.error(
                    "Action=mapBankAccountCDM, status=fail, Msg=BankAccount_Null, companyId={}, CompanyBankAccountId={}, PaycheckId={}",
                    sourceCompanyId, s.getId(), paycheckId);
            return null;
        }

        PrivilegedBankAccountCDM t = getMapper().mapToTarget(bankAccount, PrivilegedBankAccountCDM.class);
        t.setId(isNull(s.getId()) ? null : s.getId().toString());
        t.setEntityVersion(Long.toString(s.getVersion()));
        t.setUpdated(toDateTime(s.getModifiedDate()));
        t.setName(s.getSourceBankAccountName());
        t.setDefault(BankAccountStatus.Active.equals(s.getStatusCd()));
        t.setOwnerType("employer.principal");
        t.setPrincipal(getMapper().mapToTarget(company, BankPrincipalCDM.class));
        setBankAccountVerification(s, t);

        try {
            t.setCompanyId(Long.parseLong(sourceCompanyId));
        } catch (NumberFormatException e) {
            log.error("Could not parse sourceCompanyId for Paycheck, sourceCompanyId=" + sourceCompanyId);
        }
        return t;
    }

    private void setBankAccountVerification(CompanyBankAccount s, PrivilegedBankAccountCDM t) {

        BankAccountVerification bankAccountVerification = new BankAccountVerification();
        if (s.getStatusCd() == BankAccountStatus.Active) {
            t.setVerified(true);
            bankAccountVerification.setStatus(BankAccountVerificationStatus.VERIFIED);
        } else {
            bankAccountVerification.setStatus(BankAccountVerificationStatus.NEEDS_VERIFICATION);
        }
        bankAccountVerification.setType(BankAccountVerificationType.TEST_TRANSACTION);
        t.setVerifications(new BankAccountVerification[]{bankAccountVerification});
    }
}
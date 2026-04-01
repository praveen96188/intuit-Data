package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.payslip.model.PrivilegedMoneyDistributionLineCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PaycheckSplit;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.PaycheckSplitToPrivilegedMoneyDistributionLineCDMMapper")
@Slf4j
public class PaycheckSplitToPrivilegedMoneyDistributionLineCDMMapper extends
        BeanMapper<PaycheckSplit, PrivilegedMoneyDistributionLineCDM> {

    /**
     * @param s          source object: PaycheckSplit
     * @param targetType target object: PrivilegedMoneyDistributionLineCDM
     * @return PrivilegedMoneyDistributionLineCDM
     */
    @Override
    public PrivilegedMoneyDistributionLineCDM mapToTarget(PaycheckSplit s,
                                                          Class<PrivilegedMoneyDistributionLineCDM> targetType) {
        try {
            PrivilegedMoneyDistributionLineCDM t = new PrivilegedMoneyDistributionLineCDM();
            t.setAmount(isNull(s.getPaycheckSplitAmount()) ? null : s.getPaycheckSplitAmount().toBigDecimal());
            setEmployeeBankAccounts(s, t);
            return t;
        } catch (Exception e) {
            log.error("Status=Error, Action=mapToTarget, PaycheckSplitId=" + s.getId() + ", errMsg: "
                    + e.getMessage(), e);
            return null;
        }
    }

    private void setEmployeeBankAccounts(PaycheckSplit s, PrivilegedMoneyDistributionLineCDM t) {
        EmployeeBankAccount employeeBankAccount = s.getEmployeeBankAccount();

        if (isNull(employeeBankAccount)) {
            log.info("EmployeeBankAccount not found, PaycheckSplitId=" + s.getId());
            return;
        }

        t.setBankAccountId(isNull(employeeBankAccount.getId()) ? null : employeeBankAccount.getId().toString());
        PrivilegedBankAccountCDM bankAccountCDM = getBankAccountCDM(s.getCompany().getSourceCompanyId(),
                employeeBankAccount, s.getId().toString());
        t.setBankAccount(bankAccountCDM);
    }


    private PrivilegedBankAccountCDM getBankAccountCDM(String sourceCompanyId, EmployeeBankAccount s, String paycheckSplitId) {

        BankAccount bankAccount = s.getBankAccount();
        if (isNull(bankAccount)) {
            log.error(
                    "Action=mapBankAccountCDM, status=fail, Msg=BankAccount_Null, companyId={}, EmployeeBankAccountId={}, PaycheckSplitId={}",
                    sourceCompanyId, s.getId(), paycheckSplitId);
            return null;
        }

        PrivilegedBankAccountCDM t = getMapper().mapToTarget(bankAccount, PrivilegedBankAccountCDM.class);
        t.setId(isNull(s.getId()) ? null : s.getId().toString());
        t.setUpdated(s.getModifiedDate().toDateTime());
        t.setVerified(Boolean.FALSE);
        t.setOwnerType("employee");
        t.setPrincipal(getMapper().mapToTarget(s.getEmployee(), BankPrincipalCDM.class));
        try {
            t.setCompanyId(Long.parseLong(sourceCompanyId));
        } catch (NumberFormatException e) {
            log.error("Could not parse sourceCompanyId for Paycheck, sourceCompanyId=" + sourceCompanyId);
        }

        return t;
    }
}
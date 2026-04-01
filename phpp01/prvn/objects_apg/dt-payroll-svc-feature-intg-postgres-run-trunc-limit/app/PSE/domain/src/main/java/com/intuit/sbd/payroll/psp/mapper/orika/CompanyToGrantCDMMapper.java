package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.company.model.GrantCDM;
import com.intuit.payroll.api.shared.model.EmployerFlavor;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Orika-based mapping from Company to GrantCDM
 */
@Component
public class CompanyToGrantCDMMapper extends BeanMapper<Company, GrantCDM> {

    public static final String DEFAULT_PARTNER_NAME = "Quickbooks Desktop Payroll";
    public static Logger LOGGER = LoggerFactory.getLogger(CompanyToGrantCDMMapper.class);
    private DomainEntitySet<CompanyBankAccount> companyBankAccounts = null;

    private CompanyService companyService = null;

    @Override
    public void directFieldToFieldMapping() {
    }

    /**
     * Main mapping method of the class
     *
     * @param company  The Company object
     * @param grantCDM The GrantCDM object
     * @param context  The Mapping Context
     */
    @Override
    public void mapAtoB(Company company, GrantCDM grantCDM, MappingContext context) {
        grantCDM.setPartnerName(DEFAULT_PARTNER_NAME);
        grantCDM.setEmployerFlavor(EmployerFlavor.QBDT);
        setStatusOnGrantCDM(company, grantCDM);
    }

    /**
     * Method to decide on the Status of the company based on whether the
     * company has DD active, and whether it has a valid Bank account
     *
     * @param company  The Company object
     * @param grantCDM The GrantCDM object
     */
    private void setStatusOnGrantCDM(Company company, GrantCDM grantCDM) {
        if (!getAndValidateDDService(company)) {
            grantCDM.setStatus(GrantCDM.Status.INACTIVE);
        } else if (companyService.isActive() && getAndValidateBankAccount(company)) {
            grantCDM.setStatus(GrantCDM.Status.ACTIVE);
        } else if (companyService.isCancelTerm()) {
            grantCDM.setStatus(GrantCDM.Status.CLOSED);
        } else {
            grantCDM.setStatus(GrantCDM.Status.INACTIVE);
        }
    }

    private boolean getAndValidateBankAccount(Company company) {
        companyBankAccounts = company.getCompanyBankAccountCollection().find(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
        if (companyBankAccounts.size() == 1) {
            return true;
        } else if (companyBankAccounts.size() > 1) {
            LOGGER.error("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId()
                    + " has more than one active account");
        } else {
            LOGGER.debug("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId()
                    + " does not have an active account");
        }
        return false;
    }

    private boolean getAndValidateDDService(Company company) {
        companyService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        if (companyService == null) {
            LOGGER.debug("Company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId()
                    + " is not on DD service");
            return false;
        }
        return true;
    }

}

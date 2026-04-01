package com.intuit.sbd.payroll.psp.processes.accountservice;

import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Objects;

public class DeactivateAccountService extends Process {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(DeactivateAccountService.class);

    private Company mDomainCompany;
    private AccountServiceGateway accountServiceGateway;

    public DeactivateAccountService(Company pDomainCompany) {
        this.mDomainCompany = pDomainCompany;
        this.accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (Objects.isNull(mDomainCompany)) {
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany");
            return validationResult;
        }
        if (Objects.isNull(mDomainCompany.getIAMRealmId())) {
            validationResult.getMessages()
                    .BadProcessArgument("IAMRealmId");
            return validationResult;
        }
        PaymentsAccount paymentsAccount = accountServiceGateway.getPaymentsAccount(mDomainCompany.getIAMRealmId());

        if (Objects.isNull(paymentsAccount)) {
            validationResult.getMessages()
                    .MoneymovementAccountDoesNotExistOnRealmId(EntityName.Company, mDomainCompany.getIAMRealmId());
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        try {
            accountServiceGateway.deletePaymentsAccount(mDomainCompany.getIAMRealmId());
        } catch (AccountServicesException e) {
            logger.error("Error in updating the PaymentsAccount for realmId=" + mDomainCompany.getIAMRealmId()
                    + " Response=" + e.getHttpServiceResponse().toDetailedString(), e);
            processResult.getMessages().AccountsServiceUpdateError(mDomainCompany.getIAMRealmId(), e.getErrorMessage());
        } catch(HttpClientErrorException | CallNotPermittedException excp) {
            handleException(processResult, excp);
        } catch (Exception e) {
            logger.error("Unknown Error in updating the PaymentsAccount for realmId=" + mDomainCompany.getIAMRealmId(), e);
            processResult.getMessages().ExceptionOccurred("Unknown Error in updating the CompanyInfo for realmId=" + mDomainCompany.getIAMRealmId());
        }
        return processResult;
    }

    /**
     *
     * @param processResult
     * @param excp
     */
    private void handleException(ProcessResult processResult, Exception excp) {
        logger.error("Error in updating the PaymentsAccount for realmId=" + mDomainCompany.getIAMRealmId()
                + " Response=" + excp.getMessage(), excp);
        processResult.getMessages().AccountsServiceUpdateError(mDomainCompany.getIAMRealmId(), excp.getMessage());
    }

}

package com.intuit.sbd.payroll.psp.processes.accountservice;

import com.intuit.payments.cdm.v2.client.BusinessOwner;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PrimaryBusiness;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.gateways.accountservice.AccountServiceSyncDecisionManager;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;

public class UpdateAccountServiceInfo extends Process {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(UpdateAccountServiceInfo.class);

    private CompanyDTO mDtoCompany;
    private Company mDomainCompany;
    private AccountServiceGateway accountServiceGateway;
    private AccountServiceTranslator accountServiceTranslator;
    private PrimaryBusiness primaryBusiness;
    private BusinessOwner businessOwner;
    private AccountServiceSyncDecisionManager decisionManager;
    private boolean isUpdateCompanyRequired = false;

    public UpdateAccountServiceInfo(CompanyDTO pDtoCompany, Company pDomainCompany) {
        this.mDtoCompany = pDtoCompany;
        this.mDomainCompany = pDomainCompany;
        this.accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
        this.accountServiceTranslator = PayrollApplicationBeanFactory.getBean(AccountServiceTranslator.class);
        decisionManager = PayrollApplicationBeanFactory.getBean(AccountServiceSyncDecisionManager.class);
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (Objects.isNull(mDomainCompany)) {
            validationResult.getMessages()
                    .BadProcessArgument("DomainCompany");
            return validationResult;
        }
        if (Objects.isNull(mDtoCompany)) {
            validationResult.getMessages()
                    .BadProcessArgument("DtoCompany");
            return validationResult;
        }
        if (Objects.isNull(mDtoCompany.getIAMRealmId())) {
            validationResult.getMessages()
                    .BadProcessArgument("IAMRealmId");
            return validationResult;
        }
        PaymentsAccount paymentsAccount = null;

        try {
            isUpdateCompanyRequired = decisionManager.isUpdateCompanyRequired(mDtoCompany,mDomainCompany);
            if (!isUpdateCompanyRequired){
                return validationResult;
            }
            paymentsAccount = accountServiceGateway.getPaymentsAccount(mDtoCompany.getIAMRealmId());
        } catch (AccountServicesException e) {
            validationResult.getMessages().AccountsServiceAccessError(mDtoCompany.getIAMRealmId(),e.getErrorMessage());
            return validationResult;
        } catch(HttpClientErrorException excp) {
            validationResult.getMessages().AccountsServiceAccessError(mDtoCompany.getIAMRealmId(),excp.getMessage());
            return validationResult;
        }catch (CallNotPermittedException cnpe) {
            validationResult.getMessages().AccountsServiceAccessError(mDtoCompany.getIAMRealmId(),cnpe.getMessage());
            return validationResult;
        }
        catch (Exception e) {
            validationResult.getMessages().ExceptionOccurred(
                    "Exception while fetching the paymentsAccount from account service",e);
            return validationResult;
        }

        if (Objects.isNull(paymentsAccount)) {
            validationResult.getMessages()
                    .MoneymovementAccountDoesNotExistOnRealmId(EntityName.Company, mDtoCompany.getIAMRealmId());
            return validationResult;
        }
        this.primaryBusiness = paymentsAccount.getBusinessInfo();
        this.businessOwner = accountServiceTranslator.getPaymentsPrimaryPrincipal(paymentsAccount);
        if (Objects.isNull(primaryBusiness)) {
            validationResult.getMessages()
                    .AccountsServiceValidateError(mDtoCompany.getIAMRealmId(), "business info not found in account service");
        }
        if (Objects.isNull(businessOwner)) {
            validationResult.getMessages()
                    .AccountsServiceValidateError(mDtoCompany.getIAMRealmId(), "Owner info not found in account service");
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (!isUpdateCompanyRequired){
            logger.info("Account service update not required because the data is in sync");
            return processResult;
        }
        try {
            PaymentsAccount requestPaymentsAccount = accountServiceTranslator.createPaymentsAccount(primaryBusiness,businessOwner,mDtoCompany);
            accountServiceGateway.updatePaymentsAccount(mDtoCompany.getIAMRealmId(), requestPaymentsAccount);
        } catch (AccountServicesException e) {
            logger.error("Error in updating the PaymentsAccount for realmId=" + mDtoCompany.getIAMRealmId()
                    + " Response=" + e.getHttpServiceResponse().toDetailedString(), e);
            if (propagateError()) {
                logger.info("Propagating AccountServices Error for the CurrentPrincipal="+Application.getCurrentPrincipal());
                processResult.getMessages().AccountsServiceUpdateError(mDtoCompany.getIAMRealmId(), e.getErrorMessage());
            }
        } catch (HttpClientErrorException | CallNotPermittedException excp) {
            handleException(processResult, excp);
        } catch (Exception e) {
            logger.error("Unknown Error in updating the PaymentsAccount for realmId=" + mDtoCompany.getIAMRealmId(), e);
            if (propagateError()) {
                logger.info("Propagating AccountServices Error for the CurrentPrincipal="+Application.getCurrentPrincipal());
                processResult.getMessages().ExceptionOccurred("Unknown Error in updating the CompanyInfo for realmId=" + mDtoCompany.getIAMRealmId());
            }
        }
        return processResult;
    }

    /**
     *
     * @param processResult
     * @param excp
     */
    private void handleException(ProcessResult processResult, Exception excp) {
        logger.error("Error in updating the PaymentsAccount for realmId=" + mDtoCompany.getIAMRealmId()
                + " Response=" + excp.getMessage(), excp);
        if (propagateError()) {
            logger.info("Propagating AccountServices Error for the CurrentPrincipal=" + Application.getCurrentPrincipal());
            processResult.getMessages().AccountsServiceUpdateError(mDtoCompany.getIAMRealmId(), excp.getMessage());
        }
    }

    public boolean propagateError() {
        if (SystemPrincipal.QBDTAdapter.equals(Application.getCurrentPrincipal().getSystemPrincipal()))
            return false;
        if (SystemPrincipal.QBDTWSAdapter.equals(Application.getCurrentPrincipal().getSystemPrincipal()))
            return false;
        return true;
    }

}

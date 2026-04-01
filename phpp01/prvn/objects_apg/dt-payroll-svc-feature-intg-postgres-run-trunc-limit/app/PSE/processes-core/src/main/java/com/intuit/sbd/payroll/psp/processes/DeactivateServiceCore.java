/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/DeactivateServiceCore.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateGuidelineGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateTRONGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.accountservice.DeactivateAccountService;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Objects;

/**
 * Core process for cancelling a service on an existing company.
 *
 * @author Dawn Martens
 */
public class DeactivateServiceCore extends Process implements IProcess {
    private Company company;
    private CompanyService companyService;

    private String sourceCompanyId;
    private SourceSystemCode sourceSystem;
    private ServiceCode serviceCode;

    private boolean mCreateEvents = true;
    private DeactivateServiceCore mCancelService;
    private boolean mCancelVMP = false;
    private DeactivateAccountService deactivateAccountService;
    private AddOrUpdateTRONGrantProcessor addOrUpdateTRONGrantProcessor;
    private AddOrUpdateGuidelineGrantProcessor addOrUpdateGuidelineGrantProcessor;

    public DeactivateServiceCore(SourceSystemCode pSourceSystem, String pSourceCompanyId, ServiceCode pService) {
        sourceSystem = pSourceSystem;
        sourceCompanyId = pSourceCompanyId;
        serviceCode = pService;
    }

    public ProcessResult process() {
        ProcessResult<CompanyService> processResult = new ProcessResult<CompanyService>();

        // If Tax Service execute DeactivateServiceTaxCore
        if (companyService.getService().getServiceCd() == com.intuit.sbd.payroll.psp.domain.ServiceCode.Tax) {
            DeactivateServiceTax deactivateTaxServiceProcess = new DeactivateServiceTax(companyService);
            processResult.merge(deactivateTaxServiceProcess.execute());
        }

        // if DD Service cancel also BillPayment service if active
        if (companyService.getService().getServiceCd() == com.intuit.sbd.payroll.psp.domain.ServiceCode.DirectDeposit &&
                company.isCompanyOnService(com.intuit.sbd.payroll.psp.domain.ServiceCode.BillPayment)) {
            CompanyService billPaymentService = CompanyService.findCompanyService(company, com.intuit.sbd.payroll.psp.domain.ServiceCode.BillPayment);
            if (billPaymentService != null) {
                billPaymentService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
            }
        }

        //Deactivate CloudV2 and also deactivate view my paycheck if exists.
        if(companyService.getService().getServiceCd() == ServiceCode.CloudV2) {
            CompanyService cloudV2Service = CompanyService.findCompanyService(company, ServiceCode.CloudV2);
            if(cloudV2Service != null) {
                cloudV2Service.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
            }
            if(company.isCompanyOnService(ServiceCode.ViewMyPaycheck)) {
                CompanyService viewMyPaycheckService = CompanyService.findCompanyService(company, ServiceCode.ViewMyPaycheck);
                if(viewMyPaycheckService != null) {
                    viewMyPaycheckService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
                }
            }
        }

        if(canCancelCloudV2())
        {
            CompanyService cloudV2Service = CompanyService.findCompanyService(company, ServiceCode.CloudV2);
            if(cloudV2Service != null) {
                cloudV2Service.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
            }
        }

        if(mCancelVMP){
            CompanyService vmpService = CompanyService.findCompanyService(company, ServiceCode.ViewMyPaycheck);
            vmpService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled);
        }

        //cancel any bank verification debits
        FinancialTransaction.cancelPendingEmployerVerificationDebits(company);

        companyService.updateCompanyServiceStatus(ServiceSubStatusCode.Cancelled, mCreateEvents);

        company = Application.save(company);

        if (mCancelService != null) {
            if (mCancelService.companyService.getService().getServiceCd().equals(ServiceCode.DirectDeposit)) {
                mCancelService.setCreateEvents(true);
            } else {
                mCancelService.setCreateEvents(false);
            }
            processResult.merge(mCancelService.process());
        }

        if (Objects.nonNull(deactivateAccountService)){
            processResult.merge(deactivateAccountService.process());
        }

        if (Objects.nonNull(addOrUpdateTRONGrantProcessor)){
            processResult.merge(addOrUpdateTRONGrantProcessor.process());
        }

        if (Objects.nonNull(addOrUpdateGuidelineGrantProcessor)){
            processResult.merge(addOrUpdateGuidelineGrantProcessor.process());
        }

        processResult.setResult(companyService);
        return processResult;
    }

    private boolean canCancelCloudV2() {
        // Deactivate CloudV2 if VMP not turned on and WC is turning off
        // Deactivate CloudV2 if WC not turned on and VMP is turning off
        // Deactivate CloudV2 if WC not turned on and VMP is set to be turning off by Assisted cancellation
        return (companyService.getService().getServiceCd() == ServiceCode.WorkersComp && !company.isCompanyOnService(ServiceCode.ViewMyPaycheck)) ||
                ((companyService.getService().getServiceCd() == ServiceCode.ViewMyPaycheck || mCancelVMP) && !company.isCompanyOnService(ServiceCode.WorkersComp));
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystem, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystem);
        if (company == null) {
            validationResult.getMessages()
                    .CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystem.toString(), sourceCompanyId);
            return validationResult;
        }

        if (serviceCode == null) {
            validationResult.getMessages().CompanyServiceNotSpecified(EntityName.Service, null);
            return validationResult;
        }

        companyService = CompanyService.findCompanyService(company, serviceCode);
        if (companyService == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                    company.getSourceCompanyId(),
                    company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                    serviceCode.toString());
            return validationResult;
        }

        if (!company.isAllowedCapability(SystemCapabilityCode.CancelService, companyService.getService().getServiceCd())) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.CancelService.toString());
            return validationResult;
        }

        if (Application.getCurrentPrincipal().isAgent() && !companyService.getService().getCanBeManuallyCancelled()) {
            validationResult.getMessages().ServiceNotAllowedToCancelManually(EntityName.ServiceSubStatus,
                    companyService.getService().getServiceCd().name(), companyService.getService().getServiceCd().name());
            return validationResult;
        }


        //See if there are any unresolved bank returns for this service
        DomainEntitySet<TransactionReturn> transactionReturnCollection =
                TransactionReturn.findTransactionReturnsByServiceAndExcludedStatus(company, companyService.getService(),
                        TransactionReturnStatusCode.Resolved);

        if (!transactionReturnCollection.isEmpty() && !LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERReturnReceivable).equals(SpcfMoney.ZERO)) {
            validationResult.getMessages().CompanyHasUnresolvedBankReturns(EntityName.Company,
                    company.getSourceCompanyId(),
                    company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        }

        if (companyService.getService().getServiceCd() == com.intuit.sbd.payroll.psp.domain.ServiceCode.Tax) {
            if (company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                mCancelService = new DeactivateServiceCore(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
                validationResult.merge(mCancelService.validate());
            }
            //PSP-7591 : Cancel VMP if Assisted service is set for cancellation
            if(company.isCompanyOnActiveService(ServiceCode.ViewMyPaycheck)){
                mCancelVMP = true;
            }
        }

        if(serviceCode.equals(ServiceCode.DirectDeposit) && company.isMoneyMovementOnboardingEnabled()){
            deactivateAccountService = new DeactivateAccountService(company);
            validationResult.merge(deactivateAccountService.validate());
        }

        if(serviceCode == ServiceCode.DirectDeposit) {
            addOrUpdateTRONGrantProcessor = new AddOrUpdateTRONGrantProcessor(company, true);
            validationResult.merge(addOrUpdateTRONGrantProcessor.validate());
        }

        if(serviceCode == ServiceCode.Guideline401k) {
            addOrUpdateGuidelineGrantProcessor = new AddOrUpdateGuidelineGrantProcessor(company, true);
            validationResult.merge(addOrUpdateGuidelineGrantProcessor.validate());
        }

        return validationResult;
    }

    public void setCreateEvents(boolean pCreateEvents) {
        mCreateEvents = pCreateEvents;
    }
}

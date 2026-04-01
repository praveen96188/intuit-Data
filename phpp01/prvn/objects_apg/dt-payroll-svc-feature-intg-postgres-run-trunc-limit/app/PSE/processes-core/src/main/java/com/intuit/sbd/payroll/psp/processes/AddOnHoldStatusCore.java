package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 21, 2008
 * Time: 11:26:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddOnHoldStatusCore extends Process implements IProcess {
    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private Company company;

    private ServiceSubStatusCode onHoldReasonCode;
    private ServiceSubStatus onHoldStatus;


    private AddOnHoldStatusTax addOnHoldStatusTax;
    private AddOnHoldStatus401k addOnHoldStatus401k;

    public AddOnHoldStatusCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                               ServiceSubStatusCode pOnHoldReasonCd) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        onHoldReasonCode = pOnHoldReasonCd;
    }

    public Company getCompany() {
        return company;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        OnHoldReason onHoldReason = company.getCurrentOnHoldReason(onHoldReasonCode);
        if (onHoldReason != null) {
            return processResult;
        }

        company.addOnHoldReason(onHoldReasonCode);

        // Update OnHold status on financial transactions
        TransactionState createdTxnState = Application.findById(TransactionState.class, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> pendingFinancialTransactions =
                Application.find(FinancialTransaction.class,
                        FinancialTransaction.Company().equalTo(company)
                                .And(FinancialTransaction.CurrentTransactionState().equalTo(createdTxnState)));

        //todo_rhn should this be only DD Transactions?  i.e. ERTaxDebits wouldn't go on hold, Agency FTs, etc.
        for (FinancialTransaction txn : pendingFinancialTransactions) {
            // check if the transaction is not offloadable for the current onHold status
            txn.addOnHoldReasons(company);
            Application.save(txn);
        }

        Application.save(company);

        // Add company to fraud company table
        FraudCompany.addFraudRecords(company);

        if (addOnHoldStatusTax != null) {
            processResult.merge(addOnHoldStatusTax.process());
        }

        if (addOnHoldStatus401k!=null) {
            processResult.merge(addOnHoldStatus401k.process());
        }

        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        onHoldStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCode);
        if (onHoldStatus.getServiceStatus().getServiceStatusCd() != ServiceStatusCode.OnHold) {
            validationResult.getMessages().InvalidHoldSubStatus(EntityName.Company, sourceCompanyId, onHoldReasonCode);
        }

        PspPrincipal principal = Application.getCurrentPrincipal();
        if (principal.isAgent()) {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCode);
            if (!serviceSubStatus.getIsSetManually()) {
                validationResult.getMessages().OnHoldNotAllowedToSetManually(EntityName.ServiceStatus,
                        onHoldReasonCode.toString(), onHoldReasonCode.toString());
                return validationResult;
            }
        }

        // Verify company is already on hold for the same service status
        if (company.getCurrentOnHoldReason(onHoldReasonCode) != null) {
            validationResult.getMessages().CompanyAlreadyInOnHoldStatus(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(),
                    sourceCompanyId, onHoldReasonCode.toString());
            return validationResult;
        }

        // Verify the OnHoldReason code is valid for at least one of the company services
        CompanyService companyService = null;
        boolean isAllowedServiceStatus = false;
        for(CompanyService itCompanyService : company.getCompanyServiceCollection()) {
            companyService = itCompanyService;
            if (ServiceStatus.isAllowedServiceStatus(onHoldReasonCode, companyService.getService().getServiceCd())) {
                isAllowedServiceStatus = true;
                break;
            }
        }

        if (!isAllowedServiceStatus) {
            validationResult.getMessages().ServiceStatusNotAllowed(EntityName.Service,
                    companyService != null ? companyService.getService().getServiceCd().toString() : "<null company service>",
                    onHoldReasonCode != null ? onHoldReasonCode.toString() : "<null onHoldReasonCode>",
                    companyService != null ? companyService.getService().getServiceCd().toString() : "<null company service>");
            return validationResult;
        }

        // Verify the new Service status code is valid for this source system
        if (!ServiceStatus.isAllowedServiceStatus(onHoldReasonCode, this.sourceSystemCode)) {
            validationResult.getMessages().ServiceStatusNotAllowedForSourceSystem(EntityName.SourceSystem,
                    this.sourceSystemCode.toString(), onHoldReasonCode.toString(), this.sourceSystemCode.toString());
            return validationResult;
        }

        //If the user is an Agent verify whether the user role allowed to put the customer into status or not        
        if (principal.isAgent()) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());
            ServiceSubStatus newServiceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCode);

            boolean isAllowedChangeType = newServiceSubStatus.isServiceSubStatusAllowedChangeTypeForUser(foundUser,
                    SubStatusChangeType.CanMoveToSubStatus);

            if (!isAllowedChangeType) {
                validationResult.getMessages().ServiceCannotMoveToServiceSubStatus(EntityName.AuthUser,
                        foundUser.getPrimaryRole().getRoleId(), newServiceSubStatus.getServiceSubStatusCd().toString(), foundUser.getPrimaryRole().getRoleId());
            }
        }

        if (company.hasService(ServiceCode.Tax)) { //based on having service since we need to apply to cancelled service if a tax payment returns, for instance
            addOnHoldStatusTax = new AddOnHoldStatusTax(company, onHoldReasonCode);
            validationResult.merge(addOnHoldStatusTax.validate());
        }

        if (company.isCompanyOnService(ServiceCode.ThirdParty401k)) {
            addOnHoldStatus401k = new AddOnHoldStatus401k(company, onHoldReasonCode);
            validationResult.merge(addOnHoldStatus401k.validate());
        }

        return validationResult;
    }

}

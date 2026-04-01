package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 21, 2008
 * Time: 1:26:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveOnHoldStatusCore extends Process implements IProcess {
    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private ServiceSubStatusCode onHoldReasonCode;
    private Company company;

    private RemoveOnHoldStatusTax removeOnHoldStatusTax;

    public RemoveOnHoldStatusCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
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
        company.removeOnHoldReason(onHoldReasonCode);

        if (removeOnHoldStatusTax != null) {
            processResult.merge(removeOnHoldStatusTax.process());
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

        PspPrincipal principal = Application.getCurrentPrincipal();
        if (principal.isAgent()) {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCode);
            if (!serviceSubStatus.getIsRemovedManually()) {
                validationResult.getMessages().OnHoldNotAllowedToRemoveManually(EntityName.ServiceSubStatus,
                        onHoldReasonCode.toString(), onHoldReasonCode.toString());
                return validationResult;
            }
        }

        // Verify company is on hold
        if (!company.isCompanyOnHold()) {
            validationResult.getMessages().CompanyNotInOnHoldStatus(EntityName.Company, sourceCompanyId,
                    sourceSystemCode.toString(), sourceCompanyId, onHoldReasonCode.toString());
        } else {
            if (company.getCurrentOnHoldReason(onHoldReasonCode) == null) {
                validationResult.getMessages().CompanyNotInOnHoldStatus(EntityName.Company, sourceCompanyId,
                        sourceSystemCode.toString(),
                        sourceCompanyId, onHoldReasonCode.toString());
                return validationResult;
            }
        }

        //If the user is an Agent verify whether the user role allowed to move the customer out of the status or not
        if (principal.isAgent()) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());
            ServiceSubStatus newServiceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCode);

            boolean isAllowedChangeType = newServiceSubStatus.isServiceSubStatusAllowedChangeTypeForUser(foundUser,
                    SubStatusChangeType.CanMoveFromSubStatus);

            if (!isAllowedChangeType) {
                validationResult.getMessages().ServiceCannotMoveFromServiceSubStatus(EntityName.AuthUser,
                        foundUser.getPrimaryRole().getRoleId(), newServiceSubStatus.getServiceSubStatusCd().toString(), foundUser.getPrimaryRole().getRoleId());
            }
        }

        if (company.isCompanyOnService(ServiceCode.Tax)) {
            removeOnHoldStatusTax = new RemoveOnHoldStatusTax(company, onHoldReasonCode);
            validationResult.merge(removeOnHoldStatusTax.validate());
        }

        return validationResult;
    }
}

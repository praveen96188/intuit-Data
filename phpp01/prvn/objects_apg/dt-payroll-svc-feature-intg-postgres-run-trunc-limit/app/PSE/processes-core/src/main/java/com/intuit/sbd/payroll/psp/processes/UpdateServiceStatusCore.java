package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 17, 2008
 * Time: 1:58:32 PM
 */
public class UpdateServiceStatusCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private ServiceCode mServiceCode;
    private ServiceSubStatusCode mServiceSubStatusCd;
    private Company mCompany;
    private CompanyService mCompanyService;
    private DeactivateServiceCore mCancelService;
    private TerminateServiceCore terminateService;
    private UpdateEftpsEnrollmentCore mUpdateEftpsEnrollmentCore = null;

    public UpdateServiceStatusCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                   ServiceCode pServiceCode, ServiceSubStatusCode pServiceSubStatusCd) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mServiceCode = pServiceCode;
        mServiceSubStatusCd = pServiceSubStatusCd;
    }

    public CompanyService getCompanyService() {
        return mCompanyService;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (mCompanyService.getStatusCd() != null && mCompanyService.getStatusCd().equals(mServiceSubStatusCd)) {
            return processResult;
        }

        /*
        PSRV001120 Inserting logic to allow a termianted company to transition to either cancel or active state
         */
        if (ServiceSubStatusCode.Terminated.equals(mCompanyService.getStatusCd())) {
            mCompanyService.updateCompanyServiceStatus(mServiceSubStatusCd);
        } else if (mServiceSubStatusCd.equals(ServiceSubStatusCode.Cancelled)) {
            processResult.merge(mCancelService.process());
        } else if (mServiceSubStatusCd.equals(ServiceSubStatusCode.Terminated)) {
            processResult.merge(terminateService.execute());
        } else {
            mCompanyService.updateCompanyServiceStatus(mServiceSubStatusCd);
        }

        //
        // Update the company's EFTPS enrollment (if appropriate)
        //
        if (mUpdateEftpsEnrollmentCore != null) {
            processResult.merge(mUpdateEftpsEnrollmentCore.process());
        }

        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify the company is associated with the specified Service
        mCompanyService = CompanyService.findCompanyService(mCompany, mServiceCode);

        if (mCompanyService == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company, mSourceCompanyId, this.mSourceSystemCode.toString(), mSourceCompanyId, mServiceCode.toString());
            return validationResult;
        }

        if (mCompanyService.getStatusCd() != null && mCompanyService.getStatusCd().equals(mServiceSubStatusCd)) {
            return validationResult;
        }

        //If the user is an Agent verify whether the user role allowed to put the customer into the status and
        // move the customer out of the status.
        PspPrincipal principal = Application.getCurrentPrincipal();

        if (principal.isAgent()) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());
            
            ServiceSubStatus newServiceSubStatus = Application.findById(ServiceSubStatus.class, mServiceSubStatusCd);
            ServiceSubStatus existingServiceSubStatus = Application.findById(ServiceSubStatus.class, mCompanyService.getStatusCd());

            boolean isAllowedChangeType = newServiceSubStatus.isServiceSubStatusAllowedChangeTypeForUser(foundUser,
                    SubStatusChangeType.CanMoveToSubStatus);

            boolean isAllowedChangeTypeForOldStatus = existingServiceSubStatus.isServiceSubStatusAllowedChangeTypeForUser(foundUser,
                    SubStatusChangeType.CanMoveFromSubStatus);

            if (!isAllowedChangeTypeForOldStatus) {
                validationResult.getMessages().ServiceCannotMoveFromServiceSubStatus(EntityName.AuthUser,
                        foundUser.getPrimaryRole().getRoleId(), existingServiceSubStatus.getServiceSubStatusCd().toString(), foundUser.getPrimaryRole().getRoleId());
            }
            
            if (!isAllowedChangeType) {
                validationResult.getMessages().ServiceCannotMoveToServiceSubStatus(EntityName.AuthUser,
                        foundUser.getPrimaryRole().getRoleId(), newServiceSubStatus.getServiceSubStatusCd().toString(), foundUser.getPrimaryRole().getRoleId());
            }

            if (!validationResult.isSuccess()) {
                return validationResult;
            }            
        }

        if (mServiceSubStatusCd == ServiceSubStatusCode.Cancelled && !ServiceSubStatusCode.Terminated.equals(mCompanyService.getStatusCd())) {
            // PSRV001120 - Moving company from Terminated to Cancel or Active
            // Only call the cancel routine if and only if the company is not currently terminated
            mCancelService = new DeactivateServiceCore(this.mSourceSystemCode, mSourceCompanyId, ServiceCode.valueOf(mServiceCode.toString()));
            validationResult.merge(mCancelService.validate());
        }
        else if (mServiceSubStatusCd == ServiceSubStatusCode.Terminated) {
            terminateService = new TerminateServiceCore(mSourceSystemCode, mSourceCompanyId, mServiceCode);
            validationResult.merge(terminateService.validate());
        }
        else {

            // Verify the new Service status code is valid for this service
            if (!ServiceStatus.isAllowedServiceStatus(mServiceSubStatusCd, mServiceCode)) {
                validationResult.getMessages().ServiceStatusNotAllowed(EntityName.Service, mServiceCode.toString(),
                        mServiceSubStatusCd.toString(), mServiceCode.toString());
                return validationResult;
            }

            // Verify the new Service status code is valid for this source system
            if (!ServiceStatus.isAllowedServiceStatus(mServiceSubStatusCd, this.mSourceSystemCode)) {
                validationResult.getMessages().ServiceStatusNotAllowedForSourceSystem(EntityName.SourceSystem,
                        this.mSourceSystemCode.toString(), mServiceSubStatusCd.toString(), this.mSourceSystemCode.toString());
                return validationResult;
            }

            ServiceSubStatus currentServiceSubStatus = Application.findById(ServiceSubStatus.class, mCompanyService.getStatusCd());
            if (!currentServiceSubStatus.getIsRemovedManually()) {
                validationResult.getMessages().ServiceStatusCannotBeChangedManually(EntityName.ServiceSubStatus,
                        mCompanyService.getStatusCd().toString(), mCompanyService.getStatusCd().toString());
                return validationResult;
            }

            // Verify the new service status can be set manually
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, mServiceSubStatusCd);
            if (!serviceSubStatus.getIsSetManually()) {
                validationResult.getMessages().ServiceStatusNotAllowedToSetManually(EntityName.ServiceSubStatus,
                        mServiceSubStatusCd.toString(), mServiceSubStatusCd.toString());
                return validationResult;
            }

            /*
                PSRV001120: Reworked switch statement for the new behavior on company status transitions  

                Active Current -> ActiveSeasonal
                Active Seasonal -> ActiveCurrent
                Terminated -> Cancelled
             */
            boolean canUpdate = true;
            switch (mCompanyService.getStatusCd()) {
                case ActiveCurrent:
                    if (ServiceSubStatusCode.ActiveSeasonal != (mServiceSubStatusCd)) {
                        canUpdate = false;
                    }
                    break;
                case ActiveSeasonal:
                    if (ServiceSubStatusCode.ActiveCurrent != (mServiceSubStatusCd)) {
                        canUpdate = false;
                    }
                    break;
                case Terminated:
                    if (ServiceSubStatusCode.Cancelled != (mServiceSubStatusCd)) {
                        canUpdate = false;
                    }
                    break;
            }

            if (!canUpdate) {
                validationResult.getMessages().ServiceStatusNotAllowedToChange(EntityName.ServiceStatus,
                        mServiceSubStatusCd.toString(), mServiceCode.toString(), mServiceSubStatusCd.toString());
                return validationResult;
            }
        }

        //
        // Check to see if we need to update their EFTPS enrollment
        //
        validationResult.merge(checkEftpsEnrollmentStatus());

                return validationResult;
            }

    private ProcessResult checkEftpsEnrollmentStatus() {
        ProcessResult validationResult = new ProcessResult();
        EftpsEnrollmentStatus newEftpsEnrollmentStatus = null;

        switch (mServiceSubStatusCd) {
            //
            // If we're cancelling/terminating Tax service, unenroll the client
            //
            case Cancelled:
            case Terminated:
                switch (mServiceCode) {
                    case Tax:
                            newEftpsEnrollmentStatus = EftpsEnrollmentStatus.Cancelled;
                        break;
                }
                break;

            //
            // If we're not cancelling/terminating Assisted or Tax service, reenroll the client (if appropriate)
            //
            default:
                switch (mServiceCode) {
                    case Tax:
                        //
                        // Only reenroll if the current service status (pre-update) is Cancelled or Terminated
                        // (in other words, if we're reactivating Assisted/Tax service)
                        //
                        switch (mCompanyService.getStatusCd()) {
                            case Cancelled:
                            case Terminated:
                                //
                                // We're transitioning away from Cancelled/Terminated, so reenroll (if appropriate)
                                //
                                newEftpsEnrollmentStatus = EftpsEnrollmentStatus.PendingEnrollment;
                                break;
                        }
                        break;
        }
                break;
        }

        if ((newEftpsEnrollmentStatus != null) && (mCompany.getCurrentEnrollmentStatus() != null)) {
            EftpsEnrollment eftpsEnrollment = mCompany.getCurrentEnrollment();

            if (eftpsEnrollment.isAllowedTransition(newEftpsEnrollmentStatus)) {
                boolean updateEftpsEnrollment = false;

                //
                // Check the business rules to determine whether we need to make an eftps enrollment adjustment.
                //
                switch (newEftpsEnrollmentStatus) {
                    case Cancelled:
                        //
                        // We don't auto-cancel eftps enrollment on service cancel if they are already Enrolled
                        // (in this case, an agent will need to manually cancel the eftps enrollment via DDM)
                        //
                        switch (eftpsEnrollment.getStatusCd()) {
                            case PendingEnrollment:
                            case PendingAcceptance:
                            case Rejected:
                            case Invalid:
                            case AgedOut:
                                updateEftpsEnrollment = true;
                            break;
                        }
                        break;

                    case PendingEnrollment:
                        //
                        // Only re-enroll the client if they're currently Cancelled.
                        //
                        switch (eftpsEnrollment.getStatusCd()) {
                            case Cancelled:
                                updateEftpsEnrollment = true;
                                break;
                        }
                        break;
                }

                if (updateEftpsEnrollment) {
                    mUpdateEftpsEnrollmentCore = new UpdateEftpsEnrollmentCore(mCompany.getSourceSystemCd(),
                                                                               mCompany.getSourceCompanyId(),
                                                                               newEftpsEnrollmentStatus);
                    validationResult.merge(mUpdateEftpsEnrollmentCore.validate());
                }
            }
        }

        return validationResult;
    }
}

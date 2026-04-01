package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 28, 2008
 * Time: 11:22:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateRoleSubStatusCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private ServiceCode mServiceCode;
    private DomainEntitySet<ServiceSubStatus> mSubStatusList = new DomainEntitySet<ServiceSubStatus>();
    private CompanyService mCompanyService;
    private UpdateServiceStatusCore mUpdateServiceStatus;
    private List<AddOnHoldStatusCore> mAddOnHoldProcessList = new ArrayList<AddOnHoldStatusCore>();
    private List<RemoveOnHoldStatusCore> mRemoveOnHoldProcessList = new ArrayList<RemoveOnHoldStatusCore>();
    private Company mCompany;
    ServiceStatusCode mServiceStatusCode;

    public UpdateRoleSubStatusCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, ServiceCode pServiceCode,
                                   DomainEntitySet<ServiceSubStatus> pSubStatusList) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mServiceCode = pServiceCode;
        if (pSubStatusList != null){
            this.mSubStatusList.addAll(pSubStatusList);            
        }
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Verify the company is associated with the specified Service
        mCompanyService = CompanyService.findCompanyService(mCompany, mServiceCode);

        if (mCompanyService == null) {
            validationResult.getMessages().CompanyNotAssociatedWithService(EntityName.Company,
                    mSourceCompanyId, this.mSourceSystemCd.toString(),
                    mSourceCompanyId, mServiceCode.toString());

            return validationResult;
        }

        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

        if (principal.isAgent() && mSubStatusList != null && mSubStatusList.size() > 0) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());
            String userRoleId = foundUser.getPrimaryRole().getRoleId();

            //Validate move-from authorization
            if (mCompany.isCompanyOnHold())
            {
                Collection<ServiceSubStatusCode> onHoldReasons = mCompany.getCurrentOnHoldReasonCodes();
                DomainEntitySet<ServiceSubStatus> toRemoveList = new DomainEntitySet<ServiceSubStatus>();

                for (ServiceSubStatusCode subStatusCd : onHoldReasons) {
                    ServiceSubStatus subStatus = Application.findById(ServiceSubStatus.class, subStatusCd);
                    if (!mSubStatusList.contains(subStatus)) {
                        //Check if we have access to remove it
                        if (!subStatus.canMoveFromSubStatus()) {
                             validationResult.getMessages().ServiceStatusNotAllowedForRole(EntityName.AuthUser,
                            userRoleId, subStatus.getServiceSubStatusCd().toString(), userRoleId);
                        }
                    }
                }

                //To add it
                for (ServiceSubStatus subStatus : mSubStatusList) {
                    ServiceSubStatusCode subStatusCode = subStatus.getServiceSubStatusCd();
                    if (!onHoldReasons.contains(subStatusCode)) {
                        //Check if we have access to add it
                        if (!subStatus.canMoveToSubStatus()) {
                             validationResult.getMessages().ServiceStatusNotAllowedForRole(EntityName.AuthUser,
                            userRoleId, subStatus.getServiceSubStatusCd().toString(), userRoleId);
                        }
                    }
                }

            } else {

                 //Can move from single substatus
                ServiceSubStatus currentSubStatus = Application.findById(ServiceSubStatus.class, mCompanyService.getStatusCd());
                if (!currentSubStatus.canMoveFromSubStatus()) {
                             validationResult.getMessages().ServiceStatusNotAllowedForRole(EntityName.AuthUser,
                            userRoleId, currentSubStatus.getServiceSubStatusCd().toString(), userRoleId);
                }


                //Can move to substatuses
                for (ServiceSubStatus subStatus : mSubStatusList) {
                    if (!subStatus.canMoveToSubStatus()) {
                        //Check if we have access to add it
                        if (!subStatus.canMoveFromSubStatus()) {
                             validationResult.getMessages().ServiceStatusNotAllowedForRole(EntityName.AuthUser,
                            userRoleId, subStatus.getServiceSubStatusCd().toString(), userRoleId);
                        }
                    }
                }


            }

            if (!validationResult.isSuccess()){
                return validationResult;
            }
        }

        if (mSubStatusList != null && mSubStatusList.size() > 0) {
            mServiceStatusCode = mSubStatusList.get(0).getServiceStatus().getServiceStatusCd();
            if (!mServiceStatusCode.equals(ServiceStatusCode.OnHold) && mSubStatusList.size() > 1) {
                validationResult.getMessages().CannotAddMuiltipleSubStatuses(EntityName.CompanyService, mServiceStatusCode.toString(),
                        mServiceStatusCode.toString());

                return validationResult;
            }

            if (mServiceStatusCode.equals(ServiceStatusCode.OnHold)) {
                validationResult.merge(validateOnHoldReasons());
            } else {
                mUpdateServiceStatus = new UpdateServiceStatusCore(mSourceSystemCd, mSourceCompanyId,
                        mServiceCode, mSubStatusList.get(0).getServiceSubStatusCd());
                validationResult.merge(mUpdateServiceStatus.validate());
            }
        } else {
            //Check if removing from on hold status
            Collection<OnHoldReason> onHoldReasons = mCompany.getCurrentOnHoldReasons();

            //They are on hold, so we should remove them from this state because they are passing
            //no substatuses.
            if (onHoldReasons.size() > 0)
            {
                mServiceStatusCode = ServiceStatusCode.OnHold;
                validationResult.merge(validateOnHoldReasons());
            } else {
                validationResult.getMessages()
                        .RequiredInputMissingOrBlank(EntityName.ServiceSubStatus, null, "ServiceSubStatusList");
                return validationResult;
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (mServiceStatusCode.equals(ServiceStatusCode.OnHold)) {
            for (RemoveOnHoldStatusCore processCore : mRemoveOnHoldProcessList) {
                processResult.merge(processCore.process());
            }

            for (AddOnHoldStatusCore processCore : mAddOnHoldProcessList) {
                processResult.merge(processCore.process());
            }
        } else {
            processResult.merge(mUpdateServiceStatus.process());
        }

        return processResult;
    }

    private ProcessResult validateOnHoldReasons() {
        ProcessResult validationResult = new ProcessResult();

        Collection<OnHoldReason> onHoldReasons = mCompany.getCurrentOnHoldReasons();

        for (OnHoldReason onHoldReason : onHoldReasons) {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReason.getOnHoldReasonCd());
            if (!mSubStatusList.contains(serviceSubStatus)) {
                RemoveOnHoldStatusCore removeOnHoldStatus = new RemoveOnHoldStatusCore(mSourceSystemCd, mSourceCompanyId, onHoldReason.getOnHoldReasonCd());
                validationResult.merge(removeOnHoldStatus.validate());
                mRemoveOnHoldProcessList.add(removeOnHoldStatus);
            }

            if (mSubStatusList.contains(serviceSubStatus)) {
                mSubStatusList.remove(serviceSubStatus);
            }
        }

        for (ServiceSubStatus serviceSubStatus : mSubStatusList) {
            if (!onHoldReasons.contains(serviceSubStatus)) {
                AddOnHoldStatusCore addOnHoldStatus = new AddOnHoldStatusCore(mSourceSystemCd, mSourceCompanyId, serviceSubStatus.getServiceSubStatusCd());
                validationResult.merge(addOnHoldStatus.validate());
                mAddOnHoldProcessList.add(addOnHoldStatus);
            }
        }

        return validationResult;
    }
}

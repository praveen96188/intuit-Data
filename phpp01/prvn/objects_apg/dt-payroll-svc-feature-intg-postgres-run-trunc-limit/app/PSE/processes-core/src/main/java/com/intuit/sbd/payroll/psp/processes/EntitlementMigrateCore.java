package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 16, 2010
 * Time: 3:08:02 PM
 */
public class EntitlementMigrateCore extends Process implements IProcess {
    private Entitlement mOldEntitlement;
    private EntitlementDTO mNewEntitlementDTO;
    private AddEntitlementCore mAddEntitlementCore;

    public EntitlementMigrateCore(Entitlement pOldEntitlement, EntitlementDTO pEntitlementDTO) {
        mOldEntitlement = pOldEntitlement;
        mNewEntitlementDTO = pEntitlementDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mOldEntitlement == null) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, "null", "OldEntitlement");
            return validationResult;
        }

        if (mNewEntitlementDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, "null", "NewEntitlementDTO");
            return validationResult;
        }

        // Disable old entitlement if it's not already disabled
        if (!mOldEntitlement.isDisabled()) {
            mOldEntitlement.setEntitlementState(EntitlementStateCode.Disabled);
        }

        // if the new asset item number is the same as the old one move the subscription number from the old entitlement
        // to the new one to prevent new service keys from being created.
        if (mNewEntitlementDTO.getAssetItemNumber().equals(mOldEntitlement.getEntitlementCode().getAssetItemNumber())) {
            mNewEntitlementDTO.setSubscriptionNumber(mOldEntitlement.getSubscriptionNumber());
            mOldEntitlement.setSubscriptionNumber(PayrollServices.entitlementManager.createSubscriptionNumber());
        }

        mAddEntitlementCore = new AddEntitlementCore(mNewEntitlementDTO);
        validationResult.merge(mAddEntitlementCore.validate());

        return validationResult;
    }

    @Override
    public ProcessResult<Entitlement> process() {
        ProcessResult<Entitlement> processResult = new ProcessResult<Entitlement>();

        // Update old entitlement
        mOldEntitlement = Application.save(mOldEntitlement);

        // Create New Entitlement
        ProcessResult<Entitlement> addEntitlementCorePR = mAddEntitlementCore.process();
        processResult.merge(addEntitlementCorePR);
        if (!processResult.isSuccess()) {
            return processResult;
        }
        Entitlement newEntitlement = addEntitlementCorePR.getResult();
        processResult.setResult(newEntitlement);

        // Move all entitlement units from the old entitlement to the new one
        for (EntitlementUnit entitlementUnit : mOldEntitlement.getEntitlementUnitCollection()) {

            // Do not move historic entitlement units to the new entitlement.
            if (entitlementUnit.isHistoric()) {
                continue;
            }

            if (! newEntitlement.getEntitlementCode().equals(mOldEntitlement.getEntitlementCode())) {
                CompanyEvent.createEntitlementCodeChangedEvent(entitlementUnit.getCompany(), mOldEntitlement.getEntitlementCode(), newEntitlement.getEntitlementCode());
            }
            //DIY to Assisted
            if (!mOldEntitlement.getEntitlementCode().isAssisted() && newEntitlement.getEntitlementCode().isAssisted()) {
                if (EntitlementUnitStatusCode.PendingActivation.equals(entitlementUnit.getEntitlementUnitStatus()) ||
                    EntitlementUnitStatusCode.ErrorActivating.equals(entitlementUnit.getEntitlementUnitStatus())) {
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
                }
            }

            //todo if the entitlementUnit is already active and they move from DIY to Assisted what should we do? Create a new EU and deactivate the old one?

            //Assisted to DIY
            if (mOldEntitlement.getEntitlementCode().isAssisted() && !newEntitlement.getEntitlementCode().isAssisted()) {
                if (EntitlementUnitStatusCode.ActivationHold.equals(entitlementUnit.getEntitlementUnitStatus())) {
                    entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
                }
            }

            entitlementUnit.setEntitlement(newEntitlement);
            Application.save(entitlementUnit);
        }

        return processResult;
    }
}

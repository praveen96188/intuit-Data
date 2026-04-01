package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 6:31:41 PM
 */
public class UpdateEntitlementCore extends Process implements IProcess {
    private EntitlementDTO mEntitlementDTO;
    private Entitlement mEntitlement;
    private EntitlementCode mEntitlementCode;

    public UpdateEntitlementCore(EntitlementDTO pEntitlementDTO) {
        mEntitlementDTO = pEntitlementDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mEntitlementDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, "null", "EntitlementDTO");
            return validationResult;
        }

        validationResult.merge(mEntitlementDTO.validateUpdate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mEntitlement = Entitlement.findEntitlement(mEntitlementDTO.getLicenseNumber(), mEntitlementDTO.getEntitlementOfferingCode(), true);
        if (mEntitlement == null) {
            validationResult.getMessages().EntitlementDoesNotExist(EntityName.Entitlement, "Entitlement", mEntitlementDTO.getLicenseNumber(), mEntitlementDTO.getEntitlementOfferingCode());
            return validationResult;
        }

        if(mEntitlementDTO.getAssetItemNumber() != null) {
            // cannot update an entitlement to a new asset type
            if (!mEntitlement.getEntitlementCode().getAssetItemNumber().equals(mEntitlementDTO.getAssetItemNumber())) {
                validationResult.getMessages().InvalidValue(EntityName.Entitlement, mEntitlementDTO.getAssetItemNumber(), "AssetItemNumber");
                return validationResult;
            }

            mEntitlementCode = EntitlementCode.findEntitlementCode(mEntitlementDTO.getAssetItemNumber(), mEntitlementDTO.getEditionType(), mEntitlementDTO.getNumberOfEmployeesType());
            if (mEntitlementCode == null) {
                validationResult.getMessages().EntitlementCodeDoesNotExist(EntityName.EntitlementCode, "EntitlementCode", mEntitlementDTO.getAssetItemNumber(), mEntitlementDTO.getEditionType(), mEntitlementDTO.getNumberOfEmployeesType());
                return validationResult;
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<Entitlement> process() {
        ProcessResult<Entitlement> processResult = new ProcessResult<Entitlement>();

        //Check to see if the subtype is changing from a dummy value to the right one
        long newEntitlementCodeSubtype =0;
        long currentEntitlementCodeSubtype =0;
        if (mEntitlement.getEntitlementCode()!=null) {
            currentEntitlementCodeSubtype = mEntitlement.getEntitlementCode().getQuickBooksSubtype();
        }
        if(mEntitlementCode!=null) {
            newEntitlementCodeSubtype = mEntitlementCode.getQuickBooksSubtype();
        }
        boolean isChangingToNonDummyEntCode = (currentEntitlementCodeSubtype==0 && newEntitlementCodeSubtype!=0);

        // update domain entity
        processResult.merge(mEntitlementDTO.copyDTOToDomain(mEntitlement, mEntitlementCode));
        mEntitlement = Application.save(mEntitlement);

        for (EntitlementUnit entitlementUnit : mEntitlement.getEntitlementUnitCollection()) {
            if(entitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingDeactivation && mEntitlement.isDisabled()) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
                Application.save(entitlementUnit);
            }

            if(entitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.DeactivationHold && !mEntitlement.isDisabled()) {
                entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
                Application.save(entitlementUnit);
            }

            if(!entitlementUnit.isDeactivated()) {
                // use sub set of entitlement unit information
                EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO(entitlementUnit.getFedTaxId(),
                                                                               entitlementUnit.getServiceKey(),
                                                                               entitlementUnit.getExtensionKey(),
                                                                               mEntitlement.getNextChargeDate());
                entitlementUnitDTO.generateNewServiceKey(entitlementUnit.getCompany(),
                                                         mEntitlement,
                                                         mEntitlement.getEntitlementCode());

                if (entitlementUnitDTO.getServiceKey() != null) {
                    entitlementUnit.setServiceKey(entitlementUnitDTO.getServiceKey());
                    entitlementUnit.setExtensionKey(entitlementUnitDTO.getExtensionKey());
                    Application.save(entitlementUnit);
                }

                CompanyOffering.updateCompanyOffering(entitlementUnit.getCompany(), entitlementUnit);
            }
        }

        processResult.setResult(mEntitlement);

        return processResult;
    }


}

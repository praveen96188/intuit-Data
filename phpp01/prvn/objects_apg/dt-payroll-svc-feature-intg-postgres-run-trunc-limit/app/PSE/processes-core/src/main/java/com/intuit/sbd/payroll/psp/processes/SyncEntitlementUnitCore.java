package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/6/12
 * Time: 11:10 AM
 */
public class SyncEntitlementUnitCore extends Process implements IProcess {

    private Company mCompany;
    private SpcfUniqueId mEntitlementUnitId;
    private EntitlementUnitDTO mEntitlementUnitDTO;
    private EntitlementUnit mEntitlementUnit;
    private Entitlement mEntitlement;
    private EntitlementCode mExistingEntitlementCode;
    private EntitlementCode mNewEntitlementCode;

    public SyncEntitlementUnitCore(SpcfUniqueId pEntitlementUnitId, EntitlementUnitDTO pEntitlementUnitDTO) {
        mEntitlementUnitId = pEntitlementUnitId;
        mEntitlementUnitDTO = pEntitlementUnitDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mEntitlementUnitId == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementUnit, "null", "pEntitlementUnitId");
            return validationResult;
        }

        if (mEntitlementUnitDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementUnit, "null", "pEntitlementUnitDTO");
            return validationResult;
        }

        mEntitlementUnit = Application.findById(EntitlementUnit.class, mEntitlementUnitId);
        if (mEntitlementUnit == null) {
            validationResult.getMessages().EntitlementUnitDoesNotExist(EntityName.EntitlementUnit, null, mEntitlementUnitDTO.getFedTaxId(), mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode());
            return validationResult;
        }

        mCompany = mEntitlementUnit.getCompany();
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.EntitlementUnit, mEntitlementUnit.getId().toString(), null, null);
            return validationResult;
        }

        mEntitlement = mEntitlementUnit.getEntitlement();
        if (mEntitlement == null) {
            validationResult.getMessages().EntitlementDoesNotExist(EntityName.Entitlement, null, mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode());
            return validationResult;
        }

        DomainEntitySet<EntitlementUnit> entitlementUnits = EntitlementUnit.findActiveEntitlementUnits(mEntitlementUnit.getFedTaxId(), mEntitlement.getLicenseNumber(), mEntitlement.getEntitlementOfferingCode());
        if (entitlementUnits.size() > 1) {
            validationResult.getMessages().EntitlementUnitExistsWithSameFEIN(EntityName.EntitlementUnit, null, mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode(), mEntitlementUnitDTO.getFedTaxId());
            return validationResult;
        }

        mExistingEntitlementCode = mEntitlement.getEntitlementCode();
        if (mExistingEntitlementCode == null) {
            validationResult.getMessages().EntitlementCodeDoesNotExist(EntityName.Entitlement, mEntitlement.getId().toString(), null, null, null);
            return validationResult;
        }

        mNewEntitlementCode = EntitlementCode.findEntitlementCode(mEntitlementUnitDTO.getAssetItemNumber(), mEntitlementUnitDTO.getEditionType(), mEntitlementUnitDTO.getNumberOfEmployeesType());
        if (mNewEntitlementCode == null) {
            validationResult.getMessages().EntitlementCodeDoesNotExist(EntityName.EntitlementCode, mCompany.getSourceCompanyId(), mEntitlementUnitDTO.getAssetItemNumber(), mEntitlementUnitDTO.getEditionType(), mEntitlementUnitDTO.getNumberOfEmployeesType());
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult<EntitlementUnit> processResult = new ProcessResult<EntitlementUnit>();

        mEntitlementUnitDTO.setServiceKey(mEntitlementUnit.getServiceKey());

        if (!mExistingEntitlementCode.getId().equals(mNewEntitlementCode.getId())) {
            if (AssetItemCode.DIYDiskDelivery.in(mExistingEntitlementCode.getAssetItemCd(), mNewEntitlementCode.getAssetItemCd())) {
                mEntitlementUnitDTO.generateNewServiceKey(mCompany, mEntitlement, mNewEntitlementCode);
            }
        }

        long existingSubType = 0;
        long newSubType = 0;
        if (mExistingEntitlementCode != null) {
            existingSubType = mExistingEntitlementCode.getQuickBooksSubtype();
        }
        if(mExistingEntitlementCode != null) {
            newSubType = mExistingEntitlementCode.getQuickBooksSubtype();
        }
        boolean isChangingToNonDummyEntCode = (existingSubType == 0 && newSubType != 0);

        processResult.merge(mEntitlementUnitDTO.copyDTOToDomain(mEntitlement, mNewEntitlementCode, mEntitlementUnit));

        if(!mEntitlementUnit.isDeactivated()) {
            CompanyOffering.updateCompanyOffering(mCompany, mEntitlementUnit);
        }

        mEntitlement = Application.save(mEntitlement);
        mEntitlementUnit = Application.save(mEntitlementUnit);

        processResult.setResult(mEntitlementUnit);

        return processResult;
    }


}

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 28, 2010
 * Time: 1:07:11 PM
 */
public class AddOrUpdateEntitlementUnitCore extends Process implements IProcess {
    private static final SpcfLogger logger = Application.getLogger(AddOrUpdateEntitlementUnitCore.class);
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private Company mCompany;
    private EntitlementUnitDTO mEntitlementUnitDTO;
    private EntitlementUnit mEntitlementUnit;
    private Entitlement mEntitlement;
    private EntitlementCode mEntitlementCode;

    private List<EntitlementUnit> mDeactivatedEntitlementUnits;

    private AddEntitlementCore mAddEntitlementCore = null;
    private boolean mNewEntitlementUnit;
    EntitlementUnit mLastDeactivatedEU = null;
    boolean mAllPrevEUsDeactivatedOrHistoric = false;

    public AddOrUpdateEntitlementUnitCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EntitlementUnitDTO pEntitlementUnitDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mEntitlementUnitDTO = pEntitlementUnitDTO;

        mDeactivatedEntitlementUnits = new ArrayList<EntitlementUnit>();
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mEntitlementUnitDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementUnit, "null", "EntitlementUnitDTO");
            return validationResult;
        }

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        // is new entitlement?
        mNewEntitlementUnit = false;
        mEntitlementCode = EntitlementCode.findEntitlementCode(mEntitlementUnitDTO.getAssetItemNumber(), mEntitlementUnitDTO.getEditionType(), mEntitlementUnitDTO.getNumberOfEmployeesType());
        mEntitlement = Entitlement.findEntitlement(mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode());
        mEntitlementUnit = null;
        if(mEntitlement != null) {
            //cannot add second EU to Assisted asset
            mEntitlementUnit = mCompany.getEntitlementUnit(mEntitlement, mEntitlementUnitDTO.getFedTaxId());
            if(mEntitlementUnit == null &&
                    mEntitlement.getEntitlementCode().getAssetItemCd() == AssetItemCode.Assisted)
            {
                if (mEntitlement.getEntitlementCode().isDiamondAssisted() &&
                        mEntitlement.getActiveEntitlementUnitCollection().size() >= 3) {
                    validationResult.getMessages().MaxDiamondAssistedEntitlementUnitsAlreadyExist(EntityName.Entitlement, mEntitlement.getId().toString(), mEntitlementUnitDTO.getFedTaxId());
                    return validationResult;
                } else if (!mEntitlement.getEntitlementCode().isDiamondAssisted() &&
                        mEntitlement.getActiveEntitlementUnitCollection().size() > 0) {
                    validationResult.getMessages().ActiveAssistedEntitlementAlreadyExists(EntityName.Entitlement, mEntitlement.getId().toString(), mEntitlementUnitDTO.getFedTaxId());
                    return validationResult;
                }
            } else {
                if (mEntitlement.getEntitlementCode().isDiamondAssisted() &&
                        mEntitlementUnitDTO.getEntitlementUnitStatus().in( EntitlementUnitStatusCode.PendingActivation,
                                EntitlementUnitStatusCode.PendingReactivation) &&
                        mEntitlement.getActiveEntitlementUnitCollection().size() >= 3 ) {
                    validationResult.getMessages().MaxDiamondAssistedEntitlementUnitsAlreadyExist(EntityName.Entitlement, mEntitlement.getId().toString(), mEntitlementUnitDTO.getFedTaxId());
                    return validationResult;
                }
            }

            //cannot add second EU with same EIN
            EntitlementUnit activeEntitlementUnitByFedTaxId = EntitlementUnit.getActiveEntitlementUnitByFedTaxId(mEntitlementUnitDTO.getFedTaxId(), mEntitlement);
            if(activeEntitlementUnitByFedTaxId != null && mEntitlementUnitDTO.getEntitlementUnitStatus().in(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES) &&
                    (!activeEntitlementUnitByFedTaxId.equals(mEntitlementUnit) || mEntitlementUnit.getEntitlementUnitStatus().notIn(EntitlementUnit.ACTIVE_ENTITLEMENT_UNIT_STATUSES))) {
                validationResult.getMessages().EntitlementUnitExistsWithSameFEIN(EntityName.Entitlement, null, mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode(), mEntitlementUnitDTO.getFedTaxId());
                return validationResult;
            }

            //cannot activate EU on disabled entitlement
            long errorCount = mEntitlementUnit == null ? 0 : mEntitlementUnit.getErrorCount();
            if (mEntitlementUnitDTO.getEntitlementUnitStatus().in(EntitlementUnitStatusCode.Activated,
                                                                      EntitlementUnitStatusCode.PendingActivation,
                                                                      EntitlementUnitStatusCode.PendingReactivation,
                                                                      EntitlementUnitStatusCode.ActivationHold) &&
                    EntitlementStateCode.Disabled.equals(mEntitlement.getEntitlementState()) &&
                    EntitlementStateCode.Disabled.equals(mEntitlementUnitDTO.getEntitlementState()) &&
                    (mEntitlementUnitDTO.getErrorCount() == errorCount ||
                            mEntitlementUnitDTO.getErrorCount() == 0)) {
                validationResult.getMessages().EntitlementDisabled(EntityName.Entitlement, null, mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode(), mEntitlementUnitDTO.getFedTaxId());
                return validationResult;
            }

        } else {
            // don't allow more than one active "primary" entitlement
            if(mEntitlementCode != null && mEntitlementCode.getIsPrimary()) {
                EntitlementUnit activeEntitlementUnit = mCompany.getActivePrimaryEntitlementUnit();
                if(activeEntitlementUnit != null) {
                    validationResult.getMessages().ActivePrimaryEntitlementAlreadyExists(EntityName.Entitlement, activeEntitlementUnit.getEntitlement().getId().toString(), mCompany.getSourceCompanyId(), activeEntitlementUnit.getEntitlement().getEntitlementCode().getAssetItemCd().toString());
                    return validationResult;
                }
            }

            mAddEntitlementCore = new AddEntitlementCore(mEntitlementUnitDTO);
            validationResult.merge(mAddEntitlementCore.validate());
        }

        if(!validationResult.isSuccess()) {
            return validationResult;
        }

        if(mEntitlementUnit == null) {
            mNewEntitlementUnit = true;
            validationResult.merge(mEntitlementUnitDTO.validateAdd());

            DomainEntitySet<EntitlementUnit> entitlementUnits = EntitlementUnit.findEntitlementUnits(mEntitlementUnitDTO.getFedTaxId(), mEntitlementUnitDTO.getLicenseNumber(), mEntitlementUnitDTO.getEntitlementOfferingCode());
            for (EntitlementUnit entitlementUnit : entitlementUnits) {
                if (entitlementUnit.isDeactivated()) {
                    mDeactivatedEntitlementUnits.add(entitlementUnit);
                }
            }
            this.checkIfAllPrevEUsDeactivatedOrHistoric(mEntitlementUnitDTO.getFedTaxId());
        } else {
            validationResult.merge(mEntitlementUnitDTO.validateUpdate());
            if (!mEntitlement.getEntitlementCode().getAssetItemNumber().equals(mEntitlementUnitDTO.getAssetItemNumber())) {
                validationResult.getMessages().InvalidValue(EntityName.Entitlement, mEntitlementUnitDTO.getAssetItemNumber(), "AssetItemNumber");
                return validationResult;
            }
        }

        return validationResult;
    }
    @Override
    public ProcessResult<EntitlementUnit> process() {
        ProcessResult<EntitlementUnit> processResult = new ProcessResult<EntitlementUnit>();

        if(mAddEntitlementCore != null) {
            @SuppressWarnings("unchecked")
            ProcessResult<Entitlement> entitlementProcessResult = mAddEntitlementCore.execute();
            mEntitlement = entitlementProcessResult.getResult();
            // NTTF (Add EIN) -  New Entitlement
            if(isEligibleForFundingModelUpdate()){
                processResult = updateFundingModel(processResult);
            }
        }

        if(mNewEntitlementUnit) {
            mEntitlementUnit = new EntitlementUnit();
            mEntitlementUnit.setEntitlement(mEntitlement);
            mEntitlementUnit.setEntitlementUnitStatus(null);
            mEntitlementUnit.setCompany(mCompany);
            mCompany.addEntitlementUnit(mEntitlementUnit);

            mEntitlementUnitDTO.generateNewServiceKey(mCompany, mEntitlement, mEntitlementCode);
            mEntitlementUnitDTO.setErrorCount(0);
            // Add to EIN and Move EIN
            if(isEligibleForFundingModelUpdate()){
                processResult = updateFundingModel(processResult);
            }
        } else {
            mEntitlementUnitDTO.setServiceKey(mEntitlementUnit.getServiceKey());
        }

        long currentEntitlementCodeSubtype = 0;
        long newEntitlementCodeSubtype =0;
        if (mEntitlement.getEntitlementCode()!=null) {
            currentEntitlementCodeSubtype = mEntitlement.getEntitlementCode().getQuickBooksSubtype();
        }
        if(mEntitlementCode!=null) {
            newEntitlementCodeSubtype = mEntitlementCode.getQuickBooksSubtype();
        }
        boolean isChangingToNonDummyEntCode = (currentEntitlementCodeSubtype==0 && newEntitlementCodeSubtype!=0);

        processResult.merge(mEntitlementUnitDTO.copyDTOToDomain(mEntitlement, mEntitlementCode, mEntitlementUnit));

        CompanyService taxService = mCompany.getCompanyService(ServiceCode.Tax)  ;
        if(mEntitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingActivation &&
                mEntitlement.getEntitlementCode().isAssisted() && (taxService!=null && !taxService.isActive()))  {
            mEntitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.ActivationHold);
        }

        if(mEntitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingDeactivation &&
                mEntitlement.isDisabled()) {
            mEntitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        }

        if(mEntitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.DeactivationHold &&
                !mEntitlement.isDisabled()) {
            mEntitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        }

        CompanyOffering.updateCompanyOffering(mCompany, mEntitlementUnit);

        //Any existing entitlement units with the same License, EOC and FEIN should be updated to historic
        for (EntitlementUnit entitlementUnit : mDeactivatedEntitlementUnits) {
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Historic);
            Application.save(entitlementUnit);
        }

        mEntitlement = Application.save(mEntitlement);
        mEntitlementUnit = Application.save(mEntitlementUnit);
        logger.debug("Entitlement and  EntitlementUnit added"+mEntitlement);

       if (mNewEntitlementUnit == true && mAllPrevEUsDeactivatedOrHistoric && mLastDeactivatedEU != null) { // send event only if a new EU is being created for this company and all its prev ones are deactivated or made historic
            if (mLastDeactivatedEU != null) {
                // create NewPSIDCreatedForExistingCustomer
                CompanyEvent.createCompanyInfoChangeEvent(mCompany, mLastDeactivatedEU.getCompany().getSourceCompanyId(), mCompany.getSourceCompanyId(), EventTypeCode.NewPSIDCreatedForExistingCustomer);
            }
        }
        if(mNewEntitlementUnit && mEntitlement!=null ) {

            mEntitlementUnit.setEntitlement(mEntitlement);
            if(mEntitlement.getEntitlementCode().isAssisted()){
                CompanyEvent.createEntitlementUnitAddedEventAssisted(mEntitlementUnit,
                        EventEmailTemplateTypeCode.SKAssistedKey1);
            }
            else if((mEntitlement.getEntitlementUnitCollection().size()>=1 )){
                CompanyEvent.createEntitlementUnitAddedEvent(mEntitlementUnit,
                        EventEmailTemplateTypeCode.NewPayrollAccountAddedToEntitlement);
            }
       }
        processResult.setResult(mEntitlementUnit);
        return processResult;
    }

    private boolean isEligibleForFundingModelUpdate() {
        if (!FeatureFlags.get().booleanValue(FeatureFlags.Key.PSP_ENABLE_NEXT_DAY_FOR_NTTF_CUSTOMERS, true)) {
            logger.info("ENABLE_NEXT_DAY_FOR_NTTF_CUSTOMERS is Off and is not eligible for funding model update. PSID="+ mSourceCompanyId);
            return false;
        }
        logger.info("ENABLE_NEXT_DAY_FOR_NTTF_CUSTOMERS is On. PSID="+ mSourceCompanyId);

        if(CollectionUtils.isNotEmpty(mCompany.findPendingPayrolls())){
            logger.info("Company has pending payrolls so it's not eligible for funding model update. PSID="+ mSourceCompanyId);
            return false;
        }
        return true;
    }

    private ProcessResult<EntitlementUnit> updateFundingModel(ProcessResult<EntitlementUnit> processResult) {
        try {
            logger.info("Updating company funding model for the PSID=" + mSourceCompanyId);
            String fundingModelCode = (mEntitlement.getEntitlementCode().getAssetItemCd() == AssetItemCode.Assisted ||
                    mEntitlement.getEntitlementCode().getAssetItemCd() == AssetItemCode.AssistedAdvantage) ?
                    FundingModel.Codes.ONE_DAY : FundingModel.Codes.TWO_DAY;
            FundingModel fundingModel = Application.findById(FundingModel.class, fundingModelCode);
            ProcessResult processResultUpdateFundingModel = PayrollServices.companyManager.
                    updateCompanyFundingModel(mSourceSystemCode, mSourceCompanyId, fundingModel);

            processResult.addMessages(processResultUpdateFundingModel.getMessages());
            if (!processResult.isSuccess()) {
                logger.error("Unable to update company funding model for the PSID=" + mSourceCompanyId + " because " + processResult.getErrorMessages());
            } else {
                logger.info("Successfully updated company funding model for PSID=" + mSourceCompanyId);
            }
        } catch (Exception e) {
            logger.error("Failed to update company funding model. PSID="+mSourceCompanyId + e);
        }
        return processResult;
    }


    // checks if all prev EUs for the given EIN are deactivated or made historic
    // and saves the last deactivated/historic EU for later use
    private void checkIfAllPrevEUsDeactivatedOrHistoric(String fedTaxId) {
        DomainEntitySet<EntitlementUnit> entitlementUnits = EntitlementUnit.findEntitlementUnits(fedTaxId);
        int deactivatedAndHistoricEUCount = 0;
        for (EntitlementUnit entitlementUnit : entitlementUnits) {
            if (entitlementUnit.isDeactivated() || entitlementUnit.isHistoric()) {
                if (mLastDeactivatedEU == null || entitlementUnit.getModifiedDate().after(mLastDeactivatedEU.getModifiedDate())) {
                    mLastDeactivatedEU = entitlementUnit;
                }
                deactivatedAndHistoricEUCount++;
            }
        }
        if (deactivatedAndHistoricEUCount > 0 && deactivatedAndHistoricEUCount == entitlementUnits.size()) {
            mAllPrevEUsDeactivatedOrHistoric = true;
        }
    }
}

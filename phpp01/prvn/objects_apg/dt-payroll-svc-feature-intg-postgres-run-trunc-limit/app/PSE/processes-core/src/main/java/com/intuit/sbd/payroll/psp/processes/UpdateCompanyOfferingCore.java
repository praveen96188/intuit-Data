package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: ihannur
 * Date: 7/31/12
 * Time: 4:04 PM
 */
public class UpdateCompanyOfferingCore extends Process implements IProcess {

    private OfferingInfoDTO mOfferingInfoDTO;
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private Company mCompany;
    private Offering mOffering;


    public UpdateCompanyOfferingCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, OfferingInfoDTO pOfferingInfoDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mOfferingInfoDTO = pOfferingInfoDTO;
    }

    @Override
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
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        if(mOfferingInfoDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Company, mSourceCompanyId, "OfferingInfoDTO");
            return validationResult;
        }

        mOffering = Offering.findBySKU(mOfferingInfoDTO.getSKU());

        if(mOffering == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, mSourceCompanyId, "Offering SKU", mOfferingInfoDTO.getSKU());
            return validationResult;
        }

        //Check Offering is valid for the company
        if(mOffering.getServiceCode() == ServiceCode.DirectDeposit) {
            EntitlementUnit entitlementUnit = mCompany.getActivePrimaryEntitlementUnit();

            if(entitlementUnit == null) {
                DomainEntitySet<EntitlementUnit> entitlementUnits = mCompany.getPrimaryEntitlementUnits();
                if(entitlementUnits.size() == 1) {
                    entitlementUnit = entitlementUnits.getFirst();
                }
            }

            if(entitlementUnit == null) {
                validationResult.getMessages().ActivePrimaryEntitlementDoesNotExists(EntityName.EntitlementUnit, mSourceCompanyId, mCompany.getSourceSystemCompanyId());
                return validationResult;
            }

            DomainEntitySet<EntitlementCodeOffering> entitlementCodeOfferings = Application.find(EntitlementCodeOffering.class, EntitlementCodeOffering.Offering().equalTo(mOffering)
                                                                                            .And(EntitlementCodeOffering.EntitlementCode().equalTo(entitlementUnit.getEntitlement().getEntitlementCode())));

            if(entitlementCodeOfferings.size() == 0) {
                validationResult.getMessages().OfferingCanNotAssignToCompany(EntityName.Company, mSourceCompanyId, mOffering.getOfferingCode().toString(), mCompany.getSourceSystemCompanyId());
                return validationResult;
            }

            if(entitlementCodeOfferings.getFirst().getEffectiveDate().after(PSPDate.getPSPTime())) {
                validationResult.getMessages().OfferingEffectiveDateInFuture(EntityName.Company, mSourceCompanyId, mOffering.getOfferingCode().toString());
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult<CompanyOffering> result = new ProcessResult<CompanyOffering>();

        CompanyOffering companyOffering = mCompany.getOffering(mOffering.getServiceCode());
        String oldOfferingSKU = null;

        if(companyOffering != null) {
            oldOfferingSKU = companyOffering.getOffering().getSKU();
            if(companyOffering.getOffering().equals(mOffering)) {
                result.setResult(companyOffering);
                return result;
            }
            mCompany.removeCompanyOffering(companyOffering);
            Application.delete(companyOffering);
        }

        CompanyOffering newCompanyOffering = new CompanyOffering();
        newCompanyOffering.setCompany(mCompany);
        newCompanyOffering.setOffering(mOffering);
        Application.save(newCompanyOffering);
        mCompany.addCompanyOffering(newCompanyOffering);

        CompanyEvent.createOfferingUpdatedEvent(mCompany, oldOfferingSKU, mOffering.getSKU());
        result.setResult(newCompanyOffering);

        return result;
    }
}

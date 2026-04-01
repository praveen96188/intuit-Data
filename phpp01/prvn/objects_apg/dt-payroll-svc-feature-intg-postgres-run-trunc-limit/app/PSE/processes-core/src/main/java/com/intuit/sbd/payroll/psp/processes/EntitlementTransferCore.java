package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementStateCode;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnitStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 27, 2010
 * Time: 12:58:17 PM
 */
public class EntitlementTransferCore extends Process implements IProcess {
    private String mSourceLicenseNumber;
    private String mTargetLicenseNumber;
    private DomainEntitySet<Entitlement> mSourceEntitlements;

    public EntitlementTransferCore(String pSourceLicenseNumber, String pTargetLicenseNumber) {
        mSourceLicenseNumber = pSourceLicenseNumber;
        mTargetLicenseNumber = pTargetLicenseNumber;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        mSourceEntitlements = Application.find(Entitlement.class, Entitlement.LicenseNumber().equalTo(mSourceLicenseNumber));
        if(mSourceEntitlements.size() == 0) {
            validationResult.getMessages().InvalidArgument(EntityName.Entitlement, mSourceLicenseNumber, "Could not find source entitlement.");
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        for (Entitlement sourceEntitlement : mSourceEntitlements) {
            for (EntitlementUnit entitlementUnit : sourceEntitlement.getEntitlementUnitCollection()) {
                if(entitlementUnit.getEntitlementUnitStatus() == EntitlementUnitStatusCode.Activated) {
                    EntitlementUnitDTO entitlementUnitDTOToDeactivate = PayrollServices.dtoFactory.create(entitlementUnit);
                    entitlementUnitDTOToDeactivate.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);

                    EntitlementUnitDTO entitlementUnitDTOToActivate = PayrollServices.dtoFactory.create(entitlementUnit);
                    entitlementUnitDTOToActivate.setLicenseNumber(mTargetLicenseNumber);
                    entitlementUnitDTOToActivate.setServiceKey(null);
                    entitlementUnitDTOToActivate.setExtensionKey(null);
                    entitlementUnitDTOToActivate.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);

                    SourceSystemCode sourceSystemCode = entitlementUnit.getCompany().getSourceSystemCd();
                    String sourceCompanyId = entitlementUnit.getCompany().getSourceCompanyId();

                    processResult.merge(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(sourceSystemCode, sourceCompanyId, entitlementUnitDTOToDeactivate));
                    processResult.merge(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(sourceSystemCode, sourceCompanyId, entitlementUnitDTOToActivate));
                }
            }
            EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(sourceEntitlement);
            entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
            processResult.merge(PayrollServices.entitlementManager.updateEntitlement(entitlementDTO));
        }

        return processResult;
    }
}

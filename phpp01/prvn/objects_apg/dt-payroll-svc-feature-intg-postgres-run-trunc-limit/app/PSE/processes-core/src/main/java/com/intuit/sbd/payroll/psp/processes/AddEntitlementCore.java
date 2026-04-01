package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * @author Jeff Jones
 */
public class AddEntitlementCore extends Process implements IProcess {

    public static final int DEFAULT_NEXT_CHARGE_DATE_BUFFER_DAYS = 30;

    private EntitlementDTO mEntitlementDTO;
    private EntitlementCode mEntitlementCode;

    public AddEntitlementCore(EntitlementDTO pEntitlementDTO) {
        mEntitlementDTO = pEntitlementDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mEntitlementDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, "null", "EntitlementDTO");
            return validationResult;
        }

        validationResult.merge(mEntitlementDTO.validateAdd());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        Entitlement entitlement = Entitlement.findEntitlement(mEntitlementDTO.getLicenseNumber(), mEntitlementDTO.getEntitlementOfferingCode());
        if (entitlement != null) {
            validationResult.getMessages().EntitlementAlreadyExists(EntityName.Entitlement, null, mEntitlementDTO.getLicenseNumber(), mEntitlementDTO.getEntitlementOfferingCode());
            return validationResult;
        }

        mEntitlementCode = EntitlementCode.findEntitlementCode(mEntitlementDTO.getAssetItemNumber(), mEntitlementDTO.getEditionType(), mEntitlementDTO.getNumberOfEmployeesType());
        if (mEntitlementCode == null) {
            validationResult.getMessages().EntitlementCodeDoesNotExist(EntityName.EntitlementCode, "EntitlementCode", mEntitlementDTO.getAssetItemNumber(), mEntitlementDTO.getEditionType(), mEntitlementDTO.getNumberOfEmployeesType());
            return validationResult;
        }

        return validationResult;
    }

    @Override
    public ProcessResult<Entitlement> process() {
        ProcessResult<Entitlement> processResult = new ProcessResult<Entitlement>();

        Entitlement entitlement = new Entitlement();
        if (mEntitlementCode != null && mEntitlementCode.getAssetItemCd() == AssetItemCode.DIY) {
            if (mEntitlementDTO.getNextChargeDate() == null) {
                SpcfCalendar spcfCalendar = PSPDate.getPSPTime().copy();
                spcfCalendar.addDays(DEFAULT_NEXT_CHARGE_DATE_BUFFER_DAYS);
                mEntitlementDTO.setNextChargeDate(spcfCalendar);
            }
        }

        if (mEntitlementDTO.getSubscriptionNumber() == null || mEntitlementDTO.getSubscriptionNumber().length() == 0) {
            mEntitlementDTO.setSubscriptionNumber(PayrollServices.entitlementManager.createSubscriptionNumber());
        }

        entitlement.setSubscriptionNumber(mEntitlementDTO.getSubscriptionNumber());
        mEntitlementDTO.setEntitlementState(EntitlementStateCode.Enabled);

        entitlement.setEntitlementCode(mEntitlementCode);
        processResult.merge(mEntitlementDTO.copyDTOToDomain(entitlement, mEntitlementCode));

        int minutesToWait = SystemParameter.findIntValue(SystemParameter.Code.AMO_MESSAGE_EXPIRATION_WAIT_PERIOD, 20);
        SpcfCalendar expirationTime = PSPDate.getPSPTime().copy();
        expirationTime.addMinutes(minutesToWait);
        for (EntitlementMessage entitlementMessage : EntitlementMessage.findSkippedEntitlementMessages(entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode())) {
            entitlementMessage.setExpirationTimestamp(expirationTime);
            entitlementMessage.setStatus(EntitlementMessageStatusCode.New);
            entitlementMessage.setToken(EntitlementMessage.PROCESS_WITH_NEXT_BATCH_TOKEN);
            Application.save(entitlementMessage);
        }

        //New entitlements should have last message timestamp set to null.
        entitlement.setLastMessageTimestamp(null);

        entitlement = Application.save(entitlement);
        processResult.setResult(entitlement);

        return processResult;
    }
}

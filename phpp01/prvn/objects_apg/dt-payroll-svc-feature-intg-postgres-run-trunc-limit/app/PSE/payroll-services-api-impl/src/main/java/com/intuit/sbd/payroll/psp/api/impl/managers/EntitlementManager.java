package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.managers.IEntitlementManager;
import com.intuit.sbd.payroll.psp.common.utils.ServiceKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 28, 2010
 * Time: 3:02:48 PM
 */
public class EntitlementManager implements IEntitlementManager {

    public String createSubscriptionNumber() {
        return Application.nextSequenceValue(SequenceId.SEQ_SUBSCRIPTION_NUMBER, Long.class).toString();
    }

    public String generateServiceKey(String pFEIN, String pSubscriptionNumber, AssetItemCode pAssetItemCode,
                                     SpcfCalendar pNextChargeDate, long pQuickBooksSubtype){
        ServiceKey serviceKey;
        if(ServiceKey.isDiskDeliveryAssetCode(pAssetItemCode)) {
            if (pNextChargeDate == null || pQuickBooksSubtype == 0) {
                //need next charge date to create new service key
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(SpcfUtils.convertSpcfCalendarToDate(pNextChargeDate));
            serviceKey = new ServiceKey(pFEIN, pSubscriptionNumber, calendar, (int)pQuickBooksSubtype);
        } else {
            serviceKey = new ServiceKey(pFEIN, pSubscriptionNumber, pAssetItemCode);
        }

        return serviceKey.toString();
    }

    public ProcessResult<EntitlementUnit> addOrUpdateEntitlementUnit(SourceSystemCode pSourceSystemCode,
                                                                           String pSourceCompanyId,
                                                                           EntitlementUnitDTO pEntitlementUnitDTO) {
        AddOrUpdateEntitlementUnitCore processCore = new AddOrUpdateEntitlementUnitCore(pSourceSystemCode,
                                                                                              pSourceCompanyId,
                pEntitlementUnitDTO);
        return processCore.execute();
    }

    public ProcessResult<Entitlement> updateEntitlement(EntitlementDTO pEntitlementDTO) {
        UpdateEntitlementCore processCore = new UpdateEntitlementCore(pEntitlementDTO);
        return processCore.execute();
    }

    public ProcessResult<EntitlementMessage> addEntitlementMessage(EntitlementMessageDTO pEntitlementMessageDTO) {
        AddEntitlementMessageCore processCore = new AddEntitlementMessageCore(pEntitlementMessageDTO);
        return processCore.execute();
    }

    public ProcessResult<EntitlementMessage> updateEntitlementMessage(String pEntitlementMessageId, String plicenseNumber, EntitlementMessageStatusCode pStatus, String pErrorMessage) {
        UpdateEntitlementMessageCore processCore = new UpdateEntitlementMessageCore(pEntitlementMessageId, plicenseNumber, pStatus, pErrorMessage);
        return processCore.execute();
    }

    public ProcessResult transferEntitlement(String pSourceLicenseNumber, String pTargetLicenseNumber) {
        EntitlementTransferCore processCore = new EntitlementTransferCore(pSourceLicenseNumber, pTargetLicenseNumber);
        return processCore.execute();
    }

    public ProcessResult<Entitlement> migrateEntitlement(Entitlement pOldEntitlement, EntitlementDTO pNewEntitlementDTO) {
        EntitlementMigrateCore processCore = new EntitlementMigrateCore(pOldEntitlement, pNewEntitlementDTO);
        return processCore.execute();
    }

    public ProcessResult<EntitlementUnit> syncEntitlementUnit(SpcfUniqueId pEntitlementUnitId, EntitlementUnitDTO pEntitlementUnitDTO) {
        SyncEntitlementUnitCore processCore = new SyncEntitlementUnitCore(pEntitlementUnitId, pEntitlementUnitDTO);
        return processCore.execute();
    }

}

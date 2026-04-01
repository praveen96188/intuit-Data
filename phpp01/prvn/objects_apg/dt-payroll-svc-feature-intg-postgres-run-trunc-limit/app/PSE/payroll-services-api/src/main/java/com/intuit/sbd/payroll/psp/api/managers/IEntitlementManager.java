package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 28, 2010
 * Time: 3:03:19 PM
 */
public interface IEntitlementManager {

    String createSubscriptionNumber();

    String generateServiceKey(String pFEIN, String pSubscriptionNumber, AssetItemCode pAssetItemCode,
                              SpcfCalendar pNextChargeDate, long pQuickBooksSubtype);

    ProcessResult<EntitlementUnit> addOrUpdateEntitlementUnit(SourceSystemCode pSourceSystemCode,
                                                              String pSourceCompanyId,
                                                              EntitlementUnitDTO pEntitlementUnitDTO);

    ProcessResult<Entitlement> updateEntitlement(EntitlementDTO pEntitlementDTO);

    ProcessResult<EntitlementMessage> addEntitlementMessage(EntitlementMessageDTO pEntitlementMessageDTO);

    ProcessResult<EntitlementMessage> updateEntitlementMessage(String pEntitlementMessageId, String pLicenseNumber, EntitlementMessageStatusCode pStatus, String pErrorMessage);

    ProcessResult transferEntitlement(String pSourceLicenseNumber, String pTargetLicenseNumber);

    ProcessResult<Entitlement> migrateEntitlement(Entitlement pOldEntitlement, EntitlementDTO pNewEntitlementDTO);

    ProcessResult<EntitlementUnit> syncEntitlementUnit(SpcfUniqueId pEntitlementUnitId,
                                                              EntitlementUnitDTO pEntitlementUnitDTO);
}

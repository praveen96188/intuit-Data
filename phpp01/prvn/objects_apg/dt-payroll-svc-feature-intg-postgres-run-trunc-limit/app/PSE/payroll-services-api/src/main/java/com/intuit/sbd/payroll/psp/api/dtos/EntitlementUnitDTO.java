package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 28, 2010
 * Time: 1:09:51 PM
 */
public class EntitlementUnitDTO extends EntitlementDTO {
    private EntitlementUnitStatusCode mEntitlementUnitStatus;
    private String mServiceKey;
    private String mExtensionKey;
    private String mFedTaxId;
    private long mErrorCount;

    public EntitlementUnitDTO() {

    }

    public EntitlementUnitDTO(String pFedTaxId, String pServiceKey, String pExtensionKey, SpcfCalendar pNextChargeDate) {
        setFedTaxId(pFedTaxId);
        setServiceKey(pServiceKey);
        setExtensionKey(pExtensionKey);
        setNextChargeDate(pNextChargeDate);
    }

    public EntitlementUnitStatusCode getEntitlementUnitStatus() {
        return mEntitlementUnitStatus;
    }

    public void setEntitlementUnitStatus(EntitlementUnitStatusCode pEntitlementUnitStatus) {
        mEntitlementUnitStatus = pEntitlementUnitStatus;
    }

    public String getServiceKey() {
        return mServiceKey;
    }

    public void setServiceKey(String pServiceKey) {
        mServiceKey = pServiceKey;
    }

    public String getExtensionKey() {
        return mExtensionKey;
    }

    public void setExtensionKey(String pExtensionKey) {
        mExtensionKey = pExtensionKey;
    }

    public String getFedTaxId() {
        return mFedTaxId;
    }

    public void setFedTaxId(String pFedTaxId) {
        mFedTaxId = pFedTaxId;
    }

    public long getErrorCount() {
        return mErrorCount;
    }

    public void setErrorCount(long pErrorCount) {
        mErrorCount = pErrorCount;
    }

    @Override // Entitlement unit statuses are not valid for entitlement units
    public Map<String, EntitlementUnitStatusCode> getEntitlementUnitStatuses() {
        return null;
    }

    @Override
    public ProcessResult validateAdd() {
        ProcessResult validationResult = super.validateAdd();

        if(getAssetItemNumber() == null) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getAssetItemNumber(), "AssetItemNumber");
        }

        // service key is generated for new entitlements
        if(mServiceKey != null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementUnit, mServiceKey, "ServiceKey");
        }

        return validationResult;
    }    

    @Override
    protected ProcessResult validateCommon() {
        ProcessResult validationResult = super.validateCommon();

        if(mEntitlementUnitStatus == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementUnit, "null", "EntitlementUnitStatus");
        }

        if(mFedTaxId == null || mFedTaxId.length() > 9) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementUnit, mFedTaxId, "FEIN");
        }

        return validationResult;
    }


    public ProcessResult copyDTOToDomain(Entitlement pEntitlement, EntitlementCode pEntitlementCode, EntitlementUnit pEntitlementUnit) {

        // If the new status is pending activation we need to check for deactivated eins on the same entitlement reactivate
        if(getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingActivation) {
            DomainEntitySet<EntitlementUnit> deactivatedUnits = null;
            if(getFedTaxId() == null){
                deactivatedUnits = pEntitlement.getEntitlementUnitCollection().find(EntitlementUnit.FedTaxIdEnc().isNull()
                        .And(EntitlementUnit.EntitlementUnitStatus().equalTo(EntitlementUnitStatusCode.Deactivated)));
            } else {
                List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntitlementUnit.FedTaxIdKeyName,getFedTaxId());
                deactivatedUnits = pEntitlement.getEntitlementUnitCollection().find(EntitlementUnit.FedTaxIdEnc().in(fedTaxIdEncList)
                        .And(EntitlementUnit.EntitlementUnitStatus().equalTo(EntitlementUnitStatusCode.Deactivated)));
            }
            if(deactivatedUnits.size() > 0) {
                setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingReactivation);
            }
        }

        if(pEntitlementUnit.getEntitlementUnitStatus() != null) {
            switch(pEntitlementUnit.getEntitlementUnitStatus()) {
                case PendingActivation:
                case PendingReactivation:
                case ActivationHold:
                    // If the current status is pending activation or reactivation then we have not sent the activate request yet
                    if(getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingDeactivation ||
                            getEntitlementUnitStatus() == EntitlementUnitStatusCode.DeactivationHold)
                    {
                        setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                    }
                    break;

                case PendingDeactivation:
                case DeactivationHold:
                    // If the current status is pending deactivation then we have not sent the deactivate request yet
                    if(getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingActivation ||
                            getEntitlementUnitStatus() == EntitlementUnitStatusCode.PendingReactivation) {
                        setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
                    }
                    break;
            }
        }

        // dto entitlement unit status must be non-null
        if (!getEntitlementUnitStatus().equals(pEntitlementUnit.getEntitlementUnitStatus())) {
            // reset error count
            setErrorCount(0);
            CompanyEvent.createEntitlementUnitStatusChangedEvent(pEntitlementUnit.getCompany(), pEntitlementUnit.getEntitlementUnitStatus(), getEntitlementUnitStatus(), pEntitlementUnit.getId().toString());
        }

        pEntitlementUnit.setEntitlementUnitStatus(getEntitlementUnitStatus());
        pEntitlementUnit.setServiceKey(getServiceKey());
        pEntitlementUnit.setExtensionKey(getExtensionKey());
        pEntitlementUnit.setFedTaxId(getFedTaxId());
        pEntitlementUnit.setErrorCount(getErrorCount());

        // copy entitlement info
        return copyDTOToDomain(pEntitlement, pEntitlementCode);
    }

    public void generateNewServiceKey(Company pCompany, Entitlement pEntitlement, EntitlementCode pEntitlementCode) {
        String oldServiceKey = getServiceKey() + " " + getExtensionKey();
        // replace nulls, easier than null checks
        oldServiceKey = oldServiceKey.replace("null", "").trim();

        //Get the existing date if it exists else get the one in the DTO
        SpcfCalendar nextChargeDate = pEntitlement.getNextChargeDate() != null ? pEntitlement.getNextChargeDate() : getNextChargeDate();

        String newServiceKey = null;
        if (getFedTaxId() != null && pEntitlement.getSubscriptionNumber() != null) {
            newServiceKey = PayrollServices.entitlementManager.generateServiceKey(getFedTaxId(), pEntitlement.getSubscriptionNumber(), pEntitlementCode.getAssetItemCd(), nextChargeDate, pEntitlementCode.getQuickBooksSubtype());
        }

        //PSRV003573 Stop Assisted 7000 service keys being changed to 4000 service key
        if (oldServiceKey.startsWith("7")
                && newServiceKey != null && newServiceKey.startsWith("4")
                && equalsIgnoringServiceTypeAndCheckDigits(oldServiceKey, newServiceKey)) {
            newServiceKey = oldServiceKey;
        }

        if (newServiceKey != null) {
            String[] fullServiceKey = newServiceKey.split(" ");
            setServiceKey(fullServiceKey[0]);
            if(fullServiceKey.length > 1) {
                setExtensionKey(fullServiceKey[1]);
            } else {
                setExtensionKey(null);
            }
        } else {
            // PSRV002771 - 10.1 Null pointer on email when svc key has yet to be generated due to timing
            // This will allow the oldServiceKey (empty string) and newServiceKey to match so no event is generated
            // when the service key fails to generate due to incomplete entitlement data.
            newServiceKey = "";
        }

        // email new service keys ** do not generate a service key change event for dummy entitlement codes
        if(!oldServiceKey.equals(newServiceKey)) {
            CompanyEvent.createServiceKeyUpdatedEvent(pCompany, oldServiceKey, newServiceKey, pEntitlement);
        }
    }

    private static boolean equalsIgnoringServiceTypeAndCheckDigits(String serviceKey1, String serviceKey2) {
        char[] serviceKey1CharArray = serviceKey1.toCharArray();
        char[] serviceKey2CharArray = serviceKey2.toCharArray();
        serviceKey1CharArray[0] = '0'; //service type digit
        serviceKey2CharArray[0] = '0';
        serviceKey1CharArray[15] = '0'; //check digit
        serviceKey2CharArray[15] = '0';
        return Arrays.equals(serviceKey1CharArray, serviceKey2CharArray);
    }
}

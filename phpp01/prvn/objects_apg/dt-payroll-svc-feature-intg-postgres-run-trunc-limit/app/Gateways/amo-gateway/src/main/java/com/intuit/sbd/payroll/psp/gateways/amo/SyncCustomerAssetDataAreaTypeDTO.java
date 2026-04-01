package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.SyncCustomerAssetDataAreaType;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.ObjectUtils;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 20, 2010
 * Time: 4:42:06 PM
 */
public class SyncCustomerAssetDataAreaTypeDTO {
    private SyncCustomerAssetDataAreaType mSyncCustomerAssetDataAreaType;
    private String mEntitlementMessageId;
    private SpcfCalendar mTransactionDatetime;
    private EntitlementMessageStatusCode mEntitlementMessageStatusCode;
    private String mMessage;

    public SyncCustomerAssetDataAreaTypeDTO(String pMessage) {
        mMessage = pMessage;
    }
    
    public SyncCustomerAssetDataAreaTypeDTO(SyncCustomerAssetDataAreaType pSyncCustomerAssetDataAreaType) {
        mSyncCustomerAssetDataAreaType = pSyncCustomerAssetDataAreaType;
        setTransactionDatetime(mSyncCustomerAssetDataAreaType);
    }

    public SyncCustomerAssetDataAreaTypeDTO(SyncCustomerAssetDataAreaType pSyncCustomerAssetDataAreaType, String pEntitlementMessageId) {
        mSyncCustomerAssetDataAreaType = pSyncCustomerAssetDataAreaType;
        mEntitlementMessageId = pEntitlementMessageId;
        setTransactionDatetime(mSyncCustomerAssetDataAreaType);
    }

    public SyncCustomerAssetDataAreaType getSyncCustomerAssetDataAreaType() {
        return mSyncCustomerAssetDataAreaType;
    }

    public String getEntitlementMessageId() {
        return mEntitlementMessageId;
    }

    public SpcfCalendar getTransactionDatetime() {
        return mTransactionDatetime;
    }

    private void setTransactionDatetime(SyncCustomerAssetDataAreaType pSyncCustomerAssetDataAreaType) {
        if(pSyncCustomerAssetDataAreaType != null &&
                pSyncCustomerAssetDataAreaType.getTransactionInfo() != null &&
                pSyncCustomerAssetDataAreaType.getTransactionInfo().getTransactionDatetime() != null) {
            try {
                mTransactionDatetime = SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(pSyncCustomerAssetDataAreaType.getTransactionInfo().getTransactionDatetime());
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public String getEventReason() {
        if(mSyncCustomerAssetDataAreaType != null &&
                mSyncCustomerAssetDataAreaType.getEvent() != null &&
                mSyncCustomerAssetDataAreaType.getEvent().getEventReason() != null) {
            return mSyncCustomerAssetDataAreaType.getEvent().getEventReason().toString();
        }
        return null;
    }

    public EntitlementMessageStatusCode getEntitlementMessageStatusCode() {
        return mEntitlementMessageStatusCode;
    }

    public void setEntitlementMessageStatusCode(EntitlementMessageStatusCode pEntitlementMessageStatusCode) {
        mEntitlementMessageStatusCode = pEntitlementMessageStatusCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String pMessage) {
        mMessage = pMessage;
    }

    public String getOrderNumber(String pLicenseNumber, String pEOC) {
        if(mSyncCustomerAssetDataAreaType != null &&
                mSyncCustomerAssetDataAreaType.getSyncCustomerAsset() != null) {
            for (AssetType assetType : mSyncCustomerAssetDataAreaType.getSyncCustomerAsset().getAsset()) {
                if(assetType.getEntitlement() != null) {
                    String licenseNumber = null;
                    if(assetType.getEntitlement().getLicenseId() != null) {
                        licenseNumber = assetType.getEntitlement().getLicenseId().getValue();
                    }

                    String EOC = null;
                    if(assetType.getEntitlement().getEntitlementId() != null) {
                        EOC = assetType.getEntitlement().getEntitlementId().getValue();
                    }

                    if(ObjectUtils.equals(pLicenseNumber, licenseNumber) && ObjectUtils.equals(pEOC, EOC)) {
                        if(assetType.getOrderInfo() != null) {
                            return assetType.getOrderInfo().getOrderNumber();
                        }
                    }
                }
            }
        }
        return null;
    }
}

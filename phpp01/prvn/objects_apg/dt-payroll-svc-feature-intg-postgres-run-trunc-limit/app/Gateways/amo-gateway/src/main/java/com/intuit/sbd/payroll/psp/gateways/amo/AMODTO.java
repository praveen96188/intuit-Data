package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 6, 2010
 * Time: 8:32:48 AM
 */
public class AMODTO {
    private boolean mSortMessages = true;

    private String mLicenseNumber;
    private String mEntitlementOfferingCode;
    private String mSourceLicenseNumber;
    private String mDestinationLicenseNumber;
    private List<SyncCustomerAssetDataAreaTypeDTO>  mMessages;

    public String getLicenseNumber() {
        return mLicenseNumber;
    }

    public void setLicenseNumber(String pLicenseNumber) {
        mLicenseNumber = pLicenseNumber;
    }

    public String getEntitlementOfferingCode() {
        return mEntitlementOfferingCode;
    }

    public void setEntitlementOfferingCode(String pEntitlementOfferingCode) {
        mEntitlementOfferingCode = pEntitlementOfferingCode;
    }

    public String getSourceLicenseNumber() {
        return mSourceLicenseNumber;
    }

    public void setSourceLicenseNumber(String pSourceLicenseNumber) {
        mSourceLicenseNumber = pSourceLicenseNumber;
    }

    public String getDestinationLicenseNumber() {
        return mDestinationLicenseNumber;
    }

    public void setDestinationLicenseNumber(String pDestinationLicenseNumber) {
        mDestinationLicenseNumber = pDestinationLicenseNumber;
    }

    public void addMessage(SyncCustomerAssetDataAreaTypeDTO pSyncCustomerAssetDataAreaTypeDTO) {
        if(mMessages == null) {
            mMessages = new ArrayList<SyncCustomerAssetDataAreaTypeDTO>();
        }

        mMessages.add(pSyncCustomerAssetDataAreaTypeDTO);
        mSortMessages = true;
    }

    public List<SyncCustomerAssetDataAreaTypeDTO> getMessages() {
        if(mMessages == null) {
            mMessages = new ArrayList<SyncCustomerAssetDataAreaTypeDTO>();
        }

        if(mSortMessages) {
            Collections.sort(mMessages, new Comparator<SyncCustomerAssetDataAreaTypeDTO>() {
                public int compare(SyncCustomerAssetDataAreaTypeDTO a, SyncCustomerAssetDataAreaTypeDTO b) {
                    SpcfCalendar dateA = a != null ? a.getTransactionDatetime() : null;
                    SpcfCalendar dateB = b != null ? b.getTransactionDatetime() : null;

                    if(dateA == null && dateB == null) {
                        return 0;
                    } else if(dateA == null) {
                        return 1;
                    } else if(dateB == null) {
                        return -1;
                    }
                    return dateA.compareTo(dateB);
                }
            });
            mSortMessages = false;
        }
        
        return mMessages;
    }

    @Override
    public String toString() {
        return "License/EOC: " + mLicenseNumber + "/" + mEntitlementOfferingCode;
    }
}

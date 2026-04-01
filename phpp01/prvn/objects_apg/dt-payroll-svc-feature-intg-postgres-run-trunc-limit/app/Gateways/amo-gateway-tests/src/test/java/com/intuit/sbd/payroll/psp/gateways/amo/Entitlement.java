package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetStatusType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.ItemType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 15, 2010
 * Time: 8:35:51 AM
 */
public class Entitlement {
    public static final String DISABLED = "Disabled";
    public static final String ENABLED = "Enabled";

    public static final String PRIMARY_BILL_TO_ADDRESS = "PrimaryBillToAddress";
    public static final String PRIMARY_ADDRESS = "PrimaryAddress";
    public static final String PRIMARY_SHIP_TO_ADDRESS = "PrimaryShipToAddress";

    public Entitlement(String pItemNumber, String pSourceLicenseNumber, String pTargetLicenseNumber) {
        itemNumber = pItemNumber;
        entitlementTransfer = new EntitlementTransfer(pSourceLicenseNumber, pTargetLicenseNumber);
    }

    public Entitlement(String pLicenseNumber, String pEoc, String pOrderNumber, DataLoadServices.AssetItemNumber pItemNumber, String pCustomerId, String pBillingZipCode) {
        this(pLicenseNumber, pEoc, pOrderNumber, pItemNumber.toString(), pCustomerId, pBillingZipCode);
    }

    public Entitlement(String pLicenseNumber, String pEoc, String pOrderNumber, String pItemNumber, String pCustomerId, String pBillingZipCode) {
        licenseNumber = pLicenseNumber;
        eoc = pEoc;
        orderNumber = pOrderNumber;
        itemNumber = pItemNumber;
        customerId = pCustomerId;
        billingZipCode = pBillingZipCode; 
    }

    public String licenseNumber;
    public String eoc;
    public String orderNumber;
    public String itemNumber;
    public String customerId;
    public SpcfCalendar nextChargeDate;
    public SpcfCalendar subscriptionEndDate;
    public SpcfCalendar subscriptionStartDate;
    public String entitlementState;
    public String billingZipCode;
    public String addressToUse = PRIMARY_BILL_TO_ADDRESS;
    public String cancellationReason;
    public AssetStatusType assetStatus;
    public ItemType itemType;

    public EntitlementTransfer entitlementTransfer;

    public Contact contact;

    public boolean includeBillingUpdate = false;
    public CCInfo CCInfo;

    public Collection<TransactionAttribute> transactionAttributes = new ArrayList<TransactionAttribute>();

    public Collection<EntitlementUnit> entitlementUnits = new ArrayList<EntitlementUnit>();

    public void setEntitlementEnabled() {
        this.entitlementState = ENABLED;
    }

    public void setEntitlementDisabled() {
        this.entitlementState = DISABLED;
    }

    public void addEntitlementUnitUpdates(EntitlementUnit... pEntitlementUnits) {
        this.entitlementUnits.addAll(Arrays.asList(pEntitlementUnits));
    }

    public void addContactUpdate(String firstName, String middleName, String lastName, String emailAddress) {
        this.contact = new Contact(emailAddress, firstName, middleName, lastName);
    }

    public void addBillingUpdate(String pCcExpMM, String pCcExpYYYY, String pCcNum, String pCcType) {
        this.includeBillingUpdate = true;
        if(pCcExpMM != null && pCcExpYYYY != null && pCcType != null) {
            this.CCInfo = new CCInfo(BigInteger.valueOf(Long.parseLong(pCcExpMM)), BigInteger.valueOf(Long.parseLong(pCcExpYYYY)), pCcNum, pCcType);
        }
    }

    public void addTransactionAttributes(TransactionAttribute... pTransactionAttributes) {
        this.transactionAttributes.addAll(Arrays.asList(pTransactionAttributes));
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType pItemType) {
        itemType = pItemType;
    }
}

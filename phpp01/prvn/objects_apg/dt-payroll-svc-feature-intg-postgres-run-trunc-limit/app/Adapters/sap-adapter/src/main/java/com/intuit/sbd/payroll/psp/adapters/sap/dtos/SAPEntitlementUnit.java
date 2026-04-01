package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Jul 2, 2010
 * Time: 3:37:55 PM
 */
public class SAPEntitlementUnit {
    private String id;
    private String serviceKey;
    private String extensionKey;
    private String status;
    private Date lastValidationDate;
    private SAPEntitlementInfo entitlement;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SAPEntitlementInfo getEntitlement() {
        return entitlement;
    }

    public void setEntitlement(SAPEntitlementInfo entitlement) {
        this.entitlement = entitlement;
    }

    public String getExtensionKey() {
        return extensionKey;
    }

    public void setExtensionKey(String extensionKey) {
        this.extensionKey = extensionKey;
    }

    public Date getLastValidationDate() {
        return lastValidationDate;
    }

    public void setLastValidationDate(Date lastValidationDate) {
        this.lastValidationDate = lastValidationDate;
    }
}

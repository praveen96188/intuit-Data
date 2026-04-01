package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: Jun 9, 2010
 * Time: 2:00:00 PM
 */
public class SAPEntitlementSearchResult {

    private String id;

    private String licenseNumber;
    private String eoc;

    private SAPCompanyKey key;
    private String legalName;
    private String fein;
    private String PSID;

    private String entitlementUnitStatus;
    private String serviceKey;

    private SAPAssetInfo assetInfo;

    private SAPCompanyServiceState companyServiceStateCd;

    private String subtypeDescription;

    private String entitlementStatus;

    public String getEntitlementStatus() {
        return entitlementStatus;
    }

    public void setEntitlementStatus(String entitlementStatus) {
        this.entitlementStatus = entitlementStatus;
    }

    public SAPCompanyKey getKey() {
        return key;
    }

    public void setKey(SAPCompanyKey key) {
        this.key = key;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getFein() {
        return fein;
    }

    public void setFein(String fein) {
        this.fein = fein;
    }

    public String getPSID() {
        return PSID;
    }

    public void setPSID(String PSID) {
        this.PSID = PSID;
    }

    public String getEntitlementUnitStatus() {
        return entitlementUnitStatus;
    }

    public void setEntitlementUnitStatus(String entitlementUnitStatus) {
        this.entitlementUnitStatus = entitlementUnitStatus;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getEoc() {
        return eoc;
    }

    public void setEoc(String eoc) {
        this.eoc = eoc;
    }

    public SAPAssetInfo getAssetInfo() {
        return assetInfo;
    }

    public void setAssetInfo(SAPAssetInfo assetInfo) {
        this.assetInfo = assetInfo;
    }

    public SAPCompanyServiceState getCompanyServiceStateCd() {
        return companyServiceStateCd;
    }

    public void setCompanyServiceStateCd(SAPCompanyServiceState companyServiceStateCd) {
        this.companyServiceStateCd = companyServiceStateCd;
    }

    public String getSubtypeDescription() {
        return subtypeDescription;
    }

    public void setSubtypeDescription(String subtypeDescription) {
        this.subtypeDescription = subtypeDescription;
    }
}

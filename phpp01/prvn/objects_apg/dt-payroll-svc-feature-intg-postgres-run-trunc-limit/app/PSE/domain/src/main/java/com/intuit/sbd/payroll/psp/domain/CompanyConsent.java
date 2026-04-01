package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

/**
 * Hand-written business logic
 */
public class CompanyConsent extends BaseCompanyConsent {
    public static String FEINKeyName = "CompConsent_Fein";

    /**
     * Default constructor.
     */
    public CompanyConsent() {
        super();
    }

    public void setFein(String pFein) {
        super.setFeinEnc(EncryptionUtils.deterministicEncrypt(FEINKeyName, pFein));
    }

    public String getFein() {
        return EncryptionUtils.deterministicDecrypt(FEINKeyName, getFeinEnc());
    }

}
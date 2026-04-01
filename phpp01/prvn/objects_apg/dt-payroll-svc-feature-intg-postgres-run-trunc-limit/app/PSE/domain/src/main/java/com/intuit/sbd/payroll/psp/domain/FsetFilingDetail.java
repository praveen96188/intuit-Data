package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

/**
 * Hand-written business logic
 */
public class FsetFilingDetail extends BaseFsetFilingDetail {
    public static  String FedTaxIdKeyName="FSET_FLDtl_EIN";
    public static  String AgencyIdKeyName="FSET_FLDtl_AID";

    /**
     * Default constructor.
     */
    public FsetFilingDetail() {
        super();
    }

    public static FsetFilingDetail findFsetFilingDetailBySubmissionId(String pSubmissionId) {
        DomainEntitySet<FsetFilingDetail> fsetFilingDetails = Application.find(FsetFilingDetail.class, FsetFilingDetail.SubmissionId().equalTo(pSubmissionId));
        if (fsetFilingDetails.size() > 1) {
            throw new RuntimeException("More than one FsetFilingDetail is found with submissionId.");
        }
        return fsetFilingDetails.getFirst();
    }

    public static DomainEntitySet<FsetFilingDetail> findFsetFilingDetailByTransmissionId(String pTransmissionId) {
        DomainEntitySet<FsetFilingDetail> fsetFilingDetails = Application.find(FsetFilingDetail.class,
                                                                               FsetFilingDetail.ParentFile().TransmissionId().equalTo(pTransmissionId));
        return fsetFilingDetails;
    }

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }

    public void setAgencyId(String pAgencyId) {
        super.setAgencyIdEnc(EncryptionUtils.deterministicEncrypt(AgencyIdKeyName,pAgencyId));
    }


    public String getAgencyId() {
        return EncryptionUtils.deterministicDecrypt(AgencyIdKeyName,getAgencyIdEnc());
    }
}
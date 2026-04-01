package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class EftpsEnrollmentDetail extends BaseEftpsEnrollmentDetail {
    public static String FedTaxIdKeyName="EFTPSEnrlmtDtl_EIN";
    //
    // Static methods
    //

    public static EftpsEnrollmentDetail findEnrollmentDetailByTransactionSetId(int pTransactionSetId, int pTransactionId) {
        Expression<EftpsEnrollmentDetail> query = new Query<EftpsEnrollmentDetail>()
                .Where(TransactionSetId().equalTo(pTransactionSetId)
                        .And(TransactionId().equalTo(pTransactionId)));

        DomainEntitySet<EftpsEnrollmentDetail> enrollmentDetails = Application.find(EftpsEnrollmentDetail.class, query);

        if (enrollmentDetails.isEmpty()) {
            throw new RuntimeException(
                    String.format("EftpsEnrollmentDetail could not be found for TransactionSetId %d and TransactionId %d",
                                  pTransactionSetId, pTransactionId));
        } else if (enrollmentDetails.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one EftpsEnrollmentDetail was found for TransactionSetId %d and TransactionId %d",
                                  pTransactionSetId, pTransactionId));
        }

        return enrollmentDetails.get(0);
    }

    public static EftpsEnrollmentDetail createEnrollmentDetail(EftpsFile pEftpsFile, EftpsEnrollment pEnrollment,
                                                               int pGroupId, int pTxnSetId, int pTxnId) {

        Company company = pEnrollment.getCompanyAgency().getCompany();
        Address address = company.getLegalAddress();
        String legalZip = address != null ? address.getZipCode() : null;

        EftpsEnrollmentDetail enrollmentDetail = createEnrollmentDetail(pEnrollment, company.getFedTaxId(), company.getLegalName(), legalZip);

        updateEnrollmentDetail(pEftpsFile, enrollmentDetail, pGroupId, pTxnSetId, pTxnId);

        return enrollmentDetail;
    }

    public static void updateEnrollmentDetail(EftpsFile pEftpsFile, EftpsEnrollmentDetail pEnrollmentDetail,
                                                               int pGroupId, int pTxnSetId, int pTxnId) {
        pEnrollmentDetail.setParentFile(pEftpsFile);
        pEnrollmentDetail.setGroupId(pGroupId);
        pEnrollmentDetail.setTransactionSetId(pTxnSetId);
        pEnrollmentDetail.setTransactionId(pTxnId);
        pEnrollmentDetail.syncEftpsEnrollmentDetailFromEnrollment();
    }

    public static EftpsEnrollmentDetail createEnrollmentDetail(EftpsEnrollment pEftpsEnrollment, String pFedTaxId, String pLegalName, String pLegalZip) {
        EftpsEnrollmentDetail enrollmentDetail = new EftpsEnrollmentDetail();
        enrollmentDetail.setEftpsEnrollment(pEftpsEnrollment);
        enrollmentDetail.setFedTaxId(pFedTaxId);
        enrollmentDetail.setLegalName(pLegalName);
        enrollmentDetail.setLegalZip(pLegalZip);
        return Application.save(enrollmentDetail);
    }

    //
    // Instance methods
    //

    private void syncEftpsEnrollmentDetailFromEnrollment() {
        setStatusEffectiveDate(getEftpsEnrollment().getStatusEffectiveDate());
        setStatusCd(getEftpsEnrollment().getStatusCd());
    }

	/**
	 * Default constructor.
	 */
	public EftpsEnrollmentDetail()
	{
		super();
	}

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }
}
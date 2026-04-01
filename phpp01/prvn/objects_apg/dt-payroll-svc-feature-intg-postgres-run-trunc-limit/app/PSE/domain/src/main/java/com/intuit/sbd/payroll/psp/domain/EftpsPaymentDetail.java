package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.EdiTaxFileExpression;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Hand-written business logic
 */
@Slf4j
public class EftpsPaymentDetail extends BaseEftpsPaymentDetail {
    public static String FedTaxIdKeyName="EFTPSPmtDtl_EIN";
    public NaturalKey getNaturalKey() {
        return new NaturalKey(EftpsPaymentDetail.class, getTransactionSetId(), getTransactionId());
    }

    //
    // Static methods
    //

    public static EftpsPaymentDetail findPaymentDetailByTransactionIdAndAgencyPaymentId(int pTransactionId,
                                                                                        String pAgencyPaymentId) {
        Criterion<EftpsPaymentDetail> where =
                TransactionId().equalTo(pTransactionId).And(AgencyPaymentId().equalTo(pAgencyPaymentId));
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = Application.find(EftpsPaymentDetail.class, where);

        if (paymentDetails.isEmpty()) {
            throw new RuntimeException(
                    String.format("EftpsPaymentDetail could not be found for TransactionId %d and AgencyPaymentId %s",
                                  pTransactionId, pAgencyPaymentId));
        } else if (paymentDetails.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one EftpsPaymentDetail was found for TransactionId %d and AgencyPaymentId %s",
                                  pTransactionId, pAgencyPaymentId));
        }

        return paymentDetails.get(0);
    }

    static final String PaymentDetailCacheKey = "PaymentDetailByTrxSetAndTrxIdCache";
    public static EftpsPaymentDetail findPaymentDetailByTransactionSetIdAndTransactionId(int pTransactionSetId,
                                                                                         int pTransactionId) {

        NaturalKey lookupKey = new NaturalKey(EftpsPaymentDetail.class, pTransactionSetId, pTransactionId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(lookupKey);

        EftpsPaymentDetail paymentDetail = null;
        if (primaryKey != null) {
            paymentDetail = Application.findById(EftpsPaymentDetail.class, primaryKey);
        } else if (Application.getSessionCache().getNonHibernateObject(PaymentDetailCacheKey) == null){
            DomainEntitySet<EftpsPaymentDetail> paymentDetails =
                    Application.find(EftpsPaymentDetail.class,
                                     EftpsPaymentDetail.TransactionSetId().equalTo(pTransactionSetId)
                                     .And(EftpsPaymentDetail.TransactionId().equalTo(pTransactionId)));
            if (paymentDetails.size() == 1) {
                paymentDetail = paymentDetails.get(0);
            } else if (paymentDetails.size() > 1) {
                throw new RuntimeException(
                        String.format("More than one EftpsPaymentDetail was found for TransactionSetId %d and TransactionId %d",
                                      pTransactionSetId, pTransactionId));
            }

            // cache all payment details for the associated payment file
            DomainEntitySet<EftpsPaymentDetail> allPaymentDetails =
                    Application.find(EftpsPaymentDetail.class,
                                     EftpsPaymentDetail.ParentFile().equalTo(paymentDetail.getParentFile()));
            for (EftpsPaymentDetail eftpsPaymentDetail : allPaymentDetails) {
                Application.getSessionCache().addPrimaryKey(eftpsPaymentDetail.getNaturalKey(), eftpsPaymentDetail.getId());
            }

            Application.getSessionCache().addNonHibernateObject(PaymentDetailCacheKey, new Boolean(true));
        }

        if (paymentDetail == null) {
            throw new RuntimeException(
                    String.format("EftpsPaymentDetail could not be found for TransactionSetId %d and TransactionId %d",
                                  pTransactionSetId, pTransactionId));
        }

        return paymentDetail;
    }

    public static DomainEntitySet<EftpsPaymentDetail> findPaymentDetailsByTransactionSetId(int pTransactionSetId) {
        return Application.find(EftpsPaymentDetail.class, TransactionSetId().equalTo(pTransactionSetId));
    }

    public static EftpsPaymentDetail findPaymentDetailByEFTTransactionId(String pEFTTransactionId) {
        Criterion<EftpsPaymentDetail> where = EftTransactionId().equalTo(pEFTTransactionId);
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = Application.find(EftpsPaymentDetail.class, where);

        if (paymentDetails.isEmpty()) {
            throw new RuntimeException(
                    String.format("EftpsPaymentDetail could not be found for EftTransactionId %s", pEFTTransactionId));
        } else if (paymentDetails.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one EftpsPaymentDetail was found for EftTransactionId %s", pEFTTransactionId));
        }

        return paymentDetails.get(0);
    }

    public static EftpsPaymentDetail createPaymentDetail(EftpsFile pEftpsFile, MoneyMovementTransaction pMMT,
                                                         int pGroupId, int pTxnSetId, int pTxnId,
                                                         String pPaymentDetails, String pTaxTypeCode,
                                                         Date pPeriodEndDate, Date pSettlementDate) {
        EftpsPaymentDetail paymentDetail = new EftpsPaymentDetail();

        paymentDetail.setParentFile(pEftpsFile);
        paymentDetail.setGroupId(pGroupId);
        paymentDetail.setTransactionSetId(pTxnSetId);
        paymentDetail.setTransactionId(pTxnId);
        paymentDetail.setTaxTypeCode(pTaxTypeCode);
        paymentDetail.setPaymentDetails(pPaymentDetails);
        paymentDetail.setPeriodEndDate(CalendarUtils.convertToSpcfCalendar(pPeriodEndDate));
        paymentDetail.setPaymentSettlementDate(CalendarUtils.convertToSpcfCalendar(pSettlementDate));
        paymentDetail.setCompany(pMMT.getCompany());
        paymentDetail.setMoneyMovementTransaction(pMMT);
        paymentDetail.setFedTaxId(pMMT.getAgencyTaxpayerId());
        paymentDetail.setPaymentDueDate(pMMT.getDueDate());
        paymentDetail.setPaymentInitiationDate(pMMT.getInitiationDate());
        paymentDetail.setPaymentAmount(pMMT.getMoneyMovementTransactionAmount());
        paymentDetail.setReturnCd(null);

        // do this last since it will also create the company event (which needs the above members set)
        paymentDetail.updatePaymentStatus(pMMT.getTaxPaymentStatus(), false);

        return Application.save(paymentDetail);
    }

    /**
     * This form of the addPaymentDetail method is used when no MMT is available (i.e. for AS400 payment details)
     */
    public static EftpsPaymentDetail createPaymentDetail(EftpsFile pEftpsFile, int pGroupId, int pTxnSetId, int pTxnId,
                                                         String pFedTaxId, BigDecimal pPaymentAmount,
                                                         TaxPaymentStatus pPaymentStatus, String pPaymentDetails,
                                                         String pTaxTypeCode, Date pInitiationDate,
                                                         Date pPaymentDueDate, Date pPeriodEndDate, Date pSettlementDate) {
        log.error("action=OracleExit EftpsPaymentDetail createPaymentDetail without MMT called");
        EftpsPaymentDetail paymentDetail = new EftpsPaymentDetail();

        paymentDetail.setParentFile(pEftpsFile);
        paymentDetail.setGroupId(pGroupId);
        paymentDetail.setTransactionSetId(pTxnSetId);
        paymentDetail.setTransactionId(pTxnId);
        paymentDetail.setTaxTypeCode(pTaxTypeCode);
        paymentDetail.setPaymentDetails(pPaymentDetails);
        paymentDetail.setPeriodEndDate(CalendarUtils.convertToSpcfCalendar(pPeriodEndDate));
        paymentDetail.setFedTaxId(pFedTaxId);
        paymentDetail.setPaymentDueDate(CalendarUtils.convertToSpcfCalendar(pPaymentDueDate));
        paymentDetail.setPaymentSettlementDate(CalendarUtils.convertToSpcfCalendar(pSettlementDate));
        paymentDetail.setPaymentInitiationDate(CalendarUtils.convertToSpcfCalendar(pInitiationDate));
        paymentDetail.setPaymentAmount(SpcfUtils.convertToSpcfMoney(pPaymentAmount));
        paymentDetail.setStatusCd(pPaymentStatus);
        paymentDetail.setStatusEffectiveDate(PSPDate.getPSPTime());
        paymentDetail.setReturnCd(null);

        return Application.save(paymentDetail);
    }

	/**
	 * Default constructor.
	 */
	public EftpsPaymentDetail()
	{
		super();
	}

    //
    // Instance methods
    //

    public EftpsPaymentDetail cascadePaymentStatus(TaxPaymentStatus pNewStatus) {
        return updatePaymentStatus(pNewStatus, false);
    }

    public EftpsPaymentDetail updatePaymentStatus(TaxPaymentStatus pNewStatus, boolean pCreateEvent) {
        TaxPaymentStatus oldStatus = getStatusCd();

        if ((pNewStatus == null) || pNewStatus.equals(oldStatus)) {
            return this;
        }

        setStatusCd(pNewStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        EftpsPaymentDetail paymentDetail = Application.save(this);

        if (pCreateEvent) {
            CompanyEvent.createTaxPaymentStatusChangeEvent(paymentDetail, oldStatus);
        }

        return paymentDetail;
    }

    public static EftpsPaymentDetail findPaymentDetailByMoneyMovementTransaction(MoneyMovementTransaction moneyMovementTransaction) {
        Criterion<EftpsPaymentDetail> where = EftpsPaymentDetail.MoneyMovementTransaction().equalTo(moneyMovementTransaction)
                .And(EftpsPaymentDetail.Company().equalTo(moneyMovementTransaction.getCompany()));
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = Application.find(EftpsPaymentDetail.class, where);

        if (paymentDetails.isEmpty()) {
            if (moneyMovementTransaction.getMoneyMovementTransactionAmount().isLessThanEqualTo(SpcfMoney.ZERO)) {
                return null;
            } else {
                throw new RuntimeException(
                        String.format("EftpsPaymentDetail could not be found for Money Movement Transaction Id %s", moneyMovementTransaction.getId().toString()));
            }
        } else if (paymentDetails.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one EftpsPaymentDetail was found for Money Movement Transaction Id %s", moneyMovementTransaction.getId().toString()));
        }
        return paymentDetails.get(0);
    }

    public static DomainEntitySet<EftpsPaymentDetail> findPaymentDetailByStatus(TaxPaymentStatus pStatus) {
        Criterion<EftpsPaymentDetail> where = StatusCd().equalTo(pStatus);
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = Application.find(EftpsPaymentDetail.class, where);

        return paymentDetails;
    }

    public static DomainEntitySet<EftpsPaymentDetail> findAllPaymentDetails() {
       
        return Application.find(EftpsPaymentDetail.class);
    }

    public static DomainEntitySet<EftpsPaymentDetail> findByCompany(String pSourceSystemCd, String pSourceCompanyId) {
        Criterion<EftpsPaymentDetail> where = EftpsPaymentDetail.MoneyMovementTransaction().Company().SourceSystemCd().equalTo(SourceSystemCode.valueOf(pSourceSystemCd))
                .And(EftpsPaymentDetail.MoneyMovementTransaction().Company().SourceCompanyId().equalTo(pSourceCompanyId));
        DomainEntitySet<EftpsPaymentDetail> paymentDetails = Application.find(EftpsPaymentDetail.class, where);

        return paymentDetails;
    }

    public static final EdiTaxFileExpression<EftpsPaymentDetail> BaseParentFile() {return new EdiTaxFileExpression<EftpsPaymentDetail>(null, "ParentFile");};     //Todo change codegen to add property comparison accessors from base class onto subclasses

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }
}

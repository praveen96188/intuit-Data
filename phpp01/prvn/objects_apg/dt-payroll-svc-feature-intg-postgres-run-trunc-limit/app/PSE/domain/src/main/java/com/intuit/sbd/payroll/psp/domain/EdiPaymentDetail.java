package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.EdiTaxFileExpression;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Date;

/**
 * Hand-written business logic
 */
public class EdiPaymentDetail extends BaseEdiPaymentDetail {
public static  String FedTaxIdKeyName="EDIPmtDtl_FedTaxId";
	/**
	 * Default constructor.
	 */
	public EdiPaymentDetail()
	{
		super();
	}

    public static final EdiTaxFileExpression<EdiPaymentDetail> BaseParentFile() {return new EdiTaxFileExpression<EdiPaymentDetail>(null, "ParentFile");}; //todo
    
    public static EdiPaymentDetail createPaymentDetail(StateEdiTaxFile pEdiFile, MoneyMovementTransaction pMMT,
                    int pGroupId, int pTxnSetId, String pTxnGroupTime, Date pSettlementDate, String pTxnId) {
        EdiPaymentDetail paymentDetail = new EdiPaymentDetail();

        paymentDetail.setParentFile(pEdiFile);
        paymentDetail.setGroupId(pGroupId);
        paymentDetail.setTransactionSetId(pTxnSetId);
        paymentDetail.setTransactionId(pTxnId);
        paymentDetail.setTaxTypeCode(pMMT.getPaymentFrequency().getTaxCodeId());
        paymentDetail.setGroupTransactionTime(pTxnGroupTime);
        paymentDetail.setPeriodBeginDate(pMMT.getPaymentPeriodBegin());
        paymentDetail.setPeriodEndDate(pMMT.getPaymentPeriodEnd());
        paymentDetail.setPaymentSettlementDate(CalendarUtils.convertToSpcfCalendar(pSettlementDate));
        paymentDetail.setMoneyMovementTransaction(pMMT);
        paymentDetail.setCompany(pMMT.getCompany());
        paymentDetail.setFedTaxId(pMMT.getCompany().getFedTaxId());
        paymentDetail.setPaymentDueDate(pMMT.getDueDate());
        paymentDetail.setPaymentInitiationDate(pMMT.getInitiationDate());
        paymentDetail.setPaymentAmount(pMMT.getMoneyMovementTransactionAmount());

        // do this last since it will also create the company event (which needs the above members set)
        paymentDetail.updatePaymentStatus(pMMT.getTaxPaymentStatus(), false);

        return Application.save(paymentDetail);
    }

    public EdiPaymentDetail updatePaymentStatus(TaxPaymentStatus pNewStatus, boolean pCreateEvent) {
        TaxPaymentStatus oldStatus = getStatusCd();

        if ((pNewStatus == null) || pNewStatus.equals(oldStatus)) {
            return this;
        }

        setStatusCd(pNewStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        EdiPaymentDetail paymentDetail = Application.save(this);

        if (pCreateEvent) {
            CompanyEvent.createTaxPaymentStatusChangeEvent(paymentDetail, oldStatus);
        }

        // In case of Eftps, this is done in stored proc
        paymentDetail.getMoneyMovementTransaction().updateTaxPaymentStatus(pNewStatus, true, true);

        return paymentDetail;
    }

    public static EdiPaymentDetail findPaymentDetailByTxnInfo(int pTransactionSetId, String pTransactionId, int pGroupId, String pGroupTxnTime) {

        EdiPaymentDetail paymentDetail = null;

        DomainEntitySet<EdiPaymentDetail> paymentDetails =
                Application.find(EdiPaymentDetail.class,
                        EdiPaymentDetail.TransactionSetId().equalTo(pTransactionSetId).And(EdiPaymentDetail.TransactionId().equalTo(pTransactionId))
                        .And(EdiPaymentDetail.GroupId().equalTo(pGroupId)).And(EdiPaymentDetail.GroupTransactionTime().equalTo(pGroupTxnTime)));
        if(paymentDetails.size() == 0) {
            return null;
        }else if (paymentDetails.size() == 1) {
            paymentDetail = paymentDetails.get(0);
        } else if (paymentDetails.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one EdiPaymentDetail was found for TransactionSetId %d and TransactionId %s, GroupId: %d, GroupTxnTime: %s",
                            pTransactionSetId, pTransactionId, pGroupId, pGroupTxnTime));
        }
        
        return paymentDetail;
    }

    public static EdiPaymentDetail findPaymentDetailByMoneyMovementTransaction(MoneyMovementTransaction moneyMovementTransaction) {
        Criterion<EdiPaymentDetail> where = EdiPaymentDetail.MoneyMovementTransaction().equalTo(moneyMovementTransaction)
                .And(EdiPaymentDetail.Company().equalTo(moneyMovementTransaction.getCompany()));
        DomainEntitySet<EdiPaymentDetail> paymentDetails = Application.find(EdiPaymentDetail.class, where);

        if (paymentDetails.isEmpty()) {
            if (moneyMovementTransaction.getMoneyMovementTransactionAmount().isLessThanEqualTo(SpcfMoney.ZERO)) {
                return null;
            } else {
                throw new RuntimeException(
                        String.format("EdiPaymentDetail could not be found for Money Movement Transaction Id %s", moneyMovementTransaction.getId().toString()));
            }
        } else if (paymentDetails.size() > 1) {
            throw new RuntimeException(
                    String.format("More than one EdiPaymentDetail was found for Money Movement Transaction Id %s", moneyMovementTransaction.getId().toString()));
        }
        return paymentDetails.get(0);
    }

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }
}
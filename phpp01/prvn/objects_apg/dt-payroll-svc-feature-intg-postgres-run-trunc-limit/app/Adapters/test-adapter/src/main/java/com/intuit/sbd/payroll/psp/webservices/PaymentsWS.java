package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayment;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentSearch;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.EFTPSPaymentDetailWSDTO;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: mwaqarbaig
 * Date: Feb 3, 2011
 * Time: 4:37:17 PM
 */
@WebService()
public class PaymentsWS {

    @WebMethod()
    public Collection<EFTPSPaymentDetailWSDTO> QueryEFTPSPaymentDetails(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                                        @WebParam(name = "sourceCompanyID") String pSourceCompanyId) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(PaymentsWS.class);
        String envId = ConfigurationManager.getEnvironmentIdentifier();

        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        logger.info("hello" + pSourceCompanyId);

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }
            DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findByCompany(pSourceSystemCd, pSourceCompanyId);

            if (paymentDetails == null) {
                throw new RuntimeException(String.format("No EFTPS payments exist for company %s:%s.", pSourceSystemCd, pSourceCompanyId));
            }
            PayrollServices.rollbackUnitOfWork();
            return buildEFTPSPaymentDetailDTOs(paymentDetails);
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<EFTPSPaymentDetailWSDTO> buildEFTPSPaymentDetailDTOs(DomainEntitySet<EftpsPaymentDetail> pPaymentDetails) {
        Collection<EFTPSPaymentDetailWSDTO> paymentDetailWSDTOs = new ArrayList<EFTPSPaymentDetailWSDTO>();
        for (EftpsPaymentDetail paymentDetail : pPaymentDetails) {
            EFTPSPaymentDetailWSDTO detailWSDTO = new EFTPSPaymentDetailWSDTO();
            detailWSDTO.mPaymentAmount = paymentDetail.getPaymentAmount().getIntegerPart();
            detailWSDTO.mPaymentInitiationDate = (paymentDetail.getPaymentInitiationDate() == null) ? null : new Date(paymentDetail.getPaymentInitiationDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mPaymentDueDate = (paymentDetail.getPaymentDueDate() == null) ? null : new Date(paymentDetail.getPaymentDueDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mReason = paymentDetail.getReason();
            detailWSDTO.mPaymentDetails = paymentDetail.getPaymentDetails();
            detailWSDTO.mAgencyPaymentId = paymentDetail.getAgencyPaymentId();
            detailWSDTO.mEftTransactionId = paymentDetail.getEftTransactionId();
            detailWSDTO.mFedTaxId = paymentDetail.getFedTaxId();
            detailWSDTO.mGroupId = paymentDetail.getGroupId();
            detailWSDTO.mPeriodEndDate = (paymentDetail.getPeriodEndDate() == null) ? null : new Date(paymentDetail.getPeriodEndDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mResponseDate = (paymentDetail.getResponseDate() == null) ? null : new Date(paymentDetail.getResponseDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mReturnCd = (paymentDetail.getReturnCd() == null) ? null : paymentDetail.getReturnCd().toString();
            detailWSDTO.mSameDayAckNumber = paymentDetail.getSameDayAckNumber();
            detailWSDTO.mStatusCd = (paymentDetail.getStatusCd() == null) ? null : paymentDetail.getStatusCd().toString();
            detailWSDTO.mStatusEffectiveDate = (paymentDetail.getStatusEffectiveDate() == null) ? null : new Date(paymentDetail.getStatusEffectiveDate().toLocal().getTimeInMilliseconds());
            detailWSDTO.mTaxTypeCode = paymentDetail.getTaxTypeCode();
            detailWSDTO.mTransactionId = paymentDetail.getTransactionId();
            detailWSDTO.mTransactionSetId = paymentDetail.getTransactionSetId();
            paymentDetailWSDTOs.add(detailWSDTO);
        }
        return paymentDetailWSDTOs;
    }

    @WebMethod
    public void RejectEFTPSPayment(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                   @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                   @WebParam(name = "dueDate") String pDueDate,
                                   @WebParam(name = "paymentType") String pPaymentType,
                                   @WebParam(name = "rejectionCode") String pRejectionCode,
                                   @WebParam(name = "rejectionReason") String pRejectionReason) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(PaymentsWS.class);

        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pDueDate != null && pDueDate.length() != 10) {
            throw new RuntimeException(
                    "Invalid from date format" + pDueDate + ".  Correct format: MM/dd/yyyy");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyId or sourceSystemCode");
            }
            SpcfCalendar dueDate = null;
            SpcfCalendar toDate = null;
            if (pDueDate != null) {
                dueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("MM/dd/yyyy");
                SpcfCalendar parsedRunDate = dateFormat.parse(pDueDate);
                dueDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            DomainEntitySet<EftpsPaymentDetail> paymentDetails = EftpsPaymentDetail.findByCompany(pSourceSystemCd, pSourceCompanyId);
            Criterion<MoneyMovementTransaction> whereClause = (MoneyMovementTransaction.Company().equalTo(company))
                    .And(MoneyMovementTransaction.DueDate().equalTo(dueDate))
                    .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo(pPaymentType));
            DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>()
                    .Where(whereClause));

            for (MoneyMovementTransaction mmt : mmts) {
                EftpsPaymentDetail paymentDetail = EftpsPaymentDetail.findPaymentDetailByMoneyMovementTransaction(mmt);
                paymentDetail.setRejectCd(pRejectionCode);
                paymentDetail.setReason(pRejectionReason);
                TaxPaymentStatus paymentStatus = TaxPaymentStatus.RejectedByAgency;
                if (pRejectionCode.equals("830")) {
                    paymentStatus = TaxPaymentStatus.ReturnedTaxNotPaid;
                }
                else if (pRejectionCode.equals("011")) {
                    paymentStatus = TaxPaymentStatus.ReturnedTaxPaid;
                }
                mmt.updateTaxPaymentStatus(paymentStatus);
                paymentDetail.updatePaymentStatus(paymentStatus, true);
            }

            if (paymentDetails == null) {
                throw new RuntimeException(String.format("No EFTPS payments exist for company %s:%s.", pSourceSystemCd, pSourceCompanyId));
            }
            PayrollServices.commitUnitOfWork();
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod()
    public String updateScheduledDatePaid(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                                        @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                                        @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                                                        @WebParam(name = "dueDate") String pDueDate,
                                                                        @WebParam(name = "newScheduledDatePaid") String pNewScheduledDatePaid) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(PaymentsWS.class);

        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.isEmpty()) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if (pDueDate != null && pDueDate.length() != 10) {
            throw new RuntimeException(
                    "Invalid from date format" + pDueDate + ".  Correct format: MM/dd/yyyy");
        }

        if (pNewScheduledDatePaid != null && pNewScheduledDatePaid.length() != 10) {
            throw new RuntimeException(
                    "Invalid from date format" + pNewScheduledDatePaid + ".  Correct format: MM/dd/yyyy");
        }

        SpcfCalendar dueDate = null;
        SpcfCalendar newScheduledDate = null;
        if (pDueDate != null) {
            dueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(pDueDate);
            dueDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
        }

        if (pNewScheduledDatePaid != null) {
            newScheduledDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(pNewScheduledDatePaid);
            newScheduledDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
        }

        SpcfCalendar newInitDate = null;
        try {
            PayrollServices.beginUnitOfWork();
            com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).
                                    setPaymentTemplateCd(pPaymentTemplateCd).setDueDate(dueDate).setReadyToSend().find();

            ProcessResult processResult;
            for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {

                //Converting from settlement date to Initiation date by subtracting offset days.
                newInitDate = newScheduledDate.copy(); 
                CalendarUtils.addBusinessDays(newInitDate, MoneyMovementTransaction.getPaymentMethodDayOffset(moneyMovementTransaction.getMoneyMovementPaymentMethod(), moneyMovementTransaction.getPaymentTemplate())*-1);
                
                processResult = PayrollServices.paymentManager.updateInitiationDate(moneyMovementTransaction.getId().toString(), newInitDate);
                if(!processResult.isSuccess()) {
                    throw new RuntimeException(processResult.getMessages().toString());
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return newInitDate != null ? newInitDate.toString() : "No Payments to update";
    }

    @WebMethod()
    public String rejectACHorCheckPayments(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                                        @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                                        @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                                                        @WebParam(name = "dueDate") String pDueDate) throws Exception {

        int count = 0;
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(PaymentsWS.class);

        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.isEmpty()) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if (pDueDate != null && pDueDate.length() != 10) {
            throw new RuntimeException(
                    "Invalid due date format" + pDueDate + ".  Correct format: MM/dd/yyyy");
        }

        SpcfCalendar dueDate = null;
        if (pDueDate != null) {
            dueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(pDueDate);
            dueDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
        }

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }
            PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHCredit, PaymentMethod.CheckPayment, PaymentMethod.SuperCheck};
            TaxPaymentStatus[] taxPaymentStatuses = new TaxPaymentStatus[]{TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency};
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).
                                    setPaymentTemplateCd(pPaymentTemplateCd).setDueDate(dueDate).setTaxPaymentStatuses(taxPaymentStatuses).setPaymentMethods(paymentMethods).find()
                                    .find(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Executed));

            ProcessResult processResult;
            for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
                processResult = PayrollServices.paymentManager.rejectPayment(moneyMovementTransaction.getId().toString(), "Rejection from Test web service");
                if(!processResult.isSuccess()) {
                    throw new RuntimeException(processResult.getMessages().toString());
                }
            }
            count = moneyMovementTransactions.size();
            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return "Rejected "+count+" payments";
    }

    @WebMethod
    public List<SAPPayment> findTaxPayments(@WebParam(name="searchCriteria")SAPPaymentSearch search) {
        SAPSearchResults<SAPPayment> taxPayments = null;
        try {
            taxPayments = new TaxAdapter().findTaxPayments(search, 0, 10000, TaxAdapter.SORT_PSID_STATUS_INIT_DATE, false);
        } catch (Throwable pThrowable) {
            throw new RuntimeException(pThrowable);
        }
        return taxPayments.getReturnsList();
    }

    @WebMethod
    public void addTaxPaymentAgentOnHoldReason(@WebParam(name="paymentId")String pPaymentId, @WebParam(name="companyId")String companyId) {
        try {
            new TaxAdapter().addTaxPaymentAgentOnHoldReason(pPaymentId, companyId);
        } catch (Throwable pThrowable) {
            throw new RuntimeException(pThrowable);
        }
    }

    @WebMethod
    public void removeTaxPaymentAgentOnHoldReason(@WebParam(name="paymentId")String pPaymentId, @WebParam(name="companyId")String pCompanyId) {
        try {
            new TaxAdapter().removePaymentOnHoldReason(pPaymentId, "Agent", pCompanyId);
        } catch (Throwable pThrowable) {
            throw new RuntimeException(pThrowable);
        }
    }

    @WebMethod
    public List<SAPPayment> getZeroDollarMoneyMovementTransactions(@WebParam(name = "paymentTemplateCode") String paymentTemplateCd) throws Exception {
        List<SAPPayment> payments = new ArrayList<SAPPayment>();
        try {
            PayrollServices.beginUnitOfWork();
            if (StringUtils.isEmpty(paymentTemplateCd)) {
                throw new RuntimeException("paymentTemplateCode can not be null");
            }

            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)
                                                                                                    .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCd)))
                                                                                                    .sort(MoneyMovementTransaction.Company().SourceCompanyId(), MoneyMovementTransaction.TaxPaymentStatus(),
                                                                                                                                                                MoneyMovementTransaction.InitiationDate());
            for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
                payments.add(TaxTranslator.getPayment(moneyMovementTransaction, moneyMovementTransaction.getTaxPaymentStatus() != null ? moneyMovementTransaction.getTaxPaymentStatus().toString():"", null));
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return payments;
    }

}

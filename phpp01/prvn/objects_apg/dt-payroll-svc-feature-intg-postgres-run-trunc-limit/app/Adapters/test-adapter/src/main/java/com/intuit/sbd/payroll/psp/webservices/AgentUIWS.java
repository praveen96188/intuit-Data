package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.Validator;
import flex.messaging.util.StringUtils;
import org.apache.commons.io.FileUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Feb 28, 2011
 * Time: 3:18:15 PM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class AgentUIWS {

    @WebMethod
    public ArrayList<SAPFraudEvent> findCompanyFraudEvents(@WebParam(name = "pEinCid") String pEinCid,
                                                              @WebParam(name = "pFraudEventCategory") String pFraudEventCategory,
                                                              @WebParam(name = "pPayrollAmount") String pPayrollAmount,
                                                              @WebParam(name = "pFromDate") String pFromDate,
                                                              @WebParam(name = "pToDate") String pToDate,
                                                              @WebParam(name = "eventTypeCodes") ArrayList<String> eventTypeCodes) throws Exception {

        if (pEinCid!=null && !Validator.isValidEIN(pEinCid)) {
            throw new RuntimeException("Invalid EIN.");
        }
        if (pFraudEventCategory == null || pFraudEventCategory.trim().length() < 1) {
            throw new RuntimeException("FraudEventCategory is required.");
        }
        if (pPayrollAmount == null && !Validator.isDouble(pPayrollAmount)) {
            throw new RuntimeException("Invalid payroll amount.");
        }
        CompanyAdapter ca = new CompanyAdapter(false);

        ArrayList<SAPFraudEvent> sapFraudEvents = null;
        try {
            sapFraudEvents = ca.findCompanyFraudEvents(pEinCid, pFraudEventCategory, new Double(pPayrollAmount), (pFromDate == null) ? null : new Date(pFromDate), (pToDate == null) ? null : new Date(pToDate), eventTypeCodes);
        } catch (Throwable t) {
            throw new Exception(t);
        }

        return sapFraudEvents;
    }

    @WebMethod
    public SAPSearchResults<SAPBankReturn> findCompanyBankReturns(
            @WebParam(name = "pFein") String pFein,
            @WebParam(name = "pFromDate") String pFromDate,
            @WebParam(name = "pToDate") String pToDate,
            @WebParam(name = "pShowOpen") boolean pShowOpen,
            @WebParam(name = "pShowResolved") boolean pShowResolved,
            @WebParam(name = "pTransactionType") String pTransactionType,
            @WebParam(name = "pTransactionCategory") String pTransactionCategory,
            @WebParam(name = "pExclude5DayFunding") boolean pExclude5DayFunding,
            @WebParam(name = "includeCode") String includeCode,
            @WebParam(name = "pAmount") String pAmount,
            @WebParam(name = "pOrderBy") String pOrderBy,
            @WebParam(name = "pOrderDesc") boolean pOrderDesc,
            @WebParam(name = "pFirstResult") String pFirstResult,
            @WebParam(name = "pMaxResults") String pMaxResults,
            @WebParam(name = "isForPrinting") boolean isForPrinting) throws Exception {

        if (!Validator.isValidEIN(pFein)) {
            throw new RuntimeException("No EIN is specified / Invalid.");
        }

        if(pFromDate==null || pFromDate.length() < 1)
        {
          throw new RuntimeException("pFromDate is required.");
        }

        if(pToDate==null || pToDate.length() < 1)
        {
          throw new RuntimeException("pToDate is required.");
        }
        
        BankReturnAdapter ba = new BankReturnAdapter();

        SAPSearchResults<SAPBankReturn> sapBankReturns = null;
        try {
            sapBankReturns = ba.findCompanyBankReturns(pFein,
                (pFromDate==null)? null: new Date(pFromDate),
                (pToDate==null)? null: new Date(pToDate),
                pShowOpen,
                pShowResolved,
                pTransactionType,
                pTransactionCategory,
                pExclude5DayFunding,
                includeCode,
                new Double(pAmount),
                pOrderBy,
                pOrderDesc,
                new Integer(pFirstResult),
                new Integer(pMaxResults),
                isForPrinting);
        } catch (Throwable t) {
            throw new Exception(t);
        }

        return sapBankReturns;
    }

    @WebMethod
    public ArrayList<SAPLawTransactions> findTaxTransactions(@WebParam(name = "sourceSystemCd") String sourceSystemCd,
                                                                @WebParam(name = "companyId") String companyId,
                                                                @WebParam(name = "transactionDescription") String transactionDescription,
                                                                @WebParam(name = "agencyCd") String agencyCd,
                                                                @WebParam(name = "paymentTemplateCd") String paymentTemplateCd,
                                                                @WebParam(name = "specifiedLawId") String specifiedLawId,
                                                                @WebParam(name = "paymentMethod") String paymentMethod,
                                                                @WebParam(name = "yearQuarterStartDate") String yearQuarterStartDate,
                                                                @WebParam(name = "yearQuarterEndDate") String yearQuarterEndDate,
                                                                @WebParam(name = "includeNotPostedPayments") boolean includeNotPostedPayments) throws Exception {

        if(sourceSystemCd==null || sourceSystemCd.length() < 1)
        {
          throw new RuntimeException("sourceSystemCd is required.");
        }

        if(companyId==null || companyId.length() < 1)
        {
          throw new RuntimeException("companyId is required.");  
        }

        if(agencyCd==null || agencyCd.length() < 1)
        {
          throw new RuntimeException("agencyCd is required.");
        }

        if (StringUtils.isEmpty(yearQuarterStartDate)) {
            throw new RuntimeException("yearQuarterStartDate is required");
        }
        if (StringUtils.isEmpty(yearQuarterEndDate)) {
            throw new RuntimeException("yearQuarterEndDate is required");
        }

        TaxAdapter ta = new TaxAdapter();

        ArrayList<SAPLawTransactions> sapLawTransactions = null;
        try {
            sapLawTransactions = ta.findTaxTransactions(sourceSystemCd,
                companyId,
                transactionDescription,
                agencyCd,
                paymentTemplateCd,
                specifiedLawId,
                paymentMethod,
                new Date(yearQuarterStartDate),
                new Date(yearQuarterEndDate),
                includeNotPostedPayments);
        } catch (Throwable t) {
            throw new Exception(t);
        }

        return sapLawTransactions;
    }

    @WebMethod
    public void uploadQueueAndRunLedgerOperationsJob(@WebParam(name = "pathName") String pathName) {
        try {
            byte[] file = FileUtils.readFileToByteArray(new File(pathName));
            new AdministrationAdapter().uploadLedgerOperationsFile(file, "");
            queueCreatedLedgerOperationJobs();
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        } catch (Throwable pThrowable) {
            throw new RuntimeException(pThrowable);
        }
    }

    @WebMethod
    public void createQueueAndRunTORJob(@WebParam(name = "paymentTemplate") String paymentTemplate,
                                        @WebParam(name = "year") int year,
                                        @WebParam(name = "quarter") int quarter) {
        try {
            new AdministrationAdapter().createTORLedgerOperationJob(paymentTemplate, SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(year, quarter).getLastDayOfQuarter()));
            queueCreatedLedgerOperationJobs();
            BatchJobManager.runJob(BatchJobType.LedgerOperations);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

    }

    private void queueCreatedLedgerOperationJobs() throws Throwable {
        for (SAPLedgerOperationJob sapLedgerOperationJob : new AdministrationAdapter().getLedgerOperationJobs()) {
            if (sapLedgerOperationJob.getStatus().equals("Created")) {
                new AdministrationAdapter().queueLedgerOperationJob(sapLedgerOperationJob.getId());
            }
        }
    }

    @WebMethod
    public void createTOR(@WebParam(name = "companyId") String companyId,
                          @WebParam(name = "paymentTemplate") String paymentTemplate,
                          @WebParam(name = "year") int year,
                          @WebParam(name = "quarter") int quarter) {
        try {
            new TaxAdapter().createTORTransactions(SourceSystemCode.QBDT.toString(), companyId, paymentTemplate, new SAPQuarter(year, quarter));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @WebMethod
    public List<SAPTemplateQuarterAmount> getAgencyTaxRefundBreakdown(@WebParam(name = "companyId") String companyId) {
        try {
            List<SAPTemplateQuarterAmount> agencyTaxRefundBreakdown = new TaxAdapter().getAgencyTaxRefundBreakdown(SourceSystemCode.QBDT.toString(), companyId);
            Collections.sort(agencyTaxRefundBreakdown, new Comparator<SAPTemplateQuarterAmount>() {
                public int compare(SAPTemplateQuarterAmount o1, SAPTemplateQuarterAmount o2) {
                    if (!o1.getPaymentTemplateCd().equals(o2.getPaymentTemplateCd())) {
                        return o1.getPaymentTemplateCd().compareTo(o2.getPaymentTemplateCd());
                    }
                    return o1.getQuarter().compareTo(o2.getQuarter());
                }
            });
            return agencyTaxRefundBreakdown;
        } catch (Throwable pThrowable) {
            throw new RuntimeException(pThrowable);
        }
    }

    @WebMethod
    public void createManualEFTPSEnrollment(@WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                            @WebParam(name = "EIN") String EIN,
                                            @WebParam(name = "legalName") String legalName,
                                            @WebParam(name = "legalZip") String legalZip) {

        try {
            new TaxAdapter().createManualEFTPSEnrollment(SourceSystemCode.QBDT.toString(), sourceCompanyID, EIN, legalName, legalZip);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @WebMethod
    public void updateCancellationInfo(@WebParam(name = "sourceSystemCd") String sourceSystemCd,
                                       @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                       @WebParam(name = "cancelInfo") SAPTaxCompanyServiceInfo cancelInfo) {
        try {
            new CompanyAdapter().updateCompanyCancellationInfo(sourceSystemCd, sourceCompanyID, cancelInfo);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /*
    Here are the fields on rates that must be set:
    quarter
        year (2013)
        quarter (4)
    [lawRates]
        law
            lawId (61)
        newPercentage (5 =5%  NaN=Do not change)

     Returns [SAPLawQuarterRates], sorted by law:
     law
     [quarterRates], sorted by quarter
        quarter
            year
            quarter
        currentPercentage
     */
    @WebMethod
    public List<SAPLawQuarterRates> updateRatesForSingleQuarter(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                                                @WebParam(name = "sourceCompanyID") String pSourceSystemId,
                                                                @WebParam(name = "paymentTemplateCd") String paymentTemplateCd,
                                                                @WebParam(name = "rates") SAPQuarterLawRates rates,
                                                                @WebParam(name = "pushToQuickbooks") boolean pushToQuickbooks) {
        try {
            DataLoadServices.setPrincipalToAgent(OperationId.EditSUIRateCurrQTR, OperationId.EditRatesInOtherQTRs, OperationId.RateSuperUser, OperationId.EditRatesOtherLaws);
            new TaxAdapter().updateRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks);
            return new TaxAdapter().findAllEditableRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }


    /*
    Here are the fields on rates that must be set for each:
    law
        lawId
    [rates]
        quarter
            year
            quarter
        newPercentage

    Return same as above
    */
    @WebMethod
    public List<SAPLawQuarterRates> updateRatesForAllQuarters(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                                              @WebParam(name = "sourceCompanyID") String pSourceSystemId,
                                                              @WebParam(name = "paymentTemplateCd") String paymentTemplateCd,
                                                              @WebParam(name = "rates") List<SAPLawQuarterRates> rates,
                                                              @WebParam(name = "pushToQuickbooks") boolean pushToQuickbooks) {
        try {
            new TaxAdapter().updateAllRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd, rates, pushToQuickbooks);
            return new TaxAdapter().findAllEditableRates(pSourceSystemCd, pSourceSystemId, paymentTemplateCd);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /*
    returns same as above, but for only the specified law
     */
    @WebMethod
    public SAPLawQuarterRates queryLawRates(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                            @WebParam(name = "sourceCompanyID") String pSourceSystemId,
                                            @WebParam(name = "lawId") String lawId) {
        Law law;
        PaymentTemplate paymentTemplate;
        try {
            PayrollServices.beginUnitOfWork();
            law = Application.findById(Law.class, lawId);
            paymentTemplate = law.getPaymentTemplate();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        try {
            List<SAPLawQuarterRates> allEditableRates = new TaxAdapter().findAllEditableRates(pSourceSystemCd, pSourceSystemId, paymentTemplate.getPaymentTemplateCd());
            for (SAPLawQuarterRates allEditableRate : allEditableRates) {
                if (allEditableRate.getLaw().getLawId().equals(law.getLawId())) {
                    return allEditableRate;
                }
            }

            return null;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /*
    fields on SAPFilerType that must be set:
    filerType (941 or 944)
    quarter
        year
        quarter
     */
    @WebMethod
    public void updateFilerType(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                                              @WebParam(name = "sourceCompanyID") String pSourceSystemId,
                                                              @WebParam(name = "paymentTemplateCd") String paymentTemplateCd,
                                                              @WebParam(name = "rates") SAPFilerType filerType) {
        try {
            new TaxAdapter().updateFilerType(pSourceSystemCd, pSourceSystemId, filerType);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.*;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.*;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp.PSPAuthenticate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * $Author: jchickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/DISAdapter.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/03 11:23:11 $
 * $Author: jchickanosky $
 * <p/>
 * DIS Adapter containing all methods for this endpoint.
 */
@WebService()
public class DISAdapter {

    private static final SpcfLogger logger;
    public static final String DIS_RESPONSE_RESULT_XML_STR = "DISResponse";

    static {
        logger = PayrollServices.getLogger(DISAdapter.class);
    }

    /**
     * Get Financial Transactions
     *
     * @param pAuthenticateRequestDISDTO - JAX-WS request for Query Financial Transactions call
     * @return AuthenticateResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "Authenticate")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public AuthenticateResponseDISDTO Authenticate(@WebParam(name = "AuthenticateRequest") AuthenticateRequestDISDTO pAuthenticateRequestDISDTO) {
        AuthenticateResponseDISDTO response = (AuthenticateResponseDISDTO) doMethod(new PSPAuthenticate(pAuthenticateRequestDISDTO));
        return response;
    }

    /**
     * Get Financial Transactions
     *
     * @param pQueryFinancialTransactionsRequestDISDTO - JAX-WS request for Query Financial Transactions call
     * @return QueryFinancialTransactionsResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryFinancialTransactions")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryFinancialTransactionsResponseDISDTO Query_FinancialTransactions(@WebParam(name = "QueryFinancialTransactionsRequest") QueryFinancialTransactionsRequestDISDTO pQueryFinancialTransactionsRequestDISDTO) {
        QueryFinancialTransactionsResponseDISDTO response = (QueryFinancialTransactionsResponseDISDTO) doMethod(new PSPQueryFinancialTransactions(pQueryFinancialTransactionsRequestDISDTO));
        return response;
    }

    /**
     * Refund Employer Financial Transactions
     *
     * @param pRefundEmployerFinancialTransactionRequestDISDTO - JAX-WS request for Refund Employer Financial Transactions call
     * @return RefundEmployerFinancialTransactionResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "RefundEmployerFinancialTransaction")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public RefundEmployerFinancialTransactionResponseDISDTO Update_RefundEmployerFinancialTransaction(@WebParam(name = "RefundEmployerFinancialTransactionRequest") RefundEmployerFinancialTransactionRequestDISDTO pRefundEmployerFinancialTransactionRequestDISDTO) {
        RefundEmployerFinancialTransactionResponseDISDTO response = (RefundEmployerFinancialTransactionResponseDISDTO) doMethod(new PSPRefundEmployerFinancialTransaction(pRefundEmployerFinancialTransactionRequestDISDTO));
        return response;
    }

    /**
     * Rebill Employer Financial Transactions
     *
     * @param pRebillEmployerFinancialTransactionRequestDISDTO - JAX-WS request for Rebill Employer Financial Transactions call
     * @return RebillEmployerFinancialTransactionResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "RebillEmployerFinancialTransaction")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public RebillEmployerFinancialTransactionResponseDISDTO Update_RebillEmployerFinancialTransaction(@WebParam(name = "RebillEmployerFinancialTransactionRequest") RebillEmployerFinancialTransactionRequestDISDTO pRebillEmployerFinancialTransactionRequestDISDTO) {
        RebillEmployerFinancialTransactionResponseDISDTO response = (RebillEmployerFinancialTransactionResponseDISDTO) doMethod(new PSPRebillEmployerFinancialTransaction(pRebillEmployerFinancialTransactionRequestDISDTO));
        return response;
    }

    /**
     * Retrieve Company Payrolls
     *
     * @param pQueryCompanyPayrollsRequestDISDTO - JAX-WS request for Retrieve Employer Financial Transactions call
     * @return QueryCompanyPayrollsResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyPayrolls")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyPayrollsResponseDISDTO Query_CompanyPayrolls(@WebParam(name = "QueryCompanyPayrollsRequest") QueryCompanyPayrollsRequestDISDTO pQueryCompanyPayrollsRequestDISDTO) {
        QueryCompanyPayrollsResponseDISDTO response = (QueryCompanyPayrollsResponseDISDTO) doMethod(new PSPQueryCompanyPayrolls(pQueryCompanyPayrollsRequestDISDTO));
        return response;
    }


    /**
     * Query Employer Financial Transactions
     *
     * @param pQueryEmployerFinancialTransactionsRequestDISDTO - JAX-WS request for Query Employer Financial Transactions call
     * @return QueryEmployerFinancialTransactionsResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryEmployerFinancialTransactions")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryEmployerFinancialTransactionsResponseDISDTO Query_EmployerFinancialTransactions(@WebParam(name = "QueryEmployerFinancialTransactionsRequest") QueryEmployerFinancialTransactionsRequestDISDTO pQueryEmployerFinancialTransactionsRequestDISDTO) {
        QueryEmployerFinancialTransactionsResponseDISDTO response = (QueryEmployerFinancialTransactionsResponseDISDTO) doMethod(new PSPQueryEmployerFinancialTransactions(pQueryEmployerFinancialTransactionsRequestDISDTO));
        return response;
    }

    /**
     * Query tax rate history for tax payment id
     *
     * @param pQueryUpdatedCompaniesRequestDISDTO - JAX-WS request for Query Company Full call
     * @return QueryUpdatedCompaniesResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryUpdatedCompanies")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryUpdatedCompaniesResponseDISDTO Query_UpdatedCompanies(@WebParam(name = "QueryUpdatedCompaniesRequest") QueryUpdatedCompaniesRequestDISDTO pQueryUpdatedCompaniesRequestDISDTO) {
        QueryUpdatedCompaniesResponseDISDTO response = (QueryUpdatedCompaniesResponseDISDTO) doMethod(new PSPQueryUpdatedCompanies(pQueryUpdatedCompaniesRequestDISDTO));
        return response;
    }

    /**
     * Query tax rate history for tax payment id
     *
     * @param pLawRateHistoryRequestDISDTO - JAX-WS request for Query Company Full call
     * @return LawRateHistoryResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryLawRateHistory")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryLawRateHistoryResponseDISDTO Query_LawRateHistory(@WebParam(name = "QueryLawRateHistoryRequest") QueryLawRateHistoryRequestDISDTO pLawRateHistoryRequestDISDTO) {
        QueryLawRateHistoryResponseDISDTO response = (QueryLawRateHistoryResponseDISDTO) doMethod(new PSPQueryLawRateHistory(pLawRateHistoryRequestDISDTO));
        return response;
    }

    /**
     * Query deposit freq history for tax payment id
     *
     * @param pDepositFrequencyHistoryRequestDISDTO - JAX-WS request for Query Company Full call
     * @return DepositFrequencyHistoryResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryDepositFrequencyHistory")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryDepositFrequencyHistoryResponseDISDTO Query_DepositFrequencyHistory(@WebParam(name = "QueryDepositFrequencyHistoryRequest") QueryDepositFrequencyHistoryRequestDISDTO pDepositFrequencyHistoryRequestDISDTO) {
        QueryDepositFrequencyHistoryResponseDISDTO response = (QueryDepositFrequencyHistoryResponseDISDTO) doMethod(new PSPQueryDepositFrequencyHistory(pDepositFrequencyHistoryRequestDISDTO));
        return response;
    }


    /**
     * Query tax payment history for tax payment id
     *
     * @param pPaymentMethodHistoryRequestDISDTO - JAX-WS request for Query Company Full call
     * @return PaymentMethodHistoryResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryPaymentMethodHistory")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryPaymentMethodHistoryResponseDISDTO Query_PaymentMethodHistory(@WebParam(name = "QueryPaymentMethodHistoryRequest") QueryPaymentMethodHistoryRequestDISDTO pPaymentMethodHistoryRequestDISDTO) {
        QueryPaymentMethodHistoryResponseDISDTO response = (QueryPaymentMethodHistoryResponseDISDTO) doMethod(new PSPQueryPaymentMethodHistory(pPaymentMethodHistoryRequestDISDTO));
        return response;
    }

    /**
     * Query company wage and tax liab totals for a year
     *
     * @param pGetAgencyRulesRequestDISDTO - JAX-WS request for Query Company Full call
     * @return GetAgencyRulesResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "GetAgencyRules")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public GetAgencyRulesResponseDISDTO Query_GetAgencyRules(@WebParam(name = "GetAgencyRulesRequest") GetAgencyRulesRequestDISDTO pGetAgencyRulesRequestDISDTO) {
        GetAgencyRulesResponseDISDTO response = (GetAgencyRulesResponseDISDTO) doMethod(new PSPGetAgencyRules(pGetAgencyRulesRequestDISDTO));
        return response;
    }

    /**
     * Query company wage and tax liab totals for a year
     *
     * @param pPaymentTemplateYearPaymentRequestDISDTO - JAX-WS request for Query Company Full call
     * @return PaymentTemplateYearPaymentResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyAgenciesYearInfo")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyAgenciesYearInfoResponseDISDTO Query_CompanyAgenciesYearInfo(@WebParam(name = "QueryCompanyAgenciesYearInfoRequest") QueryCompanyAgenciesYearInfoRequestDISDTO pPaymentTemplateYearPaymentRequestDISDTO) {
        QueryCompanyAgenciesYearInfoResponseDISDTO response = (QueryCompanyAgenciesYearInfoResponseDISDTO) doMethod(new PSPQueryCompanyAgenciesYearInfo(pPaymentTemplateYearPaymentRequestDISDTO));
        return response;
    }

    /**
     * Query company courtesy fee refunds
     *
     * @param pPaymentTemplateYearPaymentRequestDISDTO - JAX-WS request for Query Company Full call
     * @return PaymentTemplateYearPaymentResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyCourtesyFeeRefunds")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyCourtesyFeeRefundsResponseDISDTO Query_CompanyCourtesyFeeRefunds(@WebParam(name = "QueryCompanyCourtesyFeeRefundsRequest") QueryCompanyCourtesyFeeRefundsRequestDISDTO pPaymentTemplateYearPaymentRequestDISDTO) {
        QueryCompanyCourtesyFeeRefundsResponseDISDTO response = (QueryCompanyCourtesyFeeRefundsResponseDISDTO) doMethod(new PSPQueryCompanyCourtesyFeeRefunds(pPaymentTemplateYearPaymentRequestDISDTO));
        return response;
    }

    /**
     * Query company wage and tax liab totals for a year
     *
     * @param pQueryCompanyWagedEmployeeCountRequestDISDTO - JAX-WS request for Query Company Full call
     * @return QueryCompanyWagedEmployeeCountResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyEmployeesWihPaycheckCount")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyEmployeesWihPaycheckCountResponseDISDTO Query_QueryCompanyEmployeesWihPaycheckCount(@WebParam(name = "QueryCompanyEmployeesWihPaycheckCountRequest") QueryCompanyEmployeesWihPaycheckCountRequestDISDTO pQueryCompanyWagedEmployeeCountRequestDISDTO) {
        QueryCompanyEmployeesWihPaycheckCountResponseDISDTO response = (QueryCompanyEmployeesWihPaycheckCountResponseDISDTO) doMethod(new PSPQueryCompanyEmployeesWihPaycheckCount(pQueryCompanyWagedEmployeeCountRequestDISDTO));
        return response;
    }

    /**
     * Query company payroll dates (first and last)
     *
     * @param pQueryCompanyPayrollDatesRequestDISDTO - JAX-WS request for Query Company Full call
     * @return QueryCompanyPayrollDatesResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyLastestPayrollDates")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyLatestPayrollDatesResponseDISDTO Query_CompanyLatestPayrollDates(@WebParam(name = "QueryCompanyPayrollDatesRequest", targetNamespace = "http://v1_7.dis.adapters.psp.payroll.sbd.intuit.com/") QueryCompanyLatestPayrollDatesRequestDISDTO pQueryCompanyPayrollDatesRequestDISDTO) {
        QueryCompanyLatestPayrollDatesResponseDISDTO response = (QueryCompanyLatestPayrollDatesResponseDISDTO) doMethod(new PSPQueryCompanyLatestPayrollDates(pQueryCompanyPayrollDatesRequestDISDTO));
        return response;
    }

    /**
     * Query company full web service method
     *
     * @param pSearchSAPCompanyRequestDISDTO - JAX-WS request for Query Company Full call
     * @return SearchSAPCompanyResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "SearchSAPCompany")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public SearchSAPCompanyResponseDISDTO Query_SAPCompany(@WebParam(name = "SearchSAPCompanyRequest") SearchSAPCompanyRequestDISDTO pSearchSAPCompanyRequestDISDTO) {
        SearchSAPCompanyResponseDISDTO response = (SearchSAPCompanyResponseDISDTO) doMethod(new PSPSearchSAPCompany(pSearchSAPCompanyRequestDISDTO));
        return response;
    }

    /**
     * @param pQueryCompanyEventsRequestDISDTO - JAX-WS web service request
     * @return QueryCompanyEventsResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyEvents")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyEventsResponseDISDTO Query_CompanyEvent(@WebParam(name = "QueryCompanyEventsRequest", targetNamespace = "http://v1_7.dis.adapters.psp.payroll.sbd.intuit.com/") QueryCompanyEventsRequestDISDTO pQueryCompanyEventsRequestDISDTO) {
        QueryCompanyEventsResponseDISDTO response = (QueryCompanyEventsResponseDISDTO) doMethod(new PSPQueryCompanyEvents(pQueryCompanyEventsRequestDISDTO));
        return response;
    }

    /**
     * @param pRequest - JAX-WS web service request
     * @return QueryPaymentTemplatesResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "QueryCompanyPaymentTemplates")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public QueryCompanyPaymentTemplatesResponseDISDTO Query_CompanyPaymentTemplates(@WebParam(name = "QueryCompanyPaymentTemplatesRequest") QueryCompanyPaymentTemplatesRequestDISDTO pRequest) {
        QueryCompanyPaymentTemplatesResponseDISDTO response = (QueryCompanyPaymentTemplatesResponseDISDTO) doMethod(new PSPQueryCompanyPaymentTemplates(pRequest));
        return response;
    }


    /**
     * Update Rate Financial Transactions
     *
     * @param pUpdateCompanyTaxRateRequestDISDTO - JAX-WS request
     * @return UpdateCompanyTaxRateResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "UpdateCompanyTaxRate")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public UpdateCompanyTaxRateResponseDISDTO Update_CompanyTaxRate(@WebParam(name = "UpdateCompanyTaxRateRequest") UpdateCompanyTaxRateRequestDISDTO pUpdateCompanyTaxRateRequestDISDTO) {
        UpdateCompanyTaxRateResponseDISDTO response = (UpdateCompanyTaxRateResponseDISDTO) doMethod(new PSPUpdateCompanyTaxRate(pUpdateCompanyTaxRateRequestDISDTO));
        return response;
    }

    /**
     * Update Rate Financial Transactions
     *
     * @param pUpdateCompanyAgencyIdRequestDISDTO - JAX-WS request
     * @return UpdateCompanyAgencyIdResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "UpdateCompanyAgencyId")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public UpdateCompanyAgencyIdResponseDISDTO Update_CompanyAgencyId(@WebParam(name = "UpdateCompanyAgencyIdRequest") UpdateCompanyAgencyIdRequestDISDTO pUpdateCompanyAgencyIdRequestDISDTO) {
        UpdateCompanyAgencyIdResponseDISDTO response = (UpdateCompanyAgencyIdResponseDISDTO) doMethod(new PSPUpdateCompanyAgencyId(pUpdateCompanyAgencyIdRequestDISDTO));
        return response;
    }

    /**
     * Update Rate Financial Transactions
     *
     * @param pUpdateCompanyFilingFrequencyRequestDISDTO - JAX-WS request
     * @return UpdateCompanyFilingFrequencyResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "UpdateCompanyFilingFrequency")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public UpdateCompanyFilingFrequencyResponseDISDTO Update_CompanyFilingFrequency(@WebParam(name = "UpdateCompanyFilingFrequencyRequest") UpdateCompanyFilingFrequencyRequestDISDTO pUpdateCompanyFilingFrequencyRequestDISDTO) {
        UpdateCompanyFilingFrequencyResponseDISDTO response = (UpdateCompanyFilingFrequencyResponseDISDTO) doMethod(new PSPUpdateCompanyFilingFrequency(pUpdateCompanyFilingFrequencyRequestDISDTO));
        return response;
    }


    /**
     * Refund Employer Financial Transactions
     *
     * @param pCreatePenaltiesAndInterestRefundsRequestDISDTO - JAX-WS request for Refund Employer Financial Transactions call
     * @return CreatePenaltiesAndInterestRefundsResponseDISDTO - JAX-WS web service response
     */
    @WebMethod(operationName = "CreatePenaltiesAndInterestRefunds")
    @WebResult(name = DIS_RESPONSE_RESULT_XML_STR)
    public CreatePenaltiesAndInterestRefundsResponseDISDTO Update_CreatePenaltiesAndInterestRefunds(@WebParam(name = "CreatePenaltiesAndInterestRefundsRequest") CreatePenaltiesAndInterestRefundsRequestDISDTO pCreatePenaltiesAndInterestRefundsRequestDISDTO) {
        CreatePenaltiesAndInterestRefundsResponseDISDTO response = (CreatePenaltiesAndInterestRefundsResponseDISDTO) doMethod(new PSPCreatePenaltiesAndInterestRefunds(pCreatePenaltiesAndInterestRefundsRequestDISDTO));
        return response;
    }

    /***
     * Fulfill the method web service request for the process passed in.
     *
     * @param disProcess - Process implementing DISProcessInterface to perform logic for the
     *                     WS call we are handling.
     * @return Object representing the JAX-WS Java file representing the WS response.
     */
    private Object doMethod(DISProcessInterface disProcess) {

        Object response = null;
        String sourceCompanyId = "";

        try {
            response = disProcess.process();
        } catch (DISException e) {
            // JPC 10/29/2012 - Going forward, the intent is for each process to log its own
            //     messages as appropriate.  The existing code to date has an DISException
            //     thrown whenever an error response is needed.  Going forward for proactive error
            //     responses, each process will do its own logging and return an error response as
            //     necessary.  See PSPAuthenticate as an example.
            if (e.getSourceException() != null) {
                logger.info("sourceCompanyId: " + sourceCompanyId, e.getSourceException());
            } else {
                logger.info("sourceCompanyId: " + sourceCompanyId, e);
            }

            response = disProcess.createErrorResponse(e.getDisMessages());
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("sourceCompanyId: " + sourceCompanyId + " " + t.getMessage(), t);
            response = disProcess.createErrorResponse(DISMessages.systemError(t.getMessage()));
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return response;
    }

}

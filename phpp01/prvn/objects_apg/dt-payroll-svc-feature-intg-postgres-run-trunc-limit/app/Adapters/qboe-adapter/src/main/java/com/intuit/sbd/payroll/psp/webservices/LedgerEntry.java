package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import intuit.osp.common.wsf.base.WSException;
import intuit.osp.common.wsf.base.WSValidationException;
import intuit.osp.common.wsf.server.WS;
import intuit.osp.pse.dd.wsapi.xsd.ledgerentryquery.LedgerEntryQuery;
import intuit.osp.pse.dd.wsapi.xsd.ledgerentryqueryrs.LedgerEntryQueryRs;
import intuit.osp.pse.dd.wsapi.xsd.ledgerentryret.LedgerEntryRet;
import intuit.osp.pse.dd.wsapi.xsd.responsestatus.ResponseStatus;
import org.w3c.dom.Element;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

/**
 *
 * @author lkollu
 */
public class LedgerEntry extends WS {
    private static SpcfLogger logger = Application.getLogger(LedgerEntry.class);

    public static final String SERVICE_NAME = "LedgerEntry";

    /**
     * This method executes logic for query request.
     * Returns the Ledger entries for the specified company, ledgerAccountCode
     * and payrollRunId.
     * payrollRunId is optional 
     * @param requestDocument
     * @return
     * @throws intuit.osp.common.wsf.base.WSException
     */
    public Element query(Element requestDocument) throws WSException {
        Element returnDoc;
        String[] expectedErrorCodes = {"169", "194"};
        WSServerContext wsServerContext = new WSServerContext(SERVICE_NAME, LedgerEntry.Operations.QUERY);

        LedgerEntryQueryRs ledgerEntryQueryRs = null;
        String sourcePayrollRunId = null;
    	try {
            PayrollServices.beginUnitOfWork();
            LedgerEntryQuery ledgerEntryQuery =
	        	(LedgerEntryQuery) wsServerContext.translateInputElement(requestDocument);

            ProcessResult validationResult = new ProcessResult();
    		ledgerEntryQueryRs = (LedgerEntryQueryRs) wsServerContext.getOutputDTO();
            String sourceSystemCode = ledgerEntryQuery.getSourceSystemCd();
            String sourceCompanyId = ledgerEntryQuery.getCompanyID();
            com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = null;

            com.intuit.sbd.payroll.psp.domain.Company company = com.intuit.sbd.payroll.psp.domain.Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCode));
            if (company == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId, sourceSystemCode, sourceCompanyId);
            } else {
                // Check if PayrollRun exists for the company
                sourcePayrollRunId = ledgerEntryQuery.getDDTxBatchID();

                if (sourcePayrollRunId != null) {
                    payrollRun = com.intuit.sbd.payroll.psp.domain.PayrollRun.findPayrollRun(company, sourcePayrollRunId);
                    if ( payrollRun == null) {
                        validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, sourcePayrollRunId,
                                sourcePayrollRunId, sourceSystemCode, sourceCompanyId);
                    }
                }
            }

            List retList = ledgerEntryQueryRs.getLedgerEntryRet();
            if (validationResult.isSuccess()) {

                DomainEntitySet<FinancialTransaction> collection =
                        FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(
                                company,
                                sourcePayrollRunId, DDCodeToPSP.getLedgerAccountCode(ledgerEntryQuery.getLedgerAccountCd()));

                for (FinancialTransaction finTxn : collection) {
                    DomainEntitySet<PostingRule> postingRules = PostingRule.findPostingRuleByFinancialTransaction(
                            finTxn, DDCodeToPSP.getLedgerAccountCode(ledgerEntryQuery.getLedgerAccountCd()));

                    for (PostingRule postingRule : postingRules) {
                        retList.add(this.buildLedgerEntryRet(finTxn, postingRule));
                    }
                }

            } else {
                retList = null;
            }
            ResponseStatus responseStatus = DDCommon.build_ResponseStatus(validationResult, expectedErrorCodes);
            ledgerEntryQueryRs.setResponseStatus(responseStatus);
            returnDoc = wsServerContext.translateOutputDTO();
            PayrollServices.commitUnitOfWork();
        } catch (WSValidationException wsValidationException) {
            PayrollServices.rollbackUnitOfWork();
            logger.info(wsValidationException.getMessage(), wsValidationException.getCause());
            throw wsValidationException;
		}catch (Exception exception) {
            PayrollServices.rollbackUnitOfWork();
            logger.info(exception.getMessage(), exception);
			throw new WSException(DDCommon.pse_Error, exception);
		} finally {
            PayrollServices.rollbackUnitOfWork();
        }

		return returnDoc;
    }

    /**
     * This method builds LedgerEntryRet object based on supplied FinancialTransaction object.
     * @param pFinancialTransaction
     * @param pPostingRule
     * @return
     * @throws Exception
     */
    private LedgerEntryRet buildLedgerEntryRet(FinancialTransaction pFinancialTransaction, PostingRule pPostingRule) throws Exception {

            intuit.osp.pse.dd.wsapi.xsd.ledgerentryret.ObjectFactory objectFactory =
                new intuit.osp.pse.dd.wsapi.xsd.ledgerentryret.ObjectFactory();

            LedgerEntryRet ledgerEntryRet = objectFactory.createLedgerEntryRet();

            //FinancialTransaction financialTransaction = ledgerEntry.getFinancialTransactionState().getFinancialTransaction();
            com.intuit.sbd.payroll.psp.domain.Company company = pFinancialTransaction.getCompany();

            ledgerEntryRet.setSourceSystemCd(company.getSourceSystemCd().toString());
            ledgerEntryRet.setCompanyID(company.getSourceCompanyId());

            com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = pFinancialTransaction.getPayrollRun();
            if (payrollRun != null) {
                ledgerEntryRet.setDDTxBatchID(payrollRun.getSourcePayRunId());
            }

            com.intuit.sbd.payroll.psp.domain.LedgerAccount ledgerAccount = pPostingRule.getLedgerAccount();
            
            ledgerEntryRet.setLedgerAccountCd(DDCodeToPSP.getQBOELedgerAccountCode(ledgerAccount.getLedgerAccountCd()));
            ledgerEntryRet.setLedgerAccountDesc(ledgerAccount.getName());

            ledgerEntryRet.setAmount(new BigDecimal(pFinancialTransaction.getFinancialTransactionAmount().toString()));
            //ledgerEntryRet.setCredit(postingRules.get(pIndex).getCreditDebitInd().equals(CreditDebitCode.Credit.toString()));
            ledgerEntryRet.setCredit(pPostingRule.getCreditDebitInd().equals(CreditDebitCode.Credit.toString()));


            TransactionState transactionState = pFinancialTransaction.getCurrentTransactionState();
            ledgerEntryRet.setTxStatusCd(DDCodeToPSP.getQBOETransactionStateCode(transactionState.getTransactionStateCd()));
            ledgerEntryRet.setTxStatusDesc(transactionState.getName());

            FinancialTransactionState finTxnState = pFinancialTransaction.getCurrentFinancialTransactionState();

            Calendar calendar =
                    CalendarUtils.convertToCalendar(finTxnState.getTransactionStateEffectiveDate().toLocal());

            ledgerEntryRet.setPostingDate(calendar);

            TransactionType transactionType = pFinancialTransaction.getTransactionType();
            String sku = pFinancialTransaction.getSku();
            OfferingServiceChargeType offeringServiceCharge = null;
            if (null != sku) {
                offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
            }
            String transactionTypeCode = DDCodeToPSP.getQBOETransactionTypeCode(pFinancialTransaction.getTransactionType().
                getTransactionTypeCd(), offeringServiceCharge);
            ledgerEntryRet.setTxTypeCd(transactionTypeCode);
            ledgerEntryRet.setTxTypeDesc(transactionType.getName());
        //}
        
        return ledgerEntryRet;
    }    

    /**
     * Interface to store names of operations.
     */
    public interface Operations {

		public static final String QUERY = "query";
    }
}

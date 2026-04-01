/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/PayrollRunAdapter.java#8 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.payroll.agency.impl.RulesInfo;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.sap.lcds.proxy.PSPEntityProxy;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EmailUtils;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfDecimalImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import flex.messaging.io.PropertyProxyRegistry;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import java.math.BigDecimal;
import java.util.*;

/**
 * PayrollRunAdapter
 *
 * @author Joe Warmelink
 */
public class PayrollRunAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(PayrollRunAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    public static final String LS_UNRECOVERED_TRANSACTIONS = "Unrecovered";
    public static final String LS_COLLECTED_TRANSACTIONS = "Collected";
    public static final String LS_UNCOLLECTED_TRANSACTIONS = "Uncollected";
    public static final String LS_PREFUNDING_TRANSACTIONS = "Prefunding";

    private static final String LS_PAYROLL_REASON = "Payroll";
    private static final String LS_REFUND_FOR_VOID_REASON = "Refund for Void";
    private static final String LS_REDEBIT_REASON = "Redebit";
    private static final String LS_PARTIAL_REDEBIT_REASON = "Partial Redebit";
    private static final String LS_BANK_VERIFICATION_DEBIT_REASON = "Bank Verification Debit";
    private static final String LS_BANK_VERIFICATION_CREDIT_REASON = "Bank Verification Credit";
    private static final String LS_EMPLOYEE_DD_REVERSAL_REASON = "Employee DD Reversal";
    private static final String LS_EMPLOYER_ESCALATION_REASON = "Employer Escalation";
    private static final String LS_EMPLOYER_FRAUD_REASON = "Employer Fraud or Escalation Refund Credit";
    private static final String LS_REFUND_REBILL_REASON = "Refund/Rebill";
    private static final String LS_REFUND_REASON = "Refund";
    private static final String LS_Fee_REASON = "Fee";
    private static final String LS_INTUIT_FEE_TRANSFER_REASON = "Intuit Fee Transfer";
    private static final String LS_EMPLOYEE_DD_REVERSAL_DEBIT_REASON = "Employee DD Reversal Debit";
    private static final String LS_INTUIT_EMPLOYER_VERIFICATION_RETURN_TRANSFER_REASON = "Intuit Employer Verification Return Transfer";
    private static final String LS_EMPLOYER_VERIFICATION_CREDIT_RETURN_TRANSFER_REASON = "Employer Verification Credit Return Transfer";
    private static final String LS_EMPLOYER_TAX_CREDIT_RETURNED_TRANSFER = "Employer Tax Credit Returned Transfer";
    private static final String LS_WRITE_OFF = "Write off";
    private static final String LS_EMPLOYEE_RETURN_TRANSFER = "Employee Return Transfer";
    private static final String LS_TAX_VOID_TRANSFER = "Tax Void Transfer";
    private static final String LS_REISSUE_TAX_LIABILITY_TRANSFER = "Reissue Tax Liability Transfer";
    private static final String JPMC_TRACE_ID_NOT_AVAILABLE = "Not available";

    private static final SpcfMoney ZERO = new SpcfMoney("0.00");


    public PayrollRunAdapter() {
        registerProxies();

    }

    private void registerProxies() {
        PSPEntityProxy entityProxy = new PSPEntityProxy();
        PropertyProxyRegistry.getRegistry().register(SAPPayrollRun.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPPaycheck.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPPayline.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPTransactionType.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPActionEvent.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPPayrollEmployeeTransaction.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPPayrollTransaction.class, entityProxy);
        PropertyProxyRegistry.getRegistry().register(SAPCompanyLedgerAccount.class, entityProxy);
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewPayrollScreen)
    public ArrayList<SAPPaycheckLineItem> getLineItems(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pPaycheckGseq) throws Throwable {
        logger.info("validate PSID for getLineItems PayrollRunAdapter in SAP flow:::"+pCompanyId);
        ArrayList<SAPPaycheckLineItem> sapLineItemList = new ArrayList<SAPPaycheckLineItem>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Paycheck paycheck = Application.findById(Paycheck.class, SpcfUniqueId.createInstance(pPaycheckGseq));

            //Compensation items
            DomainEntitySet<Compensation> compensationItems = paycheck.getCompensationCollection();
            if (compensationItems != null) {
                for (Compensation compensation : compensationItems) {
                    sapLineItemList.add(PayrollRunTranslator.getSAPLineItemsFromCompensation(compensation));
                }
            }

            //Deduction items
            DomainEntitySet<Deduction> deductionItems = paycheck.getDeductionCollection();
            if (deductionItems != null) {
                for (Deduction deduction : deductionItems) {
                    sapLineItemList.add(PayrollRunTranslator.getSAPLineItemsFromDeduction(deduction));
                }
            }

            //EmployerContribution items
            DomainEntitySet<EmployerContribution> employerContributionItems = paycheck.getEmployerContributionCollection();
            if (compensationItems != null) {
                for (EmployerContribution employerContribution : employerContributionItems) {
                    sapLineItemList.add(PayrollRunTranslator.getSAPLineItemsFromEmployerContribution(employerContribution));
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving line items", pSourceSystemCd, pCompanyId, "Paycheck GUID", pPaycheckGseq, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapLineItemList;
    }

    //todo currently this is only non-dd, but should revisit screens once we have tax+dd
    @FlexMethod
    public ArrayList<SAPPaycheck> findPaychecks(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId, String pSourceSystemCd, String pSourcePayrollRunId) throws Throwable {
        ArrayList<SAPPaycheck> sapPaycheckList = new ArrayList<SAPPaycheck>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(pCompanyId,
                                                  SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<Paycheck> paycheckList = Paycheck.findCloudPaychecksBySourcePayrollRunId(company, pSourcePayrollRunId);

            for (Paycheck paycheck : paycheckList) {
                sapPaycheckList.add(PayrollRunTranslator.getSAPPaycheckFromDomainEntity(paycheck, paycheck.isTOKPaycheck()));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error retrieving paychecks for payroll run", pSourceSystemCd, pCompanyId, "SourcePayrollRunId", pSourcePayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPaycheckList;
    }

    @FlexMethod
    public SAPPItemSet findPItems(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd) throws Throwable {
        SAPPItemSet pitemSet = new SAPPItemSet();
        try {

            ArrayList<SAPPItem> companyPayrollPItems = new ArrayList<SAPPItem>();
            ArrayList<SAPCompanyLaw> companyLawItems = new ArrayList<SAPCompanyLaw>();
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));
            DomainEntitySet<CompanyPayrollItem> companyPayrollItems = CompanyPayrollItem.findAllCompanyPayrollItems(company);
            DomainEntitySet<CompanyLaw> companyLaws = CompanyLaw.findAllCompanyLaws(company);
            CompanyPayrollItem directDepositPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(company);
            for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
                SAPPItem sappItem = PayrollRunTranslator.getSAPPItemFromDomainEntity(companyPayrollItem,directDepositPayrollItem);
                //Finding non-duplicated items and clearing latest Id
                if(sappItem.getPitemNumber().equals(sappItem.getLatestId()) && companyPayrollItems.find(CompanyPayrollItem.AdditionalPayrollItem().SourcePayrollItemId().equalTo(sappItem.getPitemNumber())).getFirst() == null) {
                    sappItem.setLatestId(null);
                }
                companyPayrollPItems.add(sappItem);
            }
            for (CompanyLaw companyLaw : companyLaws) {
                SAPCompanyLaw sapCompanyLaw = PayrollRunTranslator.getSAPCompanyLawFromDomainEntity(companyLaw);
                //Finding non-duplicated Company Laws and clearing latest Id
                if(sapCompanyLaw.getSourceId().equals(sapCompanyLaw.getLatestId()) && companyLaws.find(CompanyLaw.AdditionalCompanyLaw().SourceId().equalTo(sapCompanyLaw.getSourceId())).getFirst() == null) {
                    sapCompanyLaw.setLatestId(null);
                }
                companyLawItems.add(sapCompanyLaw);
            }
            pitemSet.setCompanyPayrollItems(companyPayrollPItems);
            pitemSet.setCompanyLaws(companyLawItems);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company pitems,", pSourceSystemCd, pCompanyId, t);
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return pitemSet;
    }


    @FlexMethod
    public ArrayList<SAPPayrollRun> findPayrollRunsByDate(
            @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
            String sourceSystemCd,
            ArrayList<String> payrollTypes,
            Date fromDate,
            Date toDate) throws Throwable {

        ArrayList<SAPPayrollRun> sapPayrollRunList = new ArrayList<SAPPayrollRun>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Application.findObjects(TransactionType.class);
            Application.findObjects(TransactionState.class);

            Company company = Company.findCompanyNoEagerLoad(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            SpcfCalendar spcfFromDate = fromDate != null ? SAPTranslator.getSpcfCalendarFromDate_BeginDay(fromDate) : null;
            SpcfCalendar spcfToDate = toDate != null ? SAPTranslator.getSpcfCalendarFromDate_EndDay(toDate) : null;

            List<PayrollType> payrollTypeEnums = new ArrayList<PayrollType>();
            if (payrollTypes != null && payrollTypes.size() > 0) {
                for (String currentPayrollTypeString : payrollTypes) {
                    payrollTypeEnums.add(PayrollType.valueOf(currentPayrollTypeString));
                }
            }

            DomainEntitySet<PayrollRun> payrollRuns = findPayrollRuns(company, payrollTypeEnums, spcfFromDate, spcfToDate);
            Map<SpcfUniqueId, PayrollRunAdditionalInfo> payrollRunAdditionalInfo = findPayrollRunAdditionalInfo(company, payrollTypeEnums, spcfFromDate, spcfToDate);

            DomainEntitySet<ActionEvent> allPayrollActionEvents = ActionEvent.getAllPayrollActionEvents();
            for (PayrollRun payrollRun : payrollRuns) {
                Collection<ActionEvent> tempActionEventList = payrollRun.getValidPayrollRunActions(allPayrollActionEvents);

                Collection<ActionEvent> actionEventList = new ArrayList<ActionEvent>();
                for (ActionEvent action : tempActionEventList) {
                    actionEventList.add(action);
                }

                PayrollRun.PayrollDebitInfo payrollDebitInfo = payrollRun.getPayrollDebitInfo();

                if (payrollRun.getPayrollRunType() == PayrollType.Adjustment && payrollRun.getFinancialTransactionCollection().size() == 0) {
                    //skip "adjustment" payrolls that don't have any txns
                    continue;
                }

                sapPayrollRunList.add(
                        PayrollRunTranslator.getSAPPayrollRunFromDomainEntity(
                                payrollRun,
                                payrollDebitInfo.bankAccount,
                                actionEventList,
                                SAPTranslator.getDateFromSpcfCalendar(payrollRun.getExpectedResolutionDate()),
                                payrollRunAdditionalInfo.get(payrollRun.getId()).hasVoidedPaycheck,
                                SAPTranslator.getDoubleFromSpcfMoney(payrollDebitInfo.achAmount),
                                payrollRun.getManualAdjustmentNote(),
                                payrollRunAdditionalInfo.get(payrollRun.getId()).isSuperseded));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding payrolls.  From:" + fromDate + " To:" + toDate, sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPayrollRunList;
    }

    private DomainEntitySet<PayrollRun> findPayrollRuns(Company pCompany, List<PayrollType> pPayrollTypes, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Criterion<PayrollRun> where = PayrollRun.Company().equalTo(pCompany);

        if (pFromDate != null) {
            where = where.And(PayrollRun.PayrollRunDate().greaterOrEqualThan(pFromDate));
        }

        if (pToDate != null) {
            where = where.And(PayrollRun.PayrollRunDate().lessOrEqualThan(pToDate));
        }

        if (pPayrollTypes != null && pPayrollTypes.size() > 0) {
            where = where.And(PayrollRun.PayrollRunType().in(pPayrollTypes));
        }

        Expression<PayrollRun> query =
                new Query<PayrollRun>()
                        .Where(where)
                        .OrderBy(PayrollRun.PayrollRunDate())
                        .EagerLoad(PayrollRun.FinancialTransactionSet());

        return Application.find(PayrollRun.class, query);
    }

    private Map<SpcfUniqueId, PayrollRunAdditionalInfo> findPayrollRunAdditionalInfo(Company pCompany, List<PayrollType> pPayrollTypes, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        HqlBuilder hql = new HqlBuilder(true);
        hql.append("select pr.Id, case when exists (select 'T' from pr.PaycheckSet pc where pc.CompanyAdjustmentSubmission is not null) then 'Void' else 'N' end, " +
                           " case when exists (select 'T' from pr.PaycheckSet pc where pc.SourcePaycheckId like '-%') then 'Superseded' else 'N' end" +
                           " from com.intuit.sbd.payroll.psp.domain.PayrollRun pr");
        hql.append("where pr.Company = :company");
        hql.setParameter("company", pCompany);
        if (pPayrollTypes != null && pPayrollTypes.size() > 0) {
            hql.append("and pr.PayrollRunType in (:types)");
            hql.setParameterList("types", pPayrollTypes.toArray());
        }
        if (pFromDate != null) {
            hql.append("and pr.PayrollRunDate >= :fromDate");
            hql.setParameter("fromDate", pFromDate);
        }
        if (pToDate != null) {
            hql.append("and pr.PayrollRunDate <= :toDate");
            hql.setParameter("toDate", pToDate);
        }
        List<Object[]> rows = hql.list();
        Map<SpcfUniqueId, PayrollRunAdditionalInfo> map = new HashMap<SpcfUniqueId, PayrollRunAdditionalInfo>();
        for (Object[] row : rows) {
            map.put((SpcfUniqueId) row[0], new PayrollRunAdditionalInfo(row[1].equals("Void"), row[2].equals("Superseded")));
        }
        return map;
    }

    public class PayrollRunAdditionalInfo {
        public boolean hasVoidedPaycheck;
        public boolean isSuperseded;

        public PayrollRunAdditionalInfo(boolean pPHasVoidedPaycheck, boolean pSuperseded) {
            hasVoidedPaycheck = pPHasVoidedPaycheck;
            isSuperseded = pSuperseded;
        }
    }

    @FlexMethod
    public double findPayrollRunBalanceDue(String pSourceSystemCd,
                                           @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                           String pSourcePayRunId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));
            PayrollRun payrollRunDE = PayrollRun.findPayrollRun(company, pSourcePayRunId);

            return SAPTranslator.getDoubleFromSpcfMoney(new SpcfMoney(payrollRunDE.getUncollectedAmountForPayroll()));
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding payroll balance.", pSourceSystemCd, pCompanyId, "SourcePayrollRunId", pSourcePayRunId, t);
            return 0;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public SAPPayrollRun findPayrollRun(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourcePayRunId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));
            PayrollRun payrollRunDE = PayrollRun.findPayrollRun(company, pSourcePayRunId);
            if (payrollRunDE == null) {
                aeFactory.throwGenericException("Payroll does not exist (any more) PSID:" + pCompanyId + " SourcePayRunId:" + pSourcePayRunId);
            } else {
                return findPayrollRun(payrollRunDE);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding payroll.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    private SAPPayrollRun findPayrollRun(PayrollRun payrollRunDE) {

        Collection<ActionEvent> actionEventList = payrollRunDE.getValidPayrollRunActions(ActionEvent.getAllPayrollActionEvents());

        PayrollRun.PayrollDebitInfo payrollDebitInfo = payrollRunDE.getPayrollDebitInfo();

        return PayrollRunTranslator.getSAPPayrollRunFromDomainEntity(
                payrollRunDE,
                payrollDebitInfo.bankAccount,
                actionEventList,
                SAPTranslator.getDateFromSpcfCalendar(payrollRunDE.getExpectedResolutionDate()),
                payrollRunDE.hasVoidedPaycheck(),
                SAPTranslator.getDoubleFromSpcfMoney(payrollDebitInfo.achAmount),
                payrollRunDE.getManualAdjustmentNote(),
                payrollRunDE.isSuperseded());
    }

    @FlexMethod
    public SAPPayrollRun findPayrollRunByPayrollRunId(String pPayrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            PayrollRun payrollRunDE = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(pPayrollRunId));
            if (payrollRunDE == null) {
                aeFactory.throwGenericException("Payroll does not exist (any more)  PayrollRunId:" + pPayrollRunId);
            } else {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(payrollRunDE.getCompany());
                return findPayrollRun(payrollRunDE);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding payroll.  PayrollRunId:" + pPayrollRunId, t);
        } finally {
            PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.DDTransactionCancel
    })
    public void cancelPayrollTransaction(
            @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
            String sourceSystemCd,
            ArrayList<String> transactionIds,
            String payrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<String> paycheckIds = new ArrayList<String>();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            ProcessResult processResult = new ProcessResult();
            if (domainCompany == null) {
                processResult.getMessages().CompanyDoesNotExist(EntityName.Company, companyId, sourceSystemCd, companyId);
            } else {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, payrollRunId);
                if (null == payrollRun) {
                    processResult.getMessages().PayrollRunDoesNotExist(
                            EntityName.PayrollRun, payrollRunId, payrollRunId,
                            sourceSystemCd, companyId);
                } else {
                    processResult.merge(payrollRun.convertSplitIdsToPaycheckIds(transactionIds, paycheckIds));
                }
            }

            TransactionCancelEEDTO cancelDTO = PayrollRunTranslator.getTransactionCancelDTOFromParameters(
                    paycheckIds, payrollRunId);

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>(1);
            if (processResult.isSuccess()) {
                prList.add(
                        PayrollServices.payrollManager.cancelEmployeeTransaction(
                                SourceSystemCode.valueOf(sourceSystemCd),
                                companyId,
                                cancelDTO));
            } else {
                prList.add(processResult);
            }

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error cancelling payroll transactions", "PayrollRunId", payrollRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error cancelling payroll transactions", sourceSystemCd, companyId, "PayrollRunId", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateManualLedgerEntry)
    public void cancelAdjustment(@TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String sourceSystemCd, String payrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();


            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, payrollRunId);

            List<String> casIds = new ArrayList<String>();
            CompanyAdjustmentSubmission cas = payrollRun.getLiabilityAdjustmentCollection().get(0).getCompanyAdjustmentSubmission();
            casIds.add(cas.getId().toString());

            ProcessResult processResult = PayrollServices.payrollManager.voidLiabilityAdjustments(
                    SourceSystemCode.valueOf(sourceSystemCd),
                    companyId,
                    casIds,
                    true);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error cancelling adjustment", processResult);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error cancelling payroll transactions", sourceSystemCd, companyId, "PayrollRunId", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.DDTransactionCancel)
    public void cancelBillPaymentTransaction(
            @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
            String sourceSystemCd,
            String payrollRunId,
            ArrayList<String> transactionIds) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<String> billPaymentIds = new ArrayList<String>();
            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            ProcessResult processResult = new ProcessResult();
            if (domainCompany == null) {
                processResult.getMessages().CompanyDoesNotExist(EntityName.Company, companyId, sourceSystemCd, companyId);
            } else {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, payrollRunId);
                if (null == payrollRun) {
                    processResult.getMessages().PayrollRunDoesNotExist(
                            EntityName.PayrollRun, payrollRunId, payrollRunId,
                            sourceSystemCd, companyId);
                } else {
                    processResult.merge(payrollRun.convertSplitIdsToBillPaymentIds(transactionIds, billPaymentIds));
                }
            }

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>(1);
            if (processResult.isSuccess()) {
                prList.add(
                        PayrollServices.billPaymentManager.cancelBillPaymentTransaction(
                                SourceSystemCode.valueOf(sourceSystemCd),
                                companyId,
                                billPaymentIds,null));
            } else {
                prList.add(processResult);
            }

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error cancelling payment transactions", "PayrollRunId", payrollRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error cancelling payment transactions", sourceSystemCd, companyId, "PayrollRunId", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateReversalTransaction)
    public void reversePayrollRunTransactions(
            String sourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
            ArrayList<String> transactionIds,
            String payrollRunId,
            boolean pChargeFee,
            Date pFeeTxnDate,
            String pFeeSettlementType,
            boolean pInitiateForCollection) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            TransactionReverseDTO reverseDTO = PayrollRunTranslator.getTransactionReverseDTOFromParameters(
                    transactionIds,
                    payrollRunId,
                    pChargeFee,
                    pFeeTxnDate,
                    pFeeSettlementType,
                    pInitiateForCollection);

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>(1);
            prList.add(
                    PayrollServices.payrollManager.reverseTransaction(
                            SourceSystemCode.valueOf(sourceSystemCd),
                            companyId,
                            reverseDTO));
            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error reversing payroll transactions", "PayrollRun", payrollRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error reversing payroll transactions", sourceSystemCd, companyId, "PayrollRunId", payrollRunId, ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @FlexMethod
    public ArrayList<SAPPayrollEmployeeTransaction> findEmployeeTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPPayrollEmployeeTransaction> returnTransactionList = new ArrayList<SAPPayrollEmployeeTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate(pToDate);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            if (company.hasService(ServiceCode.RiskAssessment)) {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayRunId);
                DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();

                for (Paycheck paycheck : paycheckList) {
                    for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
                        returnTransactionList.add(
                                PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(paycheckSplit, canViewFullBankAccountNumbers));
                    }
                }
            } else {
                DomainEntitySet<FinancialTransaction> financialTranasctions =
                        FinancialTransaction.findFinancialTransactions(
                                company,
                                pSourcePayRunId,
                                TransactionCategory.Employee,
                                spcfFromDate,
                                spcfToDate,
                                FinancialTransaction.PaycheckSplit().getPropertyName());

                for (FinancialTransaction financialTransaction : financialTranasctions) {
                    Collection<ActionEvent> actionEvents = getActionEventCollection(financialTransaction);

                    String transactionReturns = "";
                    if (financialTransaction.getMoneyMovementTransaction() != null) {
                        DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(financialTransaction);
                        for (TransactionReturn txnReturn : returnList) {
                            transactionReturns += txnReturn.getBankReturnCd() + "\n";
                        }
                    }

                    Paycheck paycheck = null;
                    if (financialTransaction.getPaycheckSplit() != null) {
                        paycheck = financialTransaction.getPaycheckSplit().getPaycheck();
                    }
                    
                    SAPPayrollEmployeeTransaction sapPayrollEmployeeTransaction = PayrollRunTranslator
                    		.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(
                         financialTransaction, actionEvents, transactionReturns, paycheck, canViewFullBankAccountNumbers);
                    
                    String jpmcTraceId = getTraceId(financialTransaction);

                    //TODO: Remove below logger. This statement is frequently executed.
                    logger.debug(String.format("Employee Financial Transaction Id=%s, Target Trace Id=%s", 
                    		financialTransaction.getId().toString(), jpmcTraceId));
                    sapPayrollEmployeeTransaction.setJpmcTraceId(jpmcTraceId);
                    
                    returnTransactionList.add(sapPayrollEmployeeTransaction);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employee transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

	@FlexMethod
    public ArrayList<SAPPayrollEmployeeTransaction> findVendorTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPPayrollEmployeeTransaction> returnTransactionList = new ArrayList<SAPPayrollEmployeeTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate(pToDate);

            boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            if (company.hasService(ServiceCode.RiskAssessment)) {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayRunId);
                DomainEntitySet<BillPayment> billPaymentList = payrollRun.getBillPaymentCollection();

                for (BillPayment billPayment : billPaymentList) {
                    for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                        returnTransactionList.add(
                                PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(billPaymentSplit, canViewFullBankAccountNumbers));
                    }
                }
            } else {
                DomainEntitySet<FinancialTransaction> financialTranasctions =
                        FinancialTransaction.findFinancialTransactions(
                                company,
                                pSourcePayRunId,
                                TransactionCategory.Employee,
                                spcfFromDate,
                                spcfToDate,
                                FinancialTransaction.BillPaymentSplit().getPropertyName());

                for (FinancialTransaction financialTransaction : financialTranasctions) {
                    Collection<ActionEvent> actionEvents = getActionEventCollection(financialTransaction);

                    String transactionReturns = "";
                    if (financialTransaction.getMoneyMovementTransaction() != null) {
                        DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(financialTransaction);
                        for (TransactionReturn txnReturn : returnList) {
                            transactionReturns += txnReturn.getBankReturnCd() + "\n";
                        }
                    }
                    String jpmcTraceId = getTraceId(financialTransaction);
                    SAPPayrollEmployeeTransaction vendorTransaction = 
                    		PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(
                            financialTransaction, actionEvents, transactionReturns, null, canViewFullBankAccountNumbers);
                    //TODO: Remove below logger. This statement is frequently executed.
                    logger.debug(String.format("Vendor Financial Transaction Id=%s, Target Trace Id=%s", 
                    		financialTransaction.getId().toString(), jpmcTraceId));
                    vendorTransaction.setJpmcTraceId(jpmcTraceId);
                  
                  // adding EE DD Debit Txn Number, adding here to reduce the impact of the changes
                    String employerDDDebitTxnNumber = null;
                    if(financialTransaction != null && financialTransaction.getRelatableTransaction()!= null
                            && financialTransaction.getRelatableTransaction().getMoneyMovementTransaction()!= null
                            && financialTransaction.getRelatableTransaction()
                            .getMoneyMovementTransaction().getTransactionNumber() != null){

                        employerDDDebitTxnNumber = financialTransaction.getRelatableTransaction()
                                .getMoneyMovementTransaction().getTransactionNumber();
                    }
                    vendorTransaction.setEmployerDDDebitTxnNumber(employerDDDebitTxnNumber);
                    
                    returnTransactionList.add(vendorTransaction);
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding vendor transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.DDTransactionCancel)
    public ArrayList<SAPPayrollEmployeeTransaction> findCancelableTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId) throws Throwable {

        ArrayList<SAPPayrollEmployeeTransaction> returnTransactionList = new ArrayList<SAPPayrollEmployeeTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<FinancialTransaction> financialTranasctions =
                    FinancialTransaction.findFinancialTransactions(
                            company,
                            pSourcePayRunId,
                            TransactionCategory.Employee,
                            null,
                            null,
                            null);

            boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

            for (FinancialTransaction financialTransaction : financialTranasctions) {
                returnTransactionList.add(
                        PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(
                                financialTransaction, null, null, null, canViewFullBankAccountNumbers));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding cancelable transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateReversalTransaction)
    public ArrayList<SAPPayrollEmployeeTransaction> findReversableEmployeeTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPPayrollEmployeeTransaction> returnTransactionList = new ArrayList<SAPPayrollEmployeeTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate(pToDate);

            boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<FinancialTransaction> financialTransactions =
                    FinancialTransaction.findFinancialTransactions(
                            company,
                            pSourcePayRunId,
                            TransactionCategory.Employee,
                            spcfFromDate,
                            spcfToDate, null);

            boolean isReversible;
            for (FinancialTransaction financialTransaction : financialTransactions) {
                // check for pending reversal
                isReversible = true;
                DomainEntitySet<FinancialTransaction> txnAssocList = financialTransaction.getAssociatedTransactionsCollection();

                if (txnAssocList != null) {
                    for (FinancialTransaction assocTxn : txnAssocList) {
                        if (TransactionAssociationType.Reversal.equals(
                                assocTxn.getTransactionType().getAssociationType())) {
                            TransactionStateCode txnState =
                                    assocTxn.getCurrentTransactionState().getTransactionStateCd();
                            if (!TransactionStateCode.Cancelled.equals(txnState) &&
                                    !TransactionStateCode.Voided.equals(txnState)) {
                                isReversible = false;
                            }
                        }
                    }
                }

                // check type and state
                if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployeeDdCredit &&
                        (financialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed ||
                                financialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Completed) &&
                        isReversible) {
                    returnTransactionList.add(
                            PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(
                                    financialTransaction, null, null, null, canViewFullBankAccountNumbers));
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding reversable transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

    @FlexMethod
    public ArrayList<SAPPayrollTransaction> findIntuitTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPPayrollTransaction> returnTransactionList = new ArrayList<SAPPayrollTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate(pToDate);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<FinancialTransaction> financialTranasctions =
                    FinancialTransaction.findFinancialTransactions(
                            company,
                            pSourcePayRunId,
                            TransactionCategory.Intuit,
                            spcfFromDate,
                            spcfToDate, null);

            for (FinancialTransaction financialTransaction : financialTranasctions) {
                Collection<ActionEvent> actionEvents = getActionEventCollection(financialTransaction);

                String transactionReturns = "";
                if (financialTransaction.getMoneyMovementTransaction() != null) {
                    DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(financialTransaction);
                    for (TransactionReturn txnReturn : returnList) {
                        transactionReturns += txnReturn.getBankReturnCd() + "\n";
                    }
                }

                returnTransactionList.add(
                        PayrollRunTranslator.getSAPPayrollTransactionFromDomainEntity(
                                financialTransaction, actionEvents, transactionReturns));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding intuit transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

    @FlexMethod
    public ArrayList<SAPPayrollTransaction> findEmployerTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPPayrollTransaction> returnTransactionList = new ArrayList<SAPPayrollTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate(pToDate);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<FinancialTransaction> financialTranasctions =
                    FinancialTransaction.findFinancialTransactions(
                            company,
                            pSourcePayRunId,
                            TransactionCategory.Employer,
                            spcfFromDate,
                            spcfToDate, null);

            for (FinancialTransaction financialTransaction : financialTranasctions) {
                Collection<ActionEvent> actionEvents = getActionEventCollection(financialTransaction);

                String transactionReturns = "";
                if (financialTransaction.getMoneyMovementTransaction() != null) {
                    DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(financialTransaction);
                    for (int i = 0; i < returnList.size(); i++) {
                        TransactionReturn txnReturn = returnList.get(i);
                        transactionReturns += txnReturn.getBankReturnCd();
                        if (i != (returnList.size() - 1)) {
                            transactionReturns += "\n";
                        }
                    }
                }

                returnTransactionList.add(
                        PayrollRunTranslator.getSAPPayrollTransactionFromDomainEntity(
                                financialTransaction, actionEvents, transactionReturns));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employer transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

    @FlexMethod
    public ArrayList<SAPAgencyTransaction> findAgencyTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPAgencyTransaction> returnTransactionList = new ArrayList<SAPAgencyTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate(pToDate);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<FinancialTransaction> financialTransactions =
                    FinancialTransaction.findFinancialTransactions(
                            company,
                            pSourcePayRunId,
                            TransactionCategory.Agency,
                            spcfFromDate,
                            spcfToDate, null);

            RulesInfo rulesInfo = new RulesInfo();
            for (FinancialTransaction financialTransaction : financialTransactions) {
                Collection<ActionEvent> actionEvents = getActionEventCollection(financialTransaction);

                String transactionReturns = "";
                if (financialTransaction.getMoneyMovementTransaction() != null) {
                    DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(financialTransaction);
                    for (TransactionReturn txnReturn : returnList) {
                        transactionReturns += txnReturn.getBankReturnCd() + "\n";
                    }
                }

                returnTransactionList.add(
                        PayrollRunTranslator.getSAPAgencyTransactionFromDomainEntity(
                                financialTransaction, actionEvents, transactionReturns, rulesInfo));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding agency transactions.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }

    /**
     * Returns the ActionEvents available for the financial transaction; augments PSP Core logic by
     * adding in UI specific events that may be available.  These UI specific ActionEvents will not be found in
     * the PSP_FINANCIAL_TXN_ACTION table.
     * <p/>
     * i.e. Viewing the Transaction State History by adding ActionEventCode.TxStateHistory to the returned collection
     *
     * @param financialTransaction - transaction to get actions for
     * @return available ActionEvent(s) for the financial transaction, including UI specific ActionEvents
     */
    private Collection<ActionEvent> getActionEventCollection(FinancialTransaction financialTransaction) {
        Collection<ActionEvent> actionEvents = financialTransaction.getActionCollection();

        // add UI specific action events
        // -- TxStateHistory
        ActionEvent historyEvent = Application.findById(ActionEvent.class, ActionEventCode.TxStateHistory);
        actionEvents.add(historyEvent);

        return actionEvents;

    }

    @FlexMethod
    public ArrayList<SAPPayrollTransaction> findERPayableRefundTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd) throws Throwable {

        ArrayList<SAPPayrollTransaction> returnTransactionList = new ArrayList<SAPPayrollTransaction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class,
                                                                                           FinancialTransaction.Company().equalTo(company)
                                                                                                               .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCredit)));

            for (FinancialTransaction financialTransaction : financialTransactions) {
                Collection<ActionEvent> actionEvents = getActionEventCollection(financialTransaction);

                String transactionReturns = "";
                if (financialTransaction.getMoneyMovementTransaction() != null) {
                    DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(financialTransaction);
                    for (int i = 0; i < returnList.size(); i++) {
                        TransactionReturn txnReturn = returnList.get(i);
                        transactionReturns += txnReturn.getBankReturnCd();
                        if (i != (returnList.size() - 1)) {
                            transactionReturns += "\n";
                        }
                    }
                }

                returnTransactionList.add(
                        PayrollRunTranslator.getSAPPayrollTransactionFromDomainEntity(
                                financialTransaction, actionEvents, transactionReturns));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding employer transactions.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }


    @FlexMethod
    @Operation(operationIds = {
            OperationId.LedgerView,
            OperationId.ViewPayrollScreen,
            OperationId.CreateFLA
    })
    public ArrayList<SAPCompanyLedgerAccount> findLedgerAccountsByPayroll(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId) throws Throwable {

        ArrayList<SAPCompanyLedgerAccount> returnLedgerList = new ArrayList<SAPCompanyLedgerAccount>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            for (LedgerAccountCode ledgerAccountCode : LedgerAccountCode.values()) {
                SpcfMoney balance = LedgerAccount.getLedgerAccountBalanceByPayroll(
                        ledgerAccountCode, pSourcePayRunId, company);

                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayRunId);
                LedgerAccount ledgerAccount =
                        PayrollServices.entityFinder.findById(LedgerAccount.class, ledgerAccountCode);
                Collection<ActionEvent> actionEventList =
                        payrollRun.getValidActions(ledgerAccount);

                boolean isCredit = balance.getSign() >= 0; //todo this should be wrong but the get balance by payroll is wrong so two wrongs make a right

                returnLedgerList.add(
                        PayrollRunTranslator.getSAPCompanyLedgerAccountFromParameters(
                                ledgerAccount,
                                balance, isCredit, actionEventList));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ledger accounts.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnLedgerList;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.CreateFLA,
            OperationId.LedgerView,
            OperationId.ViewPayrollScreen
    })
    public ArrayList<SAPCompanyLedgerAccount> getLedgerAccountBalanceForLaw(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            String pLawId) throws Throwable {

        ArrayList<SAPCompanyLedgerAccount> returnLedgerList = new ArrayList<SAPCompanyLedgerAccount>();
        Law law = null;

        try {
            PayrollServices.beginUnitOfWork();
            Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);
            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, pLawId);
            if (companyLaw != null) {
                law = CompanyLaw.findCompanyLaw(company, pLawId).getLaw();
            }
            for (LedgerAccountCode ledgerAccountCode : LedgerAccountCode.values()) {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayRunId);
                Map<Law, SpcfMoney> lawBalanceMap = new HashMap<Law, SpcfMoney>();
                SpcfMoney balance = new SpcfMoney(SpcfMoney.ZERO);
                LedgerAccount ledgerAccount =
                        PayrollServices.entityFinder.findById(LedgerAccount.class, ledgerAccountCode);
                if (ledgerAccount.getRequiresQuarterLaw()) {
                    lawBalanceMap = LedgerAccount.getLedgerAccountBalanceByPaymentTemplateAndQuarter(ledgerAccountCode, law.getPaymentTemplate(), company, payrollRun.getPaycheckDate());
                    balance = lawBalanceMap.get(law) == null ? new SpcfMoney(SpcfMoney.ZERO) : lawBalanceMap.get(law);
                }
                boolean isCredit = balance != null ? balance.getSign() >= 0 : false;
                returnLedgerList.add(
                        PayrollRunTranslator.getSAPCompanyLedgerAccountFromParameters(
                                ledgerAccount,
                                balance, isCredit, new ArrayList<ActionEvent>()));
            }
            PayrollServices.rollbackUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ledger accounts.", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnLedgerList;
    }


    @FlexMethod
    @Operation(operationIds = {OperationId.CreateFLA})
    public void addFinancialLedgerAdjustmentTransaction(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            String debitAccountCode,
            String creditAccountCode,
            double pAmount,
            String pLawId,
            String pNoteText) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            SpcfMoney flaAmount = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(pAmount);
            ProcessResult processResult = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.valueOf(pSourceSystemCd),
                                                                                                                              pCompanyId, LedgerAccountCode.valueOf(debitAccountCode), LedgerAccountCode.valueOf(creditAccountCode),
                                                                                                                              flaAmount, pSourcePayRunId, pLawId, pNoteText);
            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error Adding Financial Ledger transaction ", processResult);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error Adding Financial Ledger transaction ", pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @FlexMethod
    @Operation(operationIds = {
            OperationId.ViewPayrollScreen,
            OperationId.CreateFeeTransaction
    })
    public SAPCompanyLedgerAccount findLedgerAccountByPayrollAndLedgerCode(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            String pLedgerAccountCode) throws Throwable {

        SAPCompanyLedgerAccount returnLedger = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            LedgerAccountCode ledgerAccountCode = LedgerAccountCode.valueOf(pLedgerAccountCode);

            SpcfMoney balance = LedgerAccount.getLedgerAccountBalanceByPayroll(
                    ledgerAccountCode, pSourcePayRunId, company);

            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayRunId);
            LedgerAccount ledgerAccount =
                    PayrollServices.entityFinder.findById(LedgerAccount.class, ledgerAccountCode);
            Collection<ActionEvent> actionEventList =
                    payrollRun.getValidActions(ledgerAccount);

            boolean isCredit = balance.getSign() >= 0; //todo this should be wrong but the get balance by payroll is wrong so two wrongs make a right

            returnLedger =
                    PayrollRunTranslator.getSAPCompanyLedgerAccountFromParameters(
                            ledgerAccount,
                            balance, isCredit, actionEventList);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ledger accounts.  LedgerAccountCode:" + pLedgerAccountCode, pSourceSystemCd, pCompanyId, "SourcePayRunId", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnLedger;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.CreateFLA,
            OperationId.LedgerView,
            OperationId.ViewPayrollScreen
    })
    public List<SAPLawItem> getPayrollLaws(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId) throws Throwable {

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

        Company company = Company.findCompany(
                pCompanyId,
                SourceSystemCode.valueOf(pSourceSystemCd));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayRunId);
        List<SAPLawItem> sapLawsItems = new ArrayList<SAPLawItem>();
        DomainEntitySet<Law> laws = new DomainEntitySet<Law>();
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            Law tempLaw = financialTransaction.getLaw();
            if (tempLaw != null && !laws.contains(tempLaw)) {
                laws.add(tempLaw);
                sapLawsItems.add(TaxTranslator.getLawItemsFromDomainEntity(financialTransaction.getLaw()));
            }
        }

        PayrollServices.rollbackUnitOfWork();
        return sapLawsItems;
    }

    @Operation(operationIds = {
            OperationId.CreateFLA,
            OperationId.ViewTaxLedger,
            OperationId.ViewAgencyInfo,
            OperationId.CreateManualLedgerEntry,
            OperationId.LedgerView,
            OperationId.CreateRefundTransaction,
            OperationId.ViewPayrollScreen
    })
    public ArrayList<SAPCompanyLedgerAccount> findLedgerAccounts(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd) throws Throwable {

        ArrayList<SAPCompanyLedgerAccount> returnLedgerList = new ArrayList<SAPCompanyLedgerAccount>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            for (LedgerAccountCode ledgerAccountCode : LedgerAccountCode.values()) {
                SpcfMoney balance = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, ledgerAccountCode));

                LedgerAccount ledgerAccount =
                        PayrollServices.entityFinder.findById(LedgerAccount.class, ledgerAccountCode);
                boolean isCredit = CreditDebitCode.Credit == ledgerAccount.getLedgerBalanceAmountTypeIndicator(
                        balance);

                returnLedgerList.add(
                        PayrollRunTranslator.getSAPCompanyLedgerAccountFromParameters(
                                ledgerAccount,
                                balance, isCredit, new ArrayList<ActionEvent>()));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ledger accounts.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnLedgerList;
    }

    @FlexMethod
    public double getLedgerAccountBalance(String sourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String sourceCompanyId, String ledgerAccountCd) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    sourceCompanyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            return SAPTranslator.getDoubleFromSpcfMoney(new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.valueOf(ledgerAccountCd))));

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding ledger balance.", sourceSystemCd, sourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return 0;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.CreateFeeTransaction, OperationId.AddManualFeeTransactions})
    public void addFeeTransactions(
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            String pSourceSystemCd,
            String pSourcePayRunId,
            String pSettlementTypeCd,
            Date pTxnDate,
            ArrayList<SAPOfferingServiceChargePrice> fees) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            //ui should also do this check
            boolean employerFeeLimitEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_EMPLOYER_FEE_LIMIT_ENABLED, true);
            SettlementTypeDTO settlementType = SettlementTypeDTO.valueOf(pSettlementTypeCd);

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            ArrayList<ERFeeAddDTO> feeList = new ArrayList<ERFeeAddDTO>();
            int defaultEmployerFeeLimit = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_EMPLOYER_FEE_LIMIT, 500);
            for (SAPOfferingServiceChargePrice fee : fees) {
                if (fee.getChecked() && fee.getChargedPrice() > 0) {
                    if (employerFeeLimitEnabled && fee.getChargedPrice()>defaultEmployerFeeLimit) {
                        aeFactory.throwGenericException("The fee amount you have entered is higher than the permissible limit. " +
                                "Enter an amount less than $"+defaultEmployerFeeLimit+" to create the entry.");
                    }
                    ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(
                            SourceSystemCode.valueOf(pSourceSystemCd),
                            pCompanyId,
                            pSourcePayRunId,
                            settlementType,
                            pTxnDate,
                            SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(fee.getChargedPrice()),
                            OfferingServiceChargeType.valueOf(fee.getServiceChargeTypeCode()),
                            fee.getMemo());
                    feeList.add(feeAddDTO);
                }
            }

            ERFeeAddDTO[] erFeeAddDTOs = feeList.toArray(new ERFeeAddDTO[0]);

            ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = null;
            if (StringUtils.isNotEmpty(pSourcePayRunId)) {
                prList.add(PayrollServices.financialTransactionManager.addFeeTransaction(erFeeAddDTOs));
            } else {
                prList.add(PayrollServices.financialTransactionManager.createManualFeeTransaction(erFeeAddDTOs));
            }

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding fee to payroll.", "PayrollRun", pSourcePayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding fee.", pSourceSystemCd, pCompanyId, "PayrollRun ", pSourcePayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    //No operation found
    @FlexMethod
    public void addFeeRedebitTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pOldTxnId) throws Throwable {
        PayrollServices.beginUnitOfWork();
        try {
            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            FinancialTransaction originalTxn =
                    PayrollServices.entityFinder.findById(
                            FinancialTransaction.class, SpcfUniqueId.createInstance(pOldTxnId));
            RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
            if (null != originalTxn) {
                redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
                redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
                redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
            }
            ArrayList<RedebitImpoundDTO> redebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
            redebitImpoundDTOs.add(redebitDTO);
            prList.add(
                    PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                            SourceSystemCode.valueOf(pSourceSystemCd),
                            pSourceCompanyId, redebitImpoundDTOs));


            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding fee redebit.", "Transaction", pOldTxnId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding fee redebit.", pSourceSystemCd, pSourceCompanyId, "Transaction ", pOldTxnId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.EnterWireExpectedDate})
    public void addWireExpectedDateTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pFinancialTxId,
            String pCollectionStageCd,
            String pActionEvent,
            Date pTxnDate,
            Boolean pSendLastEmail) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ModifyWireExpectedDTO wireExpectedDTO = PayrollRunTranslator.getWireExpectedDTOFromParameters(
                    pFinancialTxId,
                    pCollectionStageCd,
                    pActionEvent,
                    pTxnDate,
                    pSendLastEmail);

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            prList.add(
                    PayrollServices.payrollManager.modifyWireExpectedDate(
                            SourceSystemCode.valueOf(pSourceSystemCd),
                            pSourceCompanyId, wireExpectedDTO));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error modify wire expected data.", "Transaction", pFinancialTxId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error in modify wire expected data.", pSourceSystemCd, pSourceCompanyId, "Transaction ", pFinancialTxId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.CreateRefundTransaction})
    public void refundEmployerTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pFinancialTxId,
            double pFinancialTxAmt,
            Date pTxnDate,
            String pSettlementType) throws Throwable {
        PayrollServices.beginUnitOfWork();
        try {
            ERRefundDTO erRefundDTO = PayrollRunTranslator.getERRefundDTOFromParameters(
                    pFinancialTxId,
                    pFinancialTxAmt,
                    pTxnDate,
                    pTxnDate,
                    pSettlementType);

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            prList.add(
                    PayrollServices.financialTransactionManager.
                            refundEmployerTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, erRefundDTO));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error refunding ER TXN.", "Transaction", pFinancialTxId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error refunding ER TXN.", pSourceSystemCd, pSourceCompanyId, "Transaction", pFinancialTxId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.VoidTransaction, OperationId.CreateERPenaltiesAndInterestRefunds, OperationId.VoidTORTransaction})
    public void voidTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pFinTxId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            prList.add(
                    PayrollServices.financialTransactionManager.
                            voidTransaction(SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pFinTxId));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error voiding transaction", "Transaction", pFinTxId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error voiding transaction", pSourceSystemCd, pSourceCompanyId, "Transaction", pFinTxId, ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.TransactionCancel, OperationId.CreateERPenaltiesAndInterestRefunds, OperationId.EmployerFeeDebitCancel, OperationId.RefundERPayable})
    public void cancelTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pFinTxId) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
            prList.add(
                    PayrollServices.financialTransactionManager.
                            cancelTransaction(SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pFinTxId));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error cancelling transaction.", "Transaction", pFinTxId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error cancelling transaction.", pSourceSystemCd, pSourceCompanyId, "Transaction", pFinTxId, ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.LedgerView)
    public ArrayList<SAPPayrollTransaction> findTransactionsByLedgerAccount(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pLedgerAccountCd) throws Throwable {

        return findTransactionsByLedgerAccountAndPayroll(pSourceSystemCd, pSourceCompanyId, pLedgerAccountCd, null);
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewPayrollScreen)
    public ArrayList<SAPPayrollTransaction> findTransactionsByLedgerAccountAndPayroll(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pLedgerAccountCd,
            String pPayRunId) throws Throwable {

        ArrayList<SAPPayrollTransaction> sapTxnReturnList = new ArrayList<SAPPayrollTransaction>();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pSourceCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            List<Object[]> txnList =
                    FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(
                            company,
                            pPayRunId,
                            LedgerAccountCode.valueOf(pLedgerAccountCd));

            for (Object[] finTxnComboObject : txnList) {
                FinancialTransaction financialTransaction = (FinancialTransaction) finTxnComboObject[0];
                Boolean isCredit = finTxnComboObject[1] != null && "C".equals(finTxnComboObject[1]);
                FinancialTransactionState transactionState = (FinancialTransactionState) finTxnComboObject[2];

                sapTxnReturnList.add(
                        PayrollRunTranslator.getSAPLedgerTransactionFromDomainEntity(
                                financialTransaction,
                                isCredit,
                                transactionState));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transactions.", pSourceSystemCd, pSourceCompanyId, "PayRunId", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapTxnReturnList;
    }

    private PayrollRun getCompanyPayroll(Company company, String pPayrollRunId) throws Exception {
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pPayrollRunId);

        if (payrollRun == null) {
            String companyLookupKey = company.getSourceSystemCd().toString() + ":" + company.getSourceCompanyId();
            String payrollLookupKey = companyLookupKey + "  payroll: " + pPayrollRunId;
            aeFactory.throwGenericException("Could not find company payroll - " + payrollLookupKey);
        }

        return payrollRun;
    }

    private Company getCompany(String pCompanyId, String pSourceSystemCd) throws Exception {
        Company company = Company.findCompany(
                pCompanyId,
                SourceSystemCode.valueOf(pSourceSystemCd));
        if (company == null) {
            String companyLookupKey = pSourceSystemCd + ":" + pCompanyId;
            aeFactory.throwGenericException("Could not find company - " + companyLookupKey);
        }

        return company;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.RefundEmployerFraudEscalation,
            OperationId.IssueRedebitTransaction,
            OperationId.RecordNonACHRedebitTransaction,
            OperationId.RecoverBadDebtTransaction,
            OperationId.RecordPrefundingWire
    })
    public void redebitPayrollTransactions(String sourceSystemCd,
                                           @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                           String settlementTypeCd,
                                           Date settlementDate,
                                           ArrayList<SAPPayrollBillingTransactions> payrolls) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            for (SAPPayrollBillingTransactions payroll : payrolls) {
                ArrayList<RedebitImpoundDTO> redebits = new ArrayList<RedebitImpoundDTO>();
                // add the dd transaction
                for (SAPBillingTransaction uncollectedTransaction : payroll.getDdTransactions()) {
                    redebits.addAll(
                            PayrollRunTranslator.getRedebitImpoundDTOList(
                                    uncollectedTransaction,
                                    settlementTypeCd,
                                    settlementDate,
                                    payroll.getInitiationDate()));

                }
                // add the tax transaction
                redebits.addAll(
                        PayrollRunTranslator.getRedebitImpoundDTOList(
                                payroll.getTaxTransaction(),
                                settlementTypeCd,
                                settlementDate,
                                payroll.getInitiationDate()));

                // add the fee transactions
                for (SAPBillingTransaction uncollectedTransaction : payroll.getFeeTransactions()) {
                    redebits.addAll(PayrollRunTranslator.getRedebitImpoundDTOList(uncollectedTransaction,
                                                                                  settlementTypeCd,
                                                                                  settlementDate,
                                                                                  payroll.getInitiationDate()));
                }

                Boolean erFeeRedebitRequired = Boolean.TRUE;
                //add the handling fee
                if (payroll.getHandlingFeeTransaction() != null && payroll.getHandlingFeeTransaction().getFinancialReturnAmount() > 0) {

                    //If there was no EmployerFeeDebit already present we would have added an emptyGUID for that transaction to enable the Agent to add the handlingFee Amount
                    //need to create that feeTransaction here before we process the redebits
                    if(payroll.getHandlingFeeTransaction().getFinancialTxnId().equals(SpcfUniqueId.EmptyGuid)) {

                        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
                        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payroll.getPayrollRunId());
                        String handlingFeeAmount = Double.toString(payroll.getHandlingFeeTransaction().getFinancialReturnAmount());

                        ERFeeAddDTO erFeeAddDTO = new ERFeeAddDTO();
                        erFeeAddDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);
                        erFeeAddDTO.setSettlementTypeCode(SettlementTypeDTO.valueOf(settlementTypeCd));
                        erFeeAddDTO.setSourceCompanyId(company.getSourceCompanyId());
                        erFeeAddDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
                        erFeeAddDTO.setSourceSystemCd(company.getSourceSystemCd());
                        Date today = CalendarUtils.convertToDate(PSPDate.getPSPTime());
                        erFeeAddDTO.setTxDate(today);
                        erFeeAddDTO.setAmount(new SpcfMoney(handlingFeeAmount));

                        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(erFeeAddDTO);
                        if (!processResult.isSuccess()) {
                            logger.error("Error creating Handling Fee Transaction : Unhandled ProcessResult failure from FinancialTransactionManager.addFeeTransaction(): " + processResult.toString());
                            return;
                        }
                        FinancialTransaction financialTransaction = processResult.getResult().getFirst();
                        payroll.getHandlingFeeTransaction().setFinancialTxnId(financialTransaction.getId().toString());
                        if(financialTransaction.getMoneyMovementTransaction() == null) {
                            MoneyMovementTransaction.createMoneyMovementTransaction(financialTransaction);
                        }
                        erFeeRedebitRequired = Boolean.FALSE;

                    } else {
                    redebits.addAll(PayrollRunTranslator.getRedebitImpoundDTOList(
                            payroll.getHandlingFeeTransaction(),
                            settlementTypeCd,
                            settlementDate,
                            payroll.getInitiationDate()));
                    }
                }

                ArrayList<ProcessResult> prList;

                SettlementType settlementType = SettlementType.valueOf(settlementTypeCd);
                if (settlementType == SettlementType.ACH) {
                    prList = performACHRedebits(sourceSystemCd, companyId, redebits);
                } else {
                    prList = performNonACHRedebits(sourceSystemCd, companyId, redebits);
                }


                if (payroll.getHandlingFeeTransaction() != null && payroll.getHandlingFeeTransaction().getFinancialReturnAmount() > 0 && erFeeRedebitRequired) {
                    //company has included the fee in the wire, but PSP hasn't yet charged for it.  This allows the agent to do it in one screen.
                    //However, the fee was on the wire which means it is sent to the returns account, so we must transfer that to the fee account.
                    //In order to use the fee transfer, we will create is as a redebit, though it's not really.

                    //first flush the other txns for the ledger
                    Application.getHibernateSession().flush();
                    FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
                    feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.DebitReturnFee);
                    feeTransferDTO.setFinancialTxAmt(SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(payroll.getHandlingFeeTransaction().getFinancialReturnAmount()));
                    feeTransferDTO.setSourcePayrollRunId(payroll.getPayrollRunId());
                    prList.add(PayrollServices.financialTransactionManager.addFeeTransferTransaction(SourceSystemCode.valueOf(sourceSystemCd), companyId, feeTransferDTO));
                }

                if (aeFactory.errorsOccurred(prList)) {
                    aeFactory.throwGenericException("Error adding redbit impound transaction", "Company", companyId, prList);
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding redbit impound transaction", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RefundEmployerFraudEscalation)
    public void addRefundPayrollTransactions(String sourceSystemCd,
                                             @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                             String settlementTypeCd,
                                             Date settlementDate,
                                             ArrayList<SAPPayrollBillingTransactions> payrolls) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ERRefundDTO> refunds = new ArrayList<ERRefundDTO>();

            for (SAPPayrollBillingTransactions payroll : payrolls) {
                // add the dd transaction
                for (SAPBillingTransaction refundTransaction : payroll.getDdTransactions()) {
                    refunds.addAll(
                            PayrollRunTranslator.getERRefundDTOList(
                                    refundTransaction,
                                    settlementTypeCd,
                                    settlementDate,
                                    payroll.getInitiationDate()));
                }
                // add the tax transaction
                refunds.addAll(
                        PayrollRunTranslator.getERRefundDTOList(
                                payroll.getTaxTransaction(),
                                settlementTypeCd,
                                settlementDate,
                                payroll.getInitiationDate()));

                // add the fee transactions
                for (SAPBillingTransaction refundTransaction : payroll.getFeeTransactions()) {
                    refunds.addAll(
                            PayrollRunTranslator.getERRefundDTOList(
                                    refundTransaction,
                                    settlementTypeCd,
                                    settlementDate,
                                    payroll.getInitiationDate()));
                }
            }

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(
                    PayrollServices.financialTransactionManager.addEmployerFraudOrEscalationRefund(
                            SourceSystemCode.valueOf(sourceSystemCd),
                            companyId,
                            refunds));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding refund transaction.", "Company", companyId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding refund transaction.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private ArrayList<ProcessResult> performACHRedebits(String sourceSystemCd, String companyId, List<RedebitImpoundDTO> redebits) {
        ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

        prList.add(
                PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(
                        SourceSystemCode.valueOf(sourceSystemCd),
                        companyId,
                        redebits));

        return prList;
    }

    private ArrayList<ProcessResult> performNonACHRedebits(String sourceSystemCd, String companyId, List<RedebitImpoundDTO> redebits) {
        ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

        prList.add(
                PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                        SourceSystemCode.valueOf(sourceSystemCd),
                        companyId,
                        redebits));

        return prList;
    }


    @FlexMethod
    @Operation(operationIds = {OperationId.WriteoffBadDebtTransaction})
    public void addWriteOffBadDebtTransaction(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(
                    PayrollServices.financialTransactionManager.
                            addWriteOffBadDebtTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pPayRunId));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding bad debt write-off transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding bad debt write-off transaction.", pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.WriteoffEmployeeBadDebtTransaction})
    public void addWriteOffEmployeeBadDebtTransaction(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayRunId)
            throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(
                    PayrollServices.financialTransactionManager.
                            addEmployeeWriteOffBadDebtTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pPayRunId));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding bad debt reversal write-off transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding bad debt reversal write-off transaction.",
                                            pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.BookTransferTransaction)
    public void addIntuit5DayReturnTransfer(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayRunId)
            throws Throwable {
        PayrollServices.beginUnitOfWork();
        try {
            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(
                    PayrollServices.financialTransactionManager.addIntuit5DayReturnTransferTransaction
                            (SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pPayRunId));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding intuit 5 day return transfer transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding intuit 5 day return transfer transaction.",
                                            pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.WriteoffBadDebtTransaction})
    public void voidPayrollTaxPayment(@TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String sourceSystemCd, String payrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            com.intuit.sbd.payroll.psp.domain.Company domainCompany = Company.findCompany(
                    companyId,
                    SourceSystemCode.valueOf(sourceSystemCd));

            PayrollRun payrollRun = PayrollRun.findPayrollRun(domainCompany, payrollRunId);


            ProcessResult processResult = PayrollServices.payrollManager.voidPayrollTaxPayment(
                    SourceSystemCode.valueOf(sourceSystemCd),
                    companyId,
                    payrollRun.getId().toString());

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error voiding tax payment", processResult);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error voiding tax payment", sourceSystemCd, companyId, "PayrollRunId", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.WriteoffBadDebtTransaction)
    public void reissuePayrollTaxPayment(@TenantId(IdType = CompanyIdentifierType.PSID) String companyId, String sourceSystemCd, String sourcePayrollRunId, String transferTransactionId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult processResult = PayrollServices.payrollManager.reissuePayrollTaxPayment(
                    SourceSystemCode.valueOf(sourceSystemCd),
                    companyId,
                    sourcePayrollRunId,
                    transferTransactionId);

            if (!processResult.isSuccess()) {
                aeFactory.throwGenericException("Error reissuing tax payment", processResult);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error reissuing tax payment TransferTransactionId:" + transferTransactionId,
                                            sourceSystemCd, companyId, "SourcePayrollRunId", sourcePayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RecoverBadDebtTransaction)
    public void addRecoverBadDebtTransactions(String sourceSystemCd,
                                              @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                              String settlementTypeCd,
                                              Date settlementDate,
                                              ArrayList<SAPPayrollBillingTransactions> payrolls,
                                              double collectionAgencyExpense) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<BadDebtRecoverDTO> badDebtRecoverDTOs = new ArrayList<BadDebtRecoverDTO>();

            for (SAPPayrollBillingTransactions payroll : payrolls) {
                // process dd transactions
                for (SAPBillingTransaction feeTransaction : payroll.getDdTransactions()) {
                    if (feeTransaction != null && feeTransaction.getFinancialTxnId() != null && feeTransaction.getFinancialReturnAmount() > 0) {
                        badDebtRecoverDTOs.add(PayrollRunTranslator.getBadDebtRecoverDTOFromParameters(
                                payroll.getPayrollRunId(),
                                settlementTypeCd,
                                settlementDate,
                                payroll.getInitiationDate(),
                                feeTransaction.getFinancialReturnAmount(),
                                feeTransaction.getFinancialTxnId(),
                                payroll.getIsCustomer()));
                    }
                }

                // process tax transaction
                if (payroll.getTaxTransaction() != null && payroll.getTaxTransaction().getFinancialTxnId() != null && payroll.getTaxTransaction().getFinancialReturnAmount() > 0) {
                    badDebtRecoverDTOs.add(PayrollRunTranslator.getBadDebtRecoverDTOFromParameters(
                            payroll.getPayrollRunId(),
                            settlementTypeCd,
                            settlementDate,
                            payroll.getInitiationDate(),
                            payroll.getTaxTransaction().getFinancialReturnAmount(),
                            payroll.getTaxTransaction().getFinancialTxnId(),
                            payroll.getIsCustomer()));
                }

                // process fee transactions
                for (SAPBillingTransaction feeTransaction : payroll.getFeeTransactions()) {
                    if (feeTransaction.getFinancialTxnId() != null && feeTransaction.getFinancialReturnAmount() > 0) {
                        badDebtRecoverDTOs.add(PayrollRunTranslator.getBadDebtRecoverDTOFromParameters(
                                payroll.getPayrollRunId(),
                                settlementTypeCd,
                                settlementDate,
                                payroll.getInitiationDate(),
                                feeTransaction.getFinancialReturnAmount(),
                                feeTransaction.getFinancialTxnId(),
                                payroll.getIsCustomer()));
                    }

                    if (feeTransaction.getSalesTaxTxnId() != null && feeTransaction.getSalesTaxReturnAmount() > 0) {
                        badDebtRecoverDTOs.add(PayrollRunTranslator.getBadDebtRecoverDTOFromParameters(
                                payroll.getPayrollRunId(),
                                settlementTypeCd,
                                settlementDate,
                                payroll.getInitiationDate(),
                                feeTransaction.getSalesTaxReturnAmount(),
                                feeTransaction.getSalesTaxTxnId(),
                                payroll.getIsCustomer()));
                    }
                }
            }

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            for (BadDebtRecoverDTO badDebtRecoverDTO : badDebtRecoverDTOs) {
                prList.add(
                        PayrollServices.financialTransactionManager.
                                addRecoverBadDebtTransaction(
                                        SourceSystemCode.valueOf(sourceSystemCd), companyId, badDebtRecoverDTO));
            }


            SpcfMoney collectionAgencyExpenseMoney = SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(collectionAgencyExpense);
            if (collectionAgencyExpenseMoney != null && !collectionAgencyExpenseMoney.isZero()) {
                PayrollRun payrollRun = PayrollRun.findPayrollRun(getCompany(companyId, sourceSystemCd), payrolls.get(0).getPayrollRunId());

                prList.add(
                        PayrollServices.financialTransactionManager.recordCollectionAgencyExpense(
                                SourceSystemCode.valueOf(sourceSystemCd),
                                companyId,
                                payrollRun.getId().toString(),
                                collectionAgencyExpenseMoney,
                                new DateDTO(SAPTranslator.getSpcfCalendarFromDate(settlementDate))));
            }

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding bad debt recover transaction", "Company", companyId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding bad debt recover transaction", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.BookTransferTransaction)
    public void addEmployeeReturnTransferTransaction(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayRunId)
            throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(
                    PayrollServices.financialTransactionManager.
                            addEmployeeReturnTransferTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, pPayRunId));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding employee return transfer transaction", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding employee return transfer transaction", pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RefundERPayable)
    public void refundERPayable(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String settlementTypeCode, double amount) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult pr = PayrollServices.financialTransactionManager
                                              .refundERPayable(SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, SettlementTypeDTO.valueOf(settlementTypeCode), SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(amount));

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error refunding ER Payable", pr);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error refunding ER Payable", pSourceSystemCd, pSourceCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.CreateRefundTransaction,
            OperationId.WriteoffBadDebtTransaction
    })
    public void applyERPayableToBalanceDue(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String payrollRunId, double amount) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ProcessResult pr = PayrollServices.payrollManager.applyERPayableToBalanceDue(
                    SourceSystemCode.valueOf(pSourceSystemCd),
                    pSourceCompanyId,
                    payrollRunId,
                    SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(amount));

            if (!pr.isSuccess()) {
                aeFactory.throwGenericException("Error applying ER Payable to balance due", pr);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error applying ER Payable to balance due Amount:" + amount, pSourceSystemCd, pSourceCompanyId, "PayrollRunId", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.CreateFeeTransaction, OperationId.AddManualFeeTransactions})
    public void addFeeTransferTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pPayRunId,
            double pAmount,
            String pOfferingServiceChargeTypeCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            FeeTransferDTO feeTransferDTO = PayrollRunTranslator.getFeeTransferDTOFromParameters(
                    pPayRunId,
                    pAmount,
                    pOfferingServiceChargeTypeCd);

            prList.add(
                    PayrollServices.financialTransactionManager.
                            addFeeTransferTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, feeTransferDTO));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding fee transfer transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable ex) {
            aeFactory.throwGenericException("Error adding fee transfer transaction.", pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateRefundTransaction)
    public void addRefundTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pPayRunId,
            double pAmount,
            Date pTxnDate,
            String pSettlementTypeCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            RefundDTO refundDTO = PayrollRunTranslator.getRefundDTOFromParameters(
                    pPayRunId,
                    pSettlementTypeCd,
                    pTxnDate,
                    pAmount);

            prList.add(
                    PayrollServices.financialTransactionManager.
                            addRefundTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, refundDTO));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding refund transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding refund transaction.", pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateRefundTransaction)
    public void addEmployerReturnRefundTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID)  String pSourceCompanyId,
            String pPayRunId,
            double pAmount,
            double pTaxAmount,
            Date pTxnDate,
            String pSettlementTypeCd) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            if (pAmount > 0 || pTaxAmount <= 0) {  //this process is either for the DD amount or for the entire amount if ACH
                RefundDTO refundDTO = PayrollRunTranslator.getRefundDTOFromParameters(
                        pPayRunId,
                        pSettlementTypeCd,
                        pTxnDate,
                        pAmount);

                prList.add(
                        PayrollServices.financialTransactionManager.
                                addEmployerReturnRefundTransaction(
                                        SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, refundDTO));
            }

            if (pTaxAmount > 0) {
                RefundDTO taxRefundDTO = PayrollRunTranslator.getRefundDTOFromParameters(
                        pPayRunId,
                        pSettlementTypeCd,
                        pTxnDate,
                        pTaxAmount);
                taxRefundDTO.setRefundTaxOnly(true);

                prList.add(
                        PayrollServices.financialTransactionManager.
                                addEmployerReturnRefundTransaction(
                                        SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, taxRefundDTO));
            }

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding employer return refund transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding employer return refund transaction.", pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.CreateRefundTransaction)
    public void addEmployeeReturnRefundTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pPayRunId,
            double pAmount,
            Date pTxnDate,
            String pSettlementTypeCd) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            RefundDTO refundDTO = PayrollRunTranslator.getRefundDTOFromParameters(
                    pPayRunId,
                    pSettlementTypeCd,
                    pTxnDate,
                    pAmount);

            prList.add(
                    PayrollServices.financialTransactionManager.
                            addEmployeeReturnRefundTransaction(
                                    SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, refundDTO));

            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding employee return refund transaction.",
                                                "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding employee return refund transaction.",
                                            pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewPayrollScreen)
    public ArrayList<SAPPropertyAudit> getTransactionHistory(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID)  String pSourceCompanyId,
            String pFinTxnId) throws Throwable {

        ArrayList<SAPPropertyAudit> auditListReturnVal = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            FinancialTransaction ft = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(pFinTxnId));
            DomainEntitySet<FinancialTransactionState> financialTxStateCollection = ft.getFinancialTransactionStates();

            auditListReturnVal = PayrollRunTranslator.getSAPPropertAuditsFromFinancialTransactionStates(
                    financialTxStateCollection);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transaction history.",
                                            pSourceSystemCd, pSourceCompanyId, "Transaction", pFinTxnId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return auditListReturnVal;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.EscalationCreditTransaction)
    public void addEscalation(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pPayRunId,
            boolean pIsEmployee,
            String pSettlementTypeCd,
            double pAmount,
            Date pSettlementDate) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();

            ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

            prList.add(
                    PayrollServices.financialTransactionManager.addEscalation(
                            SourceSystemCode.valueOf(pSourceSystemCd),
                            pSourceCompanyId,
                            pPayRunId,
                            pIsEmployee,
                            SettlementType.valueOf(pSettlementTypeCd),
                            new BigDecimal(pAmount),
                            new DateDTO(SAPTranslator.getSpcfCalendarFromDate(pSettlementDate))));


            if (aeFactory.errorsOccurred(prList)) {
                aeFactory.throwGenericException("Error adding escalation", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding escalation", pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewChaseReport)
    public ArrayList<SAPChaseReport> findChaseReportForDateRange(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
            Date pFromDate,
            Date pToDate) throws Throwable {

        ArrayList<SAPChaseReport> returnList = new ArrayList<SAPChaseReport>();

        try {
            PayrollServices.beginUnitOfWorkWithSecondary(FlushMode.MANUAL, true);

            SpcfCalendar spcfFromDate = SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate);
            SpcfCalendar spcfToDate = SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            ArrayList<TransactionTypeCode> transactionTypeCds = new ArrayList<TransactionTypeCode>(3);
            transactionTypeCds.add(TransactionTypeCode.EmployeeDdCredit);
            transactionTypeCds.add(TransactionTypeCode.EmployerDdDebit);
            transactionTypeCds.add(TransactionTypeCode.EmployerFeeDebit);

            ArrayList<TransactionStateCode> transactionStateCds = new ArrayList<TransactionStateCode>(3);
            transactionStateCds.add(TransactionStateCode.Executed);
            transactionStateCds.add(TransactionStateCode.Completed);
            transactionStateCds.add(TransactionStateCode.Returned);

            // get a list of payrolls for the specified date
            DomainEntitySet<PayrollRun> payrollList = PayrollRun.findPayrollRuns(
                    company,
                    spcfFromDate,
                    spcfToDate);

            boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

            for (PayrollRun payroll : payrollList) {

                DomainEntitySet<FinancialTransaction> financialTransactions =
                        FinancialTransaction.findFinancialTransactionsForPayrollByTypeAndState(
                                payroll,
                                transactionTypeCds,
                                transactionStateCds);
                if (financialTransactions.size() > 0) {
                    returnList.add(
                            PayrollRunTranslator.getSAPChaseReportFromDomainEntitys(
                                    payroll, financialTransactions, canViewFullBankAccountNumbers));
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding chase report transactions.  From:" + pFromDate + " To:" + pToDate,
                                            pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }

        return returnList;
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.AgentInitiatesRefundRebill})
    public ArrayList<ProcessResult> addRefundRebillTransaction(
            String pSourceSystemCd,
            @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
            String pPayRunId,
            double pAmount,
            Date pTxnDate,
            boolean rebill,
            String pFeeDebitTransactionId,
            double pOverrideAmount,
            Integer pOverrideQuantity) throws Throwable {
        PayrollServices.beginUnitOfWork();
        ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
        try {

            boolean payrollRefundRebillLimitEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_PAYROLL_REFUND_REBILL_ENABLED, true);
            long payrollRefundRebillLimit = SystemParameter.findLongValue(SystemParameter.Code.PAYROLL_REFUND_REBILL_LIMIT, 500);

            // refund/rebill
            if (rebill) {
                SpcfMoney overrideAmount = SAPTranslator.getSpcfMoneyFromDouble(pOverrideAmount);

                if (payrollRefundRebillLimitEnabled && pOverrideAmount > payrollRefundRebillLimit) {
                    aeFactory.throwGenericException("The rebill amount you have entered is higher than the permissible limit. " +
                            "Enter an amount less than $"+payrollRefundRebillLimit+" to create the entry.");
                }
                RebillFeeTransactionDTO rebillFeeTransactionDTO =
                        new RebillFeeTransactionDTO(pFeeDebitTransactionId, overrideAmount, pOverrideQuantity);

                prList.add(PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO));
            }
            // refund only
            else {
                if (payrollRefundRebillLimitEnabled && pAmount > payrollRefundRebillLimit) {
                    aeFactory.throwGenericException("The refund amount you have entered is higher than the permissible limit. " +
                            "Enter an amount less than $"+payrollRefundRebillLimit+" to create the entry.");
                }
                ERRefundDTO erRefundDTO = new ERRefundDTO(
                        pFeeDebitTransactionId,
                        SAPTranslator.getSpcfMoneyFromDouble(pAmount),
                        new DateDTO(SAPTranslator.getSpcfCalendarFromDate(pTxnDate)),
                        SettlementTypeDTO.ACH);

                prList.add(
                        PayrollServices.financialTransactionManager.
                                refundEmployerTransaction(
                                        SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, erRefundDTO));
            }

            if (aeFactory.errorsOccurred(prList)) {
                PayrollServices.rollbackUnitOfWork();
                aeFactory.throwGenericException("Error adding refund/rebill transaction.", "PayrollRun", pPayRunId, prList);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error adding refund/rebill transaction.",
                                            pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return prList;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.ViewPayrollScreen,
            OperationId.CreateRefundTransaction,
            OperationId.WriteoffBadDebtTransaction,
            OperationId.RefundEmployerFraudEscalation,
            OperationId.RecordNonACHRedebitTransaction
    })
    public ArrayList<SAPPayrollBillingTransactions> findPayrollUncollectedBalances(@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId,
                                                                                   String pSourceSystemCd,
                                                                                   String pPayrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return findPayrollBalancesCore(pSourceSystemCd, pCompanyId, pPayrollRunId, PayrollRunAdapter.LS_UNCOLLECTED_TRANSACTIONS, true);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding uncollected payroll transactions.",
                                            pSourceSystemCd, pCompanyId, "PayrollRun", pPayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.RecoverBadDebtTransaction
    })
    public ArrayList<SAPPayrollBillingTransactions> findPayrollUnrecoveredBalances(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return findPayrollBalancesCore(pSourceSystemCd, pSourceCompanyId, pPayrollRunId, PayrollRunAdapter.LS_UNRECOVERED_TRANSACTIONS, true);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding unrecovered payroll transactions.",
                                            pSourceSystemCd, pSourceCompanyId, "PayrollRun", pPayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.RefundEmployerFraudEscalation,
            OperationId.IssueRedebitTransaction
    })
    public ArrayList<SAPPayrollBillingTransactions> findPayrollCollectedTransactions(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return findPayrollBalancesCore(pSourceSystemCd, pSourceCompanyId, pPayrollRunId, PayrollRunAdapter.LS_COLLECTED_TRANSACTIONS, true);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding collected transactions.", pSourceSystemCd, pSourceCompanyId, "PayrollRunId", pPayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = {
            OperationId.RecordPrefundingWire
    })
    public ArrayList<SAPPayrollBillingTransactions> findPayrollPrefundingTransactions(String pSourceSystemCd, @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId, String pPayrollRunId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return findPayrollBalancesCore(pSourceSystemCd, pSourceCompanyId, pPayrollRunId, PayrollRunAdapter.LS_PREFUNDING_TRANSACTIONS, true);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding prefunding transactions.", pSourceSystemCd, pSourceCompanyId, "PayrollRunId", pPayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private ArrayList<SAPPayrollBillingTransactions> findPayrollBalancesCore(String pSourceSystemCd, String pCompanyId, String pPayrollRunId, String pTranactionsType, boolean populateReturnAmount) throws Exception {

        Company company = getCompany(pCompanyId, pSourceSystemCd);

        // initialize the return DTO
        ArrayList<SAPPayrollBillingTransactions> payrollList = new ArrayList<SAPPayrollBillingTransactions>();

        DomainEntitySet<PayrollRun> payrollRuns = new DomainEntitySet<PayrollRun>();

        if (pTranactionsType.equals(LS_UNRECOVERED_TRANSACTIONS) || pTranactionsType.equals(LS_COLLECTED_TRANSACTIONS) || pTranactionsType.equals(LS_PREFUNDING_TRANSACTIONS)) {
            payrollRuns.add(getCompanyPayroll(company, pPayrollRunId));
        } else if (pTranactionsType.equals(LS_UNCOLLECTED_TRANSACTIONS)) {
            Criterion<PayrollRun> where = PayrollRun.Company().equalTo(company)
                                                    .And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Complete))
                                                    .And(PayrollRun.SourcePayRunId().notEqualTo(pPayrollRunId));

            Expression<PayrollRun> query =
                    new Query<PayrollRun>()
                            .Where(where)
                            .OrderBy(PayrollRun.PaycheckDate())
                            .EagerLoad(PayrollRun.FinancialTransactionSet());

            payrollRuns = Application.find(PayrollRun.class, query);

            if (payrollRuns == null || payrollRuns.size() == 0) {
                payrollRuns = new DomainEntitySet<PayrollRun>();
                payrollRuns.add(getCompanyPayroll(company, pPayrollRunId));
            } else {
                payrollRuns.add(getCompanyPayroll(company, pPayrollRunId));
            }
        }

        for (PayrollRun payrollRun : payrollRuns) {
            SAPPayrollBillingTransactions sapPayrollBillingTransactions = new SAPPayrollBillingTransactions();
            sapPayrollBillingTransactions.setPayrollRunId(payrollRun.getSourcePayRunId());
            sapPayrollBillingTransactions.setCheckDate(SAPTranslator.getDateFromSpcfCalendar(payrollRun.getPaycheckDate()));

            HashMap<BillingDetail, SAPBillingTransaction> processedBillingDetails =
                    new HashMap<BillingDetail, SAPBillingTransaction>();

            HashMap<FinancialTransaction, SpcfMoney> ddAmounts = new HashMap<FinancialTransaction, SpcfMoney>();

            HashMap<FinancialTransaction, SpcfMoney> taxAmounts = new HashMap<FinancialTransaction, SpcfMoney>();

            HashMap<FinancialTransaction, SpcfMoney> fees = new HashMap<FinancialTransaction, SpcfMoney>();

            HashMap<FinancialTransaction, SpcfMoney> salesTax = new HashMap<FinancialTransaction, SpcfMoney>();

            SpcfDecimal handlingFeeAmount = SpcfMoney.ZERO;

            if (pTranactionsType.equals(LS_UNRECOVERED_TRANSACTIONS)) {
                ddAmounts = payrollRun.getUnrecoveredDirectDepositAmount();
                taxAmounts = payrollRun.getUnrecoveredTaxAmount();
                fees = payrollRun.getUnrecoveredFeeAmounts();
                salesTax = payrollRun.getUnrecoveredSalesTaxAmounts();
            } else if (pTranactionsType.equals(LS_COLLECTED_TRANSACTIONS)) {
                ddAmounts = payrollRun.getCollectedDDAmount();
                taxAmounts = payrollRun.getCollectedTaxAmount();
                fees = payrollRun.getCollectedFeeAmounts();
                salesTax = payrollRun.getCollectedSalesTaxAmounts();
            } else if (pTranactionsType.equals(LS_UNCOLLECTED_TRANSACTIONS)) {
                ddAmounts = payrollRun.getUncollectedDDAmount();
                taxAmounts = payrollRun.getUncollectedTaxAmount();
                fees = payrollRun.getUncollectedFeeAmounts();
                salesTax = payrollRun.getUncollectedSalesTaxAmounts();

                handlingFeeAmount = EmailUtils.getIntuitHandlingFee(payrollRun);

                if (ddAmounts.size() == 0) {
                    // non-ACH redebits can be added against any dd transaction, even if the uncollected balance is zero
                    FinancialTransaction transaction = payrollRun.getDdDebit();
                    if (transaction != null) {
                        ddAmounts.put(transaction, ZERO);
                    }
                }
                if (taxAmounts.size() == 0) {
                    // non-ACH redebits can be added against any dd transaction, even if the uncollected balance is zero
                    FinancialTransaction transaction = payrollRun.getEmployerTaxDebitTransaction();
                    if (transaction != null) {
                        taxAmounts.put(transaction, ZERO);
                    }
                }
            } else if (pTranactionsType.equals(LS_PREFUNDING_TRANSACTIONS)) {
                ddAmounts = payrollRun.getPrefundingPayrollAmounts();
                fees = payrollRun.getPrefundingFeeAmounts();
                salesTax = payrollRun.getPrefundingTaxAmounts();
            }

            sapPayrollBillingTransactions.setDdTransactions(new ArrayList<SAPBillingTransaction>());
            for (FinancialTransaction ddTransaction : ddAmounts.keySet()) {

                SpcfMoney payrollAmount = ddAmounts.get(ddTransaction);

                SAPBillingTransaction sapBillingTransaction =
                        PayrollRunTranslator.getSAPBillingFinancialTransaction(ddTransaction, payrollAmount, populateReturnAmount);

                sapPayrollBillingTransactions.getDdTransactions().add(sapBillingTransaction);
            }

            if (taxAmounts.size() > 1) {
                aeFactory.throwGenericException("Error in findPayrollBalances: More than one tax txn present Company:" +
                                                        pCompanyId + " PayrollRunId:" + pPayrollRunId);
            }
            for (FinancialTransaction taxTransaction : taxAmounts.keySet()) {

                SpcfMoney payrollAmount = taxAmounts.get(taxTransaction);

                SAPBillingTransaction sapBillingTransaction =
                        PayrollRunTranslator.getSAPBillingFinancialTransaction(taxTransaction, payrollAmount, populateReturnAmount);

                sapPayrollBillingTransactions.setTaxTransaction(sapBillingTransaction);
            }

            // process fee transactions
            // ADD an Retrieved transaction for each Retrieved fee, if there is a tax transaction it will be correlated later
            sapPayrollBillingTransactions.setFeeTransactions(new ArrayList<SAPBillingTransaction>());
            for (FinancialTransaction feeTransaction : fees.keySet()) {

                SpcfMoney amount = fees.get(feeTransaction);

                SAPBillingTransaction sapBillingTransaction =
                        PayrollRunTranslator.getSAPBillingFinancialTransaction(feeTransaction, amount, populateReturnAmount);

                sapPayrollBillingTransactions.getFeeTransactions().add(sapBillingTransaction);

                // mark as processed and allow for retrieving later for correlation w/a sales tax txn
                BillingDetail billingDetail = feeTransaction.getBillingDetail();
                processedBillingDetails.put(billingDetail, sapBillingTransaction);
            }

            if (handlingFeeAmount.isGreaterThan(SpcfMoney.ZERO)) {
                //this is pretty much random since there isn't really a return for it.  But if it's not a fee, it won't be a FeeRedebit.
                SpcfUniqueId txnIdToUse = null;
                if (!fees.isEmpty()){
                    txnIdToUse = fees.keySet().iterator().next().getId();
                } else {
                    FinancialTransaction feeDebit = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerFeeDebit).getFirst();
                    if (feeDebit != null) {
                        txnIdToUse = feeDebit.getId();
                    } else {
                        //adding an emptyGUID to enable the Agent to add the handlingfee from the SAP UI, the feeTransaction to be created on save
                        txnIdToUse = SpcfUniqueId.getEmptyUniqueId();
                    }
                }

                if (txnIdToUse != null) {
                    sapPayrollBillingTransactions.setHandlingFeeTransaction(PayrollRunTranslator.getSAPBillingFinancialTransactionForHandlingCharge(handlingFeeAmount, txnIdToUse));
                }
            }

            // process sales tx
            // -- if the sales tax transaction is for an already processed fee transaction, update the Retrieved SAP DTO
            // -- if the sales tax transaction 'stands alone' -- i.e. there is no associated fee transaction or the
            //    associated fee transaction does not have an Retrieved amount, then creat an Retrieved DTO for
            //    the sales tax transaction
            for (FinancialTransaction taxTransaction : salesTax.keySet()) {

                SpcfMoney amount = salesTax.get(taxTransaction);

                // check for a correlated fee transaction that has already been processed
                BillingDetail billingDetail = taxTransaction.getBillingDetail();
                if (billingDetail != null && processedBillingDetails.containsKey(billingDetail)) {
                    SAPBillingTransaction sapBillingTransaction = processedBillingDetails.get(billingDetail);
                    PayrollRunTranslator.updateSAPBillingFinancialTransactionSalesTax(
                            sapBillingTransaction, taxTransaction, amount, populateReturnAmount);
                } else {
                    SAPBillingTransaction sapBillingTransaction =
                            PayrollRunTranslator.getSAPBillingFinancialTransactionSalesTax(taxTransaction, amount);

                    // the UI uses the Transaction Type as a display label, try to fetch it for tax txn
                    // even when the associated fee txn does not have any outstanding Retrieved amount
                    if (billingDetail != null) {
                        FinancialTransaction feeTxn = billingDetail.getFeeTransaction();
                        if (feeTxn != null) {
                            sapBillingTransaction.setFinancialTxnType(feeTxn.getTransactionType().getName());
                        }
                    }

                    sapPayrollBillingTransactions.getFeeTransactions().add(sapBillingTransaction);
                }
            }
            if (pTranactionsType.equals(LS_UNCOLLECTED_TRANSACTIONS)) {
                // always add the current payroll
                if (payrollRun.getSourcePayRunId().equals(pPayrollRunId)) {
                    payrollList.add(sapPayrollBillingTransactions);
                }
                // only add the others if there are uncollected amounts
                else if ((sapPayrollBillingTransactions.getFeeTransactions() != null && sapPayrollBillingTransactions.getFeeTransactions().size() > 0) ||
                        (sapPayrollBillingTransactions.getDdTransactions() != null && sapPayrollBillingTransactions.getDdTransactions().size() > 0) ||
                        (sapPayrollBillingTransactions.getTaxTransaction() != null && sapPayrollBillingTransactions.getTaxTransaction().getFinancialAmount() > 0)) {
                    payrollList.add(sapPayrollBillingTransactions);
                }
            } else {
                payrollList.add(sapPayrollBillingTransactions);
            }
        }


        return payrollList;

    }

    @FlexMethod
    public SAPCompanyBalance findCompanyBalance(String pSourceSystemCd,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company = Company.findCompany(
                    pCompanyId,
                    SourceSystemCode.valueOf(pSourceSystemCd));

            if (company == null) {
                throw aeFactory.companyNotFoundException();
            }

            SpcfDecimal companyBalance = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERReturnReceivable);

            return PayrollRunTranslator.getCompanyBalance(new SpcfMoney(companyBalance));
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding company balance.", pSourceSystemCd, pCompanyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    public SAPPayrollTransaction findPayrollTransactionById(String transactionId,@TenantId(IdType = CompanyIdentifierType.PSID) String pCompanyId) throws Throwable {
        SAPPayrollTransaction sapPayrollTransaction = new SAPPayrollTransaction();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));
            sapPayrollTransaction = PayrollRunTranslator.getSAPPayrollTransactionFromDomainEntity(financialTransaction, null, null);
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error in finding payroll.  TransactionId" + transactionId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPayrollTransaction;
    }

    protected SAPMoneyMovementTransaction getSAPMoneyMovementTransactionForPayrollTransactionsFromMMT(MoneyMovementTransaction moneyMovementTransaction) throws Exception {
        FinancialTransaction representativeFinancialTransaction = moneyMovementTransaction.getFinancialTransactionCollection().getFirst();
        if (representativeFinancialTransaction == null) {
            return null;
        }

        // get the bank account and debit amount
        BankAccount bankAccount = null;
        if (representativeFinancialTransaction.getCreditBankAccount() != null && representativeFinancialTransaction.getCreditBankAccountType().equals(BankAccountOwnerType.Company)) {
            bankAccount = representativeFinancialTransaction.getCreditBankAccount();
        } else if (representativeFinancialTransaction.getDebitBankAccountType() != null && representativeFinancialTransaction.getDebitBankAccountType().equals(BankAccountOwnerType.Company)) {
            bankAccount = representativeFinancialTransaction.getDebitBankAccount();
        }

        // get the check date
        Date checkDate = null;
        PayrollRun payrollRun = representativeFinancialTransaction.getPayrollRun();
        if (payrollRun != null) {
            checkDate = SAPTranslator.getDateFromSpcfCalendar(payrollRun.getPaycheckDate());
        }

        Date settlementDate = SAPTranslator.getDateFromSpcfCalendar(representativeFinancialTransaction.getSettlementDate());

        return PayrollRunTranslator.getSAPMoneyMovementTransactionFromDomainEntity(
                moneyMovementTransaction,
                bankAccount,
                settlementDate,
                checkDate,
                SAPTranslator.getDoubleFromSpcfMoney(moneyMovementTransaction.getMoneyMovementTransactionAmount()),
                getACHReason(moneyMovementTransaction),
                payrollRun);
    }

    @FlexMethod
    public ArrayList<SAPMoneyMovementTransaction> findMoneyMovementTransactions(String sourceSystemCd,
                                                                                @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                                                                Date fromDate) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company =
                    Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            SpcfCalendar spcfFromDate =
                    fromDate != null ? SAPTranslator.getSpcfCalendarFromDate_BeginDay(fromDate) : null;
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = findExecutedACHERMoneyMovementTransactions(company, spcfFromDate);

            ArrayList<SAPMoneyMovementTransaction> sapMoneyMovementTransactionList = new ArrayList<SAPMoneyMovementTransaction>();
            for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
                SAPMoneyMovementTransaction retTransaction = getSAPMoneyMovementTransactionForPayrollTransactionsFromMMT(moneyMovementTransaction);
                if (retTransaction != null)
                    sapMoneyMovementTransactionList.add(retTransaction);

            }

            return sapMoneyMovementTransactionList;
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error in finding money movement transactions.  From:" + fromDate, sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private DomainEntitySet<MoneyMovementTransaction> findExecutedACHERMoneyMovementTransactions(Company pCompany, SpcfCalendar pFromDate) {

        /* note that this "filter" expression is being used in a non-standard way.  This is strictly for performance--
            to avoid joining twice on the table.  A left join will be produced, however the where clause will exclude rows that are EmployeeDdCredits.
            Since those are the only Ts on the MMT, it will not find those MMTs
        */

        TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.EmployeeDdCredit);
        Criterion<MoneyMovementTransaction> where =
                MoneyMovementTransaction.Company().equalTo(pCompany)
                                        .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Executed))
                                        .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().greaterThan(ZERO))
                                        .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit))
                                        .And(MoneyMovementTransaction.FinancialTransactionSet().Filter().TransactionType().notEqualTo(transactionType));

        if (pFromDate != null) {
            where = where.And(MoneyMovementTransaction.InitiationDate().greaterOrEqualThan(pFromDate));
        }

        Expression<MoneyMovementTransaction> query =
                new Query<MoneyMovementTransaction>()
                        .Where(where)
                        .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet().Filter().SettlementDate().greaterOrEqualThan(MoneyMovementTransaction.InitiationDate()));

        return Application.find(MoneyMovementTransaction.class, query);
    }

    private String getACHReason(MoneyMovementTransaction moneyMovementTransaction) {
        // build the collection of transaction types codes
        ArrayList<TransactionTypeCode> transactionTypeCodes = new ArrayList<TransactionTypeCode>();
        ArrayList<TransactionAssociationType> associationTypes = new ArrayList<TransactionAssociationType>();
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            transactionTypeCodes.add(financialTransaction.getTransactionType().getTransactionTypeCd());
            associationTypes.add(financialTransaction.getTransactionType().getAssociationType());
        }

        // must be a payroll
        if (associationTypes.contains(TransactionAssociationType.Impound)) {
            return LS_PAYROLL_REASON;
        }

        // check to see if there are only employer tax credits
        else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerTaxCredit)) {
            for (TransactionTypeCode transactionTypeCode : transactionTypeCodes) {
                if (transactionTypeCode != TransactionTypeCode.EmployerTaxCredit) {
                    return LS_PAYROLL_REASON;
                }
            }
            return LS_REFUND_FOR_VOID_REASON;
        }

        // redebits
        else if (associationTypes.contains(TransactionAssociationType.Redebit)) {
            FinancialTransaction originalTransaction = moneyMovementTransaction.getFinancialTransactionCollection().get(0).getOriginalTransaction();
            if (originalTransaction.getMoneyMovementTransaction().getMoneyMovementTransactionAmount().compareTo(moneyMovementTransaction.getMoneyMovementTransactionAmount()) == 0) {
                return LS_REDEBIT_REASON;
            } else {
                return LS_PARTIAL_REDEBIT_REASON;
            }
        }

        // refund rebill
        else if ((transactionTypeCodes.contains(TransactionTypeCode.EmployerFeeDebit) ||
                transactionTypeCodes.contains(TransactionTypeCode.EmployerFeeRedebit)) &&
                transactionTypeCodes.contains(TransactionTypeCode.EmployerFeeRefundCredit)) {
            return LS_REFUND_REBILL_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerVerificationDebit)) {
            return LS_BANK_VERIFICATION_DEBIT_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerVerificationCredit)) {
            return LS_BANK_VERIFICATION_CREDIT_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.IntuitFeeTransfer)) {
            return LS_INTUIT_FEE_TRANSFER_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployeeDdReversalDebit)) {
            return LS_EMPLOYEE_DD_REVERSAL_DEBIT_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.IntuitEmployerVerificationReturnTransfer)) {
            return LS_INTUIT_EMPLOYER_VERIFICATION_RETURN_TRANSFER_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerDdReversalRefundCredit)) {
            return LS_EMPLOYEE_DD_REVERSAL_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerEscalationCredit)) {
            return LS_EMPLOYER_ESCALATION_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerVerificationCreditReturnTransfer)) {
            return LS_EMPLOYER_VERIFICATION_CREDIT_RETURN_TRANSFER_REASON;
        } else if (containsAny(transactionTypeCodes, TransactionTypeCode.EmployerFraudOrEscalationRefundCredit, TransactionTypeCode.EmployerTaxFraudOrEscalationRefundCredit)) {
            return LS_EMPLOYER_FRAUD_REASON;
        } else if (associationTypes.contains(TransactionAssociationType.Refund)) {
            return LS_REFUND_REASON;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerTaxCreditReturnedTransfer)) {
            return LS_EMPLOYER_TAX_CREDIT_RETURNED_TRANSFER;
        } else if (containsAny(transactionTypeCodes, TransactionTypeCode.EmployerWriteOff, TransactionTypeCode.EmployerWriteOffFee, TransactionTypeCode.EmployerWriteOffSalesAndUseTax, TransactionTypeCode.EmployerWriteOffTax)) {
            return LS_WRITE_OFF;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.IntuitEmployeeReturnTransfer)) {
            return LS_EMPLOYEE_RETURN_TRANSFER;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.IntuitTaxVoidTransfer)) {
            return LS_TAX_VOID_TRANSFER;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.ReissueTaxLiabilityTransfer)) {
            return LS_REISSUE_TAX_LIABILITY_TRANSFER;
        } else if (transactionTypeCodes.contains(TransactionTypeCode.EmployerFeeDebit)) {
            int numberOfFeeDebits = 0;
            for (TransactionTypeCode transactionTypeCode : transactionTypeCodes) {
                if (transactionTypeCode == TransactionTypeCode.EmployerFeeDebit) {
                    numberOfFeeDebits++;
                }
            }
            if (numberOfFeeDebits == 1) {
                FinancialTransaction feeTransaction = null;
                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeDebit) {
                        feeTransaction = financialTransaction;
                    }
                }
                String feeName = PayrollRunTranslator.getFeeName(feeTransaction);
                if (feeName != null) {
                    return LS_Fee_REASON + " - " + feeName;
                }
            }
            return LS_Fee_REASON;
        }

        // if the transaction is unknown send back a string of all of the transaction codes so we can fix it
        String transactionTypeList = "";
        for (TransactionTypeCode transactionTypeCode : transactionTypeCodes) {
            transactionTypeList += transactionTypeCode.toString() + " ";
        }
        logger.error("Unknown ACH reason encountered - Transaction Types: " + transactionTypeList);
        return "Unknown";
    }

    private <T> boolean containsAny(Collection<T> allCodes, T... codesToLookFor) {
        return !Collections.disjoint(allCodes, Arrays.asList(codesToLookFor));
    }

    @FlexMethod
    public SAPPayrollACHDetailSet findAchDetailTransactions(String moneyMovementTransactionId,@TenantId(IdType = CompanyIdentifierType.PSID) String companyId) throws Throwable {
        SAPPayrollACHDetailSet sapPayrollACHDetailSet = new SAPPayrollACHDetailSet();
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Expression<MoneyMovementTransaction> query =
                    new Query<MoneyMovementTransaction>()
                            .Where(MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.createInstance(moneyMovementTransactionId)))
                            .EagerLoad(MoneyMovementTransaction.FinancialTransactionSet());

            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, query);

            boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

            if (moneyMovementTransactions.size() != 1) {
                throw new Exception("Money movement transaction with id:" + moneyMovementTransactionId + " not found");
            }

            MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.get(0);
            String transactionReason = getACHReason(moneyMovementTransaction);

            if (transactionReason.equals(LS_PAYROLL_REASON)) {
                SpcfDecimal feeTransactionsTotal = ZERO;
                PayrollRun payrollRun = null;
                boolean isERTaxDebitFound = false;
                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    // we already know that this is a payroll
                    if (payrollRun == null) {
                        payrollRun = financialTransaction.getPayrollRun();
                    }
                    // any transactions that are not the dd debit or the tax debit go in the fees section
                    if (financialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.EmployerDdDebit &&
                            financialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.EmployerTaxDebit &&
                            financialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.EmployerTaxCredit) {
                        SAPPayrollTransaction feeTransaction = PayrollRunTranslator.getFeeTransactionFromDomainEntity(financialTransaction);
                        feeTransactionsTotal = feeTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(financialTransaction));
                        sapPayrollACHDetailSet.getFeeTransactions().add(feeTransaction);
                    } else if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit) {
                        getACHDDTransactions(moneyMovementTransaction, financialTransaction, sapPayrollACHDetailSet);
                    } else if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxDebit) {
                        isERTaxDebitFound = true;
                    }
                }
                if (payrollRun != null && isERTaxDebitFound) {
                    getACHTaxTransactions(payrollRun, sapPayrollACHDetailSet);
                }
                sapPayrollACHDetailSet.setFeeTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(feeTransactionsTotal));
                sapPayrollACHDetailSet.setTaxesTotal(SAPTranslator.getDoubleFromSpcfMoney(SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(sapPayrollACHDetailSet.getTaxTransactionsTotal())));
            } else if (transactionReason.equals(LS_REFUND_FOR_VOID_REASON)) {
                boolean refundTransactionFound = false;
                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    if (!refundTransactionFound &&
                            financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxCredit &&
                            financialTransaction.getCompanyAdjustmentSubmission() != null) {
                        Map<Law, SpcfDecimal> refundAmountsMap = financialTransaction.getCompanyAdjustmentSubmission().findVoidImmediateRefundAmountsByLaw();
                        RulesInfo rulesInfo = new RulesInfo();
                        SpcfDecimal taxTransactionsTotal = ZERO;
                        for (Law law : refundAmountsMap.keySet()) {
                            // since this is a refund negate the ft amounts
                            SAPAgencyTransaction sapAgencyTransaction =
                                    PayrollRunTranslator.getACHSAPAgencyTransaction(law.getLawId(), SAPTranslator.getDoubleFromSpcfMoney(refundAmountsMap.get(law).negate()), rulesInfo);
                            taxTransactionsTotal = taxTransactionsTotal.add(refundAmountsMap.get(law).negate());
                            sapPayrollACHDetailSet.getTaxTransactions().add(sapAgencyTransaction);
                        }
                        sapPayrollACHDetailSet.setTaxTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(taxTransactionsTotal));
                        sapPayrollACHDetailSet.setTaxesTotal(SAPTranslator.getDoubleFromSpcfMoney(taxTransactionsTotal));
                        refundTransactionFound = true;
                    }
                }
            } else if (transactionReason.equals(LS_REDEBIT_REASON) || transactionReason.equals(LS_PARTIAL_REDEBIT_REASON)) {
                SpcfMoney feeTransactionsTotal = ZERO;
                SpcfMoney taxTransactionsTotal = ZERO;
                SpcfMoney ddTransactionsTotal = ZERO;
                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    if (financialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.EmployerDdRedebit &&
                            financialTransaction.getTransactionType().getTransactionTypeCd() != TransactionTypeCode.EmployerTaxRedebit) {
                        feeTransactionsTotal = (SpcfMoney) feeTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(financialTransaction));
                    } else if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdRedebit) {
                        ddTransactionsTotal = (SpcfMoney) ddTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(financialTransaction));
                    } else if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerTaxRedebit) {
                        taxTransactionsTotal = (SpcfMoney) taxTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(financialTransaction));
                    }
                }
                sapPayrollACHDetailSet.setFeeTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(feeTransactionsTotal));
                sapPayrollACHDetailSet.setTaxTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(taxTransactionsTotal));
                sapPayrollACHDetailSet.setTaxesTotal(SAPTranslator.getDoubleFromSpcfMoney(taxTransactionsTotal));
                sapPayrollACHDetailSet.setDdTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(ddTransactionsTotal));
            } else if (transactionReason.equals(LS_EMPLOYEE_DD_REVERSAL_REASON)) {
                SpcfMoney ddTransactionsTotal = ZERO;
                for (FinancialTransaction ddTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    SAPPayrollEmployeeTransaction sapPayrollEmployeeTransaction =
                            PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(ddTransaction.getOriginalTransaction(), null, null, null, canViewFullBankAccountNumbers);
                    ddTransactionsTotal = (SpcfMoney) ddTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(ddTransaction));
                    sapPayrollACHDetailSet.getDdTransactions().add(sapPayrollEmployeeTransaction);
                }

                sapPayrollACHDetailSet.setDdTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(ddTransactionsTotal));
            }
            // lump every thing into the fees collection
            else {
                SpcfMoney feeTransactionsTotal = ZERO;
                for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                    SAPPayrollTransaction feeTransaction = PayrollRunTranslator.getFeeTransactionFromDomainEntity(financialTransaction);
                    feeTransactionsTotal = (SpcfMoney) feeTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(financialTransaction));
                    sapPayrollACHDetailSet.getFeeTransactions().add(feeTransaction);
                }
                sapPayrollACHDetailSet.setFeeTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(feeTransactionsTotal));
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding detail transactions.  MMT ID:" + moneyMovementTransactionId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return sapPayrollACHDetailSet;
    }

    private void getACHTaxTransactions(PayrollRun pPayrollRun, SAPPayrollACHDetailSet pSapPayrollACHDetailSet) {
        SpcfDecimal taxCreditTransactionsTotal = ZERO;
        Map<Law, SpcfDecimal> lawAmountsMap = new HashMap<Law, SpcfDecimal>();
        for (FinancialTransaction financialTransaction : pPayrollRun.getFinancialTransactionCollection()) {
            if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxCredit) {
                if (!lawAmountsMap.containsKey(financialTransaction.getLaw())) {
                    lawAmountsMap.put(financialTransaction.getLaw(), ZERO);
                }
                lawAmountsMap.put(financialTransaction.getLaw(), lawAmountsMap.get(financialTransaction.getLaw()).add(financialTransaction.getFinancialTransactionAmount()));
                taxCreditTransactionsTotal = taxCreditTransactionsTotal.add(financialTransaction.getFinancialTransactionAmount());
            } else if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.AgencyTaxDebit) {
                if (!lawAmountsMap.containsKey(financialTransaction.getLaw())) {
                    lawAmountsMap.put(financialTransaction.getLaw(), ZERO);
                }
                lawAmountsMap.put(financialTransaction.getLaw(), lawAmountsMap.get(financialTransaction.getLaw()).subtract(financialTransaction.getFinancialTransactionAmount()));
                taxCreditTransactionsTotal = taxCreditTransactionsTotal.subtract(financialTransaction.getFinancialTransactionAmount());
            }
        }
        pSapPayrollACHDetailSet.setTaxTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(taxCreditTransactionsTotal));
        RulesInfo rulesInfo = new RulesInfo();
        for (Law law : lawAmountsMap.keySet()) {
            SAPAgencyTransaction sapAgencyTransaction =
                    PayrollRunTranslator.getACHSAPAgencyTransaction(law.getLawId(), SAPTranslator.getDoubleFromSpcfMoney(lawAmountsMap.get(law)), rulesInfo);
            pSapPayrollACHDetailSet.getTaxTransactions().add(sapAgencyTransaction);
        }
    }

    private void getACHDDTransactions(MoneyMovementTransaction moneyMovementTransaction,
                                      FinancialTransaction financialTransaction,
                                      SAPPayrollACHDetailSet sapPayrollACHDetailSet) throws Exception {
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findFinancialTransactions(
                        moneyMovementTransaction.getCompany(),
                        financialTransaction.getPayrollRun().getSourcePayRunId(),
                        TransactionCategory.Employee,
                        null,
                        null, null);

        boolean canViewFullBankAccountNumbers = PIIMask.authenticatedUserCanViewFullBankAccountNumbers();

        SpcfMoney ddTransactionsTotal = ZERO;
        for (FinancialTransaction ddTransaction : financialTransactions) {
            if (ddTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployeeDdCredit) {
                SAPPayrollEmployeeTransaction sapPayrollEmployeeTransaction =
                        PayrollRunTranslator.getSAPPayrollEmployeeOrVendorTransactionFromDomainEntity(ddTransaction, null, null, null, canViewFullBankAccountNumbers);
                ddTransactionsTotal = (SpcfMoney) ddTransactionsTotal.add(PayrollRunTranslator.getDebitAmount(ddTransaction));
                sapPayrollACHDetailSet.getDdTransactions().add(sapPayrollEmployeeTransaction);
            }
        }

        sapPayrollACHDetailSet.setDdTransactionsTotal(SAPTranslator.getDoubleFromSpcfMoney(ddTransactionsTotal));
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewPayrollScreen)
    public ArrayList<SAPPayrollBillingTransactions> getRedebitTransactionsForPayroll(String sourceSystemCd,
                                                                                     @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                                                                     String payrollRunId) throws Throwable {

        ArrayList<SAPPayrollBillingTransactions> returnList = new ArrayList<SAPPayrollBillingTransactions>(1);
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            Company company =
                    Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));

            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunId);

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                        .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))
                                                                        .And(FinancialTransaction.TransactionType().AssociationType().equalTo(TransactionAssociationType.Redebit))
                                                                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created));

            DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>().Where(where));
            ArrayList<SAPPayrollBillingTransactions> payrolls = findPayrollBalancesCore(sourceSystemCd, companyId, payrollRunId, LS_UNCOLLECTED_TRANSACTIONS, false);

            for (SAPPayrollBillingTransactions payroll : payrolls) {
                if (payroll.getPayrollRunId().equals(payrollRunId)) {
                    for (FinancialTransaction financialTransaction : financialTransactions) {
                        // set settlement date once it is the same for all transactions
                        if (financialTransaction.getSettlementDate() != null && payroll.getInitiationDate() == null) {
                            payroll.setInitiationDate(SAPTranslator.getDateFromSpcfCalendar(financialTransaction.getSettlementDate()));
                        }

                        String txnId = financialTransaction.getOriginalTransaction().getId().toString();

                        for (SAPBillingTransaction sapBillingTransaction : payroll.getDdTransactions()) {
                            if (sapBillingTransaction != null && sapBillingTransaction.getFinancialTxnId().equals(txnId)) {
                                sapBillingTransaction.setFinancialReturnAmount(SAPTranslator.getDoubleFromSpcfMoney(financialTransaction.getFinancialTransactionAmount()));
                            }
                        }

                        if (payroll.getTaxTransaction() != null && payroll.getTaxTransaction().getFinancialTxnId().equals(txnId)) {
                            payroll.getTaxTransaction().setFinancialReturnAmount(SAPTranslator.getDoubleFromSpcfMoney(financialTransaction.getFinancialTransactionAmount()));
                        }

                        for (SAPBillingTransaction sapBillingTransaction : payroll.getFeeTransactions()) {
                            if (sapBillingTransaction.getFinancialTxnId().equals(txnId)) {
                                sapBillingTransaction.setFinancialReturnAmount(SAPTranslator.getDoubleFromSpcfMoney(financialTransaction.getFinancialTransactionAmount()));
                            } else if (sapBillingTransaction.getSalesTaxTxnId() != null && sapBillingTransaction.getSalesTaxTxnId().equals(txnId)) {
                                sapBillingTransaction.setSalesTaxReturnAmount(SAPTranslator.getDoubleFromSpcfMoney(financialTransaction.getFinancialTransactionAmount()));
                            }
                        }

                    }
                    returnList.add(payroll);
                }
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding redebit transactions.", sourceSystemCd, companyId, "PayrollRun", payrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnList;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.ViewSignupFraudQueue)
    public ArrayList<SAPSuspectPaycheck> checkPayrollForSuspectPaychecks(String sourceSystemCd,
                                                                         @TenantId(IdType = CompanyIdentifierType.PSID)    String companyId,
                                                                         String sourcePayrollRunId) throws Throwable {
        ArrayList<SAPSuspectPaycheck> sapSuspectPaychecks = new ArrayList<SAPSuspectPaycheck>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

            SpcfMoney largeAmount = new SpcfMoney(FraudRule.findFraudRule(company).findFraudValueByName(FraudValueType.FraudEEPaidMax).getValue());
            if (payrollRun != null) {
                for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                    if (paycheck.getNetAmount().compareTo(largeAmount) > 0) {
                        sapSuspectPaychecks.add(PayrollRunTranslator.getSapSuspectPaycheckFromDomainEntity(paycheck, "Large Amount"));
                    }
                    if (paycheck.getNetAmount().getFractionalPart() == 0) {
                        sapSuspectPaychecks.add(PayrollRunTranslator.getSapSuspectPaycheckFromDomainEntity(paycheck, "Round Amount"));
                    }
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding suspect paychecks.", sourceSystemCd, companyId, "PayrollRun", sourcePayrollRunId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return sapSuspectPaychecks;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.RecordPrefundingWire)
    public void addPrefundPayrollTransactions(String sourceSystemCd,
                                              @TenantId(IdType = CompanyIdentifierType.PSID) String companyId,
                                              String settlementTypeCd,
                                              ArrayList<SAPPayrollBillingTransactions> payrolls) throws Throwable {

        try {
            PayrollServices.beginUnitOfWork();
            for (SAPPayrollBillingTransactions payroll : payrolls) {
                ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = new ArrayList<PrefundPayrollTransactionDTO>();

                // add the dd specific transaction
                for (SAPBillingTransaction sapBillingTransaction : payroll.getDdTransactions()) {
                    transactionDTOs.add(PayrollRunTranslator.getPrefundPayrollTransactionDTO(sapBillingTransaction));
                }

                // add the fee transactions
                for (SAPBillingTransaction sapBillingTransaction : payroll.getFeeTransactions()) {
                    transactionDTOs.add(PayrollRunTranslator.getPrefundPayrollTransactionDTO(sapBillingTransaction));
                }

                ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();

                prList.add(
                        PayrollServices.financialTransactionManager.prefundPayroll(
                                SourceSystemCode.valueOf(sourceSystemCd),
                                companyId,
                                payroll.getPayrollRunId(),
                                SettlementType.valueOf(settlementTypeCd),
                                SAPTranslator.getSpcfCalendarFromDate(payroll.getInitiationDate()),
                                transactionDTOs));

                if (aeFactory.errorsOccurred(prList)) {
                    aeFactory.throwGenericException(
                            "Error creating prefunding transactions.",
                            EntityName.FinancialTransaction.toString(),
                            payroll.getPayrollRunId(),
                            prList);
                }

                PayrollServices.commitUnitOfWork();
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error creating prefunding transactions.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    public ArrayList<SAPPayrollRunAction> getAllPayrollRunActions() throws Throwable {
        ArrayList<SAPPayrollRunAction> returnVal = new ArrayList<SAPPayrollRunAction>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            DomainEntitySet<PayrollRunAction> payrollRunActionList =
                    PayrollServices.entityFinder.find(PayrollRunAction.class);

            HashMap<PayrollStatus, ArrayList<SAPActionEvent>> statusMap =
                    new HashMap<PayrollStatus, ArrayList<SAPActionEvent>>();

            // first map the status to all of its actions. also translate actions
            for (PayrollRunAction payrollRunAction : payrollRunActionList) {
                // add to existing
                if (statusMap.containsKey(payrollRunAction.getStatus())) {
                    ArrayList<SAPActionEvent> actionList = statusMap.get(payrollRunAction.getStatus());
                    actionList.add(PayrollRunTranslator.getSAPActionEventFromDomainEntity(
                            payrollRunAction.getActionEvent()));
                    statusMap.put(payrollRunAction.getStatus(), actionList);
                }
                // create new
                else {
                    ArrayList<SAPActionEvent> actionList = new ArrayList<SAPActionEvent>();
                    actionList.add(PayrollRunTranslator.getSAPActionEventFromDomainEntity(
                            payrollRunAction.getActionEvent()));
                    statusMap.put(payrollRunAction.getStatus(), actionList);
                }
            }

            // then translate statuses
            Set<PayrollStatus> keys = statusMap.keySet();
            for (PayrollStatus payrollStatus : keys) {
                returnVal.add(PayrollRunTranslator.getSAPPayrollRunActionFromDomainEntity(
                        payrollStatus, statusMap.get(payrollStatus)));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding payroll actions.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return returnVal;
    }

    @FlexMethod
    public ArrayList<SAPTransactionType> getTransactionTypeList() throws Throwable {

        ArrayList<SAPTransactionType> returnVal = new ArrayList<SAPTransactionType>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DomainEntitySet<TransactionType> transactionTypeList =
                    PayrollServices.entityFinder.findObjects(TransactionType.class);

            for (TransactionType txnType : transactionTypeList) {
                returnVal.add(PayrollRunTranslator.getSAPTransactionTypeFromDomainEntity(txnType));
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding transaction types.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnVal;
    }

    private String getTraceId(FinancialTransaction financialTransaction) {
    		String jpmcTraceId = JPMC_TRACE_ID_NOT_AVAILABLE;
		DomainEntitySet<EntryDetailRecord> entryDetailRecordSet = 
				Application.find(EntryDetailRecord.class, 
				EntryDetailRecord.MoneyMovementTransaction().equalTo(financialTransaction.getMoneyMovementTransaction())
				.And(EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.PPD))
				.And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));
		if(entryDetailRecordSet.size() == 1 
				&& entryDetailRecordSet.get(0).getJPMCTraceNumber() != null) {
			jpmcTraceId = entryDetailRecordSet.get(0).getJPMCTraceNumber();
		}
		
		return jpmcTraceId;
	}

}

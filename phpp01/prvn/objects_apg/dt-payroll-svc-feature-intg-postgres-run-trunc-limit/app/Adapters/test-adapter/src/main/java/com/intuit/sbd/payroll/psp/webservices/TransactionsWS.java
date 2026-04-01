package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.eftps.EftpsDataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.LedgerAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 8, 2008
 * Time: 2:50:16 PM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class TransactionsWS {

    @WebMethod
    public List<BigDecimal> getRandomDebitAmounts(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                  @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                  @WebParam(name = "sourceCompanyBankAccountID")String sourceCompanyBankAccountID)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        List<BigDecimal> randomDebitAmounts = new ArrayList<BigDecimal>(2);
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || sourceCompanyBankAccountID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID or Source Company BankAccount ID"
                        + " can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            DomainEntitySet<CompanyBankAccount> companyBankAccounts = CompanyBankAccount
                    .findCompanyBankAccountsIncludingExpired(company, sourceCompanyBankAccountID);
            if (companyBankAccounts == null || companyBankAccounts.size() == 0) {
                throw new RuntimeException("No BankAccount exists for the specified company and sourceCompanyBankAccountId");
            }
            // verify there are multiple cbas with the same source bank account id
            // if multiple cbas exist get the random debits for the non-Inactive one
            // if there are multiple cbas, multiple Inactive cbas with same source bank account
            //  and no non-Inactive cba, get the most recently de-activated cba
            // and return the verification transactions of it.
            CompanyBankAccount companyBankAccount = null;
            if (companyBankAccounts.size() > 1) {
                for (CompanyBankAccount cba : companyBankAccounts) {
                    if (cba.getStatusCd() != BankAccountStatus.Inactive) {
                        companyBankAccount = cba;
                        break;
                    }
                }
                if (null == companyBankAccount) {
                    companyBankAccounts = CompanyBankAccount.findDeactivatedCompanyBankAccounts(company, sourceCompanyBankAccountID);

                    CompanyBankAccount cbaToReturn = null;
                    for (CompanyBankAccount cbaToTest : companyBankAccounts) {
                        if (cbaToReturn == null || cbaToReturn.getExpirationDate().compareTo(cbaToTest.getExpirationDate()) < 0) {
                            cbaToReturn = cbaToTest;
                        }
                    }
                    companyBankAccount = cbaToReturn;
                }
            } else {
                companyBankAccount = companyBankAccounts.get(0);

            }

//            DomainEntitySet<FinancialTransaction> verificationTransactions = CompanyBankAccountBE
//                    .findVerificationTransactions(companyBankAccount);
            TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.EmployerVerificationDebit);
            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                          .Where(FinancialTransaction.Company().equalTo(company)
                                 .And(FinancialTransaction.DebitBankAccount().equalTo(companyBankAccount.getBankAccount())
                                 .And(FinancialTransaction.TransactionType().equalTo(transactionType))))
                          .OrderBy(FinancialTransaction.SettlementDate().Descending());

            DomainEntitySet<FinancialTransaction> verificationTransactions = Application.find(FinancialTransaction.class, query);

            if (null != verificationTransactions && verificationTransactions.size() > 0) {
                randomDebitAmounts.add(SpcfUtils.convertToBigDecimal(verificationTransactions.get(0).getFinancialTransactionAmount()));
                randomDebitAmounts.add(SpcfUtils.convertToBigDecimal(verificationTransactions.get(1).getFinancialTransactionAmount()));
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return randomDebitAmounts;
    }

    @WebMethod
    public void overrideRandomDebitAmounts(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                  @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                  @WebParam(name = "sourceCompanyBankAccountID")String sourceCompanyBankAccountID,
                                                  @WebParam(name = "forcedRandomAmount1") String forcedRandomAmount1,
                                                  @WebParam(name = "forcedRandomAmount2") String forcedRandomAmount2)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID"
                        + " can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            DomainEntitySet<CompanyBankAccount> companyBankAccounts = null;
            if (sourceCompanyBankAccountID != null && sourceCompanyBankAccountID.length() > 0) {
                companyBankAccounts = CompanyBankAccount.findCompanyBankAccountsIncludingExpired(company, sourceCompanyBankAccountID);
            } else {
                companyBankAccounts = findCompanyBankAccount(company, BankAccountStatus.PendingVerification);
            }

            if (companyBankAccounts == null || companyBankAccounts.size() == 0) {
                throw new RuntimeException("No BankAccount exists for the specified company and sourceCompanyBankAccountId");
            }

            // verify there are multiple cbas with the same source bank account id
            // if multiple cbas exist get the random debits for the non-Inactive one
            // if there are multiple cbas, multiple Inactive cbas with same source bank account
            //  and no non-Inactive cba, get the most recently de-activated cba
            // and return the verification transactions of it.

            CompanyBankAccount companyBankAccount = null;
            if (companyBankAccounts.size() > 1) {
                for (CompanyBankAccount cba : companyBankAccounts) {
                    if (cba.getStatusCd() == BankAccountStatus.PendingVerification) {
                        companyBankAccount = cba;
                        break;
                    }
                }
            } else {
                companyBankAccount = companyBankAccounts.get(0);
            }
            DomainEntitySet<FinancialTransaction> bvfts = companyBankAccount.getVerificationTransactions();
            PayrollServices.commitUnitOfWork();

            if (bvfts.size() == 2) {
                overrideVerificationAmount(company, bvfts.get(0).getId(), forcedRandomAmount1);
                overrideVerificationAmount(company, bvfts.get(1).getId(), forcedRandomAmount2);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static DomainEntitySet<CompanyBankAccount> findCompanyBankAccount(Company pCompany, BankAccountStatus pStatus) {
        Expression<CompanyBankAccount> query =
                new Query<CompanyBankAccount>()
                        .Where(CompanyBankAccount.Company().equalTo(pCompany)
                                .And(CompanyBankAccount.StatusCd().equalTo(pStatus)))
                        .OrderBy(CompanyBankAccount.StatusEffectiveDate().Descending());

        return Application.find(CompanyBankAccount.class, query);
    }

    private void overrideVerificationAmount(Company pCompany, SpcfUniqueId pFtId, String pNewAmount) {
        PayrollServices.beginUnitOfWork();

        FinancialTransaction ft = Application.findById(FinancialTransaction.class, pFtId);
        SpcfDecimal oldAmount = ft.getFinancialTransactionAmount();

        // update FT amount
        ft.setFinancialTransactionAmount(new SpcfMoney(pNewAmount));
        Application.save(ft);

        // update MMT amount
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();
        mmt.setMoneyMovementTransactionAmount(new SpcfMoney(pNewAmount));
        Application.save(mmt);

        // fix up the EDRs...

        // save the original trace numbers (ok if null because the FT hasn't been offloaded yet)
        com.intuit.sbd.payroll.psp.DomainEntitySet<EntryDetailRecord> edrs = mmt.getEntryDetailRecordCollection();
        String traceC;
        String traceD;
        if (edrs.get(0).getCreditDebitIndicator() == CreditDebitCode.Credit) {
            traceC = edrs.get(0).getTraceNumber();
            traceD = edrs.get(1).getTraceNumber();
        }
        else {
            traceD = edrs.get(0).getTraceNumber();
            traceC = edrs.get(1).getTraceNumber();
        }

        // recreate EDRs for the now-modified MMT
        mmt = MoneyMovementTransaction.recreateEntryDetailRecords(mmt);

        // get the new EDRs and restore the original trace numbers
        edrs = mmt.getEntryDetailRecordCollection();
        if (edrs.get(0).getCreditDebitIndicator() == CreditDebitCode.Credit) {
            edrs.get(0).setTraceNumber(traceC);
            edrs.get(1).setTraceNumber(traceD);
        }
        else {
            edrs.get(0).setTraceNumber(traceD);
            edrs.get(1).setTraceNumber(traceC);
        }

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();

        // find ledger entries (if any) for this verification transaction
        com.intuit.sbd.payroll.psp.domain.LedgerAccount acctFeeCashRev = Application.findById(com.intuit.sbd.payroll.psp.domain.LedgerAccount.class, LedgerAccountCode.FeeCashRevenue);
        com.intuit.sbd.payroll.psp.domain.LedgerAccount acctFeeIncome = Application.findById(com.intuit.sbd.payroll.psp.domain.LedgerAccount.class, LedgerAccountCode.FeeIncome);
        DomainEntitySet<LedgerBalance> ledgerEntries =
                Application.find(LedgerBalance.class,
                                 LedgerBalance.Company().equalTo(Application.refresh(pCompany))
                                 .And(LedgerBalance.LedgerAccount().in(new com.intuit.sbd.payroll.psp.domain.LedgerAccount[]{acctFeeCashRev, acctFeeIncome})
                                 .And(LedgerBalance.BalanceAmount().equalTo(new SpcfMoney(oldAmount)))));

        // update their amounts (note: there might not be any to update)
        for (LedgerBalance ledgerBalance : ledgerEntries) {
            ledgerBalance.setBalanceAmount(new SpcfMoney(pNewAmount));
            Application.save(ledgerBalance);
        }

        PayrollServices.commitUnitOfWork();
    }

    @WebMethod
    public void overrideTaxAmounts(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                  @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                  @WebParam(name = "sourceBatchID")String sourceBatchID,
                                  @WebParam(name = "forcedTaxAmount") String forcedTaxAmount)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            DomainEntitySet<FinancialTransaction> finTxs = null;
            Criterion<FinancialTransaction> where;
            if (sourceBatchID != null) {
                PayrollRun payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourceBatchID");
                }
                where = FinancialTransaction.PayrollRun().equalTo(payrollRun);
            }
            else {
                where = FinancialTransaction.Company().equalTo(company);
            }

            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.ServiceSalesAndUseTax);

            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                          .Where(where.And(FinancialTransaction.TransactionType().equalTo(txnType)))
                          .OrderBy(FinancialTransaction.TransactionType(), FinancialTransaction.CreatedDate());

            finTxs = Application.find(FinancialTransaction.class, query);

            PayrollServices.commitUnitOfWork();

            overrideTaxAmount(company, finTxs.get(0), forcedTaxAmount);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    private void overrideTaxAmount(Company pCompany, FinancialTransaction pFt, String pNewAmount) {
        SpcfUniqueId pFtId = pFt.getId();
        PayrollServices.beginUnitOfWork();

        FinancialTransaction ft = Application.findById(FinancialTransaction.class, pFtId);
        SpcfDecimal oldAmount = ft.getFinancialTransactionAmount();

        // update FT amount
        ft.setFinancialTransactionAmount(new SpcfMoney(pNewAmount));
        Application.save(ft);

        // update MMT amount
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();
        mmt.setMoneyMovementTransactionAmount(new SpcfMoney(pNewAmount));
        Application.save(mmt);

        // fix up the EDRs...

        // save the original trace numbers (ok if null because the FT hasn't been offloaded yet)
        com.intuit.sbd.payroll.psp.DomainEntitySet<EntryDetailRecord> edrs = mmt.getEntryDetailRecordCollection();
        String traceC;
        String traceD;
        if (edrs.get(0).getCreditDebitIndicator() == CreditDebitCode.Credit) {
            traceC = edrs.get(0).getTraceNumber();
            traceD = edrs.get(1).getTraceNumber();
        }
        else {
            traceD = edrs.get(0).getTraceNumber();
            traceC = edrs.get(1).getTraceNumber();
        }

        // recreate EDRs for the now-modified MMT
        mmt = MoneyMovementTransaction.recreateEntryDetailRecords(mmt);

        // get the new EDRs and restore the original trace numbers
        edrs = mmt.getEntryDetailRecordCollection();
        if (edrs.get(0).getCreditDebitIndicator() == CreditDebitCode.Credit) {
            edrs.get(0).setTraceNumber(traceC);
            edrs.get(1).setTraceNumber(traceD);
        }
        else {
            edrs.get(0).setTraceNumber(traceD);
            edrs.get(1).setTraceNumber(traceC);
        }

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();

        // find ledger entries (if any) for this verification transaction
        com.intuit.sbd.payroll.psp.domain.LedgerAccount acctSalesAndUseTax = Application.findById(com.intuit.sbd.payroll.psp.domain.LedgerAccount.class, LedgerAccountCode.SalesAndUseTax);
        DomainEntitySet<LedgerBalance> ledgerEntries =
                Application.find(LedgerBalance.class,
                                 LedgerBalance.Company().equalTo(Application.refresh(pCompany))
                                 .And(LedgerBalance.LedgerAccount().in(new com.intuit.sbd.payroll.psp.domain.LedgerAccount[]{acctSalesAndUseTax})
                                 .And(LedgerBalance.BalanceAmount().equalTo(new SpcfMoney(oldAmount)))));

        // update their amounts (note: there might not be any to update)
        for (LedgerBalance ledgerBalance : ledgerEntries) {
            ledgerBalance.setBalanceAmount(new SpcfMoney(pNewAmount));
            Application.save(ledgerBalance);
        }

        // update billing details
        pFt = Application.findById(FinancialTransaction.class, pFt.getId());
        DomainEntitySet<BillingDetail> billingDetails = pFt.getPayrollRun().getBillingDetailCollection();
        for (BillingDetail billingDetail:billingDetails) {
            billingDetail.setItemTotal(new SpcfMoney(billingDetail.getItemTotal().add(new SpcfMoney(pNewAmount)).subtract(billingDetail.getTaxAmount())));
            billingDetail.setTaxAmount(new SpcfMoney(pNewAmount));

            Application.save(billingDetail);
        }
        PayrollServices.commitUnitOfWork();
    }

    @WebMethod
    public List<BigDecimal> getRandomDebitAmountsByBankInfo(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                            @WebParam(name = "fein")String fein,
                                                            @WebParam(name = "bankAccount")BankAccountWSDTO bankAccountWSDTO)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        List<BigDecimal> randomDebitAmounts = new ArrayList<BigDecimal>(2);
        try {

            if (sourceSystemCD == null || fein == null || bankAccountWSDTO.accountNumber == null ||
                    bankAccountWSDTO.routingNumber == null || bankAccountWSDTO.bankAccountType == null) {
                throw new RuntimeException("Any of Source System Code, FEIN, Bank Account Number, Bank Routing Number, "
                        + "or Bank Account Type can not be null");
            }

            PayrollServices.beginUnitOfWork();

            Company company = Company.findActiveCompany(SourceSystemCode.valueOf(sourceSystemCD), fein);
            if (company == null) {
                throw new RuntimeException("Invalid FEIN");
            }
            if (!company.getSourceSystemCd().equals(SourceSystemCode.valueOf(sourceSystemCD))) {
                throw new RuntimeException("Invalid SourceSystemCode and FEIN Relationship");
            }

            BankAccountType pspBankAccountType;
            if (bankAccountWSDTO.bankAccountType.toUpperCase().equals("CHECKING")) {
                pspBankAccountType = BankAccountType.Checking;
            } else {
                pspBankAccountType = BankAccountType.Savings;
            }

            DomainEntitySet<CompanyBankAccount> companyBankAccounts = CompanyBankAccount
                    .findCompanyBankAccounts(company);
            CompanyBankAccount companyBankAccount = null;
            for (Iterator<CompanyBankAccount> iter = companyBankAccounts.iterator(); iter.hasNext();) {
                companyBankAccount = iter.next();
                BankAccount bankaccount = companyBankAccount.getBankAccount();
                if (bankaccount.getAccountNumber().equals(bankAccountWSDTO.accountNumber) &&
                        bankaccount.getRoutingNumber().equals(bankAccountWSDTO.routingNumber) &&
                        bankaccount.getAccountTypeCd().equals(pspBankAccountType)) {

                    break;
                } else {
                    companyBankAccount = null;
                }
            }

            if (companyBankAccount == null) {
                throw new RuntimeException("No BankAccount exists for the specified company and Bank Account Info");
            }
            DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount
                    .getVerificationTransactions();

            for (FinancialTransaction currFinTxn : verificationTransactions) {
                randomDebitAmounts.add(SpcfUtils.convertToBigDecimal(currFinTxn.getFinancialTransactionAmount()));
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return randomDebitAmounts;
    }

    @WebMethod
    @WebResult(name = "TaxPaymentGuid")
    public String getPaymentTransactionGuid(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                            @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                            @WebParam(name = "paymentTemplate") String pPaymentTemplate,
                                            @WebParam(name = "paycheckDate") String pPaycheckDate,
                                            @WebParam(name = "paymentStatus") String pPaymentStatus) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code or Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            if (pPaymentTemplate == null) {
                throw new RuntimeException("PaymentTemplate cannot be null");
            }
            DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.PaymentTemplateCd().equalTo(pPaymentTemplate));
            if (paymentTemplates.size() == 0) {
                throw new RuntimeException("No PaymentTemplate found for code: " + pPaymentTemplate);
            }
            PaymentTemplate paymentTemplate = paymentTemplates.get(0);

            //YYYYMMDD
            if (pPaycheckDate == null) {
                throw new RuntimeException("Null paycheckDate");
            }
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            Date date = null;
            try { date = df.parse(pPaycheckDate); }
            catch (ParseException pe) {
                throw new RuntimeException("could not parse paycheckDate: " + pPaycheckDate);
            }
            SpcfCalendar paycheckDate = SpcfCalendar.createInstance(date.getTime());

            if (pPaymentStatus == null) {
                throw new RuntimeException("Null paymentStatus");
            }

            TaxPaymentStatus taxPaymentStatus = null;
            try { taxPaymentStatus = TaxPaymentStatus.valueOf(pPaymentStatus); }
            catch (IllegalArgumentException iae) {
                throw new RuntimeException("Invalid paymentStatus: " + pPaymentStatus + " - must be one of: " + TaxPaymentStatus.values());
            }

            DomainEntitySet<MoneyMovementTransaction> paymentMMTs =
                    MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplate(paymentTemplate).setPaycheckDate(paycheckDate).setTaxPaymentStatuses(taxPaymentStatus).find()
                    .sort(MoneyMovementTransaction.InitiationDate());

            return paymentMMTs.size() == 1 ? paymentMMTs.get(0).getId().toString() : null;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    @WebResult(name = "TaxPayment")
    public Collection<TaxPaymentMoneyMovementTransactionWSDTO> getPaymentTransactions(
                                                                @WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                @WebParam(name = "paymentTemplate") String pPaymentTemplate,
                                                                @WebParam(name = "paycheckDate") String pPaycheckDate) {

        ArrayList<TaxPaymentMoneyMovementTransactionWSDTO> paymentWSDTOs = new ArrayList<TaxPaymentMoneyMovementTransactionWSDTO>();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code or Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            if (pPaymentTemplate == null) {
                throw new RuntimeException("PaymentTemplate cannot be null");
            }
            DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.PaymentTemplateCd().equalTo(pPaymentTemplate));
            if (paymentTemplates.size() == 0) {
                throw new RuntimeException("No PaymentTemplate found for code: " + pPaymentTemplate);
            }
            PaymentTemplate paymentTemplate = paymentTemplates.get(0);

            //YYYYMMDD
            if (pPaycheckDate == null) {
                throw new RuntimeException("Null paycheckDate");
            }
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            Date date = null;
            try { date = df.parse(pPaycheckDate); }
            catch (ParseException pe) {
                throw new RuntimeException("could not parse paycheckDate: " + pPaycheckDate);
            }
            SpcfCalendar paycheckDate = SpcfCalendar.createInstance(date.getTime());

            DomainEntitySet<MoneyMovementTransaction> paymentMMTs =
                    MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplate(paymentTemplate).setPaycheckDate(paycheckDate).find()
                                            .sort(MoneyMovementTransaction.TaxPaymentStatus(), MoneyMovementTransaction.InitiationDate());
            for (MoneyMovementTransaction paymentMMT : paymentMMTs) {
                TaxPaymentMoneyMovementTransactionWSDTO mmtDTO = createTaxPaymentMoneyMovementTransactionDTO(paymentMMT);
                paymentWSDTOs.add(mmtDTO);
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        // sort by payment template name, tax payment status
        return paymentWSDTOs;
    }

    @WebMethod
    public Collection<TransactionWSDTO> getFinancialTransactions(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                 @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                 @WebParam(name = "sourceBatchID")String sourceBatchID,
                                                                 @WebParam(name = "addPaycheckStatus")boolean addPaycheckStatus,
                                                                 @WebParam(name = "includeActions")Boolean includeActions,
                                                                 @WebParam(name = "returnZeroEmployerFeeDebits") Boolean returnZeroEmployerFeeDebits)
            throws Exception {
        if(includeActions == null) {
            includeActions = false;
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code or Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            DomainEntitySet<FinancialTransaction> finTxs = null;
            Criterion<FinancialTransaction> where;
            if (sourceBatchID != null) {
                PayrollRun payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourceBatchID");
                }
                where = FinancialTransaction.PayrollRun().equalTo(payrollRun);
            }
            else {
                where = FinancialTransaction.Company().equalTo(company);
            }
            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                          .Where(where)
                          .OrderBy(FinancialTransaction.TransactionType(), FinancialTransaction.CreatedDate());

            finTxs = Application.find(FinancialTransaction.class, query);

            if(returnZeroEmployerFeeDebits == null || !returnZeroEmployerFeeDebits) {
                DomainEntitySet<FinancialTransaction> transactionsToRemove = finTxs.find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                             .And(FinancialTransaction.FinancialTransactionAmount().equalTo(SpcfMoney.ZERO)));
                for (FinancialTransaction financialTransaction : transactionsToRemove) {
                    finTxs.remove(financialTransaction);
                }
            }

            Collection<TransactionWSDTO> txDTOs = buildTransactionResponse(finTxs, addPaycheckStatus, includeActions);
            PayrollServices.commitUnitOfWork();
            return txDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> getFinancialTransactionsAndActions(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                           @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                           @WebParam(name = "sourceBatchID")String sourceBatchID)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Source System Code or Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            DomainEntitySet<FinancialTransaction> finTxs;
            Criterion<FinancialTransaction> where;
            if (sourceBatchID != null) {
                PayrollRun payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourceBatchID");
                }
                where = FinancialTransaction.PayrollRun().equalTo(payrollRun);
            }
            else {
                where = FinancialTransaction.Company().equalTo(company);
            }
            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                          .Where(where)
                          .OrderBy(FinancialTransaction.TransactionType(), FinancialTransaction.CreatedDate());

            finTxs = Application.find(FinancialTransaction.class, query);

            Collection<TransactionWSDTO> txDTOs = buildTransactionsResponse(finTxs);
            PayrollServices.commitUnitOfWork();
            return txDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> getFinancialTransactionsByType(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                       @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                       @WebParam(name = "sourceBatchID")String sourceBatchID,
                                                                       @WebParam(name = "transactionTypeCd")String transactionTypeCode,
                                                                       @WebParam(name = "transactionStateCd")String transactionStateCode)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null || transactionTypeCode == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID or transactionTypeCd can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }


            PayrollRun payrollRun = null;
            if (StringUtils.isNotEmpty(sourceBatchID)) {
                payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourceBatchID: " + sourceBatchID);
                }
            }

            TransactionType transactionType = PayrollServices.entityFinder.findById(TransactionType.class, TransactionTypeCode.valueOf(transactionTypeCode));
            if (transactionType == null) {
                throw new RuntimeException("Invalid transactionType: " + transactionType);
            }

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company).And(FinancialTransaction.TransactionType().equalTo(transactionType));
            if (payrollRun != null) {
                where = where.And(FinancialTransaction.PayrollRun().equalTo(payrollRun));
            }

            if (transactionStateCode != null) {
                TransactionState transactionState = PayrollServices.entityFinder.findById(TransactionState.class, TransactionStateCode.valueOf(transactionStateCode));
                if (transactionState == null) {
                    throw new RuntimeException("Invalid transactionState Code: " + transactionStateCode);
                }

                where = where.And(FinancialTransaction.CurrentTransactionState().equalTo(transactionState));
            }

            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                           .Where(where)
                           .OrderBy(FinancialTransaction.TransactionType(), FinancialTransaction.FinancialTransactionAmount());

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, query);

            Collection<TransactionWSDTO> txDTOs = buildTransactionResponse(finTxs, false);
            PayrollServices.commitUnitOfWork();
            return txDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<PayrollRunWSDTO> getPayrollRuns(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                      @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                      @WebParam(name = "paycheckDate") String paycheckDate)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code or Source Company ID"
                        + " can not be null");
            }

            SpcfCalendar spcfPaycheckDate = null;
            if(paycheckDate != null) {
                if(paycheckDate.length() != 8) {
                    throw new RuntimeException("Paycheck date format should be 'YYYYMMDD'");
                }
                // this date is created in UTC
                spcfPaycheckDate = SpcfCalendar.parse("yyyyMMdd", paycheckDate);
                // to local will subtract 7 or 8 hours making it 1 day before the parsed date, so we'll add a day and clear time
                spcfPaycheckDate = spcfPaycheckDate.toLocal();
                spcfPaycheckDate.addDays(1);
                CalendarUtils.clearTime(spcfPaycheckDate);
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            Criterion<PayrollRun> payrollRunExpression = PayrollRun.Company().equalTo(company);
            if(spcfPaycheckDate != null) {
                payrollRunExpression = payrollRunExpression.And(PayrollRun.PaycheckDate().equalTo(spcfPaycheckDate));
            }

            Expression<PayrollRun> query =
                    new Query<PayrollRun>()
                            .Where(payrollRunExpression)
                            .OrderBy((PayrollRun.PayrollRunDate().Descending()));

            DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, query);
            Collection<PayrollRunWSDTO> wsDTOs = buildPayrollRunResponse(payrollRuns);
            PayrollServices.commitUnitOfWork();
            return wsDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<PaycheckWSDTO> getPaychecks(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                  @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                  @WebParam(name = "sourceBatchID")String sourceBatchID,
                                                  @WebParam(name = "shouldIncludeTxnData")Boolean shouldIncludeTxnData,
                                                  @WebParam(name = "shouldIncludeAssistedData")Boolean shouldIncludeAssistedData,
                                                  @WebParam(name = "shouldIncludeTokens")Boolean shouldIncludeTokens)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code or Source Company ID " +
                        "can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            if (shouldIncludeTxnData == null) {
                shouldIncludeTxnData = false;
            }
            if (shouldIncludeAssistedData == null) {
                shouldIncludeAssistedData = false;
            }
            if(shouldIncludeTokens == null) {
                shouldIncludeTokens = false;
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
            Expression<Paycheck> query = null;
            if (payrollRun != null) {
                query = new Query<Paycheck>()
                                .Where(Paycheck.PayrollRun().equalTo(payrollRun))
                                .OrderBy((Paycheck.SourcePaycheckId()));
            } else {
                query = new Query<Paycheck>()
                                .Where(Paycheck.PayrollRun().Company().equalTo(company))
                                .OrderBy((Paycheck.SourcePaycheckId()));
            }

            DomainEntitySet<Paycheck> paycheks = Application.find(Paycheck.class, query);
            Collection<PaycheckWSDTO> paycheckDTOs = buildPaycheckResponse(paycheks, shouldIncludeTxnData, shouldIncludeAssistedData, shouldIncludeTokens);
            PayrollServices.commitUnitOfWork();
            return paycheckDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @WebMethod
    public Collection<String> getPaycheckSplitTranactionIds(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                  @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                  @WebParam(name = "sourcePaycheckID")String sourcePaycheckID)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || sourcePaycheckID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID or sourcePaycheckID " +
                        "can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            Paycheck paycheck = Paycheck.findPaycheck(company, sourcePaycheckID);
            if (paycheck == null) {
                throw new RuntimeException("Invalid sourcePaycheckID");
            }

            Expression<PaycheckSplit> query =
                    new Query<PaycheckSplit>()
                            .Where(PaycheckSplit.Paycheck().equalTo(paycheck))
                            .OrderBy((PaycheckSplit.PaycheckSplitAmount()));

            DomainEntitySet<PaycheckSplit> paychekSplits = Application.find(PaycheckSplit.class, query);
            Collection<String> ddTransactionIds = new ArrayList<String>(paychekSplits.size());
            for (PaycheckSplit paycheckSplit : paychekSplits) {
                ddTransactionIds.add(paycheckSplit.getSourceDdTxnId());
            }
            PayrollServices.commitUnitOfWork();
            return ddTransactionIds;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @WebMethod
    public Collection<BankReturnWSDTO> getBankReturns(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                      @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                      @WebParam(name = "sourceBatchID")String sourceBatchID,
                                                      @WebParam(name = "sourceBillPaymentID")String sourceBillPaymentID)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || (sourceBatchID == null && sourceBillPaymentID == null)) {
                throw new RuntimeException("Source System Code, Source Company ID cannot be null and Source PayrollRun ID or Source Bill Payment ID must be specified");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = null;
            if(sourceBatchID != null) {
                payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourceBatchID");
                }
            }
            else if(sourceBillPaymentID != null) {
                BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentID);
                if (billPayment == null) {
                    throw new RuntimeException("Invalid sourceBillPaymentID");
                }

                payrollRun = billPayment.getPayrollRun();
            }

            com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> finTxs = payrollRun.getFinancialTransactionCollection();

            List<MoneyMovementTransaction> mmTxs = new ArrayList<MoneyMovementTransaction>();
            if (!finTxs.isEmpty()) {
                for (FinancialTransaction financialTransaction : finTxs) {
                    if (null != financialTransaction.getMoneyMovementTransaction()) {
                        mmTxs.add(financialTransaction.getMoneyMovementTransaction());
                    }
                }
            }
            Collection<BankReturnWSDTO> bankReturnDTOs = new ArrayList<BankReturnWSDTO>();
            if (mmTxs.toArray().length > 0) {
                MoneyMovementTransaction[] mmTxsArray = new MoneyMovementTransaction[mmTxs.size()];
                for (int i = 0; i < mmTxs.size(); i++) {
                    mmTxsArray[i] = mmTxs.get(i);
                }
                Expression<TransactionReturn> query =
                    new Query<TransactionReturn>()
                            .Where(TransactionReturn.MoneyMovementTransaction().in(mmTxsArray))
                            .OrderBy((TransactionReturn.CreatedDate()));

                DomainEntitySet<TransactionReturn> txRetruns = Application.find(TransactionReturn.class, query);
                bankReturnDTOs = buildBankReturnResponse(txRetruns);
            }
            PayrollServices.commitUnitOfWork();
            return bankReturnDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<BillingDetailWSDTO> getBillingDetails(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                      @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                      @WebParam(name = "sourceBatchID")String sourceBatchID)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || sourceBatchID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID or source PayrollRun ID " +
                        "can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourceBatchID);
            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourceBatchID");
            }

            Collection<BillingDetailWSDTO> billingDetailWSDTOs = buildBillingDetailResponse(payrollRun);

            PayrollServices.commitUnitOfWork();
            return billingDetailWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> updateTransactionDates(
            @WebParam(name = "transactions")List<TransactionWSDTO> transactions) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (transactions == null) {
                throw new RuntimeException("transactions can not be null");
            }
            Map<String, Date> transactionDates = new HashMap<String, Date>(transactions.size());


            SpcfUniqueId[] txIds = new SpcfUniqueId[transactions.size()];
            int i = 0;
            for (TransactionWSDTO transaction : transactions) {
                txIds[i] = SpcfUniqueId.createInstance(transaction.id);
                transactionDates.put(transaction.id, transaction.settlementDate);
                i++;
            }

            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                            .Where(FinancialTransaction.Id().in(txIds))
                            .OrderBy(FinancialTransaction.CreatedDate());

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, query);
            SpcfCalendar settlementDate = null;
            for (FinancialTransaction financialTransaction : finTxs) {
                settlementDate = getSpcfCalendar(transactionDates.get(financialTransaction.getId().toString()));
                financialTransaction.setSettlementDate(settlementDate);
                SpcfCalendar initDate = settlementDate.copy();
                TransactionTypeCode strTxnType = financialTransaction.getTransactionType().getTransactionTypeCd();
                if (strTxnType.equals(TransactionTypeCode.EmployeeDdCredit)) {
                    CalendarUtils.addBusinessDays(initDate, -2);
                } else {
                    CalendarUtils.addBusinessDays(initDate, -1);
                }
                MoneyMovementTransaction mmTxn = financialTransaction.getMoneyMovementTransaction();
                if(mmTxn != null){
                    mmTxn.setInitiationDate(initDate);
                    DomainEntitySet<EntryDetailRecord> entryDetailList = mmTxn.getEntryDetailRecordCollection();
                    for(EntryDetailRecord entryDetailRecord : entryDetailList){
                        entryDetailRecord.setInitiationDate(initDate);
                        entryDetailRecord.setSettlementDate(settlementDate);
                        Application.save(entryDetailRecord);
                    }
                }

                Application.save(financialTransaction);
            }

            Collection<TransactionWSDTO> txDTOs = buildTransactionResponse(finTxs, false);
            PayrollServices.commitUnitOfWork();
            return txDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> refundTransaction(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                          @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                          @WebParam(name = "transactionType")String transactionType,
                                                          @WebParam(name = "transactionState")String transactionState,
                                                          @WebParam(name = "transactionId")String transactionId,
                                                          @WebParam(name = "settlementType")String settlementType,
                                                          @WebParam(name = "refundAmount")String refundAmount,
                                                          @WebParam(name = "refundDate")String refundDate,
                                                          @WebParam(name = "process")String process)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || transactionType == null || transactionState == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, transaction type " +
                        "or transaction state can not be null");
            }

            if (refundDate != null && refundDate.length() != 8) {
                throw new RuntimeException(
                        "Invalid refund date format" + refundDate + ".  Correct format: yyyyMMdd");
            }

            if ((!transactionState.equals(TransactionStateCode.Executed.toString())) &&
                    (!transactionState.equals(TransactionStateCode.Completed.toString())) &&
                    (!transactionState.equals(TransactionStateCode.Returned.toString()))) {
                throw new RuntimeException("Transaction state must be either Executed or Completed");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }
            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.valueOf(transactionType));
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.valueOf(transactionState));

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType)
                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState)));
            if(transactionId != null) {
                where = where.And(FinancialTransaction.Id().equalTo(SpcfUniqueId.createInstance(transactionId)));
            }

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

            if(transactionId != null && finTxs.size() == 0) {
                throw  new RuntimeException("Transaction with id: " + transactionId + " not found.");
            }

            DomainEntitySet<FinancialTransaction> refundTxns = new DomainEntitySet<FinancialTransaction>();

            ProcessResult result = null;
            FinancialTransaction refundTransaction = null;
            TransactionResponse refundResponse = null;
            if (!process.equalsIgnoreCase("ERTransactionRefund")) {
                for (FinancialTransaction financialTransaction : finTxs) {
                    RefundDTO refundDTO = new RefundDTO();
                    if (financialTransaction.getPayrollRun() != null) {
                        refundDTO.setSourcePayrollRunId(financialTransaction.getPayrollRun().getSourcePayRunId());
                    }
                    BankAccount bankAccount = null;
                    if (financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Company) {
                        bankAccount = financialTransaction.getCreditBankAccount();
                    }
                    else {
                        bankAccount = financialTransaction.getDebitBankAccount();
                    }
                    if (null == settlementType) {
                        refundDTO.setSettlementType(SettlementTypeDTO.valueOf(financialTransaction.getSettlementTypeCd().toString()));
                    }
                    else {
                        refundDTO.setSettlementType(SettlementTypeDTO.valueOf(settlementType));
                    }
                    if (null == refundAmount) {
                        refundDTO.setFinancialTxAmt(financialTransaction.getFinancialTransactionAmount());
                    }
                    else {
                        refundDTO.setFinancialTxAmt(new SpcfMoney(SpcfMoney.createInstance(refundAmount)));
                    }
                    if (null == refundDate) {
                        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
                    }
                    else {
                        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                        dateFormat.setPattern("yyyyMMdd");
                        SpcfCalendar parsedRunDate = dateFormat.parse(refundDate);
                        date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
                        refundDTO.setTxDate(new DateDTO(date));
                    }
                    if (process.equalsIgnoreCase("DDRefund")) {
                        result = PayrollServices.financialTransactionManager.addRefundTransaction(
                                company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO);
                    }
                    else if (process.equalsIgnoreCase("DDERRefund")) {
                        result = PayrollServices.financialTransactionManager.addEmployerReturnRefundTransaction(
                                company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO);

                    }
                    else if (process.equalsIgnoreCase("DDEERefund")) {
                        result = PayrollServices.financialTransactionManager.addEmployeeReturnRefundTransaction(
                                company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO);
                    }
                    if (result.isSuccess()) {
                        if (result.getResult() instanceof FinancialTransaction) {
                            refundTransaction = (FinancialTransaction) result.getResult();
                        }
                        else {
                            refundResponse = (TransactionResponse) result.getResult();
                            refundTransaction = refundResponse.getFinancialTransactionStates(
                            ).get(0).getFinancialTransaction();
                        }
                        refundTxns.add(refundTransaction);
                    }
                    else {
                        throw new RuntimeException(result.getMessages().get(0).toString());
                    }
                }
            }
            else {
                for (FinancialTransaction financialTransaction : finTxs) {
                    ERRefundDTO refundDTO = new ERRefundDTO();
//                    if (financialTransaction.getPayrollRun() != null) {
                    refundDTO.setFinancialTxId(financialTransaction.getId().toString());
//                    }
                    BankAccount bankAccount = null;
                    if (financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Company) {
                        bankAccount = financialTransaction.getCreditBankAccount();
                    }
                    else {
                        bankAccount = financialTransaction.getDebitBankAccount();
                    }
                    if (null == settlementType) {
                        refundDTO.setSettlementType(SettlementTypeDTO.valueOf(financialTransaction.getSettlementTypeCd().toString()));
                    }
                    else {
                        refundDTO.setSettlementType(SettlementTypeDTO.valueOf(settlementType));
                    }
                    if (null == refundAmount) {
                        refundDTO.setFinancialTxAmt(financialTransaction.getFinancialTransactionAmount());
                    }
                    else {
                        refundDTO.setFinancialTxAmt(new SpcfMoney(SpcfMoney.createInstance(refundAmount)));
                    }
                    if (null == refundDate) {
                        refundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));
                    }
                    else {
                        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                        dateFormat.setPattern("yyyyMMdd");
                        SpcfCalendar parsedRunDate = dateFormat.parse(refundDate);
                        date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
                        refundDTO.setTxDate(new DateDTO(date));
                    }

                    result = PayrollServices.financialTransactionManager.refundEmployerTransaction(
                            company.getSourceSystemCd(), company.getSourceCompanyId(), refundDTO);
                    if (result.isSuccess()) {
                        if (result.getResult() instanceof FinancialTransaction) {
                            refundTransaction = (FinancialTransaction) result.getResult();
                        }
                        else {
                            refundResponse = (TransactionResponse) result.getResult();
                            refundTransaction = refundResponse.getFinancialTransactionStates(
                            ).get(0).getFinancialTransaction();
                        }
                        refundTxns.add(refundTransaction);
                    }
                    else {
                        throw new RuntimeException(result.getMessages().get(0).toString());
                    }
                }

            }

            Collection<TransactionWSDTO> transactionWSDTOs = null;
            if (refundTxns.size() > 0) {
                transactionWSDTOs = buildTransactionResponse(refundTxns, false);
            }
            PayrollServices.commitUnitOfWork();
            return transactionWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> cancelTransaction(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                          @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                          @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                                          @WebParam(name = "transactionType")String transactionType,
                                                          @WebParam(name = "transactionState")String transactionState,
                                                          @WebParam(name = "sourceDdTxnId")String sourceDdTxnId)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || transactionType == null || transactionState == null || sourcePayrollRunID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, Source PayrollRun ID, " +
                        "transaction type or transaction state can not be null");
            }

            if (!transactionState.equals(TransactionStateCode.Created.toString())) {
                throw new RuntimeException("Transaction state must be Created");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.valueOf(transactionType));
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.valueOf(transactionState));

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType)
                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState)
                                                    .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))));

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);
            SpcfUniqueId[] txIds = new SpcfUniqueId[finTxs.size()];
            int i = 0;
            ProcessResult result = null;
            if (TransactionType.isEmployerTransactionType(TransactionTypeCode.valueOf(transactionType))) {
                for (FinancialTransaction financialTransaction : finTxs) {
                    result = PayrollServices.financialTransactionManager.cancelTransaction(
                            company.getSourceSystemCd(), company.getSourceCompanyId(),
                            financialTransaction.getId().toString());
                    if (result.isSuccess()) {
                        txIds[i++] = financialTransaction.getId();
                    } else {
                        throw new RuntimeException(result.getMessages().get(0).toString());
                    }

                }
            } else if (TransactionType.isEmployeeTransactionType(TransactionTypeCode.valueOf(transactionType))) {
                TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
                dto.setAgentCancel(true);

                //dto.setServiceCd(ServiceCode.DirectDeposit);
                dto.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
                List<String> sourcePaycheckIds = new ArrayList<String>();

                for (FinancialTransaction financialTransaction : finTxs) {
                    if (sourceDdTxnId == null || financialTransaction.getPaycheckSplit().getSourceDdTxnId().equals(sourceDdTxnId)) {
                        txIds[i++] = financialTransaction.getId();
                        if (null != financialTransaction.getPaycheckSplit()) {
                            String paycheckId = financialTransaction.getPaycheckSplit().getPaycheck().getSourcePaycheckId();
                            if(!sourcePaycheckIds.contains(paycheckId)){
                                sourcePaycheckIds.add(paycheckId);
                            }
                        }
                    }
                }

                dto.setSourcePaycheckIdList(sourcePaycheckIds);

                //Set Current Principal as agent
                //Add user
                PspPrincipal principal = Application.getCurrentPrincipal();
                AuthRole foundRole = AuthRole.findRole("DesktopCareManager");
                ProcessResult addResult = PayrollServices.userManager.addUser("TestAgent", Arrays.asList(foundRole.getRoleId()),"Test","Agent");
                AuthUser user = (AuthUser) addResult.getResult();
                //Set PSP Principal for the User to make current principal is agent
                PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName()));

                result = PayrollServices.payrollManager.cancelEmployeeTransaction(
                        company.getSourceSystemCd(), company.getSourceCompanyId(), dto);

                // Remove Agent from Principal
                PayrollServices.userManager.deleteUser("TestAgent");
                PayrollServices.setCurrentPrincipal(principal);
                if (!result.isSuccess()) {
                    throw new RuntimeException(result.getMessages().get(0).toString());
                }
            }
            Collection<TransactionWSDTO> transactionWSDTOs = null;
            if (txIds.length > 0) {
                txnState = Application.findById(TransactionState.class, TransactionStateCode.Cancelled);
                finTxs = Application.find(FinancialTransaction.class,
                                          FinancialTransaction.CurrentTransactionState().equalTo(txnState)
                                          .And(FinancialTransaction.Id().in(txIds)));

                transactionWSDTOs = buildTransactionResponse(finTxs, false);
            }
            PayrollServices.commitUnitOfWork();
            return transactionWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<BankReturnWSDTO> returnTransaction(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                         @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                         @WebParam(name = "transactionType")String transactionType,
                                                         @WebParam(name = "transactionState")String transactionState,
                                                         @WebParam(name = "bankReturnCD")String bankReturnCD,
                                                         @WebParam(name = "bankReturnData")String bankReturnData)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            // need auto-flush enabled to handle returns for different payrolls for same company (PSRV001219)
            Application.setDefaultHibernateFlushMode(FlushMode.AUTO);

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || transactionType == null || transactionState == null || bankReturnCD == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, transaction type, " +
                        "transaction state or bank return code can not be null");
            }

            if (bankReturnCD.startsWith("C") && bankReturnData == null) {
                throw new RuntimeException("BankReturnData is required for NOC bank return");
            }

            if (!transactionState.equals(TransactionStateCode.Executed.toString())) {
                throw new RuntimeException("Transaction state must be in Executed");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }
            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.valueOf(transactionType));
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.valueOf(transactionState));

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType)
                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState)));

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);
            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf(sourceSystemCD);
            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

            transactionReturnBatch = Application.save(transactionReturnBatch);
            DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
            for (FinancialTransaction financialTx : finTxs) {
                if (financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed) {
                    TransactionReturn transactionReturn = new TransactionReturn();

                    transactionReturn.setBankReturnCd(bankReturnCD);
                    transactionReturn.setBankReturnDescription(bankReturnData);
                    transactionReturn.setReturnBatch(transactionReturnBatch);
                    transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                    transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

                    if (financialTx != null) {
                        transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                        transactionReturn.setCompany(financialTx.getCompany());                        
                    }

                    transactionReturn = Application.save(transactionReturn);
                    transactionReturns.add(transactionReturn);
                }
            }
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            PayrollServices.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch

            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(transactionReturnBatch.getId());

            PayrollServices.beginUnitOfWork();
            Collection<BankReturnWSDTO> bankReturnWSDTOs = new ArrayList<BankReturnWSDTO>();
            for (TransactionReturn transactionReturn : TransactionReturnBatch.getTransactionReturns(transactionReturnBatch.getId())) {
                bankReturnWSDTOs.add(buildBankReturnWSDTO(transactionReturn));
            }
            PayrollServices.rollbackUnitOfWork();
            return bankReturnWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }
    }

    @WebMethod
    public void createReversals(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                @WebParam(name = "settlementType")String settlementType,
                                @WebParam(name = "ddTxnsToReverse")List<String> txnsToReverse,
                                @WebParam(name = "shouldChargeFee")String shouldChargeFee,
                                @WebParam(name = "intuitInitiated")String isIntuitInitiated)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || settlementType == null || sourcePayrollRunID == null || shouldChargeFee == null || isIntuitInitiated == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, source PayrollRun ID, " +
                        "settlementType, shouldChargeFee, isIntuitInitiated can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            SettlementTypeDTO settlementTypeDTO = SettlementTypeDTO.valueOf(settlementType);

            if (settlementTypeDTO == null) {
                throw new RuntimeException("Invalid settlementTypeDTO: " + settlementType);
            }

            Boolean bIsIntuitInitiated = new Boolean(isIntuitInitiated);
            Boolean bShouldChargeFee = new Boolean(shouldChargeFee);

            TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
            txnReverseDTO.setChargeFee(bShouldChargeFee);
            txnReverseDTO.setIntuitInitiatedReversals(bIsIntuitInitiated);
            txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            txnReverseDTO.setDdTransactionIdList(txnsToReverse);
            txnReverseDTO.setTxDate(null);
            txnReverseDTO.setTxSettlementTypeCd(settlementTypeDTO);
            ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);

            PayrollServices.commitUnitOfWork();
            if (!reverseTxnProcResult.isSuccess()) {
                throw new RuntimeException(reverseTxnProcResult.getMessages().get(0).toString());
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void createBillPaymentReversals(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                @WebParam(name = "settlementType")String settlementType,
                                @WebParam(name = "billPaymentsToReverse")List<String> txnsToReverse,
                                @WebParam(name = "shouldChargeFee")String shouldChargeFee,
                                @WebParam(name = "intuitInitiated")String isIntuitInitiated)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || settlementType == null || shouldChargeFee == null || isIntuitInitiated == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, source PayrollRun ID, " +
                        "settlementType, shouldChargeFee, isIntuitInitiated can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }
            SettlementTypeDTO settlementTypeDTO = SettlementTypeDTO.valueOf(settlementType);
            if (settlementTypeDTO == null) {
                throw new RuntimeException("Invalid settlementTypeDTO: " + settlementType);
            }
            Boolean bIsIntuitInitiated = Boolean.valueOf(isIntuitInitiated);
            Boolean bShouldChargeFee = Boolean.valueOf(shouldChargeFee);
            for (String billPaymentId : txnsToReverse) {

                 BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, billPaymentId);

                if(billPayment == null) {
                    throw new RuntimeException("Bill Payment with id" + billPaymentId + " does not exist");
                }

                ArrayList<String> billPaymentSplitIds = new ArrayList<String>();

                for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
                    billPaymentSplitIds.add(billPaymentSplit.getSourceId());
                }

                TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
                txnReverseDTO.setChargeFee(bShouldChargeFee);
                txnReverseDTO.setIntuitInitiatedReversals(bIsIntuitInitiated);
                txnReverseDTO.setSourcePayrollRunId(billPayment.getPayrollRun().getSourcePayRunId());
                txnReverseDTO.setDdTransactionIdList(billPaymentSplitIds);
                txnReverseDTO.setTxDate(null);
                txnReverseDTO.setTxSettlementTypeCd(settlementTypeDTO);
                ProcessResult reverseTxnProcResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), txnReverseDTO);

                if (!reverseTxnProcResult.isSuccess()) {
                    throw new RuntimeException(reverseTxnProcResult.getMessages().get(0).toString());
                }
            }

            PayrollServices.commitUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addWireExpectedDate(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                    @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                    @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                    @WebParam(name = "wireExpectedDate")String wireExpectedDate,
                                    @WebParam(name = "collectionStage")String collectionStage,
                                    @WebParam(name = "action")String actionEventCode,
                                    @WebParam(name = "lastChanceEmail")String isLastChance)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || wireExpectedDate == null || sourcePayrollRunID == null || isLastChance == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, source PayrollRun ID, " +
                        "wire expected date, isLastChance can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            CollectionStage domainCollectionStage = PayrollServices.entityFinder.findById(CollectionStage.class, CollectionStageCode.valueOf(collectionStage));

            if (domainCollectionStage == null) {
                throw new RuntimeException("Invalid collectionStage: " + collectionStage);
            }

            ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.valueOf(actionEventCode));

            if (actionEvent == null) {
                throw new RuntimeException("Invalid actionEvent: " + actionEventCode);
            }

            Boolean bIsLastChance = new Boolean(isLastChance);

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(wireExpectedDate);
            date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());


            ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(payrollRun.getSourcePayRunId(),
                    new DateDTO(date), domainCollectionStage, actionEvent.getCode(), bIsLastChance);
            ProcessResult modifyWireExpectedProcResult = PayrollServices.payrollManager.modifyWireExpectedDate(company.getSourceSystemCd(), company.getSourceCompanyId(), wireExpectedDTO);

            PayrollServices.commitUnitOfWork();
            if (!modifyWireExpectedProcResult.isSuccess()) {
                throw new RuntimeException(modifyWireExpectedProcResult.getMessages().get(0).toString());
            }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @WebMethod
    public void nonACHredebitTransaction(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                         @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                         @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                         @WebParam(name = "redebitDTOs")Collection<RedebitWSDTO> redebitDTOs,
                                         @WebParam(name = "settlementType")String settlementType,
                                         @WebParam(name = "redebitDate")String redebitDate)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || sourcePayrollRunID == null || settlementType == null || redebitDate == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, source PayrollRun ID" +
                        "redebit date or settlement type can not be null");
            }

            if (redebitDate != null && redebitDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid redebitDate date format" + redebitDate + ".  Correct format: MM/dd/yyyy");
            }

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(redebitDate);
            date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            SettlementTypeDTO dtoSettlementType = SettlementTypeDTO.valueOf(settlementType);

            if (dtoSettlementType == null) {
                throw new RuntimeException("Invalid dtoSettlementType " + dtoSettlementType);
            }

            ArrayList<RedebitImpoundDTO> allRedebits = new ArrayList<RedebitImpoundDTO>();

            for (RedebitWSDTO currRedebitDTO : redebitDTOs) {
                RedebitImpoundDTO currRedebitImpoundDTO = null;
                if (currRedebitDTO.transactionType == null) {
                    currRedebitImpoundDTO = new RedebitImpoundDTO(currRedebitDTO.originalTransactionId,
                         new SpcfMoney(currRedebitDTO.amount.toString()), new DateDTO(date), dtoSettlementType);
                } else {
                    DomainEntitySet<FinancialTransaction> finTxs = getPayrollFinancialTransactions(company, payrollRun, currRedebitDTO.transactionType);
                    if (finTxs != null && finTxs.size() > 0) {
                        FinancialTransaction originalTxn = finTxs.get(0);
                        String amount = originalTxn.getFinancialTransactionAmount().toString();

                        if (currRedebitDTO.amount != null) {
                            amount = currRedebitDTO.amount.toString();
                        }

                        currRedebitImpoundDTO = new RedebitImpoundDTO(originalTxn.getId().toString(),
                            new SpcfMoney(amount), new DateDTO(date), dtoSettlementType);
                    } else {
                        throw new RuntimeException("No financial transactions exist for the specified transaction type "
                                + currRedebitDTO.transactionType + "and for the payroll "+ sourcePayrollRunID);
                    }

                }
                allRedebits.add(currRedebitImpoundDTO);
            }

            ProcessResult procResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebits);
            if (!procResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
                throw new RuntimeException(procResult.getMessages().get(0).toString());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> redebitTransaction(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                           @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                           @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                                           @WebParam(name = "sourceBillPaymentID")String sourceBillPaymentID,
                                                           @WebParam(name = "transactionType")String transactionType,
                                                           @WebParam(name = "transactionState")String transactionState,
                                                           @WebParam(name = "settlementType")String settlementType,
                                                           @WebParam(name = "redebitAmount")String redebitAmount,
                                                           @WebParam(name = "redebitDate")String redebitDate)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || transactionType == null || transactionState == null || (sourcePayrollRunID == null && sourceBillPaymentID == null)) {
                throw new RuntimeException("Any of Source System Code, Source Company ID," +
                        "transaction type and transaction state can not be null. Source PayrollRun ID or source BillPayment ID must also be specified.");
            }

            if (!transactionState.equals(TransactionStateCode.Returned.toString())) {
                throw new RuntimeException("Transaction state must be in Returned");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = null;
            if(sourcePayrollRunID != null) {
                payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);
                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourcePayrollRunID");
                }
            }
            else if(sourceBillPaymentID != null) {
                BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentID);
                if (billPayment == null) {
                    throw new RuntimeException("Invalid sourceBillPaymentID");
                }

                payrollRun = billPayment.getPayrollRun();
            }



            TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.valueOf(transactionType));
            TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.valueOf(transactionState));

            Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType)
                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState)
                                                    .And(FinancialTransaction.PayrollRun().equalTo(payrollRun))
                                                    .And(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO))));

            DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);
            DomainEntitySet<FinancialTransaction> redebitTxns = new DomainEntitySet<FinancialTransaction>();

            String sourcePayrollRunId = null;
            String sourceCompanyBankAccountId = null;
            ProcessResult<DomainEntitySet<FinancialTransaction>> result = null;
            for (FinancialTransaction financialTransaction : finTxs) {

                if (financialTransaction.getPayrollRun() != null) {
                    sourcePayrollRunId = financialTransaction.getPayrollRun().getSourcePayRunId();
                }
                BankAccount bankAccount = null;
                if (financialTransaction.getCreditBankAccountType() == BankAccountOwnerType.Company) {
                    bankAccount = financialTransaction.getCreditBankAccount();
                }
                else {
                    bankAccount = financialTransaction.getDebitBankAccount();
                }
                CompanyBankAccount companyBankAccount =
                        CompanyBankAccount.findCompanyBankAccount(company, bankAccount);
                if (companyBankAccount != null) {
                    sourceCompanyBankAccountId = companyBankAccount.getSourceBankAccountId();
                }
                RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
                FinancialTransaction originalTxn = null;

                if (TransactionTypeCode.EmployerDdDebit == TransactionTypeCode.valueOf(transactionType)
                        || TransactionTypeCode.EmployerDdRedebit == TransactionTypeCode.valueOf(transactionType)) {
                    // Get the employer debit transactions returned for the payroll
                    DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                            new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                            new TransactionStateCode[]{TransactionStateCode.Returned});
                    //Should only be one
                    for (FinancialTransaction finTxn : financialTxs) {
                        originalTxn = finTxn;
                    }

                }
                else {
                    originalTxn = financialTransaction;
                }

                if (null != originalTxn) {
                    if (redebitAmount == null) {
                        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
                    }
                    else {
                        redebitDTO.setAmount(new SpcfMoney(redebitAmount));
                    }

                    if (redebitDate == null) {
                        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
                    }
                    else {
                        SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                        dateFormat.setPattern("MM/dd/yyyy");
                        SpcfCalendar parsedRunDate = dateFormat.parse(redebitDate);
                        date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
                        redebitDTO.setInitiationDate(new DateDTO(date));
                    }

                    redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
                }
                ArrayList<RedebitImpoundDTO> redebitCollection = new ArrayList<RedebitImpoundDTO>();
                redebitCollection.add(redebitDTO);
                result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company.getSourceSystemCd(), company.getSourceCompanyId(), redebitCollection);

                if (result.isSuccess()) {
                    if (result.getResult() != null && result.getResult().size() > 0) {
                        redebitTxns.add(result.getResult().get(0));
                    }
                }
                else {
                    throw new RuntimeException(result.getMessages().get(0).toString());
                }

            }
            Collection<TransactionWSDTO> transactionWSDTOs = null;
            if (redebitTxns.size() > 0) {
                transactionWSDTOs = buildTransactionResponse(redebitTxns, false);
            }
            PayrollServices.commitUnitOfWork();
            return transactionWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> redebitTransactionCollection(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                     @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                     @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                                                     @WebParam(name = "redebitDTOs")Collection<RedebitWSDTO> redebitDTOs,
                                                                     @WebParam(name = "redebitDate")String redebitDate)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || redebitDTOs == null || sourcePayrollRunID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, source PayrollRun ID" +
                        "transaction type or transaction state can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            ArrayList<RedebitImpoundDTO> allRedebitImpoundDTOs = new ArrayList<RedebitImpoundDTO>();
            DomainEntitySet<FinancialTransaction> redebitTxns = new DomainEntitySet<FinancialTransaction>();

            ProcessResult<DomainEntitySet<FinancialTransaction>> result = null;
            for (RedebitWSDTO currRedebitDTO : redebitDTOs) {
                RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
                SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("MM/dd/yyyy");
                SpcfCalendar parsedRunDate = dateFormat.parse(redebitDate);
                date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
                redebitDTO.setInitiationDate(new DateDTO(date));

                if (currRedebitDTO.transactionType == null) {
                    redebitDTO.setOriginalFinancialTxId(currRedebitDTO.originalTransactionId);
                    redebitDTO.setAmount(SpcfUtils.convertToSpcfMoney(currRedebitDTO.amount));
                }
                else {
                    DomainEntitySet<FinancialTransaction> finTxs = getPayrollFinancialTransactions(company, payrollRun, currRedebitDTO.transactionType);
                    if (finTxs != null && finTxs.size() > 0) {
                        FinancialTransaction originalTxn = finTxs.get(0);
                        String amount = originalTxn.getFinancialTransactionAmount().toString();

                        if (currRedebitDTO.amount != null) {
                            amount = currRedebitDTO.amount.toString();
                        }
                        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());
                        redebitDTO.setAmount(new SpcfMoney(amount));

                    }
                    else {
                        throw new RuntimeException("No financial transactions exist for the specified transaction type "
                                + currRedebitDTO.transactionType + "and for the payroll " + sourcePayrollRunID);
                    }

                }
                allRedebitImpoundDTOs.add(redebitDTO);
            }
            result = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(company.getSourceSystemCd(), company.getSourceCompanyId(), allRedebitImpoundDTOs);

            if (result.isSuccess()) {
                if (result.getResult() != null && result.getResult().size() > 0) {
                    redebitTxns.add(result.getResult().get(0));
                }
            }
            else {
                throw new RuntimeException(result.getMessages().get(0).toString());
            }


            Collection<TransactionWSDTO> transactionWSDTOs = null;
            if (redebitTxns.size() > 0) {
                transactionWSDTOs = buildTransactionResponse(redebitTxns, false);
            }
            PayrollServices.commitUnitOfWork();
            return transactionWSDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<PayrollRunWSDTO> buildPayrollRunResponse(DomainEntitySet<PayrollRun> payrollRuns) throws Exception {
        Collection<PayrollRunWSDTO> payrollDTOs = new ArrayList<PayrollRunWSDTO>(payrollRuns.size());
        PayrollRunWSDTO payrollDTO = null;
        while (payrollRuns.size() > 100) {
            payrollRuns.remove(100);
        }
        for (PayrollRun payrollRun : payrollRuns) {
            payrollDTO = new PayrollRunWSDTO();
            payrollDTO.id = payrollRun.getId().toString();
            payrollDTO.netAmount = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
            payrollDTO.paycheckCount = payrollRun.getPaycheckCollection().size();
            payrollDTO.paycheckDepositDate = CalendarUtils.getDateWithoutSeconds(payrollRun.getPaycheckDate().toLocal());
            payrollDTO.payrollRunDate = CalendarUtils.getDateWithoutSeconds(payrollRun.getPayrollRunDate().toLocal());
            payrollDTO.sourceBatchId = payrollRun.getSourcePayRunId();
            payrollDTO.status = payrollRun.getPayrollRunStatus().toString();

            com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactionCollection();
            payrollDTO.txnCount = financialTxs.size();
            DomainEntitySet<FinancialTransaction> executedFinancialTxs =
                    payrollRun.getFinancialTransactions(null, new TransactionStateCode[]{TransactionStateCode.Executed});
            payrollDTO.offloadExecutedCount = executedFinancialTxs.size();
            payrollDTOs.add(payrollDTO);
        }
        return payrollDTOs;
    }

    class TransactionWSDTOComparator implements Comparator<TransactionWSDTO> {
        public int compare(TransactionWSDTO a, TransactionWSDTO b) {
            return key(b).compareTo(key(a));
        }
        String key(TransactionWSDTO dto) {
            String s = dto.transactionType + dto.currentState + dto.transactionAmount + dto.settlementType + dto.settlementDate + dto.offloadStatus;
            if (dto.debitBankAccount != null) {
                s = s + dto.debitBankAccount.bankAccountOwnerType + dto.debitBankAccount.bankName + dto.debitBankAccount.bankAccountType + dto.debitBankAccount.accountNumber;
            }
            if (dto.creditBankAccount != null) {
                s = s + dto.creditBankAccount.bankAccountOwnerType + dto.creditBankAccount.bankName + dto.creditBankAccount.bankAccountType + dto.creditBankAccount.accountNumber;
            }
            if (dto.moneyMovementTransaction != null) {
                s = s + dto.moneyMovementTransaction.paymentStatus + dto.moneyMovementTransaction.initiationDate + dto.moneyMovementTransaction.dueDate + dto.moneyMovementTransaction.amount;
            }
            return s;
        }
    }

    private Collection<TransactionWSDTO> buildTransactionResponse(DomainEntitySet<FinancialTransaction> finTxs, Boolean addPaycheckStatus) throws Exception {
        return buildTransactionResponse(finTxs, addPaycheckStatus, false);
    }

    private Collection<TransactionWSDTO> buildTransactionResponse(DomainEntitySet<FinancialTransaction> finTxs, Boolean addPaycheckStatus, Boolean includeAction) throws Exception {
        ArrayList<TransactionWSDTO> txDTOs = new ArrayList(finTxs.size());
        TransactionWSDTO txDTO = null;
        List<MoneyMovementTransaction> mmtsWithActions = new ArrayList<MoneyMovementTransaction>();
        for (int i = finTxs.size() - 1; i >= 0; i--) {
            FinancialTransaction finTx = finTxs.get(i);
            txDTO = new TransactionWSDTO();
            txDTO.id = finTx.getId().toString();
            String sku = finTx.getSku();
            OfferingServiceChargeType offeringServiceCharge = null;
            if (null != sku) {
                offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
            }
            String transactionType = DDCodeToPSP.getQBOETransactionTypeCode(finTx.getTransactionType().getTransactionTypeCd(), offeringServiceCharge);;
            if (transactionType == null) {
                transactionType = finTx.getTransactionType().getTransactionTypeCd().toString();
            }
            txDTO.transactionType = transactionType;
            txDTO.currentState = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            txDTO.transactionAmount = SpcfUtils.convertToBigDecimal(finTx.getFinancialTransactionAmount());
            txDTO.settlementType = finTx.getSettlementTypeCd().toString();
            txDTO.settlementDate = new Date(finTx.getSettlementDate().toLocal().getTimeInMilliseconds());
            txDTO.offloadStatus = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            if (null != finTx.getCreditBankAccount() && null != finTx.getCreditBankAccountType()) {
                txDTO.creditBankAccount = getBankAccountDTO(finTx.getCreditBankAccount(), finTx.getCreditBankAccountType());
            }
            if (null != finTx.getDebitBankAccount() && null != finTx.getDebitBankAccountType()) {
                txDTO.debitBankAccount = getBankAccountDTO(finTx.getDebitBankAccount(), finTx.getDebitBankAccountType());
            }
            if (null != finTx.getMoneyMovementTransaction()) {
                txDTO.moneyMovementTransaction = getMoneyMovementTransactionDTO(finTx.getMoneyMovementTransaction());
            }
            if (addPaycheckStatus) {
            PaycheckSplit paycheckSplit = finTx.getPaycheckSplit();
            if (null != paycheckSplit) {
                Paycheck paycheck = paycheckSplit.getPaycheck();
                if (null != paycheck) {
                    txDTO.paycheckStatus = paycheck.getStatus();
                }
            }
            } else {
                txDTO.paycheckStatus = null;
            }

            if(includeAction) {
                if(finTx.getMoneyMovementTransaction() != null && !mmtsWithActions.contains(finTx.getMoneyMovementTransaction())) {
                    if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.None) {
                        if(finTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created) {
                            txDTO.action = "Run Offload";
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        } else if(finTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed) {
                            txDTO.action = "Complete Transaction";
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        }
                    } else if(finTx.getSettlementTypeCd() == SettlementType.EDI) {
                        if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.ReadyToSend) {
                            txDTO.action = "Submit Payment";
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        } else if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.SentToAgency) {
                            txDTO.action = "Payment Submitted";
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        }
                    } else if(finTx.getSettlementTypeCd() == SettlementType.EFTPS) {
                        if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.ReadyToSend) {
                            txDTO.action = "Submit Payment";
                            txDTO.template = finTx.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        } else if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.SentToAgency) {
                            txDTO.action = "Complete Payment";
                            txDTO.template = finTx.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        }
                    } else if(finTx.getMoneyMovementTransaction().getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
                        if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.ReadyToSend ||
                                finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.ATFFinalized) {
                            txDTO.action = "Submit Payment";
                            txDTO.template = finTx.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        } else if(finTx.getMoneyMovementTransaction().getTaxPaymentStatus() == TaxPaymentStatus.SentToAgency) {
                            txDTO.action = "Complete Payment";
                            txDTO.template = finTx.getMoneyMovementTransaction().getPaymentTemplate().getPaymentTemplateCd();
                            mmtsWithActions.add(finTx.getMoneyMovementTransaction());
                        }
                    }
                }
            }

            txDTOs.add(txDTO);
        }
        Collections.sort(txDTOs, new TransactionWSDTOComparator());
        return txDTOs;
    }


    private Collection<TransactionWSDTO> buildTransactionsResponse(DomainEntitySet<FinancialTransaction> financialTransactions) {
        Collection<TransactionWSDTO> wsdtos = new ArrayList<TransactionWSDTO>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            TransactionWSDTO wsdto = new TransactionWSDTO();
            wsdto.id = financialTransaction.getId().toString();
            wsdto.transactionType = financialTransaction.getTransactionType().getTransactionTypeCd().toString();
            wsdto.currentState = financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd().toString();
            wsdto.transactionAmount = SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount());
            wsdto.settlementDate = new Date(financialTransaction.getSettlementDate().toLocal().getTimeInMilliseconds());

            Collection<ActionEvent> actionEvents = financialTransaction.getActionCollection();

            // add UI specific action events
            // -- TxStateHistory
            ActionEvent historyEvent = Application.findById(ActionEvent.class, ActionEventCode.TxStateHistory);
            actionEvents.add(historyEvent);
            Collection<String> actionEventStrings = new ArrayList<String>();
            for (ActionEvent actionEvent : actionEvents) {
                actionEventStrings.add(actionEvent.getCode().toString());
            }
            Collections.sort((List<String>)actionEventStrings);
            wsdto.allowableActions = actionEventStrings;
            wsdtos.add(wsdto);
        }
        return wsdtos;
    }

    private Collection<PaycheckWSDTO> buildPaycheckResponse(DomainEntitySet<Paycheck> paychecks, Boolean shouldIncludeTxnData, Boolean shouldIncludeAssistedData, Boolean shouldIncludeTokens) throws Exception {
        Collection<PaycheckWSDTO> paycheckDTOs = new ArrayList<PaycheckWSDTO>(paychecks.size());
        PaycheckWSDTO paycheckDTO = null;
        SpcfMoney paycheckAmount = new SpcfMoney("0.00");
        DomainEntitySet<PaycheckSplit> paycheckSplits = null;
        Iterator<PaycheckSplit> iterator = null;
        for (Paycheck paycheck : paychecks) {
            paycheckDTO = new PaycheckWSDTO();
            paycheckDTO.id = paycheck.getId().toString();
            paycheckDTO.sourcePaycheckId = paycheck.getSourcePaycheckId();
            if (paycheck.getDDEmployee()!=null) {
                paycheckDTO.sourceEmployeeId = paycheck.getDDEmployee().getSourceEmployeeId();
                paycheckDTO.employeeDisplayName = paycheck.getDDEmployee().getFirstName() +
                    paycheck.getDDEmployee().getLastName();
            }

            paycheckSplits = paycheck.getPaycheckSplitCollection();
            iterator = paycheckSplits.iterator();
            if (shouldIncludeTxnData) {
                paycheckDTO.financialTransactions = new ArrayList<TransactionWSDTO>();
            }

            while (iterator.hasNext()) {
                PaycheckSplit currentPaycheckSplit = iterator.next();
                paycheckAmount = (SpcfMoney) paycheckAmount.add(currentPaycheckSplit.getPaycheckSplitAmount());
                if (shouldIncludeTxnData) {
                    DomainEntitySet<FinancialTransaction> financialTransactions = currentPaycheckSplit.getFinancialTransactions();
                    Collection<TransactionWSDTO> finTxns = buildTransactionResponse(financialTransactions, false);
                    paycheckDTO.financialTransactions.addAll(finTxns);
                }
            }

            if (shouldIncludeTxnData) {
                paycheckDTO.compensationLineItems = new ArrayList<CompensationWSDTO>();
                paycheckDTO.deductionLineItems = new ArrayList<DeductionWSDTO>();
                paycheckDTO.employerContributionLineItems = new ArrayList<EmployerContributionWSDTO>();
                paycheckDTO.compensationLineItems = buildCompensationResponse(paycheck.getCompensationCollection(), shouldIncludeAssistedData);
                paycheckDTO.deductionLineItems = buildDeductionResponse(paycheck.getDeductionCollection(), shouldIncludeAssistedData);
                paycheckDTO.employerContributionLineItems = buildEmployerContributionResponse(paycheck.getEmployerContributionCollection(), shouldIncludeAssistedData);
                paycheckDTO.taxLiabilities = buildTaxLiabilityResponse(paycheck.getTaxCollection(), shouldIncludeAssistedData);

                if (paycheck.getSourceEmployee() !=null) {
                    paycheckDTO.nonDDEmployeeDisplayName = paycheck.getSourceEmployee().getFirstName()+paycheck.getSourceEmployee().getLastName();
                    paycheckDTO.nonDDEmployeeId = paycheck.getSourceEmployee().getSourceEmployeeId();
                }

                ThirdParty401kBatchPaycheck tp401kBatchPaycheck =
                        ThirdParty401kBatchPaycheck.findThirdParty401kBatchPaycheck(paycheck);
                paycheckDTO.hasBeenOffloadedToTOK = tp401kBatchPaycheck != null;                
            }

            paycheckDTO.paycheckAmount = SpcfUtils.convertToBigDecimal(paycheckAmount);
            if (null != paycheck.getStatus()) {
                paycheckDTO.status = paycheck.getStatus().toString();
            }

            if (paycheck.getPayPeriodBeginDate()!=null) {
                paycheckDTO.payPeriodBeginDate = CalendarUtils.getDateWithoutSeconds(paycheck.getPayPeriodBeginDate().toLocal());
            }

            if (paycheck.getPayPeriodEndDate()!=null) {
                paycheckDTO.payPeriodEndDate = CalendarUtils.getDateWithoutSeconds(paycheck.getPayPeriodEndDate().toLocal());
            }

            if (shouldIncludeAssistedData) {
                paycheckDTO.netAmount = SpcfUtils.convertToBigDecimal(paycheck.getNetAmount());                

                paycheckDTO.qbdtPaycheckInfo = getQbdtPaycheckInfo(paycheck.getQbdtPaycheckInfo(), shouldIncludeTokens);
                paycheckDTO.isYTDAdjustment = paycheck.getIsYTDAdjustment();

                CompanyAdjustmentSubmission cas = paycheck.getCompanyAdjustmentSubmission();
                CompanyAdjustmentSubmissionWSDTO wsCas = new CompanyAdjustmentSubmissionWSDTO();
                if(cas != null){
                    wsCas.amount = SpcfUtils.convertToBigDecimal(cas.getAmount());
                    wsCas.isVoid = cas.getVoidSubmission() != null;
                    wsCas.sourceId = cas.getSourceId();
                    wsCas.submissionDate = CalendarUtils.convertToDate(cas.getSubmissionDate());
                    wsCas.qbdtTransactionInfo = CompanyWS.getQbdtTransactionInfoWSDTO(cas.getQbdtTransactionInfo());

                    wsCas.liabilityAdjustments = new ArrayList<LiabilityAdjustmentWSDTO>();
                    for (LiabilityAdjustment la : cas.getLiabilityAdjustmentsForCompanyVoid().sort(LiabilityAdjustment.CompanyLaw().SourceId())) {
                        LiabilityAdjustmentWSDTO wsLa = new LiabilityAdjustmentWSDTO();
                        wsLa.amount = SpcfUtils.convertToBigDecimal(la.getAmount());
                        wsLa.effectiveDate = CalendarUtils.convertToDate(la.getEffectiveDate());
                        wsLa.taxableWages = SpcfUtils.convertToBigDecimal(la.getTaxableWages());
                        wsLa.totalWages = SpcfUtils.convertToBigDecimal(la.getTotalWages());
                        wsLa.qbdtTransactionInfo = CompanyWS.getQbdtTransactionInfoWSDTO(la.getQbdtTransactionInfo());
                        wsCas.liabilityAdjustments.add(wsLa);
                    }
                }
                paycheckDTO.companyAdjustmentSubmission = wsCas;
                Collection<CompensationWSDTO> compensationWSDTOs = new ArrayList<CompensationWSDTO>();
                for (Compensation compensation : paycheck.getCompensationCollection().sort(Compensation.CompanyPayrollItem().SourcePayrollItemId())) {
                    CompensationWSDTO compensationWSDTO = new CompensationWSDTO();
                    compensationWSDTO.qbdtPaylineInfoWSDTO = new QbdtPaylineInfoWSDTO();
                    if(compensation.getQbdtPaylineInfo() != null){
                        compensationWSDTO.qbdtPaylineInfoWSDTO.job = compensation.getQbdtPaylineInfo().getJob();
                        compensationWSDTO.qbdtPaylineInfoWSDTO.rate = compensation.getQbdtPaylineInfo().getRate();
                    }
                    compensationWSDTOs.add(compensationWSDTO);
                }
                paycheckDTO.compensationLineItems = compensationWSDTOs;
            }

            paycheckDTOs.add(paycheckDTO);
        }
        return paycheckDTOs;
    }

    private QbdtPaycheckInfoWSDTO getQbdtPaycheckInfo(QbdtPaycheckInfo info, Boolean pShouldIncludeTokens) {
        QbdtPaycheckInfoWSDTO wsInfo = new QbdtPaycheckInfoWSDTO();
        if(info != null){
            wsInfo.accountName = info.getAccountName();
            wsInfo.checkNumber = info.getCheckNumber();
            wsInfo.cleared = info.getCleared();
            wsInfo.memo = info.getMemo();
            wsInfo.onService = info.getOnService();
            wsInfo.prorate = info.getProrate();
            wsInfo.trackingClass = info.getTrackingClass();
            if(pShouldIncludeTokens) {
                wsInfo.token = info.getToken();
            }
        }
        return wsInfo;
    }

    private Collection<TaxWSDTO> buildTaxLiabilityResponse(DomainEntitySet<Tax> pTaxCollection, Boolean shouldIncludeAssistedData) {
        Collection<TaxWSDTO> taxDTOs = new ArrayList<TaxWSDTO>();
        if (pTaxCollection!=null) {
            for (Tax tax: pTaxCollection) {
                TaxWSDTO taxDTO = new TaxWSDTO();
                taxDTO.id = tax.getId().toString();
                taxDTO.lawId = tax.getLaw().getLawId();
                taxDTO.payStubOrder = tax.getPayStubOrder();

                if (tax.getTaxableWagesAmount() != null)
                    taxDTO.liabilityTaxableWages = SpcfUtils.convertToBigDecimal(tax.getTaxableWagesAmount());

                if (tax.getTotalWagesAmount() != null)
                    taxDTO.liabilityTotalWages = SpcfUtils.convertToBigDecimal(tax.getTotalWagesAmount());

                if (tax.getTaxLiabilityAmount() != null)
                    taxDTO.liabilityTXAmt = SpcfUtils.convertToBigDecimal(tax.getTaxLiabilityAmount());

                if (tax.getTaxLiabilityYTDAmount() != null)
                    taxDTO.liabilityYTDWages = SpcfUtils.convertToBigDecimal(tax.getTaxLiabilityYTDAmount());

                if (shouldIncludeAssistedData &&  tax.getTipsTaxableWageAmount() != null) {
                    taxDTO.tipsTaxableWageAmount = SpcfUtils.convertToBigDecimal(tax.getTipsTaxableWageAmount());
                }

                taxDTOs.add(taxDTO);
            }
        }

        return taxDTOs;
    }

    private Collection<EmployerContributionWSDTO> buildEmployerContributionResponse(DomainEntitySet<EmployerContribution> pEmployerContributionCollection, Boolean shouldIncludeAssistedData) {
        Collection<EmployerContributionWSDTO> employerContributionDTOs = new ArrayList<EmployerContributionWSDTO>();
        EmployerContributionWSDTO employerContributionWSDTO = null;
        if (pEmployerContributionCollection!=null) {
            for (EmployerContribution currentEmployerCompensation : pEmployerContributionCollection) {
                employerContributionWSDTO = new EmployerContributionWSDTO();
                employerContributionWSDTO.id = currentEmployerCompensation.getId().toString();
                employerContributionWSDTO.contributionAmount = SpcfUtils.convertToBigDecimal(currentEmployerCompensation.getContributionAmount());
                if (currentEmployerCompensation.getContributionYTDAmount()!=null) {
                    employerContributionWSDTO.contributionYTDAmount = SpcfUtils.convertToBigDecimal(currentEmployerCompensation.getContributionYTDAmount());
                }
                if (currentEmployerCompensation.getTaxableWagesAmount()!=null) {
                    employerContributionWSDTO.taxableWagesAmount = SpcfUtils.convertToBigDecimal(currentEmployerCompensation.getTaxableWagesAmount());
                }
                if (currentEmployerCompensation.getTotalWagesAmount()!=null) {
                    employerContributionWSDTO.totalWagesAmount = SpcfUtils.convertToBigDecimal(currentEmployerCompensation.getTotalWagesAmount());
                }
                employerContributionWSDTO.payrollItem = buildPayrollItemResponse(currentEmployerCompensation.getCompanyPayrollItem());
                employerContributionWSDTO.payStubOrder = currentEmployerCompensation.getPayStubOrder();

                if (shouldIncludeAssistedData) {
                    employerContributionWSDTO.qbdtPaylineInfoWSDTO = getQbdtPaylineInfoWSDTO(currentEmployerCompensation.getQbdtPaylineInfo());
                }

                employerContributionDTOs.add(employerContributionWSDTO);
            }
        }

        return employerContributionDTOs;
    }

    private PayrollItemWSDTO buildPayrollItemResponse(CompanyPayrollItem pCompanyPayrollItem) {
        PayrollItemWSDTO payrollItemDTO = new PayrollItemWSDTO();
        payrollItemDTO.payrollItemCode = pCompanyPayrollItem.getPayrollItem() == null ? null : ObjectUtils.toString(pCompanyPayrollItem.getPayrollItem().getPayrollItemCode());
        payrollItemDTO.sourceDescription = pCompanyPayrollItem.getSourceDescription();
        payrollItemDTO.sourcePayrollItemId = pCompanyPayrollItem.getSourcePayrollItemId();
        return payrollItemDTO;
    }

    private Collection<CompensationWSDTO> buildCompensationResponse(DomainEntitySet<Compensation> pDomainCompensationTransactions, Boolean shouldIncludeAssistedData) {
        Collection<CompensationWSDTO> compensationDTOs = new ArrayList<CompensationWSDTO>();
        CompensationWSDTO compensationWSDTO = null;
        if (pDomainCompensationTransactions!=null) {
            for (Compensation currentCompensation : pDomainCompensationTransactions) {
                compensationWSDTO = new CompensationWSDTO();
                compensationWSDTO.id = currentCompensation.getId().toString();
                compensationWSDTO.compensationAmount = SpcfUtils.convertToBigDecimal(currentCompensation.getCompensationAmount());
                compensationWSDTO.compensationHoursWorked = new BigDecimal(currentCompensation.getHoursWorked());
                if (currentCompensation.getCompensationYTDAmount()!=null) {
                    compensationWSDTO.compensationYTDAmount = SpcfUtils.convertToBigDecimal(currentCompensation.getCompensationYTDAmount());
                }
                compensationWSDTO.payrollItem = buildPayrollItemResponse(currentCompensation.getCompanyPayrollItem());
                compensationWSDTO.payStubOrder = currentCompensation.getPayStubOrder();

                if (shouldIncludeAssistedData) {
                    compensationWSDTO.qbdtPaylineInfoWSDTO = getQbdtPaylineInfoWSDTO(currentCompensation.getQbdtPaylineInfo());
                }

                compensationDTOs.add(compensationWSDTO);
            }
        }

        return compensationDTOs;
    }

    private QbdtPaylineInfoWSDTO getQbdtPaylineInfoWSDTO(QbdtPaylineInfo info) {
        QbdtPaylineInfoWSDTO wsInfo = new QbdtPaylineInfoWSDTO();
        wsInfo.expenseByJob = info.getExpenseByJob();
        wsInfo.item = info.getItem();
        wsInfo.job = info.getJob();
        wsInfo.quantity = info.getQuantity();
        wsInfo.quantityType = ObjectUtils.toString(info.getQuantityType());
        wsInfo.rate = info.getRate();
        wsInfo.rateType = ObjectUtils.toString(info.getRateType());
        wsInfo.trackingClass = info.getTrackingClass();
        wsInfo.wcCode = info.getWcCode();
        return wsInfo;
    }

    private Collection<DeductionWSDTO> buildDeductionResponse(DomainEntitySet<Deduction> pDomainDeductionTransactions, Boolean shouldIncludeAssistedData) {
        Collection<DeductionWSDTO> deductionDTOs = new ArrayList<DeductionWSDTO>();
        DeductionWSDTO deductionWSDTO = null;
        if (pDomainDeductionTransactions!=null) {
            for (Deduction currentDeduction : pDomainDeductionTransactions) {
                deductionWSDTO = new DeductionWSDTO();
                deductionWSDTO.id = currentDeduction.getId().toString();
                deductionWSDTO.deductionAmount = SpcfUtils.convertToBigDecimal(currentDeduction.getDeductionAmount());
                if (currentDeduction.getDeductionYTDAmount()!=null) {
                    deductionWSDTO.deductionYTDAmount = SpcfUtils.convertToBigDecimal(currentDeduction.getDeductionYTDAmount());                    
                }
                deductionWSDTO.payrollItem = buildPayrollItemResponse(currentDeduction.getCompanyPayrollItem());
                deductionWSDTO.payStubOrder = currentDeduction.getPayStubOrder();

                if (shouldIncludeAssistedData) {
                    deductionWSDTO.qbdtPaylineInfoWSDTO = getQbdtPaylineInfoWSDTO(currentDeduction.getQbdtPaylineInfo());
                }

                deductionDTOs.add(deductionWSDTO);
            }
        }

        return deductionDTOs;
    }    

    private Collection<BankReturnWSDTO> buildBankReturnResponse(DomainEntitySet<TransactionReturn> txReturns) throws Exception {
        Collection<BankReturnWSDTO> bankRetrunDTOs = new ArrayList<BankReturnWSDTO>(txReturns.size());
        BankReturnWSDTO bankReturnWSDTO = null;
        for (TransactionReturn transactionReturn : txReturns) {
            bankReturnWSDTO = new BankReturnWSDTO();
            bankReturnWSDTO.id = transactionReturn.getId().toString();
            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

            for (FinancialTransaction financialTransaction : finTxnList) {
                //bankReturnWSDTO.transactionId = transactionReturn.getFinancialTransaction().getId().toString();
                bankReturnWSDTO.transactionId = financialTransaction.getId().toString();

                //String txTypeCode = transactionReturn.getFinancialTransaction().getTransactionType().getTransactionTypeCd();
                TransactionTypeCode txTypeCode = financialTransaction.getTransactionType().getTransactionTypeCd();

                if (txTypeCode == TransactionTypeCode.EmployeeDdCredit) {
                    PaycheckSplit split = financialTransaction.getPaycheckSplit();
                    if (split != null) {
                        Employee employee = split.getPaycheck().getDDEmployee();
                        bankReturnWSDTO.sourceEmployeeId = employee.getSourceEmployeeId();
                        bankReturnWSDTO.employeeDisplayName = employee.getFirstName() + employee.getLastName();
                    }
                } else if (txTypeCode == TransactionTypeCode.EmployeeDdReversalDebit) {
                    FinancialTransaction origFT = financialTransaction.getOriginalTransaction();
                    if (origFT != null) {
                        PaycheckSplit split = origFT.getPaycheckSplit();
                        if (split != null) {
                            Employee employee = split.getPaycheck().getDDEmployee();
                            bankReturnWSDTO.sourceEmployeeId = employee.getSourceEmployeeId();
                            bankReturnWSDTO.employeeDisplayName = employee.getFirstName() + employee.getLastName();
                        }
                    }
                } else if (txTypeCode == TransactionTypeCode.EmployeeEscalationCredit) {
                    // EE escalation credits have no relationship to original transaction or employee
                }
            }

            bankReturnWSDTO.traceNumber = new Long(transactionReturn.getBankReturnTraceNumber()).toString();
            bankReturnWSDTO.bankReturnCd = transactionReturn.getBankReturnCd();
            bankReturnWSDTO.returnStatus = getQBOEBankReturnStatus(transactionReturn.getReturnStatusCd());
            bankReturnWSDTO.statusChangeDate = new Date(transactionReturn.getReturnStatusEffectiveDate().toLocal().getTimeInMilliseconds());
            bankReturnWSDTO.createdDate = new Date(transactionReturn.getCreatedDate().toLocal().getTimeInMilliseconds());

            bankRetrunDTOs.add(bankReturnWSDTO);
        }
        return bankRetrunDTOs;
    }

    private Collection<BillingDetailWSDTO> buildBillingDetailResponse(PayrollRun pPayrollRun) throws Exception {
        DomainEntitySet<BillingDetail> billingDetails = pPayrollRun.getBillingDetailCollection();
        Collection<BillingDetailWSDTO> billingDetailWSDTOs = new ArrayList<BillingDetailWSDTO>(billingDetails.size());

        BillingDetailWSDTO billingDetailWSDTO = null;
        for (BillingDetail billingDetail:billingDetails) {
            billingDetailWSDTO = new BillingDetailWSDTO();
/*            if (billingDetail.getServiceCd() != null) {
                billingDetailWSDTO.serviceCode = billingDetail.getServiceCd().toString();
            }*/
            billingDetailWSDTO.itemName = billingDetail.getItemName();
            billingDetailWSDTO.itemSKU = billingDetail.getItemSku();
            if (billingDetail.getItemTotal() != null) {
                billingDetailWSDTO.itemTotal = billingDetail.getItemTotal().toString();
            }
            billingDetailWSDTO.quantity = billingDetail.getQuantity();
            if (billingDetail.getUnitPrice() != null) {
                billingDetailWSDTO.unitPrice = billingDetail.getUnitPrice().toString();
            }
            if ( billingDetail.getTaxAmount() != null) {
                billingDetailWSDTO.taxAmount = billingDetail.getTaxAmount().toString();
            }
            if (billingDetail.getTaxComputedDate() != null) {
                billingDetailWSDTO.taxComputedDate = new Date(billingDetail.getTaxComputedDate().toLocal().getTimeInMilliseconds());
            }
            if ( billingDetail.getTaxAmountWhenOffloaded() != null) {
                billingDetailWSDTO.taxWhenOffloaded = billingDetail.getTaxAmountWhenOffloaded().toString();
            }
            if (billingDetail.getOffloadDate() != null) {
                billingDetailWSDTO.offloadDate = new Date(billingDetail.getOffloadDate().toLocal().getTimeInMilliseconds());
            }

            billingDetailWSDTOs.add(billingDetailWSDTO);
        }
        return billingDetailWSDTOs;
    }

    private BankReturnWSDTO buildBankReturnWSDTO(TransactionReturn pTxnReturn)
            throws Exception {
        // Refresh the TransactionReturn
        pTxnReturn = Application.findById(TransactionReturn.class, pTxnReturn.getId());
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.
                findFinancialTransaction(pTxnReturn);

        FinancialTransaction financialTransaction = finTxnList.get(0);

        BankAccount nonIntuitBankAccount = getNonIntuitBankAccountType(financialTransaction);
        BankReturnWSDTO bankReturnWSDTO = new BankReturnWSDTO();
        bankReturnWSDTO.id = pTxnReturn.getId().toString();
        bankReturnWSDTO.transactionId = financialTransaction.getId().toString();
        bankReturnWSDTO.bankReturnCd = pTxnReturn.getBankReturnCd();
        bankReturnWSDTO.description = pTxnReturn.getBankReturnDescription();
        bankReturnWSDTO.returnStatus = pTxnReturn.getReturnStatusCd().toString();
        bankReturnWSDTO.traceNumber = Long.toString(pTxnReturn.getBankReturnTraceNumber());
        bankReturnWSDTO.statusChangeDate = new Date(pTxnReturn.getReturnStatusEffectiveDate().toLocal().getTimeInMilliseconds());
        bankReturnWSDTO.accountNumber = nonIntuitBankAccount.getAccountNumber();
        bankReturnWSDTO.accountType = nonIntuitBankAccount.getAccountTypeCd().toString();
        bankReturnWSDTO.routingNumber = nonIntuitBankAccount.getRoutingNumber();

        TransactionTypeCode txTypeCode = financialTransaction.getTransactionType().getTransactionTypeCd();

        if (txTypeCode == TransactionTypeCode.EmployeeDdCredit) {
            PaycheckSplit split = financialTransaction.getPaycheckSplit();
            if (split != null) {
                Employee employee = split.getPaycheck().getDDEmployee();
                bankReturnWSDTO.sourceEmployeeId = employee.getSourceEmployeeId();
                bankReturnWSDTO.employeeDisplayName = employee.getFirstName() + employee.getLastName();
            }
        } else if (txTypeCode == TransactionTypeCode.EmployeeDdReversalDebit) {
            FinancialTransaction origFT = financialTransaction.getOriginalTransaction();
            if (origFT != null) {
                PaycheckSplit split = origFT.getPaycheckSplit();
                if (split != null) {
                    Employee employee = split.getPaycheck().getDDEmployee();
                    bankReturnWSDTO.sourceEmployeeId = employee.getSourceEmployeeId();
                    bankReturnWSDTO.employeeDisplayName = employee.getFirstName() + employee.getLastName();
                }
            }
        } else if (txTypeCode == TransactionTypeCode.EmployeeEscalationCredit) {
            // EE escalation credits have no relationship to original transaction or employee
        }
        return bankReturnWSDTO;
    }

    private BankAccount getNonIntuitBankAccountType(FinancialTransaction pFinancialTransaction) {
        if (pFinancialTransaction.getCreditBankAccountType() != BankAccountOwnerType.Intuit) {
            return pFinancialTransaction.getCreditBankAccount();
        } else {
            return pFinancialTransaction.getDebitBankAccount();
        }
    }

    private BankAccountWSDTO getBankAccountDTO(BankAccount bankAccount, BankAccountOwnerType baOwnerType) {
        BankAccountWSDTO bankAccountDTO = new BankAccountWSDTO();
        bankAccountDTO.accountNumber = bankAccount.getAccountNumber();
        bankAccountDTO.bankAccountOwnerType = baOwnerType.toString();
        bankAccountDTO.bankAccountType = bankAccount.getAccountTypeCd().toString();
        bankAccountDTO.bankName = bankAccount.getBankName();
        bankAccountDTO.routingNumber = bankAccount.getRoutingNumber();
        return bankAccountDTO;
    }

    private XMLGregorianCalendar getXMLGregorianCalendar(SpcfCalendar spcfCalendar) throws Exception {
        Date date = new Date(spcfCalendar.getTimeInMilliseconds());
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.setTime(date);
        XMLGregorianCalendar pspXmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

        return pspXmlDate;
    }

    private SpcfCalendar getSpcfCalendar(Date pDate) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmSS");
        String pPSPTime = dateFormatter.format(pDate);
        SpcfCalendar newDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        int year = Integer.parseInt(pPSPTime.substring(0, 4));
        int month = Integer.parseInt(pPSPTime.substring(4, 6));
        int day = Integer.parseInt(pPSPTime.substring(6, 8));
        int hour = Integer.parseInt(pPSPTime.substring(8, 10));
        int minute = Integer.parseInt(pPSPTime.substring(10, 12));
        int second = Integer.parseInt(pPSPTime.substring(12, 14));
        newDate.setValues(year, month, day, hour, minute, second, 0);
        return newDate;
    }

    public static String getQBOETransactionStateCode(TransactionStateCode pTransactionStateCd) {
        switch (pTransactionStateCd) {
            case Created:
                return "CR";
            case Cancelled:
                return "CLD";
            case Completed:
                return "CP";
            case Executed:
                return "EX";
            case Returned:
                return "RTN";
            case Voided:
                return "VOID";
            default:
                return null;
        }
    }

    private MoneyMovementTransactionWSDTO getMoneyMovementTransactionDTO(MoneyMovementTransaction pMoneyMovementTransaction) throws RuntimeException {
        MoneyMovementTransactionWSDTO mmtDto = new MoneyMovementTransactionWSDTO();
        updateMoneyMovementTransactionDTO(mmtDto, pMoneyMovementTransaction);
        return mmtDto;
    }

    private void updateMoneyMovementTransactionDTO(MoneyMovementTransactionWSDTO mmtDto, MoneyMovementTransaction pMoneyMovementTransaction) {
        mmtDto.dueDate = new Date(pMoneyMovementTransaction.getDueDate().toLocal().getTimeInMilliseconds());
        mmtDto.initiationDate = new Date(pMoneyMovementTransaction.getInitiationDate().toLocal().getTimeInMilliseconds());
        mmtDto.paymentMethod = pMoneyMovementTransaction.getMoneyMovementPaymentMethodString();
        mmtDto.amount = SpcfUtils.convertToBigDecimal(pMoneyMovementTransaction.getMoneyMovementTransactionAmount());
        mmtDto.paymentStatus = pMoneyMovementTransaction.getStatus().toString();

        DomainEntitySet<EntryDetailRecord> entryDetailList = pMoneyMovementTransaction.getEntryDetailRecordCollection();

        mmtDto.achDetailRecords = getACHDetailRecordDTOs(entryDetailList);
        mmtDto.originalInitiationDate =
                pMoneyMovementTransaction.getOriginalInitiationDate() != null ?
                new Date(pMoneyMovementTransaction.getOriginalInitiationDate().toLocal().getTimeInMilliseconds()) : null;      
    }

    private TaxPaymentMoneyMovementTransactionWSDTO createTaxPaymentMoneyMovementTransactionDTO(MoneyMovementTransaction pMoneyMovementTransaction) {
        TaxPaymentMoneyMovementTransactionWSDTO taxPaymentDTO = new TaxPaymentMoneyMovementTransactionWSDTO();
        updateMoneyMovementTransactionDTO(taxPaymentDTO, pMoneyMovementTransaction);
        taxPaymentDTO.guid = pMoneyMovementTransaction.getId().toString();
        taxPaymentDTO.agencyTaxPayerId = pMoneyMovementTransaction.getAgencyTaxpayerId();
        taxPaymentDTO.depositFrequency = pMoneyMovementTransaction.getDepositFrequencyFk();
        taxPaymentDTO.manualPaymentStatus = pMoneyMovementTransaction.getManualPaymentStatus() != null ? pMoneyMovementTransaction.getManualPaymentStatus().name() : "";
        taxPaymentDTO.paymentPeriodBegin =
                pMoneyMovementTransaction.getPaymentPeriodBegin() != null ?
                new Date(pMoneyMovementTransaction.getPaymentPeriodBegin().toLocal().getTimeInMilliseconds()) : null;

        taxPaymentDTO.paymentPeriodEnd =
                pMoneyMovementTransaction.getPaymentPeriodEnd() != null ?
                new Date(pMoneyMovementTransaction.getPaymentPeriodEnd().toLocal().getTimeInMilliseconds()) : null;

        taxPaymentDTO.referenceNumber = pMoneyMovementTransaction.getReferenceNumber();
        taxPaymentDTO.taxPaymentStatus = pMoneyMovementTransaction.getTaxPaymentStatus() != null ? pMoneyMovementTransaction.getTaxPaymentStatus().name() : "";

        taxPaymentDTO.taxPaymentStatusEffectiveDate =
                pMoneyMovementTransaction.getTaxPaymentStatusEffectiveDate() != null ?
                new Date(pMoneyMovementTransaction.getTaxPaymentStatusEffectiveDate().toLocal().getTimeInMilliseconds()) : null;

        taxPaymentDTO.setOnHoldRecords( new ArrayList<PaymentOnHoldReasonWSDTO>(pMoneyMovementTransaction.getTaxPaymentOnHoldReasonCollection().size()));
        for (TaxPaymentOnHoldReason taxPaymentOnHoldReason : pMoneyMovementTransaction.getTaxPaymentOnHoldReasonCollection().sort(TaxPaymentOnHoldReason.EffectiveDate())) {
            PaymentOnHoldReasonWSDTO onHoldDTO = new PaymentOnHoldReasonWSDTO();
            onHoldDTO.onHoldReasonCd = taxPaymentOnHoldReason.getOnHoldReasonCd().name();
            onHoldDTO.effectiveDate = new Date(taxPaymentOnHoldReason.getEffectiveDate().toLocal().getTimeInMilliseconds());
            onHoldDTO.expirationDate =
                    taxPaymentOnHoldReason.getExpirationDate() != null ?
                            new Date(taxPaymentOnHoldReason.getExpirationDate().toLocal().getTimeInMilliseconds()) : null;
            taxPaymentDTO.getOnHoldRecords().add(onHoldDTO);
        }
        return taxPaymentDTO;
    }

    class ACHDetailRecordWSDTOComparator implements Comparator<ACHDetailRecordWSDTO> {
        public int compare(ACHDetailRecordWSDTO a, ACHDetailRecordWSDTO b) {
            return key(a).compareTo(key(b));
        }
        String key(ACHDetailRecordWSDTO dto) {
            return (dto.traceNumber==null?" ":dto.traceNumber) + (dto.recordData==null?" ":dto.recordData); 
        }
    }

    private ArrayList<ACHDetailRecordWSDTO> getACHDetailRecordDTOs(DomainEntitySet<EntryDetailRecord> pEntryDetailRecords) {
        ArrayList<ACHDetailRecordWSDTO> detailRecordDTOs = new ArrayList<ACHDetailRecordWSDTO>(pEntryDetailRecords.size());
        ACHDetailRecordWSDTO detailRecordDTO;

        for (Iterator<EntryDetailRecord> iter = pEntryDetailRecords.iterator(); iter.hasNext();) {
            EntryDetailRecord entryDetailRec = iter.next();
            detailRecordDTO = new ACHDetailRecordWSDTO();
            detailRecordDTO.amount = SpcfUtils.convertToBigDecimal(entryDetailRec.getAmount());
            detailRecordDTO.traceNumber = entryDetailRec.getTraceNumber();
            detailRecordDTO.recordData = entryDetailRec.getRecordData();
            detailRecordDTO.txpRecordData = entryDetailRec.getTxpRecordData();

            detailRecordDTOs.add(detailRecordDTO);
        }
        Collections.sort(detailRecordDTOs, new ACHDetailRecordWSDTOComparator());
        return detailRecordDTOs;
    }

    public static String getQBOEBankReturnStatus(TransactionReturnStatusCode pBankReturnStatus) {
        switch (pBankReturnStatus) {
            case Created:
                return "CR";
            case Error:
                return "ERR";
            case Open:
                return "OPEN";
            case Resolved:
                return "RSLVD";
            default:
                return null;
        }
    }

    @WebMethod
    public PayrollRunWSDTO getPayrollRun(@WebParam(name = "payrollRunID")String pPayrollRunID)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (pPayrollRunID == null) {
                throw new RuntimeException("No Payroll Run Id Specified");
            }
            PayrollRun payrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(pPayrollRunID));
            if (payrollRun == null) {
                throw new RuntimeException("Invalid payrollRunID");
            }
            PayrollRunWSDTO payrollDTO = new PayrollRunWSDTO();
            payrollDTO.id = payrollRun.getId().toString();
            payrollDTO.netAmount = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
            payrollDTO.paycheckCount = payrollRun.getPaycheckCollection().size();
            payrollDTO.paycheckDepositDate = new Date(payrollRun.getPaycheckDate().toLocal().getTimeInMilliseconds());
            payrollDTO.payrollRunDate = new Date(payrollRun.getPayrollRunDate().toLocal().getTimeInMilliseconds());
            payrollDTO.sourceBatchId = payrollRun.getSourcePayRunId();
            payrollDTO.status = payrollRun.getPayrollRunStatus().toString();
            if (payrollRun.getPayrollRunType()!=null) {
                payrollDTO.payrollType=payrollRun.getPayrollRunType().toString();
            }

            PayrollServices.commitUnitOfWork();
            return payrollDTO;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String getFeeAmount(@WebParam(name = "feeCD")String feeCode) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (feeCode == null || feeCode.trim().length() == 0) {
            throw new RuntimeException("No Fee Code is specified");
        }
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Fee> feeList = Application.find(Fee.class, Fee.FeeCd().equalTo(FeeTypeCode.valueOf(feeCode)));
            if (feeList == null) {
                throw new RuntimeException("Fee with the specified code " + feeCode + " doesn't exists");
            }
            Fee fee = feeList.get(0);
            PayrollServices.commitUnitOfWork();
            return SpcfUtils.convertToBigDecimal(fee.getAmount()).toString();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void createFeeTransaction(@WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                     @WebParam(name = "payrollRunId")String payrollRunId,
                                     @WebParam(name = "returnFeeAmount")String returnFeeAmount,
                                     @WebParam(name = "settlementType")String settlementType,
                                     @WebParam(name = "settlementDate")String settlementDate,
                                     @WebParam(name = "pspCompanyBankAccID")String pspCompanyBankAccID,
                                     @WebParam(name = "feeType")String feeTypeCode)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceCompanyID == null || payrollRunId == null
                    || returnFeeAmount == null || settlementType == null || settlementDate == null
                    || feeTypeCode == null) {
                throw new RuntimeException("Any of Source Company ID, Payroll Run ID, Fee Amount, Settlement Type " +
                        "Settlement Date, or feeTypeCode can not be null");
            }

            if (settlementDate != null && settlementDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid refund date format" + settlementDate + ".  Correct format: MM/dd/yyyy");
            }

            Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(sourceCompanyID));
            PayrollRun payrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(settlementDate);
            date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());

            SettlementTypeDTO settlementTypeDTO = DDCodeToPSP.getSettlementTypeDTO(settlementType);

            ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    payrollRun.getSourcePayRunId(), settlementTypeDTO,
                    CalendarUtils.convertToDate(date), new SpcfMoney(returnFeeAmount),
                    DDCodeToPSP.getFeeTypeCode(feeTypeCode), null);

            ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.getMessages().get(0).toString());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void createFeeTransactionBySourceCompanyId(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                     @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                     @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                     @WebParam(name = "returnFeeAmount")String returnFeeAmount,
                                     @WebParam(name = "settlementType")String settlementType,
                                     @WebParam(name = "settlementDate")String settlementDate,
                                     @WebParam(name = "pspCompanyBankAccID")String pspCompanyBankAccID,
                                     @WebParam(name = "feeType")String feeTypeCode)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null || sourcePayrollRunID == null
                    || returnFeeAmount == null || settlementType == null || settlementDate == null
                    || feeTypeCode == null) {
                throw new RuntimeException("Any of Source Company ID, Payroll Run ID, Fee Amount, Settlement Type " +
                        "Settlement Date, or feeTypeCode can not be null");
            }

            if (settlementDate != null && settlementDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid refund date format" + settlementDate + ".  Correct format: MM/dd/yyyy");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(settlementDate);
            date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());

            SettlementTypeDTO settlementTypeDTO = DDCodeToPSP.getSettlementTypeDTO(settlementType);

            ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    sourcePayrollRunID, settlementTypeDTO,
                    CalendarUtils.convertToDate(date), new SpcfMoney(returnFeeAmount),
                    DDCodeToPSP.getFeeTypeCode(feeTypeCode), null);

            ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.getMessages().get(0).toString());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public TransactionWSDTO createFeeTransactionBySourceCompanyIdForAgent(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                          @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                          @WebParam(name = "sourcePayrollRunID")String sourcePayrollRunID,
                                                                          @WebParam(name = "returnFeeAmount")String returnFeeAmount,
                                                                          @WebParam(name = "settlementType")String settlementType,
                                                                          @WebParam(name = "settlementDate")String settlementDate,
                                                                          @WebParam(name = "pspCompanyBankAccID")String pspCompanyBankAccID,
                                                                          @WebParam(name = "feeType")String feeTypeCode,
                                                                          @WebParam(name = "memo")String memo)
            throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            AuthRole foundRole = AuthRole.findRole("RMRep");
            ProcessResult processResult1 = PayrollServices.userManager.addUser("TestAdapter", Arrays.asList(foundRole.getRoleId()), "TestAdapter", "TestAdapter");
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            AuthUser user = (AuthUser) processResult1.getResult();
            user = Application.findById(AuthUser.class, user.getId());
            PayrollServices.commitUnitOfWork();
            //Set PSP Principal for the User
            PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null || sourcePayrollRunID == null
                    || returnFeeAmount == null || settlementType == null || settlementDate == null
                    || feeTypeCode == null) {
                throw new RuntimeException("Any of Source Company ID, Payroll Run ID, Fee Amount, Settlement Type " +
                        "Settlement Date, or feeTypeCode can not be null");
            }

            if (settlementDate != null && settlementDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid refund date format" + settlementDate + ".  Correct format: MM/dd/yyyy");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedRunDate = dateFormat.parse(settlementDate);
            date.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());

            SettlementTypeDTO settlementTypeDTO = DDCodeToPSP.getSettlementTypeDTO(settlementType);

            ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    payrollRun.getSourcePayRunId(), settlementTypeDTO,
                    CalendarUtils.convertToDate(date), new SpcfMoney(returnFeeAmount),
                    DDCodeToPSP.getFeeTypeCode(feeTypeCode), memo);

            ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.getMessages().get(0).toString());
            }
            FinancialTransaction finTx = (FinancialTransaction) processResult.getResult().getFirst();
            TransactionWSDTO txDTO = new TransactionWSDTO();
            txDTO.id = finTx.getId().toString();
            String sku = finTx.getSku();
            OfferingServiceChargeType offeringServiceCharge = null;
            if (null != sku) {
                offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
            }
            txDTO.transactionType = DDCodeToPSP.getQBOETransactionTypeCode(finTx.getTransactionType().getTransactionTypeCd(), offeringServiceCharge);
            txDTO.currentState = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            txDTO.transactionAmount = SpcfUtils.convertToBigDecimal(finTx.getFinancialTransactionAmount());
            txDTO.settlementType = finTx.getSettlementTypeCd().toString();
            txDTO.settlementDate = new Date(finTx.getSettlementDate().toLocal().getTimeInMilliseconds());
            txDTO.offloadStatus = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            if (null != finTx.getCreditBankAccount() && null != finTx.getCreditBankAccountType()) {
                txDTO.creditBankAccount = getBankAccountDTO(finTx.getCreditBankAccount(), finTx.getCreditBankAccountType());
            }
            if (null != finTx.getDebitBankAccount() && null != finTx.getDebitBankAccountType()) {
                txDTO.debitBankAccount = getBankAccountDTO(finTx.getDebitBankAccount(), finTx.getDebitBankAccountType());
            }
            if (null != finTx.getMoneyMovementTransaction()) {
                txDTO.moneyMovementTransaction = getMoneyMovementTransactionDTO(finTx.getMoneyMovementTransaction());
            }
            PayrollServices.commitUnitOfWork();
            return txDTO;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateTransactionDate(
            @WebParam(name = "txnId")String txnId, @WebParam(name = "days")int days) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (txnId == null) {
                throw new RuntimeException("transaction id can not be null");
            }
            SpcfCalendar settlementDate = null;

            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class,
                    SpcfUniqueId.createInstance(txnId));

            settlementDate = financialTransaction.getSettlementDate().toLocal().copy();
            CalendarUtils.addBusinessDays(settlementDate, days);
            financialTransaction.setSettlementDate(settlementDate);
            SpcfCalendar initDate = settlementDate.copy();
            TransactionTypeCode strTxnType = financialTransaction.getTransactionType().getTransactionTypeCd();
            if (strTxnType.equals(TransactionTypeCode.EmployeeDdCredit)) {
                CalendarUtils.addBusinessDays(initDate, -2);
            } else {
                CalendarUtils.addBusinessDays(initDate, -1);
            }

            MoneyMovementTransaction mmTxn = financialTransaction.getMoneyMovementTransaction();

            if(mmTxn != null){
                mmTxn.setInitiationDate(initDate);
                
                DomainEntitySet<EntryDetailRecord> entryDetailList = mmTxn.getEntryDetailRecordCollection();
                for(EntryDetailRecord entryDetailRecord : entryDetailList){
                    entryDetailRecord.setInitiationDate(initDate);
                    entryDetailRecord.setSettlementDate(settlementDate);
                    Application.save(entryDetailRecord);
                }
            }

            Application.save(financialTransaction);

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updatePayrollDate(
            @WebParam(name = "payrollRunId")String payrollRunId, @WebParam(name = "days")int days) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (payrollRunId == null) {
                throw new RuntimeException("PayrollRun id can not be null");
            }

            PayrollRun payrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));

            SpcfCalendar payrollRunDate = payrollRun.getPayrollRunDate().toLocal().copy();
            CalendarUtils.addBusinessDays(payrollRunDate, days);

            SpcfCalendar paycheckDepositDate = payrollRun.getPaycheckDate().toLocal().copy();
            CalendarUtils.addBusinessDays(paycheckDepositDate, days);

            payrollRun.setPayrollRunDate(payrollRunDate);
            payrollRun.setPaycheckDate(paycheckDepositDate);
            payrollRun = Application.save(payrollRun);

            com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> finTxs = payrollRun.getFinancialTransactionCollection();

            SpcfCalendar settlementDate = null;
            for (FinancialTransaction financialTransaction : finTxs) {
                settlementDate = financialTransaction.getSettlementDate().toLocal().copy();
                CalendarUtils.addBusinessDays(settlementDate, days);
                financialTransaction.setSettlementDate(settlementDate);
                SpcfCalendar initDate = settlementDate.copy();
                TransactionTypeCode strTxnType = financialTransaction.getTransactionType().getTransactionTypeCd();
                if (strTxnType.equals(TransactionTypeCode.EmployeeDdCredit)) {
                    CalendarUtils.addBusinessDays(initDate, -2);
                }
                else {
                    CalendarUtils.addBusinessDays(initDate, -1);
                }
                MoneyMovementTransaction moneyMovementTx = financialTransaction.getMoneyMovementTransaction();
                if (moneyMovementTx != null) {
                    moneyMovementTx.setInitiationDate(initDate);

                    DomainEntitySet<EntryDetailRecord> entryDetailRecords = moneyMovementTx.getEntryDetailRecordCollection();
                    for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
                        entryDetailRecord.setInitiationDate(moneyMovementTx.getInitiationDate());
                        entryDetailRecord.setSettlementDate(settlementDate);
                        Application.save(entryDetailRecord);
                    }
                }
                Application.save(financialTransaction);
            }

            DomainEntitySet<TransactionReturn> txnReturns = TransactionReturn.findTransactionReturns(
                    payrollRun.getSourcePayRunId(), payrollRun.getCompany());

            SpcfCalendar returnStatusEffDate = null;
            for (TransactionReturn txnReturn : txnReturns) {
                returnStatusEffDate = txnReturn.getReturnStatusEffectiveDate().toLocal().copy();
                CalendarUtils.addBusinessDays(returnStatusEffDate, days);
                txnReturn.setReturnStatusEffectiveDate(returnStatusEffDate);
                Application.save(txnReturn);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<EntryDetailRecordWSDTO> getEntryDetailRecords(@WebParam(name = "fromDate")String fromDate,
                                                                    @WebParam(name = "toDate")String toDate,
                                                                    @WebParam(name = "offloadGroupCd")String offloadGroupCd,
                                                                    @WebParam(name = "firstResult")int pFirstResult,
                                                                    @WebParam(name = "maxResults")int pMaxResults)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (offloadGroupCd == null) {
                throw new RuntimeException("Offload Group Cd can not be null");
            }

            SpcfCalendar fromdate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfCalendar todate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedFromDate = dateFormat.parse(fromDate);
            fromdate.setValues(parsedFromDate.getYear(), parsedFromDate.getMonth(), parsedFromDate.getDay());

            SpcfCalendar parsedToDate = dateFormat.parse(toDate);
            todate.setValues(parsedToDate.getYear(), parsedToDate.getMonth(), parsedToDate.getDay());
            // since there is no truncate operation on the date available now,
            // add one day and find the entries with created date less than todate.
            todate.addDays(1);

            Criterion<EntryDetailRecord> where =
                                EntryDetailRecord.MoneyMovementTransaction().OffloadBatch().OffloadGroup().OffloadGroupCd().equalTo(offloadGroupCd)
                                  .And(EntryDetailRecord.MoneyMovementTransaction().CreatedDate().greaterOrEqualThan(fromdate))
                                  .And(EntryDetailRecord.MoneyMovementTransaction().CreatedDate().lessThan(todate));
            Expression query = null;
            if (pMaxResults > 0) {
                query = new Query<EntryDetailRecord>().Where(where).LimitResults( pFirstResult, pMaxResults);
            } else {
                query = new Query<EntryDetailRecord>().Where(where);      
            }
            DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, query);

            Collection<EntryDetailRecordWSDTO> detailRecordDTOs = buildEntityDetailRecords(entryDetailRecords, pFirstResult, pMaxResults);

            PayrollServices.commitUnitOfWork();
            return detailRecordDTOs;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<EntryDetailRecordWSDTO> buildEntityDetailRecords(DomainEntitySet<EntryDetailRecord> pEntryDetailRecords,
                                                                        int pFirstResult, int pMaxResults) throws Exception {
        Collection<EntryDetailRecordWSDTO> txDTOs = new ArrayList<EntryDetailRecordWSDTO>(pEntryDetailRecords.size());
        EntryDetailRecordWSDTO detailRecordDTO = null;

        for (EntryDetailRecord entryDetailRecord : pEntryDetailRecords) {
            detailRecordDTO = new EntryDetailRecordWSDTO();
            MoneyMovementTransaction mmTxn = entryDetailRecord.getMoneyMovementTransaction();

            DomainEntitySet<TransactionReturn> txRetruns = Application.find(TransactionReturn.class, TransactionReturn.MoneyMovementTransaction().equalTo(mmTxn));

            Collection<BankReturnWSDTO> bankRetrunDTOs = buildBankReturnResponse(txRetruns);

            if (bankRetrunDTOs != null && bankRetrunDTOs.size() > 0) {
                detailRecordDTO.isBankReturnsExists = true;
                detailRecordDTO.bankReturns = bankRetrunDTOs;
            }

            detailRecordDTO.mmTransactionId = mmTxn.getId().toString();
            detailRecordDTO.amount = SpcfUtils.convertToBigDecimal(entryDetailRecord.getAmount());
            if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Credit)) {
                detailRecordDTO.creditDebitIndicator = "C";
            }
            else {
                detailRecordDTO.creditDebitIndicator = "D";
            }
            detailRecordDTO.traceNumber = entryDetailRecord.getTraceNumber();

            detailRecordDTO.settlementDate = new Date(mmTxn.getDueDate().toLocal().getTimeInMilliseconds());
            detailRecordDTO.companyId = entryDetailRecord.getCompany().getSourceCompanyId();

            BankAccountWSDTO bankAccount = null;
            if (entryDetailRecord.getRecordData() != null) {
                bankAccount = new BankAccountWSDTO();
                String recordData = entryDetailRecord.getRecordData();
                String transactionCode = recordData.substring(1, 3);

                AchTransactionCode txCode = AchTransactionCode.findAchTransactionCode(transactionCode);
                switch (txCode.getAchAccountTypeCd()) {
                    case Checking:
                        bankAccount.bankAccountType = "C";
                        break;
                    case Savings:
                        bankAccount.bankAccountType = "S";
                        break;
                    case Ledger:
                        bankAccount.bankAccountType = "G";
                        break;
                    case Loan:
                        bankAccount.bankAccountType = "L";
                }

                bankAccount.routingNumber = recordData.substring(3, 12).trim();
                bankAccount.accountNumber = recordData.substring(12, 29).trim();
                detailRecordDTO.individualName = recordData.substring(54, 76).trim();
            }
            else {
                bankAccount = new BankAccountWSDTO();
                BankAccount intuitBankAccount = entryDetailRecord.getIntuitBankAccount().getBankAccount();
                bankAccount.routingNumber = intuitBankAccount.getRoutingNumber();
                bankAccount.accountNumber = intuitBankAccount.getAccountNumber();
                if (intuitBankAccount.getACHAccountTypeCd().equals(ACHBankAccountType.Checking)) {
                    bankAccount.bankAccountType = "C";
                }
                else if (intuitBankAccount.getACHAccountTypeCd().equals(ACHBankAccountType.Savings)) {
                    bankAccount.bankAccountType = "S";
                }
                else if (intuitBankAccount.getACHAccountTypeCd().equals(ACHBankAccountType.Ledger)) {
                    bankAccount.bankAccountType = "G";
                }
                else if (intuitBankAccount.getACHAccountTypeCd().equals(ACHBankAccountType.Loan)) {
                    bankAccount.bankAccountType = "L";
                }

                detailRecordDTO.individualName = entryDetailRecord.getIntuitBankAccount().getDescription();
            }
            detailRecordDTO.bankAccount = bankAccount;
            txDTOs.add(detailRecordDTO);
        }
        return txDTOs;
    }

    @WebMethod
    public void createReebillFeeTransaction(@WebParam(name = "feeDebitTransactionId")String feeDebitTransactionId,
                                            @WebParam(name = "overrideAmount")String overrideAmount)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (feeDebitTransactionId == null) {
                throw new RuntimeException("Transaction Id can not be null");
            }

            SpcfMoney amount = null;

            if(overrideAmount != null){
                amount = new SpcfMoney(overrideAmount);
            }

            RebillFeeTransactionDTO rebillFeeTransactionDTO = new RebillFeeTransactionDTO(feeDebitTransactionId, amount);

            ProcessResult processResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillFeeTransactionDTO);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.getMessages().get(0).toString());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addPaymentOnHoldReason(@WebParam(name = "taxPaymentMoneyMovementTransactionID") String pMoneyMovementTransactionID,
                                       @WebParam(name = "taxPaymentOnHoldReasonCode") String pTaxPaymentOnHoldReasonCd) {
        if (pMoneyMovementTransactionID == null) {
            throw new RuntimeException("taxPaymentMoneyMovementTransactionID cannot be null");
        }

        if (pTaxPaymentOnHoldReasonCd == null) {
            throw new RuntimeException("taxPaymentOnHoldReasonCode cannot be null, must be one of: " + PaymentOnHoldReason.values());
        }

        PaymentOnHoldReason paymentOnHoldReason = null;
        try { paymentOnHoldReason = PaymentOnHoldReason.valueOf(pTaxPaymentOnHoldReasonCd); }
        catch(IllegalArgumentException iae) {
            throw new RuntimeException("taxPaymentOnHoldReasonCode - invalid value: " + pTaxPaymentOnHoldReasonCd + " - must be one of: " + PaymentOnHoldReason.values());
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<MoneyMovementTransaction> mmts =
                    Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.createInstance(pMoneyMovementTransactionID)));
            if (mmts.size() == 0) {
                throw new RuntimeException("No money movement transactions found with ID: " + pMoneyMovementTransactionID);
            }

            ProcessResult pr = PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmts.get(0), paymentOnHoldReason);
            if (!pr.isSuccess()) {
                throw new RuntimeException("Error: "+ pr);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void expirePaymentOnHoldReason(@WebParam(name = "taxPaymentMoneyMovementTransactionID") String pMoneyMovementTransactionID,
                                       @WebParam(name = "taxPaymentOnHoldReasonCode") String pTaxPaymentOnHoldReasonCd) {
        if (pMoneyMovementTransactionID == null) {
            throw new RuntimeException("taxPaymentMoneyMovementTransactionID cannot be null");
        }

        if (pTaxPaymentOnHoldReasonCd == null) {
            throw new RuntimeException("taxPaymentOnHoldReasonCode cannot be null, must be one of: " + PaymentOnHoldReason.values());
        }

        PaymentOnHoldReason paymentOnHoldReason = null;
        try { paymentOnHoldReason = PaymentOnHoldReason.valueOf(pTaxPaymentOnHoldReasonCd); }
        catch(IllegalArgumentException iae) {
            throw new RuntimeException("taxPaymentOnHoldReasonCode - invalid value: " + pTaxPaymentOnHoldReasonCd + " - must be one of: " + PaymentOnHoldReason.values());
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<MoneyMovementTransaction> mmts =
                    Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.Id().equalTo(SpcfUniqueId.createInstance(pMoneyMovementTransactionID)));
            if (mmts.size() == 0) {
                throw new RuntimeException("No money movement transactions found with ID: " + pMoneyMovementTransactionID);
            }

            ProcessResult pr = PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(mmts.get(0), paymentOnHoldReason);
            if (!pr.isSuccess()) {
                throw new RuntimeException("Error: " + pr);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
	
	@WebMethod
	public void voidTransaction(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                @WebParam (name="sourceCompanyID") String sourceCompanyID,
                               @WebParam (name="financialTransactionID") String financialTransactionID,
                                @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID,
                                @WebParam (name="transactionType") String transactionType)  throws Exception {
       PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
           if (sourceSystemCD == null || sourceCompanyID == null ) {
                throw new RuntimeException("Any of SourceSystemCD or SourceCompanyID can not be null");
            }

            if (financialTransactionID == null && (sourcePayrollRunID == null || transactionType == null)) {
                throw new RuntimeException("If financialTransactionID is not specified, both sourcePayrollRunID and transactionType must be specified");
            }

            if (transactionType != null) {
                Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
                if (company == null) {
                    throw new RuntimeException("Invalid sourceCompanyID");
                }

                PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourcePayrollRunID");
                }

                Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
                Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();
                types.add(TransactionTypeCode.valueOf(transactionType));
                states.add(TransactionStateCode.Completed);

                DomainEntitySet<FinancialTransaction> finTxs =
                        FinancialTransaction.findFinancialTransactionsForPayrollByTypeAndState(
                                payrollRun, types, states);
                 if (finTxs != null && finTxs.size() > 0) {
                    financialTransactionID = finTxs.get(0).getId().toString();

                } else if (financialTransactionID == null){
                    throw new RuntimeException("No financial transactions exist for the specified transaction type "
                            + transactionType + "and for the payroll "+ sourcePayrollRunID);
                }
           }

            ProcessResult result = PayrollServices.financialTransactionManager.voidTransaction(SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, financialTransactionID);

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:"+result.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addFeeTransferTransaction(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID,
                                @WebParam (name="amount") String amount,
                                @WebParam (name="offeringServiceCharge") String offeringServiceCharge)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null || sourcePayrollRunID == null || amount == null|| offeringServiceCharge == null ) {
                throw new RuntimeException("Any of SourceSystemCD, SourceCompanyID, sourcePayrollRunID, amount " +
                        "or offeringServiceCharge can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            FeeTransferDTO feeTransferDTO = new FeeTransferDTO();
            if(payrollRun != null) {
                feeTransferDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            } else {
                feeTransferDTO.setSourcePayrollRunId(sourcePayrollRunID);
            }
            feeTransferDTO.setFinancialTxAmt(new SpcfMoney(amount));
            feeTransferDTO.setFeeTypeCode(OfferingServiceChargeType.valueOf(offeringServiceCharge));
            ProcessResult result = PayrollServices.financialTransactionManager.addFeeTransferTransaction(SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, feeTransferDTO);

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:"+result.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addWriteOffBadDebtTransaction(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null || sourcePayrollRunID == null ) {
                throw new RuntimeException("Any of SourceSystemCD, SourceCompanyID or sourcePayrollRunID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            ProcessResult result = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, payrollRun.getSourcePayRunId());

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:"+result.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    public void addBadDebtRecoveryTransactionCore(String sourceSystemCD,
                                                  String sourceCompanyID,
                                                  String sourcePayrollRunID,
                                                  String transactionType,
                                                  String transactionID,
                                                  String amount,
                                                  String txDate,
                                                  String settlementType,
                                                  Boolean isCustomer) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null || sourcePayrollRunID == null
                    || txDate == null || settlementType == null) {
                throw new RuntimeException("Any of SourceSystemCD, SourceCompanyID, sourcePayrollRunID," +
                        "txDate or settlementType can not be null");
            }

            if (transactionID == null && transactionType == null) {
                throw new RuntimeException("Atleast one of transactionID or transactionType must be specified");    
            }

            PayrollRun payrollRun = null;
            if (transactionType != null) {
                Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
                if (company == null) {
                    throw new RuntimeException("Invalid sourceCompanyID");
                }

                payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourcePayrollRunID");
                }

                DomainEntitySet<FinancialTransaction> finTxs = getPayrollFinancialTransactions(company, payrollRun, transactionType);
                if (finTxs != null && finTxs.size() > 0) {
                    FinancialTransaction originalTxn = finTxs.get(0);
                    transactionID = originalTxn.getId().toString();
                    if (amount == null) {
                        amount = originalTxn.getFinancialTransactionAmount().toString();
                    }

                } else {
                    throw new RuntimeException("No financial transactions exist for the specified transaction type "
                            + transactionType + "and for the payroll "+ sourcePayrollRunID);
                }
            }

            BadDebtRecoverDTO badDebtRecoverDTO = new BadDebtRecoverDTO();
            if(payrollRun != null) {
                badDebtRecoverDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            } else {
                badDebtRecoverDTO.setSourcePayrollRunId(sourcePayrollRunID);
            }
            badDebtRecoverDTO.setOriginalTransactionId(transactionID);
            badDebtRecoverDTO.setFinancialTxAmt(new SpcfMoney(amount));
            badDebtRecoverDTO.setTxDate(new DateDTO(txDate));
            badDebtRecoverDTO.setSettlementType(SettlementTypeDTO.valueOf(settlementType));
            badDebtRecoverDTO.setCustomer(isCustomer);    
            ProcessResult result = PayrollServices.financialTransactionManager.addRecoverBadDebtTransaction(
                    SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, badDebtRecoverDTO);

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:"+result.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addRepaymentTransaction(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID,
                                @WebParam (name="transactionType") String transactionType,
                                @WebParam (name="transactionID") String transactionID,
                                @WebParam (name="amount") String amount,
                                @WebParam (name="txDate") String txDate,
                                @WebParam (name="settlementType") String settlementType)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null ||  txDate == null || settlementType == null) {
                throw new RuntimeException("Any of SourceSystemCD, SourceCompanyID, txDate or settlementType can not be null");
            }

            if (transactionID == null && (sourcePayrollRunID == null || transactionType == null)) {
                throw new RuntimeException("If transactionID is not specified, both sourcePayrollRunID and transactionType must be specified");
            }

            if (transactionType != null) {
                Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
                if (company == null) {
                    throw new RuntimeException("Invalid sourceCompanyID");
                }

                PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

                if (payrollRun == null) {
                    throw new RuntimeException("Invalid sourcePayrollRunID");
                }

                DomainEntitySet<FinancialTransaction> finTxs = getPayrollFinancialTransactions(company, payrollRun, transactionType);
                if (finTxs != null && finTxs.size() > 0) {
                    FinancialTransaction originalTxn = finTxs.get(0);
                    transactionID = originalTxn.getId().toString();
                    if (amount == null) {
                        amount = originalTxn.getFinancialTransactionAmount().toString();
                    }

                } else {
                    throw new RuntimeException("No financial transactions exist for the specified transaction type "
                            + transactionType + "and for the payroll "+ sourcePayrollRunID);
                }
            }

            RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
            redebitDTO.setAmount(new SpcfMoney(amount));
            redebitDTO.setInitiationDate(new DateDTO(txDate));
            redebitDTO.setOriginalFinancialTxId(transactionID);
            redebitDTO.setSettlementType(SettlementTypeDTO.valueOf(settlementType));

            List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
            collectionOfRedebitImpounds.add(redebitDTO);

            ProcessResult result = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                    SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, collectionOfRedebitImpounds);

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:"+result.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addPrefundingTransaction(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID,
                                @WebParam (name="prefundDate") String prefundDate,
                                @WebParam (name="settlementType") String settlementType,
                                @WebParam(name = "prefundDTOs")Collection<PrefundWSDTO> prefundDTOs)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = new ArrayList<PrefundPayrollTransactionDTO>();
            for (PrefundWSDTO prefundDTO : prefundDTOs) {
                transactionDTOs.add(findDebitTransactions(payrollRun, prefundDTO));
            }

            SpcfCalendar date = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("MM/dd/yyyy");
            SpcfCalendar parsedPrefundDate = dateFormat.parse(prefundDate);
            date.setValues(parsedPrefundDate.getYear(), parsedPrefundDate.getMonth(), parsedPrefundDate.getDay(), 0, 0, 0, 0);
                    
            ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, payrollRun.getSourcePayRunId(), SettlementType.valueOf(settlementType), date, transactionDTOs);

            if (!prefundPayrollCore.isSuccess()) {
                throw new RuntimeException("Error:" + prefundPayrollCore.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private DomainEntitySet<FinancialTransaction> getPayrollFinancialTransactions(Company pCompany, PayrollRun pPayrollRun, String pTransactionType) {
        DomainEntitySet<FinancialTransaction> finTxs = null;
        TransactionType transactionType = PayrollServices.entityFinder.findById(TransactionType.class, TransactionTypeCode.valueOf(pTransactionType));
        if (transactionType == null) {
            throw new RuntimeException("Invalid transactionType: " + transactionType);
        }

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                          .Where(FinancialTransaction.Company().equalTo(pCompany)
                                 .And(FinancialTransaction.TransactionType().equalTo(transactionType)
                                 .And(FinancialTransaction.PayrollRun().equalTo(pPayrollRun))))
                          .OrderBy(FinancialTransaction.TransactionType(), FinancialTransaction.FinancialTransactionAmount());

        finTxs = Application.find(FinancialTransaction.class, query);

        return finTxs;
    }

    private PrefundPayrollTransactionDTO findDebitTransactions(PayrollRun payrollRun, PrefundWSDTO prefundDTO) throws Exception {
        PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = new PrefundPayrollTransactionDTO();

        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(payrollRun)
                            .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.ACH))
                            .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.valueOf(prefundDTO.transactionType)))
                            .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney(prefundDTO.originalTransactionAmount.toString())))));
        if(financialTransactions == null || financialTransactions.size() == 0 || financialTransactions.size() > 1){
            throw new Exception("Transaction with amount $" + prefundDTO.originalTransactionAmount.toString() + " and type " + prefundDTO.transactionType + " exists 0 or more than 1 times");
        }


        prefundPayrollTransactionDTO.setOriginalTransactionId(financialTransactions.get(0).getId().toString());
        prefundPayrollTransactionDTO.setTransactionAmount(new SpcfMoney(prefundDTO.newTransactionAmount.toString()));

        BillingDetail billingDetail = financialTransactions.get(0).getBillingDetail();
        // find the tax transaction
        if(billingDetail != null && prefundDTO.originalTaxTransactionAmount != null){
            DomainEntitySet<FinancialTransaction> taxDebitTransactions = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                    new TransactionStateCode[]{TransactionStateCode.Created});
            for (FinancialTransaction taxTransaction : taxDebitTransactions) {
                if(billingDetail ==  taxTransaction.getBillingDetail() && taxTransaction.getFinancialTransactionAmount().equals(new SpcfMoney(prefundDTO.originalTaxTransactionAmount.toString()))){
                    prefundPayrollTransactionDTO.setOriginalTaxTransactionId(taxTransaction.getId().toString());
                    prefundPayrollTransactionDTO.setTaxTransactionAmount(new SpcfMoney(prefundDTO.newTaxTransactionAmount.toString()));
                }
            }
        }

        return prefundPayrollTransactionDTO;
    }

    @WebMethod
    public void addBadDebtRecoveryTransaction(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                              @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                              @WebParam(name = "sourcePayrollRunID") String sourcePayrollRunID,
                                              @WebParam(name = "transactionType") String transactionType,
                                              @WebParam(name = "transactionID") String transactionID,
                                              @WebParam(name = "amount") String amount,
                                              @WebParam(name = "txDate") String txDate,
                                              @WebParam(name = "settlementType") String settlementType) throws Exception {
        addBadDebtRecoveryTransactionCore(sourceSystemCD, sourceCompanyID, sourcePayrollRunID, transactionType, transactionID, amount, txDate, settlementType, null);
    }

    /**
     * Adds a transaction to recover bad debt from customer or collections agency
     *
     * @param sourceSystemCD
     * (String) Source System Code
     * @param sourceCompanyID
     * (String) Source System Id of the company
     * @param sourcePayrollRunID
     * (String) Id of the payrollRun
     * @param transactionID
     * (String) Transaciton Id
     * @param amount
     * (String) Amount of recovery
     * @param txDate
     * (String) Transaction Date
     * @param settlementType
     * (String) Settlement Type
     * @param isCustomer
     * (Boolean) Is this recovery from Customer?
     * @throws Exception
     */
    @WebMethod
    public void addBadDebtRecoveryTransactionCustomerAgency(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                            @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                                            @WebParam(name = "sourcePayrollRunID") String sourcePayrollRunID,
                                                            @WebParam(name = "transactionType") String transactionType,
                                                            @WebParam(name = "transactionID") String transactionID,
                                                            @WebParam(name = "amount") String amount,
                                                            @WebParam(name = "txDate") String txDate,
                                                            @WebParam(name = "settlementType") String settlementType,
                                                            @WebParam(name = "isCustomer") Boolean isCustomer,
                                                            @WebParam(name = "expenseAmount") String expenseAmount,
                                                            @WebParam(name = "collectionSettlementDate") String collectionSettlementDate) throws Exception {
        try {
            addBadDebtRecoveryTransactionCore(sourceSystemCD, sourceCompanyID, sourcePayrollRunID, transactionType, transactionID, amount, txDate, settlementType, isCustomer);
            DateDTO settlementDateDto = new DateDTO(collectionSettlementDate);
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);
            ProcessResult processResult = PayrollServices.financialTransactionManager.recordCollectionAgencyExpense(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, payrollRun.getId().toString(), new SpcfMoney(expenseAmount), settlementDateDto);

            if (!processResult.isSuccess()) {
                throw new RuntimeException("Error:" + processResult.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<TransactionWSDTO> refundERPayable(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                        @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                                        @WebParam(name = "transactionType") String transactionType,
                                                        @WebParam(name = "transactionState") String transactionState,
                                                        @WebParam(name = "settlementType") String settlementType,
                                                        @WebParam(name = "refundAmount") String refundAmount,
                                                        @WebParam(name = "refundDate") String refundDate) throws Exception {
        return refundTransaction(sourceSystemCD, sourceCompanyID, transactionType, transactionState, null, settlementType, refundAmount, refundDate, "ERTransactionRefund");
    }

    @WebMethod
    public void reissuePayrollTaxPayment(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                         @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                         @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);

            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            DomainEntitySet<FinancialTransaction> taxVoidTransferFTs = payrollRun.getFinancialTransactions(TransactionTypeCode.IntuitTaxVoidTransfer);

            if(taxVoidTransferFTs.size() == 0){
                throw new RuntimeException("No transactions with type = TransactionTypeCode.IntuitTaxVoidTransfer for the payrollRunId:"+sourcePayrollRunID);
            }

            if(taxVoidTransferFTs.size() > 1){
                throw new RuntimeException("No transactions with type = TransactionTypeCode.IntuitTaxVoidTransfer for the payrollRunId:"+sourcePayrollRunID);
            }

            ProcessResult reissuePayrollTaxResult = PayrollServices.payrollManager.reissuePayrollTaxPayment(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, payrollRun.getSourcePayRunId(), taxVoidTransferFTs.get(0).getId().toString());

            if (!reissuePayrollTaxResult.isSuccess()) {
                throw new RuntimeException("Error:" + reissuePayrollTaxResult.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void voidPayrollTaxPayment(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                      @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                      @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);
            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            ProcessResult voidPayrollTaxResult = PayrollServices.payrollManager.voidPayrollTaxPayment(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, payrollRun.getId().toString());

            if (!voidPayrollTaxResult.isSuccess()) {
                throw new RuntimeException("Error:" + voidPayrollTaxResult.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void applyERPayableToBalanceDue(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                           @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                           @WebParam (name="sourcePayrollRunID") String sourcePayrollRunID,
                                           @WebParam (name="amountToApply") String amountToApply)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            PayrollRun payrollRun = findPayrollRunBySourceId(company, sourcePayrollRunID);
            if (payrollRun == null) {
                throw new RuntimeException("Invalid sourcePayrollRunID");
            }

            SpcfDecimal amount = new SpcfMoney(amountToApply);

            if (amountToApply == null || !amount.isGreaterThan(SpcfMoney.ZERO)){
                throw new RuntimeException("Invalid amountToApply, it has to be positive");
            }

            ProcessResult applyERPayableResult = PayrollServices.payrollManager.applyERPayableToBalanceDue(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, payrollRun.getId().toString(), amount);

            if (!applyERPayableResult.isSuccess()) {
                throw new RuntimeException("Error:" + applyERPayableResult.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void refundEmployerPayable(@WebParam (name="sourceSystemCD") String sourceSystemCD,
                                @WebParam (name="sourceCompanyID") String sourceCompanyID,
                                @WebParam (name="amount") Double amount,
                                @WebParam (name="settlementType") String settlementType)  throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID,
                    SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            SpcfMoney refundAmount;
            if (amount == null || amount == 0) {
                refundAmount = new SpcfMoney(LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable));
            } else {
                refundAmount = new SpcfMoney(amount.toString());
            }

            SettlementTypeDTO refundSettlementType;
            if (settlementType == null) {
                refundSettlementType = SettlementTypeDTO.ACH;
            } else {
                refundSettlementType = SettlementTypeDTO.valueOf(settlementType);
            }

            ProcessResult refundERPayableResult = PayrollServices.financialTransactionManager.refundERPayable(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, refundSettlementType, refundAmount);

            if (!refundERPayableResult.isSuccess()) {
                throw new RuntimeException("Error:" + refundERPayableResult.getMessages().get(0).getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void offloadTransaction(@WebParam (name="transactionId") String transactionId) {
        if(transactionId == null || transactionId.trim().length() == 0) {
            throw new RuntimeException("TransactionId must be set.");
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));

            if(financialTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' not found.");
            }

            MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            if(moneyMovementTransaction == null ||
                    moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.None ||
                    moneyMovementTransaction.getStatus() != PaymentStatus.Created) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' cannot be offloaded.");
            }

            Company company = moneyMovementTransaction.getCompany();

            SpcfCalendar initDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(initDate);
            if(company.getOffloadGroup().isBeforeActualCutoffTime(initDate)) {
                CalendarUtils.addBusinessDays(initDate, -1);
            }
            moneyMovementTransaction.updateInitiationDate(initDate);

            SpcfCalendar settlementDate = initDate.copy();
            CalendarUtils.addBusinessDays(settlementDate, 1);
            for (FinancialTransaction transaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                transaction.setSettlementDate(settlementDate);
            }
            Application.save(moneyMovementTransaction);
            PayrollServices.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            String offloadId = offloader.offloadAndPostOffload(company.getOffloadGroup().getOffloadGroupCd(), initDate);

            PayrollServices.beginUnitOfWork();
            // complete nacha files
            DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, NACHAFile.OffloadBatch().Id().equalTo(SpcfUniqueId.createInstance(offloadId)).And(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
            for (NACHAFile nachaFile : nachaFiles) {
                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
                BatchUtils.moveFile(nachaFile.getFileName(), archiveDir);
                nachaFile.setStatus(NACHAFileStatus.Archived);
                Application.save(nachaFile);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void offloadTransactionACHPayment(@WebParam (name="transactionId") String transactionId) {
        if(transactionId == null || transactionId.trim().length() == 0) {
            throw new RuntimeException("TransactionId must be set.");
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));

            if(financialTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' not found.");
            }

            MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            if(moneyMovementTransaction == null ||
                    (moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.ReadyToSend && moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.ATFFinalized) ||
                    moneyMovementTransaction.getStatus() != PaymentStatus.Created) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' cannot be offloaded.");
            }

            SpcfCalendar initDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(initDate);
            if(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.TAXPAYMENT).isBeforeCutoffTime(initDate)) {
                CalendarUtils.addBusinessDays(initDate, -1);
            }
            moneyMovementTransaction.updateInitiationDate(initDate);

            SpcfCalendar settlementDate = initDate.copy();
            CalendarUtils.addBusinessDays(settlementDate, 2);
            for (FinancialTransaction transaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                transaction.setSettlementDate(settlementDate);
            }
            Application.save(moneyMovementTransaction);
            PayrollServices.commitUnitOfWork();

            OffloadACHTransactions offloader = new OffloadACHTransactions();
            String offloadId = offloader.offloadAndPostOffload(OffloadGroup.Codes.TAXPAYMENT, initDate, ACHFileType.Tax);

            PayrollServices.beginUnitOfWork();
            // complete nacha files
            DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, NACHAFile.OffloadBatch().Id().equalTo(SpcfUniqueId.createInstance(offloadId)));
            for (NACHAFile nachaFile : nachaFiles) {
                String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
                BatchUtils.moveFile(nachaFile.getFileName(), archiveDir);
                nachaFile.setStatus(NACHAFileStatus.Archived);
                Application.save(nachaFile);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void processACHTransaction(@WebParam (name="transactionId") String transactionId) {
        if(transactionId == null || transactionId.trim().length() == 0) {
            throw new RuntimeException("TransactionId must be set.");
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));

            if(financialTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' not found.");
            }

            MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            if(moneyMovementTransaction == null ||
                    (moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.None && moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.SentToAgency) ||
                    moneyMovementTransaction.getStatus() != PaymentStatus.Executed) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' cannot be processed.");
            }

            List<SpcfUniqueId> transactionsOnMmt = new ArrayList<SpcfUniqueId>();
            for (FinancialTransaction transaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                transactionsOnMmt.add(transaction.getId());
            }
            PayrollServices.rollbackUnitOfWork();

            //to avoid escalating visibility to public
            new ProcessACHTransactions() {
                public void processTransactions(List<SpcfUniqueId> transactions, SpcfUniqueId companyId) {
                    this.processOffloadedAchTransaction(transactions, companyId);
                }
            }.processTransactions(transactionsOnMmt, financialTransaction.getCompany().getId());

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void submitEFTPSPayment(@WebParam (name="transactionId") String transactionId) {
        if(transactionId == null || transactionId.trim().length() == 0) {
            throw new RuntimeException("TransactionId must be set.");
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));

            if(financialTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' not found.");
            }

            MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            if(moneyMovementTransaction == null ||
                    moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.ReadyToSend ||
                    moneyMovementTransaction.getStatus() != PaymentStatus.Created) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' cannot be submitted.");
            }

            SpcfCalendar initDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(initDate);
            moneyMovementTransaction.updateInitiationDate(initDate);

            SpcfCalendar settlementDate = initDate.copy();
            CalendarUtils.addBusinessDays(settlementDate, 1);
            for (FinancialTransaction transaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
                transaction.setSettlementDate(settlementDate);
            }
            Application.save(moneyMovementTransaction);
            PayrollServices.commitUnitOfWork();

            DataLoadServices.submitPayment(null);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void completeEFTPSPayment(@WebParam (name="transactionId") String transactionId) throws S3ConnectionException,S3UploadException {
        if(transactionId == null || transactionId.trim().length() == 0) {
            throw new RuntimeException("TransactionId must be set.");
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));

            if(financialTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' not found.");
            }

            MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            if(moneyMovementTransaction == null ||
                    moneyMovementTransaction.getTaxPaymentStatus() != TaxPaymentStatus.SentToAgency ||
                    moneyMovementTransaction.getStatus() != PaymentStatus.Executed) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' cannot be completed.");
            }

            //override status as completed (i.e by passing FTP). This step is not seen in production.
            DomainEntitySet<EftpsPaymentDetail> eftpsPaymentDetails = Application.find(EftpsPaymentDetail.class,
                                                                                       EftpsPaymentDetail.MoneyMovementTransaction().equalTo(moneyMovementTransaction));
            for (EftpsPaymentDetail eftpsPaymentDetail : eftpsPaymentDetails) {
                if(eftpsPaymentDetail.getParentFile() != null) {
                    EftpsFile eftFile = eftpsPaymentDetail.getParentFile();
                    eftFile.setStatusCd(EdiFileStatus.Completed);
                    Application.save(eftFile);
                }
            }
            PayrollServices.commitUnitOfWork();

            EftpsDataLoader.callSimulator();

            //Archive file. Only payment file(813) will be in completed status.
            EdiManager.archiveFiles();

            // process awaiting response file. i.e processing the files in TFA directory. this step will process responses/acknowledgements from TFA and archive
            // anything it completes
            // If the response file owner is AS400, it will log the info in PSP and send it to AS400. The file status will be INPROCESS state until it uploads to TFA.
            BatchJobManager.runJob(BatchJobType.EftpsResponse);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String getPaymentTemplateCd(@WebParam (name="FinancialTransactionId") String transactionId) {
        String paymentTemplateCd = null;
        if(transactionId == null || transactionId.trim().length() == 0) {
            throw new RuntimeException("Financial TransactionId must be set.");
        }

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(transactionId));

            if(financialTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' not found.");
            }

            MoneyMovementTransaction moneyMovementTransaction = financialTransaction.getMoneyMovementTransaction();
            if(moneyMovementTransaction == null) {
                throw new RuntimeException("Financial Transaction with id '" + transactionId + "' does not have MMT.");
            }
            paymentTemplateCd = moneyMovementTransaction.getPaymentTemplate().getPaymentTemplateCd();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return paymentTemplateCd;
    }

    @WebMethod
    public TransactionWSDTO addFLATransaction(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                              @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                              @WebParam(name = "debitLedgerAccountCode") String debitLedgerAccountCode,
                                              @WebParam(name = "creditLedgerAccountCode") String creditLedgerAccountCode,
                                              @WebParam(name = "transactionAmount") String transactionAmount,
                                              @WebParam(name = "payrollRunId") String payrollRunId,
                                              @WebParam(name = "lawId") String lawId,
                                              @WebParam(name = "noteText") String noteText) throws Exception {
        LedgerAccountCode dbtLedgerAccountCd;
        LedgerAccountCode cdtLedgerAccountCd;
        SpcfMoney amount;
        TransactionWSDTO txDTO = new TransactionWSDTO();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of SourceSystemCD or SourceCompanyID can not be null");
            }

            if (debitLedgerAccountCode == null) {
                throw new RuntimeException("Debit Ledger account Code can not be null");
            } else {
                dbtLedgerAccountCd = LedgerAccountCode.valueOf(debitLedgerAccountCode);
            }

            if (creditLedgerAccountCode == null) {
                throw new RuntimeException("Credit Ledger account Code can not be null");
            } else {
                cdtLedgerAccountCd = LedgerAccountCode.valueOf(creditLedgerAccountCode);
            }

            if (transactionAmount == null) {
                throw new RuntimeException("Transaction amount can not be null");
            } else {
                amount = new SpcfMoney(transactionAmount);
            }

            if (noteText == null) {
                throw new RuntimeException("Note Text can not be null");
            }

            ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.addFinancialLedgerAdjustmentTransaction(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID,
                    dbtLedgerAccountCd, cdtLedgerAccountCd, amount, payrollRunId, lawId, noteText);

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:" + result.getMessages());
            }

            FinancialTransaction finTx = result.getResult();
            txDTO.id = finTx.getId().toString();
            String sku = finTx.getSku();
            OfferingServiceChargeType offeringServiceCharge = null;
            if (null != sku) {
                offeringServiceCharge = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
            }
            txDTO.transactionType = DDCodeToPSP.getQBOETransactionTypeCode(finTx.getTransactionType().getTransactionTypeCd(), offeringServiceCharge);
            txDTO.currentState = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            txDTO.transactionAmount = SpcfUtils.convertToBigDecimal(finTx.getFinancialTransactionAmount());
            txDTO.settlementType = finTx.getSettlementTypeCd().toString();
            txDTO.settlementDate = new Date(finTx.getSettlementDate().toLocal().getTimeInMilliseconds());
            txDTO.offloadStatus = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            if (null != finTx.getCreditBankAccount() && null != finTx.getCreditBankAccountType()) {
                txDTO.creditBankAccount = getBankAccountDTO(finTx.getCreditBankAccount(), finTx.getCreditBankAccountType());
            }
            if (null != finTx.getDebitBankAccount() && null != finTx.getDebitBankAccountType()) {
                txDTO.debitBankAccount = getBankAccountDTO(finTx.getDebitBankAccount(), finTx.getDebitBankAccountType());
            }
            if (null != finTx.getMoneyMovementTransaction()) {
                txDTO.moneyMovementTransaction = getMoneyMovementTransactionDTO(finTx.getMoneyMovementTransaction());
            }
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return txDTO;
    }

    @WebMethod
    public void resetStaticDataForTempFLAs() throws Exception {
        try {
            //Delete posting rules assigned for Temp Transaction Types
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<PostingRule> postingRules = Application.findObjects(PostingRule.class);
            for (PostingRule postingRule : postingRules) {
                if (TransactionType.TEMP_FLA_TRANSACTION_TYPES.contains(postingRule.getTransactionType().getTransactionTypeCd())) {
                    Application.deleteObject(postingRule);
                }
            }

            //Update Temp Transaction types to reusable if used
            DomainEntitySet<TransactionType> transactionTypes = Application.findObjects(TransactionType.class);
            for (TransactionType transactionType : transactionTypes) {
                if (TransactionType.TEMP_FLA_TRANSACTION_TYPES.contains(transactionType.getTransactionTypeCd()) && !transactionType.getTransactionTypeCd().toString().equals(transactionType.getName())) {
                    transactionType.setName(transactionType.getTransactionTypeCd().toString());
                    Application.getHibernateSession().save(transactionType);
                }
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public TransactionWSDTO addBookTransferTransaction(@WebParam(name = "fromAccountCode") String fromAccountCode,
                                              @WebParam(name = "toAccountCode") String toAccountCode,
                                              @WebParam(name = "transactionAmount") String transactionAmount) throws Exception {
        SpcfMoney amount;
        TransactionWSDTO txDTO = new TransactionWSDTO();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            if (StringUtils.isEmpty(fromAccountCode)) {
                throw new RuntimeException("fromAccountCode can not be null");
            }

            if (StringUtils.isEmpty(toAccountCode)) {
                throw new RuntimeException("toAccountCode can not be null");
            }

            if (transactionAmount == null) {
                throw new RuntimeException("Transaction amount can not be null");
            } else {
                amount = new SpcfMoney(transactionAmount);
            }

            ProcessResult<FinancialTransaction> result = PayrollServices.financialTransactionManager.addBookTransferTransaction(fromAccountCode, toAccountCode, amount);

            if (!result.isSuccess()) {
                throw new RuntimeException("Error:" + result.getMessages());
            }

            FinancialTransaction finTx = result.getResult();
            txDTO.id = finTx.getId().toString();
            txDTO.currentState = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            txDTO.transactionAmount = SpcfUtils.convertToBigDecimal(finTx.getFinancialTransactionAmount());
            txDTO.settlementType = finTx.getSettlementTypeCd().toString();
            txDTO.settlementDate = new Date(finTx.getSettlementDate().toLocal().getTimeInMilliseconds());
            txDTO.offloadStatus = getQBOETransactionStateCode(finTx.getCurrentTransactionState().getTransactionStateCd());
            if (null != finTx.getCreditBankAccount() && null != finTx.getCreditBankAccountType()) {
                txDTO.creditBankAccount = getBankAccountDTO(finTx.getCreditBankAccount(), finTx.getCreditBankAccountType());
                txDTO.creditBankAccount.intuitBankAccountDesc = IntuitBankAccount.findIntuitBankAccount(finTx.getCreditBankAccount()).getDescription();
            }
            if (null != finTx.getDebitBankAccount() && null != finTx.getDebitBankAccountType()) {
                txDTO.debitBankAccount = getBankAccountDTO(finTx.getDebitBankAccount(), finTx.getDebitBankAccountType());
                txDTO.debitBankAccount.intuitBankAccountDesc = IntuitBankAccount.findIntuitBankAccount(finTx.getDebitBankAccount()).getDescription();
            }
            if (null != finTx.getMoneyMovementTransaction()) {
                txDTO.moneyMovementTransaction = getMoneyMovementTransactionDTO(finTx.getMoneyMovementTransaction());
            }
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return txDTO;
    }

    @WebMethod
    public Collection<TransactionWSDTO> createPenaltiesAndInterestRefunds(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                                          @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                                                          @WebParam(name = "settlementType") String settlementType,
                                                                          @WebParam(name = "penaltiesRefundAmount") String penaltiesRefundAmount,
                                                                          @WebParam(name = "interestRefundAmount") String interestRefundAmount,
                                                                          @WebParam(name = "notes") String notes) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        Collection<TransactionWSDTO> transactionWSDTOs = null;
        try {

            SpcfMoney penaltiesAmount = SpcfMoney.ZERO;
            SpcfMoney interestAmount = SpcfMoney.ZERO;
            PayrollServices.beginUnitOfWork();
            if (StringUtils.isEmpty(sourceSystemCD) || StringUtils.isEmpty(sourceCompanyID)
                    || StringUtils.isEmpty(settlementType) || StringUtils.isEmpty(notes)) {
                throw new RuntimeException("Any of Source System Code, Source Company ID, settlement type " +
                        "or notes can not be null");
            }

            if (StringUtils.isEmpty(penaltiesRefundAmount) && StringUtils.isEmpty(interestRefundAmount)) {
                throw new RuntimeException("Both Penalties and Interest Refund amounts can not be null, either one is required");
            }

            if (!StringUtils.isEmpty(penaltiesRefundAmount)) {
                penaltiesAmount = new SpcfMoney(penaltiesRefundAmount);
            }

            if (!StringUtils.isEmpty(interestRefundAmount)) {
                interestAmount = new SpcfMoney(interestRefundAmount);
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Company not found with sourceCompanyID:" + sourceCompanyID + " sourceSystemCd:" + sourceSystemCD);
            }

            SettlementTypeDTO settlementTypeDTO = SettlementTypeDTO.valueOf(settlementType);
            if(settlementTypeDTO != SettlementTypeDTO.CheckType && settlementTypeDTO != SettlementTypeDTO.ACH && settlementTypeDTO != SettlementTypeDTO.Wire ) {
                throw new RuntimeException("Settlement Type:"+ settlementType + " is invalid for Penalties and Interest Refunds. Valid settlement types are: ACH, Wire and CheckType");

            }

            ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.addPenaltiesAndInterestRefund(
                    company.getSourceSystemCd(), company.getSourceCompanyId(), penaltiesAmount, interestAmount, notes, settlementTypeDTO);
            if (result.isSuccess()) {
                if (result.getResult().size() > 0) {
                    transactionWSDTOs = buildTransactionResponse(result.getResult(), false);
                }
                PayrollServices.commitUnitOfWork();
            } else {
                throw new RuntimeException(result.getMessages().get(0).toString());
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return transactionWSDTOs;
    }

    public static PayrollRun findPayrollRunBySourceId(Company pCompany, String pSourcePayrollRunId) {
        if(pCompany.getSourceSystemCd() == SourceSystemCode.QBOE) {
            return PayrollRun.findPayrollRun(pCompany, pSourcePayrollRunId);
        }

        boolean isInteger;
        int sourceId = 0;
        try {
            sourceId = Integer.parseInt(pSourcePayrollRunId);
            isInteger = true;
        } catch (NumberFormatException e) {
            isInteger = false;
        }

        if(isInteger) {
            // subtract 2 for verification debits, and 1 for to make the count 0 based
            sourceId = sourceId - 3;
            sourceId = Math.max(0, sourceId);
            DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(pCompany).sort(PayrollRun.PayrollRunDate());
            if(payrollRuns.size() > sourceId) {
                return payrollRuns.get(sourceId);
            }
        } else {
            return PayrollRun.findPayrollRun(pCompany, pSourcePayrollRunId);
        }

        throw new RuntimeException("Could not find payroll run for source id " + pSourcePayrollRunId);
    }

    @WebMethod
    @WebResult(name = "TXPAndRecordData")
    public ArrayList<ACHDetailRecordWSDTO> getTXPAndRecordData(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                                                        @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                                                        @WebParam(name = "paymentTemplate") String pPaymentTemplate,
                                                                        @WebParam(name = "paycheckDate") String pPaycheckDate) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();

            if (sourceSystemCD == null || sourceCompanyID == null) {
                throw new RuntimeException("Any of Source System Code or Source Company ID can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID");
            }

            if (pPaymentTemplate == null) {
                throw new RuntimeException("PaymentTemplate cannot be null");
            }

            PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(pPaymentTemplate);

            if (paymentTemplate == null) {
                throw new RuntimeException("No PaymentTemplate found for code: " + pPaymentTemplate);
            }

            //YYYYMMDD
            SpcfCalendar paycheckDate = null;
            if (pPaycheckDate != null) {
                paycheckDate = CalendarUtils.createInstanceFromDate(pPaycheckDate);
            }

            DomainEntitySet<EntryDetailRecord> entryDetailRecords = new DomainEntitySet<EntryDetailRecord>();
            DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = new DomainEntitySet<MoneyMovementTransaction>();
            if(paycheckDate != null) {
                DomainEntitySet<PayrollRun> payrollRuns = Application.find(PayrollRun.class, PayrollRun.Company().equalTo(company).And(PayrollRun.PaycheckDate().equalTo(paycheckDate)));
                for (PayrollRun payrollRun : payrollRuns) {
                    for (FinancialTransaction financialTransaction : payrollRun.getTaxPaymentTransactions()) {
                        moneyMovementTransactions.add(financialTransaction.getMoneyMovementTransaction());                       
                    }
                }
                
                moneyMovementTransactions = moneyMovementTransactions.find(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate));

                for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
                    entryDetailRecords.addAll(moneyMovementTransaction.getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));
                }                
            } else {
                entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.Company().equalTo(company)
                                                                                                .And(EntryDetailRecord.MoneyMovementTransaction().PaymentTemplate().equalTo(paymentTemplate))
                                                                                                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));
            }

            ArrayList<ACHDetailRecordWSDTO> achDetailRecordWSDTOs = new ArrayList<ACHDetailRecordWSDTO>();
            for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
                ACHDetailRecordWSDTO achDetailRecordWSDTO = new ACHDetailRecordWSDTO();
                achDetailRecordWSDTO.recordData = entryDetailRecord.getRecordData();
                achDetailRecordWSDTO.txpRecordData = entryDetailRecord.getTxpRecordData();
                achDetailRecordWSDTOs.add(achDetailRecordWSDTO);
            }
            return achDetailRecordWSDTOs;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}

package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.AgencyIdDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Helper Methods for State TXP Tests
 */
public class StateTxpTestsHelper {


    public static String formatAmount(SpcfMoney amount, int dollarsDigits) {
        return formatAmount(amount, dollarsDigits, true);
    }

    public static String formatAmount(SpcfMoney amount, int dollarsDigits, boolean leaveLeadingZeroes) {
        StringBuffer dollarPattern = new StringBuffer(dollarsDigits);

        if (leaveLeadingZeroes) {
            for (int i = 0; i < dollarsDigits; i++) {
                dollarPattern.append("0");
            }
        }

        DecimalFormat dollarsFormat = new DecimalFormat(dollarPattern.toString());
        String dollarsOutput = dollarsFormat.format(amount.getIntegerPart());
        DecimalFormat centsFormat = new DecimalFormat("00");
        String centsOutput = centsFormat.format(amount.getFractionalPart());

        return dollarsOutput + centsOutput;
    }

    public static String getWithoutHyphens(String pString) {
        if (pString == null) {
            return null;
        }

        return pString.replace("-","");
    }

    public static String getEnd(EntryDetailRecord entryDetailRecord, String datePattern) {
        return entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodEnd().format(datePattern);
    }
    public static String getMonthEndDate(EntryDetailRecord pEdr,String datePattern) {
        return CalendarUtils.getLastDayOfMonth(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()).format(datePattern);
    }
    public static String getQuarterEndDate(EntryDetailRecord pEdr,String datePattern){
        return CalendarUtils.getLastDayOfQuarter(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd()).format(datePattern);
    }

    public static String getBegin(EntryDetailRecord entryDetailRecord, String datePattern) {
        return entryDetailRecord.getMoneyMovementTransaction().getPaymentPeriodBegin().format(datePattern);
    }

    public static String getEndDateYear(EntryDetailRecord pEdr) {
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yyyy");
    }

    public static String getEndYearAndQuarter(EntryDetailRecord pEdr) {
        //Return YYQ
        int quarter = CalendarUtils.getQuarterAsInt(pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd());
        return pEdr.getMoneyMovementTransaction().getPaymentPeriodEnd().format("yy")+String.valueOf(quarter);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pEin, String pStateEin, String pState, PaymentTemplateCategory pCategory) {
        return createEntryDetailRecord(pEin, pStateEin, pState, null, pCategory);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pEin, String pStateEin, String pState, AgencyIdDTO pAgencyIdDTO, PaymentTemplateCategory pCategory) {
        String psid = "123272727";
        List<Employee> employees = DataLoadServices.setupCompany(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        return createEntryDetailRecord(psid, new DateDTO("2011-01-07"), pEin, pStateEin, pState, 1, pAgencyIdDTO, pCategory);
    }

    public static DomainEntitySet<EntryDetailRecord> createEntryDetailRecords(String pEin, String pStateEin, String pState, PaymentTemplateCategory pCategory) {
        String psid = "123272727";
        List<Employee> employees = DataLoadServices.setupCompany(psid);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        return createEntryDetailRecords(psid, new DateDTO("2011-01-07"), pEin, pStateEin, pState, null, null, 1, null, pCategory);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pEin, String pStateEin, String pState, int pCount, AgencyIdDTO pAgencyIdDTO, PaymentTemplateCategory pCategory) {
        String psid = "123272727";
        List<Employee> employees = DataLoadServices.setupCompany(psid);
        DataLoadServices.enrollEFTPS(Company.findCompany(psid, SourceSystemCode.QBDT));
        return createEntryDetailRecord(psid, new DateDTO("2011-01-07"), pEin, pStateEin, pState, pCount, pAgencyIdDTO, pCategory);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, int pCount, AgencyIdDTO pAgencyIdDTO,PaymentTemplateCategory pCategory ) {
        return createEntryDetailRecord(pPsid, pPayrollRunDate, pEin, pStateEin, pState, null, null, pCount, pAgencyIdDTO, pCategory);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate, int pCount, PaymentTemplateCategory pCategory) {
        return createEntryDetailRecord(pPsid, pPayrollRunDate, pEin, pStateEin, pState, pDepositFrequencyCode, pEffectiveDate, pCount, null, pCategory);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate, int pCount, AgencyIdDTO pAgencyIdDTO, PaymentTemplateCategory pCategory) {
        return createEntryDetailRecord(pPsid, pPayrollRunDate, pEin, pStateEin, pState, pDepositFrequencyCode, pEffectiveDate, pCount, pAgencyIdDTO, pCategory, null);
    }

    public static EntryDetailRecord createEntryDetailRecord(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate, int pCount, AgencyIdDTO pAgencyIdDTO, PaymentTemplateCategory pCategory, SpcfMoney defaultLawAmount) {
        if (pState.equals("NY")) {
           return createEntryDetailRecordNY_Metro(pPsid, pPayrollRunDate, pEin, pStateEin, pState, null, null, pCount, pAgencyIdDTO);
        }

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = createEntryDetailRecords(pPsid, pPayrollRunDate, pEin, pStateEin, pState, pDepositFrequencyCode, pEffectiveDate, pCount, pAgencyIdDTO, pCategory, defaultLawAmount);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.ACHCredit);

        EntryDetailRecord entryDetailRecord = null;
        if (statePayments.size() > 0) {
            assertEquals("State ACH credit payment entry detail", 1, entryDetailRecords.size());
            entryDetailRecord = entryDetailRecords.get(0);
        }
        PayrollServices.rollbackUnitOfWork();

        return entryDetailRecord;
    }

    public static DomainEntitySet<EntryDetailRecord> createEntryDetailRecords(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate, int pCount, AgencyIdDTO pAgencyIdDTO, PaymentTemplateCategory pCategory) {
        return createEntryDetailRecords(pPsid, pPayrollRunDate, pEin, pStateEin, pState, pDepositFrequencyCode, pEffectiveDate, pCount, pAgencyIdDTO, pCategory, null);
    }

    public static DomainEntitySet<EntryDetailRecord> createEntryDetailRecords(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate, int pCount, AgencyIdDTO pAgencyIdDTO, PaymentTemplateCategory pCategory, SpcfMoney defaultTaxAmount) {
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        //Update Fed tax Id
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
        company.setFedTaxId(pEin);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(pState, pCategory);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);


        if (pDepositFrequencyCode != null && pEffectiveDate != null) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(pPsid, paymentTemplate.getPaymentTemplateCd(), pDepositFrequencyCode, pEffectiveDate);
        }

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");

        SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;

        List<CompanyLaw> companyLaws = DataLoadServices.addCompanyLawsWithAgencyId(pStateEin, company, pState);
        for (CompanyLaw companyLaw : companyLaws) {
            lawAmounts.put(companyLaw.getLaw().getLawId(), defaultTaxAmount != null ? defaultTaxAmount.toString() : companyLaw.getLaw().getLawId());
            withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
        }
        // Multiply by number of employees
        withHoldingsAmount = withHoldingsAmount.multiply(SpcfDecimal.createInstance("2"));

        if (pAgencyIdDTO != null) {
            PayrollServices.beginUnitOfWork();
            assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), pAgencyIdDTO));
            PayrollServices.commitUnitOfWork();
        }

        //Enable ACH Credit payment
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true));
        PayrollServices.commitUnitOfWork();

        //Update state Tax Id
        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);
        capt.setAgencyTaxpayerId(pStateEin);
        Application.save(capt);
        PayrollServices.commitUnitOfWork();

        //Update enrollment if applicable
        DataLoadServices.enrollACH(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, pPayrollRunDate, employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pPsid, payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.ACHCredit);
        boolean isCheckPayment = false;
        if (statePayments.size() == 0) {
            statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.CheckPayment);
            isCheckPayment = true;
        }
        assertEquals("Number of State payments", pCount, statePayments.size());

        DomainEntitySet<MoneyMovementTransaction> irs941Payments = DataLoadServices.getReadyToSendTaxPayments(company, "IRS-941-PAYMENT");
        assertEquals("IRS 941 payments", pCount, irs941Payments.size());
        for (MoneyMovementTransaction irs941Payment : irs941Payments) {
            assertEquals("IRS 941 payment Amount", new SpcfMoney("100"), irs941Payment.getMoneyMovementTransactionAmount());
        }
        DomainEntitySet<MoneyMovementTransaction> irs940Payments = DataLoadServices.getReadyToSendTaxPayments(company, "IRS-940-PAYMENT");
        assertEquals("IRS 940 payments", pCount, irs940Payments.size());
        for (MoneyMovementTransaction irs940Payment : irs940Payments) {
            assertEquals("IRS 940 payment Amount", new SpcfMoney("13"), irs940Payment.getMoneyMovementTransactionAmount());
        }

        SettlementType settlementType = SettlementType.ACH;
        if (isCheckPayment) {
            settlementType = SettlementType.CheckType;
        }

        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.SettlementTypeCd().equalTo(settlementType));

        assertTrue("State ACH agency tax credit FTs", financialTransactions.size() > 0);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)
                                                                                                                           .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(financialTransactions.get(0).getMoneyMovementTransaction())));
        PayrollServices.rollbackUnitOfWork();

        return entryDetailRecords;
    }

    public static EntryDetailRecord createEntryDetailRecordNY_Metro(String pPsid, DateDTO pPayrollRunDate, String pEin, String pStateEin, String pState, DepositFrequencyCode pDepositFrequencyCode, SpcfCalendar pEffectiveDate, int pCount, AgencyIdDTO pAgencyIdDTO) {

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        //Update Fed tax Id
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
        company.setFedTaxId(pEin);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        List<Employee> employees = Arrays.asList(Employee.findEmployees(company).toArray(new Employee[]{}));

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(pState, PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        PaymentTemplate paymentTemplateNYMetro = null;
        if (pState.equals("NY")) {
            DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", supportedDate);
            paymentTemplateNYMetro = PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT");
        }

        if (pDepositFrequencyCode != null && pEffectiveDate != null) {
            DataLoadServices.updateEffectiveDepositFreqEffDate(pPsid, paymentTemplate.getPaymentTemplateCd(), pDepositFrequencyCode, pEffectiveDate);
        }

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1", "25");
        lawAmounts.put("65", "6.5");

        SpcfDecimal withHoldingsAmount = SpcfMoney.ZERO;

        List<CompanyLaw> companyLaws = DataLoadServices.addCompanyLawsWithAgencyId(pStateEin, company, pState);
        for (CompanyLaw companyLaw : companyLaws) {
            lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
            withHoldingsAmount = withHoldingsAmount.add(SpcfDecimal.createInstance(companyLaw.getLaw().getLawId()));
        }
        // Multiply by number of employees
        withHoldingsAmount = withHoldingsAmount.multiply(SpcfDecimal.createInstance("2"));

        if (pAgencyIdDTO != null) {
            PayrollServices.beginUnitOfWork();
            assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), pAgencyIdDTO));
            PayrollServices.commitUnitOfWork();
        }

        //Enable ACH Credit payment
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true));
        if (paymentTemplateNYMetro != null) {
            assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplateNYMetro.getPaymentTemplateCd(), PaymentMethod.ACHCredit, true));
        }
        PayrollServices.commitUnitOfWork();

        //Update state Tax Id
        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplate);
        capt.setAgencyTaxpayerId(pStateEin);
        Application.save(capt);
        if (paymentTemplateNYMetro != null) {
            capt = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, paymentTemplateNYMetro);
            capt.setAgencyTaxpayerId(pStateEin);
            Application.save(capt);
        }


        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, pPayrollRunDate, employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pPsid, payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.ACHCredit);


        DomainEntitySet<MoneyMovementTransaction> irs941Payments = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        //assertEquals("IRS 941 payments", pCount, irs941Payments.size());
        for (MoneyMovementTransaction irs941Payment : irs941Payments) {
            assertEquals("IRS 941 payment Amount", new SpcfMoney("100"), irs941Payment.getMoneyMovementTransactionAmount());
        }
        DomainEntitySet<MoneyMovementTransaction> irs940Payments = DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT");
        // assertEquals("IRS 941 payments", pCount, irs940Payments.size());
        for (MoneyMovementTransaction irs940Payment : irs940Payments) {
            assertEquals("IRS 941 payment Amount", new SpcfMoney("13"), irs940Payment.getMoneyMovementTransactionAmount());
        }

        SettlementType settlementType = SettlementType.ACH;

        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult().getFinancialTransactions
                (TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.SettlementTypeCd().equalTo(settlementType))
                .find(FinancialTransaction.Law().PaymentTemplate().PaymentTemplateCd().equalTo(pAgencyIdDTO.getPaymentTemplateCd()));

        assertTrue("State ACH agency tax credit FTs", financialTransactions.size() > 0);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, EntryDetailRecord.NACHAFileType().equalTo(NACHAFileType.CCDPlus)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)).And(EntryDetailRecord.MoneyMovementTransaction().equalTo(financialTransactions.get(0).getMoneyMovementTransaction())));
        EntryDetailRecord entryDetailRecord = null;
        if (entryDetailRecords.size() > 0) {
            assertEquals("State ACH credit payment entry detail", 1, entryDetailRecords.size());
            entryDetailRecord = entryDetailRecords.get(0);
        }
        PayrollServices.rollbackUnitOfWork();

        return entryDetailRecord;
    }

   
}

package com.intuit.sbd.payroll.psp.processes;
//import com.intuit.money.sales.sale.trait.payment.model.PaymentMethod;
import ch.qos.logback.core.net.SyslogOutputStream;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.ach.txp.TxpRecordManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.PrintedChecksSelector;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.EftpsEnrollmentStatus;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.PaymentOnHoldReason;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.PaymentMethod;
import com.intuit.sbd.payroll.psp.domain.PaymentStatus;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.util.StringUtil;
import junit.framework.Assert;
import org.junit.*;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * User: dweinberg
 * Date: 7/7/11
 * Time: 2:39 PM
 * Tests payment method and enrollment hold calculations
 */
public class PaymentMethodTests {
    private int achTaxOffloadOffset;

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.updateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        //Actual requirement is that the EFT number is specified.  Will instead test a particular format
        PayrollServices.beginUnitOfWork();
        AgencyIdRequirement agencyIdRequirement = assertOne(Application.find(AgencyIdRequirement.class, AgencyIdRequirement.PaymentTemplateAgencyId().PaymentTemplate().PaymentTemplateCd().equalTo("CO-DR1094-PAYMENT")));
        agencyIdRequirement.setPattern("\\d{2}-\\d{3}");
        Application.save(agencyIdRequirement);

        //also add a regular AID requirement
        AgencyIdRequirement aidRequirement = new AgencyIdRequirement();
        aidRequirement.setPattern("\\d{4}-\\d{2}");
        aidRequirement.setExample("1224-56");
        aidRequirement.setRequired(true);
        aidRequirement.setProhibitDefaultIds(false);
        PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = PaymentTemplate.findPaymentTemplate("CO-DR1094-PAYMENT").getPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit);

        Application.executeSqlCommand("INSERT INTO PSP_PAYMENT_METHOD_REQUIREMENT\n" +
                "   (PAYMENT_METHOD_REQUIREMENT_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, PMT_TEMPLATE_PMT_METHOD_FK)\nvalues\n   ('d79226a2-0000-0000-abcd-000000000020', 0, 'UnitTest', current_timestamp, 'UnitTest', current_timestamp, -1, ?)", true, paymentTemplatePaymentMethod.getId().toString());
        Application.executeSqlCommand("INSERT INTO PSP_AGENCY_ID_REQUIREMENT\n   (AGENCY_ID_REQUIREMENT_SEQ, REALM_ID, PATTERN, EXAMPLE, PAYMENT_TEMPLATE_AGENCY_ID_FK, REQUIRED, CUSTOM_REQUIREMENT, PROHIBIT_DEFAULT_IDS" +
                ")\nvalues ('d79226a2-0000-0000-abcd-000000000020', -1, '\\d{4}-\\d{2}', '1224-56', null, 1, 'None', 0) ", true);


        PayrollServices.commitUnitOfWork();



    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

        //remove the test ID requirement
        PayrollServices.beginUnitOfWork();
        for (PaymentMethodRequirement paymentMethodRequirement : PaymentTemplate.findPaymentTemplate("CO-DR1094-PAYMENT").getPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit).getPaymentMethodRequirementCollection()) {
            if (paymentMethodRequirement instanceof AgencyIdRequirement && ((AgencyIdRequirement) paymentMethodRequirement).getPaymentTemplateAgencyId() == null) {
                Application.delete(paymentMethodRequirement);
            }
        }

        DomainEntitySet<AgencyIdRequirement> agencyIdRequirements = Application.find(AgencyIdRequirement.class, AgencyIdRequirement.PaymentTemplateAgencyId().PaymentTemplate().PaymentTemplateCd().equalTo("CO-DR1094-PAYMENT"));
        if (agencyIdRequirements.size() == 1) {
            agencyIdRequirements.getFirst().setPattern(null);
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRequirements() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CO-DR1094-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLaws(company, "7");
        DataLoadServices.addCompanyLaws(company, "25", "12");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "LA");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2010-09-20"), "1");

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-02-20"), "3");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-03-20"), "7");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-04-20"), "13");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-05-20"), "17");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-07-20"), "23");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 19, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-09-20"), "29");
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2012-01-20"), "31");

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.CheckType);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "CO-DR1094-PAYMENT", PaymentMethod.ACHCredit, true));
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), new AgencyIdDTO("CO-DR1094-PAYMENT", "State EFT Number", "44-678")));
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(CompanyAgency.findCompanyAgency(company, "CODOR"));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("CO-DR1094-PAYMENT").setAgencyTaxpayerId("9876-54");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "CODOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.ACHCredit);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.ACH);

        PayrollServices.beginUnitOfWork();
        companyAgencyDTO = PayrollServices.dtoFactory.create(CompanyAgency.findCompanyAgency(company, "CODOR"));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("CO-DR1094-PAYMENT").setAgencyTaxpayerId("9876-54x");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "CODOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.CheckType);

        PayrollServices.beginUnitOfWork();
        companyAgencyDTO = PayrollServices.dtoFactory.create(CompanyAgency.findCompanyAgency(company, "CODOR"));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("CO-DR1094-PAYMENT").setAgencyTaxpayerId("9876-54");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "CODOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.ACHCredit);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.ACH);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), new AgencyIdDTO("CO-DR1094-PAYMENT", "State EFT Number", "x44-678")));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.CheckType);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), new AgencyIdDTO("CO-DR1094-PAYMENT", "State EFT Number", "44-678")));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.ACHCredit);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.ACH);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "CO-DR1094-PAYMENT", PaymentMethod.ACHCredit, false));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "CO-DR1094-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "CO-DR1094-PAYMENT", SettlementType.CheckType);

        assertPaymentMethod(company, "IRS-941-PAYMENT", PaymentMethod.EFTPS);
        assertSettlementType(company, "IRS-941-PAYMENT", SettlementType.EFTPS);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingEnrollment));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "IRS-941-PAYMENT", PaymentMethod.EFTPS, true);
        assertSettlementType(company, "IRS-941-PAYMENT", SettlementType.EFTPS);
    }

    @Test
    public void testNoValidCreatesEnrollmentHold() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MN-MW1-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLaws(company, "7");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "MN");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "GA");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "LA");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency =   CompanyAgency.findCompanyAgency(company, "MNDOR");
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("MN-MW1-PAYMENT").setAgencyTaxpayerId(null);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "MNDOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        runPayroll(company, new DateDTO("2011-09-20"), "500");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mwPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MN-MW1-PAYMENT").find());
        assertNull(mwPayment.getMoneyMovementPaymentMethod());
        assertEquals(PaymentOnHoldReason.Enrollment, assertOne(mwPayment.getActiveOnHoldReasons()).getOnHoldReasonCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDepositFrequencyRequirements() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-GAV-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "GA-GAV-PAYMENT", false);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2011-09-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertNull(gaPayment.getMoneyMovementPaymentMethod());
        assertEquals(PaymentOnHoldReason.Enrollment, assertOne(gaPayment.getActiveOnHoldReasons()).getOnHoldReasonCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 21, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2011, 9, 1));

        PayrollServices.beginUnitOfWork();
        gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertNull(gaPayment.getMoneyMovementPaymentMethod());
        assertEquals(1, gaPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify ACH Credit is getting populated if the deposit frequency is SEMIWEEKLY
         */
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, "GA-GAV-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company,"GA-GAV-PAYMENT","2234560-ab");
        PayrollServices.beginUnitOfWork();
        gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, gaPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testDueDateMonthlyBoundary() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2012, 9, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-GAV-PAYMENT", SpcfCalendar.createInstance(2012, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 9, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 9, 1));

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency =   CompanyAgency.findCompanyAgency(company, "GADOR");
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("GA-GAV-PAYMENT").setAgencyTaxpayerId(null);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "GADOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 9, 29, SpcfTimeZone.getLocalTimeZone()));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-09-29"));

        // Verify MMT pay period is correct.
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class,
                MoneyMovementTransaction.Company().equalTo(company)
                        .And(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("GA-GAV-PAYMENT"))));

        assertEquals("MMT Count", 1, mmts.size());
        assertEquals("Period Begin", SpcfCalendar.createInstance(2012, 9, 29, SpcfTimeZone.getLocalTimeZone()), mmts.getFirst().getPaymentPeriodBegin().toLocal());
        assertEquals("Period End", SpcfCalendar.createInstance(2012, 9, 30, SpcfTimeZone.getLocalTimeZone()), mmts.getFirst().getPaymentPeriodEnd().toLocal());
    }

    @Test
    public void testDueDateYearlyBoundary() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2012, 12, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CO-DR1094-PAYMENT", SpcfCalendar.createInstance(2012, 12, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 9, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CO-DR1094-PAYMENT", DepositFrequencyCode.WEEKLY, SpcfCalendar.createInstance(2012, 12, 1));

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency =   CompanyAgency.findCompanyAgency(company, "CODOR");
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("CO-DR1094-PAYMENT").setAgencyTaxpayerId(null);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "CODOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 12, 31, SpcfTimeZone.getLocalTimeZone()));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-12-31"));

        // Verify MMT pay period is correct.
        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class,
                MoneyMovementTransaction.Company().equalTo(company)
                        .And(MoneyMovementTransaction.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("CO-DR1094-PAYMENT"))));

        assertEquals("MMT Count", 1, mmts.size());
        assertEquals("MMT due date", SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone()), mmts.getFirst().getDueDate().toLocal());
        assertEquals("MMT initiation date", SpcfCalendar.createInstance(2013, 1, 2, SpcfTimeZone.getLocalTimeZone()), mmts.getFirst().getInitiationDate().toLocal());
        assertEquals("FT settlement date", SpcfCalendar.createInstance(2013, 1, 4, SpcfTimeZone.getLocalTimeZone()), mmts.getFirst().getFirstFinancialTransaction().getSettlementDate().toLocal());
    }
    // Removing check payment method for this payment template AL-CR4WH-PAYMEN, so ignoring this method.
    @Ignore
    @Test
    public void testThresholdRequirements() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("AL-CR4WH-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateACHAgentEnabledFlags(company, "AL-CR4WH-PAYMENT", false);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 18, SpcfTimeZone.getLocalTimeZone()));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-09-21"), new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"3"}, new String[]{"500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AL-CR4WH-PAYMENT").find());
        assertNull(gaPayment.getMoneyMovementPaymentMethod());
        assertEquals(PaymentOnHoldReason.Enrollment, assertOne(gaPayment.getActiveOnHoldReasons()).getOnHoldReasonCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 21, SpcfTimeZone.getLocalTimeZone()));

        voidAPaycheck(processResult.getResult());

        PayrollServices.beginUnitOfWork();
        gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AL-CR4WH-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, gaPayment.getMoneyMovementPaymentMethod());
        assertEquals(0, gaPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoidAndChangeDepositFrequency() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-GAV-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLaws(company, "86", "179", "20", "7");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "UT");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "MN");
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "GA");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(CompanyAgency.findCompanyAgency(company, "GADOR"));
        /**
         * Verify ACH Credit is getting populated if Agency id Valid
         */
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("GA-GAV-PAYMENT").setAgencyTaxpayerId("1234567-aB");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "GADOR", companyAgencyDTO));
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(),"GA-GAV-PAYMENT", PaymentMethod.ACHCredit, true));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = runPayroll(company, new DateDTO("2011-09-20"), "400");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mwPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, mwPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 19, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 21, SpcfTimeZone.getLocalTimeZone()));
        voidAPaycheck(payrollRun);

        PayrollServices.beginUnitOfWork();
        mwPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, mwPayment.getMoneyMovementPaymentMethod());
        assertEquals("400.00", mwPayment.getMoneyMovementTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollServices.beginUnitOfWork();
        mwPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, mwPayment.getMoneyMovementPaymentMethod());
        assertEquals("400.00", mwPayment.getMoneyMovementTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"GA-GAV-PAYMENT","1234aB");

        PayrollServices.beginUnitOfWork();
        mwPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertNull(mwPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPaymentMethodRestrictDF() {
        String psid = "123272727";
        String ein = "22-222222312";

        List<Employee> employees = DataLoadServices.setupCompany(psid);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        //Update Fed tax Id
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        company.setFedTaxId(ein);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate("IL", PaymentTemplateCategory.Withholding);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(supportedDate);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62","6.2");
        lawAmounts.put("63","6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("1","25");
        lawAmounts.put("65","6.5");


        List<CompanyLaw> companyLaws = DataLoadServices.addCompanyLawsWithAgencyId(ein, company, "IL");
        for (CompanyLaw companyLaw : companyLaws) {
            lawAmounts.put(companyLaw.getLaw().getLawId(), companyLaw.getLaw().getLawId());
        }

        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate.getPaymentTemplateCd(), ein);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-01-05"), employees, lawAmounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.CheckPayment);
        Assert.assertEquals("Number of State payments", 1, statePayments.size());

        for (MoneyMovementTransaction statePayment : statePayments) {
            Assert.assertEquals("State payment Amount", new SpcfMoney("32"), statePayment.getMoneyMovementTransactionAmount());
        }
        DomainEntitySet<MoneyMovementTransaction> irs941Payments = DataLoadServices.getOnHoldTaxPayments(company, "IRS-941-PAYMENT");
        Assert.assertEquals("IRS 941 payments", 1, irs941Payments.size());
        for (MoneyMovementTransaction irs941Payment : irs941Payments) {
            Assert.assertEquals("IRS 941 payment Amount", new SpcfMoney("100"), irs941Payment.getMoneyMovementTransactionAmount());
        }
        DomainEntitySet<MoneyMovementTransaction> irs940Payments = DataLoadServices.getOnHoldTaxPayments(company, "IRS-940-PAYMENT");
        Assert.assertEquals("IRS 941 payments", 1, irs940Payments.size());
        for (MoneyMovementTransaction irs940Payment : irs940Payments) {
            Assert.assertEquals("IRS 941 payment Amount", new SpcfMoney("13"), irs940Payment.getMoneyMovementTransactionAmount());
        }

        SettlementType settlementType = SettlementType.CheckType;
        DomainEntitySet<FinancialTransaction> financialTransactions = processResult.getResult().getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit).find(FinancialTransaction.SettlementTypeCd().equalTo(settlementType));
        assertTrue("State ACH agency tax credit FTs", financialTransactions.size() > 0);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.SEMIWEEKLY, PSPDate.getPSPTime());
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-04-05"), employees, lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.CheckPayment);
        Assert.assertEquals("Number of State payments", 1, statePayments.size());
        statePayments = DataLoadServices.getOnHoldTaxPayments(company, paymentTemplate.getPaymentTemplateCd());
        Assert.assertEquals("Number of State payments on Hold", 1, statePayments.size());
        Assert.assertEquals("Number of On Hold State payment Payment method", "None", statePayments.get(0).getMoneyMovementPaymentMethodString());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 7, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), DepositFrequencyCode.QUARTERLY, PSPDate.getPSPTime());
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-07-05"), employees, lawAmounts);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        statePayments = DataLoadServices.getReadyToSendTaxPayments(company, PaymentMethod.CheckPayment);
        Assert.assertEquals("Number of State payments", 2, statePayments.size());
        statePayments = DataLoadServices.getOnHoldTaxPayments(company, paymentTemplate.getPaymentTemplateCd());
        Assert.assertEquals("Number of State payments on Hold", 1, statePayments.size());
        Assert.assertEquals("Number of On Hold State payment Payment method", "None", statePayments.get(0).getMoneyMovementPaymentMethodString());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMORequirements() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MO-941-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateACHAgentEnabledFlags(company, "MO-941-PAYMENT", false);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MO-941-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.setPSPDate(2012,1,1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moMMT = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MO-941-PAYMENT").find());
        assertNull(moMMT.getMoneyMovementPaymentMethod());
        assertEquals(PaymentOnHoldReason.Enrollment, assertOne(moMMT.getActiveOnHoldReasons()).getOnHoldReasonCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012,1,2);
        DataLoadServices.updateACHAgentEnabledFlags(company, "MO-941-PAYMENT", true);

        PayrollServices.beginUnitOfWork();
        moMMT = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MO-941-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, moMMT.getMoneyMovementPaymentMethod());
        assertEquals(0, moMMT.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012,1,3);
        DataLoadServices.updateACHAgentEnabledFlags(company, "MO-941-PAYMENT", false);

        PayrollServices.beginUnitOfWork();
        moMMT = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MO-941-PAYMENT").find());
        assertNull(moMMT.getMoneyMovementPaymentMethod());
        assertEquals(PaymentOnHoldReason.Enrollment, assertOne(moMMT.getActiveOnHoldReasons()).getOnHoldReasonCd());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testUpdatePaymentMethodDoesNotChangeSettlementDate() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WV-IT101-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1)); //ach + check
        DataLoadServices.updatePaymentTemplateSupportedDate("WV-IT101-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1)); //ach + check
        DataLoadServices.updatePaymentTemplateSupportedDate("MN-MW1-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1)); //ach only
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1)); //ach only
        DataLoadServices.updatePaymentTemplateSupportedDate("OR-OTCWH-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1)); //ach + check

        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012,1,1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        DataLoadServices.updateAgencyTaxpayerId(company,"OR-OTCWH-PAYMENT","63259094-1");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction orPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("OR-OTCWH-PAYMENT").find());

        //Initiation date is same. Settlement date is adjusted
        assertSuccess(PayrollServices.paymentManager.updateInitiationDate(orPayment.getId().toString(), SpcfCalendar.createInstance(2012, 3, 5, 8, 0, 0, 0, SpcfTimeZone.getLocalTimeZone())));
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();

        MoneyMovementTransaction wvPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("WV-IT101-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, wvPayment.getMoneyMovementPaymentMethod());
        assertDates(wvPayment, 2012, 2, 15 - achTaxOffloadOffset);

        MoneyMovementTransaction mnPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MN-MW1-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, mnPayment.getMoneyMovementPaymentMethod());
        assertDates(mnPayment, 2012, 1, 13 - achTaxOffloadOffset);

        MoneyMovementTransaction msPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MS-M89-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, msPayment.getMoneyMovementPaymentMethod());
        assertDates(msPayment, 2012, 2, 15 - achTaxOffloadOffset);

        Application.refresh(orPayment);
        assertEquals(PaymentMethod.ACHCredit, orPayment.getMoneyMovementPaymentMethod());
        assertDates(orPayment, 2012, 3, 5);//Initiation date is same. Settlement date is adjusted

        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 1, 2);

        DataLoadServices.updateACHAgentEnabledFlags(company, "WV-IT101-PAYMENT", false);
        DataLoadServices.updateACHAgentEnabledFlags(company, "MN-MW1-PAYMENT", false);
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", false);
        DataLoadServices.updateACHAgentEnabledFlags(company, "OR-OTCWH-PAYMENT", false);


        PayrollServices.beginUnitOfWork();
        //Checkpayment tax offset is 2.
        Application.refresh(wvPayment);
        assertEquals(PaymentMethod.CheckPayment, wvPayment.getMoneyMovementPaymentMethod());
        assertDates(wvPayment, 2012, 2, 13);

        Application.refresh(mnPayment);
        assertNull(mnPayment.getMoneyMovementPaymentMethod());
        assertDates(mnPayment, 2012, 1, 13);

        Application.refresh(msPayment);
        assertEquals(PaymentMethod.CheckPayment, msPayment.getMoneyMovementPaymentMethod());
        assertDates(msPayment, 2012, 2, 13);

        Application.refresh(orPayment);
        assertEquals(PaymentMethod.CheckPayment, orPayment.getMoneyMovementPaymentMethod());
        
        SpcfCalendar initDate = SpcfCalendar.createInstance(2012, 3, 5);
        if(achTaxOffloadOffset == 1) {
        	CalendarUtils.addBusinessDays(initDate, -1);//CheckPayment offset is 2. 
        }
        assertDates(orPayment, initDate.getYear(), initDate.getMonth(), initDate.getDay());

        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateACHAgentEnabledFlags(company, "WV-IT101-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "MN-MW1-PAYMENT", true);
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", true);
        DataLoadServices.updateACHAgentEnabledFlags(company, "OR-OTCWH-PAYMENT", true);

        PayrollServices.beginUnitOfWork();

        Application.refresh(wvPayment);
        assertEquals(PaymentMethod.ACHCredit, wvPayment.getMoneyMovementPaymentMethod());
        assertDates(wvPayment, 2012, 2, 15 - achTaxOffloadOffset);

        Application.refresh(mnPayment);
        assertEquals(PaymentMethod.ACHCredit, mnPayment.getMoneyMovementPaymentMethod());
        assertDates(mnPayment, 2012, 1, 13 - achTaxOffloadOffset);

        Application.refresh(msPayment);
        assertEquals(PaymentMethod.ACHCredit, msPayment.getMoneyMovementPaymentMethod());
        assertDates(msPayment, 2012, 2, 15 - achTaxOffloadOffset);

        Application.refresh(orPayment);
        assertEquals(PaymentMethod.ACHCredit, orPayment.getMoneyMovementPaymentMethod());
        assertDates(orPayment, 2012, 3, 5);

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testUpdatePaymentMethodDoesNotChangeSettlementDateAfterThreshold() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-501-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IL-501-PAYMENT", DepositFrequencyCode.QUARTERMONTHLY, SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "IL-501-PAYMENT", false);

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "ILDOR");
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("IL-501-PAYMENT").setAgencyTaxpayerId(company.getFedTaxId() + "0123");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "ILDOR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012,1,1);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2012-01-10"), new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"16"}, new String[]{"12000"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction ilPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("IL-501-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, ilPayment.getMoneyMovementPaymentMethod());
        assertDates(ilPayment, 2012, 1, 9);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateACHAgentEnabledFlags(company, "IL-501-PAYMENT", true);

        PayrollServices.beginUnitOfWork();
        Application.refresh(ilPayment);
        assertEquals(PaymentMethod.ACHCredit, ilPayment.getMoneyMovementPaymentMethod());
        assertDates(ilPayment, 2012, 1, 11 - achTaxOffloadOffset);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testUpdateCompanyLawCalculatesPaymentMethods() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012,1,1);
        DataLoadServices.updateAgencyTaxpayerId(company,"PA-501-PAYMENT","12348912123");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        PayrollServices.beginUnitOfWork();
        assertEquals(PaymentMethod.ACHCredit, assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("PA-501-PAYMENT").find()).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "40"));
        companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(null);

        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals(PaymentMethod.CheckPayment, assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("PA-501-PAYMENT").find()).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();



    }

    @Test
    public void testStatePaymentEnrollmentHoldRemovedOnVoidToZeroPayment() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("AR-941M-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateRequiredIDs(company, "AR-941M-PAYMENT", false);

        DataLoadServices.setPSPDate(2012,1,1);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        DataLoadServices.setPSPDate(2012,1,6);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction arPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AR-941M-PAYMENT").find());
        assertEquals(null, arPayment.getMoneyMovementPaymentMethod());
        assertEquals(PaymentOnHoldReason.Enrollment, assertOne(arPayment.getActiveOnHoldReasons()).getOnHoldReasonCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012,1,12);

        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(arPayment);
        assertEquals(null, arPayment.getMoneyMovementPaymentMethod());
        assertEquals(TaxPaymentStatus.AcknowledgedByAgency, arPayment.getTaxPaymentStatus());
        assertEquals(SpcfCalendar.createInstance(2012, 2, 15, 8, 0, 0, 0), arPayment.getInitiationDate());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPaymentMethodSwitchToCheckPayment_AchOffload_DoesNotProcess() {

        DataLoadServices.setPSPDate(2011, 1, 1);
        SpcfCalendar supportDate = PSPDate.getPSPTime().copy();

        String[] statesList = new String[]{"PA"};
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        DataLoadServices.setPSPDate(2011, 4, 1);
        DataLoadServices.updateAgencyTaxpayerId(company,"PA-501-PAYMENT","12348912123");
        DataLoadServices.runPayrollRun(company, statesList, supportDate, new DateDTO("2011-04-07"), false,new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setTaxPaymentStatuses(TaxPaymentStatus.ReadyToSend).find());
        assertEquals("Payment Template code", "PA-501-PAYMENT", mmt.getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("PA state payment status", PaymentStatus.Created, mmt.getStatus());
        assertEquals("PA state payment Payment method", PaymentMethod.ACHCredit, mmt.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "PADOR");
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo("PA-501-PAYMENT")).getFirst();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId("-1213435");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        SpcfCalendar checkPaymentInitDate = mmt.getInitiationDate();
        if(achTaxOffloadOffset == 1) {
        	CalendarUtils.addBusinessDays(checkPaymentInitDate, -1);//ACH --> 1 Day, Check Payment - 2 Day.
        }
        DataLoadServices.runOffloadTaxPayments(checkPaymentInitDate);

        PayrollServices.beginUnitOfWork();
        Application.refresh(mmt);
        assertEquals("PA state payment status", PaymentStatus.Created, mmt.getStatus());
        assertEquals("PA state payment Payment method", PaymentMethod.CheckPayment, mmt.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        new PrintedChecksSelector().processCheckBatchSelection(PaymentMethod.CheckPayment);

        PayrollServices.beginUnitOfWork();
        Application.refresh(mmt);
        assertEquals("PA state payment status", PaymentStatus.InProcess, mmt.getStatus());
        assertNotNull("PA state payment check number", mmt.getReferenceNumber());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAgencyIdUpdateOnPendingPayments() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("OH-IT501-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "OH");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        String states[] = {"OH"};
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "OH-IT501-PAYMENT", PaymentMethod.ACHCredit, true));
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "OHDOT");
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("OH-IT501-PAYMENT").setAgencyTaxpayerId(null);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "OHDOT", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2012, 1, 1);
        DataLoadServices.runPayrollRun(company, states,  new HashMap<String, String>(), new DateDTO("2012-01-20"));

        assertPaymentMethod(company, "OH-IT501-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "OH-IT501-PAYMENT", SettlementType.CheckType);

        PayrollServices.beginUnitOfWork();
        companyAgency = CompanyAgency.findCompanyAgency(company, "OHDOT");
        companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("OH-IT501-PAYMENT").setAgencyTaxpayerId("12 245678");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "OHDOT", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "OH-IT501-PAYMENT", PaymentMethod.ACHCredit);
        assertSettlementType(company, "OH-IT501-PAYMENT", SettlementType.ACH);

    }

    @Test
    public void testACHCreditPaymentMethodForLASUI() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("LA-ES61-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "LA");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        String states[] = {"LA"};
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2012-01-20"), false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        assertPaymentMethod(company, "LA-ES61-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "LA-ES61-PAYMENT", SettlementType.CheckType);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "LA-ES61-PAYMENT", PaymentMethod.ACHCredit, true));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "LA-ES61-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "LA-ES61-PAYMENT", SettlementType.CheckType);

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "LADOL");
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgency);
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("LA-ES61-PAYMENT").setAgencyTaxpayerId("5678934");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "LADOL", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "LA-ES61-PAYMENT", PaymentMethod.ACHCredit);
        assertSettlementType(company, "LA-ES61-PAYMENT", SettlementType.ACH);


        DataLoadServices.updateRequiredIDs(company, "LA-ES61-PAYMENT", false);

        assertPaymentMethod(company, "LA-ES61-PAYMENT", PaymentMethod.CheckPayment);
        assertSettlementType(company, "LA-ES61-PAYMENT", SettlementType.CheckType);

    }

    @Test
    public void testACHDebitPaymentMethodForNMSUI() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"NM"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NM-ES903A-PAYMENT").find());
        assertEquals("NM-ES903A-PAYMENT Payment method",  PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("NM-ES903A-PAYMENT Payment Amount", new SpcfMoney("610"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("NM-ES903A-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("NM-ES903A-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 4, 29);
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("NM-ES903A-PAYMENT Tax payment status", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("NM-ES903A-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testACHDebitPaymentMethodForWAF5208() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WA-F5208-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WA-F5208-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "WA-F5208-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"WA-F5208-PAYMENT","123-134456-11-1");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2011-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WA-F5208-PAYMENT").find());
        assertEquals("WA-F5208-PAYMENT Payment method",  PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("WA-F5208-PAYMENT Payment Amount", new SpcfMoney("1180"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("WA-F5208-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("WA-F5208-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 4, 29);
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("WA-F5208-PAYMENT Tax payment status", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("WA-F5208-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }


    /**
     * PSP-13606: Add ACH Debit Payment Method for NY-MTA305-PAYMENT
     * Verify if NY Metro Payments are created with ACHDebit Payment Method
     */
    @Test
    public void testACHDebitPaymentMethodForNYMTA305() {
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        String psid = "123456789";
        String[] statesList = {"NY"};
        DataLoadServices.setPSPDate(2015, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        String stateAccessCode = "";
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO("NY-MTA305-PAYMENT", "State Access Code", stateAccessCode);
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 3, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 =
                DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1,
                        company,
                        new DateDTO("2015-03-05"),
                        emps,
                        new String[]{"36", "197"},
                        new String[]{"425", "136"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments()
                .setPaymentTemplate(PaymentTemplate.findPaymentTemplate("NY-MTA305-PAYMENT"))
                .setDueDate(SpcfCalendar.createInstance(2015, 4, 30, SpcfTimeZone.getLocalTimeZone()))
                .setCompany(company).setReadyToSend().find());
        assertEquals("Payment Frequency ", DepositFrequencyCode.QUARTERLY, moneyMovementTransaction.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("Payment Amount", SpcfMoney.createInstance("136.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("Payment Method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("Payment Initiation date", SpcfCalendar.createInstance(2015, 04, 28, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getInitiationDate().toLocal());
        assertEquals("Payment Due date", SpcfCalendar.createInstance(2015, 4, 30, SpcfTimeZone.getLocalTimeZone()), moneyMovementTransaction.getDueDate().toLocal());

        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testACHCreditPaymentMethodForPASUI() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"PA"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("PA-UC2-PAYMENT").find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("PA-UC2-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("PA-UC2-PAYMENT Payment Amount", new SpcfMoney("516"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("PA-UC2-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("PA-UC2-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "PADLI");
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().getFirst();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId(null);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application .refresh(moneyMovementTransaction);
        assertEquals("PA-UC2-PAYMENT Payment method",  null, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("PA-UC2-PAYMENT Payment Amount", new SpcfMoney("516"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("PA-UC2-PAYMENT Tax payment status", TaxPaymentStatus.OnHold, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("PA-UC2-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void manualChangeDoesNotGetAutomaticallyChangedBack() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-GAV-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.invalidateDepositFrequencies(company, "GA-GAV-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.QUARTERLY);

        DataLoadServices.setPSPDate(2013, 1, 19);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-25"));

        DataLoadServices.setPSPDate(2013, 1, 20);
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("GA-GAV-PAYMENT").find());

        assertSuccess(PayrollServices.paymentManager.changePaymentMethod(payment.getCompany().getSourceSystemCd(),
                payment.getCompany().getSourceCompanyId(), payment.getId(), PaymentMethod.CheckPayment));
        
        if(achTaxOffloadOffset == 1) {
        	//Make CheckPayment as first payment preference.
        	DomainEntitySet<PaymentTemplatePaymentMethod> PaymentTemplatePaymentMethodSet = Application.find(PaymentTemplatePaymentMethod.class, PaymentTemplatePaymentMethod.PaymentTemplate().PaymentTemplateCd().equalTo("GA-GAV-PAYMENT"));
        	for(PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : PaymentTemplatePaymentMethodSet) {
        		int paymentOrder = paymentTemplatePaymentMethod.getPaymentMethodOrder();
        		paymentOrder = paymentTemplatePaymentMethod.getPaymentMethod() == PaymentMethod.ACHCredit? 2 : 1;//Swap the payment order. Check Payment should be 1.
        		paymentTemplatePaymentMethod.setPaymentMethodOrder(paymentOrder);
        		Application.save(paymentTemplatePaymentMethod);
        	}
        }
        
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.ACHCredit);

        //submit a new payroll that will combine
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-02-05"));
        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.ACHCredit);

        DataLoadServices.setPSPDate(2013, 1, 23);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 30);
        DataLoadServices.runACHTransactionProcessor(0);

        DataLoadServices.setPSPDate(2013, 2, 1);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 2, 5);
        DataLoadServices.returnTxns(payrollRun, TransactionTypeCode.EmployerTaxDebit);
        //At this point, we will have an on hold payment for ACH.  If the check offloads before the redebit completes, it will go off hold as ACH as well.
        //We will not handle that scenario.
        DataLoadServices.runOffload();

        DataLoadServices.runACHTransactionProcessor();
        //If it completes

        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.ACHCredit);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(Application.refresh(payment), PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.ACHCredit);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.expireTaxPaymentOnHoldReason(Application.refresh(payment), PaymentOnHoldReason.Agent));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.ACHCredit);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updateInitiationDate(payment.getId().toString(), SpcfCalendar.createInstance(2013, 10, 1).toLocal()));
        PayrollServices.commitUnitOfWork();

        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.CheckPayment);
        PayrollServices.beginUnitOfWork();
        if(achTaxOffloadOffset == 1) {
        	//Restore previously changed entries. Make ACH as first payment preference.
        	DomainEntitySet<PaymentTemplatePaymentMethod> PaymentTemplatePaymentMethodSet = Application.find(PaymentTemplatePaymentMethod.class, PaymentTemplatePaymentMethod.PaymentTemplate().PaymentTemplateCd().equalTo("GA-GAV-PAYMENT"));
        	for(PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : PaymentTemplatePaymentMethodSet) {
        		int paymentOrder = paymentTemplatePaymentMethod.getPaymentMethodOrder();
        		paymentOrder = paymentTemplatePaymentMethod.getPaymentMethod() == PaymentMethod.ACHCredit? 1 : 2;//Swap the payment order. Check Payment should be 1.
        		paymentTemplatePaymentMethod.setPaymentMethodOrder(paymentOrder);
        		Application.save(paymentTemplatePaymentMethod);
        	}
        }
        PayrollServices.commitUnitOfWork();
        assertPaymentMethod(company, "GA-GAV-PAYMENT", PaymentMethod.ACHCredit);
    }

    @Test
    public void manualChangeGetsAutomaticallyChangedBackIfNotValid() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("AL-CR4WH-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 10);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        String[] laws = {"1", "3"};
        String[] amounts = {"500", "500"};

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-15"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);

        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction alPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("AL-CR4WH-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, alPayment.getMoneyMovementPaymentMethod());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2013-01-15"), new ArrayList<Employee>(company.getCloudEmployees()),laws, amounts);
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        //ACHCredit because Check no longer valid
        PayrollServices.beginUnitOfWork();
        Application.refresh(alPayment);
        assertEquals(PaymentMethod.ACHCredit, alPayment.getMoneyMovementPaymentMethod());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAgencyIdForGAWithholding() {
        String[] states = {"GA"};
        long psid = 12345678l;
        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        Company company = assertOne(DataLoadServices.setupCompany(psid, 1, states, PaymentTemplateCategory.Withholding));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2014, 1, 1, SpcfTimeZone.getLocalTimeZone()), new DateDTO(2014, 1, 5), true);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        assertEquals("Agency Id", "1234567-aB", moneyMovementTransaction.getAgencyTaxpayerId());
        EntryDetailRecord entryDetailRecord = assertOne(moneyMovementTransaction.getEntryDetailRecordCollection().find(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));
        assertEquals("TXP Record", "TXP*1234567-AB*011*140131*T*0000002400\\", entryDetailRecord.getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

    }

    private static void assertDates(MoneyMovementTransaction payment, int initYear, int initMonth, int initDay) {
        SpcfCalendar initDate = SpcfCalendar.createInstance(initYear, initMonth, initDay, 8, 0, 0, 0);
        SpcfCalendar settlementDate = initDate.copy();
        int offset = MoneyMovementTransaction.getPaymentMethodDayOffset(payment.getMoneyMovementPaymentMethod(), payment.getPaymentTemplate());
        CalendarUtils.addBusinessDays(settlementDate, offset);
        assertEquals(initDate, payment.getInitiationDate());
        assertEquals(settlementDate, payment.getSettlementDate());
    }

    private static void assertPaymentMethod(Company company, String paymentTemplateCode, PaymentMethod expected) {
        assertPaymentMethod(company, paymentTemplateCode, expected, false);
    }

    private static void assertPaymentMethod(Company company, String paymentTemplateCode, PaymentMethod expected, boolean onHold) {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd(paymentTemplateCode).setPending().find();
        assertTrue(moneyMovementTransactions.size() > 0);
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertEquals(expected, moneyMovementTransaction.getMoneyMovementPaymentMethod());
            TaxPaymentOnHoldReason enrollmentHoldReason = moneyMovementTransaction.getActiveOnHoldReason(PaymentOnHoldReason.Enrollment);
            if (onHold) {
                assertNotNull(enrollmentHoldReason);
                assertNotNull(enrollmentHoldReason.getCompany());
                assertEquals(moneyMovementTransaction.getCompany(),enrollmentHoldReason.getCompany());
            } else {
                assertNull(enrollmentHoldReason);
            }

        }
        PayrollServices.rollbackUnitOfWork();
    }

    private static void assertSettlementType(Company company, String paymentTemplateCode, SettlementType pExpected) {
        PayrollServices.beginUnitOfWork();
        for (MoneyMovementTransaction moneyMovementTransaction :MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd(paymentTemplateCode).setPending().find()) {
            assertSettlementType(moneyMovementTransaction, pExpected);
        }
        PayrollServices.rollbackUnitOfWork();
    }

    private static void assertSettlementType(MoneyMovementTransaction pMoneyMovementTransaction, SettlementType pSettlementTypeExpected) {
        BankAccount debitAccount = null;
        BankAccount creditAccount = null;

        switch (pSettlementTypeExpected) {
            case CheckType:
                debitAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_CHECK).getBankAccount();
                break;
            case ACH:
                debitAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.AgencyTaxCredit, CreditDebitCode.Debit).getBankAccount();
                PaymentTemplateBankAccount pmtTemplateBankAccount = PaymentTemplateBankAccount.findActiveBankAccount(pMoneyMovementTransaction.getPaymentTemplate());
                creditAccount = pmtTemplateBankAccount.getBankAccount();
                break;
            case EFTPS:
                debitAccount = IntuitBankAccount.findIntuitBankAccount(TransactionTypeCode.AgencyTaxCredit, CreditDebitCode.Debit).getBankAccount();
                break;
        }

        for (FinancialTransaction financialTransaction : pMoneyMovementTransaction.getFinancialTransactionCollection().
                find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit))) {
            assertEquals("settlement type", pSettlementTypeExpected, financialTransaction.getSettlementTypeCd());
            if(debitAccount != null) {
                assertEquals("debit account", debitAccount, financialTransaction.getDebitBankAccount());
            } else {
                assertNull("debit account", financialTransaction.getDebitBankAccount());
            }

            if(creditAccount != null) {
                assertEquals("credit account", creditAccount, financialTransaction.getCreditBankAccount());
            } else {
                assertNull("credit account", financialTransaction.getCreditBankAccount());
            }

        }
    }

    private PayrollRun runPayroll(Company company, DateDTO date, String amount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"47", "61", "62", "63", "64", "66", "1", "25", "12", "20", "7"}, new String[]{amount, amount, amount, amount, amount, amount, amount, amount, amount, amount, amount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

    public void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testACHCreditPaymentMethodForIDSUI() {
        String paymentTemplate = "ID-020-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 1, 1));
        String[] statesList = new String[]{"ID"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2016, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2016-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("ID-020-PAYMENT").find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("ID-020-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("ID-020-PAYMENT Payment Amount", new SpcfMoney("484"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("ID-020-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("ID-020-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();
        String expectedTXP = "TXP*0122456789*13090*160331*T*0000048400\\";
        assertNotNull("TXP Record", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "IDCL");
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().getFirst();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId(null);
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("ID-020-PAYMENT Payment method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("ID-020-PAYMENT Payment Amount", new SpcfMoney("484"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("ID-020-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("ID-020-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyAgency = CompanyAgency.findCompanyAgency(company, "IDCL");
        companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().getFirst();
        companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId("0122456789");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2016, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);
        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);
        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 44);
        assertEquals("TXP record from ach file", expectedTXP, actualTXP);
    }

    @Test
    public void testACHCreditPaymentMethodForGASUI() {
        String paymentTemplate = "GA-DOL4-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 1, 1));
        String[] statesList = new String[]{"GA"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2016, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2016-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-DOL4-PAYMENT").find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("GA-DOL4-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("GA-DOL4-PAYMENT Payment Amount", new SpcfMoney("494"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("GA-DOL4-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("GA-DOL4-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();
        String expectedTXP = "TXP*12345712*13000*1*2016*00000049400*CRI                 \\                     ";
        assertNotNull("TXP Record", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "GADOL");
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().getFirst();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId("136729-19");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2016, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);
        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);
        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 83);
        expectedTXP="TXP*13672919*13000*1*2016*00000049400*CRI                 \\                     ";
        assertEquals("TXP record from ach file", expectedTXP, actualTXP);
    }


    /**
     * PSP-15767: Change payment method for KYDES KY-UI3-PAYMENT
     * Verify if KYDES Payments are created with ACHDebit Payment Method
     */
    @Test
    public void testACHDebitPaymentMethodForKYDES() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2018, 2, 1));
        String[] statesList = new String[]{"KY"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2018, 3, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2018-03-05");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KY-UI3-PAYMENT").find());
        assertEquals("KY-UI3-PAYMENT Payment method",  PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("KY-UI3-PAYMENT Payment Amount", new SpcfMoney("604"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("KY-UI3-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("KY-UI3-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2018, 4, 27);
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("KY-UI3-PAYMENT Tax payment status", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("KY-UI3-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }


    /**
     * PSP-15862: Change the default payment method for SCESC SC-UCE120-PAYMENT to ACH Debit
     * Verify if SCESC  Payments are created with ACHDebit Payment Method
     */
    /**
     * PSP-18895: Change ACHDebit to ACHCredit
     */
    @Test
    public void testACHDebitPaymentMethodForSCESC() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("SC-UCE120-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT",
                DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "SC-UCE120-PAYMENT",
                DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, "SC-UCE120-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"SC-UCE120-PAYMENT","11222334");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction =
                assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("SC-UCE120-PAYMENT").find());
        assertEquals("SC-UCE120-PAYMENT Agency Id", "11222334",
                moneyMovementTransaction.getAgencyTaxpayerId());
        assertFalse("SC-UCE120-PAYMENT Payment method ACHDebit",
                moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.ACHDebit);
        assertEquals("SC-UCE120-PAYMENT Payment Amount", new SpcfMoney("1140"),
                moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("SC-UCE120-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend,
                moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("SC-UCE120-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);

        PayrollServices.commitUnitOfWork();
    }

    /**
     * PSP-11851
     * Add Supercheck to the payments methods for MS-UI23-PAYMENT
     */
    @Test
    public void testSuperCheckPaymentMethodForMSSUI() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"MS"};
        Company company = assertOne(DataLoadServices.setupCompany(158906L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.SuperCheck));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MS-UI23-PAYMENT").find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("MS-UI23-PAYMENT Payment method",  PaymentMethod.SuperCheck, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("MS-UI23-PAYMENT Payment Amount", new SpcfMoney("590"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("MS-UI23-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("MS-UI23-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * PSP-16127
     * Change the default payment method for UT-F3-PAYMENT to ACH Debit
     */
    @Test
    public void testACHDebitPaymentMethodForUTDWS() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"UT"};
        Company company = assertOne(DataLoadServices.setupCompany(158906L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("UT-F3-PAYMENT").find());
        //moneyMovementTransaction.setAgencyTaxpayerId("C 4-417036-0");
        //System.out.println(moneyMovementTransaction.getAgencyTaxpayerId());
        Application.refresh(moneyMovementTransaction);

        assertEquals("UT-F3-PAYMENT Payment method",  PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("UT-F3-PAYMENT Payment Amount", new SpcfMoney("256"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("UT-F3-PAYMENTTax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("UT-F3-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(moneyMovementTransaction.getInitiationDate());
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);
        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("UT-F3-PAYMENT Tax payment status", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("UT-F3-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testACHCreditPaymentMethodForORDORWH() {
        String paymentTemplate = "OR-OTCWH-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 1, 1));
        String[] statesList = new String[]{"OR"};
        Company company = assertOne(DataLoadServices.setupCompany(158906L, 1, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2016, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2016-01-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        DataLoadServices.updateAgencyTaxpayerId(company,paymentTemplate,"63259094-1");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("OR-OTCWH-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("OR-OTCWH-PAYMENT Payment Amount", new SpcfMoney("78"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("OR-OTCWH-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("OR-OTCWH-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();
        String expectedTXP = "TXP*632590941*01101*160331*S*0*S*7800*S*0\\";
        assertNotNull("TXP Record", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * PSP-17475: Change the default payment method for DCDES DC-UC30-PAYMENT to ACH Debit
     * Verify if DCDES  Payments are created with ACHDebit Payment Method
     */
    @Test
    public void testACHDebitPaymentMethodForDCUC30() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 2, 1));
        String[] statesList = new String[]{"DC"};
        Company company = assertOne(DataLoadServices.setupCompany(987654321, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2019, 3, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2019-03-05");


        PayrollServices.beginUnitOfWork();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(CompanyAgency.findCompanyAgency(company, "DCDES"));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("DC-UC30-PAYMENT").setAgencyTaxpayerId("565965");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "DCDES", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);



        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-UC30-PAYMENT").find());

        Application.refresh(moneyMovementTransaction);
        assertEquals("DC-UC30-PAYMENT Agency Id", "565965", moneyMovementTransaction.getAgencyTaxpayerId());
        assertEquals("DC-UC30-PAYMENT Payment method",  PaymentMethod.ACHDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("DC-UC30-PAYMENT Payment Amount", new SpcfMoney("556"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("DC-UC30-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("DC-UC30-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);

        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(moneyMovementTransaction.getInitiationDate());
        BatchJobManager.runJob(BatchJobType.AchDebitOffload);

        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("DC-UC30-PAYMENT Tax payment status", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("DC-UC30-PAYMENT status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * PSP-17475: Change the default payment method for DCDES DC-UC30-PAYMENT to ACH Debit
     * Verify if DCDES  Payments are created with ACHDebit Payment Method
     */
    @Test
    public void testPaymentMethodSwitchToCheckPaymentForDCUC30() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 2, 1));
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2019, 3, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2019-03-05");

        String[] statesList = new String[]{"DC"};
        Company company = assertOne(DataLoadServices.setupCompany(987654327, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));

        PayrollServices.beginUnitOfWork();
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(company, "DCDES");
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = companyAgency.getCompanyAgencyPaymentTemplateCollection().find(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo("DC-UC30-PAYMENT")).getFirst();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(companyAgencyPaymentTemplate.getCompanyAgency());

        companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId("-1213435");
        // Switch to valid Agency Id to confirm Payment Method result is ACHDebit
        //companyAgencyDTO.getCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()).setAgencyTaxpayerId("121 343");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), companyAgency.getAgency().getAgencyId(), companyAgencyDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList, supportDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-UC30-PAYMENT").find());

        assertEquals("DC state payment Payment method", PaymentMethod.CheckPayment, moneyMovementTransaction.getMoneyMovementPaymentMethod());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testCheckPaymentForDCPFLPAYMENT() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("DC-PFL-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "DC-PFL-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "DC-PFL-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-PFL-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAchDebitForDCPFLPAYMENTWithCorrectAgencyID() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("DC-PFL-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "DC-PFL-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "DC-PFL-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"DC-PFL-PAYMENT","123123");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-PFL-PAYMENT").find());
        assertEquals(PaymentMethod.ACHDebit, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testAchDebitForDCPFLPAYMENTWithInCorrectAgencyID() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("DC-PFL-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "DC-PFL-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "DC-PFL-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"DC-PFL-PAYMENT","123-1234");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-PFL-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAchDebitForDCPFLPAYMENTWithMissingAgencyID() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("DC-PFL-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "DC-PFL-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "DC-PFL-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"DC-PFL-PAYMENT","");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-PFL-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    /*
    Ref Jira : https://jira.intuit.com/browse/PSP-17992
     */
    @Test
    public void testCheckPaymentRemoveForHI() {
        SpcfCalendar supportDate = SpcfCalendar.createInstance(1999, 1, 1, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("HI-UCB6-PAYMENT", supportDate);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 11, 1));
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("HI-UCB6-PAYMENT").find());
        assertEquals(PaymentMethod.ACHDebit, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }
    /* PSP-17711
        Set Up ACH Credit payment method to MT-UI5-PAYMENT*/
    @Test
    public void testPaymentMethodSetUpACHCreditForMTU15() {
        String paymentTemplate = "MT-UI5-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 5, 1));
        String[] statesList = new String[]{"MT"};
        Company company = assertOne(DataLoadServices.setupCompany(113104L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2019, 5, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2019-05-10");

        DataLoadServices.runPayrollRun(company, statesList, supportDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        DataLoadServices.updateAgencyTaxpayerId(company,paymentTemplate,"031 2632");


        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("MT-UI5-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("MT-UI5-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("MT-UI5-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();
        String expectedTXP = "705TXP*0312632*13000*190630*2*D JS ELECTRIC INC\\";
        assertNotNull("TXP Record", entryDetailRecordCollection.getFirst().getTxpRecordData());
        //assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

    }

    /**
     * PSP-17847: Change the default payment method for WAESD WA-PFML-PAYMENT to ACH Credit
     * Verify if WAESD  Payments are created with ACHCredit Payment Method
     */
    @Test
    public void testACHCreditPaymentMethodForWAESD() {
        String paymentTemplate = "WA-PFML-PAYMENT";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WA-PFML-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WA-PFML-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "WA-PFML-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"WA-PFML-PAYMENT","112 223 334");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WA-PFML-PAYMENT").find());

        assertEquals("WA-PFML-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("WA-PFML-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("WA-PFML-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();
        String expectedTXP = "TRN**652012787\\N1****A200000005\\RMR**C602675675**0.01\\";
        assertNotNull("TXP Record", entryDetailRecordCollection.getFirst().getTxpRecordData());
        //assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

    }
    /*
    Ref Jira : https://jira.intuit.com/browse/PSP-17992
     */
    @Test
    public void testCreateCompanyWithACHDebitForHI(){
        SpcfCalendar supportDate = SpcfCalendar.createInstance(1999,1, 1, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 06, 1));
        DateDTO payrollDate = new DateDTO("2019-07-08");
        String[] statesList = new String[]{"HI"};

        Company company = assertOne(DataLoadServices.setupCompany(227472302, 1, statesList,
                PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit));

        PayrollServices.beginUnitOfWork();
        CompanyAgencyDTO companyAgencyDTO = PayrollServices.dtoFactory.create(CompanyAgency.findCompanyAgency(company,
                "HIDLIR"));
        companyAgencyDTO.getCompanyAgencyPaymentTemplate("HI-UCB6-PAYMENT").setAgencyTaxpayerId("22741");
        assertSuccess(PayrollServices.companyManager.updateCompanyAgency(company.getSourceSystemCd(), company.getSourceCompanyId(), "HIDLIR", companyAgencyDTO));
        PayrollServices.commitUnitOfWork();
      DataLoadServices.runPayrollRun(company, statesList,supportDate,payrollDate,false,
                new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments()
                .setPaymentTemplateCd("HI-UCB6-PAYMENT").find());
        Application.refresh(dcPayment);

        assertEquals("HI-UCB6-PAYMENT Agency Id", "22741", dcPayment.getAgencyTaxpayerId());
        assertEquals(PaymentMethod.ACHDebit, dcPayment.getMoneyMovementPaymentMethod());
        assertEquals("HI-UCB6-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, dcPayment.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    /**
     * PSP-17946: Change the default payment method for KS KS-KCNS100-PAYMENT to ACH Credit
     * Verify if KCNS100  Payments are created with ACHCredit Payment Method
     */
    @Test
    public void testACHCreditPaymentMethodForKSKCNS100() {
        String paymentTemplate = "KS-KCNS100-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 5, 1));
        String[] statesList = new String[]{"KS"};
        Company company = assertOne(DataLoadServices.setupCompany(157904L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2019, 5, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2019-05-07");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        DataLoadServices.updateAgencyTaxpayerId(company,paymentTemplate,"6321941");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("KS-KCNS100-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("KS-KCNS100-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("KS-KCNS100-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        PayrollServices.commitUnitOfWork();

    }


    /**
     * PSP-17946: Payment method for KS KS-KCNS100-PAYMENT
     * Verify if KCNS100  Payments are created with CheckPayment Payment Method
     */
    @Test
    public void testAchDebitForKSKCNS100PAYMENTWithMissingAgencyID() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KS-KCNS100-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "KS-KCNS100-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "KS-KCNS100-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"KS-KCNS100-PAYMENT","");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KS-KCNS100-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAchCreditForCT_2MAGAgencyID() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CT-2MAG-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();


        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CT-2MAG-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "CT-2MAG-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"CT-2MAG-PAYMENT","13-34521-0-00");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CT-2MAG-PAYMENT").find()); //fetching mnymvmnt and setting pymnt template
        assertEquals(PaymentMethod.ACHCredit, dcPayment.getMoneyMovementPaymentMethod()); //if the format is correct then ACHDebit else Cheque
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testACHCreditPaymentMethodForCOFAMLIPAYMENT() {

        String paymentTemplate = "CO-FAMLI-PAYMENT";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();  //create  company

        Application.beginUnitOfWork();
        Application.refresh(company).setFedTaxId("11-3411111");
        Application.commitUnitOfWork();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY); 
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true); //update ACH enabled flag

        String ein = company.getFedTaxId();
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, "1234876512");
        

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2021-01-07")); 

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find()); 
        Application.refresh(moneyMovementTransaction);

        assertEquals("CO-FAMLI-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod()); 
        assertEquals("CO-FAMLI-PAYMENT Payment Amount", new SpcfMoney("1780"), moneyMovementTransaction.getMoneyMovementTransactionAmount()); 
        assertEquals("CO-FAMLI-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus()); 
        assertEquals("CO-FAMLI-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus()); 
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection(); 

        String amount = changeAmountFormat(moneyMovementTransaction.getMoneyMovementTransactionAmount(),4);


        String expectedTXP = "705TXP*1234876512*"+amount+"*"+ein+"*"+getPayorFEIN()+"\\";

        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2021, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);

        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP  record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);
        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(0, 83).trim();
        assertEquals("TXP Record ",expectedTXP, actualTXP);
    }



    @Test
    public void testACHCreditForWAPFMLPaymentFEIN(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WA-PFML-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WA-PFML-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "WA-PFML-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"WA-PFML-PAYMENT","112 223 334");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WA-PFML-PAYMENT").find());
        assertEquals("WA-PFML-PAYMENT Agency Id", "112 223 334", moneyMovementTransaction.getAgencyTaxpayerId());
        assertEquals("WA-PFML-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("WA-PFML-PAYMENT Payment Amount", new SpcfMoney("1652"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("WA-PFML-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("WA-PFML-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
        Application.save(moneyMovementTransaction);
        PayrollServices.commitUnitOfWork();

    }
    public void testACHCreditPaymentMethodForMAESD(){
        String paymentTemplate="MA-PFML-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019,11,1));
        String[]statesList=new String[]{"MA"};
        Company company=assertOne(DataLoadServices.setupGenericCompany(154904L,1,statesList,PaymentTemplateCategory.Withholding,PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate=SpcfCalendar.createInstance(2019,11,1,SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate=new DateDTO("2019-11-07");

        DataLoadServices.runGenericPayrollRun(company,statesList,supportedDate,payrollDate,false,new HashMap<String,String>(),PaymentTemplateCategory.Withholding);

        DataLoadServices.updateAgencyTaxpayerId(company,paymentTemplate,"PFM-11441047-003");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction=assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("MA-PFML-PAYMENTPaymentmethod",PaymentMethod.ACHCredit,moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("MA-PFML-PAYMENTTaxpaymentstatus",TaxPaymentStatus.ReadyToSend,moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("MA-PFML-PAYMENTstatus",PaymentStatus.Created,moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord>entryDetailRecordCollection=moneyMovementTransaction.getEntryDetailRecordCollection();
        String expectedTXP="TRN**652012787\\N1****A200000005\\RMR**C602675675**0.01\\";
        PayrollServices.commitUnitOfWork();

    }
    @Test
    public void testACHCreditPaymentMethodForORSTTV() {

        String paymentTemplate = "OR-STTV-PAYMENT";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2020, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, "17676765-9");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2020-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("OR-STTV-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("OR-STTV-PAYMENT Payment Amount", new SpcfMoney("820"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("OR-STTV-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("OR-STTV-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String expectedTXP = "TXP*176767659*01113*200331*S*82000\\";
        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2020, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);
        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        System.out.println(achLines);
        assertNotNull(achLines);

        /*
        101 02100002197226160002004281325W094101JPMORGAN CHASE         INTUIT
        5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY200430200430   1021000020000001
        6221232067070000015091       0000082000000000176767659TEST_COMPANY_1          1000000000100006
        705TXP*176767659000000*01113*200331*S*82000\                                       00010100006
        822000000200123206700000000000000000000820009118556001                         021000020000001
        5225INTUIT                     7700346619118556001CCDEFT TAX PY200430200430   1021000020000002
        627021000021911855633        0000082000911855633      INTUIT TAX              0000000000100007
        822500000100021000020000000820000000000000009118556001                         021000020000002
        9000002000001000000030014420672000000082000000000082000
        9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
        */

        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 66).trim();
        assertEquals("TXP Record ",expectedTXP, actualTXP);
    }
    @Test
    public void testCheckPaymentRemoveForWYDOE(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WY-WYO056-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WY-WYO056-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "WY-WYO056-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WY-WYO056-PAYMENT").find());
        System.out.println(dcPayment.getMoneyMovementPaymentMethod());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddACHDebitForWYDOE(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WY-WYO056-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WY-WYO056-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "WY-WYO056-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"WY-WYO056-PAYMENT","4342345654");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WY-WYO056-PAYMENT").find());
        assertEquals(PaymentMethod.ACHDebit, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddACHDebitForAKNS(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("AK-AKNS-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "AK-AKNS-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "AK-AKNS-PAYMENT", true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company,"AK-AKNS-PAYMENT","58697456");
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("AK-AKNS-PAYMENT").find());
        assertEquals(PaymentMethod.ACHDebit, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testACHCreditPaymentMethodForSCESC(){
        String paymentTemplate = "SC-UCE120-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 1, 1));
        String[] statesList = new String[]{"SC"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI,
                PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2020, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2020-01-07");
        DataLoadServices.updateAgencyTaxpayerId(company,paymentTemplate,"53259094");
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(),
                PaymentTemplateCategory.SUI);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("SC-UCE120-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("SC-UCE120-PAYMENT Payment Amount", new SpcfMoney("570"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("SC-UCE120-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("SC-UCE120-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String expectedTXP = "TXP*53259094 *000000000*0000057000*0000000000000000000000000000000000000000000*\\";
        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2020, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);

        PayrollServices.commitUnitOfWork();
        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class,
                new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);

        /** OutPut--TraceNumber/EDRSeq will be different in each run
         * ----101 02100002197226160001604281325O094101JPMORGAN CHASE         INTUIT
         * ----5220TEST_COMPANY_1             158905   9118556001CCDEFT TAX PY160502160502   1021000020000001
         * ----6220611006064563931801       0000057000158905         TEST_COMPANY_1          1000000000100000
         * ----705TXP*53259094 *000000000*0000057000*0000000000000000000000000000000000000000000*\00010100020
         * ----822000000200061100600000000000000000000570009118556001                         021000020000001
         * ----5225INTUIT                     7700346619118556001CCDEFT TAX PY160502160502   1021000020000002
         * ----627021000021911855633        0000057000911855633      INTUIT TAX              0000000000100001
         * ----822500000100021000020000000570000000000000009118556001                         021000020000002
         * ----9000002000001000000030008210062000000057000000000057000
         * ----9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
         */
        String actualTXP = achLines[3].substring(3, 83).trim();
        assertEquals("TXP record from ach file", expectedTXP, actualTXP);
    }

    @Test
    public void testRemoveCheckPaymentForNCNC5PPayment() {
        SpcfCalendar supportDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NC-NC5P-PAYMENT", supportDate);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 11, 1));
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NC-NC5P-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRemoveCheckPaymentForNE941NPayment(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NE-941N-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NE-941N-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        /**
         * Verify ACH Debit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"NE-941N-PAYMENT","21121211");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NE-941N-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());;
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"NE-941N-PAYMENT","123112");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NE-941N-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRemoveCheckPaymentForGADOL4PAYMENTandIA600103PAYMENT(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-DOL4-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IA-600103-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-DOL4-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IA-600103-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        /**
         * Verify ACH Debit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"GA-DOL4-PAYMENT","201234-78");
        DataLoadServices.updateACHAgentEnabledFlags(company, "IA-600103-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company,"IA-600103-PAYMENT","00237214");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-DOL4-PAYMENT").find());
        MoneyMovementTransaction iaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("IA-600103-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, gaPayment.getMoneyMovementPaymentMethod());
        assertEquals(PaymentMethod.ACHCredit, iaPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"GA-DOL4-PAYMENT","123112");
        DataLoadServices.updateACHAgentEnabledFlags(company, "IA-600103-PAYMENT", false);
        DataLoadServices.updateAgencyTaxpayerId(company,"IA-600103-PAYMENT","123112");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction gaPayment1 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-DOL4-PAYMENT").find());
        MoneyMovementTransaction iaPayment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("IA-600103-PAYMENT").find());
        assertNull(gaPayment1.getMoneyMovementPaymentMethod());
        assertNull(iaPayment2.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testACHCreditPaymentMethodForDEDFR() {

        String paymentTemplate = "DE-DES-PAYMENT";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2020, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2020, 1, 1));
        System.out.println(company.getFedTaxId().toString());

        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);
        // Set correct Agency Id and verify ACHCredit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, "1-"+company.getFedTaxId().toString()+"-001");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2020-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("DE-DES-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("DE-DES-PAYMENT Payment Amount", new SpcfMoney("40"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("DE-DES-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("DE-DES-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String expectedTXP = "TXP*1"+company.getFedTaxId().toString()+"001*01106*200331*T*4000\\";
        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();
        SpcfCalendar taxOffloadDate = SpcfCalendar.createInstance(2020, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxOffloadDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(taxOffloadDate);
        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        System.out.println(achLines);
        assertNotNull(achLines);

    /*
    101 02100002197226160002004281325U094101JPMORGAN CHASE         INTUIT
    5220CRI                        TEST_00019118556001CCDEFT TAX PY200430200430   1021000020000001
    622021409169893000           0000004000TEST_0001      TEST_COMPANY_1          1000000000100000
    705TXP*1510336527001*01106*200331*T*4000\                                          00010100000
    822000000200021409160000000000000000000040009118556001                         021000020000001
    5225INTUIT                     7700346619118556001CCDEFT TAX PY200430200430   1021000020000002
    627021000021911855633        0000004000911855633      INTUIT TAX              0000000000100001
    822500000100021000020000000040000000000000009118556001                         021000020000002
    9000002000001000000030004240918000000004000000000004000
    9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
     */

        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 66).trim();
        assertEquals("TXP Record ",expectedTXP, actualTXP);

        //3rd line contains Bank details
        String expectedBankdetails = "021409169893000";
        assertTrue(achLines[2].contains(expectedBankdetails));


    }
    @Test
    public void testACHCreditPaymentMethodNullForCTPFML() {

        String paymentTemplate = "CT-PFML-PAYMENT";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);

        // Set any Agency Id and verify payment method is null
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, company.getSourceCompanyId());

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2021-01-08"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertNull(moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testACHCreditPaymentMethodForCTPFML() {

        String paymentTemplate = "CT-PFML-PAYMENT";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);

        // Set correct Agency Id and verify ACHCredit payment method is selected
        String ein = company.getFedTaxId();
        String fedTaxId = ein.substring(0,2)+"-"+ein.substring(2);
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, fedTaxId);

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2021-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("CT-PFML-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("CT-PFML-PAYMENT Payment Amount", new SpcfMoney("872"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("CT-PFML-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("CT-PFML-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String amount = changeAmountFormat(moneyMovementTransaction.getMoneyMovementTransactionAmount(),9);

        String expectedTXP = "*"+getPayorFEIN()+"*"+fedTaxId+"*"+"2021-03-31"+"*"+amount+"*";

        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());

        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2021, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);

        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);
        /*
        101 02100002197226160002004281325P094101JPMORGAN CHASE         INTUIT
        5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY200430200430   1021000020000001
        6220865006349986520          0000042800TEST_0001      TEST_COMPANY_1          1000000000100008
        705*10-0000000*91-9999956*2021-03-31*00000042800*00000000100*                 00010100008
        822000000200086500634000000000000000000428009118556001                         021000020000001
        5225INTUIT                     7700346619118556001CCDEFT TAX PY200430200430   1021000020000002
        627021000021911855633        0000042800911855633      INTUIT TAX              0000000000100009
        822500000100021000020000000428000000000000009118556001                         021000020000002
        9000002000001000000030010750065000000042800000000042800
        9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
        */
        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 83).trim();
        String expectedTxpAfterOffload = "*"+getPayorFEIN()+"*"+fedTaxId+"*"+"2021-03-31"+"*"+amount+"*"+"00000000000";
        assertEquals("TXP Record ",expectedTxpAfterOffload, actualTXP);
    }

    @Test
    public void testACHCreditPaymentMethodForCT2MAGPAYMENT() {

        String paymentTemplate = "CT-2MAG-PAYMENT";

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2021, 1, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);

        // Set correct Agency Id and verify ACHCredit payment method is selected
        String ein = company.getFedTaxId().replaceAll("-","");
        String fedTaxId = ein.substring(0,2)+"-"+ein.substring(2);
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, "48-67876-0-00");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2021-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("CT-2MAG-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("CT-2MAG-PAYMENT Payment Amount", new SpcfMoney("356"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("CT-2MAG-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("CT-2MAG-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String amount = changeAmountFormat(moneyMovementTransaction.getMoneyMovementTransactionAmount(),8);

        SpcfCalendar quaterEndDate = CalendarUtils.getLastDayOfQuarter(PSPDate.getPSPTime());

        String expectedTXP = "705TXP*"+ein+"*CTSUI*"+"210331"+"*T*"+amount+"\\";

        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());

        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2021, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);

        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);
        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(0, 83).trim();
        assertEquals("TXP Record ",expectedTXP, actualTXP);
    }

    @Test
    public void testCheckPaymentRemoveForUTF3(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("UT-F3-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "UT-F3-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));


        DataLoadServices.updateACHAgentEnabledFlags(company, "UT-F3-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("UT-F3-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify ACH Debit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"UT-F3-PAYMENT","Z 9-999999-9");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("UT-F3-PAYMENT").find());
        assertEquals(PaymentMethod.ACHDebit, payment.getMoneyMovementPaymentMethod());;
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"UT-F3-PAYMENT","123987");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("UT-F3-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCheckPaymentRemoveForDCOTR(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("DC-FR900-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "DC-FR900-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, "DC-FR900-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));


        DataLoadServices.updateAgencyTaxpayerId(company,"DC-FR900-PAYMENT","2376787653" );
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("DC-FR900-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testRemoveCheckPaymentMethodForSCUCE120() {

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("SC-UCE120-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "SC-UCE120-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "SC-UCE120-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("SC-UCE120-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();
    }
    
    @Test
    public void testACHCreditPaymentMethodForMOUI() {

        String paymentTemplate = "MO-MODES-PAYMENT";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2020, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);
        // Set correct Agency Id and verify ACHCredit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, "06-15228-1-00");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2020-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("MO-MODES-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("MO-MODES-PAYMENT Payment Amount", new SpcfMoney("428"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("MO-MODES-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("MO-MODES-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String expectedTXP = "TXP*06152281000000*99999*200331*T*42800*"+company.getFedTaxId().replaceAll("[^0-9]", "")+"*"+StringUtil.truncate(company.getLegalName().toUpperCase(), 24)+"\\";
        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2020, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);

        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        System.out.println(achLines);
        assertNotNull(achLines);

        /*
        101 02100002197226160002004281325P094101JPMORGAN CHASE         INTUIT
        5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY200430200430   1021000020000001
        6220865006349986520          0000042800TEST_0001      TEST_COMPANY_1          1000000000100008
        705TXP*ACCNT_10000000*99999*200331*T*428*000000001*TEST_COMPANY_1\                 00010100008
        822000000200086500634000000000000000000428009118556001                         021000020000001
        5225INTUIT                     7700346619118556001CCDEFT TAX PY200430200430   1021000020000002
        627021000021911855633        0000042800911855633      INTUIT TAX              0000000000100009
        822500000100021000020000000428000000000000009118556001                         021000020000002
        9000002000001000000030010750065000000042800000000042800
        9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
        */

        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 83).trim();
        assertEquals("TXP Record ",expectedTXP, actualTXP);
    }
  
    @Test
    public void testRemoveCheckPaymentForKYDORPayment() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("KY-K1-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "KY-K1-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "KY-K1-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KY-K1-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        assertEquals(1, dcPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify ACH Credit is getting populated if Agency id Valid
         */
        DataLoadServices.updateACHAgentEnabledFlags(company, "KY-K1-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company,"KY-K1-PAYMENT","328960asb");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KY-K1-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());
        assertEquals(0, payment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"KY-K1-PAYMENT","12398");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("KY-K1-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        assertEquals(1, payment2.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNewLawForMEUITD() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("ME-941C1ME-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "ME-941C1ME-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("ME-941C1ME-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, dcPayment.getMoneyMovementPaymentMethod());
        assertEquals(1,dcPayment.getFinancialTransactionCollection().find(FinancialTransaction.Law().LawId().equalTo("219")).size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "219"));
        companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(null);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateACHAgentEnabledFlags(company, "ME-941C1ME-PAYMENT", false);
        DataLoadServices.updateAgencyTaxpayerId(company,"ME-941C1ME-PAYMENT",null);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt=assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("ME-941C1ME-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, mmt.getMoneyMovementPaymentMethod());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRemoveCheckPaymentForNDSTCPayment() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("ND-306-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "ND-306-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("ND-306-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        assertEquals(1, dcPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify ACH Credit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"ND-306-PAYMENT",company.getFedTaxId()+ "23" );

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("ND-306-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());
        assertEquals(0, payment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"ND-306-PAYMENT","12398");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("ND-306-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        assertEquals(1, payment2.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

//dmehta2
    @Test
    public void testNewLawForMASUP() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-1700HI-PAYMENT", SpcfCalendar.createInstance(2021, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2021, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MA-1700HI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2021, 1, 1));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2021-02-03"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MA-1700HI-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, dcPayment.getMoneyMovementPaymentMethod());
        assertEquals(1,dcPayment.getFinancialTransactionCollection().find(FinancialTransaction.Law().LawId().equalTo("220")).size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "220"));
        companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(null);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateACHAgentEnabledFlags(company, "MA-1700HI-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company,"MA-1700HI-PAYMENT","13572468");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt=assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("MA-1700HI-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, mmt.getMoneyMovementPaymentMethod());
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testCheckPaymentForWACARESPAYMENT(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("WA-CARES-PAYMENT", SpcfCalendar.createInstance(2022, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2022, 7, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "WA-CARES-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2022, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "WA-CARES-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2022-07-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("WA-CARES-PAYMENT").find());
        assertEquals(PaymentMethod.CheckPayment, dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "221"));
        companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(null);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testRemoveCheckPaymentForORPFMSLPAYMENT(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("OR-PFMSL-PAYMENT", SpcfCalendar.createInstance(2023, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2023, 7, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "OR-PFMSL-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2023, 1, 1));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2023-07-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("OR-PFMSL-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "224"));
        companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(null);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

    }
    @Test
    public void testACHCreditPaymentMethodForORDORUI() {
        String paymentTemplate = "OR-OTCUI-PAYMENT";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 1, 1));
        String[] statesList = new String[]{"OR"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHCredit));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2016, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2016-01-07");

        DataLoadServices.updateACHAgentEnabledFlags(company, "OR-OTCUI-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate,"53259094-2");
        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI, "OR-OTCUI-PAYMENT");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        assertEquals("OR-OTCUI-PAYMENT Payment method",  PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("OR-OTCUI-PAYMENT Payment Amount", new SpcfMoney("1980"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("OR-OTCUI-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("OR-OTCUI-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());

        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String expectedTXP = "TXP*532590942*01102*160331*L*34400*L*34600\\";
        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();


        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2016, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);
        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        assertNotNull(achLines);

        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 83).trim();
        if(achLines[2].substring(34,37).equals("690")){
            expectedTXP = "TXP*532590942*01102*160331*L*34400*L*34600\\";
        }else {
            expectedTXP = "TXP*532590942*01101*160331*S*24000*S*0*S*70000\\";
        }
        assertEquals("TXP record from ach file", expectedTXP, actualTXP);
    }
    @Test
    public void testAchCreditForORPFMSLPAYMENT(){
        String paymentTemplate = "OR-PFMSL-PAYMENT";
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2020, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), paymentTemplate, DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2020, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, paymentTemplate, true);
        // Set correct Agency Id and verify ACHDebit payment method is selected
        DataLoadServices.updateAgencyTaxpayerId(company, paymentTemplate, "17676765-9");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2020-01-07"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd(paymentTemplate).find());
        Application.refresh(moneyMovementTransaction);
        Application.refresh(moneyMovementTransaction);
        assertEquals("OR-STTV-PAYMENT Payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        assertEquals("OR-STTV-PAYMENT Payment Amount", new SpcfMoney("1796"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("OR-STTV-PAYMENT Tax payment status", TaxPaymentStatus.ReadyToSend, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("OR-STTV-PAYMENT status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        DomainEntitySet<EntryDetailRecord> entryDetailRecordCollection = moneyMovementTransaction.getEntryDetailRecordCollection();

        String expectedTXP = "TXP*176767659*01125*200331*S*179600\\";
        assertNotNull("TXP Record ", entryDetailRecordCollection.getFirst().getTxpRecordData());
        assertEquals("TXP Record ",expectedTXP, entryDetailRecordCollection.getFirst().getTxpRecordData());
        PayrollServices.commitUnitOfWork();

        //Offload the tax payment and check the ach file for the txp record
        PayrollServices.beginUnitOfWork();
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2020, 4, 30, 20, 30, 0, 0, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        PSPDate.setPSPTime(statePaymentInitiationDate);
        PayrollServices.commitUnitOfWork();

        //Manually verified the ach file generated for the TXP record
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        DomainEntitySet<NACHAFile> nachaFiles = Application.find(NACHAFile.class, new Query<NACHAFile>().Where(NACHAFile.Status().equalTo(NACHAFileStatus.Finalized)));
        NACHAFile nachaFile = assertOne(nachaFiles);
        String fileContent = DataLoadServices.readFile(nachaFile.getFileName());
        String[] achLines = fileContent.split(System.getProperty("line.separator"));
        System.out.println(achLines);
        assertNotNull(achLines);

        /*
        101 02100002197226160002004281325W094101JPMORGAN CHASE         INTUIT
        5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY200430200430   1021000020000001
        6221232067070000015091       0000082000000000176767659TEST_COMPANY_1          1000000000100006
        705TXP*176767659000000*01113*200331*S*82000\                                       00010100006
        822000000200123206700000000000000000000820009118556001                         021000020000001
        5225INTUIT                     7700346619118556001CCDEFT TAX PY200430200430   1021000020000002
        627021000021911855633        0000082000911855633      INTUIT TAX              0000000000100007
        822500000100021000020000000820000000000000009118556001                         021000020000002
        9000002000001000000030014420672000000082000000000082000
        9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999
        */

        //4th line in the ach file is the TXP record
        String actualTXP = achLines[3].substring(3, 66).trim();
        assertEquals("TXP Record ",expectedTXP, actualTXP);
    }
    @Test
    public void testCheckPaymentForCOFAMLIPAYMENT(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CO-FAMLI-PAYMENT", SpcfCalendar.createInstance(2023, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2023, 7, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CO-FAMLI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2023, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "CO-FAMLI-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2023-07-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CO-FAMLI-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLawDTO companyLawDTO = PayrollServices.dtoFactory.create(CompanyLaw.findCompanyLaw(company, "222"));
        companyLawDTO.getQBDTPayrollItemInfoDTO().setAgencyId(null);
        assertSuccess(PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO));
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testRemoveCheckPaymentForSCDORPayment() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("SC-WH1601-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "SC-WH1601-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.updateACHAgentEnabledFlags(company, "SC-WH1601-PAYMENT", false);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction dcPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("SC-WH1601-PAYMENT").find());
        assertNull(dcPayment.getMoneyMovementPaymentMethod());
        assertEquals(1, dcPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify ACH Credit is getting populated if Agency id Valid
         */
        DataLoadServices.updateACHAgentEnabledFlags(company, "SC-WH1601-PAYMENT", false);
        DataLoadServices.updateAgencyTaxpayerId(company,"SC-WH1601-PAYMENT","123456783");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("SC-WH1601-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());
        assertEquals(0, payment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"SC-WH1601-PAYMENT","12398");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("SC-WH1601-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        assertEquals(1, payment2.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRemoveCheckPaymentForNCESCPayment() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NC-101-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NC-101-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        /**
         * Verify ACH Credit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"NC-101-PAYMENT","13-24-587 8");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NC-101-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());
        assertEquals(0, payment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"NC-101-PAYMENT","12398");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NC-101-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        assertEquals(1, payment2.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRemoveCheckPaymentForNEWDPayment() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NE-UI11T-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NE-UI11T-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        /**
         * Verify ACH Credit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"NE-UI11T-PAYMENT","7658567843");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NE-UI11T-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, payment.getMoneyMovementPaymentMethod());
        assertEquals(0, payment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();

        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"NE-UI11T-PAYMENT","12398");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction payment2 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NE-UI11T-PAYMENT").find());
        assertNull(payment2.getMoneyMovementPaymentMethod());
        assertEquals(1, payment2.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * PSP-25246: Remove Check Payment Method for ORDOR, OR-OTCUI-PAYMENT
     */
    @Test
    public void testRemoveCheckPaymentForOROTCUIPAYMENT(){

        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("OR-OTCUI-PAYMENT", SpcfCalendar.createInstance(2019, 1, 1));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2019, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "OR-OTCUI-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2019, 1, 1));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2019-04-20"));

        /**
         * Verify ACH Debit is getting populated if Agency id Valid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"OR-OTCUI-PAYMENT","00564690-0");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction gaPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("OR-OTCUI-PAYMENT").find());
        assertEquals(PaymentMethod.ACHCredit, gaPayment.getMoneyMovementPaymentMethod());
        assertEquals(0, gaPayment.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
        /**
         * Verify Tax Payment Status - None is getting populated if Agency id Invalid
         */
        DataLoadServices.updateAgencyTaxpayerId(company,"OR-OTCUI-PAYMENT","123112");
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction gaPayment1 = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("OR-OTCUI-PAYMENT").find());
        assertNull(gaPayment1.getMoneyMovementPaymentMethod());
        assertEquals(1, gaPayment1.getActiveOnHoldReasons().size());
        PayrollServices.rollbackUnitOfWork();
    }

    public String changeAmountFormat(SpcfMoney amount, int dollarsDigits) {
        StringBuffer dollarPattern = new StringBuffer(dollarsDigits);

        for (int i = 0; i < dollarsDigits; i++) {
            dollarPattern.append( "0" );
        }

        DecimalFormat dollarsFormat = new DecimalFormat(dollarPattern.toString());
        String dollarsOutput = dollarsFormat.format(amount.getIntegerPart());
        DecimalFormat centsFormat = new DecimalFormat("00");
        String centsOutput = centsFormat.format(amount.getFractionalPart());

        return dollarsOutput + centsOutput;
    }

    public String getPayorFEIN() {
        String payorFEIN = null;
        String transmitterFEINParam = SourcePayrollParameter.findSourcePayrollParameter
                (SourceSystemCode.QBDT, SourcePayrollParameterCode.TransmitterFEIN).getParameterValue();

        payorFEIN = transmitterFEINParam.substring(0,2) + "-" + transmitterFEINParam.substring(2);
        return payorFEIN;
    }
}

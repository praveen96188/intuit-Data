package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: May 1, 2008
 * Time: 2:38:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClaimOfferCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void addCompanyOfferNullCompany() {
        Offer offer = Offer.findOfferByOfferCode("P57213");
        
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany(offer.getOfferCd(), null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("Error message code", "141", errorMessage.getMessageCode());
        assertEquals("Error message", "Company is not specified.", errorMessage.getMessage());
    }

    @Test
    public void addCompanyOfferNullOfferCd() {
        Company company = addCompany();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany(null, null, company);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 2, result.getMessages().size());
        MessageList messageList = result.getMessages("5002");
        assertEquals("Error messages with code - 5002", 1, messageList.size());
        assertEquals("Error message", "Required 'OfferCd' input is missing or blank", messageList.get(0).getMessage());

        messageList = result.getMessages("5003");
        assertEquals("Error messages with code - 5003", 1, messageList.size());
        assertEquals("Error message", "Offer 'null' does not exist", messageList.get(0).getMessage());

    }

    @Test
    public void addCompanyOfferInvalidOfferCd() {
        Company company = addCompany();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany("ABC", null, company);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("Error message code", "5003", errorMessage.getMessageCode());
        assertEquals("Error message", "Offer 'ABC' does not exist", errorMessage.getMessage());
    }

    @Test
    public void addCompanyOfferBeforeEfectiveDate() {
        Company company = addCompany();
        DataLoadServices.setPSPDate(2008,11,01);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany("Twenty percent off Monthly Fees", null, company);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "5000", errorMessage.getMessageCode());
        assertEquals("Error message", "Offer has not started", errorMessage.getMessage());
    }

    @Test
    public void addCompanyOfferSuccess() {
        Offer offer = Offer.findOfferByOfferCode("P57213");

        Company company = addCompany();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany(offer.getOfferCd(), null, company);
        PayrollServices.commitUnitOfWork();

        assertTrue("Process Result", result.isSuccess());

        CompanyOffer claimedCompanyOffer = result.getResult();

        //Assertion for Source Company Id
        assertEquals("Source Company Id ", company.getSourceCompanyId(),
                claimedCompanyOffer.getCompany().getSourceCompanyId());

        //Assertion for Claimed Offer Code for a Company
        assertEquals("Claimed Offer Code ", offer.getOfferCd(), claimedCompanyOffer.getOffer().getOfferCd());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyOffer> foundCompanyOffer = company.getCompanyOffers();
        PayrollServices.commitUnitOfWork();

        //Assertion for the Company Offers
        Assert.assertTrue(foundCompanyOffer != null);
        Assert.assertTrue(foundCompanyOffer.size() == 1);
        Assert.assertTrue(foundCompanyOffer.get(0).getId().equals(claimedCompanyOffer.getId()));
    }

    @Test
    public void alternatePriceOffer() {
        String offerCd = "P58359";

        Company company = addCompany();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany(offerCd, null, company);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("claimOfferForCompany()", result);

        CompanyOffer claimed = result.getResult();

        assertEquals("claimed offer code", offerCd, claimed.getOffer().getOfferCd());
        assertEquals("claimed offer discount type", DiscountType.AltPrice, claimed.getOffer().getDiscountType());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyOffer> foundCompanyOffer = company.getCompanyOffers();
        PayrollServices.commitUnitOfWork();

        //Assertion for the Company Offers
        Assert.assertTrue(foundCompanyOffer != null);
        Assert.assertTrue(foundCompanyOffer.size() == 1);
        Assert.assertTrue(foundCompanyOffer.get(0).getId().equals(claimed.getId()));
    }

    @Test
    public void testFirstPayrollOffer() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 11, 9, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"AR","AZ","CA"};
        Company company = assertOne(DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding));
        
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyOffer> offerPR = PayrollServices.companyManager.claimOfferForCompany("1099427", null, company);
        PayrollServices.commitUnitOfWork();

        assertSuccess(offerPR);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 11, 13, SpcfTimeZone.getLocalTimeZone()));


        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 11, 9, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2012-11-11");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar expectedBeginDate = SpcfCalendar.createInstance(2012, 11, 13, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar expectedEndDate = SpcfCalendar.createInstance(2012, 12, 13, SpcfTimeZone.getLocalTimeZone());
        company = Application.refresh(company);
        CompanyOffer companyOffer = assertOne(company.getCompanyOffers());
        assertEquals(expectedBeginDate,companyOffer.getBeginDate().toLocal());
        assertEquals(expectedEndDate,companyOffer.getEndDate().toLocal());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 11, 26, SpcfTimeZone.getLocalTimeZone()));

        supportedDate = SpcfCalendar.createInstance(2012, 11, 8, SpcfTimeZone.getLocalTimeZone());
        payrollDate = new DateDTO("2012-11-28");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false);

        //Check that the offer dates haven't changed and check that the payrolls still have $0 fees
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        companyOffer = assertOne(company.getCompanyOffers());
        assertEquals(expectedBeginDate,companyOffer.getBeginDate().toLocal());
        assertEquals(expectedEndDate,companyOffer.getEndDate().toLocal());

        DomainEntitySet<FinancialTransaction> feeTransactions =
                FinancialTransaction.findFinancialTransactions(company,TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        for (FinancialTransaction transaction : feeTransactions) {
            if (transaction.getSku().equals("297370")) {
                assertEquals(SpcfMoney.ZERO, transaction.getFinancialTransactionAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        //Cancel offer and ensure this payroll does get fees
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 2, 5, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        companyOffer = assertOne(company.getCompanyOffers());
        company.cancelOfferForCompany(companyOffer.getOffer());
        PayrollServices.commitUnitOfWork();

        payrollDate = new DateDTO("2013-02-12");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        PayrollRun pr = PayrollRun.findLatestCompanyPayrollRun(company);
        feeTransactions =
                FinancialTransaction.findFinancialTransactions(pr,new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit}, new TransactionStateCode[]{TransactionStateCode.Created});

        for (FinancialTransaction transaction : feeTransactions) {
            if (transaction.getSku().equals("297370")) {
                assertTrue(transaction.getFinancialTransactionAmount().compareTo(SpcfMoney.ZERO)>0);
            }
        }
        PayrollServices.rollbackUnitOfWork();

        //Claim extra state offer and ensure this payroll does get both extra state fees
        PayrollServices.beginUnitOfWork();
        offerPR = PayrollServices.companyManager.claimOfferForCompany("Ten percent off Monthly Fees", null, company);
        PayrollServices.commitUnitOfWork();

        assertSuccess(offerPR);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 3, 5, 10, 10, 10, 10, SpcfTimeZone.getLocalTimeZone()));

        payrollDate = new DateDTO("2013-03-12");

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, false);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        pr = PayrollRun.findLatestCompanyPayrollRun(company);
        feeTransactions =
                FinancialTransaction.findFinancialTransactions(pr,new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit}, new TransactionStateCode[]{TransactionStateCode.Created});

        for (FinancialTransaction transaction : feeTransactions) {
            if (transaction.getSku().equals("297370")) {
                assertEquals(new SpcfMoney("71.1"), transaction.getFinancialTransactionAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void offerP60241() {    
        // create a QBDT company -- only they will have one of the Offerings that the offer applies to
        Company company = addCompany(SourceSystemCode.QBDT);

        // claim the offer
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        company = Application.refresh(company);
        Offer offer = Offer.findOfferByOfferCode("P60241");
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany(offer.getOfferCd(), null, company);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("claimOfferForCompany", result);



        // make sure the offer is applicable to a certain charge as of today
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        OfferingServiceCharge charge = Offering.findOffering(company, ServiceCode.DirectDeposit).getCharge(OfferingServiceChargeType.DirectDepositFee, 1);
        Offer applicableOffer = company.getApplicableOffer(charge);
        PayrollServices.commitUnitOfWork();
        assertTrue("first day, found an applicable offer", applicableOffer != null);
        assertEquals("first day, applicable offer code", "P60241", applicableOffer.getOfferCd());

        // advance the date to the last day and make sure it's still applicable
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime( offer.getDurationDays()-1 );
        company = Application.refresh(company);
        charge = Application.refresh(charge);
        applicableOffer = company.getApplicableOffer(charge);
        PayrollServices.commitUnitOfWork();
        assertTrue("last day, found an applicable offer", applicableOffer != null);
        assertEquals("last day, applicable offer code", "P60241", applicableOffer.getOfferCd());

        // advance the date one more day and make sure it's NOT applicable any longer
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime( 1 );
        company = Application.refresh(company);
        charge = Application.refresh(charge);
        applicableOffer = company.getApplicableOffer(charge);
        PayrollServices.commitUnitOfWork();
        assertTrue("too late, no applicable offer", applicableOffer == null);
    }

    @Test
    public void bug1016() {
        /**
         * PSRV0001016: Offers can be claimed after they have expired
         * The EndDate for duration- or usage-based offers is not checked when a company claims the offer.  Consequently,
         * you could never stop offering a duration- or usage-limited discount.
         */

        // create a QBDT company -- only they will have one of the Offerings that the offer applies to
        Company company = addCompany(SourceSystemCode.QBDT);

        // try to claim it the day after it expires
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        Offer offer = Offer.findOfferByOfferCode("P60708");
        PSPDate.setPSPTime(offer.getEndDate());
        PSPDate.addDaysToPSPTime(1); // day after last day
        ProcessResult<CompanyOffer> result = PayrollServices.companyManager.claimOfferForCompany(offer.getOfferCd(),null,  company);
        PayrollServices.commitUnitOfWork();
        assertTrue("claim after last day fails", !result.isSuccess());
        assertEquals("expired offer error count", 1, result.getMessages().size());
        assertEquals("expired offer error code", "5000", result.getMessages().get(0).getMessageCode());
        assertTrue("expired offer error message is correct", result.getMessages().get(0).getMessage().indexOf("Offer has expired") >= 0);

        // try it again on the last day it's available
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        offer = Application.refresh(offer);
        PSPDate.setPSPTime(offer.getEndDate()); // last day
        result = PayrollServices.companyManager.claimOfferForCompany(offer.getOfferCd(), null, company);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("claim on the last day", result);
    }

    private Company addCompany(){
        return addCompany(SourceSystemCode.QBOE);
    }

    private Company addCompany(SourceSystemCode pSrcSystemCd){
        DataLoader loader = new DataLoader();
        loader.setSrcSystemCodeForNewCompany(pSrcSystemCd);

        PayrollServices.beginUnitOfWork();
        CompanyDTO dtoCompany = loader.getTestIntuitCompany();
        ProcessResult<Company> resultCompany = DataLoader.addCompany(dtoCompany);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(resultCompany.getResult());

        Assert.assertTrue(resultCompany.isSuccess());
        Assert.assertTrue(resultCompany.getMessages().size() == 0);



        return resultCompany.getResult();
    }

}

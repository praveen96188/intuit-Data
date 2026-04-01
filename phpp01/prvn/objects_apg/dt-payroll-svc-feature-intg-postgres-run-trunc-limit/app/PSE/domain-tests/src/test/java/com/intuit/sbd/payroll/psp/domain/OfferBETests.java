package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Apr 4, 2008
 * Time: 10:50:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class OfferBETests {
    static private final String OFFER_CODE_A = "P60642";
    static private final String OFFER_CODE_B = "P57553";

    static private Offer offerA;
    static private Offer offerB;
    static private Offer offerC;

    static private final String CHARGE_FOR_A = "70995ef3-0002-9373-e040-11ac3bda020f";
    static private final String CHARGE_FOR_B = "70995ef3-0001-9373-e040-11ac3bda020f";

    static private OfferingServiceCharge chargeP;
    static private OfferingServiceCharge chargeQ;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();

        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.resetPSPTime();
        PSPDate.addDaysToPSPTime(-7);
        PayrollServices.commitUnitOfWork();

        offerA = Offer.findOfferByOfferCode(OFFER_CODE_A);
        offerB = Offer.findOfferByOfferCode(OFFER_CODE_B);

        chargeP = Application.findById(OfferingServiceCharge.class, SpcfUniqueId.createInstance(CHARGE_FOR_A));
        chargeQ = Application.findById(OfferingServiceCharge.class, SpcfUniqueId.createInstance(CHARGE_FOR_B));

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void getOfferByCode_Found() {
        PayrollServices.beginUnitOfWork();
        Offer found = Offer.findOfferByOfferCode(OFFER_CODE_B);
        PayrollServices.commitUnitOfWork();

        Assert.assertNotNull(found);
        Assert.assertTrue(found.getOfferCd().equals(OFFER_CODE_B));
    }

    @Test
    public void getOfferByCode_NotFound() {
        PayrollServices.beginUnitOfWork();
        Offer found = Offer.findOfferByOfferCode("NO_SUCH_OFFER_CODE");
        PayrollServices.commitUnitOfWork();

        Assert.assertNull(found);
    }

    @Test
    public void discounts() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20081003010101");
        SpcfDecimal grossA = SpcfDecimal.createInstance(5.55);
        SpcfMoney expectedDiscountA = new SpcfMoney(SpcfDecimal.createInstance(0.00));
        SpcfDecimal grossB = SpcfDecimal.createInstance(4.44);
        SpcfDecimal expectedDiscountB = SpcfDecimal.createInstance(2.22);

        SpcfMoney discountA = new SpcfMoney(offerA.getDiscount(grossA));
        assertEquals("Discount amount",expectedDiscountA.setScale(2), discountA.setScale(2));

        SpcfDecimal discountB = offerB.getDiscount(grossB); 
        assertEquals("Discount amount",expectedDiscountB.setScale(2), discountB.setScale(2));

    }

    @Test
    public void claimOffer() {
        DataLoader loader = new DataLoader();
        loader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        // create two companies -- one that will have and offer and one that won't
        PayrollServices.beginUnitOfWork();
        Company companyX = loader.persistTestActiveCompany();
        DDServiceInfoDTO ddCompanyService = new DDServiceInfoDTO();

        ddCompanyService.setAveragePayrollAmount(new BigDecimal("150.00"));

        ddCompanyService.setHighAnnualPayrollAmount(new BigDecimal("250.00"));
        loader.persistCompanyService(companyX,ddCompanyService);

        Company companyY = loader.persistTestActiveCompany123123123();
        loader.persistCompanyService(companyY,ddCompanyService);
        PayrollServices.commitUnitOfWork();

        // claim an offer for it
        CompanyOffer claimed = null;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20081003010101");
        try {
            claimed = companyX.claimOfferForCompany(offerA);
        } catch (Exception x) {
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("Unable to claimOfferForCompany(): "+x.getMessage());
            return;
        }
        PayrollServices.commitUnitOfWork();

        Assert.assertTrue(claimed != null);
        Assert.assertTrue(claimed.companyOfferIsActive());

        // try to claim it a second time
        CompanyOffer claimedAgain = null;
        PayrollServices.beginUnitOfWork();
        try {
            claimedAgain = companyX.claimOfferForCompany(offerA);
        } catch (Exception x) {
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("Unable to claimOfferForCompany(): "+x.getMessage());
            return;
        }
        PayrollServices.commitUnitOfWork();

        Assert.assertTrue(claimedAgain != null);
        Assert.assertTrue(claimedAgain.getId().equals(claimed.getId())); // same CompanyOffer created by first claim

        // make sure it's applicable where it should be, and not where it should not be
        PayrollServices.beginUnitOfWork();
        companyX = Company.findCompany("123272727", SourceSystemCode.QBDT);
        companyY = Company.findCompany("123123123", SourceSystemCode.QBDT);
        chargeP = Application.findById(OfferingServiceCharge.class, SpcfUniqueId.createInstance(CHARGE_FOR_A));
        chargeQ = Application.findById(OfferingServiceCharge.class, SpcfUniqueId.createInstance(CHARGE_FOR_B));
        Offer offerXP = companyX.getApplicableOffer(chargeP); // should return offerA
        Offer offerXQ = companyX.getApplicableOffer(chargeQ); // company has offer, but offer not applicable to chargeQ
        Offer offerYP = companyY.getApplicableOffer(chargeP); // company doesn't have any offer that applies to chargeP
        PayrollServices.commitUnitOfWork();

        Assert.assertTrue(offerA.equals(offerXP));
        Assert.assertTrue(offerYP == null);
        Assert.assertTrue(offerXQ == null);
    }
}

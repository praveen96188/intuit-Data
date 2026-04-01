package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jul 1, 2008
 * Time: 11:37:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class PricingFinderTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void findAvailableOfferings() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Offering> found = Offering.findAvailableOfferings();
        PayrollServices.commitUnitOfWork();

        // ignore Offerings created by other tests
        int nRealOfferings = 0;
        String lastSKU = "";
        for (Offering offering : found) {
            String sku = offering.getSKU();

            // make sure they're sorted by sku, ascending
            Assert.assertTrue(sku.compareTo(lastSKU) > 0);

            // make sure that the three "real" skus are includied, in addition to any others created by other tests
            if (sku.equals("QBOE DD") || sku.equals("DIYDD-STD") || sku.equals("DIYDDSTD-3")) {
                ++nRealOfferings;
            }
        }
        Assert.assertEquals("found right number of real offerings", 3, nRealOfferings);
    }

    @Test
    public void findPayrollCharges() {
        PayrollServices.beginUnitOfWork();
        Offering offering = Offering.findBySKU("DIYDD-STD");
        List<OfferingServiceCharge> payrollCharges = offering.getPayrollCharges();
        PayrollServices.commitUnitOfWork();

        for (Iterator<OfferingServiceCharge> iter = payrollCharges.iterator(); iter.hasNext();) {
            OfferingServiceCharge charge = iter.next();
            if (charge.getOfferingServiceChargeGroup().getName().equals("W2 Fee")
                    || charge.getOfferingServiceChargeGroup().getName().equals("W2 Base Fee")) {
                iter.remove();
            }
        }

        // make sure the count is right
        Assert.assertEquals("number of payroll charges", 2 , payrollCharges.size());

        // make sure they're the right ones
        for (OfferingServiceCharge charge : payrollCharges) {
            OfferingServiceChargeType chargeType = charge.getOfferingServiceChargeGroup().getAppliesTo();
            Assert.assertTrue("found charge is a payroll charge type",
                              OfferingServiceChargeGroup.isPayrollChargeType(chargeType));
        }
    }

    @Test
    public void findApplicableOffers() {
        PayrollServices.beginUnitOfWork();
        Offering offering = Offering.findBySKU("DIYDDSTD-3");

        SpcfCalendar tomorrow = PSPDate.getPSPTime();
        tomorrow.addDays(1);
        CalendarUtils.clearTime(tomorrow);

        DomainEntitySet<Offer> applicableOffers = offering.getApplicableOffers();
        int nFound = applicableOffers.size();

        PayrollServices.commitUnitOfWork();

        // make sure the count is right
        Assert.assertEquals("number of Offers", 18, nFound);
    }

    @Test
    public void foo() {
        PayrollServices.beginUnitOfWork();
        Offering offering = Offering.findBySKU("DIYDDSTD-3");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();

        SpcfCalendar tomorrow = PSPDate.getPSPTime();
        tomorrow.addDays(1);
        CalendarUtils.clearTime(tomorrow);

        String hql = "Select offer from com.intuit.sbd.payroll.psp.domain.Offer offer left outer join offer.OfferingServiceChargeSet cl where cl in " +
                     "(Select charge from com.intuit.sbd.payroll.psp.domain.OfferingServiceCharge charge " +
                     "where charge.OfferingServiceChargeGroup.Offering = :offering)";
        // that hql works

        hql = "Select offer from com.intuit.sbd.payroll.psp.domain.Offer offer join offer.OfferingServiceChargeSet as charge where charge.OfferingServiceChargeGroup.Offering = :offering and offer.EffectiveDate < :tomorrow";

        Query q = Application.createHibernateQuery(hql);
        q.setParameter("offering", offering);
        q.setParameter("tomorrow", tomorrow);
        List<Offer> found = q.list();

        System.out.println("found "+found.size()+" offers:");
        for (Offer offer : found) {
            System.out.println("\t"+offer.getOfferCd());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void bar() {
        PayrollServices.beginUnitOfWork();
        Offering offering = Offering.findBySKU("DIYDDSTD-3");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar tomorrow = PSPDate.getPSPTime();
        tomorrow.addDays(1);
        CalendarUtils.clearTime(tomorrow);

        String[] paramNames = new String[2];
        Object[] paramValues = new Object[2];

        paramNames[0] = "offering";
        paramValues[0] = offering;

        paramNames[1] = "tomorrow";
        paramValues[1] = tomorrow;

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Offer> foundDiscountOffers = Application.findByNamedQuery("findApplicableDiscountOffers", paramNames, paramValues);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Application Discount offers", 18, foundDiscountOffers.size());

        System.out.println("found "+foundDiscountOffers.size()+" applicable Discount offers:");
        for (Offer offer : foundDiscountOffers) {
            System.out.println("\t"+offer.getOfferCd());
        }
    }

}

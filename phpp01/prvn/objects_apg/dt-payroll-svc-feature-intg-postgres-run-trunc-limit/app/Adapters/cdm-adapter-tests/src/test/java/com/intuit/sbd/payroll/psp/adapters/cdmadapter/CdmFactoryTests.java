package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.factories.CdmFactory;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.schema.ems.v3.PayrollEmployee;
import com.intuit.schema.ems.v3.RateType;
import com.intuit.schema.ems.v3.TaxPaystubLineItem;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class CdmFactoryTests {
    private static final String psid = "99000123";

    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHourSumming() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        //Need to eager load additional fields
        paystub = PaystubFinder.findPaystubById(paystub.getId());
        com.intuit.schema.ems.v3.Paystub cdmPaystub = CdmFactory.createPaystub(paystub);
        //Expected value based on TestHelper data
        Assert.assertEquals("60.00", cdmPaystub.getTotalHours());
        Application.rollbackUnitOfWork();
    }

    //Negative numbers should stay negative, unlike they currently do in the IOP implementation
    @Test
    public void testNegativeNumbers() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Paystub paystub = VmpTestUtil.createPaystub(employee, "1000.00", SpcfCalendar.createInstance(2013, 1, 1));
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        //Need to eager load additional fields
        paystub = PaystubFinder.findPaystubById(paystub.getId());
        com.intuit.schema.ems.v3.Paystub cdmPaystub = CdmFactory.createPaystub(paystub);
        List<TaxPaystubLineItem> taxItems = cdmPaystub.getTaxItem();
        //Expected values based on TestHelper data, but should no longer be negative
        Assert.assertEquals(3, taxItems.size());
        for(TaxPaystubLineItem taxLineItem : taxItems) {
            Assert.assertEquals(SpcfUtils.convertToBigDecimal(SpcfDecimal.createInstance("-100.00")), taxLineItem.getCurrent());
            Assert.assertEquals(SpcfUtils.convertToBigDecimal(SpcfDecimal.createInstance("-325.00")), taxLineItem.getYTD());
        }

        Assert.assertEquals(SpcfUtils.convertToBigDecimal(SpcfDecimal.createInstance("-200.00")), cdmPaystub.getTaxes());
        Assert.assertEquals(SpcfUtils.convertToBigDecimal(SpcfDecimal.createInstance("-650.00")), cdmPaystub.getYTDTaxes());
        Application.rollbackUnitOfWork();
    }

    //Pto policy should be returned unless ytd used and available are both blank
    @Test
    public void testNoPtoPolicy() {
        PstubPaidTimeoffItem paidTimeoffItem = new PstubPaidTimeoffItem();
        paidTimeoffItem.setAvailable("0:00");
        paidTimeoffItem.setYTDUsed("0:00");
        Assert.assertNull(CdmFactory.createPaidTimeOffItem(paidTimeoffItem));

        paidTimeoffItem.setAvailable("-1.00");
        paidTimeoffItem.setYTDUsed("0.00");
        Assert.assertNotNull(CdmFactory.createPaidTimeOffItem(paidTimeoffItem));

        paidTimeoffItem.setAvailable("0:00");
        paidTimeoffItem.setYTDUsed("-22:35");
        Assert.assertNotNull(CdmFactory.createPaidTimeOffItem(paidTimeoffItem));
    }

    @Test
    public void testCreatePaystubEarningItem() {
        PstubPayItem payItem = new PstubPayItem();
        payItem.setQtyAmt("3.10");
        payItem.setRate("2.0");
        com.intuit.schema.ems.v3.Paystub.EarningItem earningItem = CdmFactory.createPaystubEarningItem(payItem);
        Assert.assertEquals("3.10", earningItem.getQty());
        Assert.assertEquals(new BigDecimal("2.00"), earningItem.getRate());
        Assert.assertEquals(RateType.CURRENCY, earningItem.getRateType());

        payItem = new PstubPayItem();
        payItem.setQtyTime("30:20");
        payItem.setRate("10.0%");
        earningItem = CdmFactory.createPaystubEarningItem(payItem);
        Assert.assertEquals("30:20", earningItem.getQty());
        Assert.assertEquals(new BigDecimal("10.00"), earningItem.getRate());
        Assert.assertEquals(RateType.PERCENTAGE, earningItem.getRateType());

        //Test with null value
        payItem = new PstubPayItem();
        payItem.setQtyAmt("3.10");
        earningItem = CdmFactory.createPaystubEarningItem(payItem);
        Assert.assertEquals(null, earningItem.getRateType());
    }

    @Test
    public void testCreatePayrollEmployeeCompanyValues() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        PayrollEmployee payrollEmployee = CdmFactory.createPayrollEmployee(employee);
        Assert.assertEquals(employee.getCompany().getDbaName(), payrollEmployee.getCompanyName());
        Assert.assertEquals(employee.getCompany().getFedTaxId(), payrollEmployee.getCompanyTaxId());
    }

    @Test
    public void testCreatePayrollEmployeeNullFields() {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        Application.beginUnitOfWork();
        Application.refresh(employee);
        Company company = employee.getCompany();
        company.setFedTaxId(null);
        company.setDbaName(null);
        employee.setTaxId(null);
        PayrollEmployee payrollEmployee = CdmFactory.createPayrollEmployee(employee);
        Assert.assertNull(payrollEmployee.getCompanyName());
        Assert.assertNull(payrollEmployee.getCompanyTaxId());
        Assert.assertNull(payrollEmployee.getSSN());
        Assert.assertNull(payrollEmployee.getUnmaskedSSN());
        Assert.assertFalse(payrollEmployee.isViewingPaystubDisabled());
        Application.commitUnitOfWork();
    }
}

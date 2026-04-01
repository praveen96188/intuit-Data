package com.intuit.sbd.payroll.psp.api;

import com.intuit.sbd.payroll.psp.FakeSalesTaxGateway;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;


/**
 * User: dweinberg
 * Date: Sep 2, 2010
 * Time: 12:59:43 PM
 */
public class ServiceChargePrices {

    public static SpcfMoney getNormalPerPayrollServiceCharge() {
        return getNormalPerPayrollServiceCharge(1);
    }

    public static SpcfMoney getNormalPerPayrollServiceCharge(int number) {
        return new SpcfMoney(new SpcfMoney("1.45").multiply(SpcfDecimal.createInstance(number)));
    }

    public static SpcfMoney getNormalPerPayrollServiceChargeWithSalesTax() {
        return getNormalPerPayrollServiceChargeWithSalesTax(1);
    }

    public static SpcfMoney getNormalPerPayrollServiceChargeWithSalesTax(int number) {
        return new SpcfMoney(new SpcfMoney("1.45").multiply(SpcfDecimal.createInstance(number)).add(FakeSalesTaxGateway.getFakeTaxAmount()));
    }


    public static SpcfMoney getNormalPerPayrollServiceChargeFY16() {
        return getNormalPerPayrollServiceChargeFY16(1);
    }
    public static SpcfMoney getNormalPerPayrollServiceChargeFY16(int number) {
        return new SpcfMoney(new SpcfMoney("1.75").multiply(SpcfDecimal.createInstance(number)));
    }
    public static SpcfMoney getNormalPerPayrollServiceChargeWithSalesTaxFY16() {
        return getNormalPerPayrollServiceChargeWithSalesTaxFY16(1);
    }

    public static SpcfMoney getNormalPerPayrollServiceChargeWithSalesTaxFY16(int number) {
        return new SpcfMoney(new SpcfMoney("1.75").multiply(SpcfDecimal.createInstance(number)).add(FakeSalesTaxGateway.getFakeTaxAmount()));
    }


}

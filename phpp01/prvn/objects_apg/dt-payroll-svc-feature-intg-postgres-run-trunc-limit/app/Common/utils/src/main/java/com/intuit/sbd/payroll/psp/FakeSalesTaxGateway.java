package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.gateways.salestax.ISalesTaxGateway;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;

/**
 * User: dweinberg
 * Date: Dec 23, 2009
 * Time: 11:40:37 AM
 * Set this class as the instanceClass in the SalesTaxGatewayFactory for non-integration tests
 * It will always return success with a tax of $0.08 as long as there is a non-zero request
 *
 * This class should only be used for testing code not related to the sales tax gateway as
 * it does not allow testing all scenarios.
 */
public class FakeSalesTaxGateway implements ISalesTaxGateway {

    public static double taxAmount = 0.08;

    public static SpcfMoney getFakeTaxAmount() {
        return new SpcfMoney(SpcfDecimal.createInstance(taxAmount));
    }

    public SalesTaxResponse send(SalesTaxRequest pSalesTaxRequest) {
        if (! nonZeroRequest(pSalesTaxRequest)) {
            return null;
        }

        SalesTaxResponse response = new SalesTaxResponse();
        response.setSuccess(true);
        response.setTotalTaxAmount(BigDecimal.valueOf(taxAmount));

        for (SalesTaxRequestLine salesTaxRequestLine : pSalesTaxRequest.getSalesTaxRequestLineList()) {
            SalesTaxResponseLine salesTaxResponseLine = new SalesTaxResponseLine();
            salesTaxResponseLine.setTaxAmount(BigDecimal.valueOf(taxAmount));
            salesTaxResponseLine.setSKU(salesTaxRequestLine.getSKU());
            response.addLine(salesTaxResponseLine);
        }

        return response;
    }

    private Boolean nonZeroRequest(SalesTaxRequest pSalesTaxRequest) {
        for (SalesTaxRequestLine taxRequestLine : pSalesTaxRequest.getSalesTaxRequestLineList()) {
            if (taxRequestLine.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                return true;
            }
        }
        return false;
    }

}

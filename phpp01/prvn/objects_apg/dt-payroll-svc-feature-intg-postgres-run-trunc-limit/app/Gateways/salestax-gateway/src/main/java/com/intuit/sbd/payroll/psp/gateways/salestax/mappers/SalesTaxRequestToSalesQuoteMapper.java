package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.SalesQuote;
import com.intuit.gst.TotalDiscount;
import com.intuit.gst.TotalFreight;
import com.intuit.gst.TotalTransaction;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SalesTaxRequestToSalesQuoteMapper extends BeanMapper<SalesTaxRequest, SalesQuote> {

    private final String USD= "USD";

    @Override
    public SalesQuote mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<SalesQuote> salesQuoteClass) {
        SalesQuote salesQuote = new SalesQuote();

        //todo - check if we can safely set it to 0 for all the values
        TotalFreight totalFreight = new TotalFreight().withCurrency(USD).withValue((float) 0);

        //todo - We should be able to set this to 0,
        // since while setting amount in psp request line we subtract discount beforehand
        TotalDiscount totalDiscount = new TotalDiscount().withCurrency(USD).withValue((float) 0);

        //todo - set this based on calculations from SalesTaxRequest lines
        TotalTransaction totalTransaction = getTotalTransactionFromSalesTaxRequestLines(pspSalesTaxRequest.getSalesTaxRequestLineList());

        salesQuote.setTotalFreight(totalFreight);
        salesQuote.setTotalDiscount(totalDiscount);
        salesQuote.setTotalTransaction(totalTransaction);

        return salesQuote;
    }

    private TotalTransaction getTotalTransactionFromSalesTaxRequestLines(List<SalesTaxRequestLine> pspSalesTaxRequestLines) {
        BigDecimal totalTransactionValue= new BigDecimal("0");
        for(SalesTaxRequestLine salesTaxRequestLine: pspSalesTaxRequestLines) {
           totalTransactionValue = totalTransactionValue.add(salesTaxRequestLine.getAmount());
        }
        TotalTransaction totalTransaction = new TotalTransaction().withCurrency(USD).withValue(totalTransactionValue.floatValue());
        return totalTransaction;

    }
}

package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class SalesTaxGSTAndEOSComparisonImpl implements ISalesTaxGateway{


    private SalesTaxGatewayImpl salesTaxEos;
    private SalesTaxResponseComparisonService salesTaxResponseComparisonService;


    @Autowired
    public SalesTaxGSTAndEOSComparisonImpl(SalesTaxResponseComparisonService salesTaxResponseComparisonService) {
        this.salesTaxEos = new SalesTaxGatewayImpl();
        this.salesTaxResponseComparisonService = salesTaxResponseComparisonService;
    }

    @Override
    public SalesTaxResponse send(SalesTaxRequest salesTaxRequest) {
        String docId = Objects.nonNull(salesTaxRequest) ? salesTaxRequest.getDocumentId() : null;
        log.info("action=SalesTaxComparisonEOSRequestStart docId={}",docId);
        SalesTaxResponse salesTaxResponseEos = salesTaxEos.send(salesTaxRequest);
        log.info("action=SalesTaxComparisonEOSRequestComplete docId={}",docId);
        //make this call in parallel,
        // we might need to have a separate thread pool to do this if there are too many requests and we don't want new threads everytine
        salesTaxResponseComparisonService.getResponseFromGSTAndShallowCompareWithEOSResponseProvided(salesTaxRequest,salesTaxResponseEos);
        return salesTaxResponseEos;
    }

}

package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.GSTRequest;
import com.intuit.gst.SalesTaxHeader;
import com.intuit.gst.SalesTaxLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class SalesTaxRequestToGSTRequestMapper extends BeanMapper<SalesTaxRequest, GSTRequest> {

    @Override
    public GSTRequest mapToTarget(SalesTaxRequest salesTaxRequest, Class<GSTRequest> gstRequestClass) {

        GSTRequest gstRequest = new GSTRequest();

        //todo - check this mapper (2)
        SalesTaxHeader gstRequestSalesTaxHeader = getMapperRegistry().mapToTarget(salesTaxRequest, SalesTaxHeader.class);

        List<SalesTaxLine> gstRequestSalesTaxLines = new ArrayList<>();
        long i=0;
        for(SalesTaxRequestLine salesTaxRequestLine: salesTaxRequest.getSalesTaxRequestLineList()) {
            //todo - check this mapper (2)
            SalesTaxLine gstRequestSalesTaxLine = getMapperRegistry().mapToTarget(salesTaxRequestLine, SalesTaxLine.class);
            gstRequestSalesTaxLine.setLineNumber(i++);
            gstRequestSalesTaxLines.add(gstRequestSalesTaxLine);
        }

        gstRequest.setSalesTaxHeader(gstRequestSalesTaxHeader);
        gstRequest.setSalesTaxLine(gstRequestSalesTaxLines);

        return gstRequest;
    }
}

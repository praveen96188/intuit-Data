package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component
public class SalesTaxRequestToSalesTaxHeaderMapper extends BeanMapper<SalesTaxRequest, SalesTaxHeader> {
    @Override
    public SalesTaxHeader mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<SalesTaxHeader> salesTaxHeaderClass) {
        checkForNullObjects(pspSalesTaxRequest);
        SalesTaxHeader salesTaxHeader = new SalesTaxHeader();

        //todo - check this mapper (2)
        Parties parties  = getMapperRegistry().mapToTarget(pspSalesTaxRequest, Parties.class);

        //todo - check this mapper (2)
        SalesContext salesContext = getMapperRegistry().mapToTarget(pspSalesTaxRequest, SalesContext.class);

        //todo - check this mapper (2)
        SalesQuote salesQuote =  getMapperRegistry().mapToTarget(pspSalesTaxRequest, SalesQuote.class);

        //fill the object
        salesTaxHeader.setDocumentId(pspSalesTaxRequest.getDocumentId());
        //todo- check if this is OK
        salesTaxHeader.setDocumentDateTime(pspSalesTaxRequest.getDocumentDateTime().getTimeInMillis());
        salesTaxHeader.setParties(parties);
        //todo - check if we need to set Freight, wasn't present in the example request shared
        //salesTaxHeader.setFreight(freight);
        salesTaxHeader.setSalesContext(salesContext);
        salesTaxHeader.setSalesQuote(salesQuote);

        return salesTaxHeader;
    }

    private void checkForNullObjects(SalesTaxRequest pspSalesTaxRequest) {
        String exceptionMessage = "";
        if(Objects.isNull(pspSalesTaxRequest)) {
            exceptionMessage = "pspSalesTaxRequest is null";
        } else if(Objects.isNull(pspSalesTaxRequest.getDocumentDateTime())) {
            exceptionMessage = "pspSalesTaxRequest.getDocumentDateTime() is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage =
                    "Can not map SalesTaxRequest inside PSP to SalesTaxHeader" + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }
    }
}

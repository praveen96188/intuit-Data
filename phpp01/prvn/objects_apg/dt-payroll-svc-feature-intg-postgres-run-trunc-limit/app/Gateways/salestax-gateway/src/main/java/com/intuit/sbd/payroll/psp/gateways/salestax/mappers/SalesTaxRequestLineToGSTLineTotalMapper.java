package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.LineTotal;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SalesTaxRequestLineToGSTLineTotalMapper extends BeanMapper<SalesTaxRequestLine, LineTotal> {

    private final String USD = "USD";

    @Override
    public LineTotal mapToTarget(SalesTaxRequestLine pspSalesTaxRequestLine, Class<LineTotal> lineTotalClass) {
        checkForNullObjects(pspSalesTaxRequestLine);
        LineTotal lineTotal = new LineTotal();
        lineTotal.setValue(pspSalesTaxRequestLine.getAmount().floatValue());
        //todo - setting this as earlier we have directly defaulted to USD, check this once
        lineTotal.setCurrency(USD);
        return lineTotal;
    }

    private void checkForNullObjects(SalesTaxRequestLine pspSalesTaxRequestLine) {
        String exceptionMessage = "";
        if(Objects.isNull(pspSalesTaxRequestLine)) {
            exceptionMessage = "SalesTaxRequestLine is null";
        } else if(Objects.isNull(pspSalesTaxRequestLine.getAmount())) {
            exceptionMessage = "pspSalesTaxRequestLine.getAmount() is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage = "Can not map SalesTaxRequestLine to LineTotal, exMessage=" + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }
    }
}

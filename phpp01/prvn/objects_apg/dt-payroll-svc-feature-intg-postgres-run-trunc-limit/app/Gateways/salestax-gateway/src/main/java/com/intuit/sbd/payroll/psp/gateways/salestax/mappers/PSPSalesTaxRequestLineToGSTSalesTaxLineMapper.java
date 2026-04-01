package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PSPSalesTaxRequestLineToGSTSalesTaxLineMapper extends BeanMapper<SalesTaxRequestLine, SalesTaxLine> {


    private final String USD = "USD";

    @Override
    public SalesTaxLine mapToTarget(SalesTaxRequestLine pspSalesTaxRequestLine, Class<SalesTaxLine> salesTaxLineClass) {
        checkForNullObjects(pspSalesTaxRequestLine);
        SalesTaxLine gstRequestSalesTaxLine = new SalesTaxLine();

        //todo - check this mapper (2)
        LineTotal gstLineTotal = getMapperRegistry().mapToTarget(pspSalesTaxRequestLine, LineTotal.class);

        //todo - check this mapper (2)
        OrderItem gstOrderItem = getMapperRegistry().mapToTarget(pspSalesTaxRequestLine,OrderItem.class);

        //todo - verify that this is correct
        OrderQuantity gstOrderQuantity = new OrderQuantity();
        gstOrderQuantity.setValue((long) pspSalesTaxRequestLine.getQuantity());
        gstOrderQuantity.setUom("Each");

        //todo - check the implementation
        UnitPrice gstUnitPrice = getUnitPriceForPSPSalesTaxLine(pspSalesTaxRequestLine);

        gstRequestSalesTaxLine.setActionType("ADD");
        //line number will be set 1 level up
        gstRequestSalesTaxLine.setLineTotal(gstLineTotal);
        gstRequestSalesTaxLine.setOrderItem(gstOrderItem);
        gstRequestSalesTaxLine.setOrderQuantity(gstOrderQuantity);
        gstRequestSalesTaxLine.setUnitPrice(gstUnitPrice);

        return gstRequestSalesTaxLine;
    }


    private UnitPrice getUnitPriceForPSPSalesTaxLine(SalesTaxRequestLine pspSalesTaxRequestLine) {
        UnitPrice unitPrice = new UnitPrice();

        Amount amount = new Amount();
        amount.setValue(pspSalesTaxRequestLine.getAmount().doubleValue());
        amount.setCurrency(USD);

        PerQuantity perQuantity = new PerQuantity();
        perQuantity.setUom("Each");
        perQuantity.setValue(1L);

        unitPrice.setAmount(amount);
        unitPrice.setPerQuantity(perQuantity);

        return unitPrice;
    }

    private void checkForNullObjects(SalesTaxRequestLine pspSalesTaxRequestLine) {
        String exceptionMessage = "";
        if(Objects.isNull(pspSalesTaxRequestLine)) {
            exceptionMessage = "SalesTaxRequestLine is null";
        } else if(Objects.isNull(pspSalesTaxRequestLine.getQuantity())) {
            exceptionMessage = "pspSalesTaxRequestLine.getQuantity is null";
        } else if(Objects.isNull(pspSalesTaxRequestLine.getAmount())) {
            exceptionMessage = "pspSalesTaxRequestLine.getAmount() is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage =
                    "Can not map SalesTaxRequestLine to SalesTaxLine (for GSTRequest)" + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }
    }
}

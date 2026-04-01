package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.SalesTaxLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class GSTSalesTaxLineToPSPSalesTaxResponseLineMapper extends BeanMapper<SalesTaxLine, SalesTaxResponseLine> {
    @Override
    public SalesTaxResponseLine mapToTarget(SalesTaxLine gstSalesTaxLine, Class<SalesTaxResponseLine> salesTaxResponseLineClass) {
        checkForNullObjects(gstSalesTaxLine);
        SalesTaxResponseLine salesTaxResponseLine = new SalesTaxResponseLine();
        salesTaxResponseLine.setSKU(getItemSKUFromGSTSalesTaxLine(gstSalesTaxLine));
        salesTaxResponseLine.setTaxAmount(getTotalTaxAmountFromGSTSalesTaxLine(gstSalesTaxLine));
        salesTaxResponseLine.setTaxRate(getTaxRateFromGSTSalesTaxLine(gstSalesTaxLine));

        return salesTaxResponseLine;
    }

    private String getItemSKUFromGSTSalesTaxLine(SalesTaxLine gstSalesTaxLine) {
        return gstSalesTaxLine.getOrderItem().getItemIds().getItemId().getId().getValue();
    }

    private BigDecimal getTotalTaxAmountFromGSTSalesTaxLine(SalesTaxLine gstSalesTaxLine) {
        return new BigDecimal(Float.toString(gstSalesTaxLine.getTax().getExtendedTaxAmount().getValue()));
    }

    private BigDecimal getTaxRateFromGSTSalesTaxLine(SalesTaxLine gstSalesTaxLine) {
        return gstSalesTaxLine.getTax().getTaxRate().getValue();
    }

    private void checkForNullObjects(SalesTaxLine gstSalesTaxLine) {
        String exceptionMessage = "";
        if(Objects.isNull(gstSalesTaxLine)) {
            exceptionMessage = "SalesTaxLine is null";
        } else if(Objects.isNull(gstSalesTaxLine.getTax())) {
            exceptionMessage = "gstSalesTaxLine.getTax() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getTax().getExtendedTaxAmount())) {
            exceptionMessage = "gstSalesTaxLine.getTax().getExtendedAmount() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getTax().getExtendedTaxAmount().getValue())) {
            exceptionMessage = "gstSalesTaxLine.getTax().getExtendedAmount().getValue() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getTax().getTaxRate())) {
            exceptionMessage = "gstSalesTaxLine.getTax().getTaxRate() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getTax().getTaxRate().getValue())) {
            exceptionMessage = "gstSalesTaxLine.getTax().getTaxRate().getValue() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getOrderItem())) {
            exceptionMessage = "gstSalesTaxLine.getOrderItem() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getOrderItem().getItemIds())) {
            exceptionMessage = "gstSalesTaxLine.getOrderItem().getItemIds() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getOrderItem().getItemIds().getItemId())) {
            exceptionMessage = "gstSalesTaxLine.getOrderItem().getItemIds(),getItemId() is null";
        } else if(Objects.isNull(gstSalesTaxLine.getOrderItem().getItemIds().getItemId().getId())) {
            exceptionMessage = "gstSalesTaxLine.getOrderItem().getItemIds(),getItemId().getId() is null";
        }
        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage
                    = "Unable to map SalesTaxLine from GSTResponse to SalesTaxResponseLine in PSP" + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }
    }
}

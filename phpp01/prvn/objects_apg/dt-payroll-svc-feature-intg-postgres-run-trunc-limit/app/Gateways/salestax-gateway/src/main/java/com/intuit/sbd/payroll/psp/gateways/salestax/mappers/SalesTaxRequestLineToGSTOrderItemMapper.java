package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SalesTaxRequestLineToGSTOrderItemMapper extends BeanMapper<SalesTaxRequestLine, OrderItem> {


    @Override
    public OrderItem mapToTarget(SalesTaxRequestLine pspSalesTaxRequestLine, Class<OrderItem> orderItemClass) {
        checkForNullObjects(pspSalesTaxRequestLine);

        OrderItem gstOrderItem = new OrderItem();

        //todo - check this implementation (2)
        ItemAttributes itemAttributes = getItemAttributesForSalesTaxRequestLine(pspSalesTaxRequestLine);

        //todo - check this implementation (2)
        ItemIds itemIds = getItemIdsForSalesTaxRequestLine(pspSalesTaxRequestLine);

        //we are setting blank description based on what we saw in existing requests going to GST
        gstOrderItem.setDescription("");
        gstOrderItem.setItemAttributes(itemAttributes);
        gstOrderItem.setItemIds(itemIds);

        return gstOrderItem;

    }

    private ItemAttributes getItemAttributesForSalesTaxRequestLine(SalesTaxRequestLine pspSalesTaxRequestLine) {
        ItemAttributes itemAttributes = new ItemAttributes();
        String productClass = pspSalesTaxRequestLine.getProductClassForSKU();

        itemAttributes.setProductClass(productClass);
        //todo- check if this would be same for all
        itemAttributes.setShippableInd(false);

        return itemAttributes;
    }

    private ItemId createItemIdForItemSKU(String ItemSKU) {
        ItemId itemId = new ItemId();
        Id id = new Id().withSchemeName("OracleSKU").withValue(ItemSKU);
        itemId.setId(id);
        return itemId;
    }

    private ItemIds getItemIdsForSalesTaxRequestLine(SalesTaxRequestLine pspSalesTaxRequestLine) {
        ItemIds itemIds = new ItemIds();
        ItemId itemId = createItemIdForItemSKU(pspSalesTaxRequestLine.getSKU());
        itemIds.setItemId(itemId);
        return itemIds;
    }
    private void checkForNullObjects(SalesTaxRequestLine pspSalesTaxRequestLine) {
        String exceptionMessage = "";
        if(Objects.isNull(pspSalesTaxRequestLine)) {
            exceptionMessage = "SalesTaxRequestLine is null";
        } else if(Objects.isNull(pspSalesTaxRequestLine.getSKU())) {
            exceptionMessage = "pspSalesTaxRequestLine.getSKU() is null";
        } else if(Objects.isNull(pspSalesTaxRequestLine.getProductClassForSKU())) {
            exceptionMessage = "pspSalesTaxRequestLine.getProductClassForSKU() is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage =
                    "Can not map SalesTaxRequestLine to OrderItem" + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }
    }
}

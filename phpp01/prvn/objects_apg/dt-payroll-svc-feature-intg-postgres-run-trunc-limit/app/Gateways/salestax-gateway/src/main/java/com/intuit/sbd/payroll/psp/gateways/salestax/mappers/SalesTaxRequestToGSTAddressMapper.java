package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.Address;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class SalesTaxRequestToGSTAddressMapper extends BeanMapper<SalesTaxRequest, Address> {
    @Override
    public Address mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<Address> addressClass) {
        checkForNullObjects(pspSalesTaxRequest);
        Address address = new Address();


        //set address lines
        List<String> addressLines = new ArrayList<>();
        addressLines.add(pspSalesTaxRequest.getAddressLine1());

        if(!StringUtils.isEmpty(pspSalesTaxRequest.getAddressLine2())) {
            addressLines.add(pspSalesTaxRequest.getAddressLine2());
        }
        if(!StringUtils.isEmpty(pspSalesTaxRequest.getAddressLine3())) {
            addressLines.add(pspSalesTaxRequest.getAddressLine3());
        }

        address.setAddressLine(addressLines);
        address.setPostalCode(pspSalesTaxRequest.getZipCode());
        address.setCity(pspSalesTaxRequest.getCity());
        address.setStateOrProvince(pspSalesTaxRequest.getState());

        //todo - check if this needs to be set as US or USA
        address.setCountry("USA".equals(pspSalesTaxRequest.getCountry()) ? "US": pspSalesTaxRequest.getCountry());

        address.setStandardizeAddress(true);
        address.setStandardizedAddressInd(false);

        return address;
    }

    private void checkForNullObjects(SalesTaxRequest pspSalesTaxRequest) {
        String exceptionMessage = "";
        if(Objects.isNull(pspSalesTaxRequest)) {
            exceptionMessage = "pspSalesTaxRequest is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage =
                    "Can not map SalesTaxRequest inside PSP to GST Address" + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }
    }
}

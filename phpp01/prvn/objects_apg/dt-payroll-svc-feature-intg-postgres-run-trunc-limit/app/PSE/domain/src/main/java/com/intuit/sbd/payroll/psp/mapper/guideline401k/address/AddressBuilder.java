package com.intuit.sbd.payroll.psp.mapper.guideline401k.address;

import com.intuit.sbd.payroll.psp.constants.Guideline401kConstants;
import com.intuit.v4.common.Address;
import com.intuit.v4.common.NameValue;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to help Address mapping
 */
public class AddressBuilder {

    private Address addressModel;
    private int componentIndex;

    public AddressBuilder() {
        this.addressModel = new Address();
        this.componentIndex = 0;
    }

    public AddressBuilder setAddressLine1(String addressLine1){
        if (StringUtils.isNotEmpty(addressLine1)) {
            addressModel.setAddressComponents(componentIndex++,
                    new NameValue().name(Guideline401kConstants.ADDRESS_COMPONENT_LINE1).value(addressLine1));
        }
        return this;
    }

    public AddressBuilder setAddressLine2(String addressLine2){
        if (StringUtils.isNotEmpty(addressLine2)) {
            addressModel.setAddressComponents(componentIndex++,
                    new NameValue().name(Guideline401kConstants.ADDRESS_COMPONENT_LINE2).value(addressLine2));
        }
        return this;
    }

    public AddressBuilder setCity(String city){
        if (StringUtils.isNotEmpty(city)) {
            addressModel.setAddressComponents(componentIndex++,
                    new NameValue().name(Guideline401kConstants.ADDRESS_COMPONENT_CITY).value(city));
        }
        return this;
    }

    public AddressBuilder setPostalCode(String postalCode){
        if (StringUtils.isNotEmpty(postalCode)) {
            addressModel.setAddressComponents(componentIndex++,
                    new NameValue().name(Guideline401kConstants.ADDRESS_COMPONENT_POSTAL_CODE).value(postalCode));
        }
        return this;
    }

    public AddressBuilder setState(String state){
        //v4 region = psp state
        if (StringUtils.isNotEmpty(state)) {
            addressModel.setAddressComponents(componentIndex++, new NameValue().name(Guideline401kConstants.ADDRESS_COMPONENT_REGION).value(state));
        }
        return this;
    }

    public AddressBuilder setCountry(String country){
        if (StringUtils.isNotEmpty(country)) {
            addressModel.setAddressComponents(componentIndex++,
                    new NameValue().name(Guideline401kConstants.ADDRESS_COMPONENT_COUNTRY).value(country));
        }
        return this;
    }

    public Address build(){
        return addressModel;
    }
}

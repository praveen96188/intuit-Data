package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.AddressCDMImpl;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.sbd.payroll.psp.domain.Address;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.AddressToAddressCDMMapper")
public class AddressToAddressCDMMapper extends BeanMapper<Address, AddressSubCDM> {

    // todo: NULL check abstraction can be moved to BaseClass
    //  Target Class object creation

    @Override
    public AddressSubCDM mapToTarget(Address address, Class<AddressSubCDM> target) {
        if(Objects.isNull(address)){
            return null;
        }
        AddressSubCDM addressSubCDM = new AddressCDMImpl();
        addressSubCDM.setStreetAddress(StringUtils.isBlank(address.getStreetAddress()) ? null:address.getStreetAddress());
        addressSubCDM.setCity(address.getCity());
        //todo: this will set country as US for all entity types. Need to restrict this to employee only
        addressSubCDM.setCountry(Objects.isNull(address.getCountry())? "US" : address.getCountry());
        addressSubCDM.setPostalCode(StringUtils.isBlank(address.getFullZipCode()) ? null: address.getFullZipCode());
        addressSubCDM.setRegion(address.getState());
        return addressSubCDM;
    }
}
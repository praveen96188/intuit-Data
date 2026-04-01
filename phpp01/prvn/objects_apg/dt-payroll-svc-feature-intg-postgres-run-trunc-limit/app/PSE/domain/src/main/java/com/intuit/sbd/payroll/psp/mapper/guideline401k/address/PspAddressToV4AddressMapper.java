package com.intuit.sbd.payroll.psp.mapper.guideline401k.address;

import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.common.Address;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PspAddressToV4AddressMapper extends BeanMapper<com.intuit.sbd.payroll.psp.domain.Address, Address> {

    @Override
    public Address mapToTarget(com.intuit.sbd.payroll.psp.domain.Address pspAddress, Class<Address> t) {

        Address v4Address = new Address();

        if(Objects.isNull(pspAddress)){
            return v4Address;
        }

        AddressBuilder addressBuilder = new AddressBuilder();
        Address tempAddress = addressBuilder.setAddressLine1(pspAddress.getAddressLine1()).setAddressLine2(pspAddress.getAddressLine2())
                .setCity(pspAddress.getCity()).setPostalCode(pspAddress.getFullZipCode()).setState(pspAddress.getState())
                .setCountry(pspAddress.getCountry()).build();
        v4Address.setAddressComponents(tempAddress.getAddressComponents());

        return v4Address;
    }
}

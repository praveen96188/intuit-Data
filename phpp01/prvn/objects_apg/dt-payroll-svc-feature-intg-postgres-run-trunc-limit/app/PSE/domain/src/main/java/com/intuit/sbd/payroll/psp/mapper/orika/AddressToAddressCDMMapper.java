package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.sbd.payroll.psp.domain.Address;
import ma.glasnost.orika.MappingContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Custom mapper class associates the field name between
 * {@link com.intuit.sbd.payroll.psp.domain.Address} and
 * {@link com.intuit.payroll.api.shared.model.AddressCDM}
 *
 * @author kmuthurangam
 */
@Component
public class AddressToAddressCDMMapper extends BeanMapper<Address, AddressCDM> {

    @Override
    public void directFieldToFieldMapping() {
        addBidirectionalFieldMapping("city", "city");
        addBidirectionalFieldMapping("country", "country");
        addBidirectionalFieldMapping("state", "region");
        addBidirectionalFieldMapping("zipCode", "postalCode");
    }

    @Override
    public void indirectFieldToFieldMapping() {
        addAtoBFieldAssociationMapping("addressLine1", "streetAddress");
        addAtoBFieldAssociationMapping("addressLine2", "streetAddress");
        addAtoBFieldAssociationMapping("addressLine3", "streetAddress");
    }

    @Override
    public void mapAtoB(Address address, AddressCDM addressCDM, MappingContext mappingContext) {
        // returns blank when address1 , address2 & address3 are null (empty)
        // ignore blank to treat them as null

        String streetAddress = address.getAddressLine1() != null ? address.getAddressLine1()
                : "" + address.getAddressLine2() != null ? address.getAddressLine2()
                : "" + address.getAddressLine3() != null ? address.getAddressLine3()
                : "";
        if (!StringUtils.isBlank(streetAddress)) {
            addressCDM.setStreetAddress(streetAddress);
        }
    }


}

package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.shared.model.*;
import com.intuit.sbg.nucleus.mapper.AbstractConcreteTypeRegistry;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import org.springframework.stereotype.Component;

@Component
public class ConcreteTypeMapperRegistry extends AbstractConcreteTypeRegistry {

    public ConcreteTypeMapperRegistry() {
        registerConcreteType(AddressCDM.class, AddressCDMImpl.class);
        registerConcreteType(PrivilegedBankAccountCDM.class, PrivilegedBankAccountCDMImpl.class);
        registerConcreteType(BankPrincipalCDM.class, BankPrincipalCDMImpl.class);
        registerConcreteType(SpcfCalendar.class, SpcfCalendarImpl.class);
        registerConcreteType(BankAccountSubCDM.class, BankAccountCDMImpl.class);
    }
}

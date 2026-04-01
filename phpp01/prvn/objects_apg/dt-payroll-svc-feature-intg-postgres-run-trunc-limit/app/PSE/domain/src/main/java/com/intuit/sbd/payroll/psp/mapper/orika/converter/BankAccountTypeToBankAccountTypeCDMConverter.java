package com.intuit.sbd.payroll.psp.mapper.orika.converter;

import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

/**
 * A custom Orika converter to convert from PSP
 * {@link com.intuit.sbd.payroll.psp.domain.BankAccountType} to DD
 * {@link com.intuit.payroll.api.shared.model.BankAccountType} in both directions
 *
 * @author kmuthurangam
 */
@Component
public class BankAccountTypeToBankAccountTypeCDMConverter
        extends BidirectionalConverter<BankAccountType, com.intuit.payroll.api.shared.model.BankAccountType> {

    @Override
    public com.intuit.payroll.api.shared.model.BankAccountType convertTo(BankAccountType source,
                                                                         Type<com.intuit.payroll.api.shared.model.BankAccountType> destinationType) {
        com.intuit.payroll.api.shared.model.BankAccountType bankAccountType = null;

        switch (source) {
            case Checking:
                bankAccountType = com.intuit.payroll.api.shared.model.BankAccountType.BUSINESS_CHECKING;
                break;
            case Savings:
                bankAccountType = com.intuit.payroll.api.shared.model.BankAccountType.BUSINESS_SAVINGS;
                break;
        }
        return bankAccountType;
    }

    @Override
    public BankAccountType convertFrom(com.intuit.payroll.api.shared.model.BankAccountType source,
                                       Type<BankAccountType> destinationType) {
        BankAccountType bankAccountType = null;
        switch (source) {
            case BUSINESS_CHECKING:
                bankAccountType = BankAccountType.Checking;
                break;
            case BUSINESS_SAVINGS:
                bankAccountType = BankAccountType.Savings;
                break;
            case BUSINESS_LOAN:
            case PERSONAL_CHECKING:
            case PERSONAL_SAVINGS:
            case PERSONAL_LOAN:
                break;
        }
        return bankAccountType;
    }

}

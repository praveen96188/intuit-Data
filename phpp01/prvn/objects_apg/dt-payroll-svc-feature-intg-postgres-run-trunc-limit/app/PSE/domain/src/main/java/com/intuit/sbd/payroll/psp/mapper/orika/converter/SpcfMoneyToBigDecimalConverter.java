package com.intuit.sbd.payroll.psp.mapper.orika.converter;
import com.intuit.spc.foundations.primary.SpcfMoney;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * A custom Orika converter to convert from {@link com.intuit.spc.foundations.primary.SpcfMoney} to
 * {@link BigDecimal} in both directions
 *
 * @author kmuthurangam
 */
@Component
public class SpcfMoneyToBigDecimalConverter extends BidirectionalConverter<SpcfMoney, BigDecimal> {

    @Override
    public BigDecimal convertTo(SpcfMoney source, Type<BigDecimal> destinationType) {
        return new BigDecimal(source.toString());
    }

    @Override
    public SpcfMoney convertFrom(BigDecimal source, Type<SpcfMoney> destinationType) {
        return new SpcfMoney(source.toString());
    }

}

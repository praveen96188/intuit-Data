package com.intuit.sbd.payroll.psp.mapper.orika.converter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * A custom Orika converter to convert from
 * {@link com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl} to
 * {@link org.joda.time.LocalDate} in both directions
 *
 * @author kmuthurangam
 */
@Component("dateConverter")
public class LocalDateConverter extends BidirectionalConverter<SpcfCalendarImpl, LocalDate> {

    @Override
    public SpcfCalendarImpl convertFrom(LocalDate source, Type<SpcfCalendarImpl> destinationType) {
        return (SpcfCalendarImpl) SpcfCalendar.createInstance(source.getYear(), source.getMonthOfYear(), source.getDayOfMonth());
    }

    @Override
    public LocalDate convertTo(SpcfCalendarImpl source, Type<LocalDate> destinationType) {
        return new LocalDate(source.getTimeInMilliseconds());
    }

}

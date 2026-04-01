package com.intuit.sbd.payroll.psp.mapper.orika.converter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component
public class SpcfCalendarToDateTimeConverter extends BidirectionalConverter<SpcfCalendar, DateTime> {

    @Override
    public SpcfCalendar convertFrom(DateTime source, Type<SpcfCalendar> destinationType) {
        if (source == null) {
            return null;
        }
        return SpcfCalendar.createInstance(source.getMillis());
    }

    @Override
    public DateTime convertTo(SpcfCalendar source, Type<DateTime> destinationType) {
        if (source == null) {
            return null;
        }
        return new DateTime(source.getTimeInMilliseconds());
    }

}

package com.intuit.sbd.payroll.psp.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Component
public class SpcfCalendarImplToStringSerializer extends JsonSerializer<SpcfCalendar> {

    @Override
    public void serialize(SpcfCalendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        gen.writeString(dateFormat.format(new Date(value.getTimeInMilliseconds())));
    }

}

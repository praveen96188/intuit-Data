package com.intuit.sbd.payroll.psp.jss.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SpcfCalendarImplToStringSerializer extends JsonSerializer<SpcfCalendar> {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Override
    public void serialize(SpcfCalendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        gen.writeString(dateFormat.format(new Date(value.getTimeInMilliseconds())));
    }

}

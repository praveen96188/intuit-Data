package com.intuit.sbd.payroll.psp.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class StringToSpcfCalendarImplSerializer extends JsonDeserializer<SpcfCalendar> {

    @Override
    public SpcfCalendar deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        DateFormat dateFormat = new SimpleDateFormat(CommonConstants.DEFAULT_DATE_FORMAT);
        try {
            long milliSeconds = dateFormat.parse(p.getValueAsString()).getTime();
            return SpcfCalendar.createInstance(milliSeconds);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing the date", e);
        }
    }

}

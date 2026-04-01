package com.intuit.sbd.payroll.psp.mapper.jackson.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class SpcfCalendarImplModule extends SimpleModule {

    private static final long serialVersionUID = 3875720307616947435L;

    public SpcfCalendarImplModule() {
        super();
        addSerializer(SpcfCalendar.class, new SpcfCalendarImplToStringSerializer());
        addDeserializer(SpcfCalendar.class, new StringToSpcfCalendarImplSerializer());
    }

}

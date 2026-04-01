package com.intuit.sbd.payroll.psp.mapper.jackson.serializer;

import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.LocalDate;

/**
 * Allows customization of the joda serialization / deserialization module.
 *
 * Reason we have custom Deserializer is just to capture deserialization exception and customize it.
 *
 * For Serializer, we don't need to as the standard default LocalDateSerializer
 * and DateTimeSerializer works per platform specs:
 * Per standard, in the response, we  expect all resources to adhere to a consistent representation of
 * DateTime in the UTC format.
 * We do not wish to represent timezone in the DateTime format so that we do not inadvertently expose
 * our server timezone.
 *
 */
public class JodaModuleCustom extends JodaModule {
    public JodaModuleCustom() {
        super();
        addDeserializer(LocalDate.class, new LocalDateDeserializerCustom());
    }
}

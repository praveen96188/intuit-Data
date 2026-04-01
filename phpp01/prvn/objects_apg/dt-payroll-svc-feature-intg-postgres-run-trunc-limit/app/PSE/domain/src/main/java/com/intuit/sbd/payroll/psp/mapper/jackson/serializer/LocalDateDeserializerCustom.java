package com.intuit.sbd.payroll.psp.mapper.jackson.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.joda.deser.LocalDateDeserializer;
import com.intuit.sbg.nucleus.exception.ValidationErrorNumber;
import com.intuit.sbg.nucleus.exception.ValidationException;
import org.joda.time.LocalDate;

import java.io.IOException;

/**
 * Used to customize the exception returned by the deserializer, other behavior is the same.
 */
public class LocalDateDeserializerCustom extends LocalDateDeserializer {

    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        try {
            return super.deserialize(jp, ctxt);
        } catch(IllegalArgumentException exception) {
            throw new ValidationException(ValidationErrorNumber.DATE_INVALID, exception.getMessage(), exception);
        }
    }
}

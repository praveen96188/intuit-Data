package com.intuit.sbd.payroll.psp.mapper.jackson;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intuit.payroll.api.shared.BigDecimalModuleCustom;
import com.intuit.sbd.payroll.psp.mapper.jackson.serializer.JodaModuleCustom;
import org.apache.commons.lang3.BooleanUtils;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Jersey provided way for configuring the jackson ObjectMapper.
 */
@Provider
public class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

    private static ObjectMapperResolver instance;
    public static ObjectMapperResolver getInstance() {
        if(instance == null) {
            instance = new ObjectMapperResolver();
        }
        return instance;
    }

	private final ObjectMapper objectMapper;

    public ObjectMapperResolver() {
        //Setup jackson object mapper configuration
        objectMapper = new ObjectMapper();
        //Determine whether we should pretty print or not
        //String prettyPrintValue = PropertiesManager.getProperty("pretty.print");
        boolean prettyPrint = false;
        //Pretty print output indentation
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, prettyPrint);
        //Don't serialize map entries with null values
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        //Don't serialize null values
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		//Serialize BigDecimal as String
		objectMapper.registerModule(new BigDecimalModuleCustom());

		/**
		 * Configure jackson to handle Joda time types
		 * Disable WRITE_DATES_AS_TIMESTAMPS: Feature that determines whether Date values
		 * (and Date-based things like Calendars) are to be serialized
		 * as numeric timestamps (true; the default), or as something else (usually textual representation).
		 */
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JodaModuleCustom());
    }

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return objectMapper;
	}

    public ObjectMapper getContext() {
        return objectMapper;
    }

    public <T> String serialize(T o) {
        try {
            return getContext().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return objectMapper.readValue(json, clazz);
    }

    public <T> List<T> deserializeList(String json, Class<T> clazz) throws IOException {
        JavaType type = getContext().getTypeFactory().constructCollectionType(List.class, clazz);
        if (json.indexOf("ArrayList") > 0) {
            type = getContext().getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        }

        //noinspection unchecked
        return (List<T>) getContext().readValue(json, type);
    }
}

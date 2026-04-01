package com.intuit.sbd.payroll.psp.mapper.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbd.payroll.psp.mapper.jackson.serializer.SpcfCalendarImplModule;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class CustomObjectMapperResolver {

    private ObjectMapperResolver mapperResolver = ObjectMapperResolver.getInstance();

    @PostConstruct
    public void register() {
        ObjectMapper mapper = mapperResolver.getContext();
        mapper.registerModule(new SpcfCalendarImplModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String serialize(Object entity) {
        return mapperResolver.serialize(entity);
    }

    public <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return mapperResolver.getContext().readValue(json, clazz);
    }

    @SuppressWarnings({"rawtypes"})
    public <T> T deserialize(String json, TypeReference valueTypeRef) throws IOException {
        return (T) mapperResolver.getContext().readValue(json, valueTypeRef);
    }
}

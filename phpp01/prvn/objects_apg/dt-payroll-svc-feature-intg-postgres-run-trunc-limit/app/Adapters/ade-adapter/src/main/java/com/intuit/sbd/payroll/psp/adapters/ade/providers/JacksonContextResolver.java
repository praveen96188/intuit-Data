package com.intuit.sbd.payroll.psp.adapters.ade.providers;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 4/12/14
 * Time: 3:39 AM
 * To change this template use File | Settings | File Templates.
 */

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.text.SimpleDateFormat;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonContextResolver implements ContextResolver<ObjectMapper> {
    private static JacksonContextResolver instance;
    static JacksonContextResolver getInstance() {
        if(instance == null) {
            instance = new JacksonContextResolver();
        }
        return instance;
    }

    private ObjectMapper objectMapper;

    public JacksonContextResolver() {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        objectMapper.configure(DeserializationConfig.Feature.UNWRAP_ROOT_VALUE, false);
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);


        AnnotationIntrospector primary = new NonEmptyJaxbAnnotationInspector();
        this.objectMapper.setAnnotationIntrospector(primary);

        // this pattern is incorrect, but IOP CEP is doing the same thing, so we are going to keep it to be consistent
        this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    public ObjectMapper getContext(Class<?> objectType) {
        return objectMapper;
    }

    public <T> String serialize(T o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


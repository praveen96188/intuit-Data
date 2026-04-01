package com.intuit.sbg.psp.dd.util;

import com.google.gson.FieldNamingStrategy;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;

/**
 * @author dchoudhary1
 * FieldNameStrategy to Use XMLElement Annotation Name if available.
 */
public class GsonFieldNameStrategy implements FieldNamingStrategy {

    @Override
    /**
     * Over-ride Gson Field Naming Strategy to use XmlElement Annotation Name if present. This is required as
     * many POJO's are Code Generated from XSD's and carry XmlElement Name, and without this over-ride, there is a
     * mis-match in JSON Field Names.
     */
    public String translateName(Field field) {
        XmlElement xmlElementAnnotation = field.getAnnotation(XmlElement.class);
        return xmlElementAnnotation != null ? xmlElementAnnotation.name() : field.getName();
    }
}

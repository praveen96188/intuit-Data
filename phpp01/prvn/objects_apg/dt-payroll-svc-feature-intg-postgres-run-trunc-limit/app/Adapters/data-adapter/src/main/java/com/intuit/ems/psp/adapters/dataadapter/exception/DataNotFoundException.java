package com.intuit.ems.psp.adapters.dataadapter.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by ajhawar on 10/21/2015.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class DataNotFoundException extends Exception {
    @XmlElement
    String errorMessage;

    public DataNotFoundException(String message) {
        super(message);
        errorMessage = message;
    }

}

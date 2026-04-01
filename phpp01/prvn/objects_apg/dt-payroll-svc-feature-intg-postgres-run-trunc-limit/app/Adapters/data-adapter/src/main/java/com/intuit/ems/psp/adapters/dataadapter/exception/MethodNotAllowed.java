package com.intuit.ems.psp.adapters.dataadapter.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by ajhawar on 10/22/2015.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class MethodNotAllowed extends RuntimeException {
    @XmlElement
    String errorMessage;

    public MethodNotAllowed(String message) {
        super(message);
        errorMessage = message;
    }

}

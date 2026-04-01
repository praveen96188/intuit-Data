package com.intuit.ems.psp.adapters.dataadapter.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by ajhawar on 1/3/2016.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BadRequestException extends Exception {
    @XmlElement
    String errorMessage;

    public BadRequestException(String message) {
        super(message);
        errorMessage = message;
    }
}

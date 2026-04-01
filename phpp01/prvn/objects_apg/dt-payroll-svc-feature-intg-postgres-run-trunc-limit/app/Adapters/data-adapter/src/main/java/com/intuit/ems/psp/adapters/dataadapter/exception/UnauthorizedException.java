package com.intuit.ems.psp.adapters.dataadapter.exception;

//TODO Change to Mapper
/**
 * Created by charithah418 on 10/11/15.
 */

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class UnauthorizedException extends RuntimeException {

    @XmlElement
    String errorMessage;

    public UnauthorizedException(String message) {
        super(message);
        errorMessage = message;
    }
}
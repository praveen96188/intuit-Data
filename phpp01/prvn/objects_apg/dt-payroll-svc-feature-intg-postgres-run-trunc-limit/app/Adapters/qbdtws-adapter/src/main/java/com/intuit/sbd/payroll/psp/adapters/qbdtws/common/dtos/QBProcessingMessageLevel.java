package com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos;


import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="QBProcessingMessageLevel")
@XmlEnum
public enum QBProcessingMessageLevel {
    ERROR,
    WARNING,
    INFO;

    //private static SpcfLogger logger = PayrollServices.getLogger(QBProcessingMessageLevel.class);

    public static QBProcessingMessageLevel fromPSPMessageLevel(String messageLevelName) {
        if (messageLevelName == null)
            return QBProcessingMessageLevel.INFO;

        try {
            return QBProcessingMessageLevel.valueOf(messageLevelName);
        }
        catch (IllegalArgumentException e) {
            //logger.error("could not translate PSP MessageLevel: " + messageLevelName);
            return QBProcessingMessageLevel.INFO;
        }
    }
}

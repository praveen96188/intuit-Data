package com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.ArrayList;

@XmlType(name = "QBProcessingMessages")
public class QBProcessingMessages {

    private List<QBProcessingMessage> mProcessingErrorsList;

    @XmlElementWrapper(name = "ProcessingMessages")
    @XmlElement(required = false, name = "ProcessingMessage")
    public List<QBProcessingMessage> getProcessingMessagesList() {
         if (mProcessingErrorsList == null) {
            mProcessingErrorsList = new ArrayList<QBProcessingMessage>();
        }
        return this.mProcessingErrorsList;
    }
}

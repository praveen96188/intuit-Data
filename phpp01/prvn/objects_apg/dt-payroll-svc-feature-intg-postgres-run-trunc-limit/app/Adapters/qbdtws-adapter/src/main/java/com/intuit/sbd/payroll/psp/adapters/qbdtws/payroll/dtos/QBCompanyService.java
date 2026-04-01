package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QBCompanyService", propOrder = {
    "name",
    "status"
})
public class QBCompanyService {
    @XmlElement(name = "Name", required = true)
    protected QBCompanyServiceEnum name;

    @XmlElement(name = "Status", required = true)
    protected String status;

    public QBCompanyServiceEnum getName() {
        return name;
    }

    public void setName(QBCompanyServiceEnum name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

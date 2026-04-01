package com.intuit.ems.psp.adapters.dataadapter.dto;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 * Date: 7/21/15
 */

@XmlRootElement(name = "PayrollStatus")
public class PayrollStatus{

    @XmlElement(name="QBLicense")
    public String getQBLicense() {
        return QBLicense;
    }

    public void setQBLicense(String pQBLicense) {
        QBLicense = pQBLicense;
    }

    private String Status;
    private String EndDate;
    @XmlElement(name="Status")
    public String getStatus() {
        return Status;
    }

    public void setStatus(String pStatus) {
        Status = pStatus;
    }

    @XmlElement(name="EndDate")
    public String getEndDate() {
        return EndDate;
    }

    public void setEndDate(String pEndDate) {
        EndDate = pEndDate;
    }


    private String UUID;
    private String QBLicense;
}

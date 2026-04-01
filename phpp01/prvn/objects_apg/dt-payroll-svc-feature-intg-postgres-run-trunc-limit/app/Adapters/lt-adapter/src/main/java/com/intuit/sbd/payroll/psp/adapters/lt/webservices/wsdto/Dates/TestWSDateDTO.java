package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.SimpleDateFormat;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TestWS_Dates", propOrder = {
        "pspDate",
        "genNachaFiles"
})
public class TestWSDateDTO implements LtDateDTO{

    @XmlElement(name = "Test_PSPDate")
    public String pspDate;

    @XmlElement(name = "Test_GenerateNACHAFilesDate")
    public String genNachaFiles;

    public TestWSDateDTO() {
    }

    public TestWSDateDTO(Date date) {
        this.setDate(date);
    }

    public String getPspDate() {
        return pspDate;
    }

    public void setPspDate(String pspDate) {
        this.pspDate = pspDate;
    }

    public String getGenNachaFiles() {
        return genNachaFiles;
    }

    public void setGenNachaFiles(String genNachaFiles) {
        this.genNachaFiles = genNachaFiles;
    }

    public void setDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat();

        //format PSP_DATE date
        sdf.applyPattern(DateUtilities.PSP_DATE_FORMAT);
        this.pspDate = sdf.format(date);

        //format GenerateNachaFile date
        sdf.applyPattern(DateUtilities.TESTWS_SHORT_FORMAT);
        this.genNachaFiles = sdf.format(date);
    }
}

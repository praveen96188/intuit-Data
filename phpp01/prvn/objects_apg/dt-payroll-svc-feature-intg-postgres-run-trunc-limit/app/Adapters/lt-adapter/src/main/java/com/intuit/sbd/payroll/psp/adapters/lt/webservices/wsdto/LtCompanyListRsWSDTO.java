package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.LtCompanyDTO;

import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * User: msalayko
 */
public class LtCompanyListRsWSDTO {
    public int numberOfCompanies;
    public ArrayList<LtCompanyDTO> Company;
    public int status;
    public String message;

    public LtCompanyListRsWSDTO() {
        this.Company = new ArrayList<LtCompanyDTO>();
    }
}

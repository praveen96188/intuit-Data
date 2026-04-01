package com.intuit.sbd.payroll.psp.adapters.lt.rest;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class VmpCompany {
    public String id;
    public String companyRealmId;
    public List<VmpEmployee> employees;
}

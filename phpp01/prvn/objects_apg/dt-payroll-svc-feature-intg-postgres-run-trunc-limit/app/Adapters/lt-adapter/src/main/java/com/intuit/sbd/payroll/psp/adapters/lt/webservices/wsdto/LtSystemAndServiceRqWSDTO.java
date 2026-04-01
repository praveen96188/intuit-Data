package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Feb 25, 2010
 * Time: 9:26:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class LtSystemAndServiceRqWSDTO {

    @XmlElement(name = "SourceSystemCode", nillable = false, required = true)
    public SourceSystemCode sourceSystemId;

    @XmlElement(name = "ServiceCode", nillable = false, required = true)
    public ServiceCode serviceCode;

    @XmlElement(name = "StatusCode", nillable = false, required = true)
    public ServiceSubStatusCode statusCode;
}

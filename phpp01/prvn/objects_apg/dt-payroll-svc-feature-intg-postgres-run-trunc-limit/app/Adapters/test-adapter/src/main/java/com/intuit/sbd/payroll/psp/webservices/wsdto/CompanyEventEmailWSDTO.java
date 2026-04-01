package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jul 8, 2009
 * Time: 8:53:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyEventEmailWSDTO {
    public String emailTemplateTypeCd;
    public Collection<CompanyEventEmailParamWSDTO> eventEmailParams;    
}

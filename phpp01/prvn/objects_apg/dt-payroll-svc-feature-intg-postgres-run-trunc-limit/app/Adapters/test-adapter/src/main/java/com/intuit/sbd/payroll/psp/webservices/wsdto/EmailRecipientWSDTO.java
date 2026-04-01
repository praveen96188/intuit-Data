package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Aug 19, 2008
 * Time: 9:27:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailRecipientWSDTO {
    public String companyId;
    public String recipientEmail;
    public String recipientId;
    public String recipientName;
    public String templateId;

    public Collection<EmailPropertyWSDTO> properties;
}

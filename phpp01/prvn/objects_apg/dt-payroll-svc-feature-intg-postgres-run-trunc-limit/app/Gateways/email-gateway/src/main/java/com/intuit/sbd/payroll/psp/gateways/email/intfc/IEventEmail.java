package com.intuit.sbd.payroll.psp.gateways.email.intfc;

import com.intuit.sbd.payroll.psp.gateways.email.util.EmailFormatType;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 20, 2008
 * Time: 11:44:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IEventEmail {
    public String getCompanyId();
    public String getRecipientId();
    public String getRecipientName();
    public String getRecipientEmail();
    public EventEmailTemplateTypeCode getTemplateId();
    public EmailFormatType getPreferredFormat();
    public Properties getProperties();
    public void failedValidation(String pErrorDetail);
    public void serviceReturned(String pErrorDetail);
    public void serviceFault(String pErrorDetail);
}

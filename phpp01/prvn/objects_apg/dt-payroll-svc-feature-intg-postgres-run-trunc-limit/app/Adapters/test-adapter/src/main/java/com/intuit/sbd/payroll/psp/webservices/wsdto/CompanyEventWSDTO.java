package com.intuit.sbd.payroll.psp.webservices.wsdto;

import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventGroup;

import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Nov 20, 2008
 * Time: 11:14:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyEventWSDTO {
    public Date eventDate;
    public EventTypeCode eventTypeCd;
    public CompanyEventStatus statusCd;
    public Date statusEffectiveDate;
    public String id;
    public String eventTypeName;
    public String eventTypeDescription;
    public Collection<CompanyEventDetailWSDTO> eventDetails;
}

package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 19, 2008
 * Time: 9:08:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ActivationCheckListWSDTO {
    public String activationStatus;
    public Date activationStatusDate;
    public String postActivationStatus;
    public Date postActivationStatusDate;
    public Date endDate;
    public String assignedUser;
    public Collection<ActivationCheckListItemWSDTO> checklistItems;
}
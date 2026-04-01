package com.intuit.sbd.payroll.psp.gateways.email.intfc;

import com.intuit.sbd.payroll.psp.gateways.email.util.EventStatus;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmail;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jun 12, 2009
 * Time: 4:44:44 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IEventRepository {
    public EventStatus addEvent(CompanyEventEmail pEvent);
}

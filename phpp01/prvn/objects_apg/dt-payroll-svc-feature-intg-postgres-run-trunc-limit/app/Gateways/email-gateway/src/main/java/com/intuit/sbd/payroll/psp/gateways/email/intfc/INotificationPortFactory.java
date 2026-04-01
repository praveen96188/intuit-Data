package com.intuit.sbd.payroll.psp.gateways.email.intfc;

import com.intuit.ias.notification.pub.wsdl.NotificationPort;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 8/20/12
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public interface INotificationPortFactory {
    public NotificationPort getNotificationPort();
}

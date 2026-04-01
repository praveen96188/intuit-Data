package com.intuit.sbd.payroll.psp.gateways.email.factory;

import com.intuit.sbd.payroll.psp.gateways.email.factory.product.service.EmailNotificationService;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.INotificationPortFactory;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 20, 2008
 * Time: 9:16:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotificationServiceFactory {
    private static INotificationPortFactory mPortFactory = new NotificationPortFactory();

    public static void useDefaultPortFactory() {
        setPortFactory(null);
    }

    public static void setPortFactory(INotificationPortFactory pPortFactory) {
        mPortFactory = ((pPortFactory != null) ? pPortFactory : new NotificationPortFactory());
    }

    public static EmailNotificationService getEmailServiceInstance() {
        EmailNotificationService service;

        try {
            service = new EmailNotificationService(mPortFactory.getNotificationPort(),
                                                   EmailUtils.getConfig("iasns-senderid"),
                                                   EmailUtils.getConfig("iasns-sendername"),
                                                   EmailUtils.getConfig("iasns-senderaddress"));
        } catch (Exception e) {
            throw new RuntimeException("Error initializing EmailNotificationService instance.", e);
        }

        return service;
    }
}

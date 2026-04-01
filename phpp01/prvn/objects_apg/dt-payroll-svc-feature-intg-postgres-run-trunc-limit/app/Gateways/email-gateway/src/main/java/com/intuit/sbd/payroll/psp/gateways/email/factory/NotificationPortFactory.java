package com.intuit.sbd.payroll.psp.gateways.email.factory;

import com.intuit.ias.notification.pub.wsdl.NotificationPort;
import com.intuit.ias.notification.pub.wsdl.NotificationService;
import com.intuit.sbd.payroll.psp.common.utils.PspCertificateManager;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.INotificationPortFactory;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: 8/20/12
 * Time: 11:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotificationPortFactory implements INotificationPortFactory {
    private static final String KEY_STORE_ALIAS = "email.gateway";
    private static final String SSL_SOCKET_FACTORY = "com.sun.xml.ws.transport.https.client.SSLSocketFactory";

    public NotificationPort getNotificationPort() {
        NotificationPort notificationPort;

        try {
            String serviceUrl = EmailUtils.getConfig("iasns-serviceurl");
            URL wsdlLocation = new URL(serviceUrl + "?wsdl");
            QName qName = new QName(EmailUtils.getConfig("iasns-namespace"), EmailUtils.getConfig("iasns-servicename"));
            Service notificationService;

            try {
                notificationService = new NotificationService(wsdlLocation, qName);
            } catch (Exception e) {
                String wsdlPath = EmailUtils.getConfig("iasns-servicepath");
                notificationService = new NotificationService(new URL("file:///" + wsdlPath), qName);
            }

            notificationPort = notificationService.getPort(NotificationPort.class);

            Map<String, Object> requestContext = ((BindingProvider) notificationPort).getRequestContext();

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, serviceUrl);

            if (serviceUrl.startsWith("https:")) {
                requestContext.put(SSL_SOCKET_FACTORY, PspCertificateManager.getSSLSocketFactory(KEY_STORE_ALIAS));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing NotificationPort instance.", e);
        }

        return notificationPort;
    }
}

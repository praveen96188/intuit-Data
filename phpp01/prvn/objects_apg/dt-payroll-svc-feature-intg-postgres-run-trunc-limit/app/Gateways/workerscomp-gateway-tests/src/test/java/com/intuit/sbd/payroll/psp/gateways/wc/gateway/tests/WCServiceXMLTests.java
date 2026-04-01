package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.bp.wc.common.schema.*;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static junit.framework.Assert.assertTrue;

/**
 * Author: Sriram Nutakki
 * Date created: 11/5/12
 */
public class WCServiceXMLTests {

    @Test
    public void testSubscriptionXMLMarshallingAndUnmarshalling() throws Exception {
        JAXBContext context = JAXBContext.newInstance(WorkersCompSubscriptions.class);

        // Marshal
        StringWriter writer = new StringWriter();
        WorkersCompSubscriptions objIn = new WorkersCompSubscriptions();
        WorkersCompSubscription sub1 = new WorkersCompSubscription();
        sub1.setPSID("123");
        sub1.setActive(true);
        XMLGregorianCalendar cal = WCUtil.createXMLGregorianCalendar(2005, 5, 20);
        sub1.setStartDate(cal);
        objIn.getWorkersCompSubscription().add(sub1);
        context.createMarshaller().marshal(objIn, writer);
        String xml = writer.toString();
        WorkersCompSubscriptions objOut = (WorkersCompSubscriptions) context.createUnmarshaller().unmarshal(new StringReader(xml));

        assertTrue(sub1.getPSID().equals(objOut.getWorkersCompSubscription().get(0).getPSID()));
        assertTrue(sub1.isActive() == objOut.getWorkersCompSubscription().get(0).isActive());

        //System.out.println(xml);
    }

    @Test
    public void testSubscriptionStatusDataXMLMarshallingAndUnmarshalling() throws Exception {
        JAXBContext context = JAXBContext.newInstance(WCSubscriptionStatusData.class);

        // Marshal
        StringWriter writer = new StringWriter();
        WCSubscriptionStatusData objIn = new WCSubscriptionStatusData();
        Item item1 = new Item();
        item1.setKey("PayrollSentDate");
        item1.setLabel("Payroll Sent Date");
        item1.setValue("12/12/2012");
        objIn.getData().add(item1);

        Item item2 = new Item();
        item2.setKey("PayrollDate");
        item2.setLabel("Payroll Date");
        item2.setValue("12/12/2012");
        objIn.getData().add(item2);

        Item item3 = new Item();
        item3.setKey("InsuranceProvider");
        item3.setLabel("Insurance Provider");
        item3.setValue("AP Intego");
        objIn.getData().add(item3);

        context.createMarshaller().marshal(objIn, writer);
        String xml = writer.toString();
        System.out.println(xml);
        WCSubscriptionStatusData objOut = (WCSubscriptionStatusData) context.createUnmarshaller().unmarshal(new StringReader(xml));

        assertTrue(item1.getKey().equals(objOut.getData().get(0).getKey()));
        assertTrue(item1.getLabel().equals(objOut.getData().get(0).getLabel()));
        assertTrue(item1.getValue().equals(objOut.getData().get(0).getValue()));
    }
}

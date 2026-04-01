package com.intuit.ems.payroll.psp.gateways.ers;

import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.developer.JAXWSProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.ws.BindingProvider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 20, 2010
 * Time: 1:32:48 PM
 */
public class ERSGatewayTests {
    @Before
    public void beforeEachTest() {

    }

    @After
    public void afterEachTest() {
        ERSGatewayFactory.setInstanceClass(ERSGateway.class);
    }

    @Test
    @Ignore
    public void testActivateEntitlement_RequestTimeout() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        // set the request timeout to 5ms - this will cause a web service exception caused by a socket timeout
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 5);
        try {
            ersGateway.activateEntitlement("test123", "testEOC", "testEIN", false, null);
            fail("WebServiceException not encountered");
        } catch (Throwable t) {
            t.printStackTrace();
            assertEquals("wrong exception caught" + t.getMessage(), ERSConnectionException.class, t.getClass());
        }
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 30000);
    }

    @Test
    @Ignore
    public void testActivateEntitlement_ConnectionTimeout() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        // set the connection timeout to 5ms - this will cause a client transport exception caused by a socket timeout
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, 5);
        try {
            ersGateway.activateEntitlement("test123", "testEOC", "testEIN", false, null);
            fail("ClientTransportException not encountered");
        } catch (Throwable t) {
            t.printStackTrace();
            assertEquals("wrong exception caught", ERSConnectionException.class, t.getClass());
        }
    }

    @Test
    @Ignore
    public void testDisableEntitlement_RequestTimeout() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        // set the request timeout to 5ms - this will cause a web service exception caused by a socket timeout
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 5);
        try {
            ersGateway.disableEntitlement("test123", "123456", null);
            fail("WebServiceException not encountered");
        } catch (Throwable t) {
            t.printStackTrace();
            assertEquals("wrong exception caught" + t.getMessage(), ERSConnectionException.class, t.getClass());
        }
    }

    @Test @Ignore
    public void testDisableEntitlement_ConnectionTimeout() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        // set the connection timeout to 5ms - this will cause a client transport exception caused by a socket timeout
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, 5);
        try {
            ersGateway.disableEntitlement("test123", "123456", null);
            fail("ClientTransportException not encountered");
        } catch (Throwable t) {
            t.printStackTrace();
            assertEquals("wrong exception caught", ERSConnectionException.class, t.getClass());
        }
    }

    //test to verify that able to hit new .net PA+ end-point
    @Test
    public void testgetEntitlementInfo() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 50000);
        try {
            EntitlementInfoDTO ersResponse=ersGateway.getEntitlementInfo("237800157862085","298555",true, null );
            assertEquals(23273721,ersResponse.getCustomerId() );
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    public void testActivateEntitlement() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 50000);
        try {
            ersGateway.activateEntitlement("237800157862085", "298555","",false,null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Test
    public void testDisableEntitlement() {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();

        // set the request timeout to 5ms - this will cause a web service exception caused by a socket timeout
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 50000);
        try {
            ersGateway.disableEntitlement("341274576250684", "087689" , null);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    @Test
    public void testDeactivateEntitlementUnit()
    {
        IERSGateway ersGateway = ERSGatewayFactory.createInstance();
        ((BindingProvider) ((ERSGateway)ersGateway).getSOAPPort()).getRequestContext().put(JAXWSProperties.REQUEST_TIMEOUT, 50000);
        try
        {
            ersGateway.deactivateEntitlementUnit("701762515857391", "298555", "436956755",null);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
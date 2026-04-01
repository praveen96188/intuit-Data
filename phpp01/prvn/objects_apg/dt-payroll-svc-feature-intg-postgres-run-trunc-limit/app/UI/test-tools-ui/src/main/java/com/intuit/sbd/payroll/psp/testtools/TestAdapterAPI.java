package com.intuit.sbd.payroll.psp.testtools;

import com.intuit.sbd.payroll.psp.webservices.client.*;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;

import javax.xml.namespace.QName;
import java.net.URL;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author Wiktor Kozlik
 */
public class TestAdapterAPI {
    private static final String EWS_VERSION = "v1_10";
    private static final String EWS_ADAPTER = "EWSAdapter";

    private static BatchJobsWS batchJobsWS;
    private static CompanyWS companyWS;
    private static OffloadGroupWS offloadGroupWS;
    private static PSPDateWS pspDateWS;
    private static SourceSystemWS sourceSystemWS;
    private static TestDataWS testDataWS;
    private static TransactionsWS transactionsWS;
    private static EDIPaymentsWS ediPaymentsWS;
    private static EntitlementWS entitlementWS;
    private static EWSAdapter ewsAdapter;

    private static String testToolsWsUrl;
    private static String ewsAdapterUrl;

    public static BatchJobsWS getBatchJobsWS() {
        if (batchJobsWS == null) {
            batchJobsWS = (BatchJobsWS) getServicePort("BatchJobsWS");
        }
        return batchJobsWS;
    }

    public static CompanyWS getCompanyWS() {
        if (companyWS == null) {
            companyWS = (CompanyWS) getServicePort("CompanyWS");
        }
        return companyWS;
    }

    public static PSPDateWS getPSPDateWS() {
        if (pspDateWS == null) {
            pspDateWS = (PSPDateWS) getServicePort("PSPDateWS");
        }
        return pspDateWS;
    }

    public static SourceSystemWS getSourceSystemWS() {
        if (sourceSystemWS == null) {
            sourceSystemWS = (SourceSystemWS) getServicePort("SourceSystemWS");
        }
        return sourceSystemWS;
    }

    public static TestDataWS getTestDataWS() {
        if (testDataWS == null) {
            testDataWS = (TestDataWS) getServicePort("TestDataWS");
        }
        return testDataWS;
    }

    public static TransactionsWS getTransactionsWS() {
        if (transactionsWS == null) {
            transactionsWS = (TransactionsWS) getServicePort("TransactionsWS");
        }
        return transactionsWS;
    }

    public static OffloadGroupWS getOffloadGroupWS() {
        if (offloadGroupWS == null) {
            offloadGroupWS = (OffloadGroupWS) getServicePort("OffloadGroupWS");
        }
        return offloadGroupWS;
    }

    public static EDIPaymentsWS getEdiPaymentsWS() {
        if (ediPaymentsWS == null) {
            ediPaymentsWS = (EDIPaymentsWS) getServicePort("EDIPaymentsWS");
        }
        return ediPaymentsWS;
    }

    public static EntitlementWS getEntitlementWS() {
        if (entitlementWS == null) {
            entitlementWS = (EntitlementWS) getServicePort("EntitlementWS");
        }
        return entitlementWS;
    }

    public static EWSAdapter getEwsAdapter() {
        if (ewsAdapter == null) {
            ewsAdapter = (EWSAdapter) getServicePort(EWS_ADAPTER);
        }
        return ewsAdapter;
    }

    private static URL getURL(String pWebServiceName) throws java.lang.Throwable {
        String webServiceClassName = "com.intuit.sbd.payroll.psp.webservices.client." + pWebServiceName + "Service";
        Class webServiceClass = Class.forName(webServiceClassName);
        URL baseUrl = webServiceClass.getResource(".");
        if(EWS_ADAPTER.equals(pWebServiceName)) {
            return new URL(baseUrl, getTestToolsWsUrl(pWebServiceName) + "/" + pWebServiceName + "/" + EWS_VERSION + "?wsdl");
        } else {
            return new URL(baseUrl, getTestToolsWsUrl(pWebServiceName) + "/" + pWebServiceName + "?wsdl");
        }
    }

    private static QName getQName(String pWebServiceName) {
        if(EWS_ADAPTER.equals(pWebServiceName)) {
            return new QName("http://webservices." + EWS_VERSION + ".ews.adapters.psp.payroll.sbd.intuit.com/", pWebServiceName + "Service");
        } else {
            return new QName("http://webservices.psp.payroll.sbd.intuit.com/", pWebServiceName + "Service");
        }

    }

    private static Object getServicePort(String pWebServiceName) {
        try {
            String webServiceClassName = "com.intuit.sbd.payroll.psp.webservices.client." + pWebServiceName + "Service";
            Class cls = Class.forName(webServiceClassName);
            Constructor ct = cls.getDeclaredConstructor(URL.class, QName.class);
            ct.setAccessible(true);
            Object retobj = ct.newInstance(getURL(pWebServiceName), getQName(pWebServiceName));
            Method method = cls.getDeclaredMethod("get" + pWebServiceName + "Port");
            return method.invoke(retobj);
        }
        catch (java.lang.Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getTestToolsWsUrl(String pWebServiceName) {
        if(EWS_ADAPTER.equals(pWebServiceName)) {
            if(ewsAdapterUrl == null) {
                // Re-use the WS server url for test tools UI
                String ddRepUiUrl = ConfigurationManager.getSettingValue(ConfigurationModule.DDRepUI, "ews.url");
                ewsAdapterUrl = ddRepUiUrl.replaceFirst("ddrepui-ws", "EWSAdapter/services");
                System.out.println("test-ws.url=" + ewsAdapterUrl);
            }
            return ewsAdapterUrl;
        }
        else {
            if(testToolsWsUrl == null) {
                // Re-use the WS server url for test tools UI
                String ddRepUiUrl = ConfigurationManager.getSettingValue(ConfigurationModule.DDRepUI, "ddrepui-ws.url");
                testToolsWsUrl = ddRepUiUrl.replaceFirst("ddrepui-ws", "test-ws/services");
                System.out.println("test-ws.url=" + testToolsWsUrl);
            }
            return testToolsWsUrl;
        }
    }
}

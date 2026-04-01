package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import flex.messaging.messages.RemotingMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

public class SAPJavaAdapterTest {

    @BeforeClass
    public static void runBeforeAllTests() {
        PayrollServicesTest.beforeEachTest();
        ApplicationSecondary.initialize();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        ApplicationSecondary.initialize();
    }

    @Before
    public void runBeforeEachTest() {
        ApplicationSecondary.beginUnitOfWork();
        ApplicationSecondary.truncateTables();
        ApplicationSecondary.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        ApplicationSecondary.beginUnitOfWork();
        ApplicationSecondary.truncateTables();
        ApplicationSecondary.commitUnitOfWork();
    }

    @Test
    public void recordInvocationTest(){

        RemotingMessage remotingMessage = new RemotingMessage();
        remotingMessage.setSource(null);
        remotingMessage.setOperation("getQBDTTokens");
        remotingMessage.setParameters(Arrays.asList("QBDT", "999006429"));
        remotingMessage.setRemoteUsername(null);
        remotingMessage.setRemotePassword(null);
        remotingMessage.setClientId("10214365-EB0E-B914-68C5-9718CA747066");
        remotingMessage.setDestination("taxservice");
        remotingMessage.setMessageId("96F3910E-5F7D-536C-BA3A-2EC0B3E7A92A");
        remotingMessage.setTimestamp(new Long(1641463018342L));
        remotingMessage.setTimeToLive(0);

        remotingMessage.setHeader("authorizationToken", "b4aa8544-9c23-4963-b01c-96441f9489da");
        remotingMessage.setHeader("realmId", "50000000");
        remotingMessage.setHeader("corpId", "50002339093");
        remotingMessage.setHeader("ticket", "V1-10-X3svu0vtsfpccjvpy6l1m9");
        remotingMessage.setHeader("timezoneOffset", "-330");
        remotingMessage.setHeader("DSEndpoint", "sap-secure-http");
        remotingMessage.setHeader("DSId", "10212C34-6B0A-9CCF-A476-8E51FCBC91B6");
        remotingMessage.setHeader("screenPath", "/Company/QBDT:999006429/Info/Company-Information//");
        remotingMessage.setHeader("authId", "9130356079302156");

        SAPJavaAdapter sapJavaAdapter = new SAPJavaAdapter();

        SAPJavaAdapter.CallTracker callTracker = sapJavaAdapter.new CallTracker(true);

        callTracker.invocationBegin(remotingMessage,"Akshit Agarwal","b4aa8544-9c23-4963-b01c-96441f9489da");

        callTracker.invocationComplete(null);

        PayrollServicesTest.validateSapMethodCall(PayrollServicesTest.getSapMethodCall("/Company/QBDT:999006429/Info/Company-Information//", "taxservice",null,"getQBDTTokens","QBDT,999006429"));

    }

}
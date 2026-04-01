package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.AuthenticateRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.AuthenticateResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexUnitDataLoaderService;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPGetAgencyRulesTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPAuthenticationTests {
    @Test
    public void testUserLoginSuccess() {
        try {
            FlexUnitDataLoaderService.AddUsers();

            DISAdapter disAdapter = new DISAdapter();
            AuthenticateRequestDISDTO request = new AuthenticateRequestDISDTO();
            request.setUsername("AutoLogin");
            request.setPassword("Admin");

            AuthenticateResponseDISDTO response = disAdapter.Authenticate(request);
            TestHelper.verifyFailure(response.getDisResponse());
            System.out.println("ERR: " + response.getDisResponse().getDisResponseMessageDISDTO().get(0).getMessage());

        } catch (Throwable t) {
            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(t.getMessage());
        }
    }

//    @Test
//    public void testUserLoginAgainVerifySameToken() {
//        try {
//            FlexUnitDataLoaderService.AddUsers();
//
//            DISAdapter disAdapter = new DISAdapter();
//            AuthenticateRequestDISDTO request = new AuthenticateRequestDISDTO();
//            request.setUsername("AutoLogin");
//            request.setPassword("Admin");
//
//            AuthenticateResponseDISDTO response = disAdapter.Authenticate(request);
//            TestHelper.verifySuccess(response.getDisResponse());
//            Assert.assertNotNull(response.getToken());
//            String originalToken = response.getToken();
//
//            response = disAdapter.Authenticate(request);
//            TestHelper.verifySuccess(response.getDisResponse());
//            Assert.assertNotNull(response.getToken());
//            String secondToken = response.getToken();
//
//            TestCase.assertEquals(originalToken, secondToken);
//
//        } catch (Throwable t) {
//            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            TestCase.fail(t.getMessage());
//        }
//    }
//
//    @Test
//    public void testUserLoginExpired() {
//        try {
//            FlexUnitDataLoaderService.AddUsers();
//
//            DISAdapter disAdapter = new DISAdapter();
//            AuthenticateRequestDISDTO request = new AuthenticateRequestDISDTO();
//            request.setUsername("AutoLogin");
//            request.setPassword("Admin");
//
//            AuthenticateResponseDISDTO response = disAdapter.Authenticate(request);
//            TestHelper.verifySuccess(response.getDisResponse());
//            Assert.assertNotNull(response.getToken());
//            String originalToken = response.getToken();
//
//            PayrollServices.beginUnitOfWork();
//            SpcfCalendar spcfCalendar = SpcfCalendar.getNow();
//            spcfCalendar.addDays(3);
//            PSPDate.setPSPTime(spcfCalendar);
//            PayrollServices.commitUnitOfWork();
//
//            response = disAdapter.Authenticate(request);
//            TestHelper.verifySuccess(response.getDisResponse());
//            Assert.assertNotNull(response.getToken());
//            String secondToken = response.getToken();
//
//            TestCase.assertNotSame(originalToken,secondToken);
//
//        } catch (Throwable t) {
//            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            TestCase.fail(t.getMessage());
//        }
//    }
//
//    @Test
//    public void testUserLoginInvalidPassword() {
//        try {
//            DISAdapter disAdapter = new DISAdapter();
//            AuthenticateRequestDISDTO request = new AuthenticateRequestDISDTO();
//            request.setUsername("automatedtaxforms");
//            request.setPassword("blah");
//
//            AuthenticateResponseDISDTO response = disAdapter.Authenticate(request);
//            TestHelper.verifyFailure(response.getDisResponse());
//
//        } catch (Throwable t) {
//            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            TestCase.fail(t.getMessage());
//        }
//    }
//
//    @Test
//    public void testUserDNE() {
//        try {
//            DISAdapter disAdapter = new DISAdapter();
//            AuthenticateRequestDISDTO request = new AuthenticateRequestDISDTO();
//            request.setUsername("blah");
//            request.setPassword("blah");
//
//            AuthenticateResponseDISDTO response = disAdapter.Authenticate(request);
//            TestHelper.verifyFailure(response.getDisResponse());
//
//        } catch (Throwable t) {
//            t.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            TestCase.fail(t.getMessage());
//        }
//    }

}

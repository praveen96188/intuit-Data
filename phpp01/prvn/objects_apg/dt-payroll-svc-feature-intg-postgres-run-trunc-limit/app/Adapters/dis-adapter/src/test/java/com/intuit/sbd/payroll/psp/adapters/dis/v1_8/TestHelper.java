package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.ResponseStatusEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.DISResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AuthAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/TestHelper.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class TestHelper {
    public static void verifyDISResponse(DISMessage pExpectedDISMessages, DISResponseDISDTO pDISResponseDISDTO) {
        Assert.assertEquals(ResponseStatusEnum.Failure, pDISResponseDISDTO.getStatus());
        Assert.assertEquals(1, pDISResponseDISDTO.getDisResponseMessageDISDTO().size());
        Assert.assertEquals("DIS-" + pExpectedDISMessages.getCode(), pDISResponseDISDTO.getDisResponseMessageDISDTO().get(0).getCode());
        Assert.assertEquals(pExpectedDISMessages.getMessage(), pDISResponseDISDTO.getDisResponseMessageDISDTO().get(0).getMessage());
    }

    public static void verifySuccess(DISResponseDISDTO pDISResponseDISDTO) {
        if (!ResponseStatusEnum.Success.equals(pDISResponseDISDTO.getStatus())) {
            if (pDISResponseDISDTO.getDisResponseMessageDISDTO() != null && pDISResponseDISDTO.getDisResponseMessageDISDTO().size() > 0) {
                TestCase.fail(pDISResponseDISDTO.getDisResponseMessageDISDTO().get(0).getMessage());
            } else {
                TestCase.fail("DIS Response expected success but was failed");
            }
        }
    }

    public static void verifyFailure(DISResponseDISDTO pDISResponseDISDTO) {
        Assert.assertEquals(ResponseStatusEnum.Failure, pDISResponseDISDTO.getStatus());
    }

    public static SAPUser loginAdminUser() {
        String user = "AutoLogin";
        String password = "Admin";
        return loginUser(user,password);
    }

    public static SAPUser loginSalesUser() {
        String user = "AutoLogin";
        String password = "salesagent";
        return loginUser(user,password);
    }

    public static SAPUser loginUser(String pUsername,String pPassword) {
        try {
            AuthAdapter authAdapter = new AuthAdapter();
            SAPUser sapUser = authAdapter.login(pUsername,pPassword,false);
            return sapUser;
        } catch (Throwable pThrowable) {
            TestCase.fail("Could not create user "+pUsername+".");
            return null;
        }
    }

}

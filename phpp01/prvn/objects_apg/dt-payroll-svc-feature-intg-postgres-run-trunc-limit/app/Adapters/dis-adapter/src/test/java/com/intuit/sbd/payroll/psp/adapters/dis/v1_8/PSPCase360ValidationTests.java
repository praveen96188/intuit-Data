package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.api.dtos.PayrollFrequencyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPCase360ValidationTests.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/15 17:18:20 $
 * $Author: JChickanosky $
 */
public class PSPCase360ValidationTests {
    @Test
    // This test is here so if an enumeration any of the DISAdapter WS utilizes change,
    //     we are proactively notified.
    public void validatedEnumsHaveNotChanged() {
        assertEquals(3, BankAccountStatus.values().length);
        assertEquals(2, BankAccountType.values().length);
        assertEquals(2, CommunicationType.values().length);
        assertEquals(2, CompanyEventStatus.values().length);
        assertEquals(4, ContactRole.values().length);
        assertEquals(16, LawCategoryCode.values().length);
        assertEquals(8, PayrollFrequencyDTO.values().length);
        assertEquals(12, ServiceCode.values().length);
        assertEquals(5, ServiceStatusCode.values().length);
        assertEquals(29, ServiceSubStatusCode.values().length);
    }

}

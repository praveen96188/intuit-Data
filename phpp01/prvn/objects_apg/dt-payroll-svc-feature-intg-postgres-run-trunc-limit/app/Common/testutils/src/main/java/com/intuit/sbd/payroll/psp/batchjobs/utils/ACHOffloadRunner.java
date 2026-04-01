package com.intuit.sbd.payroll.psp.batchjobs.utils;

import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: rnorian
 * Date: Oct 5, 2009
 * Time: 10:02:58 AM
 */
public class ACHOffloadRunner {
    public static void runAchOffload(String dateYYYYMMDD) {
        runAchOffload(dateYYYYMMDD, 0);
    }

    public static void runAchOffload(SpcfCalendar pInitiationDate) {
        runAchOffload(pInitiationDate, 0);
    }

    public static void runAchOffload(String dateYYYYMMDD, int daysBack) {
        if (dateYYYYMMDD != null)
            dateYYYYMMDD = dateYYYYMMDD.replace("-", "");
        runAchOffload(CalendarUtils.createInstanceFromDate(dateYYYYMMDD), daysBack);
    }

    public static void runAchOffload(SpcfCalendar pInitiationDate, int daysBack) {
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        SpcfCalendar offloadDate = pInitiationDate.copy();
        offloadDate.setValues(offloadDate.getYear(), offloadDate.getMonth(), offloadDate.getDay(), 17, 5, 0, 0);

        // add an extra day back because '2 days back of Friday is Wednesday' in most people's minds
        CalendarUtils.addBusinessDays(offloadDate, (daysBack + 1) * -1);
        for (int i = 0; i <= daysBack; i++) {
            PayrollServices.beginUnitOfWork();
            CalendarUtils.addBusinessDays(offloadDate, 1);
            PSPDate.setPSPTime(offloadDate);
            PayrollServices.commitUnitOfWork();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, offloadDate);
        }
    }
}

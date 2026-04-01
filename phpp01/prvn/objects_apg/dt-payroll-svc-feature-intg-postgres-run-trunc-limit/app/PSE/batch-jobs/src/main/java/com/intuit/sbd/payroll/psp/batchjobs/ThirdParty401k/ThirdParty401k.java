package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.regex.Pattern;
import java.math.BigDecimal;

/**
    @author Jeff Jones
 */
public class ThirdParty401k {
    private static final SpcfLogger logger = PayrollServices.getLogger(ThirdParty401k.class);

    public static SpcfCalendar get401kCheckDate(int pValidationOffset) {
        // TODO: Check to see if this needs to be removed
        int waitPeriod = SourcePayrollParameter.findIntValue(SourceSystemCode.QBDT,
                                                             SourcePayrollParameterCode.ThirdParty401kOffloadWaitPeriod);
        waitPeriod *= -1;

        SpcfCalendar cal = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(cal, pValidationOffset);
        CalendarUtils.addBusinessDays(cal, waitPeriod);
        CalendarUtils.clearTime(cal);
        return cal;
    }

    static final Pattern cutOffTimePattern = Pattern.compile("([0-2]?[0-9]:[0-5][0-9]).*");

    /**
     * Updates the a newly update employee's data
     * @param employee The newly updated employee
     */
    public static void updateEmployee401K(Employee employee) {
        DomainEntitySet<Paycheck> paychecks = Paycheck.findNonFinalTP401kPaychecks(employee);
        for (Paycheck paycheck : paychecks) {
            // See if this paycheck's status needs to be updated now that the employee was updated
            ThirdParty401kPaycheck k401Paycheck = paycheck.getThirdParty401kPaycheck();
            if (k401Paycheck == null) {
                continue;
            }
            k401Paycheck.updatePaycheckStateCode();
        }
    }

}

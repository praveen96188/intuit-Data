package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation to reEngage employees previously invited by ER
 */
@Service
public class SendReEngageERInvites extends SendWorkforceInvitesAbstract implements SendWorkforceInvites {

    //cooling period- we do not send the invites if employee has been invited within this many number of days
    private static final int latestInviteDateDefault = 30;
    //The maximum number of previous bulk invites allowed for eligible employees, we don't want to continuously reEngage
    private static final int maxPreviousBulkInviteLimit = 1;

    @Override
    public List<SpcfUniqueId> getEmployeesForInvitationMode(SpcfUniqueId companyId, Map<String, Object> queryParams) {
        int settlementDateDuration = (int) queryParams.get("settlementDateDuration");
        int lastPaidDurationEmployee = (int) queryParams.get("lastPaidDurationEmployee");
        boolean isDDQuery = (boolean) queryParams.get("isDDQuery");

        int latestInviteDate = SystemParameter.findIntValue(SystemParameter.Code.WORKFORCE_LATEST_INVITE_DATE, latestInviteDateDefault);

        List<SpcfUniqueId> employeeIdList= Employee.getWorkforceReInviteEmployeeListForCompany(companyId, lastPaidDurationEmployee,
                settlementDateDuration, isDDQuery, true, latestInviteDate, maxPreviousBulkInviteLimit);
        return employeeIdList;
    }

    @Override
    public String getInvitationSource() {
        return InvitationMode.ReEngageERInvited.getInvitationSource();
    }


    @Override
    public InvitationMode getInvitationMode() {
        return InvitationMode.ReEngageERInvited;
    }
}

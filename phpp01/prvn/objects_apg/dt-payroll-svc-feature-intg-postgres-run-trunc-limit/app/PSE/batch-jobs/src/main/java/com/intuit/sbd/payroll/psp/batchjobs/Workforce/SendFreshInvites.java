package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Implementation to invite previously uninvited employees
 */
@Service
public class SendFreshInvites extends SendWorkforceInvitesAbstract implements SendWorkforceInvites {

    @Override
    public List<SpcfUniqueId> getEmployeesForInvitationMode(SpcfUniqueId companyId, Map<String, Object> queryParams) {
        // retrieving params from map
        int settlementDateDuration = (int) queryParams.get("settlementDateDuration");
        int lastPaidDurationEmployee = (int) queryParams.get("lastPaidDurationEmployee");
        boolean isDDQuery = (boolean) queryParams.get("isDDQuery");

        List<SpcfUniqueId> employeeIDList = Employee.findWorkforceEligibleEmployees(companyId.toString(),
                lastPaidDurationEmployee, settlementDateDuration,isDDQuery);
        return employeeIDList;
    }


    @Override
    public String getInvitationSource() {
        return InvitationMode.FreshInvites.getInvitationSource();
    }

    @Override
    public InvitationMode getInvitationMode() {
        return InvitationMode.FreshInvites;
    }
}

package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.http.MethodNotSupportedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SendInvitesToNewEmployees  extends SendWorkforceInvitesAbstract implements SendWorkforceInvites{

    @Override
    public List<SpcfUniqueId> getEmployeesForInvitationMode(SpcfUniqueId companyId, Map<String, Object> queryParams)  {
        throw new RuntimeException("MethodNotAllowed");
    }


    @Override
    public String getInvitationSource() {
        return InvitationMode.NewInvitesOnEmployeeAdd.getInvitationSource();
    }

    @Override
    public InvitationMode getInvitationMode() {
        return InvitationMode.NewInvitesOnEmployeeAdd;
    }
}

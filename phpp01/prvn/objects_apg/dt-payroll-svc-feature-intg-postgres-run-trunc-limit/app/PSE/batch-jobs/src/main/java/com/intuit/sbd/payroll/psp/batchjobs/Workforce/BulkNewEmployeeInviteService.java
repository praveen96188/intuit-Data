package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BulkNewEmployeeInviteService {

    private CompanyWorkforceInviteService companyWorkforceInviteService;
    private static final Logger logger = LoggerFactory.getLogger(BulkNewEmployeeInviteService.class);

    @Autowired
    public BulkNewEmployeeInviteService(CompanyWorkforceInviteService companyWorkforceInviteService){
        this.companyWorkforceInviteService = companyWorkforceInviteService;
    }

    public void sendInvite(Integer chunkSize, Map<InvitationMode, List<String>> invitationModeEmailTemplatesListMap,
                           CompanyEvent companyEvent) {
        if (companyEvent.getCompany() == null) {
            logger.error("action=sendInvitesToNewEmployees Exception=Company_NULL, PSID={}", companyEvent.getCompany().getSourceCompanyId());
            return;
        }

        List<SpcfUniqueId> employeeIdList = new ArrayList<>();
        String value = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId);
        List<String> employeeRecNums = Arrays.asList(value.split(","));
        Expression<QbdtEmployeeInfo> query =
                new Query<QbdtEmployeeInfo>().Select(QbdtEmployeeInfo.Employee())
                        .Where((QbdtEmployeeInfo.ListId().in(employeeRecNums)).
                                And(QbdtEmployeeInfo.Company().equalTo(companyEvent.getCompany())));
        ScrollableResults employeeScrollableResults = Application.findScrollable(QbdtEmployeeInfo.class, query);
        while (employeeScrollableResults.next()) {
            Employee employee = (Employee) employeeScrollableResults.get(0);
            if (employee.getPersonaId() == null) {
                SpcfUniqueId employeeId = employee.getId();
                employeeIdList.add(employeeId);
            } else {
                logger.info("action=sendInvitesToNewEmployees ,workflow=employeeSyncedSendInvite, status=employeeAlreadySentInvite employeeId={}", employee.getId());
            }
        }
        int totalInvites = 0;
        if (employeeIdList.size() > 0) {
            totalInvites = companyWorkforceInviteService.inviteCompany(companyEvent.getCompany().getId(), chunkSize, invitationModeEmailTemplatesListMap, employeeIdList);
        }
        updateCompanyEvent(companyEvent.getId(), employeeIdList, employeeRecNums, totalInvites);
    }

    private static void updateCompanyEvent(SpcfUniqueId companyEventId, List<SpcfUniqueId> employeeIdList, List<String> employeeRecNums, int totalInvites) {
            int MAX_RETRY = SystemParameter.findIntValue(SystemParameter.Code.WORKFORCE_INVITE_MAX_RETRY, 1);
            CompanyEvent companyEvent = Application.findById(CompanyEvent.class, companyEventId);
            if (totalInvites > 0) {
                companyEvent.setStatusCd(CompanyEventStatus.Inactive);
                logger.info("action=sendInvitesToNewEmployees ,workflow=employeeSyncedSendInvite , PSID={} , totalEmployeesRequested={} , totalEmployeessynced={} , totalInvitesSent={}" +
                                ", unsyncedEmployees={} , errorInInvites={}", companyEvent.getCompany().getSourceCompanyId(), employeeRecNums.size(), employeeIdList.size(), totalInvites,
                        employeeRecNums.size() - employeeIdList.size(), employeeIdList.size() - totalInvites);
            }
            if (employeeIdList.size() == 0 || totalInvites == 0) {
                logger.info("action=sendInvitesToNewEmployees workflow=employeesNotSynced , PSID={}", companyEvent.getCompany().getSourceCompanyId());
                DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.Details);
                if (!Objects.isNull(companyEventDetails) && companyEventDetails.size() == 1) {
                    CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
                    int retryCount = Integer.parseInt(companyEventDetail.getValue());
                    if (retryCount >= MAX_RETRY) {
                        companyEvent.setStatusCd(CompanyEventStatus.Inactive);
                        logger.info("action=sendInvitesToNewEmployees workflow=employeesNotSynced , PSID={} , retryCount={}, companyEventStatus=Inactive", companyEvent.getCompany().getSourceCompanyId(), retryCount);
                    }
                    companyEventDetail.setValue(String.valueOf(retryCount + 1));
                    logger.info("action=sendInvitesToNewEmployees workflow=employeesNotSynced , PSID={} , retryCount={}", companyEvent.getCompany().getSourceCompanyId(), retryCount);
                }
            }
    }

}

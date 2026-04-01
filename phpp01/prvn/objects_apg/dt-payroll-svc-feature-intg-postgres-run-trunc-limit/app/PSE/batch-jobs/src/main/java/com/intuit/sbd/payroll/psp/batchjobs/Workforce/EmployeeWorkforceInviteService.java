package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeWorkforceInviteService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeWorkforceInviteService.class);

    public Integer inviteEmployeeChunk(Company company, List<SpcfUniqueId> employeeIdList, IAMTicket iamTicket,
                                           Integer chunkSize, boolean isResend, String emailTemplateName, String invitationSource) {
        Integer inviteCount = 0;
        boolean hasEarlierSession = Application.hasActiveTransaction();

        List<List<SpcfUniqueId>> partitionedEmployeeIdList = ListUtils.partition(employeeIdList, chunkSize);
        for(List<SpcfUniqueId> partitionedEmployeeIdSubList : partitionedEmployeeIdList) {
            try {
                if(!hasEarlierSession){
                    Application.beginUnitOfWork();
                }
                ProcessResult<Integer> chunkResult = PayrollServices.employeeManager.employeeChunkInvite(partitionedEmployeeIdSubList,
                        company, iamTicket, isResend, emailTemplateName, invitationSource);
                inviteCount += chunkResult.getResult();
                if(!hasEarlierSession) {
                    Application.commitUnitOfWork();
                }
            } catch (Exception e) {
                logger.error("Invitation Error CompanyId={} Exception={}", company.getSourceCompanyId(), e.getMessage(), e);
            } finally {
                if(!hasEarlierSession) {
                    Application.rollbackUnitOfWork();
                }
            }
        }
        return inviteCount;
    }
}

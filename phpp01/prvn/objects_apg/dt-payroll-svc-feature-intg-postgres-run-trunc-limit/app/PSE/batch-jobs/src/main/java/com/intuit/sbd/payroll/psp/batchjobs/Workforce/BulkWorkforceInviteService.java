package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class BulkWorkforceInviteService {

    @Autowired
    private CompanyWorkforceInviteService companyWorkforceInviteService;

    @Async("inviteThreadPoolExecutor")
    public CompletableFuture<Integer> asyncInviteCompanies(List<SpcfUniqueId> partitionCompanyIdSubList, Integer chunkSize,
                                                           boolean isResend, Map<InvitationMode, List<String>> invitationModeEmailTemplatesListMap,
                                                           Map<String,Object> queryParams) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BulkWorkforceInviteProcessor));
        Integer inviteCount = inviteCompaniesBatch(partitionCompanyIdSubList, chunkSize, isResend,
                invitationModeEmailTemplatesListMap, queryParams);
        return CompletableFuture.completedFuture(inviteCount);
    }

    public Integer inviteCompaniesBatch(List<SpcfUniqueId> partitionCompanyIdSubList, Integer chunkSize, boolean isResend,
                                        Map<InvitationMode, List<String>> invitationModeEmailTemplatesListMap, Map<String, Object> queryParams) {
        int inviteCount = 0;
        for(SpcfUniqueId companyId : partitionCompanyIdSubList) {
            inviteCount += companyWorkforceInviteService.inviteCompany(companyId, chunkSize, isResend,
                    invitationModeEmailTemplatesListMap, queryParams);
        }

        return inviteCount;
    }
}

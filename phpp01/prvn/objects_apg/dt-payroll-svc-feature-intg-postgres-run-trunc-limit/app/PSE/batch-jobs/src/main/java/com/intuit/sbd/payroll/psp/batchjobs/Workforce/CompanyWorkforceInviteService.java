package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompanyWorkforceInviteService {

    @Autowired
    private SendFreshInvites sendFreshInvites;

    @Autowired
    private SendReEngageAutoInvites sendReEngageAutoInvites;

    @Autowired
    private SendReEngageERInvites sendReEngageERInvites;

    @Autowired
    private SendInvitesToNewEmployees sendInvitesToNewEmployees;


    private static final Logger logger = LoggerFactory.getLogger(CompanyWorkforceInviteService.class);

    /**
     * Sends workforce invites for a company
     * @param companyId companyId(company_seq in DB) for the company for which invites have to be sent
     * @param chunkSize max commit unit of work (we make a commit after each company, and also commit after each after each employeeChunk of size chunkSize
     * @param isResend should invites be sent using v1/remind api instead of v1/invite
     * @param invitationModeEmailTemplatesListMap Map containing the invitation modes requested and the list of emailTemplates to be used with each mode
     * @param queryParams queryParams to retrieve eligible employees, currently all modes require int settlementDateDuration, int lastPaidDurationEmployee, boolean isDDQuery
     * @return
     */
    public int inviteCompany(SpcfUniqueId companyId, Integer chunkSize, boolean isResend, Map<InvitationMode, List<String>> invitationModeEmailTemplatesListMap, Map<String, Object> queryParams) {

        Company company = Application.findById(Company.class, companyId);
        if(Objects.isNull(company)) {
            logger.error("Error Retrieving Company CompanySeq={}", companyId);
            return 0;
        }
        logger.info("Retrieved Company CompanyId={}", company.getSourceCompanyId());

        int totalInviteCount = 0;
        boolean isError = false;

        Pair<Integer,Boolean> resultForInviteMode = new Pair<>(0,false);
        List<InvitationMode> invitationModes = new ArrayList<>(invitationModeEmailTemplatesListMap.keySet());
        for(InvitationMode invitationMode: invitationModes) {
            switch (invitationMode) {
                case FreshInvites:
                    resultForInviteMode = sendFreshInvites.sendWorkforceInvites(company,
                            invitationModeEmailTemplatesListMap.get(invitationMode), queryParams, chunkSize, isResend);
                    break;
                case ReEngageAutoInvited:
                    resultForInviteMode = sendReEngageAutoInvites.sendWorkforceInvites(company,
                            invitationModeEmailTemplatesListMap.get(invitationMode), queryParams, chunkSize, isResend);
                    break;
                case ReEngageERInvited:
                    resultForInviteMode = sendReEngageERInvites.sendWorkforceInvites(company,
                            invitationModeEmailTemplatesListMap.get(invitationMode), queryParams, chunkSize, isResend);
                    break;
            }
            totalInviteCount += resultForInviteMode.getKey();
            isError = isError || resultForInviteMode.getValue();
        }

        logger.info("inviteCompany result companyId={} isError={} totalInvites={}",
                company.getSourceCompanyId(), isError, totalInviteCount);

        //set the publish status for the company based on requested invitation modes and results
        setPublishStatusForCompany(companyId, isError, invitationModes);
        return totalInviteCount;
    }


    //sets the publish status of the company after it is processed
    private void setPublishStatusForCompany(SpcfUniqueId companyId, boolean isError,  List<InvitationMode> requestedInvitationModes) {
        try{
            Application.beginUnitOfWork();
            Company company = Application.findById(Company.class, companyId);
            PublishStatusWorkflowState publishStatus;

            //if we encountered an error in fresh/reInvites mark error
            if(isError) {
                publishStatus = PublishStatusWorkflowState.ERROR;
            } else if(sendReEngageERInvites.shouldSendInvitesForMode(requestedInvitationModes) || sendReEngageAutoInvites.shouldSendInvitesForMode(requestedInvitationModes)) {
                //if re-invitation was requested (for either ER/Bulk invited employee), mark as republish done
                publishStatus = PublishStatusWorkflowState.REPUBLISH_DONE;
            } else {
                //only fresh invites were requested, so mark in done
                publishStatus = PublishStatusWorkflowState.DONE;
            }

            company.setPublishStatusWorkflowState(CompanyPublishStatusWorkflows.WORKFORCE_INVITE, publishStatus);
            logger.info("Flag Set for CompanyId={} PublishStatus={}", company.getSourceCompanyId(), publishStatus.name());
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    public int inviteCompany(SpcfUniqueId companyId, Integer chunkSize, Map<InvitationMode, List<String>> invitationModeEmailTemplatesListMap,
                             List<SpcfUniqueId> employeeIdList) {
        InvitationMode invitationMode = InvitationMode.NewInvitesOnEmployeeAdd;
        Company company = Application.findById(Company.class, companyId);
        if (Objects.isNull(company)) {
            logger.error("Error Retrieving Company CompanySeq={}", companyId);
            return 0;
        }
        logger.info("Retrieved Company CompanyId={}", company.getSourceCompanyId());

        int totalInviteCount = 0;
        boolean isError = false;

        Pair<Integer, Boolean> resultForInviteMode = new Pair<>(0, false);
        resultForInviteMode = sendInvitesToNewEmployees.sendWorkforceInvites(company,
                invitationModeEmailTemplatesListMap.get(invitationMode), employeeIdList, chunkSize, false);
        totalInviteCount += resultForInviteMode.getKey();
        isError = isError || resultForInviteMode.getValue();

        logger.info("inviteCompany result companyId={} isError={} totalInvites={}",
                company.getSourceCompanyId(), isError, totalInviteCount);

        return totalInviteCount;
    }


}

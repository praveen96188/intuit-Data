package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

public interface SendWorkforceInvites {

    /**
     * gets the list of eligible employees that can be invited for a specific invitation mode
     * @param companyId company sequence of the company in DB
     * @param queryParams Map containing query params and their values
     * @return List of SpcfuniqueId of employees
     */
    public List<SpcfUniqueId> getEmployeesForInvitationMode(SpcfUniqueId companyId, Map<String, Object> queryParams);

    /**
     * gives the invitationSource that will be logged in the DB(company_event_details table) for given invitation mode
     * @return
     */
    public String getInvitationSource();

    Pair<Integer,Boolean> sendWorkforceInvites(Company company, List<String> emailTemplateList, List<SpcfUniqueId> employeeIdList,
                                               int chunkSize, boolean isResend);

    /**
     * Tells if invitations should be sent for a specific invitation mode
     * @param requestedInvitationModes list of invitation modes requested by the user
     * @return
     */
    public boolean shouldSendInvitesForMode(List<InvitationMode> requestedInvitationModes);

    /**
     * send invites for a company for specified invitation mode
     * @param company company
     * @param emailTemplateList list of email templates that should be used
     * @param queryParams map containing query params and values
     * @param chunkSize size at which empployee list will be partitioned, we also commit after each partition
     * @param isResend should invites be sent using IUS v1/remind api instead of v1/invite
     * @return A Pair having - Integer numberOfInvitesSentSuccefully, Boolean didSomeErrorOccurDuringInvitationProcess
     */
    public Pair<Integer,Boolean> sendWorkforceInvites(Company company, List<String> emailTemplateList, Map<String,Object> queryParams, int chunkSize, boolean isResend);

    /**
     * returns the invitation mode supported by the implementer/bean
     * @return
     */
    public InvitationMode getInvitationMode();



}

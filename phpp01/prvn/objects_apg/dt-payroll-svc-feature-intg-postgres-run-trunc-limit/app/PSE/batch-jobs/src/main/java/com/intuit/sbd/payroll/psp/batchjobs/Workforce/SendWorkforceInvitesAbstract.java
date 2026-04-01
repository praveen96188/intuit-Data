package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.gateways.iam.PSPRealmType;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.launchdarkly.shaded.com.google.common.base.Strings;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class SendWorkforceInvitesAbstract implements SendWorkforceInvites {

    @Autowired
    private EmployeeWorkforceInviteService employeeWorkforceInviteService;

    private static Pair<Integer,Boolean> defaultFailedResult = new Pair<>(0,true);

    protected static final Logger logger = LoggerFactory.getLogger(SendWorkforceInvitesAbstract.class);

    /**
     * This function returns a randomly picked emailTemplate from list of emailTemplates provided
     * Useful for doing A/B testing
     * @param emailTemplateNames
     * @return
     */
    protected String getRandomEmailTemplateNameFromList(List<String> emailTemplateNames) {
        //if only 1 template no need to generate a random number,
        //we have ensured that this set can never be empty
        if(emailTemplateNames.size()==1) {
            return emailTemplateNames.get(0);
        }
        Random rand = new Random();
        String rdmEmailTemplate = emailTemplateNames.get(rand.nextInt(emailTemplateNames.size()));

        return rdmEmailTemplate;
    }



    /**
     * Implementation for sending invites for the invitation mode. We break it into three step process
     * 1. retrieve eligible employees for the mode
     * 2. get emailTemplateName and invitationSource based for the invitation mode
     * 3. send the invites
     * @param company company
     * @param emailTemplateList list of email templates that should be used
     * @param queryParams map containing query params and values
     * @param chunkSize size at which empployee list will be partitioned, we also commit after each partition
     * @param isResend should invites be sent using IUS v1/remind api instead of v1/invite
     * @return
     */
    @Override
    public Pair<Integer,Boolean> sendWorkforceInvites(Company company, List<String> emailTemplateList, Map<String, Object> queryParams,
                                                      int chunkSize, boolean isResend) {
        Pair<Integer, Boolean> result;
        InvitationMode invitationModeRequested = getInvitationMode();

        List<SpcfUniqueId> employeeIdList;
        try {
            employeeIdList = getEmployeesForInvitationMode(company.getId(),queryParams);
            logger.info("Retrieved employee list for invites, companyId={} invitationMode={} eligibleEmployeesRetrieved={} ",company.getSourceCompanyId(),
                    invitationModeRequested, employeeIdList.size());
        } catch (Exception e) {
            logger.error("Error while retrieving the employee list companyId={} invitationMode={}, error={}",company.getSourceCompanyId(),
                    invitationModeRequested, e.getMessage(), e);
            //returning (0,true)
            return defaultFailedResult;
        }

        return sendWorkforceInvites(company,emailTemplateList,employeeIdList,chunkSize,isResend);
    }

    @Override
    public Pair<Integer,Boolean> sendWorkforceInvites(Company company, List<String> emailTemplateList, List<SpcfUniqueId> employeeIdList,
                                                      int chunkSize, boolean isResend) {
        Pair<Integer, Boolean> result;
        InvitationMode invitationModeRequested = getInvitationMode();
        //if no eligible employees return without proceeding further (empList also isn't null otherwise logging the size would have given an error)
        if(employeeIdList.isEmpty()) {
            result = new Pair<>(0, false);
            return result;
        }

        //get the IAMTicket
        IAMTicket iamTicket;
        try {
            iamTicket = getIAMTicketForRealm(company.getIAMRealmId());
        } catch (Exception e) {
            logger.error("Error while getting IAMTicket companyId={} invitationMode={}",company.getSourceCompanyId(), invitationModeRequested);
            //returning (0,true)
            return defaultFailedResult;
        }

        //This will pick a template at random from given list of templates, useful for A/B testing
        String emailTemplateName = getRandomEmailTemplateNameFromList(emailTemplateList);

        result = sendInvites(company, employeeIdList, iamTicket, chunkSize, isResend, emailTemplateName, getInvitationSource());
        logger.info("Invites sent companyId={} invitationMode={} emailTemplateName={} isError={} eligibleEmployeesForMode={} successfulInvitesForMode={}",
                company.getSourceCompanyId(), invitationModeRequested, emailTemplateName, result.getValue(), employeeIdList.size(), result.getKey());

        return result;
    }

    @Override
    public boolean shouldSendInvitesForMode(List<InvitationMode> requestedInvitationModes) {
        return requestedInvitationModes.contains(getInvitationMode());
    }

    //sends invite for the given list of employees
    private Pair<Integer, Boolean> sendInvites(Company company,List<SpcfUniqueId> employeeIdList, IAMTicket iamTicket, int chunkSize, boolean isResend,
                                               String emailTemplateName, String invitationSource) {
        boolean isError = false;

        //inviteEmployeeChunk already catches the exceptions so no need to do that here
        int inviteCount = employeeWorkforceInviteService.inviteEmployeeChunk(company, employeeIdList,
                iamTicket, chunkSize, isResend, emailTemplateName, invitationSource);
        if(inviteCount != employeeIdList.size()) {
            logger.error("Successful number of invites does not match the number of eligible employees");
            isError = true;
        }

        return new Pair<>(inviteCount, isError);

    }

    private IAMTicket getIAMTicketForRealm(String realmId) {
        String offlineTicket;
        if(Strings.isNullOrEmpty(realmId)){
            throw new IllegalArgumentException("Null or Empty RealmId");
        }
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_WORKFORCE_INVITE, true)){
            offlineTicket= PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class).getOfflineTicket(realmId);
            logger.info("AuthN: Identity 2 offline ticket received for Workforce Invite with RealmId={}",realmId);
        }else{
            String mPSPRealmId = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, PSPRealmType.CUSTOMER_WIDE_REALM.getValue());
            offlineTicket = OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP, mPSPRealmId, realmId);
            logger.info("AuthN: Identity 1 offline ticket received for Workforce Invite with RealmId={}",realmId);
        }

        if(!Strings.isNullOrEmpty(offlineTicket)){
            IAMTicket iamTicketForSystemUser = new IAMTicket();
            iamTicketForSystemUser.setTicket(offlineTicket);
            iamTicketForSystemUser.setRealmId(realmId);
            return iamTicketForSystemUser;
        }
        throw new IllegalArgumentException("IAMTicket RealmId=" + realmId);
    }


}

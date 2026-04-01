package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.platform.integration.ius.common.types.User;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailStatus;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.gateways.iam.ConsumerRealm;
import com.intuit.sbd.payroll.psp.gateways.iam.IDLMClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.IamUser;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.paycycle.util.StringUtil;

/*
 * This processor looks for CompanyEventEmails that need to be sent to IAM email addresses and looks up those addresses
 * and adds them to the parameters.
 */
public class IamEmailAddressProcessor extends BatchJobProcessor {

    private IDLMClientWrapper idlmClientWrapper;
    public IamEmailAddressProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
        idlmClientWrapper = PayrollApplicationBeanFactory.getBean(IDLMClientWrapper.class);
    }

    @Override
    protected void execute() {
        logger.info("Starting IamEmailAddress batch job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(new InsertEmailAddress());
        logger.info("Completed IamEmailAddress batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public class InsertEmailAddress extends BatchJobProcessorStep {
        @Override
        public void execute() {
            try {
                Application.beginUnitOfWork();
                //We create VmpPaystubNotification events in FormatError state because they don't have an email address to start
                Expression<CompanyEventEmail> query = new Query<CompanyEventEmail>()
                    .Where(CompanyEventEmail.EmailTemplateTypeCd().in(EventEmailTemplateTypeCode.VmpPaystubNotification)
                                            .And(CompanyEventEmail.StatusCd().in(EventEmailStatus.FormatError)));
                DomainEntitySet<CompanyEventEmail> companyEventEmails = Application.find(CompanyEventEmail.class, query);
                for(CompanyEventEmail companyEventEmail : companyEventEmails) {
                    try {
                        CompanyEvent companyEvent = companyEventEmail.getCompanyEvent();
                        DomainEntitySet<CompanyEventDetail> details = companyEvent.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeId);
                        if(details != null)  {
                            String employeeId = details.getFirst().getValue();
                            Employee employee = Application.findById(Employee.class, SpcfUniqueId.createInstance(employeeId));
                            if(employee.getConsumerRealmId() == null) {
                                logger.warn("Error can't look up email address for employee " + employeeId + " because consumer realm id is missing.");
                            } else {
                                String recipientEmailAddress = getReceipientEmailAddress(employee);
                                if(!StringUtil.isNullOrEmpty(recipientEmailAddress) && Validator.isValidEmail(recipientEmailAddress)) {
                                    addEmailParameter(companyEventEmail, EventEmailParamTypeCode.RecipientEmail, recipientEmailAddress);
                                    //Set to pending so email gateway picks it up
                                    companyEventEmail.setStatusCd(EventEmailStatus.Pending);
                                } else {
                                    //Ignore sending emails to invalid email addresses
                                    logger.info("Error invalid emailAddress=" + recipientEmailAddress + " for employeeId=" + employeeId + " with consumerRealmId=" + employee.getConsumerRealmId()+ " with userAuthId=" + employee.getUserAuthId());
                                    companyEventEmail.setStatusCd(EventEmailStatus.Ignore);
                                }
                                companyEventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());
                                Application.save(companyEventEmail);
                            }
                        } else {
                            logger.error("Error can't find employee id to look up employee for CompanyEvent id=" + companyEvent.getId());
                        }
                    } catch(RuntimeException e) {
                        SpcfUniqueId companyEventEmailId = companyEventEmail.getId();
                        logger.warn("Error updating email address for CompanyEventEmail id=" + companyEventEmailId , e);
                    }
                }
                Application.commitUnitOfWork();
            } catch(RuntimeException e) {
                logger.error("Error querying CompanyEventEmails for VmpPaystubNotifications that need IAM email addresses inserted", e);
            } finally {
                Application.rollbackUnitOfWork();
            }
        }

        private String getEmailAddressUsingUserAuthId(String userAuthId) {
            logger.info(String.format("action=getEmailAddressUsingUserAuthId, userAuthId=%s",userAuthId));
            try {
                IamUser iamUser = idlmClientWrapper.getUserDetailsForAuthId(userAuthId);
                return iamUser != null ? iamUser.getEmailAddress() : null;
            } catch(Exception ex) {
                return null;
            }
        }

        private String getEmailAddressUsingConsumerRealmId(String consumerRealmId) {
            logger.info(String.format("action=getEmailAddressUsingConsumerRealmId, consumerRealmId=%s",consumerRealmId));
            ConsumerRealm consumerRealm = new ConsumerRealm();
            User userForConsumerRealmId = consumerRealm.getUserForConsumerRealmId(consumerRealmId);
            String recipientEmailAddress = userForConsumerRealmId != null && userForConsumerRealmId.getEmail() != null ? userForConsumerRealmId.getEmail().getAddress() : null;
            return recipientEmailAddress;
        }

        private String getReceipientEmailAddress(Employee employee) {
            if (FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDLM_ENABLED_FOR_FETCHING_USER_DETAILS, true)) {
                String authId = getAuthId(employee);
                return authId != null ? getEmailAddressUsingUserAuthId(authId) : null;
            } else {
                return getEmailAddressUsingConsumerRealmId(employee.getConsumerRealmId());
            }
        }

        //getAuthId is a throwaway code and hence it is present at 2 places, this will be removed once CFR cleanup is done.
        private String getAuthId(Employee employee) {
            if(employee.getUserAuthId() != null) {
                logger.info(String.format("action=getAuthId, AuthId found in employeeRecord, userAuthId=%s, employeeId=%s",employee.getUserAuthId(), employee.getId()));
                return employee.getUserAuthId();
            } else if(employee.getConsumerRealmId() != null) {
                ConsumerRealm consumerRealm = new ConsumerRealm();
                logger.info(String.format("AuthId is empty in employee record, fetching it from consumerRealmId=%s",employee.getConsumerRealmId()));
                return consumerRealm.getAuthIdFromConsumerRealmId(employee.getConsumerRealmId());
            }
            else {
                return null;
            }
        }

        private void addEmailParameter(CompanyEventEmail companyEventEmail, EventEmailParamTypeCode pEventEmailParamTypeCode, String pValue) {
            CompanyEventEmailParam eventEmailParam = new CompanyEventEmailParam();
            eventEmailParam.setCompanyEventEmail(companyEventEmail);
            eventEmailParam.setCompany(companyEventEmail.getCompanyEvent().getCompany());
            eventEmailParam.setParamTypeCd(pEventEmailParamTypeCode);
            eventEmailParam.setValue(pValue);
            Application.save(eventEmailParam);
        }
    }
}

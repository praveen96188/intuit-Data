package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Class to send Email from PSP via OINP
 *
 * @author nramesh1
 */

//TODO: Can be renamed as MailSender once Static impl of MailSender is removed
@Service
public class MailSenderSpringService {

    public final SpcfLogger logger = Application.getLogger(MailSenderSpringService.class);

    private OINPRequestHelper oinpRequestHelper;
    private EmailSenderService emailSenderService;

    @Autowired
    public MailSenderSpringService(OINPRequestHelper oinpRequestHelper, EmailSenderService emailSenderService)
    {
        this.oinpRequestHelper = oinpRequestHelper;
        this.emailSenderService = emailSenderService;
    }

    public void sendEmailViaOINP(String pToAddress,
                                        String pFromAddress,
                                        String pSubject,
                                        String pMsgBody,
                                        List<String> pAttachmentList,
                                        String pAttachmentData) {
        Boolean isAttachmentDataPresent = false;
        File file = null;
        try {

            if (pAttachmentData != null && !pAttachmentData.isEmpty()) {
                isAttachmentDataPresent=true;
                file = new File("ATT00001" + SpcfUniqueId.generateRandomUniqueId().toString() + ".txt");
                System.out.println(file.getName());
            }

            logger.info("OINP: send Email Via OINP");
            EmailRequest eventRequest = oinpRequestHelper.createOINPEmailRequest(pToAddress, pFromAddress, pSubject, pMsgBody, pAttachmentList, pAttachmentData, file);

            StopWatch timer = StopWatch.startTimer();

            EmailResponse response = emailSenderService.sendMailViaOINP(eventRequest);

            logger.info("OINP: Email request response time= " + timer.stop().getElapsedTimeString());

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("OINP: SendEmail via OINP with subject " + pSubject + " failed due to: " + e);

        }
       finally {
           if (isAttachmentDataPresent) {
               if (file != null) {
                   file.delete();
               }
           }
       }
    }

    public void sendEmailAsyncViaOINP(String pToAddress, String pFromAddress, String pSubject, String pMsgBody) {
        try {
            EmailRequest eventRequest = oinpRequestHelper.createOINPEmailRequest(pToAddress, pFromAddress, pSubject, pMsgBody, null, null, null);

            Future<EmailResponse> response = emailSenderService.asyncSendMailViaOINP(eventRequest);
            logger.info("Email queued - via OINP Async : " + pSubject);

        } catch (Exception e) {
            logger.error("Sendemail via OINP Async :" + pSubject + " failed." + e);
            e.printStackTrace();
        }
    }

    /**
     * Retrieve List of all the OINPEnabled subjects
     *
     * @return
     */
    public List<String> getOINPEnabledTemplateSubjects() {
        List<String> result = null;
        String templatesEnabledForOINP = FeatureFlags.get().stringValue(FeatureFlags.Key.OINP_ENABLED_SUBJECTS_LIST, "Template Subjects");
        if (!templatesEnabledForOINP.isEmpty()) {
            String[] commaSeparatedArr = templatesEnabledForOINP.split("\\s*,\\s*");
            result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        }
        return result;
    }
    /**
     * Retrieve List of all the OINPEnabled templates with Attachments
     *
     * @return
     */
    public List<String> getOINPEnabledTemplateSubjectsWithAttachments() {
        List<String> result = null;
        String templatesEnabledForOINP = FeatureFlags.get().stringValue(FeatureFlags.Key.OINP_ENABLED_SUBJECTS_WITH_ATTACHMENTS, "Template Subjects");
        if (!templatesEnabledForOINP.isEmpty()) {
            String[] commaSeparatedArr = templatesEnabledForOINP.split("\\s*,\\s*");
            result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        }
        return result;
    }


    /**
     * Method to check if a email subject is OINP enabled or not
     *
     * @param templateSubject
     * @return
     */
    public Boolean isSubjectWithAttachmentsOINPEnabled(String templateSubject){
        List<String> OINPEnabledTemplatesWithAttachments = getOINPEnabledTemplateSubjectsWithAttachments();
        if(Objects.isNull(OINPEnabledTemplatesWithAttachments)){
            return false;
        }
        for (String OINPEnabledTemplateWithAttachments: OINPEnabledTemplatesWithAttachments) {
            if(templateSubject.contains(OINPEnabledTemplateWithAttachments)){
                return true;
            }
        }
        return false;
    }

    public Boolean isSubjectWithoutAttachmentsOINPEnabled(String templateSubject){
        List<String> OINPEnabledTemplates = getOINPEnabledTemplateSubjects();
        if(Objects.isNull(OINPEnabledTemplates)){
            return false;
        }
        for (String OINPEnabledTemplate: OINPEnabledTemplates) {
            if(templateSubject.contains(OINPEnabledTemplate)){
                return true;
            }
        }
        return false;
    }

    public Boolean isTemplateSubjectOINPEnabled(String templateSubject, Boolean isAttachmentPresent) {

       if(isAttachmentPresent) {
           boolean isSubjectwithAttachmentsOINPEnabled = isSubjectWithAttachmentsOINPEnabled(templateSubject);
           logger.info("OINP: Feature Flag value of OINP_ENABLED_SUBJECTS_WITH_ATTACHMENTS is " +isSubjectwithAttachmentsOINPEnabled);
           return isSubjectwithAttachmentsOINPEnabled;
       }
       else{
           boolean isSubjectwithoutAttachmentsOINPEnabled = isSubjectWithoutAttachmentsOINPEnabled(templateSubject);
           logger.info("OINP: Feature Flag value of OINP_ENABLED_SUBJECTS_LIST is " +isSubjectwithoutAttachmentsOINPEnabled);
           return isSubjectwithoutAttachmentsOINPEnabled;
       }

    }
}

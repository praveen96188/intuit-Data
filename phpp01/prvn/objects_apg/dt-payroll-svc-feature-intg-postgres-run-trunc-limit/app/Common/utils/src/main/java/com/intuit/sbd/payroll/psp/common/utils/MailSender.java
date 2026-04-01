package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;

//TODO: On completion of all phases of Migration to OINP, all sendEmails to be redirected to MailSenderSpringService (via OINP, Spring impl). The current class can be removed
public class MailSender {

    public final SpcfLogger logger = Application.getLogger(MailSender.class);

     //static ApplicationContext applicationContext = new AnnotationConfigApplicationContext(EmailConfig.class, EmailConfigOverride.class, OINPServicesConfig.class);
    //static EmailSenderService emailSenderService = applicationContext.getBean(EmailSenderService.class);

    private EmailSenderService emailSenderService = PayrollApplicationBeanFactory.getBean(EmailSenderService.class);
    private MailSenderSpringService mailSenderSpringService = PayrollApplicationBeanFactory.getBean(MailSenderSpringService.class);

    private String mServerName;
    private String mToAddress;
    private String mFromAddress;
    private String mSubject;
    private String mMsgBody;
    private boolean mHighPriority;
    private String mReplyToAddress;
    private String mAttachmentData;
    private List<String> mAttachmentList = new Vector<String>(5, 5);

    public MailSender(){
    }

    public MailSender(String pServerName, String pToAddress, String pFromAddress, String pSubject, String pMsgBody) {
        mServerName = pServerName;
        mToAddress = pToAddress;
        mFromAddress = pFromAddress;
        mSubject = pSubject;
        mMsgBody = pMsgBody;
    }

    /*
     *  This contstructor will also send an attachment
     */
    public MailSender(String pServerName,
                      String pToAddress,
                      String pFromAddress,
                      String pSubject,
                      String pMsgBody,
                      String attachment) {
        this(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody);
        addAttachment(attachment);
    }

    public static void sendEmail(String pServerName,
                                 String pToAddress,
                                 String pFromAddress,
                                 String pSubject,
                                 String pMsgBody,
                                 String... pAttachmentList) {
        sendEmail(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody, Arrays.asList(pAttachmentList));
        return;
    }

    public static void sendEmail(String pServerName,
                                 String pToAddress,
                                 String pFromAddress,
                                 String pSubject,
                                 String pMsgBody,
                                 List<String> pAttachmentList) {
        sendEmail(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody, true, null, pAttachmentList, null);
        return;
    }

    public static void sendEmail(String pServerName,
                          String pToAddress,
                          String pFromAddress,
                          String pSubject,
                          String pMsgBody,
                          boolean pHighPriority,
                          String pReplyToAddress,
                          List<String> pAttachmentList,
                          String pAttachmentData) {

        new MailSender().sendMail(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody, pHighPriority, pReplyToAddress, pAttachmentList, pAttachmentData);
    }
    public void sendMail(String pServerName,
                                 String pToAddress,
                                 String pFromAddress,
                                 String pSubject,
                                 String pMsgBody,
                                 boolean pHighPriority,
                                 String pReplyToAddress,
                                 List<String> pAttachmentList,
                                 String pAttachmentData) {
        Boolean isAttachmentPresent = isAttachmentPresent(pAttachmentList, pAttachmentData);
        Boolean isOINPEnabled = mailSenderSpringService.isTemplateSubjectOINPEnabled(pSubject,isAttachmentPresent);

        File file = null;
        Boolean isAttachmentDataPresent = false;

        if(isOINPEnabled)
        {
            logger.info("OINP: Conditions satisfied, proceed to send mail via OINP");
            mailSenderSpringService.sendEmailViaOINP(pToAddress, pFromAddress, pSubject, pMsgBody, pAttachmentList, pAttachmentData);
        }
        else // Send Email via TxE
        {
            try {
                logger.info("TxE: Send mail via TxE");

                if (pAttachmentData != null && !pAttachmentData.isEmpty()) {
                    file = new File("ATT00001" + SpcfUniqueId.generateRandomUniqueId().toString() + ".txt");
                    System.out.println(file.getName());
                }
                String pFromEmailDisplayName = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "psp_email_txe_fromemaildisplayname");

                EmailRequest request = EmailRequest.builder()
                        .toEmailAddresses(pToAddress.split(","))
                        .subject(pSubject)
                        .htmlContent(TextToHtmlConverter.textToHTML(pMsgBody))
                        .fromEmailAddress(pFromAddress)
                        .fromEmailDisplayName(pFromAddress)
                        .build();

                //sending data with list of attachments
                if (pAttachmentList != null && !pAttachmentList.isEmpty()) {

                    logger.info("Sendemail via Txe with attachment: " + pSubject);
                    request.setAttachmentList(pAttachmentList);
                    request.setEmailStrategyType(EmailStrategyType.SendGridWithAttachments);
                    //sending data as a attachment
                } else if (pAttachmentData != null && pAttachmentData.length() > 0) {

                    logger.info("Sendemail via Txe with attachment: " + pSubject);
                    FileUtils.writeStringToFile(file, pAttachmentData);
                    List<String> attachmentList = Arrays.asList(file.getPath());
                    request.setAttachmentList(attachmentList);
                    request.setEmailStrategyType(EmailStrategyType.SendGridWithAttachments);
                    isAttachmentDataPresent = true;
                } else {
                    //sending email without attachment
                    logger.info("Sendemail via Txe without attachment: " + pSubject);
                    request.setAttachmentList(null);
                    request.setEmailStrategyType(EmailStrategyType.SendGrid);

                }
                StopWatch timer = StopWatch.startTimer();

                EmailResponse response = emailSenderService.sendMail(request);

                logger.info("Email request response time= " + timer.stop().getElapsedTimeString());
            } catch (Exception e) {
                logger.error("SendEmail with subject " + pSubject + " failed due to: " + e);
                e.printStackTrace();
            } finally {
                if (isAttachmentDataPresent) {
                    if (file != null) {
                        file.delete();
                    }
                }
            }
        }
    }

    public static void sendEmailAsync(String pServerName,
                                      String pToAddress,
                                      String pFromAddress,
                                      String pSubject,
                                      String pMsgBody){
        new MailSender().sendMailAsync(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody);
    }

    public void sendMailAsync(String pServerName,
                                 String pToAddress,
                                 String pFromAddress,
                                 String pSubject,
                                 String pMsgBody) {
        if (!mailSenderSpringService.isTemplateSubjectOINPEnabled(pSubject,false)) {
            try {

                EmailRequest request = EmailRequest.builder()
                        .subject(pSubject)
                        .toEmailAddresses(pToAddress.split(","))
                        .htmlContent(TextToHtmlConverter.textToHTML(pMsgBody))
                        .fromEmailAddress(pFromAddress)
                        .fromEmailDisplayName(pFromAddress)
                        .pHighPriority(true)
                        .build();

                //sending email without attachment
                logger.info("Sendemail via Txe Async : " + pSubject);
                request.setAttachmentList(null);
                request.setEmailStrategyType(EmailStrategyType.SendGrid);
                Future<EmailResponse> response = emailSenderService.asyncSendMail(request);
                logger.info("Email queued- via Txe Async : " + pSubject);
            } catch (Exception e) {
                logger.error("Sendemail via Txe Async " + pSubject + " failed." + e);
            }
        }
        else{
            logger.info("OINP: Send Async mail via OINP");
            mailSenderSpringService.sendEmailAsyncViaOINP( pToAddress, pFromAddress, pSubject, pMsgBody);
        }
    }

    //TODO: remove below methods for TxE

    /**
     * Retrieve List of all the TxeEnabled templates
     *
     * @return
     */
    public List<String> getTxeEnabledTemplates() {
        List<String> result = null;
        String templatesEnabledForTxE = FeatureFlags.get().stringValue(FeatureFlags.Key.TXE_ENABLED_TEMPLATES, "Template Subjects");
        if (!templatesEnabledForTxE.isEmpty()) {
            String[] commaSeparatedArr = templatesEnabledForTxE.split("\\s*,\\s*");
            result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Method to check if a email template is Txe enabled or not
     *
     * @param templateSubject
     * @return
     */
    public Boolean isTemplateTxeEnabled(String templateSubject) {
        List<String> txeEnabledTemplates = getTxeEnabledTemplates();
        if(Objects.isNull(txeEnabledTemplates)){
            return false;
        }
        for (String txeEnabledTemplate: txeEnabledTemplates) {
            if(templateSubject.contains(txeEnabledTemplate)){
                return true;
            }
        }
        return false;
    }

    /**
     * Temporary Method to check if attachments are present - OINP Migration Phase1
     *
     * @return
     */
    public Boolean isAttachmentPresent(List<String> pAttachmentList,
                                              String pAttachmentData ) {
        if((pAttachmentList != null && !pAttachmentList.isEmpty()) || (pAttachmentData != null && pAttachmentData.length() > 0)) {
            return true;
        }
        return false;
    }

    public String getServerName() {
        return mServerName;
    }

    public void setServerName(String pServerName) {
        mServerName = pServerName;
    }

    public String getToAddress() {
        return mToAddress;
    }

    public void setToAddress(String pToAddress) {
        mToAddress = pToAddress;
    }

    public String getFromAddress() {
        return mFromAddress;
    }

    public void setFromAddress(String pFromAddress) {
        mFromAddress = pFromAddress;
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String pSubject) {
        mSubject = pSubject;
    }

    public String getMsgBody() {
        return mMsgBody;
    }

    public void setMsgBody(String pMsgBody) {
        mMsgBody = pMsgBody;
    }

    public List<String> getAttachments() {
        return mAttachmentList;
    }

    public void addAttachment(String pAttachment) {
        mAttachmentList.add(pAttachment);
    }

    public void sendEmail() {
        sendEmail(mServerName, mToAddress, mFromAddress, mSubject, mMsgBody, mAttachmentList);
    }

}

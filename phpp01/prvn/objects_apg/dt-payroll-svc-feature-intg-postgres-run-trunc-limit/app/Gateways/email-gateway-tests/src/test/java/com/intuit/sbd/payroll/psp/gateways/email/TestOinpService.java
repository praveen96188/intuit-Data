package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.sbd.payroll.psp.common.utils.EmailEtConfigOverride;
import com.intuit.sbd.payroll.psp.common.utils.OINPServicesConfig;
import com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.emailsender.EmailConfig;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.exceptions.KafkaPublisherException;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class TestOinpService {

    private static final String sfProviderSystem = "PSPEmailGateway";

    private final static String mSenderAddress = "no_reply@intuit.com";

    private final static ApplicationContext applicationContext = new AnnotationConfigApplicationContext(EmailConfig.class, EmailEtConfigOverride.class, OINPServicesConfig.class);

    private static String mContentId = "";
    private static String mObjectType = "";

    @SuppressWarnings("unchecked")
    private void testService(EventEmailTemplateTypeCode pTemplateType) {
        try {


            System.out.println("Send OINP request...");

            OINPServicesConfig oinpServicesConfig = applicationContext.getBean(OINPServicesConfig.class);

            mContentId = oinpServicesConfig.getTemplateName(pTemplateType.toString());
            mObjectType = oinpServicesConfig.getTemplateObjectType(pTemplateType.toString());


            //Get Request
            EmailRequest req = getEmailRequest();

            EmailSenderService emailSenderService = applicationContext.getBean(EmailSenderService.class);


            //Send Request
            EmailResponse response = emailSenderService.sendMailViaOINP(req);

        } catch (KafkaPublisherException e) {
            System.out.println("OINP: Error publishing notification via Kafka");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing OINP EmailNotificationService instance.", e);
        }
    }

    private EmailRequest getEmailRequest() {
        String intuit_tid = "PSP-" + SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
        //String offlineTicket = OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP);

        System.out.println("OINP: Creating Email request for OINP ...");

        EmailRequest.EmailRequestBuilder builder = EmailRequest.builder()
                .templateName(getTemplateName())
                .templateObjectType(getTemplateObjectType())
                .templateAttributes(getTemplateAttributes())
                .intuitTid(intuit_tid)
                .emailStrategyType(EmailStrategyType.OINPBulkKafka);

        return builder.build();
    }

    private Map<String, Object> getTemplateAttributes() {
        Map<String,Object> attribute = new HashMap<>();

        String name = "";
        String value = "";

        attribute.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES,"namrata_ramesh@intuit.com");
        attribute.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS,"no_reply@intuit.com");

        name = EventEmailParamTypeCode.PayrollAdminFirstName.toString();
        value = "Ken";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute.put(name,value);

        name = EventEmailParamTypeCode.PayrollAdminLastName.toString();
        value = "Paul";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute.put(name,value);

        name = EventEmailParamTypeCode.PayrollAdminEmail.toString();
        value = "namrata_ramesh@intuit.com";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute.put(name,value);

        name = EventEmailParamTypeCode.CompanyLegalName.toString();
        value = "Joe's Garage";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute.put(name,value);

        name = EventEmailParamTypeCode.SourcePayrollSystem.toString();
        value = "QBDT";
        System.out.println("   Email Parameter:   " + name + " = " + value);
        attribute.put(name,value);

        return attribute;
    }

    private String getTemplateObjectType() {
        return mObjectType;
    }

    private String getTemplateName() {
        return mContentId;
    }

    /*
     * Main
     */


    public static void main(String[] args) {
        try {
            TestOinpService client = new TestOinpService();

            System.out.println("OINP Test NotificationService...");

            client.testService(EventEmailTemplateTypeCode.DDSignupConfirmation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}

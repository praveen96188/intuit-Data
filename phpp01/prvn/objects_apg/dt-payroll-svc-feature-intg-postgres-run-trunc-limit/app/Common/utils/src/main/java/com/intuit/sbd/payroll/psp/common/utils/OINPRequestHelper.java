package com.intuit.sbd.payroll.psp.common.utils;

import org.apache.commons.io.FileUtils;
import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.platform.integration.ius.common.types.IntuitContext;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Helper class to create request for OINP
 *
 * @author nramesh1
 */

@Component
public class OINPRequestHelper {

    public final SpcfLogger logger = Application.getLogger(OINPRequestHelper.class);

    private OINPServicesConfig oinpServicesConfig;

    //name of common template used for sending all emails sent via OINPStrategy/ OINPWithAttachmentsStrategy
    private static String TEMPLATE_ID = "NonTemplateEmailEvent";

    @Autowired
    public OINPRequestHelper( OINPServicesConfig oinpServicesConfig)
    {
        this.oinpServicesConfig = oinpServicesConfig;
    }

    public EmailRequest createOINPEmailRequest(String pToAddress,
                                             String pFromAddress,
                                             String pSubject,
                                             String pMsgBody,
                                             List<String> pAttachmentList,
                                             String pAttachmentData,
                                             File file) throws IOException {

        //header params
        String intuit_tid = "PSP-"+ SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
        logger.info("OINP: Creating OINP request with intuit_tid: " + intuit_tid);

        //build Map for event data consisting of toEmail, fromEMail, subject and htmlContent
        Map<String,Object> eventData = new HashMap<>();
        eventData.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS,pFromAddress );
        eventData.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, pToAddress);
        eventData.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, pSubject);
        eventData.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, TextToHtmlConverter.textToHTML(pMsgBody));

        EmailRequest request = EmailRequest.builder()
                .templateAttributes(eventData)
                .templateName(oinpServicesConfig.getTemplateName(TEMPLATE_ID))
                .templateObjectType(oinpServicesConfig.getTemplateObjectType(TEMPLATE_ID))
                .intuitTid(intuit_tid)
                .build();

        //sending data with list of attachments
        if (pAttachmentList != null && !pAttachmentList.isEmpty()) {

            logger.info("OINP: Sendemail via OINP with attachment: " + pSubject);
            request.setAttachmentList(pAttachmentList);
            request.setEmailStrategyType(EmailStrategyType.OINPWithAttachments);

        } else if (pAttachmentData != null && pAttachmentData.length() > 0) {

            logger.info("OINP: Sendemail via OINP with attachment: " + pSubject);
            FileUtils.writeStringToFile(file, pAttachmentData);
            List<String> attachmentList = Arrays.asList(file.getAbsolutePath());
            request.setAttachmentList(attachmentList);
            request.setEmailStrategyType(EmailStrategyType.OINPWithAttachments);

        } else {
            //sending email without attachment
            logger.info("OINP: SendEmail via OINP without attachment: " + pSubject);
            request.setAttachmentList(null);
            request.setEmailStrategyType(EmailStrategyType.OINP);
        }

        return request;
    }

    /*private String getIntuitTId() {
        String tid;
        IntuitContext intuitContext = RequestAttributesUtils.getAttribute(ContextConstants.INTUIT_CONTEXT, IntuitContext.class);
        if (Objects.nonNull(intuitContext) && Objects.nonNull(intuitContext.getTransactionId())) {
            tid = intuitContext.getTransactionId();
        } else if(!Objects.isNull(MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID))){
            tid = (String)MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID);
        } else {
            tid = SpcfUniqueId.generateRandomUniqueIdString();
        }
        return tid;
    }*/

}

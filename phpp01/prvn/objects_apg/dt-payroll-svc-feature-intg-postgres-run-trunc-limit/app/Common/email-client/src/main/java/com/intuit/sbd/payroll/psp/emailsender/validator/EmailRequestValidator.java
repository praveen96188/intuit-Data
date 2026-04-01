package com.intuit.sbd.payroll.psp.emailsender.validator;


import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import org.apache.commons.lang3.Validate;
import org.apache.commons.validator.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to validate the Client Request
 *
 * @author vishalb849
 */
@Service
public class EmailRequestValidator {

    private final Logger logger = LoggerFactory.getLogger(EmailRequestValidator.class);
    EmailValidator emailValidator = EmailValidator.getInstance();

    public boolean validate(EmailRequest emailRequest) {
        Validate.notBlank(emailRequest.getSubject(), "Bad Request, Email Subject is Null or empty");
        Validate.notBlank(emailRequest.getHtmlContent(), "Bad Request, Email body is Null or empty");
        if (emailRequest.getFromEmailAddress() != null && !emailRequest.getFromEmailAddress().isEmpty()) {
            Validate.isTrue(emailValidator.isValid(emailRequest.getFromEmailAddress()), "From Email address is not valid");
        }
        return true;
    }

    public boolean validateOINPStrategyEventData(EmailRequest emailRequest) {

        Map<String,Object> eventData = emailRequest.getTemplateAttributes();

        Validate.isTrue(eventData.containsKey(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT), "Bad Request, no Email Subject provided" );
        Validate.notBlank(eventData.get(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT).toString(), "Bad Request, Email Subject is Null or empty");

        Validate.isTrue(eventData.containsKey(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT), "Bad Request, no HTML Content provided");
        Validate.notBlank(eventData.get(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT).toString(), "Bad Request, Email body is Null or empty");

        Validate.isTrue(eventData.containsKey(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES), "Bad Request, no to Email provided");
        Validate.notBlank(eventData.get(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES).toString(), "Bad Request, to Email is Null or empty");

        Validate.isTrue(eventData.containsKey(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS), "Bad Request, no from Email Address provided");
        if (eventData.get(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS).toString() != null && !eventData.get(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS).toString().isEmpty()) {
            Validate.isTrue(emailValidator.isValid(eventData.get(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS).toString()), "From Email address is not valid");
        }
        return true;
    }

    public boolean validateOINPTemplateProperties(String name, String objectType, String serviceName) {

        Validate.notBlank(name, "Bad Request, Template name is Null or empty");
        Validate.notBlank(objectType, "Bad Request, Template ObjectType is Null or empty");
        Validate.notBlank(serviceName, "Bad Request, Template sourceServiceName is Null or empty");
        return true;
    }

    public String[] getValidEmailList(String[] emailArray){
        List<String> emailList = new ArrayList<String>();
        for(int i=0; i < emailArray.length; i++) {
            if(!emailValidator.isValid(emailArray[i])) {
                logger.error("invalid email id: "+emailArray[i]);
                continue;
            }
            emailList.add(emailArray[i]);
        }
        if(emailList.isEmpty()){
            logger.error("no valid email id present ");
            throw new EmailServiceException("no valid email id present");
        }
        return emailList.stream().toArray(String[]::new);
    }

    public boolean validateAttachments(EmailRequest emailRequest) {
        Validate.notNull(emailRequest.getAttachmentList(), "No attachments to process.");
        Validate.notEmpty(emailRequest.getAttachmentList(), "No attachments to process. Attachment list is empty");
        return true;
    }

    public boolean validateAttachmentUri(String Uri) {
        Validate.notNull(Uri, "OINP: Uri is Null");
        Validate.notEmpty(Uri, "OINP: Uri is empty");
        return true;
    }

    public boolean validateDocumentId(String docId) {
        Validate.notNull(docId, "OINP: DocumentId is Null");
        Validate.notEmpty(docId, "OINP: DocumentId is empty");
        return true;
    }

    public boolean validateSourceKey(String sourceKey) {
        Validate.notNull(sourceKey, "OINP: SourceKey is Null");
        Validate.notEmpty(sourceKey, "OINP: SourceKey is empty");
        return true;
    }
}
package com.intuit.sbd.payroll.psp.gateways.email.factory.product;

import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;

import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 26, 2008
 * Time: 1:23:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventEmailTemplate {
    // max of 1000 destination elements per email service request (limit set by schema)
    //private static final int sfMaxEmailsPerRequest = 1000;

    private EventEmailTemplateTypeCode mTemplateId;
    private Map<String, EventEmail> mEmailRecipients = new Hashtable<String, EventEmail>();
    private Properties mTemplateProperties = new Properties();

    public EventEmailTemplate(EventEmailTemplateTypeCode pTemplateId) {
        mTemplateId = pTemplateId;
    }

    public EventEmailTemplateTypeCode getTemplateId() {
        return mTemplateId;
    }

    public boolean isEmpty() {
        return mEmailRecipients.isEmpty();
    }

    public void merge(final EventEmailTemplate pSourceTemplate) {
        if ((pSourceTemplate == null) || (pSourceTemplate.getTemplateId() != mTemplateId)) {
            return;
        }

        mEmailRecipients.putAll(pSourceTemplate.mEmailRecipients);
    }

    public void addEmailRecipient(EventEmail pEmail) {
        mEmailRecipients.put(pEmail.getRecipientId(), pEmail);
    }

    public Properties getProperties() {
        return mTemplateProperties;
    }

    public void addProperty(String pParamName, String pParamValue) {
        mTemplateProperties.setProperty(pParamName, pParamValue);
    }

    public int getRecipientCount() {
      return mEmailRecipients.size();
    }

    /**
     * Retrieves the email recipients as a List of List<IEventEmail>. Each request to the Notification Service may
     * contain a maximum of 1000 recipients.
     * @return A List of List<IEventEmail>; each member list will contain <= 1000 recipients.
     */
    public List<List<IEventEmail>> getRecipientsToTransmit() {
        int ntfBatchSize = SystemParameter.findIntValue(SystemParameter.Code.EMAIL_GATEWAY_NTF_BATCH_SIZE, 100); // max emails per web service request
        List<List<IEventEmail>> recipientLists = new Vector<List<IEventEmail>>();
        List<IEventEmail> list = new Vector<IEventEmail>(ntfBatchSize);

        recipientLists.add(list);

        for (EventEmail eventEmail : mEmailRecipients.values()) {
            if (list.size() == ntfBatchSize) {
                list = new Vector<IEventEmail>(ntfBatchSize);
                recipientLists.add(list);
            }

            if (eventEmail.readyToSend()) {
                list.add(eventEmail);
            }
        }

        return recipientLists;
    }

    public void emailSent(String pRecipientId) {
        EventEmail email = mEmailRecipients.get(pRecipientId);

        if (email != null) {
            email.sendSuccessful();
        }
    }

    public void failedValidation(String pErrorDetail, List<IEventEmail> pRecipients) {
        for (IEventEmail email : pRecipients) {
            failedValidation(pErrorDetail, email.getRecipientId());
        }
    }

    public void failedValidation(String pErrorDetail, String pRecipientId) {
        EventEmail email = mEmailRecipients.get(pRecipientId);

        if (email != null) {
            email.failedValidation(pErrorDetail);
        }
    }

    public void failedWithListDetectiveError(String pRecipientId, String pErrorCd, String pErrorMsg) {
        EventEmail email = mEmailRecipients.get(pRecipientId);
        if (email != null) {
            email.failedWithListDetectiveError(pErrorCd, pErrorMsg);
        }
    }

    public void serviceReturned(String pErrorDetail, List<IEventEmail> pRecipients) {
        for (IEventEmail email : pRecipients) {
            serviceReturned(pErrorDetail, email.getRecipientId());
        }
    }

    public void serviceReturned(String pErrorDetail, String pRecipientId) {
        EventEmail email = mEmailRecipients.get(pRecipientId);

        if (email != null) {
            email.serviceReturned(pErrorDetail);
        }
    }

    public void serviceFault(String pErrorDetail, List<IEventEmail> pRecipients) {
        for (IEventEmail email : pRecipients) {
            serviceFault(pErrorDetail, email.getRecipientId());
        }
    }

    public void serviceFault(String pErrorDetail, String pRecipientId) {
        EventEmail email = mEmailRecipients.get(pRecipientId);

        if (email != null) {
            email.serviceFault(pErrorDetail);
        }
    }

    public StringBuffer reportErrors() {
        StringBuffer err = new StringBuffer();
        StringBuffer subErr = new StringBuffer();

        for (EventEmail email : mEmailRecipients.values()) {
            subErr.append(email.reportErrors());
        }

        if (subErr.length() > 0) {
            err.append(EmailUtils.sfNewLine);

            err.append("*** ");
            err.append("Email Template parameters for Template ID: ");
            err.append((mTemplateId != null) ? mTemplateId.toString() : "<unknown>");
            err.append(" ***");
            err.append(EmailUtils.sfNewLine);
            err.append(EmailUtils.sfNewLine);

            err.append("> Template parameters: ");
            err.append(EmailUtils.sfNewLine);

            if (mTemplateProperties.isEmpty()) {
                err.append("  <no template level (global) parameters specified>");
                err.append(EmailUtils.sfNewLine);
            } else {
                for (Map.Entry<Object, Object> pair : mTemplateProperties.entrySet()) {
                    err.append("  [ ");
                    err.append(pair.getKey().toString());
                    err.append(" ] ");
                    err.append(pair.getValue().toString());
                    err.append(EmailUtils.sfNewLine);
                }
            }

            err.append(subErr);
        }

        return err;
    }
}

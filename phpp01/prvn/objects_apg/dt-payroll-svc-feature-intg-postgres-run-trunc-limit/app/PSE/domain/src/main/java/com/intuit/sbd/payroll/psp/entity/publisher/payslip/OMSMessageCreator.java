package com.intuit.sbd.payroll.psp.entity.publisher.payslip;

import org.springframework.jms.core.MessageCreator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Map;

public class OMSMessageCreator implements MessageCreator {

    private String transactionId;
    private String payload;
    private Map<String, String> headers;

    OMSMessageCreator(String transactionId, String payload, Map<String, String> headers) {
        this.transactionId = transactionId;
        this.payload = payload;
        this.headers = headers;
    }

    @Override
    public Message createMessage(Session session) throws JMSException {
        TextMessage message = session.createTextMessage(payload);
        applyTid(message, transactionId);
        injectHeaders(message, headers);
        return message;

    }

    @Nonnull
    protected <T extends Message> T applyTid(@Nonnull T message, @Nonnull String tid) throws JMSException {
        message.setStringProperty("intuit_tid", tid);
        message.setJMSCorrelationID(tid);
        return message;
    }

    protected @Nonnull <T extends Message> T injectHeaders(@Nonnull T message, @CheckForNull Map<String, String> props)
            throws JMSException {
        try {
            if (props != null && props.size() > 0) {
                props.forEach((k, v) -> {
                    try {
                        message.setStringProperty(k, v.toString());
                    } catch (JMSException e) {
                        throw new RuntimeException("Exception from JMS setStringProperty(" + k + ", " + v + ")", e);
                    }
                });
            }
            return message;
        } catch (RuntimeException rex) {
            if (rex.getCause() instanceof JMSException) {
                throw (JMSException) rex.getCause();
            } else {
                throw rex;
            }
        }
    }
}
package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.*;

/**
 * Created by Ankit on 9/21/2015.
 */
public class AsyncMailSender implements Runnable {

    public static final SpcfLogger logger = Application.getLogger(AsyncMailSender.class);

    private String mServerName;
    private String mToAddress;
    private String mFromAddress;
    private String mSubject;
    private String mMsgBody;
    private boolean mHighPriority;
    private String mReplyToAddress;
    private List<String> mAttachmentList = new Vector<String>(5, 5);
    private String mAttachmentData;

    public AsyncMailSender(String pServerName, String pToAddress, String pFromAddress, String pSubject, String pMsgBody,
                           boolean pHighPriority, String pReplyToAddress, List<String> pAttachmentList, String pAttachmentData) {
        mServerName = pServerName;
        mToAddress = pToAddress;
        mFromAddress = pFromAddress;
        mSubject = pSubject;
        mMsgBody = pMsgBody;
        mHighPriority = pHighPriority;
        mReplyToAddress = pReplyToAddress;
        mAttachmentList = pAttachmentList;
        mAttachmentData = pAttachmentData;
    }

    public static void sendEmail(String pServerName,
                                 String pToAddress,
                                 String pFromAddress,
                                 String pSubject,
                                 String pMsgBody,
                                 String... pAttachmentList) {
        sendEmail(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody, Arrays.asList(pAttachmentList));
    }

    public static void sendEmail(String pServerName,
                                 String pToAddress,
                                 String pFromAddress,
                                 String pSubject,
                                 String pMsgBody,
                                 List<String> pAttachmentList) {
        sendEmail(pServerName, pToAddress, pFromAddress, pSubject, pMsgBody, true, null, pAttachmentList, null);
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
        AsyncMailSender asyncMailSender = new AsyncMailSender(pServerName, pToAddress, pFromAddress, pSubject,
                                                              pMsgBody, pHighPriority, pReplyToAddress, pAttachmentList, pAttachmentData);
        Thread thread = new Thread(asyncMailSender);
        thread.start();
    }

    public void run() {
        try {
            logger.info("Trying to send email with subject "+mSubject+" to "+mToAddress);
            Properties props = System.getProperties();

            // The SMTP server to connect to.
            props.put("mail.smtp.host", mServerName);

            // PSRV001820
            // Socket I/O timeout value in milliseconds. Default is infinite timeout.
            props.put("mail.smtp.timeout", "10000");

            // PSRV001820
            // Socket connection timeout value in milliseconds. Default is infinite timeout.
            props.put("mail.smtp.connectiontimeout", "10000");

            Session session = Session.getDefaultInstance(props, null);

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(mFromAddress));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mToAddress, false));
            //msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
            msg.setSubject(mSubject);
            //msg.setHeader("X-Mailer", "");
            //msg.setHeader("Message-ID", "");
            if (mHighPriority) {
                msg.setHeader("Importance", "high");
            }
            if (mReplyToAddress != null) {
                msg.setReplyTo(InternetAddress.parse(mReplyToAddress, false));
            }

            msg.setSentDate(new Date());

            if ((mAttachmentList != null) && !mAttachmentList.isEmpty()) {
                logger.info("Sending email via Mailsender with attachments");
                // email with attachment(s) is a multipart message
                Multipart multipart = new MimeMultipart();

                // part one is the message body
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(mMsgBody);

                multipart.addBodyPart(messageBodyPart);

                for (String attachment : mAttachmentList) {
                    // part two is attachment
                    DataSource source = new FileDataSource(attachment);
                    messageBodyPart = new MimeBodyPart();
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(source.getName());
                    multipart.addBodyPart(messageBodyPart);
                }

                // put parts in message
                msg.setContent(multipart);
            } else if (mAttachmentData != null && mAttachmentData.length() > 0) {
                logger.info("Sending email via Mailsender with attachments");
                // email with attachment(s) is a multipart message
                Multipart multipart = new MimeMultipart();

                // part one is the message body
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(mMsgBody);

                multipart.addBodyPart(messageBodyPart);

                // part two is attachment
                DataSource source = new ByteArrayDataSource(mAttachmentData, "text/plain");
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(source));
                multipart.addBodyPart(messageBodyPart);

                // put parts in message
                msg.setContent(multipart);
            } else {
                logger.info("Sending email via Mailsender without attachments");
                msg.setText(mMsgBody);
            }

            // send the message
            Transport.send(msg);
            logger.info("Successfully send email with subject "+mSubject+" to "+mToAddress);
        } catch (Exception ex) {
            logger.error("Unable to send mail with subject "+mSubject+" to "+mToAddress, ex);
            throw new RuntimeException("Error sending email via AsyncMailSender", ex);
        }
    }

}

package com.intuit.sbd.payroll.psp.gateways.email.txe;


import com.intuit.ias.notification.pub.wsdl.Fault;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.EmailEtConfigOverride;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.emailsender.EmailConfig;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.validator.EmailValidator;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;


import java.util.ArrayList;
import java.util.List;


public class TxeExactTargetSendEmail {


    private static final SpcfLogger sfLogger = Application.getLogger(TxeExactTargetSendEmail.class);

    private static final String intuit_tid = "intuit_tid";

    private static final TxeExactTargetResponseProcessor processExactTargetTxeResponse = new TxeExactTargetResponseProcessor();
    private static final TxeExactTargetRequestlHelper exactTargetRequest = new TxeExactTargetRequestlHelper();

    private static EmailSenderService emailSenderService = PayrollApplicationBeanFactory.getBean(EmailSenderService.class);



    //This is used to send emails to Exact target via TXE service
    public void sendMail(EventEmailTemplate pTemplate) throws Exception {

        if (sfLogger.isDebugEnabled()) {
            sfLogger.debug("================================================");
            sfLogger.debug("Sending email request to TXE service...");
            sfLogger.debug("================================================");
        }


        String curTransactionId = "<unknown>";
        List<IEventEmail> curEmailBatch = null; // PSRV003705 - Multiple emails

        try {
            for (List<IEventEmail> emailBatch : pTemplate.getRecipientsToTransmit()) {
                if (emailBatch.isEmpty()) continue;

                curEmailBatch = emailBatch;


                //Get Request
                EmailRequest req = exactTargetRequest.getEmailRequest(pTemplate, emailBatch);


                StopWatch timer = StopWatch.startTimer();

                //Send Request
                EmailResponse response = emailSenderService.sendMail(req);

                sfLogger.info("Sending Email Via TXE.Template Id: " + pTemplate.getTemplateId() + " . Time Taken : " + timer.stop().getElapsedTimeString() + ". Number of Recipients: " + req.getSendRequest().getNotification().getDestinations().getDestination().size());

                curTransactionId = response.getHeaders().get(intuit_tid).toString();


                //Process Request
                processExactTargetTxeResponse.processResponse(response, pTemplate, curTransactionId, emailBatch);


            }
        } catch (EmailServiceException |
                EmailProcessingException e) {
            throw e;
        } catch (
                Throwable t) {
            if (curEmailBatch != null) {
                pTemplate.serviceFault("Service fault [Service Transaction ID: " + curTransactionId + "]", curEmailBatch);
            }

            throw new EmailServiceException("A service error has occurred in the TXE Notification Service " +
                    "[Service Transaction ID: " + curTransactionId + "]", t);
        }

    }

}


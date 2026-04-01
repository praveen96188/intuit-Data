package com.intuit.sbd.payroll.psp.gateways.email;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventEmailStatus;
import com.intuit.sbd.payroll.psp.gateways.email.exception.EmailProcessingException;
import com.intuit.sbd.payroll.psp.gateways.email.factory.CompanyEventEmailManager;
import com.intuit.sbd.payroll.psp.gateways.email.factory.EventEmailTemplateFactory;
import com.intuit.sbd.payroll.psp.gateways.email.factory.NotificationServiceFactory;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.service.EmailNotificationService;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.oinp.OINPEmailHelper;
import com.intuit.sbd.payroll.psp.gateways.email.oinp.OINPSendEmail;
import com.intuit.sbd.payroll.psp.gateways.email.txe.TxeExactTargetSendEmail;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.gateways.email.txe.TxeEmailHelper;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.*;


/**
 * User: kpaul
 * Date: Jul 17, 2008
 * Time: 2:09:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailGateway {
    private final SpcfLogger logger = Application.getLogger(EmailGateway.class);
    private TxeExactTargetSendEmail txeSendEmail = new TxeExactTargetSendEmail();

    private OINPSendEmail oinpSendEmail = PayrollApplicationBeanFactory.getBean(OINPSendEmail.class);
    private OINPEmailHelper oinpEmailHelper = PayrollApplicationBeanFactory.getBean(OINPEmailHelper.class);

    private PSPRequestContextManager pspRequestContextManager;

    public EmailGateway() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }


    public void processCompanyEventsForEmail() {
        Application.beginUnitOfWork();

        // set flush mode to manual to reduce hibernate cache overhead
        Application.getHibernateSession().setFlushMode(FlushMode.MANUAL);

        try {
            // Select a maximum number of events. This will prevent processing stalls due to too many events queued to be processed.
            int batchSize = SystemParameter.findIntValue(SystemParameter.Code.EMAIL_GATEWAY_MAX_BATCH_SIZE, 5000);
            DomainEntitySet<CompanyEventEmail> eventList = CompanyEventEmail.findEmailEventsByStatus(batchSize,
                    EventEmailStatus.Pending, EventEmailStatus.GroupIncomplete, EventEmailStatus.Resend, EventEmailStatus.PendingResend);

            Map<Company, DomainEntitySet<CompanyEventEmail>> eventListPerCompany = new HashMap<Company, DomainEntitySet<CompanyEventEmail>>();

            for (CompanyEventEmail event : eventList) {
                DomainEntitySet<CompanyEventEmail> eventsForACompany = eventListPerCompany.get(event.getCompany());
                if (eventsForACompany == null) {
                    eventsForACompany = new DomainEntitySet<CompanyEventEmail>();
                    eventListPerCompany.put(event.getCompany(), eventsForACompany);
                }

                eventsForACompany.add(event);
            }

            try {
                for (Company company : eventListPerCompany.keySet()) {
                    try {
                        pspRequestContextManager.setRequestContextCompany(company);
                        DomainEntitySet<CompanyEventEmail> eventsForACompany = eventListPerCompany.get(company);
                        processCompanyEventsForEmailForOneCompany(eventsForACompany);
                    } finally {
                        pspRequestContextManager.clearRequestContextCompany();
                    }
                }
            } finally {
                // flush the hibernate cache
                Application.getHibernateSession().flush();

                // always commit so we don't resend successfully sent emails next pass
                // (we want to preserve any changes made to this point)
                Application.commitUnitOfWork();
            }


        } catch (Throwable t) {
            logger.fatal("An unrecoverable exception occurred during email processing.", t);
        } finally {
            Application.rollbackUnitOfWork(); // catch-all in case error occurs above (does nothing if commit works.)
        }
    }

    public void processCompanyEventsForEmailForOneCompany(DomainEntitySet<CompanyEventEmail> eventList) throws Exception {
        Collection<CompanyEventEmailManager> companyEmailManagers =
                EventEmailTemplateFactory.buildCompanyEmailManagers(eventList);

        List<EventEmailTemplate> masterTemplateList =
                EventEmailTemplateFactory.buildMasterTemplateList(companyEmailManagers);

        try {
            for (EventEmailTemplate template : masterTemplateList) {
                if (template == null || template.isEmpty()) continue;

                Boolean isTemplateOINPEnabled = oinpEmailHelper.isTemplateOINPEnabled(template.getTemplateId().toString());
                logger.info("Feature Flag value of OINP_ENABLED_TEMPLATES is " + isTemplateOINPEnabled);

                if (!isTemplateOINPEnabled) {
                    logger.info("Sending EmailEvents via TxE for template: " + template.getTemplateId().toString());
                    sendEmailViaTXE(template);
                } else {
                    logger.info("OINP: Sending EmailEvents via OINP for template: " + template.getTemplateId().toString());
                    sendEmailViaOINP(template);
                }
            }

        } finally {
            // update all domain objects with their corresponding statuses

            for (CompanyEventEmailManager manager : companyEmailManagers) {
                manager.persistEventStatuses();
            }

            try {
                // report any errors that have accumulated
                // (do before commit so entities are still accessible)
                EmailUtils.reportEmailErrors(companyEmailManagers);
            } catch (Throwable t) {
                logger.error("Error while processing/sending email error log in email gateway.", t);
            }
        }
    }

    private void sendEmailViaTXE(EventEmailTemplate template) throws Exception {
        try {

            txeSendEmail.sendMail(template);
        } catch (EmailProcessingException e) {
            logger.warn("A recoverable exception occurred during email processing [template id: " +
                    template.getTemplateId().toString() + "]", e);
        }
    }

    private void sendEmailViaOINP(EventEmailTemplate template) throws Exception {
        try {
            oinpSendEmail.sendTemplateMails(template);

        } catch (EmailProcessingException e) {

            //change status of emails events of given template
            for (List<IEventEmail> emailBatch : template.getRecipientsToTransmit()) {
                if (emailBatch.isEmpty()) continue;

                template.failedValidation(e.getMessage(),emailBatch);
            }

            logger.warn("OINP: A recoverable exception occurred during template processing [template id: " +
                    template.getTemplateId().toString() + "]", e);
        }
    }

    public static void main(String[] args) {
        // todo v2: add param to process failed emails

        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.EmailGateway);
            new EmailGateway().processCompanyEventsForEmail();
        } catch (RuntimeException e) {
            System.exit(1);
        } catch (Exception e) {
            System.exit(2);
        }
    }
}

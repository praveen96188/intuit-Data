package com.intuit.sbd.payroll.psp.gateways.amo;

/*
 * Copyright (c) 2010 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import weblogic.jms.common.TextMessageImpl;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.bind.JAXBException;
import java.util.*;

public class AMOGateway extends AbstractAMOGateway {

    private static final String UNMARSHALL_ERROR = "ERROR_UNMARSHALLING";
    private static SpcfLogger logger = SpcfLogManager.getLogger(AMOGateway.class);
    private static List<JmsTemplate> jmsTemplates = new ArrayList<JmsTemplate>();

    protected AMOGateway() {
        if(jmsTemplates.isEmpty()) {
            initialize();
        }
    }

    private void initialize() {
        String amoDestination = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_jms_destination");
        int receiveTimeout = SystemParameter.findIntValue(SystemParameter.Code.AMO_MESSAGE_RECEIVE_TIMEOUT, 5000);

        Exception exception = null;
        boolean failure = true;

        String[] amoProviderURLs = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_jms_url").split(",");
        String amoInitialContext = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_jms_initial_context");
        String amoSecurityPrincipal = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_jms_user");
        String amoSecurityCredential = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_jms_password");
        String amoConnectionFactory = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_jms_connection_factory");
        int maxAttempts = SystemParameter.findIntValue(SystemParameter.Code.AMO_CONNECTION_RETRY_ATTEMPTS, 5);
        long waitInterval = SystemParameter.findIntValue(SystemParameter.Code.AMO_CONNECTION_RETRY_WAIT_PERIOD, 5000);

        List<Properties> propertiesList = new ArrayList<Properties>(amoProviderURLs.length);
        for (String amoProviderURL : amoProviderURLs) {
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, amoInitialContext);
            properties.put(Context.PROVIDER_URL, amoProviderURL);
            properties.put(Context.SECURITY_PRINCIPAL, amoSecurityPrincipal);
            properties.put(Context.SECURITY_CREDENTIALS, amoSecurityCredential);
            propertiesList.add(properties);
        }


        for (int x = 0; x < maxAttempts; x++) {
            try {
                for (Properties properties : propertiesList) {
                    Context context = new InitialContext(properties);
                    QueueConnectionFactory factory = (QueueConnectionFactory) context.lookup(amoConnectionFactory);
                    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(factory);
                    cachingConnectionFactory.setReconnectOnException(true);
                    cachingConnectionFactory.setExceptionListener(new AMOExceptionListener(cachingConnectionFactory));
                    cachingConnectionFactory.setCacheConsumers(true);
                    JmsTemplate template = new JmsTemplate(cachingConnectionFactory);
                    template.setDefaultDestination((Destination) context.lookup(amoDestination));
                    template.setReceiveTimeout(receiveTimeout);
                    template.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
                    jmsTemplates.add(template);
                }
                failure = false;
                break;
            } catch (Exception e) {
                exception = e;
                logger.warn(e.getMessage(), e);
                try {
                    Thread.sleep(waitInterval);
                } catch (Exception s) {
                    logger.warn(s);
                }
            }
        }

        if (failure) {
            logger.error("Error initializing amo gateway.", exception);
            throw new ExceptionInInitializerError(exception);
        }
    }

    /**
     * Retrieves given number of messages from each queue.
     *
     * Caller is responsible for messages once they are returned. This message consumer acknowledges the messages
     * once it has successfully parsed them.
     *
     * Underlying connections are reset by the spring framework --- resets but not reconnects the connection :(
     * The AMO exception listener will reinitialize the connection if it sees a "Connection is closed" message
     */
    public Collection<AMODTO> getMessages(int numberOfMessagesToGet) {
        Map<String, AMODTO> entitlementMessageMap = readMessagesFromFiles();
        String strMsg = null;
        try {
            int messageCount = entitlementMessageMap.size();
            initializeUnmarshaller();

            // retrieve messages from all connections
            for (JmsTemplate template : jmsTemplates) {
                
                while (messageCount < numberOfMessagesToGet) {
                    Message message = template.receive();
                    
                    // no more messages in the queue
                    if(message == null) {
                        break;
                    }

                    // make sure message contains XML
                    strMsg = ((TextMessageImpl) message).getText();
                    if (!strMsg.contains("<?xml")) {
                        message.acknowledge();
                        break;
                    }

                    try {
                        processStringMessage(entitlementMessageMap, strMsg);
                    } catch (JAXBException e) {
                        logger.error("Failed to unmarshall message. Message will be stored with a license number of " + UNMARSHALL_ERROR, e);
                        // failed to unmarshall the message
                        AMODTO amodto = entitlementMessageMap.get(UNMARSHALL_ERROR);
                        if(amodto == null) {
                            amodto = new AMODTO();
                            amodto.setLicenseNumber(UNMARSHALL_ERROR);
                        }
                        SyncCustomerAssetDataAreaTypeDTO syncCustomerAssetDataAreaTypeDTO = new SyncCustomerAssetDataAreaTypeDTO(strMsg);
                        syncCustomerAssetDataAreaTypeDTO.setEntitlementMessageStatusCode(EntitlementMessageStatusCode.Error);
                        amodto.addMessage(syncCustomerAssetDataAreaTypeDTO);
                        entitlementMessageMap.put(UNMARSHALL_ERROR, amodto);
                    }
                    strMsg = null;

                    // acknowledge message received and parsed successfully
                    message.acknowledge();
                    messageCount++;
                }
            }
        } catch (Throwable t) {
            logger.error("Error retrieving AMO messages.", t);
            if(strMsg != null) {
                try{
                    writeMessageToFile(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir"),
                            strMsg);

                }catch (Throwable t1){
                    logger.error("Unable to write messages from AMO to file",t1);
                }
            }
        }

        return entitlementMessageMap.values();
    }
}


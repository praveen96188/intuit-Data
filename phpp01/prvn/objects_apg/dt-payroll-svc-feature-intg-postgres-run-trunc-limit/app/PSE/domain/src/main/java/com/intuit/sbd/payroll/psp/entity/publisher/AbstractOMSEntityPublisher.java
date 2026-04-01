package com.intuit.sbd.payroll.psp.entity.publisher;

import com.intuit.platform.messaging.pulsar.jms.client.PulsarConnectionFactory;
import com.intuit.platform.messaging.pulsar.jms.client.PulsarQueue;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.entity.publisher.payslip.PayslipPublisherOMSSettings;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

@Slf4j
public abstract class AbstractOMSEntityPublisher<E extends DomainEntity> extends AbstractEntityPublisher<E> {

    protected static final String DELIMITER = "-";
    protected JmsTemplate jmsTemplate;

    public AbstractOMSEntityPublisher(CDMMapper cdmMapper) {

        super(null,cdmMapper);
    }

    public JmsTemplate getJmsTemplate() {
        if (this.jmsTemplate == null) {
            String logPrefix = "Action=getJmsTemplate, status={}";
            log.info(logPrefix, "started");
            this.jmsTemplate = EntityPublisherJmsTemplate.INSTANCE.get();
            log.info(logPrefix, "completed");
        }
        return this.jmsTemplate;
    }

    private enum EntityPublisherJmsTemplate {
        INSTANCE;

        private JmsTemplate get() {
            String logPrefix = "Action=getJmsTemplate, status={}";

            JmsTemplate jmsTemplate = new JmsTemplate();
            ConnectionFactory producerConnectionFactory = new PulsarConnectionFactory(PayslipPublisherOMSSettings.getUsername(), PayslipPublisherOMSSettings.getPassword(), PayslipPublisherOMSSettings.getPulsorUrl());
            QueueConnectionFactory factory = (QueueConnectionFactory) (producerConnectionFactory);
            CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(factory);
            cachingConnectionFactory.setReconnectOnException(true);
            cachingConnectionFactory.setCacheConsumers(true);

            jmsTemplate = new JmsTemplate(cachingConnectionFactory);
            jmsTemplate.setPubSubDomain(true);
            jmsTemplate.setConnectionFactory(cachingConnectionFactory);
            jmsTemplate.setDefaultDestination(new PulsarQueue(PayslipPublisherOMSSettings.getQueueName()));
            jmsTemplate.setReceiveTimeout(5000);
            jmsTemplate.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

            return jmsTemplate;
        }
    }

    @Override
    protected boolean publishNonRestrictedEntity(EntityEvent entityEvent) {
        return publishEntity(entityEvent);
    }



    protected boolean publishEntity(EntityEvent entityEvent) {
        boolean isPublishToOMSEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_OMS_ENABLED_FOR_PMO, false);
        log.info("Feature Flag value={}",isPublishToOMSEnabled);
        if(isPublishToOMSEnabled) {
            publishEntityToOMS(entityEvent);
        }
        return true;
    }
    private void publishEntityToOMS(final EntityEvent entityEvent) {
        try {
            publishToOMS(entityEvent);
            log.info("Action=publishEntityToOMS, status=complete, entityEventId={}", entityEvent.getEntityId());
        } catch (Exception e) {
            log.error("Action=publishEntityToOMS, status=fail, entityEventId={}, reason={}", entityEvent.getEntityId(),
                    e.getMessage(), e);
        }
    }
    abstract protected void publishToOMS(EntityEvent entityEvent);
    @Override
    protected boolean publishRestrictedEntity(EntityEvent entityEvent) {
        return publishEntity(entityEvent);
    }
}

package com.intuit.sbd.payroll.psp.entity;

import com.intuit.platform.messaging.pulsar.jms.client.PulsarConnectionFactory;
import com.intuit.platform.messaging.pulsar.jms.client.PulsarQueue;
import com.intuit.sbd.payroll.psp.entity.publisher.payslip.PayslipPublisherOMSSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;

@Configuration
@ComponentScan(basePackages = {"com.intuit.sbd.payroll.psp.entity","com.intuit.sbg.psp.events.publisher.kafka", "com.intuit.sbg.psp.events.consumer.kafka"})
public class EntityConfig {

}


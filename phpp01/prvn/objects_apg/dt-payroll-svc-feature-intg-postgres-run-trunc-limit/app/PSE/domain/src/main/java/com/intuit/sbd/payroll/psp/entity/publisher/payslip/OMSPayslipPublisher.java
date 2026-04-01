package com.intuit.sbd.payroll.psp.entity.publisher.payslip;

import com.intuit.payroll.api.payslip.model.PrivilegedPayslipCDM;
import com.intuit.platform.integration.messaging.crypto.engine.IdpsEncrypterEngine;
import com.intuit.platform.integration.messaging.crypto.provider.IdpsConnectionManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.AbstractOMSEntityPublisher;
import com.intuit.sbd.payroll.psp.entity.publisher.EntityEvent;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@Slf4j
public class OMSPayslipPublisher extends AbstractOMSEntityPublisher<Paycheck> {

    String activeProfile;


    public OMSPayslipPublisher(CDMMapper cdmMapper, @Value("${spring.profiles.active}") String activeProfile) {
        super(cdmMapper);
        this.activeProfile = activeProfile.replace("iks", "");

    }

    @Override
    public Class<?> getEntityClass() {
        return Paycheck.class;
    }

    @Override
    protected String getTopic() {
        return null;
    }

    @Override
    protected Company getCompany(EntityContext<Paycheck> entityContext) {
        Paycheck paycheck = entityContext.getCurrentEntity();
        return paycheck.getCompany();
    }


    public void publishToOMS(final EntityEvent entityEvent) {
        try {
            log.info("OMS: publish toOMS");
            JmsTemplate template = getJmsTemplate();
            String transactionId = entityEvent.getHeaders().get("intuit_tid");
            String encryptedPayload = encrypt(PayslipPublisherOMSSettings.getIdpsQualifiedPrivateKeyName(),
                    PayslipPublisherOMSSettings.getIdpsSubscriberNameSpace(),
                    entityEvent.getPayload(), transactionId);
            log.info("OMS: Encrypted Payload is built");
            template.send(new OMSMessageCreator(transactionId,
                    encryptedPayload, entityEvent.getHeaders()));
            log.info("OMS: published to OMS successfully");
        } catch (Exception ex){
            log.error("OMS: Issue in publishing to JMS with tid={} and Exception ={}", entityEvent.getHeaders().get("intuit_tid"), ex.getMessage(), ex);
        }
    }


    private String encrypt(String qualifiedPrivateKeyName, String subscriberNameSpace, String payload, String tid) {

        Properties properties = new Properties();
        properties.put("policy_id", PayslipPublisherOMSSettings.getIdpsPolicyId());
        properties.put("endpoint", PayslipPublisherOMSSettings.getIdpsEndPoint());
        properties.put("access_type", "kube");

        String encryptedMsg;

        try {
            IdpsEncrypterEngine encrypter = new IdpsEncrypterEngine(qualifiedPrivateKeyName, subscriberNameSpace,
                    new IdpsConnectionManager(properties));
            encryptedMsg = new String(encrypter.encrypt(payload.getBytes(), tid));
            log.info("Action=OMS_Encrypt, payloadSize={}, encryptedMsgSize={}", payload.length(), encryptedMsg.length());
        }
        catch (Exception ex){
            log.info("Exception occured during encryption with tid={} and Exception = {}",tid,ex.getMessage(),ex);
            throw new RuntimeException(String.format("Exception occured while encryption=%s"),ex);
        }

        return encryptedMsg;
    }

    @Override
    protected Class getRestrictedCDMMapperClass() {
        return PrivilegedPayslipCDM.class;
    }
    @Override
    protected Class getCDMMapperClass() {
        return PrivilegedPayslipCDM.class;
    }


}

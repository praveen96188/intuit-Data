package com.intuit.sbd.payroll.psp.entity.publisher.payslip;
import com.intuit.platform.integration.messaging.crypto.engine.IdpsEncrypterEngine;
import com.intuit.platform.integration.messaging.crypto.provider.IdpsConnectionManager;
import com.intuit.payroll.api.payslip.model.PayslipCDM;
import com.intuit.payroll.api.payslip.model.PrivilegedPayslipCDM;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.AbstractEntityPublisher;
import com.intuit.sbd.payroll.psp.mapper.cdm.CDMMapper;
import com.intuit.sbg.nucleus.model.ResourceModel;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaSDKPublisher;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import com.intuit.sbd.payroll.psp.entity.publisher.EntityEvent;
import org.springframework.jms.connection.CachingConnectionFactory;
import com.intuit.platform.messaging.pulsar.jms.client.PulsarConnectionFactory;
import com.intuit.platform.messaging.pulsar.jms.client.PulsarQueue;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Security;
import java.util.*;

import static com.intuit.sbd.payroll.psp.configuration.ConfigurationManager.getSettingValue;
import static com.intuit.sbd.payroll.psp.entity.publisher.payslip.EventHeaders.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.*;

@Component
@Slf4j
/* Imp Note: Partner systems who just need Payslip data should be supplied PayslipCDM, not PrivilegedPayslipCDM */
public class PayslipPublisher extends AbstractEntityPublisher<Paycheck> {

    private static final String DELIMITER = "-";
    private static final String PAYSLIP_ENTITY_TYPE = "Payslip";
    private static final String PSP_APPID = "Intuit.sbg.psp";
    private static final String BANK_VERIFY_DATE = "bank_verify_date";
    private static final String DIRECT_DEPOSIT = "direct_deposit";
    private static final String TOPIC_PAYSLIP = "sbseg-dtpayroll-payslip-restricted";

    private static final String IS_CONSUME_OMS_MESSAGE = "isConsumeOMSMessage";
    private final OMSPayslipPublisher omsPayslipPublisher;

    String activeProfile;  

    @Autowired
    public PayslipPublisher(KafkaSDKPublisher kafkaPublisher, CDMMapper cdmMapper, @Value("${spring.profiles.active}") String activeProfile) {
        super(kafkaPublisher, cdmMapper);
        this.activeProfile = activeProfile.replace("iks", "");
        this.activeProfile = this.activeProfile.replace("dr", "");
        this.omsPayslipPublisher = new OMSPayslipPublisher(cdmMapper,activeProfile);
    }

    @Override
    public boolean publishRestricted(EntityContext entityContext) {
        boolean isOMSPublished = this.omsPayslipPublisher.publishRestricted(entityContext);
        return super.publishRestricted(entityContext) && isOMSPublished;
    }
    @Override
    public boolean publish(EntityContext entityContext) {
        boolean isOMSPublished = this.omsPayslipPublisher.publish(entityContext);
        return super.publish(entityContext) && isOMSPublished;
    }


    @Override
    protected Class getCDMMapperClass() {
        return PrivilegedPayslipCDM.class;
    }

    @Override
    protected String getTopic() {
        String topicName = TOPIC_PAYSLIP;
        if (activeProfile.equalsIgnoreCase("prd")) {
            return topicName;
        }
        return String.join(DELIMITER, topicName, activeProfile);
    }

    @Override
    protected Class getRestrictedCDMMapperClass() {
        return PrivilegedPayslipCDM.class;
    }

    @Override
    public Class<?> getEntityClass() {
        return Paycheck.class;
    }

    @Override
    protected Company getCompany(EntityContext<Paycheck> entityContext) {
        Paycheck paycheck = entityContext.getCurrentEntity();
        return paycheck.getCompany();
    }

    @Override
    protected Map<String, String> getHeaders(EntityContext<Paycheck> entityContext) {

        if (isNull(entityContext) || isNull(entityContext.getCurrentEntity())) {
            throw new IllegalArgumentException("Paycheck_CurrentEntity is null");
        }

        Company company = getCompany(entityContext);

        if (isNull(company)) {
            throw new IllegalArgumentException("Paycheck_Company is null");
        }

        Map<String, String> map = new HashMap<>();
        addHeaders(map, entityContext, company);

        // commented below because Risk ignore Payslip message if changed_attributes does not contain atleast one of:
        // approved, moneyDistributionLines, checkDate, approvedDate
        /*String changedAttributes = isNull(entityContext.getChangedAttributes()) ? EMPTY :
                Arrays.toString(entityContext.getChangedAttributes().toArray());

        if (!isBlank(changedAttributes)) {
            map.put(CHANGED_ATTRIBUTES, changedAttributes);
        }*/

        SpcfCalendar modifiedDate = entityContext.getCurrentEntity().getModifiedDate();
        if (nonNull(modifiedDate)) {
            long modifiedDateLong = modifiedDate.getTimeInMilliseconds();
            map.put(DISPATCH_TIMESTAMP, String.valueOf(modifiedDateLong));
        }

        map.put(BANK_VERIFY_DATE, getBankVerifyDateString(company));
        map.put(DIRECT_DEPOSIT, nonNull(entityContext.getCurrentEntity().getDDEmployee()) ? "true" : "false");

        return map;
    }

    protected void overrideEntityVersion(ResourceModel object, String entityVersion) {
        // no-op
    }

    private void addHeaders(Map<String, String> map, EntityContext<Paycheck> entityContext, Company company) {

        String entityVersion = String.valueOf(entityContext.getCurrentEntity().getVersion());
        if (!isBlank(entityVersion)) {
            map.put(ENTITY_VERSION, entityVersion);
        } else {
            map.put(ENTITY_VERSION, "1");
        }
        String pspOfferingId = getSettingValue(ConfigurationModule.BatchJobs, "psp_offeringid");
        map.put(INTUIT_OFFERINGID, pspOfferingId);
        String sessionId = entityContext.getCurrentEntity().getSessionId();
        if (!isBlank(sessionId)) {
            map.put(SESSION_ID, sessionId);
        }
        map.put(INTUIT_APPID, PSP_APPID);
        String entityId = entityContext.getEntityId().toString();
        if (!isBlank(entityId)) {
            map.put(ENTITY_ID, entityId);
        }
        map.put(INTUIT_LOCALE, String.valueOf(Locale.ENGLISH));
        if (nonNull(entityContext.getEventEnumType())) {
            map.put(EVENT_TYPE, entityContext.getEventEnumType().toString());
        }
        map.put(ENTITY_TYPE, PAYSLIP_ENTITY_TYPE);
        map.put(INTUIT_SERVICETYPE, SourceSystemCode.QBDT.toString());
        map.put(COUNTRYCODE, "US");
        map.put(BACK_OFFICE_ID, company.getSourceCompanyId());
        map.put(INTUIT_COUNTRY, "US");
        map.put(INTUIT_ENCRYPTED, "true");
        map.put(INTUIT_COMPRESSED, "false");
        map.put(INTUIT_REALMID, isAllBlank(company.getIAMRealmId()) ? EMPTY : company.getIAMRealmId());
        map.put(SOURCE_TID, isAllBlank(entityContext.getSourceTid()) ? EMPTY : entityContext.getSourceTid());
        map.put(INTUIT_TID, UUID.randomUUID().toString());
        map.put(IDEMPOTENCE_KEY, UUID.randomUUID().toString());
        map.put(INTUIT_DESTINATION, getTopic());
        map.put(INTUIT_JMSBODYNAMESPACE, PayslipCDM.class.getName());
        map.put(IS_CONSUME_OMS_MESSAGE,"false");
    }

    private String getBankVerifyDateString(Company company) {
        try {
            if (isNull(company)) {
                log.warn("Action=getBankVerifyDateString, msg=company_is_null");
                return EMPTY;
            }

            CompanyBankAccount bankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
            if (nonNull(bankAccount) && nonNull(bankAccount.getStatusEffectiveDate())) {
                return String.valueOf(bankAccount.getStatusEffectiveDate().getTimeInMilliseconds());
            }

            return EMPTY;
        } catch (Exception e) {
            log.warn("Action=getBankVerifyDateString, msg={}" + e.getMessage(), e);
            return EMPTY;
        }
    }
}

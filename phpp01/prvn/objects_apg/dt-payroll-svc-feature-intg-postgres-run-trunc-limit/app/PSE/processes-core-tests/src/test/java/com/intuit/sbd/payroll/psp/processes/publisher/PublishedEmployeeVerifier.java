package com.intuit.sbd.payroll.psp.processes.publisher;

import com.google.gson.JsonObject;
import com.intuit.eventbus.exceptions.FormatException;
import com.intuit.eventbus.utils.Result;
import com.intuit.payroll.api.employee.model.EmployeeCDM;
import com.intuit.payroll.api.shared.model.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.mapper.jackson.CustomObjectMapperResolver;
import com.intuit.sbg.psp.events.core.kafka.EventHeaders;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class PublishedEmployeeVerifier {

    private static final String DESKTOP = "Desktop";
    private static final String BACK_OFFICE_ID = "backOfficeId";

    private static final int MAX_RETRY = 30;
    private TestConsumer consumer;
    private CustomObjectMapperResolver customObjectMapperResolver;
    private CDMVerifier cdmVerifier;

    public PublishedEmployeeVerifier(){
        consumer = new TestConsumer("sbseg-dtpayroll-employee-restricted-test", true);
        customObjectMapperResolver = new CustomObjectMapperResolver();
        cdmVerifier = new CDMVerifier();
    }

    public void init(){
        consumer.seekLatest();
    }


    public void verifyEmployeePublished(String employeeId, EventEnumType eventType, Set<String> changedAttributes) throws IOException {

        verifyEmployeePublishStatusInEntityUpdate(employeeId, Employee.class.getSimpleName(), eventType, changedAttributes);
        verifyEmployeePublishedThroughConsumer(employeeId, eventType, changedAttributes);
    }

    public void verifyEmployeeNotPublished(String employeeId) {
        assertNull("Employee event found in Entity Update", getEntityUpdate(employeeId));
    }

    public void verifyEmployeePublishedThroughConsumer(String employeeId, EventEnumType eventType, Set<String> changedAttributes) throws IOException {
        ConsumerRecords<String, Result<FormatException, String>> consumerRecords = consumer.getMessages();

        assertEquals("records consumed mismatch ", 1, consumerRecords.count());

        Map<String, String> map = createHeaderMap(employeeId, eventType, changedAttributes);
        for (ConsumerRecord<String, Result<FormatException, String>> consumerRecord: consumerRecords) {
            verifyConsumedHeaders(consumerRecord.headers(), map, changedAttributes);
            verifyConsumedPayload(consumerRecord.value().get(), employeeId);
        }
    }

    public void verifyConsumedPayload(String payload, String employeeId) throws IOException {
        EmployeeCDM employeeCdm = customObjectMapperResolver.deserialize(payload, EmployeeCDM.class);
        assertNotNull("Failed to parse consumed message", Objects.isNull(employeeCdm));

        try{
            Application.beginUnitOfWork();
            Employee employee = Application.findById(Employee.class, new SpcfUniqueIdImpl(employeeId));

            cdmVerifier.verifyEmployeeCdm(employeeCdm, employee);
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    private Map<String, String> createHeaderMap(String employeeId, EventEnumType eventType, Set<String> changedAttributes){
        try {
            Application.beginUnitOfWork();
            Employee employee = Application.findById(Employee.class, new SpcfUniqueIdImpl(employeeId));

            Map<String, String> map = new HashMap<>();

            map.put(EventHeaders.REALM_ID, StringUtils.isAllBlank(employee.getCompany().getIAMRealmId()) ? StringUtils.EMPTY : employee.getCompany().getIAMRealmId());
            map.put(BACK_OFFICE_ID, employee.getCompany().getSourceCompanyId());
            map.put(EventHeaders.ENTITY_ID, employee.getId().toString());
            map.put(EventHeaders.SOURCE, DESKTOP);
            map.put(EventHeaders.EVENT_TYPE, getEventTypeName(eventType));
            map.put(EventHeaders.ENTITY_VERSION, String.valueOf(employee.getVersion()));

            return map;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private void verifyChangedAttribute(String changedAttributes, Set<String> changedAttributeSet){
        changedAttributes = changedAttributes.replace('[',' ');
        changedAttributes = changedAttributes.replace(']',' ');

        String[] attributes = changedAttributes.trim().split(",");
        if(CollectionUtils.isEmpty(changedAttributeSet) && (attributes.length == 0 || (attributes.length==1 && StringUtils.isBlank(attributes[0]))))
            return;

        assertEquals("changed attributes do not match",changedAttributeSet.size(), attributes.length);
        for (String att: attributes) {
            assertTrue("changed attributes do not match",changedAttributeSet.contains(att.trim()));
        }
    }

    String getEventTypeName(EventEnumType type) {
        switch (type) {
            case EntityCreate:
                return "ENTITY_CREATE";
            case EntityUpdate:
                return "ENTITY_UPDATE";
            case EntityDelete:
                return "ENTITY_DELETE";
        }
        return null;
    }

    private void verifyConsumedHeaders(Headers headers, Map<String, String> headerMap, Set<String> changedAttributes){

        for (Header header:headers) {
            if(headerMap.containsKey(header.key())) {
                assertEquals(String.format("header %s did not match", header.key()), headerMap.get(header.key()), new String(header.value()));
            } else if(header.key().equals(EventHeaders.CHANGED_ATTRIBUTES)){
                verifyChangedAttribute(new String(header.value()), changedAttributes);
            }
        }

    }


    public void verifyEmployeePublishStatusInEntityUpdate(String entityId, String entityName, EventEnumType eventType, Set<String> changedAttributes){
        int retry = 0;
        while(retry < MAX_RETRY) {
            try {
                boolean isVerified = verifyEntityUpdate(entityId, entityName, eventType, com.intuit.sbd.payroll.psp.domain.Status.Published, changedAttributes);
                if(isVerified){
                    return;
                }
                retry++;
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assertTrue("EntityUpdate not published", false);
    }

    private boolean verifyEntityUpdate(String entityId, String entityName, EventEnumType eventType, Status status, Set<String> changedAttribute){
        try{
            Application.beginUnitOfWork();

            EntityUpdate entityUpdate = getEntityUpdate(entityId);
            assertNotNull("Could not find EntityUpdate",entityUpdate);

            if(entityUpdate.getStatus() != Status.InProgress){
                assertEquals("EntityUpdate status do not match", status, entityUpdate.getStatus());
                assertEquals("EntityUpdate modifier id do not match", "EntityPublisher", entityUpdate.getModifierId());
                assertEquals("EntityUpdate entity name do not match", entityName, entityUpdate.getEntityName());
                assertEquals("EntityUpdate event type do not match", eventType.toString(), entityUpdate.getEventType().toString());
                verifyChangedAttributes(entityUpdate.getChangedAttribute(), changedAttribute);
                return true;
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private EntityUpdate getEntityUpdate(String entityId){
        EntityUpdate entityUpdate = Application.find(EntityUpdate.class, new com.intuit.sbd.payroll.psp.query.Query<EntityUpdate>()
                .Where(EntityUpdate.EntityId().equalTo(entityId)
                        .And(EntityUpdate.Status().equalTo(Status.InProgress)
                                .Or(EntityUpdate.ModifierId().equalTo("EntityPublisher"))))).getFirst();

        return entityUpdate;
    }

    private void verifyChangedAttributes(JsonObject actualChangedAtrributes, Set<String> expectedChangedAttribute){

        String strActualChangedAtrributes = Objects.isNull(actualChangedAtrributes)? StringUtils.EMPTY : actualChangedAtrributes.toString();
        String[] attributes = StringUtils.isBlank(strActualChangedAtrributes)? new String[0] : strActualChangedAtrributes.split(",");

        if(Objects.isNull(actualChangedAtrributes)){
            assertEquals("EntityUpdate change attributes do not match", 0, attributes.length);
            return;
        }

        assertEquals("EntityUpdate change attributes do not match", expectedChangedAttribute.size(), attributes.length);
        for (String attribute: attributes) {
            assertTrue("EntityUpdate change attributes do not match",expectedChangedAttribute.contains(attribute));
        }
    }

}

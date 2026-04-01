package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.WorkflowFinderConstants;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.EntityPublisher;
import com.intuit.sbd.payroll.psp.entity.publisher.PublisherFactory;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.employee.EmployeePublishStatusWorkflows;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This RTB job is designed primarily to publish eq eligible employees blocked for publishing at the time of initial load.
 * Publish_status 1st bit set to 5
 * It can also be used to publish list of employees provided in a csv
 */
public class PublishEmployeeInitialLoad {
    private static SpcfLogger logger = SpcfLogManager.getLogger(PublishEmployeeInitialLoad.class);
    private static long size = 5000;
    private static String db = "Oracle";
    private static String filePath;
    private PublisherFactory publisherFactory;
    private DomainEntitySet<Employee> employeeList;
    private static int success =0;
    private static int fail=0;

    public PublishEmployeeInitialLoad() {
        this.publisherFactory = PayrollApplicationBeanFactory.getBean(PublisherFactory.class);
    }

    public static void main(String[] args) throws Exception {
        try {
            parseArgs(args);
            Application.initialize();
            Application.beginUnitOfWork();
            PublishEmployeeInitialLoad publishEmployeeInitialLoad = new PublishEmployeeInitialLoad();
            publishEmployeeInitialLoad.publishEmployees();
            Application.commitUnitOfWork();
        }catch (Exception e){
            logger.error("Exception in PublishEmployeeInitialLoad:", e);
        }finally {
            Application.rollbackUnitOfWork();
            Application.uninitialize();
        }
    }

    private void publishEmployees() {
        if(filePath !=null){
            employeeList = findEmployeesFromListToBePublished(filePath);
        }else {
            employeeList = findEmployeesToBePublished();
        }
        logger.info("Successfully got entities to be published " + employeeList.size());
        Set<EntityContext> entityContexts = createEntityContext(employeeList);
        logger.info("Successfully created entities to be published " + employeeList.size());
        publishEvent(entityContexts);
        logger.info("Publishing done success:"+success+" failures:"+fail);
    }

    private DomainEntitySet<Employee> findEmployeesFromListToBePublished(String employeeString) {
        List<SpcfUniqueId> employees= read(filePath);

        DomainEntitySet<Employee> employeeList= Application.find(Employee.class, new Query<Employee>().Where(Employee.Id().in(employees)));
        return employeeList;
    }

    private void publishEvent(Set<EntityContext> entityContexts) {
        for (EntityContext entityContext : entityContexts) {
            publishEntityContext(entityContext);
        }
    }

    private boolean publishEntityContext(EntityContext context) {
        boolean isPublished = false;
        try {
            EntityPublisher publisher = publisherFactory.getPublisher(context.getEntityType());
            isPublished = publisher.publish(context);
        } catch (Exception e) {
            logger.error(String.format("Action=Publish_Failed_Exception, EntityContext=%s, ExceptionStackTrace=", context.toString(), e));
        } finally {
            //todo: new state to handle invalid publish request
            PublishStatusWorkflowState status = isPublished ? PublishStatusWorkflowState.DONE : PublishStatusWorkflowState.ERROR;
            updateEventStatus(context, status);
            logger.info(String.format("PublishStatus=%s, EntityContext=%s", status, context.getEntityId()));
            if(isPublished)
                success = success+1;
            else
                fail = fail+1;
            return isPublished;
        }
    }

    private void updateEventStatus(EntityContext context, PublishStatusWorkflowState status) {
        Employee employee = Application.findById(Employee.class,
                SpcfUniqueId.createInstance(context.getEventId()));
        employee.setPublishStatusWorkflowState(EmployeePublishStatusWorkflows.EMS, status);
        Application.save(employee);
    }


    private DomainEntitySet<Employee> findEmployeesToBePublished() {
        StringBuffer query = new StringBuffer("select ee from com.intuit.sbd.payroll.psp.domain.Employee ee where (SUBSTR(ee.PublishStatus,1, 1) = 5)");
        /*org.hibernate.Query hibernateQuery = Application.createHibernateQuery("select ee\n" +
                        "                from com.intuit.sbd.payroll.psp.domain.Employee ee\n" +
                        "                where (SUBSTR(ee.PublishStatus,1, 1) = 5)\n" +
                        "                and rownum <= :max_fetch_records");*/
        if(db.equalsIgnoreCase("Postgres")) {
            query.append(" limit <= :max_fetch_records");
        }else {
            query.append(" and rownum <= :max_fetch_records");
        }
        org.hibernate.Query hibernateQuery = Application.createHibernateQuery(query.toString());
        hibernateQuery.setParameter("max_fetch_records", size);
        List<Employee> employees= hibernateQuery.list();
        DomainEntitySet<Employee> employeeList= new DomainEntitySet();
        employeeList.addAll(employees);
        return employeeList;
    }

    private Set<EntityContext> createEntityContext(DomainEntitySet<Employee> employeeList) {
        Set<EntityContext> entityContexts = new HashSet<>();
        for (Employee employee : employeeList) {
            entityContexts.add(createEntityContext(employee));
        }
        return entityContexts;
    }

    private EntityContext createEntityContext(Employee employee) {
        DomainEntity domainEntity = employee;
        EntityContext entityContext = new EntityContext(employee, EventEnumType.EntityCreate);
        entityContext.setEventId(employee.getId().toString());
        entityContext.setCurrentEntity(domainEntity);
        entityContext.setCompany(employee.getCompany());
        return entityContext;
    }

    private static void parseArgs(String[] args) throws Exception {
        for (String arg : args) {
            String argParts[] = arg.split("=");
            switch (argParts[0].trim()) {
                case "Size":
                    size = Integer.parseInt(argParts[1]);
                    break;
                case "FilePath":
                    filePath = argParts[1];
                    break;
                case "DB":
                    db= argParts[1];
            }
        }
    }

    public List<SpcfUniqueId> read(String filePath){
        logger.info("Event=FileRead, Status=Start, FileName=" + filePath);
        List<SpcfUniqueId> lines = new ArrayList<>();
        Path path = Paths.get(filePath);
        try {
            Files.lines(path).forEach(line -> {
                if(StringUtils.isNotBlank(line)) {
                    lines.add(SpcfUniqueId.createInstance(line.replace(WorkflowFinderConstants.DOUBLE_QUOTES, StringUtils.EMPTY)));
                }
            });
        } catch (IOException e) {
            logger.warn("Event=FileRead, Status=Failed, FileName=" + filePath);
        }
        logger.info("Event=FileRead, Status=Done, FileName=" + filePath + ", Total Workflows=" + lines.size());
        return lines;
    }

}

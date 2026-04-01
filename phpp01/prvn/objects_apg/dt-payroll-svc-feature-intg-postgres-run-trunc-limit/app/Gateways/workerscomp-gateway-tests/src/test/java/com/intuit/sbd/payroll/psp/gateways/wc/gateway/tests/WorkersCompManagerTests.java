package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.bp.wc.common.schema.Employee;
import com.intuit.bp.wc.common.schema.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventResponseDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompServiceDelegate;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;


/**
 * Author: Sriram Nutakki
 * Date created: 11/14/12
 */
public class WorkersCompManagerTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

        /////////////////////// clean up data
        // remove WC_SYNC_TOKEN from SystemParameter table
        try {
        PayrollServices.beginUnitOfWork();
        String[] paramNames = new String[1];
        paramNames[0] = "systemParameterCd";

        Object[] paramValues = new Object[1];
        paramValues[0] = SystemParameter.Code.WC_SYNC_TOKEN.toString();

        String hql = " Select systemParameter " +
                " from com.intuit.sbd.payroll.psp.domain.SystemParameter as systemParameter " +
                " where systemParameter.SystemParameterCd=:systemParameterCd";

        SystemParameter sysParam = (SystemParameter)Application.findByHQLQuery(hql, paramNames, paramValues, 0, 25).get(0);
        Application.delete(SystemParameter.class, sysParam.getId());
        PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {

        }
    }

    @Test
    public void testAddWorkersCompService() throws Exception {

        // Add subscription
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        CompanyService cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());

        // Deactivate subscription
        psid = String.valueOf(System.currentTimeMillis());
        company = TestUtil.createCompanyEmployeesAndPayroll(psid);
        subscription.setPSID(psid);
        subscription.setActive(true);
        manager.saveSubscriptionChanges(subscriptions);
        subscription.setPSID(psid);
        subscription.setActive(false);
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, workersCompService.getStatusCd());
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, cloudV2Service.getStatusCd());
    }

    @Test
    public void testReactivateWorkersCompServiceAssisted() throws Exception {
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);
        testReactivateWorkersCompService(company);
    }

    @Test
    public void testReactivateWorkersCompServiceDIY() throws Exception {
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createDIYCompany(psid);
        testReactivateWorkersCompService(company);
    }

    private void testReactivateWorkersCompService(Company company) throws Exception {
        String psid = company.getSourceCompanyId();
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        CompanyService workersCompService = null;
        WorkersCompSubscriptions result = null;

        // Deactivate subscription (Workerscomp service was never activated)
        subscription.setActive(false);
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertNull(workersCompService);
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);

        // Add subscription
        subscription.setActive(true);
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
        CompanyService cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());

        // Activate again
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());

        // Deactivate
        subscription.setActive(false);
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, workersCompService.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, cloudV2Service.getStatusCd());

        // Deactivate again
        subscription.setActive(false);
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, workersCompService.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, cloudV2Service.getStatusCd());

        // Reactivate
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2010, 5, 20));
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
        Assert.assertEquals(2010, workersCompService.getServiceStartDate().getYear());
        Assert.assertEquals(6, workersCompService.getServiceStartDate().getMonth());
        Assert.assertEquals(20, workersCompService.getServiceStartDate().getDay());
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());
    }

    @Test
    public void testClodV2ServiceEdgeCases() throws Exception {

        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);

        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        CompanyService workersCompService = null;
        WorkersCompSubscriptions result = null;

        // Add WC subscription when CloudV2 is in cancelled state
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
        serviceInfoDTO.setServiceStartDate(SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(
                WCUtil.createXMLGregorianCalendar(2005, 5, 20)));
        serviceInfoDTO.setServiceCode(ServiceCode.CloudV2);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.addService(
                SourceSystemCode.QBDT, psid, serviceInfoDTO);
        PayrollServices.commitUnitOfWork();
        company = DataLoadServices.refreshCompany(company);
        CompanyService cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());
        Assert.assertTrue(processResult.isSuccess());
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.deactivateService(
                SourceSystemCode.QBDT, psid, ServiceCode.CloudV2);
        PayrollServices.commitUnitOfWork();
        company = DataLoadServices.refreshCompany(company);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, cloudV2Service.getStatusCd());
        Assert.assertTrue(processResult.isSuccess());
        subscription.setActive(true);
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);

        // Reactivate WC subscription when CloudV2 service record is not there in the DB
        subscription.setActive(false);
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, workersCompService.getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, cloudV2Service.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
        PayrollServices.beginUnitOfWork();
        Application.delete(cloudV2Service);
        DomainEntitySet<CompanyOffering> companyOfferings = Application.find(CompanyOffering.class);
        for (CompanyOffering companyOffering : companyOfferings) {
            if (companyOffering.getOffering().getServiceCode() == ServiceCode.CloudV2) {
                Application.delete(companyOffering);
            }
        }
        PayrollServices.commitUnitOfWork();
        company = DataLoadServices.refreshCompany(company);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertNull(cloudV2Service);
        subscription.setActive(true);
        result = manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        workersCompService = company.getService(ServiceCode.WorkersComp);
        cloudV2Service = company.getService(ServiceCode.CloudV2);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, cloudV2Service.getStatusCd());
        Assert.assertEquals(result.getWorkersCompSubscription().get(0).getPSID(), psid);
    }



    @Test
    public void testGetCompaniesWithWCPendingPaychecks() throws Exception {

        // Add subscription
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);


        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        assertNotNull(workersCompService);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        manager.saveSubscriptionChanges(subscriptions);



        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);

        for (String companyId : dto.getIncludedPaychecksByCompany().keySet()) {
            List<WorkersCompPaycheck> paychecks = dto.getIncludedPaychecks(companyId);
            if (paychecks != null && paychecks.size() > 0) {
                manager.markAsSent(paychecks);
            }
        }
    }

    @Test
    public void testGetPreviousFein() {
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);
        String fein = company.getFedTaxId();
        String newFein = "987654321";

        //-- Update Company FEIN
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();
        serviceInfoDTO.setServiceCode(ServiceCode.Tax);
        DateDTO serviceStartDate = new DateDTO(2012, 11, 11);
        serviceInfoDTO.setServiceStartDate(serviceStartDate.toSpcfCalendar());
        ProcessResult<CompanyService> processResult = PayrollServices.companyManager.addService(
                SourceSystemCode.QBDT,
                psid,
                serviceInfoDTO);
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein(newFein);
        PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        //-- Get Previous FEIN
        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                        .Where(
                                CompanyEvent.Company().equalTo(company)
                                            .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.EINChanged)));
        ((Query<CompanyEvent>) query).EagerLoad(CompanyEvent.CompanyEventDetailSet());
        List<CompanyEvent> events = Application.executeQuery(CompanyEvent.class, query);
        assertNotNull(events);
        assertTrue(events.size() > 0);

        // Sort in reverse chronological order
        Collections.sort(events, new Comparator<CompanyEvent>() {
            public int compare(CompanyEvent o1, CompanyEvent o2) {
                return o2.getCreatedDate().compareTo(o1.getCreatedDate());
            }
        });

        // Get old and new FEINs
        HashMap<EventDetailTypeCode, String> eventDetails = events.get(0).getEventDetailInfo();
        assertEquals(eventDetails.get(EventDetailTypeCode.OldStringValue), fein);
        assertEquals(eventDetails.get(EventDetailTypeCode.NewStringValue), newFein);
    }


    @Test
    public void shouldBeNullForInvalidPaymentType() {
        initializeForPmtTypeAndOvertime(1.5, new long[]{816,0});

        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();

        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);

        for (Employee emp: dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem()){
            for (com.intuit.bp.wc.common.schema.Paycheck pc: emp.getPaychecks().getItem()){
                for (PaycheckItem pci: pc.getPaycheckItems().getItem()){

                    if (pci.getType() == PaycheckItemType.PAY){
                        assertEquals(1.5, pci.getOverTimeMultiplier() );
                        assertNull(pci.getPayItemName());
                        assertEquals("My description!", pci.getPayCustomItemName());
                    }
                }
            }
        }
    }

    @Test
    public void shouldGetMappedPaymentTypeAndOverTimeQualifier() {
        initializeForPmtTypeAndOvertime(1.5, new long[]{1000026});

        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();

        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);

        for (Employee emp: dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem()){
              for (com.intuit.bp.wc.common.schema.Paycheck pc: emp.getPaychecks().getItem()){
                  for (PaycheckItem pci: pc.getPaycheckItems().getItem()){
                        if (pci.getType() == PaycheckItemType.PAY){
                            assertEquals(1.5, pci.getOverTimeMultiplier() );
                            assertEquals("OVERTIMEHOURLY", pci.getPayItemName());
                            assertEquals("My description!", pci.getPayCustomItemName());

                        }
                  }
              }
        }
    }

    private Company initializeForPmtTypeAndOvertime(double overTimeMultiplier, long [] payCodes) {  //randomly assign the paycodes if more than 1 is given
        Company company = TestUtil.createCompanyEmployeesAndComplexPayroll();
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<PayrollRun> payrollRunSet = PayrollRun.findPayrollRuns(company);
        Random rnd = new Random();
        for (PayrollRun p: payrollRunSet ){
            DomainEntitySet<Paycheck> payChecks = p.getPaycheckCollection()     ;
            for (Paycheck pc: payChecks){
                DomainEntitySet<Compensation> compensationSet= pc.getCompensationCollection();

                for (Compensation comp: compensationSet){

                    QbdtPayrollItemInfo qbInfo =  new QbdtPayrollItemInfo();
                    qbInfo.setOvertimeMultiplier(overTimeMultiplier);

                    qbInfo.setDetailType(payCodes[rnd.nextInt(payCodes.length)]);             //LAW_AZSUI

                    qbInfo.setCompanyPayrollItem(comp.getCompanyPayrollItem());
                    Application.save(qbInfo);
                }
            }
        }
        PayrollServices.commitUnitOfWork();
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(company.getSourceCompanyId());
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        subscriptions.getWorkersCompSubscription().add(subscription);
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        return company;
    }


    @Test
    public void testGetCompaniesWithWCPendingPaychecksWithFullPayroll() {
        final int totalCompaniesToCreate = 1;

        List<Company> companies = new ArrayList<Company>(totalCompaniesToCreate);

        for (int i = 0; i < totalCompaniesToCreate; i++) {
            Company company = TestUtil.createCompanyEmployeesAndComplexPayroll();


                companies.add(company);
        }

        // Add subscr
        // iption
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();

        for (Company company : companies) {
            WorkersCompSubscription subscription = new WorkersCompSubscription();
            subscription.setPSID(company.getSourceCompanyId());
            subscription.setActive(true);
            subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
            subscriptions.getWorkersCompSubscription().add(subscription);
        }

        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);

        for (Company company : companies) {
            company = DataLoadServices.refreshCompany(company);
            CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
            assertNotNull(workersCompService);
            Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        }

        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);

        for (String companyId : dto.getIncludedPaychecksByCompany().keySet()) {
            List<WorkersCompPaycheck> paychecks = dto.getIncludedPaychecks(companyId);
            if (paychecks != null && paychecks.size() > 0) {
                manager.markAsSent(paychecks);
            }
        }
    }

    @Test
    public void testPendingPaychecksWithMissingEmployeeInfo() throws Exception {

        // Create Company
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid, 2, 3);

        // Subscribe to workerscomp
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        assertNotNull(workersCompService);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
        manager.saveSubscriptionChanges(subscriptions);

        // Get pending paychecks
        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Get pending paychecks after setting employee work state to null
        Employee employee = dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0);
        PayrollServices.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Employee domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        String workState = domainEmployee.getWorkState();
        domainEmployee.setWorkState(null);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        //return employees' paychecks even if workstate is null
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Back to normal
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        domainEmployee.setWorkState(workState);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Get pending paychecks after setting employee first name to null
        employee = dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0);
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        String firstName = domainEmployee.getFirstName();
        domainEmployee.setFirstName(null);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        //return paychecks even if employees' first name is null
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Back to normal
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        domainEmployee.setFirstName(firstName);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Get pending paychecks after setting employee first name to <EMPTY>
        employee = dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0);
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        firstName = domainEmployee.getFirstName();
        domainEmployee.setFirstName("<EMPTY>");
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        //return paychecks even if employees' first name is EMPTY
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());
        for (Employee e: dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem())    {
            if (employee.getEmployeeID().equals(e.getEmployeeID())){
                assertNull(e.getFirstName());
            }
        }

        // Back to normal
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        domainEmployee.setFirstName(firstName);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Get pending paychecks after setting employee last name to null
        employee = dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0);
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        String lastName = domainEmployee.getLastName();
        domainEmployee.setLastName(null);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        //return paychecks even if employees' last name is null
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Back to normal
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        domainEmployee.setLastName(lastName);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Get pending paychecks after setting employee last name to <EMPTY>
        employee = dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0);
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        lastName = domainEmployee.getLastName();
        domainEmployee.setLastName("<EMPTY>");
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);

        //return paychecks even if employees' last name is EMPTY
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());
        for (Employee e: dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem())    {
            if (employee.getEmployeeID().equals(e.getEmployeeID())){
                assertNull(e.getLastName());
            }
        }

        // Back to normal
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        domainEmployee.setLastName(lastName);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Get pending paychecks after setting employee first name, last name and work state to null
        employee = dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0);
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        workState = domainEmployee.getWorkState();
        firstName = domainEmployee.getFirstName();
        lastName = domainEmployee.getLastName();
        domainEmployee.setWorkState(null);
        domainEmployee.setFirstName(null);
        domainEmployee.setLastName(null);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        //return paychecks even if employees' first name, last name and workstate  is null
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());

        // Back to normal
        PayrollServices.beginUnitOfWork();
        domainEmployee = com.intuit.sbd.payroll.psp.domain.Employee.findEmployee(company, employee.getEmployeeID());
        domainEmployee.setWorkState(workState);
        domainEmployee.setFirstName(firstName);
        domainEmployee.setLastName(lastName);
        Application.save(domainEmployee);
        PayrollServices.commitUnitOfWork();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);
        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertEquals(6, dto.getIncludedPaychecks(psid).size());
        assertEquals(2, dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().size());
    }

    public void testGetCompaniesWithWCPendingPaychecksWithVoidedChecks() {

        Company company = TestUtil.createCompanyEmployeesAndComplexPayroll(true);
        // Add subscription
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();


        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(company.getSourceCompanyId());
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);

        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);


        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        assertNotNull(workersCompService);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());

        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        assertNotNull(dto.getPayroll());
        assertTrue("Paycheck status DELETE" , dto.getPayroll().getBusinesses().getItem().get(0).getEmployees().getItem().get(0).getPaychecks().getItem().get(0).getCheckStatus() == PaycheckStatusType.DELETE);

        for (String companyId : dto.getIncludedPaychecksByCompany().keySet()) {
            List<WorkersCompPaycheck> paychecks = dto.getIncludedPaychecks(companyId);
            if (paychecks != null && paychecks.size() > 0) {
                manager.markAsSent(paychecks);
            }
        }
    }


    @Test
    public void shouldChangeSentToPendingEdit() throws Exception {

        //**********************Data setup*******************************
        String psid = "99000123";
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 2, 10);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 2, 10);

        com.intuit.sbd.payroll.psp.domain.Employee employee = TestUtil.setupCompanyCreateEmployee(psid, SpcfCalendar.createInstance(2007, 2, 10), ServiceCode.WorkersComp);

        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
        PaystubDTO paystubDto = DataLoadServices.createPaystubDto(employee, beginDate, endDate, paycheckDate, "1000.00");

        DomainEntitySet<Paycheck> pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);

        TestUtil.savePaystub(employee,pcs.get(0), paystubDto);

        Paycheck pc = pcs.get(0);
        pc.getWorkersCompPaycheck().markAsSent();

        Application.commitUnitOfWork();

        //******************************************test****************************************

        //edit pay stub paystubDto is modified....
        // the paystub data should be updated and also the data in WorkersComp should be modified to PendingEdit
        paystubDto.setPreTaxDeducts("2000.00");

        Application.beginUnitOfWork();

        pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);
        Paystub paystub = Paystub.findPaystub(pcs.get(0));
        PayrollServices.paystubManager.updatePaystub(pcs.get(0), employee, paystub, paystubDto);
        Application.commitUnitOfWork();

        //********************************Validate*********************

        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(pcs.get(0));
        assertEquals(wcs.get(0).getCurrentStateCd(), WorkersCompPaycheckStateCode.PendingEdit)  ;

    }
    @Test
    public void shouldIgnoreIfWorkersCompCancelled() throws Exception {

        //**********************Data setup*******************************
        String psid = "99000123";
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 2, 10);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 2, 10);

        com.intuit.sbd.payroll.psp.domain.Employee employee = TestUtil.setupCompanyCreateEmployee(psid, SpcfCalendar.createInstance(2008, 2, 10), ServiceCode.WorkersComp);

        Application.beginUnitOfWork();
        SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2007, 2, 1);
        PaystubDTO paystubDto = DataLoadServices.createPaystubDto(employee, beginDate, endDate, paycheckDate, "1000.00");

        DomainEntitySet<Paycheck> pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);

        TestUtil.savePaystub(employee,pcs.get(0), paystubDto);

        Paycheck pc = pcs.get(0);
        pc.getWorkersCompPaycheck().markAsSent();


        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.WorkersComp);

        Application.commitUnitOfWork();

        //******************************************test****************************************

        //edit pay stub paystubDto is modified....
        // the paystub data should be updated and also the data in WorkersComp should be modified to PendingEdit
        paystubDto.setPreTaxDeducts("2000.00");

        Application.beginUnitOfWork();

        pcs = Paycheck.findPaychecksBySourceEmployee(employee.getCompany(), employee);
        Paystub paystub = Paystub.findPaystub(pcs.get(0));
        PayrollServices.paystubManager.updatePaystub(pcs.get(0), employee, paystub, paystubDto);
        Application.commitUnitOfWork();

        //********************************Validate*********************

        DomainEntitySet<WorkersCompPaycheck> wcs = WorkersCompPaycheck.findByPaycheck(pcs.get(0));
        assertEquals(wcs.get(0).getCurrentStateCd(), WorkersCompPaycheckStateCode.Sent)  ;

    }

    @Test
    public void testCreateCompany() throws Exception {
        // create sample company
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createDIYCompany("34563231");
        DataLoadServices.addCompanyPIN(company, "test1234");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        System.out.println("time now is " + PSPDate.getPSPTime().toString());

    }

        @Test
    public void testGetCompanyEvents() throws Exception {
        // create sample company
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);

        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        //////////////////// test 1: create events and get them
        // get token
        long startTime = manager.getToken();

        // create events
        createEvents(company);

        // get end time
        long endTime = PSPDate.getPSPTime().getTimeInMilliseconds();

        // get company events
        List<WCChangeEventDTO> events = manager.getCompanyChangeEvents(startTime, endTime);
        assertNotNull(events);
        assertEquals(2, events.size());

        // store the token
        manager.updateToken(endTime);

        ////////////////////// test2: get events again

        // get token
        startTime = manager.getToken();

        // get end time
        endTime = PSPDate.getPSPTime().getTimeInMilliseconds();

        // get company events
        events = manager.getCompanyChangeEvents(startTime, endTime);
        assertNotNull(events);
        assertEquals(0, events.size());

        // store the token
        manager.updateToken(endTime);
    }


    private void createEvents(Company company) {
        // EIN changed event
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = new DTOFactory().create(company);
        companyDTO.setLegalName(company.getLegalName());
        companyDTO.setFein("223000002");
        PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, company.getSourceCompanyId(), companyDTO);
        PayrollServices.commitUnitOfWork();

        // PSID changed event - TODO
        PayrollServices.beginUnitOfWork();
        Company c1 = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit eu1 = c1.getActiveEntitlementUnits().get(0);
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.deactivateEntitlementUnit(eu1);
        DataLoadServices.addDIYEntitlementUnit(c1, "12345678901234567890", "09876543210987654321", EditionType.Basic, NumberOfEmployeesType.UNLIMITED);
    }



}

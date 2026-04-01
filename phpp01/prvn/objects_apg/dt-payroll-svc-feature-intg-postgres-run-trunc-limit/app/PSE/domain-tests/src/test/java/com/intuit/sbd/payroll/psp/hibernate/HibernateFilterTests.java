package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import org.hibernate.SQLQuery;
import org.junit.*;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.*;

public class HibernateFilterTests {
    PSPRequestContextManager pspRequestContextManager;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
        pspRequestContextManager.clearRequestContext();
    }

    @After
    public void runAfterEachTest() {
        pspRequestContextManager.clearRequestContext();
    }
    @Ignore
    @Test
    public void FindByIdDifferentSessionTest(){
        //Create Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        CompanyEvent companyEvent = createCompanyEvent(foundCompany);

        //Set request Context
        pspRequestContextManager.setRequestContext(foundCompany, RequestType.TEST, "Test" );

        PayrollServices.commitUnitOfWork();

        //find company by Id
        PayrollServices.beginUnitOfWork();
        CompanyEvent foundEvent = Application.findById(CompanyEvent.class, companyEvent.getId());
        Assert.assertNotNull(foundEvent);
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void FindByIdSameSessionTest(){
        //Create Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        //create CompanyEvent
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        CompanyEvent companyEvent = createCompanyEvent(foundCompany);

        //Set request Context
        pspRequestContextManager.setRequestContext(foundCompany, RequestType.TEST, "Test" );

        //find company by Id
        CompanyEvent foundEvent = Application.findById(CompanyEvent.class, companyEvent.getId());
        Assert.assertNotNull(foundEvent);
        PayrollServices.commitUnitOfWork();

    }


    @Ignore
    @Test
    public void FindByCriteriaQueryDifferentSessionTest(){
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(foundCompany, EventTypeCode.ACHReturn);
        PayrollServices.commitUnitOfWork();

        //find company by Id
        PayrollServices.beginUnitOfWork();
        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                        .Where(CompanyEvent.Id().equalTo(companyEvent.getId()));
        DomainEntitySet<CompanyEvent> foundEvents = Application.find(CompanyEvent.class, query);
        Assert.assertEquals(1, foundEvents.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void UpdateSSTTest(){
        //Create Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        ApplicationSecondary.beginUnitOfWork();
        SourceSystemTransmission sourceSystemTransmission = new SourceSystemTransmission();
        sourceSystemTransmission.setTransmissionIdentifier("TestSSTIdentifier");
        sourceSystemTransmission.setHost("Test");
        sourceSystemTransmission.setDescription("Test Description");
        sourceSystemTransmission.setApplicationId("Test Application");
        sourceSystemTransmission.setTaxTableId("Test tax table");
        sourceSystemTransmission = ApplicationSecondary.save(sourceSystemTransmission);
        ApplicationSecondary.commitUnitOfWork();

        ApplicationSecondary.beginUnitOfWork();
        SourceSystemTransmission sourceSystemTransmission1 = new SourceSystemTransmission();
        sourceSystemTransmission1 = ApplicationSecondary.findById(SourceSystemTransmission.class, sourceSystemTransmission.getId());
        sourceSystemTransmission1.setCompanyId(foundCompany.getId().toString());
        ApplicationSecondary.save(sourceSystemTransmission1);
        ApplicationSecondary.commitUnitOfWork();



    }


    @Ignore
    @Test
    public void FindByCriteriaQuerySameSessionTest(){
        Application.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(foundCompany, EventTypeCode.ACHReturn);

        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                        .Where(CompanyEvent.Id().equalTo(companyEvent.getId()));
        DomainEntitySet<CompanyEvent> foundEvents = Application.find(CompanyEvent.class, query);
        Assert.assertEquals(1, foundEvents.size());
        Application.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testFilter(){
        String table_name = "PSP_COMPANY_EVENT";
        String seqName = String.format("%s%s", table_name.replace("PSP_", ""), "_SEQ");

        String regexTemplate = "select .* from %s ([^\\s]+) where ([^\\s]+).%s=\\?";
        String regex = String.format(regexTemplate, table_name, seqName);

        String sql = "select companyeve0_.COMPANY_EVENT_SEQ as company_event_seq1_55_0_, companyeve0_.VERSION as version2_55_0_, companyeve0_.CREATOR_ID as creator_id3_55_0_, companyeve0_.CREATED_DATE as created_date4_55_0_, companyeve0_.MODIFIER_ID as modifier_id5_55_0_, companyeve0_.MODIFIED_DATE as modified_date6_55_0_, companyeve0_.REALM_ID as realm_id7_55_0_, companyeve0_.EVENT_TIME_STAMP as event_time_stamp8_55_0_, companyeve0_.STATUS_EFFECTIVE_DATE as status_effective_d9_55_0_, companyeve0_.STATUS_CD as status_cd10_55_0_, companyeve0_.EVENT_TYPE_CD as event_type_cd11_55_0_, companyeve0_.EVENT_TOKEN as event_token12_55_0_, companyeve0_.SOURCE_ID as source_id13_55_0_, companyeve0_.NOTE_LAST_UPDATED_DATE as note_last_updated14_55_0_, companyeve0_.COMPANY_FK as company_fk15_55_0_ from PSP_COMPANY_EVENT companyeve0_ where companyeve0_.COMPANY_EVENT_SEQ=?";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        boolean res = matcher.matches();
        Assert.assertTrue(res);
    }

    @Test
    public void testCriteriaFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );
        PayrollServices.beginUnitOfWork();
        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                        .Where(CompanyEvent.Id().equalTo(companyEvent.getId()));
        DomainEntitySet<CompanyEvent> foundEvents = Application.find(CompanyEvent.class, query);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSetFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent1 = Application.findById(CompanyEvent.class, companyEvent.getId());
        DomainEntitySet<CompanyEventDetail> companyEventDetailCollection = companyEvent1.getCompanyEventDetailCollection();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHQLSelect() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        HqlBuilder hql = new HqlBuilder("select ce from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.StatusCd = :companyEventStatus");
        hql.setParameter("companyEventStatus", CompanyEventStatus.Active);
        List<CompanyEvent> companyEvents = hql.list();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHQLUpdate() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        StringBuilder hql = new StringBuilder("update com.intuit.sbd.payroll.psp.domain.CompanyEvent ce set ce.StatusCd = :newCompanyEventStatus where ce.StatusCd = :companyEventStatus");
        String[] paramNames = {"newCompanyEventStatus", "companyEventStatus"};
        Object[] paramValues = {CompanyEventStatus.Inactive, CompanyEventStatus.Active};
        Application.executeHQLUpdate(hql.toString(), paramNames, paramValues);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    @Ignore
    public void testHQLDelete() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        StringBuilder hql = new StringBuilder("delete from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.StatusCd = :companyEventStatus");
        String[] paramNames = {"companyEventStatus"};
        Object[] paramValues = {CompanyEventStatus.Active};
        Application.executeHQLUpdate(hql.toString(), paramNames, paramValues);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHQLSelectWithFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        Company company1 = createCompany1();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        HqlBuilder hql = new HqlBuilder("select ce from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.StatusCd = :companyEventStatus");
        hql.setParameter("companyEventStatus", CompanyEventStatus.Active);
        List<CompanyEvent> companyEvents = hql.list();
        PayrollServices.commitUnitOfWork();

        assertEquals(1, companyEvents.size());

        pspRequestContextManager.setRequestContext(company1, RequestType.TEST, "Test" );
        PayrollServices.beginUnitOfWork();
        companyEvents = hql.list();
        PayrollServices.commitUnitOfWork();

        assertEquals(0, companyEvents.size());
    }

    @Test
    public void testHQLUpdateWithFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        StringBuilder hql = new StringBuilder("update com.intuit.sbd.payroll.psp.domain.CompanyEvent ce set ce.StatusCd = :newCompanyEventStatus where ce.StatusCd = :companyEventStatus");
        String[] paramNames = {"newCompanyEventStatus", "companyEventStatus"};
        Object[] paramValues = {CompanyEventStatus.Inactive, CompanyEventStatus.Active};
        Application.executeHQLUpdate(hql.toString(), paramNames, paramValues);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    @Ignore
    public void testHQLDeleteWithFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        StringBuilder hql = new StringBuilder("delete from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.StatusCd = :companyEventStatus");
        String[] paramNames = {"companyEventStatus"};
        Object[] paramValues = {CompanyEventStatus.Active};
        Application.executeHQLUpdate(hql.toString(), paramNames, paramValues);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHQLSelectWithFilterAlreadyHasCompany() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        HqlBuilder hql = new HqlBuilder("select ce from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.StatusCd = :companyEventStatus and ce.Company = :company");
        hql.setParameter("companyEventStatus", CompanyEventStatus.Active);
        hql.setParameter("company", company);
        List<CompanyEvent> companyEvents = hql.list();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testHQLUpdateWithFilterAlreadyHasCompany() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();

        StringBuilder hql = new StringBuilder("update com.intuit.sbd.payroll.psp.domain.CompanyEvent ce set ce.StatusCd = :newCompanyEventStatus where ce.StatusCd = :companyEventStatus and ce.Company = :company");
        String[] paramNames = {"newCompanyEventStatus", "companyEventStatus", "company"};
        Object[] paramValues = {CompanyEventStatus.Inactive, CompanyEventStatus.Active, company};
        Application.executeHQLUpdate(hql.toString(), paramNames, paramValues);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    @Ignore
    public void testHQLDeleteWithFilterAlreadyHasCompany() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();

        StringBuilder hql = new StringBuilder("delete from com.intuit.sbd.payroll.psp.domain.CompanyEvent ce where ce.StatusCd = :companyEventStatus and ce.Company = :company");
        String[] paramNames = {"companyEventStatus", "company"};
        Object[] paramValues = {CompanyEventStatus.Active, company};
        Application.executeHQLUpdate(hql.toString(), paramNames, paramValues);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNamedSQLSelect() {
        //TODO: Find Named SQL Query on Partitioned Table without Company
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        List<Object[]> userIds = Application.executeNamedQuery("findUserNameForCompanyEvents", new String[]{"companyId"}, new Object[]{company.getId().toString()});
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNamedSQLSelectWithFilter() {
        //TODO: Find Named SQL Query on Partitioned Table without Company
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        List<Object[]> userIds = Application.executeNamedQuery("findUserNameForCompanyEvents", new String[]{"companyId"}, new Object[]{company.getId().toString()});
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNamedSQLSelectWithFilterAlreadyHasCompany() {
        //Filter is not getting applied
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        List<Object[]> userIds = Application.executeNamedQuery("findUserNameForCompanyEvents", new String[]{"companyId"}, new Object[]{company.getId().toString()});
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSQLSelectWithFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        String sql = "SELECT * FROM PSP_COMPANY_EVENT WHERE STATUS_CD=:companyEventStatus";
        SQLQuery query = Application.getHibernateSession().createSQLQuery(sql);
        query.setString("companyEventStatus", "Active");
        List<Object[]> results = query.list();
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testSQLUpdateWithFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        String updateSql = "UPDATE PSP_COMPANY_EVENT SET STATUS_CD='Inactive' WHERE STATUS_CD=:companyEventStatus";
        SQLQuery query = Application.getHibernateSession().createSQLQuery(updateSql);
        query.setString("companyEventStatus","Active");
        query.executeUpdate();
        PayrollServices.commitUnitOfWork();

    }

    @Test
    @Ignore
    public void testSQLDeleteWithFilter() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        String updateSql = "DELETE FROM PSP_COMPANY_EVENT WHERE STATUS_CD=:companyEventStatus";
        SQLQuery query = Application.getHibernateSession().createSQLQuery(updateSql);
        query.setString("companyEventStatus","Active");
        query.executeUpdate();
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testSQLDelete() throws SQLException {
       //TODO
    }

    @Test
    public void testJoinFilter(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        Criterion<CompanyEvent> where;
        where = CompanyEvent.EventTypeCd().equalTo(EventTypeCode.ACHReturn);

        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where)
                .EagerLoad(CompanyEvent.CompanyEventDetailSet());
        Application.find(CompanyEvent.class, query);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testJoinFilterAlreadyHasCompany(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        Criterion<CompanyEvent> where;
        where = CompanyEvent.EventTypeCd().equalTo(EventTypeCode.ACHReturn).And(CompanyEvent.Company().equalTo(company));

        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where)
                .EagerLoad(CompanyEvent.CompanyEventDetailSet());
        Application.find(CompanyEvent.class, query);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testManyToOne(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(company, EventTypeCode.ACHReturn);

        //create CompanyEventDetail
        CompanyEventDetail companyEventDetail = new CompanyEventDetail();
        companyEventDetail.setEventDetailTypeCd(EventDetailTypeCode.BankAccountNumber);
        companyEventDetail.setValue("Test bank account");
        companyEventDetail.setEventDetailSubtype("Test bank account");
        companyEventDetail.setCompanyEvent(companyEvent);
        companyEventDetail.setCompany(company);

        Application.save(companyEvent);
        Application.save(companyEventDetail);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        CompanyEventDetail companyEventDetail1 = Application.findById(CompanyEventDetail.class, companyEventDetail.getId());
        CompanyEvent companyEvent1 = companyEventDetail1.getCompanyEvent();
        companyEvent1.getStatusCd();
        assertEquals(companyEventDetail, companyEventDetail1);
        assertEquals(companyEvent, companyEvent1);
        PayrollServices.rollbackUnitOfWork();
    }

    /* Problem */
    @Test
    public void testManyToOneCriteria(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(company, EventTypeCode.ACHReturn);

        //create CompanyEventDetail
        CompanyEventDetail companyEventDetail = new CompanyEventDetail();
        companyEventDetail.setEventDetailTypeCd(EventDetailTypeCode.BankAccountNumber);
        companyEventDetail.setValue("Test bank account");
        companyEventDetail.setEventDetailSubtype("Test bank account");
        companyEventDetail.setCompanyEvent(companyEvent);
        companyEventDetail.setCompany(company);

        Application.save(companyEvent);
        Application.save(companyEventDetail);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        Criterion<CompanyEventDetail> where;
        where = CompanyEventDetail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.ACHReturn);
        Expression<CompanyEventDetail> query;
        query = new Query<CompanyEventDetail>()
                .Where(where);
        DomainEntitySet<CompanyEventDetail> companyEventDetailDomainEntitySet = Application.find(CompanyEventDetail.class, query);
        CompanyEventDetail companyEventDetail1 = companyEventDetailDomainEntitySet.getFirst();
        assertEquals(companyEventDetail, companyEventDetail1);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testManyToOneCriteriaEagerLoad(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(company, EventTypeCode.ACHReturn);

        //create CompanyEventDetail
        CompanyEventDetail companyEventDetail = new CompanyEventDetail();
        companyEventDetail.setEventDetailTypeCd(EventDetailTypeCode.BankAccountNumber);
        companyEventDetail.setValue("Test bank account");
        companyEventDetail.setEventDetailSubtype("Test bank account");
        companyEventDetail.setCompanyEvent(companyEvent);
        companyEventDetail.setCompany(company);

        Application.save(companyEvent);
        Application.save(companyEventDetail);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        Criterion<CompanyEventDetail> where;
        where = CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.BankAccountNumber);
        Expression<CompanyEventDetail> query;
        query = new Query<CompanyEventDetail>()
                .Where(where).EagerLoad(CompanyEventDetail.CompanyEvent());
        DomainEntitySet<CompanyEventDetail> companyEventDetailDomainEntitySet = Application.find(CompanyEventDetail.class, query);
        CompanyEventDetail companyEventDetail1 = companyEventDetailDomainEntitySet.getFirst();
        CompanyEvent companyEvent1 = companyEventDetail1.getCompanyEvent();
        assertEquals(companyEventDetail, companyEventDetail1);
        assertEquals(companyEvent, companyEvent1);
        PayrollServices.rollbackUnitOfWork();
    }

    /* Working fine */
    @Test
    public void testOneToManyCriteria(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(company, EventTypeCode.ACHReturn);

        //create CompanyEventDetail
        CompanyEventDetail companyEventDetail = new CompanyEventDetail();
        companyEventDetail.setEventDetailTypeCd(EventDetailTypeCode.BankAccountNumber);
        companyEventDetail.setValue("Test bank account");
        companyEventDetail.setEventDetailSubtype("Test bank account");
        companyEventDetail.setCompanyEvent(companyEvent);
        companyEventDetail.setCompany(company);

        Application.save(companyEvent);
        Application.save(companyEventDetail);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        Criterion<CompanyEvent> where;
        where = CompanyEvent.EventTypeCd().equalTo(EventTypeCode.ACHReturn);
        Expression<CompanyEvent> query;
        query = new Query<CompanyEvent>()
                .Where(where).EagerLoad(CompanyEvent.CompanyEventDetailSet());
        Application.find(CompanyEvent.class, query);

        PayrollServices.rollbackUnitOfWork();
    }

    private Company createCompany() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        return company;
    }

    private Company createCompany1() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany2();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        return company;
    }
    private CompanyEvent createCompanyEvent(Company foundCompany){
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(foundCompany, EventTypeCode.ACHReturn);

        //create CompanyEventDetail
        CompanyEventDetail companyEventDetail = new CompanyEventDetail();
        companyEventDetail.setEventDetailTypeCd(EventDetailTypeCode.BankAccountNumber);
        companyEventDetail.setValue("Test bank account");
        companyEventDetail.setEventDetailSubtype("Test bank account");
        companyEventDetail.setCompanyEvent(companyEvent);
        companyEventDetail.setCompany(foundCompany);

        //Update CompanyEvent
        companyEvent.setModifierId("Test");

        Application.save(companyEvent);
        Application.save(companyEventDetail);

        //Update CompanyEventDetail
        companyEventDetail.setValue("Test update bank account");
        Application.save(companyEventDetail);
        return companyEvent;
    }
}


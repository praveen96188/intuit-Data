package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.procedure.internal.ProcedureCallImpl;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.procedure.internal.ProcedureParameterImpl;
import org.junit.*;

import javax.persistence.StoredProcedureQuery;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ProcedureExecutionJPATests {

    private static final String PDT_DATE_TIME = "2023-05-23 10:10:10.1";
    // UTC is 7hrs ahead PDT
    private static final String UTC_PDT_DATE_TIME = "2023-05-23 17:10:10.1";

    private static final String PST_DATE_TIME = "2023-01-01 10:10:10.1";
    // UTC is 8hrs ahead PST
    private static final String UTC_PST_DATE_TIME = "2023-01-01 18:10:10.1";

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    // Daylight Saving Time usually starts in March-April and ends in September-November
    // PSP Application is in PST/PDT Timezone
    // PSP DB is in UTC Timezone
    // UTC is 7hrs ahead PDT
    // UTC is 8hrs ahead PST

    @Test
    public void testIfPDT() {
        Timestamp pdtTimestamp = Timestamp.valueOf(PDT_DATE_TIME);
        Date date = new Date(pdtTimestamp.getTime());

        TimeZone pstTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
        Assert.assertTrue(pstTimeZone.inDaylightTime(date));
    }

    @Test
    public void testIfPST() {
        Timestamp pdtTimestamp = Timestamp.valueOf(PST_DATE_TIME);
        Date date = new Date(pdtTimestamp.getTime());

        TimeZone pstTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
        Assert.assertFalse(pstTimeZone.inDaylightTime(date));
    }

    @Test
    public void testTimezoneConversionPDT() {
        PayrollServices.beginUnitOfWork();

        Session session = Application.getHibernateSession();
        StoredProcedureQuery query = session.createStoredProcedureQuery("Dummy_Procedure");

        Application.addParameter(query, 1, Pair.of(Timestamp.class, Timestamp.valueOf(PDT_DATE_TIME)));

        String utcTimestampActualFromStoredProcedure = ((ProcedureCallImpl) query).getQueryParameterBindings().getBinding(1).getBindValue().toString();
        Assert.assertEquals(UTC_PDT_DATE_TIME, utcTimestampActualFromStoredProcedure);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTimezoneConversionPST() {
        PayrollServices.beginUnitOfWork();

        Session session = Application.getHibernateSession();
        StoredProcedureQuery query = session.createStoredProcedureQuery("Dummy_Procedure");

        Application.addParameter(query, 1, Pair.of(Timestamp.class, Timestamp.valueOf(PST_DATE_TIME)));

        String utcTimestampActualFromStoredProcedure = ((ProcedureCallImpl) query).getQueryParameterBindings().getBinding(1).getBindValue().toString();
        Assert.assertEquals(UTC_PST_DATE_TIME, utcTimestampActualFromStoredProcedure);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddParameterForNull() {
        PayrollServices.beginUnitOfWork();

        Session session = Application.getHibernateSession();
        StoredProcedureQuery query = session.createStoredProcedureQuery("Dummy_Procedure");

        int finalCount = Application.addParameter(query, 1,
                                                    Pair.of(Integer.class, 123),
                                                    Pair.of(String.class, "test"),
                                                    Pair.of(String.class, null));

        Assert.assertEquals(4, finalCount);

        ProcedureCallImpl procedureCallImpl = ((ProcedureCallImpl) query);
        Assert.assertEquals(3, procedureCallImpl.getParameterMetadata().getParameterCount());

        Assert.assertEquals(Integer.class.toString(), ((ArrayList<QueryParameter>) procedureCallImpl.getParameterMetadata().getPositionalParameters()).get(0).getParameterType().toString());
        Assert.assertEquals(123, procedureCallImpl.getParameterValue(1));
        Assert.assertEquals(String.class.toString(), ((ArrayList<QueryParameter>) procedureCallImpl.getParameterMetadata().getPositionalParameters()).get(1).getParameterType().toString());
        Assert.assertEquals("test", procedureCallImpl.getParameterValue(2));
        Assert.assertEquals(String.class.toString(), ((ArrayList<QueryParameter>) procedureCallImpl.getParameterMetadata().getPositionalParameters()).get(2).getParameterType().toString());
        Assert.assertNull(procedureCallImpl.getParameterValue(3));

        ProcedureParameterImpl procedureParameterImplOne = (ProcedureParameterImpl) query.getParameter(1);
        Assert.assertEquals(Integer.class.toString(), procedureParameterImplOne.getParameterType().toString());
        Assert.assertFalse(procedureParameterImplOne.isPassNullsEnabled());

        ProcedureParameterImpl procedureParameterImplTwo = (ProcedureParameterImpl) query.getParameter(2);
        Assert.assertEquals(String.class.toString(), procedureParameterImplTwo.getParameterType().toString());
        Assert.assertFalse(procedureParameterImplTwo.isPassNullsEnabled());

        ProcedureParameterImpl procedureParameterImplThree = (ProcedureParameterImpl) query.getParameter(3);
        Assert.assertEquals(String.class.toString(), procedureParameterImplThree.getParameterType().toString());
        Assert.assertTrue(procedureParameterImplThree.isPassNullsEnabled());

        PayrollServices.rollbackUnitOfWork();
    }

    @Ignore
    @Test
    public void testParametersForDummyProcedure() throws SQLException {
        PayrollServices.beginUnitOfWork();
        Session session = Application.getHibernateSession();
        Connection connection = Application.getConnection(session);
        Statement stmt = connection.createStatement();
        StoredProcedures testStoredProcedures = StoredProcedures.TEMP_TEST_PROC;
        String storedProcedureQuery;
        if(testStoredProcedures.getStoredProcedureName().equals("temp_test_proc_oracle")) {
            storedProcedureQuery = "CREATE OR REPLACE PROCEDURE TEMP_TEST_PROC_ORACLE(\n" +
                    "    p_psid IN VARCHAR2,\n" +
                    "    p_name IN VARCHAR2,\n" +
                    "    p_phone IN NUMBER,\n" +
                    "    p_creator_id IN VARCHAR2,\n" +
                    "    p_created_date IN TIMESTAMP,\n" +
                    "    p_modifier_id IN VARCHAR2,\n" +
                    "    p_modified_date IN TIMESTAMP\n" +
                    ")\n" +
                    "    IS\n" +
                    "BEGIN\n" +
                    "    UPDATE PSP_COMPANY\n" +
                    "    SET LEGAL_NAME    = p_name,\n" +
                    "        PHONE         = p_phone,\n" +
                    "        CREATOR_ID    = p_creator_id,\n" +
                    "        CREATED_DATE  = p_created_date,\n" +
                    "        MODIFIER_ID   = p_modifier_id,\n" +
                    "        MODIFIED_DATE = p_modified_date\n" +
                    "    WHERE SOURCE_COMPANY_ID = p_psid;\n" +
                    "END;";
        } else if(testStoredProcedures.getStoredProcedureName().equals("temp_test_proc_postgres")) {
            storedProcedureQuery = "CREATE OR REPLACE PROCEDURE TEMP_TEST_PROC_POSTGRES(\n" +
                    "    p_psid IN VARCHAR,\n" +
                    "    p_name IN VARCHAR,\n" +
                    "    p_phone IN NUMERIC,\n" +
                    "    p_creator_id IN VARCHAR,\n" +
                    "    p_created_date IN TIMESTAMP,\n" +
                    "    p_modifier_id IN VARCHAR,\n" +
                    "    p_modified_date IN TIMESTAMP\n" +
                    ")\n" +
                    "    AS $$\n" +
                    "BEGIN\n" +
                    "    UPDATE PSP_COMPANY\n" +
                    "    SET LEGAL_NAME    = p_name,\n" +
                    "        PHONE         = p_phone,\n" +
                    "        CREATOR_ID    = p_creator_id,\n" +
                    "        CREATED_DATE  = p_created_date,\n" +
                    "        MODIFIER_ID   = p_modifier_id,\n" +
                    "        MODIFIED_DATE = p_modified_date\n" +
                    "    WHERE SOURCE_COMPANY_ID = p_psid;\n" +
                    "END; \n"+"$$ LANGUAGE plpgsql;";
        } else {
            throw new RuntimeException("Unknown Procedure Name");
        }
        stmt.execute(storedProcedureQuery);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        PayrollServices.commitUnitOfWork();

        Application.executeSqlProcedure(StoredProcedures.TEMP_TEST_PROC, true,
                                        Pair.of(String.class, company.getSourceCompanyId()),
                                        Pair.of(String.class, "NEW_COMPANY_1"),
                                        Pair.of(Integer.class, 123456789),
                                        Pair.of(String.class, "NEW_CREATOR_ID"),
                                        Pair.of(Timestamp.class, Timestamp.valueOf(PDT_DATE_TIME)),
                                        Pair.of(String.class, "NEW_MODIFIER_ID"),
                                        Pair.of(Timestamp.class, Timestamp.valueOf(PST_DATE_TIME)));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> request = Application.find(Company.class);
        Company updatedCompany = request.iterator().next();
        Assert.assertEquals("NEW_COMPANY_1", updatedCompany.getLegalName());
        Assert.assertEquals("123456789", updatedCompany.getPhone());
        Assert.assertEquals("NEW_CREATOR_ID", updatedCompany.getCreatorId());
        Assert.assertEquals(UTC_PDT_DATE_TIME, updatedCompany.getCreatedDate().format("yyyy-MM-dd HH:mm:ss")+ ".1");
        Assert.assertEquals("NEW_MODIFIER_ID", updatedCompany.getModifierId());
        Assert.assertEquals(UTC_PST_DATE_TIME, updatedCompany.getModifiedDate().format("yyyy-MM-dd HH:mm:ss") + ".1");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        session = Application.getHibernateSession();
        connection = Application.getConnection(session);
        stmt = connection.createStatement();
        String query = "DROP PROCEDURE "+StoredProcedures.TEMP_TEST_PROC.getStoredProcedureName();
        stmt.execute(query);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testJdbcTypeJavaClassMappings() {
        Assert.assertEquals(String.class.toString(), Application.determineJavaClassForJdbcTypeCode(java.sql.Types.VARCHAR).toString());
        Assert.assertEquals(Integer.class.toString(), Application.determineJavaClassForJdbcTypeCode(java.sql.Types.INTEGER).toString());
    }

}

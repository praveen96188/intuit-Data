package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessMonthlyFees;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.config.ISpcfImmutableConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import oracle.jdbc.OracleConnection;
import org.ehcache.CacheManager;
import org.hibernate.Session;
import org.junit.*;
import org.springframework.security.core.parameters.P;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Sep 7, 2010
 * Time: 5:22:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HibernateUtilsTests {


    public static SpcfLogger logger = SpcfLogManager.getLogger(HibernateUtilsTests.class);

    @Ignore
    @Test

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    public void testParseStaleObjectStateException_SystemParameter() {
        SpcfUniqueId sysParamId = null;

        try {
            // create the test parameter
            PayrollServices.beginUnitOfWork();

            SystemParameter sysParam = new SystemParameter();
            sysParam.setSystemParameterCd("StaleStateTestParameter");
            sysParam.setSystemParameterDescription("Test parameter for testing StaleObjectStateException");
            sysParam.setSystemParameterOrg("PSP");
            sysParam.setSystemParameterValue("Default Value");

            Application.save(sysParam);

            PayrollServices.commitUnitOfWork();

            // save the param id for later use
            sysParamId = sysParam.getId();
            final SpcfUniqueId paramId = sysParamId;

            // force the StaleObjectStateException
            PayrollServices.beginUnitOfWork();

            // read the param before the thread changes it
            sysParam = Application.findById(SystemParameter.class, paramId);

            // change the param value in a separate transaction
            PayrollServices.executeTransactionThread(new TransactionThread<ProcessResult>() {
                public ProcessResult transaction() {
                    SystemParameter param = Application.findById(SystemParameter.class, paramId);

                    param.setSystemParameterValue("Transaction Thread Value");

                    Application.save(param);

                    return new ProcessResult();
                }
            });

            // give the thread a little time to complete
            Thread.sleep(250);

            // change the param value in the main thread
            //sysParam.setSystemParameterValue("Main Thread Value"); // causes StaleObjectStateException
            sysParam.setSystemParameterDescription("test"); // causes StaleObjectStateException

            Application.save(sysParam);

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();

            if (sysParamId != null) {
                // delete the test parameter
                try {
                    PayrollServices.beginUnitOfWork();
                    Application.delete(SystemParameter.class, sysParamId);
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
        }
    }

    @Ignore
    @Test
    public void testParseStaleObjectStateException_Paycheck() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // Verify persisted data
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        // Ensure that Payroll Run was created correctly
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        assertTrue("PayrollRun Not Null", payrollRun != null);
        assertEquals("PayrollRun Id:", payrollRun.getSourcePayRunId(), payrollRunDTO.getPayrollTXBatchId());
        // Ensure that Paychecks were created correctly
        assertEquals("Number of Paychecks:", payrollRun.getPaycheckCollection().size(), payrollRunDTO.getPaychecks().size());
        Paycheck paycheck = Paycheck.findPaycheck(company, payrollRunDTO.getPaychecks().iterator().next().getPaycheckId());

        PayrollServices.commitUnitOfWork();

        final SpcfUniqueId id = paycheck.getId();

        try {
            // force the StaleObjectStateException
            PayrollServices.beginUnitOfWork();

            // read the param before the thread changes it
            paycheck = Application.findById(Paycheck.class, id);

            // change the param value in a separate transaction
            PayrollServices.executeTransactionThread(new TransactionThread<ProcessResult>() {
                public ProcessResult transaction() {
                    Paycheck check = Application.findById(Paycheck.class, id);

                    CompanyAdjustmentSubmission adj = new CompanyAdjustmentSubmission();
                    adj.setCompany(check.getPayrollRun().getCompany());
                    Application.save(adj);

                    check.setCompanyAdjustmentSubmission(adj);
                    Application.save(check);

                    return new ProcessResult();
                }
            });

            // give the thread a little time to complete
            Thread.sleep(250);

            CompanyAdjustmentSubmission cas = new CompanyAdjustmentSubmission();
            cas.setCompany(paycheck.getPayrollRun().getCompany());
            Application.save(cas);

            // change the param value in the main thread
            paycheck.setCompanyAdjustmentSubmission(cas); // causes StaleObjectStateException
            Application.save(paycheck);

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    /*
    Test to check if hibernate session.evict() and session.merge() are working as expected
     */
    @Ignore
    @Test
    public void testSessionEvict() {

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        Company company1 = dataloader.persistTestIntuitCompany();
        final SpcfUniqueId companyId = company1.getId();
        final String psID = company1.getSourceCompanyId();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        //normal save, changes should be reflected
        PayrollServices.beginUnitOfWork();
        Company c1 = Application.findById(Company.class, companyId);
        c1.setIAMRealmId("123456788");
        Application.save(c1);
        PayrollServices.commitUnitOfWork();

        c1 = Application.findById(Company.class, companyId);
        Assert.assertEquals(c1.getIAMRealmId(), "123456788");


        //using session.evict, changes should not be reflected even after save
        PayrollServices.beginUnitOfWork();
        c1 = Application.findById(Company.class, companyId);
        c1.setIAMRealmId("998765431");
        Application.save(c1);
        Application.evict(c1);
        PayrollServices.commitUnitOfWork();

        //value should not have changed
        c1 = Application.findById(Company.class, companyId);
        Assert.assertEquals(c1.getIAMRealmId(), "123456788");


        //checking if session.merge() works as expected
        //1 call evict
        PayrollServices.beginUnitOfWork();
        c1 = Application.findById(Company.class, companyId);
        c1.setIAMRealmId("998765431");
        Session session1 = Application.getHibernateSession();
        logger.info("testSessionEvict session1 hashCode=" + session1.hashCode());
        Application.save(c1);
        Application.evict(c1);
        PayrollServices.commitUnitOfWork();

        //attach the object to a different session and commit
        PayrollServices.beginUnitOfWork();
        Application.getHibernateSession().merge(c1);
        Session session2 = Application.getHibernateSession();
        logger.info("testSessionEvict session2 hashCode=" + session2.hashCode());
        PayrollServices.commitUnitOfWork();

        //check that changes are reflected this time
        c1 = Application.findById(Company.class, companyId);
        Assert.assertEquals(c1.getIAMRealmId(), "998765431");
    }


    /*
    Test to verify that new physical connection is not being created with each new hibernate session
     */
    @Ignore
    @Test
    public void testc3p0ConnectionPool() throws SQLException {
        //verify in the logs that new connect pool is not being created

        try {
            PayrollServices.beginUnitOfWork();
            logger.info("testc3p0ConnectionPool before first execution");
            Statement statement = Application.getConnection().createStatement();
            //internally it is a T4Connection which extends PhysicalConnection extending OracleConnection (public class)
            OracleConnection oracleConnection1 = statement.getConnection().unwrap(OracleConnection.class);
            Class c1 = Application.getConnection().unwrap(OracleConnection.class).getClass();
            logger.info("testc3p0ConnectionPool class = " + c1.toString());
            statement.close();
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            logger.info("testc3p0ConnectionPool before second execution");
            statement = Application.getConnection().createStatement();
            OracleConnection oracleConnection2 = statement.getConnection().unwrap(OracleConnection.class);
            statement.close();
            logger.info("testc3p0ConnectionPool finish");
            PayrollServices.commitUnitOfWork();

            Assert.assertEquals(oracleConnection1, oracleConnection2);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    /*
    Need to use debudgger and check logs
    Verify if cache is working as expected
     */
    @Ignore
    @Test
    public void testCacheLookup() {

        try {

            PayrollServices.beginUnitOfWork();

            logger.info("testCacheLookup start");

            DataLoader dataloader = new DataLoader();
            logger.info("testCacheLookup before persistTestIntuitCompany");
            Company company1 = dataloader.persistTestIntuitCompany();
            final SpcfUniqueId companyId = company1.getId();
            final String psID = company1.getSourceCompanyId();
            logger.info("testCacheLookup before persistTestIntuitService");
            dataloader.persistTestCompanyService(company1);


            boolean flcContains = Application.getHibernateSession().getSessionFactory().getCache().contains(Company.class, company1.getId());
            logger.info("testCacheLookup firstLevelCache present=" + flcContains);

            logger.info("testCacheLookup before c1");
            Company c1 = Application.findById(Company.class, companyId);
            logger.info("testCacheLookup before c2");
            Company c2 = Company.findCompany(psID, SourceSystemCode.QBDT);
            logger.info("testCacheLookup before c3");
            Company c3 = Application.findById(Company.class, companyId);

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return;
    }

    @Ignore
    @Test
    public void testHibernateCalls() {

        try {
            // Insert data in company table using Hibernate ORM
            PayrollServices.beginUnitOfWork();
            DataLoader dataloader = new DataLoader();
            Company company1 = dataloader.persistTestIntuitCompany();
            final SpcfUniqueId companyId = company1.getId();
            final String psID = company1.getSourceCompanyId();
            logger.info("Create company for test Hibernate Calls");
            dataloader.persistTestCompanyService(company1);
            PayrollServices.commitUnitOfWork();

            // Fetch data from company table using Hibernate
            PayrollServices.beginUnitOfWork();
            Company fetchedCompany = Application.findById(Company.class, companyId);
            Assert.assertEquals("QBOE:" + psID, fetchedCompany.getSourceSystemCompanyId());
            PayrollServices.rollbackUnitOfWork();

            //Fetch using different criteria
            PayrollServices.beginUnitOfWork();
            Expression<Company> query = new Query<Company>().Where(Company.SourceCompanyId().equalTo("123456"));
            DomainEntitySet<Company> comp = Application.find(Company.class, query);
            Assert.assertTrue(comp.size() == 1);
            PayrollServices.rollbackUnitOfWork();

            //Update data to company table using Hibernate (Targeting Boolean field)
            PayrollServices.beginUnitOfWork();
            Company updateCompany = Application.findById(Company.class, companyId);
            updateCompany.setIsFlaggedForFraud(true);
            updateCompany.setDDPublishFlag(false);
            updateCompany.setDebugLogging(true);
            Application.save(updateCompany);
            PayrollServices.commitUnitOfWork();

            Assert.assertTrue(Application.findById(Company.class, companyId).getDDPublishFlag());
            Assert.assertTrue(Application.findById(Company.class, companyId).getIsFlaggedForFraud());

        } catch (Exception e) {
            logger.info("Exception:" + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Ignore
    @Test
    public void testHQLCalls() {
        try {
            // Insert data in company table using Hibernate ORM
            PayrollServices.beginUnitOfWork();
            DataLoader dataloader = new DataLoader();
            Company company1 = dataloader.persistTestIntuitCompany();
            final SpcfUniqueId companyId = company1.getId();
            final String psID = company1.getSourceCompanyId();
            logger.info("Create company for test Hibernate Calls");
            dataloader.persistTestCompanyService(company1);
            PayrollServices.commitUnitOfWork();

            //Update table using HQL
            PayrollServices.beginUnitOfWork();
            String updateQuery = "UPDATE com.intuit.sbd.payroll.psp.domain.Company SET DDPublishFlag= :flag, IsFlaggedForFraud= :flag\n" +
                    " WHERE SOURCE_COMPANY_ID = :companyPSID";

            String[] updatePNames = new String[]{"flag", "companyPSID"};
            Object[] updatePValues = new Object[]{true, psID};
            Application.executeHQLUpdate(updateQuery, updatePNames, updatePValues);
            PayrollServices.commitUnitOfWork();


            //Select
            PayrollServices.beginUnitOfWork();
            String query = "SELECT C.DDPublishFlag, C.IsFlaggedForFraud, C.IsDgDisassociated\n" +
                    " FROM com.intuit.sbd.payroll.psp.domain.Company as C\n" +
                    " WHERE SOURCE_COMPANY_ID = :companyPSID\n";

            String[] pNames = new String[]{"companyPSID"};
            String[] pValues = new String[]{psID};
            List<Boolean> obj = Application.executeHQLQuery(query, pNames, pValues);
            Assert.assertTrue(obj.get(0));
            Assert.assertTrue(obj.get(1));
            PayrollServices.rollbackUnitOfWork();

        } catch (Exception e) {
            logger.info("Exception:" + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Ignore
    @Test
    public void testNativeQuery() throws SQLException {

        try {
            // Insert data in company table using Hibernate ORM
            PayrollServices.beginUnitOfWork();
            DataLoader dataloader = new DataLoader();
            Company company1 = dataloader.persistTestIntuitCompany();
            final SpcfUniqueId companyId = company1.getId();
            final String psID = company1.getSourceCompanyId();
            logger.info("Create company for test Hibernate Calls");
            dataloader.persistTestCompanyService(company1);
            PayrollServices.commitUnitOfWork();

            //Update Query
            Connection con = ProcessMonthlyFees.getDatabaseConnection();
            Statement stmt = con.createStatement();
            stmt.setFetchSize(10);
            String updateSql = "UPDATE PSP_COMPANY SET D_D_PUBLISH_FLAG = 1, IS_FLAGGED_FOR_FRAUD = 1 WHERE SOURCE_COMPANY_ID = '%s'";
            int valp = stmt.executeUpdate(String.format(updateSql, psID));
            logger.info(valp);

            stmt.close();
            con.close();

            // Fetch from company table using Native Query
            con = ProcessMonthlyFees.getDatabaseConnection();
            stmt = con.createStatement();
            stmt.setFetchSize(10);
            String sql = "SELECT D_D_PUBLISH_FLAG, IS_DG_DISASSOCIATED, IS_FLAGGED_FOR_FRAUD FROM PSP_COMPANY WHERE SOURCE_COMPANY_ID = '%s'";

            ResultSet rs = stmt.executeQuery(String.format(sql, psID));
            while (rs.next()) {
                Boolean val1 = rs.getBoolean(rs.findColumn("D_D_PUBLISH_FLAG"));
                Boolean val2 = rs.getBoolean(rs.findColumn("IS_DG_DISASSOCIATED"));
                Boolean val3 = rs.getBoolean(rs.findColumn("IS_FLAGGED_FOR_FRAUD"));
                logger.info("Values : " + val1 + " " + val2 + " " + val3);
                Assert.assertTrue(val1);
                Assert.assertTrue(val3);
                Assert.assertFalse(val2);
            }

            rs.close();
            stmt.close();
            con.close();

            //Insert Query
            con = ProcessMonthlyFees.getDatabaseConnection();
            stmt = con.createStatement();
            stmt.setFetchSize(10);
            String insertSql = "INSERT INTO PSP_COMPANY\n" +
                    "    (COMPANY_SEQ, VERSION, CREATOR_ID, CREATED_DATE, MODIFIER_ID, MODIFIED_DATE, REALM_ID, PRIVATE_KEY_ENC, PHONE, DBA_NAME, LEGAL_NAME, SOURCE_COMPANY_ID, DEBUG_LOGGING,\n" +
                    "     NOTIFICATION_EMAIL, NEXT_PAYROLL_TRANSACTION_ID, NEXT_PAYCHECK_ID, NEXT_EMPLOYEE_ID, NEXT_PAYROLL_ITEM_ID, ACCOUNT_LOCKED_UNTIL, NBR_OF_FAILED_LOGIN_ATTEMPTS, CURRENT_TOKEN,\n" +
                    "     SOURCE_SYSTEM_CD, TAX_EXEMPT_EXPIRATION_DATE, NAME_CONTROL, IS_FLAGGED_FOR_FRAUD, SIGN_UP_DATE, PUBLIC_KEY, NBR_FAILED_AUTHENTICATIONS, CLOUD_CURRENT_TOKEN, I_A_M_REALM_ID,\n" +
                    "     TAX_EXEMPT_STATUS, PRICE_TYPE, FED_TAX_ID_ENC, D_D_PUBLISH_FLAG, O_I_I_FLAG, IS_DG_DISASSOCIATED, PUBLISH_STATUS, RECORD_METADATA, PAYROLL_FREQUENCY_FK, COMPLIANCE_ADDRESS_FK,\n" +
                    "     MAILING_ADDRESS_FK, LEGAL_ADDRESS_FK, OFFLOAD_GROUP_FK, FUNDING_MODEL_FK) VALUES\n" +
                    "    ('845a5182-c506-4179-a721-dcd2f9f8b751', -1, 'Sample1', TO_TIMESTAMP('2010-12-12 15:10:52.307000', 'YYYY-MM-DD HH24:MI:SS.FF6'), 'Sample1', TO_TIMESTAMP('2010-12-12 15:10:58.563000', 'YYYY-MM-DD HH24:MI:SS.FF6'), -1, null, null, 'Intuit', 'Intuit', '7654321', 1, 'notifications@intuit.com', '1', '1', '1', '1', null, 0, 1, 'QBOE', null, null, 1, TO_TIMESTAMP('2010-12-12 15:10:52.289000', 'YYYY-MM-DD HH24:MI:SS.FF6'), null, 0, 0, null, 'New', 'Standard', '2gIAAAASAQAEAAAACAUACGhTVFRaSEFadryixwqcwQ8ABHnTfqRtoQ3fNnoS9kvJ+w==', 1, '0000000000000000', 0, '0000000000000000', null, '12', null, null, null,\n" +
                    "     '3b67b658-dc4e-012a-fc4f-005056c02727', '5D')";
            stmt.executeUpdate(insertSql);
            rs.close();
            stmt.close();
            con.close();


            PayrollServices.beginUnitOfWork();
            Expression<Company> query = new Query<Company>().Where(Company.SourceCompanyId().equalTo("7654321"));
            DomainEntitySet<Company> comp = Application.find(Company.class, query);
            Assert.assertTrue(comp.size() == 1);
            PayrollServices.rollbackUnitOfWork();
        } catch (SQLException e) {
            logger.info("SQL Exeption:" + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Ignore
    @Test
    public void testEntitlementTableBooleanMapping(){
        String licenseNumber = "12345678901234567890";
        String eoc = "1";

        PayrollServices.beginUnitOfWork();
        EntitlementDTO entitlementDTO = new EntitlementDTO();
        entitlementDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        entitlementDTO.setEntitlementOfferingCode(eoc);
        entitlementDTO.setLicenseNumber(licenseNumber);
        AddEntitlementCore addEntitlementCore = new AddEntitlementCore(entitlementDTO);
        PSP_PRAssert.assertSuccess("add entitlement", addEntitlementCore.execute());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, eoc);
        Assert.assertNotNull(entitlement);
        entitlement.setRetail(true);
        entitlement.setTrialAssociated(true);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();

        Entitlement updatedEntitlement = Entitlement.findEntitlement(licenseNumber, eoc);
        Assert.assertNotNull(updatedEntitlement);
        Assert.assertTrue(updatedEntitlement.getRetail());
        Assert.assertTrue(updatedEntitlement.getTrialAssociated());
        PayrollServices.rollbackUnitOfWork();
    }

    @Ignore
    @Test
    public void testLiabilityCheckTableBooleanMapping(){

        String psid = "123456789";
        DataLoadServices.setupCompany(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), null,
                SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("5.10"),
                OfferingServiceChargeType.OtherFee, "Other Fee-Testing");

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly));
        feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId(),
                SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("10.20"),
                OfferingServiceChargeType.OtherFee, "Other Fee-Testing2");

        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        LiabilityCheck liabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(company1, "1");
        Assert.assertNotNull(liabilityCheck);
        liabilityCheck.setIsVoid(true);
        Application.save(liabilityCheck);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany(psid, SourceSystemCode.QBDT);
        LiabilityCheck updatedLiabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(company2, "1");
        Assert.assertNotNull(updatedLiabilityCheck);
        Assert.assertTrue(updatedLiabilityCheck.getIsVoid());
        PayrollServices.rollbackUnitOfWork();
    }

    @Ignore
    @Test
    public void testReportJobSetupBooleanMapping(){


        //Fetch using different criteria
        PayrollServices.beginUnitOfWork();
        Expression<ReportJobSetup> query = new Query<ReportJobSetup>().Where(ReportJobSetup.ReportName().equalTo("Company_Agency_Info"));
        DomainEntitySet<ReportJobSetup> reportJob = Application.find(ReportJobSetup.class, query);
        Assert.assertTrue(reportJob.size() == 1);
        Assert.assertTrue(reportJob.getFirst().getIsAutomaticallyScheduled());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testQBDTPaycheckInfoBooleanMapping() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        ProcessResult procResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PSP_PRAssert.assertSuccess("Initial payroll run success", procResult);
        PayrollServices.commitUnitOfWork();

        Collection<PaycheckDTO> paycheckCol = payrollRunDto.getPaychecks();
        Iterator<PaycheckDTO> iterator = paycheckCol.iterator();
        PaycheckDTO paycheckDto = iterator.next();
        QBDTPaycheckInfoDTO qbdtpaycheckInfo = paycheckDto.getQBDTPaycheckInfoDTO();
        qbdtpaycheckInfo.setOnService(false);
        qbdtpaycheckInfo.setProrate(true);

        PayrollServices.beginUnitOfWork();
        procResult = PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PayrollServices.commitUnitOfWork();

        PSP_PRAssert.assertSuccess("After updating qbdt paycheck Info", procResult);
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDto.getPayrollTXBatchId());
        Paycheck paycheck = assertOne(payrollRun.getPaycheckCollection().find(Paycheck.SourcePaycheckId().equalTo(paycheckDto.getPaycheckId())));
        assertFalse("Service flag set is failed.",paycheck.getQbdtPaycheckInfo().getOnService());
        assertTrue(paycheck.getQbdtPaycheckInfo().getProrate());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testTransactionTypeBooleanMapping(){

        PayrollServices.beginUnitOfWork();
        Expression<TransactionType> query = new Query<TransactionType>().Where(TransactionType.Name().equalTo("FLA dERLO cTXCC"));
        DomainEntitySet<TransactionType> transactionTypes = Application.find(TransactionType.class, query);
        Assert.assertTrue(transactionTypes.size() == 1);
        Assert.assertFalse(transactionTypes.getFirst().getIncludeInTransactionResponse());
        Assert.assertFalse(transactionTypes.getFirst().getFeeInd());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testQBDTPayLineInfoBooleanMapping(){

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        ProcessResult procResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PSP_PRAssert.assertSuccess("Initial payroll run success", procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<Company> query = new Query<Company>().Where(Company.SourceCompanyId().equalTo(company.getSourceCompanyId()));
        DomainEntitySet<Company> comp = Application.find(Company.class, query);
        Assert.assertTrue(comp.size() == 1);

        Expression<QbdtPaylineInfo> paylineInfoQuery = new Query<QbdtPaylineInfo>().Where(QbdtPaylineInfo.Company().equalTo(comp.getFirst()));
        DomainEntitySet<QbdtPaylineInfo> qbdtPaylineInfos = Application.find(QbdtPaylineInfo.class, paylineInfoQuery);
        Assert.assertTrue(qbdtPaylineInfos.size() > 0);
        QbdtPaylineInfo qbdtPaylineInfo = qbdtPaylineInfos.getFirst();
        qbdtPaylineInfo.setExpenseByJob(true);
        Application.save(qbdtPaylineInfo);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<QbdtPaylineInfo> pLineInfoQuery = new Query<QbdtPaylineInfo>().Where(QbdtPaylineInfo.ExpenseByJob().equalTo(true));
        DomainEntitySet<QbdtPaylineInfo> paylineInfos = Application.find(QbdtPaylineInfo.class, pLineInfoQuery);
        Assert.assertTrue(paylineInfos.size() == 1);
        Assert.assertTrue(paylineInfos.getFirst().getExpenseByJob());
        PayrollServices.rollbackUnitOfWork();
    }

    @Ignore
    @Test
    public void testOffloadBatchBooleanMapping(){

        SpcfCalendar spcfCalendar = SpcfCalendar.getNow();
        spcfCalendar.addDays(-10);
        PayrollServices.beginUnitOfWork();
        Expression<OffloadBatch> query = new Query<OffloadBatch>().Where(OffloadBatch.IsOffloadedTransactionsEventCreationComplete().equalTo(false).And(OffloadBatch.OffloadDate().greaterOrEqualThan(spcfCalendar)));
        DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class, query);
        Assert.assertTrue(offloadBatches.size() > 0);
        Assert.assertFalse(offloadBatches.getFirst().getIsOffloadedTransactionsEventCreationComplete());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testOfferBooleanMapping(){

        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByOfferCode("Waive all major fees");
        Assert.assertNotNull(offer);
        Assert.assertTrue(offer.getIsApproved());
        PayrollServices.rollbackUnitOfWork();

    }

    @Ignore
    @Test
    public void testQBDTEmployeeInfoBooleanMapping(){

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        ProcessResult procResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PSP_PRAssert.assertSuccess("Initial payroll run success", procResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<Company> query = new Query<Company>().Where(Company.SourceCompanyId().equalTo(company.getSourceCompanyId()));
        DomainEntitySet<Company> comp = Application.find(Company.class, query);
        Assert.assertTrue(comp.size() == 1);

        Expression<QbdtEmployeeInfo> qbdtEmployeeInfoQuery = new Query<QbdtEmployeeInfo>().Where(QbdtEmployeeInfo.Company().equalTo(comp.getFirst()));
        DomainEntitySet<QbdtEmployeeInfo> qbdtEmployeeInfos = Application.find(QbdtEmployeeInfo.class, qbdtEmployeeInfoQuery);
        Assert.assertTrue(qbdtEmployeeInfos.size() > 0);
        QbdtEmployeeInfo qbdtEmployeeInfo = qbdtEmployeeInfos.getFirst();
        qbdtEmployeeInfo.setIsDeleted(true);
        qbdtEmployeeInfo.setEnforceSubjectTo(true);
        qbdtEmployeeInfo.setUseDD(true);
        qbdtEmployeeInfo.setUseTime(true);
        qbdtEmployeeInfo.setIsAssisted(false);
        Application.save(qbdtEmployeeInfo);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<QbdtEmployeeInfo> qbdtEmployeeInfoExpression = new Query<QbdtEmployeeInfo>().Where(QbdtEmployeeInfo.EnforceSubjectTo().equalTo(true));
        DomainEntitySet<QbdtEmployeeInfo> qbdtEmployeeInfos1 = Application.find(QbdtEmployeeInfo.class, qbdtEmployeeInfoExpression);
        Assert.assertTrue(qbdtEmployeeInfos1.size() == 1);
        Assert.assertTrue(qbdtEmployeeInfos1.getFirst().getUseDD());
        Assert.assertTrue(qbdtEmployeeInfos1.getFirst().getUseTime());
        Assert.assertTrue(qbdtEmployeeInfos1.getFirst().getIsDeleted());
        Assert.assertFalse(qbdtEmployeeInfos1.getFirst().getIsAssisted());
        PayrollServices.rollbackUnitOfWork();
    }
}
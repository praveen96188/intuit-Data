package com.intuit.sbd.payroll.psp.migration.cris;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Aug 5, 2010
 * Time: 9:06:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrisMigrationTests {
    private static final String sfmDropTempTable = "DROP TABLE TEMP_MIGRATION CASCADE CONSTRAINTS";

    private static final String sfmCreateTempTable =
            "CREATE TABLE TEMP_MIGRATION"+
            "("+
            "  ACCOUNT_ROWID        VARCHAR2(15 CHAR)        NOT NULL,"+
            "  AGREE_ROWID          VARCHAR2(15 CHAR)        NOT NULL,"+
            "  EIN                  VARCHAR2(100 CHAR)       NOT NULL,"+
            "  DBA_NAME             VARCHAR2(100 CHAR)       NOT NULL,"+
            "  COMPANY_PHONE        VARCHAR2(40 CHAR),"+
            "  AGREE_NBR            NUMBER(22,7),"+
            "  QB_SUBTYPE           VARCHAR2(30 CHAR),"+
            "  X_TAX_EXEMPT_EXP_DT  DATE,"+
            "  CONTACT_LINK         VARCHAR2(15 CHAR),"+
            "  ADDR                 VARCHAR2(800 CHAR),"+
            "  CITY                 VARCHAR2(200 CHAR),"+
            "  COUNTRY              VARCHAR2(120 CHAR),"+
            "  STATE                VARCHAR2(40 CHAR),"+
            "  ZIPCODE              VARCHAR2(120 CHAR),"+
            "  SUFFIX               VARCHAR2(15 CHAR),"+
            "  LAST_NAME            VARCHAR2(50 CHAR),"+
            "  FIRST_NAME           VARCHAR2(50 CHAR),"+
            "  MI_NAME              CHAR(1 CHAR),"+
            "  EMAIL                VARCHAR2(100 CHAR),"+
            "  WORK_PHONE           VARCHAR2(40 CHAR),"+
            "  HOME_PHONE           VARCHAR2(40 CHAR),"+
            "  MIGRATION_STATUS     VARCHAR2(10 CHAR)"+
            ")";

    private static final String[] sfmMigrationTestData = {
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1-36XCIT', '1-36XCWS', '742344869', 'B & B AUTOMOTIVE SERVICES', '9797753413', "+
            "    410842, 'QB Payroll Standard', NULL, '1-36XCIT', '1007 S COULTER DR', "+
            "    'BRYAN', 'TX', 'USA', '77803-4502', 'Mrs.', "+
            "    'MELISSARI', 'KIM', 'J', 'KMELISSARI@SUDDENLINK.NET', '9797753413', "+
            "    '9797753413', NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1-36XCIT', '1-36XCWS', '742605084', 'B & B AUTOMOTIVE SERVICES', '9797753413', "+
            "    410842, 'QB Payroll Standard', NULL, '1-36XCIT', '1007 S COULTER DR', "+
            "    'BRYAN', 'TX', 'USA', '77803-4502', 'Mrs.', "+
            "    'MELISSARI', 'KIM', 'J', 'KMELISSARI@SUDDENLINK.NET', '9797753413', "+
            "    '9797753413', NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1+715+50', '1-36XFZW', '351893270', 'MAINTENANCE DYNAMICS', '2199808003', "+
            "    410854, 'QB Payroll Enhanced', NULL, '1+715+50', 'PO BOX 1926', "+
            "    'GARY', 'IN', 'USA', '46409-0926', NULL, "+
            "    'BROSKY', 'DANETTE', NULL, 'INFO@INDUSTRIALRENTSINC.COM', '2199808003', "+
            "    '2199808003', NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1+7C8+301', '1-36XM67', '010680653', 'HALE INSURANCE AND INVESTMENTS AGENCY', '8156231511', "+
            "    410891, 'QB Payroll Standard', NULL, '1+7C8+301', '12012 N LEDGES DR', "+
            "    'ROSCOE', 'IL', 'USA', '61073-9600', 'Ms.', "+
            "    'HALE', 'GRACIELLA', NULL, 'abc@intuit.com', '8156231511', "+
            "    NULL, NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1+7C8+301', '1-36XM67', '200767780', 'HALE INSURANCE AND INVESTMENTS AGENCY', '8156231511', "+
            "    410891, 'QB Payroll Standard', NULL, '1+7C8+301', '12012 N LEDGES DR', "+
            "    'ROSCOE', 'IL', 'USA', '61073-9600', 'Ms.', "+
            "    'HALE', 'GRACIELLA', NULL, 'abc@intuit.com', '8156231511', "+
            "    NULL, NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1-B1BA8S', '1-B5RZMN', '651190726', 'GATORVILLE INC', '7725674470', "+
            "    1710921, 'QB Payroll Standard', NULL, '1-B1BA8S', 'PO BOX 650131', "+
            "    'VERO BEACH', 'FL', 'USA', '32965-0131', 'Mr.', "+
            "    'SCHIEFELBEIN', 'KEN', NULL, NULL, '7725674470', "+
            "    NULL, NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1-B59UDW', '1-B5KQON', '593214295', 'DOBIES FUNERAL HOME', '7279377555', "+
            "    1710148, 'QB Payroll Enhanced Unlimited', NULL, '1-B59UDW', '4910 BARTELT RD', "+
            "    'HOLIDAY', 'FL', 'USA', '34690-5527', NULL, "+
            "    NULL, NULL, NULL, NULL, NULL, "+
            "    NULL, NULL)",
            "Insert into TEMP_MIGRATION"+
            "   (ACCOUNT_ROWID, AGREE_ROWID, EIN, DBA_NAME, COMPANY_PHONE, "+
            "    AGREE_NBR, QB_SUBTYPE, X_TAX_EXEMPT_EXP_DT, CONTACT_LINK, ADDR, "+
            "    CITY, COUNTRY, STATE, ZIPCODE, SUFFIX, "+
            "    LAST_NAME, FIRST_NAME, MI_NAME, EMAIL, WORK_PHONE, "+
            "    HOME_PHONE, MIGRATION_STATUS)"+
            " Values"+
            "   ('1-36XCIT', '1-36XCWS', '742344869', 'B & B AUTOMOTIVE SERVICES', '9797753413', "+
            "    410843, 'QB Payroll Standard', NULL, '1-36XCIT', '1007 S COULTER DR', "+
            "    'BRYAN', 'TX', 'USA', '77803-4502', 'Mrs.', "+
            "    'MELISSARI', 'KIM', 'J', 'KMELISSARI@SUDDENLINK.NET', '9797753413', "+
            "    '9797753413', NULL)"
    };

//    private void deleteTempTable() {
//        try {
//            Application.executeSqlCommand(sfmDropTempTable, true);
//        } catch (RuntimeException e) {
//            StringWriter sw = new StringWriter();
//
//            e.printStackTrace(new PrintWriter(sw));
//
//            List<String> stack = Arrays.asList(sw.toString().split("\r?\n"));
//
//            // if the table doesn't exist, that's ok; otherwise, re-throw the exception
//            if (!stack.toString().matches(".*table or view does not exist.*")) {
//                throw e;
//            }
//        }
//    }

//    private void createTempTable() {
//        deleteTempTable();
//        Application.executeSqlCommand(sfmCreateTempTable, true);
//    }

//    @Before
//    public void beforeEachTest() throws Exception {
//        Application.truncateTables();
//
//        createTempTable();
//
//        // fill temp table with migration data
//        for (String sql : sfmMigrationTestData) {
//            Application.executeSqlCommand(sql, true);
//        }
//    }

//    @After
//    public void afterEachTest() throws Exception {
//        deleteTempTable();
//    }

//    @Test
//    public void testMigration() {
//        MigrateCrisCompanies.main(new String[0]);
//    }

//    @Test
//    public void prepEmptyTempTable() {
//        Application.truncateTables();
//        //createTempTable();
//    }

//    @Test
//    public void prepForMigrationRun() {
//        Application.truncateTables();
//        Application.executeSqlCommand("update TEMP_MIGRATION set MIGRATION_STATUS = null", true);
//    }

//    @Test
//    public void testConsolidateEoEr() throws Exception {
//        ConsolidateEntitlementTask task = new ConsolidateEntitlementTask("810844505637458");
//        task.call();
//    }

//    @SuppressWarnings("unchecked")
//    private DomainEntitySet<Company> findCompany(SourceSystemCode pSourceSystemCd,
//                                                 String pAgreementNumber,
//                                                 String... pEinList) {
//        String hql = "   select company " +
//                     "     from com.intuit.sbd.payroll.psp.domain.Company as company " +
//                     "     join fetch company.QuickbooksInfo qbinfo " +
//                     "     join fetch company.CompanyServiceSet " +
//                     "     join fetch company.MailingAddress " +
//                     "     join fetch company.LegalAddress " +
//                     "     join fetch company.ContactSet " +
//                     "    where company.FedTaxId in (:einList) " +
//                     "      and company.SourceSystemCd = :sourceSystemCd " +
//                     "      and qbinfo.SubscriptionNumber = :agreementNumber " +
//                     " order by company.CreatedDate desc ";
//
//        Session session = Application.getHibernateSession();
//        org.hibernate.Query hibernateQuery = session.createQuery(hql);
//
//        hibernateQuery.setParameterList("einList", pEinList);
//        hibernateQuery.setParameter("sourceSystemCd", pSourceSystemCd);
//        hibernateQuery.setParameter("agreementNumber", pAgreementNumber);
//
//        System.out.println(hibernateQuery.getQueryString());
//
//        return Application.<Company>getUniqueActualObjects(hibernateQuery.list());
//    }
//
//    @Test
//    public void testCreateQuery() throws Exception {
//        Application.beginUnitOfWork();
//
//        try {
//            DomainEntitySet<Company> companySet = findCompany(SourceSystemCode.QBDT, "106710", "CRIS-541616474", "541616474");
//            System.out.println("Company set size: " + companySet.size());
//        } finally {
//            Application.rollbackUnitOfWork();
//        }
//    }
}

package com.intuit.sbd.payroll.psp.api;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.FakeSalesTaxGateway;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexUnitDataLoaderService;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.QbdtRequestInfo;
import com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayImpl;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.FileUtils;
import com.intuit.sbd.payroll.psp.util.XMLComparator;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static junit.framework.Assert.*;

/**
 * PSP in process API for unit testing
 * <br>Entry point for methods that are not suitable to be used outside unit tests
 * <br>Note: this will be moved under test when the _one_ dependency disappears
 */
public class PayrollServicesTest {
    private static SpcfLogger logger = SpcfLogManager.getLogger(PayrollServicesTest.class);
    private static String DEFAULT_BASE_DATE = "2012-01-01";
    //Values for year & month in case of parse exception for the above date
    private static int EXCEPTION_DEFAULT_BASE_YEAR = 2012;
    private static int EXCEPTION_DEFAULT_BASE_MONTH = 1;
    private static final String XML_TAG_MASK = "#####";
    private static final String XML_TAG_REGEX = "(%s)(.+?)(%s)";
    private static final String OPENING_TAG = "<%s>";
    private static final String END_TAG = "</%s>";
    public static Integer BASE_YEAR;
    public static Integer BASE_MONTH;

    static {
        String baseDateString = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "test.baseDate");
        Date baseDate;
        try {
            baseDate = new SimpleDateFormat("yyyy-mm-dd").parse(StringUtils.isNotEmpty(baseDateString) ? baseDateString : DEFAULT_BASE_DATE);
            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate);
            BASE_YEAR = cal.get(Calendar.YEAR);
            //Month is index in calendar
            BASE_MONTH = cal.get(Calendar.MONTH) + 1;
        } catch (ParseException e) {
            //Hard coding in case of parse exception
            BASE_YEAR = EXCEPTION_DEFAULT_BASE_YEAR;
            BASE_MONTH = EXCEPTION_DEFAULT_BASE_MONTH;
            logger.error(e);
        }
    }

    public static void truncateTables() {
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    public static void updateTables() {
        Application.updateTables();
    }

    public static void deleteFiles(String folderPath, String extension) {
        final String fileExtension = extension;
        File folder = new File(folderPath);

        // Get all files satisfying pattern
        File[] files = folder.listFiles(new FilenameFilter() {
            public boolean accept(File fDir, String strName) {
                return (strName.endsWith(fileExtension));
            }
        });

        // Delete all files
        for (File file : files) {
            file.delete();
        }
    }

    public static <T extends DomainEntity> T save(T dataEntity) {
        return Application.save(dataEntity);
    }

    public static void beforeEachTest() {
        Application.initialize();
        ApplicationSecondary.initialize();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.UnitTest);
        assertTransactionNotInProgress();
        assertTransactionNotInProgressSecondary();
        SalesTaxGatewayFactory.setInstanceClass(FakeSalesTaxGateway.class);
    }

    public static void afterEachTest() {
        assertTransactionNotInProgress();
        assertTransactionNotInProgressSecondary();
        if (System.getProperty("insertTestUsers") != null) {
            FlexUnitDataLoaderService.AddUsers();
            Application.executeSqlCommand("update psp_agency agency set AGENCY.AGENCY_SUPPORTED = CASE (select count(1) from PSP_PAYMENT_TEMPLATE pt where PT.AGENCY_FK = AGENCY.AGENCY_ID and PT.SUPPORT_START_DATE is not null) WHEN 0 THEN 0 ELSE 1 END", true);
        }
        SalesTaxGatewayFactory.setInstanceClass(SalesTaxGatewayImpl.class);
    }

    private static void assertTransactionNotInProgress() {
        if (Application.hasActiveTransaction()) {
            logger.error("Transaction in progress. The current unit of work originated at:\n" +
                    Application.getSessionCache().getOriginOfUnitOfWork());
            Application.rollbackUnitOfWork();
            // failing test from here suppresses assert messages from a test
            //TestCase.fail("Transaction in progress.");
        }
    }

    private static void assertTransactionNotInProgressSecondary() {
        if (ApplicationSecondary.hasActiveTransaction()) {
            logger.error("Transaction in progress. The current unit of work originated at:\n" +
                    ApplicationSecondary.getSessionCache().getOriginOfUnitOfWork());
            ApplicationSecondary.rollbackUnitOfWork();
            // failing test from here suppresses assert messages from a test
            //TestCase.fail("Transaction in progress.");
        }
    }


    public static <T> T assertSuccessResult(ProcessResult<T> pProcessResult) {
        return assertSuccessResult(null, pProcessResult);
    }

    public static <T> T assertSuccessResult(String pMessage, ProcessResult<T> pProcessResult) {
        assertSuccess(pMessage, pProcessResult);
        return pProcessResult.getResult();
    }


    public static void assertSuccess(ProcessResult pProcessResult) {
        assertSuccess(null, pProcessResult);
    }

    public static void assertSuccess(String pMessage, ProcessResult pProcessResult) {
        if (!pProcessResult.isSuccess()) {
            StringBuilder sb = new StringBuilder();
            if (pMessage != null) {
                sb.append(pMessage);
                sb.append('\n');
            }
            for (Message msg : pProcessResult.getMessages()) {
                sb.append(msg.getLevel().toString());
                sb.append(" ");
                sb.append(msg.getMessageCode());
                sb.append(": ");
                sb.append(msg.getMessage());
                sb.append(" at ");
                sb.append(msg.getInterestingStackElement());
                sb.append('\n');
            }
            TestCase.fail(sb.toString());
        }
    }

    //Assert a collection (like from a query) has only one result and then return that result
    public static <T> T assertOne(Collection<T> collection) {
        assertNotNull(collection);
        assertEquals("Collection size", 1, collection.size());
        return collection.iterator().next();
    }

    /**
     * Validate the contents from source transmission table
     * @param sourceSystemTransmission SourceSystemTransmission
     * @param expectedResponseLocation location
     * @param ignoreFields ignoreFields
     */
    public static void validateSourceSystemTransmission(SourceSystemTransmission sourceSystemTransmission, String expectedResponseLocation, List<String> ignoreFields) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            Criterion<SourceSystemTransmission> query = SourceSystemTransmission.CompanyId().equalTo(sourceSystemTransmission.getCompanyId())
                    .And(SourceSystemTransmission.FromSourceSystem().equalTo(SourceSystemCode.valueOf(sourceSystemTransmission.getFromSourceSystem().toString()))
                            .And(SourceSystemTransmission.ToSourceSystem().equalTo(SourceSystemCode.valueOf(sourceSystemTransmission.getToSourceSystem().toString())))
                            .And(SourceSystemTransmission.Type().equalTo(TransmissionType.valueOf(sourceSystemTransmission.getType().toString()))));

            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, new Query<SourceSystemTransmission>().Where(query)
                    .OrderBy(SourceSystemTransmission.CreatedDate().Descending()));
            String expectedResponse = FileUtils.readClasspathFileContent(expectedResponseLocation);
            String expectedResponseMasked = replaceIgnoreFields(expectedResponse, ignoreFields);
            String actualResponse = sourceSystemTransmissions.get(0).getResponseDocument();
            String actualResponseMasked = replaceIgnoreFields(actualResponse, ignoreFields);
            DetailedDiff diff = XMLComparator.compareXML(expectedResponseMasked, actualResponseMasked);
            List<?> allDifferences = diff.getAllDifferences();
            assertEquals("Differences found: " + diff.toString(), 0, allDifferences.size());
        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    /**
     * Validate the contents from PSP_QBDT_REQUEST_INFO table
     * @param sourceSystemTransmission
     * @param qbdtRequestInfo
     */
    public static void validateQbdtRequestInfo(SourceSystemTransmission sourceSystemTransmission, QbdtRequestInfo qbdtRequestInfo) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            Criterion<SourceSystemTransmission> query = SourceSystemTransmission.CompanyId().equalTo(sourceSystemTransmission.getCompanyId())
                    .And(SourceSystemTransmission.FromSourceSystem().equalTo(SourceSystemCode.valueOf(sourceSystemTransmission.getFromSourceSystem().toString()))
                            .And(SourceSystemTransmission.Type().equalTo(TransmissionType.valueOf(sourceSystemTransmission.getType().toString()))))
                    .And(SourceSystemTransmission.Description().equalTo(sourceSystemTransmission.getDescription()));

            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = ApplicationSecondary.find(SourceSystemTransmission.class, new Query<SourceSystemTransmission>().Where(query)
                    .OrderBy(SourceSystemTransmission.CreatedDate().Descending()));

            Criterion<QbdtRequestInfo> query1 = QbdtRequestInfo.SourceSystemTransmission().equalTo(sourceSystemTransmissions.get(0));

            DomainEntitySet<QbdtRequestInfo> qbdtRequestInfos = ApplicationSecondary.find(QbdtRequestInfo.class, new Query<QbdtRequestInfo>().Where(query1)
                    .OrderBy(QbdtRequestInfo.CreatedDate().Descending()));

            assertEquals(qbdtRequestInfo.getEmployeeAddCount(),qbdtRequestInfos.get(0).getEmployeeAddCount());
            assertEquals(qbdtRequestInfo.getEmployeeDeleteCount(),qbdtRequestInfos.get(0).getEmployeeDeleteCount());
            assertEquals(qbdtRequestInfo.getEmployeeUpdateCount(),qbdtRequestInfos.get(0).getEmployeeUpdateCount());
            assertEquals(qbdtRequestInfo.getPaycheckAddCount(),qbdtRequestInfos.get(0).getPaycheckAddCount());
            assertEquals(qbdtRequestInfo.getPaycheckDeleteCount(),qbdtRequestInfos.get(0).getPaycheckDeleteCount());
            assertEquals(qbdtRequestInfo.getPaycheckUpdateCount(),qbdtRequestInfos.get(0).getPaycheckUpdateCount());
            assertEquals(qbdtRequestInfo.getPayrollItemAddCount(),qbdtRequestInfos.get(0).getPayrollItemAddCount());
            assertEquals(qbdtRequestInfo.getPayrollItemDeleteCount(),qbdtRequestInfos.get(0).getPayrollItemDeleteCount());
            assertEquals(qbdtRequestInfo.getPayrollItemUpdateCount(),qbdtRequestInfos.get(0).getPayrollItemUpdateCount());
            assertEquals(qbdtRequestInfo.getPayrollTransactionAddCount(),qbdtRequestInfos.get(0).getPayrollTransactionAddCount());
            assertEquals(qbdtRequestInfo.getPayrollTransactionDeleteCount(),qbdtRequestInfos.get(0).getPayrollTransactionDeleteCount());
            assertEquals(qbdtRequestInfo.getPayrollTransactionUpdateCount(),qbdtRequestInfos.get(0).getPayrollTransactionUpdateCount());

        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    /**
     * Validate the contents from PSP_SAP_METHOD_CALL table
     * @param sapMethodCall SapMethodCall
     */
    public static void validateSapMethodCall(SAPMethodCall sapMethodCall) {
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            Criterion<SAPMethodCall> query = SAPMethodCall.ScreenPath().equalTo(sapMethodCall.getScreenPath())
                    .And(SAPMethodCall.ServiceName().equalTo(sapMethodCall.getServiceName()));

            DomainEntitySet<SAPMethodCall> sapMethodCalls = ApplicationSecondary.find(SAPMethodCall.class, new Query<SAPMethodCall>().Where(query)
                    .OrderBy(SAPMethodCall.CreatedDate().Descending()));

            assertEquals(sapMethodCalls.size(), 1);
            assertEquals(sapMethodCall.getExceptionTrace(), sapMethodCalls.get(0).getExceptionTrace());
            assertEquals(sapMethodCall.getMethodName(), sapMethodCalls.get(0).getMethodName());
            assertEquals(sapMethodCall.getParameters(), sapMethodCalls.get(0).getParameters());
            assertFalse("Created Date and Modified Date are same",sapMethodCalls.get(0).getCreatedDate().toString().equals(sapMethodCalls.get(0).getModifiedDate().toString()));

        } finally {
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }
    }

    /**
     * Certain fields in xml data keeps on change during each test run.
     * ignore those fields and compare the remaining contents
     * @param response expected response
     * @param ignoreFields ignoreFields
     * @return updated response with masked fields
     */
    private static String replaceIgnoreFields(String response, List<String> ignoreFields) {

        String updatedResponse = response;
        for (String ignoreField : ignoreFields) {

            final String openingTag = String.format(OPENING_TAG, ignoreField);
            final String endTag = String.format(END_TAG, ignoreField);
            updatedResponse = updatedResponse.replaceAll(String.format(XML_TAG_REGEX, openingTag, endTag), openingTag + XML_TAG_MASK + endTag);
        }
        return updatedResponse;
    }

    /**
     * Creates SourceSystemTransmission
     * @param companyId companyId
     * @param fromSourceSystem fromSourceSystem
     * @param toSourceSystem toSourceSystem
     * @param type type
     * @return SourceSystemTransmission
     */
    public static SourceSystemTransmission getSourceSystem(String companyId, SourceSystemCode fromSourceSystem, SourceSystemCode toSourceSystem, TransmissionType type) {

        SourceSystemTransmission sourceSystemTransmission = new SourceSystemTransmission();
        sourceSystemTransmission.setCompanyId(companyId);
        sourceSystemTransmission.setFromSourceSystem(fromSourceSystem);
        sourceSystemTransmission.setToSourceSystem(toSourceSystem);
        sourceSystemTransmission.setType(type);
        return sourceSystemTransmission;
    }

    public static SourceSystemTransmission getSourceSystem(String companyId, SourceSystemCode fromSourceSystem, SourceSystemCode toSourceSystem, TransmissionType type, String description) {

        SourceSystemTransmission sourceSystemTransmission = new SourceSystemTransmission();
        sourceSystemTransmission.setCompanyId(companyId);
        sourceSystemTransmission.setFromSourceSystem(fromSourceSystem);
        sourceSystemTransmission.setToSourceSystem(toSourceSystem);
        sourceSystemTransmission.setType(type);
        sourceSystemTransmission.setDescription(description);
        return sourceSystemTransmission;
    }

    public static QbdtRequestInfo getQbdtRequestInfo(int pEmployeeAddCount, int pEmployeeDeleteCount, int pEmployeeUpdateCount,
                                                     int pPaycheckAddCount,int pPaycheckDeleteCount, int pPaycheckUpdateCount,
                                                     int pPayrollItemAddCount, int pPayrollItemDeleteCount, int pPayrollItemUpdateCount,
                                                     int pPayrollTransactionAddCount, int pPayrollTransactionDeleteCount, int pPayrollTransactionUpdateCount) {

        QbdtRequestInfo qbdtRequestInfo = new QbdtRequestInfo();

        qbdtRequestInfo.setEmployeeAddCount(pEmployeeAddCount);
        qbdtRequestInfo.setEmployeeDeleteCount(pEmployeeDeleteCount);
        qbdtRequestInfo.setEmployeeUpdateCount(pEmployeeUpdateCount);

        qbdtRequestInfo.setPaycheckAddCount(pPaycheckAddCount);
        qbdtRequestInfo.setPaycheckDeleteCount(pPaycheckDeleteCount);
        qbdtRequestInfo.setPaycheckUpdateCount(pPaycheckUpdateCount);

        qbdtRequestInfo.setPayrollItemAddCount(pPayrollItemAddCount);
        qbdtRequestInfo.setPayrollItemDeleteCount(pPayrollItemDeleteCount);
        qbdtRequestInfo.setPayrollItemUpdateCount(pPayrollItemUpdateCount);

        qbdtRequestInfo.setPayrollTransactionAddCount(pPayrollTransactionAddCount);
        qbdtRequestInfo.setPayrollTransactionDeleteCount(pPayrollTransactionDeleteCount);
        qbdtRequestInfo.setPayrollTransactionUpdateCount(pPayrollTransactionUpdateCount);

        return qbdtRequestInfo;
    }

    /**
     * Creates SAPMethodCall
     * @param pScreenPath
     * @param pServiceName
     * @param pExceptionTrace
     * @param pMethodName
     * @param pParameters
     * @return SAPMethodCall
     */
    public static SAPMethodCall getSapMethodCall(String pScreenPath, String pServiceName, String pExceptionTrace, String pMethodName, String pParameters) {

        SAPMethodCall sapMethodCall = new SAPMethodCall();
        sapMethodCall.setScreenPath(pScreenPath);
        sapMethodCall.setServiceName(pServiceName);
        sapMethodCall.setExceptionTrace(pExceptionTrace);
        sapMethodCall.setMethodName(pMethodName);
        sapMethodCall.setParameters(pParameters);

        return sapMethodCall;
    }
}
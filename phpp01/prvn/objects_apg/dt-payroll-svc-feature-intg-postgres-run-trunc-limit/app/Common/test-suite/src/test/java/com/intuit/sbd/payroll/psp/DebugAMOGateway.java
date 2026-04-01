package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.amo.ProcessSavedAMOMessages;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.Hibernate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 22, 2011
 * Time: 8:01:11 PM
 */
public class DebugAMOGateway {
    private static SpcfLogger logger = Application.getLogger(DebugAMOGateway.class);

    public static void main(String[] args) throws Throwable {
        String licenseNumber = "267436355054195";
        String eoc = "389857";
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        File outputDir = new File(System.getProperty("user.dir") + File.separatorChar + "AMO");

        File[] files = outputDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                file.delete();
            }
        }

        long tokenCount = fetchAMOMessages(licenseNumber, eoc);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc, null, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, SpcfCalendar.createInstance());

        loadAMOMessages(licenseNumber, eoc);

        for(int i = 0; i < tokenCount; i++) {
            new ProcessSavedAMOMessages().processSavedAMOMessages();
        }
    }


    private static long fetchAMOMessages(String pLicenseNumber, String pEOC) throws Throwable {
        logger.info("Fetching AMO messages");
        int tokenCounter = 0;

        SimpleDateFormat createdDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
        createdDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Connection connection = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@sdgdbpspqa01.corp.intuit.net:1521:pspqa01",
                                                     "pspapp",
                                                     "pspapp01");
            if (connection != null) {
                String sql = " select token, message " +
                             " from psp_entitlement_message" +
                             " where license_number = ?" +
                             " and entitlement_offering_code = ?" +
                        " order by created_date";

                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, pLicenseNumber);
                stmt.setString(2, pEOC);

                ResultSet rs = stmt.executeQuery();

                Map<Long, List<String>> tokenMessageMap = new HashMap<Long, List<String>>();
                while (rs.next()) {
                    long token = rs.getLong("token");

                    String requestDocument = rs.getString("message");
                    if(!tokenMessageMap.containsKey(token)) {
                        tokenMessageMap.put(token, new ArrayList<String>());
                    }
                    tokenMessageMap.get(token).add(requestDocument);
                }

                int messageCounter = 0;
                for (Long token : tokenMessageMap.keySet()) {
                    String tokenCounterString = String.format("%05d", tokenCounter);
                    for (String requestDocument : tokenMessageMap.get(token)) {
                        messageCounter++;
                        File outputDir = new File(System.getProperty("user.dir") + File.separatorChar + "AMO");
                        String submissionCounterString = String.format("%05d", messageCounter);
                        String requestFileName = tokenCounterString + "_AMO_" + submissionCounterString + "." + "AMO";
                        String requestFilePath = outputDir.getAbsolutePath() + File.separator + requestFileName;
                        FileWriter fw = new FileWriter(requestFilePath, false);
                        fw.write(requestDocument);
                        fw.close();
                    }
                    tokenCounter++;
                }
            }
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        logger.info("Done fetching AMO messages");
        return tokenCounter;
    }

    public static void loadAMOMessages(String pLicense, String pEOC) {

        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AS400Migration));

        File requestDir = new File(System.getProperty("user.dir") + File.separator + "AMO");
        String[] messageFiles = requestDir.list();
        Arrays.sort(messageFiles, new Comparator<String>() {
            public int compare(String a, String b) {
                a = a.substring(0, a.indexOf("_"));
                b = b.substring(0, b.indexOf("_"));
                return a.compareTo(b);
            }
        });

        try {
            PayrollServices.beginUnitOfWork();
            long batchToken = SystemParameter.findLongValue(SystemParameter.Code.AMO_BATCH_TOKEN, -1);
            for (String messageFile : messageFiles) {
                String message = readFile(requestDir + File.separator + messageFile);

                EntitlementMessage entitlementMessage = new EntitlementMessage();
                entitlementMessage.setLicenseNumber(pLicense);
                entitlementMessage.setEntitlementOfferingCode(pEOC);
                entitlementMessage.setToken(batchToken + Long.parseLong(messageFile.substring(0, messageFile.indexOf("_"))));
                entitlementMessage.setStatus(EntitlementMessageStatusCode.New);
                entitlementMessage.setFailureCount(0);
                Application.save(entitlementMessage);
                entitlementMessage.setMessage(message);
            }
            PayrollServices.commitUnitOfWork();
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static String readFile(String pFileName) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            FileReader fileReader = new FileReader(new File(pFileName));
            BufferedReader input = new BufferedReader(fileReader);
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }

            return stringBuilder.toString();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}

package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.hibernate.Session;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;



/**
 * User: VVAKATI
 * Date: 3/10/19
 * Time: 12:24 PM
 */
@ScheduledJob(name = "SoxDBUserReport", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class,  scheduleGenerator = JSSScheduleGenerator.class)
public class SoxDBUsersProcessor extends JSSBatchJob {

    static {
        ApplicationSecondary.initialize();
    }

    public SoxDBUsersProcessor(String[] pArguments) {
        super(pArguments);
    }

    public SoxDBUsersProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }
    private static String DATE_FORMAT = "yyyyMMdd_hhmm";
    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

    private static String REPORT_DATE_FORMAT = "dd-MMM-yy";
    private static SimpleDateFormat reportDateFormat = new SimpleDateFormat(REPORT_DATE_FORMAT);


    private static String message = "Please see attached.";
    private static StringBuilder headerBuilder=new StringBuilder();
    private static String dbMetadata = "select db_unique_name, sysdate from v$database";
    private static StringBuilder dbExecutionHeaderBuilder=new StringBuilder();

    static{
        headerBuilder.append(rightPadding("Account",25)).append("|")
                .append(rightPadding("Account Status",16)).append("|")
                .append(rightPadding("Role",30)).append("|")
                .append(rightPadding("Privilege",65)).append("|")
                .append(rightPadding("Profile",30)).append("|")
                .append(rightPadding("created",9))
                .append("\n")
                 .append(rightPaddingSeparator("",180)).append("\n");

        dbExecutionHeaderBuilder.append(rightPadding("DB_UNIQUE_NAME",30)).append("|SYSDATE").append("\n").append(rightPaddingSeparator("",55));
    }





    private static String soxQuery="SELECT username Account,account_status \"Account Status\",null role,null privilege,profile, created FROM dba_users\n" +
            "UNION\n" +
            "SELECT grantee Account,null \"Account Status\",granted_role role,null privilege,null profile, null created FROM dba_role_privs WHERE grantee IN (SELECT username FROM dba_users)\n" +
            "UNION\n" +
            "SELECT grantee Account,null \"Account Status\",null role,privilege,null profile,null created FROM dba_sys_privs WHERE grantee IN (SELECT username FROM dba_users)\n" +
            "UNION\n" +
            "SELECT grantee Account,null \"Account Status\",null role, privilege||' on '||owner||'.'||table_name privilege, null profile, null created FROM dba_tab_privs WHERE grantee IN (SELECT username FROM dba_users)\n" +
            "ORDER BY ACCOUNT";


    @Override
    protected void execute() {
        getLogger().info("Starting SoxDBUsersProcessor batch job");
        StopWatch timer = StopWatch.startTimer();

        executeStep(WriteSOXDBUSersReport.class);

        getLogger().info("Completed SoxDBUsersProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }




    public static class WriteSOXDBUSersReport extends JSSBatchJobStep<SoxDBUsersProcessor> {
        public void execute() {
            try {
                Application.beginUnitOfWork(FlushMode.MANUAL,true);
                Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.SoxDBUserReportBatchJob));
                QueryAndReport(Application.getHibernateSession());
                Application.commitUnitOfWork();
		ApplicationSecondary.beginUnitOfWork(FlushMode.MANUAL);
                QueryAndReport(ApplicationSecondary.getHibernateSession());
                ApplicationSecondary.commitUnitOfWork();

            } catch (Throwable t) {
                getLogger().error("Error in step WriteSOXDBUSersReport", t);
            } finally {
            	Application.rollbackUnitOfWork();
	    }
        }

        private void QueryAndReport(Session session) {
            org.hibernate.Query soxquery = session.createSQLQuery(soxQuery);
            org.hibernate.Query dbMetadataQuery = session.createSQLQuery(dbMetadata);
            ScrollableResults dbMetadataResult = dbMetadataQuery.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
            ScrollableResults soxqueryResult = soxquery.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
            getBatchJobProcessor().SendEmailToRecipients(soxqueryResult,dbMetadataResult,session);
        }
    }

    private void SendEmailToRecipients(ScrollableResults pQueryResults,ScrollableResults metadataResults,Session session)  {

        String dbname=null;
        String dbTime=null;
        while (metadataResults.next()) {
            dbname= (String)metadataResults.get(0);
            dbTime= metadataResults.get(1)+"";
        }

        String filenameExtension = ".log";
        String tempDir = BatchUtils.getConfigString("psp_batch_temp", "");
        String executionTimestamp=simpleDateFormat.format(new Date());
        String filename = String.format("%s_account_info_%s", dbname,executionTimestamp);
        File tempDbusersReportFile = new File(tempDir, filename + filenameExtension);
        if (!tempDbusersReportFile.getParentFile().exists()) {
            boolean created = tempDbusersReportFile.getParentFile().mkdirs();
            if (!created) {
                getLogger().error("Unable to create directory for temp SoxDBUserReport files.");
                return;

            }

        }
        // Write out report to file so it can be attached
        FileWriter writer;
        int numberOfRecords = 0;
        try {
            writer = new FileWriter(tempDbusersReportFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            bufferedWriter.append(dbMetadata+"\n\n").append("Audit script execution time: \n").append(dbExecutionHeaderBuilder.toString()).append("\n").append(rightPadding(dbname,30)).append("|").append(dbTime).append("\n\n\nQuery executed is :\n\n\n").append(soxQuery).append("\n\n\n");

            bufferedWriter.append(headerBuilder.toString());


            while (pQueryResults.next()) {
                numberOfRecords++;
                bufferedWriter.append(rightPadding((String)pQueryResults.get(0),25)).append("|");
                bufferedWriter.append(rightPadding((String)pQueryResults.get(1),16)).append("|");
                bufferedWriter.append(rightPadding((String)pQueryResults.get(2),30)).append("|");
                bufferedWriter.append(rightPadding((String)pQueryResults.get(3),65)).append("|");
                bufferedWriter.append(rightPadding((String)pQueryResults.get(4),30)).append("|");
                if(pQueryResults.get(5) != null)
                {
                    bufferedWriter.append(reportDateFormat.format(pQueryResults.get(5)));
                }

                bufferedWriter.append("\n");
                // To allow for unlimited size result sets, we need to keep the cache clean.
                evictObjectsFromCache(pQueryResults.get(),session);

            }
            bufferedWriter.append("\n\n");
            bufferedWriter.append(numberOfRecords+"").append( " rows selected.");
            bufferedWriter.close();
            writer.close();


        } catch (Throwable e) {
            throw new RuntimeException(e);

        } finally {
            pQueryResults.close();

        }
        if (numberOfRecords > 0) {

            StringBuilder subject = new StringBuilder();
            subject.append(dbname).append(":").append(" Account Report");



            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString("psp_sox_email_list"),
                    "no_reply@intuit.com",
                    subject.toString(),
                    message,
                    tempDbusersReportFile.getAbsolutePath());

        } else {
            getLogger().info("No records found to email in SoxDBUserReport");

        }

    }

    protected static void evictObjectsFromCache(Object[] pObjects,Session session) {
        for (Object obj : pObjects) {
            if (obj != null) {
                Application.evict(obj, session);

            }

        }

    }

    private static String rightPadding(String str, int num) {
        if(str == null)
        {
            str = "";
        }
        return String.format("%1$-" + num + "s", str);
    }

    private static String rightPaddingSeparator(String str, int num) {
        if(str == null)
        {
            str = "";
        }
        return String.format("%1$-" + num + "s", str).replace(' ', '-');
    }
}

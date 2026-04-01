package com.intuit.sbd.payroll.psp.jss.processors;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.*;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSenderService;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.processors.plSqlBatchJob.handler.PlSqlJobHandler;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJobs;
import org.hibernate.SQLQuery;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 *This class executes the native anonymous PlSql procedures and
 * records the output and sends it in email
 */

@ScheduledJobs(
        {@ScheduledJob(name = "FailedPayrollPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "PayrollFraudBatchPurgePlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "EFTPSOnHoldPaymentPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "ValidateEmployeeWagePlansPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "NCDFixALLPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "OfferingUpdateUsageBillingPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "EDRAssociationFixPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "NCDFixPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "CostCoPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class),
        @ScheduledJob(name = "RetryEntitlementActivationPlSqlJobsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
        })
public class PlSqlJobsProcessor extends JSSBatchJob {

    private static PlSqlJobHandler plSqlJobHandler;

    public PlSqlJobsProcessor(String[] pArguments) {
        super(pArguments);
    }

    public PlSqlJobsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting PlSqlJobsProcessor batch job");
        getLogger().info("The parameters for the batch job "+this.getBatchJobType());
        getLogger().info("=========================================================");
        Map<String,String> params =this.getParameters();
        for(Map.Entry entry: params.entrySet()){
           getLogger().info("Params are In primary execute  "+entry.getKey()+" entry value "+entry.getValue());
        }

        getLogger().info("Starting batch job ");
        StopWatch timer = StopWatch.startTimer();
        executeStep(ExecutePLSql.class);
        executeStep(SendEmail.class);
        getLogger().info("Completed PlSqlJobsProcessor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }


    public static class ExecutePLSql extends JSSBatchJobStep<PlSqlJobsProcessor> {

        /**Returns the file content as string
         *Important : It appends a space for each newline , so that sql statements are
         * well formed
         * @param fileName
         * @return
         * @throws IOException
         */
        public static String readFileASString(String fileName) throws IOException {
            String folderRoot= BatchUtils.getConfigString("sql_scripts_root");
            String file=BatchUtils.getConfigString("sql_scripts_root")+fileName;

            StringBuilder builder = new StringBuilder();
            try (InputStream input = new FileInputStream(file)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(input, "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(" ").
                            append(line).
                            append(" ")    ;
                }
            }
            return builder.toString();
        }
        @Override
        public void execute() {

            if (plSqlJobHandler == null) {
                plSqlJobHandler =  PayrollApplicationBeanFactory.getBean(PlSqlJobHandler.class);
            }

            getLogger().info("Starting executeplsql step ");

            Map<String,String> params =this.getBatchJobProcessor().getParameters();
            String batchJobName= this.getBatchJobProcessor().getBatchJobType().name();
            getLogger().info("batch job name is "+batchJobName);
            for(Map.Entry entry: params.entrySet()){
                getLogger().info("Params are ExecuteStep  "+entry.getKey()+" entry value "+entry.getValue());
            }

            getLogger().info("Filename is "+params.get(batchJobName+"."+"sql_files_list"));
            String sqlFileListString=params.get(batchJobName+"."+"sql_files_list");
            String [] sqlFiles  = sqlFileListString.split(",");
            String sqlStr="";
           /* try {
                 sqlStr = readFileASString(params.get(batchJobName+"."+"sql_files_list"));
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            try {

                String currentTimeStamp=new SimpleDateFormat("yyyyMMddHHmm").format(new java.util.Date());;
                String filename=BatchUtils.getConfigString("psp_batch_temp")+File.separator+batchJobName+"_"+currentTimeStamp+"_"+"dbgenoutput.txt";
                //put this filename into parameter map
                //so that it can be fetched in sendmail step
                this.getBatchJobProcessor().getParameters().put("scriptOutputFileName",filename);

                //String filename=BatchUtils.getConfigString("sql_scripts_root")+batchJobName+"_"+"dbgenoutput.txt";
                File file = new File(filename);
                FileWriter fw= null;

                try {
                    fw = new FileWriter(file);
                } catch (IOException e) {
                    getLogger().info("Error while writing the file "+filename);
                    e.printStackTrace();
                }
                BufferedWriter bw= new BufferedWriter(fw);
                Application.beginUnitOfWork();
                if (FeatureFlags.get().booleanValue(FeatureFlags.Key.PLSQL_NAMED_QUERY, false)) {
                    getLogger().info("Executing the PLSQL namedquery");
                    String output = plSqlJobHandler.process(batchJobName);
                    writeOutput(output, fw, batchJobName);
                }
                else {
                    SQLQuery enableDBMSOP =Application.getHibernateSession().createSQLQuery("begin dbms_output.enable(); end;");
                    enableDBMSOP.executeUpdate();
                    getLogger().info("Executing the Anonymous PLSQL ");
                    for(String sqlfile :sqlFiles) {
                        try {
                            sqlStr = readFileASString(sqlfile);
                        } catch (IOException e) {
                            getLogger().error("Error while reading content of sql file");
                            e.printStackTrace();
                        }
                        Application.executeAnonymousSQl(sqlStr, false);
                    }
                    Connection connection = Application.getConnection();
                    CallableStatement call = connection.prepareCall("declare "
                            + "  num integer := 1000;"
                            + "begin "
                            + "  dbms_output.get_lines(?, num);"
                            + "end;");
                    call.registerOutParameter(1, Types.ARRAY,
                            "DBMSOUTPUT_LINESARRAY");
                    call.execute();

                    Array array = null;

                    try {
                        array = call.getArray(1);
                        Object[] dbmsOutPut= (Object[]) array.getArray();
                        String res="";
                        for(int i=0;i<dbmsOutPut.length&&dbmsOutPut[i]!=null;i++){
                            res=res+dbmsOutPut[i]+"\n";
                        }
                        getLogger().info(res);

                        fw.write(res);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (array != null)
                            array.free();
                        try {
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Application.commitUnitOfWork();
                getLogger().info("Completed processing ");



            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                Application.rollbackUnitOfWork();

            }
        }

        private void writeOutput(String output, FileWriter fw, String batchJobName) {
            try {
                getLogger().info(output);
                fw.write(output);
            } catch (IOException e) {
                getLogger().error("BatchJob={}, Event=WriteOutputToFile, Status=Failed", batchJobName, e);
            } finally {
                try {
                    fw.close();
                } catch (IOException e) {
                    getLogger().error("BatchJob={}, Event=CloseFile, Status=Failed", batchJobName, e);
                }
            }
        }


    }

    public static class SendEmail extends JSSBatchJobStep<PlSqlJobsProcessor> {

        private String readContentForBody(String fileName) throws IOException {
            File file = new File(fileName);
            FileReader fr= new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuffer str=new StringBuffer();
            String line="";
            while((line=br.readLine())!=null){
                str.append(line).append("\n");
            }
            return str.toString();
        }

        @Override
        protected void execute() throws Exception {
            getLogger().info("Starting Sendmail  step  for "+this.getBatchJobProcessor().getBatchJobType());
            String batchJobName= this.getBatchJobProcessor().getBatchJobType().name();
            String attachmentFileName = this.getBatchJobProcessor().getParameters().get("scriptOutputFileName");
            //all the parameters of this batch job
            Map<String,String> params =this.getBatchJobProcessor().getParameters();

            for(Map.Entry entry: params.entrySet()){
                getLogger().info("Params are ExecuteStep  "+entry.getKey()+" entry value "+entry.getValue());
            }

            String toEmailAddresses=params.get(batchJobName+".email_recipient");
            String subject=params.get(batchJobName+".email_subject");
            String content="";
            content = readContentForBody(attachmentFileName);
            content = "Hi All \n\n"+content+"\n\n\n\n"+"Thanks\n-PSP Batch Jobs Team";
            String body=content;

            String fromEmailAddress="no_reply@intuit.com";
            String fromEmailDisplayName="PSP-batch-job-processor";

            String pServerName = ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "internalemailserver");

            EmailSenderService emailSenderService = PayrollApplicationBeanFactory.getBean(EmailSenderService.class);
            MailSenderSpringService mailSenderSpringService = PayrollApplicationBeanFactory.getBean(MailSenderSpringService.class);
            OINPRequestHelper oinpRequestHelper = PayrollApplicationBeanFactory.getBean(OINPRequestHelper.class);

            List<String> fileList = new ArrayList<String >();

            getLogger().info("File to be emailed is "+this.getBatchJobProcessor().getParameters().get("scriptOutputFileName"));
            fileList.add(attachmentFileName);

            if(mailSenderSpringService.isTemplateSubjectOINPEnabled(subject,true)){
                EmailRequest request = oinpRequestHelper.createOINPEmailRequest(toEmailAddresses,fromEmailAddress,subject,body,fileList,null,null);
                request.setEmailStrategyType(EmailStrategyType.OINPWithAttachments);

                try {
                    EmailResponse response = emailSenderService.sendMailViaOINP(request);
                    if (response.getHttpServiceResponse().isSuccessful()) {
                        getLogger().info("OINP: Email sent successfully for batch job " + batchJobName);
                        //now delete the file
                        getLogger().info("OINP: Deleting the file " + attachmentFileName);
                        FileUtils.deleteFile(attachmentFileName);
                    }
                } catch (Exception e) {
                    getLogger().error("OINP: SendEmail failed due to: " + e);
                    e.printStackTrace();
                }
            } else {
                EmailRequest request = EmailRequest.builder()
                        .toEmailAddresses(toEmailAddresses.split(","))
                        .subject(subject)
                        .htmlContent(TextToHtmlConverter.textToHTML(body))
                        .fromEmailAddress(fromEmailAddress)
                        .fromEmailDisplayName(fromEmailDisplayName)
                        .pHighPriority(true)
                        .emailStrategyType(EmailStrategyType.SendGridWithAttachments)
                        .build();
                request.setAttachmentList(fileList);

                try {
                    EmailResponse response = emailSenderService.sendMail(request);
                    if (response != null && response.getStatus() == 200) {
                        getLogger().info("Email sent successfully for batch job " + batchJobName);
                        //now delete the file
                        getLogger().info("Deleting the file " + attachmentFileName);
                        FileUtils.deleteFile(attachmentFileName);
                    }
                } catch (Exception e) {
                    getLogger().error("SendEmail failed due to: " + e);
                    e.printStackTrace();
                }
            }
        }
    }
}




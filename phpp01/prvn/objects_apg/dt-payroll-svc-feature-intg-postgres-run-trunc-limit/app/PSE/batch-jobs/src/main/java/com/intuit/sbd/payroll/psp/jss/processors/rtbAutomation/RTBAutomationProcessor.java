package com.intuit.sbd.payroll.psp.jss.processors.rtbAutomation;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.RTBBackUpEventType;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.RTBAUTOMATIONBACKUPExpression;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import scala.App;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sandeep Modgil
 * Description: Batch job to delete Automation backup data
 * Date: Aug 28, 2019
 */
@ScheduledJob(name = "RTBAutomation", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class RTBAutomationProcessor extends JSSBatchJob {

    private SpcfCalendar mRunDate;
    private Boolean mPriorDate = false;

    public RTBAutomationProcessor(String[] pArguments) {
        super(pArguments);
    }

    public RTBAutomationProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    public SpcfCalendar getRunDate() {
        return mRunDate;
    }

    public void setRunDate(SpcfCalendar pRunDate) {
        mRunDate = pRunDate;
    }

    public Boolean isPriorDate() {
        return mPriorDate;
    }

    public void setIsPriorDate(Boolean pPriorDate) {
        mPriorDate = pPriorDate;
    }

    @Override
    protected void validateRuntimeParameters() {
        SpcfCalendar now = PSPDate.getPSPTime();
        String commandLine = getJobInstanceParameters().trim();

        if (commandLine.length() == 0) {
            setRunDate(now.copy());
        } else {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    // date must be formatted as yyyyMMdd (more precisely, the format must be 20yyMMdd)
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);

                        setRunDate(SpcfCalendar.createInstance(clDate.getYear(),
                                clDate.getMonth(),
                                clDate.getDay(),
                                0, 0, 0, 0,
                                SpcfTimeZone.getLocalTimeZone()));
                        setIsPriorDate(true);
                    }
                }
            }
        }
    }

    @Override
    protected void validateStepRuntimeParameters(String stepName) {
        validateRuntimeParameters();
    }

    @Override
    protected void execute() {
        getLogger().info("Starting RTB Automation processor");

        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.RTBAutomationBatchJob));

        /*  Create the files    */
        executeStep(RTBAutomationDataCleanup.class);

        getLogger().info("Completed RTB Automation processor. Elapsed time: " + timer.stop().getElapsedTimeString());
    }

    public static class RTBAutomationDataCleanup extends JSSBatchJobStep<RTBAutomationProcessor> {
        public void execute() {
            try {

                getLogger().info("Starting RTBAutomationDataCleanup job");
                String noofDaysBefore = BatchUtils.getConfigString("psp_jss_duplicateemp_sendemailbeforedelete_days");
                String noofDaysAfter = BatchUtils.getConfigString("psp_jss_duplicateemp_sendemailafterdelete_days");
                String eventTypeConfig =  BatchUtils.getConfigString("psp_jss_rtbautomation_eventtypes");
                boolean activeTransaction=false;
                Integer daysBefore = Integer.valueOf(noofDaysBefore);
                Integer daysAfter = Integer.valueOf(noofDaysAfter);
                String[] eventTypes=null;
                Integer deletedCount=0;
                Integer totalCount=0;
                //Fetch all Event Types
                if(eventTypeConfig!=null){
                    eventTypes=eventTypeConfig.split(",");
                }
                String recipient="";
                //Deleting the data based on Configured eventtype and no of Days.
                for(String eventType:eventTypes){
                    DomainEntitySet<RTBAUTOMATIONBACKUP> rtbAutomationBackups =
                            Application.find(RTBAUTOMATIONBACKUP.class, RTBAUTOMATIONBACKUP.EventType().equalTo(RTBBackUpEventType.valueOf(eventType))
                            );
                    StringBuffer ids=new StringBuffer();
                    for(RTBAUTOMATIONBACKUP rtbAutomationBackup: rtbAutomationBackups) {
                        ids.append(rtbAutomationBackup.getId()+", ");
                    }
                    getLogger().info("RTBAutomationProcessor: Data fetched for unique id = "+ids.toString());

                    for(RTBAUTOMATIONBACKUP rtbAutomationBackup: rtbAutomationBackups) {
                        getLogger().info("RTBAutomationProcessor: Difference in days " +CalendarUtils.getDifferenceInDays(PSPDate.getPSPTime(), rtbAutomationBackup.getModifiedDate()) );

                        //Code to delete the backup and send email to customer

                        if (Math.abs(CalendarUtils.getDifferenceInDays(PSPDate.getPSPTime(), rtbAutomationBackup.getModifiedDate())) == daysBefore){
                            //Send email to the customer before deleting the data
                            totalCount+=1;
                            Date modifiedDate =CalendarUtils.convertToDate(rtbAutomationBackup.getModifiedDate());
                            SpcfCalendar tempDate =PSPDate.getPSPTime();
                            tempDate.addDays(+1);
                            Date deleteDate = CalendarUtils.convertToDate(tempDate);

                            getLogger().info("RTBAutomationProcessor: User is null.Getting the user based on corpId");
                            AuthUser user = AuthUser.findUser(rtbAutomationBackup.getCreatorId());
                            getLogger().info("RTBAutomationProcessor: Retrieved the user based on corpId");
                            String predeletionMsg = BatchUtils.pMessage;
                            if(user!=null){
                                getLogger().info("RTBAutomationProcessor: User is not null");
                                predeletionMsg=predeletionMsg.replace("PayrollAdminFirstName",user.getFirstName());
                                predeletionMsg=predeletionMsg.replace("PayrollAdminLastName",user.getLastName());
                                recipient=user.getFirstName()+"_"+ user.getLastName() +"@intuit.com";
                                predeletionMsg=predeletionMsg.replace("DeletionDate",deleteDate.toString());
                                predeletionMsg=predeletionMsg.replace("ModifiedDate",modifiedDate.toString());
                                predeletionMsg=predeletionMsg.replace("GetUniqueId",rtbAutomationBackup.getId().toString());
                                predeletionMsg=predeletionMsg.replace("CompanyId",rtbAutomationBackup.getCompanyId());
                                getLogger().info("RTBAutomation: Sending Intimation for Deletion ");
                                BatchUtils.sendRTBBackupDeletionNotificationEmail(predeletionMsg,recipient);
                                predeletionMsg=BatchUtils.pMessage;
                                getLogger().info("RTBAutomation: Intimation for Deletion sent ");
                            }else{
                                getLogger().warn("RTBAutomation: User is null.Hence no notification will be sent");
                            }

                        }else  if (Math.abs(CalendarUtils.getDifferenceInDays( PSPDate.getPSPTime(), rtbAutomationBackup.getModifiedDate())) == daysAfter) {
                            //Data cleanup
                            PayrollServices.beginUnitOfWork();
                            Application.delete(rtbAutomationBackup);
                            getLogger().info("RTBAutomationProcessor: Deleted record unique id is: "+rtbAutomationBackup.getId());
                            PayrollServices.commitUnitOfWork();
                            deletedCount+=1;
                            totalCount+=1;
                            getLogger().info("RTBAutomationProcessor: User is null.Getting the user based on corpId");
                            AuthUser user = AuthUser.findUser(rtbAutomationBackup.getCreatorId());
                            getLogger().info("RTBAutomationProcessor: Retrieved the user based on corpId");
                            String postdeletionMsg = BatchUtils.deletionMsg;
                            if(user!=null){
                                getLogger().info("RTBAutomationProcessor: User is not null");
                                postdeletionMsg=postdeletionMsg.replace("PayrollAdminFirstName",user.getFirstName());
                                postdeletionMsg=postdeletionMsg.replace("PayrollAdminLastName",user.getLastName());
                                recipient=user.getFirstName()+"_"+ user.getLastName() +"@intuit.com";
                                Date modifiedDate =CalendarUtils.convertToDate(rtbAutomationBackup.getModifiedDate());
                                postdeletionMsg=postdeletionMsg.replace("ModifiedDate",modifiedDate.toString());
                                postdeletionMsg=postdeletionMsg.replace("CompanyId",rtbAutomationBackup.getCompanyId());
                                postdeletionMsg=postdeletionMsg.replace("CurrentDate",PSPDate.getPSPTime().toString());
                                postdeletionMsg=postdeletionMsg.replace("EventType",eventType);
                                getLogger().info("RTBAutomation: Sending Deletion mail ");
                                BatchUtils.sendRTBBackupDeletionNotificationEmail(postdeletionMsg,recipient);
                                postdeletionMsg=BatchUtils.deletionMsg;
                                getLogger().info("RTBAutomation: Deletion mail sent ");
                            }else{
                                getLogger().warn("RTBAutomation: User is null.Hence no notification will be sent");
                            }

                        }else{
                            totalCount+=1;
                            getLogger().info("RTBAutomationDataCleanup: No matching data found to be deleted ");
                        }

                    }
                    getLogger().info("RTBAutomation: TotalCount for "+eventType+"= "+ totalCount +" and DeletedCount = "+deletedCount);
                    getLogger().info("Completed RTBAutomationDataCleanup job");
                    //Setting totalcount back to 0 for next event type
                    totalCount=0;
                }

            } catch (Throwable t) {
                PayrollServices.rollbackUnitOfWork();
                getLogger().info("RTBAutomationDataCleanup: exception message "+t.getMessage());
                throw new RuntimeException("Exception in RTBAutomationDataCleanup job "+ getClass().getSimpleName(), t.getCause());
            }
        }
    }
}


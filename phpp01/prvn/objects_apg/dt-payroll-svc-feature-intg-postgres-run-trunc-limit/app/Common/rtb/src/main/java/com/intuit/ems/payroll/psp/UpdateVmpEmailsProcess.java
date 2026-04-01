package com.intuit.ems.payroll.psp;

import com.intuit.platform.integration.ius.common.types.User;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iam.ConsumerRealm;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.paycycle.util.StringUtil;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: praveenkumarh635
 * Date: 8/17/17
 * Time: 12:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateVmpEmailsProcess {



    public static void main(String[] args) {
        int batchSize = 5000;
        System.out.println("Started processing");
        UpdateVmpEmailsProcess updateVmpEmailsProcess = new UpdateVmpEmailsProcess();
        if(args != null && args.length > 0)
            batchSize = Integer.valueOf(args[0]);
        updateVmpEmailsProcess.process(batchSize);
        System.out.println("Completed processing");
    }

    public void process(int batchSize){
        ExecutorService executorService = null;
        int interval = 60;
        int maxWait = 300;
        System.out.println("Batch size : "+batchSize);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // Create threadPool with given parameters
            executorService = Executors.newFixedThreadPool(10);
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(executorService);

            Boolean done = Boolean.FALSE;

            while(!done) {
                Application.beginUnitOfWork();
                // We create VmpPaystubNotification events in FormatError state
                // because they don't have an email address to start
                Expression<CompanyEventEmail> query = new Query<CompanyEventEmail>().Where(
                        CompanyEventEmail.EmailTemplateTypeCd().in(EventEmailTemplateTypeCode.VmpPaystubNotification)
                                .And(CompanyEventEmail.StatusCd().in(EventEmailStatus.FormatError)))
                        .LimitResults(0, batchSize);
                DomainEntitySet<CompanyEventEmail> companyEventEmails = Application.find(CompanyEventEmail.class,
                        query);

                if(companyEventEmails.size() == 0){
                    done = Boolean.TRUE;
                    break;
                }

                List<CompanyEventEmail> eventEmail = new ArrayList<CompanyEventEmail>(companyEventEmails);

                List<List<CompanyEventEmail>> partition = ListUtils.partition(eventEmail, 100);
                for(final List<CompanyEventEmail> eventEmailList : partition){
                    completionService.submit(new Callable<Integer>() {
                        public Integer call() throws Exception {
                            System.out.println("Processing "+eventEmailList.size()+" records");
                            return updateRecepientEmailId(eventEmailList);
                        }
                    });
                }

                // Get the results of each thread execution
                int recordsProcessedCount = 0;

                for (int t = 0; t < partition.size(); t++) {
                    Future<Integer> f = completionService.take();
                    recordsProcessedCount += f.get();

                    if (recordsProcessedCount % 1000 == 0) {
                        System.out.println("working -- completed processing " + recordsProcessedCount + " records  in " + stopWatch.getElapsedTimeString());
                    }
                }
                Application.commitUnitOfWork();
                System.out.println("Completed processing "+recordsProcessedCount+" records");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }  catch (ExecutionException e) {
            throw ThreadingUtils.launderThrowable(e.getCause());
        }
        finally {
            if (executorService != null) {
                ThreadingUtils.shutdownAndAwaitTermination(executorService, interval, maxWait);
            }
        }
    }

    public Integer updateRecepientEmailId(List<CompanyEventEmail> eventEmails){

        try {
            Application.beginUnitOfWork();
            for(CompanyEventEmail companyEventEmail : eventEmails) {
                try {
                    companyEventEmail = Application.findById(CompanyEventEmail.class, companyEventEmail.getId());

                    DomainEntitySet<CompanyEventDetail> details = companyEventEmail.getCompanyEvent()
                            .getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeId);

                    //companyEventEmail = companyEvent.getCompanyEventEmailCollection().getFirst();
                    if (details != null) {
                        String employeeId = details.getFirst().getValue();
                        Employee employee = Application.findById(Employee.class,
                                SpcfUniqueId.createInstance(employeeId));
                        if (employee.getConsumerRealmId() == null) {
                            System.out.println("Error can't look up email address for employee " + employeeId
                                    + " because consumer realm id is missing.");

                            companyEventEmail.setStatusCd(EventEmailStatus.Ignore);
                        } else {

                            ConsumerRealm consumerRealm = new ConsumerRealm();
                            User userForConsumerRealmId = consumerRealm.getUserForConsumerRealmId(employee.getConsumerRealmId());
                            String recipientEmailAddress =  userForConsumerRealmId != null && userForConsumerRealmId.getEmail() != null ? userForConsumerRealmId.getEmail().getAddress() : null;

                            if (!StringUtil.isNullOrEmpty(recipientEmailAddress)
                                    && Validator.isValidEmail(recipientEmailAddress)) {
                                addEmailParameter(companyEventEmail, EventEmailParamTypeCode.RecipientEmail,
                                        recipientEmailAddress);
                                // Set to pending so email gateway picks it
                                // up
                                companyEventEmail.setStatusCd(EventEmailStatus.Pending);
                            } else {
                                // Ignore sending emails to invalid email
                                // addresses
                                System.out.println("Error invalid emailAddress=" + recipientEmailAddress
                                        + " for employeeId=" + employeeId + " with consumerRealmId="
                                        + employee.getConsumerRealmId());
                                companyEventEmail.setStatusCd(EventEmailStatus.Ignore);
                            }
                            companyEventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());
                            Application.save(companyEventEmail);
                        }
                    } else {
                        System.out.println("Error can't find employee id to look up employee for CompanyEvent id="
                                + companyEventEmail.getCompanyEvent().getId());
                    }

                } catch (RuntimeException e) {
                    SpcfUniqueId companyEventEmailId = companyEventEmail.getId();
                    e.printStackTrace();
                    System.out.println("Error updating email address for CompanyEventEmail id=" + companyEventEmailId + e);
                }
            }
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        return 1;
    }

    private void addEmailParameter(CompanyEventEmail companyEventEmail,
                                   EventEmailParamTypeCode pEventEmailParamTypeCode, String pValue) {
        CompanyEventEmailParam eventEmailParam = new CompanyEventEmailParam();
        eventEmailParam.setCompanyEventEmail(companyEventEmail);
        eventEmailParam.setCompany(companyEventEmail.getCompanyEvent().getCompany());
        eventEmailParam.setParamTypeCd(pEventEmailParamTypeCode);
        eventEmailParam.setValue(pValue);
        Application.save(eventEmailParam);
    }



    /*public static Logger getLogger(){
        return logger;
    }*/
}

package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.platform.integration.ius.common.types.User;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmail;
import com.intuit.sbd.payroll.psp.domain.CompanyEventDetail;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailStatus;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmailParam;
import com.intuit.sbd.payroll.psp.gateways.iam.ConsumerRealm;
import com.intuit.sbd.payroll.psp.gateways.iam.IDLMClientWrapper;
import com.intuit.sbd.payroll.psp.gateways.iam.IamUser;
import com.intuit.sbd.payroll.psp.gateways.iam.exception.MultiplePersonaConsumerException;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.paycycle.util.StringUtil;
import org.apache.commons.collections4.ListUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/*
 * This processor looks for CompanyEventEmails that need to be sent to IAM email addresses and looks up those addresses
 * and adds them to the parameters.
 */
@ScheduledJob(name = "IamEmailAddressProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class IamEmailAddressProcessor extends JSSBatchJob {

    public IamEmailAddressProcessor(String[] pArguments) {
        super(pArguments);
    }

    public IamEmailAddressProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() {
        getLogger().info("Starting IamEmailAddress batch job");
        StopWatch timer = StopWatch.startTimer();
        executeStep(InsertEmailAddress.class);
        getLogger().info("Completed IamEmailAddress batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
    }


    public static class InsertEmailAddress extends JSSBatchJobStep<IamEmailAddressProcessor> {
        private int interval;
        private int maxWait;
        static int totalRecordsProcessed;
        private PSPRequestContextManager pspRequestContextManager;
        private IDLMClientWrapper idlmClientWrapper;


        public InsertEmailAddress() {
            pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
            idlmClientWrapper = PayrollApplicationBeanFactory.getBean(IDLMClientWrapper.class);
        }

        @Override
        public void execute() {

            ExecutorService executorService = null;
            interval = SystemParameter.findIntValue(SystemParameter.Code.IAMEMAILADDRESS_CONTROLS_THREAD_POOL_INTERVAL, 60);
            maxWait = SystemParameter.findIntValue(SystemParameter.Code.IAMEMAILADDRESS_CONTROLS_THREAD_POOL_MAX_WAIT, 5 * 60);
            int batchSize = 5000;
            try {
                SystemParameter systemParameter = SystemParameter.findSystemParameter("IAMEMAILADDRESSPROCESSOR_JOB_BATCH_SIZE");
                if (systemParameter != null) {
                    batchSize = Integer.parseInt(systemParameter.getSystemParameterValue());
                }
            } catch (Throwable t) {
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            try {
                // Create threadPool with given parameters
                int cores = Runtime.getRuntime().availableProcessors();
                getLogger().info("No of cores: "+cores);
                int numberOfThreads=cores*2;
                if(numberOfThreads<=0){

                     numberOfThreads=10;
                }
              //  executorService = Executors.newFixedThreadPool(numberOfThreads);
                executorService = new ThreadPoolExecutor(cores, // core size
                        numberOfThreads, // max size
                        60*5, // idle timeout
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>());
                //  executorService = new ThreadPoolExecutor()
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
                                //getLogger().info("Processing "+eventEmailList.size()+" records");
                                totalRecordsProcessed=totalRecordsProcessed+ eventEmailList.size();
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
                            getLogger().info("working -- completed processing " + recordsProcessedCount + " records  in " + stopWatch.getElapsedTimeString());
                        }
                    }
                    Application.commitUnitOfWork();
                    getLogger().info("Completed processing "+totalRecordsProcessed+" records");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }  catch (ExecutionException e) {
                throw ThreadingUtils.launderThrowable(e.getCause());
            }
            finally {
                Application.rollbackUnitOfWork();
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
                        pspRequestContextManager.setRequestContext(companyEventEmail.getCompany(), RequestType.OLAP, "IamEmailAddressProcessor");

                        DomainEntitySet<CompanyEventDetail> details = companyEventEmail.getCompanyEvent()
                                .getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.EmployeeId);

                        //companyEventEmail = companyEvent.getCompanyEventEmailCollection().getFirst();
                        if (details != null) {
                            String employeeId = details.getFirst().getValue();
                            Employee employee = Application.findById(Employee.class,
                                    SpcfUniqueId.createInstance(employeeId));
                            if (employee.getConsumerRealmId() == null) {
                                getLogger().warn("Error can't look up email address for employee " + employeeId
                                        + " because consumer realm id is missing.");

                                companyEventEmail.setStatusCd(EventEmailStatus.Ignore);
                            } else {
                                String recipientEmailAddress = getReceipientEmailAddress(employee);
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
                                    getLogger().warn("Error invalid emailAddress=" + recipientEmailAddress
                                            + " for employeeId=" + employeeId + " with consumerRealmId="
                                            + employee.getConsumerRealmId()+ " with userAuthId=" + employee.getUserAuthId());
                                    companyEventEmail.setStatusCd(EventEmailStatus.Ignore);
                                }
                                companyEventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());
                                Application.save(companyEventEmail);
                            }
                        } else {
                            getLogger().error("Error can't find employee id to look up employee for CompanyEvent id="
                                    + companyEventEmail.getCompanyEvent().getId());
                        }

                    } catch (MultiplePersonaConsumerException e) {
                        getLogger().warn(String.format("Email Update Failed. Marking the record to failed. Event Email Id=%s, Reason=%s", companyEventEmail.getId(), e.getMessage()));
                        companyEventEmail.setStatusCd(EventEmailStatus.SendFailed);
                        Application.save(companyEventEmail);
                    } catch (RuntimeException e) {
                        SpcfUniqueId companyEventEmailId = companyEventEmail.getId();
                        getLogger().warn("Error updating email address for CompanyEventEmail id=" + companyEventEmailId + ", Root Cause=" + e);
                    } finally {
                        pspRequestContextManager.clearRequestContextCompany();
                    }
                }
                Application.commitUnitOfWork();
            } finally {
                Application.rollbackUnitOfWork();
            }
            return 1;
        }

        private String getEmailAddressUsingUserAuthId(String userAuthId) {
            getLogger().info(String.format("action=getEmailAddressUsingUserAuthId, userAuthId=%s",userAuthId));
            try {
                IamUser iamUser = idlmClientWrapper.getUserDetailsForAuthId(userAuthId);
                return iamUser != null ? iamUser.getEmailAddress() : null;
            } catch(Exception ex) {
                return null;
            }
        }
        private String getEmailAddressUsingConsumerRealmId(String consumerRealmId) {
            getLogger().info(String.format("action=getEmailAddressUsingConsumerRealmId, consumerRealmId=%s",consumerRealmId));
            ConsumerRealm consumerRealm = new ConsumerRealm();
            User userForConsumerRealmId = consumerRealm.getUserForConsumerRealmId(consumerRealmId);
            String recipientEmailAddress = userForConsumerRealmId != null && userForConsumerRealmId.getEmail() != null ? userForConsumerRealmId.getEmail().getAddress() : null;
            return recipientEmailAddress;
        }

        private String getReceipientEmailAddress(Employee employee) {
            if (FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDLM_ENABLED_FOR_FETCHING_USER_DETAILS, true)) {
                String authId = getAuthId(employee);
                return authId != null ? getEmailAddressUsingUserAuthId(authId) : null;
            } else {
                return getEmailAddressUsingConsumerRealmId(employee.getConsumerRealmId());
            }
        }

        //getAuthId is a throwaway code and hence it is present at 2 places, this will be removed once CFR cleanup is done.
        private String getAuthId(Employee employee) {
            if(employee.getUserAuthId() != null) {
                getLogger().info(String.format("action=getAuthId, AuthId found in employeeRecord, userAuthId=%s, employeeId=%s",employee.getUserAuthId(), employee.getId()));
                return employee.getUserAuthId();
            } else if(employee.getConsumerRealmId() != null) {
                ConsumerRealm consumerRealm = new ConsumerRealm();
                getLogger().info(String.format("AuthId is empty in employee record, fetching it from consumerRealmId=%s",employee.getConsumerRealmId()));
                return consumerRealm.getAuthIdFromConsumerRealmId(employee.getConsumerRealmId());
            }
            else {
                return null;
            }
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
    }
}

package com.intuit.sbd.payroll.psp.gateways.email.util;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileOutputStream;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.gateways.email.factory.CompanyEventEmailManager;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 30, 2008
 * Time: 6:11:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailUtils {
    public static final String sfNewLine;
    private static SpcfLogger logger = Application.getLogger(EmailUtils.class);

    static {
        sfNewLine = System.getProperty("line.separator");

        // Set SSL properties (for client cert)
/*        System.setProperty("javax.net.ssl.trustStore", ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-keystore"));
        System.setProperty("javax.net.ssl.trustStorePassword", ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-keystorepassword"));
        System.setProperty("javax.net.ssl.keyStore", ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, "iasns-keystorepassword"));*/
    }

    public static String getConfig(String pConfigParam) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.EmailGateway, pConfigParam);
    }

    public static boolean isInternlDistributionOnly() {
        // config string 'internaldistributiononly' must be present and equal to 'false' to allow external emailing
        String str = getConfig("internaldistributiononly");
        return (str == null) || !str.equalsIgnoreCase("false");
    }

    public static void formatFailedValidationError(String pReason, EventStatus pEventStatus) {
        pEventStatus.addError(new EventStatusError(pReason, EventStatusErrorType.FailedValidation));
    }

    public static void formatServiceReturnedError(String pReason, EventStatus pEventStatus) {
        pEventStatus.addError(new EventStatusError(pReason, EventStatusErrorType.ServiceReturned));
    }

    public static void formatServiceFaultError(String pReason, EventStatus pEventStatus) {
        pEventStatus.addError(new EventStatusError(pReason, EventStatusErrorType.ServiceFault));
    }

    public static String generateErrorFile(Collection<CompanyEventEmailManager> pManagers) {
        String errorFileName = null;
        StringBuffer err = new StringBuffer();

        for (CompanyEventEmailManager manager : pManagers) {
            err.append(manager.reportErrors());
        }

        if (err.length() > 0) {
            try {
                // write the errors to the file
                String fileName = "email-errors-" + PSPDate.getPSPTime().format("yyyyMMdd-HHmmss") + ".log";
                String dir = getConfig("emailerrorfilelocation");

                dir = dir.replaceAll("\\\\", "/");
                if (!dir.endsWith("/")) {
                    dir += "/";
                }

                // create the error dir if it doesn't exist.
                File d = new File(dir);
                if (!d.exists()) {
                    d.mkdirs();
                }

                // delete any existing error file matching our file name
                File file = new File(dir + fileName);
                if (file.exists()) {
                    file.delete();
                }
                Key key  = IDPSFileStreamManager.newKeyHandleLatest();
                FileOutputStream fos = new IDPSFileOutputStream(file,key);
                OutputStreamWriter osw = new OutputStreamWriter(fos);

                try {
                    osw.write(err.toString());
                } finally {
                    osw.flush();
                    osw.close();
                }

                errorFileName = file.getPath();

            } catch (Exception e) {
                throw new RuntimeException("Error creating the email error file.", e);
            }
        }

        return errorFileName;
    }

    public static void reportEmailErrors(Collection<CompanyEventEmailManager> pManagers) throws S3UploadException, S3ConnectionException {
        String errorFileName = generateErrorFile(pManagers);
        String tempFileName = null;
        if(errorFileName != null) {
             tempFileName = StreamUtil.createDecryptedFileForEmail(errorFileName);
        }
        // errorFileName will be null if there are no errors
        if (tempFileName != null) {
            try {
                // send the email to the alert list
                MailSender.sendEmail(getConfig("internalemailserver"),
                                     getConfig("emailfailurealertlist"),
                                     getConfig("emailfailurealertlist"),
                                     "PSP Client Email Failure Alert",
                                     "<<client email error log attached>>",
                                     tempFileName);
            } catch (Exception e) {
                throw new RuntimeException("Error sending the email error file to email alert list.", e);
            }finally{
                try {
                    FileUtils.forceDelete(new File(tempFileName));
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
            }
            File error_file = new File(errorFileName);
            if(error_file.exists()){
                String dir = getConfig("emailerrorfilelocation");
                String batchJobName = BatchJobType.EmailGateway.name();

                S3UploadUtils.archive(batchJobName,dir,errorFileName);
            }
        }
    }

    /**
     * Finds an entity by its unique id
     *
     * @param pClass    The domain class representing the entity
     * @param pUniqueId The unique entity id
     * @return The entity matching the unique id, null otherwise
     */
    public static <T extends DomainEntity> T getById(Class<T> pClass, String pUniqueId) {
        return Application.findById(pClass, SpcfUniqueId.createInstance(pUniqueId));
    }

    public static String getDetailString(EventStatus pEventStatus, EventDetailTypeCode pDetailType) {
        if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
            Company company = pEventStatus.getEvent().getCompany();

            Criterion<CompanyEvent> where = CompanyEvent.Company().equalTo(company)
                    .And(CompanyEvent.Id().equalTo(pEventStatus.getEvent().getCompanyEvent().getId()));

            DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class, new Query<CompanyEvent>()
                    .Where(where)
                    .EagerLoad(CompanyEvent.CompanyEventDetailSet().Filter().EventDetailTypeCd().equalTo(pDetailType)
                            .And(CompanyEvent.CompanyEventDetailSet().Filter().Company().equalTo(company))));

            CompanyEvent companyEvent = companyEvents.get(0);

            return companyEvent.getCompanyEventDetailValue(pDetailType);
        }
        return pEventStatus.getEvent().getCompanyEvent().getCompanyEventDetailValue(pDetailType);
    }

    public static FinancialTransaction getFinancialTransaction(EventStatus pEventStatus) {
        String ftId = getDetailString(pEventStatus, EventDetailTypeCode.FinancialTransactionId);
        return (ftId != null) ? (FinancialTransaction) getById(FinancialTransaction.class, ftId) : null;
    }

    public static BillingDetail getFeeBillingDetail(EventStatus pEventStatus) {
        String bdId = getDetailString(pEventStatus, EventDetailTypeCode.FeeBillingDetailId);
        return (bdId != null) ? (BillingDetail) getById(BillingDetail.class, bdId) : null;
    }

    public static List<EventStatus> findEventsByType(List<EventStatus> pEventList, EventTypeCode... pEventTypes) {
        List<EventStatus> likeList = new Vector<EventStatus>();
        List<EventTypeCode> typeList = Arrays.asList(pEventTypes);

        for (EventStatus event : pEventList) {
            if (typeList.contains(event.getEvent().getCompanyEvent().getEventTypeCd())) {
                likeList.add(event);
            }
        }

        return likeList;
    }

    public static List<EventStatus> findAndLinkLikeEvents(List<EventStatus> pEventList, EventStatus pMasterEvent) {
        List<EventStatus> likeList = findEventsByType(pEventList,
                                                      pMasterEvent.getEvent().getCompanyEvent().getEventTypeCd());

        // link all the like events to the master event
        for (EventStatus event : likeList) {
            pMasterEvent.linkStatusEvent(event); // link so won't process later
        }

        // add the master event to the list
        likeList.add(pMasterEvent);

        return likeList;
    }

    public static List<EventStatus> findAndLinkLikeEventsByEmail(List<EventStatus> pEventList, EventStatus pMasterEvent) {
        List<EventStatus> likeList = new Vector<EventStatus>();
        List<EventStatus> tempList = findEventsByType(pEventList,
                                                      pMasterEvent.getEvent().getCompanyEvent().getEventTypeCd());

        EventEmailParamTypeCode masterEmailParamType = EventEmailParamTypeCode.PayrollAdminEmail;
        DomainEntitySet<CompanyEventEmailParam> paramSet =
                pMasterEvent.getEvent().getEmailParamForEmailEvent(masterEmailParamType);

        if (paramSet.isEmpty()) {
            masterEmailParamType = EventEmailParamTypeCode.PrimaryPrincipalEmail;
            paramSet = pMasterEvent.getEvent().getEmailParamForEmailEvent(masterEmailParamType);
        }

        if (!paramSet.isEmpty()) {
            String masterEmail = paramSet.get(0).getValue();

            if (masterEmail == null) {
                masterEmail = "";
            }

            // link all the like events to the master event
            for (EventStatus event : tempList) {
                paramSet = event.getEvent().getEmailParamForEmailEvent(masterEmailParamType);

                if (!paramSet.isEmpty()) {
                    String eventEmail = paramSet.get(0).getValue();

                    if (eventEmail == null) {
                        eventEmail = "";
                    }

                    if (masterEmail.equals(eventEmail)) {
                        likeList.add(event); // this event is like the master event
                        pMasterEvent.linkStatusEvent(event); // link so won't process later
                    }
                }
            }
        }

        // add the master event to the list
        likeList.add(pMasterEvent);

        return likeList;
    }

    /**
     * This method performs two functions:<br>
     * 1) It finds and links like events to the master event.<br>
     * 2) It updates the state of the associated financial transaction in the pTxnStatusMap list
     * (to ReversalOK or ReversalReturn).<br>
     *
     * @param pEventList    The list of events to search for like events.
     * @param pMasterEvent  The master event (all like events are linked to this event.)
     * @param pTxnStatusMap The transaction status map (ReversalRequested, ReversalOK, ReversalReturn)
     * @return A List<EventStatus> of like events.
     * @see EmailUtils#createReversalStatusMap(EventStatus)
     */
    public static List<EventStatus> correlateEventsToStatusMap(List<EventStatus> pEventList,
                                                               EventStatus pMasterEvent,
                                                               Hashtable<String, EventTypeCode> pTxnStatusMap) {
        List<EventStatus> likeList = new Vector<EventStatus>();
        String txnId = getDetailString(pMasterEvent, EventDetailTypeCode.FinancialTransactionId);

        // the master event's txn id must be found in the status map to proceed
        if (pTxnStatusMap.containsKey(txnId)) {
            // add the master event to the like list
            likeList.add(pMasterEvent);

            // update the status map with this txn's reversal status
            pTxnStatusMap.put(txnId, pMasterEvent.getEvent().getCompanyEvent().getEventTypeCd());

            // pEventList does not contain pMasterEvent since it's already been popped
            List<EventStatus> findList = findEventsByType(pEventList,
                                                          EventTypeCode.ReversalOK,
                                                          EventTypeCode.ReversalReturn);

            // all txn id's in pTxnStatusMap are for the same settlement date, therefore not all
            // reversal events in findList for this payroll run will necessarily be in pTxnStatusMap
            // (i.e. different settlement dates)
            for (EventStatus event : findList) {
                txnId = getDetailString(event, EventDetailTypeCode.FinancialTransactionId);

                if (pTxnStatusMap.containsKey(txnId)) {
                    // update the status map with this txn's reversal status
                    pTxnStatusMap.put(txnId, event.getEvent().getCompanyEvent().getEventTypeCd());

                    // link the like events to the master event (so won't process later)
                    pMasterEvent.linkStatusEvent(event);

                    // add the event to the like list
                    likeList.add(event);
                }
            }
        }

        // finally, if this is a late ReversalReturn, we need to bring in the previous reversal statuses
        // (i.e. if a group of reversals has already been completed, and this reversal event is coming
        // in late, we need to retrieve the previous reversal ReversalOK and/or ReversalReturn statuses)
        // (if this is not a late ReversalReturn, this check will do no harm)
        for (Map.Entry<String, EventTypeCode> entry : pTxnStatusMap.entrySet()) {
            // if we haven't yet assigned a reversal status to this entry,
            // check for previous ReversalOK or ReversalReturned event
            if (entry.getValue() == EventTypeCode.ReversalRequested) {
                txnId = entry.getKey();

                FinancialTransaction ft = getById(FinancialTransaction.class, txnId);
                DomainEntitySet<CompanyEventDetail> detailList;

                // look for any ReversalReturn events
                if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
                    detailList = CompanyEvent.findCompanyEventDetailsEagerLoadCompanyEventAndCompanyEventEmailSet(ft.getCompany(),
                            EventTypeCode.ReversalReturn,
                            EventDetailTypeCode.FinancialTransactionId,
                            txnId);
                } else {
                    detailList = CompanyEvent.findCompanyEventDetails(ft.getCompany(),
                            EventTypeCode.ReversalReturn,
                            EventDetailTypeCode.FinancialTransactionId,
                            txnId);
                }

                // if no ReversalReturn events found, look for any ReversalOK events
                if (detailList.isEmpty()) {
                    if(FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_EAGER_LOAD_QUERIES)) {
                        detailList = CompanyEvent.findCompanyEventDetailsEagerLoadCompanyEventAndCompanyEventEmailSet(ft.getCompany(),
                                EventTypeCode.ReversalOK,
                                EventDetailTypeCode.FinancialTransactionId,
                                txnId);
                    } else {
                        detailList = CompanyEvent.findCompanyEventDetails(ft.getCompany(),
                                EventTypeCode.ReversalOK,
                                EventDetailTypeCode.FinancialTransactionId,
                                txnId);
                    }
                }

                // if we have an old reversal status, update the status map
                if (!detailList.isEmpty()) {
                    // get the most recent event
                    CompanyEventDetail detail = detailList.get(detailList.size() - 1);

                    DomainEntitySet<CompanyEventEmail> emailList =
                            detail.getCompanyEvent().getCompanyEventEmailCollection();

                    if (!emailList.isEmpty()) {
                        // update the status map with this txn's reversal status
                        entry.setValue(detail.getCompanyEvent().getEventTypeCd());

                        // make a new event status object to populate the email
                        EventStatus newEventStatus = pMasterEvent.spawnChild(emailList.get(0));

                        // add the event to the like list
                        likeList.add(newEventStatus);
                    }
                }

                for (CompanyEventDetail detail : detailList) {
                    // keep the cache clear of noise
                    Application.evict(detail);
                }

                // keep the cache clear of noise
                Application.evict(ft);
            }
        }

        return likeList;
    }

    /**
     * Creates a map of [key: financial transaction id][value: EventTypeCode], defaulting values to ReversalRequested.
     *
     * @param pMasterEvent The master event to use for finding related financial transactions. Related financial
     *                     transactions are those of type EmployeeDdReversalDebit belonging to the same payroll run and having the same
     *                     settlement date.
     * @return A hash table of type: Hashtable<String, EventTypeCode>
     */
    public static Hashtable<String, EventTypeCode> createReversalStatusMap(EventStatus pMasterEvent) {
        FinancialTransaction ft = EmailUtils.getFinancialTransaction(pMasterEvent);
        PayrollRun payrollRun = ft.getPayrollRun();
        Company company = payrollRun.getCompany();

        // get the list of all related EmployeeDdReversalDebit txns for this payroll run
        // match criteria are: same payroll run, same settlement date, txn type of EmployeeDdReversalDebit
        TransactionType txnType = PayrollServices.entityFinder.findById(TransactionType.class,
                                                                        TransactionTypeCode.EmployeeDdReversalDebit);

        Criterion<FinancialTransaction> where =
                FinancialTransaction.PayrollRun().equalTo(payrollRun)
                        .And(FinancialTransaction.TransactionType().equalTo(txnType))
                        .And(FinancialTransaction.SettlementDate().equalTo(ft.getSettlementDate().toLocal()));

        DomainEntitySet<FinancialTransaction> rvsTxnList =
                PayrollServices.entityFinder.find(FinancialTransaction.class, where);

        // this map will be used to flag the ReversalOK and ReversalReturn state of each txn
        // (each txn state is initialized to EventTypeCode.ReversalRequested)
        Hashtable<String, EventTypeCode> txnStatusMap = new Hashtable<String, EventTypeCode>();

        // build a master list of all client initiated reversals for this payroll run, defaulting their
        // event types to ReversalRequested (actual statuses will be updated in this list later)
        for (FinancialTransaction txn : rvsTxnList) {
            if (txn.isReversalClientRequested(company)) {
                txnStatusMap.put(txn.getId().toString(), EventTypeCode.ReversalRequested);
            }

            // keep the cache clear of noise
            Application.evict(txn);
        }

        // keep the cache clear of noise
        Application.evict(company);
        Application.evict(payrollRun);
        Application.evict(ft);

        return txnStatusMap;
    }
}

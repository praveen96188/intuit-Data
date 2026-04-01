package com.intuit.sbd.payroll.psp.batchjobs.AnnualBilling;

import com.intuit.ems.payroll.psp.gateways.tfs.ITFSGateway;
import com.intuit.ems.payroll.psp.gateways.tfs.TFSGatewayFactory;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/28/12
 * Time: 4:29 PM
 */
public class AnnualBilling {

    private int mInterval;
    private int mMaxWait;
    private int mMinPoolSize;
    private int mMaxPoolSize;
    private int mFormYear;
    private FormTypeCode mFormTypeCode;
    private AnnualBillingBatch mAnnualBillingBatch;

    private static int annualBillingItemCount = 0;
    private static ExecutorService threadPool;
    private static CompletionService<Boolean> completionService;
    private static SpcfLogger logger = Application.getLogger(AnnualBilling.class);

    private PSPRequestContextManager pspRequestContextManager;

    public AnnualBilling() {
        mFormTypeCode = FormTypeCode.W2;
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public ProcessResult main(String args[]) {
        try {
            parseArgs(args);

            getPreferences();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            mAnnualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(mFormTypeCode, mFormYear);
            if (mAnnualBillingBatch == null) {
                ITFSGateway tfsGateway = TFSGatewayFactory.createInstance();
                Map<String, Integer> formPageCountsByCompany =  tfsGateway.getW2PageCountsByCompany(mFormYear);

                if (formPageCountsByCompany != null && !formPageCountsByCompany.isEmpty()) {
                    createAnnualBillingBatch(mFormTypeCode);

                    createAnnualBillingItems(formPageCountsByCompany);
                }
            }
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            multithreadedAnnualFormBilling();

            DomainEntitySet<AnnualBillingItem> annualBillingItems = AnnualBillingItem.findPendingAnnualBillingItems(mAnnualBillingBatch,
                    AnnualBillingItemStatusCode.Pending, AnnualBillingItemStatusCode.Error);
            if (annualBillingItems.size() == 0) {
                mAnnualBillingBatch.setAnnualBillingBatchStatusCd(AnnualBillingBatchStatusCode.Completed);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().equals("usage")) {
                usage();
            } else {
                e.printStackTrace();
            }
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
                threadPool = null;
                completionService = null;
            }

            PayrollServices.rollbackUnitOfWork();
        }

        return null;
    }

    private void createAnnualBillingBatch(FormTypeCode pFormTypeCode) {
        mAnnualBillingBatch = new AnnualBillingBatch();

        mAnnualBillingBatch.setAnnualBillingBatchStatusCd(AnnualBillingBatchStatusCode.Pending);
        mAnnualBillingBatch.setFormTypeCd(pFormTypeCode);
        mAnnualBillingBatch.setFormYear(mFormYear);

        Application.save(mAnnualBillingBatch);
    }

    private void createAnnualBillingItems(Map<String, Integer> pFormPageCountsByCompany) {
        String errorMessage = null;
        AnnualBillingItemStatusCode statusCd = null;

        for(String psid : pFormPageCountsByCompany.keySet()) {
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (company == null) {
                logger.error(String.format("Company not found. SourceSystemCode:QBDT PSID:%s ", psid));
                continue;
            } else {
                TaxCompanyServiceInfo taxService = (TaxCompanyServiceInfo) company.getService(ServiceCode.Tax);
                if (taxService == null) {
                    errorMessage = "Tax service not found.";
                    statusCd = AnnualBillingItemStatusCode.Error;
                } else {
                    int lastQtrToFile = Integer.valueOf(String.format("%d%d", mAnnualBillingBatch.getFormYear(), 4));
                    if (taxService.isCancelTerm() &&
                            taxService.getFileAnnualReturns() &&
                            taxService.getLastQuarterToFile() == lastQtrToFile) {
                        statusCd = AnnualBillingItemStatusCode.Skipped;
                    } else {
                        statusCd = AnnualBillingItemStatusCode.Pending;
                    }
                }
            }

            Integer formCount = pFormPageCountsByCompany.get(psid);

            AnnualBillingItem annualBillingItem = new AnnualBillingItem();
            annualBillingItem.setAnnualBillingBatch(mAnnualBillingBatch);
            annualBillingItem.setCompany(company);
            annualBillingItem.setFormCount(formCount);
            annualBillingItem.setAnnualBillingItemStatusCd(statusCd);
            annualBillingItem.setErrorMessage(errorMessage);
            Application.save(annualBillingItem);
        }
    }

    private void multithreadedAnnualFormBilling() {
        try {
            if (threadPool == null) {
                threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                completionService = new ExecutorCompletionService<Boolean>(threadPool);
            }

            mAnnualBillingBatch = AnnualBillingBatch.findAnnualBillingBatch(mFormTypeCode, mFormYear);
            DomainEntitySet<AnnualBillingItem> pendingAnnualBillingItems = AnnualBillingItem.
                    findPendingAnnualBillingItems(mAnnualBillingBatch, AnnualBillingItemStatusCode.Pending);
            int pendingAnnualBillingItemCount = pendingAnnualBillingItems.size();

            for (AnnualBillingItem pendingAnnualBillingItem : pendingAnnualBillingItems) {
                final SpcfUniqueId pendingItemId = pendingAnnualBillingItem.getId();
                completionService.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        return createAnnualFormBilling(pendingItemId);
                    }
                });
                Application.evict(pendingAnnualBillingItem);
            }

            try {
                for (int t = 0; t < pendingAnnualBillingItemCount ; t++) {
                    Future<Boolean> isSuccessful = completionService.take();
                    annualBillingItemCount++;
                    if (annualBillingItemCount % 100 == 0) {
                        logger.info("working -- completed processing " + annualBillingItemCount + " annual billing items");
                    }
                }
            } catch (InterruptedException e) {
                logger.warn(e);
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    private Boolean createAnnualFormBilling(SpcfUniqueId pAnnualBillingItemId) {
        try {
            PayrollServices.setCurrentPrincipal(SystemPrincipal.AnnualBillingBatchJob);
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            AnnualBillingItem annualBillingItem = Application.findById(AnnualBillingItem.class, pAnnualBillingItemId);
            
            // 2nd check to detect cancelled companies
            AnnualBillingItemStatusCode statusCd = annualBillingItem.getAnnualBillingItemStatusCd();
            Company company = annualBillingItem.getCompany();

            pspRequestContextManager.setRequestContext(company, RequestType.OLAP, BatchJobType.AnnualBillingProcessor.toString());

            if(Company.isDGDeleteFeatureEnabled() && company.getIsDgDisassociated()){
                throw new RuntimeException(String.format("Cannot process as Company associated with pAnnualBillingItemId=%s is DG Dissociated true", pAnnualBillingItemId));
            }

            TaxCompanyServiceInfo taxService = (TaxCompanyServiceInfo) company.getService(ServiceCode.Tax);
            if (taxService == null) {
                statusCd = AnnualBillingItemStatusCode.Error;
                annualBillingItem.setErrorMessage("Tax service not found.");
            } else {
                int lastQtrToFile = Integer.valueOf(String.format("%d%d", mAnnualBillingBatch.getFormYear(), 4));
                if (taxService.isCancelTerm()
                        && taxService.getFileAnnualReturns()
                        && taxService.getLastQuarterToFile() == lastQtrToFile) {
                    statusCd = AnnualBillingItemStatusCode.Skipped;
                }
            }

            if (statusCd == AnnualBillingItemStatusCode.Pending) {
                // Biiling the Tax Service activated Company
                if (annualBillingItem.getFormCount() > 0) {
                    CompanyOffering companyOffering = company.getOffering(ServiceCode.DirectDeposit);

                    SpcfDecimal formCount = SpcfDecimal.createInstance(annualBillingItem.getFormCount());
                    Date today = CalendarUtils.convertToDate(PSPDate.getPSPTime());

                    ERFeeAddDTO formFee = null;
                    OfferingServiceChargePrice w2FeeOSCP = findOfferingServiceChargePrice(companyOffering.getOffering(), OfferingServiceChargeType.W2Fee);
                    if (w2FeeOSCP != null && w2FeeOSCP.getUnitPrice().isGreaterThan(SpcfMoney.ZERO)) {
                        SpcfMoney unitPrice = new SpcfMoney(w2FeeOSCP.getUnitPrice().multiply(formCount));

                        formFee = new ERFeeAddDTO();
                        formFee.setSourceSystemCd(company.getSourceSystemCd());
                        formFee.setSourceCompanyId(company.getSourceCompanyId());
                        formFee.setFeeTypeCode(OfferingServiceChargeType.W2Fee);
                        formFee.setAmount(unitPrice);
                        formFee.setSettlementTypeCode(SettlementTypeDTO.ACH);
                        formFee.setTxDate(today);
                    }

                    ERFeeAddDTO formBaseFee = null;
                    OfferingServiceChargePrice w2BaseFeeOSCP = findOfferingServiceChargePrice(companyOffering.getOffering(), OfferingServiceChargeType.W2BaseFee);
                    if (w2BaseFeeOSCP != null && w2BaseFeeOSCP.getBasePrice().isGreaterThan(SpcfMoney.ZERO)) {
                        formBaseFee = new ERFeeAddDTO();
                        formBaseFee.setSourceSystemCd(company.getSourceSystemCd());
                        formBaseFee.setSourceCompanyId(company.getSourceCompanyId());
                        formBaseFee.setFeeTypeCode(OfferingServiceChargeType.W2BaseFee);
                        formBaseFee.setAmount(w2BaseFeeOSCP.getBasePrice());
                        formBaseFee.setSettlementTypeCode(SettlementTypeDTO.ACH);
                        formBaseFee.setTxDate(today);
                    }

                    if (formFee != null || formBaseFee != null) {
                        ProcessResult processResult = PayrollServices.financialTransactionManager.createManualFeeTransaction(formFee, formBaseFee);
                        if (!processResult.isSuccess()) {
                            throw new Exception(processResult.toString());
                        }
                    }
                }
                
                statusCd = AnnualBillingItemStatusCode.Completed;
            }

            annualBillingItem.setAnnualBillingItemStatusCd(statusCd);
            Application.save(annualBillingItem);

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();

            PayrollServices.beginUnitOfWork();
            AnnualBillingItem annualBillingItem = Application.findById(AnnualBillingItem.class, pAnnualBillingItemId);
            annualBillingItem.setAnnualBillingItemStatusCd(AnnualBillingItemStatusCode.Error);
            annualBillingItem.setErrorMessage(e.getMessage());
            PayrollServices.commitUnitOfWork();

            logger.warn(e.getMessage(), e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }

        return true;
    }

    public static OfferingServiceChargePrice findOfferingServiceChargePrice(Offering pOffering, OfferingServiceChargeType pOfferingServiceChargeType) {
        Expression<OfferingServiceChargePrice> query =
                new Query<OfferingServiceChargePrice>()
                        .Where(OfferingServiceChargePrice.OfferingServiceCharge().OfferingServiceChargeGroup().Offering().equalTo(pOffering)
                                .And(OfferingServiceChargePrice.OfferingServiceCharge().OfferingServiceChargeGroup().AppliesTo().equalTo(pOfferingServiceChargeType)));
        return Application.find(OfferingServiceChargePrice.class, query).getFirst();
    }

    private void parseArgs(String args[]) throws Exception {
        if (args.length == 1 && validateValue(args[0], false, "^(20)\\d{2}$")) {
            mFormYear = Integer.parseInt(args[0]);
        } else {
            throw new Exception("usage");
        }
    }

    public Boolean validateValue(String pValue, boolean pNullable, String pPattern) {
        Pattern pattern = Pattern.compile(pPattern);

        if ((!pNullable) && (pValue == null)) {
            return false;
        }

        if (pValue == null) {
            return true;
        }

        Matcher matcher = pattern.matcher(pValue.trim());
        return matcher.matches();
    }

    private void usage() {
        System.out.println("Usage: ./BatchJobManager run AnnualBillingProcessor <4 digit W2 Year>");
    }

    private void getPreferences() {
        mInterval = SystemParameter.findIntValue(SystemParameter.Code.ANNUAL_BILLING_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.ANNUAL_BILLING_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ANNUAL_BILLING_MIN_THREAD_POOL_SIZE, 8);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.ANNUAL_BILLING_MAX_THREAD_POOL_SIZE, 8);
    }

}

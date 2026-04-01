package com.intuit.sbd.payroll.psp.batchjobs.printedchecks;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.CheckDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.LineItemDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayeeDTO;
import com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos.PayerDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.CheckUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.PdfPrinter;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.AgencyCheckBatch;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatchStatus;
import com.intuit.sbd.payroll.psp.domain.CheckPrintSignature;
import com.intuit.sbd.payroll.psp.domain.Law;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.domain.PaymentBatchAssoc;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplate;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplatePrintedCheckInfo;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemPrintedCheckInfo;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 27, 2011
 * Time: 4:54:27 PM
 */
public class PrintManualChecks {

    private static SpcfLogger logger = Application.getLogger(PrintManualChecks.class);

    private PayerDTO mPayerDTO;

    private Map<String, byte[]> checksToPrint = Collections.synchronizedMap(new TreeMap<String, byte[]>());

    public void processAgencyCheckBatches(SourceSystemCode pSourceSystemCode) {
        logger.info("processAgencyCheckBatches started");
        StopWatch timer = StopWatch.startTimer();

        int interval = SystemParameter.findIntValue(SystemParameter.Code.CD_PRINT_PAYCHECK_THREAD_POOL_INTERVAL, 60);
        int axWait = SystemParameter.findIntValue(SystemParameter.Code.CD_PRINT_PAYCHECK_THREAD_POOL_MAX_WAIT, 5 * 60);
        int inPoolSize = SystemParameter.findIntValue(SystemParameter.Code.CD_PRINT_PAYCHECK_MIN_THREAD_POOL_SIZE, 10);
        int axPoolSize = SystemParameter.findIntValue(SystemParameter.Code.CD_PRINT_PAYCHECK_MAX_THREAD_POOL_SIZE, 40);

        // source system check info
        SourceSystemPrintedCheckInfo sourceSystemPrintedCheckInfo;
        DomainEntitySet<SourceSystemPrintedCheckInfo> sourceSystemPrintedCheckInfos = Application.find(SourceSystemPrintedCheckInfo.class,
                                                                                     SourceSystemPrintedCheckInfo.SourceSystemCode().equalTo(pSourceSystemCode));
        if(sourceSystemPrintedCheckInfos.size() != 1) {
            throw new RuntimeException("Error finding check info for source system: " + pSourceSystemCode);
        }
        sourceSystemPrintedCheckInfo = sourceSystemPrintedCheckInfos.get(0);

        // signature
        byte[] signature;
        DomainEntitySet<CheckPrintSignature> signatures =
                Application.find(CheckPrintSignature.class,
                                 CheckPrintSignature.SourceSystemPrintedCheckInfo().SourceSystemCode().equalTo(pSourceSystemCode));
        if(signatures.size() != 1) {
            throw new RuntimeException("Error finding check signature for source system: " + pSourceSystemCode);
        }
        signature = signatures.get(0).getSignatureImage();

        mPayerDTO = new PayerDTO();
        mPayerDTO.setAddressLine1(sourceSystemPrintedCheckInfo.getAddress().getAddressLine1());
        mPayerDTO.setAddressLine2(sourceSystemPrintedCheckInfo.getAddress().getAddressLine2());
        mPayerDTO.setCity(sourceSystemPrintedCheckInfo.getAddress().getCity());
        mPayerDTO.setState(sourceSystemPrintedCheckInfo.getAddress().getState());
        mPayerDTO.setZip(sourceSystemPrintedCheckInfo.getAddress().getFullZipCode());
        mPayerDTO.setNameLine1(sourceSystemPrintedCheckInfo.getNameLine1());
        mPayerDTO.setNameLine2(sourceSystemPrintedCheckInfo.getNameLine2());
        mPayerDTO.setBankLogo(sourceSystemPrintedCheckInfo.getBankLogoImage());
        mPayerDTO.setLogo(sourceSystemPrintedCheckInfo.getSourceSystemLogoImage());
        mPayerDTO.setSignature(signature);

        int totalChecksProcessed = 0;
        int numberOfProcessedBatches = 0;
        ExecutorService threadPool = null;
        try {
            // create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(inPoolSize, axPoolSize, interval, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            DomainEntitySet<AgencyCheckBatch> agencyCheckBatches =
                    Application.find(AgencyCheckBatch.class, new Query<AgencyCheckBatch>()
                            .Where(AgencyCheckBatch.CheckPrintBatchStatusCode().equalTo(CheckPrintBatchStatus.Pending))
                            .OrderBy(AgencyCheckBatch.PaymentTemplate(), AgencyCheckBatch.CreatedDate()));

            // process each batch in a separate thread
            for (AgencyCheckBatch agencyCheckBatch : agencyCheckBatches) {
                numberOfProcessedBatches++;
                final AgencyCheckBatch finalAgencyCheckBatch = agencyCheckBatch;
                completionService.submit(new Callable<Integer>() {
                    public Integer call() {
                        return processCheckBatch(finalAgencyCheckBatch);
                    }
                });
            }

            // Get the results of each thread execution
            try {
                for (int t = 0; t < numberOfProcessedBatches; t++) {
                    Future<Integer> f = completionService.take();
                    totalChecksProcessed += f.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw ThreadingUtils.launderThrowable(e.getCause());
            }
        } finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, interval, axWait);
            }
        }

        String paycheckPrinterName = SystemParameter.findValue(SystemParameter.Code.PRINTED_CHECKS_PRINTER_NAME);

        for (String key : checksToPrint.keySet()) {
            String[] keyIds = key.split("\\$\\$");
            logger.info("keyIds" + keyIds[0].toString());
            try {
                logger.info("PrintManualChecks -Feature Flag ENABLE_SFTP_PRINTING value = " + isSftpPrintingEnabled());
                if (paycheckPrinterName.matches("^[a-zA-Z]:.*")) {
                    //this is only for testing output
                    new FileOutputStream(paycheckPrinterName.replaceAll("\\.pdf", Integer.toString((int) (Math.random() * 10000)) + ".pdf")).write(checksToPrint.get(key));

                } else if (isSftpPrintingEnabled()) {

                    PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(keyIds[0]);
                    String currentDate = PSPDate.getPSPTime().format("yyyyMMddHHmmss").concat(String.valueOf(PSPDate.getPSPTime().getMillisecond()));
                    String fileName = paymentTemplate.getCategoryInShortForm() + "_"
                            + paymentTemplate.getPaymentTemplateCd() + "_" + currentDate + ".Checks.pdf";
                    logger.info("File name" +fileName);
                    BatchUtils.uploadFileViaSftp(SystemParameter.findValue(SystemParameter.Code.PRINTED_CHECKS_SFTP_HOST),
                            SystemParameter.findValue(SystemParameter.Code.PRINTED_CHECKS_SFTP_USER),
                            SystemParameter.findValue(SystemParameter.Code.PRINTED_CHECKS_SFTP_PASSWORD),
                            SystemParameter.findValue(SystemParameter.Code.PRINTED_CHECKS_SFTP_PATH),
                            fileName,
                            new ByteArrayInputStream(checksToPrint.get(key)),
                            SystemParameter.findIntValue(SystemParameter.Code.PRINTED_CHECKS_SFTP_RETRY_COUNT),
                            Transporter.TIME_OUT,
                            logger);
                } else {
                    logger.info("PrintManualChecks -Feature Flag ENABLE_SFTP_PRINTING is not enabled");
                    PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(keyIds[0]);
                    new PdfPrinter(paycheckPrinterName, paymentTemplate.getCategoryInShortForm() + "_" + paymentTemplate.getPaymentTemplateCd() + ".Checks").printPdf(checksToPrint.get(key), false);
                }

            } catch (Throwable t) {
                AgencyCheckBatch printBatch = Application.findById(AgencyCheckBatch.class, SpcfUniqueId.createInstance(keyIds[2]));
                Application.refresh(printBatch);
                printBatch.setCheckPrintBatchStatusCode(CheckPrintBatchStatus.Error);
                printBatch.setCheckPrintBatchMessage("Error while printing checks:" + t.getMessage());

                logger.error("Error while printing checks:" + t.getMessage(), t);
                Application.save(printBatch);
            }
        }

        logger.info("PrintManualChecks finished. Total batches processed: " + numberOfProcessedBatches + " checks printed: " + totalChecksProcessed + " Elapsed time: " + timer.getElapsedTimeString());
    }

    private Integer processCheckBatch(AgencyCheckBatch pBatchToPrint) {
        try {
            logger.info("Started processing batch for payment template: " + pBatchToPrint.getPaymentTemplate().getPaymentTemplateCd());
            StopWatch timer = StopWatch.startTimer();

            PayrollServices.beginUnitOfWork();

            int checksPrinted = printBatch(pBatchToPrint);

            logger.info("Finished processing batch for payment template: " + pBatchToPrint.getPaymentTemplate().getPaymentTemplateCd() + " checks printed: " + checksPrinted + " in " + timer.getElapsedTimeString());
            PayrollServices.commitUnitOfWork();

            return checksPrinted;
        } catch (Throwable t) {
            logger.error("Error processing batch: " + pBatchToPrint.getId().toString() + " :" + t.getMessage(), t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return 0;
    }

    private int printBatch(AgencyCheckBatch printBatch) {
        int printedChecks = 0;

        printBatch = Application.findById(AgencyCheckBatch.class, printBatch.getId());
        printBatch.setCheckPrintBatchStatusCode(CheckPrintBatchStatus.SentToPrinter);
        printBatch.setCheckPrintBatchMessage("");
        printBatch.setSentToPrinter(PSPDate.getPSPTime());

        String step = "";
        try {
            step = "create dtos";
            List<CheckDTO> checkDTOs = createCheckDTOs(printBatch);

            if (checkDTOs.size() > 0) {
                printedChecks = checkDTOs.size();
                step = "generating checks";
                byte[] checks = generateManualChecks(checkDTOs);

                String key = printBatch.getPaymentTemplate().getPaymentTemplateCd() + "$$" + printBatch.getCreatedDate().toString() + "$$" + printBatch.getId().toString();
                checksToPrint.put(key, checks);
            }

        } catch (Throwable t) {
            printBatch.setCheckPrintBatchStatusCode(CheckPrintBatchStatus.Error);
            printBatch.setCheckPrintBatchMessage("Error while " + step + ":" + t.getMessage());

            logger.error("Error while " + step + ":" + t.getMessage(), t);
        }

        Application.save(printBatch);

        return printedChecks;
    }

    private List<CheckDTO> createCheckDTOs(AgencyCheckBatch pBatchToPrint) {
        List<PaymentBatchAssoc> paymentBatchAssociations = PaymentBatchAssoc.findPaymentBatchAssocsByBatch(pBatchToPrint, true);

        PaymentTemplatePrintedCheckInfo paymentTemplatePrintedCheckInfo = PaymentTemplatePrintedCheckInfo.findPaymentTemplatePrintedCheckInfo(pBatchToPrint.getPaymentTemplate());
        if(paymentTemplatePrintedCheckInfo == null) {
            throw new RuntimeException("Error finding payment template check info for: " + pBatchToPrint.getPaymentTemplate().getPaymentTemplateCd());
        }

        PayeeDTO payeeDTO = new PayeeDTO();
        payeeDTO.setAddressLine1(paymentTemplatePrintedCheckInfo.getAddress().getAddressLine1());
        payeeDTO.setAddressLine2(paymentTemplatePrintedCheckInfo.getAddress().getAddressLine2());
        payeeDTO.setCity(paymentTemplatePrintedCheckInfo.getAddress().getCity());
        payeeDTO.setState(paymentTemplatePrintedCheckInfo.getAddress().getState());
        payeeDTO.setZip(paymentTemplatePrintedCheckInfo.getAddress().getFullZipCode());
        payeeDTO.setNameLine1(paymentTemplatePrintedCheckInfo.getNameLine1());
        payeeDTO.setNameLine2(paymentTemplatePrintedCheckInfo.getNameLine2());

        List<CheckDTO> checkDTOs = new ArrayList<CheckDTO>();
        if (pBatchToPrint.getSuperCheck()) {
            SpcfDecimal amount = SpcfMoney.ZERO;
            Map<Law, SpcfMoney> liabilitiesMap = new HashMap<Law, SpcfMoney>();
            for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                amount = amount.add(payment.getMoneyMovementTransactionAmount());
                mergeAdd(liabilitiesMap, payment.getLiabilityBalances());
            }

            MoneyMovementTransaction representativePayment = paymentBatchAssociations.get(0).getMoneyMovementTransaction();
            CheckDTO checkDTO = new CheckDTO();
            checkDTO.setSuperCheck(true);
            checkDTO.setCheckAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(amount)));
            checkDTO.setCheckDate(SpcfUtils.convertSpcfCalendarToDate(representativePayment.getSettlementDate()));
            checkDTO.setCheckNumber(representativePayment.getReferenceNumber());
            checkDTO.setPrintDate(SpcfUtils.convertSpcfCalendarToDate(pBatchToPrint.getSentToPrinter()));

            checkDTO.setPayerDTO(mPayerDTO);
            checkDTO.setPayeeDTO(payeeDTO);

            int liabilityQuarter = CalendarUtils.getQuarterAsInt(representativePayment.getPaymentPeriodEnd());
            int liabilityYear = representativePayment.getPaymentPeriodEnd().getYear();
            for (Law law : liabilitiesMap.keySet()) {
                LineItemDTO lineItemDTO = new LineItemDTO();
                lineItemDTO.setAmount(SpcfUtils.convertToBigDecimal(liabilitiesMap.get(law)));
                lineItemDTO.setLiabilityQuarter(liabilityQuarter);
                lineItemDTO.setLiabilityYear(liabilityYear);
                lineItemDTO.setType((law.getLawTypeCd() != null) ? law.getLawTypeCd() : law.getLawAbbrev());
                checkDTO.getLineItems().add(lineItemDTO);
            }

            // find the intuit bank account to debit
            BankAccount bankAccount = representativePayment.findIntuitDebitAccount();
            if(bankAccount != null) {
                checkDTO.setBankAccountNumber(bankAccount.getAccountNumber());
                checkDTO.setRoutingNumber(bankAccount.getRoutingNumber());
                checkDTOs.add(checkDTO);
            } else {
                throw new RuntimeException("Could not find Intuit debit bank account for check payment: " + representativePayment.getId().toString());
            }
        } else {
            for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                MoneyMovementTransaction payment = paymentBatchAssociation.getMoneyMovementTransaction();
                CheckDTO checkDTO = new CheckDTO();
                checkDTO.setCheckAmount(SpcfUtils.convertToBigDecimal(payment.getMoneyMovementTransactionAmount()));
                checkDTO.setCheckDate(SpcfUtils.convertSpcfCalendarToDate(payment.getSettlementDate()));
                checkDTO.setCheckNumber(payment.getReferenceNumber());
                checkDTO.setCompanyLegalName(payment.getCompany().getLegalName());
                checkDTO.setPrintDate(SpcfUtils.convertSpcfCalendarToDate(pBatchToPrint.getSentToPrinter()));
                checkDTO.setSourceCompanyNumber(payment.getCompany().getSourceCompanyId());
                checkDTO.setFEIN(payment.getCompany().getFedTaxId());
                checkDTO.setTaxId(payment.getAgencyTaxpayerId());

                checkDTO.setPayerDTO(mPayerDTO);
                checkDTO.setPayeeDTO(payeeDTO);

                int liabilityQuarter = CalendarUtils.getQuarterAsInt(payment.getPaymentPeriodEnd());
                int liabilityYear = payment.getPaymentPeriodEnd().getYear();
                Map<Law, SpcfMoney> liabilitiesMap = payment.getLiabilityBalances();
                for (Law law : liabilitiesMap.keySet()) {
                    LineItemDTO lineItemDTO = new LineItemDTO();
                    lineItemDTO.setAmount(SpcfUtils.convertToBigDecimal(liabilitiesMap.get(law)));
                    lineItemDTO.setLiabilityQuarter(liabilityQuarter);
                    lineItemDTO.setLiabilityYear(liabilityYear);
                    lineItemDTO.setType((law.getLawTypeCd() != null) ? law.getLawTypeCd() : law.getLawAbbrev());
                    checkDTO.getLineItems().add(lineItemDTO);
                }

                // find the intuit bank account to debit
                BankAccount bankAccount = payment.findIntuitDebitAccount();
                if(bankAccount != null) {
                    checkDTO.setBankAccountNumber(bankAccount.getAccountNumber());
                    checkDTO.setRoutingNumber(bankAccount.getRoutingNumber());
                    checkDTOs.add(checkDTO);
                } else {
                    throw new RuntimeException("Could not find Intuit debit bank account for check payment: " + payment.getId().toString());
                }
            }
        }

        return checkDTOs;
    }

    public void mergeAdd(Map<Law, SpcfMoney> map1, Map<Law, SpcfMoney> map2) {
        for (Map.Entry<Law, SpcfMoney> entry : map2.entrySet()){
            if (map1.containsKey(entry.getKey())) {
                map1.put(entry.getKey(), new SpcfMoney(map1.get(entry.getKey()).add(entry.getValue())));
            } else {
                map1.put(entry.getKey(), entry.getValue());
            }
        }

    }

    private static boolean isSftpPrintingEnabled(){
        return FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_SFTP_PRINTING, false);
    }

    public static byte[] generateManualChecks(List<CheckDTO> pCheckDTOs) throws Exception {

        List<byte[]> paycheckPdfs = new ArrayList<byte[]>(pCheckDTOs.size());

        float marginLeft = Float.parseFloat(SystemParameter.findStringValue(SystemParameter.Code.SFTP_A4_MARGIN_LEFT, "5"));
        float marginRight = Float.parseFloat(SystemParameter.findStringValue(SystemParameter.Code.SFTP_A4_MARGIN_RIGHT, "5"));
        float marginTop = Float.parseFloat(SystemParameter.findStringValue(SystemParameter.Code.SFTP_A4_MARGIN_TOP, "18"));
        float marginBottom = Float.parseFloat(SystemParameter.findStringValue(SystemParameter.Code.SFTP_A4_MARGIN_BOTTOM, "0"));


        for (CheckDTO checkDTO : pCheckDTOs) {
            CheckTemplate checkTemplate;
            if (isSftpPrintingEnabled()) {
                checkTemplate = new CheckTemplate(new Document(PageSize.A4, marginLeft, marginRight, marginTop, marginBottom), CheckUtils.getMICRFont());
            } else {
                checkTemplate = new CheckTemplate(new Document(PageSize.A4, 25, 25, 38, 0), CheckUtils.getMICRFont());
            }
            paycheckPdfs.add(checkTemplate.generateManualCheck(checkDTO));
        }

        return CheckUtils.combinePdfs(paycheckPdfs);
    }
}

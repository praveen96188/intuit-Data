package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.LimitValueType;
import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Feb 8, 2011
 * Time: 5:05:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class AchReturnAccountingFile {
    private static final SpcfLogger logger = Application.getLogger(AchReturnAccountingFile.class);
    private static final SpcfMoney ZERO = new SpcfMoney("0.00");
    private PSPRequestContextManager pspRequestContextManager;

    //
    // File header record
    //
    private static final String mRecordHeader = "\"Hold Status\",\"Hold Subtype\"," +
                                                "\"User ID\",\"EIN\",\"Product\",\"Company Legal Name\"," +
                                                "\"Company DBA Name\",\"Address\",\"City\",\"State\",\"Zip Code\"," +
                                                "\"Telephone\",\"FAX\",\"Payroll Admin\",\"E-mail Address\"," +
                                                "\"Company Limits\",\"EE Limits\",\"Reject Code\",\"Redebit Failed/ returned twice\",\"Trace #\"," +
                                                "\"Entry Date\",\"Check Date\",\"Strikes\",\"Amount\",\"Payroll Tax\"," +
                                                "\"Direct Deposit\",\"EE Fees\",\"Monthly Fees\",\"State Fees\"," +
                                                "\"DD Fees\",\"Backdate Fees\",\"DD Reversal Fees\",\"Adjustment Fees\"," +
                                                "\"Amendment Fees\",\"SSN Amend Fees\",\"W2-C Fees\",\"Extra Copy Fees\"," +
                                                "\"Entity Change Fees\",\"Special Fees\",\"Sales Tax\",\"Enroll Fee\"," +
                                                "\"W2 Fee\",\"Additional Fee 1 Desc\",\"Additional Fee 1\"," +
                                                "\"Additional Fee 2 Desc\",\"Additional Fee 2\",\"Additional Fee 3 Desc\"," +
                                                "\"Addtional Fee 3\",\"addition Fee 4 ,Desc\",\"Additional Fee 4\"," +
                                                "\"Additional Fee 5 Desc\",\"Additional Fee 5\",\"Additional Info 1\"," +
                                                "\"Additional Info 2\",\"Additional Info 3\",\"Additional Info 4\","+
                                                "\"# of payrolls\",\"Sign Up Date\"\r\n";
    
    //
    // Each record contains 52 fields
    //
    private static final String mRecordTemplate = "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\","+
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"," +
                                                  "\"%s\",\"%s\",\"%s\"\r\n";

    private SpcfUniqueId mReturnBatchId;
    private SpcfCalendar mReturnDate;
    private File mFile = null;

    public AchReturnAccountingFile(SpcfUniqueId pReturnBatchId) {
        mReturnBatchId = pReturnBatchId;
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);

    }

    public SpcfUniqueId getReturnBatchId() {
        return mReturnBatchId;
    }

    public SpcfCalendar getReturnDate() {
        return mReturnDate;
    }

    public File getFile() {
        return mFile;
    }

    public static ArrayList<File> createFile(SpcfUniqueId pReturnBatchId) {
        AchReturnAccountingFile file = new AchReturnAccountingFile(pReturnBatchId);
        ArrayList<File> files = null;
        if (file.createFiles()) {
            /*  there's at least one file   */
            files = new ArrayList<File>();
            if (file.getFile() != null && file.getFile().exists()) {
                files.add(file.getFile());
            }
        }
        return files;
    }

    /**
     *
     * @return
     *
     * 'true' if either of returns file or failures file was created with non-zero records, 'false' otherwise.
     */
    public boolean createFiles() {
        int recordCount = 0;

        try {
            TransactionReturnBatch batch = Application.findById(TransactionReturnBatch.class, mReturnBatchId);

            mReturnDate = batch.getReturnDate();

            Writer writer = getWriter();

            try {
                writer.write(mRecordHeader);

                DomainEntitySet<TransactionReturn> txnReturnSet =
                        Application.find(TransactionReturn.class, TransactionReturn.ReturnBatch().equalTo(batch));

                for (TransactionReturn txnReturn : txnReturnSet) {
                    try {
                        pspRequestContextManager.setRequestContextCompany(txnReturn.getCompany());
                        if (txnReturn.isRejectReturn()) {
                            if (writeRecord(txnReturn, writer)) {
                                ++recordCount;
                            }
                        }
                    } finally {
                        pspRequestContextManager.clearRequestContextCompany();
                    }
                }
            } finally {
                writer.flush();
                writer.close();
            }

            if (recordCount > 0) {
                logger.info(String.format("Created ACH returns accounting file %s", mFile.getPath()));
            }
        } catch (Throwable t) {
            logger.error(String.format("Error creating ACH returns or ACH re-debit returns accounting file for transaction returns batch id %s ",
                                       mReturnBatchId.toString()), t);
        } finally {
            //
            // Clean up the files if it contains no return data
            //
            if ((recordCount == 0) && (mFile != null) && mFile.exists()) {
                if (!mFile.delete()) {
                    logger.warn(String.format("Failed to delete defunct ACH returns accounting file %s", mFile.getPath()));
                }
            }
        }

        return recordCount > 0;
    }

    /**
     *
     * @return
     *          The required writer.
     * @throws IOException
     */
    private Writer getWriter() throws IOException  {
        String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");
        String fileName = String.format("psp-ach-returns-%s.csv", mReturnDate.format("yyyyMMdd"));

        int fileCount = 0;
        File writerFile = new File(archiveDir, fileName);
        //
        // Ensure file name is unique for this return batch
        //
        while (writerFile.exists()) {
            fileName = String.format("psp-ach-returns-%s-%d.csv", mReturnDate.format("yyyyMMdd"), ++fileCount);
            writerFile = new File(archiveDir, fileName);
        }
        mFile = writerFile;

        return new BufferedWriter(new FileWriter(writerFile));
    }

    private boolean writeRecord(TransactionReturn pTxnReturn, Writer pWriter) throws IOException {
        MoneyMovementTransaction mmt = pTxnReturn.getMoneyMovementTransaction();
        TransactionState returned = Application.findById(TransactionState.class, TransactionStateCode.Returned);
        Criterion<FinancialTransaction> where = FinancialTransaction.CurrentTransactionState().equalTo(returned);
        DomainEntitySet<FinancialTransaction> returnedTxnSet = mmt.getFinancialTransactionCollection().find(where);
        Company company = mmt.getCompany();


        boolean isRedebitTxnReturn = false;
        //
        // Calculate amounts
        //
        SpcfMoney feeAmount = ZERO;
        SpcfMoney salesTaxAmount = ZERO;
        SpcfMoney taxAmount = ZERO;
        SpcfMoney ddAmount = ZERO; 
        
        ServiceCode serviceTypeCode = null;

        for (FinancialTransaction ft : returnedTxnSet) {
            if(serviceTypeCode == null && ft.getPayrollRun() != null && ft.getPayrollRun().getPayrollRunType() == PayrollType.BillPayment) {
                serviceTypeCode = ServiceCode.BillPayment;
            }
            switch (ft.getTransactionType().getTransactionTypeCd()) {
                case EmployerFeeRedebit:
                    isRedebitTxnReturn = true;
                case EmployerFeeDebit:
                    feeAmount = (SpcfMoney) feeAmount.add(ft.getFinancialTransactionAmount());
                    break;
                case ServiceSalesAndUseTaxRedebit:
                    isRedebitTxnReturn = true;
                case ServiceSalesAndUseTax:
                    salesTaxAmount = (SpcfMoney) salesTaxAmount.add(ft.getFinancialTransactionAmount());
                    break;
                case EmployerDdRedebit:
                    isRedebitTxnReturn = true;
                case EmployerDdDebit:
                    ddAmount = (SpcfMoney) ddAmount.add(ft.getFinancialTransactionAmount());
                    break;
                case EmployerTaxRedebit:
                    isRedebitTxnReturn = true;
                case EmployerTaxDebit:
                    taxAmount = (SpcfMoney) taxAmount.add(ft.getFinancialTransactionAmount());
                    break;
            }
        }

        //
        // If we have no amount to report to the file, just return.
        //
        if (feeAmount.equals(ZERO) && salesTaxAmount.equals(ZERO) && taxAmount.equals(ZERO) && ddAmount.equals(ZERO)) {
            return false;
        }

        //
        // Get legal address info
        //
        Address address = company.getLegalAddress();
        String legalAddress = (address == null) ? "<unknown>" : address.getAddressLine1();
        String legalCity = (address == null) ? "<unknown>" : address.getCity();
        String legalState = (address == null) ? "<unknown>" : address.getState();
        String legalZip = (address == null) ? "<unknown>" : address.getZipCode();

        if ((address != null) && (address.getZipCodeExtension() != null)) {
            legalZip += "-" + address.getZipCodeExtension();
        }

        //
        // Get Payroll Admin contact info
        //
        Contact payrollAdmin = company.getContactByRoleCode(ContactRole.PayrollAdmin);
        String contactName = "<unknown>";
        String phoneNumber = "<unknown>";
        String faxNumber = "<unknown>";
        String emailAddress = "<unknown>";

        if(payrollAdmin != null) {
            contactName = payrollAdmin.getFullName();
            phoneNumber = payrollAdmin.getPhone();
            faxNumber = payrollAdmin.getFax();
            emailAddress = payrollAdmin.getEmail();
        }

        //
        // Get paycheck date from payroll run
        //
        DomainEntitySet<FinancialTransaction> txnSet = returnedTxnSet.find(FinancialTransaction.PayrollRun().isNotNull());
        SpcfCalendar paycheckDateCal = txnSet.isEmpty() ? null : txnSet.get(0).getPayrollRun().getPaycheckDate();
        String paycheckDate = (paycheckDateCal == null) ? "<unknown>" : paycheckDateCal.format("MM/dd/yyyy");

        String companyMaxLimit = null;
        String eeMaxLimit = null;

        if(serviceTypeCode == null) {
            DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) company.getCompanyService(ServiceCode.DirectDeposit);
            if(ddCompanyServiceInfo != null) {
                if(ddCompanyServiceInfo.getOverrideEmployeeLimitAmount() != null) {
                    eeMaxLimit = ddCompanyServiceInfo.getOverrideEmployeeLimitAmount().toString();
                } else {
                    eeMaxLimit = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultEmployeeLimit).getValue();
                }
                if(ddCompanyServiceInfo.getOverrideCompanyLimitAmount() != null) {
                    companyMaxLimit = ddCompanyServiceInfo.getOverrideCompanyLimitAmount().toString();
                } else {
                    companyMaxLimit = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultCompanyLimit).getValue();
                }
            }
            if(company.isCompanyOnService(ServiceCode.Tax)) {
                serviceTypeCode = ServiceCode.Tax;
            } else if(company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                serviceTypeCode = ServiceCode.DirectDeposit;
            }
        } else {
            BPCompanyServiceInfo bpCompanyServiceInfo = (BPCompanyServiceInfo) company.getCompanyService(ServiceCode.BillPayment);
            if(bpCompanyServiceInfo != null) {
                if(bpCompanyServiceInfo.getCompanyLimit() != null) {
                    eeMaxLimit = bpCompanyServiceInfo.getCompanyLimit().toString();
                } else {
                    eeMaxLimit = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultEmployeeLimit).getValue();
                }
                if(bpCompanyServiceInfo.getPayeeLimit() != null) {
                    companyMaxLimit = bpCompanyServiceInfo.getPayeeLimit().toString();
                } else {
                    companyMaxLimit = LimitRule.findLimitRule(company, ServiceCode.DirectDeposit).findLimitValueByName(LimitValueType.DefaultCompanyLimit).getValue();
                }
            }
        }

        //Get customer signed up date from company event(EventTypeCode.CustomerSignedUp) for the service reporting here.
        String signUpDate = "<unknown>";
        CompanyService companyService = company.getCompanyService(serviceTypeCode);
        if(companyService != null) {
            DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.CustomerSignedUp, EventDetailTypeCode.CompanyServiceId, companyService.getId().toString());
            if(companyEventDetails.isNotEmpty()) {
                signUpDate = companyEventDetails.getFirst().getCompanyEvent().getEventTimeStamp().format("MM/dd/yyyy");
            }
        }

        //Get strike count for last 12 months
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-12);

        //
        // Write the formatted record to the writer
        //
        pWriter.write(String.format(mRecordTemplate,
                                    company.isCompanyOnHold() ? "OnHold":"",
                                    company.getOnHoldNotesString().replaceAll("\\(|\\)", ""),
                                    company.getSourceCompanyId(),
                                    company.getFedTaxId(),
                                    serviceTypeCode != null ? serviceTypeCode.toString() : "<unknown>", // Service name
                                    company.getLegalName(),
                                    company.getDbaName(),
                                    legalAddress,
                                    legalCity,
                                    legalState,
                                    legalZip,
                                    phoneNumber,
                                    faxNumber,
                                    contactName,
                                    emailAddress,
                                    companyMaxLimit,
                                    eeMaxLimit,
                                    pTxnReturn.getBankReturnCd(),
                                    isRedebitTxnReturn ? "Yes" : "",
                                    pTxnReturn.getBankReturnTraceNumber(),
                                    mReturnDate.format("MM/dd/yyyy"),
                                    paycheckDate,
                                    CompanyEvent.getCompanyStrikeCount(company, fromDate, null),
                                    mmt.getMoneyMovementTransactionAmount().toString(),
                                    taxAmount.toString(),
                                    ddAmount.toString(),
                                    feeAmount.toString(),
                                    "0.00", "0.00", "0.00", "0.00", "0.00", "0.00",
                                    "0.00", "0.00", "0.00", "0.00", "0.00", "0.00",
                                    salesTaxAmount.toString(),
                                    "0.00", "0.00",
                                    "", "0.00", "", "0.00", "", "0.00", "", "0.00", "", "0.00", "",
                                    pTxnReturn.getBankReturnDescription(),
                                    "", "",
                                    PayrollRun.getPayrollRunCount(company),
                                    signUpDate));

        return true;
    }
}

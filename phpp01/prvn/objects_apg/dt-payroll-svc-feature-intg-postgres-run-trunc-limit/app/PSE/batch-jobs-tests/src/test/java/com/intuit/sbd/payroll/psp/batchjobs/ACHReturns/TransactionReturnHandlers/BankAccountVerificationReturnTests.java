package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ContactDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 27, 2008
 * Time: 2:07:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class BankAccountVerificationReturnTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testBankAccountVerificationReturn_PendingVerificationStatus() {
        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForBankAccountVerificationReturnEvent_BA_not_active();
        PayrollServices.commitUnitOfWork();

        // find and invoke the handler
        DomainEntitySet<TransactionReturn> processedReturns = new DomainEntitySet<TransactionReturn>();
        PayrollServices.beginUnitOfWork();
        for (TransactionReturn transactionReturn : returnList) {
            TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            transactionReturn = handler.execute(Application.refresh(transactionReturn));
            processedReturns.add(transactionReturn);
        }
        PayrollServices.commitUnitOfWork();

        // make sure IntuitEmployerVerificationReturnTransfer got created

        for (TransactionReturn currRet : processedReturns) {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn
                    .findFinancialTransaction(currRet);
            FinancialTransaction finTxn = returnedFTs.get(0);
            DomainEntitySet<FinancialTransaction> xferFTsCreated = FinancialTransaction.
                    findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(),
                            finTxn.getCompany().getSourceCompanyId(),
                            TransactionTypeCode.IntuitEmployerVerificationReturnTransfer,
                            TransactionStateCode.Created);
            CompanyBankAccount cba = CompanyBankAccount
                    .findCompanyBankAccountIncludingExpired(finTxn.getCompany(), finTxn.getDebitBankAccount());
            BankAccountStatus cbaStatus = cba.getStatusCd();
            PayrollServices.commitUnitOfWork();
            assertEquals("Number of returned transactions", 1, returnedFTs.size());
            assertEquals("Transfer financial transactions ", 2, xferFTsCreated.size());

            PayrollServices.beginUnitOfWork();
            boolean matchedAmount = false;
            finTxn = Application.refresh(finTxn);
            String sku = finTxn.getSku();
            for (FinancialTransaction xferTxn : xferFTsCreated) {
                if (xferTxn.getFinancialTransactionAmount().equals(finTxn.getFinancialTransactionAmount())) {
                    matchedAmount = true;
                    assertEquals("SKU", sku, xferTxn.getSku());
                }
            }

            assertTrue("Found transfer with correct amount", matchedAmount);

            // make sure original FT is now "Returned"
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    finTxn.getCurrentTransactionState().getTransactionStateCd());

            //Ensure the company bank account was deactivated
            assertEquals("Company bank account status", BankAccountStatus.Inactive, cbaStatus);

            // make sure the TransactionReturn is now "Resolved"
            assertEquals("Transaction Return Status", TransactionReturnStatusCode.Resolved,
                    currRet.getReturnStatusCd());

            PayrollServices.commitUnitOfWork();
        }

        // make sure the right company event got created
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CBAVerifyReturn, CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 2, companyEventsList.size());
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("Verification Status", EnumUtils.getReadableName(VerificationStatusType.CBADeactivated),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.VerificationStatus));

            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.CBAVerificationReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testQBDTBankAccountVerificationReturn_PendingVerificationStatus() {
        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForBankAccountVerificationReturnEvent_BA_not_active();
        PayrollServices.commitUnitOfWork();

        // find and invoke the handler
        DomainEntitySet<TransactionReturn> processedReturns = new DomainEntitySet<TransactionReturn>();
        PayrollServices.beginUnitOfWork();
        for (TransactionReturn transactionReturn : returnList) {
            TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            transactionReturn = handler.execute(Application.refresh(transactionReturn));
            processedReturns.add(transactionReturn);
        }
        PayrollServices.commitUnitOfWork();

        for (TransactionReturn currRet : processedReturns) {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn
                    .findFinancialTransaction(currRet);
            FinancialTransaction finTxn = returnedFTs.get(0);
            DomainEntitySet<FinancialTransaction> xferFTsCreated = FinancialTransaction.
                    findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(),
                            finTxn.getCompany().getSourceCompanyId(),
                            TransactionTypeCode.IntuitEmployerVerificationReturnTransfer,
                            TransactionStateCode.Created);
            CompanyBankAccount cba = CompanyBankAccount
                    .findCompanyBankAccountIncludingExpired(finTxn.getCompany(), finTxn.getDebitBankAccount());
            BankAccountStatus cbaStatus = cba.getStatusCd();
            PayrollServices.commitUnitOfWork();
            assertEquals("Number of returned transactions", 1, returnedFTs.size());
            assertEquals("Transfer financial transactions ", 2, xferFTsCreated.size());

            PayrollServices.beginUnitOfWork();
            boolean matchedAmount = false;
            finTxn = Application.refresh(finTxn);
            String sku = finTxn.getSku();
            for (FinancialTransaction xferTxn : xferFTsCreated) {
                if (xferTxn.getFinancialTransactionAmount().equals(finTxn.getFinancialTransactionAmount())) {
                    matchedAmount = true;
                    assertEquals("SKU", sku, xferTxn.getSku());
                }
            }

            assertTrue("Found transfer with correct amount", matchedAmount);

            // make sure original FT is now "Returned"
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    finTxn.getCurrentTransactionState().getTransactionStateCd());

            //Ensure the company bank account was NOT deactivated
            assertEquals("Company bank account status", BankAccountStatus.PendingVerification, cbaStatus);

            // make sure the TransactionReturn is now "Resolved"
            assertEquals("Transaction Return Status", TransactionReturnStatusCode.Resolved,
                    currRet.getReturnStatusCd());
            PayrollServices.commitUnitOfWork();
        }

        // make sure the right company event got created
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CBAVerifyReturn, CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 2, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("Verification Status", EnumUtils.getReadableName(VerificationStatusType.PendingVerification),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.VerificationStatus));
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.CBAVerificationReturn), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBankAccountVerificationReturn_ActiveStatus() {
        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForBankAccountVerificationReturnEvent();
        PayrollServices.commitUnitOfWork();

        // find and invoke the handler
        DomainEntitySet<TransactionReturn> processedReturns = new DomainEntitySet<TransactionReturn>();
        PayrollServices.beginUnitOfWork();
        for (TransactionReturn transactionReturn : returnList) {
            TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            transactionReturn = handler.execute(transactionReturn);
            processedReturns.add(transactionReturn);
        }
        PayrollServices.commitUnitOfWork();

        for (TransactionReturn currRet : processedReturns) {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn
                    .findFinancialTransaction(currRet);
            FinancialTransaction finTxn = Application.refresh(returnedFTs.get(0));
            DomainEntitySet<FinancialTransaction> xferFTsCreated = FinancialTransaction.
                    findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(),
                            finTxn.getCompany().getSourceCompanyId(),
                            TransactionTypeCode.IntuitEmployerVerificationReturnTransfer,
                            TransactionStateCode.Created);
            CompanyBankAccount cba = CompanyBankAccount
                    .findCompanyBankAccountIncludingExpired(finTxn.getCompany(), finTxn.getDebitBankAccount());
            BankAccountStatus cbaStatus = cba.getStatusCd();
            PayrollServices.commitUnitOfWork();
            assertEquals("Number of returned transactions", 1, returnedFTs.size());
            assertEquals("Transfer financial transactions ", 2, xferFTsCreated.size());

            PayrollServices.beginUnitOfWork();

            boolean matchedAmount = false;
            finTxn = Application.refresh(finTxn);
            String sku = finTxn.getSku();
            for (FinancialTransaction xferTxn : xferFTsCreated) {
                if (xferTxn.getFinancialTransactionAmount().equals(finTxn.getFinancialTransactionAmount())) {
                    matchedAmount = true;
                    assertEquals("SKU", sku, xferTxn.getSku());
                }
            }

            assertTrue("Found transfer with correct amount", matchedAmount);

            // make sure original FT is now "Returned"
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    finTxn.getCurrentTransactionState().getTransactionStateCd());

            //Ensure the company bank account was not deactivated
            assertEquals("Company bank account status", BankAccountStatus.Active, cbaStatus);

            // make sure the TransactionReturn is now "Resolved"
            assertEquals("Transaction Return Status", TransactionReturnStatusCode.Resolved,
                    currRet.getReturnStatusCd());

            PayrollServices.commitUnitOfWork();
        }

        // make sure the right company event got created
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.CBAVerifyReturn, CompanyEventStatus.Active, null, null);
        CompanyBankAccount cba = CompanyBankAccount.findCompanyBankAccount(company, "123123");
        boolean bIsOnHold = company.isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = company.getOnHoldReasonCollection();

        //Ensure the company is on hold
        assertTrue("Company is on hold", bIsOnHold);
        //Ensure the company has one on hold reason and that it is for fraud review
        assertEquals("Number of reasons company is on hold", 1, onHoldReasons.size());
        OnHoldReason onHoldReason = onHoldReasons.iterator().next();
        assertEquals("On Hold Reason", ServiceSubStatusCode.Fraud, onHoldReason.getOnHoldReasonCd());

        assertEquals("Company Events", 2, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.CBAVerificationReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testBankAccountVerificationReturn_QBDTActiveStatus() {
        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForBankAccountVerificationReturnEvent();
        PayrollServices.commitUnitOfWork();

        // find and invoke the handler
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        // make sure IntuitEmployerVerificationReturnTransfer got created
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn
                .findFinancialTransaction(transactionReturn);
        FinancialTransaction finTxn = returnedFTs.get(0);
        DomainEntitySet<FinancialTransaction> xferFTsCreated = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(),
                        finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.IntuitEmployerVerificationReturnTransfer,
                        TransactionStateCode.Created);
        String sku = finTxn.getSku();
		for (FinancialTransaction xferTxn: xferFTsCreated) {
			assertEquals("SKU", sku, xferTxn.getSku());
        }
        CompanyBankAccount cba = CompanyBankAccount
                .findCompanyBankAccountIncludingExpired(finTxn.getCompany(), finTxn.getDebitBankAccount());
        BankAccountStatus cbaStatus = cba.getStatusCd();
        boolean bIsOnHold = finTxn.getCompany().isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = finTxn.getCompany().getOnHoldReasonCollection();
        Application.commitUnitOfWork();
        assertEquals("Financial Transactions ", 1, xferFTsCreated.size());
        assertEquals("Financial Transaction Amount ", xferFTsCreated.get(0).getFinancialTransactionAmount(),
                finTxn.getFinancialTransactionAmount());

        // make sure original FT is now "Returned"
        assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                finTxn.getCurrentTransactionState().getTransactionStateCd());

        // make sure the right company event got created
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent
                .findCompanyEvents(finTxn.getCompany(),
                        EventTypeCode.CBAVerifyReturn, CompanyEventStatus.Active, null, null);

        //Ensure the company bank account is active
        assertEquals("Company bank account status", BankAccountStatus.Active, cbaStatus);
        //Ensure the company is on hold
        assertTrue("Company is on hold", bIsOnHold);
        //Ensure the company has one on hold reason and that it is for fraud review
        assertEquals("Number of reasons company is on hold", 1, onHoldReasons.size());
        OnHoldReason onHoldReason = onHoldReasons.iterator().next();
        assertEquals("On Hold Reason", ServiceSubStatusCode.Fraud, onHoldReason.getOnHoldReasonCd());

        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent companyEvent = companyEventsList.get(0);
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.CBAVerificationReturn),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        // make sure the TransactionReturn is now "Resolved"
        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    //PSRV000943
    @Test
    public void testBankAccountVerificationReturn_CompanyOnFraudReview() {
        Company1Dataloader c1dl = new Company1Dataloader();
        Company2Dataloader c2dl = new Company2Dataloader();
        DataLoader dataloader = new DataLoader();

        //Terminate DD Service
        PayrollServices.beginUnitOfWork();
        Company company = c1dl.persistCompany1();
        ProcessResult pr = PayrollServices.companyManager.terminateService
                (company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("terminate service", pr);
        PayrollServices.commitUnitOfWork();

        //Create company2
        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = c2dl.getCompany1();
        for (ContactDTO currContact : company2.getContacts()) {
            currContact.setEmail("someEmail1@aol.com");
        }
        Company domainCompany2 = dataloader.persistCompany(company2);
        dataloader.persistCompanyService(domainCompany2, c2dl.getCompany1Service());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companybankAccount2 = c2dl.getCompany1BankAccount();
        //same BA used by c1dl persistCompany
        BankAccountDTO c1BA = dataloader.getTestCompanyBankAccount().getBankAccountDTO();
        companybankAccount2.setBankAccountDTO(c1BA);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBOE, company2.getCompanyId(), companybankAccount2, true, true);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Company bank account added", processResult);
        assertEquals("Number of messages", 1, processResult.getMessages().size());

        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Message code", "1040", errorMessage.getMessageCode());
        assertEquals("Message text",
                "Company QBOE:2222222 was added but could not be activated because it matches an existing company that is either on hold or terminated.",
                errorMessage.getMessage().trim());
        assertEquals("Message level", MessageInfo.MessageLevel.WARNING,
                errorMessage.getLevel());

        //Offload Company2 Bank Verification Debits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        //Return Company2 Bank Verification Debits.
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("2222222", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> offLoadedTransactions = FinancialTransaction.
                findFinancialTransactions(company.getSourceSystemCd(),company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Executed);

        assertEquals("Number of C1 EmployerVerificationDebit txns", 2, offLoadedTransactions.size());
        PSPDate.setPSPTime("20070911000000");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(offLoadedTransactions, "R02",
                         "Non-NSF Return");
        // make sure IntuitEmployerVerificationReturnTransfer got created
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("2222222", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> returnFinTxns = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(),company.getSourceCompanyId(),
                                                                                                             TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Returned);

        assertEquals("Number of returned EmployerVerificationDebit Txns", 2, returnFinTxns.size());

        DomainEntitySet<FinancialTransaction> xferFTsCreated = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(),
                                                                                                              company.getSourceCompanyId(),
                                                                                                              TransactionTypeCode.IntuitEmployerVerificationReturnTransfer,
                                                                                                              TransactionStateCode.Created);
        String sku = returnFinTxns.get(0).getSku();
        for (FinancialTransaction xferTxn : xferFTsCreated) {
            assertEquals("SKU", sku, xferTxn.getSku());
        }

        FinancialTransaction erVerDebitFinTxn = returnFinTxns.get(0);
        CompanyBankAccount cba = CompanyBankAccount
                .findCompanyBankAccountIncludingExpired(company, erVerDebitFinTxn.getDebitBankAccount());
        BankAccountStatus cbaStatus = cba.getStatusCd();
        boolean bIsOnHold = erVerDebitFinTxn.getCompany().isCompanyOnHold();
        DomainEntitySet<OnHoldReason> onHoldReasons = erVerDebitFinTxn.getCompany().getOnHoldReasonCollection();

        PayrollServices.commitUnitOfWork();
        assertEquals("Financial Transactions ", 2, xferFTsCreated.size());

        //Ensure the company bank account is inactive
        assertEquals("Company bank account status", BankAccountStatus.Inactive, cbaStatus);
        //Ensure the company is on hold
        assertTrue("Company is on hold", bIsOnHold);
        //Ensure the company has one on hold reason and that it is for fraud review
        assertEquals("Number of reasons company is on hold", 1, onHoldReasons.size());
        OnHoldReason onHoldReason = onHoldReasons.iterator().next();
        assertEquals("On Hold Reason", ServiceSubStatusCode.FraudReview, onHoldReason.getOnHoldReasonCd());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent
                .findCompanyEvents(company,
                                   EventTypeCode.CBAVerifyReturn, CompanyEventStatus.Active, null, null);

        PayrollServices.commitUnitOfWork();

        assertEquals("Company Events", 2, companyEventsList.size());

    }

    @Test
    public void testQBDTSymphonyBankAccountVerificationDebitReturnPendingVerificationStatus() {
        String sourceCompanyId = "8574536";

        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForBankAccountVerificationReturnEvent_BA_not_active(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        // find and invoke the handler
        DomainEntitySet<TransactionReturn> processedReturns = new DomainEntitySet<TransactionReturn>();
        PayrollServices.beginUnitOfWork();
        for (TransactionReturn transactionReturn : returnList) {
            TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            transactionReturn = handler.execute(Application.refresh(transactionReturn));
            processedReturns.add(transactionReturn);
        }
        PayrollServices.commitUnitOfWork();


        // Validation

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        for (TransactionReturn transactionReturn : processedReturns) {

            DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(transactionReturn);

            FinancialTransaction returnedFinancialTransaction = returnedFinancialTransactions.getFirst();

            DomainEntitySet<FinancialTransaction> employerVerificationReturnTransfers = transactionReturnHelper.findEmployerVerificationReturnTransfers(company);

            transactionReturnVerifier.verifyIntuitEmployerVerificationReturnTransfer(employerVerificationReturnTransfers, returnedFinancialTransactions);

            transactionReturnVerifier.verifyVariableBankDebitAmount(employerVerificationReturnTransfers, returnedFinancialTransaction);

            transactionReturnVerifier.verifyEmployerVerificationDebitFinancialTransactionReturn(returnedFinancialTransactions);

            transactionReturnVerifier.verifyBankAccountStatus(company, returnedFinancialTransaction.getDebitBankAccount(), BankAccountStatus.PendingVerification);

            transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturn, TransactionReturnStatusCode.Resolved);
        }

        transactionReturnVerifier.verifyCBAVerifyReturnCompanyEvent(company, 2);

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testQBDTSymphonyBankAccountVerificationDebitReturnActiveStatus() {
        String sourceCompanyId = "8574536";

        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForBankAccountVerificationReturnEvent(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        // find and invoke the handler
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        // make sure IntuitEmployerVerificationReturnTransfer got created
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction returnedFinancialTransaction = returnedFinancialTransactions.getFirst();

        DomainEntitySet<FinancialTransaction> employerVerificationReturnTransfers = transactionReturnHelper.findEmployerVerificationReturnTransfers(company);

        transactionReturnVerifier.verifyEmployerVerificationDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyVariableBankDebitAmount(employerVerificationReturnTransfers, returnedFinancialTransaction);

        transactionReturnVerifier.verifyBankAccountStatus(company, returnedFinancialTransaction.getDebitBankAccount(), BankAccountStatus.Active);

        transactionReturnVerifier.verifyCompanyOnHold(company, ServiceSubStatusCode.Fraud);

        transactionReturnVerifier.verifyCBAVerifyReturnCompanyEvent(company, 1);

        transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturn, TransactionReturnStatusCode.Resolved);

        Application.rollbackUnitOfWork();
    }
}

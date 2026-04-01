package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.tools.ComplianceToolkit;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: dweinberg
 * Date: 10/16/13
 * Time: 1:32 PM
 */
public class PaymentTemplateBankAccountTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testCODRBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CO-DR1094-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2014, 8, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 8, 21));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2014, 9, 12));
        OffloadBatch offloadBatch=null;
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("CO-DR1094-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("121000248", creditBankAccount.getRoutingNumber());
        assertEquals("19451234567891945", creditBankAccount.getAccountNumber());
        offloadBatch=  moneyMovementTransaction.getOffloadBatch();

        DomainEntitySet<NACHAFile> files = offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        String fileName = files.getFirst().getFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));
        String[] expectedOutput = {"101 02100002197226160001409111325J094101JPMORGAN CHASE         INTUIT                         ",
                "5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY140915140915   1021000020000001",
                "622121000248194512345678919450000002800TEST_0001      TEST_COMPANY_1          \\d{16}+",
                "705TXP\\*122456\\*011  \\*140831\\*T\\*0000002800\\\\                                           \\d{11}+",
                "822000000200121000240000000000000000000028009118556001                         021000020000001",
                "5225INTUIT                     7700346619118556001CCDEFT TAX PY140915140915   1021000020000002",
                "627021000021911855633        0000002800911855633      INTUIT TAX              \\d{16}+",
                "822500000100021000020000000028000000000000009118556001                         021000020000002",
                "9000002000001000000030014200026000000002800000000002800                                       ",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"};

        Assert.assertEquals("Offload output sizes do not match up.", lines.length, expectedOutput.length);
        for (int i = 0; i < expectedOutput.length; i++) {
            String expected=expectedOutput[i];//.replaceAll("\\*","X");
            String actual=lines[i];//.replaceAll("\\*","X");
            Pattern pattern = Pattern.compile(expected);
            Matcher matcher = pattern.matcher(actual);

            assertTrue("Did not find expected output:\n" + expectedOutput[i] + "\nIn output:\n" + lines[i], matcher.matches());
        }

        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testTXC3VBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("TX-C3V-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2017, 1, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2017, 2, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2017, 2, 21));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2017, 3, 12));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("TX-C3V-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("111000614", creditBankAccount.getRoutingNumber());
        assertEquals("00100000414", creditBankAccount.getAccountNumber());
        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "TXP*122456789*68307*170331*1*116000\\";

        assertEquals("TXP are not the same", edr.getFirst().getTxpRecordData(), expected);

        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testVTC101BankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-DOL4-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("VT-C101-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2017, 1, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate=CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("VT-C101-PAYMENT"));
        companyAgencyPaymentTemplate.setAgencyTaxpayerId("333 4444");
        PayrollServices.commitUnitOfWork();

        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.AddCompanyPaymentMethods.toString(), "VT-C101-PAYMENT"});
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.RecalculateCompanyPaymentMethodsEnabled.toString(), "VT-C101-PAYMENT"});

        DataLoadServices.setPSPDate(2017, 2, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2017, 2, 21));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2017, 3, 12));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("VT-C101-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("011600062", creditBankAccount.getRoutingNumber());
        assertEquals("89000203", creditBankAccount.getAccountNumber());
        assertEquals("CHITTENDEN", creditBankAccount.getBankName());
        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "DOL*019*3334444*01111*170331*T*0000052000*0000000000000000\\*0000052000";

        assertEquals("TXP are not the same", edr.getFirst().getTxpRecordData(), expected);

        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testComplianceToolkitOnPaymentTemplateBankAccountChange() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("AL-CR4WH-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 10, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 10, 16));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("AL-CR4WH-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("083000108", creditBankAccount.getRoutingNumber());
        assertEquals("3160062351", creditBankAccount.getAccountNumber());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplateBankAccount activeBankAccount = PaymentTemplateBankAccount.findActiveBankAccount(PaymentTemplate.findPaymentTemplate("AL-CR4WH-PAYMENT"));
        activeBankAccount.setStatusCd(BankAccountStatus.Inactive);
        activeBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());

        BankAccount newBankAccount = new BankAccount();
        newBankAccount.setRoutingNumber("4567");
        newBankAccount.setAccountNumber("1234");
        Application.save(newBankAccount);

        PaymentTemplateBankAccount newPTBankAccount = new PaymentTemplateBankAccount();
        newPTBankAccount.setBankAccount(newBankAccount);
        newPTBankAccount.setPaymentTemplate(PaymentTemplate.findPaymentTemplate("AL-CR4WH-PAYMENT"));
        newPTBankAccount.setStatusCd(BankAccountStatus.Active);
        newPTBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());
        Application.save(newPTBankAccount);

        PayrollServices.commitUnitOfWork();

        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.UpdateBankAccountsOnPendingPayments.toString()});
        //rollback status  to active
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PaymentTemplateBankAccount> paymentTemplateBankAccounts =
                Application.find(PaymentTemplateBankAccount.class,
                                 PaymentTemplateBankAccount.PaymentTemplate().equalTo(PaymentTemplate.findPaymentTemplate("AL-CR4WH-PAYMENT"))
                                                           .And(PaymentTemplateBankAccount.StatusCd().equalTo(BankAccountStatus.Inactive)));
        activeBankAccount = paymentTemplateBankAccounts.getFirst();
        activeBankAccount.setStatusCd(BankAccountStatus.Active);
        activeBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
        //rollback end
        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("4567", creditBankAccount.getRoutingNumber());
        assertEquals("1234", creditBankAccount.getAccountNumber());
        PayrollServices.rollbackUnitOfWork();

    }


    /*
    The purpose of this test is to enforce that changes to agency bank accounts follow the proper process.
    The proper process is more-or-less illustrated in the test above, but in real life, it will be done in SQL,
    either against production or in the static data.  Here are those steps:
      1) Update the current payment template bank account to "Inactive"
      2) Insert a _new_ BankAccount with the new information.
      3) Insert a new payment template bank account as "Active" and associate with the new bank account
      4) Run compliance toolkit (UpdateBankAccountsOnPendingPayments & RecreateEntryDetailRecords) on the affected template.
      5) Update this unit test with _new_ records only
    Essentially this test is testing that no one has changed the static data that should be immutable.
      -Do NOT change the association of a payment template bank account's bank account
      -Do NOT change the account or routing numbers on a bank account
    In the past this has been improperly done and we have lost important history.
     */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    public void testBankAccountsNotChanged() {

        //select pba.pmt_template_bankaccount_seq, pba.payment_template_fk, pba.bank_account_fk from psp_pmt_template_bankaccount pba
        String paymentTemplateBankAccounts =
                        "afe8b613-6588-4fb5-b7e0-285e96120c75\tTX-C3V-PAYMENT\t33b17266-6d6d-4dfb-83cd-93511f52c1f1\n" +
                        "22ff6070-7843-6ccc-e053-690aa80ad194\tME-941C1ME-PAYMENT\t33e4fff0-3595-4d66-9150-06ddb79c2f60\n" +
                        "e7654d2b-995a-4f77-bc2d-cefb7fb5c1da\tVT-C101-PAYMENT\t347cccb9-8d41-4bd4-9a1e-a20401b20909\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51cac85\tGA-DOL4-PAYMENT\t4063bac7-4c8f-4811-8188-1852d71d0303\n" +
                        "bd1ea6e7-5958-4e38-9fdb-ed2ad7e1fb4e\tVT-WH433-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0303\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51cac95\tWA-PFML-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0313\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51bcc95\tMT-UI5-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0319\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51cac75\tMA-PFML-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0320\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51cac76\tSC-UCE120-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0321\n" +
                        "fedd202c-4c45-4648-8e57-67f2d58cad79\tOR-STTV-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0333\n" +
                        "5f748cc5-87e4-4c44-a591-13bbf6127001\tMN-MW1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157567\n" +
                        "5f748cc5-87e4-4c44-a591-13bbf6127002\tMN_WH\ta5263743-7cf0-42fc-b164-25ea83157567\n" +
                        "5f748cc5-87e4-4c44-a591-13bbf6127003\tMN-DEED1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157568\n" +
                        "5f748cc5-87e4-4c44-a591-13bbf6127004\tMN_UI\ta5263743-7cf0-42fc-b164-25ea83157568\n" +
                        "fedd202c-4c45-4648-8e57-67f2d56cad79\tMD-MW506-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0331\n" +
                        "b79e01ce-9fd7-4d23-94e5-406adba0193d\tME-900ME-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315756e\n" +
                        "5aab16dd-acd1-4ca3-b01b-85122bffb21e\tLA-L1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157571\n" +
                        "cbaeb7b3-3373-4ad2-979f-78779a55ff58\tAR-941M-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157573\n" +
                        "98e9c30c-3264-42a2-81f7-6a16e3205d55\tIL-501-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157577\n" +
                        "ff18fe10-8db0-4eca-8dc3-f68414d9d8fc\tIL-UI340-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157578\n" +
                        "fedd202c-4c45-4648-8e57-67f2d60cad79\tOR-OTCWH-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0333\n" +
                        "fedd202c-4c45-4648-8e57-67f2d59cad79\tOR-OTCUI-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0333\n" +
                        "e2194ad0-38b8-4e23-99c9-9e7880a33b9c\tPA-501-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315757a\n" +
                        "318bb7cd-9319-47f1-b7aa-c2bf7b9c12d6\tNY-SDI-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315757c\n" +
                        "4213c67e-5fab-4ae8-a737-2a2ed08e8ea4\tGA-GAV-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315757d\n" +
                        "7cf5d420-e794-4695-86e8-c57dd3432af0\tUT-TC96-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315757e\n" +
                        "47dd9069-1f63-4384-843e-ff0204b7632a\tMA-M941-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315757f\n" +
                        "cc93773a-f2f3-449b-b9ec-e05c5e2e6c04\tMA-1700HI-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157580\n" +
                        "8988ae59-d4d8-4982-b4c4-f282f1a8aebf\tMO-941-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157581\n" +
                        "22e5e45d-98fb-44b3-9fd4-768dd4e20ce8\tWI-WT6-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157583\n" +
                        "6b6ab7a7-b24c-449c-a5a3-d59fa194cd63\tWI-UCT101-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157584\n" +
                        "02440b36-219e-4f4c-8474-0196b2d281b4\tNC-101-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157586\n" +
                        "a49aef78-85f5-406a-8e47-c9bb51338789\tSC-WH1601-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157587\n" +
                        "c158d33a-5030-4ce2-b85a-c35d1a050786\tVA-VA15-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157588\n" +
                        "59959487-e5e6-48b7-a68e-cfbdce109c8a\tOH-SD101-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157589\n" +
                        "043ed144-5dda-4f92-bb4f-e5db783617ba\tOH-JFS20127-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157589\n" +
                        "0f19a26d-104d-40ad-9c5a-75d5510e2c5a\tOH-IT501-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157589\n" +
                        "c2d40f8f-eb37-448c-9262-466c565673e1\tAZ-A1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315758a\n" +
                        "04346dd0-35c7-4d5a-a01a-b61e44d91c00\tAZ-UC018-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315758b\n" +
                        "d3ae8f40-c789-4bc6-82d9-952d236f7128\tOK-OW9A-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315758d\n" +
                        "fedd202c-4c45-4648-8e57-67f2d55cad79\tAL-CR4WH-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0330\n" +
                        "12534f49-4108-44a4-aa26-a0a0ed3f64d9\tAL-CR4UI-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315758f\n" +
                        "a6dd42d5-030b-496d-834b-f50dc8c7b007\tKY-K1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157590\n" +
                        "a4649656-c219-496c-9538-4d0f4c805e97\tCT-CTWH-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157591\n" +
                        "84682199-8610-4bd4-b475-336fb5485dc0\tNJ-NJ500-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157592\n" +
                        "c1b97921-07e6-49ad-9dd9-04a713f14d51\tNJ-NJ500UI-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157593\n" +
                        "5ab9c4c1-e42d-41b0-937a-4ae6ea20f2e4\tNE-941N-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157594\n" +
                        "ac9a5376-dad9-4643-b08b-b69101319cda\tNE-UI11T-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157595\n" +
                        "f1c9d366-b8d5-4410-8c6a-7cd1a8ec5db6\tHI-VP1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157596\n" +
                        "a270798f-623b-4674-a87c-a2862f54059b\tIA-44105-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157597\n" +
                        "803189e1-e320-4848-91ea-a1f8db24879f\tIA-600103-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157598\n" +
                        "fedd202c-4c45-4648-8e57-67f2d54cad79\tNM-CRS1-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0329\n" +
                        "f64bbb98-b188-475d-8dac-31b46cf6c50c\tID-910-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315759b\n" +
                        "4dcc863d-1635-4f0f-ae75-0c08acb14277\tKS-KW5-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315759c\n" +
                        "3b5f3bdf-b33b-4484-a7fc-a32117be7358\tKS-CNS100-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315759d\n" +
                        "c821ef56-60fa-4a1b-ac82-ff2c876a5531\tRI-941-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315759e\n" +
                        "f43c177b-e15d-4c37-85f2-13c9fc315cf0\tMT-MW1-PAYMENT\ta5263743-7cf0-42fc-b164-25ea8315759f\n" +
                        "002702d4-150e-47b5-8181-f4137490c10f\tNV-NUCS4072-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a0\n" +
                        "f50c4742-e699-43bc-8224-0869ca0e7bb5\tND-306-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a1\n" +
                        "07dc9b87-c670-4f27-98d1-9c8ba0fb2f7e\tND-SFN41263-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a2\n" +
                        "fedd202c-4c45-4648-8e57-67f2d53cad79\tDC-FR900-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0328\n" +
                        "40b599d7-a4ce-4307-8815-f6cc81c4a0e2\tWV-IT101-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a6\n" +
                        "94fe53da-1422-4b37-b350-e82b5383e4b2\tTN-LB0456-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a7\n" +
                        "5aab16dd-acd1-4ca3-b01b-85122bffb21f\tLA-ES61-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a8\n" +
                        "ce1ba085-ba03-4c86-82cc-3192083b9fc3\tMS-M89-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575a9\n" +
                        "f64bbb98-b188-475d-8dac-31b46cf6c50d\tID-020-PAYMENT\ta5263743-7cf0-42fc-b164-25ea831575ff\n" +
                        "448c4ef4-270a-4d94-98d8-a2d2b6764be1\tCA-UIETT-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0456\n" +
                        "74467a70-6d32-8911-bc91-197a95ba0a86\tCA-PITSDI-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0456\n" +
                        "b1d970db-d11e-4850-90be-fe0f8b54878f\tFL-UCT6-PAYMENT\ta5263743-7cf0-42fc-b164-25ea83157601\n" +
                        "3b584bd6-6653-471e-bec8-be985b76acb5\tKS-KCNS100-PAYMENT\ta5263743-7cf0-42fc-b264-25ea8325759d\n" +
                        "d8c6b705-af28-44a2-b949-d8deb6c1f7f5\tMD-DLLR-PAYMENT\ta5263743-7cf0-42fc-c164-25ea8315756a\n" +
                        "29126343-d5a7-4dc1-9e17-f424ff140d9a\tNJ-NJ927PWH-PAYMENT\ta5263743-7cf0-42fc-c164-25ea83157592\n" +
                        "4213cde0-9276-4de0-c951-a9eabc7c6f42\tNJ-NJ927PUI-PAYMENT\ta5263743-7cf0-42fc-c164-25ea83157593\n" +
                        "95b4d5de-3fd7-478d-ad5c-3d8b2c6dcce8\tPA-UC2-PAYMENT\te0a70827-e95d-4684-b857-11ea7d0d6dc8\n" +
                        "dfe33d13-b19e-4510-a721-9f11f3cb2eeb\tNC-NC5P-PAYMENT\te0a70827-e95d-4684-b857-11ea7d0d6dc9\n" +
                        "65e7edb1-9a28-4611-8174-e72ca1eece86\tCO-DR1094-PAYMENT\te0a70827-e95d-4684-b857-11ea7d0d6dd0\n" +
                        "cbfac8ba-d0fb-4f6e-a0f7-34a0780c5358\tMI-MW106-PAYMENT\te0a70827-e95d-4684-b857-11ea7d0d6dd1\n" +
                        "ce1ba085-ba03-4c86-82cc-3192083b9fc4\tNY-1MN-PAYMENT\te0a70827-e95d-4684-b857-11ea7d0d6dd2\n" +
                        "ce1ba085-ba03-4c86-82cc-3192083b9fc5\tNY-MTA305-PAYMENT\te0a70827-e95d-4684-b857-11ea7d0d6dd3\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51cad78\tDE-DES-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0324\n" +
                        "fedd202c-4c45-4648-8e57-67f2d51cad79\tMO-MODES-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0326\n"+
                        "fedd202c-4c45-4648-8e57-67f2d52cad79\tCT-PFML-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0327\n" +
                        "fedd202c-4c45-4648-8e57-67f2d57cad79\tCT-2MAG-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0332\n" +
                                "fedd202c-4c45-4648-8e57-68f2d61cad79\tOR-PFMSL-PAYMENT\t4063bac7-4c8f-4811-8188-1852e71d0333";


                        //select ba.bank_account_seq, ba.routing_number, ba.account_number from psp_pmt_template_bankaccount pba join psp_bank_account ba on ba.bank_account_seq = pba.bank_account_fk
        String bankAccounts = "33b17266-6d6d-4dfb-83cd-93511f52c1f1\t111000614\t00100000414\n" +
                "33e4fff0-3595-4d66-9150-06ddb79c2f60\t021052053\t26066997\n" +
                "347cccb9-8d41-4bd4-9a1e-a20401b20909\t011600062\t89000203\n" +
                "4063bac7-4c8f-4811-8188-1852d71d0303\t061113415\t0005242744227\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0303\t221172186\t8877770634\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0313\t123000848\t153911802301\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0319\t092900383\t156041206772\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0320\t011000206\t501321259\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0321\t061100606\t4563931801\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0333\t021052053\t72561852\n" +
                "a5263743-7cf0-42fc-b164-25ea83157567\t091000022\t104757767371\n" +
                "a5263743-7cf0-42fc-b164-25ea83157567\t091000022\t104757767371\n" +
                "a5263743-7cf0-42fc-b164-25ea83157568\t091000022\t104774436109\n" +
                "a5263743-7cf0-42fc-b164-25ea83157568\t091000022\t104774436109\n" +
                "a5263743-7cf0-42fc-b164-25ea83157569\t052001633\t2001800598\n" +
                "a5263743-7cf0-42fc-b164-25ea8315756e\t021052053\t81302364\n" +
                "a5263743-7cf0-42fc-b164-25ea83157571\t065400137\t7900406139\n" +
                "a5263743-7cf0-42fc-b164-25ea83157573\t082000073\t0089431823\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0331\t121000248\t4104095807\n" +
                "a5263743-7cf0-42fc-b164-25ea83157578\t071109338\t038954\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0333\t021052053\t72561852\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0333\t021052053\t72561852\n" +
                "a5263743-7cf0-42fc-b164-25ea8315757a\t043000096\t1001342875\n" +
                "a5263743-7cf0-42fc-b164-25ea8315757c\t021000021\t817084296\n" +
                "a5263743-7cf0-42fc-b164-25ea8315757d\t061000227\t2000017207881\n" +
                "a5263743-7cf0-42fc-b164-25ea8315757e\t121000248\t0510805161\n" +
                "a5263743-7cf0-42fc-b164-25ea8315757f\t011000206\t501321576\n" +
                "a5263743-7cf0-42fc-b164-25ea83157580\t011000138\t004625512102\n" +
                "a5263743-7cf0-42fc-b164-25ea83157581\t086507174\t8600500\n" +
                "a5263743-7cf0-42fc-b164-25ea83157583\t075000022\t121634552\n" +
                "a5263743-7cf0-42fc-b164-25ea83157584\t075000022\t182845580\n" +
                "a5263743-7cf0-42fc-b164-25ea83157586\t053000196\t000684198943\n" +
                "a5263743-7cf0-42fc-b164-25ea83157587\t053207766\t2003203947868\n" +
                "a5263743-7cf0-42fc-b164-25ea83157588\t051000020\t201328895\n" +
                "a5263743-7cf0-42fc-b164-25ea83157589\t041001039\t014511002488\n" +
                "a5263743-7cf0-42fc-b164-25ea83157589\t041001039\t014511002488\n" +
                "a5263743-7cf0-42fc-b164-25ea83157589\t041001039\t014511002488\n" +
                "a5263743-7cf0-42fc-b164-25ea8315758a\t122101706\t412715057\n" +
                "a5263743-7cf0-42fc-b164-25ea8315758b\t122101706\t457014057841\n" +
                "a5263743-7cf0-42fc-b164-25ea8315758d\t103000648\t010229187\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0330\t083000108\t3160062351\n" +
                "a5263743-7cf0-42fc-b164-25ea8315758f\t062000019\t73010472\n" +
                "a5263743-7cf0-42fc-b164-25ea83157590\t083000137\t937190478\n" +
                "a5263743-7cf0-42fc-b164-25ea83157591\t011900445\t50273142\n" +
                "a5263743-7cf0-42fc-b164-25ea83157592\t121000248\t4123817025\n" +
                "a5263743-7cf0-42fc-b164-25ea83157593\t121000248\t4123817025\n" +
                "a5263743-7cf0-42fc-b164-25ea83157594\t021052053\t76786182\n" +
                "a5263743-7cf0-42fc-b164-25ea83157595\t021052053\t40939272\n" +
                "a5263743-7cf0-42fc-b164-25ea83157596\t121301015\t01089005\n" +
                "a5263743-7cf0-42fc-b164-25ea83157597\t073000228\t0007031934\n" +
                "a5263743-7cf0-42fc-b164-25ea83157598\t121000248\tNotPresent\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0329\t121000248\t4938721941\n" +
                "a5263743-7cf0-42fc-b164-25ea8315759b\t121000248\t4159548171\n" +
                "a5263743-7cf0-42fc-b164-25ea8315759c\t101101154\t0200005177\n" +
                "a5263743-7cf0-42fc-b164-25ea8315759d\t101000695\t9870969246\n" +
                "a5263743-7cf0-42fc-b164-25ea8315759e\t211170101\t1918024777\n" +
                "a5263743-7cf0-42fc-b164-25ea8315759f\t092900383\tDOR156041200221\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a0\t121000248\t73409999999999999\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a1\t091300285\t0910231\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a2\t091300285\t519486\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0328\t121000248\t20210000000000001\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a6\t051904634\t07020001\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a7\t064000020\t000113308258\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a8\t065400137\t645889593\n" +
                "a5263743-7cf0-42fc-b164-25ea831575a9\t065305436\t0018570607\n" +
                "a5263743-7cf0-42fc-b164-25ea831575ff\t123103729\t153395055608\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0456\t122235821\t158300057334\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0456\t122235821\t158300057334\n" +
                "a5263743-7cf0-42fc-b164-25ea83157601\t121000248\t4842702862\n" +
                "a5263743-7cf0-42fc-b264-25ea8325759d\t101000187\t145593011953\n" +
                "a5263743-7cf0-42fc-c164-25ea8315756a\t121000248\t4035301704\n" +
                "a5263743-7cf0-42fc-c164-25ea83157592\t031100209\t38968295\n" +
                "a5263743-7cf0-42fc-c164-25ea83157593\t031100209\t38968295\n" +
                "e0a70827-e95d-4684-b857-11ea7d0d6dc8\t043000096\t1013637558\n" +
                "e0a70827-e95d-4684-b857-11ea7d0d6dc9\t021052053\t33109164\n" +
                "e0a70827-e95d-4684-b857-11ea7d0d6dd0\t121000248\t19451234567891945\n" +
                "e0a70827-e95d-4684-b857-11ea7d0d6dd1\t072000326\t754037133\n" +
                "e0a70827-e95d-4684-b857-11ea7d0d6dd2\t121000248\t4089246128\n" +
                "e0a70827-e95d-4684-b857-11ea7d0d6dd3\t121000248\t4089246136\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0324\t021409169\t893000\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0326\t086500634\t9986520\n"+
                "4063bac7-4c8f-4811-8188-1852e71d0333\t021052053\t72561852\n"+
                "4063bac7-4c8f-4811-8188-1852e71d0327\t011900254\t00000385015954138\n" +
                "4063bac7-4c8f-4811-8188-1852e71d0332\t011900254\t000000108929";

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<PaymentTemplateBankAccount> currentPTBAs = Application.find(PaymentTemplateBankAccount.class);
        currentPTBAs= currentPTBAs.find(PaymentTemplateBankAccount.StatusCd().notEqualTo(BankAccountStatus.Inactive));
        for (String s : paymentTemplateBankAccounts.split("\n")) {
            String[] line = s.split("\t");
            String guid = line[0];
            String template = line[1];
            String ba = line[2];


            PaymentTemplateBankAccount ptba = Application.findById(PaymentTemplateBankAccount.class, SpcfUniqueId.createInstance(guid));
            assertEquals(template, ptba.getPaymentTemplate().getPaymentTemplateCd());
            assertEquals(ba, ptba.getBankAccount().getId().toString());

            currentPTBAs.remove(currentPTBAs.findEntity(PaymentTemplateBankAccount.Id().equalTo(SpcfUniqueId.createInstance(guid))));
        }
        assertEquals(0, currentPTBAs.size());

        for (String s : bankAccounts.split("\n")) {
            String[] line = s.split("\t");
            String guid = line[0];
            String routing = line[1];
            String account = line[2];

            BankAccount ba = Application.findById(BankAccount.class, SpcfUniqueId.createInstance(guid));
            assertEquals(routing, ba.getRoutingNumber());
            assertEquals(account, ba.getAccountNumber());
        }

        PayrollServices.rollbackUnitOfWork();

    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testMIWHBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-MW106-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2015, 2, 25);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany("125456789");

        DataLoadServices.setPSPDate(2015, 2, 27);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2015, 2, 28));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2015, 3, 19));
        OffloadBatch offloadBatch=null;
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MI-MW106-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("072000326", creditBankAccount.getRoutingNumber());
        assertEquals("754037133", creditBankAccount.getAccountNumber());
        offloadBatch=  moneyMovementTransaction.getOffloadBatch();

        DomainEntitySet<NACHAFile> files = offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        String fileName = files.getFirst().getFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));
        String[] expectedOutput = {"101 02100002197226160001503181325A094101JPMORGAN CHASE         INTUIT                         ",
                "5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY150320150320   1021000020000001",
                "622072000326754037133        0000009600TEST_0001      TEST_COMPANY_1          \\d{16}+",
                "705TXP\\*12-5456789\\*01100\\*150228\\*T\\*0000009600\\*\\*\\*\\*\\\\                                   \\d{11}+",
                "822000000200072000320000000000000000000096009118556001                         021000020000001",
                "5225INTUIT                     7700346619118556001CCDEFT TAX PY150320150320   1021000020000002",
                "627021000021911855633        0000009600911855633      INTUIT TAX              \\d{16}+",
                "822500000100021000020000000096000000000000009118556001                         021000020000002",
                "9000002000001000000030009300034000000009600000000009600                                       ",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"};

        Assert.assertEquals("Offload output sizes do not match up.", lines.length, expectedOutput.length);
        for (int i = 0; i < expectedOutput.length; i++) {
            String expected=expectedOutput[i];//.replaceAll("\\*","X");
            String actual=lines[i];//.replaceAll("\\*","X");
            Pattern pattern = Pattern.compile(expected);
            Matcher matcher = pattern.matcher(actual);

            assertTrue("Did not find expected output:\n" + expectedOutput[i] + "\nIn output:\n" + lines[i], matcher.matches());
        }

        PayrollServices.rollbackUnitOfWork();

    }


    /***
     * Tests the Bank Account change for FLDOR (FL-UCT6) payment templates.
     */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testFLDORBankAccountChange(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2014, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("FL-UCT6-PAYMENT", SpcfCalendar.createInstance(2014, 9, 1));

        DataLoadServices.setPSPDate(2015, 10, 01);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany("125456789");

        DataLoadServices.setPSPDate(2015,10,05);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2015,10,8));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2015,10, 13));

        DataLoadServices.setPSPDate(2016,01,15);
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("FL-UCT6-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();

        assertEquals("121000248", creditBankAccount.getRoutingNumber());
        assertEquals("4842702862", creditBankAccount.getAccountNumber());

        OffloadBatch offloadBatch = moneyMovementTransaction.getOffloadBatch();

        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "6221210002484842702862       0000036800TEST_0001      TEST_COMPANY_1          1";

        assertEquals("RECORDS are not the same", edr.getFirst().getRecordData(), expected);
    }

    @Test
    /***
     * Tests the Bank Account change for CAEDD (CA-PITSDI & CA-UIETT) payment templates.
     */
    public void testCAEDDBankAccountChange(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        // DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2015, 10, 01);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany("125456789");

        DataLoadServices.setPSPDate(2015,10,05);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2015,10,06));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2015,10, 06));

        PayrollServices.beginUnitOfWork();
        String expOutput = "622122235821158300057334     0000029200TEST_0001      TEST_COMPANY_1          1";
        validateCAEDDMMT(company, "CA-PITSDI-PAYMENT", expOutput);
        expOutput = "622122235821158300057334     0000091600TEST_0001      TEST_COMPANY_1          1";
        validateCAEDDMMT(company, "CA-UIETT-PAYMENT", expOutput);
    }

    private void validateCAEDDMMT(Company company, String paymentTemplate, String expOutput) {
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd(paymentTemplate).find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();

        assertEquals("122235821", creditBankAccount.getRoutingNumber());
        assertEquals("158300057334", creditBankAccount.getAccountNumber());

        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment).And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        assertEquals("RECORDS are not the same", edr.getFirst().getRecordData(), expOutput);
    }

    @Test
    public void testNY1MNBankAccountChange(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2015, 10, 01);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany("258963147");


        DataLoadServices.setPSPDate(2015,10,05);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2015,10,06));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2015,10, 06));

        OffloadBatch offloadBatch = null;

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NY-1MN-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();

        assertEquals("121000248", creditBankAccount.getRoutingNumber());
        assertEquals("4089246128", creditBankAccount.getAccountNumber());

        offloadBatch = moneyMovementTransaction.getOffloadBatch();

        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();

        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
              .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));
        

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "6221210002484089246128       0000081200TEST_0001      TEST_COMPANY_1          1";

        assertEquals("RECORDS are not the same", edr.getFirst().getRecordData(), expected);
    }

    @Test
    public void testNYMTA305BankAccountChange(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2015, 10, 01);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany("258963147");


        DataLoadServices.setPSPDate(2015,10,05);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2015,10,06));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2015,9, 22));

        OffloadBatch offloadBatch = null;

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NY-MTA305-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();

        assertEquals("121000248", creditBankAccount.getRoutingNumber());
        assertEquals("4089246136", creditBankAccount.getAccountNumber());

        offloadBatch = moneyMovementTransaction.getOffloadBatch();

        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();

        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                                     .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));


        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "6221210002484089246136       0000169200TEST_0001      TEST_COMPANY_1          1";

        assertEquals("RECORDS are not the same", edr.getFirst().getRecordData(), expected);
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testMIWHBankAccountChanges2_23() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MI-MW106-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2015, 2, 17);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany("125456789");


        DataLoadServices.setPSPDate(2015, 2, 18);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2015, 2, 19));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2015, 3, 19));
        OffloadBatch offloadBatch=null;
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MI-MW106-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("072000326", creditBankAccount.getRoutingNumber());
        assertEquals("754037133", creditBankAccount.getAccountNumber());
        offloadBatch=  moneyMovementTransaction.getOffloadBatch();

        DomainEntitySet<NACHAFile> files = offloadBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        String fileName = files.getFirst().getFileName();
        String output = DataLoadServices.readFile(fileName);

        String[] lines = output.split(System.getProperty("line.separator"));
        String[] expectedOutput = {"101 02100002197226160001503181325A094101JPMORGAN CHASE         INTUIT                         ",
                "5220TEST_COMPANY_1             TEST_00019118556001CCDEFT TAX PY150320150320   1021000020000001",
                "622072000326754037133        0000009600TEST_0001      TEST_COMPANY_1          \\d{16}+",
                "705TXP\\*12-5456789\\*01100\\*150228\\*T\\*0000009600\\*\\*\\*\\*\\\\                                   \\d{11}+",
                "822000000200072000320000000000000000000096009118556001                         021000020000001",
                "5225INTUIT                     7700346619118556001CCDEFT TAX PY150320150320   1021000020000002",
                "627021000021911855633        0000009600911855633      INTUIT TAX              \\d{16}+",
                "822500000100021000020000000096000000000000009118556001                         021000020000002",
                "9000002000001000000030009300034000000009600000000009600                                       ",
                "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999"};

        Assert.assertEquals("Offload output sizes do not match up.", lines.length, expectedOutput.length);
        for (int i = 0; i < expectedOutput.length; i++) {
            String expected=expectedOutput[i];//.replaceAll("\\*","X");
            String actual=lines[i];//.replaceAll("\\*","X");
            Pattern pattern = Pattern.compile(expected);
            Matcher matcher = pattern.matcher(actual);

            assertTrue("Did not find expected output:\n" + expectedOutput[i] + "\nIn output:\n" + lines[i], matcher.matches());
        }

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testMDDLLRBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2017, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MD-DLLR-PAYMENT", SpcfCalendar.createInstance(2017, 9, 1));

        DataLoadServices.setPSPDate(2018, 1, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2018, 2, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2018, 2, 21));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2018, 3, 12));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MD-DLLR-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("121000248", creditBankAccount.getRoutingNumber());
        assertEquals("4035301704", creditBankAccount.getAccountNumber());
        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "TXP*1224567890*130*180331*T*41200\\";

        assertEquals("TXP are not the same", edr.getFirst().getTxpRecordData(), expected);

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testNJNJ927PWHBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2017, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NJ-NJ927PWH-PAYMENT", SpcfCalendar.createInstance(2017, 9, 1));

        DataLoadServices.setPSPDate(2018, 2, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateAgencyTaxpayerId(company,"NJ-NJ927PWH-PAYMENT",company.getFedTaxId()+"/000");

        DataLoadServices.setPSPDate(2018, 3, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2018, 3, 21));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2018, 4, 12));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NJ-NJ927PWH-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("031100209", creditBankAccount.getRoutingNumber());
        assertEquals("38968295", creditBankAccount.getAccountNumber());
        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "TXP*B000000001000*01170*180324*T*13200*****TEST\\";

        assertEquals("TXP are not the same", edr.getFirst().getTxpRecordData(), expected);

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testNJNJ927PUIBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2017, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NJ-NJ927PUI-PAYMENT", SpcfCalendar.createInstance(2017, 9, 1));

        DataLoadServices.setPSPDate(2018, 2, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateAgencyTaxpayerId(company,"NJ-NJ927PUI-PAYMENT",company.getFedTaxId()+"/000");

        DataLoadServices.setPSPDate(2018, 3, 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2018, 3, 21));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2018, 4, 12));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("NJ-NJ927PUI-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("031100209", creditBankAccount.getRoutingNumber());
        assertEquals("38968295", creditBankAccount.getAccountNumber());
        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "TXP*B000000001000*13002*180331*T*572400*****TEST\\";

        assertEquals("TXP are not the same", edr.getFirst().getTxpRecordData(), expected);

        PayrollServices.rollbackUnitOfWork();

    }
    @Test
    public void testVTWHBankAccountChanges() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("VT-WH433-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));

        DataLoadServices.setPSPDate(2018, 2, 1);
        DataLoadServices.reinitialize();
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate=CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate("VT-C101-PAYMENT"));
        companyAgencyPaymentTemplate.setAgencyTaxpayerId("WHT10016186");
        PayrollServices.commitUnitOfWork();

        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.AddCompanyPaymentMethods.toString(), "VT-WH433-PAYMENT"});
        ComplianceToolkit.main(new String[]{ComplianceToolkit.ToolkitCommand.RecalculateCompanyPaymentMethodsEnabled.toString(), "VT-WH433-PAYMENT"});

        DataLoadServices.setPSPDate(2018, 2, 16);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2018, 2, 16));
        DataLoadServices.runOffloadTaxPayments(SpcfCalendar.createInstance(2018, 2, 16));
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("VT-WH433-PAYMENT").find());
        BankAccount creditBankAccount = moneyMovementTransaction.getFirstFinancialTransaction().getCreditBankAccount();
        assertEquals("221172186", creditBankAccount.getRoutingNumber());
        assertEquals("8877770634", creditBankAccount.getAccountNumber());
        assertEquals("People's United Bank", creditBankAccount.getBankName());
        Query<EntryDetailRecord> query = new Query<EntryDetailRecord>();
        query.Where(EntryDetailRecord.NACHABatchType().equalTo(NACHABatchType.TaxPayment)
                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Credit)));

        DomainEntitySet<EntryDetailRecord> edr = moneyMovementTransaction.getEntryDetailRecordCollection().find(query);

        String expected = "TXP*WHT12345678*01101*180228*T*0000019600\\";

        assertEquals("TXP are not the same", edr.getFirst().getTxpRecordData(), expected);

        PayrollServices.rollbackUnitOfWork();

    }


}

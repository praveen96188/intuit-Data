package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 16, 2008
 * Time: 11:08:01 AM
 */
public class AddQBDTOFX {

    private static DataLoader dataloader = new DataLoader();

    public static void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    public static void main(String[] args) {
        runBeforeEachTest();
        testFindCompanyTransmissions();
    }
    public static void testFindCompanyTransmissions() {
        testFindCompanyTransmissionsSecondary();
    }

    public static void testFindCompanyTransmissionsSecondary() {
        // Load Company
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        ProcessResult<Company> result = DataLoader.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()), company1.getCompanyId(), dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        assertEquals("Load Company", 0, result.getMessages().size());

        // Create a Source System Transmission

        SourceSystemTransmissionDTO sourceSystemTransmissionDTO;
        Long initialToken = 0L;
        for (int i = 1; i <= 3; i++) {
            PayrollServices.beginUnitOfWork();
            String transmissionId = SpcfUniqueId.createInstance(true).toString();
            sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
            sourceSystemTransmissionDTO.setRequestToken(initialToken + i);
            SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 11, 15+i, SpcfTimeZone.getLocalTimeZone());
            PSPDate.setPSPTime(testTime);
            PayrollServices.commitUnitOfWork();

            sourceSystemTransmissionDTO.setRequestDocument(OFX_REQUEST_DOC);
            sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
            sourceSystemTransmissionDTO.setFromSourceSystem(SourceSystemCode.QBOE);
            ProcessResult<SourceSystemTransmission> processResult = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBOE,
                    "123456", transmissionId, sourceSystemTransmissionDTO);
//            PayrollServices.commitUnitOfWork();

            // Check that transmission was successfully created
            assertSuccess("initializeSourceSystemTransmission", processResult);
            SourceSystemTransmission sourceSystemTransmission = processResult.getResult();
            PayrollServices.beginUnitOfWorkWithSecondary();
            SourceSystemTransmission savedSourceSystemTransmission = PayrollServices.entityFinderSecondary.findById(SourceSystemTransmission.class, sourceSystemTransmission.getId());
            PayrollServices.commitUnitOfWorkWithSecondary();

            // Finalize the Source System Transmission
//            PayrollServices.beginUnitOfWork();
            sourceSystemTransmissionDTO.setResponseToken(initialToken + i + 1);
            sourceSystemTransmissionDTO.setResponseDocument(OFX_RESPONSE_DOC);
            processResult = PayrollServices.transmissionManagerSecondary.finalizeTransmission(SourceSystemCode.QBOE,
                    "123456", transmissionId, sourceSystemTransmissionDTO);
//            PayrollServices.commitUnitOfWork();

            // Check that transmission was successfully finalized
            assertSuccess("finalizeSourceSystemTransmission", processResult);
            sourceSystemTransmission = processResult.getResult();
        }

        // Get all the transmissions
        PayrollServices.beginUnitOfWorkWithSecondary();
        DomainEntitySet<SourceSystemTransmission> transmissions = SourceSystemTransmission.findCompanyTransmissions(company1.getCompanyId(), result.getResult().getSourceSystemCd());
        assertEquals("Number of Transmissions:", 3, transmissions.size());

        // Get the last Transmission - Make sure the token is the last one
        Company company = Company.findCompany(company1.getCompanyId(), result.getResult().getSourceSystemCd());
        SourceSystemTransmission lastTransmission = SourceSystemTransmission.findLastTransmission(company);

        long lastTransmissionToken = SourceSystemTransmission.findLastTransmissionResponseToken(company);

        assertEquals("Last Transmission Response Token:", 4L, lastTransmission.getResponseToken());

        // Get the transmission duration
        SourceSystemTransmission transmission = PayrollServices.entityFinderSecondary.findById(SourceSystemTransmission.class, lastTransmission.getId());
        Long duration = transmission.getFinalizeDateTime().getTimeInMilliseconds() - transmission.getInitializeDateTime().getTimeInMilliseconds();
        assertTrue("Transmission Duration:", duration >= 0L);
        PayrollServices.rollbackUnitOfWorkWithSecondary();
    }

    public static void loadOFXTransmission() throws Exception {
            runBeforeEachTest();
            testFindCompanyTransmissions();
    }

    private static String OFX_REQUEST_DOC = "<OFX>\n"+
            "<SIGNONMSGSRQV1>\n"+
            "<SONRQ>\n"+
            "<DTCLIENT>20080718001754\n"+
            "<USERID>8574536\n"+
            "<USERPASS>test1234\n"+
            "<LANGUAGE>ENG\n"+
            "<APPVER>50.00.R.3/20804#pro\n"+
            "<APPID>QBWPRO\n"+
            "<I.QBFILENAME>C:\\Documents and Settings\\All Users\\Documents\\Intuit\\QuickBooks\\Company Files\\Joes Cool Co.QBW\n"+
            "<I.QBFILEID>c8e251053a984b3b9e107e8daa9bb640\n"+
            "<I.IPADDRESS>FileInfo:QB_data_engine_18:172.17.214.180#10180\n"+
            "<I.QBUSERNAME>Admin\n"+
            "</SONRQ>\n"+
            "</SIGNONMSGSRQV1>\n"+
            "<I.PAYROLLMSGSRQV1>\n"+
            "<I.PAYROLLUPDATERQ>\n"+
            "<TOKEN>1\n"+
            "<REJECTIFMISSING>Y\n"+
            "<I.PAYROLLTRNRQ>\n"+
            "<TRNUID>87536D20-79F5-1000-BB15-CB9C31AB0026\n"+
            "<I.PAYROLLRQ>\n"+
            "<I.PAYROLLRUN>\n"+
            "<I.DTPAYCHKS>20070810\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>1\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070810\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>2\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Bank of Money\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-927.69\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>2\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070810\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>Abe's Acct\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>0\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>$40.00\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>11122221111\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-40.00\n"+
            "</I.DDLINE>\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>333322222233\n"+
            "<ACCTTYPE>CHECKING\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-153.11\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.DDADVICE>\n"+
            "<I.DDAMT>$-0.00\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-927.69\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-40.00\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-153.11\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "</I.DDADVICE>\n"+
            "</I.PAYROLLRUN>\n"+
            "<I.PAYROLLRUN>\n"+
            "<I.DTPAYCHKS>20070816\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>3\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070816\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>2\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Bank of Money\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-8091.11\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.PAYCHK>\n"+
            "<I.PAYCHKID>4\n"+
            "<I.EMPID>0\n"+
            "<I.DTTX>20070816\n"+
            "<I.PAYCHKTYPE>PAYCHK\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.CLASS>^@~*\n"+
            "<I.ACCTNAME>Abe's Acct\n"+
            "<I.AMT>$0.00\n"+
            "<I.PAYCHKINFO>\n"+
            "<I.SICKACCRUED>^@~*\n"+
            "<I.VACACCRUED>^@~*\n"+
            "<I.PRORATE>N\n"+
            "<I.CHKNUM>TOPRINT\n"+
            "</I.PAYCHKINFO>\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.DTPAYPDBEGIN>20071117\n"+
            "<I.DTPAYPDEND>20071130\n"+
            "<I.MEMO>Direct Deposit\n"+
            "<I.CLEARED>0\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>$100.00\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>11122221111\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-100.00\n"+
            "</I.DDLINE>\n"+
            "<I.DDLINE>\n"+
            "<I.DDACCT>\n"+
            "<I.ACCTNAME>Abe's Bank\n"+
            "<I.AMT>^@~*\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>333322222233\n"+
            "<ACCTTYPE>CHECKING\n"+
            "</BANKACCTTO>\n"+
            "</I.DDACCT>\n"+
            "<I.PITEMID>0\n"+
            "<I.AMT>$-2012.44\n"+
            "</I.DDLINE>\n"+
            "</I.PAYCHK>\n"+
            "<I.DDADVICE>\n"+
            "<I.DDAMT>$-0.00\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-8091.11\n"+
            "<I.EMPNAME>Donovan McNabb\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-100.00\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "<I.DD>\n"+
            "<BANKACCTTO>\n"+
            "<BANKID>113003842\n"+
            "<ACCTID>0011992288\n"+
            "<ACCTTYPE>SAVINGS\n"+
            "</BANKACCTTO>\n"+
            "<I.EMPID>0\n"+
            "<I.AMT>$-2012.44\n"+
            "<I.EMPNAME>Abe Lincoln\n"+
            "<I.SSN>567-12-3456\n"+
            "</I.DD>\n"+
            "</I.DDADVICE>\n"+
            "</I.PAYROLLRUN>\n"+
            "</I.PAYROLLRQ>\n"+
            "</I.PAYROLLTRNRQ>\n"+
            "</I.PAYROLLUPDATERQ>\n"+
            "</I.PAYROLLMSGSRQV1>\n"+
            "</OFX>";

    private static String OFX_RESPONSE_DOC = "<OFX>\n"+
            "<SIGNONMSGSRSV1>\n"+
            "<SONRS>\n"+
            "<STATUS>\n"+
            "<CODE>0\n"+
            "<SEVERITY>INFO\n"+
            "</STATUS>\n"+
            "<DTSERVER>20080718001917\n"+
            "<LANGUAGE>ENG\n"+
            "</SONRS>\n"+
            "</SIGNONMSGSRSV1>\n"+
            "<I.PAYROLLMSGSRSV1>\n"+
            "<I.PAYROLLUPDATERS>\n"+
            "<TOKEN>2\n"+
            "<I.PAYROLLTXNEXTID>3\n"+
            "<I.PAYCHKNEXTID>5\n"+
            "<I.EMPNEXTID>1\n"+
            "<I.PITEMNEXTID>1\n"+
            "<I.PAYROLLTRNRS>\n"+
            "<TRNUID>87536D20-79F5-1000-BB15-CB9C31AB0026\n"+
            "<STATUS>\n"+
            "<CODE>0\n"+
            "<SEVERITY>INFO\n"+
            "</STATUS>\n"+
            "<I.PAYROLLRS>\n"+
            "<I.PAYROLLTX>\n"+
            "<I.PAYROLLTXID>1\n"+
            "<I.NAME>QuickBooks Payroll Service\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$-1126.29\n"+
            "<I.MEMO>Created by Payroll Services on 08/02/2007\n"+
            "<I.CLEARED>0\n"+
            "<I.DTTX>20070810\n"+
            "<I.REFNUM>^@~*\n"+
            "<I.PAYROLLTXTYPE>LIABCHK\n"+
            "<I.DTPAYPDEND>20070810\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$2.10\n"+
            "<I.MEMO>Fee for 2 direct deposit(s) at $1.05 each\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$3.00\n"+
            "<I.MEMO>Direct Deposit Transmission Fee\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>0.51\n"+
            "<I.MEMO>Sales Tax for null\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.AMT>$1120.80\n"+
            "<I.ISDD>Y\n"+
            "</I.TXLINE>\n"+
            "</I.PAYROLLTX>\n"+
            "</I.PAYROLLRS>\n"+
            "<I.PAYROLLRS>\n"+
            "<I.PAYROLLTX>\n"+
            "<I.PAYROLLTXID>2\n"+
            "<I.NAME>QuickBooks Payroll Service\n"+
            "<I.ACCTNAME>BofA\n"+
            "<I.AMT>$-10209.04\n"+
            "<I.MEMO>Created by Payroll Services on 08/02/2007\n"+
            "<I.CLEARED>0\n"+
            "<I.DTTX>20070816\n"+
            "<I.REFNUM>^@~*\n"+
            "<I.PAYROLLTXTYPE>LIABCHK\n"+
            "<I.DTPAYPDEND>20070816\n"+
            "<I.VOID>N\n"+
            "<I.ONSERVICE>Y\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$2.10\n"+
            "<I.MEMO>Fee for 2 direct deposit(s) at $1.05 each\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>$3.00\n"+
            "<I.MEMO>Direct Deposit Transmission Fee\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.ACCTNAME>Payroll Expenses\n"+
            "<I.AMT>0.51\n"+
            "<I.MEMO>Sales Tax for null\n"+
            "</I.TXLINE>\n"+
            "<I.TXLINE>\n"+
            "<I.AMT>$10203.55\n"+
            "<I.ISDD>Y\n"+
            "</I.TXLINE>\n"+
            "</I.PAYROLLTX>\n"+
            "</I.PAYROLLRS>\n"+
            "</I.PAYROLLTRNRS>\n"+
            "</I.PAYROLLUPDATERS>\n"+
            "</I.PAYROLLMSGSRSV1>\n"+
            "</OFX>";
}

package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 *
 * User: mvillani
 * Date: Sep 24, 2007
 * Time: 12:40:57 PM

 */
public class CompanyBankAccountDataLoader {


    public CompanyBankAccountDataLoader() {
//        String fileName = "resources/CompanyBankAccountAdd_load_dataset.sql";
//        PayrollServicesTest.loadDataset(fileName);
    }

    public static Company loadCompany() {
        DataLoader dataLoader = new DataLoader();

        // Create Company
        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);
        return company;

    }

    public static Company loadCompanyWithRealmId() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        // Create Company
        Company company = dataLoader.persistTestActiveCompany();
        company.setIAMRealmId(getNewIAMRealmId());
        dataLoader.persistTestCompanyService(company);
        return company;
    }

    public static Company loadCompany1WithRealmId() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        // Create Company
        Company company = dataLoader.persistTestActiveCompany1();
        company.setIAMRealmId(getNewIAMRealmId());
        dataLoader.persistTestCompanyService(company);
        return company;
    }

    public static Company loadQBDTCompany() {
        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        
        // Create Company
        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);
        return company;

    }



    public static CompanyBankAccount addTestCompanyBankAccount(Company pCompany) {

        CompanyBankAccountDTO companyBankAccountDTO = getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), companyBankAccountDTO, true, true);
        assertSuccess("addCompanyBankAccount", processResult);
        CompanyBankAccount pCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(pCompany, companyBankAccountDTO.getCompanyBankAccountID());

        return pCompanyBankAccount;
    }

    public static CompanyBankAccountDTO getTestCompanyBankAccountDTOWithNewBankAccount() {
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        BankAccountDTO bankAccountDTO = BankAccountDataLoader.generateBankAccountDTO();
        companyBankAccountDTO.setBankAccountDTO(bankAccountDTO);
        companyBankAccountDTO.setCompanyBankAccountID("123123");
        return companyBankAccountDTO;
    }

    public static CompanyBankAccountDTO getCompanyBankAccountDTO(CompanyBankAccount pCompanyBankAccount) {
        CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
        BankAccount bankAccount = pCompanyBankAccount.getBankAccount();
        companyBankAccountDTO.setBankAccountDTO(BankAccountDataLoader.getBankAccountDTOFromBankAccount(bankAccount));
        companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccount.getSourceBankAccountId());
        return companyBankAccountDTO;
    }

    public static BankAccount createBankAccount() {
        BankAccount bankAccount = BankAccountDataLoader.generateBankAccount();
        bankAccount = PayrollServicesTest.save(bankAccount);
        return bankAccount;
    }

    public static SourceSystemCode getSourceSystemCd() {
        return SourceSystemCode.QBOE;
    }

    public static String getSourceCompanyId() {
        return "123272727";
    }

    private static String getNewIAMRealmId() {
        return "9130352961219286";
    }

    public static void createCompanyWithCancelledDebits() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20091009000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        Company c = loadQBDTCompany();

        PayrollServices.commitUnitOfWork();

        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.setSourceBankAccountName("Drawing a Bank");

        PayrollServices.beginUnitOfWork();

        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(SourceSystemCode.QBDT, getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        
        PayrollServices.commitUnitOfWork();

        //offload the debits

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        try{
            PayrollServices.beginUnitOfWork();
            PSPDate.addDaysToPSPTime(5);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ProcessACHTransactions post = new ProcessACHTransactions();
            post.process(PSPDate.getPSPTime());
            PayrollServices.commitUnitOfWork();
        }
        finally{
            PayrollServices.rollbackUnitOfWork();
        }




        //incorrectly verify
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.verifyCompanyBankAccount(
                SourceSystemCode.QBDT,
                getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(),
                new SpcfMoney("532.34"),
                new SpcfMoney("1045.42"), false);
        PayrollServices.commitUnitOfWork();

        //again
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.verifyCompanyBankAccount(
                SourceSystemCode.QBDT,
                getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(),
                new SpcfMoney("532.34"),
                new SpcfMoney("1045.42"), false);
        PayrollServices.commitUnitOfWork();

        //again
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.verifyCompanyBankAccount(
                SourceSystemCode.QBDT,
                getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(),
                new SpcfMoney("532.34"),
                new SpcfMoney("1045.42"), false);
        PayrollServices.commitUnitOfWork();

        //again
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.verifyCompanyBankAccount(
                SourceSystemCode.QBDT,
                getSourceCompanyId(),
                companyBankAccountDTO.getCompanyBankAccountID(),
                new SpcfMoney("532.34"),
                new SpcfMoney("1045.42"), false);
        PayrollServices.commitUnitOfWork();
        

        //cancel
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTransactions;
        finTransactions =  FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT,
                    getSourceCompanyId(),
                    TransactionTypeCode.EmployerVerificationDebit,
                    TransactionStateCode.Created);

        for (FinancialTransaction finTx : finTransactions) {
            finTx.updateFinancialTransactionState(
                    TransactionStateCode.Cancelled);
        }

        PayrollServices.commitUnitOfWork();



    }

}

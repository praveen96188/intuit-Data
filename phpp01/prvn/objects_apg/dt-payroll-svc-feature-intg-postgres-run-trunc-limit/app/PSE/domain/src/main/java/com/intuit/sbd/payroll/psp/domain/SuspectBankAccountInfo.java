package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SuspectBankAccountInfo {
    private static SpcfLogger logger = Application.getLogger(PayrollRun.class);
    public String routingNumber;
    public String accountNumber;
    public BankAccountType accountTypeCd;
    public String accountOwnerName;
    public String companyId;

    @Override
    public boolean equals(Object o) {
        SuspectBankAccountInfo other = (SuspectBankAccountInfo) o;
        return (routingNumber.equals(other.routingNumber) && accountNumber.equals(other.accountNumber) && accountTypeCd.equals((other.accountTypeCd)));
    }

    public static Map<String, ArrayList<SuspectBankAccountInfo>> loadSuspectBankAccountMap() {
        //This query is different from other fraud queries in that it only looks for Fraud and FraudReview on hold codes.

        long start = System.currentTimeMillis();

        DomainEntitySet<FraudBankAccount> suspectTermBankAccounts = Application.find(FraudBankAccount.class);
        ArrayList<Object[]> suspectFraudBankAccounts = new ArrayList<Object[]>();
        ArrayList<Object[]> suspectPayeeFraudBankAccounts = new ArrayList<Object[]>();
        suspectFraudBankAccounts = Application.executeNamedQuery("sqlBankAccountsOfFraudCompaniesENC", new String[]{}, new String[]{});
        suspectPayeeFraudBankAccounts = Application.executeNamedQuery("sqlPayeeBankAccountsOfFraudCompaniesENC", new String[]{}, new String[]{});
        Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap = new HashMap<String, ArrayList<SuspectBankAccountInfo>>(suspectTermBankAccounts.size() + suspectFraudBankAccounts.size() + suspectPayeeFraudBankAccounts.size());
        addToSuspectBankAccountMap(suspectEmployeeBankAccountMap, suspectTermBankAccounts);
        addToSuspectBankAccountMap(suspectEmployeeBankAccountMap, suspectFraudBankAccounts, false);
        addToSuspectBankAccountMap(suspectEmployeeBankAccountMap, suspectPayeeFraudBankAccounts, true);

        long stop = System.currentTimeMillis();
        logger.info("fetched and cached termed/fraud review bank accounts -- total count: " + suspectEmployeeBankAccountMap.size() + " in " + (int) ((stop - start) / 1000) + " seconds");
        return suspectEmployeeBankAccountMap;
    }

    private static void addToSuspectBankAccountMap(Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap, ArrayList<Object[]> suspectTermBankAccounts, boolean isPayee) {
        for (Object[] bankAccounts : suspectTermBankAccounts) {
            String employeeOrPayeeId = (String) bankAccounts[0];
            String routingNumber = (String) bankAccounts[1];
            String accountNumber = (String) bankAccounts[2];        //Encrypted accountNumber value is read from DB
            accountNumber = EncryptionUtils.deterministicDecrypt(BankAccount.AccountNumberKeyName, accountNumber);

            String accountTypeCd = (String) bankAccounts[3];
            String key = String.format("%s:%s:%s", routingNumber, accountNumber, accountTypeCd);

            SuspectBankAccountInfo suspectBankAccountInfo = new SuspectBankAccountInfo();
            suspectBankAccountInfo.routingNumber = routingNumber;
            suspectBankAccountInfo.accountNumber = accountNumber;
            suspectBankAccountInfo.accountTypeCd = BankAccountType.valueOf(accountTypeCd);
            if (!isPayee) {
                Employee employee = Application.findById(Employee.class, SpcfUniqueId.createInstance(employeeOrPayeeId));
                suspectBankAccountInfo.accountOwnerName = employee.getFullName();
                suspectBankAccountInfo.companyId = employee.getCompany().getSourceCompanyId();
            } else {
                Payee payee = Application.findById(Payee.class, SpcfUniqueId.createInstance(employeeOrPayeeId));
                suspectBankAccountInfo.accountOwnerName = payee.getName();
                suspectBankAccountInfo.companyId = payee.getCompany().getSourceCompanyId();
            }

            ArrayList<SuspectBankAccountInfo> suspectAccounts = suspectEmployeeBankAccountMap.get(key);
            if (suspectAccounts == null)
                suspectAccounts = new ArrayList<SuspectBankAccountInfo>(1);

            if (!suspectAccounts.contains(suspectBankAccountInfo))
                suspectAccounts.add(suspectBankAccountInfo);

            suspectEmployeeBankAccountMap.put(key, suspectAccounts);
        }
    }

    private static void addToSuspectBankAccountMap(Map<String, ArrayList<SuspectBankAccountInfo>> suspectEmployeeBankAccountMap, DomainEntitySet<FraudBankAccount> suspectTermBankAccounts) {
        for (FraudBankAccount fraudBankAccount : suspectTermBankAccounts) {
            String key = String.format("%s:%s:%s", fraudBankAccount.getRoutingNumber(), fraudBankAccount.getAccountNumber(), fraudBankAccount.getAccountTypeCd().name());

            SuspectBankAccountInfo suspectBankAccountInfo = new SuspectBankAccountInfo();
            suspectBankAccountInfo.routingNumber = fraudBankAccount.getRoutingNumber();
            suspectBankAccountInfo.accountNumber = fraudBankAccount.getAccountNumber();
            suspectBankAccountInfo.accountTypeCd = fraudBankAccount.getAccountTypeCd();
            suspectBankAccountInfo.accountOwnerName = fraudBankAccount.getBankAccountOwnerName();
            suspectBankAccountInfo.companyId = fraudBankAccount.getCompany().getSourceCompanyId();

            ArrayList<SuspectBankAccountInfo> suspectAccounts = suspectEmployeeBankAccountMap.get(key);
            if (suspectAccounts == null)
                suspectAccounts = new ArrayList<SuspectBankAccountInfo>(1);

            if (!suspectAccounts.contains(suspectBankAccountInfo))
                suspectAccounts.add(suspectBankAccountInfo);

            suspectEmployeeBankAccountMap.put(key, suspectAccounts);
        }
    }

}


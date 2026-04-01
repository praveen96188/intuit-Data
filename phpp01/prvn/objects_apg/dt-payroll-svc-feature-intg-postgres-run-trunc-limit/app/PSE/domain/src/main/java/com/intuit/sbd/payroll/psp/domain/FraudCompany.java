package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import java.util.List;

/**
 * Hand-written business logic
 */
public class FraudCompany extends BaseFraudCompany {
    public static String FedTaxIdKeyName="FCompany_FedTaxId";

    /**
     * Default constructor.
     */
    public FraudCompany() {
        super();
    }

    public FraudCompany(Company pCompany) {
        super();
        
        if (pCompany != null) {
            this.setCompany(pCompany);
            this.setFedTaxId(pCompany.getFedTaxId());
            this.setLegalName(pCompany.getLegalName());
            this.setDbaName(pCompany.getDbaName());
            this.setNotificationEmail(pCompany.getNotificationEmail());
            if (pCompany.getQuickbooksInfo() != null) {
                this.setLicenseNumber(pCompany.getQuickbooksInfo().getLicenseNumber());
            }
        }
    }

    public static boolean exists(Company pCompany) {
        return findFraudCompany(pCompany) != null;
    }

    public static FraudCompany findFraudCompany(Company pCompany) {
        Criterion<FraudCompany> where =  FraudCompany.Company().equalTo(pCompany);
        List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(FraudCompany.FedTaxIdKeyName,pCompany.getFedTaxId());
        where = where.And(FraudCompany.FedTaxIdEnc().in(fedTaxIdEncList))
                            .And(FraudCompany.LegalName().equalTo(pCompany.getLegalName()))
                            .And(FraudCompany.DbaName().equalTo(pCompany.getDbaName()))
                            .And(FraudCompany.NotificationEmail().equalTo(pCompany.getNotificationEmail()));

        if (pCompany.getQuickbooksInfo() != null && pCompany.getQuickbooksInfo().getLicenseNumber() != null ) {
            where = where.And(FraudCompany.LicenseNumber().equalTo(pCompany.getQuickbooksInfo().getLicenseNumber()));
        } else {
            where = where.And(FraudCompany.LicenseNumber().isNull());
        }

        DomainEntitySet<FraudCompany> fraudCompanies = Application.find(FraudCompany.class, where);

        return fraudCompanies.isEmpty() ? null : fraudCompanies.get(0);
    }
    public static void addFraudRecords(Company pCompany) {
        addFraudRecords(pCompany, true);
    }

    public static void addFraudRecords(Company pCompany, boolean addEmployeeBankAccounts) {
        if (!FraudCompany.exists(pCompany)) {
            FraudCompany fraudCompany = new FraudCompany(pCompany);
            Application.save(fraudCompany);
        }

        for (CompanyBankAccount companyBankAccount : pCompany.getCompanyBankAccountCollection()) {
            if (FraudBankAccount.findFraudBankAccount(pCompany, companyBankAccount) == null) {
                FraudBankAccount fraudBankAccount = new FraudBankAccount(pCompany, companyBankAccount);
                Application.save(fraudBankAccount);
            }
        }

        if (addEmployeeBankAccounts) {
            for (Employee employee : Employee.findEmployees(pCompany)) {
                for (EmployeeBankAccount employeeBankAccount : employee.getEmployeeBankAccountCollection()) {
                    if (FraudBankAccount.findFraudBankAccount(pCompany, employeeBankAccount) == null) {
                        FraudBankAccount fraudBankAccount = new FraudBankAccount(pCompany, employeeBankAccount);
                        Application.save(fraudBankAccount);
                    }
                }
            }
        }

        if (pCompany.getLegalAddress() != null && !FraudAddress.exists(pCompany, pCompany.getLegalAddress())) {
            FraudAddress fraudAddress = new FraudAddress(pCompany, pCompany.getLegalAddress());
            Application.save(fraudAddress);
        }

        if (pCompany.getMailingAddress() != null && !FraudAddress.exists(pCompany, pCompany.getMailingAddress())) {
            FraudAddress fraudAddress = new FraudAddress(pCompany, pCompany.getMailingAddress());
            Application.save(fraudAddress);
        }

        for (Contact contact : pCompany.getContactCollection()) {
            if (!FraudContact.exists(pCompany, contact)) {
                FraudContact fraudContact = new FraudContact(pCompany, contact);
                Application.save(fraudContact);
            }
        }
    }

    public static void removeFraudRecords(Company pCompany) {
        for (FraudCompany fraudCompany : pCompany.getFraudCompanyCollection()) {
            Application.delete(fraudCompany);
        }

        for (FraudBankAccount fraudBankAccount : pCompany.getFraudBankAccountSet()) {
            Application.delete(fraudBankAccount);
        }

        for (FraudAddress fraudAddress : pCompany.getFraudAddressCollection()) {
            Application.delete(fraudAddress);
        }

        for (FraudContact fraudContact : pCompany.getFraudContactCollection()) {
            Application.delete(fraudContact);
        }
    }

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }
}

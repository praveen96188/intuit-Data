package com.intuit.sbd.payroll.psp.batchjobs.Iop;

import com.intuit.onlinepayroll.webservices.v1.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Random;

/**
 * @author Jeff Jones
 */
public class IOPResponseCreator {

    private static Random random = new Random();
    private static SpcfLogger logger = PayrollServices.getLogger(IOPResponseCreator.class);

    public static PayrollCompanyModel createPayrollCompanyModel(int pCompanyId, int pEmployees, int pPaychecks) {
        PayrollCompanyModel payrollCompanyModel = new PayrollCompanyModel();

        payrollCompanyModel.setCompany(createCompanyModel(pCompanyId));

        for (int eIndex = 0; eIndex < pEmployees; eIndex++) {
            payrollCompanyModel.getEmployees().add(createEmployeeModel(eIndex, false));
            for (int pIndex = 0; pIndex < pPaychecks; pIndex++) {
                payrollCompanyModel.getPaychecks().add(createPaycheckModel(eIndex, pIndex));
            }
        }

        return payrollCompanyModel;
    }

    /**
     * Creates a company with payees a companies with a specified number of payments
     * @param pCompanyId The company id
     * @param pPayees The number of company payees
     * @param pPayments The number of payments
     * @return A ContractorPaymentCompanyModel with payees and payments
     */
    public static ContractorPaymentCompanyModel createContractorPaymentCompanyModel(int pCompanyId, int pPayees, int pPayments) {
        return createContractorPaymentCompanyModel(pCompanyId, pPayees, 0, pPayments);
    }

    /**
     * Creates a company with payees a companies with a specified number of payments
     * @param pCompanyId The company id
     * @param pCompanyPayees The number of company payees
     * @param pIndividualPayees The number of individual/person payees
     * @param pPayments The number of payments
     * @return A ContractorPaymentCompanyModel with payees and payments
     */
    public static ContractorPaymentCompanyModel createContractorPaymentCompanyModel(int pCompanyId, int pCompanyPayees, int pIndividualPayees, int pPayments) {
        ContractorPaymentCompanyModel contractorPaymentCompanyModel = new ContractorPaymentCompanyModel();

        contractorPaymentCompanyModel.setCompany(createCompanyModel(pCompanyId));

        for (int eIndex = 0; eIndex < pCompanyPayees; eIndex++) {
            contractorPaymentCompanyModel.getContractors().add(createContractorModel(eIndex, false, true, true));
            for (int pIndex = 0; pIndex < pPayments; pIndex++) {
                contractorPaymentCompanyModel.getContractorPayments().add(createContractorPaymentData(eIndex, pIndex));
            }
        }

        for (int eIndex = 0; eIndex < pIndividualPayees; eIndex++) {
            contractorPaymentCompanyModel.getContractors().add(createContractorModel(eIndex, false, false, true));
            for (int pIndex = 0; pIndex < pPayments; pIndex++) {
                contractorPaymentCompanyModel.getContractorPayments().add(createContractorPaymentData(eIndex, pIndex));
            }
        }

        return contractorPaymentCompanyModel;
    }

    private static CompanyModel createCompanyModel(int pCompanyId) {
        CompanyModel companyModel = new CompanyModel();

        companyModel.setId(pCompanyId);
        companyModel.setBusinessName("IOP Business Name");
        companyModel.setBusinessAddress(createAddressModel());
        companyModel.setPrimaryContact(createPrimaryContactModel());
        companyModel.setCompanyTaxSetup(createCompanyTaxSetupModel());
        companyModel.setDdAccount(createBankAccountModel());
        companyModel.setSignupDate(createXMLGregorianCalendar(2010, 12, 15, 8, 0, 0, 0));

        return companyModel;
    }

    private static AddressModel createAddressModel() {
        AddressModel addressModel = new AddressModel();

        addressModel.setId(1);
        addressModel.setAddress1("123 55th Street");
        addressModel.setCity("San Bernardino");
        addressModel.setState("CA");
        addressModel.setZip("92405");

        return addressModel;
    }

    private static PrimaryContactModel createPrimaryContactModel() {
        PrimaryContactModel primaryContactModel = new PrimaryContactModel();

        primaryContactModel.setId(1);
        primaryContactModel.setFirstName("Primary First");
        primaryContactModel.setLastName("Primary Last");
        primaryContactModel.setEMailAddress("test@intuit.com");
        primaryContactModel.getPhones().add(createPhone());

        return primaryContactModel;
    }

    private static Phone createPhone() {
        Phone phone = new Phone();

        phone.setPhoneNumber("000-000-0000");
        phone.setPhoneType(PhoneType.WORK);

        return phone;
    }

    private static CompanyTaxSetupModel createCompanyTaxSetupModel() {
        CompanyTaxSetupModel companyTaxSetupModel = new CompanyTaxSetupModel();

        companyTaxSetupModel.setFilingName("IOP Filing Name");
        companyTaxSetupModel.setFilingAddress(createAddressModel());
        companyTaxSetupModel.setFederalEIN("123459999");

        return companyTaxSetupModel;
    }

    private static BankAccountModel createBankAccountModel() {
        BankAccountModel bankAccountModel = new BankAccountModel();


        bankAccountModel.setAccountNumber("000000001");
        bankAccountModel.setBankRoutingNumber("121000358");
        bankAccountModel.setBankName("IOP Bank");
        bankAccountModel.setBankAccountType(BankAccountType.CHECKING);

        return bankAccountModel;
    }

    private static EmployeeModel createEmployeeModel(int pId, boolean pPaycheckEmp) {
        EmployeeModel employeeModel = new EmployeeModel();

        employeeModel.setId(pId);

        if (pPaycheckEmp)
            return employeeModel;

        employeeModel.setFirstName("Emp First");
        employeeModel.setLastName("Emp Last");
        employeeModel.setSocialSecurityNumber("123451111");
        employeeModel.setHireDate(createDateModel());
        employeeModel.setGender(Gender.FEMALE);
        employeeModel.setDirectDepositAccount1(createBankAccountModel());        
        employeeModel.setDirectDepositAccount2(createBankAccountModel());

        return employeeModel;
    }

    private static ContractorModel createContractorModel(int pId, boolean pPayeeEmp, boolean pIsCompany, boolean pIsFullObject) {
        ContractorModel contractorModel = new ContractorModel();

        contractorModel.setId(pId);

        if (pPayeeEmp)
            return contractorModel;

        if (pIsFullObject) {
            // Do not simulate the malformed ContractorModel from IOP
            if (pIsCompany) {
                contractorModel.setBusinessName("Contractor Company Name");
                contractorModel.setTIN("987654321");
                contractorModel.setType(1);
            } else {
                contractorModel.setFirstName("First");
                contractorModel.setLastName("Last");
                contractorModel.setSocialSecurityNumber("123456789");
                contractorModel.setType(2);
            }

            UserModel userModel = new UserModel();
            userModel.setEmailAddress("none@none.com");
            contractorModel.setContact(userModel);

            Phone phone = new Phone();
            phone.setPhoneNumber("5551234567");
            userModel.getPhones().add(phone);

            //contractorModel.setIs1099(true);

            AddressModel addressModel = new AddressModel();
            addressModel.setAddress1("123 Elm St");
            addressModel.setCity("Reno");
            addressModel.setState("NV");
            addressModel.setZip("89511");

            contractorModel.setAddressModel(addressModel);

            BankAccountModel bankAccountModel = new BankAccountModel();
            bankAccountModel.setAccountNumber("000000001");
            bankAccountModel.setBankRoutingNumber("121000358");
            bankAccountModel.setBankName("IOP Bank");
            bankAccountModel.setBankAccountType(BankAccountType.CHECKING);

            contractorModel.setDirectDepositAccount1(bankAccountModel);
        }
        
        return contractorModel;
    }

    private static DateModel createDateModel() {
        DateModel dateModel = new DateModel();

        dateModel.setMonth(2);
        dateModel.setDay(1);
        dateModel.setYear(1980);

        return dateModel;
    }

    public static PaycheckModel createPaycheckModel(int pEmployeeId, int pPaycheckId,XMLGregorianCalendar checkDate,XMLGregorianCalendar periodStartDate,XMLGregorianCalendar periodEndDate) {
        if (checkDate == null) {
            checkDate = createXMLGregorianCalendar(2011, 3, 4, 0, 0, 0, 0);
        }
        if (periodStartDate == null) {
            periodStartDate = createXMLGregorianCalendar(2011, 2, 21, 0, 0, 0, 0);
        }
        if (periodEndDate == null) {
            periodEndDate = createXMLGregorianCalendar(2011, 3, 4, 0, 0, 0, 0);
        }
        PaycheckModel paycheckModel = new PaycheckModel();
        paycheckModel.setId(getIntegerRandomNumber());
        paycheckModel.setEmployee(createEmployeeModel(pEmployeeId, true));
        paycheckModel.setCheckDate(checkDate);
        paycheckModel.setPeriodStartDate(periodStartDate);
        paycheckModel.setPeriodEndDate(periodEndDate);
        paycheckModel.setNetCheckAmount(BigDecimal.valueOf(1024.00));
        paycheckModel.setGrossAmount(BigDecimal.valueOf(2048.00));
        paycheckModel.setDdAmount(BigDecimal.valueOf(999.00));
        paycheckModel.setDdAmount2(BigDecimal.valueOf(25.00));
        paycheckModel.setIsDeleted(false);

        return paycheckModel;
    }
    public static PaycheckModel createPaycheckModel(int pEmployeeId, int pPaycheckId) {
        return createPaycheckModel(pEmployeeId,pPaycheckId,null,null,null);
    }

    public static ContractorPaymentModel createContractorPaymentData(int pEmployeeId, int pPaycheckId) {
        ContractorPaymentModel contractorPaymentModel = new ContractorPaymentModel();

        contractorPaymentModel.setCheckDate(createXMLGregorianCalendar(2011, 3, 4, 0, 0, 0, 0));
        contractorPaymentModel.setGrossAmount(new BigDecimal("100.0000"));
        contractorPaymentModel.setCheckAmount(new BigDecimal("100.0000"));
        contractorPaymentModel.setDdAmount(new BigDecimal("100.0000"));
        //contractorPaymentModel.setDdAmount2(new BigDecimal(100));
        contractorPaymentModel.setId(getIntegerRandomNumber());
        contractorPaymentModel.setContractor(createContractorModel(pEmployeeId, false, true, false));
        contractorPaymentModel.setMemo("Memo");

        return contractorPaymentModel;
    }

    public static XMLGregorianCalendar createXMLGregorianCalendar(int pYear,
                                                            int pMonth,
                                                            int pDay,
                                                            int pHour,
                                                            int pMinute,
                                                            int pSecond,
                                                            int pMillisecond) {
        XMLGregorianCalendar xmlGC = null;

        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            xmlGC = df.newXMLGregorianCalendar();

            xmlGC.setYear(pYear);
            xmlGC.setMonth(pMonth);
            xmlGC.setDay(pDay);
            xmlGC.setHour(pHour);
            xmlGC.setMinute(pMinute);
            xmlGC.setSecond(pSecond);
            xmlGC.setMillisecond(pMillisecond);
        } catch (DatatypeConfigurationException e) {
            logger.error("Error creating XMLGregorianCalendar object. ", e);
        }

        return xmlGC;
    }

    private static long getIntegerRandomNumber(){
        return random.nextInt(Integer.MAX_VALUE);
    }
}

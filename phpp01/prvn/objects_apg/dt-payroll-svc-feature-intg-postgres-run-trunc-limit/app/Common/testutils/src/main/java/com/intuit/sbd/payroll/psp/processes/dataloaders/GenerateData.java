package com.intuit.sbd.payroll.psp.processes.dataloaders;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * User: mvillani
 * Date: Oct 8, 2007
 * Time: 12:07:29 PM
 */
public class GenerateData {

    public static Collection<Employee> generateEmployees(Company pCompany, int pNumber) {
        Collection<Employee> employees = new ArrayList<Employee>();
        for (int i = 1; i <= pNumber; i++) {
            String sourceEmployeeId = "Emp" + i;
            EmployeeDTO employee = getEmployee(sourceEmployeeId);
            ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(
                    pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), employee);
            PayrollServicesTest.assertSuccess("addEmployee", procResult);
            employees.add(procResult.getResult());
        }
        return employees;
    }

    public static Collection<Employee> generate123123123Employees(Company pCompany, int pNumber) {
        Collection<Employee> employees = new ArrayList<Employee>();
        for (int i = 1; i <= pNumber; i++) {
            String sourceEmployeeId = "Emp123123123" + i;
            EmployeeDTO employee = getEmployee(sourceEmployeeId);
            ProcessResult<Employee> procResult = PayrollServices.employeeManager.addEmployee(
                    pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), employee);
            PayrollServicesTest.assertSuccess("addEmployee", procResult);
            employees.add(procResult.getResult());
        }
        return employees;
    }

    public static Collection<EmployeeBankAccount> generateEmployeeBankAccounts(Collection<Employee> pEmployees, int pNumberOfAccounts, String pStatusCd) {
        Collection<EmployeeBankAccount> employeeBankAccounts = new ArrayList();
        for (Employee employee : pEmployees) {
            for (int i = 1; i <= pNumberOfAccounts; i++) {
                String sourceEmployeeBankAccountId = employee.getSourceEmployeeId() + "Acct" + i;
                EmployeeBankAccountDTO employeeBankAccountDTO = getEmployeeBankAccountDTO(sourceEmployeeBankAccountId);
                ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employee.getSourceEmployeeId(), employeeBankAccountDTO);
                assertSuccess("addEmployeeBankAccount", processResult);
                employeeBankAccounts.add(processResult.getResult());

            }
        }
        return employeeBankAccounts;
    }

    public static EmployeeDTO getEmployee(String pSourceEmployeeId) {

        EmployeeDTO employee = new EmployeeDTO();
        employee.setFirstName(pSourceEmployeeId);
        employee.setLastName(pSourceEmployeeId + "LastName");
        employee.setSocialSecurityNumber("111223333");
        employee.setEmployeeId(pSourceEmployeeId);

        return employee;
    }

    public static EmployeeBankAccountDTO getEmployeeBankAccountDTO(String pSourceEmployeeBankAccountId) {

        EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
        employeeBankAccountDTO.setEmployeeBankAccountId(pSourceEmployeeBankAccountId);
        employeeBankAccountDTO.setBankAccount(BankAccountDataLoader.generateBankAccountDTO());
        return employeeBankAccountDTO;
    }

    public static PayeeBankAccountDTO getPayeeBankAccountDTO(String pSourcePayeeBankAccountId) {

        PayeeBankAccountDTO payeeBankAccountDTO = new PayeeBankAccountDTO();
        payeeBankAccountDTO.setPayeeBankAccountId(pSourcePayeeBankAccountId);
        payeeBankAccountDTO.setBankAccount(BankAccountDataLoader.generateBankAccountDTO());
        return payeeBankAccountDTO;
    }

    public static Collection<Employee> getEmployeeCollection(DomainEntitySet<Employee> pEmployees) {
        ArrayList<Employee> employees = new ArrayList<Employee>();
        for (int i = 0; i < pEmployees.size(); i++) {
            employees.add(pEmployees.get(i));
        }
        return employees;
    }

    public static PayeeDTO getTestPayee() {
        PayeeDTO payee = new PayeeDTO();
        payee.setSourcePayeeId("TESTADDPAYEE");
        payee.setName("Add Payee Core Test");
        payee.setPhone("775-227-7227");
        payee.setTaxId("123456789");

        AddressDTO mailingAddress = new AddressDTO();
        mailingAddress.setAddressLine1("123 High Country Rd");
        mailingAddress.setCity("Reno");
        mailingAddress.setState("NV");
        mailingAddress.setZipCode("89502");
        mailingAddress.setCountry("USA");
        payee.setMailingAddress(mailingAddress);

        return payee;
    }


    public static Collection<Payee> generatePayees(Company pCompany, int pNumber) {
        Collection<Payee> payees = new ArrayList<Payee>();
        for (int i = 1; i <= pNumber; i++) {
            String sourcePayeeId = "Payee" + i;
            PayeeDTO payee = getPayee(sourcePayeeId);
            ProcessResult<Payee> procResult = PayrollServices.billPaymentManager.addOrUpdatePayee(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payee);
            PayrollServicesTest.assertSuccess("addPayee", procResult);
            payees.add(procResult.getResult());
        }
        return payees;
    }

    public static PayeeDTO getPayee(String pSourcePayeeId) {

        PayeeDTO payee = new PayeeDTO();
        payee.setName(pSourcePayeeId + " Name.");
        payee.setEmail(pSourcePayeeId + "@hotmail.com");
        payee.setSourcePayeeId(pSourcePayeeId);
        payee.setPhone("773-222-2727");

        return payee;
    }

    public static PayeeDTO getPayee_PayBills(String pSourcePayeeId) {

        PayeeDTO payee;
        payee = getPayee(pSourcePayeeId);
        payee.setAccountNumber("VendorAccount" + pSourcePayeeId);

        return payee;
    }

    public static Collection<BillPaymentSplitDTO> generateBillPaymentSplits(int pNumberOfSplits) {
        ArrayList<BillPaymentSplitDTO> billPaymentSplits = new ArrayList<BillPaymentSplitDTO>();
        for (int i = 0; i < pNumberOfSplits; i++) {
            BillPaymentSplitDTO billPaymentSplitDTO = new BillPaymentSplitDTO();
            billPaymentSplitDTO.setPayeeBankAccount(GenerateData.getPayeeBankAccountDTO(UUID.randomUUID().toString()));
            billPaymentSplitDTO.setAmount(new BigDecimal(Math.random()*200  + 0.01).setScale(2, RoundingMode.FLOOR));
            billPaymentSplitDTO.setBillPaymentSplitId(UUID.randomUUID().toString());
            billPaymentSplits.add(billPaymentSplitDTO);

        }
       return billPaymentSplits;
    }

    public static BillPaymentDTO generateBillPayment(String pSourcePayeeId, DateDTO pDepositDate, int pNumberOfSplits) {
        return generateBillPayment(pSourcePayeeId, pDepositDate, pNumberOfSplits,SpcfUniqueId.generateRandomUniqueIdString(),
                false,null);
    }

    public static BillPaymentDTO generateBillPayment_PayBills(String pSourcePayeeId, DateDTO pDepositDate, int pNumberOfSplits,
                                                              String pReferenceNumber, String pMemo) {
        return generateBillPayment_PayBills(pSourcePayeeId, pDepositDate, pNumberOfSplits,pReferenceNumber, pMemo,
                SpcfUniqueId.generateRandomUniqueIdString());
    }

    public static BillPaymentDTO generateBillPayment(String pSourcePayeeId, DateDTO pDepositDate, int pNumberOfSplits, String sourcePaymentId,
        boolean payBill,String pReferenceNumber) {

        BillPaymentDTO billPaymentDTO = new BillPaymentDTO();
        billPaymentDTO.setPayeeDTO(getPayee(pSourcePayeeId));
        Collection<BillPaymentSplitDTO> billPaymentSplits = GenerateData.generateBillPaymentSplits(pNumberOfSplits);
        billPaymentDTO.setPaymentTransactions(billPaymentSplits);
        SpcfMoney amount = (new SpcfMoney("0.0"));
        for (BillPaymentSplitDTO bps:billPaymentSplits) {
          amount = new SpcfMoney(amount.add(new SpcfMoney(bps.getAmount().toString())));

          if(payBill)
            bps.setReferenceNumber(pReferenceNumber);  
        }
        billPaymentDTO.setAmount(amount);
        billPaymentDTO.setDepositDate(pDepositDate);
        billPaymentDTO.setBillPaymentId(sourcePaymentId);
        return billPaymentDTO;
    }

    public static BillPaymentDTO generateBillPayment_PayBills(String pSourcePayeeId, DateDTO pDepositDate, int pNumberOfSplits,
                                                              String pReferenceNumber, String pMemo,
                                                              String sourcePaymentId) {
        BillPaymentDTO billPaymentDTO;
        billPaymentDTO = generateBillPayment(pSourcePayeeId, pDepositDate, pNumberOfSplits,
                sourcePaymentId,true,pReferenceNumber);

        billPaymentDTO.setMemo(pMemo);
        billPaymentDTO.setTransactionType(BillPaymentTransactionType.PayBills);
        billPaymentDTO.setPayeeDTO(getPayee_PayBills(pSourcePayeeId));
        return billPaymentDTO;
    }
}

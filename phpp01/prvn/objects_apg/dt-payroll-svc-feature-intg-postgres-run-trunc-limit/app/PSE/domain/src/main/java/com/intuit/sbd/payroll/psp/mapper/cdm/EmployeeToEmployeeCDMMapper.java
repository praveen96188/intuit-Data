package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.employee.model.EmployeeCDM;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.EmploymentStatusCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.EmployeeToEmployeeCDMMapper")
public class EmployeeToEmployeeCDMMapper extends BeanMapper<Employee,EmployeeCDM> {

    @Override
    public EmployeeCDM mapToTarget(Employee employee, Class<EmployeeCDM> target) {
        if(Objects.isNull(employee)){
            return null;
        }

        EmployeeCDM employeeCDM = new EmployeeCDM();
        SpcfUniqueId id= employee.getId();
        if(Objects.nonNull(id)) {
            employeeCDM.setId(id.toString());
        }
        employeeCDM.setIusAuthId(employee.getUserAuthId());
        employeeCDM.setConsumerFinanceRealm(employee.getConsumerRealmId());
        employeeCDM.setConsumerRealmId(employee.getConsumerRealmId());
        employeeCDM.setCreated(SpcfCalendar.toDateTime(employee.getCreatedDate()));
        employeeCDM.setUpdated(SpcfCalendar.toDateTime(employee.getModifiedDate()));
        employeeCDM.setEntityVersion(String.valueOf(employee.getVersion()));
        employeeCDM.setFirstName(employee.getFirstName());
        employeeCDM.setMiddleName(employee.getMiddleName());
        employeeCDM.setLastName(employee.getLastName());
        employeeCDM.setGender(Objects.isNull(employee.getGenderCd())? null : employee.getGenderCd().name());
        if (Objects.nonNull(employee.getBirthDate()))
            employeeCDM.setBirthDate(SpcfCalendar.createInstance(
                    employee.getBirthDate().getYear(),
                    employee.getBirthDate().getMonth(),
                    employee.getBirthDate().getDay(),
                    SpcfTimeZone.getLocalTimeZone()).toLocalDate());
        employeeCDM.setTaxId(employee.getTaxId());
        employeeCDM.setHomeAddress(getMapper().mapToTarget(employee.getMailingAddress(), AddressSubCDM.class));

        if(!StringUtils.isBlank(employee.getWorkState())){
            Address workAddress = new Address();
            workAddress.setState(employee.getWorkState());
            employeeCDM.setWorkAddress(getMapper().mapToTarget(workAddress, AddressSubCDM.class));
        }
        employeeCDM.setHireDate(SpcfCalendar.toLocalDate(employee.getHireDate()));
        employeeCDM.setTerminationDate(SpcfCalendar.toLocalDate(employee.getTerminationDate()));
        employeeCDM.setRehireDate(SpcfCalendar.toLocalDate(employee.getReHireDate()));
        employeeCDM.setHomePhone(employee.getPhone());
        employeeCDM.setBusinessEmail(employee.getEmail());
        employeeCDM.setBankAccounts(getBankAccounts(employee, employeeCDM));
        employeeCDM.setEmploymentStatus(getEmploymentStatus(employee));
        employeeCDM.setPayrollCompanyId(employee.getCompany().getSourceCompanyId());
        return employeeCDM;
    }

    private List<BankAccountSubCDM> getBankAccounts(Employee source, EmployeeCDM employeeCDM) {
        DomainEntitySet<EmployeeBankAccount> bankAccount = source.getEmployeeBankAccountCollection();
        if (bankAccount != null && bankAccount.size() >= 1) {
            List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
            for (int i = 0, n = bankAccount.size(); i < n; i++) {
                EmployeeBankAccount employeeBankAccount = bankAccount.get(i);
                if (employeeBankAccount.getStatusCd().equals(BankAccountStatus.Active)) {
                    BankAccountSubCDM bankAccountSubCDM = getMapper().mapToTarget(employeeBankAccount, BankAccountSubCDM.class);
                    bankAccountSubCDMs.add(bankAccountSubCDM);
                    String walletId= employeeBankAccount.getBankAccount().getWalletId();
                    //EMS is saving walletId from EmployeeCDM, avoiding null check since its already checked while bank account mapper
                    if (employeeBankAccount.getAccountOrder()==0)
                        employeeCDM.setWalletId(walletId);
                    else if(employeeBankAccount.getAccountOrder()==1)
                        employeeCDM.setWalletId2(walletId);
                }
            }
            return bankAccountSubCDMs;
        } else {
            return null;
        }
    }

    private EmploymentStatusCDM getEmploymentStatus(Employee source) {
        if(source.getStatusCd()== EmployeeStatus.Active){
            return EmploymentStatusCDM.ACTIVE;
        }else if(source.getStatusCd() == EmployeeStatus.Inactive){
            return EmploymentStatusCDM.TERMINATED;
        }else{
            return null;
        }
    }

}

package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import org.hibernate.event.spi.AbstractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EmployeeEntityProcessor extends BaseEntityProcessor<Employee> {

    private final List<Class> interestedEntityClasses;
    private final Map<String, String> attributesCdmMap;

    @Autowired
    public EmployeeEntityProcessor(EntityProcessorUtility entityProcessorUtility) {
        super(entityProcessorUtility);
        interestedEntityClasses = Arrays.asList(Employee.class, BankAccount.class, Address.class, EmployeeBankAccount.class);
        attributesCdmMap = new HashMap<String, String>()
        {{
            put("ConsumerRealmId", "ConsumerRealmId,ConsumerFinanceRealm");
            put("UserAuthId", "IusAuthId");
            put("FirstName", "FirstName");
            put("MiddleName", "MiddleName");
            put("LastName", "LastName");
            put("GenderCd", "Gender");
            put("Phone", "HomePhone");
            put("TaxIdEnc", "TaxId");
            put("BirthDateEnc", "BirthDate");
            put("HireDate", "HireDate");
            put("TerminationDate", "TerminationDate");
            put("RehireDate", "RehireDate");
            put("Email", "BusinessEmail");
            put("StatusCd", "EmploymentStatus");
            put("MailingAddress", "HomeAddress");
            put("WorkState", "WorkAddress");
            put("EmployeeBankAccount", "BankAccount");
            put("EmployeeBankAccount.StatusCd", "BankAccount");
            put("BackAccount", "BackAccount");
            put("BankAccount.AccountTypeCd", "BankAccount");
            put("BankAccount.AccountNumberEnc", "BankAccount");
            put("Address.City", "HomeAddress");
            put("Address.Country", "HomeAddress");
            put("Address.State", "HomeAddress");
            put("Address.ZipCode", "HomeAddress");
            put("Address.AddressLine1", "HomeAddress");
            put("Address.AddressLine2", "HomeAddress");
            put("Address.AddressLine3", "HomeAddress");

        }};
    }

    @Override
    public List<Class> getInterestedEntities() {
        return interestedEntityClasses;
    }

    @Override
    protected Set<String> getAttributeFilters() {
        return attributesCdmMap.keySet();
    }

    @Override
    protected String getCdmAttributeName(String pspAttribute) {
        return attributesCdmMap.get(pspAttribute);
    }

    @Override
    public EntityContext<Employee> process(AbstractEvent abstractEvent) {
        return createEntityContext(abstractEvent);
    }

    @Override
    protected Class<?> getEntityType() {
        return Employee.class;
    }

    @Override
    protected Company getCompany(Employee entity) {
        return entity.getCompany();
    }

    @Override
    protected Employee getEntity(Object entity) {
        if (entity instanceof Employee) {
            return (Employee) entity;
        } else if (entity instanceof EmployeeBankAccount) {
            return ((EmployeeBankAccount) entity).getEmployee();
        } else if (entity instanceof BankAccount) {
            return getEmployeeFromBankAccount((BankAccount) entity);
        } else if (entity instanceof EmployeeCustomField) {
            return ((EmployeeCustomField) entity).getEmployee();
        } else if (entity instanceof EmployeePayrollItem) {
            return ((EmployeePayrollItem) entity).getEmployee();
        } else if(entity instanceof Address){
            return getEmployeeFromAddress((Address) entity);
        }
        return null;
    }

    private Employee getEmployeeFromBankAccount(BankAccount entity) {
        EmployeeBankAccount account = entity.getEmployeeBankAccount();
        if (Objects.nonNull(account)) {
            return account.getEmployee();
        }
        return null;
    }

    private Employee getEmployeeFromAddress(Address entity) {
        if(Objects.nonNull(entity.getIndividual()) && entity.getIndividual() instanceof Employee){
            return (Employee)entity.getIndividual();
        }
        return null;
    }
}

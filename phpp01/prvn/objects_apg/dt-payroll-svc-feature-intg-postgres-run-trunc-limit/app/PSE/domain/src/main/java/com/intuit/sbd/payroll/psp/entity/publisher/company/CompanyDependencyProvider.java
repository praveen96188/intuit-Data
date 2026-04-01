package com.intuit.sbd.payroll.psp.entity.publisher.company;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import com.intuit.sbd.payroll.psp.entity.publisher.IDependentEntityContextProvider;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CompanyDependencyProvider implements IDependentEntityContextProvider {
    private final Set<String> intrestedCdmAttributesSet;

    public CompanyDependencyProvider(){
        intrestedCdmAttributesSet = new HashSet(){{
            add("Id");
        }};
    }

    @Override
    public List<EntityContext> process(EntityContext entityContext, Set<String> updatedChangedAttributes) {
        List<EntityContext> result = new ArrayList<>();
        Company company = Application.findById(Company.class, entityContext.getEntityId());
        boolean realmIdAdded = isRealmIdAdded(company);
        if (realmIdAdded) {
            addEmployeeEntityContexts(result, company);
            addPayeeEntityContexts(result, company);
        }
        return result;
    }

    private void addEmployeeEntityContexts(List<EntityContext> result, Company company) {
        DomainEntitySet<Employee> employees = company.getEmployees();
        if (CollectionUtils.isEmpty(employees)) {
            return;
        }
        for (Employee employee : employees) {
            EntityContext<Employee> entityContext = new EntityContext<Employee>(employee, EventEnumType.EntityCreate);
            entityContext.setCompany(employee.getCompany());
            result.add(entityContext);
        }
    }

    private void addPayeeEntityContexts(List<EntityContext> result, Company company) {
        DomainEntitySet<Payee> payees = company.getPayeeCollection();
        if (CollectionUtils.isEmpty(payees)) {
            return;
        }
        for (Payee payee: payees) {
            EntityContext<Payee> entityContext = new EntityContext<Payee>(payee, EventEnumType.EntityCreate);
            entityContext.setCompany(payee.getCompany());
            result.add(entityContext);
        }
    }

    private boolean isRealmIdAdded(Company company) {
        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        spcfCalendar.addMinutes(-1);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.RealmIdAdded,null,spcfCalendar,null,true);
        if (CollectionUtils.isEmpty(companyEvents)) {
            return false;
        }
        return true;
    }

    @Override
    public Class getEntityClass() {
        return Company.class;
    }

    @Override
    public boolean isInterestedCdmAttribute(String attribute) {
        return this.intrestedCdmAttributesSet.contains(attribute);
    }

}

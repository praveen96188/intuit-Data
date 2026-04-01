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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class CompanyAllEmployeeProvider implements IDependentEntityContextProvider {

    private final Set<String> intrestedCdmAttributesSet;

    public CompanyAllEmployeeProvider(){
        intrestedCdmAttributesSet = new HashSet(){{
            add("Id");
        }};
    }

    @Override
    public List<EntityContext> process(EntityContext entityContext, Set<String> updatedChangedAttributes) {
        List<EntityContext> result = new ArrayList<>();
        Company company = Application.findById(Company.class, entityContext.getEntityId());
        if (!publishEmployee(company)) {
            return result;
        }
        DomainEntitySet<Employee> employees = company.getEmployees();
        if (Objects.isNull(employees)) {
            return result;
        }
        for (Employee employee : employees) {
            EntityContext<Employee> employeeEntityContext = new EntityContext(employee, EventEnumType.EntityCreate);
            employeeEntityContext.setCompany(employee.getCompany());
            result.add(employeeEntityContext);
        }
        return result;
    }

    private boolean publishEmployee(Company company) {
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

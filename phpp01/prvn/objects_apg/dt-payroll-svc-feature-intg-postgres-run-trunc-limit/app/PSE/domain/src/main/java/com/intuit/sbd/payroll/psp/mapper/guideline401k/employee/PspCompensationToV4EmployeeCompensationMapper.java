package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.common.Decimal;
import com.intuit.v4.common.TimeQuantity;
import com.intuit.v4.payroll.employee.EmployeeCompensation;
import com.intuit.v4.payroll.employer.EmployerCompensation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
public class PspCompensationToV4EmployeeCompensationMapper extends BeanMapper<EmployeeCompensationCDM, EmployeeCompensation> {

    @Override
    public EmployeeCompensation mapToTarget(EmployeeCompensationCDM compensation, Class<EmployeeCompensation> t) {

        EmployeeCompensation v4employeeCompensation = new EmployeeCompensation();

        v4employeeCompensation.setId(GlobalId.create("", compensation.getEmployeePItemSeq().getStandardFormatString()));
        v4employeeCompensation.setActive(compensation.getPItemStatus() == PayrollItemStatus.Active);
        v4employeeCompensation.setAmount(new Amount(compensation.getAmount().toString()));
        v4employeeCompensation.setMultiplier(Decimal.valueOf(compensation.getOvertime()));

        TimeQuantity timeQuantity = new TimeQuantity();
        switch(compensation.getPItemCode()){
            case Salary: timeQuantity.setUnit("ANNUAL");
                timeQuantity.setValue(Decimal.valueOf(1.0));
                break;
            case Hourly: timeQuantity.setUnit("HOURLY");
                break;
        }
        v4employeeCompensation.setTimeWorked(timeQuantity);

        EmployerCompensation v4EmployerCompensation = new EmployerCompensation();
        v4EmployerCompensation.setName(compensation.getPItemCode().name());
        v4EmployerCompensation.setId(GlobalId.create("", compensation.getCompanyPItemSeq().getStandardFormatString()));
        v4employeeCompensation.setEmployerCompensation(v4EmployerCompensation);

        return v4employeeCompensation;
    }
}

package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service;

import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckPendingState;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDTO;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.mapper.DomainObjToWCObjConverter;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class WorkersCompServiceNext extends WorkersCompService<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll>{
    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompServiceNext.class);

    /**
     * @return
     */
    @Override
    protected List<Set<SpcfUniqueId>> getCompaniesWithPendingPaychecks() {
        logger.info("getCompaniesWithPendingPaychecks() for next customers");
        return WorkersCompPaycheckPendingState.getCompaniesWithPendingPaychecks("findDistinctNextCompaniesWithWCPendingPaychecks");
    }

    /**
     * @param companyEmployees
     * @param employeePendingPaychecks
     * @param processedCompanyIds
     * @return
     */
    @Override
    public PayrollDTO getDTOFromDomainObject(List<WorkersCompPaycheckPendingState> pendingPaychecks) {
        return DomainObjToWCObjConverter.createDomainObjectToPayrollDto(pendingPaychecks);
    }

    /**
     * @param dto
     * @return
     */
    @Override
    boolean validateDto(PayrollDTO<Payroll> dto) {
            return (dto != null
                && dto.getPayroll() != null
                && dto.getPayroll().getBusinesses() != null
                && dto.getPayroll().getBusinesses().getBusiness() != null
                && dto.getPayroll().getBusinesses().getBusiness().size() > 0);
    }

    @Autowired
    public WorkersCompServiceNext(WorkersCompFileGeneratorNext fileGenerator) {
        super(fileGenerator);
    }
}

package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryCompanyEmployeesWihPaycheckCountRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryCompanyEmployeesWihPaycheckCountResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
public class PSPQueryCompanyEmployeesWihPaycheckCount extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyEmployeesWihPaycheckCount.class);
    }

    private QueryCompanyEmployeesWihPaycheckCountRequestDISDTO queryCompanyWagedEmployeeCountRequestDISDTO;
    private QueryCompanyEmployeesWihPaycheckCountResponseDISDTO queryCompanyWagedEmployeeCountResponseDISDTO;

    /**
     * Constructor
     *
     * @param pQueryCompanyWagedEmployeeCountRequestDISDTO
     *
     */
    public PSPQueryCompanyEmployeesWihPaycheckCount(QueryCompanyEmployeesWihPaycheckCountRequestDISDTO pQueryCompanyWagedEmployeeCountRequestDISDTO) {
        queryCompanyWagedEmployeeCountRequestDISDTO = pQueryCompanyWagedEmployeeCountRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyEmployeesWihPaycheckCount.process()");
        queryCompanyWagedEmployeeCountResponseDISDTO = new QueryCompanyEmployeesWihPaycheckCountResponseDISDTO();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(queryCompanyWagedEmployeeCountRequestDISDTO.getSourceCompanyId(), translateSourceSystemCode(queryCompanyWagedEmployeeCountRequestDISDTO.getSourceSystem()));
        if (company == null) {
            throw new DISException(DISMessages.companyDoesNotExist(queryCompanyWagedEmployeeCountRequestDISDTO.getSourceCompanyId()));
        }

        SpcfCalendar firstPaycheckDate = CalendarUtils.createInstanceFromDate(queryCompanyWagedEmployeeCountRequestDISDTO.getYear() + "0101");
        SpcfCalendar lastPaycheckDate = CalendarUtils.createInstanceFromDate(queryCompanyWagedEmployeeCountRequestDISDTO.getYear() + "1231");

        Expression<Paycheck> paycheckForYearQuery =
                new Query<Paycheck>().Where(Paycheck.Company().equalTo(company)
                        .And(Paycheck.PayrollRun().PaycheckDate().greaterOrEqualThan(firstPaycheckDate))
                        .And(Paycheck.PayrollRun().PaycheckDate().lessOrEqualThan(lastPaycheckDate)));

        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, paycheckForYearQuery);
        ArrayList uniqueEmps = new ArrayList();
        for (Paycheck paycheck : paychecks) {
            if (paycheck.getSourceEmployee() != null && paycheck.getSourceEmployee().getId() != null) {
                if (!uniqueEmps.contains(paycheck.getSourceEmployee().getId().getStandardFormatString())) {
                    uniqueEmps.add(paycheck.getSourceEmployee().getId().getStandardFormatString());
                }
            }
        }
        queryCompanyWagedEmployeeCountResponseDISDTO.setEmployeeCount(uniqueEmps.size());
        return queryCompanyWagedEmployeeCountResponseDISDTO;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryCompanyWagedEmployeeCountResponseDISDTO;
    }
}

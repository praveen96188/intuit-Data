package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISException;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyLatestPayrollDatesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyLatestPayrollDatesResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollType;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Calendar;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPQueryCompanyLatestPayrollDates.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/15 17:18:20 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyLatestPayrollDates extends DISProcessInterface {
    private static final SpcfLogger logger;
    static {
        logger = PayrollServices.getLogger(PSPQueryCompanyLatestPayrollDates.class);
    }

    private QueryCompanyLatestPayrollDatesRequestDISDTO queryCompanyPayrollDatesRequestDISDTO;
    private QueryCompanyLatestPayrollDatesResponseDISDTO queryCompanyPayrollDatesResponseDISDTO;

    /***
     * Constructor
     * @param pQueryCompanyPayrollDatesRequestDISDTO
     */
    public PSPQueryCompanyLatestPayrollDates(QueryCompanyLatestPayrollDatesRequestDISDTO pQueryCompanyPayrollDatesRequestDISDTO) {
        queryCompanyPayrollDatesRequestDISDTO = pQueryCompanyPayrollDatesRequestDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPQueryCompanyLatestPayrollDates.process()");
        queryCompanyPayrollDatesResponseDISDTO = new QueryCompanyLatestPayrollDatesResponseDISDTO();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(queryCompanyPayrollDatesRequestDISDTO.getSourceCompanyId(),translateSourceSystemCode(queryCompanyPayrollDatesRequestDISDTO.getSourceSystem()));
        // A DIS message will be thrown and logged if the company does not exist.
        // This should not be happening in production and if it is, we need to know about it.
        if (company == null) {
            throw new DISException(DISMessages.companyDoesNotExist(queryCompanyPayrollDatesRequestDISDTO.getSourceCompanyId()));
        }

        // JPC 10/15/2012 - Added logic to only pull back Regular and CloudOnly when getting company
        //    last check date and last payroll date.
        Calendar latestPayrollRunDate = null;
        {
            DomainEntitySet<PayrollRun> payrollRuns = Application.find(
                    PayrollRun.class,
                    new Query<PayrollRun>().Where(
                            PayrollRun.Company().
                                    equalTo(company).
                                    And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Canceled).
                                    And(PayrollRun.PayrollRunType().in(PayrollType.CloudOnly,PayrollType.Regular))
                                    )
                    ).OrderBy(PayrollRun.PayrollRunDate().Descending()).LimitResults(0, 1));

            if (payrollRuns != null && payrollRuns.size() > 0) {
                if (payrollRuns.get(0).getPayrollRunDate() != null) {
                    latestPayrollRunDate = CalendarUtils.convertToCalendar(payrollRuns.get(0).getPayrollRunDate());
                }
            }
        }

        Calendar latestPayrollCheckDate = null;
        {
            DomainEntitySet<PayrollRun> payrollRuns = Application.find(
                    PayrollRun.class,
                    new Query<PayrollRun>().Where(
                            PayrollRun.Company().equalTo(company).
                                    And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Canceled)).
                                    And(PayrollRun.PayrollRunType().in(PayrollType.CloudOnly,PayrollType.Regular))
                    ).OrderBy(PayrollRun.PaycheckDate().Descending()).LimitResults(0, 1));

            if (payrollRuns != null && payrollRuns.size() > 0) {
                if (payrollRuns.get(0).getPaycheckDate() != null) {
                    latestPayrollCheckDate = CalendarUtils.convertToCalendar(payrollRuns.get(0).getPaycheckDate());
                }
            }
        }
        queryCompanyPayrollDatesResponseDISDTO.setLatestPayrollCheckDate(latestPayrollCheckDate);
        queryCompanyPayrollDatesResponseDISDTO.setLatestPayrollRunDate(latestPayrollRunDate);

        Calendar firstPayrollRunDate = null;
        {
            DomainEntitySet<PayrollRun> payrollRuns = Application.find(
                    PayrollRun.class,
                    new Query<PayrollRun>().Where(
                            PayrollRun.Company().
                                    equalTo(company).
                                    And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Canceled).
                                    And(PayrollRun.PayrollRunType().in(PayrollType.CloudOnly,PayrollType.Regular))
                                    )
                    ).OrderBy(PayrollRun.PayrollRunDate()).LimitResults(0, 1));

            if (payrollRuns != null && payrollRuns.size() > 0) {
                if (payrollRuns.get(0).getPayrollRunDate() != null) {
                    firstPayrollRunDate = CalendarUtils.convertToCalendar(payrollRuns.get(0).getPayrollRunDate());
                }
            }
        }

        Calendar firstPayrollCheckDate = null;
        {
            DomainEntitySet<PayrollRun> payrollRuns = Application.find(
                    PayrollRun.class,
                    new Query<PayrollRun>().Where(
                            PayrollRun.Company().equalTo(company).
                                    And(PayrollRun.PayrollRunStatus().notEqualTo(PayrollStatus.Canceled)).
                                    And(PayrollRun.PayrollRunType().in(PayrollType.CloudOnly,PayrollType.Regular))
                    ).OrderBy(PayrollRun.PaycheckDate()).LimitResults(0, 1));

            if (payrollRuns != null && payrollRuns.size() > 0) {
                if (payrollRuns.get(0).getPaycheckDate() != null) {
                    firstPayrollCheckDate = CalendarUtils.convertToCalendar(payrollRuns.get(0).getPaycheckDate());
                }
            }
        }
        queryCompanyPayrollDatesResponseDISDTO.setFirstPayrollCheckDate(firstPayrollCheckDate);
        queryCompanyPayrollDatesResponseDISDTO.setFirstPayrollRunDate(firstPayrollRunDate);

        return queryCompanyPayrollDatesResponseDISDTO;
    }

    @Override
    public ResponseDISDTO getResponse() {
        return queryCompanyPayrollDatesResponseDISDTO;
    }
}

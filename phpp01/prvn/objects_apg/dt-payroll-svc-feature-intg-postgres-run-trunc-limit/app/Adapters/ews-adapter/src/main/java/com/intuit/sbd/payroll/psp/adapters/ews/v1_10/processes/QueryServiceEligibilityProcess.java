package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsEinServiceEligibility;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsEinServiceEligibilityResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class QueryServiceEligibilityProcess extends BaseProcess {

    private EwsEinServiceEligibility mRequest;
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(QueryServiceEligibilityProcess.class);
    }

    public QueryServiceEligibilityProcess(EwsEinServiceEligibility pRequest) {
        mRequest = pRequest;
        mEIN = pRequest.getEin();
        
        logger.info("Processing Query Service Eligibility Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsEinServiceEligibilityResponse execute() {
        EwsEinServiceEligibilityResponse response = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsEinServiceEligibilityResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsEinServiceEligibilityResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();
    }

    @Override
    protected EwsEinServiceEligibilityResponse process() throws Exception {
        EwsEinServiceEligibilityResponse response = new EwsEinServiceEligibilityResponse();

        response.setEligibleForDIY(true);
        response.setEligibleForDD(true);
        response.setEligibleForAssisted(true);

        DomainEntitySet<Company> companies = PspFactory.findCompaniesByEin(mEIN);
        if (companies != null && !companies.isEmpty()) {
            for (Company company : companies) {
                try {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                    if (company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                        response.setEligibleForDD(false);
                        response.setEligibleForAssisted(false);
                        break;
                    }

                    if (company.hasService(ServiceCode.Tax)) {
                        if (company.isCompanyOnService(ServiceCode.Tax)) {
                            response.setEligibleForDD(false);
                            response.setEligibleForAssisted(false);
                            break;
                        } else {
                            if (mRequest.isEnableCurrentTaxYearValidation()) {
                                if (hasTaxLedgerTxnsInCurrentYear(company)) {
                                    response.setEligibleForDD(true);
                                    response.setEligibleForAssisted(false);
                                    break;
                                }
                            }
                        }
                    }
                } finally {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                }
            }
        }

        return response;
    }

    private boolean hasTaxLedgerTxnsInCurrentYear(Company pCompany) {

        SpcfCalendar fromDate = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), 1, 1);
        SpcfCalendar toDate = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), 12, 31, 23, 59, 59, 999);

        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(pCompany, fromDate, toDate);

        boolean hasTaxLedgerTxns = false;

        for (PayrollRun payrollRun : payrollRuns) {
            if (!payrollRun.getLiabilityCheckCollection().isEmpty()) {
                hasTaxLedgerTxns = true;
                break;
            }
            for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                if (!paycheck.getTaxCollection().isEmpty()) {
                    hasTaxLedgerTxns = true;
                    break;
                }
            }
        }

        return hasTaxLedgerTxns;
    }
}

package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.LockMode;

import java.util.List;

/**
 * Hand-written business logic
 */
public class WorkersCompPaycheck extends BaseWorkersCompPaycheck {

    public static SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompPaycheck.class);
            /**
             * Default constructor.
             */

    public WorkersCompPaycheck()
	{
		super();
	}

    /*
     * Creates a corresponding workers comp paycheck for a paycheck
     */
    public static WorkersCompPaycheck updateWorkersCompPaycheck(Paystub pPaystub, Paycheck paycheck) {

        try{
            if (paycheck.getCompany().isCompanyOnService(ServiceCode.WorkersComp)
                    && CompanyService.wasCompanyOnServiceForDate(paycheck.getCompany(), ServiceCode.WorkersComp, pPaystub.getPaycheckDate())){

                WorkersCompPaycheck workersCompPaycheck = paycheck.getWorkersCompPaycheck();
                if(WorkersCompPaycheckStateCode.Sent == workersCompPaycheck.getCurrentStateCd()) {
                    if (paycheck.getStatus() == PaycheckStatusCode.Active){
                        workersCompPaycheck.setPaycheckVersion(workersCompPaycheck.getPaycheckVersion()+1);
                        workersCompPaycheck.updateCurrentStateCd(WorkersCompPaycheckStateCode.PendingEdit);
                    }
                }

                logger.info("Update WC Paycheck :: Company=" + paycheck.getCompany().getSourceCompanyId() +
                        ", PayCheck_Seq=" + paycheck.getId().toString() + ", PayCheck_ID=" +
                        paycheck.getSourcePaycheckId() + ", WC_Seq=" + workersCompPaycheck.getId());

                return workersCompPaycheck;
            }
            else {
                return null;
            }

        }
        catch (Exception e){
            logger.error("Error in updateWorkersCompPaycheck",e );
            return null;
        }
    }

    /*
     * Creates a corresponding workers comp paycheck for a paycheck
     */
    public static WorkersCompPaycheck createWorkersCompPaycheck(Paycheck paycheck) {
        if (isPaycheckBeforeServiceStartDate(paycheck) || isPaycheckAfterServiceEndDate(paycheck)) {
            return null;
        }

        WorkersCompPaycheck workersCompPaycheck = paycheck.getWorkersCompPaycheck();
        if(workersCompPaycheck == null) {
            workersCompPaycheck = new WorkersCompPaycheck();
            workersCompPaycheck.setCompany(paycheck.getCompany());
            paycheck.setWorkersCompPaycheck(workersCompPaycheck);
            workersCompPaycheck.setPaycheck(paycheck);
        }

        workersCompPaycheck.updateCurrentStateCd(WorkersCompPaycheckStateCode.PendingNew);
        workersCompPaycheck.setPaycheckVersion(1);
        Application.save(workersCompPaycheck);

        logger.info("Create WC Paycheck :: Company=" + paycheck.getCompany().getSourceCompanyId() +
                ", PayCheck_Seq=" + paycheck.getId().toString() + ", PayCheck_ID=" +
                paycheck.getSourcePaycheckId() + ", WC_Seq=" + workersCompPaycheck.getId());

        return workersCompPaycheck;
    }

    /*
     * Cancels a workers comp paycheck
     */
    public static WorkersCompPaycheck cancelOrDeleteWorkersCompPaycheck(Paycheck paycheck) {
        if (isPaycheckBeforeServiceStartDate(paycheck) || isPaycheckAfterServiceEndDate(paycheck)) {
            return null;
        }

        WorkersCompPaycheck workersCompPaycheck = paycheck.getWorkersCompPaycheck();
        if (workersCompPaycheck == null) {
            throw new RuntimeException("Missing corresponding workers comp Paycheck record for paycheck id : " + paycheck.getId() + " payCheckDate : " + paycheck.getPayrollRun().getPaycheckDate());
        }
        if(WorkersCompPaycheckStateCode.PendingNew == workersCompPaycheck.getCurrentStateCd()) {
            //Safe to cancel a  new paycheck if it has not been sent
            workersCompPaycheck.updateCurrentStateCd(WorkersCompPaycheckStateCode.Cancelled);
        } else if(WorkersCompPaycheckStateCode.Sent == workersCompPaycheck.getCurrentStateCd()) {
            //Need to send a delete if the paycheck has been sent
            workersCompPaycheck.setPaycheckVersion(workersCompPaycheck.getPaycheckVersion()+1);
            workersCompPaycheck.updateCurrentStateCd(WorkersCompPaycheckStateCode.PendingDelete);
        }
        //If a paycheck is PendingDelete or Cancelled we don't need to make any changes as it is the correct state
        //already for being voided

        logger.info("Cancel/Delete WC Paycheck :: Company=" + paycheck.getCompany().getSourceCompanyId() +
                ", PayCheck_Seq=" + paycheck.getId().toString() + ", PayCheck_ID=" +
                paycheck.getSourcePaycheckId() + ", WC_Seq=" + workersCompPaycheck.getId());

        return workersCompPaycheck;
    }

    public void markAsSent() {
        this.updateCurrentStateCd(WorkersCompPaycheckStateCode.Sent);
    }

    private static boolean isPaycheckBeforeServiceStartDate(Paycheck paycheck) {

        if (paycheck.getPayrollRun().getCompany().getCompanyService(ServiceCode.WorkersComp) == null) {
            //tht means workerscomp is not present, so the paycheckbeforeServiceStartDate can be returned as true
            return true;
        }
        SpcfCalendar serviceStartDate = paycheck.getPayrollRun().getCompany().getCompanyService(ServiceCode.WorkersComp).getServiceStartDate();
        return paycheck.getPayrollRun().getPaycheckDate().before(serviceStartDate);
    }

    private static boolean isPaycheckAfterServiceEndDate(Paycheck paycheck) {

        if (paycheck.getPayrollRun().getCompany().getCompanyService(ServiceCode.WorkersComp) == null) {
            //tht means workerscomp is not present, so the isPaycheckAfterServiceEndDate can be returned as true ,Kept same as in isPaycheckBeforeServiceStartDate().
            return true;
        }
        if (paycheck.getPayrollRun().getCompany().isCompanyOnService(ServiceCode.WorkersComp)) {
            //This means workers comp is not terminated/cancelled , so return false
            return false;
        }
        SpcfCalendar serviceEndDate = paycheck.getPayrollRun().getCompany().getCompanyService(ServiceCode.WorkersComp).getStatusEffectiveDate();
        return paycheck.getPayrollRun().getPaycheckDate().after(serviceEndDate);

    }

    private void updateCurrentStateCd(WorkersCompPaycheckStateCode workersCompPaycheckStateCode) {
        if (workersCompPaycheckStateCode == this.getCurrentStateCd()) {
            return;
        }

        if (this.getPaycheck() == null) {
            throw new RuntimeException("Cannot set WorkersCompPaycheck to" + workersCompPaycheckStateCode + " -- no Paycheck association has been set");
        }

        this.setCurrentStateCd(workersCompPaycheckStateCode);
        Application.save(this);

        //Add an entry to the state table
        createWorkersCompPaycheckState(workersCompPaycheckStateCode);

        if (isPendingStateCode(workersCompPaycheckStateCode)) {
            WorkersCompPaycheckPendingState workersCompPaycheckPendingState;
            if (this.getPaycheck().getWorkersCompPaycheck().getWorkersCompPaycheckPendingState() != null) {
                workersCompPaycheckPendingState = getPaycheck().getWorkersCompPaycheck().getWorkersCompPaycheckPendingState();
            } else {
                workersCompPaycheckPendingState = new WorkersCompPaycheckPendingState();
            }
            workersCompPaycheckPendingState.setStateCd(workersCompPaycheckStateCode);
            workersCompPaycheckPendingState.setWorkersCompPaycheck(this);
            Application.save(workersCompPaycheckPendingState);
            this.setWorkersCompPaycheckPendingState(workersCompPaycheckPendingState);
        } else if (this.getWorkersCompPaycheckPendingState() != null) {
            //If the new status is not Pending but a pending paycheck exists, delete it
            Application.delete(this.getWorkersCompPaycheckPendingState());
            this.setWorkersCompPaycheckPendingState(null);
        }

        updateInitiationDate();
    }

    private void createWorkersCompPaycheckState(WorkersCompPaycheckStateCode workersCompPaycheckStateCode) {
        WorkersCompPaycheckState newPaycheckState = new WorkersCompPaycheckState();
        newPaycheckState.setStateEffectiveDate(PSPDate.getPSPTime());
        newPaycheckState.setStateCd(workersCompPaycheckStateCode);
        newPaycheckState.setWorkersCompPaycheck(this);
        Application.save(newPaycheckState);
    }

    public static boolean isPendingStateCode(WorkersCompPaycheckStateCode stateCode) {
        return stateCode != null && (stateCode == WorkersCompPaycheckStateCode.PendingNew || stateCode == WorkersCompPaycheckStateCode.PendingDelete || stateCode == WorkersCompPaycheckStateCode.PendingEdit);
    }

    private SpcfCalendar updateInitiationDate() {
        setInitiationDate(PSPDate.getPSPTime());

        if (getWorkersCompPaycheckPendingState() != null) {
            getWorkersCompPaycheckPendingState().setInitiationDate(getInitiationDate());
        }

        return getInitiationDate();
    }

    public static DomainEntitySet<WorkersCompPaycheck> findByPaycheck(Paycheck paycheck){
        Expression<WorkersCompPaycheck> query = new Query<WorkersCompPaycheck>()
                .Where(WorkersCompPaycheck.Paycheck().equalTo(paycheck));

        return Application.find(WorkersCompPaycheck.class, query);
    }

    public static int findPaycheckSentCount(WorkersCompPaycheck wcPaycheck) {
        Expression<WorkersCompPaycheckState> query =
                new Query<WorkersCompPaycheckState>()
                        .Select(WorkersCompPaycheckState.StateCd().Count())
                        .Where(WorkersCompPaycheckState.WorkersCompPaycheck().equalTo(wcPaycheck)
                                .And(WorkersCompPaycheckState.StateCd().equalTo(WorkersCompPaycheckStateCode.Sent)));
        return Application.executeScalarAggQuery(WorkersCompPaycheckState.class, query).intValue();
    }
    public static void markAsSent(List<WorkersCompPaycheck> paychecks) {
        if (paychecks != null && paychecks.size() > 0) {
            for (WorkersCompPaycheck paycheck : paychecks) {
                Application.getHibernateSession().lock(paycheck, LockMode.NONE); //This will not hit the DB again
                paycheck.markAsSent();
            }
        }
    }
}
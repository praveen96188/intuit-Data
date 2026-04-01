package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowPackager;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.payee.PayeePublishStatusWorkflows;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * Hand-written business logic
 */
public class Payee extends BasePayee {
    public static String AccountNumberKeyName="Payee_AccNo";
    public static String TaxIdKeyName="Payee_TaxId";
    private PublishStatusWorkflowPackager publishStatusWorkflowPackager = null;

    /**
     * Default constructor.
     */
    public Payee() {
        super();
    }

    @Override
    public void setEmail(String pEmail) {
        //If Email is changed update the email and reset HasInvalidEmail flag to false
        if ( !StringUtils.equals(getEmail(), pEmail) ) {
            setHasInvalidEmail(false);
        }
        super.setEmail(pEmail);
    }

    public NaturalKey getNaturalKey() {
        return new NaturalKey(Payee.class, getCompany().getId(), getSourcePayeeId());
    }

    public static Payee findPayee(Company pCompany, String pSourcePayeeId) {
        Payee foundPayee = null;

        NaturalKey naturalKey = new NaturalKey(Payee.class, pCompany.getId(), pSourcePayeeId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundPayee = Application.findById(Payee.class, primaryKey);
        } else {
            DomainEntitySet<Payee> payees =
                    Application.find(Payee.class,
                            Payee.Company().equalTo(pCompany)
                                    .And(Payee.SourcePayeeId().equalTo(pSourcePayeeId)));

            if (payees.size() > 1) {
                throw new RuntimeException(
                        "Query for payee by company " + pCompany + " and source payee id " + pSourcePayeeId + " did not return 0 or 1 results as expected");
            }

            if (!payees.isEmpty()) {
                foundPayee = payees.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundPayee.getId());
            }
        }
        return foundPayee;
    }

    public static DomainEntitySet<Payee> findPayees(Company pCompany) {
        return Application.find(Payee.class,
                Payee.Company().equalTo(pCompany));
    }

    public static DomainEntitySet<Payee> findPayees(Address pAddress) {
        return Application.find(Payee.class,
                Payee.MailingAddress().equalTo(pAddress));
    }

    public void setAccountNumber(String pAccountNumber) {
        super.setAccountNumberEnc(EncryptionUtils.deterministicEncrypt(AccountNumberKeyName,pAccountNumber));
    }

    public String getAccountNumber() {
        return EncryptionUtils.deterministicDecrypt(AccountNumberKeyName,getAccountNumberEnc());
    }
    public void setTaxId(String pTaxId)
    {
        super.setTaxIdEnc(EncryptionUtils.deterministicEncrypt(TaxIdKeyName,pTaxId));
    }
    public String getTaxId() {
        return EncryptionUtils.deterministicDecrypt(TaxIdKeyName,getTaxIdEnc());
    }

    public PublishStatusWorkflowPackager getPublishStatusWorkFlowPackager() {

        if(Objects.isNull(this.publishStatusWorkflowPackager)){
            String workFlowsFlag = super.getPublishStatus();
            this.publishStatusWorkflowPackager = new PublishStatusWorkflowPackager(workFlowsFlag);
        }

        return this.publishStatusWorkflowPackager;
    }

    public void setPublishStatusWorkflowState(PayeePublishStatusWorkflows workflow, PublishStatusWorkflowState workflowState) {
        this.getPublishStatusWorkFlowPackager().setWorkflowState(workflow, workflowState);
    }

    public String getPublishStatus() {
        String workFlowsFlag = this.getPublishStatusWorkFlowPackager().getWorkFlowsFlagString();
        return workFlowsFlag;
    }

    public SpcfCalendar getLastPayDate() {
        Criterion<BillPayment> where = BillPayment.Payee().equalTo(this).And(BillPayment.PayrollRun().PaycheckDate().isNotNull());
        Expression<BillPayment> query = new Query<BillPayment>().Where(where).OrderBy(BillPayment.PayrollRun().PaycheckDate().Descending()).LimitResults(0,1);
        DomainEntitySet<BillPayment> billSet = Application.find(BillPayment.class, query);
        return billSet.size() > 0 ? (billSet.get(0)).getPayrollRun().getPaycheckDate(): null;
    }
}
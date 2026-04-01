package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Hand-written business logic
 */
public class QbdtPayrollTransaction extends BaseQbdtPayrollTransaction implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public QbdtPayrollTransaction()
	{
		super();
	}

    public static QbdtPayrollTransaction findQbdtPayrollTransactionBySourceId(Company pCompany, String pSourceId) {
        DomainEntitySet<QbdtPayrollTransaction> qbdtPayrollTransactions =
                Application.find(QbdtPayrollTransaction.class,
                                 QbdtPayrollTransaction.Company().equalTo(pCompany)
                                         .And(QbdtPayrollTransaction.SourceId().equalTo(pSourceId)));

        if(qbdtPayrollTransactions.size() > 0) {
            return qbdtPayrollTransactions.get(0);
        }

        return null;
    }


    public static DomainEntitySet<QbdtPayrollTransaction> findQbdtPayrollTransactionsByEmployee(Company pCompany, Employee pEmployee, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        Criterion<QbdtPayrollTransaction> where = QbdtPayrollTransaction.Company().equalTo(pCompany)
                                                    .And(QbdtPayrollTransaction.Employee().equalTo(pEmployee));
        if (pFromDate != null) {
            where = where.And(QbdtPayrollTransaction.TransactionDate().greaterOrEqualThan(pFromDate));
        }
        if (pToDate != null) {
            where = where.And(QbdtPayrollTransaction.TransactionDate().lessOrEqualThan(pToDate));
        }

        return Application.find(QbdtPayrollTransaction.class, new Query<QbdtPayrollTransaction>().Where(where)
                                                                     .OrderBy(QbdtPayrollTransaction.TransactionDate()).EagerLoad(QbdtPayrollTransaction.QbdtPayrollTransactionLineSet()));
    }


    // ----- QBDT Token overrides -----

    @Override
    public void setTransactionType(QbdtPayrollTransactionType pTransactionType) {
        if(!ObjectUtils.equals(getTransactionType(), pTransactionType)) {
            onUpdate();
        }
        super.setTransactionType(pTransactionType);    
    }

    @Override
    public void setAmount(SpcfMoney pAmount) {
        if(!ObjectUtils.equals(getAmount(), pAmount)) {
            onUpdate();
        }
        super.setAmount(pAmount);    
    }

    @Override
    public void setPeriodEndDate(SpcfCalendar pPeriodEndDate) {
        if(!ObjectUtils.equals(getPeriodEndDate(), pPeriodEndDate)) {
            onUpdate();
        }
        super.setPeriodEndDate(pPeriodEndDate);    
    }

    @Override
    public void setIsVoided(boolean pIsVoided) {
        if(!ObjectUtils.equals(getIsVoided(), pIsVoided)) {
            onUpdate();
        }
        super.setIsVoided(pIsVoided);    
    }

    @Override
    public void setSourceId(String pSourceId) {
        if(!ObjectUtils.equals(getSourceId(), pSourceId)) {
            onUpdate();
        }

        super.setSourceId(pSourceId);

        if(getCompany() != null && getSourceId() != null) {
            getCompany().usedPayrollTransactionId(getSourceId());
        }
    }

    @Override
    public void setTransactionDate(SpcfCalendar pTransactionDate) {
        if(!ObjectUtils.equals(getTransactionDate(), pTransactionDate)) {
            onUpdate();
        }
        super.setTransactionDate(pTransactionDate);    
    }

    @Override
    public void setCompanyAdjustmentSubmission(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        if(!ObjectUtils.equals(getCompanyAdjustmentSubmission(), pCompanyAdjustmentSubmission)) {
            onUpdate();
        }
        super.setCompanyAdjustmentSubmission(pCompanyAdjustmentSubmission);    
    }

    @Override
    public void setPriorPaymentSubmission(PriorPaymentSubmission pPriorPaymentSubmission) {
        if(!ObjectUtils.equals(getPriorPaymentSubmission(), pPriorPaymentSubmission)) {
            onUpdate();
        }
        super.setPriorPaymentSubmission(pPriorPaymentSubmission);    
    }

    @Override
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);    
    }

    @Override
    public void setEmployee(Employee pEmployee) {
        if(!ObjectUtils.equals(getEmployee(), pEmployee)) {
            onUpdate();
        }
        super.setEmployee(pEmployee);    
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }

        super.setCompany(pCompany);

        if(getCompany() != null && getSourceId() != null) {
            getCompany().usedPayrollTransactionId(getSourceId());
        }
    }

    @Override
    public void addQbdtPayrollTransactionLine(QbdtPayrollTransactionLine pQbdtPayrollTransactionLine) {
        super.addQbdtPayrollTransactionLine(pQbdtPayrollTransactionLine);    
        onUpdate();
    }

    @Override
    public void removeQbdtPayrollTransactionLine(QbdtPayrollTransactionLine pQbdtPayrollTransactionLine) {
        super.removeQbdtPayrollTransactionLine(pQbdtPayrollTransactionLine);
        onUpdate();
    }

    public void onUpdate() {
        if(getCompanyAdjustmentSubmission() != null) {
            getCompanyAdjustmentSubmission().onUpdate();
        } else if(getPriorPaymentSubmission() != null) {
            getPriorPaymentSubmission().onUpdate();
        } else {
            update();
        }
    }

    // used to avoid cyclical updates
    public void update() {
        if(getQbdtTransactionInfo() != null) {
            getQbdtTransactionInfo().onUpdate();
        }
    }
}
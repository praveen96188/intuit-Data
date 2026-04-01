package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Hand-written business logic
 */
public class QbdtTransactionInfo extends BaseQbdtTransactionInfo implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public QbdtTransactionInfo()
	{
		super();
	}

    public static DomainEntitySet<QbdtTransactionInfo> findQbdtTransactionInfosGreaterToken(Company pCompany, long pSyncToken) {
        Expression<QbdtTransactionInfo> query = new Query<QbdtTransactionInfo>()
                .Where(QbdtTransactionInfo.Company().equalTo(pCompany)
                                          .And(QbdtTransactionInfo.Token().greaterThan(pSyncToken))
                                          .And(QbdtTransactionInfo.CompanyAdjustmentSubmission().isNotNull()
                                                                  .Or(QbdtTransactionInfo.LiabilityCheck().isNotNull())
                                                                  .Or(QbdtTransactionInfo.PriorPaymentSubmission().isNotNull())
                                                                  .Or(QbdtTransactionInfo.QbdtPayrollTransaction().isNotNull())))
                .EagerLoad(QbdtTransactionInfo.Company(),
                           QbdtTransactionInfo.CompanyAdjustmentSubmission(),
                           QbdtTransactionInfo.LiabilityCheck(),
                           QbdtTransactionInfo.PriorPaymentSubmission(),
                           QbdtTransactionInfo.QbdtPayrollTransaction())
                .ReadOnly(true);
        return Application.find(QbdtTransactionInfo.class, query);
    }

    // ----- QBDT Token overrides -----

    @Override
    public void setAgencyName(String pAgencyName) {
        if(!ObjectUtils.equals(getAgencyName(), pAgencyName)) {
            onUpdate();
        }
        super.setAgencyName(pAgencyName);
    }

    @Override
    public void setReferenceNumber(String pReferenceNumber) {
        if(!ObjectUtils.equals(getReferenceNumber(), pReferenceNumber)) {
            onUpdate();
        }
        super.setReferenceNumber(pReferenceNumber);
    }

    @Override
    public void setAccountName(String pAccountName) {
        if(!ObjectUtils.equals(getAccountName(), pAccountName)) {
            onUpdate();
        }
        super.setAccountName(pAccountName);
    }

    @Override
    public void setMemo(String pMemo) {
        if(!ObjectUtils.equals(getMemo(), pMemo)) {
            onUpdate();
        }
        super.setMemo(pMemo);
    }

    @Override
    public void setOnService(boolean pOnService) {
        if(!ObjectUtils.equals(getOnService(), pOnService)) {
            onUpdate();
        }
        super.setOnService(pOnService);
    }

    @Override
    public void setCleared(String pCleared) {
        if(!ObjectUtils.equals(getCleared(), pCleared)) {
            onUpdate();
        }
        super.setCleared(pCleared);
    }

    @Override
    public void setTrackingClass(String pTrackingClass) {
        if(!ObjectUtils.equals(getTrackingClass(), pTrackingClass)) {
            onUpdate();
        }
        super.setTrackingClass(pTrackingClass);
    }

    @Override
    public void setIsDeleted(boolean pIsDeleted) {
        if(!ObjectUtils.equals(getIsDeleted(), pIsDeleted)) {
            onUpdate();
        }
        super.setIsDeleted(pIsDeleted);
    }

    @Override
    public void setIsDirectDeposit(boolean pIsDirectDeposit) {
        if(!ObjectUtils.equals(getIsDirectDeposit(), pIsDirectDeposit)) {
            onUpdate();
        }
        super.setIsDirectDeposit(pIsDirectDeposit);
    }

    @Override
    public void setCompanyAdjustmentSubmission(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        if(!ObjectUtils.equals(getCompanyAdjustmentSubmission(), pCompanyAdjustmentSubmission)) {
            onUpdate();
        }
        super.setCompanyAdjustmentSubmission(pCompanyAdjustmentSubmission);
    }

    @Override
    public void setLiabilityAdjustment(LiabilityAdjustment pLiabilityAdjustment) {
        if(!ObjectUtils.equals(getLiabilityAdjustment(), pLiabilityAdjustment)) {
            onUpdate();
        }
        super.setLiabilityAdjustment(pLiabilityAdjustment);
    }

    @Override
    public void setLiabilityCheck(LiabilityCheck pLiabilityCheck) {
        if(!ObjectUtils.equals(getLiabilityCheck(), pLiabilityCheck)) {
            onUpdate();
        }
        super.setLiabilityCheck(pLiabilityCheck);
    }

    @Override
    public void setQbdtPayrollTransaction(QbdtPayrollTransaction pQbdtPayrollTransaction) {
        if(!ObjectUtils.equals(getQbdtPayrollTransaction(), pQbdtPayrollTransaction)) {
            onUpdate();
        }
        super.setQbdtPayrollTransaction(pQbdtPayrollTransaction);
    }

    @Override
    public void setMoneyMovementTransaction(MoneyMovementTransaction pMoneyMovementTransaction) {
        if(!ObjectUtils.equals(getMoneyMovementTransaction(), pMoneyMovementTransaction)) {
            onUpdate();
        }
        super.setMoneyMovementTransaction(pMoneyMovementTransaction);
    }

    @Override
    public void setPriorPaymentSubmission(PriorPaymentSubmission pPriorPaymentSubmission) {
        if(!ObjectUtils.equals(getPriorPaymentSubmission(), pPriorPaymentSubmission)) {
            onUpdate();
        }
        super.setPriorPaymentSubmission(pPriorPaymentSubmission);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany)) {
            onUpdate();
        }
        super.setCompany(pCompany);
    }

    @Override
    public void setToken(long pToken) {
        if(getToken() == Company.EXCLUDE_TOKEN) {
            // once a transaction's token has been set to exclude it cannot be updated outside of sql.
            // these payroll transactions should be recovered from the AS400
            return;
        }

        super.setToken(pToken);
    }

    public void onUpdate() {
        if(getPriorPaymentSubmission() != null) {
            getPriorPaymentSubmission().onUpdate();
        } else if(getFinancialTransaction() != null) {
            getFinancialTransaction().onUpdate();
        } else if(getLiabilityCheckLine() != null) {
            getLiabilityCheckLine().onUpdate();
        } else if(getQbdtPayrollTransactionLine() != null) {
            getQbdtPayrollTransactionLine().onUpdate();
        } else {
            update();
        }
    }

    // used to avoid cyclical updates
    public void update() {
        if(getCompany() != null) {
            if (getLiabilityAdjustment() == null) {
                setToken(getCompany().getNextToken());
            } else if (getLiabilityAdjustment().isCreatedInCurrentSession()) {
                setToken(getCompany().getNextToken());
            }
        }
    }
}
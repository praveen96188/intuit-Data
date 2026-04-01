package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class LiabilityCheck extends BaseLiabilityCheck implements IUpdatable {

	/**
	 * Default constructor.
	 */
	public LiabilityCheck()
	{
		super();
	}

    public NaturalKey getNaturalKey() {
        return new NaturalKey(LiabilityCheck.class, getCompany().getId(), getSourceId());
    }

    public static LiabilityCheck findLiabilityCheckBySourceId(Company pCompany, String pSourceId) {
        if(pSourceId == null) {
            return null;
        }

        LiabilityCheck foundLiabilityCheck = null;
        NaturalKey naturalKey = new NaturalKey(LiabilityCheck.class, pCompany.getId(), pSourceId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundLiabilityCheck = Application.findById(LiabilityCheck.class, primaryKey);
        } else {
            DomainEntitySet<LiabilityCheck> liabilityChecks = Application.find(LiabilityCheck.class,
                                                                               LiabilityCheck.Company().equalTo(pCompany)
                                                                                             .And(LiabilityCheck.SourceId().equalTo(pSourceId)));

            if(liabilityChecks.size() > 0) {
                foundLiabilityCheck = liabilityChecks.get(0);
            }
        }

        return foundLiabilityCheck;
    }

    // ----- QBDT Token overrides -----
    
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
    public void setIsVoid(boolean pIsVoid) {
        if(!ObjectUtils.equals(getIsVoid(), pIsVoid)) {
            onUpdate();
        }
        super.setIsVoid(pIsVoid);    
    }

    @Override
    public void setTransactionDate(SpcfCalendar pTransactionDate) {
        if(!ObjectUtils.equals(getTransactionDate(), pTransactionDate)) {
            onUpdate();
        }
        super.setTransactionDate(pTransactionDate);    
    }

    @Override
    public void setType(LiabilityCheckType pType) {
        if(!ObjectUtils.equals(getType(), pType)) {
            onUpdate();
        }
        super.setType(pType);    
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
    public void setQbdtTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        if(!ObjectUtils.equals(getQbdtTransactionInfo(), pQbdtTransactionInfo)) {
            onUpdate();
        }
        super.setQbdtTransactionInfo(pQbdtTransactionInfo);    
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
    public void setPayrollRun(PayrollRun pPayrollRun) {
        if(!ObjectUtils.equals(getPayrollRun(), pPayrollRun)) {
            onUpdate();
        }
        super.setPayrollRun(pPayrollRun);    
    }

    @Override
    public void addLiabilityCheckLine(LiabilityCheckLine pLiabilityCheckLine) {
        super.addLiabilityCheckLine(pLiabilityCheckLine);
        onUpdate();
    }

    @Override
    public void removeLiabilityCheckLine(LiabilityCheckLine pLiabilityCheckLine) {
        super.removeLiabilityCheckLine(pLiabilityCheckLine);
        onUpdate();
    }

    public void onUpdate() {
        if(getQbdtTransactionInfo() != null) {
            getQbdtTransactionInfo().onUpdate();
        }
    }
}
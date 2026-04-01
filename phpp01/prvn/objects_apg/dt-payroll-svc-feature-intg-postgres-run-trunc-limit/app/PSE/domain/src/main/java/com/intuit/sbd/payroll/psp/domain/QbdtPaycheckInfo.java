package com.intuit.sbd.payroll.psp.domain;

import org.apache.commons.lang.ObjectUtils;

/**
 * Hand-written business logic
 */
public class QbdtPaycheckInfo extends BaseQbdtPaycheckInfo implements IUpdatable {

    /**
     * Default constructor.
     */
    public QbdtPaycheckInfo()
    {
        super();
    }        

    // ----- QBDT Token overrides -----

    @Override
    public void setProrate(boolean pProrate) {
        if(!ObjectUtils.equals(getProrate(), pProrate)) {
            onUpdate();
        }
        super.setProrate(pProrate);
    }

    @Override
    public void setCheckNumber(String pCheckNumber) {
        if(!ObjectUtils.equals(getCheckNumber(), pCheckNumber)) {
            onUpdate();
        }
        super.setCheckNumber(pCheckNumber);
    }

    @Override
    public void setMemo(String pMemo) {
        if(!ObjectUtils.equals(getMemo(), pMemo)) {
            onUpdate();

            // make sure the void messages are not removed
            if(pMemo != null && getMemo() != null) {
                if(getMemo().contains(Paycheck.VOID_FUNDS_RECOVERED) && !pMemo.contains(Paycheck.VOID_FUNDS_RECOVERED)) {
                    pMemo += " " + Paycheck.VOID_FUNDS_RECOVERED;
                } else if(getMemo().contains(Paycheck.VOID_FUNDS_NOT_RECOVERED) && !pMemo.contains(Paycheck.VOID_FUNDS_NOT_RECOVERED)) {
                    pMemo += " " + Paycheck.VOID_FUNDS_NOT_RECOVERED;
                }
            }
        }
        super.setMemo(pMemo);
    }

    @Override
    public void setCleared(String pCleared) {
        if(!ObjectUtils.equals(getCleared(), pCleared)) {
            onUpdate();
        }
        super.setCleared(pCleared);
    }

    @Override
    public void setOnService(boolean pOnService) {
        if(!ObjectUtils.equals(getOnService(), pOnService)) {
            onUpdate();
        }
        super.setOnService(pOnService);
    }

    @Override
    public void setTrackingClass(String pTrackingClass) {
        if(!ObjectUtils.equals(getTrackingClass(), pTrackingClass)) {
            onUpdate();
        }
        super.setTrackingClass(pTrackingClass);
    }

    @Override
    public void setAccountName(String pAccountName) {
        if(!ObjectUtils.equals(getAccountName(), pAccountName)) {
            onUpdate();
        }
        super.setAccountName(pAccountName);
    }

    @Override
    public void setVacationHoursAccrued(double pVacationHoursAccrued) {
        if(!ObjectUtils.equals(getVacationHoursAccrued(), pVacationHoursAccrued)) {
            onUpdate();
        }
        super.setVacationHoursAccrued(pVacationHoursAccrued);
    }

    @Override
    public void setSickHoursAccrued(double pSickHoursAccrued) {
        if(!ObjectUtils.equals(getSickHoursAccrued(), pSickHoursAccrued)) {
            onUpdate();
        }
        super.setSickHoursAccrued(pSickHoursAccrued);
    }

    @Override
    public void setPaycheck(Paycheck pPaycheck) {
        if(!ObjectUtils.equals(getPaycheck(), pPaycheck)) {
            onUpdate();
        }
        super.setPaycheck(pPaycheck);
    }

    @Override
    public void setCompany(Company pCompany) {
        if(!ObjectUtils.equals(getCompany(), pCompany) && pCompany != null) {
            setToken(pCompany.getNextToken());
        }
        super.setCompany(pCompany);
    }

    @Override
    public void setVoidToken(long pVoidToken) {
        if(!ObjectUtils.equals(getVoidToken(), pVoidToken)) {
            if(getPaycheck() != null && "0".equals(getPaycheck().getSourcePaycheckId())) {
                // never update the void token on a paycheck with a 0 source id
                super.setVoidToken(-1);
                return;
            }
        }

        super.setVoidToken(pVoidToken);
    }

    public void onUpdate() {
        if(getCompany() != null) {
            setToken(getCompany().getNextToken());
        }
    }
}
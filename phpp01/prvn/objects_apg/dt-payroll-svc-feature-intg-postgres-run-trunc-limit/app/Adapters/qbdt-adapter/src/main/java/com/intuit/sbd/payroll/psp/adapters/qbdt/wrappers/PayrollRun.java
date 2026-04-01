package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.domain.BillPayment;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import java.util.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 15, 2010
 * Time: 10:57:30 AM
 */
public class PayrollRun {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(PayrollRun.class);

    private IPAYROLLRUN mPayrollRun;

    private HashMap<String, Paycheck> mNewPaychecksMap;
    private HashMap<String, Paycheck> mPaycheckUpdatesMap;
    private HashMap<String, Paycheck> mMigratePaychecksMap;
    private DisburseAdvice mDisburseAdvice;

    public PayrollRun(IPAYROLLRUN pPayrollRun) {
        mPayrollRun = pPayrollRun;
    }

    public boolean hasNewPaychecks() {
        return mPayrollRun.getIPAYCHK() != null && mPayrollRun.getIPAYCHK().size() > 0;
    }

    public String getDeviceSessionID() {
        return mPayrollRun.getISESSIONID();
    }

    public boolean hasPaycheckUpdates() {
        return mPayrollRun.getIPAYCHKMOD() != null && mPayrollRun.getIPAYCHKMOD().size() > 0;
    }

    public boolean hasPaycheckMigrates() {
        return getMigratePaycheckMap().size() > 0;
    }

    public Date getPaycheckDate() {
        return QBOFX.mapOFXStringToDate(mPayrollRun.getIDTPAYCHKS());
    }

    public Collection<Paycheck> getNewPaychecks() {
        return getNewPaychecksMap().values();
    }

    private HashMap<String, Paycheck> getNewPaychecksMap() {
        if(mNewPaychecksMap == null || mNewPaychecksMap.size() < mPayrollRun.getIPAYCHK().size()) {
            // enforce first one in wins rule (i.e. when duplicate paychecks are received in a request)
            mNewPaychecksMap = new HashMap<String,Paycheck>();
            for (IPAYCHK ipaychk :  mPayrollRun.getIPAYCHK()) {
                if(mNewPaychecksMap.get(ipaychk.getIPAYCHKID()) == null) {
                    mNewPaychecksMap.put(ipaychk.getIPAYCHKID(), new Paycheck(ipaychk));
                }
            }
        }
        return mNewPaychecksMap;
    }

    public Set<String> getNewPaycheckIds() {
        return getNewPaychecksMap().keySet();
    }

    public Map<String, String> getNewPaycheckListIdMap() {
        Map<String, String> listIds = new HashMap<String, String>();
        for (Paycheck paycheck : mNewPaychecksMap.values()) {
            if(paycheck.getListId() == null) {
                // either all of the paychecks have list ids or none of them do
                listIds = new HashMap<String, String>();
                break;
            } else {
                listIds.put(paycheck.getListId(), paycheck.getSourceId());
            }
        }
        return listIds;
    }

    public Paycheck findNewPaycheckById(String pPaycheckId) {
        return getNewPaychecksMap().get(pPaycheckId);
    }

    public Collection<Paycheck> getMigratePaychecks() {
        return getMigratePaycheckMap().values();
    }

    private HashMap<String, Paycheck> getMigratePaycheckMap() {
        if (mMigratePaychecksMap == null) {
            mMigratePaychecksMap = new HashMap<String, Paycheck>();
            //nothing is a migrate by default--transient collection
        }
        return mMigratePaychecksMap;
    }

    public Collection<Paycheck> getPaycheckUpdates() {
        return getPaycheckUpdatesMap().values();
    }

    private HashMap<String, Paycheck> getPaycheckUpdatesMap() {
        if(mPaycheckUpdatesMap == null || mPaycheckUpdatesMap.size() < mPayrollRun.getIPAYCHKMOD().size()) {
            // enforce first one in wins rule (i.e. when duplicate paychecks are received in a request)
            mPaycheckUpdatesMap = new HashMap<String,Paycheck>();
            for (IPAYCHK ipaychk :  mPayrollRun.getIPAYCHKMOD()) {

                if(mPaycheckUpdatesMap.get(ipaychk.getIPAYCHKID()) == null) {
                    mPaycheckUpdatesMap.put(ipaychk.getIPAYCHKID(), new Paycheck(ipaychk, true));
                }
            }
        }
        return mPaycheckUpdatesMap;
    }

    public Set<String> getPaycheckUpdateIds() {
        return getPaycheckUpdatesMap().keySet();
    }

    public Paycheck findPaycheckUpdateById(String pPaycheckId) {
        return getPaycheckUpdatesMap().get(pPaycheckId);
    }

    public void markPaychecksAsUpdates(List<String> pPaycheckIds) {
        // update internal structures
        for (Iterator<IPAYCHK> iterator = mPayrollRun.getIPAYCHK().iterator(); iterator.hasNext();) {
            IPAYCHK ipaychk = iterator.next();

            if(pPaycheckIds.contains(ipaychk.getIPAYCHKID())) {
                mPayrollRun.getIPAYCHKMOD().add(ipaychk);
                iterator.remove();
            }
        }

        // update maps
        mNewPaychecksMap = null;
        mPaycheckUpdatesMap = null;
    }

    public void markPaychecksAsNew(Set<String> pPaycheckIds) {

        // update internal structures
        for (Iterator<IPAYCHK> iterator = mPayrollRun.getIPAYCHKMOD().iterator(); iterator.hasNext();) {
            IPAYCHK ipaychk = iterator.next();

            if(pPaycheckIds.contains(ipaychk.getIPAYCHKID())) {
                mPayrollRun.getIPAYCHK().add(ipaychk);
                iterator.remove();
            }
        }

        // update maps
        mNewPaychecksMap = null;
        mPaycheckUpdatesMap = null;
    }

    public void markPaychecksAsMigrate(List<String> pPaycheckIds) {
        for (IPAYCHK ipaychk : mPayrollRun.getIPAYCHK()) {
            if (pPaycheckIds.contains(ipaychk.getIPAYCHKID())) {
                getMigratePaycheckMap().put(ipaychk.getIPAYCHKID(), new Paycheck(ipaychk));
            }
        }
    }

    public DisburseAdvice getDisburseAdvice() {
        if(mDisburseAdvice == null) {
            mDisburseAdvice = new DisburseAdvice(mPayrollRun.getIDISBURSEADVICE(), mPayrollRun.getIDTPAYCHKS());
        }
        return mDisburseAdvice;
    }
}

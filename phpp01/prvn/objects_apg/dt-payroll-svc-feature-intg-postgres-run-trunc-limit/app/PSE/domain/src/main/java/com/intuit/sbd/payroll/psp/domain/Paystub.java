package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Hand-written business logic
 */
public class Paystub extends BasePaystub {

	/**
	 * Default constructor.
	 */
	public Paystub()
	{
		super();
	}

    /*
    public static Paystub findPaystub(Paycheck pPaycheck, int pModTimestamp) {
        NaturalKey naturalKey = new NaturalKey(Paystub.class, pPaycheck.getId(), pModTimestamp);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(Paystub.class, primaryKey);
        } else {
            DomainEntitySet<Paystub> retList = Application.find(Paystub.class, Paystub.Paycheck().equalTo(pPaycheck).And(Paystub.SourceModTime().equalTo(pModTimestamp)));
            if (retList.size() > 1) {
                throw new RuntimeException("Query for paystub by mod time" + pModTimestamp + " did not return 0 or 1 results as expected");
            }

            if (!retList.isEmpty()) {
                Paystub paystub = retList.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, paystub.getId());
                return paystub;
            } else {
                return null;
            }
        }
    }
    */

    public static Paystub findPaystub(Paycheck pPaycheck) {
        NaturalKey naturalKey = new NaturalKey(Paystub.class, pPaycheck.getId());
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(Paystub.class, primaryKey);
        } else {
            DomainEntitySet<Paystub> retList = Application.find(Paystub.class, Paystub.Paycheck().equalTo(pPaycheck));
            if (retList.size() > 1) {
                throw new RuntimeException("Query for paystub by paycheck id" + pPaycheck.getId() + " did not return 0 or 1 results as expected");
            }

            if (!retList.isEmpty()) {
                Paystub paystub = retList.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, paystub.getId());
                return paystub;
            } else {
                return null;
            }
        }
    }

    public static Paystub createPaystub(Paycheck pPaycheck, int pModTimestamp, PstubEmployerInfo pPstubEmployerInfo, PstubEmployeeInfo pPstubEmployeeInfo) {
        return createPaystub(pPaycheck, pModTimestamp, pPstubEmployerInfo, pPstubEmployeeInfo, Boolean.TRUE);
    }

    public static Paystub createPaystub(Paycheck pPaycheck, int pModTimestamp, PstubEmployerInfo pPstubEmployerInfo, PstubEmployeeInfo pPstubEmployeeInfo, boolean saveObject) {
        Paystub createdPaystub = new Paystub();

        createdPaystub.setPaycheck(pPaycheck);
        createdPaystub.setCompany(pPaycheck.getCompany());
        createdPaystub.setSourceModTime(pModTimestamp);
        createdPaystub.setPstubEmployerInfo(pPstubEmployerInfo);
        createdPaystub.setPstubEmployeeInfo(pPstubEmployeeInfo);

        if(saveObject){
            Application.save(createdPaystub);
        }

        NaturalKey naturalKey = new NaturalKey(Paystub.class, pPaycheck.getId());
        Application.getSessionCache().addPrimaryKey(naturalKey, createdPaystub.getId());

        return createdPaystub;
    }
}
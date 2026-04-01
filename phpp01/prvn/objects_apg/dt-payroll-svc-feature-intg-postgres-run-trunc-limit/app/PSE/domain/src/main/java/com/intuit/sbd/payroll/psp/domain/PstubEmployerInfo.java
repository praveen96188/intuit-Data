package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Hand-written business logic
 */
public class PstubEmployerInfo extends BasePstubEmployerInfo {

	/**
	 * Default constructor.
	 */
	public PstubEmployerInfo()
	{
		super();
	}

    public void delete() {
        if (getPstubAddress() != null) {
            Application.delete(getPstubAddress());
        }
        Application.delete(this);
    }

    public static PstubEmployerInfo findPstubEmployerInfo(String pName, String pObjHash) {
        NaturalKey naturalKey = new NaturalKey(PstubEmployerInfo.class, pName, pObjHash);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(PstubEmployerInfo.class, primaryKey);
        } else {
            DomainEntitySet<PstubEmployerInfo> retList = Application.find(PstubEmployerInfo.class, PstubEmployerInfo.Name().equalTo(pName).And(PstubEmployerInfo.ObjectHash().equalTo(pObjHash)));
            if (retList.size() > 1) {
                throw new RuntimeException("Query for pstubEmployer by name " + pName + " did not return 0 or 1 results as expected");
            }

            if (!retList.isEmpty()) {
                PstubEmployerInfo pstubEmployer = retList.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, pstubEmployer.getId());
                return pstubEmployer;
            } else {
                return null;
            }
        }
    }

    public static PstubEmployerInfo createPstubEmployerInfo(String pName, String pObjHash) {
        PstubEmployerInfo createdPstubEmployerInfo = new PstubEmployerInfo();

        createdPstubEmployerInfo.setName(pName);
        createdPstubEmployerInfo.setObjectHash(pObjHash);

        Application.save(createdPstubEmployerInfo);

        NaturalKey naturalKey = new NaturalKey(PstubEmployerInfo.class, pName, pObjHash);
        Application.getSessionCache().addPrimaryKey(naturalKey, createdPstubEmployerInfo.getId());

        return createdPstubEmployerInfo;
    }
}
package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Hand-written business logic
 */
@Slf4j
public class PstubEmployeeInfo extends BasePstubEmployeeInfo {

	/**
	 * Default constructor.
	 */
	public PstubEmployeeInfo()
	{
		super();
	}

    public void delete() {
        if (getPstubAddress() != null) {
            Application.delete(getPstubAddress());
        }
        Application.delete(this);
    }

    public static PstubEmployeeInfo findPstubEmployeeInfo(Employee pEmployee, int pModTimestamp) {
        NaturalKey naturalKey = new NaturalKey(PstubEmployeeInfo.class, pEmployee.getId(), pModTimestamp);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            return Application.findById(PstubEmployeeInfo.class, primaryKey);
        } else {
            DomainEntitySet<PstubEmployeeInfo> retList = Application.find(PstubEmployeeInfo.class, PstubEmployeeInfo.Employee().equalTo(pEmployee).And(PstubEmployeeInfo.SourceModTime().equalTo(pModTimestamp)));
            if (retList.size() > 1) {
                throw new RuntimeException("Query for pstubEmployee by mod time" + pModTimestamp + " did not return 0 or 1 results as expected");
            }

            if (!retList.isEmpty()) {
                PstubEmployeeInfo pstubEmployee = retList.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, pstubEmployee.getId());
                return pstubEmployee;
            } else {
                return null;
            }
        }
    }

    public static PstubEmployeeInfo createPstubEmployeeInfo(Employee pEmployee, int pModTimestamp){
        return createPstubEmployeeInfo(pEmployee, pModTimestamp, Boolean.TRUE);
    }

    public static PstubEmployeeInfo createPstubEmployeeInfo(Employee pEmployee, int pModTimestamp, boolean saveObject) {
        PstubEmployeeInfo createdPstubEmployeeInfo = new PstubEmployeeInfo();

        createdPstubEmployeeInfo.setEmployee(pEmployee);
        createdPstubEmployeeInfo.setSourceModTime(pModTimestamp);

        // Company Fk population
        if(Objects.isNull(pEmployee.getCompany())){
            log.info("Action=company_fk_population_validation, company_fk is null");
        }
        createdPstubEmployeeInfo.setCompany(pEmployee.getCompany());

        if(saveObject){
            Application.save(createdPstubEmployeeInfo);
        }

        NaturalKey naturalKey = new NaturalKey(PstubEmployeeInfo.class, pEmployee.getId(), pModTimestamp);
        Application.getSessionCache().addPrimaryKey(naturalKey, createdPstubEmployeeInfo.getId());

        return createdPstubEmployeeInfo;
    }
}
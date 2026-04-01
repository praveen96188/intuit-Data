package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class EmployeeLawQtrTotals extends BaseEmployeeLawQtrTotals {

	/**
	 * Default constructor.
	 */
	public EmployeeLawQtrTotals()
	{
		super();
	}

    public static SpcfMoney getPreviousQuartersTaxableWages(SpcfUniqueId pEmployeeId, int pYear, int pQuarter, String pLawId) {

        String[] paramNames = new String[4];
        Object[] paramValues = new Object[4];

        paramNames[0] = "law";
        paramNames[1] = "employee";
        paramNames[2] = "year";
        paramNames[3] = "quarter";

        paramValues[0] = pLawId;
        paramValues[1] = pEmployeeId.toString();
        paramValues[2] = pYear;
        paramValues[3] = pQuarter;

        List<SpcfMoney> retList = Application.executeNamedQuery("findPreviousQuarterEmployeeTaxableWage", paramNames, paramValues);
        
        if (!retList.isEmpty() && retList.get(0) != null) {
            return retList.get(0);
        }

        return SpcfMoney.ZERO;
    }

    public static Map<String, SpcfMoney> getPreviousQuartersTaxableWages(SpcfUniqueId pEmployeeId, int pYear, int pQuarter) {

        Map<String, SpcfMoney> previousTaxableWagesByLaw = new HashMap<String, SpcfMoney>();
        String[] paramNames = new String[3];
        Object[] paramValues = new Object[3];

        paramNames[0] = "employee";
        paramNames[1] = "year";
        paramNames[2] = "quarter";

        paramValues[0] = pEmployeeId.toString();
        paramValues[1] = pYear;
        paramValues[2] = pQuarter;

        List<Object[]> retList = Application.executeNamedQuery("findPreviousQuarterEmployeeTaxableWageByLaw", paramNames, paramValues);

        if (!retList.isEmpty()) {
            for (Object[] values : retList) {
                if(values[0] != null && values[1] != null) {
                    previousTaxableWagesByLaw.put((String) values[0], (SpcfMoney) values[1]);
                }
            }
        }

        return previousTaxableWagesByLaw;
    }

}
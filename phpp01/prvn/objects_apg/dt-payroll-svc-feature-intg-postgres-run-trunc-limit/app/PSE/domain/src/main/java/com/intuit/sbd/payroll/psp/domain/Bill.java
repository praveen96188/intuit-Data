package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.Query;

import java.util.*;

/**
 * Hand-written business logic
 */
public class Bill extends BaseBill {

	/**
	 * Default constructor.
	 */
	public Bill() {
		super();
	}

    public static Bill findBill(CompanyUsage pCompanyUsage, SpcfCalendar pBillDate) {
        Bill foundBill = null;

        CalendarUtils.clearTime(pBillDate);

        NaturalKey naturalKey = new NaturalKey(Bill.class, pCompanyUsage.getId(), pBillDate);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            foundBill = Application.findById(Bill.class, primaryKey);
        } else {
            DomainEntitySet<Bill> bills = Application.find(Bill.class, Bill.BillDate().equalTo(pBillDate).And(Bill.CompanyUsage().equalTo(pCompanyUsage)));

            if (bills.size() > 1) {
                throw new RuntimeException("Query for bills by billing date" + pBillDate + " and company " + pCompanyUsage + " did not return 0 or 1 results as expected");
            }

            if (!bills.isEmpty()) {
                foundBill = bills.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, foundBill.getId());
            }
        }

        return foundBill;
    }

    public static DomainEntitySet<Bill> findBillsInDateRange(CompanyUsage pCompanyUsage, SpcfCalendar pBillStartDate,SpcfCalendar pBillEndDate) {
        CalendarUtils.clearTime(pBillStartDate);
        CalendarUtils.clearTime(pBillEndDate);

        DomainEntitySet<Bill> bills = Application.find(Bill.class,
                                                       Bill.BillDate().greaterOrEqualThan(pBillStartDate).
                                                       And(Bill.BillDate().lessOrEqualThan(pBillEndDate)).
                                                       And(Bill.CompanyUsage().equalTo(pCompanyUsage)).
                                                       And(Bill.Closed().equalTo(true)));
        return bills;
    }


	public static Bill findBill(CompanyUsage pCompanyUsage, SpcfCalendar pBillDate, boolean pClosed) {
		Bill foundBill = findBill(pCompanyUsage, pBillDate);
        if (foundBill == null || foundBill.getClosed() != pClosed) {
            return null;
        } else {
            return foundBill;
        }
	}

	public static Bill createBill(CompanyUsage pCompanyUsage, SpcfCalendar pBillDate) {
		Bill createdBill = new Bill();
        CalendarUtils.clearTime(pBillDate);

		createdBill.setCompanyUsage(pCompanyUsage);
		createdBill.setBillDate(pBillDate);
		createdBill.setUsageCount(0);
		createdBill.setSynchedCount(0);
        createdBill.setClosed(false);

		Application.save(createdBill);

		NaturalKey naturalKey = new NaturalKey(Bill.class, pCompanyUsage.getId(), pBillDate);
		Application.getSessionCache().addPrimaryKey(naturalKey, createdBill.getId());

		return createdBill;
	}

	public static Bill findOpenBillOrCreate(CompanyUsage pCompanyUsage, SpcfCalendar pBillDate, SpcfCalendar pNextBillDate) {
		Bill aBill = findBill(pCompanyUsage, pBillDate);
		if (aBill == null) {
			aBill = createBill(pCompanyUsage, pBillDate);
		} else if (aBill.getClosed()) {
            aBill = findBill(pCompanyUsage, pNextBillDate, false);
            if (aBill == null) {
                aBill = createBill(pCompanyUsage, pNextBillDate);
            }
        }

		return aBill;
	}

	// only synch the current open bill with BRM
	public static List<SpcfUniqueId> findOpenBillsOnDate(SpcfCalendar pBillDate) {
        CalendarUtils.clearTime(pBillDate);

        Query queryObject = Application.getNamedQuery("findUsageBillsOnDate");
        queryObject.setParameter("billDate", pBillDate);

        List<SpcfUniqueId> billIds = queryObject.list();
		return billIds;
	}

    public static Map<CompanyKey, Set<SpcfUniqueId>> findOpenBillsByCompanyDuring(SpcfCalendar pStartDate, SpcfCalendar pEndDate) {
        Map<CompanyKey, Set<SpcfUniqueId>> billMap = new HashMap<CompanyKey, Set<SpcfUniqueId>>();

        ArrayList<Object[]> returnObjects = Application.executeQuery(Bill.class, new com.intuit.sbd.payroll.psp.query.Query<Bill>()
                .Select(Bill.CompanyUsage().SourceCompanyId(), Bill.CompanyUsage().SourceSystemCd(), Bill.Id())
                .Where(Bill.Closed().equalTo(false)
                           .And(Bill.BillDate().greaterOrEqualThan(pStartDate))
                           .And(Bill.BillDate().lessOrEqualThan(pEndDate))));

        for (Object[] returnObject : returnObjects) {
            String sourceCompanyId = (String) returnObject[0];
            SourceSystemCode sourceSystemCode = (SourceSystemCode) returnObject[1];
            SpcfUniqueId billId = (SpcfUniqueId) returnObject[2];

            CompanyKey key = new CompanyKey(sourceCompanyId, sourceSystemCode);
            if (!billMap.containsKey(key)) {
                billMap.put(key, new HashSet<SpcfUniqueId>());
            }
            billMap.get(key).add(billId);
        }

        return billMap;
    }

    public static class CompanyKey {
        public String sourceCompanyId;
        public SourceSystemCode sourceSystemCode;

        public CompanyKey(String pSourceCompanyId, SourceSystemCode pSourceSystemCode) {
            sourceCompanyId = pSourceCompanyId;
            sourceSystemCode = pSourceSystemCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompanyKey that = (CompanyKey) o;

            if (!sourceCompanyId.equals(that.sourceCompanyId)) return false;
            if (sourceSystemCode != that.sourceSystemCode) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = sourceCompanyId.hashCode();
            result = 31 * result + sourceSystemCode.hashCode();
            return result;
        }
    }


    public static List<Object[]> findBillDetailsByLicenceNumber(SpcfCalendar pBillDate,String pLicenseId) {
        String[] paramNames = new String[2];
        paramNames[0] = "licenseId";
        paramNames[1] = "billDate";

        Object[] paramValues = new Object[2];
        CalendarUtils.clearTime(pBillDate);
        paramValues[0] = pLicenseId;
        paramValues[1] = pBillDate;
        List<Object[]> results = Application.executeNamedQuery("findBillDetailsByBillDateAndLicenseNumber", paramNames, paramValues);

        return results;
    }

    public static List<Object[]> findBillDetails(SpcfUniqueId pBillId) {
        UsagePeriod foundUsagePeriod = null;
        String[] paramNames = new String[1];
        paramNames[0] = "billId";

        Object[] paramValues = new Object[1];
        paramValues[0] = pBillId;
        List<Object[]> results = Application.executeNamedQuery("findBillDetailsForBill", paramNames, paramValues);

        return results;
    }

	public int increaseUsageCount() {
		setUsageCount(getUsageCount() + 1);
		return getUsageCount();
	}

	public int decreaseUsageCount() {
		int oldCount = getUsageCount();
		if (oldCount > 0) {
			setUsageCount(oldCount - 1);
		}
		return getUsageCount();
	}

    public boolean isForEntitlement(Entitlement pEntitlement) {
        return getCompanyUsage().getLicenseId().equals(pEntitlement.getLicenseNumber()) &&
                getCompanyUsage().getEntitlementId().equals(pEntitlement.getEntitlementOfferingCode());
    }
}

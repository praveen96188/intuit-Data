package com.intuit.sbd.payroll.psp.batchjobs.billing;

import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Tiger Shao
 * Date: 4/26/12
 * Time: 2:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class MockingBRM {
	private static MockingBRM mSingleton = null;
	private TreeMap<String, Integer> mCompanyBillingData = null;

	private MockingBRM() {
		mCompanyBillingData = new TreeMap<String, Integer>();
	}

	public static MockingBRM getInstance() {
		if (mSingleton == null) {
			synchronized (MockingBRM.class) {
				if (mSingleton == null) {
					mSingleton = new MockingBRM();
				}
			}
		}

		return mSingleton;
	}

	public synchronized void acceptNewCharge(String pCompanyId, int pNewCharge) {
		if (mCompanyBillingData.containsKey(pCompanyId)) {
			mCompanyBillingData.put(pCompanyId, pNewCharge + mCompanyBillingData.get(pCompanyId));
		} else {
			mCompanyBillingData.put(pCompanyId, pNewCharge);
		}
		System.out.println("BRM\tCompany\t" + pCompanyId + "\tNewCharge\t" + String.valueOf(pNewCharge) + "\tTotal\t" + mCompanyBillingData.get(pCompanyId).toString());
	}

    public synchronized int getCharge(String pCompanyId) {
        return mCompanyBillingData.get(pCompanyId);
    }

	public synchronized void reset() {
		mCompanyBillingData = new TreeMap<String, Integer>();
	}

	public synchronized void showAllBills() {
		System.out.println();
		for (String companyId : mCompanyBillingData.navigableKeySet()) {
			System.out.println("BRM\tCompany\t" + companyId + "\tTotal\t" + mCompanyBillingData.get(companyId).toString());
		}
	}

    public static void main(String[] args) {
		MockingBRM tBRM = MockingBRM.getInstance();
		tBRM.acceptNewCharge("comp1", 2);
		tBRM.acceptNewCharge("comp2", 3);
		tBRM.acceptNewCharge("comp1", 3);
		tBRM.showAllBills();
	}
}

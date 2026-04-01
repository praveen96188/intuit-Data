package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class AsstBundleBillDetail extends BaseAsstBundleBillDetail {

	/**
	 * Default constructor.
	 */
	public AsstBundleBillDetail()
	{
		super();
	}

	public static void associateAssistedBundleBillWithBillingDetail(AssistedBundleBill pAssistedBundleBill, BillingDetail pBillingDetail) {
		AsstBundleBillDetail asstBundleBillDetail = new AsstBundleBillDetail();
		asstBundleBillDetail.setAssistedBundleBill(pAssistedBundleBill);
		asstBundleBillDetail.setBillingDetailId(pBillingDetail.getId().toString());
		Application.save(asstBundleBillDetail);
	}
}
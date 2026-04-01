package com.intuit.sbd.payroll.psp.processes.billing;

import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/16/12
 * Time: 5:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompanyDTO {
	private SourceSystemCode mSourceSystemCode;
	private String mSourceCompanyId;
	private String mEntitlementId;
	private String mLicenseId;
	private int mBillingDayOfMonth;
	private int mStartDayOfUsageMonth;

	public CompanyDTO(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pEntitlementId, String pLicenseId, int pBillingDayOfMonth, int pStartDayOfUsageMonth) {
		mSourceSystemCode = pSourceSystemCode;
		mSourceCompanyId = pSourceCompanyId;
		mEntitlementId = pEntitlementId;
		mLicenseId = pLicenseId;
		mBillingDayOfMonth = pBillingDayOfMonth;
		mStartDayOfUsageMonth = pStartDayOfUsageMonth;
	}

	public String getSourceCompanyId() {
		return mSourceCompanyId;
	}

	public void setSourceCompanyId(String pSourceCompanyId) {
		mSourceCompanyId = pSourceCompanyId;
	}

	public String getEntitlementId() {
		return mEntitlementId;
	}

	public void setEntitlementId(String pEntitlementId) {
		mEntitlementId = pEntitlementId;
	}

	public String getLicenseId() {
		return mLicenseId;
	}

	public void setLicenseId(String pLicenseId) {
		mLicenseId = pLicenseId;
	}

	public SourceSystemCode getSourceSystemCode() {
		return mSourceSystemCode;
	}

	public void setSourceSystemCode(SourceSystemCode pSourceSystemCode) {
		mSourceSystemCode = pSourceSystemCode;
	}

	public int getBillingDayOfMonth() {
		return mBillingDayOfMonth;
	}

	public void setBillingDayOfMonth(int pBillingDayOfMonth) {
		mBillingDayOfMonth = pBillingDayOfMonth;
	}

	public int getStartDayOfUsageMonth() {
		return mStartDayOfUsageMonth;
	}

	public void setStartDayOfUsageMonth(int pStartDayOfUsageMonth) {
		mStartDayOfUsageMonth = pStartDayOfUsageMonth;
	}

	// to do: validate all fields
	public ProcessResult validate() {
		ProcessResult validationResult = new ProcessResult();

		if ((mSourceCompanyId == null) || !(Validator.isValidLength(mSourceCompanyId, 1, 50))) {
			validationResult.getMessages().InvalidValue(EntityName.Company, "CompanyDTO", "SourceCompanyId");
		}

		if ((mSourceSystemCode == null) || !(Validator.isValidLength(mSourceSystemCode.toString(), 1, 10))) {
			validationResult.getMessages().InvalidValue(EntityName.Company, "CompanyDTO", "SourceSystemCode");
		}

		if (mBillingDayOfMonth < 1 || mBillingDayOfMonth > 31) {
			validationResult.getMessages().InvalidValue(EntityName.Company, "CompanyDTO", "BillingDayOfMonth");
		}

		if (mStartDayOfUsageMonth < 1 || mStartDayOfUsageMonth > 31) {
			validationResult.getMessages().InvalidValue(EntityName.Company, "CompanyDTO", "BillingDayOfMonth");
		}

		return validationResult;
	}
}

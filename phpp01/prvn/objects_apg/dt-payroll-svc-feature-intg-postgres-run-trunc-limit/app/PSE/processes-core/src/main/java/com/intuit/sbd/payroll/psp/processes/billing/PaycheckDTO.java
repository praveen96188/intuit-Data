package com.intuit.sbd.payroll.psp.processes.billing;

import com.intuit.sbd.payroll.psp.domain.ReasonForFreeChargeCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/16/12
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaycheckDTO {
	private String mPaycheckListID;
	private SpcfCalendar mPaycheckDate;
	private String mCheckNumber;
	private String mTransactionID;
	private boolean mPaycheckStatusActive;
    private ReasonForFreeChargeCode mReasonForFreeCharge;
    private boolean mPaycheckCreatedDateLessThanBillingStartDate;
	private String mSessionID;

	public PaycheckDTO(String pPaycheckListID, SpcfCalendar pPaycheckDate, String pCheckNumber, String pTransactionID, boolean pPaycheckStatusActive, ReasonForFreeChargeCode pReasonForFreeCharge,
					   boolean pPaycheckCreatedDateLessThanBillingStartDate) {
        mPaycheckListID = pPaycheckListID;
		mPaycheckDate = pPaycheckDate;
		mCheckNumber = pCheckNumber;
		mTransactionID = pTransactionID;
		mPaycheckStatusActive = pPaycheckStatusActive;
        mReasonForFreeCharge = pReasonForFreeCharge;
        mPaycheckCreatedDateLessThanBillingStartDate = pPaycheckCreatedDateLessThanBillingStartDate;
	}

    public String getPaycheckListID() {
        return mPaycheckListID;
    }

    public void setPaycheckListID(String pPaycheckListID) {
        mPaycheckListID = pPaycheckListID;
    }

    public SpcfCalendar getPaycheckDate() {
		return mPaycheckDate;
	}

	public void setPaycheckDate(SpcfCalendar pPaycheckDate) {
		mPaycheckDate = pPaycheckDate;
	}

	public String getCheckNumber() {
		return mCheckNumber;
	}

	public void setCheckNumber(String pCheckNumber) {
		mCheckNumber = pCheckNumber;
	}

	public String getTransactionID() {
		return mTransactionID;
	}

	public void setTransactionID(String pTransactionID) {
		mTransactionID = pTransactionID;
	}

	public boolean isPaycheckStatusActive() {
		return mPaycheckStatusActive;
	}

	public void setPaycheckStatusActive(boolean pPaycheckStatusActive) {
		mPaycheckStatusActive = pPaycheckStatusActive;
	}

    public ReasonForFreeChargeCode getReasonForFreeCharge() {
        return mReasonForFreeCharge;
    }

    public void setReasonForFreeCharge(ReasonForFreeChargeCode pReasonForFreeCharge) {
        mReasonForFreeCharge = pReasonForFreeCharge;
    }

	public boolean isPaycheckCreatedDateLessThanBillingStartDate() {
		return mPaycheckCreatedDateLessThanBillingStartDate;
	}

	public void setPaycheckCreatedDateLessThanBillingStartDate(boolean mPaycheckCreatedDateLessThanBillingStartDate) {
		this.mPaycheckCreatedDateLessThanBillingStartDate = mPaycheckCreatedDateLessThanBillingStartDate;
	}

	// to do: validate all fields
	public ProcessResult validate() {
		ProcessResult validationResult = new ProcessResult();

		if ((mPaycheckListID == null) || !(Validator.isValidLength(mPaycheckListID, 1, 50))) {
			validationResult.getMessages().InvalidValue(EntityName.Paycheck, "PaycheckDTO", "paycheckListID");
		}

		if (mPaycheckDate == null) {
			validationResult.getMessages().InvalidValue(EntityName.Paycheck, "PaycheckDTO", "paycheckDate");
		}

		return validationResult;
	}

	public String getSessionID() {
		return mSessionID;
	}

	public void setSessionID(String mSessionID) {
		this.mSessionID = mSessionID;
	}
}

package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

/**
 * Hand-written business logic
 */
public class ReportingAgent extends BaseReportingAgent {
	public static String FedTaxIdKeyName="RptAgent_FedTaxId";
	public static String FedIdKeyName="RptAgent_FedId";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ReportingAgent()
	{
		super();
	}

	public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
	}


	public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
	}

	public void setFedId(String pFedId) {
        super.setFedIdEnc(EncryptionUtils.deterministicEncrypt(FedIdKeyName,pFedId));
	}

	public String getFedId() {
        return EncryptionUtils.deterministicDecrypt(FedIdKeyName,getFedIdEnc());
	}
}
package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

/**
 * Hand-written business logic
 */
public class ACHEnrollmentDetail extends BaseACHEnrollmentDetail {
	public static String FeinKeyName="ACHEnrlmnt_Dtl_EIN";
	public static String AgencyIdKeyName="ACHEnrlmnt_Dtl_AID";


	/**
	 * Default constructor.
	 */
	public ACHEnrollmentDetail()
	{
		super();
	}

       public void setFEIN(String pFEIN) {
           super.setFeinEnc(EncryptionUtils.deterministicEncrypt(FeinKeyName,pFEIN));
       }


       public String getFEIN() {
               return EncryptionUtils.deterministicDecrypt(FeinKeyName,getFeinEnc());
       }

       public void setAgencyId(String pAgencyId) {
           super.setAgencyIdEnc(EncryptionUtils.deterministicEncrypt(AgencyIdKeyName,pAgencyId));
       }


       public String getAgencyId() {
               return EncryptionUtils.deterministicDecrypt(AgencyIdKeyName,getAgencyIdEnc());
       }

}
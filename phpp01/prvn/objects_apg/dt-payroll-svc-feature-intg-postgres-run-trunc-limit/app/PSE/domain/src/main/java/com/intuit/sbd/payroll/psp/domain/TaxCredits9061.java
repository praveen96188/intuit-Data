package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;

import javax.persistence.Entity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class TaxCredits9061 extends BaseTaxCredits9061 {
    public static String FedTaxIdKeyName="TaxCredit_FedTaxId";
    public static String TaxCredit9061KeyName="TaxCredit_SSN";

	/**
	 * Default constructor.
	 */
	public TaxCredits9061()
	{
		super();
	}

    public byte[] get9061Bytes() {

        if (getForm9061() == null) {
            return null;
        }

        try {
            return getForm9061().getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Unable to decode 9061 form for " + getFedTaxId() + "/" + getEmployeeName());
        }
    }

    public void set9061Bytes(byte[] pValue) {
        setForm9061(new String(pValue));
    }

    public void setFedTaxId(String pFedTaxId) {
        super.setFedTaxIdEnc(EncryptionUtils.deterministicEncrypt(FedTaxIdKeyName,pFedTaxId));
    }


    public String getFedTaxId() {
        return EncryptionUtils.deterministicDecrypt(FedTaxIdKeyName,getFedTaxIdEnc());
    }

    public void setSSN(String pSSN)
    {
        super.setSSNEnc(EncryptionUtils.deterministicEncrypt(TaxCredit9061KeyName,pSSN));
    }

    public String getSSN() {
        return EncryptionUtils.deterministicDecrypt(TaxCredit9061KeyName,getSSNEnc());
    }
}
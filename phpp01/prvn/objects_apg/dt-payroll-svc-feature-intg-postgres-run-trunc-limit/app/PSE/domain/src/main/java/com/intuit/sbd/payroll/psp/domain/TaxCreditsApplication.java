package com.intuit.sbd.payroll.psp.domain;


import javax.persistence.Entity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class TaxCreditsApplication extends BaseTaxCreditsApplication {

	/**
	 * Default constructor.
	 */
	public TaxCreditsApplication()
	{
		super();
	}

    public byte[] getUnsignedDocumentBytes() {

        if (getUnsignedDocument() == null) {
            return null;
        }

        try {
            return getUnsignedDocument().getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Unable to decode unsigned document form for " + getDocumentKey());
        }
    }

    public void setUnsignedDocumentBytes(byte[] pValue) {
        setUnsignedDocument(new String(pValue));
    }

    public byte[] getSignedDocumentBytes() {

        if (getSignedDocument() == null) {
            return null;
        }

        try {
            return getSignedDocument().getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Unable to decode signed document form for " + getDocumentKey());
        }
    }

    public void setSignedDocumentBytes(byte[] pValue) {
        setSignedDocument(new String(pValue));
    }

}
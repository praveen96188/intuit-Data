package com.intuit.sbd.payroll.psp.domain;

import org.hibernate.Hibernate;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.persistence.Entity;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Hand-written business logic
 */
@Entity // Annotate the class with @Entity for compile time BytecodeEnhancement for attribute lazy loading
public class SourceSystemPrintedCheckInfo extends BaseSourceSystemPrintedCheckInfo {

	/**
	 * Default constructor.
	 */
	public SourceSystemPrintedCheckInfo()
	{
		super();
	}

    public byte[] getBankLogoImage() {
       String bankLogo = getBankLogo();
        if (Objects.isNull(bankLogo)) {
            return null;
        } else {
            BASE64Decoder base64 = new BASE64Decoder();
            try {
                return base64.decodeBuffer(bankLogo);
            } catch (Exception x) {
                throw new RuntimeException("Unable to decode document image for RepositoryDocument with ID " + getId());
            }
        }
    }

    public void setBankLogoAsImage(byte[] pValue) {
        if (pValue == null) {
            setBankLogo(null);
        } else {
            BASE64Encoder base64 = new BASE64Encoder();
            String encodedBytes = base64.encodeBuffer(pValue);
            setBankLogo(encodedBytes);
        }
    }

    public byte[] getSourceSystemLogoImage() {
        String  sourceSystemLogo = getSourceSystemLogo();
        if (Objects.isNull(sourceSystemLogo)) {
            return null;
        } else {
            BASE64Decoder base64 = new BASE64Decoder();
            try {
                return base64.decodeBuffer(sourceSystemLogo);
            } catch (Exception x) {
                throw new RuntimeException("Unable to decode document image for RepositoryDocument with ID " + getId());
            }
        }
    }

    public void setSourceSystemLogoAsImage(byte[] pValue) {
        if (pValue == null) {
            setSourceSystemLogo(null);
        } else {
            BASE64Encoder base64 = new BASE64Encoder();
            String encodedBytes = base64.encodeBuffer(pValue);
            setSourceSystemLogo(encodedBytes);
        }
    }
}
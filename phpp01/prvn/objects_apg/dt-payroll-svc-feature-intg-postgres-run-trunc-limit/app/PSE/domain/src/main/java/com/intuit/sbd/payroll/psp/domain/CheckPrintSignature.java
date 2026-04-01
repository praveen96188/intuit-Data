package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

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
public class CheckPrintSignature extends BaseCheckPrintSignature {

    /**
     * Default constructor.
     */
    public CheckPrintSignature()
    {
        super();
    }

    public byte[] getSignatureImage() {
        String signature = getSignature();
        if (Objects.isNull(signature)) {
            return null;
        } else {
            BASE64Decoder base64 = new BASE64Decoder();
            try {
                return base64.decodeBuffer(signature);
            } catch (Exception x) {
                throw new RuntimeException("Unable to decode document image for RepositoryDocument with ID " + getId());
            }
        }
    }


    public void setSignatureAsImage(byte[] pValue) {
        if (pValue == null) {
            super.setSignature(null);
        } else {
            BASE64Encoder base64 = new BASE64Encoder();
            String encodedBytes = base64.encodeBuffer(pValue);
            setSignature(encodedBytes);
        }
    }

    public static CheckPrintSignature findCheckPrintSignature(Company pCompany) {
        Expression<CheckPrintSignature> query =
                new Query<CheckPrintSignature>()
                        .Where(CheckPrintSignature.Company().equalTo(pCompany))
                        .OrderBy(CheckPrintSignature.CreatedDate());

        DomainEntitySet<CheckPrintSignature> checkPrintSignatures  = Application.find(CheckPrintSignature.class, query);

        if(checkPrintSignatures.size() > 0) {
            return checkPrintSignatures.get(0);
        }

        return null;
    }

}
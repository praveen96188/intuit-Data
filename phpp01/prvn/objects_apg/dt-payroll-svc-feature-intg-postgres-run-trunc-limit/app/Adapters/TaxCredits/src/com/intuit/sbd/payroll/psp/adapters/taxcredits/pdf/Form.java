package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

import com.intuit.sbd.payroll.psp.Application;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 14, 2010
 * Time: 10:47:48 AM
 */
public abstract class Form {
    // checkbox values
    public static final String CHECKED = "Yes";
    public static final String UNCHECKED = "";

    protected boolean shouldFlattenFile = true;

    public abstract String getFileName();

    public byte[] generateForm(Map<String, String> formValues) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(Application.findFileOnClassPath(getFileName()));
        // removing the rights gets rid of a acrobat message telling the user the file was edited
        reader.removeUsageRights();
        PdfStamper stamper = new PdfStamper(reader, byteArrayOutputStream);
        AcroFields form = stamper.getAcroFields();
        for (String key : formValues.keySet()) {
            form.setField(key, formValues.get(key));
        }

        // remove the ability to edit fields
        stamper.setFormFlattening(shouldFlattenFile);
        if (shouldFlattenFile) {
            for (Object key : form.getFields().keySet()) {
                String keyString = (String) key;
                if (! keyString.contains("_es_")) {
                    stamper.partialFormFlattening(keyString);
                }                
            }            
        }
        stamper.close();

        return byteArrayOutputStream.toByteArray();
    }
}

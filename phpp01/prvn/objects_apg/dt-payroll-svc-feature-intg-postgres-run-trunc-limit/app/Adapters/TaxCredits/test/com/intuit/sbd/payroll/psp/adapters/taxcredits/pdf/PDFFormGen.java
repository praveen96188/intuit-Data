package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

import com.intuit.sbd.payroll.psp.Application;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 13, 2010
 * Time: 3:54:37 PM
 */
public class PDFFormGen {

    public PDFFormGen() {
    }

    public static void main(String[] args) {
        try {
            File newDirectory = new File("C:\\dev\\PSP\\main\\Adapters\\TaxCredits\\test\\resources\\new");
            if (! newDirectory.exists()) {
                newDirectory.mkdir();
            }
            generateForms(newDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //generateFields("resources/Youth_Self_Attestation_Form.pdf");
    }

    public static void generateForms(File outputDirectory) throws Exception {
        generateForm(outputDirectory, PDFFormTests.generateTest2848(), new Form2848());
        generateForm(outputDirectory, PDFFormTests.generateTest8850(), new Form8850());
        generateForm(outputDirectory, PDFFormTests.generateTest9061(), new Form9061());
        generateForm(outputDirectory, PDFFormTests.generateTestCoverPage(), new CoverPage());        
        generateForm(outputDirectory, PDFFormTests.generateTestCTJS182(), new CTJS182());
        generateForm(outputDirectory, PDFFormTests.generateTestSummaryPage(), new SummaryPage());
        generateForm(outputDirectory, PDFFormTests.generateTestYouthSelfAttestation(), new YouthSelfAttestation());
    }

    private static void generateForm(File outputDirectory, byte[] formBytes, Form form) throws Exception {
        FileOutputStream fos = new FileOutputStream(new File(outputDirectory, "test_" + form.getFileName().split("/")[1]));
        fos.write(formBytes);
        fos.close();
    }

    public static void generateFields(String fileName) {
        try {
            PdfReader reader = new PdfReader(Application.findFileOnClassPath(fileName));
            reader.removeUsageRights();
            PdfStamper stamper = new PdfStamper( reader,
                    new ByteArrayOutputStream());
            AcroFields form = stamper.getAcroFields();
            HashMap<String, AcroFields.Item> fields = form.getFields();                        

            Object[] keyArray = fields.keySet().toArray();
            Arrays.sort(keyArray);
            String key;
            for (Object o : keyArray) {
                key = (String)o;
                System. out. println("public static final String " + key.toUpperCase().replace(" ","_") +" = \"" + key + "\";");
            }
            stamper.setFormFlattening(true);

            stamper.close();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}

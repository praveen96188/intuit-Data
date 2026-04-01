package com.intuit.sbd.payroll.psp.adapters.taxcredits.pdf;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 18, 2010
 * Time: 2:28:49 PM
 */
public class CoverPage extends Form {
    // pdf file
    public static final String FILE_NAME = "resources/CoverPage.pdf";

    public static final String POSTMARK_DATE = "postmark_date";
    
    public String getFileName() {
        return FILE_NAME;
    }
}

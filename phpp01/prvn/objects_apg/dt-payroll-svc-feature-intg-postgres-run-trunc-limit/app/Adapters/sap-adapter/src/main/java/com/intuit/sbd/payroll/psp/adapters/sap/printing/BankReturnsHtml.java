package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.BankReturnAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturn;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import org.owasp.encoder.Encode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Aug 3, 2008
 * Time: 4:11:35 PM
 */

@SuppressWarnings({"deprecation"})
public class BankReturnsHtml extends BaseHTML {


    public BankReturnsHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    public String getTitle() {
        return "Bank Returns Report";
    }

    public String getBody() throws Throwable {
        StringBuffer sb = new StringBuffer();
        // get search paramaters
        String fein = nullOptionalValue(mRequest.getParameter("fein"));
        String fromDateString = mRequest.getParameter("from");
        String toDateString = mRequest.getParameter("to");
        boolean showOpen = "true".equals(mRequest.getParameter("showOpen"));
        boolean showResolved = "true".equals(mRequest.getParameter("showResolved"));
        String transactionType = mRequest.getParameter("transactionType");
        String transactionCategory = mRequest.getParameter("transactionCategory");
        boolean exclude5Day = "true".equals(mRequest.getParameter("exclude5DayFunding"));
        String excludeCode = mRequest.getParameter("excludeCode");
        String achAmountString = nullOptionalValue(mRequest.getParameter("achAmount"));

        String reportType= mRequest.getParameter("reportType");

        Date fromDate = (fromDateString == null || fromDateString.length() == 0 || "null".equals(fromDateString)) ? null : new Date(fromDateString);
        Date toDate = (toDateString == null || toDateString.length() == 0 || "null".equals(toDateString)) ? null : new Date(toDateString);
        double achAmount = (achAmountString == null || achAmountString.length() == 0 || "null".equals(achAmountString)) ? SAPTranslator.DEFAULT_NUMBER_VALUE: Double.parseDouble(achAmountString);

        String reportName = "";

        //Report Name mapping
        if("AllReturns".equals(reportType)) {
            reportName = "All Returns";
        } else if("FRG".equals(reportType)) {
            reportName = "Financial Resolutions";
        } else if("RiskAssessment".equals(reportType)) {
            reportName = "Risk Assessment";
        } else if("RiskCollections".equals(reportType)) {
            reportName = "Risk Collections";
        }

        //Do null checks
        transactionType = nullOptionalValue(transactionType);
        transactionCategory = nullOptionalValue(transactionCategory);
        excludeCode = nullOptionalValue(excludeCode);



        // do search
        BankReturnAdapter bankReturnAdapter = new BankReturnAdapter();

        SAPSearchResults<SAPBankReturn> results = new SAPSearchResults<SAPBankReturn>();

        if("AllReturns".equals(reportType)) {
            results =
                    bankReturnAdapter.findCompanyBankReturns(fein, fromDate, toDate, showOpen, showResolved,
                            transactionType, transactionCategory,
                            exclude5Day, excludeCode, achAmount, null, false, -1, -1, true);
        } else {
            results = bankReturnAdapter.findCompanyBankReturnsByComplexSearch(fein, fromDate, toDate, achAmount,
                    reportType, null, false, -1, -1, true);
        }


        //Reverse null checks
        transactionType = blankOptionalValue(transactionType);
        transactionCategory = blankOptionalValue(transactionCategory);
        excludeCode = blankOptionalValue(excludeCode);
        fromDateString =  blankOptionalValue(fromDateString);
        toDateString = blankOptionalValue(toDateString);
        achAmountString = blankOptionalValue(achAmountString);
        fein = blankOptionalValue(fein);
        fein = Encode.forHtml(fein);

        // style
        String style = "<style type=\"text/css\">\n" +
                "    .label {\n" +
                "        font-weight: bold;\n" +
                "    }\n" +
                "    .bankInfo TH {\n" +
                "        width:70px;\n" +
                "        text-align: center;\n" +
                "        border-bottom: 0px;\n" +
                "        border-right: 0px;\n" +
                "    }\n" +
                "    .bankInfo TD{\n" +
                "        width:70px !important;\n" +
                "        text-align: center !important;\n" +
                "        border-bottom: 0px !important;\n" +
                "    }\n" +
                "    .transactionTable TD, TH{\n" +
                "        border-bottom: 1px black solid;\n" +
                "        border-right: 1px black solid;\n" +
                "        padding: 5px;\n" +
                "    }\n" +
                "    .transactionTable {\n" +
                "        border: 1px black solid;\n" +
                "    }\n" +
                "</style>\n";
        sb.append(style);

        // header
        sb.append("<h2>Bank Returns Report</h2>");


        // add records
        ArrayList<SAPBankReturn> resultSet = results.getReturnsList();

        sb.append("<b>Report Type:</b> ").append(reportName).append("<br/>");
        sb.append("<b>Start Date:</b>").append(Encode.forHtml(fromDateString)).append("<br/>");
        sb.append("<b>End Date:</b>").append(Encode.forHtml(toDateString)).append("<br/>");
        sb.append("<b>EIN:</b>").append(Encode.forHtml(fein)).append("<br/>");
        sb.append("<b>ACH Amount:</b>").append(Encode.forHtml(achAmountString)).append("<br/>");
        if("AllReturns".equals(reportType)) {
            sb.append("<b>Exclude C return Codes:</b> ").append(excludeCode.length() > 0).append("<br/>");
            sb.append("<b>Exclude 5 Day Funding:</b> ").append(exclude5Day).append("<br/>");
            sb.append("<b>Show:</b> ");
            sb.append( (!showOpen && !showResolved) ? "All Returns" : (showOpen) ? "Open Returns" : "Resolved Returns"
            ).append(" , ");
            if(transactionCategory.length() != 0) { sb.append(Encode.forHtml(transactionCategory)); } else { sb.append("All Returns"); }
            sb.append( " ");
            if(transactionType.length() != 0) { sb.append(Encode.forHtml(transactionType)); } else { sb.append(""); }
            sb.append("<br>");
        }


        sb.append("<br>Results Returned: ").append(resultSet.size()).append("<br><br>");

        sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
                .append("    <tr>\n")
                .append("        <td colspan=\"2\" width=\"100%\">\n")
                .append("            <table class=\"transactionTable\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\n")
                .append("                <tr>\n")
                .append("                    <th>ACH Processing Status<br/><font size=-1>ER Debit Status</font></th>\n");
        if("AllReturns".equals(reportType)) { sb.append("                    <th>Transaction Type</th>\n");    }

        sb.append("                    <th>Company Name<br/><font size=-1>And EE name if EE Return</font></th>\n")
                .append("                    <th nowrap>EIN</th>\n")
                .append("                    <th nowrap>Bank Account #<br/>Bank Routing #</th>\n")
                .append("                    <th nowrap>Return Date<br/>Check Date</th>\n")
                .append("                    <th nowrap>ACH<br/>Return</th>\n")
                .append("                    <th nowrap>Balance</th>\n")
                .append("                    <th nowrap>Return<br/>Code</th>\n")
                .append("                    <th nowrap>Expected<br/>Resolution<br/>Date</th>\n")
                .append("                </tr>\n");





        for(SAPBankReturn bankReturn : resultSet){

            sb.append("                <tr>\n")
                    .append("                    <td align=\"left\">").append(bankReturn.getPayrollStatus() != null ? bankReturn.getPayrollStatus() : "N/A").append("</td>\n");
            if("AllReturns".equals(reportType)) { sb.append("                    <td align=\"left\">").append(bankReturn.getTxnType()).append("</td>\n"); }

            sb.append("                    <td align=\"left\">").append(bankReturn.getCompanyName());
            if(bankReturn.getEmployeeName() != null && bankReturn.getEmployeeName().length() > 0) {
                sb.append("<br/>EE: ").append(bankReturn.getEmployeeName());
            }
            sb.append("</td>")
                    .append("                    <td align=\"left\">").append(bankReturn.getFein()).append("</td>\n")
                    .append("                    <td align=\"left\">").append(maskBankAccountNumber(bankReturn.getBankAccountNumber())).append("<br>").append(bankReturn.getBankRoutingNumber()).append("</td>\n")
                    .append("                    <td align=\"left\">").append(bankReturn.getReturnDate() != null ? mDateFormat.format(bankReturn.getReturnDate()) : "&nbsp;").append("<br>").append(bankReturn.getCheckDate() != null ? mDateFormat.format(bankReturn.getCheckDate()) : "&nbsp").append("</td>\n")
                    .append("                    <td align=\"right\">").append(mNumberFormatter.format(bankReturn.getAmount())).append("</td>\n")
                    .append("                    <td align=\"right\">").append(mNumberFormatter.format(bankReturn.getBankReturnExtendedInfo().getPayrollBalanceDue())).append("</td>\n")
                    .append("                    <td align=\"center\">").append(bankReturn.getReturnCd()).append("</td>\n")
                    .append("                    <td align=\"left\">").append(bankReturn.getBankReturnExtendedInfo().getExpectedResolutionDate() != null ? mDateFormat.format(bankReturn.getBankReturnExtendedInfo().getExpectedResolutionDate()) : "&nbsp").append("</td>\n")
                    .append("                </tr>\n");



        }
        sb.append("            </table>\n")
                .append("        </td>\n")
                .append("    </tr>\n")
                .append("</table>");

        return sb.toString();
    }

    private String nullOptionalValue(String value) {
        return (value == null || value.length() == 0 || "null".equals(value)) ? null : value;
    }

    private String blankOptionalValue(String value) {
        return (value == null || value.length() == 0 || "null".equals(value)) ? "" : value;
    }
}
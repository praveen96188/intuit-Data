package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPChaseReport;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPChaseReportTransaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 2, 2008
 * Time: 11:29:32 AM
 */
@SuppressWarnings({"deprecation"})
public class ChaseReportHtml extends BaseHTML {


    public ChaseReportHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    public String getTitle() {
        return "Chase Report";
    }

    public String getBody() throws Throwable {
        StringBuffer sb = new StringBuffer();
            // get search paramaters
            String sourceSystem = mRequest.getParameter("sourcesystem");
            String companyId = mRequest.getParameter("companyid");

            String fromDateStr = mRequest.getParameter("from");
            Date fromDate = null;
            if(fromDateStr != null && fromDateStr.length() > 0){
                fromDate = new Date(fromDateStr);
            }

            String toDateStr = mRequest.getParameter("to");
            Date toDate = null;
            if(toDateStr != null && toDateStr.length() > 0){
                toDate = new Date(toDateStr);
            }

            // do search
            PayrollRunAdapter payrollAdapter = new PayrollRunAdapter();

            ArrayList<SAPChaseReport> records =
                    payrollAdapter.findChaseReportForDateRange(sourceSystem,
                            companyId,
                            fromDate,
                            toDate);

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
            sb.append("<h2>Chase Report</h2>");
            // add records
            for(SAPChaseReport chaseReport : records){
                sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">\n")
                        .append("    <tr>\n")
                        .append("        <td class=\"label\" width=\"150\">Sent from Client:</td>\n")
                        .append("        <td>").append(
                        (chaseReport.getConnectionDate()!= null) ? mTimeDateFormat.format(chaseReport.getConnectionDate()) : "&nbsp;"
                ).append("</td>\n")
                        .append("    </tr>\n")
                        .append("    <tr>\n")
                        .append("        <td class=\"label\">Posting Date:</td>\n")
                        .append("        <td>").append(mDateFormat.format(chaseReport.getPostingDate())).append("</td>\n")
                        .append("    </tr>\n")
                        .append("    <tr>\n")
                        .append("        <td class=\"label\">Company:</td>\n")
                        .append("        <td>").append(chaseReport.getCompanyName()).append("</td>\n")
                        .append("    </tr>\n")
                        .append("    <tr>\n")
                        .append("        <td class=\"label\">Source System:</td>\n")
                        .append("        <td>").append(chaseReport.getSourceSystem()).append("</td>\n")
                        .append("    </tr>\n")
                        .append("</table>\n")
                        .append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
                        .append("    <tr>\n")
                        .append("        <td colspan=\"2\" width=\"100%\">\n")
                        .append("            <table class=\"transactionTable\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">\n")
                        .append("                <tr>\n")
                        .append("                    <th>Release<br/>Date</th>\n")
                        .append("                    <th>Debit</th>\n")
                        .append("                    <th>Credit</th>\n")
                        .append("                    <th nowrap>Transferred From</th>\n")
                        .append("                    <th nowrap>Transfered To</th>\n")
                        .append("                    <th style=\"padding: 0px; !important\">\n")
                        .append("                        <table class=\"bankInfo\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n")
                        .append("                            <tr><th colspan=\"2\" style=\"border-bottom: 1px black solid; width: 100% !important;\">From Bank Account</th></tr>\n")
                        .append("                            <tr><th style=\"border-right: 1px black solid;\">Routing</th><th>Account</th></tr>\n")
                        .append("                        </table>\n")
                        .append("                    </th>\n")
                        .append("                    <th style=\"padding: 0px; border-right:0px; !important\">\n")
                        .append("                        <table class=\"bankInfo\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n")
                        .append("                            <tr><th colspan=\"2\" style=\"border-bottom: 1px black solid; width: 100% !important;\">To Bank Account</th></tr>\n")
                        .append("                            <tr><th style=\"border-right: 1px black solid;\">Routing</th><th>Account</th></tr>\n")
                        .append("                        </table>\n")
                        .append("                    </th>\n")
                        .append("                </tr>\n");

                // add transactions
                double debitTotal = 0;
                double creditTotal = 0;
                // there is a blank row that we want to skip
                for(int i = 0; i < chaseReport.getTransactions().size() - 1; i++){
                    SAPChaseReportTransaction transaction  =  chaseReport.getTransactions().get(i);
                    debitTotal += transaction.getDebitAmount();
                    creditTotal += transaction.getCreditAmount();
                    sb.append("                <tr>\n")
                            .append("                    <td align=\"center\">").append(mDateFormat.format(transaction.getSettlementDate())).append("</td>\n")
                            .append("                    <td align=\"right\">").append(
                            (transaction.getDebitAmount() > 0) ? mNumberFormatter.format(transaction.getDebitAmount()) : "&nbsp;"
                    ).append("</td>\n")
                            .append("                    <td align=\"right\">").append(
                            (transaction.getCreditAmount() > 0) ? mNumberFormatter.format(transaction.getCreditAmount()) : "&nbsp;"
                    ).append("</td>\n")
                            .append("                    <td align=\"left\">").append(transaction.getDebitAccountName()).append("</td>\n")
                            .append("                    <td align=\"left\">").append(transaction.getCreditAccountName()).append("</td>\n")
                            .append("                    <td style=\"padding: 0px; !important\" height=\"100%\">\n")
                            .append("                        <table class=\"bankInfo\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n")
                            .append("                            <tr>\n")
                            .append("                                <td style=\"border-right: 1px black solid;\">").append(transaction.getDebitAccountRoutingNumber()).append("</td>\n")
                            .append("                                <td style=\"border-right: 0px !important;\">").append(maskBankAccountNumber(transaction.getDebitAccountNumber())).append("</td>\n")
                            .append("                            </tr>\n")
                            .append("                        </table>\n")
                            .append("                    </td>\n")
                            .append("                    <td style=\"padding: 0px; border-right:0px; !important\" height=\"100%\">\n")
                            .append("                        <table class=\"bankInfo\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" height=\"100%\">\n")
                            .append("                            <tr>\n")
                            .append("                                <td style=\"border-right: 1px black solid;\">").append(transaction.getCreditAccountRoutingNumber()).append("</td>\n")
                            .append("                                <td style=\"border-right: 0px !important;\">").append(maskBankAccountNumber(transaction.getCreditAccountNumber())).append("</td>\n")
                            .append("                            </tr>\n")
                            .append("                        </table>\n")
                            .append("                    </td>\n")
                            .append("                </tr>\n");
                }
                sb.append("                <tr>\n")
                        .append("                    <td style=\"border-bottom: 0px\" align=\"right\">Total</td>\n")
                        .append("                    <td style=\"border-bottom: 0px\" align=\"right\">$").append(mCurrencyFormatter.format(debitTotal)).append("</td>\n")
                        .append("                    <td style=\"border-bottom: 0px\" align=\"right\">$").append(mCurrencyFormatter.format(creditTotal)).append("</td>\n")
                        .append("                    <td style=\"border-bottom: 0px\">&nbsp;</td>\n")
                        .append("                    <td style=\"border-bottom: 0px\">&nbsp;</td>\n")
                        .append("                    <td style=\"border-bottom: 0px\">&nbsp;</td>\n")
                        .append("                    <td style=\"border-bottom: 0px; border-right:0px;\">&nbsp;</td>\n")
                        .append("                </tr>\n")
                        .append("            </table>\n")
                        .append("        </td>\n")
                        .append("    </tr>\n")
                        .append("</table>");
            }

        return sb.toString();
    }

}

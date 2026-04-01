package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEmployeeTaxLedgerItem;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLedgerItemDetailsCriterion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * User: ihannur
 * Date: 11/26/12
 * Time: 3:34 PM
 */
public class TaxLedgerItemDetailsReportHtml extends BaseHTML {

    public TaxLedgerItemDetailsReportHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullSSN) {
        super(request, response, pCanViewFullSSN);
    }

    @Override
    public String getTitle() {
        return "Ledger Item Detail";
    }

    @Override
    public String getBody() throws Throwable {

        String detailsCriterionAmfString = mRequest.getParameter("detailsCriterion");
        SAPLedgerItemDetailsCriterion ledgerItemDetailsCriterion = new AmfSerializer().fromAmf(detailsCriterionAmfString);

        StringBuilder sb = new StringBuilder();
        TaxAdapter taxAdapter = new TaxAdapter();

        ArrayList<SAPEmployeeTaxLedgerItem> employeeTaxLedgerItems = taxAdapter.findEmployeeLedgerItems(ledgerItemDetailsCriterion);
        boolean showTips = true;
        if (!employeeTaxLedgerItems.isEmpty()) {
            showTips = employeeTaxLedgerItems.get(0).getShowTaxTips();
        }

        sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
          .append("    <tr>\n")
          .append("                    <th nowrap>Employee Name</th>\n")
          .append("                    <th nowrap>SSN</th>\n")
          .append("                    <th nowrap>Total Wages</th>\n")
          .append("                    <th nowrap>Taxable Wage</th>\n");
        if (showTips) {
            sb.append("                    <th nowrap>Tips</th>\n");
        }
        sb.append("                    <th nowrap>Tax Amount</th>\n")
          .append("                </tr>\n");


        for (SAPEmployeeTaxLedgerItem employeeTaxLedgerItem : employeeTaxLedgerItems) {
            sb.append("<tr>")
              .append("<td align=\"left\">").append(employeeTaxLedgerItem.getEmployeeName()).append("</td>\n")
              .append("<td align=\"right\">").append(maskBankAccountNumber(employeeTaxLedgerItem.getSocialSecurityNumber())).append("</td>\n")
              .append("<td align=\"right\">").append(employeeTaxLedgerItem.getTotalWages()).append("</td>\n")
              .append("<td align=\"right\">").append(employeeTaxLedgerItem.getTaxableWages()).append("</td>\n");
            if (employeeTaxLedgerItem.getShowTaxTips()) {
                sb.append("<td align=\"right\">").append(employeeTaxLedgerItem.getTaxTips()).append("</td>\n");
            }
            sb.append("<td align=\"right\">").append(employeeTaxLedgerItem.getTaxAmount()).append("</td>\n")
              .append("</tr>");
        }

        return sb.toString();
    }

}

package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLawTransactions;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPTaxTransaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Mar 7, 2011
 * Time: 2:00:51 PM
 */
public class TaxLedgerReportHtml extends BaseHTML{


    public TaxLedgerReportHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    public String getTitle() {
        return "Tax Ledger";
    }

    public String getBody()  throws Throwable {
        StringBuffer sb = new StringBuffer();

            String sourceSystemCd =  mRequest.getParameter("companyCd");
            String companyId = mRequest.getParameter("companyId");
            String agencyId = mRequest.getParameter("agencyId");
            String type = mRequest.getParameter("type");
            String paymentType = mRequest.getParameter("paymentTemplate");
            String taxType = mRequest.getParameter("taxType");
            String paymentMethod = mRequest.getParameter("paymentMethod");
            String startDate = mRequest.getParameter("startDate");
            String endDate = mRequest.getParameter("endDate");
            String includePending = mRequest.getParameter("includePending");
            String selectedAgencyId = mRequest.getParameter("selectedAgencyId");
            String selectedLawId = mRequest.getParameter("selectedLawId");

            TaxAdapter taxAdapter = new TaxAdapter();


            ArrayList<SAPLawTransactions> lawTransactionses = taxAdapter.findTaxTransactions(sourceSystemCd, companyId, type, agencyId, paymentType, taxType, paymentMethod, new Date(startDate), new Date(endDate), Boolean.parseBoolean(includePending));

            sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
                .append("    <tr>\n")
                .append("                    <th nowrap>Type</th>\n")
                .append("                    <th nowrap>Status</th>\n")
                .append("                    <th nowrap>Submission Date</th>\n")
                .append("                    <th nowrap>Check/Payment Date</th>\n")
                .append("                    <th nowrap>Payment Method</th>\n")
                .append("                    <th nowrap>Current Taxes</th>\n")
                .append("                    <th nowrap>Current Wages</th>\n")
                .append("                    <th nowrap>QTD Taxes</th>\n")
                .append("                    <th nowrap>QTD Wages</th>\n")
                .append("                    <th nowrap>YTD Taxes</th>\n")
                .append("                    <th nowrap>YTD Wages</th>\n")
                .append("                </tr>\n");

            for (SAPLawTransactions lawTransaction : lawTransactionses) {
                if(selectedAgencyId.equals(lawTransaction.getAgency().getAgencyId()) && (selectedLawId.equals(lawTransaction.getLaw().getLawId()) || (selectedLawId.equals("") && lawTransaction.getLaw().getLawId() == null))){
                    for (SAPTaxTransaction sapTaxTransaction : lawTransaction.getTaxTransactions()) {
                        sb.append("<tr>")
                            .append("<td align=\"left\">").append(sapTaxTransaction.getTxnDescription()).append("</td>\n")
                            .append("<td align=\"right\">").append(sapTaxTransaction.getPaymentStatus()).append("</td>\n")
                            .append("<td align=\"right\">").append(sapTaxTransaction.getSubmissionDate()!=null ? sapTaxTransaction.getSubmissionDate():"").append("</td>\n")
                            .append("<td align=\"right\">").append(sapTaxTransaction.getCheckPaymentDate()).append("</td>\n")
                            .append("<td align=\"right\">").append(sapTaxTransaction.getPaymentMethod() != null ? sapTaxTransaction.getPaymentMethod():"").append("</td>\n")
                            .append("<td align=\"right\">").append(sapTaxTransaction.getCurrentTaxes()).append("</td>\n");
                        if(!sapTaxTransaction.getTxnDescription().equals("Payment")){
                            if(lawTransaction.getLaw().getLawId() != null){
                                sb.append("<td align=\"right\">").append(sapTaxTransaction.getCurrentWages()).append("</td>\n");
                            }else{
                                sb.append("<td align=\"right\"></td>\n");
                            }
                            sb.append("<td align=\"right\">").append(sapTaxTransaction.getQTDTaxes()).append("</td>\n");
                            if(lawTransaction.getLaw().getLawId() != null){
                                sb.append("<td align=\"right\">").append(sapTaxTransaction.getQTDWages()).append("</td>\n");
                            }else{
                                sb.append("<td align=\"right\"></td>\n");
                            }
                            sb.append("<td align=\"right\">").append(sapTaxTransaction.getYTDTaxes()).append("</td>\n");
                            if(lawTransaction.getLaw().getLawId() != null){
                                sb.append("<td align=\"right\">").append(sapTaxTransaction.getYTDWages()).append("</td>\n");
                            }else{
                                sb.append("<td align=\"right\"></td>\n");
                            }
                        }
                        sb.append("</tr>");
                    }
                }
            }
        return sb.toString();
    }
}

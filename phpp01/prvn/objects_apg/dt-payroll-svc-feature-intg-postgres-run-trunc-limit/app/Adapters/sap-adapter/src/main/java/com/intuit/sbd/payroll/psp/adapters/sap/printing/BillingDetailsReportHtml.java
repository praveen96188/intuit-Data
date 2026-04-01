package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.BillingHistoryAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingDetail;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingEmployeeDetail;
import org.owasp.encoder.Encode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: nloharuka
 * Date: Aug 18, 2017
 * Time: 11:29:32 AM
 */
@SuppressWarnings({"deprecation"})
public class BillingDetailsReportHtml extends BaseHTML {


    public BillingDetailsReportHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    public String getTitle() {
        return "Billing Details Report";
    }

    public String getBody() throws Throwable {
        StringBuffer sb = new StringBuffer();
        // get search paramaters
        String sourceSystemCd = mRequest.getParameter("sourceSystemCd");
        String companyId = mRequest.getParameter("companyId");

        String billDateStr = mRequest.getParameter("billDate");
        Date billDate = null;
        if (billDateStr != null && billDateStr.length() > 0) {
            billDate = new Date(billDateStr);
        }

        String viewAllFlag = mRequest.getParameter("isViewAll");
        Boolean viewAll = new Boolean(viewAllFlag);

        // do search
        BillingHistoryAdapter billingAdapter = new BillingHistoryAdapter();

        SAPUsageBillingDetail billingDetail = billingAdapter.findBillingDetails(companyId, sourceSystemCd, billDate, viewAll);

        //date format
        DateFormat dFormat=new SimpleDateFormat("MM/dd/yyyy");

        sb.append("<table width=\"100%\" cellspacing=\"0\">")
                .append("<tr><td>Company ID: ").append(Encode.forHtml(companyId)).append("</td>")
                .append("<td>Source System Cd: ").append(Encode.forHtml(sourceSystemCd)).append("</td></tr>")
                .append("<tr><td>Usage Period: ").append(dFormat.format(billingDetail.getUsagePeriodStartDate()))
                .append(" - ").append(dFormat.format(billingDetail.getUsagePeriodEndDate())).append("</td>")
                .append("<td>Bill Date: ").append(dFormat.format(billDate)).append("</td></tr>")
                .append("<tr><td>Is Multi EIN: ").append(billingDetail.getIsMultiEin()).append("</td>")
                .append("<td>View All: ").append(viewAll).append("</td></tr>")
                .append("<tr><td>No. of Companies Billed: ").append(billingDetail.getNumCompaniesBilled()).append("</td></tr>")
                .append("<tr><td>No. of Employees Billed: ").append(billingDetail.getNumEmployeesBilled()).append("</td></tr>")
                .append("<tr><td></td></tr></table>")
                .append("<table width=\"100%\" cellspacing=\"0\" border=\"1\">")
                .append("<tr>")
                .append("<th nowrap>Company Legal Name</th>\n")
                .append("<th nowrap>EIN</th>\n")
                .append("<th nowrap>Employee</th>\n")
                .append("<th nowrap>Pay Date</th>\n")
                .append("<th nowrap>Check Number</th>\n")
                .append("</tr>");


        for (SAPUsageBillingEmployeeDetail empDetails : billingDetail.getEmployeeDetails()) {
            sb.append("<tr>")
                    .append("<td align=\"left\">").append(empDetails.getCompanyName()).append("</td>\n")
                    .append("<td align=\"left\">").append(empDetails.getEin()).append("</td>\n")
                    .append("<td align=\"left\">").append(empDetails.getEmployeeName()).append("</td>\n")
                    .append("<td align=\"left\">").append(dFormat.format(empDetails.getPaycheckDate())).append("</td>\n")
                    .append("<td align=\"left\">").append(empDetails.getCheckNumber()).append("</td>\n")
                    .append("</tr>");

        }

        sb.append("</table>");

        return sb.toString();
    }

}
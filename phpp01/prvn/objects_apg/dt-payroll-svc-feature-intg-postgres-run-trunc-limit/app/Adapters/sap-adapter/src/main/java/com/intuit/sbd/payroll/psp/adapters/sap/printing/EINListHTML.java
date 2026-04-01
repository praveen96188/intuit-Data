package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPEntitlementSearchResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * User: dweinberg
 * Date: 10/9/12
 * Time: 9:19 AM
 */
public class EINListHTML extends BaseHTML {


    public EINListHTML(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    @Override
    public String getTitle() {
        return "EIN List";
    }

    @Override
    public String getBody() throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
          .append("    <tr>\n")
          .append("                    <th nowrap>Legal Name</th>\n")
          .append("                    <th nowrap>EIN</th>\n")
          .append("                    <th nowrap>PSID</th>\n")
          .append("                    <th nowrap>Status</th>\n")
          .append("                    <th nowrap>Service Key</th>\n")
          .append("                </tr>\n");


        String licenseNumber =  mRequest.getParameter("licenseNumber");
        String eoc = mRequest.getParameter("eoc");

        ArrayList<SAPEntitlementSearchResult> currentEINs = new CompanyAdapter().findCurrentEINs(licenseNumber, eoc);
        for (SAPEntitlementSearchResult currentEIN : currentEINs) {
            sb.append("<tr>")
              .append("<td align=\"left\">").append(currentEIN.getLegalName()).append("</td>\n")
              .append("<td align=\"right\">").append(currentEIN.getFein()).append("</td>\n")
              .append("<td align=\"right\">").append(currentEIN.getPSID()).append("</td>\n")
              .append("<td align=\"right\">").append(currentEIN.getEntitlementUnitStatus()).append("</td>\n")
              .append("<td align=\"right\">").append(currentEIN.getServiceKey()).append("</td>\n")
              .append("</tr>");
        }

        return sb.toString();
    }
}

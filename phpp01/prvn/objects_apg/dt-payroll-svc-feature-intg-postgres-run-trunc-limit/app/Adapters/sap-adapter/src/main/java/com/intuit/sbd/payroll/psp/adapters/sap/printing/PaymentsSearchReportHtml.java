package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayment;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPaymentSearch;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.MoneyMovementTransaction;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import org.hibernate.FlushMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * User: dweinberg
 * Date: 6/12/12
 * Time: 4:08 PM
 */
public class PaymentsSearchReportHtml extends BaseHTML {

    public PaymentsSearchReportHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    @Override
    public String getTitle() {
        return "Tax Payments";
    }

    @Override
    public String getBody() throws Throwable {
        String paymentSearchAmfString = mRequest.getParameter("paymentSearch");
        SAPPaymentSearch paymentSearch = new AmfSerializer().fromAmf(paymentSearchAmfString);

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        HqlBuilder hql = new TaxAdapter().getTaxPaymentsHQLBuilder(paymentSearch);
        hql.append("order by mmt.InitiationDate, mmt.PaymentTemplate, mmt.Company.SourceCompanyId");
        ArrayList<SAPPayment> payments = new ArrayList<SAPPayment>();
        PSPRequestContextManager pspRequestContextManager = PSPRequestContextManagerHelper.getPSPRequestContextManager();
        for (MoneyMovementTransaction moneyMovementTransaction : hql.<MoneyMovementTransaction>list()) {
            try {
                pspRequestContextManager.setRequestContextCompanyFromSeq(moneyMovementTransaction.getCompany().getId().toString());
                String mmtStatus = TaxAdapter.getSimpleStatus(moneyMovementTransaction);
                payments.add(TaxTranslator.getPayment(moneyMovementTransaction, mmtStatus, null));
                Application.evict(moneyMovementTransaction);
            } finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }
        PayrollServices.rollbackUnitOfWork();

        StringBuilder sb = new StringBuilder();

        sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
          .append("    <tr>\n")
          .append("                    <th nowrap>Holds</th>\n")
          .append("                    <th nowrap>Status</th>\n")
          .append("                    <th nowrap>Company Legal Name</th>\n")
          .append("                    <th nowrap>PSID</th>\n")
          .append("                    <th nowrap>FEIN</th>\n")
          .append("                    <th nowrap>Agency ID</th>\n")
          .append("                    <th nowrap>Initiation Date</th>\n")
          .append("                    <th nowrap>Settlement Date</th>\n")
          .append("                    <th nowrap>Due Date</th>\n")
          .append("                    <th nowrap>Amount</th>\n")
          .append("                    <th nowrap>Payment Method</th>\n")
          .append("                </tr>\n");

        for (SAPPayment sapPayment : payments) {
            sb.append("<tr>")
              .append("<td align=\"left\">").append(Arrays.toString(sapPayment.getHolds().toArray(new String[sapPayment.getHolds().size()]))).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getStatus()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getCompanyName()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getPsId()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getEin()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getAgencyId()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getInitiationDate().toString()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getSettlementDate().toString()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getDueDate().toString()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getAmount().toString()).append("</td>\n")
              .append("<td align=\"right\">").append(sapPayment.getPaymentMethod()).append("</td>\n")
              .append("</tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }
}

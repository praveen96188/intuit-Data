package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPACHEnrollmentDetail;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.ACHEnrollment;
import com.intuit.sbd.payroll.psp.domain.ACHEnrollmentStatus;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.hibernate.FlushMode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * User: ihannur
 * Date: 5/28/13
 * Time: 3:32 PM
 */
public class ACHEnrollmentReportHtml extends BaseHTML {

    public ACHEnrollmentReportHtml(HttpServletRequest request, HttpServletResponse response, boolean pCanViewFullAccountNumber) {
        super(request, response, pCanViewFullAccountNumber);
    }

    @Override
    public String getTitle() {
        return "FL ACH Enrollments";
    }

    @Override
    public String getBody() throws Throwable {
        ACHEnrollmentStatus status = ACHEnrollmentStatus.valueOf(mRequest.getParameter("statusToFind"));

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        DomainEntitySet<ACHEnrollment> enrollments = Application.find(ACHEnrollment.class,
                new Query<ACHEnrollment>().Where(ACHEnrollment.Status().equalTo(status)).EagerLoad(
                        ACHEnrollment.ACHEnrollmentDetail(), ACHEnrollment.CompanyAgency().Company(),
                        ACHEnrollment.CompanyAgency().CompanyAgencyPaymentTemplateSet()));

        ArrayList<SAPACHEnrollmentDetail> enrollmentDetails = new ArrayList<SAPACHEnrollmentDetail>();
        for (ACHEnrollment enrollment : enrollments) {
            enrollmentDetails.add(TaxTranslator.getSAPACHEnrollmentDetailFromDomainEntity(enrollment, enrollment.getACHEnrollmentDetail()));
        }
        PayrollServices.rollbackUnitOfWork();

        StringBuilder sb = new StringBuilder();

        sb.append("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">")
          .append("    <tr>\n")
          .append("                    <th nowrap>Company Legal Name</th>\n")
          .append("                    <th nowrap>PSID</th>\n")
          .append("                    <th nowrap>FEIN</th>\n")
          .append("                    <th nowrap>Agency ID</th>\n")
          .append("                    <th nowrap>Effective Date</th>\n")
          .append("                    <th nowrap>Creation Date</th>\n");
        if (status == ACHEnrollmentStatus.EnrollmentRejected) {
            sb.append("                    <th nowrap>Rejection Reason</th>\n");
        }

        sb.append("                    <th nowrap>Last Update</th>\n")
          .append("                </tr>\n");

        for (SAPACHEnrollmentDetail enrollmentDetail : enrollmentDetails) {
            sb.append("<tr>")
              .append("<td align=\"right\">").append(enrollmentDetail.getCompanyName()).append("</td>\n")
              .append("<td align=\"right\">").append(enrollmentDetail.getCompanyKey().getCompanyId()).append("</td>\n")
              .append("<td align=\"right\">").append(enrollmentDetail.getEin()).append("</td>\n")
              .append("<td align=\"right\">").append(enrollmentDetail.getAid()).append("</td>\n")
              .append("<td align=\"right\">").append(enrollmentDetail.getEffectiveDate().toString()).append("</td>\n")
              .append("<td align=\"right\">").append(enrollmentDetail.getCreationDate().toString()).append("</td>\n");
            if (status == ACHEnrollmentStatus.EnrollmentRejected) {
                sb.append("<td align=\"right\">").append(enrollmentDetail.getRejectionReason()).append("</td>\n");
            }
            sb.append("<td align=\"right\">").append(enrollmentDetail.getModifiedDate().toString()).append("</td>\n")
              .append("</tr>");
        }

        return sb.toString();
    }
}

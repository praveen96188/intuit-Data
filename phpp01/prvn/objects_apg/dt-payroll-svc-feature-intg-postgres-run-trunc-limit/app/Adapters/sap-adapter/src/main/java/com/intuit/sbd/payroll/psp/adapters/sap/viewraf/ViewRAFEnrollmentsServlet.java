package com.intuit.sbd.payroll.psp.adapters.sap.viewraf;

import com.intuit.sbd.payroll.psp.adapters.sap.UserOperationVerifier;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRAFEnrollmentDetail;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPRAFEnrollmentSearch;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.owasp.encoder.Encode;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Oct 6, 2011
 * Time: 2:28:56 PM
 */
public class ViewRAFEnrollmentsServlet extends HttpServlet implements Servlet {
    private static final SpcfLogger logger = PayrollServices.getLogger(ViewRAFEnrollmentsServlet.class);

    public ViewRAFEnrollmentsServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserOperationVerifier verifier = new UserOperationVerifier(request);
        verifier.requireValidUser();
        verifier.requireOperation(OperationId.ViewGlobalEnrollments);
        String psid_ein = request.getParameter("psid_ein");
        String enrollmentStatus = request.getParameter("enrollmentStatus");
        String creationDateStartString = request.getParameter("creationDateStart");
        String creationDateEndString = request.getParameter("creationDateEnd");
        String lastUpdateStartString = request.getParameter("lastUpdateStart");
        String lastUpdateEndString = request.getParameter("lastUpdateEnd");

        Date creationDateStart = null;
        Date creationDateEnd = null;
        Date lastUpdateStart = null;
        Date lastUpdateEnd = null;
        DateFormat formatter;
        formatter = new SimpleDateFormat("MM/dd/yyyy");
        try {
            if (creationDateStartString != null && creationDateStartString.trim().length() > 0) {
                creationDateStart = formatter.parse(creationDateStartString);

            }
            if (creationDateEndString != null && creationDateEndString.trim().length() > 0) {
                creationDateEnd = formatter.parse(creationDateEndString);

            }
            if (lastUpdateStartString != null && lastUpdateStartString.trim().length() > 0) {
                lastUpdateStart = formatter.parse(lastUpdateStartString);

            }
            if (lastUpdateEndString != null && lastUpdateEndString.trim().length() > 0) {
                lastUpdateEnd = formatter.parse(lastUpdateEndString);

            }
        } catch(ParseException e){
            throw new RuntimeException(e);
        }
        PrintWriter writer = null;
        try {
            SAPSearchResults<SAPRAFEnrollmentDetail> enrollments = new TaxAdapter().getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(enrollmentStatus, psid_ein, creationDateStart, creationDateEnd, lastUpdateStart, lastUpdateEnd), false, -1, -1);
            response.setContentType("application/x-msexcel");
            response.setHeader("Content-disposition",
                    "attachment, filename=\"RAF_" + enrollmentStatus + ".xls" + "\"");
            writer = response.getWriter();
            String EOL = System.getProperty("line.separator");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>RAF Enrollment Report for " + Encode.forHtml(enrollmentStatus) + "</title>");
            writer.print("<style type=\"text/css\">\n" +
                    "    .label {\n" +
                    "        font-weight: bold;\n" +
                    "    }\n");
            writer.print("</style>\n");
            writer.println("</head>");
            writer.println("<body> ");
            writer.print("<table width=\"100%\" cellpadding=\"3\" cellspacing=\"0\">" + EOL);
            writer.print("    <tr>\n");
            writer.print("        <td class=\"label\" width=\"200\">Company Name</td>\n");
            writer.print("        <td class=\"label\">PSID</td>\n");
            writer.print("        <td class=\"label\">EIN</td>\n");
            writer.print("        <td class=\"label\">Creation Date</td>\n");
            writer.print("        <td class=\"label\">Last Updated</td>\n");
            writer.print("    </tr>\n");
            for (SAPRAFEnrollmentDetail enrollmentDetail : enrollments.getReturnsList()) {
                writer.print("    <tr>\n");
                writer.print(String.format("    <td>%s</td>\n    <td>%s</td>\n    <td>%s</td>\n    <td>%s</td>\n    <td>%s%s", enrollmentDetail.getCompanyName(), enrollmentDetail.getCompanyKey().getCompanyId(),
                        enrollmentDetail.getEin(), enrollmentDetail.getCreationDate(), enrollmentDetail.getModifiedDate(), EOL));
                writer.print("    </tr>\n");
            }
            writer.print("    </table>\n");
            writer.println("</body>");
            writer.println("</html>");
        }
        catch (Throwable pThrowable) {
            logger.info(pThrowable.toString());
            pThrowable.printStackTrace();
            writer.println("<html>");
            writer.println("<head><title>Error</title></head>");
            writer.println("<body>");
            writer.println("<H1>There was an error building the report! Details: " + pThrowable.getMessage() + "<H1>");
            writer.println("</body>");
            writer.println("</html>");
        }
        finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }
}

package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.BillingHistoryAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.owasp.encoder.Encode;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 1, 2008
 * Time: 7:17:50 PM
 */
public class PrintServlet extends HttpServlet implements Servlet {
    private static final String CHASE_REPORT = "chasereport";
    private static final String BANK_RETURNS_REPORT = "bankreturnsreport";
    private static final String BANK_ACCOUNT_SEARCH_REPORT = "bankAccountSearchReport";
    private static final String IP_BASED_FRAUD_FILTERING_REPORT = "ipBasedFraudFilteringReport";
    private static final String TAX_LEDGER_SEARCH_REPORT = "taxLedgerSearchReport";
    private static final String TAX_PAYMENTS_SEARCH_REPORT = "taxPaymentsSearchReport";
    private static final String EIN_LIST_REPORT = "einListReport";
    private static final String TAX_LEDGER_ITEM_DETAILS_REPORT = "taxLedgerItemDetailsReport";
    private static final String ACH_ENROLLMENT_REPORT = "achEnrollmentReport";
    private static final String BILLING_DETAILS_REPORT = "billingDetailsReport";


    private static final SpcfLogger logger = PayrollServices.getLogger(PrintServlet.class);

    public PrintServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String report = request.getParameter("report");
        PSPRequestContextManager pspRequestContextManager = PSPRequestContextManagerHelper.getPSPRequestContextManager();
        try {
            pspRequestContextManager.setRequestContext(null, RequestType.SAP, report);
            boolean canViewFullAccountNumbers;
            boolean canViewFullSSN;
            try {
                PayrollServices.beginUnitOfWork();
                AuthUser loggedInUser = getLoggedInUser(request);
                if (loggedInUser == null || !loggedInUser.hasAnyOperation(getReportViewOperations(report))) {
                    writeInvalidSessionMessage(response);
                    return;
                }
                canViewFullAccountNumbers = loggedInUser.hasOperation(OperationId.ViewFullBankAccountNumbers);
                canViewFullSSN = loggedInUser.hasOperation(OperationId.ViewEEPII);

            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            String export = request.getParameter("export");

            if(export != null && export.equals("true"))
            {
                response.setContentType("application/x-msexcel");
                request.setAttribute("contentType", "application/x-msexcel");
            } else {
                response.setContentType("text/html");
                request.setAttribute("contentType", "text/html");
            }


            BaseHTML htmlGenerator = null;
            if(report.equals(CHASE_REPORT)){
                htmlGenerator = new ChaseReportHtml(request, response, canViewFullAccountNumbers);
            } else if(report.equals(BANK_RETURNS_REPORT)) {
                htmlGenerator = new BankReturnsHtml(request, response, canViewFullAccountNumbers);
            } else if (report.equals(TAX_LEDGER_SEARCH_REPORT)) {
                htmlGenerator = new TaxLedgerReportHtml(request, response, canViewFullAccountNumbers);
            } else if (report.equals(TAX_PAYMENTS_SEARCH_REPORT)) {
                htmlGenerator = new PaymentsSearchReportHtml(request, response, canViewFullAccountNumbers);
            } else if (report.equals(EIN_LIST_REPORT)) {
                htmlGenerator = new EINListHTML(request, response, canViewFullAccountNumbers);
            } else if (report.equals(TAX_LEDGER_ITEM_DETAILS_REPORT)) {
                htmlGenerator = new TaxLedgerItemDetailsReportHtml(request, response, canViewFullSSN);
            } else if (report.equals(ACH_ENROLLMENT_REPORT)) {
                htmlGenerator = new ACHEnrollmentReportHtml(request, response, canViewFullSSN);
            } else if (report.equals(BILLING_DETAILS_REPORT)) {
                response.addHeader("Content-Disposition", "attachment; filename=\"Billing_Details_Report.xls\"");
                htmlGenerator = new BillingDetailsReportHtml(request, response, canViewFullSSN);
            }

            RequestDispatcher view = null;
            if(report.equals(BANK_ACCOUNT_SEARCH_REPORT)) {
                CompanyAdapter companyAdapter = new CompanyAdapter();
                String routingNumber = request.getParameter("routingNumber");
                String accountNumber = request.getParameter("accountNumber");
                request.setAttribute("routingNumber", routingNumber);
                request.setAttribute("accountNumber", accountNumber);
                request.setAttribute("bankAccounts", companyAdapter.findBankAccounts(routingNumber,
                                                                                     accountNumber,
                                                                                     null, false, -1, -1));
                view = request.getRequestDispatcher("/BankAccountSearchReport.jspx");
                response.addHeader("Content-Disposition", "attachment; filename=\"Bank_Account_Search.xls\"");
            }
            if(report.equals(IP_BASED_FRAUD_FILTERING_REPORT)) {
                CompanyAdapter companyAdapter = new CompanyAdapter();
                String ipAddress = request.getParameter("ipAddress");
                String startDatString = request.getParameter("startDate");
                String endDateString = request.getParameter("endDate");
                request.setAttribute("ipAddress", ipAddress);
                request.setAttribute("startDate", startDatString);
                request.setAttribute("endDate", endDateString);
                Date fromDate = (startDatString == null || startDatString.length() == 0 || "null".equals(startDatString)) ? null : new Date(startDatString);
                Date toDate = (endDateString == null || endDateString.length() == 0 || "null".equals(endDateString)) ? null : new Date(endDateString);
                request.setAttribute("searchResults", companyAdapter.findTransmissionByIPAndDate(ipAddress,fromDate,toDate));
                view = request.getRequestDispatcher("/IPBasedFraudFilteringReport.jspx");
                response.addHeader("Content-Disposition", "attachment; filename=\"IP_Based_Fraud_Filtering.xls\"");
            }

            if(htmlGenerator != null){
                PrintWriter writer = response.getWriter();
                writer.println("<html>");
                writer.println("<head>");
                writer.println("<title>" + htmlGenerator.getTitle() + "</title>");
                writer.println("</head>");
                writer.println("<body onload=\"window.print();\"> ");
                writer.println(htmlGenerator.getBody());
                writer.println("</body>");
                writer.println("</html>");
                writer.close();
            }
            else if(view != null) {
                view.forward(request, response);
            }
            else{
                PrintWriter writer = response.getWriter();
                writer.println("<html>");
                writer.println("<head><title>Error</title></head>");
                writer.println("<body>");
                writer.println("<H1>The report requested does not exist: " + Encode.forHtml(report) + "<H1>");
                writer.println("</body>");
                writer.println("</html>");
                writer.close();
            }
        } catch (Throwable t) {
            logger.error("Error generating " + report + " report", t);
            PrintWriter writer = response.getWriter();
            writer.println("<html>");
            writer.println("<head><title>Error</title></head>");
            writer.println("<body>");
            writer.println("<H1>There was an error building the report! Details: " + t.getMessage() + "<H1>");
            writer.println("</body>");
            writer.println("</html>");
            writer.close();
        } finally {
            pspRequestContextManager.clearRequestContext();
        }
    }

    private AuthUser getLoggedInUser(HttpServletRequest request) {
        String token = request.getParameter("token");
        if(token != null){
            DomainEntitySet<AuthUser> users = Application.find(AuthUser.class, AuthUser.AuthorizationToken().equalTo(token));

            return users.getFirst();
        }
        return null;
    }

    private void writeInvalidSessionMessage(HttpServletResponse response) throws IOException  {
        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Invalid Session</title>");
        writer.println("</head>");
        writer.println("<body> ");
        writer.println("Your session has expired or you do not have access to this report. Please try logging in again.");
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
    }

    private OperationId[] getReportViewOperations(String report) {
        if(report.equals(CHASE_REPORT)){
            return new OperationId[] { OperationId.PrintChaseReport };
        } else if(report.equals(BANK_RETURNS_REPORT)) {
            return new OperationId[] { OperationId.BankReturnView };
        } else if(report.equals(BANK_ACCOUNT_SEARCH_REPORT)) {
            return new OperationId[] { OperationId.ViewSignupFraudQueue };
        } else if (report.equals(IP_BASED_FRAUD_FILTERING_REPORT)) {
            return new OperationId[] { OperationId.IPBasedFraudFilteringView };
        } else if(report.equals(TAX_LEDGER_SEARCH_REPORT)) {
            return new OperationId[] { OperationId.ViewTaxLedger };
        } else if (report.equals(TAX_PAYMENTS_SEARCH_REPORT)) {
            return new OperationId[] { OperationId.ViewGlobalTaxPayments };
        } else if (report.equals(EIN_LIST_REPORT)) {
            return new OperationId[] { OperationId.AccessApplication };
        } else if (report.equals(TAX_LEDGER_ITEM_DETAILS_REPORT)) {
            return new OperationId[] { OperationId.ViewTaxLedger };
        } else if (report.equals(ACH_ENROLLMENT_REPORT)) {
            return new OperationId[] { OperationId.ManageRAFEnrollment, OperationId.ViewGlobalEnrollments };
        } else if (report.equals(BILLING_DETAILS_REPORT)) {
            return new OperationId[] { OperationId.AccessApplication };
        }
        return null;
    }
}


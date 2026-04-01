package com.intuit.sbd.payroll.psp.adapters.sap.viewofx;

import com.intuit.sbd.payroll.psp.adapters.sap.UserOperationVerifier;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.domain.util.PIIMask;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class ViewFullOFXServlet extends HttpServlet implements Servlet {

    private static final SpcfLogger logger = PayrollServices.getLogger(ViewFullOFXServlet.class);

    public ViewFullOFXServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserOperationVerifier verifier = new UserOperationVerifier(request);
        verifier.requireValidUser();
        verifier.requireOperation(OperationId.ViewOFX);
        String transmissionId = request.getParameter("transmissionId");

        PrintWriter out = null;
        try {
            PayrollServices.beginUnitOfWorkWithSecondary();
            SourceSystemTransmission transmission = SourceSystemTransmission.getSourceSystemTransmissionById(transmissionId);
            response.setContentType("text/plain");
            response.setHeader("Content-disposition",
                    "attachment, filename=\"" + "fullOFX.txt" + "\"");
            out = response.getWriter();

            DomainEntityChangeManager.setDomainEntityChangeModelContext(transmission.getClass(), transmission);
            String docString = transmission.getRequestDocument();
            out.print("Request\r\n=====================================================\r\n\r\n");
            docString = PIIMask.getMaskedString(docString, verifier.canPerformOperation(OperationId.ViewFullBankAccountNumbers), verifier.canPerformOperation(OperationId.ViewEEPII)).replaceAll("\n", "\r\n");
            out.print(docString);
            docString = transmission.getResponseDocument();;
            out.print("\r\n\r\nResponse\r\n=====================================================\r\n\r\n");
            docString = PIIMask.getMaskedString(docString, verifier.canPerformOperation(OperationId.ViewFullBankAccountNumbers), verifier.canPerformOperation(OperationId.ViewEEPII)).replaceAll("\n", "\r\n");
            out.print(docString);
        }
        catch (Throwable pThrowable) {
            logger.info(pThrowable.toString());
            pThrowable.printStackTrace();
        }
        finally {
            DomainEntityChangeManager.removeDomainEntityChangeModel();
            PayrollServices.rollbackUnitOfWorkWithSecondary();
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
}

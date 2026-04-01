package com.intuit.sbd.payroll.psp.adapters.sap.printing;

import com.intuit.sbd.payroll.psp.adapters.sap.UserOperationVerifier;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxCreditsAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.OperationId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dweinberg
 * Date: Jan 28, 2010
 * Time: 9:15:14 AM
 */
public class PrintTaxCreditsServlet  extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserOperationVerifier uov = new UserOperationVerifier(request);
        uov.requireValidUser();
        uov.requireOperation(OperationId.TaxCreditsWOTC);

        String formId = request.getParameter("formId");

        if (formId == null) {
            throw new ServletException("formId must be specified");
        }

        String type = request.getParameter("type");
        if (type == null) {
            type = "9061";
        }

        PayrollServices.beginUnitOfWork();

        TaxCreditsAdapter taxCreditsAdapter = new TaxCreditsAdapter();


        byte[] taxForm;
        try {
            if (type.equals("9061")) {
                taxForm = taxCreditsAdapter.findTaxFormBytes(formId);
            } else if (type.equals("unsignedPacket")) {
                taxForm = taxCreditsAdapter.findUnsignedApplicationBytes(formId);
            } else if (type.equals("signedPacket")) {
                taxForm = taxCreditsAdapter.findSignedApplicationBytes(formId);
            } else {
                throw new ServletException("Unrecognized form type of " + type);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        response.setContentType("application/pdf");
        response.getOutputStream().write(taxForm);

        PayrollServices.commitUnitOfWork();

    }

}

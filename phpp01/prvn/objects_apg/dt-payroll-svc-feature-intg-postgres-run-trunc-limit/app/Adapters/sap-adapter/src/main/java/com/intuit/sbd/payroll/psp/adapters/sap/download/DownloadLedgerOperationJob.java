package com.intuit.sbd.payroll.psp.adapters.sap.download;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.UserOperationVerifier;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJob;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: dweinberg
 * Date: 11/13/12
 * Time: 11:26 AM
 */
public class DownloadLedgerOperationJob extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserOperationVerifier uov = new UserOperationVerifier(request);
        uov.requireValidUser();
        uov.requireOperation(OperationId.LedgerOperations);

        String jobId = request.getParameter("jobId");
        boolean useProcessedFile = request.getParameter("processedFile").equals("true");

        PayrollServices.beginUnitOfWork();

        LedgerOperationJob job = Application.findById(LedgerOperationJob.class, SpcfUniqueId.createInstance(jobId));
        String file = useProcessedFile ? job.getProcessedFileString() : job.getOriginalFileString();

        response.setContentType("text/csv");
        response.addHeader("Content-disposition", "attachment;filename=LedgerOperationJob.csv");
        response.getOutputStream().write(file.getBytes());

        PayrollServices.rollbackUnitOfWork();

    }
}

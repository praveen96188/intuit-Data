package com.intuit.sbd.payroll.psp.adapters.sap.download;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.sap.UserOperationVerifier;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.SUICreditsJob;
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
public class DownloadSUICreditsJob extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserOperationVerifier uov = new UserOperationVerifier(request);
        uov.requireValidUser();
        uov.requireOperation(OperationId.ViewOperatorTab);

        String jobId = request.getParameter("jobId");

        PayrollServices.beginUnitOfWork();

        SUICreditsJob job = Application.findById(SUICreditsJob.class, SpcfUniqueId.createInstance(jobId));
        String file = job.getProcessedFileString();
        response.setContentType("text/csv");
        response.addHeader("Content-disposition", "attachment;filename=SUICreditsJob.csv");
        response.getOutputStream().write(file.getBytes());

        PayrollServices.rollbackUnitOfWork();

    }
}

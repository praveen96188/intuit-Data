package com.intuit.sbd.payroll.psp.adapters.sap.upload;

import com.intuit.sbd.payroll.psp.adapters.sap.UserOperationVerifier;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdministrationAdapter;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * User: dweinberg
 * Date: 11/13/12
 * Time: 10:58 AM
 */
public class UploadLedgerOperationJob extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserOperationVerifier uov = new UserOperationVerifier(request);
        uov.requireValidUser();
        uov.requireOperation(OperationId.LedgerOperations);
        String description = request.getParameter("description");
        byte[] fileBinary;
        try {
            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            List /* FileItem */ items = upload.parseRequest(request);

            DiskFileItem uploadedFileItem = (DiskFileItem) items.get(1);
            InputStream uploadedFileIS = uploadedFileItem.getInputStream();
            fileBinary = new byte[uploadedFileIS.available()];
            //noinspection ResultOfMethodCallIgnored
            uploadedFileIS.read(fileBinary);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }

        try {
            new AdministrationAdapter().uploadLedgerOperationsFile(fileBinary, description);
        } catch (Throwable pThrowable) {
            throw new ServletException(pThrowable);
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.write("success");  //should be ignored
    }
}

package com.intuit.sbd.payroll.psp.adapters.sap.download;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by anandp233 on 3/16/14.
 */
public class DownloadTextContent extends HttpServlet {
    private static final int BYTES_DOWNLOAD = 1024;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int read = 0;
        try {
            String textContent = request.getParameter("textContent");
            response.setContentType("text/plain");
            response.addHeader("Content-disposition", "attachment;filename=output.txt");
            response.getOutputStream().write(textContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

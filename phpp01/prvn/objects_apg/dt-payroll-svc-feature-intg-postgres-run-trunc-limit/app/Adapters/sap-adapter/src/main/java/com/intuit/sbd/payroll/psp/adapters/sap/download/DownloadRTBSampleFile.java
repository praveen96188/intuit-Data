package com.intuit.sbd.payroll.psp.adapters.sap.download;

import com.intuit.sbd.payroll.psp.Application;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by anandp233 on 3/11/14.
 */
public class DownloadRTBSampleFile extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String jobName = request.getParameter("jobName").toLowerCase();
        String jobFileName = "/resources/rtb/" + jobName + "template.xls";
        try {
            String filePath = Application.findFileOnClassPath(jobFileName);
            File file = new File(filePath);
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
            response.setContentType("text/xls");
            response.addHeader("Content-disposition", "attachment;filename=" + jobName + "template.xls");
            response.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
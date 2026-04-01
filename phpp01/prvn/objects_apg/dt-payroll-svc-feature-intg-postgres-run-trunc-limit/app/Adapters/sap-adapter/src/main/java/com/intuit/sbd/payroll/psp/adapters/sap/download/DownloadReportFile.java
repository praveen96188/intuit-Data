package com.intuit.sbd.payroll.psp.adapters.sap.download;

import com.intuit.sbd.payroll.psp.adapters.sap.reportDownload.ReportExport;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by: smodgil on 01/18/20.
 * Description: This is a servlet class which downloads the decrypted file from Ec2
 */
public class DownloadReportFile extends HttpServlet {

    private static final SpcfLogger logger = PayrollServices.getLogger(DownloadReportFile.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String reportType = request.getParameter("reportType");
        String date = request.getParameter("date");
        File decryptedFile = null;
        logger.info("Starting to download the report="+reportType+" for date="+date);

        try {
            if(StringUtils.isBlank(reportType)) {
                throw new Exception("Report Type is NULL");
            } else if(StringUtils.isBlank(date)) {
                throw new Exception("Date is NULL");
            }

            ReportExport reportExport = new ReportExport();
            String remoteFileName = reportExport.getFileName(reportType, date);
            decryptedFile = reportExport.exportDecryptedFileFromPSP(remoteFileName);
            byte[] bytes = IOUtils.toByteArray(new FileInputStream(decryptedFile));
            response.setContentType("text/plain");
            response.addHeader("Content-disposition", "attachment;filename="+decryptedFile.getName());
            response.getOutputStream().write(bytes);
            logger.info("File = "+decryptedFile.getName()+" downloaded successfully");
        } catch (IOException e) {
            logger.error("Exception occurred while downloading report file {}", e);
        } catch (Exception e) {
            logger.error("Exception occurred while downloading report file {}", e);
        } finally {
            logger.info("Deleting the file " +decryptedFile.getName() +" after download ");
            decryptedFile.delete();
        }
    }
}
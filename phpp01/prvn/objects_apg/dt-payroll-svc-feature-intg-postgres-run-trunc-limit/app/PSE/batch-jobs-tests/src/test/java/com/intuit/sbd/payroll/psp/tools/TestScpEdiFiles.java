package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ScpEdiVanFileUpload;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Nov 2, 2011
 * Time: 3:00:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestScpEdiFiles {

    private final static String destination =  ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_edi_scp_destination");
    private final static String destinationBackup = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_edi_scp_backup_destination");

    public static void main(String args[]) {
        try {


            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

            System.out.println("Server Name:" + BatchUtils.getTaxAgencyConfigString("psp_edi_scp_server"));
            System.out.println("User name:" + BatchUtils.getTaxAgencyConfigString("psp_edi_scp_username"));
            System.out.println("Destination directory:" + destination);
            System.out.println("Backup destination directory:" + destinationBackup);            

            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");


            // collect connection details and launch example
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Do you want to SCP all pending transmission files (Y/N): ");
            String transmitPendingFiles = reader.readLine();
            if("Y".equals(transmitPendingFiles)) {
                ScpEdiVanFileUpload scpEdiVanFileUpload = new ScpEdiVanFileUpload();
                scpEdiVanFileUpload.upload();
                System.out.print("List of Files Ids transmitted :"+ scpEdiVanFileUpload.getUploadedFileIdList());
            } else {
                System.out.print("Enter Full file name to SCP : ");

                String inputFileName = reader.readLine();

                System.out.println("File name is ============>:" + inputFileName); 

                TestScpEdiFiles testScpEdiFiles = new TestScpEdiFiles();
                testScpEdiFiles.scp(inputFileName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void scp(String pFileName) {
        Transporter sftp = null;
        try {
            sftp = BatchUtils.getEdiVanScpConnection(new JSchAdapter());

            sftp.connect();

            File ediFile = new File(pFileName);
            sftp.uploadFile(pFileName, destination);
            sftp.uploadFile(pFileName, destinationBackup);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(sftp != null) {
                try {
                    sftp.disconnect();
                    System.out.println("*************** Disconnect works  *******************");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private class ScpFileUploadListener extends JSchAdapter {

        public void connected(String scpConnectedEvent) {
            System.out.println("Successfully connected ..");
        }

        public void disconnected(String scpDisconnectedEvent) {
            System.out.println("Successfully Disconnected ..");
        }

        public void download(FileBean scpFileDownloadedEvent) {
            System.out.println("File Downloaded :" + scpFileDownloadedEvent.getFilename());
        }

        public void upload(FileBean scpFileUploadedEvent) {
            System.out.println("File Uploaded :"+ scpFileUploadedEvent.getFilename());
        }

    }
}

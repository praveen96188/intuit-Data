package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.common;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDtoCompanyFileInfo;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkersCompTransporter {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompTransporter.class);
    private Transporter sftp = null;
    public void uploadSftpFile(String hostname,String username,String password,String sendDir,String destDir,
                                     List<PayrollDtoCompanyFileInfo> payrollInfoList) {
        uploadSftpFile(hostname,username,password,sendDir,destDir,payrollInfoList,!WorkersCompGatewayManager.isWCForAllCustomers());
    }
    public void uploadSftpFile(String hostname,String username,String password,String sendDir,String destDir,
                                     List<PayrollDtoCompanyFileInfo> payrollInfoList,boolean markAsSend) {
        try {
            createConnection(hostname, username, password);
            for (PayrollDtoCompanyFileInfo payrollDto : payrollInfoList) {
                try {
                    uploadFile(payrollDto.getFileName().concat(".pgp"), sendDir, destDir);
                    if (markAsSend) {
                        updatePaycheckMarkAsSent(payrollDto);
                    }
                     }catch (Exception e){
                        logger.info("Upload of file failed filename ="+payrollDto.getFileName());
                }
            }
        }catch (Exception e) {
            throw new RuntimeException("Error sending WC payroll files to server (aborting process) "+e);
        }
        finally {
            disconnectConnection();
        }
    }

    public void updatePaycheckMarkAsSent(PayrollDtoCompanyFileInfo payrollDto )// correct name PayrollDtoCompanyFileInfo
    {
        logger.info("updatePaycheckMarkAsSent");
        for (Object paychecks : payrollDto.getPayrollDTO().getIncludedPaychecksByCompany().values()) {
            if (paychecks != null && ((List<WorkersCompPaycheck>)paychecks).size() > 0) {
                WorkersCompPaycheck.markAsSent((List<WorkersCompPaycheck>) paychecks);
            }
        }
    }

    private void  createConnection(String hostname,String username, String password) {
        logger.info("WC SFTP connection initiated...");
        sftp = BatchUtils.getWCConnection(new JSchAdapter(),hostname,username,password);
        try {
            sftp.connect();
        } catch (Exception e) {

            throw new RuntimeException("WC Connection to SFTP failed ", e);
        }
        logger.info("WC SFTP connection created");
    }
    public void uploadFile(String filename,String sendDir,String destinationDir) {
        try {
            sftp.changeLocalDir(sendDir);
            sftp.changeRemoteDir(destinationDir);
            sftp.uploadFile(filename);
        } catch (Exception e) {
            logger.info("sftp Upload of file failed filename=",e,filename);
        }
    }
    private void disconnectConnection() {
        if (sftp != null) {
            try {
                sftp.disconnect();
            } catch (Exception e) {
                logger.error("Error in SFTP Disconnect at WC payroll Upload step", e);
            }
        }
    }
}

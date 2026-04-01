package com.intuit.sbd.payroll.psp.batchjobs.raf;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3DownloadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RAFFileWriter {
    static final int REC_LEN = 402;
    private static final String USAGE = "java " + RAFFileWriter.class.getName() + "filename [batchid]";
    private static final String ADD = "A";
    private static final String DELETE = "D";

    private SpcfLogger logger = Application.getLogger(RAFFileWriter.class);

    public List<SpcfUniqueId> execute() {
        ArrayList<SpcfUniqueId> rafFileGUIDs = new ArrayList<SpcfUniqueId>();
        try {
            logger.info("Executing job step RAFTapeWriter");

            PayrollServices.beginUnitOfWork();
            //get all pending files for initiation
            DomainEntitySet<RAFEnrollmentFile> initiatedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.Initiated);
            for (RAFEnrollmentFile currentFile : initiatedEnrollmentFiles) {
                    boolean anyEnrollmentsForFile = RAFEnrollment.getCountRAFEnrollmentsForActionCode(currentFile.getRAFActionCode()) > 0;
                    if (anyEnrollmentsForFile) {
                        writeFile(currentFile);
                        currentFile.setStatus(RAFFileStatus.Finalized);
                        rafFileGUIDs.add(currentFile.getId());
                        Application.save(currentFile);
                    } else {
                        //If there's nothing to put into the file, just delete it
                        logger.info("File for action code: "+currentFile.getRAFActionCode()+" doesn't have any pending transactions; deleting");
                        Application.delete(currentFile);
                    }
            }

            //get all pending files for re-initiation and move the actual file from the archived directory back to the processing directory
            DomainEntitySet<RAFEnrollmentFile> recreationInitiatedEnrollmentFiles = RAFEnrollmentFile.getRAFFilesByStatus(RAFFileStatus.RecreationInitiated);
            String bucketName = BatchUtils.getConfigString(S3UploadUtils.PSP_BATCHJOBS_S3_BUCKET);
            String processingDirectory = BatchUtils.getConfigString("psp_raf_ftp_srcdir");

            for (RAFEnrollmentFile currentFile : recreationInitiatedEnrollmentFiles) {
                String absoluteFileName = currentFile.getFileName();
                File src_file = new File(absoluteFileName);
                File dest_file = new File(processingDirectory,src_file.getName());

                if(Application.isAWSEnvironment()){
                    String firstChar = absoluteFileName.substring(0,1);
                    String absoluteS3RemoteFileName;

                    if(firstChar.equals(File.separator)){
                        absoluteS3RemoteFileName = absoluteFileName.substring(1);
                    }else {
                        absoluteS3RemoteFileName = absoluteFileName;
                    }

                    absoluteFileName = dest_file.getAbsolutePath();
                    try{
                        S3UploadUtils.downloadFromS3FileStore(bucketName,absoluteS3RemoteFileName,absoluteFileName);
                    }catch (S3ConnectionException e){
                        throw e;
                    }catch (S3DownloadException e){
                        throw e;
                    }
                }else{
                    FileUtils.moveFileTo(src_file,processingDirectory);
                }
                currentFile.setFileName(dest_file.getAbsolutePath());
                rafFileGUIDs.add(currentFile.getId());
            }

            PayrollServices.commitUnitOfWork();

        } catch (Throwable e) {
            logger.error("unable to create tapes", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return rafFileGUIDs;
    }
    public void writeFile(RAFEnrollmentFile file) throws Throwable {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.RAFProcessorBatchJob);
       // PrintStream fileStream = null;
        PrintWriter fileStream = null;

        try {
            //Create tape
            String fileName = createFileName(BatchUtils.getConfigString("psp_raf_ftp_srcdir"), file);
            String emailFileName = fileName+".csv";

            file.setFileName(fileName);
            file.setEmailFileName(emailFileName);
            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            IDPSFileWriter fileWriter = new IDPSFileWriter(new File(fileName),key,"US-ASCII");
            fileStream = new PrintWriter(fileWriter);
            // Write Tape
            writeReportingAgentRecord(fileStream);
            int numberOfAffectedRows = writeTaxpayerRecord(fileStream, file);
            writeEndOfFileTrailer(fileStream, numberOfAffectedRows);
            logger.debug("RAFTapeWriter completed: " + numberOfAffectedRows + " records written.");
        } finally {
            if (fileStream!=null) {
                fileStream.close();
            }
        }
    }

    public void writeReportingAgentRecord(PrintWriter tape) {
        Set<ReportingAgent> agents = Application.findObjects(ReportingAgent.class);
        if (agents == null || agents.size() != 1) {
            throw new RuntimeException("unable to find reporting agent record");
        }
        //
        ReportingAgent agent = agents.iterator().next();
        tape.printf("RA%2.2s%9.9s%-35.35s%-35.35s%-35.35s%-20.20s%-2.2s%9.9s%-9.9s%10.10s%-36.36s%196s\r\n",
                "",
                format(agent.getFedTaxId(), "[0-9]*"),
                format(agent.getLegalName(), "[A-Z0-9& -]*"),
                "",
                format(agent.getAddress().getAddressLine1(), "[A-Z0-9&%/ -]*"),
                format(agent.getAddress().getCity(), "[A-Z0-9 -]*"),
                format(agent.getAddress().getState(), "[A-Z]*"),
                format(agent.getAddress().getZipCode()+agent.getAddress().getZipCodeExtension(), "[0-9]*"),
                format(agent.getPhone(), "[0-9]*"),
                format(agent.getFax(), "[0-9]*"),
                format(agent.getContact(), "[A-Z ]*"),
                "");
    }

    public void writeEndOfFileTrailer(PrintWriter tape, int numberOfRecords) {
        tape.printf("E%06d%393s\r\n", numberOfRecords, "");
    }

    public int writeTaxpayerRecord(PrintWriter tape, RAFEnrollmentFile file) {
        int numberOfRecords = 0;
        int rafSelectSize = SystemParameter.findIntValue(SystemParameter.Code.RAF_ENROLLMENT_SELECT_SIZE);
        DomainEntitySet<RAFEnrollment> rafEnrollments;
        while (true) {
            if(file.getRAFActionCode()==RAFActionCode.Delete) {
                rafEnrollments = RAFEnrollment.getPendingDeleteRAFEnrollmentsOrderedByFEIN(rafSelectSize, file.getRAFActionCode());
            }else {
               rafEnrollments = RAFEnrollment.getPendingRAFEnrollmentsOrderedByFEIN(rafSelectSize, file.getRAFActionCode());
            }
            if (rafEnrollments.size() == 0) {
                break;
            }

            for (RAFEnrollment rafEnrollment : rafEnrollments) {
                writeTaxpayerRecord(tape, rafEnrollment, file);
                numberOfRecords++;
            }
        }
        return numberOfRecords;
    }

    public void writeTaxpayerRecord(PrintWriter tape, RAFEnrollment pRAFEnrollment, RAFEnrollmentFile pFile) {
        RAFEnrollmentDetail enrollmentDetail;

        String actionCode = getActionCode(pFile);
        CompanyAgency companyAgency = pRAFEnrollment.getCompanyAgency();
        Company company = companyAgency.getCompany();
        String year = getYear(company, actionCode);
        String f940TaxPeriod = year+"12";
        String f941TaxPeriod = getQuarterForF941TaxPeriod(company, actionCode);
        String f94xFTDPeriod = getQuarterForF94xFTDPeriod(company, actionCode);

        //If this is an "Add", we want to create the enrollment detail from the existing company information
        //If this is a "Delete", we want to use the enrollment detail we sent with the original enrollment
        if (pFile.getRAFActionCode() == RAFActionCode.Add) {
            enrollmentDetail = new RAFEnrollmentDetail();

            //Save exactly what we wrote to tape to the enrollment detail record
            enrollmentDetail.setFedTaxid(company.getFedTaxId());
            enrollmentDetail.setLegalName(company.getLegalName());
            enrollmentDetail.setLegalStreetAddress(getStreetAddress(company.getLegalAddress()));
            enrollmentDetail.setLegalCity(company.getLegalAddress().getCity());
            enrollmentDetail.setLegalState(company.getLegalAddress().getState());
            enrollmentDetail.setLegalZipCode(company.getLegalAddress().getZipCode());
            enrollmentDetail.setF940TaxPeriod(f940TaxPeriod);
            enrollmentDetail.setF941TaxPeriod(f941TaxPeriod);
            enrollmentDetail.setF94xFTDPeriod(f94xFTDPeriod);

            enrollmentDetail.setRAFEnrollment(pRAFEnrollment);

            enrollmentDetail.setEnrollmentFile(pFile);
            Application.save(enrollmentDetail);

            pRAFEnrollment.setRAFEnrollmentDetail(enrollmentDetail);
            pRAFEnrollment.updateEnrollmentStatus(RAFEnrollmentStatus.PendingEnrollmentResponse);
        } else {
            enrollmentDetail = pRAFEnrollment.getRAFEnrollmentDetail();
            if (enrollmentDetail!=null) {
                enrollmentDetail.setDeleteFile(pFile);
                Application.save(enrollmentDetail);
                pRAFEnrollment.updateEnrollmentStatus(RAFEnrollmentStatus.Deleted);
            } else {
                logger.error("Could not find enrollment detail while trying to write delete tape for RAF enrollment: "+pRAFEnrollment.getId().toString());
            }
        }
        if (enrollmentDetail!=null) {
            tape.printf(
                    "TP%9.9s%-35.35s%4.4s%-35.35s%-20.20s%2.2s%-9.9s%-10.10sNN%4.4s%4.4sY%6.6s%1.1sY%6.6s%1.1sY%6.6s%1.1sY%6.6s%1.1s%232.232s\r\n",
                    format(enrollmentDetail.getFedTaxid(), "[0-9]*"),
                    format(enrollmentDetail.getLegalName(), "[A-Z0-9& -]*"),
                    "",
                    format(enrollmentDetail.getLegalStreetAddress(), "[A-Z0-9&%/ -]*"),
                    format(enrollmentDetail.getLegalCity(), "[A-Z0-9 -]*"),
                    format(enrollmentDetail.getLegalState(), "[A-Z]*"),
                    format(enrollmentDetail.getLegalZipCode(), "[0-9]*"),
                    "", // Client Account Number
                    year, // W2 Year
                    year, // 1099 Year
                    f940TaxPeriod, // 940 Tax Period
                    actionCode, // 940 Action Code
                    f941TaxPeriod,  // 941 Tax Period
                    actionCode, // 941 Action Code
                    f94xFTDPeriod, // 940 FTD Tax Period
                    actionCode, // 940 Action Code
                    f94xFTDPeriod, // 941 FTD Tax Period
                    actionCode, // 941 Action Code
                    ""  // Fill
            );
        }
    }

    protected String getActionCode(RAFEnrollmentFile pRAFEnrollment) {
        if (pRAFEnrollment.getRAFActionCode() == RAFActionCode.Add) {
            return ADD;
        } else {
            return DELETE;
        }        
    }

    protected String getYear(Company company, String actionCode) {
        return getCalendarForActionCode(company, actionCode).getYear() + "";
    }

    protected String getQuarter(Company company, String actionCode) {
        SpcfCalendar cal = getCalendarForActionCode(company, actionCode);
        return String.format("%04d%02d", cal.getYear(), cal.getMonth() - cal.getMonth() % 3);
    }

    protected String getQuarterForF941TaxPeriod(Company company, String actionCode) {
        SpcfCalendar cal = getCalendarForActionCode(company, actionCode);
        SpcfCalendar lastDay = CalendarUtils.getLastDayOfQuarter(cal);
        return String.format("%04d%02d", lastDay.getYear(), lastDay.getMonth());
    }

    protected String getQuarterForF94xFTDPeriod(Company company, String actionCode) {
        SpcfCalendar cal = getCalendarForActionCode(company, actionCode);
        return String.format("%04d%02d", cal.getYear(), cal.getMonth());
    }

    private SpcfCalendar getCalendarForActionCode(Company company, String actionCode) {
        SpcfCalendar cal = PSPDate.getPSPTime();
        if (ADD.equals(actionCode)) {
            CompanyService taxService = company.getCompanyService(ServiceCode.Tax);
            if (taxService!=null) {
                cal = taxService.getCreatedDate();
            }
        }

        return cal;
    }

    protected String format(String val, String allowedRegEx) {
        val = val.toUpperCase().trim();
        val = val.replaceAll(" +", " ");
        char[] charNotAllowed = val.replaceAll(allowedRegEx, "").toCharArray();
        for (char ch : charNotAllowed) {
            val = val.replace(ch, '~');
        }
        return val.replaceAll("~", "");
    }

    public static String getStreetAddress(Address addr) {
        String[] lines = {
                addr.getAddressLine1(),
                addr.getAddressLine2(),
                addr.getAddressLine3()
        };
        String sep = "";
        StringBuffer buf = new StringBuffer();
        for (String line : lines) {
            if (line != null && line.trim().length() > 0) {
                buf.append(sep + line.trim());
                sep = ", ";
            }
        }
        return buf.toString();
    }
    //
    public static void main(String[] args) throws Exception {
        if (args.length > 2) {
            System.err.println(USAGE);
            System.exit(1);
        }
        //
        String fileName = args[0];
        String batchId = (args.length > 1) ? args[1] : null;
        // new RAFTapeWriter(fileName, "3cabaffa-6253-41fc-8cf5-be276202646f").execute();
        //new RAFTapeWriter(fileName, null).execute();
    }

    protected String createFileName(String path, RAFEnrollmentFile pEnrollmentFile) {
        SpcfCalendar now = PSPDate.getPSPTime();
        String name = String.format("RAF"+getActionCode(pEnrollmentFile)+"%04d%02d%02d%02d%02d%02d", now.getYear(),
                now.getMonth(), now.getDay(), now.getHour(), now.getMinute(), now.getSecond());
        return path + File.separator + name;
    }
}

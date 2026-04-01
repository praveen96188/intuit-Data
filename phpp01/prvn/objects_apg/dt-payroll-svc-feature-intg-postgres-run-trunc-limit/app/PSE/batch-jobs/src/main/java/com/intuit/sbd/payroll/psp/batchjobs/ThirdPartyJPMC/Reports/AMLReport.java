package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessage;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.sbd.payroll.psp.common.pgp.impl.PgpCommonEncryptedWriter;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.paycycle.util.DateUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charithah418 on 6/1/15.
 */
public class AMLReport extends JPMCReportBase {

    private static final String DELIMITER = "|";
    private static final String SPACE =" ";
    private static final String FILE_EOL = "\r\n";
    public static final String AML_DATA_FILE_PREFIX = "AML_Data_File_";
    public String dataFile;
    public static final String ENCRYPTED_FILE_EXT = ".pgp";
    public static final String FILE_EXT = ".txt";

    private List<EventTypeCode> mEventTypeCodes;

    public AMLReport(){
        mEventTypeCodes = new ArrayList<EventTypeCode>();
        mEventTypeCodes.add(EventTypeCode.OFXServiceActivated);
        mEventTypeCodes.add(EventTypeCode.PrimaryPrincipalNameChanged);
        mEventTypeCodes.add(EventTypeCode.PrimaryPrincipalSSNChanged);
        mEventTypeCodes.add(EventTypeCode.PrimaryPrincipalDOBChanged);
        logger =  Application.getLogger(this.getClass());
    }

    public void createJPMCReport(SpcfCalendar fromDate, SpcfCalendar toDate)throws Exception{
        logger.info("Getting Data for AML report");
        String formattedDate = StringFormatter.formatDate(toDate, "yyyyMMdd");
        this.dataFile = OUTPUT_DIRECTORY + File.separator + AML_DATA_FILE_PREFIX + formattedDate + FILE_EXT;
        List<JPMCEventMessage> jpmcEventMessages = getJPMCReportData(mEventTypeCodes, Boolean.TRUE, fromDate, toDate,true);
        try {
            createAMLFile(jpmcEventMessages);
        } catch (Exception exception) {
            FileUtils.deleteFile(OUTPUT_DIRECTORY + File.separator + AML_DATA_FILE_PREFIX + formattedDate + ENCRYPTED_FILE_EXT);
            throw new RuntimeException("Exception while encrypting the AML report " + exception);
        }finally {
            FileUtils.deleteFile(dataFile);
        }
    }

    public void createAMLFile(List<JPMCEventMessage> pJPMCEventMessages) throws Exception{
        logger.info("Creating AML Report");
           /* SourceCompanyId, dob, PrimaryPrincipal Name, SSN */
        PgpWriter fileWriter = new PgpCommonEncryptedWriter(BatchUtils.getAMLPgpKeys());
        fileWriter.open(dataFile);

        StringBuilder recordData = new StringBuilder();
        for (JPMCEventMessage jpmcEventMessage: pJPMCEventMessages)
        {
              createAMLRecord(jpmcEventMessage, recordData);
        }
        writeData(fileWriter, recordData.toString());
        logger.info("Finished creating AML report. Number of Records=" + pJPMCEventMessages.size());
        fileWriter.flush();
        fileWriter.close();
    }

    protected void writeData(PgpWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }

    public void createAMLRecord( JPMCEventMessage jpmcEventMessage,StringBuilder recordData){
        recordData.append(applyFieldConstraint(jpmcEventMessage.getSourceCompanyId(),"COID"));
        recordData.append(DELIMITER);
        recordData.append(jpmcEventMessage.getDateOfBirth() != null ? DateUtil.dateFormat(jpmcEventMessage.getDateOfBirth().getTime(),"MMddyyyy"):"");
        recordData.append(DELIMITER);
        recordData.append(applyFieldConstraint(jpmcEventMessage.getFirstName(),"firstname"));
        recordData.append(SPACE);
        if(jpmcEventMessage.getMiddleName() != null){
            recordData.append(jpmcEventMessage.getMiddleName());
            recordData.append(SPACE);
        }
        recordData.append(applyFieldConstraint(jpmcEventMessage.getLastName(),"lastname"));
        recordData.append(DELIMITER);
        recordData.append(applyFieldConstraint(jpmcEventMessage.getSsn(),"ssn"));
        recordData.append(DELIMITER);
        recordData.append(applyFieldConstraint(jpmcEventMessage.getRealmId(),"realmId"));
        recordData.append(FILE_EOL);
    }

    /**
     * This method will be used to apply any constraints on the field.Currently we dont have any constraints
     * @param field
     * @param fieldType
     * @return
     */
    public String applyFieldConstraint(String field,String fieldType){
        if(field == null){
            return "";
        }

       if(fieldType.equals("ssn") && field.length() == 9){
            field = field.substring(0,3) + "-" + field.substring(3,5) + "-" + field.substring(5,9);
       }

       return field;
    }
}

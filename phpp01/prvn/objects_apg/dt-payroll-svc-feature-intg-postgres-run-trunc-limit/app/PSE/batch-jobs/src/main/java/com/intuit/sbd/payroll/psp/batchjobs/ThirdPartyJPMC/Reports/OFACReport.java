package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessage;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.paycycle.util.DateUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by charithah418 on 6/1/15.
 */
public class OFACReport extends JPMCReportBase {
    private static final String DELIMITER = "|";
    private static final String FILE_EOL = "\r\n";
    public static final String OFAC_PREFIX = "OFAC_File_";
    public static final String DATA_FILE_EXT = ".csv";
    public static final String TRG_FILE_EXT = ".trg";
    public static final String ENCRYPTED_DATA_FILE_EXT= ".pgp";
    public static final String AUDIT_FILE= "_audit";

    public static final int subListIndex = 3;

    private List<EventTypeCode> mEventTypeCodes;
    public String dataFile;
    public String triggerFile;

    /**
     * File date for particular date
     */
    public OFACReport() {
        logger = Application.getLogger(this.getClass());
        mEventTypeCodes = new ArrayList<EventTypeCode>();
        mEventTypeCodes.add(EventTypeCode.OFXServiceActivated);
        mEventTypeCodes.add(EventTypeCode.PrimaryPrincipalNameChanged);
        mEventTypeCodes.add(EventTypeCode.PrimaryPrincipalDOBChanged);
        //Sublist for which event detail should be considered
        mEventTypeCodes.add(EventTypeCode.ServiceStatusChange);
    }
    /**
     * @param fromDate
     * @param toDate
     * @throws Exception
     */
    public void createJPMCReport(SpcfCalendar fromDate, SpcfCalendar toDate) throws Exception {
        logger.info("Getting Data for OFAC report");
        String formattedDate = StringFormatter.formatDate(toDate, "yyyyMMdd");
        this.dataFile = OUTPUT_DIRECTORY + File.separator + OFAC_PREFIX + formattedDate + DATA_FILE_EXT;
        this.triggerFile = OUTPUT_DIRECTORY + File.separator + OFAC_PREFIX  + formattedDate + TRG_FILE_EXT;

        //Send sublist start for which event detail should be considered.
        List<JPMCEventMessage> jpmcEventMessages = getOFACReportData(mEventTypeCodes, subListIndex, fromDate, toDate);
        List<String> encKeys = new ArrayList<String>();
        List<String> encryptFiles = new ArrayList<String>();
        List<String> errorString = new ArrayList<String>();
        createOFACFile(jpmcEventMessages);
        String encryptCSIFile = BatchUtils.getConfigString("psp_jpmc_csi_encrypt_file");
        if (encryptCSIFile != null && encryptCSIFile.trim().equalsIgnoreCase("true")) {
            encKeys.add("psp_jpmc_csi_public_key");
            encryptFiles.add(File.separator + OFAC_PREFIX + formattedDate + ENCRYPTED_DATA_FILE_EXT);
            errorString.add("Exception while encrypting the OFAC report");
            // file for audit
            encKeys.add("psp_jpmc_audit_public_key");
            encryptFiles.add(File.separator + OFAC_PREFIX + formattedDate + AUDIT_FILE + ENCRYPTED_DATA_FILE_EXT);
            errorString.add("Exception while encrypting the OFAC audit file");
            for( int encKeysCount=0; encKeysCount< encKeys.size(); encKeysCount++) {
                try {
                    encryptOFACFile(OFAC_PREFIX + formattedDate, encKeys.get(encKeysCount), encryptFiles.get(encKeysCount) );
                }
                catch (Exception exception) {
                    FileUtils.deleteFile(dataFile);
                    FileUtils.deleteFile(OUTPUT_DIRECTORY +encryptFiles.get(encKeysCount));
                    throw new RuntimeException(errorString.get(encKeysCount).toString() + exception);
                }
            }
        }
        if (encryptCSIFile !=null && encryptCSIFile.trim().equalsIgnoreCase("true") ) {
            FileUtils.deleteFile(dataFile);
        }

        createTriggerFile();
    }
    private void encryptOFACFile(String fileNameWithoutExtension, String encKey, String pgpFileName ) throws Exception {
        String txtFileName = File.separator + fileNameWithoutExtension + DATA_FILE_EXT;
        // String pgpFileName = File.separator + fileNameWithoutExtension + AUDIT_FILE + ENCRYPTED_DATA_FILE_EXT ;
        List<String> encKeyList = new ArrayList<String>();
        encKeyList.add(BatchUtils.getConfigString(encKey));
        logger.info("Starting pgpEncrypt without SignFile...");
        StopWatch sw = StopWatch.create(false);
        sw.start();
        PgpFileUtils.pgpEncryptWithoutSign(OUTPUT_DIRECTORY,
                txtFileName,
                pgpFileName,
                encKeyList,
                false,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                true);          // Include Integrity Packet(s) in the encrypted stream
        sw.stop();
        logger.info("Encrypted File in location :" + OUTPUT_DIRECTORY + pgpFileName);
        logger.info("Completed pgpEncrypt without sign" + sw.getElapsedTimeString());
    }
    /**
     * @param pJPMCEventMessageList
     * @throws Exception
     */
    public void createOFACFile(List<JPMCEventMessage> pJPMCEventMessageList) throws Exception {
        logger.info("Creating Data file for OFAC Report");
       /* FirstName, MiddleName, LastName, Other, Unparsed, All Entities
        * Street Address, Street Address 2, City, State, Postal, Country_iso3, Country
        * DOB, Unique_ID, Client Defined1, Client Defined2, Client Defined3
        */
        StringBuilder recordData = new StringBuilder();
        //FileWriter fileWriter = new FileWriter(dataFile);
        OutputStreamWriter fileWriter = null;

        fileWriter = new FileWriter(dataFile);

        for (JPMCEventMessage jpmcEventMessage : pJPMCEventMessageList) {
            createOFACRecord(jpmcEventMessage, recordData);
        }
        writeData(fileWriter, recordData.toString());
        logger.info("Finished creating Data file for OFAC report. Number of Records=" + pJPMCEventMessageList.size());
        fileWriter.flush();
        fileWriter.close();
    }
    /**
     * @param jpmcEventMessage
     * @param recordData
     * @return
     */
    public void createOFACRecord(JPMCEventMessage jpmcEventMessage, StringBuilder recordData) {
        //#1 First name Principal officer
        recordData.append(applyFieldConstraint(jpmcEventMessage.getFirstName(), 128));
        recordData.append(DELIMITER);
        //#2  Middle name Principal officer
        if (jpmcEventMessage.getMiddleName() != null && jpmcEventMessage.getFirstName() != null && (jpmcEventMessage.getFirstName().length() + jpmcEventMessage.getMiddleName().length()) < 128) {
            recordData.append(applyFieldConstraint(jpmcEventMessage.getMiddleName(), 128-jpmcEventMessage.getFirstName().length()));
        }
        recordData.append(DELIMITER);
        //#3  last name Principal officer
        recordData.append(applyFieldConstraint(jpmcEventMessage.getLastName(), 128));
        recordData.append(DELIMITER);
        //#4 Company legal name
        recordData.append(applyFieldConstraint(jpmcEventMessage.getLegalName(), 200));
        recordData.append(DELIMITER);
        recordData.append(DELIMITER);
        recordData.append(DELIMITER);
        //#7 AddressLine1
        recordData.append(applyFieldConstraint(jpmcEventMessage.getAddressLine1(), 100));
        recordData.append(DELIMITER);
        //#8 Address Line 2
        recordData.append(applyFieldConstraint(jpmcEventMessage.getAddressLine2(), 100));
        recordData.append(DELIMITER);
        //#9 City
        recordData.append(applyFieldConstraint(jpmcEventMessage.getCity(), 50));
        recordData.append(DELIMITER);
        //#10 State
        recordData.append(applyFieldConstraint(jpmcEventMessage.getState(), 2));
        recordData.append(DELIMITER);
        //#11 ZipCode
        recordData.append(applyFieldConstraint(jpmcEventMessage.getZipCode(), 20));
        recordData.append(DELIMITER);
        //#12 Country code(country will be  USA)
        recordData.append("USA");
        recordData.append(DELIMITER);
        //#13 Country Sinvce we are putting code Country is not reqd
        //recordData.append(jpmcEventMessage.getCountry() == null ? "USA" : applyFieldConstraint(jpmcEventMessage.getCountry(), 50));
        recordData.append(DELIMITER);
        //#14 DOB
        recordData.append(jpmcEventMessage.getDateOfBirth() != null ? DateUtil.dateFormat(jpmcEventMessage.getDateOfBirth().getTime(), "MM/dd/yyyy") : "");
        recordData.append(DELIMITER);
        //#15 UniqueId
        recordData.append(applyFieldConstraint(jpmcEventMessage.getUniqueID(), 128));
        recordData.append(DELIMITER);
        //#16 PSID
        recordData.append(applyFieldConstraint(jpmcEventMessage.getSourceCompanyId(), 128));
        recordData.append(DELIMITER);
        recordData.append(DELIMITER);
        recordData.append(DELIMITER);
        //#19 RecordStatus
        recordData.append(applyFieldConstraint(jpmcEventMessage.getRecordStatus(), 1));
        recordData.append(FILE_EOL);
    }
    public void createTriggerFile() throws IOException {
        logger.info("Creating Trigger file for OFAC report");
      //  FileWriter fileWriter = new FileWriter(triggerFile);
        Key key  = IDPSFileStreamManager.newKeyHandleLatest();
        OutputStreamWriter fileWriter = new IDPSFileWriter(triggerFile,key);
        StringBuilder recordData = new StringBuilder();
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        recordData.append("<BatchManifest>\n" +
                "<BillingCode>");
        recordData.append(getXMLTagsWithValue("PartnerCode", BatchUtils.getConfigString("psp_jpmc_ofac_partnerCode")));
        recordData.append(getXMLTagsWithValue("BillingGUID", BatchUtils.getConfigString("psp_jpmc_ofac_billingGUID")));
        recordData.append(getXMLTagsWithValue("ClientCode", BatchUtils.getConfigString("psp_jpmc_ofac_clientCode")));
        recordData.append(getXMLTagsWithValue("Version", BatchUtils.getConfigString("psp_jpmc_ofac_version")));
        recordData.append("</BillingCode>");
        recordData.append("<Settings>");
        recordData.append(getXMLTagsWithValue("BatchName",OFAC_PREFIX  + StringFormatter.formatDate(pspDate, "yyyyMMdd")));
        recordData.append(getXMLTagsWithValue("FileExtension", BatchUtils.getConfigString("psp_jpmc_ofac_fileExtension")));
        recordData.append(getXMLTagsWithValue("PerformDecryption", "true"));
        recordData.append(getXMLTagsWithValue("GCLFilter", BatchUtils.getConfigString("psp_jpmc_ofac_gclFilter")));
        recordData.append(getXMLTagsWithValue("Threshold", BatchUtils.getConfigString("psp_jpmc_ofac_threshold")));
        recordData.append(getXMLTagsWithValue("TranslatorProcessor", BatchUtils.getConfigString("psp_jpmc_ofac_translatorProcessor")));
        recordData.append(getXMLTagsWithValue("DeliveryProcessor", BatchUtils.getConfigString("psp_jpmc_ofac_deliveryProcessor")));
        recordData.append(getXMLTagsWithValue("AddressDisqualification", BatchUtils.getConfigString("psp_jpmc_ofac_addressDisqualification")));
        recordData.append(getXMLTagsWithValue("DOBDisqualification", BatchUtils.getConfigString("psp_jpmc_ofac_dobDisqualification")));
        recordData.append(getXMLTagsWithValue("DOBRange", BatchUtils.getConfigString("psp_jpmc_ofac_dobRange")));
        recordData.append(getXMLTagsWithValue("GenerateCustomerID", BatchUtils.getConfigString("psp_jpmc_ofac_generateCustomerID")));
        recordData.append("</Settings><DataLists><Lists><DataList>");
        recordData.append(getXMLTagsWithValue("ListName", BatchUtils.getConfigString("psp_jpmc_ofac_listName")));
        recordData.append("</DataList>");
        recordData.append("</Lists></DataLists>");
        recordData.append("</BatchManifest>");
        writeData(fileWriter, recordData.toString());
        logger.info("Finished creating Trigger file for OFAC report");
        fileWriter.flush();
        fileWriter.close();
    }
    private String getXMLTagsWithValue(String tagName, String value) {
        String returnValue = "<" + tagName + ">" + value + "</" + tagName + ">" + FILE_EOL;
        return returnValue;
    }
    /**
     * @param field
     * @param columnLength
     * @return
     */
    public String applyFieldConstraint(String field, int columnLength) {
        if (field == null) {
            return "";
        }
        field = field.replaceAll("[^\\p{ASCII}]", "");
        field = field.replaceAll("\"|'", "");
        if (field.length() > columnLength) {
            field = field.substring(0, columnLength);
        }
        if (field.contains(",")) {
            field = "\"" + field + "\"";
        }
        return field;
    }
}
package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPOutputStream;


/**
 * Created by IntelliJ IDEA.
 * User: jpatel
 * Date: May 22, 2009
 * Time: 6:00:54 PM
 * To change this template use File | Settings | File Templates.
 */
abstract public class BaseATFExtractFileProcess extends BatchJobProcessor {
    //Logger for logging and monitoring
    protected SpcfLogger logger;

    protected String mBatchGuid;
    protected String mExtractOutputDir;
    protected String mCuttOffTime;// used only for UpdatedData Extract  eg: 5:00 PM every day
    protected int mRecordCount;

    protected ATFDataExtractBatch mExtractBatch;
    protected ATFDataExtractFileType mExtractFileType;
    //protected String mFtpDestinationDir;

    //The formatted file create time used to write in the header/ trailer record and in the extract file name
    protected String mCreateTime;
    //The Extension to use if the extract files are to be zip files
    private static final String ZIP_FILE_EXTENSION = ".gz";
    private static final String HEADER_REC_IDENTIFIER = "HDR";
    private static final String TRAILER_RECORD_IDENTIFIER="TLR";
    private static final String PAYROLL_SRC_SYSTEM_NAME = "Assisted";
    private static final String INT_REVISION_NUM ="1";
    protected static final String DELIMITER = "\",\"";
    protected static final String DOUBLE_QUOTE = "\"";
    protected String mATFExtractTypeID;
    //These 2 variables tell the DB SQl loader what to do when receiving the files
    protected static final String COMPARE_PROCESS_CODE = "C";
    protected static final String REFRESH_CODE = "R";
    // The actual process code (one of 2 above)
    protected String mProcessCode;


    /**
     * Constructor
     */
    public BaseATFExtractFileProcess(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters){
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);

        String[] parameters = pJobInstanceParameters.split("%");

        mBatchGuid = parameters[0];

        logger = Application.getLogger(this.getClass());
        mExtractOutputDir = BatchUtils.getConfigString("psp_atf_send_dir");
        //mFtpDestinationDir = BatchUtils.getConfigString("psp_atf_ftp_destdir");
        //used only for UpdatedData Extract
        mCuttOffTime = SystemParameter.findSystemParameter(SystemParameter.Code.ATF_EXTRACT_CUTOFF).getSystemParameterValue();
        int monthOfQuarterCurQtrCutOff = SystemParameter.findIntValue(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF);
        int dayOfMonthCurQtrCutOff= SystemParameter.findIntValue(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF);
        int dayOfWeekCurQtrCutOff= SystemParameter.findIntValue(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF);
        CutOffDateCalc.createInstance(monthOfQuarterCurQtrCutOff, dayOfMonthCurQtrCutOff, dayOfWeekCurQtrCutOff );

        //retrieve the Batch
        mExtractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(mBatchGuid));
    }

    /**
     * This base method is responsible to create the o/p folder if it does not already exist and
     * the Extract file with the header and trailer record. This method also calls the writeData method in the
     * implementing sub classes to fill data in the extracted files
     *
     */
    @Override
    public void execute() {
        executeStep(new ExtractFileStep()); 

    }

    protected static void evictObjectsFromCache(Object[] pObjects) {
        for (Object obj : pObjects) {
            if (obj != null) {
                Application.evict(obj);
            }
        }
    }

    public class ExtractFileStep extends BatchJobProcessorStep {
        public void execute() {
            ATFDataExtractFile extractFile = null;
            try {
                String batchFolderStr = mExtractOutputDir + File.separator + "PSP_" + mExtractBatch.getBatchId();
                String fileName =  batchFolderStr + File.separator + genExtractFileName(true);
                // create the ATFDataExtractFile record;
                extractFile = createATFDataExtractFileRecord(mExtractBatch, mExtractFileType, fileName);

                File batchFolder = new File(batchFolderStr);
                // create batch output folder if it does not already exist
                if (!batchFolder.exists()) batchFolder.mkdir();
                // Create the actual CO-INFO file for ATF
                File file =  new File(fileName);
                //create the extract file if it does not already exist
                if (!file.exists()) {
                    file.createNewFile();
                }
                // Get the Print Writer to write data to the file
                PrintWriter pw =  getPW(fileName, true);
                // write the header record
                writeHeaderRecord(pw);

                //get Unit of Work
                PayrollServices.beginUnitOfWork();
                // Write the Actual Data
                writeData(pw);

                // write the trailer record
                writeTrailerRecord(pw);

                // update status of file to Extracted
                updateATFExtractFileStatus(extractFile.getId(), ATFDataExtractFileStatus.Extracted);

                //commit unit of work
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t){
                // Rollback if transaction is in process and error has occurred
                PayrollServices.rollbackUnitOfWork();

                logger.error("Exception trying to write data for " + mATFExtractTypeID , t);
                logger.info("Updating status of ATFExtractFile to '" + ATFDataExtractFileStatus.Failed + "'");
                System.out.println(t.toString()); //add some quick debugging for the teamcity logs
                t.printStackTrace(System.out);
                if (extractFile != null){

                    PayrollServices.beginUnitOfWork();
                    updateATFExtractFileStatus(extractFile.getId(), ATFDataExtractFileStatus.Failed);
                    PayrollServices.commitUnitOfWork();

                } else {
                    logger.error("ATFDataExtractFile record is not created.");
                }
                throw new RuntimeException(t);

            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    abstract protected void writeData(PrintWriter pPW) throws Throwable;

    /**
     * This method is to create a new ATFDataExtractFile record for a data extract batch.
     * 
     * @param pExtractBatch
     * @param pATFDataExtractFileType
     * @param pFileName
     * @return
     */
    protected ATFDataExtractFile createATFDataExtractFileRecord(ATFDataExtractBatch pExtractBatch, ATFDataExtractFileType pATFDataExtractFileType, String pFileName) {
        PayrollServices.beginUnitOfWork();
        
        ATFDataExtractFile extractFile = new ATFDataExtractFile();
        extractFile.setATFDataExtractBatch(pExtractBatch);
        extractFile.setFileType(pATFDataExtractFileType);
        extractFile.setFileName(pFileName);
        extractFile.setStartDate(PSPDate.getPSPTime());
        extractFile.setFileStatus(ATFDataExtractFileStatus.Started);
        extractFile.setStatusEffectiveDate(PSPDate.getPSPTime());

        Application.save(extractFile);
        PayrollServices.commitUnitOfWork();

        return extractFile;
    }

    /**
     * This method is to update the status of the ATFDataExtractFile record in the DB
     * 
     * @param pExtractFileGUID
     * @param pStatus
     * @return
     */
    protected ATFDataExtractFile updateATFExtractFileStatus(SpcfUniqueId pExtractFileGUID, ATFDataExtractFileStatus pStatus) {
        ATFDataExtractFile extractFile = PayrollServices.entityFinder.findById(ATFDataExtractFile.class, pExtractFileGUID);
        extractFile.setFileStatus(pStatus);
        extractFile.setStatusEffectiveDate(PSPDate.getPSPTime());
        Application.save(extractFile);
        return extractFile;
    }

    /**
     * Writes the header record.
     * Format: "HDR","Payroll System Name","YYYYMMDD","intRevisionNumber"
     * 
     * @param pPW PrintWriter Extract file print writer
     * 
     */
    protected void writeHeaderRecord(PrintWriter pPW){

        pPW.print(DOUBLE_QUOTE);
        pPW.print(HEADER_REC_IDENTIFIER);
        pPW.print(DELIMITER);
        pPW.print(PAYROLL_SRC_SYSTEM_NAME);
        pPW.print(DELIMITER);
        pPW.print(mProcessCode);
        pPW.print(DELIMITER);
        pPW.print(mCreateTime);
        pPW.print(DELIMITER);
        pPW.print(INT_REVISION_NUM);
        pPW.println(DOUBLE_QUOTE);

        //Include the header record in the record count
        mRecordCount++;

    }

    /**
     * This method write the last trailer record into the file and closes/flushes the print writer.
     *
     * @param pPW
     */
    protected void writeTrailerRecord(PrintWriter pPW){
        
        pPW.print(DOUBLE_QUOTE);
        pPW.print(TRAILER_RECORD_IDENTIFIER);
        pPW.print(DELIMITER);
        //include trailer record in the record count
        pPW.print(++mRecordCount);
        pPW.print(DELIMITER);
        pPW.print(mCreateTime);
        pPW.print(DELIMITER);
        pPW.print(INT_REVISION_NUM);
        pPW.println(DOUBLE_QUOTE);

        //Cleanup
        pPW.flush();
        pPW.close();
    }

    // This method is to get the PrintWriter.
    /**
     * Creates the extract file print writer
     * @param pFilePath String Extract filename
     * @return PrintWriter Extract file writer
     * @throws Throwable If there was a problem creating the print writer
     */
    protected PrintWriter getPW(String pFilePath, boolean pZipTheData) throws IOException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(pFilePath);
        PrintWriter pw = null;
        if (pZipTheData) {
            pw = new PrintWriter(new GZIPOutputStream(fos));
        } else {
            pw = new PrintWriter(fos);
        }
        return (pw);
    }

    /**
     * Generate the extract filename as EXTRACTTYPE_YYYYMMDDHHmmssSS and with the appropriate zip suffix,
     * if applicable
     * @return String The extract filename
     * @throws Throwable If there was a problem generating the extract filename
     */
    protected String genExtractFileName(boolean pZipTheData) throws Throwable {

        SpcfCalendar cal = PSPDate.getPSPTime();

        // the create time used in the header and trailer records.
        mCreateTime = StringFormatter.formatDate(PSPDate.getPSPTime(),"yyyyMMdd");

        //Start the filename out with the extract type and an underscore
        StringBuffer sb = new StringBuffer(mATFExtractTypeID + "_");

        //We need to save the timestamp for sending via JMS, so create its own StringBuffer
        StringBuffer sbTimeStamp = new StringBuffer();

        //Format: YYYYMMDDHHmmssSS
        sbTimeStamp.append(fillString(cal.getYear(), 4));
        sbTimeStamp.append(fillString(cal.getMonth(), 2));
        sbTimeStamp.append(fillString(cal.getDay(), 2));
        sbTimeStamp.append(fillString(cal.getHour(), 2));
        sbTimeStamp.append(fillString(cal.getMinute(), 2));
        sbTimeStamp.append(fillString(cal.getSecond(), 2));
        sbTimeStamp.append(fillString(cal.getMillisecond(), 3));

        //Get and save the timestamp
        //mExtractTimestamp = sbTimeStamp.toString();

        //Append timestamp to the filename
        sb.append(sbTimeStamp);

        //Append zip suffix to the filename if we're zipping data
        if (pZipTheData) {
            sb.append(ZIP_FILE_EXTENSION);
        }

        return sb.toString();

    }

    /**
     * Appends leading zeroes to an int
     * @param pInp int Input number
     * @param pLength int Number of zeroes to append
     * @return String Output string, with the appropriate number of final zeroes
     */
    public static String fillString(int pInp, int pLength) {
        StringBuffer outputStr = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        sb.append(pInp);
        if (sb.length() < pLength) {
            for (int i = 0; i < pLength - sb.length(); i++) {
                outputStr.append("0");
            }
        }
        outputStr.append(sb);
        return (outputStr.toString());
    }

    /**
     * Prepends leading zeroes to a string
     * @param str String Input string
     * @param length int Desired length
     * @return String Output string of the desired length, with zeroes
     */
    public static String prependLeadingZeros(String str, int length) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append("0");
        }
        sb.append(str);
        return (sb.toString());
    }

    /**
     * Returns the date at which the Extract was last run successfully
     * @return  SpcfCalendar
     */
    protected SpcfCalendar getLastSuccessfulExtractBatchStartdate(boolean pQuarterSpecificExtractType){

        //This is the default for the first time we run this
        SpcfCalendar maxDate =  PSPDate.getPSPTime().copy();
        maxDate.addYears(-50);

        String[] paramNames = new String[2];
        paramNames[0] = "batchStatusCd";
        paramNames[1] = "runType";

        Object[] paramValues = new Object[2];
        paramValues[0] = ATFDataExtractBatchStatus.Completed;  
        paramValues[1] = ATFDataExtractRunType.UpdatedData;

        Criterion<ATFDataExtractBatch> where = ATFDataExtractBatch.BatchStatus().equalTo(ATFDataExtractBatchStatus.Completed).
                                        And(ATFDataExtractBatch.StartDate().isNotNull());

        if (pQuarterSpecificExtractType) {
            where = where.And(ATFDataExtractBatch.RunType().equalTo(ATFDataExtractRunType.UpdatedData));
        }

        SpcfCalendar latestExtractBatchStartDate = Application.executeObjectAggQuery(ATFDataExtractBatch.class,
                new Query<ATFDataExtractBatch>().Select(ATFDataExtractBatch.StartDate().Max())
                        .Where(where));

        if (latestExtractBatchStartDate != null) {
            maxDate=latestExtractBatchStartDate;
        }

        return maxDate;
    }


    /**
     * This method escapes any double quotes and writes to the print writer
     * @param pPW  PrintWriter Extract file writer
     * @param pField the field to write
     * @throws Throwable
     */
    protected void writeFormatted(PrintWriter pPW, String pField) throws Throwable {
        writeFormatted(pPW, pField, false);
    }

    /**
     * This method escapes any double quotes and writes to the print writer
     * @param pPW  PrintWriter Extract file writer
     * @param pField the field to write
     * @param isLastColumn the field is last column
     * @throws Throwable
     */
    protected void writeFormatted(PrintWriter pPW, String pField, boolean isLastColumn) throws Throwable {
        if (pField != null) {
            String strToWrite = pField.trim();
            //Replace a single quote with double quotes
            strToWrite = strToWrite.replaceAll("\"", "\"\"");

            // Replace new line/carriage return character
            strToWrite = strToWrite.replaceAll("[\n\r]", " ");

            pPW.print(strToWrite);
        }
        if(isLastColumn) {
            pPW.println(DOUBLE_QUOTE);
        } else {
            pPW.print(DELIMITER);
        }
    }

    /**
     * This method returns the last day of the filing quarter if the current date is before the cuttoff date
     * other wise it retuns the last date of the current qtr
     * @return
     */
//    protected SpcfCalendar getLastDateToGetData(){
//        SpcfCalendar pspDate = PSPDate.getPSPTime();
//        SpcfCalendar retval = pspDate;
//        boolean isOnOrAfterCuttOff = CutOffDateCalc.getInstance().isOnOrAfterCutOffDate(pspDate);
//        if (isOnOrAfterCuttOff)  {
//            retval =  CalendarUtils.getLastDayOfQuarter(pspDate);
//            retval.addDays(1);// to make it morning (00:00:00) of the next day
//        } else {
//            //if the current date is NOT AFTER the cuttOffDate
//            int qtr = CalendarUtils.getQuarterAsInt(pspDate);
//            int year = pspDate.getYear();
//            qtr--;
//            if (qtr == 0){
//                //it is the prior year
//                qtr = 4;
//                year--;
//            }
//            retval =  CalendarUtils.getLastDayOfQuarter(year, qtr);
//            retval.addDays(1);// to make it morning (00:00:00) of the next day
//        }
//        return retval;
//    }

    protected SpcfCalendar getPriorQtrEndDate(SpcfCalendar pDate){

        int qtr = CalendarUtils.getQuarterAsInt(pDate);
        int year = pDate.getYear();
        qtr--;
        if (qtr == 0){
            //it is the prior year
            qtr = 4;
            year--;
        }
        SpcfCalendar retval = CalendarUtils.getLastDayOfQuarter(year, qtr);
        return retval;
    }

    public static void appendDGCheckCondition(StringBuilder queryBuilder, String alias, Boolean nativeSql) {
        if (Company.isDGDeleteFeatureEnabled()) {
            String queryColumn = nativeSql ? ".IS_DG_DISASSOCIATED=0 \n" : ".IsDgDisassociated=0 \n";
            queryBuilder.append(" and ").append(alias).append(queryColumn);
        }
    }

}




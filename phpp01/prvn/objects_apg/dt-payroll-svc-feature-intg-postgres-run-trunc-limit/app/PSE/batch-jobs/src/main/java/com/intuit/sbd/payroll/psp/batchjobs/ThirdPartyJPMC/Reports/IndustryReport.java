package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessage;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by charithah418 on 6/1/15.
 */
public class IndustryReport extends JPMCReportBase {
    private static final String DELIMITER = "\t";
    private static final String FILE_EOL = "\r\n";
    public static final String FILE_EXT = ".txt";
    public static final String INDUSTRY_TYPE_PREFIX = "1-87763_";
    public static final String ACTIVE_DD_SERVICE = "_Add";
    public static final String INACTIVE_DD_SERVICE = "_Remove";

    public String activeDDServiceFile;
    public String inActiveDDServiceFile;
    private List<EventTypeCode> mEventTypeCodes;

    /**
     * File date for particular date
     */
    public IndustryReport() {
        logger = Application.getLogger(this.getClass());
    }

    /**
     * @param fromDate
     * @param toDate
     * @throws Exception
     */
    @Override
    public void createJPMCReport(SpcfCalendar fromDate, SpcfCalendar toDate) throws Exception {
        logger.info("Getting Data for Industry report");

        mEventTypeCodes = new ArrayList<EventTypeCode>();
        mEventTypeCodes.add(EventTypeCode.OFXServiceActivated);
        mEventTypeCodes.add(EventTypeCode.CompanyIndustryTypeChanged);
        this.activeDDServiceFile = OUTPUT_DIRECTORY + File.separator + INDUSTRY_TYPE_PREFIX + StringFormatter.formatDate(toDate, "yyyyMMdd") + ACTIVE_DD_SERVICE + FILE_EXT;
        List<JPMCEventMessage> jpmcEventMessages = getJPMCReportData(mEventTypeCodes, Boolean.TRUE, fromDate, toDate,true);
        createIndustryFile(jpmcEventMessages, this.activeDDServiceFile);


        mEventTypeCodes = new ArrayList<EventTypeCode>();
        mEventTypeCodes.add(EventTypeCode.ServiceStatusChange);
        this.inActiveDDServiceFile = OUTPUT_DIRECTORY + File.separator + INDUSTRY_TYPE_PREFIX + StringFormatter.formatDate(toDate, "yyyyMMdd") + INACTIVE_DD_SERVICE + FILE_EXT;
        jpmcEventMessages = getJPMCReportData(mEventTypeCodes, Boolean.FALSE, fromDate, toDate,true);
        createIndustryFile(jpmcEventMessages, this.inActiveDDServiceFile);
    }

    public void createIndustryFile(List<JPMCEventMessage> pJPMCEventMessages, String pDataFile) throws IOException {
        logger.info("Creating Data file for Industry Report");
        //URL (leave blank), Legal Business Name, Trade Name(use DBA), DBA, MCC(use SIC code), ID 1(FEIN),
        // ID 2(PSID), Address 1, Address 2, City, State, Country(USA), Zip Code, Phone Number, Email Address(use PP email)
        Key key  = IDPSFileStreamManager.newKeyHandleLatest();
        OutputStreamWriter fileWriter = new IDPSFileWriter(pDataFile,key);
        StringBuilder recordData = new StringBuilder();
        createHeader(recordData);

        for (JPMCEventMessage jpmcEventMessage : pJPMCEventMessages) {
            createIndustryRecord(jpmcEventMessage, recordData);
        }
        writeData(fileWriter, recordData.toString());
        logger.info("Finished creating Data file for Industry report. Number of Records=" + pJPMCEventMessages.size());
        fileWriter.flush();
        fileWriter.close();

    }

    public void createIndustryRecord(JPMCEventMessage jpmcEventMessage, StringBuilder recordData) {

        //First entry url :default blank
        //URL (leave blank), Legal Business Name, Trade Name(use DBA), DBA, MCC(use SIC code), ID 1(FEIN),
        // ID 2(PSID), Address 1, Address 2, City, State, Country(USA), Zip Code, Phone Number, Email Address(use PP email)

        createEntryWithDelimter(recordData, "");
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getLegalName()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getDba()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getDba()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getIndustrySicCode()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getFedTaxId()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getSourceCompanyId()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getAddressLine1()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getAddressLine2()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getCity()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getState()));
        createEntryWithDelimter(recordData, "USA");
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getZipCode()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getPhoneNumber()));
        createEntryWithDelimter(recordData, applyFieldConstraint(jpmcEventMessage.getEmail()));
        createEntryWithDelimter(recordData, "");
        recordData.append("");
        recordData.append(FILE_EOL);
    }

    private void createEntryWithDelimter(StringBuilder recordData, String entry) {
        recordData.append(entry)
                  .append(DELIMITER);

    }

    public void createHeader(StringBuilder recordData) {

        //Header
        //URL (leave blank), Legal Business Name, Trade Name(use DBA), DBA, MCC(use SIC code), ID 1(FEIN),
        // ID 2(PSID), Address 1, Address 2, City, State, Country(USA), Zip Code, Phone Number, Email Address(use PP email)

        createEntryWithDelimter(recordData, "URL");
        createEntryWithDelimter(recordData, "Legal Business Name");
        createEntryWithDelimter(recordData, "Trade Name");
        createEntryWithDelimter(recordData, "DBA/Billing Descriptor");
        createEntryWithDelimter(recordData, "MCC");
        createEntryWithDelimter(recordData, "ID1");
        createEntryWithDelimter(recordData, "ID2");
        createEntryWithDelimter(recordData, "Address1");
        createEntryWithDelimter(recordData, "Address2");
        createEntryWithDelimter(recordData, "City");
        createEntryWithDelimter(recordData, "Region/State");
        createEntryWithDelimter(recordData, "Country");
        createEntryWithDelimter(recordData, "Postal/Zip code");
        createEntryWithDelimter(recordData, "Phone Number");
        createEntryWithDelimter(recordData, "Email Address");
        createEntryWithDelimter(recordData, "Additional Data 1");
        recordData.append("Additional Data 2");

        recordData.append(FILE_EOL);
    }

    /**
     * This method will be used to apply any constraints on the field.Currently we dont have any constraints
     *
     * @param field
     * @return
     */
    private String applyFieldConstraint(String field) {
        if (field == null) {
            return "";
        }
        return field;
    }
}

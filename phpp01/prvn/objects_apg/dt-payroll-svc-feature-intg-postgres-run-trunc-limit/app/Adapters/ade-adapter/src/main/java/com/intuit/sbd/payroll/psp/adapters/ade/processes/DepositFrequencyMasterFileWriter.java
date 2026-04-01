package com.intuit.sbd.payroll.psp.adapters.ade.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.FrequencyMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.JurisdictionIdMapper;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.TaxPaymentGroupIdMapper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.schema.payroll.v3.common.FrequencyEnum;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.*;

/**
 * User: Shivanandad069
 * Date: 10/29/13
 */
public class DepositFrequencyMasterFileWriter implements Runnable {
    private static final String MM_DD_YYYY = "MM-dd-yyyy";
    protected static final SpcfLogger logger = Application.getLogger(DepositFrequencyMasterFileWriter.class);

    public static String SOURCE_SYSTEM = "ASSISTED";

    private static final char FIELD_DELIMITER = '|';

    private String state;
    private int year = 0;
    private int quarter = 0;
    private File file;

    public DepositFrequencyMasterFileWriter(String pState, int pYear, int pQuarter,  File pFile) {
        state = pState;
        year = pYear;
        quarter = pQuarter;
        file = pFile;
    }

    public void run() {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
        List<Object[]> customerDFInfo = getActiveAssistedCustomerDFDataForState();

        try {

            if (file == null) {
                throw new RuntimeException("File not initialized.");
            }

            OutputStream outputStream = new FileOutputStream(file, true);

            Application.beginUnitOfWork();

            // Find the Agency Request.

            int count = writeToStream(customerDFInfo, outputStream);
            outputStream.close();


        } catch (FileNotFoundException fnf) {
            logger.error("File Not Found Exception", fnf);
        } catch (IOException io) {
            logger.error("I/O Exception", io);
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    private List<Object[]> getActiveAssistedCustomerDFDataForState() {
        String[] params = new String[]{"yearQuarter", "state", "excludeDeletedCompany"};
        String yearQuarter = Integer.toString(year) + Integer.toString(quarter);
        Object[] values = new Object[]{yearQuarter, state + "-%", !AuthUser.hasSAPAdminAccess()};
        return Application.executeNamedQuery(
                Application.getQueryName("findCustomerDepositFrequencyDataForStateEnc"), params, values);
    }

    private int writeToStream(List<Object[]> customers,OutputStream outputStream) throws IOException {

        int recordCount = 0;
        final MathContext rounding = new MathContext(6);      // Round off to fix any floating point issues.
        final byte[] assisted = SOURCE_SYSTEM.getBytes();
        List <String>paymentTemplateAdded= new ArrayList<String>();
        Map <String,List<String>>sourceCompanyPaymentTemplateAdded = new HashMap<String,List<String>>();
        final char lineFeed = '\n';
        String zipCode;
        final char hyphen = '-';
        String zipExtension;
        final byte[] stateBytes = state.getBytes();

        for (Object[] customer : customers) {

            // Don't put the same company in the request file twice for same payment template.
            paymentTemplateAdded=sourceCompanyPaymentTemplateAdded.get((String)customer[0]);
            if(paymentTemplateAdded  == null){
                paymentTemplateAdded=  new ArrayList<String>();
                paymentTemplateAdded.add((String) customer[19]) ;
                sourceCompanyPaymentTemplateAdded.put((String)customer[0],paymentTemplateAdded);
            } else if(paymentTemplateAdded.contains((String)customer[19])) {
                continue;
            }else{
                paymentTemplateAdded.add((String) customer[19]) ;
                sourceCompanyPaymentTemplateAdded.put((String) customer[0], paymentTemplateAdded);
            }
            writeField(assisted, outputStream);                               // Platform Identifier
            writeCEPMappedCanonicalField((String) customer[19], outputStream, CEP_CANONICAL_MAPPED_FILEDS.PAYMENT_TEMPLATE_ID);                     //Paymenttemplaet id
            writeField(EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName,(String) customer[2]), outputStream);
            writeField(EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName,(String) customer[1]), outputStream);
            writeField((String) customer[0], outputStream);                    //Source companyId
            writeField((String) customer[3], outputStream);                    // Legal Name
            writeField((String) customer[4], outputStream);                    // Legal Address Line 1
            writeField((String) customer[5], outputStream);                    // Legal Address Line 2
            writeField((String) customer[6], outputStream);                    // City
            writeCEPMappedCanonicalField((String) customer[7], outputStream,CEP_CANONICAL_MAPPED_FILEDS.JURISIDCTION);                    // State

            // Zip code with "+4" if available.  Some of our zip codes have hyphens in the DB.
            // Since most do not, remove any hyphens and then join with a hyphen in the middle.
            zipCode = StringUtils.remove((String) customer[8], hyphen).trim();
            outputStream.write(zipCode.getBytes());
            zipExtension = (String) customer[9];
            if (zipExtension != null) {
                outputStream.write(hyphen);
                outputStream.write(StringUtils.remove(zipExtension, hyphen).trim().getBytes());
            }
            outputStream.write(FIELD_DELIMITER);


            /*
            // Combine first and last name into same field with space separator.
            outputStream.write(((String)customer[10]).getBytes());            // Payroll Admin Contact First Name
            lastName = (String)customer[11];                                  // Payroll Admin Contact Last Name
            if (lastName != null) {
                outputStream.write(space);
                outputStream.write(lastName.getBytes());
            }
            outputStream.write(FIELD_DELIMITER);
             */
            writeField((String) customer[12], outputStream);                   // Payroll Admin Phone
            //writeField((String)customer[13], outputStream);                   // Payroll Admin FAX
            //writeField((String)customer[14], outputStream);                   // Payroll Admin Email
           // writeField(stateBytes, outputStream);                                   // Jurisdiction (Two Letter State Code)
            writeCEPMappedCanonicalField(state, outputStream,CEP_CANONICAL_MAPPED_FILEDS.JURISIDCTION);                 // Jurisdiction (Two Letter State Code)
            writeCEPMappedCanonicalField((String) customer[16], outputStream, CEP_CANONICAL_MAPPED_FILEDS.DEPOSIT_FREQUENCY);                // deposit frequency
            writeDateField(getSpcfCalendarDate((Timestamp) customer[15]), outputStream);                        // EffectiveDate
            writeCEPMappedCanonicalField((String) customer[18], outputStream, CEP_CANONICAL_MAPPED_FILEDS.DEPOSIT_FREQUENCY);            // deposit frequency
            writeDateField(getSpcfCalendarDate((Timestamp) customer[17]), outputStream);                        // future EffectiveDate
            writeDateField(getSpcfCalendarDate((Timestamp) customer[21]), outputStream);                       // Form Usage Date/Intuit Response date
            outputStream.write(lineFeed);
            recordCount++;
        }

        paymentTemplateAdded= null;
        sourceCompanyPaymentTemplateAdded = null;
        return recordCount;
    }

    private void writeCEPMappedCanonicalField(String data, OutputStream pOutputStream, CEP_CANONICAL_MAPPED_FILEDS pMappedField) {
        try {
            if (pMappedField == CEP_CANONICAL_MAPPED_FILEDS.PAYMENT_TEMPLATE_ID) {
                if(data == null || data.isEmpty()){
                    pOutputStream.write(FIELD_DELIMITER);
                }  else{
                    String taxPaymentGroupId = TaxPaymentGroupIdMapper.getComplianceTaxPayGroupIdByPSPPaymentTemplateCd(data);
                    writeField(taxPaymentGroupId, pOutputStream);
                }

            } else if (pMappedField == CEP_CANONICAL_MAPPED_FILEDS.JURISIDCTION) {
                if(data == null || data.isEmpty()){
                    pOutputStream.write(FIELD_DELIMITER);
                }  else{
                    String complianceJurisdiction = JurisdictionIdMapper.getComplianceJurisdictionId("US", data);
                    writeField(complianceJurisdiction, pOutputStream);
                }

            } else if (pMappedField == CEP_CANONICAL_MAPPED_FILEDS.DEPOSIT_FREQUENCY) {
                if(data == null || data.isEmpty()){
                    pOutputStream.write(FIELD_DELIMITER);
                }  else{
                    FrequencyEnum frequencyEnum = FrequencyMapper.getComplainceFrequencyByDepositFrequencyCode(getDepositFrequencyCode(data));
                    writeField(frequencyEnum.name(), pOutputStream);
                }
            }
        } catch (Exception ex) {
            try {
                pOutputStream.write(FIELD_DELIMITER);
            } catch (IOException e) {
                logger.info("Exception while writing to DF stread data for field" + pMappedField.name() + " and data is " + data);
            }
            logger.info("Exception while getting CEP mapped data for field" + pMappedField.name() + " and data is " + data);
        }

    }

    DepositFrequencyCode getDepositFrequencyCode(String depositFrequency) {

        if (depositFrequency == null) {
            return null;
        }
        return DepositFrequencyCode.valueOf(depositFrequency);
    }

    private void writeField(String value, OutputStream outputStream) throws IOException {
        if (value != null) {
            // If the field contains the delimiter, replace it with a space.
            value = value.replace(FIELD_DELIMITER, ' ');
            writeField(value.getBytes(), outputStream);
        } else {
            outputStream.write(FIELD_DELIMITER);
        }
    }

    private void writeField(byte[] value, OutputStream outputStream) throws IOException {
        outputStream.write(value);
        outputStream.write(FIELD_DELIMITER);
    }

    private void writeDateField(SpcfCalendar date, OutputStream outputStream) throws IOException {
        if (date != null) {
            try {
                String effectiveDate = date.format(MM_DD_YYYY);
                writeField(effectiveDate.getBytes(), outputStream);
            } catch (Exception ex) {
                outputStream.write(FIELD_DELIMITER);
            }

        } else {
            outputStream.write(FIELD_DELIMITER);
        }
    }

    /**
     * Gets the database timestamp
     *
     * @return SpcfCalendar
     */
    public static SpcfCalendar getSpcfCalendarDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }

        return SpcfCalendar.createInstance(timestamp.getTime(), SpcfTimeZone.getLocalTimeZone());
    }

    private enum CEP_CANONICAL_MAPPED_FILEDS {
        PAYMENT_TEMPLATE_ID,
        DEPOSIT_FREQUENCY,
        JURISIDCTION
    }
}

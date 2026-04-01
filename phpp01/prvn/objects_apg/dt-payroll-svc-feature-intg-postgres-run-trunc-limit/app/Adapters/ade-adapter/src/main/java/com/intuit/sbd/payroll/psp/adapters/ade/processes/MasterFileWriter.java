package com.intuit.sbd.payroll.psp.adapters.ade.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.RateRequestStatus;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * User: TimothyD698
 * Date: 1/31/13
 */
public class MasterFileWriter implements Runnable {

    protected static final SpcfLogger logger = Application.getLogger(MasterFileWriter.class);

    public static String SOURCE_SYSTEM = "ASSISTED";

    private static final char FIELD_DELIMITER = '|';

    private String state;
    private int year = 0;
    private int quarter = 0;
    private String requestSeq;
    private File file;

    public MasterFileWriter(String pState, int pYear, int pQuarter, String pRequestSeq, File pFile) {
        state = pState;
        year = pYear;
        quarter = pQuarter;
        requestSeq = pRequestSeq;
        file = pFile;
    }

    public void run() {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.AgencyDataExchange);
        List<Object[]> customerSuiInfo = getActiveAssistedCustomerSuiRatesForState();

        try {

            if (file == null) {
                throw new RuntimeException("File not initialized.");
            }

            OutputStream outputStream = new FileOutputStream(file, true);

            Application.beginUnitOfWork();

            // Find the Agency Request.
            AgencyRateRequest request = Application.findById(AgencyRateRequest.class, SpcfUniqueId.createInstance(requestSeq));

            int count = writeToStream(customerSuiInfo, request, outputStream);
            outputStream.close();

            request.setRecordCount(count);
            request.setStatus(AgencyRateRequestStatus.RequestGenerated);
            Application.commitUnitOfWork();

        } catch (FileNotFoundException fnf) {
            logger.error("File Not Found Exception", fnf);
        } catch (IOException io) {
            logger.error("I/O Exception", io);
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    private List<Object[]> getActiveAssistedCustomerSuiRatesForState() {
        String[] params = new String[]{"yearQuarter", "state", "excludeDeletedCompany"};
        String yearQuarter = Integer.toString(year) + Integer.toString(quarter);
        Object[] values = new Object[]{yearQuarter, state + "-%", !AuthUser.hasSAPAdminAccess()};
        return Application.executeNamedQuery(
                Application.getQueryName("findSuiCustomerRatesDataForStateEnc"), params, values);
    }

    private int writeToStream(List<Object[]> customers, AgencyRateRequest request, OutputStream outputStream) throws IOException {

        int recordCount = 0;
        final MathContext rounding = new MathContext(6);      // Round off to fix any floating point issues.
        final byte[] assisted = SOURCE_SYSTEM.getBytes();
        String lastSourceCompanyId = null;
        final char lineFeed = '\n';
        final byte[] sui = "SUI".getBytes();
        String zipCode;
        final char hyphen = '-';
        final char space = ' ';
        String zipExtension;
        final byte[] stateBytes = state.getBytes();
        String lastName;
        Double rate;
        String rateString;

        for (Object[] customer : customers) {

            // Don't put the same company in the request file twice.  This happens when we have duplicate
            // Company Law records, or multiple Company Law Rate records that are valid date-wise.
            if ((customer[0]).equals(lastSourceCompanyId)) {
                continue;
            }

            writeField(assisted, outputStream);                               // Platform Identifier

            lastSourceCompanyId = (String)customer[0];                        // Source Company Id
            writeField((String)customer[0], outputStream);

            writeField(EncryptionUtils.deterministicDecrypt(Company.FedTaxIdKeyName,(String) customer[1]), outputStream);
            writeField(EncryptionUtils.deterministicDecrypt(CompanyAgencyPaymentTemplate.AgencyTaxPayerIdKeyName,(String) customer[2]), outputStream);
            writeField((String)customer[3], outputStream);                    // Legal Name
            writeField((String)customer[4], outputStream);                    // Legal Address Line 1
            writeField((String)customer[5], outputStream);                    // Legal Address Line 2
            writeField((String)customer[6], outputStream);                    // City
            writeField((String)customer[7], outputStream);                    // State

            // Zip code with "+4" if available.  Some of our zip codes have hyphens in the DB.
            // Since most do not, remove any hyphens and then join with a hyphen in the middle.
            zipCode = StringUtils.remove((String) customer[8], hyphen).trim();
            outputStream.write(zipCode.getBytes());
            zipExtension = (String)customer[9];
            if (zipExtension != null) {
                outputStream.write(hyphen);
                outputStream.write(StringUtils.remove(zipExtension, hyphen).trim().getBytes());
            }
            outputStream.write(FIELD_DELIMITER);

            writeField(stateBytes, outputStream);                             // Jurisdiction (Two Letter State Code)

            // Combine first and last name into same field with space separator.
            outputStream.write(((String)customer[10]).getBytes());            // Payroll Admin Contact First Name
            lastName = (String)customer[11];                                  // Payroll Admin Contact Last Name
            if (lastName != null) {
                outputStream.write(space);
                outputStream.write(lastName.getBytes());
            }
            outputStream.write(FIELD_DELIMITER);

            writeField((String)customer[12], outputStream);                   // Payroll Admin Phone
            writeField((String)customer[13], outputStream);                   // Payroll Admin FAX
            writeField((String)customer[14], outputStream);                   // Payroll Admin Email

            rate = (Double)customer[15];                        // Rate
            if (rate != null) {
                rateString = BigDecimal.valueOf(rate * 100).round(rounding).stripTrailingZeros().toPlainString();
                outputStream.write(rateString.getBytes());
            }
            outputStream.write(FIELD_DELIMITER);

            outputStream.write(sui);                            // Rate Type

            outputStream.write(lineFeed);

            // Create the rate request record.
            CompanyRateRequest coRequest = new CompanyRateRequest();
            CompanyAgency agency = Application.findById(CompanyAgency.class, SpcfUniqueId.createInstance((String)customer[16]));
            coRequest.setCompanyAgency(agency);
            coRequest.setAgencyRateRequest(request);
            coRequest.setStatus(RateRequestStatus.Waiting);
            coRequest.setOldRate(rate);
            Application.save(coRequest);

            recordCount++;
        }

        return recordCount;
    }

    private void writeField( String value, OutputStream outputStream ) throws IOException {
        if (value != null) {
            // If the field contains the delimiter, replace it with a space.
            value = value.replace(FIELD_DELIMITER, ' ');
            writeField(value.getBytes(), outputStream);
        } else {
            outputStream.write(FIELD_DELIMITER);
        }
    }

    private void writeField( byte[] value, OutputStream outputStream ) throws IOException {
        outputStream.write(value);
        outputStream.write(FIELD_DELIMITER);
    }

}

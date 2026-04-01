package com.intuit.sbd.payroll.psp.adapters.ade.tools;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ade.processes.DepositFrequencyMasterFileWriter;
import com.intuit.sbd.payroll.psp.adapters.ade.processes.MasterFileWriter;
import com.intuit.sbd.payroll.psp.adapters.ade.processes.RateApplyProcess;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * User: TimothyD698
 * Date: 1/3/13
 */
public class ADETool {

    private static final String STATE_COMMAND = "-state";
    private static final String YEAR_COMMAND = "-year";
    private static final String QUARTER_COMMAND = "-quarter";
    private static final String OUTPUT_FORMAT_COMMAND = "-outputFormat";

    // support "ALL" option for state command - all states except "IN"
    // "IN" is *not* in this list - since PSP does not support it
    private static final String ALL_STATES = "AL,AK,AZ,AR,CA,CO,CT,DE,DC,FL,GA,HI,ID,IL,IA,KS,KY,LA,ME,MD,MA,MI,MN,MS,MO,MT,NE,NV,NH,NJ,NM,NY,NC,ND,OH,OK,OR,PA,RI,SC,SD,TN,TX,UT,VT,VA,WA,WV,WI,WY";

    public static final String FILE_EXT = ".csv";
    public static String REQUEST_FOLDER = "/request/";

    private List<String> states;
    private int year = 0;
    private int quarter = 0;
    private outputFormatType outputFormat;
    String currentDateAndTimeForFileName;

    protected static final SpcfLogger logger = Application.getLogger(ADETool.class);

    public enum outputFormatType {
        ADE,
        PSP,
        ADEDF
    }

    public static void main( String[] args ) throws Exception {

        logger.info("ADETool Started.");

        ADETool ade = new ADETool();

        ade.parseArgs(args);

        ade.verifyParameters();

        ade.process();

        logger.info("ADETool Completed.");
    }

    public ADETool() {

    }

    public ADETool(String pState, int pYear, int pQuarter) {
        states = new ArrayList<String>();
        states.add(pState);
        year = pYear;
        quarter = pQuarter;
    }

    public ADETool(List<String> pStates, int pYear, int pQuarter) {
        states = pStates;
        year = pYear;
        quarter = pQuarter;
    }
    //Used only in DF master data file
    public ADETool(String pState, int pYear, int pQuarter,String currentDateAndTimeForFileName) {
        states = new ArrayList<String>();
        states.add(pState);
        year = pYear;
        quarter = pQuarter;
        this.currentDateAndTimeForFileName=currentDateAndTimeForFileName;
    }

    //Used only in DF master data file
    public ADETool(List<String> pStates, int pYear, int pQuarter,String currentDateAndTimeForFileName) {
        states = pStates;
        year = pYear;
        quarter = pQuarter;
        this.currentDateAndTimeForFileName= currentDateAndTimeForFileName;
    }
    public void process() {
        if (outputFormat == outputFormatType.ADE) {
            generateAdeRequest();
        } else if (outputFormat == outputFormatType.PSP) {
            applyAdeRates();
        } else if (outputFormat == outputFormatType.ADEDF) {
            generateAdeDepositFrequencyRequest();
        }
    }

    public void generateAdeRequest() {

        File file = null;

        for (String state : states) {
            Application.beginUnitOfWork();
            AgencyRateRequest request = new AgencyRateRequest();
            request.setRecordCount(0);
            request.setStatus(AgencyRateRequestStatus.GeneratingRequest);
            request.setYearQuarter(Integer.toString(year) + quarter);

            Law suiErLaw = Law.getSuiLaw(state, LawCategoryCode.UnemploymentEmployer);
            request.setAgency(suiErLaw.getPaymentTemplate().getAgency());
            Application.save(request);
            Application.commitUnitOfWork();

            if (file == null) {
                file = createFile(request);
            }

            MasterFileWriter masterFileWriter = new MasterFileWriter(state, year, quarter, request.getId().toString(), file);
            masterFileWriter.run();

            Application.beginUnitOfWork();
            Application.refresh(request);
            request.setStatus(AgencyRateRequestStatus.RequestGenerated);
            Application.commitUnitOfWork();
        }
    }

    public void applyAdeRates(String... agencyNames) {
        // We should only apply one state at a time.
        String state = states.get(0);

        // Find the SUI agency for the given state.
        DomainEntitySet<PaymentTemplate> paymentTemplates = Application.find(PaymentTemplate.class, PaymentTemplate.Category().equalTo(PaymentTemplateCategory.SUI)
                .And(PaymentTemplate.PaymentTemplateCd().like(state + "-%")));
        Agency agency = paymentTemplates.getFirst().getAgency();

        if(agencyNames.length !=0) {
            List<PaymentTemplate> filteredPaymentTemplates = paymentTemplates
                    .stream().filter(pt -> pt.getAgency().getAgencyId().equals(agencyNames[0]))
                    .collect(Collectors.toList());
            if(filteredPaymentTemplates.size() == 1) {
                agency = filteredPaymentTemplates.get(0).getAgency();
            }
        }

        // Look for the request that is pending a response.
        DomainEntitySet<AgencyRateRequest> requests = Application.find(AgencyRateRequest.class,
                                                                       AgencyRateRequest.Agency().equalTo(agency)
                                                                        .And(AgencyRateRequest.YearQuarter().equalTo(Integer.toString(year) + quarter))
                                                                        .And(AgencyRateRequest.Status().equalTo(AgencyRateRequestStatus.ResponseReceived)));
        if (requests.size() == 0) {
            logger.error("Unable to find a request for " + agency.getAgencyId() + "/" + year + "/" + quarter);
            throw new RuntimeException("Unable to find a request for " + agency.getAgencyId() + "/" + year + "/" + quarter);
        } else if (requests.size() > 1) {
            logger.error("More than one request found for " + agency.getAgencyId() + "/" + year + "/" + quarter);
            throw new RuntimeException("More than one request found for " + agency.getAgencyId() + "/" + year + "/" + quarter);
        }

        AgencyRateRequest request = requests.getFirst();

        Application.beginUnitOfWork();
        request.setStatus(AgencyRateRequestStatus.ResponseApplying);
        Application.commitUnitOfWork();

        RateApplyProcess applyProcess = new RateApplyProcess(state, year, quarter, request.getId().toString());
        applyProcess.run();
    }

    private void parseArgs(String[] args) {

        final int param = 0;
        final int value = 1;

        // For each provided argument
        for (String arg : args) {
            // Arguments should be of the form param=value
            String[] argParts = arg.split("=");
            if(argParts.length == 2) {

                if(argParts[param].equals(STATE_COMMAND)) {
                    // Convert the comma separated list into a List.
                    states = new ArrayList<String>();
                    String[] stateTokens ;
                    if("ALL".equals(argParts[value])) {
                        stateTokens = ALL_STATES.split(",");
                    } else  {
                        stateTokens = argParts[value].split(",");
                    }
                    for (String state : stateTokens) {
                        if (state.length() != 2) {
                            throw new RuntimeException("Invalid parameter value for state - " + state);
                        } else {
                            states.add(state);
                        }
                    }
                } else if(argParts[param].equals(YEAR_COMMAND)) {
                    year = Integer.parseInt(argParts[value]);
                    if (year < 2012 || year > 2020) {
                        throw new RuntimeException("Invalid parameter value for year - " + year);
                    }
                } else if(argParts[param].equals(QUARTER_COMMAND)) {
                    quarter = Integer.parseInt(argParts[value]);
                    if (quarter < 1 || quarter > 4) {
                        throw new RuntimeException("Invalid parameter value for quarter - " + quarter);
                    }
                } else if (argParts[param].equals(OUTPUT_FORMAT_COMMAND)) {
                    // Must be "PSP" or "ADE" or "ADEDF".
                    if ("PSP".equals(argParts[value])) {
                        outputFormat = outputFormatType.PSP;
                    } else if ("ADE".equals(argParts[value])) {
                        outputFormat = outputFormatType.ADE;
                    } else if ("ADEDF".equals(argParts[value])) {
                        outputFormat = outputFormatType.ADEDF;
                    }else {
                        throw new RuntimeException("Invalid parameter value for outputFormat - " + argParts[value]);
                    }
                } else {
                    throw new RuntimeException("Invalid parameter: " + argParts[param]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }
    }

    private void verifyParameters() {

        // Verify required parameters.
        if (states == null || states.size() < 1 || year == 0 || quarter == 0 || outputFormat == null) {
            logger.error("Invalid parameters - Usage: ADETool -state=AA -year=YYYY -quarter=Q -outputFormat=PSP|ADE");
            System.exit(-1);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Running ADETool for " + year + "/Q" + quarter + " for the following states : ");
            for (String state : states) {
                sb.append(state);
                sb.append(", ");
            }
            logger.info(sb.toString());
        }
    }

    private File createFile(AgencyRateRequest pRequest) {
        String DIR_ROOT = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_ade_directory_root");
        String REQUEST_FOLDER = "/request/";

        // Make sure the directory path exists.
        String fileDir = DIR_ROOT + REQUEST_FOLDER;
        ADERateUtils.verifyDirectory(fileDir);

        String fileName;
        if (states.size() == 1) {
            // State specific request.
            fileName = String.format("%s_%d_%d", states.get(0), year, quarter);
        } else {
            // Single request for multiple states.  Use one file that we append each state to.
            fileName = String.format("ADE_MASTER_FILE_%d_%d", year, quarter);
        }

        int i = 1;
        File file = new File(String.format("%s%s%s", fileDir, fileName, FILE_EXT));
        while (file.exists()) {
            file = new File(String.format("%s%s (%d)%s", fileDir, fileName, i++, FILE_EXT));
        }

        return file;
    }

    private File createDepositFrequencyFile() {
        String DIR_ROOT = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, "psp_ade_directory_root");
        String REQUEST_FOLDER = "/request/";

        // Make sure the directory path exists.
        String fileDir = DIR_ROOT + REQUEST_FOLDER;
        ADERateUtils.verifyDirectory(fileDir);

        String fileName;
        if (states.size() == 1) {
            // State specific request.
            fileName = String.format("%s_DEPOSIT_FREQUENCY_%d_%d", states.get(0), year, quarter);
        } else {
            // Single request for multiple states.  Use one file that we append each state to.
            fileName = String.format("ADE_DEPOSIT_FREQUENCY_MASTER_FILE_%d_%d", year, quarter);
        }
        //This is just make sure file name is unique each time it generated
        if(currentDateAndTimeForFileName != null){
            fileName = fileName+"_"+currentDateAndTimeForFileName;
        }  else{
            SimpleDateFormat simpleDateFormat =new SimpleDateFormat("MMddyyyy-hhmmss.SSS");
            String dateAndTime= simpleDateFormat.format( new Date() ) ;
            fileName = fileName+"_"+dateAndTime;
        }


        int i = 1;
        File file = new File(String.format("%s%s%s", fileDir, fileName, FILE_EXT));
        while (file.exists()) {
            file = new File(String.format("%s%s (%d)%s", fileDir, fileName, i++, FILE_EXT));
        }

        return file;
    }
    public void generateAdeDepositFrequencyRequest() {
        File file = null;
        for (String state : states) {
            if (file == null) {
                file = createDepositFrequencyFile();
            }
            DepositFrequencyMasterFileWriter masterFileWriter = new DepositFrequencyMasterFileWriter(state, year, quarter, file);
            masterFileWriter.run();
        }
    }
}

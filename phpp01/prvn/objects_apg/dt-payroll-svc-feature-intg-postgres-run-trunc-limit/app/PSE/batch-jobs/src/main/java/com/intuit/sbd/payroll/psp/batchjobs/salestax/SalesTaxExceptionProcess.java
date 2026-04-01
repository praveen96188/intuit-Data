package com.intuit.sbd.payroll.psp.batchjobs.salestax;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.BillingDetail;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 21, 2008
 * Time: 2:01:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalesTaxExceptionProcess {
    private static SpcfLogger logger = Application.getLogger(SalesTaxExceptionProcess.class);
    private static final String INPUT_DATE_FORMAT = "yyyyMMdd";
    private HashMap<PayrollRun, DomainEntitySet<BillingDetail>> mBillingDetailMap =
            new HashMap<PayrollRun, DomainEntitySet<BillingDetail>>();

    public static void main(String args[]) {
        String vertexUpdateDate;
        try {
            //Input argument validation
            if (args == null || args.length != 1) {
                throw new RuntimeException(
                        "Wrong number of parameters. Usage: SalesTaxExceptionProcess vertexUpdateDate as yyyyMMdd");
            }

            if (args[0] == null || args[0].length() != 8) {
                throw new RuntimeException(
                        "Invalid VertexUpdate date format " + args[0] + ".  Correct format: yyyyMMdd");
            }
            vertexUpdateDate = args[0];

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.SalesTaxExceptionsBatchJob));
            //Process
            PayrollServices.beginUnitOfWork();
            SalesTaxExceptionProcess process = new SalesTaxExceptionProcess();
            process.process(vertexUpdateDate);
            PayrollServices.commitUnitOfWork();

        } catch (Throwable ex) {
            logger.fatal(ex.getMessage(), ex);
            PayrollServices.rollbackUnitOfWork();
            System.exit(1);
        }
    }

    /**
     * Process to get the offloaded transactions that include tax amounts that may have changed between tax-calculation
     * and offload and re-calculate those tax amounts using current rates, getting them by making calls to
     * Sales Tax Gateway for each of BillingDetails related to the same payroll run.
     *
     * @param pVertexUpdateDate String
     * @throws Exception exception
     */
    public void process(String pVertexUpdateDate) {
        SpcfCalendar vertexUpdateDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        if (pVertexUpdateDate != null) {
            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern(INPUT_DATE_FORMAT);
            SpcfCalendar parsedVertexDate = dateFormat.parse(pVertexUpdateDate);
            //Set the date on the calendar that has the local time zone
            vertexUpdateDate.setValues(parsedVertexDate.getYear(), parsedVertexDate.getMonth(), parsedVertexDate.getDay());
        }

        //Get the Billing details with the input Vertex-Update date
        DomainEntitySet<BillingDetail> billingDetilList = getBillingDetails(vertexUpdateDate);

        DomainEntitySet<BillingDetail> billingDetails;

        //Group the BillingDetails for each payroll run
        for (BillingDetail billingDetail : billingDetilList) {
            if (mBillingDetailMap.containsKey(billingDetail.getPayrollRun())) {
                billingDetails = mBillingDetailMap.get(billingDetail.getPayrollRun());
                billingDetails.add(billingDetail);
            } else {
                billingDetails = new DomainEntitySet<BillingDetail>();
                billingDetails.add(billingDetail);
                mBillingDetailMap.put(billingDetail.getPayrollRun(), billingDetails);
            }
        }

        //Calculate the sales tax by making calls to Sales Tax Gateway component for each group of Billing Details
        //that is related to the same payroll run, then update each Billing Detail TaxAmountWhenOffloaded property with
        //returned tax amount.
        String NL = System.getProperty("line.separator");
        int nWarnings = 0;
        int nRequests = 0;
        int nLines = 0;
        long tStart = PSPDate.getPSPTime().getTimeInMilliseconds();
        try {
            //Loop thru each payroll run to make a call to Sales Tax Gateway component and get the response.
            for (PayrollRun payrollRun : mBillingDetailMap.keySet()) {
                //Get the BillingDetail list for each payroll run from BillingDetail Map
                DomainEntitySet<BillingDetail> details = mBillingDetailMap.get(payrollRun);

                //Make a call to SalesTax Gateway component.
                SalesTaxResponse salesTaxResponse = BillingDetail.getSalesTaxInfo(payrollRun, details);
                ++nRequests;
                nLines += details.size();

                //If condition to check if the SalesTaxResoponse from the SalesTaxGateway Component is success/failure.
                //If the response is success then update BillingDetail TaxAmountWhenOffloaded property with responseline
                //Tax amount and save the billingDetail object
                if (salesTaxResponse == null) {
                    // no response
                    String msg = "Unable to get sales tax for company " +
                            payrollRun.getCompany().getSourceSystemCd() + ":" + payrollRun.getCompany().getSourceCompanyId();
                    msg = msg + NL + "No response from Sales Tax Gateway";
                    logger.error(msg);
                } else if (! salesTaxResponse.isSuccess()) {
                    // error response, build the message and log it as a warning
                    Company company = payrollRun.getCompany();
                    String msg = "Unable to get sales tax for company " +
                            company.getSourceSystemCd() + ":" + company.getSourceCompanyId();

                    ErrorMessage summary = salesTaxResponse.getSummaryErrorMessage();
                    msg = msg + NL + "Sales Tax Gateway Summary Error " +
                            summary.getErrorCode() + ": " + summary.getErrorDescription();

                    for (ErrorMessage detail : salesTaxResponse.getDetailErrorMessageList()) {
                        msg = msg + NL + "Sales Tax Gateway Detail Error " +
                                detail.getErrorCode() + ": " + detail.getErrorDescription();
                    }

                    logger.warn(msg);
                    ++nWarnings;
                } else {
                    // all is well
                    for (BillingDetail billingDetail : details) {
                        for (SalesTaxResponseLine responseLine : salesTaxResponse.getSalesTaxResponseLineList()) {
                            if (billingDetail.getItemSku().equals(responseLine.getSKU())) {
                                billingDetail.setTaxAmountWhenOffloaded(SpcfUtils.convertToSpcfMoney(responseLine.getTaxAmount()));
                                billingDetail = Application.save(billingDetail);
                            }
                        }
                    }
                }
            }
        } finally {
            if (nWarnings > 0) {
                logger.error("Logged "+nWarnings+" warnings for unsuccessful calls to the SalesTaxGateway");
            } else {
                logger.info("No SalesTaxGateway failure warnings were logged");
            }
            long tEnd = PSPDate.getPSPTime().getTimeInMilliseconds();
            long seconds = (tEnd - tStart + 500) / 1000;
            long average = (nRequests>0) ? ((tEnd-tStart)/nRequests) : 0;
            logger.info("Processed "+nRequests+" requests containing "+nLines+" lines in "+seconds+" seconds = "+average+" ms per request");
        }
    }

    /**
     * Function to get the billing details
     *
     * @param pVertexUpdateDate SpcfCalendar
     * @return DomainEntitySet<BillingDetail>
     */
    public static DomainEntitySet<BillingDetail> getBillingDetails(SpcfCalendar pVertexUpdateDate) {
        SpcfCalendar currentDate = PSPDate.getPSPTime();
        CalendarUtils.clearTime(currentDate);
        String[] paramNames = new String[3];

        paramNames[0] = "currentDate";
        paramNames[1] = "vertexUpdateDate";
        paramNames[2] = "txnStateCd";

        Object[] paramValues = new Object[3];
        paramValues[0] = currentDate;
        paramValues[1] = pVertexUpdateDate;
        paramValues[2] = TransactionStateCode.Executed;

        return Application.findByNamedQueryUsingCache(BillingDetail.class, "findOffloadedBillingDetails", paramNames, paramValues);
    }
    
}

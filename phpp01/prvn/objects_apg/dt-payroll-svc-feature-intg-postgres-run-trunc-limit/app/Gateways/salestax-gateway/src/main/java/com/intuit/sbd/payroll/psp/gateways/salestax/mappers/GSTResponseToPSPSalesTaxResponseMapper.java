package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.GSTError;
import com.intuit.gst.GSTResponse;
import com.intuit.gst.SalesTaxLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
public class GSTResponseToPSPSalesTaxResponseMapper extends BeanMapper<GSTResponse, SalesTaxResponse> {
    @Override
    public SalesTaxResponse mapToTarget(GSTResponse gstResponse, Class<SalesTaxResponse> salesTaxResponseClass) {
        //check at the very start for all the nulls
        checkForNullResultObjects(gstResponse);
        SalesTaxResponse pspSalesTaxResponse = new SalesTaxResponse();

        if(checkIfGSTResponseSuccessful(gstResponse)) {
            //check and set if gst sales tax call succeeded
            pspSalesTaxResponse.setSuccess(true);

            //todo - check if this is correct (2)
            checkForNullObjectsBeforeTotalTaxCalc(gstResponse);
            BigDecimal pspTotalTaxAmount = getTotalTaxValueFromGSTResponse(gstResponse);
            pspSalesTaxResponse.setTotalTaxAmount(pspTotalTaxAmount);

            //iterate over the sales tax line items
            for (SalesTaxLine gstSalesTaxLine : gstResponse.getSalesTaxLine()) {
                //todo - check this mapper (2)
                SalesTaxResponseLine pspSalesTaxResponseLine = getMapperRegistry().mapToTarget(gstSalesTaxLine, SalesTaxResponseLine.class);
                pspSalesTaxResponse.addLine(pspSalesTaxResponseLine);
            }
        } else {
            pspSalesTaxResponse.setSuccess(false);
            //set the error message list
            ArrayList<ErrorMessage> detailErrorMessageList = getErrorMessageListFromGSTResponse(gstResponse);
            //set the summary error message
            ErrorMessage summaryErrorMessage = getMapperRegistry().mapToTarget(gstResponse.getResult().getError(), ErrorMessage.class);
            pspSalesTaxResponse.setDetailErrorMessageList(detailErrorMessageList);
            pspSalesTaxResponse.setSummaryErrorMessage(summaryErrorMessage);

        }

        return pspSalesTaxResponse;
    }


    //This should be called only after it has been verified that none of the objects are null
    private BigDecimal getTotalTaxValueFromGSTResponse(GSTResponse gstResponse) {
        //note that following is the preferred way to convert float to BigDecimal according to javadocs
        return new BigDecimal(Float.toString(gstResponse.getSalesTaxHeader().getSalesQuote().getTotalTax().getValue()));
    }

    public void checkForNullResultObjects(GSTResponse gstResponse) {
        String exceptionMessage = "";
        if(Objects.isNull(gstResponse)) {
            exceptionMessage = "gstResponse is null";
        } else if(Objects.isNull(gstResponse.getResult())) {
            exceptionMessage = "gstResponse.getResult() is null";
        } else if(Objects.isNull(gstResponse.getResult().getStatus())) {
            exceptionMessage = "gstResponse.getResult().getStatus() is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            throw new RuntimeException("Unable to map GSTResponse to PSPSalesTaxResponse"+exceptionMessage);
        }
    }

    private void checkForNullObjectsBeforeTotalTaxCalc(GSTResponse gstResponse) {
        String exceptionMessage = "";
        if(Objects.isNull(gstResponse)) {
            exceptionMessage = "gstResponse is null";
        } else if(Objects.isNull(gstResponse.getSalesTaxHeader())) {
            exceptionMessage = "gstResponse.getSalesTaxHeader() is null";
        } else if(Objects.isNull(gstResponse.getSalesTaxHeader().getSalesQuote())) {
            exceptionMessage = "gstResponse.getSalesTaxHeader().getSalesQuote() is null";
        } else if(Objects.isNull(gstResponse.getSalesTaxHeader().getSalesQuote().getTotalTax())) {
            exceptionMessage = "gstResponse.getSalesTaxHeader().getSalesQuote().getTotalTax() is null";
        }

        if(!StringUtils.isEmpty(exceptionMessage)) {
            String completeExceptionMessage = "Unable to get TotalTax from GSTResponse, " + exceptionMessage;
            throw new RuntimeException(completeExceptionMessage);
        }

    }

    private boolean checkIfGSTResponseSuccessful(GSTResponse gstResponse) {
        return gstResponse.getResult().getStatus().equals("Success");
    }

    private ArrayList<ErrorMessage> getErrorMessageListFromGSTResponse(GSTResponse gstResponse) {
        ArrayList<ErrorMessage> errorMessageList = new ArrayList<>();
        if (Objects.nonNull(gstResponse.getError())) {
            for (GSTError gstError : gstResponse.getError()) {
                //todo - check this mapper (2)
                ErrorMessage pspSalesTaxError = getMapperRegistry().mapToTarget(gstError, ErrorMessage.class);
                errorMessageList.add(pspSalesTaxError);
            }
        }
        return errorMessageList;
    }

}

package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
public class SalesTaxResponseComparisonService {

    private SalesTaxGSTImpl salesTaxGST;

    @Autowired
    public SalesTaxResponseComparisonService(SalesTaxGSTImpl salesTaxGST) {
        this.salesTaxGST = salesTaxGST;
        log.info("Created SalesTaxResponseComparisonService bean successfully");
    }


    @Async("salesTaxComparisonThreadPoolExecutor")
    public void getResponseFromGSTAndShallowCompareWithEOSResponseProvided(SalesTaxRequest pspSalesTaxRequest, SalesTaxResponse salesTaxResponseEOS) {
        String docId = Objects.nonNull(pspSalesTaxRequest) ? pspSalesTaxRequest.getDocumentId() : null;
        log.info("action=SalesTaxShallowComparisonStart docId={}",docId);
        try {
            //1. Retrieve the response from the gst service
            SalesTaxResponse salesTaxResponseGST = salesTaxGST.send(pspSalesTaxRequest);
            String comparisonDiff = "";

            //2. Do first level comparison (total tax amounts)
            if (!salesTaxResponseGST.getTotalTaxAmount().equals(salesTaxResponseEOS.getTotalTaxAmount())) {
                comparisonDiff = String.format("Total tax does not match fromGST=%s fromEOS=%s", salesTaxResponseGST.getTotalTaxAmount(),
                        salesTaxResponseEOS.getTotalTaxAmount());
            } else {
                //3. Compare each line item tax amount
                for (int i = 0; i < salesTaxResponseGST.getSalesTaxResponseLineList().size(); i++) {
                    SalesTaxResponseLine gstLine = salesTaxResponseGST.getSalesTaxResponseLineList().get(i);
                    SalesTaxResponseLine eosLine = salesTaxResponseEOS.getSalesTaxResponseLineList().get(i);

                    if (!compareTaxValuesWithDeltaMargin(gstLine.getTaxAmount(), eosLine.getTaxAmount())) {
                        comparisonDiff = String.format("difference in line item, itemSKU=%s fromGST=%s fromEOS=%s",
                                gstLine.getSKU(), gstLine.getTaxAmount(), eosLine.getTaxAmount());
                        break;
                    }
                }
            }

            //4. log the results
            if (!StringUtils.isEmpty(comparisonDiff)) {
                log.error("action=GSTComparisonDifference docID={} issue={}", docId, comparisonDiff);
            } else {
                log.info("action=GSTComparisonSame docId={} tax={}", docId,
                        salesTaxResponseGST.getTotalTaxAmount());
            }
            log.info("action=SalesTaxShallowComparisonComplete docId={}",docId);
        } catch (Exception ex) {
            String errorMessage = String.format("action=SalesTaxShallowComparisonError docId=%s",docId);
            log.error(errorMessage,ex);
        }
    }

    private boolean compareTaxValuesWithDeltaMargin(BigDecimal val1, BigDecimal val2) {
        if(val1.subtract(val2).floatValue() > -0.001 && val1.subtract(val2).floatValue() < 0.001) {
            return true;
        }
        return false;
    }


}

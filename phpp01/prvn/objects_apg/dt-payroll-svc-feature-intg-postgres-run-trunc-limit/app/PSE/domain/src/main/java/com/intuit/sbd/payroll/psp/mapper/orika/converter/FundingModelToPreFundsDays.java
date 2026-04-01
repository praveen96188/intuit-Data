package com.intuit.sbd.payroll.psp.mapper.orika.converter;

import com.intuit.sbd.payroll.psp.domain.FundingModel;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Used to convert between FundingModel and preFundsDays
 *
 * @author dchoudhary1
 */

@Component
public class FundingModelToPreFundsDays extends BidirectionalConverter<FundingModel, Integer> {

    public static Logger LOGGER = LoggerFactory.getLogger(FundingModelToPreFundsDays.class);

    @Override
    public FundingModel convertFrom(Integer preFundsDay, Type<FundingModel> fundingModel) {
        FundingModel fmodel = new FundingModel();
        if (preFundsDay.equals(1)) {
            fmodel.setFundingModelCd("1D");
        } else if (preFundsDay.equals(2)) {
            fmodel.setFundingModelCd("2D");
        } else if (preFundsDay.equals(5)) {
            fmodel.setFundingModelCd("5D");
        } else {
            LOGGER.error("FundingModelToPreFundsDays:preFundsDay data has invalid value {}", preFundsDay);
        }
        // return default value
        return fmodel;
    }

    @Override
    public Integer convertTo(FundingModel fundingModel, Type<Integer> preFundsDay) {
        System.out.println(fundingModel.getFundingModelCd());
        if (fundingModel.getFundingModelCd().equals("1D")) {
            return 1;
        } else if (fundingModel.getFundingModelCd().equals("2D")) {
            return 2;
        } else if (fundingModel.getFundingModelCd().equals("5D")) {
            return 5;
        } else {
            LOGGER.error("FundingModelToPreFundsDays:fundingModel has invalid value {}",
                    fundingModel.getFundingModelCd());
        }
        // return default value
        return 2;
    }

}
package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.LinkedList;
import java.util.List;

/**
 * Hand-written business logic
 */
public class LimitRule extends BaseLimitRule {

	/**
	 * Default constructor.
	 */
	public LimitRule()
	{
		super();
	}
    
    public static LimitRule findLimitRule(Company pCompany, ServiceCode pServiceCode) {
        DomainEntitySet<CompanyOffering> offerings = pCompany.getCompanyOfferingCollection().find(CompanyOffering.Offering().ServiceCode().equalTo(pServiceCode));
        if(offerings.size() > 0) {
            return offerings.get(0).getOffering().getLimitRule();
        }

        return null;
    }
    
    public List<AutoLimitIncreaseTier> getAutoLimitIncreaseTiers() {
        List<AutoLimitIncreaseTier> autoLimitIncreaseTiers = new LinkedList<AutoLimitIncreaseTier>();

        AutoLimitIncreaseTier autoLimitIncreaseTier = null;
        int tier = 0;
        for (LimitValue limitValue : getLimitValueCollection().find(LimitValue.Tier().greaterThan(0)).sort(LimitValue.Tier().Descending())) {
            if(tier != limitValue.getTier()) {
                tier = limitValue.getTier();
                if(autoLimitIncreaseTier != null) {
                    autoLimitIncreaseTiers.add(autoLimitIncreaseTier);
                }
                autoLimitIncreaseTier = new AutoLimitIncreaseTier();
                autoLimitIncreaseTier.setSourceSystemCd(getSourceSystemCd());
                autoLimitIncreaseTier.setLevel(Integer.toString(tier));
            }
            
            switch (limitValue.getName()) {
                case AutoLimitIncreaseIncreaseMultiplier:
                    autoLimitIncreaseTier.setIncreaseMultiplier(new SpcfMoney(limitValue.getValue()));
                    break;
                case AutoLimitIncreaseMaxCompanyLimit:
                    autoLimitIncreaseTier.setCompanyCap(new SpcfMoney(limitValue.getValue()));
                    break;
                case AutoLimitIncreaseMaxEmployeeLimit:
                    autoLimitIncreaseTier.setPayeeCap(new SpcfMoney(limitValue.getValue()));
                    break;
                case AutoLimitIncreaseMinEarliestPayrollRunDays:
                    autoLimitIncreaseTier.setDaysSinceFirstPayroll(Integer.parseInt(limitValue.getValue()));
                    break;
                case AutoLimitIncreaseMinPayrolls:
                    autoLimitIncreaseTier.setPayrollsRun(Integer.parseInt(limitValue.getValue()));
                    break;                
            }
        }
        if(autoLimitIncreaseTier != null) {
            autoLimitIncreaseTiers.add(autoLimitIncreaseTier);
        }
        return autoLimitIncreaseTiers;
    }

    public LimitValue findLimitValueByName(LimitValueType pLimitValueType) {
        return getLimitValueCollection().findEntity(LimitValue.Name().equalTo(pLimitValueType));
    }

}
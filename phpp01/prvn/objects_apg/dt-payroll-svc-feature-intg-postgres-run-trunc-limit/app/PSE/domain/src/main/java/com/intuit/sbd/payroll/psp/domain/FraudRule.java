package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class FraudRule extends BaseFraudRule {

	/**
	 * Default constructor.
	 */
	public FraudRule()
	{
		super();
	}

    public static FraudRule findFraudRule(Company pCompany) {        
        
        CompanyOffering companyOffering;
        if(pCompany.getSourceSystemCd() == SourceSystemCode.IOP) {
            companyOffering = pCompany.getOffering(ServiceCode.RiskAssessment);
        } else {
            companyOffering = pCompany.getOffering(ServiceCode.DirectDeposit);
        }
        
        if(companyOffering != null && companyOffering.getOffering() != null) {
            return companyOffering.getOffering().getFraudRule();
        }
        return null;
    }
    
    public FraudValue findFraudValueByName(FraudValueType pFraudValueType) {
        return getFraudValueCollection().findEntity(FraudValue.Name().equalTo(pFraudValueType));
    }
}
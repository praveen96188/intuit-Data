package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SourcePayrollParameterDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.webservices.wsdto.ParameterWSDTO;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
    @author Jeff Jones
 */
@WebService()
public class SourceSystemWS {

    private static Map<String, LimitValue> LIMIT_VALUE_MAP = new ConcurrentHashMap<String, LimitValue>();

    static {
        LimitValue limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseIncreaseMultiplier);
        limitValue.setTier(1);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier1IncreaseMultiplier", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMaxCompanyLimit);
        limitValue.setTier(1);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier1MaxCompanyLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMaxEmployeeLimit);
        limitValue.setTier(1);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier1MaxEmployeeLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMinEarliestPayrollRunDays);
        limitValue.setTier(1);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier1MinEarliestPayrollRunDays", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMinPayrolls);
        limitValue.setTier(1);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier1MinPayrolls", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseIncreaseMultiplier);
        limitValue.setTier(2);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier2IncreaseMultiplier", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMaxCompanyLimit);
        limitValue.setTier(2);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier2MaxCompanyLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMaxEmployeeLimit);
        limitValue.setTier(2);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier2MaxEmployeeLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMinEarliestPayrollRunDays);
        limitValue.setTier(2);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier2MinEarliestPayrollRunDays", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMinPayrolls);
        limitValue.setTier(2);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier2MinPayrolls", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseIncreaseMultiplier);
        limitValue.setTier(3);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier3IncreaseMultiplier", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMaxCompanyLimit);
        limitValue.setTier(3);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier3MaxCompanyLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMaxEmployeeLimit);
        limitValue.setTier(3);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier3MaxEmployeeLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMinEarliestPayrollRunDays);
        limitValue.setTier(3);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier3MinEarliestPayrollRunDays", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.AutoLimitIncreaseMinPayrolls);
        limitValue.setTier(3);
        LIMIT_VALUE_MAP.put("DDAutoLimitIncreaseTier3MinPayrolls", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.CompanyLimitDuration);
        LIMIT_VALUE_MAP.put("DDCompanyLimitDuration", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.EmployeeLimitDuration);
        LIMIT_VALUE_MAP.put("DDEmployeeLimitDuration", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.DefaultCompanyLimit);
        LIMIT_VALUE_MAP.put("DefaultBPCompanyLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.DefaultEmployeeLimit);
        LIMIT_VALUE_MAP.put("DefaultBPPayeeLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.DefaultCompanyLimit);
        LIMIT_VALUE_MAP.put("DefaultDDCompanyLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.DefaultEmployeeLimit);
        LIMIT_VALUE_MAP.put("DefaultDDEmployeeLimit", limitValue);

        limitValue = new LimitValue();
        limitValue.setName(LimitValueType.MaxCompanyLimitDefault);
        LIMIT_VALUE_MAP.put("MaxDDCompanyLimitDefault", limitValue);
    }

    @WebMethod
    public void updateParameters(@WebParam(name = "sourceSystemCD")String pSourceSystemCD,
                                @WebParam(name = "parameters")List<ParameterWSDTO> pParameters,
                                @WebParam(name = "offeringId")String pOfferingId) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCD == null || pSourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pParameters == null || pParameters.isEmpty()) {
            throw new RuntimeException("No parameters specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Offering offering = null;
            if(pOfferingId != null) {
                offering = Application.findById(Offering.class, SpcfUniqueId.createInstance(pOfferingId));
            }

            SourceSystemCode sourceSystemCd = SourceSystemCode.valueOf(pSourceSystemCD);

            List<SourcePayrollParameterDTO> sourcePayrollParameterDTOs = new Vector<SourcePayrollParameterDTO>(pParameters.size());
            Map<FraudValueType, String> fraudValueTypeMap = new HashMap<FraudValueType, String>();
            Map<LimitValueType, String> limitValueTypeMap = new HashMap<LimitValueType, String>();
            Map<LimitValue, String> limitValueMap = new HashMap<LimitValue, String>();

            for(ParameterWSDTO param : pParameters) {
                String code = param.getCode();
                String value = param.getValue();

                try {
                    SourcePayrollParameterCode sourcePayrollParameterCode = SourcePayrollParameterCode.valueOf(code);
                    SourcePayrollParameterDTO paramDTO =
                            new SourcePayrollParameterDTO(sourceSystemCd, null, null, sourcePayrollParameterCode, value);
                    sourcePayrollParameterDTOs.add(paramDTO);
                    continue;
                } catch (IllegalArgumentException e) {
                    // ignore
                }

                try {
                    FraudValueType fraudValueType = FraudValueType.valueOf(code);
                    fraudValueTypeMap.put(fraudValueType, value);
                    continue;
                } catch (IllegalArgumentException e) {
                    // ignore
                }

                try {
                    LimitValueType limitValueType = LimitValueType.valueOf(code);
                    limitValueTypeMap.put(limitValueType, value);
                    continue;
                } catch (IllegalArgumentException e) {
                    // ignore
                }

                if(LIMIT_VALUE_MAP.containsKey(code)) {
                    limitValueMap.put(LIMIT_VALUE_MAP.get(code), value);
                    continue;
                }

                throw new RuntimeException("Parameter: " + code + " not found.");
            }

            if(sourcePayrollParameterDTOs.size() > 0) {
                assertSuccess(PayrollServices.payrollManager.updateSourcePayrollParameter(sourceSystemCd, sourcePayrollParameterDTOs));
            }

            if(!fraudValueTypeMap.isEmpty()) {
                DomainEntitySet<FraudRule> fraudRules;
                if(offering != null) {
                    fraudRules = new DomainEntitySet<FraudRule>();
                    fraudRules.add(offering.getFraudRule());
                } else {
                    fraudRules = Application.find(FraudRule.class, FraudRule.SourceSystemCd().equalTo(sourceSystemCd));
                }
                for (FraudRule fraudRule : fraudRules) {
                    for (FraudValueType fraudValueType : fraudValueTypeMap.keySet()) {
                        FraudValue fraudValue = fraudRule.findFraudValueByName(fraudValueType);
                        fraudValue.setValue(fraudValueTypeMap.get(fraudValueType));
                        Application.save(fraudValue);
                    }
                }
            }

            DomainEntitySet<LimitRule> limitRules;
            if(offering != null) {
                limitRules = new DomainEntitySet<LimitRule>();
                limitRules.add(offering.getLimitRule());
            } else {
                limitRules = Application.find(LimitRule.class, LimitRule.SourceSystemCd().equalTo(sourceSystemCd));
            }
            if(!limitValueTypeMap.isEmpty()) {
                for (LimitRule limitRule : limitRules) {
                    for (LimitValueType limitValueType : limitValueTypeMap.keySet()) {
                        LimitValue limitValue = limitRule.findLimitValueByName(limitValueType);
                        limitValue.setValue(limitValueTypeMap.get(limitValueType));
                        Application.save(limitValue);
                    }
                }
            }

            if(!limitValueMap.isEmpty()) {
                for (LimitRule limitRule : limitRules) {
                    for (LimitValue limitValue : limitValueMap.keySet()) {
                        DomainEntitySet<LimitValue> limitValues = limitRule.getLimitValueCollection().find(LimitValue.Name().equalTo(limitValue.getName()).And(LimitValue.Tier().equalTo(limitValue.getTier())));
                        for (LimitValue value : limitValues) {
                            value.setValue(limitValueMap.get(limitValue));
                            Application.save(value);
                        }
                    }
                }
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

}

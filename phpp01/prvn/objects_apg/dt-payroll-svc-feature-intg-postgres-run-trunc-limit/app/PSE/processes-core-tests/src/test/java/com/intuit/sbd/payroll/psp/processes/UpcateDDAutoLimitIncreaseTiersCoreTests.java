package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DDAutoLimitIncreaseTierDTO;
import com.intuit.sbd.payroll.psp.domain.AutoLimitIncreaseTier;
import com.intuit.sbd.payroll.psp.domain.LimitRule;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class UpcateDDAutoLimitIncreaseTiersCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testGetUpdateGetAutoIncreaseTiers() {
        // fetch, change, update, verify that set of values for system updated changed
        Map<String, DDAutoLimitIncreaseTierDTO[]> originalDtosMap = getLimitTiersDTOMap();
        Map<String, DDAutoLimitIncreaseTierDTO[]> dtosMap = getLimitTiersDTOMap();

        // save the updates
        PayrollServices.beginUnitOfWork();
        for (String ruleId : dtosMap.keySet()) {
            assertSuccess(PayrollServices.payrollManager.updateDDAutoLimitIncreaseTiers(ruleId, dtosMap.get(ruleId)));
        }
        PayrollServices.commitUnitOfWork();

        // verify the updates
        Map<String, List<AutoLimitIncreaseTier>> updatedDtosMap = getLimitTiersMap();

        for (String ruleId : updatedDtosMap.keySet()) {
            DDAutoLimitIncreaseTierDTO[] dtos = dtosMap.get(ruleId);
            List<AutoLimitIncreaseTier> updatedTiers = updatedDtosMap.get(ruleId);
            for (AutoLimitIncreaseTier updatedTier : updatedTiers) {
                int i = Integer.parseInt(updatedTier.getLevel()) - 1;
                assertEquals("level", dtos[i].getLevel(), updatedTier.getLevel());
                assertEquals("payrolls run", Integer.parseInt(dtos[i].getPayrollsRun()), updatedTier.getPayrollsRun());
                assertEquals("days since first payroll", Integer.parseInt(dtos[i].getDaysSinceFirstPayroll()), updatedTier.getDaysSinceFirstPayroll());
                assertEquals("increase multiplier", new SpcfMoney(dtos[i].getIncreaseMultiplier()), updatedTier.getIncreaseMultiplier());
                assertEquals("company cap", new SpcfMoney(dtos[i].getCompanyCap()), updatedTier.getCompanyCap());
                assertEquals("employee cap", new SpcfMoney(dtos[i].getEmployeeCap()), updatedTier.getPayeeCap());
            }
        }

        // Revert the updates
        PayrollServices.beginUnitOfWork();
        for (String ruleId : originalDtosMap.keySet()) {
            assertSuccess(PayrollServices.payrollManager.updateDDAutoLimitIncreaseTiers(ruleId, originalDtosMap.get(ruleId)));
        }
        PayrollServices.commitUnitOfWork();
    }

    private Map<String, DDAutoLimitIncreaseTierDTO[]> getLimitTiersDTOMap() {
        PayrollServices.beginUnitOfWork();
        Map<String, DDAutoLimitIncreaseTierDTO[]> dtosMap = new HashMap<String, DDAutoLimitIncreaseTierDTO[]>();
        DomainEntitySet<LimitRule> limitRules = Application.find(LimitRule.class);
        for (LimitRule limitRule : limitRules) {                    
            List<AutoLimitIncreaseTier> tiers = limitRule.getAutoLimitIncreaseTiers();
            // update each property of each tier
            DDAutoLimitIncreaseTierDTO[] dtos = new DDAutoLimitIncreaseTierDTO[tiers.size()];
            for (AutoLimitIncreaseTier tier : tiers) {
                int i = Integer.parseInt(tier.getLevel()) - 1;
                dtos[i] = new DDAutoLimitIncreaseTierDTO();                
                dtos[i].setLevel(tier.getLevel());
                dtos[i].setPayrollsRun(Integer.toString(tier.getPayrollsRun() + 1));
                dtos[i].setDaysSinceFirstPayroll(Integer.toString(tier.getDaysSinceFirstPayroll() + 1));
                dtos[i].setIncreaseMultiplier(tier.getIncreaseMultiplier().add(SpcfDecimal.createInstance("0.01")).toString());
                dtos[i].setCompanyCap(Long.toString(new SpcfMoney(tier.getCompanyCap().add(SpcfDecimal.createInstance("1"))).getIntegerPart()));
                dtos[i].setEmployeeCap(Long.toString(new SpcfMoney(tier.getPayeeCap().add(SpcfDecimal.createInstance("1"))).getIntegerPart()));
            }
            if(dtos.length > 0) {
                dtosMap.put(limitRule.getId().toString(), dtos);
            }
        }
        PayrollServices.rollbackUnitOfWork();
        return dtosMap;
    }

    private Map<String, List<AutoLimitIncreaseTier>> getLimitTiersMap() {
        PayrollServices.beginUnitOfWork();
        Map<String, List<AutoLimitIncreaseTier>> tierMap = new HashMap<String, List<AutoLimitIncreaseTier>>();
        DomainEntitySet<LimitRule> limitRules = Application.find(LimitRule.class);
        for (LimitRule limitRule : limitRules) {            
            tierMap.put(limitRule.getId().toString(), limitRule.getAutoLimitIncreaseTiers());
        }
        PayrollServices.rollbackUnitOfWork();
        return tierMap;
    }
}

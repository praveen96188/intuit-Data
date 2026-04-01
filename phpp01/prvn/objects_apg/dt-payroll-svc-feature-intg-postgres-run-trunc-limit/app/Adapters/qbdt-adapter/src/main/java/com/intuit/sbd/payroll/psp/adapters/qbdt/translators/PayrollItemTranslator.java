package com.intuit.sbd.payroll.psp.adapters.qbdt.translators;

import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Rate;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemLawAssoc;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 15, 2010
 * Time: 8:25:13 AM
 */
public class PayrollItemTranslator {
    public static void populateCompanyLawDTO(PayrollItem pPayrollItem, CompanyLawDTO pCompanyLawDTO) {
        pCompanyLawDTO.setDTOCreatedBySystem(false);
        pCompanyLawDTO.setLawId(SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, pPayrollItem.getSourceLawId()).getLawId());
        pCompanyLawDTO.setSourceDescription(pPayrollItem.getSourceDescription());
        pCompanyLawDTO.setSourceId(pPayrollItem.getSourceId());
        pCompanyLawDTO.setStatus(pPayrollItem.getPayrollItemStatus());
        pCompanyLawDTO.setTaxFormLine(pPayrollItem.getTaxFormLine());
        pCompanyLawDTO.setW2Code(pPayrollItem.getW2Code());
        // remove and re-add all rates
        pCompanyLawDTO.setRateDTOs(createRateDTOs(pPayrollItem.getFutureRate(), pPayrollItem.getRateChanges()));

        if(pCompanyLawDTO.getQBDTPayrollItemInfoDTO() == null) {
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            pCompanyLawDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        }
        populateQBDTPayrollItemsInfoDTO(pPayrollItem, pCompanyLawDTO.getQBDTPayrollItemInfoDTO());
    }

    public static void populatePayrollItemDTO(PayrollItem pPayrollItem, CompanyPayrollItemDTO pCompanyPayrollItemDTO) {
        pCompanyPayrollItemDTO.setPayrollItemCode(pPayrollItem.getPayrollItemCode());
        pCompanyPayrollItemDTO.setPayrollItemStatus(pPayrollItem.getPayrollItemStatus());        
        pCompanyPayrollItemDTO.setTaxFormLine(pPayrollItem.getTaxFormLine());
        pCompanyPayrollItemDTO.setW2Code(pPayrollItem.getW2Code());
        pCompanyPayrollItemDTO.setSourcePayrollItemDescription(pPayrollItem.getSourceDescription());
        pCompanyPayrollItemDTO.setSourcePayrollItemId(pPayrollItem.getSourceId());

        // remove and re-add all taxable laws
        pCompanyPayrollItemDTO.setTaxableToCompanyLawIds(new ArrayList<String>());
        pCompanyPayrollItemDTO.getTaxableToCompanyLawIds().addAll(pPayrollItem.getTaxableToPayrollItemIds());

        if(pCompanyPayrollItemDTO.getQBDTPayrollItemInfoDTO() == null) {
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            pCompanyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        }
        populateQBDTPayrollItemsInfoDTO(pPayrollItem, pCompanyPayrollItemDTO.getQBDTPayrollItemInfoDTO());
    }

    public static void populateQBDTPayrollItemsInfoDTO(PayrollItem pPayrollItem, QBDTPayrollItemInfoDTO pQBDTPayrollItemInfoDTO) {
        pQBDTPayrollItemInfoDTO.setAdjustsGross(pPayrollItem.getAdjustsGross());
        pQBDTPayrollItemInfoDTO.setAgencyId(pPayrollItem.getAgencyId());
        pQBDTPayrollItemInfoDTO.setBasedOnQuantity(pPayrollItem.getBasedOnQuantity());
        pQBDTPayrollItemInfoDTO.setDefaultLimit(pPayrollItem.getDefaultLimit());
        pQBDTPayrollItemInfoDTO.setDefaultRate(pPayrollItem.getDefaultRate());
        pQBDTPayrollItemInfoDTO.setDefaultRateType(pPayrollItem.getDefaultRateType());
        pQBDTPayrollItemInfoDTO.setExpenseAccount(pPayrollItem.getExpenseAccount());
        pQBDTPayrollItemInfoDTO.setExpenseByJob(pPayrollItem.getExpenseByJob());
        pQBDTPayrollItemInfoDTO.setIsEmployeePaid(pPayrollItem.getIsEmployeePaid());
        pQBDTPayrollItemInfoDTO.setLiabilityAccount(pPayrollItem.getLiabilityAccount());
        pQBDTPayrollItemInfoDTO.setLiabilityAgency(pPayrollItem.getLiabilityAgency());
        pQBDTPayrollItemInfoDTO.setOnService(pPayrollItem.getOnService());
        pQBDTPayrollItemInfoDTO.setPayType(pPayrollItem.getPayType());
        pQBDTPayrollItemInfoDTO.setSpecialType(pPayrollItem.getSpecialType());
        pQBDTPayrollItemInfoDTO.setIsEarningsTable(pPayrollItem.getEarningsTable());
        pQBDTPayrollItemInfoDTO.setListId(pPayrollItem.getListId());
        pQBDTPayrollItemInfoDTO.setOvertimeMultiplier(pPayrollItem.getOvertimeMultiplier());
        pQBDTPayrollItemInfoDTO.setDetailType(pPayrollItem.getDetailType());
    }

    public static List<CompanyLawRateDTO> createRateDTOs(Rate futureRate, Map<Date, Rate> pRatesMap) {
        List<CompanyLawRateDTO> companyLawRateDTOs = new ArrayList<CompanyLawRateDTO>();
        Object[] keySet = pRatesMap.keySet().toArray();
        Arrays.sort(keySet);

        // the first effective date is always null
        Date lastDate = null;
        for (Object endDate : keySet) {
            Rate rate = pRatesMap.get(endDate);
            if (rate != null) {
                CompanyLawRateDTO futureRateDTO = new CompanyLawRateDTO();
                if (lastDate != null) {
                    futureRateDTO.setEffectiveDate(new DateDTO(lastDate));
                }
                futureRateDTO.setRate(rate.getRate());
                futureRateDTO.setRateType(rate.getRateType());
                companyLawRateDTOs.add(futureRateDTO);
                lastDate = (Date) endDate;
            }
        }

        if(futureRate != null) {
            CompanyLawRateDTO futureRateDTO = new CompanyLawRateDTO();
            if(lastDate != null) {
                futureRateDTO.setEffectiveDate(new DateDTO(lastDate));
            }
            futureRateDTO.setRate(futureRate.getRate());
            futureRateDTO.setRateType(futureRate.getRateType());
            companyLawRateDTOs.add(futureRateDTO);
        }

        return companyLawRateDTOs;
    }
}

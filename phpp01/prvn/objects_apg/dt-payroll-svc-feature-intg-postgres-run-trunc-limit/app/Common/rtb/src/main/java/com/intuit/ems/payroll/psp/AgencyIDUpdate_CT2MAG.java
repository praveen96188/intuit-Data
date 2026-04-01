package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Varun Kaura on 1-Jul-2022.
 * Utility to update the agencyIds for all companies for CT-2MAG-PAYMENT from the current format
 * of XX-XXX-XX to XX-XXXXX-0-00
 */
public class AgencyIDUpdate_CT2MAG {
    protected static final SpcfLogger logger = Application.getLogger(AgencyIDUpdate_CT2MAG.class);
    private static final Pattern AGENCY_ID_PATTERN_OLD = Pattern.compile("\\d{2}-\\d{3}-\\d{2}");
    private static final Pattern AGENCY_ID_PATTERN_NEW = Pattern.compile("\\d{2}-\\d{5}-0-00");
    private static final String STRING_TO_PAD = "-0-00";

    public static void main(String[] args) {
        System.out.println("Started processing AgencyID Update for CTDOL - CT-2MAG-PAYMENT");
        AgencyIDUpdate_CT2MAG agencyIDUpdateUtilityCT2MAG = new AgencyIDUpdate_CT2MAG();
        agencyIDUpdateUtilityCT2MAG.updateAgencyIds();
    }

    public void updateAgencyIds() {
        try {
            Application.beginUnitOfWork();

            ArrayList<String> paymentTemplateList = new ArrayList<>();
            paymentTemplateList.add("CT-2MAG-PAYMENT");
            String currentAgencyId = "";
            int updateCAPTRecordCount = 0;
            int updateMMTRecordCount = 0;

            for (String paymentTemplateCode : paymentTemplateList) {

                Map<SpcfUniqueId, String> qbdtPayrollItemInfoMap = getQbdtPayrollItemInfoMap(paymentTemplateCode);
                List<SpcfUniqueId> companyFkList = new ArrayList<>(qbdtPayrollItemInfoMap.keySet());

                DomainEntitySet<MoneyMovementTransaction> mmtResult = getMoneyMovementTransactionAllList(companyFkList, paymentTemplateCode);
                DomainEntitySet<CompanyAgencyPaymentTemplate> captResult = getCompanyAgencyPaymentTemplateAllList(companyFkList, paymentTemplateCode);

                if (CollectionUtils.isNotEmpty(captResult)) {
                    for (CompanyAgencyPaymentTemplate captRecord : captResult) {
                        currentAgencyId = qbdtPayrollItemInfoMap.get(captRecord.getCompanyAgency().getCompany().getId());

                        logger.info("Current CAPT Agency TaxPayerID >> " + captRecord.getAgencyTaxpayerId() + " Current Agency Id >> " + currentAgencyId);

                        Matcher matcherOld = AGENCY_ID_PATTERN_OLD.matcher(currentAgencyId);
                        boolean matchOld = matchFormat(matcherOld);
                        if (matchOld) {
                            String newAgencyId = frameAgencyId(currentAgencyId);

                            Matcher matcherNew = AGENCY_ID_PATTERN_NEW.matcher(newAgencyId);
                            boolean matchNew = matchFormat(matcherNew);
                            if (matchNew) {
                                captRecord.setAgencyTaxpayerId(newAgencyId);
                                Application.save(captRecord);
                                updateCAPTRecordCount++;
                                logger.info("FOR CAPT update Company FK >> " + captRecord.getCompanyAgency().getCompany().getId() +
                                        " PSID >> " + captRecord.getCompanyAgency().getCompany().getSourceCompanyId() + " Latest Agency ID >> " + newAgencyId);
                            }
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(mmtResult)) {
                    for (MoneyMovementTransaction mmtRecord : mmtResult) {
                        currentAgencyId = qbdtPayrollItemInfoMap.get(mmtRecord.getCompany().getId());

                        logger.info("Current MMT Agency TaxPayerID >> " + mmtRecord.getAgencyTaxpayerId() + " Current Agency Id >> " + currentAgencyId);

                        Matcher matcherOld = AGENCY_ID_PATTERN_OLD.matcher(currentAgencyId);
                        boolean matchOld = matchFormat(matcherOld);

                        if (matchOld) {
                            String agencyId = frameAgencyId(currentAgencyId);
                            Matcher matcherNew = AGENCY_ID_PATTERN_NEW.matcher(agencyId);
                            boolean matchNew = matchFormat(matcherNew);
                            if (matchNew) {
                                mmtRecord.setAgencyTaxpayerId(agencyId);
                                Application.save(mmtRecord);
                                updateMMTRecordCount++;
                                logger.info("FOR MMT update Company FK >> " + mmtRecord.getCompany().getId() +
                                        " PSID >> " + mmtRecord.getCompany().getSourceCompanyId() + " Latest Agency ID >> "+ agencyId);
                            }
                        }
                    }
                }
                Application.commitUnitOfWork();
                logger.info("Agency Ids Updated Successfully");
                logger.info("NO of CAPT records updated >> " + updateCAPTRecordCount);
                logger.info("NO of MMT records updated >> " + updateMMTRecordCount);
            }
        } catch (Exception e) {
            logger.error("Failed to  Update Agency IDs " + e);
            Application.rollbackUnitOfWork();
        }
    }

    public boolean matchFormat(Matcher matcher) {
        return matcher.matches();
    }

    public String frameAgencyId(String latestAgencyId) {
        String noHyphen = StringUtils.replace(latestAgencyId, "-", "");
        String firstPart = StringUtils.substring(noHyphen, 0, 2);
        String secondPart = StringUtils.substring(noHyphen, 2, 7);
        String finalString = firstPart + "-" + secondPart;

        String agencyId = StringUtils.rightPad(finalString, finalString.length() + STRING_TO_PAD.length(), STRING_TO_PAD);
        return agencyId;
    }

    public Map<SpcfUniqueId, String> getQbdtPayrollItemInfoMap(String paymentTemplateCode) {
        DomainEntitySet<QbdtPayrollItemInfo> qbdtPayrollItemInfoList = new DomainEntitySet<>();
        Map<SpcfUniqueId, String> sourceCompanyIdMap = new HashMap<>();

        try {
            // Retrieve Agency Ids for only Active Assisted companies
            Criterion<QbdtPayrollItemInfo> qbdtPayrollItemInfoCriteria = QbdtPayrollItemInfo.AgencyIdEnc().isNotNull()
                    .And(QbdtPayrollItemInfo.CompanyLaw().Law().LawId().equalTo("89"))
                    .And(QbdtPayrollItemInfo.CompanyLaw().CompanyAgency().Agency().AgencyId().equalTo("CTDOL"))
                    .And(QbdtPayrollItemInfo.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)))
                    .And(QbdtPayrollItemInfo.Company().CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));

            qbdtPayrollItemInfoList = Application.find(QbdtPayrollItemInfo.class, qbdtPayrollItemInfoCriteria);
            logger.info("QBDT PITEM INFO Number of Records Existing for " + paymentTemplateCode + ">> " + qbdtPayrollItemInfoList.size());

            for (QbdtPayrollItemInfo qbdtPayrollItemInfoRecord : qbdtPayrollItemInfoList) {
                SpcfUniqueId companyFk = qbdtPayrollItemInfoRecord.getCompany().getId();
                String latestAgencyId = qbdtPayrollItemInfoRecord.getAgencyId();
                logger.info("Company FK >> " + companyFk + "PSID >> " + qbdtPayrollItemInfoRecord.getCompany().getSourceCompanyId());

                sourceCompanyIdMap.put(companyFk, latestAgencyId);
            }
        } catch (Exception e) {
            logger.error("Failed to  Retrieve QBDT PITEM INFO Records " + e);
            throw e;
        }
        return sourceCompanyIdMap;
    }

    public DomainEntitySet<CompanyAgencyPaymentTemplate> getCompanyAgencyPaymentTemplateAllList(List<SpcfUniqueId> companyFkList, String paymentTemplateCode) {
        DomainEntitySet<CompanyAgencyPaymentTemplate> captList = new DomainEntitySet<>();
        try {
            Criterion<CompanyAgencyPaymentTemplate> captCriteria =
                    CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().isNotNull()
                            .And(CompanyAgencyPaymentTemplate.CompanyAgency().Company().Id().in(companyFkList))
                            .And(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCode));
            captList = Application.find(CompanyAgencyPaymentTemplate.class, captCriteria);
            logger.info("CAPT Number of Records Existing for " + paymentTemplateCode + ">> " + captList.size());
        } catch (Exception e) {
            logger.error("Failed to  Retrieve CAPT Records " + e);
            throw e;
        }
        return captList;
    }

    public DomainEntitySet<MoneyMovementTransaction> getMoneyMovementTransactionAllList(List<SpcfUniqueId> companyFkList, String paymentTemplateCode) {
        DomainEntitySet<MoneyMovementTransaction> mmtList = new DomainEntitySet<>();
        ArrayList<TaxPaymentStatus> taxPaymentStatuses = new ArrayList<>();
        taxPaymentStatuses.add(TaxPaymentStatus.OnHold);
        taxPaymentStatuses.add(TaxPaymentStatus.ReadyToSend);
        try {
            Criterion<MoneyMovementTransaction> mmtCriteria =
                    MoneyMovementTransaction.AgencyTaxpayerIdEnc().isNotNull()
                            .And(MoneyMovementTransaction.PaymentTemplate().in(PaymentTemplate.findPaymentTemplate(paymentTemplateCode)))
                            .And(MoneyMovementTransaction.TaxPaymentStatus().in(taxPaymentStatuses))
                            .And(MoneyMovementTransaction.Company().Id().in(companyFkList));
            mmtList = Application.find(MoneyMovementTransaction.class, mmtCriteria);
            logger.info("MMT Number of Records Existing for " + paymentTemplateCode + ">>" + mmtList.size());
        } catch (Exception e) {
            logger.error("Failed to  Retrieve MMT Records " + e);
            throw e;
        }
        return mmtList;
    }
}
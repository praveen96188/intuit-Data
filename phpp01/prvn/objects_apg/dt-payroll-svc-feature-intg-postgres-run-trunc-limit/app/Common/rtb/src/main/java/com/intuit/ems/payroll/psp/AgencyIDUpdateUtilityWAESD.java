package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ServiceStatusCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


/**
 * Created by Aisshwarya Patil on 17-Jun-2019.
 * Since Blank value was getting updated for Agency Id in CAPT and MMT table for WA-PFML-PAYMENT
 * This utility is created to read Agency Id from QBDT PITEM INFO and update in CAPT and MMT
 */
public class AgencyIDUpdateUtilityWAESD {

    protected static final SpcfLogger logger = Application.getLogger(AgencyIDUpdateUtilityWAESD.class);

    public static void main(String[] args) {

        System.out.println("Started processing");
        AgencyIDUpdateUtilityWAESD agencyIDUpdateUtilityWAESD = new AgencyIDUpdateUtilityWAESD();
        agencyIDUpdateUtilityWAESD.updateAgencyIds();
    }

    public void updateAgencyIds() {

        try{

            Application.beginUnitOfWork();

            ArrayList<String> paymentTemplateList = new ArrayList<String>();
            paymentTemplateList.add("WA-PFML-PAYMENT");
            String latestAgencyId = "";
            int updateCAPTRecordCount = 0;
            int updateMMTRecordCount = 0;

            for (String paymentTemplateCode : paymentTemplateList) {

                Map<SpcfUniqueId,String> qbdtPayrollItemInfoMap = getQbdtPayrollItemInfoMap(paymentTemplateCode);
                List<SpcfUniqueId> companyFkList = new ArrayList<SpcfUniqueId>(qbdtPayrollItemInfoMap.keySet());

                DomainEntitySet<MoneyMovementTransaction> mmtResult = getMoneyMovementTransactionAllList(companyFkList,paymentTemplateCode);
                DomainEntitySet<CompanyAgencyPaymentTemplate> captResult = getCompanyAgencyPaymentTemplateAllList(companyFkList,paymentTemplateCode);

                    // Update CompanyAgencyPaymentTemplate Agency Id if it is blank
                    if (captResult != null && captResult.size() > 0 ){
                        for (CompanyAgencyPaymentTemplate captRecord : captResult) {
                            latestAgencyId = qbdtPayrollItemInfoMap.get(captRecord.getCompanyAgency().getCompany().getId());

                            logger.info("Existing CAPT Agency TaxPayerID >>" + captRecord.getAgencyTaxpayerId() + "Latest Agency Id >>"+latestAgencyId);
                            logger.info("FOR CAPT update Company FK >>" + captRecord.getCompanyAgency().getCompany().getId() + "PSID >> " + captRecord.getCompanyAgency().getCompany().getSourceCompanyId());
                            captRecord.setAgencyTaxpayerId(latestAgencyId);
                            Application.save(captRecord);
                            updateCAPTRecordCount++;
                        }
                    }

                    if (mmtResult != null && mmtResult.size() > 0){
                        for (MoneyMovementTransaction mmtRecord : mmtResult) {
                            latestAgencyId = qbdtPayrollItemInfoMap.get(mmtRecord.getCompany().getId());
                            logger.info("Existing MMT Agency TaxPayerID >>" + mmtRecord.getAgencyTaxpayerId() + "Latest Agency Id >>"+latestAgencyId);
                            logger.info("FOR MMT update Company FK >>" + mmtRecord.getCompany().getId() + "PSID >> " + mmtRecord.getCompany().getSourceCompanyId());
                            mmtRecord.setAgencyTaxpayerId(latestAgencyId);
                            Application.save(mmtRecord);
                            updateMMTRecordCount++;

                        }
                    }
                Application.commitUnitOfWork();
                logger.info("Agency Ids Updated Successfully");
                logger.info("NO of CAPT records updated >> " +updateCAPTRecordCount);
                logger.info("NO of MMT records updated >> " +updateMMTRecordCount);
            }
        }catch (Exception e) {
            logger.error("Failed to  Update Agency IDs " +  e);
            Application.rollbackUnitOfWork();
        }

    }



    public  Map<SpcfUniqueId,String> getQbdtPayrollItemInfoMap(String paymentTemplateCode) {
        DomainEntitySet<QbdtPayrollItemInfo> qbdtPayrollItemInfoList = new DomainEntitySet<QbdtPayrollItemInfo>();
        Map<SpcfUniqueId,String> sourceCompanyIdMap = new HashMap<SpcfUniqueId,String>();

        try {
            // Retrieve Agency Ids for only Active Assisted companies
            Criterion<QbdtPayrollItemInfo> qbdtPayrollItemInfoCriteria = QbdtPayrollItemInfo.AgencyIdEnc().isNotNull()
                    .And(QbdtPayrollItemInfo.CompanyLaw().Law().LawId().equalTo("206"))
                    .And (QbdtPayrollItemInfo.CompanyLaw().CompanyAgency().Agency().AgencyId().equalTo("WAESD"))
                    .And (QbdtPayrollItemInfo.Company().CompanyServiceSet().Exists(CompanyService.Service().ServiceCd().equalTo(ServiceCode.Tax)))
                    .And (QbdtPayrollItemInfo.Company().CompanyServiceSet().Exists(CompanyService.StatusCd().equalTo(ServiceSubStatusCode.ActiveCurrent)));

            qbdtPayrollItemInfoList = Application.find(QbdtPayrollItemInfo.class, qbdtPayrollItemInfoCriteria);
            logger.info("QBDT PITEM INFO Number of Records Existing for "+ paymentTemplateCode + ">>" + qbdtPayrollItemInfoList.size());

            for (QbdtPayrollItemInfo qbdtPayrollItemInfoRecord : qbdtPayrollItemInfoList){
                SpcfUniqueId companyFk = qbdtPayrollItemInfoRecord.getCompany().getId();
                String latestAgencyId = qbdtPayrollItemInfoRecord.getAgencyId();
                logger.info("Company FK >>" + companyFk + "PSID >> " + qbdtPayrollItemInfoRecord.getCompany().getSourceCompanyId());

                sourceCompanyIdMap.put(companyFk,latestAgencyId);
            }

        }catch (Exception e) {
            logger.error("Failed to  Retrieve QBDT PITEM INFO Records " +  e);
            throw e;
        }

        return sourceCompanyIdMap;
    }

    public  DomainEntitySet<CompanyAgencyPaymentTemplate> getCompanyAgencyPaymentTemplateAllList(List<SpcfUniqueId> companyFkList,String paymentTemplateCode) {

        DomainEntitySet<CompanyAgencyPaymentTemplate> captList =  new DomainEntitySet<CompanyAgencyPaymentTemplate>();
        try {
            Criterion<CompanyAgencyPaymentTemplate> captCriteria =
                    CompanyAgencyPaymentTemplate.AgencyTaxpayerIdEnc().isNull()
                            .And(CompanyAgencyPaymentTemplate.CompanyAgency().Company().Id().in(companyFkList))
                            .And(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCode));

            captList = Application.find(CompanyAgencyPaymentTemplate.class, captCriteria);
            logger.info("CAPT Number of Records Existing for "+ paymentTemplateCode + ">>" + captList.size());
        }catch (Exception e) {
            logger.error("Failed to  Retrieve CAPT Records " +  e);
            throw e;

        }
        return captList;

    }

    public DomainEntitySet<MoneyMovementTransaction> getMoneyMovementTransactionAllList(List<SpcfUniqueId> companyFkList, String paymentTemplateCode)
    {

        DomainEntitySet<MoneyMovementTransaction> mmtList = new DomainEntitySet<MoneyMovementTransaction>();
        ArrayList taxPaymentStatuses = new ArrayList();
        taxPaymentStatuses.add(TaxPaymentStatus.OnHold);
        taxPaymentStatuses.add(TaxPaymentStatus.ReadyToSend);

        try {
            Criterion<MoneyMovementTransaction> mmtCriteria =
                    MoneyMovementTransaction.AgencyTaxpayerIdEnc().isNull()
                            .And(MoneyMovementTransaction.PaymentTemplate().in(PaymentTemplate.findPaymentTemplate(paymentTemplateCode)))
                            .And(MoneyMovementTransaction.TaxPaymentStatus().in(taxPaymentStatuses))
                            .And(MoneyMovementTransaction.Company().Id().in(companyFkList));

            mmtList = Application.find(MoneyMovementTransaction.class, mmtCriteria);
            logger.info("MMT Number of Records Existing for "+ paymentTemplateCode + ">>" + mmtList.size());

        }catch (Exception e) {
            logger.error("Failed to  Retrieve MMT Records " +  e);
            throw e;

        }
        return mmtList;
    }





}
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingDetail;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingInvoice;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUsageBillingInvoiceDetail;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.aia.AIAGateway;
import com.intuit.sbd.payroll.psp.gateways.aia.BillInfo;
import com.intuit.sbd.payroll.psp.gateways.aia.ItemCharge;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Usage Billing Adapter
 */
public class BillingHistoryAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(BillingHistoryAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    /**
     * Get Billing History from BRM
     *
     * @param companyId      companyId
     * @param sourceSystemCd source system code
     * @return List of SAPUsageBillingInvoice
     * @throws Throwable
     */
    @FlexMethod
    public ArrayList<SAPUsageBillingInvoice> findBillingHistoryByDate(
            String companyId,
            String sourceSystemCd) throws Throwable {
        ArrayList<SAPUsageBillingInvoice> billingData = null;
        try {
            PayrollServices.beginUnitOfWork();
            AIAGateway aiaGateway = new AIAGateway();
            Company company = Company.findCompany(
                    companyId,
                    SourceSystemCode.QBDT);

            if (company == null) return null;

            //Active primary entitlement
            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            Entitlement entitlement = null;
            if (eu != null) {
                entitlement = eu.getEntitlement();
            }
            if (entitlement == null) return null;

            String customerId = entitlement.getCustomerId();
            String billingProfileId = entitlement.getBillingProfileId();

            // PSP-14072? Fetch BP using CAN from AIA
            if (billingProfileId == null) {
                logger.info("Unable to fetch billingProfileId from entitlement. Query from AIA Gateway");
                billingProfileId = aiaGateway.queryBillingProfile(customerId);

                if (billingProfileId == null) {
                    aeFactory.throwGenericException("Error finding billing history.", sourceSystemCd,
                            companyId, new NullPointerException("No BillingProfile found for given CAN"));
                } else {
                    EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);
                    entitlementDTO.setBillingProfileId(billingProfileId);
                    ProcessResult<Entitlement> entitlementPR = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);

                    logger.info("Saved BillingProfile in DB --> " + entitlementPR.getResult().getBillingProfileId());

                }
            }

            List<BillInfo> billInfoList = aiaGateway.queryInvoiceList(customerId, billingProfileId);
            if (billInfoList != null) {
                billingData = new ArrayList<SAPUsageBillingInvoice>();
                for (BillInfo billInfo : billInfoList) {
                    if (billInfo != null) {
                        billingData.add(BillingHistoryTranslator.getSAPUsageBillingInvoiceFromAIABillInfo(billInfo));
                    }
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding billing history.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return billingData;
    }

    /**
     * Get Invoice Details from BRM
     *
     * @param companyId      CompanyId
     * @param sourceSystemCd Source System Cd
     * @param billPOID       BRM external id (BilPOID)
     * @return SAPUsageBillingInvoiceDetail object
     * @throws Throwable
     */
    @FlexMethod
    public SAPUsageBillingInvoiceDetail findInvoiceDetails(
            String companyId,
            String sourceSystemCd,
            String subscriptionNumber,
            String billPOID) throws Throwable {
        SAPUsageBillingInvoiceDetail invoiceDetail = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            AIAGateway aiaGateway = new AIAGateway();
            Company company = Company.findCompany(
                    companyId,
                    SourceSystemCode.QBDT);

            if (company == null) return null;

            Entitlement entitlement = null;
            if(subscriptionNumber != null && !subscriptionNumber.equals("") ) {
                entitlement = Entitlement.findEntitlementBySubscriptionNumber(subscriptionNumber);
            } else {
                EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
                if (eu != null) {
                    entitlement = eu.getEntitlement();
                }
            }
            if (entitlement == null) return null;

            String customerId = entitlement.getCustomerId();
            String billingProfileId = entitlement.getBillingProfileId();
            List<ItemCharge> itemCharges = aiaGateway.queryInvoiceDetails(customerId, billingProfileId, billPOID);
            invoiceDetail = new SAPUsageBillingInvoiceDetail();
            if (itemCharges != null) {
                Double employeeFees = 0d;
                int usages = 0;
                for (ItemCharge itemCharge : itemCharges) {
                    if (itemCharge != null) {
                        if (itemCharge.isAnnualSubscriptionItem() || itemCharge.isMonthlySubscriptionItem()) {
                            invoiceDetail.setSubscriptionFee(Double.valueOf(itemCharge.getItemChargeAmount()));
                            invoiceDetail.setIsPayrollItem(true);
                        } else if (itemCharge.isPayrollEEItem()) {
                            employeeFees += (Double.valueOf(itemCharge.getItemChargeAmount()));
                            usages += aiaGateway.queryEventDetails(customerId, billingProfileId, billPOID, itemCharge.getItemChargeId());
                            invoiceDetail.setIsPayrollItem(true);
                        } else {
                            invoiceDetail.setIsPayrollItem(false);
                        }
                    }
                }
                invoiceDetail.setEmployeeFee(employeeFees);
                invoiceDetail.setNumEmployessPaidPrevMonth(usages);
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding billing history.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return invoiceDetail;
    }

    /**
     * Get Billing Details (employee/paycheck details)
     *
     * @param companyId      CompanyID
     * @param sourceSystemCd Source System Cd
     * @param billDate       Bill Date
     * @param viewAll        Used for multi-EIN scenario
     * @return SAPUsageBillingDetail object
     * @throws Throwable
     */
    @FlexMethod
    public SAPUsageBillingDetail findBillingDetails(
            String companyId,
            String sourceSystemCd,
            Date billDate,
            Boolean viewAll) throws Throwable {

        SAPUsageBillingDetail billingDetail = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);
            if (company == null || billDate == null) return null;

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            if (eu == null || eu.getEntitlement() == null) return null;

            Entitlement entitlement = eu.getEntitlement();
            CompanyUsage companyUsage = CompanyUsage.findCompanyUsage(companyId, SourceSystemCode.QBDT, entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
            if (companyUsage == null) return null;

            DomainEntitySet<EntitlementUnit> euList = entitlement.getActiveEntitlementUnitCollection();
            billingDetail = new SAPUsageBillingDetail();
            boolean isMultiEin = (euList != null && euList.size() > 1);
            billingDetail.setIsMultiEin(isMultiEin);

            SpcfCalendar spcfBillDate = SAPTranslator.getSpcfCalendarFromDate(billDate);
            List<Object[]> billDetails;
            if (viewAll) {
                // Current company may not have any bills, but sibling EINs may. So skipping the part to retrieve bill
                billDetails = Bill.findBillDetailsByLicenceNumber(spcfBillDate, entitlement.getLicenseNumber());
            } else {
                Bill bill = Bill.findBill(companyUsage, spcfBillDate);
                if (bill == null) return billingDetail;
                billDetails = Bill.findBillDetails(bill.getId());
            }
            if (billDetails != null && billDetails.size() != 0) {
                billingDetail = BillingHistoryTranslator.getSAPUsageBillingDetailFromDomainObject(spcfBillDate, billDetails, entitlement);
            }

        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding Billing Details.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return billingDetail;
    }

    @FlexMethod
    public ArrayList<String> findSymphonySubscriptionNumbers(String companyId, String sourceSystemCd) throws Throwable{
      return Entitlement.findSymphonySubscriptionNumberByCompany(companyId, sourceSystemCd);
    }


    @FlexMethod
    public ArrayList<SAPUsageBillingInvoice> findBillingHistoryBySubscriptionAndDate(
            String companyId, String sourceSystemCd, String subscriptionNumber, Date fromDate, Date toDate) throws Throwable {

        ArrayList<SAPUsageBillingInvoice> billingData = null;
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            AIAGateway aiaGateway = new AIAGateway();
            Company company = Company.findCompany(
                    companyId,
                    SourceSystemCode.QBDT);

            if (company == null) return null;

            Entitlement entitlement = null;
            if(subscriptionNumber != null && !subscriptionNumber.equals("") ) {
                entitlement = Entitlement.findEntitlementBySubscriptionNumber(subscriptionNumber);
            } else {
                EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
                if (eu != null) {
                    entitlement = eu.getEntitlement();
                }
            }

            if (entitlement == null) return null;

            String customerId = entitlement.getCustomerId();
            String billingProfileId = entitlement.getBillingProfileId();

            List<BillInfo> billInfoList = aiaGateway.queryInvoiceList(customerId, billingProfileId, fromDate, toDate);

            if (billInfoList != null) {
                billingData = new ArrayList<SAPUsageBillingInvoice>();
                for (BillInfo billInfo : billInfoList) {
                    if (billInfo != null) {
                        billingData.add(BillingHistoryTranslator.getSAPUsageBillingInvoiceFromAIABillInfo(billInfo));
                    }
                }
            }
        } catch (Throwable t) {
            aeFactory.throwGenericException("Error finding billing history.", sourceSystemCd, companyId, t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return billingData;
    }
}

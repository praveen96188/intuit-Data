package com.intuit.sbd.payroll.psp.adapters.brm.webservices;


import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.brm.dto.GetUsageBillingDetailRequest;
import com.intuit.sbd.payroll.psp.adapters.brm.messages.BRMMessage;
import com.intuit.sbd.payroll.psp.adapters.brm.dto.GetUsageBillingDetailResponse;
import com.intuit.sbd.payroll.psp.adapters.brm.dto.ResponseStatus;
import com.intuit.sbd.payroll.psp.adapters.brm.dto.UsageBillingEmployeeDetail;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: vkrishnamoorthy
 * Date: Jun 14, 2010
 * Time: 11:33:23 AM
 */
@WebService()
public class BRMWebServices {
    private static final SpcfLogger logger = PayrollServices.getLogger(BRMWebServices.class);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");


    @WebMethod
    @WebResult(name = "GetUsageBillingDetailResponse")
    public GetUsageBillingDetailResponse getUsageBillingEmployeeDetails(@WebParam(name = "GetUsageBillingDetailRequest") GetUsageBillingDetailRequest pRequest) {
        GetUsageBillingDetailResponse response = new GetUsageBillingDetailResponse();
        try {
            PayrollServices.beginUnitOfWork();

            if (pRequest.getEin() == null || pRequest.getBillDate() == null) {
                response.setStatus(createErrorStatus(BRMMessage.missingInputs(pRequest.getEin())));
                return response;
            }

            Company company=null;

            if (pRequest.getCompanyId()!=null)
            {
                company = Company.findActiveCompanyWithPSID(SourceSystemCode.QBDT, pRequest.getEin(), pRequest.getCompanyId());
            }
            else
            {
                company = Company.findActiveCompany(SourceSystemCode.QBDT, pRequest.getEin());
            }
            if (company == null) {
                response.setStatus(createErrorStatus(BRMMessage.companyNotFound(pRequest.getEin())));
                return response;
            }

            EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
            if (eu == null || eu.getEntitlement() == null) {
                response.setStatus(createErrorStatus(BRMMessage.entitlementNotFound(pRequest.getEin())));
                return response;
            }

            Entitlement entitlement = eu.getEntitlement();
            DomainEntitySet<EntitlementUnit> euList = entitlement.getActiveEntitlementUnitCollection();
            boolean isMultiEin = (euList != null && euList.size() > 1);

            CompanyUsage companyUsage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), SourceSystemCode.QBDT, entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
            if (companyUsage == null) {
                response.setStatus(createErrorStatus(BRMMessage.companyUsageNotFound(pRequest.getEin())));
                return response;
            }
            if (eu == null || eu.getEntitlement() == null) {
                response.setStatus(createErrorStatus(BRMMessage.entitlementNotFound(pRequest.getEin())));
                return response;
            }
            Date billDate = SIMPLE_DATE_FORMAT.parse(pRequest.getBillDate());
            SpcfCalendar spcfBillDate = CalendarUtils.convertToSpcfCalendar(billDate);

            List<Object[]> billDetails = null;
            if (pRequest.getViewAll()) {
                // Current company may not have any bills, but sibling EINs may. So skipping the part to retrieve bill
                billDetails = Bill.findBillDetailsByLicenceNumber(spcfBillDate, entitlement.getLicenseNumber());
            } else {
                Bill bill = Bill.findBill(companyUsage, spcfBillDate);
                if (bill == null) {
                    response.setStatus(createErrorStatus(BRMMessage.billNotFound(pRequest.getEin())));
                    return response;
                }
                billDetails = Bill.findBillDetails(bill.getId());
            }

            if (billDetails == null || billDetails.size() == 0) {
                response.setStatus(createErrorStatus(BRMMessage.billDetailsNotFound(pRequest.getEin())));
                return response;
            }

            ArrayList<UsageBillingEmployeeDetail> empDetails = new ArrayList<UsageBillingEmployeeDetail>();
            response.setIsMultiEin(isMultiEin);

            String prevEmployeeName = null;
            String prevCompanyName = null;
            int numEmployees = 0;
            int numCompanies = 0;
            for (Object[] billDetail : billDetails) {
                if (billDetail != null) {
                    UsageBillingEmployeeDetail empDetail = new UsageBillingEmployeeDetail();
                    String companyName = (String) billDetail[0];
                    String employeeName = (String) billDetail[1];
                    SpcfCalendar paycheckDate = (SpcfCalendar) billDetail[2];

                    empDetail.setCompanyName(companyName);
                    empDetail.setEmployeeName(employeeName);
                    if (paycheckDate != null) {
                        Date checkDate = CalendarUtils.convertToDate(paycheckDate);
                        empDetail.setPaycheckDate(checkDate);
                    }
                    empDetail.setCheckNumber((String) billDetail[3]);
                    empDetail.setEin(entitlement.getLicenseNumber());
                    empDetails.add(empDetail);
                    if (employeeName != null && !employeeName.equals(prevEmployeeName)) numEmployees++;
                    if (companyName != null && !companyName.equals(prevCompanyName)) numCompanies++;
                    prevEmployeeName = employeeName;
                    prevCompanyName = companyName;
                }
            }

            SpcfCalendar usageStartDateCal = CalendarUtils.getFirstDayOfPrevMonth(spcfBillDate);
            SpcfCalendar usageEndDateCal = CalendarUtils.getLastDayOfMonth(usageStartDateCal);
            Date usageStartDate = CalendarUtils.convertToDate(usageStartDateCal);
            Date usageEndDate = CalendarUtils.convertToDate(usageEndDateCal);
            response.setUsagePeriodStartDate(usageStartDate);
            response.setUsagePeriodEndDate(usageEndDate);
            response.setEmployeeDetails(empDetails);
            response.setNumEmployeesBilled(numEmployees);
            response.setNumCompaniesBilled(numCompanies);
            response.setStatus(createSuccessStatus());

        } catch (Exception e) {
            logger.warn(e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return response;
    }

    private ResponseStatus createErrorStatus(BRMMessage message) {
        ResponseStatus status = new ResponseStatus();
        status.setSuccess("N");
        status.setCode(message.getCode());
        status.setMessage(message.getMessage());
        logger.info("Sending error:" + message.getMessage());
        return status;
    }

    private ResponseStatus createSuccessStatus() {
        ResponseStatus status = new ResponseStatus();
        status.setSuccess("Y");
        return status;
    }

}
package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

import java.util.Collection;
import java.util.Iterator;

/**
 * Hand-written business logic
 */
public class ServiceSubStatus extends BaseServiceSubStatus {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Function to return the List of ServiceSubStatuses for a given SourceSystemCode, Source CompanyId & ServiceCode
     * @param pSourceSystemCode SourceSystemCode
     * @param pSourceCompanyId String
     * @param pServiceCd ServiceCode
     * @return DomainEntitySet<ServiceSubStatus>
     */
    public static DomainEntitySet<ServiceSubStatus> findCurrentSubStatuses(SourceSystemCode pSourceSystemCode,
                                                            String pSourceCompanyId, ServiceCode pServiceCd) {
        DomainEntitySet<ServiceSubStatus> serviceSubStatusList = new DomainEntitySet<ServiceSubStatus>();

        Company company = Company.findCompany(pSourceCompanyId, pSourceSystemCode);
        CompanyService companyService = CompanyService.findCompanyService(company, pServiceCd);

        Collection<ServiceSubStatusCode> onHoldReasonCodes = company.getCurrentOnHoldReasonCodes();

        if (company.isCompanyOnHold()) {
            for (Iterator<ServiceSubStatusCode> iter = onHoldReasonCodes.iterator();
                 iter.hasNext();
                    ) {
                ServiceSubStatusCode onHoldReasonCd = iter.next();
                ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReasonCd);
                serviceSubStatusList.add(serviceSubStatus);
            }
        }
        else {
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, companyService.getStatusCd());
            serviceSubStatusList.add(serviceSubStatus);
        }

        return serviceSubStatusList;
    }

    /**
     * Function to return the possible substatus for a given ServiceStatusCode, Service Code and the logged in User role
     * If the user is not an agent, method returns all the ServiceSubStatuses for a given ServiceStatusCode.
     * If the user is an agent, this method returns all the the possible substatuses from the RoleSubStatus entity
     * for the given ServiceStatusCode and user role.
     * Method gets the user role from the PspPrincipal.
     * @param pServiceStatusCode ServiceStatusCode
     * @param pServiceCd  ServiceCode
     * @return DomainEntitySet<ServiceSubStatus>
     */
    public static DomainEntitySet<ServiceSubStatus> findPossibleSubStatuses(ServiceStatusCode pServiceStatusCode, ServiceCode pServiceCd) {
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

        DomainEntitySet<ServiceSubStatus> possibleSubStatusList = new DomainEntitySet<ServiceSubStatus>();

        ServiceStatus serviceStatus = Application.findById(ServiceStatus.class, pServiceStatusCode);

        for (ServiceSubStatus serviceSubStatus : serviceStatus.getServiceSubStatusCollection()) {
            if (ServiceStatus.isAllowedServiceStatus(serviceSubStatus.getServiceSubStatusCd(), pServiceCd)) {
                possibleSubStatusList.add(serviceSubStatus);
            }
        }

        if (principal.isAgent() && possibleSubStatusList.size() > 0) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());

            ServiceSubStatus subStatusArray[] = new ServiceSubStatus[possibleSubStatusList.size()];
            subStatusArray = possibleSubStatusList.toArray(subStatusArray);

            DomainEntitySet<RoleSubStatus> roleStatusList = foundUser.getRoleSubStatuses().find(RoleSubStatus.ServiceSubStatus().in(subStatusArray));

            possibleSubStatusList = new DomainEntitySet<ServiceSubStatus>();
            for (RoleSubStatus roleSubStatus : roleStatusList) {
                if (ServiceStatus.isAllowedServiceStatus(roleSubStatus.getServiceSubStatus().getServiceSubStatusCd(),
                        pServiceCd)) {
                    if (roleSubStatus.getAllowedChangeType().equals(SubStatusChangeType.CanMoveToSubStatus))
                        possibleSubStatusList.add(roleSubStatus.getServiceSubStatus());
                }
            }
        }

        return possibleSubStatusList;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public ServiceSubStatus()
	{
		super();
	}

    public boolean canMoveFromSubStatus() {
         PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

         if (principal.isAgent()) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());

            ServiceSubStatus[] subStatuses = new ServiceSubStatus[1];
            subStatuses[0] = this;

            DomainEntitySet<RoleSubStatus> roleStatusList = foundUser.getRoleSubStatuses().find(RoleSubStatus.ServiceSubStatus().in(subStatuses));

            for (RoleSubStatus roleSubStatus : roleStatusList) {
                if(roleSubStatus.getAllowedChangeType().equals(SubStatusChangeType.CanMoveFromSubStatus))
                    return true;
            }

            return false;

         }  else {
             return true;
         }
    }

    public boolean canMoveToSubStatus() {
         PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();

         if (principal.isAgent()) {
            AuthUser foundUser = AuthUser.findUser(principal.getId());

            ServiceSubStatus[] subStatuses = new ServiceSubStatus[1];
            subStatuses[0] = this;

            DomainEntitySet<RoleSubStatus> roleStatusList = foundUser.getRoleSubStatuses().find(RoleSubStatus.ServiceSubStatus().in(subStatuses));


            for (RoleSubStatus roleSubStatus : roleStatusList) {
                if(roleSubStatus.getAllowedChangeType().equals(SubStatusChangeType.CanMoveToSubStatus))
                    return true;
            }

            return false;

         }  else {
             return true;
         }
    }

    public boolean isServiceSubStatusAllowedChangeTypeForUser(AuthUser pAuthUser,
                                                              SubStatusChangeType pSubStatusChangeType){

        return pAuthUser.getRoleSubStatuses()
                .find(RoleSubStatus.ServiceSubStatus().equalTo(this)
                        .And(RoleSubStatus.AllowedChangeType().equalTo(pSubStatusChangeType)))
                .size() > 0;
    }

    public static boolean isAS400HoldReason(ServiceSubStatusCode pServiceSubStatusCode) {
        return pServiceSubStatusCode.equals(ServiceSubStatusCode.AS400DirectDepositLimitHold) || pServiceSubStatusCode.equals(ServiceSubStatusCode.AS400Hold);
    }
}
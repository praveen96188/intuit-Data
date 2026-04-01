package com.intuit.sbd.payroll.psp.adapters.ivr.webservices;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.ivr.dto.GetServiceKeyRequest;
import com.intuit.sbd.payroll.psp.adapters.ivr.dto.GetServiceKeyResponse;
import com.intuit.sbd.payroll.psp.adapters.ivr.dto.ServiceKeyInfo;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

/**
 * User: dweinberg
 * Date: Jun 14, 2010
 * Time: 11:33:23 AM
 */
@WebService()
public class IVRWebServices {

    private static final SpcfLogger logger = PayrollServices.getLogger(IVRWebServices.class);

    @WebMethod
    @WebResult(name = "GetServiceKeyResponse")
    public GetServiceKeyResponse getServiceKey(@WebParam(name = "GetServiceKeyRequest") GetServiceKeyRequest pRequest) {
        GetServiceKeyResponse response = new GetServiceKeyResponse();
        try {
            PayrollServices.beginUnitOfWork();

            List<ServiceKeyInfo> serviceKeys = new ArrayList<ServiceKeyInfo>();

            DomainEntitySet<EntitlementUnit> entitlementUnits = EntitlementUnit.findEntitlementUnits(pRequest.getEin(), pRequest.getLicenseNumber(), pRequest.getEoc());

            for (EntitlementUnit entitlementUnit : entitlementUnits) {
                ServiceKeyInfo info = new ServiceKeyInfo();
                info.setEoc(entitlementUnit.getEntitlement().getEntitlementOfferingCode());
                info.setLicenseNumber(entitlementUnit.getEntitlement().getLicenseNumber());
                info.setServiceKey(entitlementUnit.getServiceKey());
                serviceKeys.add(info);
            }

            response.setServiceKeys(serviceKeys);
        } catch (Exception e) {
            logger.warn(e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

}
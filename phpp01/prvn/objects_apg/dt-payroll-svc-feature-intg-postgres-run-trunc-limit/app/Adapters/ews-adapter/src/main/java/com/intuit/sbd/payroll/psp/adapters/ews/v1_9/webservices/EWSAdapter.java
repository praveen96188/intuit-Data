package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.webservices;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.GetPayrollInfoWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PayrollInfoWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author Jeff Jones
 */

@WebService()
public class EWSAdapter {

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "CreateAccountResponse")
    public EwsCreateAccountResponse Create_Account(@WebParam(name = "CreateAccountRequest") EwsCreateAccount pRequest) {
        CreateAccountProcess process = new CreateAccountProcess(pRequest, false);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "QueryAccountResponse")
    public EwsQueryAccountResponse Query_Account(@WebParam(name = "QueryAccountRequest") EwsQueryAccount pRequest) {
        QueryAccountProcess process = new QueryAccountProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "AddServiceResponse")
    public EwsAddServiceResponse Add_Service(@WebParam(name = "AddServiceRequest") EwsAddService pRequest) {
        AddServiceProcess process = new AddServiceProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "DeactivateServiceResponse")
    public EwsDeactivateServiceResponse Deactivate_Service(@WebParam(name = "DeactivateServiceRequest") EwsDeactivateService pRequest) {
        DeactivateServiceProcess process = new DeactivateServiceProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "ResetPinResponse")
    public EwsResetPinResponse Reset_Pin(@WebParam(name = "ResetPinRequest") EwsResetPin pRequest) {
        ResetPinProcess process = new ResetPinProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "CreatePinResponse")
    public EwsBasePinResponse Create_Pin(@WebParam(name = "CreatePinRequest") EwsBasePin pRequest) {
        CreatePinProcess process = new CreatePinProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "AuthenticatePinResponse")
    public EwsBasePinResponse Authenticate_Pin(@WebParam(name = "AuthenticatePinRequest") EwsBasePin pRequest) {
        AuthenticatePinProcess process = new AuthenticatePinProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "UpdatePinResponse")
    public EwsBasePinResponse Update_Pin(@WebParam(name = "UpdatePinRequest") EwsUpdatePin pRequest) {
        UpdatePinProcess process = new UpdatePinProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "ValidateBankResponse")
    public EwsBankResponse Validate_Bank(@WebParam(name = "ValidateBankRequest") EwsValidateBank pRequest) {
        ValidateBankProcess process = new ValidateBankProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "UpdateBankResponse")
    public EwsBankResponse Update_Bank(@WebParam(name = "UpdateBankRequest") EwsUpdateBank pRequest) {
        UpdateBankProcess process = new UpdateBankProcess(pRequest);
        return process.execute();
    }

    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "UpdateAccountResponse")
    public EwsUpdateAccountResponse Update_Account(@WebParam(name = "UpdateAccountRequest") EwsUpdateAccount pRequest) {
        UpdateAccountProcess process = new UpdateAccountProcess(pRequest);
        return process.execute();
    }

    @WebMethod
    @WebResult(name = "ValidateSubscriptionResponse")
    public EwsValidateSubscriptionResponse Validate_Subscription(@WebParam(name = "ValidateSubscriptionRequest") EwsValidateSubscription pRequest) {
        ValidateSubscriptionProcess process = new ValidateSubscriptionProcess(pRequest);
        return process.execute();
    }
    
    /**
     * @param pRequest
     * @return
     */
    @WebMethod()
    @WebResult(name = "QueryOfferResponse")
    public EwsQueryOfferResponse Query_Offer(@WebParam(name = "QueryOfferRequest") EwsQueryOffer pRequest) {
        QueryOfferProcess process = new QueryOfferProcess(pRequest);
        return process.execute();
    }

    @WebMethod()
    @WebResult(name = "QuerySubscriptionsResponse")
    public EwsQuerySubscriptionsResponse Query_Subscriptions(@WebParam(name = "QuerySubscriptions") EwsQuerySubscriptions pRequest) {
        QuerySubscriptionsProcess process = new QuerySubscriptionsProcess(pRequest);
        return process.execute();
    }

    //@WebMethod()
    //@WebResult(name = "MigrateAccountResponse")
    //public EwsMigrateAccountResponse Migrate_Account(@WebParam(name = "MigrateAccount") EwsMigrateAccount pRequest) {
    //    MigrateAccountProcess process = new MigrateAccountProcess(pRequest);
    //    return process.execute();
    //}

    @WebMethod()
    @WebResult(name = "PayrollInfo")
    public PayrollInfoWSDTO QueryPayrollStatus(@WebParam(name = "GetPayrollInfo") GetPayrollInfoWSDTO pRequest) {
        QueryPayrollStatusProcess process = new QueryPayrollStatusProcess(pRequest);
        return process.execute();
    }

    @WebMethod()
    @WebResult(name = "UpdateBillingDetailsResponse")
    public EwsResponse Update_Billing_Details(@WebParam(name = "UpdateBillingDetails") EwsUpdateBillingDetails pRequest) {
        UpdateBillingDetailsProcess process = new UpdateBillingDetailsProcess(pRequest);
        return process.execute();
    }

    @WebMethod()
    @WebResult(name = "EinServiceEligibilityResponse")
    public EwsEinServiceEligibilityResponse Check_EIN_Service_Eligibility (@WebParam(name = "EinServiceEligibility") EwsEinServiceEligibility pRequest) {
        QueryServiceEligibilityProcess process = new QueryServiceEligibilityProcess(pRequest);
        return process.execute();
    }

    @WebMethod()
    @WebResult(name = "QueryServiceKeyResponse")
    public EwsQueryServiceKeyResponse Query_Service_Key (@WebParam(name = "QueryServiceKey") EwsQueryServiceKey pRequest) {
        QueryServiceKeyProcess process = new QueryServiceKeyProcess(pRequest);
        return process.execute();
    }

    @WebMethod()
    @WebResult(name = "MigrateEntitlementResponse")
    public EwsMigrateEntitlementResponse Migrate_Entitlement(@WebParam(name = "MigrateEntitlement") EwsMigrateEntitlement pRequest) {
        MigrateEntitlementProcess process = new MigrateEntitlementProcess(pRequest);
        return process.execute();
    }
}

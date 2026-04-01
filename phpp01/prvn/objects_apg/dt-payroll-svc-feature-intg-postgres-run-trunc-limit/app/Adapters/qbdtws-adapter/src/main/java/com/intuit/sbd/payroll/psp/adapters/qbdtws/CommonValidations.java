package com.intuit.sbd.payroll.psp.adapters.qbdtws;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: rnorian
 * Date: Dec 15, 2009
 * Time: 1:52:13 PM
 */
public class CommonValidations {

    public static Company validateCompanyPin(Request pRequest, QBProcessingMessages pProcessingMessages) {
        String sourceCompanyId = pRequest.getPSID();
        String companyPIN = pRequest.getPIN();

        // validate company & PIN
        ProcessResult<Company> companyValidationResult =
                PayrollServices.subscriptionManager.verifyCompanyPIN(SourceSystemCode.QBDT, sourceCompanyId,  companyPIN);
        if (!companyValidationResult.isSuccess()) {
            ErrorMessageList.mergeResults(companyValidationResult, pProcessingMessages.getProcessingMessagesList());
        }

        return companyValidationResult.getResult();
    }

    public static ProcessResult<Company> validateCompanyPin(Request pRequest) {
        ProcessResult<Company> processResult = new ProcessResult<Company>();

        String sourceCompanyId = pRequest.getPSID();
        String companyPIN = pRequest.getPIN();

        return PayrollServices.subscriptionManager.verifyCompanyPIN(SourceSystemCode.QBDT, sourceCompanyId,  companyPIN);
    }

    /**
     * Verify that the app version has not been sunsetted.
     *
     * @param pClientVersion - qb version e.g. 17.01.R.10/14586#pro
     * @param pProcessingMessages - processing message list
     * @return - true if valid, false if not.
     */
    public static OFXAPPVERObject isQBVersionActive(String pClientVersion, QBProcessingMessages pProcessingMessages) {
        // todo we probably need our own parameter
        // MinQBVersionSupported is cached on the domain level.
        String minAppQBVersionSupportedStr =
                SourcePayrollParameter.findSourcePayrollParameter(
                    SourceSystemCode.QBDT, SourcePayrollParameterCode.MinQBVersionSupported).getParameterValue();
        Integer minAppQBVersionSupported = new Integer(minAppQBVersionSupportedStr);
        OFXAPPVERObject ofxappverObject = new OFXAPPVERObject(pClientVersion);

        if(ofxappverObject.getIntQBVersion() == null){
            pProcessingMessages.getProcessingMessagesList().add(ErrorMessageList.fieldDataNotValid("ClientApplicationVersion", "QBCompany"));
            return ofxappverObject;
        }

        if(ofxappverObject.getIntQBVersion() < minAppQBVersionSupported) {
            pProcessingMessages.getProcessingMessagesList().add(ErrorMessageList.unsupportedVersion(ofxappverObject.getIntQBVersion().toString()));
        }

        return ofxappverObject;
    }

    public static boolean hasActiveCloudService(Company company, QBProcessingMessages pProcessingMessages) {
        DomainEntitySet<CompanyService> activeCloudServices = company.getCompanyServiceCollection().find(
                CompanyService.Service().ServiceCd().in(ServiceCode.ThirdParty401k, ServiceCode.DirectDeposit, ServiceCode.CheckDistribution)
                .And(CompanyService.StatusCd().notIn(ServiceSubStatusCode.Terminated, ServiceSubStatusCode.Cancelled)));

        if (activeCloudServices.size() == 0) {
            pProcessingMessages.getProcessingMessagesList().add(ErrorMessageList.NoActiveCloudService());
        }

        return activeCloudServices.size() > 0;
    }

    private static Boolean validateValue(String pValue, boolean pNullable, String pPattern) {
        Pattern pattern = Pattern.compile(pPattern);

        if ((!pNullable) && (pValue == null)) {
            return false;
        }

        if (pValue == null) {
            return true;
        }

        Matcher matcher = pattern.matcher(pValue.trim());
        return matcher.matches();
    }
}

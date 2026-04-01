package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: May 14, 2008
 * Time: 12:50:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class InitializeSourceSystemTransmissionSecondaryCore extends Process implements IProcess {
    /**
     * Core process for initializing a transmission to PSP
     *
     * @author Marcela Villani
     */

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mTransmissionId;
    private SourceSystemTransmissionDTO mSourceSystemTransmissionDTO;
    private Company mCompany;
    private SpcfCalendar mInitializeDateTime;

    public InitializeSourceSystemTransmissionSecondaryCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mTransmissionId = pTransmissionId;
        mSourceSystemTransmissionDTO = pSourceSystemTransmissionDTO;
        mInitializeDateTime = PSPDate.getPSPTime();
    }

    public InitializeSourceSystemTransmissionSecondaryCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, SpcfCalendar pInitializeDateTime) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mTransmissionId = pTransmissionId;
        mSourceSystemTransmissionDTO = pSourceSystemTransmissionDTO;
        mInitializeDateTime = pInitializeDateTime;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        // SourceSystem and Company are optional parameters so only perform company validations
        // if at least one of the two is passed to the process
        if (mSourceSystemCd != null || mSourceCompanyId != null) {
            // Check if Company parameters are valid
            validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            // Check if Company exists

            mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

            if (mCompany == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                        mSourceSystemCd.toString(), mSourceCompanyId);
                return validationResult;
            }
        }

        // Check if Transmission ID is null
        if (mTransmissionId == null) {
            validationResult.getMessages()
                    .InvalidSourceSystemTransmission(EntityName.SourceSystemTransmission, "Source System Transmission");
            return validationResult;
        }

        // Validate Payroll Transmission DTO
        validationResult.merge(mSourceSystemTransmissionDTO.validateSourceSystemTransmissionDTO());
        return validationResult;
    }

    public ProcessResult<SourceSystemTransmission> process() {
        ProcessResult processResult = new ProcessResult();

        SourceSystemTransmission sourceSystemTransmission = new SourceSystemTransmission();
        if(mCompany != null && mCompany.getId()!=null)
            sourceSystemTransmission.setCompanyId(mCompany.getId().toString());
        sourceSystemTransmission.setTransmissionIdentifier(mTransmissionId);
        sourceSystemTransmission.setType(mSourceSystemTransmissionDTO.getTransmissionType());

        sourceSystemTransmission.setRequestToken(mSourceSystemTransmissionDTO.getRequestToken());
        sourceSystemTransmission.setDescription(mSourceSystemTransmissionDTO.getDescription());
        sourceSystemTransmission.setFromSourceSystem(mSourceSystemTransmissionDTO.getFromSourceSystem());
        sourceSystemTransmission.setToSourceSystem(mSourceSystemTransmissionDTO.getToSourceSystem());
        sourceSystemTransmission.setInitializeDateTime(mInitializeDateTime);
        sourceSystemTransmission.setApplicationId(mSourceSystemTransmissionDTO.getApplicationId());
        sourceSystemTransmission.setApplicationVersion(mSourceSystemTransmissionDTO.getApplicationVersion());
        sourceSystemTransmission.setTaxTableId(mSourceSystemTransmissionDTO.getTaxTableId());
        sourceSystemTransmission.setIPAddress(mSourceSystemTransmissionDTO.getIPAddress());

        sourceSystemTransmission.setHost("Unknown");
        try {
            if (InetAddress.getLocalHost() != null) {
                sourceSystemTransmission.setHost(InetAddress.getLocalHost().getHostName());
            }
        } catch (UnknownHostException e) {
            // ignore
        }

        sourceSystemTransmission = ApplicationSecondary.save(sourceSystemTransmission);

        if (mSourceSystemTransmissionDTO.getIPAddress() != null) {
            sourceSystemTransmission.setIPAddress(mSourceSystemTransmissionDTO.getIPAddress());
        }
        sourceSystemTransmission.setRequestDocument(mSourceSystemTransmissionDTO.getRequestDocument());
        sourceSystemTransmission = ApplicationSecondary.save(sourceSystemTransmission);
        processResult.setResult(sourceSystemTransmission);
        return processResult;
    }

}
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementMessageDTO;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessage;
import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.Hibernate;


/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 8, 2010
 * Time: 7:49:02 AM
 */
public class AddEntitlementMessageCore extends Process implements IProcess {
    private EntitlementMessageDTO mEntitlementMessageDTO;
    private EntitlementMessage mEntitlementMessage;

    public AddEntitlementMessageCore(EntitlementMessageDTO pEntitlementMessageDTO) {
        mEntitlementMessageDTO = pEntitlementMessageDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mEntitlementMessageDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementMessage, "null", "EntitlementMessageDTO");
            return validationResult;
        }

        validationResult.merge(mEntitlementMessageDTO.validate());

        return validationResult;
    }

    @Override
    public ProcessResult<EntitlementMessage> process() {
        ProcessResult<EntitlementMessage> processResult = new ProcessResult<EntitlementMessage>();

        copyDTOToDomain();

        int minutesToWait = SystemParameter.findIntValue(SystemParameter.Code.AMO_MESSAGE_EXPIRATION_WAIT_PERIOD, 20);
        SpcfCalendar expirationTime = PSPDate.getPSPTime().copy();
        expirationTime.addMinutes(minutesToWait);
        mEntitlementMessage.setExpirationTimestamp(expirationTime);

        mEntitlementMessage = Application.save(mEntitlementMessage);

        // Workaround: we have to set the clob after the save call because of batching behavior
        mEntitlementMessage.setMessage(mEntitlementMessageDTO.getMessage());

        processResult.setResult(mEntitlementMessage);

        return processResult;
    }

    private void copyDTOToDomain() {
        mEntitlementMessage = new EntitlementMessage();
        if(mEntitlementMessageDTO.getEntitlementMessageStatusCode() == null) {
            mEntitlementMessage.setStatus(EntitlementMessageStatusCode.New);
        } else {
            mEntitlementMessage.setStatus(mEntitlementMessageDTO.getEntitlementMessageStatusCode());
        }
        mEntitlementMessage.setLicenseNumber(mEntitlementMessageDTO.getLicenseNumber());
        mEntitlementMessage.setEntitlementOfferingCode(mEntitlementMessageDTO.getEntitlementOfferingCode());
        mEntitlementMessage.setOrderNumber(mEntitlementMessageDTO.getOrderNumber());
        mEntitlementMessage.setToken(mEntitlementMessageDTO.getToken());
        mEntitlementMessage.setEventReason(mEntitlementMessageDTO.getEventReason());
        mEntitlementMessage.setMessageTimestamp(mEntitlementMessageDTO.getMessageTimestamp());
    }
}

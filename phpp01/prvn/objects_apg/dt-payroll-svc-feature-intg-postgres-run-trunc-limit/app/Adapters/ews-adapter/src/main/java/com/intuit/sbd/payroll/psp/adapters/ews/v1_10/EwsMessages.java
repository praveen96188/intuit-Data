package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.text.MessageFormat;

/**
 * @author Jeff Jones
 */
public class EwsMessages {

    private static SpcfLogger logger = PayrollServices.getLogger(EwsMessages.class);

    public static EwsMessage success() {
        return createEwsMessage(0);
    }

    public static EwsMessage systemError() {
        return createEwsMessage(30001);
    }

    public static EwsMessage xmlParsingError() {
        return createEwsMessage(30002);
    }

    public static EwsMessage fieldCanNotBeNullOrEmpty(Object... pArgs) {
        return createEwsMessage(30004, pArgs);
    }

    public static EwsMessage fieldDataNotValid(Object... pArgs) {
        return createEwsMessage(30005, pArgs);
    }

    public static EwsMessage objectCanNotBeNull(Object... pArgs) {
        return createEwsMessage(30006, pArgs);
    }

    public static EwsMessage webServiceNotAvailable() {
        return createEwsMessage(30007);
    }

    public static EwsMessage ersConnectionError() {
        return createEwsMessage(30010);
    }

    public static EwsMessage psidDoesNotExistError() {
        return createEwsMessage(30101);
    }

    public static EwsMessage einDoesNotExistError() {
        return createEwsMessage(30102);
    }

    public static EwsMessage subscriptionNumberDoesNotExistError() {
        return createEwsMessage(30142);
    }

    public static EwsMessage pinAlreadyExists() {
        return createEwsMessage(30111);
    }

    public static EwsMessage offerCodeDoesNotExist() {
        return createEwsMessage(30112);
    }

    public static EwsMessage forceRandomAmountProcessError() {
        return createEwsMessage(30117);
    }

    public static EwsMessage pendingUpgradeToAssisted() {
        return createEwsMessage(30118);
    }

    public static EwsMessage subTypeNotFound(Object... pArgs) {
        return createEwsMessage(30119, pArgs);
    }

    public static EwsMessage noBankAccount() {
        return createEwsMessage(30121);
    }

    public static EwsMessage invalidEWStoPSPStatusMapping() {
        return createEwsMessage(30125);
    }

    public static EwsMessage companyMigrating() {
        return createEwsMessage(30127);
    }

    public static EwsMessage invalidActionType() {
        return createEwsMessage(30128);
    }

    public static EwsMessage resetPinFailureNotOnPSP() {
        return createEwsMessage(30133);
    }

    public static EwsMessage serviceAlreadyExists(Object... pArgs) {
        return createEwsMessage(30137, pArgs);
    }

    public static EwsMessage missingDiyEntitlement() {
        return createEwsMessage(30140);
    }

    public static EwsMessage missingAssistedEntitlement() {
        return createEwsMessage(30141);
    }

    public static EwsMessage psiSocketConnectionError() {
        return createEwsMessage(30009);
    }

    public static EwsMessage noActiveEntitlementUnit() {
        return createEwsMessage(30145);
    }

    public static EwsMessage partialIAMInfo() {
        return createEwsMessage(30150);
    }

    public static EwsMessage ddAndAssistedServiceExistError() {
        return createEwsMessage(30151);
    }

    public static EwsMessage dynamicError(Object... pArgs) {
        return createEwsMessage(30152, pArgs);
    }

    public static EwsMessage realmIdDoesNotExists() {
        return createEwsMessage(30153);
    }

    public static EwsMessage psidMismatch(Object... pArgs) {
        return  createEwsMessage(30157, pArgs);
    }

    public static EwsMessage duplicateEntitlementsFound() {
        return createEwsMessage(30159);
    }

    public static EwsMessage duplicateActiveEntitlementUnitsFound() {
        return createEwsMessage(30160);
    }

    public static EwsMessage nonUniqueEntitlementUnitFound() {
        return createEwsMessage(30161);
    }

    public static EwsMessage einAndSubscriptionNumberDoesNotExists() {
        return createEwsMessage(30162);
    }

    public static EwsMessage IUSUpdatesFailed() {
        return createEwsMessage(30167);
    }

    public static EwsMessage assistedOrAssistedAdvantageEntitlementCannotDeactivated(String ein) {
        return createEwsMessage(30164, ein);
    }

    public static EwsMessage einDoesNotBelongToTheGivenSubscriptionNumber(String ein, String subscriptionNumber) {
        return createEwsMessage(30165, ein, subscriptionNumber);
    }

    public static EwsMessage einIsAlreadyDeactivated(String ein) {
        return createEwsMessage(30166, ein);
    }

    //Creating EWS Message for VMPGrantFailure
    public static EwsMessage VMPGrantAdditionFailed() {
        return createEwsMessage(30168);
    }

    public static EwsMessage convertPSPMessage(Message pPSPMessage) {
        Integer pspMessageCode = Integer.parseInt(pPSPMessage.getMessageCode());

        switch (pspMessageCode) {
            case 189:
                return createEwsMessage(30115);
            case 190:
                return createEwsMessage(30104);
            case 197:
                return createEwsMessage(30130);
            case 204:
                return createEwsMessage(30132);
            case 205:
                return createEwsMessage(30114);
            case 208:
                return createEwsMessage(30131);
            case 217:
                return createEwsMessage(30123);
            case 218:
                return createEwsMessage(30122);
            case 219:
                return createEwsMessage(30135);
            case 226:
                return createEwsMessage(30124);
            case 250:
                return createEwsMessage(30136);
            case 292:
                return createEwsMessage(30103);
            case 293:
                return createEwsMessage(30108);
            case 294:
                return createEwsMessage(30116);
            case 302:
                return createEwsMessage(30134);
            case 316:
                return createEwsMessage(30138);
            case 320:
                return createEwsMessage(30143);
            case 322:
                return createEwsMessage(30155);
            case 324:
                return createEwsMessage(30156);
            case 325:
                return createEwsMessage(30158);
            case 601:
                return createEwsMessage(30144);
            case 602:
                return createEwsMessage(30139);
            case 1101:
                return createEwsMessage(30107);
            case 1038:
                return createEwsMessage(30126);
            case 1039:
                return createEwsMessage(30129);
            case 1068:
                return createEwsMessage(30163);
            case 1511:
                return createEwsMessage(30154);
            default:
                logger.error("Unexpected PSP message code: " + pspMessageCode + " " + pPSPMessage.getMessage());
                return createEwsMessage(30001);
        }
    }

    public static EwsMessage perCheckFeeMessage(Object... pArgs) {
        return createEwsMessage(90000, pArgs);
    }

    public static EwsMessage salesTaxMessage() {
        return createEwsMessage(90001);
    }

    public static EwsMessage transmissionFeeMessage() {
        return createEwsMessage(90002);
    }

    public static EwsMessage reversalFeeMessage() {
        return createEwsMessage(90003);
    }

    public static EwsMessage nsfFeeMessage() {
        return createEwsMessage(90004);
    }

    public static EwsMessage paymentArrangementFeeMessage() {
        return createEwsMessage(90005);
    }

    public static EwsMessage employeesPaidFeeMessage(Object... pArgs) {
        return createEwsMessage(90006, pArgs);
    }

    public static EwsMessage createEwsMessage(int pCode) {
        return createEwsMessage(pCode, "");
    }

    public static EwsMessage createEwsMessage(int pCode, Object... pArgs) {
        MessageDefinition defintion = MessageDefinition.getMessageDefinition(pCode);
        return createEwsMessage(defintion, pArgs);
    }

    private static EwsMessage createEwsMessage(MessageDefinition messageDefinition, Object... args) {
        EwsMessage message = new EwsMessage();
        message.setCode(messageDefinition.getNumber());

        MessageFormat format = new MessageFormat(messageDefinition.getMessageFormat());
        message.setMessage(format.format(args));

        return message;
    }    
    
}

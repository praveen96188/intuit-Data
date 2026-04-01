package com.intuit.sbd.payroll.psp.common.utils.log;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.slf4j.Logger;

import java.util.Objects;

public class MoneyMovementLogHelper {

    public static final String MONEY_MOVEMENT_PSID_CONTEXT_KEY = "MoneyMovement.PSID";
    public static final String MONEY_MOVEMENT_REALMID_CONTEXT_KEY = "MoneyMovement.RealmId";
    public static final String MONEY_MOVEMENT_SUBSCRIPTION_CONTEXT_KEY = "MoneyMovement.Subscription";
    public static final String MONEY_MOVEMENT_QB_VERSION_CONTEXT_KEY = "MoneyMovement.QBVersion";
    private static final String LOG_DELIMITER = ",";

    public static void logMoneyMovementEventMessage(SpcfLogger logger, MoneyMovementLogHelper.EventType eventType, String customeMessage){
        logMoneyMovementEventMessage(logger, eventType,customeMessage,null);
    }

    public static void logMoneyMovementEventMessage(SpcfLogger logger, MoneyMovementLogHelper.EventType eventType, String customeMessage, String errorMessage){
        logMoneyMovementEventMessage(logger, eventType,
                SpcfLogManager.getContext(MONEY_MOVEMENT_PSID_CONTEXT_KEY),
                SpcfLogManager.getContext(MONEY_MOVEMENT_REALMID_CONTEXT_KEY),
                SpcfLogManager.getContext(MONEY_MOVEMENT_SUBSCRIPTION_CONTEXT_KEY),
                SpcfLogManager.getContext(MONEY_MOVEMENT_QB_VERSION_CONTEXT_KEY),
                customeMessage,
                errorMessage);
    }

    public static void logMoneyMovementEventMessage(SpcfLogger logger, MoneyMovementLogHelper.EventType eventType,
                                                     String psid,
                                                     String realmId,
                                                     String subscription,
                                                     String qbVersion,
                                                     String customeMessage,
                                                     String errorMessage){
        logger.info(createLogMessage(eventType, psid, realmId, subscription, qbVersion, customeMessage, errorMessage));
    }

    public static void logMoneyMovementEventMessage(Logger logger, MoneyMovementLogHelper.EventType eventType, String customeMessage){
        logMoneyMovementEventMessage(logger, eventType,customeMessage,null);
    }

    public static void logMoneyMovementEventMessage(Logger logger, MoneyMovementLogHelper.EventType eventType, String customeMessage, String errorMessage){
        logMoneyMovementEventMessage(logger, eventType,
                SpcfLogManager.getContext(MONEY_MOVEMENT_PSID_CONTEXT_KEY),
                SpcfLogManager.getContext(MONEY_MOVEMENT_REALMID_CONTEXT_KEY),
                SpcfLogManager.getContext(MONEY_MOVEMENT_SUBSCRIPTION_CONTEXT_KEY),
                customeMessage,
                errorMessage);
    }

    public static void logMoneyMovementEventMessage(Logger logger, MoneyMovementLogHelper.EventType eventType,
                                                    String psid,
                                                    String realmId,
                                                    String subscription,
                                                    String customeMessage,
                                                    String errorMessage){
        logger.info(createLogMessage(eventType, psid, realmId, subscription, null, customeMessage, errorMessage));
    }

    public static String createLogMessage(EventType eventType, String psid, String realmId, String subscription,String qbVErsion, String customeMessage, String errorMessage) {
        StringBuilder result = new StringBuilder();
        if (Objects.nonNull(eventType)){
            result.append("EventType=").append(eventType.name()).append(LOG_DELIMITER);
        }
        String currentPrincipal = getPrimaryPrincipalName();
        if (Objects.nonNull(currentPrincipal)){
            result.append("CurrentPrincipal=").append(currentPrincipal).append(LOG_DELIMITER);
        }
        if (Objects.nonNull(psid)){
            result.append("PSID=").append(psid).append(LOG_DELIMITER);
        }
        if (Objects.nonNull(realmId)){
            result.append("RealmId=").append(realmId).append(LOG_DELIMITER);
        }
        if (Objects.nonNull(subscription)){
            result.append("Subscription=").append(subscription).append(LOG_DELIMITER);
        }
        if (Objects.nonNull(errorMessage)){
            result.append("ErrorReason=").append(errorMessage).append(LOG_DELIMITER);
        }
        if (Objects.nonNull(customeMessage)){
            result.append(customeMessage);
        } else if (result.length()>0 && LOG_DELIMITER.equals(result.charAt(result.length()-1))){
            result.deleteCharAt(result.length()-1);
        }
        return result.toString();
    }

    public static void setLoggerContext(Company company){
        if (Objects.isNull(company))
            return;
        setLoggerContext(company.getSourceCompanyId(),
                company.getIAMRealmId(),
                company.hasService(ServiceCode.Tax)?"Assisted":"DIY",
                Objects.nonNull(company.getQuickbooksInfo())?company.getQuickbooksInfo().getApplicationVersion():"");
    }

    public static void setLoggerContext(String psid, String realmId, String subscription,String qbVersion){
        SpcfLogManager.putContext(
                MoneyMovementLogHelper.MONEY_MOVEMENT_PSID_CONTEXT_KEY, psid);
        SpcfLogManager.putContext(
                MoneyMovementLogHelper.MONEY_MOVEMENT_REALMID_CONTEXT_KEY, realmId);
        SpcfLogManager.putContext(
                MoneyMovementLogHelper.MONEY_MOVEMENT_SUBSCRIPTION_CONTEXT_KEY, subscription);
        SpcfLogManager.putContext(
                MoneyMovementLogHelper.MONEY_MOVEMENT_QB_VERSION_CONTEXT_KEY, qbVersion);
    }

    public static void removeLoggerContext(){
        SpcfLogManager.removeContext(MoneyMovementLogHelper.MONEY_MOVEMENT_PSID_CONTEXT_KEY);
        SpcfLogManager.removeContext(MoneyMovementLogHelper.MONEY_MOVEMENT_REALMID_CONTEXT_KEY);
        SpcfLogManager.removeContext(MoneyMovementLogHelper.MONEY_MOVEMENT_SUBSCRIPTION_CONTEXT_KEY);
        SpcfLogManager.removeContext(MoneyMovementLogHelper.MONEY_MOVEMENT_QB_VERSION_CONTEXT_KEY);
    }

    private static String getPrimaryPrincipalName() {
        return Objects.nonNull(Application.getCurrentPrincipal())? Application.getCurrentPrincipal().getName():"";
    }

    public enum EventType{
        AddService,
        CreatePin,
        QueryAccount,
        RealmidPsidAssociation,
        MoneyMovementAccountSync,
        DirectDepositEligibility,
        UpdateAccount
    }

}

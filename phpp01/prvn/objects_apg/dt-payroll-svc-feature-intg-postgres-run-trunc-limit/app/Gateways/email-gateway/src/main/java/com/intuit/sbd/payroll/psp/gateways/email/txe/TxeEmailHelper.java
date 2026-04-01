package com.intuit.sbd.payroll.psp.gateways.email.txe;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TxeEmailHelper {


    private static final SpcfLogger sfLogger = Application.getLogger(TxeEmailHelper.class);

    protected void logEmailDigest(String transactionId, EmailRequest pBody) {
        //
        // PSRV003631 - Log a digest of email id's so we can detect redundant emails
        //

        StringBuilder digest = new StringBuilder();

        for (NotificationDataType.Destinations.Destination dest : pBody.getSendRequest().getNotification().getDestinations().getDestination()) {
            digest.append(String.format("[Txn id: %s, Rec id: %s]%n", transactionId, dest.getRecipientId()));
        }

        if (digest.length() > 0) {
            sfLogger.info(String.format("%n<begin email digest>%n%s<end email digest>", digest.toString()));
        }
    }

    /**
     * Retrieve List of all the TxeEnabled templates
     *
     * @return
     */
    public static List<String> getTxeEnabledEtTemplates() {
        List<String> result = null;
        String templatesEnabledForTxE = FeatureFlags.get().stringValue(FeatureFlags.Key.ET_TXE_ENABLED_TEMPLATES, "Template ID");
        if (!templatesEnabledForTxE.isEmpty()) {
            String[] commaSeparatedArr = templatesEnabledForTxE.split("\\s*,\\s*");
            result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Method to check if a template ID is Txe enabled or not
     *
     * @param templateId
     * @return
     */
    public static Boolean isEtTemplateTxeEnabled(String templateId) {

        List<String> txeEnabledEtTemplates = getTxeEnabledEtTemplates();
        if (Objects.isNull(txeEnabledEtTemplates)) {
            return false;
        }
        if (txeEnabledEtTemplates.contains("ALL")) {
            return true;
        }
        for (String txeEnabledTemplate : txeEnabledEtTemplates) {
            if (templateId.contains(txeEnabledTemplate)) {
                return true;
            }
        }
        return false;
    }
}


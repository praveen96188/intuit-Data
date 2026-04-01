package com.intuit.sbd.payroll.psp.configuration;

/**
 * @author achaves
 *         Date: Feb 5, 2008
 *         Time: 10:29:41 PM
 */
public enum ConfigurationModule {
    QBDTAdapter("PSP-QBDT-Adapter"),
    QBOEAdapter("PSP-QBOE-Adapter"),
    EwsAdapter("PSP-EWS-Adapter"),
    SAPAdapter("PSP-SAP-Adapter"),
    EmailGateway("PSP-EMAIL-Gateway"),
    BatchJobs("PSP-BatchJobs"),
    DDRepUI("PSP-DDRepUI"),
    SalesTaxGateway("PSP-SalesTax-Gateway"),
    TaxCreditsAdapter("PSP-TaxCredits-Adapter"),
    TaxAgency("PSP-Agency"),
    WorkersCompGateway("PSP-WC-Gateway"),
    ViewMyPaycheck("PSP-VMP"),
    Common("PSP-Common");

    String moduleId;
    ConfigurationModule(String pModuleId) {
        moduleId = pModuleId;
    }
}

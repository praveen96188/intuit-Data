package com.intuit.ems.payroll.psp.gateway.brm;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/14/12
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IBRMGateway {
    /**
     * Wrapper for createServiceUsage  call
     */
    public CreateServiceResponse createUsage(String pTransactionId,
                                             SpcfCalendar pTransactionDateTime,
                                             String pSalesOrganization,
                                             String pLicenceId,
                                             String pLicenceSchemaName,
                                             String pEntitlementId,
                                             String pEntitlementSchemeName,
                                             String pActivityName,
                                             BigInteger pQuantityUsed) throws Throwable;

    /**
     * Wrapper for  queryUsageBalance  call
     */
    public QueryUsageBalanceResponse queryUsageBalance(String pTransactionId,
                                                       String pSalesOrganization,
                                                       String pLicenseId,
                                                       String pLicenceSchemaName,
                                                       String pEntitlementId,
                                                       String pEntitlementSchemeName,
                                                       String pActivityName,
                                                       XMLGregorianCalendar pTransactionDatetime
    ) throws Throwable;

}

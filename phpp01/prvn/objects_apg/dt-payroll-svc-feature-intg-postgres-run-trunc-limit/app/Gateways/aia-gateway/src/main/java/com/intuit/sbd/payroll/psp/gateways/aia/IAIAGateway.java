package com.intuit.sbd.payroll.psp.gateways.aia;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/14/12
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IAIAGateway {
    public List<BillInfo> queryInvoiceList(String pCustomerId, String pBillingProfileId) throws IntuitFaultMsg;
    public List<ItemCharge> queryInvoiceDetails(String pCustomerId, String pBillingProfileId, String pBillPOID) throws IntuitFaultMsg;
    public int queryEventDetails(String pCustomerId, String pBillingProfileId, String pBillPOID, String pItemChargeId) throws IntuitFaultMsg;
}

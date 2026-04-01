package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 30, 2010
 * Time: 2:01:05 PM
 */
public class EntitlementMessageWSDTO {
    public String CustomerId;
    public String LicenseNumber;
    public String EntitlementOfferingCode;
    public String OrderNumber;
    public String BillingZipCode;
    public String BillingAddress;
    public String CancellationReason;
    public String AssetItemNumber;
    public String ContactEmail;
    public String ContactFirstName;
    public String ContactMiddleName;
    public String ContactLastName;
    public String EntitlementState;
    public Boolean IncludeBillingInformation;
    public String CCExpirationMonth;
    public String CCExpirationYear;
    public String CCNumber;
    public String CCType;
    public String Edition;
    public String NumberOfEmployees;
    public XMLGregorianCalendar NextChargeDate;
    public XMLGregorianCalendar TransactionDate;
    public XMLGregorianCalendar SubscriptionEndDate;
    public String SourceLicenseNumber;
    public String TargetLicenseNumber;
    public String AssetStatus;
    public String EventReason;
    public List<EntitlementUnitMessageWSDTO> EntitlementUnits;
}

<%@ page import="javax.xml.datatype.DatatypeFactory" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.GregorianCalendar" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    String stackTrace = null;
    String psid = null;
    String serviceKey = null;

    String ein = request.getParameter("ein");
    String LegalName = request.getParameter("LegalName");
    String pin = request.getParameter("pin");
    String service = request.getParameter("service");
    String AppVersion = request.getParameter("AppVersion");
    String LicenseNumber = request.getParameter("LicenseNumber");
    String Address = request.getParameter("Address");
    String City = request.getParameter("City");
    String State = request.getParameter("State");
    String Zip = request.getParameter("Zip");
    String FirstNamePA = request.getParameter("FirstNamePA");
    String LastNamePA = request.getParameter("LastNamePA");
    String JobTitlePA = request.getParameter("JobTitlePA");
    String EMailPA = request.getParameter("EMailPA");
    String WorkPhonePA = request.getParameter("WorkPhonePA");
    String FirstNamePP = request.getParameter("FirstNamePP");
    String LastNamePP = request.getParameter("LastNamePP");
    String JobTitlePP = request.getParameter("JobTitlePP");
    String EMailPP = request.getParameter("EMailPP");
    String WorkPhonePP = request.getParameter("WorkPhonePP");
    String BankName = request.getParameter("BankName");
    String AccountNumber = request.getParameter("AccountNumber");
    String RoutingNumber = request.getParameter("RoutingNumber");
    String QuickBooksName = request.getParameter("QuickBooksName");
    String AccountType = request.getParameter("AccountType");

    if (ein == null || ein.trim().equals("") || LegalName == null || LegalName.trim().equals("") || service == null || service.trim().equals("")) {
        errorMsg = "EIN, Legal Name, and Service are required";
    }   else {
        try{
            String ipAddress = "127.0.0.1";
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

            EwsCreateAccount ewsCreateAccount = new EwsCreateAccount();
            ewsCreateAccount.setForceRandomDollar(true);
            ewsCreateAccount.setIpAddress(ipAddress);
            ewsCreateAccount.setDateTimeStamp(xmlGregorianCalendar);

            EwsCompany ewsCompany = new EwsCompany();
            ewsCompany.setDBA(LegalName);
            ewsCompany.setEIN(ein);

            EwsLegalInfo ewsLegalInfo = new EwsLegalInfo();
            ewsLegalInfo.setLegalName(LegalName);
            ewsLegalInfo.setAddressLine1(Address);
            ewsLegalInfo.setCity(City);
            ewsLegalInfo.setState(State);
            ewsLegalInfo.setZip(Zip);
            ewsCompany.setLegalInfo(ewsLegalInfo);

            EwsAddress ewsAddress = new EwsAddress();
            ewsAddress.setAddressLine1(Address);
            ewsAddress.setCity(City);
            ewsAddress.setState(State);
            ewsAddress.setZip(Zip);
            ewsCompany.setMailingAddress(ewsAddress);

            EwsContact pa = new EwsContact();
            pa.setFirstName(FirstNamePA);
            pa.setLastName(LastNamePA);
            pa.setJobTitle(JobTitlePA);
            pa.setEMail(EMailPA);
            pa.setWorkPhone(WorkPhonePA);
            ewsCompany.setPayrollAdmin(pa);

            EwsContact pp = new EwsContact();
            pp.setFirstName(FirstNamePP);
            pp.setLastName(LastNamePP);
            pp.setJobTitle(JobTitlePP);
            pp.setEMail(EMailPP);
            pp.setWorkPhone(WorkPhonePP);
            ewsCompany.setPrimaryPrincipal(pp);

            EwsQuickBooks ewsQuickBooks = new EwsQuickBooks();
            ewsQuickBooks.setAppVersion(AppVersion);
            ewsQuickBooks.setLicenseNumber(LicenseNumber);
            ewsCompany.setQuickBooks(ewsQuickBooks);

            ewsCreateAccount.setCreateCompany(ewsCompany);

            EwsServices ewsServices = new EwsServices();
            ewsServices.setCloud(new EwsBaseService());

            EwsBankAccount ewsBankAccount = new EwsBankAccount();
            ewsBankAccount.setAccountNumber(AccountNumber);
            ewsBankAccount.setRoutingNumber(RoutingNumber);
            ewsBankAccount.setAccountType(EwsBankAccountType.valueOf(AccountType.toUpperCase()));
            ewsBankAccount.setBankName(BankName);
            ewsBankAccount.setCreateRandomDebits(true);
            ewsBankAccount.setQuickBooksName(QuickBooksName);

            EwsEntitlement ewsEntitlement = new EwsEntitlement();
            ewsEntitlement.setAddEin(false);
            ewsEntitlement.setBuyerEmailAddress("buyer@intuit.com");
            ewsEntitlement.setBillingAccountId("21");
            String entitlementLicenseNumber = ein + "Test";
            ewsEntitlement.setLicenseNumber(entitlementLicenseNumber);

            if(service.equals("dd")) {
                ewsEntitlement.setAssetItemNumber("1099581");
                ewsEntitlement.setEntitlementOfferingCode("DIY");
                ewsEntitlement.setEdition(EwsEditionType.ENHANCED);
                ewsEntitlement.setTier(EwsTierType.UNLIMITED);

                EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
                ewsDirectDepositService.setBankAccount(ewsBankAccount);
                ewsServices.setDirectDeposit(ewsDirectDepositService);
            } else if(service.equals("assisted")) {
                ewsEntitlement.setAssetItemNumber("1099734");
                ewsEntitlement.setEntitlementOfferingCode("Assisted");

                EwsAssistedService ewsAssistedService = new EwsAssistedService();
                ewsAssistedService.setBankAccount(ewsBankAccount);
                ewsServices.setAssisted(ewsAssistedService);
            /*
            } else if(service.equals("symphony basic")) {
                ewsEntitlement.setAssetItemNumber("1100520");
                ewsEntitlement.setEntitlementOfferingCode("389857");
                ewsEntitlement.setEdition(EwsEditionType.BASIC);
                //ewsEntitlement.setTier(EwsTierType.ONE);

                EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
                ewsDirectDepositService.setBankAccount(ewsBankAccount);
                ewsServices.setDirectDeposit(ewsDirectDepositService);
            } else if(service.equals("symphony enhanced")) {
                ewsEntitlement.setAssetItemNumber("1100520");
                ewsEntitlement.setEntitlementOfferingCode("389857");
                ewsEntitlement.setEdition(EwsEditionType.ENHANCED);
                //ewsEntitlement.setTier(EwsTierType.ONE);

                EwsDirectDepositService ewsDirectDepositService = new EwsDirectDepositService();
                ewsDirectDepositService.setBankAccount(ewsBankAccount);
                ewsServices.setDirectDeposit(ewsDirectDepositService);
            */
            } else if(service.equals("VMP")) {
                ewsEntitlement.setAssetItemNumber("1099581");
                ewsEntitlement.setEntitlementOfferingCode("DIY");
                ewsEntitlement.setEdition(EwsEditionType.ENHANCED);
                ewsEntitlement.setTier(EwsTierType.UNLIMITED);

                EwsBaseService ewsViewMyPaycheckService = new EwsBaseService();
                ewsServices.setViewMyPaycheck(ewsViewMyPaycheckService);
            }
            ewsCreateAccount.getEntitlement().add(ewsEntitlement);
            ewsCreateAccount.setServices(ewsServices);

            EwsCreateAccountResponse ewsCreateAccountResponse = TestAdapterAPI.getEwsAdapter().createAccount(ewsCreateAccount);
            EwsResponseStatus createAccountResponseStatus = ewsCreateAccountResponse.getResponseStatus();
            if(createAccountResponseStatus.getCode() != 0) {
                throw new RuntimeException("Error Creating Company - " + createAccountResponseStatus.getCode() + " " + createAccountResponseStatus.getMessage());
            }

            psid = ewsCreateAccountResponse.getPSID();
            serviceKey = ewsCreateAccountResponse.getEntitlementUnitResponses().get(0).getServiceKey();
            String agreementNumber = ewsCreateAccountResponse.getEntitlementUnitResponses().get(0).getEntitlementResponse().getSubscriptionNumber();

            EwsBasePin ewsBasePin = new EwsBasePin();
            ewsBasePin.setIpAddress(ipAddress);
            ewsBasePin.setDateTimeStamp(xmlGregorianCalendar);
            ewsBasePin.setPin(pin);
            ewsBasePin.setPSID(psid);
            EwsBasePinResponse ewsBasePinResponse = TestAdapterAPI.getEwsAdapter().createPin(ewsBasePin);
            EwsResponseStatus createPinResponseStatus = ewsBasePinResponse.getResponseStatus();
            if(createPinResponseStatus.getCode() != 0) {
                throw new RuntimeException("Error Creating Company PIN - " + createPinResponseStatus.getCode() + " " + createPinResponseStatus.getMessage());
            }

            TestAdapterAPI.getEntitlementWS().activateEntitlementUnit(psid, entitlementLicenseNumber);
            if (service.equals("assisted") || service.equals("dd")) {
                TestAdapterAPI.getCompanyWS().activateBankAccount("QBDT", psid);
            }

            if(service.equals("assisted")) {
                TestAdapterAPI.getCompanyWS().activateAssistedCompany(psid, agreementNumber);
                TestAdapterAPI.getCompanyWS().updateServiceStatus("QBDT", psid, "Tax", "PendingBalanceFile");
            }
        }catch(java.lang.Throwable ex){
            errorMsg = "Error: " + ex.getMessage() + "\n";

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            ex.printStackTrace(printWriter);
            stackTrace = stringWriter.toString();
        }
    }

    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("stackTrace", stackTrace);

    request.setAttribute("psid", psid);
    request.setAttribute("serviceKey", serviceKey);
    request.setAttribute("companyName", LegalName);
    request.setAttribute("ein", ein);
%>
<html>
<head>
    <title>Create Default Company Submit</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
</head>
<body>
<table border=0>
    <tr>
        <td valign="top">
            <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
            <%@ include file="TestCompanyHeader.jsp" %>
            <c:choose>
                <c:when test="${errorMsg != null}">
                    <h3>Company Not Created</h3>
                    <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
                    <textarea cols="125" rows="25" disabled="disabled" style="background-color: #ffffff;"><c:out value="${stackTrace}"/></textarea>
                </c:when>
                <c:otherwise>
                    <h3>Company Created</h3>
                    <table>
                        <tr><td><b>Company Name:</b></td><td><c:out value="${companyName}"/></td></tr>
                        <tr><td><b>Service Key:</b></td><td><c:out value="${serviceKey}"/></td></tr>
                        <tr><td><b>PSID:</b></td><td><c:out value="${psid}"/></td></tr>
                        <tr><td><b>EIN:</b></td><td><c:out value="${ein}"/></td></tr>
                    </table>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>

</body>
</html>
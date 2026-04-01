<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<html>
<head>
    <title>Create Default Company</title>
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
            <form action="TestCreateDefaultCompany_Submit.jsp" method="POST">
                <div>
                    <fieldset>
                        <legend>Required Information *</legend>
                        <table>
                            <tr><td><label for="ein">EIN</label></td><td><input id="ein" name="ein" type="text" maxlength="9"/></td></tr>
                            <tr><td><label for="LegalName">Legal Name</label></td><td><input id="LegalName" name="LegalName" type="text" /></td></tr>
                            <tr><td><label for="pin">PIN</label></td><td><input id="pin" name="pin" value="test1234" /></td></tr>
                        </table>
                        <b>Service</b><br/>
                        <table>
                            <tr>
                                <td>
                                    <label>
                                        <input type="radio" name="service" value="assisted"/>
                                        Assisted
                                    </label>
                                    <br/>
                                    <label>
                                        <input type="radio" name="service" value="dd"/>
                                        Direct Deposit
                                    </label>
                                    <br/>
                                    <label>
                                        <input type="radio" name="service" value="VMP"/>
                                        VMP
                                    </label>
                                    <br/>
                                </td>
                                <td valign="bottom" align="right">
                                    <input type="submit" value="Submit"/>
                                </td>
                            </tr>
                        </table>
                    </fieldset>
                </div>
                <div>
                    <fieldset>
                        <legend>Company Information</legend>
                        <table>
                            <tr><td><label for="AppVersion">App Version</label></td><td><input id="AppVersion" name="AppVersion" value="32.00.R.1/22204#pro" /></td></tr>
                            <tr><td><label for="LicenseNumber">License Number</label></td><td><input id="LicenseNumber" name="LicenseNumber" value="6487-4844-4441-476" /></td></tr>
                        </table>
                        <br/>
                        <b>Address</b>
                        <table>
                            <tr><td><label for="Address">Address</label></td><td><input id="Address" name="Address" type="text" value="6888 Sierra Center Parkway"/></td></tr>
                            <tr><td><label for="City">City</label></td><td><input id="City" name="City" type="text" value="Reno"/></td></tr>
                            <tr><td><label for="State">State</label></td><td><input id="State" name="State" value="NV" /></td></tr>
                            <tr><td><label for="Zip">Zip</label></td><td><input id="Zip" name="Zip" value="89511" /></td></tr>
                        </table>
                    </fieldset>
                </div>
                <div>
                    <fieldset>
                        <legend>Contacts</legend>
                        <b>Payroll Admin</b>
                        <table>
                            <tr><td><label for="FirstNamePA">First Name</label></td><td><input id="FirstNamePA" name="FirstNamePA" type="text" value="Payroll"/></td></tr>
                            <tr><td><label for="LastNamePA">Last Name</label></td><td><input id="LastNamePA" name="LastNamePA" type="text" value="Admin"/></td></tr>
                            <tr><td><label for="JobTitlePA">Job Title</label></td><td><input id="JobTitlePA" name="JobTitlePA" type="text" value="Payroll Admin"/></td></tr>
                            <tr><td><label for="EMailPA">EMail</label></td><td><input id="EMailPA" name="EMailPA" type="text" value="payroll_admin@intuit.com"/></td></tr>
                            <tr><td><label for="WorkPhonePA">Work Phone</label></td><td><input id="WorkPhonePA" name="WorkPhonePA" type="text" value="999-999-9999"/></td></tr>
                        </table>
                        <br/>
                        <b>Primary Principal</b>
                        <table>
                            <tr><td><label for="FirstNamePP">First Name</label></td><td><input id="FirstNamePP" name="FirstNamePP" type="text" value="Primary"/></td></tr>
                            <tr><td><label for="LastNamePP">Last Name</label></td><td><input id="LastNamePP" name="LastNamePP" type="text" value="Principal"/></td></tr>
                            <tr><td><label for="JobTitlePP">Job Title</label></td><td><input id="JobTitlePP" name="JobTitlePP" type="text" value="Primary Principal"/></td></tr>
                            <tr><td><label for="EMailPP">EMail</label></td><td><input id="EMailPP" name="EMailPP" type="text" value="primary_principal@intuit.com"/></td></tr>
                            <tr><td><label for="WorkPhonePP">Work Phone</label></td><td><input id="WorkPhonePP" name="WorkPhonePP" type="text" value="999-999-9999"/></td></tr>
                        </table>
                    </fieldset>
                </div>
                <div>
                    <fieldset>
                        <legend>Bank Account</legend>
                        <table>
                            <tr><td><label for="BankName">Bank Name</label></td><td><input id="BankName" name="BankName" type="text" value="Bank of Intuit"/></td></tr>
                            <tr><td><label for="AccountNumber">Account Number</label></td><td><input id="AccountNumber" name="AccountNumber" type="text" value="12345-12345"/></td></tr>
                            <tr><td><label for="RoutingNumber">Routing Number</label></td><td><input id="RoutingNumber" name="RoutingNumber" type="text" value="111000025"/></td></tr>
                            <tr><td><label for="QuickBooksName">QuickBooks COA</label></td><td><input id="QuickBooksName" name="QuickBooksName" type="text" value="BOFI"/></td></tr>
                            <tr><td><label for="AccountType">Account Type</label></td><td><input id="AccountType" name="AccountType" type="text" value="Checking"/></td></tr>
                        </table>
                    </fieldset>
                </div>
            </form>
        </td>
    </tr>
</table>
</body>
</html>
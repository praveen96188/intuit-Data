<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.TreeMap" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%
    TreeMap bankReturnCodes = new TreeMap();
    bankReturnCodes.put("R01", "Insufficient Funds");
    bankReturnCodes.put("R02", "Account Closed");
    bankReturnCodes.put("R03", "No Account/Unable to Locate Account");
    bankReturnCodes.put("R04", "Invalid Account Number");
    bankReturnCodes.put("R05", "Unauthorized Debit to Consumer Account Using Corporate SEC Code");
    bankReturnCodes.put("R06", "Returned per ODFIs Request");
    bankReturnCodes.put("R07", "Authorization Revoked by Customer");
    bankReturnCodes.put("R08", "Payment Stopped");
    bankReturnCodes.put("R09", "Uncollected Funds");
    bankReturnCodes.put("R09", "Uncollected Funds");
    bankReturnCodes.put("R10", "Customer Advises Not Authorized, Notice Not Provided, Improper Source Document, or Amount of Entry Not Accurately Obtained from Source Document");
    bankReturnCodes.put("R11", "Check Truncation Entry Return");
    bankReturnCodes.put("R12", "Account Sold to Another DFI");
    bankReturnCodes.put("R13", "RDFI Not Qualified to Participate");
    bankReturnCodes.put("R14", "Representative Payee Deceased or Unable to Continue in that Capacity");
    bankReturnCodes.put("R15", "Beneficiary or Account Holder (Other Than a Representative Payee) Deceased");
    bankReturnCodes.put("R16", "Account Frozen");
    bankReturnCodes.put("R17", "File Record Edit Criteria");
    bankReturnCodes.put("R18", "Improper Effective Entry Date");
    bankReturnCodes.put("R19", "Amount Field Error");
    bankReturnCodes.put("R20", "Non-Transaction Account");
    bankReturnCodes.put("R21", "Invalid Company Identification");
    bankReturnCodes.put("R22", "Invalid Individual ID Number");
    bankReturnCodes.put("R23", "Credit Entry Refused by Receiver");
    bankReturnCodes.put("R24", "Duplicate Entry");
    bankReturnCodes.put("R25", "Addenda Error");
    bankReturnCodes.put("R26", "Mandatory Field Error");
    bankReturnCodes.put("R27", "Trace Number Error");
    bankReturnCodes.put("R28", "Routing Number Check Digit Error");
    bankReturnCodes.put("R29", "Corporate Customer Advises Not Authorized");
    bankReturnCodes.put("R30", "RDFI Not Participant in Check Truncation Program");
    bankReturnCodes.put("R31", "Permissible Return Entry");
    bankReturnCodes.put("R32", "RDFI Non-Settlement R34 Limited Participation DFI");
    bankReturnCodes.put("R33", "Return of XCK Entry");
    bankReturnCodes.put("R35", "Return of Improper Debit Entry");
    bankReturnCodes.put("R36", "Return of Improper Credit Entry");
    bankReturnCodes.put("R37", "Source Document Presented for Payment");
    bankReturnCodes.put("R38", "Stop Payment on Source Document");
    bankReturnCodes.put("R39", "Improper Source Document");
    bankReturnCodes.put("R40", "Return of ENR Entry by Federal Government Agency");
    bankReturnCodes.put("R41", "Invalid Transaction Code");
    bankReturnCodes.put("R42", "Routing Number/Check Digit Error");
    bankReturnCodes.put("R43", "Invalid DFI Account Number");
    bankReturnCodes.put("R44", "Invalid Individual ID Number/Identification Number");
    bankReturnCodes.put("R45", "Invalid Individual Name/Company Name");
    bankReturnCodes.put("R46", "Invalid Representative Payee Indicator");
    bankReturnCodes.put("R47", "Duplicate Enrollment");
    bankReturnCodes.put("R50", "State Law Affecting RCK Acceptance");
    bankReturnCodes.put("R51", "Item is Ineligible, Notice Not Provided, Signature Not Genuine, Item Altered, or Amount of Entry Not Accurately Obtained from Item");
    bankReturnCodes.put("R52", "Stop Payment on Item");
    bankReturnCodes.put("R53", "Item and ACH Entry Presented for Payment");
    bankReturnCodes.put("R61", "Misrouted Return");
    bankReturnCodes.put("R62", "Incorrect Trace Number");
    bankReturnCodes.put("R63", "Incorrect Dollar Amount");
    bankReturnCodes.put("R64", "Incorrect Individual Identification");
    bankReturnCodes.put("R65", "Incorrect Transaction Code");
    bankReturnCodes.put("R66", "Incorrect Company Identification");
    bankReturnCodes.put("R67", "Duplicate Return");
    bankReturnCodes.put("R68", "Untimely Return");
    bankReturnCodes.put("R69", "Multiple Errors [Field Errors(s)]");
    bankReturnCodes.put("R70", "Permissible Return Entry Not Accepted");
    bankReturnCodes.put("R71", "Misrouted Dishonored Return");
    bankReturnCodes.put("R72", "Untimely Dishonored Return");
    bankReturnCodes.put("R73", "Timely Original Return");
    bankReturnCodes.put("R74", "Corrected Return");
    bankReturnCodes.put("R75", "Original Return Not a Duplicate");
    bankReturnCodes.put("R76", "No Errors Found");
    bankReturnCodes.put("R80", "Cross-Border Payment Coding Error");
    bankReturnCodes.put("R81", "Non-Participant in Cross-Border Program");
    bankReturnCodes.put("R82", "Invalid Foreign Receiving DFI Identification");
    bankReturnCodes.put("R83", "Foreign Receiving DFI Unable to Settle");
    bankReturnCodes.put("R84", "Entry Not Processed by OGO");
    
    bankReturnCodes.put("C01", "Incorrect DFI Account Number");
    bankReturnCodes.put("C02", "Incorrect Routing Number");
    bankReturnCodes.put("C03", "Incorrect Routing Number and Incorrect DFI Account Number");
    bankReturnCodes.put("C04", "Incorrect Individual Name/Receiving Company Name");
    bankReturnCodes.put("C05", "Incorrect Transaction Code");
    bankReturnCodes.put("C06", "Incorrect DFI Account Number and Incorrect Transaction Code");
    bankReturnCodes.put("C07", "Incorrect Routing Number, Incorrect DFI Account Number, and Incorrect Transaction Code");
    bankReturnCodes.put("C08", "Incorrect Foreign Receiving DFI Identification");
    bankReturnCodes.put("C09", "Incorrect Individual Identification Number");
    bankReturnCodes.put("C13", "Addenda Format Error");
    bankReturnCodes.put("C61", "Misrouted Notification of Change");
    bankReturnCodes.put("C62", "Incorrect Trace Number");
    bankReturnCodes.put("C63", "Incorrect Company Identification Number");
    bankReturnCodes.put("C64", "Incorrect Individual Identification Number/Identification Number");
    bankReturnCodes.put("C65", "Incorrectly Formatted Corrected Data");
    bankReturnCodes.put("C66", "Incorrect Discretionary Data");
    bankReturnCodes.put("C67", "Routing Number Not From Original Entry Detail Record");
    bankReturnCodes.put("C68", "DFI Account Number Not From Original Entry Detail Record");
    bankReturnCodes.put("C69", "Incorrect Transaction Code");

    request.setAttribute("returnCodes", bankReturnCodes);
%>
<html>
  <head>
    <title>Bank Return Codes</title>
  	<link rel="stylesheet" href="css/Styles.css" type="text/css">  
  </head>
  <body>
       <table border="1" cellspacing="0" cellpadding="3">
            <tr valign="top" bgcolor="#dddddd">
                <td>
                    <b>Code</b>
                </td>
                <td>
                    <b>Description</b>
                </td>
            </tr>
    <c:forEach var="retCode" items="${returnCodes}" varStatus="status">
        <c:choose>
            <c:when test="${(status.count % 2) == 0}">
                <c:set var="color" value="#eeeeee" />
            </c:when>
            <c:otherwise>
                <c:set var="color" value="#ffffff" />
            </c:otherwise>
        </c:choose>
            <tr bgcolor='<c:out value="${color}"/>' >
                <td>
                    <c:out value="${retCode.key}"/>
                </td>
                <td>
                    <c:out value="${retCode.value}"/>
                </td>
            </tr>
    </c:forEach>
       </table>  
  </body>
</html>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyWSDTO" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
	String errorMsg = null;
    String action = request.getParameter("action");
    String companyId = request.getParameter("companyId");
    String legalName = request.getParameter("legalName");

    try {
        if (action != null && action.equals("search")) {
          List<CompanyWSDTO> companies = TestAdapterAPI.getCompanyWS().queryCompaniesByLegalName(companyId, legalName, false, false, false, false, true);
          request.setAttribute("companyResult", companies);
        }
    } catch (Exception e) {
		errorMsg = e.getMessage();
	}

	request.setAttribute("errorMsg", errorMsg);
%>


<html>
  <head>
    <title>Company list</title>
 	<link rel="stylesheet" href="css/Styles.css" type="text/css">
    <script language="javascript">
	function whichButtonPressed(whichButton) {
		var companyIdElement = document.getElementById('companyId');
		var companyNameElement = document.getElementById('legalName');        
        if(whichButton == 'CompanyID'){
            if(companyIdElement != null){
                if(companyIdElement.value == ""){
                    alert("Enter Company ID");
                    return false;
                }
                companyNameElement.value ="";
            }
        }else if(whichButton == 'LegalName'){
            if(companyNameElement != null){
                if(companyNameElement.value == ""){
                    alert("Enter Company Legal Name");
                    return false;
                }
            }
            companyIdElement.value = "";
        }

        document.forms[0].submit();
    }
    </script>
  </head>
  <body>
  <form action="index.jsp" method="POST">
    <input type="hidden" name="action" value="search"/>  
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3>Company Search</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <table border="0">
                    <tr>
                        <td>Company ID:</td>
                        <td><input type="text" name="companyId" maxlength="50" size="25"
                                   value="" onkeypress="toUpperCase();"></td>
                        <td>&nbsp;</td>
                        <td><button type="button" onClick="whichButtonPressed('CompanyID')">Search</button></td>
                    </tr>
                    <tr>
                        <td>Company Legal Name:</td>
                        <td><input type="text" name="legalName" maxlength="100" size="25"
                                   value="" onkeypress="toUpperCase();"></td>
                        <td>&nbsp;</td>
                        <td><button type="button" onClick="whichButtonPressed('LegalName')">Search</button></td>
                    </tr>
            </table>
            <br/>
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Company<br/>PSP ID</b>
                </th>
                <th><b>Source System</b></th>
                <th>
	                <b>Company<br/>Source ID</b>
                </th>
                <th><b>Legal Name</b>
                </th>
                <th><b>DD Status</b></th>
                <th><b>On Hold Reasons</b></th>
                <th><b>Offload Group</b>
                </th>
              </tr>
                <c:forEach var="company" items="${companyResult}" varStatus="status">
                    <c:choose>
                        <c:when test="${(status.count % 2) == 0}">
                            <c:set var="color" value="#eeeeee" />
                        </c:when>
                        <c:otherwise>
                             <c:set var="color" value="#ffffff" />
                        </c:otherwise>
                    </c:choose>

                    <tr bgcolor='<c:out value="${color}"/>' >
                        <td align="right"><c:out value="${company.pspCompanyID}"/></td>
                        <td>
                            <c:out value="${company.sourceSystemCD}"/>
                        </td>
                        <td>
							<a href='TestSelectCompany.jsp?companyGseq=<c:out value="${company.pspCompanyID}"/>'><c:out value="${company.sourceCompanyID}"/></a>
                        </td>
                        <td align="left"><c:out value="${company.legalName}"/>&nbsp;</td>
                        <td>
                            <c:out value="${company.ddStatus}"/>
                        </td>
                        <td>
                            <table border=0>
                            <c:forEach var="onHoldReason" items="${company.onHoldReasons}" varStatus="status">
                                <tr>
                                    <td><c:out value="${onHoldReason.onHoldReasonCd}"/></td>
                                </tr>
                            </c:forEach>
                            </table>
                        </td>
                        <td align="left">
                            <c:out value="${company.offloadGroup}"/>
                        </td>
                    </tr>
                </c:forEach>
            </table>
        </td>
      </tr>
    </table>
      </form>
  </body>
</html>


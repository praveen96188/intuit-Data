<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.OffloadGroupWSDTO" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    try{
        List<OffloadGroupWSDTO> offloadGroups = TestAdapterAPI.getOffloadGroupWS().queryOffloadGroups();
        request.setAttribute("offloadGroupResult", offloadGroups);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }                                              
    request.setAttribute("errorMsg", errorMsg);
%>

<html>
  <head>
    <title>Offload Groups</title>
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
            <h3>Offload Groups</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <a href="TestAddOffloadGroup.jsp">Add Offload Group</a><br/><br/>
            <table border=1>
              <tr valign="top" bgcolor="#dddddd">
                <th><b>Offload<br/>Group CD</b></th>
                <th><b>Name</b></th>
                <th><b>Description</b></th>
                <th><b>Cutoff Time</b></th>
              </tr>
                <c:forEach var="offload" items="${offloadGroupResult}" varStatus="status">
                    <c:choose>
                        <c:when test="${(status.count % 2) == 0}">
                            <c:set var="color" value="#eeeeee" />
                        </c:when>
                        <c:otherwise>
                             <c:set var="color" value="#ffffff" />
                        </c:otherwise>
                    </c:choose>
                    <tr bgcolor='<c:out value="${color}"/>' >
		              <td align="center">
                        <c:out value="${offload.offloadGroupCd}"/>
                      </td>
                      <td align="left">
                        <c:out value="${offload.name}"/>
                      </td>
                      <td align="left">
                        <c:out value="${offload.description}"/>
                      </td>
                      <td align="center">
                          <c:out value="${offload.cutoffTime}"/>
                      </td>
                    </tr>                    
                </c:forEach>
            </table>
        </td>
      </tr>
    </table>
  </body>
</html>
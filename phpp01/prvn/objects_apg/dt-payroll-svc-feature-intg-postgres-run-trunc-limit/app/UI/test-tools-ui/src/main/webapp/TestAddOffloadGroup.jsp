<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%
    String errorMsg = null;
    String offloadGroupCd = request.getParameter("offloadGroupCd");
    String offloadCutoffTime = request.getParameter("offloadCutoffTime");
    String action = request.getParameter("action");

    if (action != null && action.equals("add")) {
        try{
            TestAdapterAPI.getOffloadGroupWS().add(offloadGroupCd, offloadCutoffTime);
        }catch(Exception ex){
            errorMsg = ex.getMessage();
        }
    }

    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("offloadGroupCd", offloadGroupCd == null ? "" : offloadGroupCd);
    request.setAttribute("offloadCutoffTime", offloadCutoffTime == null ? "" : offloadCutoffTime);    
%>

<html>
  <head>
      <title>Add Offload Group</title>
      <link rel="stylesheet" href="css/Styles.css" type="text/css">
      <script language="javascript" src="scripts/Scripts.js"></script>
  </head>
  <body>
  <form action="TestAddOffloadGroup.jsp" method="POST">
    <input type="hidden" name="action" value="add"/>
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3>Add Offload Group</h3>
            <c:if test="${errorMsg != null}">
                <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
                <table border="0">
                    <tr>
                        <td>Offload Group CD:</td>
                        <td><input type="text" name="offloadGroupCd" maxlength="10" size="10"
                                   value="<c:out value="${offloadGroupCd}"/>" onkeypress="toUpperCase();"></td>
                    </tr>
                    <tr>
                        <td>Cutoff Time:</td>
                        <td><input type="text" name="offloadCutoffTime" maxlength="8" size="8"
                                   value="<c:out value="${offloadCutoffTime}"/>"
                                   onkeypress="filterInvalidChars('[0-9,:]')">(ex. 15:34:00) </td>
                    </tr>
                    <tr>
                        <td>&nbsp;</td>
                        <td><button type="submit">Submit</button></td>
                    </tr>
                </table>  
        </td>
      </tr>
    </table>
  </form>
  </body>
</html>
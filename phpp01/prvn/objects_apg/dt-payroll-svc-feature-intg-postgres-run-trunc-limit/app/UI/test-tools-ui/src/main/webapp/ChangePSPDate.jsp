<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    String action = request.getParameter("action");
    System.out.println("Time action=" + action);
    String pspDate = request.getParameter("pspDate");
    try{
        if("Update".equals(action)){
            System.out.println("Time change started");
            TestAdapterAPI.getPSPDateWS().set(pspDate);
            System.out.println("Time change successful");
        } else if("Reset".equals(action)) {
            System.out.println("Time reset started");
            TestAdapterAPI.getPSPDateWS().reset();
            System.out.println("Time reset successful");
        }
    }catch(Exception ex){
        errorMsg = ex.getMessage();
        System.out.println("Exception occurred in change/reset time "+ errorMsg + " ,action = "+action);
    }

    request.setAttribute("errorMsg", errorMsg);
    request.setAttribute("pspDate", pspDate);
    request.setAttribute("action", action);
%>

<html>
  <head><title>Simple jsp page</title></head>
  <link rel="stylesheet" href="css/Styles.css" type="text/css">
  <script language="javascript" src="scripts/Scripts.js"></script>
    <body>
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <form action="ChangePSPDate.jsp" method="POST">
                <h3>Change PSP Date</h3>
                <c:if test="${action !=null && errorMsg != null}">
                    <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
                </c:if>
                <table border=0>
                  <tr valign="top">
                    <td><b>PSP Date:</b></td>
                    <td><input type="text" id="pspDate" name="pspDate" maxlength="14" size="14" value=""
                               onkeypress="filterInvalidChars('[0-9:]')"> (Expected format: YYYYMMDDHHMMSS)
                    </td>
                  </tr>
                </table>
                <input type="submit" name="action" value="Update"/>
                <br/><br/>
                <span style='color:red'>Note: Changing the PSP Date will effect everyone testing in this environment. </span><br/>
                <br/>
                <table border=0>
                    <tr>
                        <h3>Reset PSP Date</h3>
                        <c:if test="${action !=null && errorMsg != null}">
                            <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
                        </c:if>
                        <input type="submit" name="action" value="Reset" />
                        <br/><br/>
                        <span style='color:red'>Note: Resetting the PSP Date will effect everyone testing in this environment. </span><br/>
                    </tr>
                </table>
            </form>
        </td>
      </tr>
    </table>

  </body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    boolean txnInserted = false;
    String onHoldReasonCd = request.getParameter("onHoldReasonCd");
    String onHoldReasonGseq = request.getParameter("onHoldReasonId");
    String serviceStatusCD = request.getParameter("serviceStatusCD");
    String action = request.getParameter("action");
    String selectedCompanyGseq = getCompanyGseq(request);
    System.out.println("onHoldReasonCd " + onHoldReasonCd);
    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        if(action != null && action.equals("add")){
            TestAdapterAPI.getCompanyWS().addOnHoldReason(selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID(),onHoldReasonCd);
            txnInserted = true;
        }else if(action != null && action.equals("delete")){
            TestAdapterAPI.getCompanyWS().removeOnHoldReason(selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID(),onHoldReasonCd);
            txnInserted = true;
        }else if(action != null && action.equals("updateServiceStatus")){
            TestAdapterAPI.getCompanyWS().updateServiceStatus(selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID(),"DirectDeposit", serviceStatusCD);
            txnInserted = true;
        }
        response.sendRedirect("TestEditCompanyStatuses.jsp");
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("txnInserted", txnInserted);
%>
<html>
  <head>
    <title>On Hold Reason</title>
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
           <div>
               <c:if test="${not txnInserted}">
             <h3>Failed to create transaction</h3>
               </c:if>
             <span style='color:red'><%=errorMsg%></span><br/><br/>
             <button onClick='javascript:history.go(-1);'>Back</button>
           </div>
        </td>
      </tr>
    </table>
  </body>
</html>
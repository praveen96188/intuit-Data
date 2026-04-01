<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>
<%
    String errorMsg = null;
    String selectedCompanyGseq = getCompanyGseq(request);
    String fundingModel = request.getParameter("fundingModel");
    try{
        CompanyWSDTO selectedCompany = TestAdapterAPI.getCompanyWS().queryCompany(selectedCompanyGseq,false, false, false);
        if(fundingModel != null && fundingModel.length() > 0){
            errorMsg = TestAdapterAPI.getCompanyWS().updateFundingModel(selectedCompany.getSourceSystemCD(), selectedCompany.getSourceCompanyID(), fundingModel);
        }else{

            fundingModel = selectedCompany.getFundingModel();
        }
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("fundingModel", fundingModel);
    request.setAttribute("errorMsg", errorMsg);
%>
<html>
  <head>
    <title>Edit Funding Model</title>
    <link rel="stylesheet" href="css/Styles.css" type="text/css">
  </head>
  <body>
  <form action="TestEditFundingModel.jsp" method="POST">
    <table border=0>
      <tr>
        <td valign="top">
           <%@ include file="TestToolsMenu.jsp" %>
        </td>
        <td valign="top">
           <%@ include file="TestCompanyHeader.jsp" %>
            <h3 class="pageTitle">Change Funding Model</h3>
          <br>
          Funding Model:
          &nbsp;&nbsp;
          <select name="fundingModel">
            <option value="2D" <c:if test="${fundingModel == '2D'}">selected</c:if> >2-day</option>
            <option value="5D" <c:if test="${fundingModel == '5D'}">selected</c:if> >5-day</option>
          </select>
          &nbsp;&nbsp;&nbsp;
          <button type="submit">Update</button>
          <br/><br/>
          <c:if test="${errorMsg != null}">
            <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
          </c:if>
          <br/>
        </td>
      </tr>
    </table>
    </form>  
  </body>
</html>
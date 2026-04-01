
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.BankReturnWSDTO" %>
<%@ page import="java.util.*" %>
<%@page import="org.owasp.encoder.Encode"%>
<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>

<%! int bankReturnTxnCount = 0; %>
<%
    String errorMsg = null;
    Enumeration paramNamesEnumeration = request.getParameterNames();
    Map paramMap = request.getParameterMap();
    List keyList = Collections.list(paramNamesEnumeration);
    Collections.sort(keyList);
    Enumeration sortedParamNamesEnumeration = Collections.enumeration(keyList);
    String paramName;
    List<BankReturnWSDTO> bankReturnDTOs=new ArrayList<BankReturnWSDTO>();
    BankReturnWSDTO bankReturnDTO = null;
    while (sortedParamNamesEnumeration.hasMoreElements()) {
        paramName = (String) sortedParamNamesEnumeration.nextElement();
        String paramValue = request.getParameter(paramName);
        //out.println(paramName + " =  " + paramValue + "<br>"); 	//uncomment to debug
        if (paramName.indexOf(".returnCode") != -1 && paramValue != null && !paramValue.equals("")) {
            String tempString1 = paramName.substring(10, paramName.length());
            //out.println(tempString1 + "<br>");			//uncomment to debug
            int startPos = tempString1.indexOf(".returnCode");
            String mmTxnId = tempString1.substring(0, startPos);
            //out.println("MoneyMovement Txn = " + mmTxnId + "<br>");	//uncomment to debug

            String nachaCode = paramValue;
            String routingNumber = ((String[])paramMap.get("achtxnseq." + mmTxnId + ".routingNumber"))[0];
            String accountNumber = ((String[])paramMap.get("achtxnseq." + mmTxnId + ".accountNumber"))[0];
            String accountType = ((String[])paramMap.get("achtxnseq." + mmTxnId + ".accountType"))[0];
            bankReturnDTO = new BankReturnWSDTO();
            bankReturnDTO.setBankReturnCd(nachaCode);
            bankReturnDTO.setRoutingNumber(routingNumber);
            bankReturnDTO.setAccountNumber(accountNumber);
            bankReturnDTO.setAccountType(accountType);
            bankReturnDTO.setTransactionId(mmTxnId.substring(0,mmTxnId.length()-2));
            bankReturnDTOs.add(bankReturnDTO);
        }
    }

    try{
        TestAdapterAPI.getBatchJobsWS().createBankReturnsForMoneyMovementTransactions(bankReturnDTOs);
    }catch(Exception ex){
        errorMsg = ex.getMessage();
    }
    request.setAttribute("errorMsg" , errorMsg);
%>

<html>
  <head>
    <title>Bank Simulator</title>
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
            <c:if test="${errorMsg != null}">
              <span style='color:red'><c:out value="${errorMsg}"/></span><br/>
            </c:if>
            <h3><% out.println(bankReturnDTOs.size()); %>Bank Returns Created</h3>

            <%-- display the Bank Returns the Bank Simulator created --%>
            <%
  		    // sort the enumeration to make it easier to view when debugging
     		  Collections.sort(keyList);
		      Enumeration sortedParamNamesEnumeration2 = Collections.enumeration(keyList);

              String paramName2;

              while (sortedParamNamesEnumeration2.hasMoreElements()) {
                paramName2 = (String) sortedParamNamesEnumeration2.nextElement();
                String paramValue2 = request.getParameter(paramName2);

                if (paramName2.indexOf(".returnCode") != -1) {
                  String tempString1 = paramName2.substring(10, paramName2.length());
                  int startPos = tempString1.indexOf(".returnCode");
                  String achtxnseq = tempString1.substring(0, startPos);

                  // only process txns from Bank Simulator that were assigned nachacode bank return codes by the Bank Simulator.
                  if (paramValue2 != null && !paramValue2.equals("")) {
                         //display the Bank Returns the Bank Simulator created
                      out.println(Encode.forHtml(paramName2) + " =  " + Encode.forHtml(paramValue2) + "<br>");
                  }
                }
              }
            %>
           </div>
        </td>
      </tr>
    </table>  
  </body>
</html>
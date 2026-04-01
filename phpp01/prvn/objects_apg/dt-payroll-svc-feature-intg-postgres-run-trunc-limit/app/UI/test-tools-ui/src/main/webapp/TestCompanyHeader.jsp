<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyWSDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="org.owasp.encoder.Encode"%>

<div style="background-color:#eeeeee;padding:10 10 10 10;margin:0 0 20 0">
    <%
        String pspCompanyID = getCompanyGseq(request);
        if (pspCompanyID != null && !pspCompanyID.equals("")) {

            CompanyWSDTO companyDTO = TestAdapterAPI.getCompanyWS().queryCompany(pspCompanyID,false, false, false);

            if (companyDTO != null) {
                out.println("<b>Company: id=[" + Encode.forHtml(companyDTO.getSourceCompanyID()) + "]," +
                        " pspID=[" +  Encode.forHtml(companyDTO.getPspCompanyID()) + "]," +
                        " legal_name=[" + Encode.forHtml(companyDTO.getLegalName()) + "]</b>");
            }
            else {
                out.println("Company not found for PSP Company ID = " + Encode.forHtml(pspCompanyID));
            }
        }
        else {
            out.println("Company is not selected");
        }
    %>
    <br/>
    <b>PSPDate = <%=new SimpleDateFormat().format(TestAdapterAPI.getPSPDateWS().get().toGregorianCalendar().getTime())%></b> (<a href="ChangePSPDate.jsp" class="activeLink">change/reset</a>)<br>
    <br>
</div>
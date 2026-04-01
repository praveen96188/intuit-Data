<%@page import="com.intuit.sbd.payroll.psp.ach.security.Encryption"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="com.intuit.sbd.payroll.psp.ach.*"%>
<%@page import="java.util.Hashtable"%>
<%@page import="java.math.BigDecimal"%>
<%@page import="java.util.Enumeration"%>
<%@page import="com.intuit.sbd.payroll.psp.ach.RecordId"%>
<%@page import="com.intuit.sbd.payroll.psp.ach.util.Helper"%>
<%@page import="com.intuit.sbd.payroll.psp.ach.util.Request"%>
<%@ page import="java.io.*" %>
<%@ page import="com.intuit.sbd.payroll.psp.ach.fixedlen.*" %>
<%@ page import="com.intuit.sbd.payroll.psp.webservices.client.CompanyWSDTO" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="org.owasp.encoder.Encode"%>

<%@ taglib uri="c.tld" prefix="c" %>
<%@ taglib uri="fmt.tld" prefix="fmt" %>

<%@ include file="Cookies.jsp" %>


<jsp:useBean id="pool" scope="page" class="com.intuit.sbd.payroll.psp.ach.util.XmlResourcePool"/>

<style type="text/css">
    pre {
        font-size: 10;
        FONT-FAMILY: lucida console, courier;
    }
    TABLE.ach {
        font-family: arial, helvetica, sans-serif;
        background-color: black;
    }

    TH.ach {
        text-align: left;
        color: white;
        font-size: 10px;
        background-color: gray;
    }

    TD.ach {
        background-color: white;
        font-size: 10px;
    }
</style>

<html>
<head>
    <title>Ach Analyser</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <link rel="stylesheet" href="css/Styles.css" type="text/css">

</head>
<table border=0>
<tr>
<td valign="top">
    <%@ include file="TestToolsMenu.jsp" %>
</td>
<td valign="top">
<%@ include file="TestCompanyHeader.jsp" %>
<%
    Request req = new Request (request);
    java.io.File achfile = req.getFile ("achfile");
    String srcpath = req.getParameter ("srcpath");
    String sendMethod = req.getParameter ("sendMethod");

    String modemMethod = "";
    String ftpMethod = "";
    boolean analyzeChase =  false;
%>

<%
    // Initialize common resources
    if (!analyzeChase) // default to boa
    {
        //sendMethod = "HTTPS" || "SFTP";
        pool.add (this.getServletConfig().getServletContext().getRealPath("/")+ "ach/boaDefinitions.xml");
    }
    else
    {
        if (Helper.isAchFtpEnabled()) {
            if (sendMethod != null && sendMethod.equals("modem")) {
                pool.add ( this.getServletConfig().getServletContext().getRealPath("/")+ "ach/definitions.xml");
                sendMethod = "MODEM";
                modemMethod = "checked";
            } else {
                pool.add (this.getServletConfig().getServletContext().getRealPath("/")+"ach/ftpDefinitions.xml");
                sendMethod = "FTP";
                ftpMethod = "checked";
            }
        } else {
            pool.add (this.getServletConfig().getServletContext().getRealPath("/")+ "ach/definitions.xml");
            sendMethod = "MODEM";
            modemMethod = "checked";
        }
    }
%>


<form name="path" action="achFileAnalyser.jsp" method="post"  enctype="multipart/form-data">
    <table border="0" cellspacing="3" cellpadding="3">

        <%	if (analyzeChase) { %>
        <tr><td><h4>Select the ACH file transfer type </h4></td></tr>
        <tr><td colspan="2">
            <input type='radio' name='sendMethod' <%=ftpMethod%> value='ftp'><b>FTP</b> based ACH file.</input>
            <input type='radio' name='sendMethod' <%=modemMethod%> value='modem'><b>MODEM</b> based ACH file.</input>
        </td>
        </tr>
        <%	} %>
        <tr><td><h4>Select the ACH file to analyze</h4></td></tr>

        <tr><td><input type='file' name='achfile' size='45'></td>
            <td><input type="submit" value="Analyze"
                       onclick="document.path.srcpath.value = document.path.achfile.value; return true"></td>
        </tr>
        <tr><td>Is Encrypted ACH file : </td>
            <td><input type='radio' name='isEncrypted' value='0' checked="checked">No</input></td>
            <td><input type='radio' name='isEncrypted' value='1'>Yes</input></td>
        </tr>
    </table>
    <input type="hidden" name="srcpath" value="">
</form>

<%
    if (achfile == null && srcpath == null)
    {
        out.flush ();
        return;
    }

    if (achfile == null && srcpath != null)
        achfile = new java.io.File (srcpath);

    if (achfile == null && srcpath == null)
    {
        // If nothing to analyze, return
        out.println ("<div class='error'>Cannot open " + Encode.forHtml(srcpath) + "<div>");
        out.flush ();
        return;
    }

    out.println ("<h4>The content of " +  Encode.forHtml(srcpath) + " is:</h4><br>");
    if (analyzeChase)
        out.println ("File was transferred via: <b>" + Encode.forHtml(sendMethod) + "</b><br><br>");

    class Listener implements RecordListener
    {
        boolean m_shownEntryHeader;
        JspWriter m_out;
        int m_columnCount;

        public void recordCreated (RecordTemplate template)
        {
            try
            {
                switch (template.getId ())
                {
                    case RecordId.PPD_ENTRY_DETAIL:
                    case RecordId.CCD_ENTRY_DETAIL:
                    case RecordId.ADDENDA:
                    {
                        if (! m_shownEntryHeader)
                        {
                            m_out.println ("<h5>" + template.getName () + "</h5>");
                            m_out.println ("<table class='ach' border=0 cellspacing=1 cellpadding=1><tr>");
                            printLabels (m_out, template);
                            m_out.println ("</tr>");
                            m_shownEntryHeader = true;
                        }

                        m_out.println ("<tr>");
                        printValues (m_out, template);
                        m_out.println ("</tr>");
                    }
                    break;

                    default:
                    {
                        // Terminate last entry table
                        if (m_shownEntryHeader)
                            m_out.println ("</table><br>");

                        m_shownEntryHeader = false;
                        m_out.println ("<h5>" + template.getName () + "</h5>");

                        m_out.println ("<table class='ach' border=0 cellspacing=1 cellpadding=2><tr>");
                        printLabels (m_out, template);
                        m_out.println ("</tr><tr>");
                        printValues (m_out, template);
                        m_out.println ("</tr></table><br>");
                    }
                }
            }
            catch (Exception ex)
            {
                // Unable to write
            }
        }

        void printLabels (JspWriter w, RecordTemplate template) throws IOException
        {
            String color = "gray";
            switch (template.getId ())
            {
                case RecordId.PPD_ENTRY_DETAIL:
                case RecordId.CCD_ENTRY_DETAIL:
                case RecordId.ADDENDA:
                    color = "darkgreen";
                    break;

                case RecordId.BATCH_HEADER:
                case RecordId.BATCH_CONTROL:
                    color = "darkcyan";
                    break;
            }

            Enumeration e = template.getFields ();
            FieldTemplate f;
            m_columnCount = 0;
            while (e.hasMoreElements ())
            {
                f = (FieldTemplate)e.nextElement ();
                w.println ("<th class='ach' style='background-color:" + color + "'>"
                        + f.getName () + "</th>");
                m_columnCount++;
            }
        }

        void printValues (JspWriter w, RecordTemplate template) throws IOException
        {
            Enumeration e = template.getFields ();
            FieldTemplate f;

            while (e.hasMoreElements ())
            {
                int colwidth = 1;
                f = (FieldTemplate)e.nextElement ();

                //If addenda  payment information field
                if(f.getId() == FieldId.PAYMENT_RELATED_INFORMATION)
                    colwidth = m_columnCount - template.getFieldCount() + 1;

                w.println ("<td class='ach' colspan='" + colwidth + "'>" + f.getValue ().trim() + "</td>");
            }
        }

        public void recordCreated (RecordTemplate template, BigDecimal credit, BigDecimal debit)
        {}

        public void recordCreated (RecordTemplate template, Hashtable attributes)
        {}

    }

    // Create a record listener to output records
    Listener l = new Listener ();
    l.m_out = out;

    // Create an ACH file envelope
    AchFile ach = new AchFile ();
    ach.setResourcePool (pool);
    ach.addRecordListener (l);

    // Now read the file
    try {
        String isEncryptedFile = req.getParameter ("isEncrypted");
        // First decrypt the encrypted file to a temporary file
        Reader r=null;
        String clear =null;
        if("1".equals(isEncryptedFile)){
            clear = Encryption.decrypt (achfile);
            r = new StringReader (clear);
        } else {
            r= new BufferedReader(new FileReader(achfile));
        }

        ach.read (r);
        r.close ();

        // output the raw content
        out.println ("<pre><b>Raw content:</b><br>");

        if("1".equals(isEncryptedFile)){
            r = new StringReader (clear);
        } else {
            r=  new BufferedReader(new FileReader(achfile));
        }

        DecimalFormat nf = new DecimalFormat ("0000");
        int i = 0, j;
        char res[] = new char[Ach.RECORD_LENGTH];
        while ((j = r.read(res)) != -1) {
            out.println (nf.format (i) + ": " + String.valueOf(res));
            i += j;
            if (j != Ach.RECORD_LENGTH)
                break;
        }
        out.println ("Total number of characters read = " + i);
        r.close ();

        out.println ("</pre>");

    } catch (Exception ex) {
        ex.printStackTrace();
        System.out.println ("Unable to read: " + ex);
    }
%>
</table>
</body>
</html>

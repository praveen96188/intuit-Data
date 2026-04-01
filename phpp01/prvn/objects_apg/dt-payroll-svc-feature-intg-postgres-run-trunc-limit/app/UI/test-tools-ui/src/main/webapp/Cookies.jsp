<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    String getCompanyGseq(HttpServletRequest request) {
        return getCookie("DD-TEST-COMPANYGSEQ", request);
    }
%>

<%!
    void setCompanyGseq(String companyGseq, HttpServletResponse response) {
        setCookie("DD-TEST-COMPANYGSEQ", companyGseq, response);
    }
%>

<%!
    void setCookie(String cookieName, String cookieValue, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        if (cookieValue == null) {
            cookie.setMaxAge(0);
        }
        else {
            cookie.setMaxAge(3600*24*14); // two weeks
        }
        response.addCookie(cookie);
    }
%>

<%!
    String getCookie(String cookieName, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String cookieValue = null;
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                 if (cookies[i].getName().equals(cookieName)) {
                     cookieValue = cookies[i].getValue();
                     break;
                 }
            }
        }
        return cookieValue;
    }
%>

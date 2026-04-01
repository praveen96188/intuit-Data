package com.intuit.sbd.payroll.psp.adapters.taxcredits.servlet;

import com.intuit.sbd.payroll.psp.adapters.taxcredits.adapter.TaxCreditsAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: dweinberg
 * Date: Oct 1, 2010
 * Time: 3:33:19 PM
 */
public class PrintInstructionsServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String password = request.getParameter("password");
        String categoriesString = request.getParameter("categories");
        String[] categories = categoriesString.split(",");
        String eeEmail = request.getParameter("eeEmail");
        String erEmail = request.getParameter("erEmail");
        String submitDate = request.getParameter("submitDate");
        String eeName = request.getParameter("eeName");
        String signerType = request.getParameter("signerType");

        String instructions;
        try {
            instructions = new TaxCreditsAdapter().getInstructionsHTML(password, Arrays.asList(categories), eeEmail, erEmail, submitDate, eeName, signerType);
        } catch (Exception e) {
            throw new ServletException(e);
        }
                
        response.getOutputStream().write(instructions.getBytes());



    }
}

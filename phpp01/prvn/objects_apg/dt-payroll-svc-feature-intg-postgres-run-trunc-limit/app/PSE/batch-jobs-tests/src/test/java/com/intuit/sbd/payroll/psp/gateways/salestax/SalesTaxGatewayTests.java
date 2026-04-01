package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.google.gson.Gson;
import com.intuit.gst.GSTRequest;
import com.intuit.gst.GSTResponse;
import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.mappers.MapperRegistry;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.context.request.RequestAttributes;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 7, 2008
 * Time: 4:49:43 PM
 * 7/23/2013 DWeinberg added lenient runner and moved to batch jobs to avoid cyclical dependencies
 */
public class SalesTaxGatewayTests {

    //Raised JIRA to fix the endpoint - PSP-6409, till that time disabling these tests.
    @Ignore
    @Test
    public void testFailureResponseWithMissingSKU(){
        ISalesTaxGateway gateway = SalesTaxGatewayFactory.createISalesTaxGateway();
        SalesTaxResponse response = gateway.send(build_SalesTaxRequest(""));

        assertTrue("SalesTaxGateway responded", response!=null);
        assertEquals("Failure Response " ,false,response.isSuccess());
        assertEquals("Error Message List Size " ,2,response.getDetailErrorMessageList().size());
        assertEquals("Sales Tax Response Line List " ,0,response.getSalesTaxResponseLineList().size());

        for (int index = 0; index < response.getDetailErrorMessageList().size(); index++) {
            ErrorMessage message = (ErrorMessage) response.getDetailErrorMessageList().get(index);
            assertEquals("Detailed Message Code ", "1102", message.getErrorCode());
            assertEquals("Message Description", "Object, element or attribute is required in the given context. " +
                    "Either ItemId or AlternateItemId is required. [Field=OrderItem.ItemIds]",
                    message.getErrorDescription());
        }

        assertEquals("Failure Message Code ", "101", response.getSummaryErrorMessage().getErrorCode());
        assertEquals("Failure Message Description", "Transaction failed.  At least one data error occurred.",
                response.getSummaryErrorMessage().getErrorDescription());
    }

    //Raised JIRA to fix the endpoint - PSP-6409, till that time disabling these tests.
    @Ignore
    @Test
    public void testFailureResponseWithInvalidSKU(){
        ISalesTaxGateway gateway = SalesTaxGatewayFactory.createISalesTaxGateway();
        SalesTaxResponse response = gateway.send(build_SalesTaxRequest("PerPaycheck"));

        assertTrue("SalesTaxGateway responded", response!=null);
        assertEquals("Failure Response " ,false,response.isSuccess());
        assertEquals("Error Message List Size " ,2,response.getDetailErrorMessageList().size());
        assertEquals("Sales Tax Response Line List " ,0,response.getSalesTaxResponseLineList().size());

        for (int index = 0; index < response.getDetailErrorMessageList().size(); index++) {
            ErrorMessage message = (ErrorMessage) response.getDetailErrorMessageList().get(index);
            System.out.println("Code " + message.getErrorCode());
            System.out.println("Desc " + message.getErrorDescription());
            assertEquals("Detailed Message Code ", "1103", message.getErrorCode());
            assertEquals("Message Description", "Element or attribute value is invalid.  The value is either out " +
                    "of the accepted range or it is in an invalid format. [Field=ItemId.Id,Value=PerPaycheck]",
                    message.getErrorDescription());
        }

        assertEquals("Failure Message Code ", "101", response.getSummaryErrorMessage().getErrorCode());
        assertEquals("Failure Message Description", "Transaction failed.  At least one data error occurred.",
                response.getSummaryErrorMessage().getErrorDescription());
    }

    //Raised JIRA to fix the endpoint - PSP-6409, till that time disabling these tests.
    @Ignore
    @Test
    public void testHappyPath(){
        ISalesTaxGateway gateway = SalesTaxGatewayFactory.createISalesTaxGateway();
        //SalesTaxRequest request = build_SalesTaxRequest("OLXFEENF07RT");
        SalesTaxRequest request = build_SalesTaxRequest("293939"); // 293939 is valid (in GEMS) and taxable in the test address
        SalesTaxResponse response = gateway.send(request);

        assertTrue("SalesTaxGateway responded", response!=null);
        assertEquals("Success Response " ,true,response.isSuccess());
        assertEquals("Sales Tax Response Line List " ,2,response.getSalesTaxResponseLineList().size());
        assertNull(response.getDetailErrorMessageList());
        assertNull(response.getSummaryErrorMessage());

        ArrayList<SalesTaxResponseLine> responseList = response.getSalesTaxResponseLineList();
        BigDecimal totalTaxAmt= new BigDecimal("0");

        for (SalesTaxResponseLine responseLine : responseList) {
            for( SalesTaxRequestLine line:request.getSalesTaxRequestLineList()){
                assertEquals("SKU ",responseLine.getSKU(),line.getSKU());
            }
            System.out.println("Amount " + responseLine.getTaxAmount());
            System.out.println("Tax Rate " + responseLine.getTaxRate());
            System.out.println("SKU " + responseLine.getSKU());
            totalTaxAmt = totalTaxAmt.add(responseLine.getTaxAmount());
        }

        assertEquals("Total Tax Amt ",totalTaxAmt, response.getTotalTaxAmount());
    }

    private SalesTaxRequest build_SalesTaxRequest(String pSKU){
        SalesTaxRequest taxRequest = new SalesTaxRequest();
        taxRequest.setDocumentId("Batch001");
        taxRequest.setDocumentDateTime(Calendar.getInstance());
        taxRequest.setCompanyName("12345");
        taxRequest.setAddressLine1("13433 Wyoming Valley");
        taxRequest.setAddressLine2("");
        taxRequest.setAddressLine3("");
        taxRequest.setCity("Austin");
        taxRequest.setCountry("US");
        taxRequest.setZipCode("78727");
        taxRequest.setState("TX");
        taxRequest.setFirstName("");
        taxRequest.setLastName("");
        taxRequest.setEmail("");
        taxRequest.setPhoneNumber("");        

        SalesTaxRequestLine line1 = new SalesTaxRequestLine();
        line1.setSKU(pSKU);
        line1.setQuantity(1);
        line1.setAmount(new BigDecimal("74.95"));

        taxRequest.addLine(line1);

        SalesTaxRequestLine line2 = new SalesTaxRequestLine();
        line2.setSKU(pSKU);
        line2.setQuantity(1);
        line2.setAmount(new BigDecimal("300.00"));

        taxRequest.addLine(line2);

        return taxRequest;
    }



    @Ignore
    @Test
    public void TestGSTImpl() {
        PayrollServicesTest.beforeEachTest();

        String salesTaxGatewayImplementationClassFlag = "SalesTaxGSTImpl";
        ISalesTaxGateway taxGateway;

        switch (salesTaxGatewayImplementationClassFlag) {
            case "SalesTaxGSTImpl":
                taxGateway = PayrollApplicationBeanFactory.getBean(SalesTaxGSTImpl.class);
                break;
            case "SalesTaxGSTAndEOSComparisonImpl":
                taxGateway = PayrollApplicationBeanFactory.getBean(SalesTaxGSTAndEOSComparisonImpl.class);
                break;
            //In case of any issues we will switch to original implementation
            default:
                String salesTaxGatewayImplementationClass = SystemParameter.findStringValue(SystemParameter.Code.SALES_TAX_GATEWAY_IMPLEMENTATION_CLASS, null);
                taxGateway = SalesTaxGatewayFactory.createISalesTaxGateway(salesTaxGatewayImplementationClass);
                break;
        }


        SalesTaxRequest pspSalesTaxRequest = build_SalesTaxRequest("293939");
        SalesTaxResponse salesTaxResponse = taxGateway.send(pspSalesTaxRequest);

        assertTrue(salesTaxResponse.isSuccess());
    }




}

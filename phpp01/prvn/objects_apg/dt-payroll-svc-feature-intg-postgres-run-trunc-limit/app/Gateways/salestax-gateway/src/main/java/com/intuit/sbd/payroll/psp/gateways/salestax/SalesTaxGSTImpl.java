package com.intuit.sbd.payroll.psp.gateways.salestax;

import com.intuit.gst.GSTRequest;
import com.intuit.gst.GSTResponse;
import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.mappers.MapperRegistry;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.salestaxservices.EOSProductClassInfoService;
import com.intuit.sbg.psp.salestaxservices.GST;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestContextHolderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Component
public class SalesTaxGSTImpl implements ISalesTaxGateway{

    private final GST gstService;
    private final MapperRegistry mapperRegistry;
    private final AuthorizationManager authorizationManager;
    private final EOSProductClassInfoService eosProductClassInfoService;
    private OfflineTicketClient offlineTicketClient;

    @Autowired
    public SalesTaxGSTImpl(GST gst, MapperRegistry mapperRegistry, AuthorizationManager authorizationManager,
                           EOSProductClassInfoService eosProductClassInfoService, OfflineTicketClient offlineTicketClient) {
        this.gstService = gst;
        this.mapperRegistry = mapperRegistry;
        this.authorizationManager = authorizationManager;
        this.eosProductClassInfoService = eosProductClassInfoService;
        this.offlineTicketClient = offlineTicketClient;
        log.info("Created SalesTaxGSTImpl bean successfully");
    }

    @Override
    public SalesTaxResponse send(SalesTaxRequest pSalesTaxRequest) {
        SalesTaxResponse salesTaxResponse = new SalesTaxResponse();

        String docId = Objects.nonNull(pSalesTaxRequest) ? pSalesTaxRequest.getDocumentId() : null;
        boolean haveSetSystemOfflineTicketInRequestAttributes = false;
        try {

            log.info("action=GSTCallStart docId={}",docId);
            //1. Set offline ticket in request attributes if auth context is not there
            if (!checkIfAuthContextPresentInRequestAttributes()) {
                setSystemAuthnOfflineTicketInReqAttr();
                haveSetSystemOfflineTicketInRequestAttributes = true;
            }
            //2. Core logic
            addProductClassInfoForSalesTaxLines(pSalesTaxRequest);
            GSTRequest gstRequest = mapperRegistry.mapToTarget(pSalesTaxRequest, GSTRequest.class);
            GSTResponse gstResponse = gstService.getGSTResponseForRequest(gstRequest);
            salesTaxResponse = mapperRegistry.mapToTarget(gstResponse, SalesTaxResponse.class);
            log.info("action=GSTCallComplete docId={}",docId);
        } catch (Exception ex) {
            String exceptionMessage = String.format("action=GSTCallError docId=%s Unable to get SalesTaxResponse from SalesTaxGSTImpl",docId);
            log.error(exceptionMessage,ex);
        } finally {
            if(haveSetSystemOfflineTicketInRequestAttributes) {
                removeSystemOfflineTicketFromRequestAttributes();
            }
        }
        return salesTaxResponse;

    }

    protected void setSystemAuthnOfflineTicketInReqAttr() {
        log.info("Setting authorization ID2 context to call SalesTaxGSTImpl");
        String offlineTicket = offlineTicketClient.getOfflineTicket();
        if(offlineTicket.isEmpty()) {
            throw new RuntimeException(String.format("Unable to set SalesTaxGSTImpl Authorization Context ID2"));
        }
        RequestContextHolderUtils.getRequestAttributes().setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket, RequestAttributes.SCOPE_REQUEST);
    }

    protected void removeSystemOfflineTicketFromRequestAttributes() {
        RequestContextHolderUtils.getRequestAttributes().removeAttribute(ContextConstants.AUTHN_CONTEXT, RequestAttributes.SCOPE_REQUEST);
    }

    protected boolean checkIfAuthContextPresentInRequestAttributes() {
        AuthorizationContext authorizationContext = (AuthorizationContext) RequestContextHolderUtils.getRequestAttributes().getAttribute(ContextConstants.AUTHN_CONTEXT,RequestAttributes.SCOPE_REQUEST);
        if(Objects.nonNull(authorizationContext)) {
            return true;
        }
        return false;
    }

    private void addProductClassInfoForSalesTaxLines(SalesTaxRequest salesTaxRequest) {
        try {
            for (SalesTaxRequestLine salesTaxRequestLine : salesTaxRequest.getSalesTaxRequestLineList()) {
                String productClassForSKU = eosProductClassInfoService.getProductClassForSKU(salesTaxRequestLine.getSKU());
                salesTaxRequestLine.setProductClassForSKU(productClassForSKU);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Unable to retrieve and set productClass for ItemSKU",ex);
        }
    }


}

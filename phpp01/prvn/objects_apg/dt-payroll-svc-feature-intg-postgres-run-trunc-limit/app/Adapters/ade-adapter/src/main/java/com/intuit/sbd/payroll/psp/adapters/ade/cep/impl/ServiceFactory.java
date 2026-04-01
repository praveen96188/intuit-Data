package com.intuit.sbd.payroll.psp.adapters.ade.cep.impl;

import com.intuit.ems.cep.api.*;
import com.intuit.ems.cep.api.exception.ServiceResultException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.v1.company.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 4/23/14
 * Time: 9:07 AM
 */
public class ServiceFactory extends com.intuit.ems.cep.api.ServiceFactory {
    private static SpcfLogger logger = Application.getLogger(ServiceFactory.class);

    private static Map<ResourceNameEnum, Class> getServiceClassMap = new HashMap<ResourceNameEnum, Class>();
    private static Map<ResourceNameEnum, Class> getListServiceClassMap = new HashMap<ResourceNameEnum, Class>();
    private static Map<ResourceNameEnum, Class> createServiceClassMap = new HashMap<ResourceNameEnum, Class>();
    private static Map<ResourceNameEnum, Class> updateServiceClassMap = new HashMap<ResourceNameEnum, Class>();
    private static Map<ResourceNameEnum, Class> deleteServiceClassMap = new HashMap<ResourceNameEnum, Class>();

    static {
        // Get Services mapping
        registerGetService(ResourceNameEnum.COMPANIES, CompanyGetService.class);
        registerGetService(ResourceNameEnum.TAXSETUP, CompanyTaxSetupGetService.class);
        registerGetService(ResourceNameEnum.TAXITEMS, CompanyTaxItemGetService.class);
        registerGetService(ResourceNameEnum.TAXPAYMENTGROUPS, CompanyTaxPaymentGroupGetService.class);

        // GetList Services mapping
        registerGetListService(ResourceNameEnum.AGENCIES, CompanyAgencyListService.class);
        registerGetListService(ResourceNameEnum.CONTACTS, CompanyContactListService.class);
        registerGetListService(ResourceNameEnum.COMPANIES, CompanyGetListService.class);
        registerGetListService(ResourceNameEnum.TAXITEMS, CompanyTaxItemGetListService.class);
        registerGetListService(ResourceNameEnum.TAXJURISDICTIONS, CompanyTaxJurisdictionGetListService.class);
        registerGetListService(ResourceNameEnum.TAXPAYMENTGROUPS, CompanyTaxPaymentGroupGetListService.class);
        registerGetListService(ResourceNameEnum.TAXFILINGTYPES, CompanyTaxFilingTypeGetListService.class);

        //Update Services mapping
        registerUpdateService(ResourceNameEnum.AGENCIES, CompanyAgencyUpdateService.class);
        registerUpdateService(ResourceNameEnum.TAXPAYMENTGROUPS, CompanyTaxPaymentGroupUpdateService.class);
        registerUpdateService(ResourceNameEnum.TAXSETUP, CompanyTaxSetupUpdateService.class);
        registerUpdateService(ResourceNameEnum.TAXITEMS, CompanyTaxItemUpdateService.class);
        registerUpdateService(ResourceNameEnum.TAXFILINGTYPES, CompanyTaxFilingTypeUpdateService.class);
    }

    @Override
    public <T, S extends ServiceParams> AbstractGetService<T, S> constructGetServiceInstance(ResourceNameEnum pResourceNameEnum) {
        //noinspection unchecked
        return (AbstractGetService<T, S>) createNewServiceInstance(pResourceNameEnum, getServiceClassMap);
    }

    @Override
    public <T, S extends ServiceParams> AbstractGetListService<T, S> constructGetListServiceInstance(ResourceNameEnum pResourceNameEnum) {
        //noinspection unchecked
        return (AbstractGetListService<T, S>) createNewServiceInstance(pResourceNameEnum, getListServiceClassMap);
    }

    @Override
    public <T, S extends ServiceParams> AbstractCreateService<T, S> constructCreateServiceInstance(ResourceNameEnum pResourceNameEnum) {
        //noinspection unchecked
        return (AbstractCreateService<T, S>) createNewServiceInstance(pResourceNameEnum, createServiceClassMap);
    }

    @Override
    public <T, S extends ServiceParams> AbstractUpdateService<T, S> constructUpdateServiceInstance(ResourceNameEnum pResourceNameEnum) {
        //noinspection unchecked
        return (AbstractUpdateService<T, S>) createNewServiceInstance(pResourceNameEnum, updateServiceClassMap);
    }

    @Override
    public <T, S extends ServiceParams> AbstractDeleteService<T, S> constructDeleteServiceInstance(ResourceNameEnum pResourceNameEnum) {
        //noinspection unchecked
        return (AbstractDeleteService<T, S>) createNewServiceInstance(pResourceNameEnum, deleteServiceClassMap);
    }

    private Object createNewServiceInstance(ResourceNameEnum pResourceNameEnum, Map<ResourceNameEnum, Class> pClassMap) {
        try {
            Class serviceClass = pClassMap.get(pResourceNameEnum);
            if(serviceClass == null) {
                ServiceResult serviceResult = new ServiceResult();
                serviceResult.getMessages().NotImplemented();
                throw new ServiceResultException(serviceResult);
            }

            return pClassMap.get(pResourceNameEnum).newInstance();
        } catch (Exception e) {
            logger.error("Error creating service class", e);
            throw new RuntimeException(e);
        }
    }

    public static void registerGetService(ResourceNameEnum pResourceNameEnum, Class pClass) {
        if (getServiceClassMap.containsKey(pResourceNameEnum)) {
            throw new RuntimeException("A class for " + pResourceNameEnum + " has already been registered! Tried to register " + pClass.getName());
        }

        getServiceClassMap.put(pResourceNameEnum, pClass);
    }

    public static void registerGetListService(ResourceNameEnum pResourceNameEnum, Class pClass) {
        if (getListServiceClassMap.containsKey(pResourceNameEnum)) {
            throw new RuntimeException("A class for " + pResourceNameEnum + " has already been registered! Tried to register " + pClass.getName());
        }

        getListServiceClassMap.put(pResourceNameEnum, pClass);
    }

    public static void registerCreateService(ResourceNameEnum pResourceNameEnum, Class pClassName) {
        if (createServiceClassMap.containsKey(pResourceNameEnum)) {
            throw new RuntimeException("A class for " + pResourceNameEnum + " has already been registered! Tried to register " + pClassName.getName());
        }

        createServiceClassMap.put(pResourceNameEnum, pClassName);
    }

    public static void registerUpdateService(ResourceNameEnum pResourceNameEnum, Class pClassName) {
        if (updateServiceClassMap.containsKey(pResourceNameEnum)) {
            throw new RuntimeException("A class for " + pResourceNameEnum + " has already been registered! Tried to register " + pClassName.getName());
        }

        updateServiceClassMap.put(pResourceNameEnum, pClassName);
    }

    public static void registerDeleteService(ResourceNameEnum pResourceNameEnum, Class pClassName) {
        if (deleteServiceClassMap.containsKey(pResourceNameEnum)) {
            throw new RuntimeException("A class for " + pResourceNameEnum + " has already been registered! Tried to register " + pClassName.getName());
        }

        deleteServiceClassMap.put(pResourceNameEnum, pClassName);
    }
}

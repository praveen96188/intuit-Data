package com.intuit.ems.payroll.psp.ams_migration;

import com.intuit.ems.payroll.psp.model.Address;
import com.intuit.ems.payroll.psp.model.AddressReportModel;
import com.intuit.payments.cdm.v2.client.BusinessOwner;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.PhysicalAddress;
import com.intuit.payments.cdm.v2.client.PrimaryBusiness;
import com.intuit.payments.cdm.v2.client.enums.ContactTypeEnum;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;


@Slf4j
public class AddressDataFix {

    private final AccountServiceGateway accountServiceGateway;

    private static String addressFixFor;
    private final String inputBizInfoFile;
    private final String inputBizOwnerFile;
    private static String outPutFile;

    private final static String SUCCESS = "Success";
    private static final String ADDRESS_SEPARATOR = "\r";
    private final static String VALIDATION_FAILURE = "Validation_failure";
    private final static String AMS_FAILURE = "Ams_failure";
    private final static String START = "Start";
    private final static String COMPLETED = "Completed";
    private final static String COUNTRY_US = "US";
    private final static String COUNTRY_USA = "USA";

    private final static String BUSINESS_INFO = "BusinessInfo";
    private final static String BUSINESS_OWNER = "BusinessOwner";



    private AddressDataFix(String addressFixFor, String inputBizInfoFile, String inputBizOwnerFile, String outPutFile) {
        this.addressFixFor = addressFixFor;
        this.inputBizInfoFile = inputBizInfoFile;
        this.inputBizOwnerFile = inputBizOwnerFile;
        this.outPutFile = outPutFile;
        accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
    }

    public static void main(String[] args) {

        String logPrefix = "Job=AddressDataFix, Action=mainFn, Status={}{}";
        try {
            Application.initialize();

            log.info(logPrefix, START, ", args={}" + args);
            long s = System.currentTimeMillis();
            AddressDataFix addressDataFix = new AddressDataFix(args[0], args[1], args[2], args[3]);
            List<AddressReportModel> outputReport = addressDataFix.process(addressFixFor);
            generateReport(outputReport);
            long e = System.currentTimeMillis();
            log.info(logPrefix, COMPLETED, ", time_taken_millis=" + (e-s));
        } catch (Exception e) {
            log.error(logPrefix, "Error", ", errType=" + e.getMessage(), e);
        } finally {
            Application.uninitialize();
            System.exit(0);
        }

    }

    private List<AddressReportModel> process(String addressFixFor) {

        switch (addressFixFor) {
            case "updateOnlyBizInfo":
                return updateOnlyBizInfo(inputBizInfoFile);

            case "updateOnlyOwnerInfo":
                return updateOnlyBizOwner(inputBizOwnerFile);

            case "updateBoth":
                return updateBothInfoOwner(inputBizInfoFile, inputBizOwnerFile);

            default:
                throw new IllegalStateException("Unexpected dataField, fieldName=" + addressFixFor);
        }
    }


    private List<AddressReportModel> updateOnlyBizInfo(String bizInfoFileName) {

        Map<String, AddressReportModel> AddressReportModelMap = new HashMap<>();
        Map<String, Address> bizInfoAddressMap = parseAndMap(bizInfoFileName);

        for (String realmId : bizInfoAddressMap.keySet()) {

            Address bizInfoAddress = bizInfoAddressMap.get(realmId);

            AddressReportModel AddressReportModel = updatePaymentsAccount(bizInfoAddress, null);
            AddressReportModelMap.put(realmId, AddressReportModel);

        }
        return new ArrayList(AddressReportModelMap.values());
    }

    private List<AddressReportModel> updateOnlyBizOwner(String bizOwnerFile) {

        Map<String, AddressReportModel> AddressReportModelMap = new HashMap<>();
        Map<String, Address> bizOwnerAddressMap = parseAndMap(bizOwnerFile);

        for (String realmId : bizOwnerAddressMap.keySet()) {

            Address bizOwnerAddress = bizOwnerAddressMap.get(realmId);

            AddressReportModel AddressReportModel = updatePaymentsAccount(null, bizOwnerAddress);
            AddressReportModelMap.put(realmId, AddressReportModel);

        }
        return new ArrayList(AddressReportModelMap.values());
    }

    private List<AddressReportModel> updateBothInfoOwner(String bizInfoFileName, String bizOwnerFile) {

        Map<String, AddressReportModel> AddressReportModelMap = new HashMap<>();

        Map<String, Address> bizInfoAddressMap = parseAndMap(bizInfoFileName);
        Map<String, Address> bizOwnerAddressMap = parseAndMap(bizOwnerFile);

        //To get the common realms for Business Info and Business Owner
        for (String realmId : bizOwnerAddressMap.keySet()) {
            Address bizInfoAddress = bizInfoAddressMap.get(realmId);
            Address bizOwnerAddress = bizOwnerAddressMap.get(realmId);
            AddressReportModel AddressReportModel = updatePaymentsAccount(bizInfoAddress, bizOwnerAddress);
            AddressReportModelMap.put(realmId, AddressReportModel);
        }

        //Check if there are any only BizInfo realms to be updated
        for (String realmId : bizInfoAddressMap.keySet()) {
            if (!AddressReportModelMap.containsKey(realmId)) {
                AddressReportModel AddressReportModel = updatePaymentsAccount(bizInfoAddressMap.get(realmId), null);
                AddressReportModelMap.put(realmId, AddressReportModel);
            }
        }

        return new ArrayList(AddressReportModelMap.values());
    }

    private Map<String, Address> parseAndMap(String fileName) {

        String logPrefix = "Job=AddressDataFix, Action=parseAndMap, Status={}, fileName={}{}";

        log.info(logPrefix, START, fileName);

        Map<String, Address> addressMap = null;

        try {
            List<Address> addressList = new CsvToBeanBuilder<Address>(new FileReader(fileName))
                    .withType(Address.class).build().parse();

            addressMap = addressList.stream().collect(
                    Collectors.toMap(address -> address.getRealmId(), address -> address));


        } catch (IOException ioe) {
            log.error(logPrefix, "Error", ioe.getMessage(), ioe);
            throw new RuntimeException("UnableTo parse and map the file=" + fileName);
        }
        return addressMap;
    }


    private PaymentsAccount createCombinedPaymentsAccount(PaymentsAccount account, Address bizInfoAddress, Address bizOwnerAddress, String realmId, String bizId) {

        PaymentsAccount pAccount = createBizInfoPaymentsAccount(account, bizInfoAddress, realmId);

        if (Objects.isNull(pAccount)) {
            return null;
        }
        return createBizOwnerPaymentsAccount(pAccount, bizOwnerAddress, realmId, bizId);
    }

    private PaymentsAccount createBizInfoPaymentsAccount(PaymentsAccount account, Address bizInfoAddress, String realmId) {

        String logPrefix = "Job=AddressDataFix, Action=createBizInfoPaymentsAccount, Status={}, realmId={}{}";

        log.info(logPrefix, START, realmId);

        PaymentsAccount updatedBizInfoPaymentsAccount = account;

        //Business Info
        PrimaryBusiness updatedBusinessInfo = new PrimaryBusiness();
        PhysicalAddress updatedBizInfoAddress = createPhysicalAddress(bizInfoAddress);

        if (Objects.isNull(updatedBizInfoAddress)) {
            return null;
        }

        updatedBusinessInfo.setAddress(updatedBizInfoAddress);
        updatedBusinessInfo.setMailingAddress(updatedBizInfoAddress);

        updatedBizInfoPaymentsAccount.setBusinessInfo(updatedBusinessInfo);

        return updatedBizInfoPaymentsAccount;

    }


    private PaymentsAccount createBizOwnerPaymentsAccount(PaymentsAccount paymentsAccount, Address bizOwnerAddress, String realmId, String bizOwnerId) {

        String logPrefix = "Job=AddressDataFix, Action=createBizOwnerPaymentsAccount, Status={}, realmId={}{}";

        log.info(logPrefix, START, realmId);

        PaymentsAccount updatedBizOwnerPaymentsAccount;

        if (Objects.nonNull(paymentsAccount)) {
            updatedBizOwnerPaymentsAccount = paymentsAccount;
        } else {
            updatedBizOwnerPaymentsAccount = new PaymentsAccount();
        }

        //Business Owner update details
        List<BusinessOwner> updatedBizOwnerList = new ArrayList<>();
        BusinessOwner updatedBizOwner = new BusinessOwner();
        updatedBizOwner.setId(bizOwnerId);
        updatedBizOwner.setAddress(createPhysicalAddress(bizOwnerAddress));
        updatedBizOwnerList.add(updatedBizOwner);

        updatedBizOwnerPaymentsAccount.setBusinessOwners(updatedBizOwnerList);

        return updatedBizOwnerPaymentsAccount;

    }

    private AddressReportModel updatePaymentsAccount(Address bizInfoAddress, Address bizOwnerAddress) {

        String logPrefix = "Job=AddressDataFix, Action=updatePaymentsAccount, Status={}, realmId={}{}";

        AddressReportModel model;
        String message = null;
        String status;
        String realmId = null;
        String sourceCompanyId = null;
        boolean isBizInfoProcessed = false;
        boolean isBizOwnerProcessed = false;

        PaymentsAccount toBeUpdatedPaymentsAccount = new PaymentsAccount();

        if (Objects.isNull(bizOwnerAddress)) {
            realmId = bizInfoAddress.getRealmId();
            sourceCompanyId = bizInfoAddress.getSourceCompanyId();
            isBizInfoProcessed = true;
            toBeUpdatedPaymentsAccount = createBizInfoPaymentsAccount(toBeUpdatedPaymentsAccount, bizInfoAddress, bizInfoAddress.getRealmId());
        } else if (Objects.isNull(bizInfoAddress)) {
            realmId = bizOwnerAddress.getRealmId();
            sourceCompanyId = bizOwnerAddress.getSourceCompanyId();
            String bizOwnerId = getBizOwnerId(realmId);

            if (StringUtils.isNotBlank(bizOwnerId)) {
                isBizOwnerProcessed = true;
                toBeUpdatedPaymentsAccount = createBizOwnerPaymentsAccount(toBeUpdatedPaymentsAccount, bizOwnerAddress, bizOwnerAddress.getRealmId(), bizOwnerId);
            } else {
                message = "Business Id not found!";
                return buildOutputData(sourceCompanyId, realmId, false, false, VALIDATION_FAILURE, message);
            }

        } else {
            realmId = bizOwnerAddress.getRealmId();
            String bizOwnerId = getBizOwnerId(realmId);
            sourceCompanyId = bizInfoAddress.getSourceCompanyId();
            if (StringUtils.isNotBlank(bizOwnerId)) {
                isBizInfoProcessed = true;
                isBizOwnerProcessed = true;
                toBeUpdatedPaymentsAccount = createCombinedPaymentsAccount(toBeUpdatedPaymentsAccount, bizInfoAddress, bizOwnerAddress, bizOwnerAddress.getRealmId(), bizOwnerId);
            } else {
                message = "Business Id not found! ";
                return buildOutputData(sourceCompanyId, realmId, false, false, VALIDATION_FAILURE, message);
            }
        }

        //If the toBeUpdatedPaymentsAccount is null, no need to call AMS API
        if (Objects.isNull(toBeUpdatedPaymentsAccount)) {
            model = buildOutputData(sourceCompanyId, realmId, false, false, VALIDATION_FAILURE, "toBeUpdatedPaymentsAccount account is nulls");
            return model;
        }

        try {

            log.info(logPrefix, START, realmId, StringUtils.EMPTY);

            PaymentsAccount updatedPaymentsAccount = accountServiceGateway.updatePaymentsAccount(realmId, toBeUpdatedPaymentsAccount);


            if (Objects.nonNull(updatedPaymentsAccount) && isBizInfoProcessed) {
                message =  compareAddress(toBeUpdatedPaymentsAccount.getBusinessInfo().getAddress(),
                        updatedPaymentsAccount.getBusinessInfo().getAddress(), BUSINESS_INFO);
            }

            if (Objects.nonNull(updatedPaymentsAccount) && isBizOwnerProcessed && StringUtils.isEmpty(message)) {
                message = validateBizOwnerAddressUpdate(toBeUpdatedPaymentsAccount, updatedPaymentsAccount);
            }


            if (!StringUtils.isNotBlank(message)) {
                status = SUCCESS;
            } else {
                status = VALIDATION_FAILURE;
            }

            model = buildOutputData(sourceCompanyId, realmId, isBizInfoProcessed, isBizOwnerProcessed, status, message);
            log.info(logPrefix, COMPLETED, realmId, StringUtils.EMPTY);
            return model;


        } catch (AccountServicesException e) {
            if (e.getHttpServiceResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.error(logPrefix, "Error", realmId, ", errType=PaymentsAccountNotFound, errMsg=" + e.getHttpServiceResponse().toDetailedString(), e);
            } else {
                log.error(logPrefix, "Error", realmId, ", errType=AccountServicesException, errMsg=" + e.getHttpServiceResponse().toDetailedString(), e);
            }
            model = buildOutputData(sourceCompanyId, realmId, isBizInfoProcessed, isBizOwnerProcessed, AMS_FAILURE, e.getErrorMessage());

            return model;

        } catch(HttpClientErrorException httpClientErrorException) {
            return handleException(logPrefix, realmId, sourceCompanyId, isBizInfoProcessed, isBizOwnerProcessed, "PaymentsAccountNotFound", httpClientErrorException);
        } catch (CallNotPermittedException callNotPermittedException) {
            return handleException(logPrefix, realmId, sourceCompanyId, isBizInfoProcessed, isBizOwnerProcessed, "AccountServicesException", callNotPermittedException);
        }

    }

    /**
     *
     * @param logPrefix
     * @param realmId
     * @param sourceCompanyId
     * @param isBizInfoProcessed
     * @param isBizOwnerProcessed
     * @param errorMsg
     * @param excp
     * @return
     */
    private AddressReportModel handleException(String logPrefix, String realmId, String sourceCompanyId, boolean isBizInfoProcessed, boolean isBizOwnerProcessed, String errorMsg, Exception excp) {
        AddressReportModel model;
        log.error(logPrefix, "Error", realmId, ", errType=" + errorMsg + ", errMsg=" + excp.getMessage(), excp);
        model = buildOutputData(sourceCompanyId, realmId, isBizInfoProcessed, isBizOwnerProcessed, AMS_FAILURE, excp.getMessage());

        return model;
    }


    private PhysicalAddress createPhysicalAddress(Address address) {
        if (Objects.isNull(address))
            return null;
        PhysicalAddress physicalAddress = new PhysicalAddress();
        physicalAddress.setStreetAddress(getStreetAddress(address));
        physicalAddress.setCity(address.getCity());
        physicalAddress.setRegion(address.getState());
        physicalAddress.setCountry(StringUtils.isNotBlank(address.getCountry()) ? address.getCountry() : COUNTRY_US);
        physicalAddress.setPostalCode(getFullZipCode(address));
        return physicalAddress;
    }


    private String getStreetAddress(Address address) {

        StringBuilder str = new StringBuilder();

        if (StringUtils.isNotBlank(address.getAddressLine1()) && StringUtils.isNotBlank(address.getAddressLine2())) {
            str.append(address.getAddressLine1().trim()).append(ADDRESS_SEPARATOR).append(address.getAddressLine2().trim());
        } else {
            str.append(address.getAddressLine1().trim());
        }

        return str.toString();

    }


    private String getFullZipCode(Address address) {

        String zipCode = "";

        if (StringUtils.isNotBlank(address.getZipCode())) {
            zipCode += address.getZipCode();
            if (StringUtils.isNotBlank(address.getZipCodeExtension())) {
                zipCode += address.getZipCodeExtension();
            }
        }

        return zipCode;
    }

    private String getBizOwnerId(String realmId) {

        String bizId = null;

        String logPrefix = "Job=AddressDataFix, Action=getBizOwnerId, Status={}, realmId={}{}";

        try {
            log.info(logPrefix, START, realmId, StringUtils.EMPTY);

            PaymentsAccount existingPaymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);

            List<BusinessOwner> bizOwnerList = existingPaymentsAccount.getBusinessOwners();

            for (BusinessOwner bizOwner : bizOwnerList) {
                if (ContactTypeEnum.OWNER.equals(bizOwner.getContactType())) {
                    bizId = bizOwner.getId();
                    break;
                }
            }

        } catch (AccountServicesException e) {
            if (e.getHttpServiceResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                log.error(logPrefix, "Error", realmId, ", errType=PaymentsAccountNotFound, errMsg=" + e.getHttpServiceResponse().toDetailedString(), e);
            } else {
                log.error(logPrefix, "Error", realmId, ", errType=AccountServicesException, errMsg=" + e.getHttpServiceResponse().toDetailedString(), e);
            }

        } catch(HttpClientErrorException excp) {
            log.error(logPrefix, "Error", realmId, ", errType=ClientErrorExceptionPaymentsAccountNotFound, errMsg=" + excp.getResponseBodyAsString(), excp);
        } catch (CallNotPermittedException cnpe) {
            log.error(logPrefix, "Error", realmId, ", errType=CallNotPermittedException, errMsg=" + cnpe.getMessage(), cnpe);
        }
        return bizId;

    }


    private String validateBizOwnerAddressUpdate(PaymentsAccount paymentsAccount, PaymentsAccount updatedPaymentsAccount) {

        String logPrefix = "Job=AddressDataFix, Action=validateBizOwnerAddressUpdate, errMsg={}";

        List<BusinessOwner> eBizOwnerList = paymentsAccount.getBusinessOwners();
        List<BusinessOwner> uBizOwnerList = updatedPaymentsAccount.getBusinessOwners();

        BusinessOwner uBizOwner = null;

        BusinessOwner eBizOwner = eBizOwnerList.get(0);

        for (BusinessOwner bizOwner : uBizOwnerList) {
            if (bizOwner.getContactType().equals(ContactTypeEnum.OWNER)) {
                uBizOwner = bizOwner;
                break;
            }
        }

        if (!eBizOwner.getId().equals(uBizOwner.getId())) {
            String errMsg = "Business Owner Id does not match!";
            log.error(logPrefix, errMsg);
            return errMsg;
        }

        return  compareAddress(eBizOwner.getAddress(), uBizOwner.getAddress(), BUSINESS_OWNER);

    }

    private AddressReportModel buildOutputData(String sourceCompanyId, String realmId, boolean infoAddressProcessed, boolean ownerAddressProcessed, String status, String message) {
        AddressReportModel model = new AddressReportModel();
        model.setCSource_Company_Id(sourceCompanyId);
        model.setCRealm_Id(realmId);
        model.setInfo_address_processed(infoAddressProcessed);
        model.setOwner_address_processed(ownerAddressProcessed);
        model.setStatus(status);
        model.setMessage(message);
        return model;

    }

    private static void generateReport(List<AddressReportModel> outputReport) {
        String logPrefix = "Job=AddressDataFix, Action=generateReport, Status={}";
        log.info(logPrefix, START);
        try {
            Writer writer = new FileWriter(outPutFile);

            final StatefulBeanToCsvBuilder<AddressReportModel> builder =
                    new StatefulBeanToCsvBuilder(writer);
            StatefulBeanToCsv beanWriter =
                    builder.withApplyQuotesToAll(false).build();

            beanWriter.write(outputReport);
            log.info(logPrefix, COMPLETED + " Processed=" + (outputReport.size()));
            writer.close();
        } catch (Exception e) {
            log.error(logPrefix, "Error" + " errMsg={}" + e.getMessage(), e);
        }

    }

    /**
     * compares addresses of type Physical address and Physical address
     * compares the street address
     *
     * @param address1
     * @param address2
     * @param type
     * @return
     */
    public String compareAddress(PhysicalAddress address1, PhysicalAddress address2, String type) {

        String logPrefix = "Job=AddressDataFix, Action=compareAddress, errMsg={}";

        String errMsg = StringUtils.EMPTY;

        if (!equalsIgnoreCase(trim(address1.getStreetAddress()), trim(address2.getStreetAddress()))) {
            errMsg = "Street address does not match!, Type=" + type;
            log.error(logPrefix, errMsg);
            return errMsg;
        }

        if (!equalsIgnoreCase(trim(address1.getCity()), trim(address2.getCity()))) {
            errMsg = "City does not match!, Type=" + type;
            log.error(logPrefix, errMsg);
            return errMsg;
        }

        if (!equalsIgnoreCase(trim(address1.getRegion()), trim(address2.getRegion()))) {
            errMsg = "Region does not match!, Type=" + type;
            log.error(logPrefix, errMsg);
            return errMsg;
        }

        if (!((equalsIgnoreCase(trim(address1.getCountry()), COUNTRY_US) &&
                equalsIgnoreCase(trim(address2.getCountry()), COUNTRY_USA)) ||
                equalsIgnoreCase(trim(address1.getCountry()), trim(address2.getCountry())))) {
            errMsg = "Country does not match!, Type=" + type;
            log.error(logPrefix, errMsg);
            return errMsg;
        }


        if (!StringUtils.equals(trim(address1.getPostalCode()), trim(address2.getPostalCode()))) {
            errMsg = "Postal Code does not match!, Type=" + type;
            log.error(logPrefix, errMsg);
            return errMsg;
        }

        return errMsg;
    }



}
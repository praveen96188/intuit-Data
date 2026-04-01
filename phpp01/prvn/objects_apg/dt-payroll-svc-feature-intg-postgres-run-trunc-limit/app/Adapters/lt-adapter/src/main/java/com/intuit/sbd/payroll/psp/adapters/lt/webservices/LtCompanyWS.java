package com.intuit.sbd.payroll.psp.adapters.lt.webservices;

import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtCompanyRsWSDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtCompanyListRsWSDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtSystemAndServiceRqWSDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.LtCompany;
import com.intuit.sbd.payroll.psp.adapters.lt.LtCompanyDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.LtTransmissionRqWSDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.OFXToXML;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;

import java.util.ArrayList;
import javax.jws.WebService;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebMethod;

/**
 * Web Service used to expose methods for retrieving information about companies in the database.  All methods return an
 * <code>LtCompanyWSDTO</code> object.  The <code>LtCompanyWSDTO</code> is a wrapper for returning the following:
 * <ul>
 * <li>One or more <code>LtCompanyDTO</code> elements - reach representing a company
 * <li>Number of companies retrieved (number of LtCompanyDTO elements)
 * <li>Status of the request
 * </ul>
 * Each <code>LtCompanyDTO</code> contains the following elements:
 * <ul>
 * <li>Company ID
 * <li>FEIN
 * <li>Company Name
 * <li>Bank Account
 * <li>Source System Id
 * <li>Current Token - not meaningfull for QBOE companies
 * </ul>
 * RETURN STATUS:
 * <ul>
 * <li>0 - Success
 * <li>1 - Some inputs were invalid
 * <li>2 - Input is missing
 * </ul>
 * Status' 1 & 2 only occure when methods have string inputs that are not constrained - ie, PSID's or EIN's
 */
@WebService
public class LtCompanyWS {

    /**
     * Given a Source System Code and PSID, this will return information about a single company
     * @param companyId - The PSID of the company to search for
     * @param systemId - The Source System Code of the company to search for
     * @return <code>LtCompanyWSDTO</code> containing all company matching the request criteria
     * @throws Exception runtime exception
     */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtCompanyRsWSDTO getSingle(@WebParam(name = "companyId") String companyId,
                                    @WebParam(name = "sourceSystemId") String systemId) throws Exception {

        //Create the strutures to return
        LtCompany company = new LtCompany();
        LtCompanyRsWSDTO dto = new LtCompanyRsWSDTO();

        try{
            dto.company = company.getCompany(companyId, SourceSystemCode.valueOf(systemId));

        }catch(Exception e){
            throw e;
        }

        dto.status = 0;
        return dto;

    }

/**
 * Purpose: Given a Source System Id, this will return all Active companies on that Source System.  Each company will
 * contain the following information.
 * <ul>
 * <li>Company ID
 * <li>FEIN
 * <li>Company Name
 * <li>Bank Account
 * <li>Source System Id
 * <li>Current Token - not meaningfull for QBOE companies
 * </ul>
 */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtCompanyListRsWSDTO getCompanyList(@WebParam(name = "GetCompanyListRequest")LtSystemAndServiceRqWSDTO rq) throws Exception {
        //@TODO: right now the status is meaningless - fix it

        LtCompany company = new LtCompany();
        LtCompanyListRsWSDTO dto = new LtCompanyListRsWSDTO();

        //Retrieve the list of companies and create the DTO
        try{
            dto.Company.addAll(company.getCompanyList(rq.sourceSystemId,"Active"));
            dto.numberOfCompanies = dto.Company.size();

        }catch(Exception e){
            throw e;
        }

        dto.status = 0;
        return dto;
    }
    /**
     * Purpose: Given a Source System Id, Service Code, and Service Status,this will return all companies that match the
     * requested criteria.
     *
     * @param rq - <code>LtSystemAndServiceRqWSDTO</code> structure containing the parameters for the query
     * @return  <code>LtCompanyWSDTO</code> containing all companies matching the request criteria
     * @throws Exception runtime exception
     */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtCompanyListRsWSDTO getCompaniesBySystemAndService(@WebParam(name="GetCompanyListBySystemAndServiceRq") LtSystemAndServiceRqWSDTO rq) throws Exception{
        LtCompany company = new LtCompany();
        LtCompanyListRsWSDTO dto = new LtCompanyListRsWSDTO();

        //Retrieve the list of companies and create the DTO
        try{
            dto.Company.addAll(company.getCompaniesBySystemAndService(rq.sourceSystemId,rq.serviceCode, rq.statusCode));
            dto.numberOfCompanies = dto.Company.size();

        }catch(Exception e){
            throw e;
        }

        dto.status = 0;
        return dto;
    }

        /**
     * Purpose: Given a Source System Id, Service Code, and Service Status,as well as a number of companies to return, this will return all companies that match the
     * requested criteria up to the number specified by the <code>limit</code>.
     *
     * @param rq - <code>LtSystemAndServiceRqWSDTO</code> structure containing the parameters for the query
     * @return  <code>LtCompanyWSDTO</code> containing all companies matching the request criteria
     * @throws Exception runtime exception
     */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtCompanyListRsWSDTO getCompaniesBySystemAndServiceLimited(@WebParam(name="GetCompanyListBySystemAndServiceRq") LtSystemAndServiceRqWSDTO rq,
                                                                      @WebParam(name="limit") int limit) throws Exception{

        if (limit == 0){
            throw new RuntimeException("Limit = 0.  Limit must be a non-zero possitive number");
        }

        LtCompany company = new LtCompany();
        LtCompanyListRsWSDTO dto = new LtCompanyListRsWSDTO();

        //Retrieve the list of companies and create the DTO
        try{
            dto.Company.addAll(company.getCompaniesBySystemAndServiceLimitReturn(rq.sourceSystemId,rq.serviceCode, rq.statusCode, limit));
            dto.numberOfCompanies = dto.Company.size();

        }catch(Exception e){
            throw e;
        }

        dto.status = 0;
        return dto;
    }


    /**
     * Given a list (1 or more) of company FEINs (comma seperated) this method returns information for each FEIN submitted
     * @param pFeins String of 1 or more comma separated FEINs
     * @return <code>LtCompanyWSDTO</code> containing company information for each VALID FEIN submitted
     * @throws Exception runtime exception
     */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtCompanyListRsWSDTO getCompanysByFein(@WebParam(name = "FEIN") String pFeins) throws Exception {

        ArrayList<LtCompanyDTO> companyList = new ArrayList<LtCompanyDTO>();
        LtCompanyListRsWSDTO dto = new LtCompanyListRsWSDTO();
        LtCompany companyInfo = new LtCompany();
        StringBuilder invalidPsids = new StringBuilder("The following FEINs are invalid: ");

        //Set initial Status
        dto.status = 0;

        //Validate input string
        if(pFeins.length() == 0){
            dto.status = 2;
            dto.message = "Input string is empty";
            return dto;
        }

        //Tokenize the input string
        String[] feinList = pFeins.split(",");

        //Request All companies for each FEIN - iterate through the array
        for (String fein : feinList){

            //Handle empty strings
            if (fein.length() > 0){
                companyList = companyInfo.getCompaniesByFEIN(fein);
            }

            //Store the invalid FEIN to return to the user
            if (companyList.size() == 0){
                dto.status = 1;
                invalidPsids.append(fein).append(",");
            }else{
                dto.Company.addAll(companyList);
            }

        }

        //Set the number of companies found
        dto.numberOfCompanies = dto.Company.size();

        //Set Status Message
        if (dto.status == 0){
            dto.message = "Success";
        }else{
            dto.message = invalidPsids.toString();
        }

        return dto;

    }

    /**
     * Given a list (1 or more) of company PSIDs (comma seperated) this method returns information for each PSID submitted
     * @param pPsids String of 1 or more comma separated PSIDs
     * @return <code>LtCompanyWSDTO</code> containing company information for each VALID PSID submitted
     * @throws Exception runtime exception
     */
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public LtCompanyListRsWSDTO getCompanysByPSID(@WebParam(name = "PSID") String pPsids) throws Exception {

        ArrayList<LtCompanyDTO> companyList = new ArrayList<LtCompanyDTO>();
        LtCompanyListRsWSDTO dto = new LtCompanyListRsWSDTO();
        LtCompany companyInfo = new LtCompany();
        StringBuilder invalidPsids = new StringBuilder("The following PSIDs are invalid: ");

        //Set initial status
        dto.status = 0;

        //Validate input string
        if(pPsids.length() == 0){
            dto.status = 2;
            dto.message = "PSID field is empty";
            return dto;
        }

        //Tokenize the input string
        String[] psidList = pPsids.split(",");

        //Request All companies for each PSID - iterate through the array
        for (String psid : psidList){

            //Handle empty strings
            if (psid.length() > 0){
                companyList = companyInfo.getCompaniesByPSID(psid);
            }

            //Store invalid PSID's to be returned to the user
            if (companyList.size() == 0){
                dto.status = 1;
                invalidPsids.append(psid).append(",");
            }else{
                dto.Company.addAll(companyList);
            }
        }

        //Set the number of companies found
        dto.numberOfCompanies = dto.Company.size();

        //Set Status Message
        if (dto.status == 0){
            dto.message = "Success";
        }else{
            dto.message = invalidPsids.toString();
        }

        return dto;

    }

    //Get the last payroll transmission for the given company
    @WebMethod
    @WebResult(name="LtAdapterWS")
    public String getLatestCompanyPayrollTransmission(@WebParam(name="GetCompanyTransmission") LtTransmissionRqWSDTO rq) throws Exception {
        String transactionToReturn = "";
        LtCompany ltcompany = new LtCompany();

        String rawTransaction = ltcompany.getLastTransmissionByTypeSecondary(rq.getSourceCompanyId(), rq.getSourceSystemCd(), rq.getTransmissionType());

        if (rawTransaction.equals("")){
            transactionToReturn = "no transactions to return";
        }else{
            transactionToReturn = OFXToXML.convert(rawTransaction);
        }

        return transactionToReturn;
    }
}

package com.intuit.sbd.payroll.psp.adapters.lt;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.*;

/**
 * Collection of Company specific helper functions (queries).  All public methods return an array of <code>LtCompanyDTO</code>
 * except <code>getCompany</code> which returns a single containing <code>LtCompanyDTO</code>. This structure contains
 * the following information:
 * <ul>
 * <li>Company ID
 * <li>FEIN
 * <li>Company Name
 * <li>Bank Account
 * <li>Source System Id
 * <li>Current Token - not meaningfull for QBOE companies
 * </ul>
 */
public class LtCompany {

    /**
     * Purpose: Given a companyId and sourceSystemId, this will return a structure contatining the necessary information
     * to run a payroll.
     * @param companyId - PSId of the company to query for
     * @param sourceSystemCd - system code for the company
     * @return LtCompanyDTO - contains the nessesary company info
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
    public LtCompanyDTO getCompany(String companyId, SourceSystemCode sourceSystemCd)throws Exception{
        //Create the DTO to return
        LtCompanyDTO dto; // = new LtCompanyDTO();

        //Validate the companyId isn't empty
        if (companyId == null || companyId.trim().length() == 0) {
            throw new RuntimeException("No companyId is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            //Find the company object using the companyFinder
            Company co = Company.findCompany(companyId, sourceSystemCd);
            //Validate the returned Company object
            if (co == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemId");
            }

            //Build the DTO with the nessesary info
            dto = this.buildDTO(co);

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
         }finally{
            PayrollServices.rollbackUnitOfWork();
        }

      //Return the completed DTO
      return dto;
    }


    /**
     * purpose: Given a sourceSystemId, this will build and return an ArrayList<LtCompanyWSDTO> of all the companies
     *          in the database for the requested system that match the status passed in.
     * @param sourceSystemId - system whos companies are to be retrieved (ie, QBOE)
     * @param pBaStatus - Bank account status to query for
     * @return ArrayList<LtCompanyWSDTO> - the built list of companies
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
    public ArrayList<LtCompanyDTO> getCompanyList(SourceSystemCode sourceSystemId, String pBaStatus)throws Exception{

        //Create and empty ArrayList<LtCompanyDTO>
        ArrayList<LtCompanyDTO> dtoList = new ArrayList<LtCompanyDTO>();

        try{
            PayrollServices.beginUnitOfWork();

        String[] paramNames = new String[2];
        paramNames[0] = "statusCd";
        paramNames[1] = "sourceSystemCd";

        Object[] paramValues = new Object[2];
        paramValues[0] = BankAccountStatus.Active;
        paramValues[1] = sourceSystemId;

            DomainEntitySet<CompanyBankAccount> baList = Application.findByNamedQuery("findCompaniesBySourceSystemAndBaStatus",paramNames,paramValues);
            
            //Process each company in the list and build the WSDTO
            for (CompanyBankAccount ba : baList){
                LtCompanyDTO tempCo = buildDTO(ba);

                //Add the company to the list for return
                dtoList.add(tempCo);
            }

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
         }finally{
            PayrollServices.rollbackUnitOfWork();
        }
        //Return the ArrayList<LtCompanyWSDTO>
        return dtoList;
    }

    /**
     * purpose: Given a sourceSystemId and Service Code, this will build and return an <code>ArrayList<LtCompanyWSDTO></code> of all the companies
     *          in the database for the requested system that have the specified service, in the requested status
     * @param pSourceSystem - system whos companies are to be retrieved (ie, QBOE)
     * @param pServiceCode - Service the companies should have
     * @param pStatusCode - Status the service should be in
     * @return ArrayList<LtCompanyWSDTO> The lisst of companies matching the criteria
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
   public ArrayList<LtCompanyDTO>getCompaniesBySystemAndService(SourceSystemCode pSourceSystem, ServiceCode pServiceCode, ServiceSubStatusCode pStatusCode)throws Exception{

        //Create an empty ArrayList<LtCompanyDTO>
        ArrayList<LtCompanyDTO> dtoList = new ArrayList<LtCompanyDTO>();

        try{
            PayrollServices.beginUnitOfWork();

            Expression<CompanyService> query =
                    new Query<CompanyService>()
                            .Where(CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                                    .And(CompanyService.StatusCd().equalTo(pStatusCode))
                                    .And(CompanyService.Company().SourceSystemCd().equalTo(pSourceSystem)))
                            .EagerLoad(CompanyService.Company(), CompanyService.Company().CompanyBankAccountSet());

            DomainEntitySet<CompanyService> coServiceList = Application.find(CompanyService.class, query);

            for (CompanyService cs : coServiceList){
                LtCompanyDTO tempCo = buildDTO(cs);

                //Add the company to the list for return
                dtoList.add(tempCo);
            }

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
         }finally{
            PayrollServices.rollbackUnitOfWork();
        }

        //Return the ArrayList<LtCompanyRsWSDTO>
        return dtoList;
    }

        /**
     * purpose: Given a sourceSystemId, Service Code, Subscription status code, and a limit, this will build and return
     * <code>ArrayList<LtCompanyWSDTO></code> of all the companies in the database
     * up to the spcified limit for the requested system that have the specified service, in the requested status
     * @param pSourceSystem - system whos companies are to be retrieved (ie, QBOE)
     * @param pServiceCode - Service the companies should have
     * @param pStatusCode - Status the service should be in
     * @param pLimit - Maximum number of companies to return
     * @return ArrayList<LtCompanyWSDTO> The lisst of companies matching the criteria
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
   public ArrayList<LtCompanyDTO>getCompaniesBySystemAndServiceLimitReturn(SourceSystemCode pSourceSystem,
                                                                           ServiceCode pServiceCode,
                                                                           ServiceSubStatusCode pStatusCode,
                                                                           int pLimit)throws Exception{

        //Create an empty ArrayList<LtCompanyDTO>
        ArrayList<LtCompanyDTO> dtoList = new ArrayList<LtCompanyDTO>();

        try{
            PayrollServices.beginUnitOfWork();

            //Get ALL companies matching the SourceSystem, ServiceCode, and StatusCode
            Expression<CompanyService> countQuery =
                    new Query<CompanyService>()
                            .Select(CompanyService.Id().Count())
                            .Where(CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                                    .And(CompanyService.StatusCd().equalTo(pStatusCode))
                                    .And(CompanyService.Company().SourceSystemCd().equalTo(pSourceSystem)))
                            .EagerLoad(CompanyService.Company(), CompanyService.Company().CompanyBankAccountSet());


            ArrayList c = Application.executeQuery(CompanyService.class, countQuery);
            int count = Integer.parseInt(c.get(0).toString());
            System.out.println("Total CompanyService count matching criteria: " + count);

            int offset;
            int max;

            if(count <= pLimit){
                offset = 0;
                max = count;
            }else{
                int choices = count / pLimit;
                offset = pLimit * ((((int) ((choices) * Math.random()) + 1)) - 1);
                max = pLimit;
            }

            System.out.println("Query Recordset Offset: " + offset);
            System.out.println("Query Max Return: " + max);

                Expression<CompanyService> query =
                    new Query<CompanyService>()
                            .Where(CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                                    .And(CompanyService.StatusCd().equalTo(pStatusCode))
                                    .And(CompanyService.Company().SourceSystemCd().equalTo(pSourceSystem)))
                            .EagerLoad(CompanyService.Company()).LimitResults(offset, max);

            DomainEntitySet<CompanyService> coServiceList = Application.find(CompanyService.class, query);

            //Build the DTW and add the bank account info
            for (CompanyService cs : coServiceList){
                LtCompanyDTO tempCo = buildDTO(cs);
                
                //Add the company to the list for return
                dtoList.add(tempCo);
            }

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            throw new RuntimeException(ex);
         }finally{
            PayrollServices.rollbackUnitOfWork();
        }

        return dtoList;
    }

    /**
     * purpose: Given a Payroll Service ID, this will build and return an <code>ArrayList<LtCompanyWSDTO></code> of all the companies
     *          in the database for the requested PSID - generally there should only be one.
     * @param pPsid - PSID of the company to retrieve
     * @return ArrayList<LtCompanyWSDTO> the company with the specified PSID
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
    public ArrayList<LtCompanyDTO> getCompaniesByPSID(String pPsid)throws Exception{

        //Create an empty ArrayList<LtCompanyDTO> to be returned
        ArrayList<LtCompanyDTO> dtoList = new ArrayList<LtCompanyDTO>();

        try {
            PayrollServices.beginUnitOfWork();

            //Find the company object using the companyFinder
            DomainEntitySet<Company> coList = Company.searchCompaniesBySourceCompanyId(pPsid);

            //Validate the returned Company object
            if (coList == null) {
                throw new RuntimeException("Invalid PSID");
            }

            for (Company co : coList){
                LtCompanyDTO tempCo = buildDTO(co);

                //Add the company to the list for return
                dtoList.add(tempCo);
            }

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
         }finally{
            PayrollServices.rollbackUnitOfWork();
        }

      //Return the completed DTO
      return dtoList;
    }


    /**
     * purpose: Given a Federal Tax ID (FEIN), this will build and return an ArrayList<LtCompanyWSDTO> of all the companies
     *          in the database for the requested FEIN
     * @param pFein  - FEIN of the company to retrieve
     * @return ArrayList<LtCompanyWSDTO> - The companies with the supplied FEIN
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
    public ArrayList<LtCompanyDTO> getCompaniesByFEIN(String pFein)throws Exception{

        //Create an empty ArrayList<LtCompanyDTO> to return
        ArrayList<LtCompanyDTO> dtoList = new ArrayList<LtCompanyDTO>();

        try {
            PayrollServices.beginUnitOfWork();

            //Find the company object using the companyFinder
            DomainEntitySet<Company> coList = Company.searchCompaniesByEIN(pFein);

            //Validate the returned Company object
            if (coList == null) {
                throw new RuntimeException("Invalid FEIN");
            }

            for (Company co : coList){
                LtCompanyDTO tempCo = buildDTO(co);

                //Add the company to the list for return
                dtoList.add(tempCo);
            }

            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
         }finally{
            PayrollServices.rollbackUnitOfWork();
        }

      //Return the completed DTO
      return dtoList;
    }


    public String getLastTransmissionByTypeSecondary(String pPSID, SourceSystemCode pSourceSystem, TransmissionType pType)throws Exception{

        String namedQuery = "ltFindLastTransmissionSpecificType";
        Company co = Company.findCompany(pPSID, pSourceSystem);
        String lastPayrollTransmission = "";

        try{
            PayrollServices.beginUnitOfWorkWithSecondary();

            int lookBackMonths = SystemParameter.findIntValue(SystemParameter.Code.LAST_TRANSMISSION_LOOKBACK_MONTHS, 4);
            SpcfCalendar lowerDateBound = PSPDate.getPSPTime();
            lowerDateBound.addMonths(lookBackMonths * -1);

            // first search - try against most recent partitions only
            List<com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission> transmissions =
                    ApplicationSecondary.executeNamedQuery(namedQuery,
                            new String[]{"company", "lowerDateBound", "transType"},
                            new Object[]{co.getId(), lowerDateBound, pType});


            if (transmissions.size() == 0 && co.getCreatedDate().before(lowerDateBound)) {
                transmissions =
                        ApplicationSecondary.executeNamedQuery(namedQuery,
                                new String[]{"company", "lowerDateBound", "transType"},
                                new Object[]{co.getId(), co.getCreatedDate(), pType});
            }

            System.out.println("Transmission Count: " + transmissions.size());

            if (transmissions.size() > 0) {
                lastPayrollTransmission = transmissions.get(0).getRequestDocument();

            }


            PayrollServices.commitUnitOfWorkWithSecondary();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            PayrollServices.rollbackUnitOfWorkWithSecondary();
        }


        return lastPayrollTransmission;
    }


    /**
     * Purpose: Builds a single LtCompanyDTO from the Company object provided.
     *
     * @param pCoServ - Company object used to build the LtCompanyWSDTO
     * @return completed LtCompanyDTO for the provided company
     */
     private LtCompanyDTO buildDTO(CompanyService pCoServ){

        //Create an object that will be built up
        LtCompanyDTO coDTO   = new LtCompanyDTO();

        //Build the DTO with the nessesary info
        coDTO.companyId      = pCoServ.getCompany().getSourceCompanyId();
        coDTO.fein           = pCoServ.getCompany().getFedTaxId();
        coDTO.companyName    = pCoServ.getCompany().getDbaName();
        coDTO.sourceSystemId = pCoServ.getCompany().getSourceSystemCd();
        coDTO.token          = pCoServ.getCompany().getCurrentToken();
        coDTO.cloudToken     = pCoServ.getCompany().getCloudCurrentToken();

        // Test to ensure there is a bamk account that meets the criteria.
        // If no no bank accounts match, the following is thrown:
        //          java.util.NoSuchElementException
        try{
            CompanyBankAccount bankAccount = pCoServ.getCompany().getCompanyBankAccountCollection().find(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Active)).get(0);
            coDTO.bankAccount = bankAccount.getSourceBankAccountId();
        }catch (NoSuchElementException e){
            coDTO.bankAccount = "No Bank Account";
        }

        return coDTO;
    }

    /**
     * Purpose: Builds a single LtCompanyDTO from the Company object provided.
     *
     * @param pCo - Company object used to build the LtCompanyWSDTO
     * @return completed LtCompanyDTO for the provided company
     */
     private LtCompanyDTO buildDTO(Company pCo){

        //Create an object that will be built up
        LtCompanyDTO coDTO   = new LtCompanyDTO();

        //Build the DTO with the nessesary info
        coDTO.companyId      = pCo.getSourceCompanyId();
        coDTO.fein           = pCo.getFedTaxId();
        coDTO.companyName    = pCo.getDbaName();
        coDTO.sourceSystemId = pCo.getSourceSystemCd();
        coDTO.token          = pCo.getCurrentToken();
        coDTO.cloudToken     = pCo.getCloudCurrentToken();


        coDTO.bankAccount = getCompanyBA(pCo);
            if (coDTO.bankAccount == null){
                coDTO.bankAccount = "No Bank Account";
            }


        return coDTO;
    }
    /**
         * Purpose: Builds a single LtCompanyWSDTO from the CompanyBankAccount/Company object provided.
     *
     * @param ba - BankAccount object containing the company object to build the LtCompanyWSDTO
     * @return completed LtCompanyWSDTO for the provided company
     */
    public LtCompanyDTO buildDTO(CompanyBankAccount ba){

        //Create an object that will be built up
        //And temp bank account
        LtCompanyDTO coDTO   = new LtCompanyDTO();
        Company co = ba.getCompany();

        //Build the DTO with the nessesary info
        coDTO.companyId      = co.getSourceCompanyId();
        coDTO.fein           = co.getFedTaxId();
        coDTO.companyName    = co.getDbaName();
        coDTO.bankAccount    = ba.getSourceBankAccountId();
        coDTO.sourceSystemId = co.getSourceSystemCd();
        coDTO.token          = co.getCurrentToken();
        coDTO.cloudToken     = co.getCloudCurrentToken();

        return coDTO;
    }


    /**
     * Purpose: Given a Company object, this requests all bank accounts belonging to the company.  The sourceBankAccountId
     *          for the first bank account is returned.
     * @param co - PSID of the company who's bank accounts are to be returned
     * @return the bank account Id
     */
    private String getCompanyBA(Company co){
        //request the list of bank accounts with matching status for the company
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = Application.find(CompanyBankAccount.class, CompanyBankAccount.Company().equalTo(co));

        //Check to make sure there are bank accounts
        if (companyBankAccounts.size() == 0){
            return null;
        }

        //return the first account in the list
        return companyBankAccounts.get(0).getSourceBankAccountId();
    }


}

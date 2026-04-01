package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.CompanyAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAddCompany;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyAgencyPaymentTemplateAgencyId;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.RemoveFraudFlagCore;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.tools.ComplianceToolkit;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.PINUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.webservices.wsdto.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 11, 2008
 * Time: 3:08:12 PM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class CompanyWS {

    @WebMethod
    public String getPinSignature(@WebParam(name = "psid") String pPSID,
                                  @WebParam(name = "ein") String pEIN,
                                  @WebParam(name = "subscriptionNumber") String pSubscriptionNumber,
                                  @WebParam(name = "privateKey") String pPrivateKey) throws Exception {
        String encryptedPinSignature = null;

        try {
            if (pPSID == null || pPSID.length() == 0) {
                throw new Exception("PSID cannot be null or empty.");
            }
            if (pEIN == null || pEIN.length() == 0) {
                throw new Exception("EIN cannot be null or empty.");
            }
            if (pSubscriptionNumber == null || pSubscriptionNumber.length() == 0) {
                throw new Exception("SubscriptionNum cannot be null or empty.");
            }
            if (pPrivateKey == null || pPrivateKey.length() == 0) {
                throw new Exception("PrivateKey cannot be null or empty.");
            }

            String quid = java.util.UUID.randomUUID().toString();
            String pinSignature = pEIN + ":" + pPSID + ":" + pSubscriptionNumber + ":" + quid;

            PrivateKey pk = PINUtils.getPrivateKeyFromString(pPrivateKey);
            encryptedPinSignature = PINUtils.getEncryptedValue(pinSignature, pk);

        } catch (Exception e) {
            throw e;
        }

        return encryptedPinSignature;
    }

    @WebMethod
    public void removeFraudFlag(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();

            RemoveFraudFlagCore pRemoveFraudFlag = new RemoveFraudFlagCore(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID );
            ProcessResult processResult =   PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID);

            if (!processResult.isSuccess()){
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @WebMethod
    public void setOffloadGroup(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                @WebParam(name = "offloadGroupCD") String offloadGroupCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (offloadGroupCD == null || offloadGroupCD.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCD is specified");
        }
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(offloadGroupCD);
            if (offloadGroup == null) {
                throw new RuntimeException("OffloadGroup with the specified code "+ offloadGroupCD  +" doesn't exists");
            }
            company.setOffloadGroup(offloadGroup);
            Application.save(company);
            PayrollServices.commitUnitOfWork();

        }catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void add401kService(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                @WebParam(name = "custodialId") String custodialId) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            CompanyService existing401kService = CompanyService.findCompanyService(company, ServiceCode.ThirdParty401k);
            if (existing401kService != null) {
                throw new RuntimeException("Company "+ sourceSystemCD +":" + sourceCompanyID  +" already exists on the 401k service");
            }
            ThirdParty401kServiceInfoDTO serviceInfoDTO = new ThirdParty401kServiceInfoDTO();
            serviceInfoDTO.setCustodialId(custodialId);
            serviceInfoDTO.setHasSafeHarbor(false);
            
            SpcfCalendar serviceStartDate = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(serviceStartDate, -1);
            CalendarUtils.clearTime(serviceStartDate);
            serviceInfoDTO.setServiceStartDate(serviceStartDate);

            ProcessResult processResult = PayrollServices.companyManager.addService(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, serviceInfoDTO);
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            PayrollServices.commitUnitOfWork();

        }catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addCheckDistributionCompany(@WebParam(name = "CompanyWSDTO") CompanyDTO pCompany) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Offering> offerings = PayrollServices.entityFinder.find(Offering.class, Offering.SKU().equalTo(ServiceCode.CheckDistribution.toString()));
            Offering offering = offerings.get(0);
            OfferingInfoDTO offeringInfoDTO = new OfferingInfoDTO();
            offeringInfoDTO.setSKU(offering.getSKU());

            ProcessResult<Company> companyPR = PayrollServices.companyManager.addCompany(pCompany);

            if (!companyPR.isSuccess()) {
                Message msg = companyPR.getMessages().get(0);
                throw new Exception("Error adding company - Message Code: " + msg.getMessageCode() + " Message: " + msg.getMessage());
            }

            Company company = companyPR.getResult();

            ServiceInfoDTO serviceInfoDTO = new CheckDistributionServiceInfoDTO();
            Long lastPaycheckId = 0l;
            ((CheckDistributionServiceInfoDTO)serviceInfoDTO).setLastPaycheckId(lastPaycheckId);
            serviceInfoDTO.setServiceCode(ServiceCode.CheckDistribution);

            ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), serviceInfoDTO);

            if (!servicePR.isSuccess()) {
                Message msg = servicePR.getMessages().get(0);
                throw new Exception("Error adding service - Message Code: " + msg.getMessageCode() + " Message: " + msg.getMessage());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String updateServiceStatus(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "serviceCD") String serviceCD,
                                      @WebParam(name = "serviceStatusCD") String serviceStatusCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (serviceCD == null) {
            throw new RuntimeException("No serviceCD is specified");
        }

        if (serviceStatusCD == null) {
            throw new RuntimeException("No serviceStatusCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateServiceStatus(
                                                            SourceSystemCode.valueOf(sourceSystemCD),
                                                            sourceCompanyID,
                                                            ServiceCode.valueOf(serviceCD),
                                                            ServiceSubStatusCode.valueOf(serviceStatusCD));
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            CompanyService companyService = processResult.getResult();
            String serviceStatus = companyService.getStatusCd().toString();
            PayrollServices.commitUnitOfWork();
            return serviceStatus;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public TaxCompanyServiceInfoWSDTO queryTaxCompanyServiceInfo(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                              @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {

        TaxCompanyServiceInfoWSDTO response = new TaxCompanyServiceInfoWSDTO();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            TaxCompanyServiceInfo taxService = (TaxCompanyServiceInfo) company.getService(ServiceCode.Tax);
            if (taxService == null) {
                throw new RuntimeException("Company is not on the tax service");
            }

            response.lastQuarterToFile = taxService.getLastQuarterToFile();
            response.fileAnnualReturns = taxService.getFileAnnualReturns();
            response.finalAnnualReturns = taxService.getFinalAnnualReturns();
            if (taxService.getW2DeliveryPreferenceCd() != null) {
                response.w2DeliveryPref = taxService.getW2DeliveryPreferenceCd().toString();
            }
            if (taxService.getClientPacketDeliveryPreferenceCd() != null) {
                response.clientPacketDeliveryPref = taxService.getClientPacketDeliveryPreferenceCd().toString();
            }
            response.lastTaxYear = taxService.getLastTaxYear();
            if (taxService.getLastPayrollDate() != null) {
                response.lastPayrollDate = CalendarUtils.convertToCalendar(taxService.getLastPayrollDate());
            }
            response.inHouseW2 = taxService.getInHouseW2();
            response.includeOnSsaFile = taxService.getIncludeOnSSAFile();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @WebMethod
    public TaxCompanyServiceInfoWSDTO updateTaxCompanyServiceInfo(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                              @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                              @WebParam(name = "taxCompanyServiceInfo") TaxCompanyServiceInfoWSDTO taxCompanyServiceInfoWSDTO) throws Exception {

        TaxCompanyServiceInfoWSDTO response = new TaxCompanyServiceInfoWSDTO();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (taxCompanyServiceInfoWSDTO == null) {
            throw new RuntimeException("No taxCompanyServiceInfo is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            TaxCompanyServiceInfo taxService = (TaxCompanyServiceInfo) CompanyService.findCompanyService(
                    SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, ServiceCode.Tax);
            if (taxService == null) {
                throw new RuntimeException("Company is not on the tax service");
            }

            TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(taxService);

            taxServiceInfoDTO.setIncludeOnSsaFile(taxCompanyServiceInfoWSDTO.includeOnSsaFile);
            taxServiceInfoDTO.setInHouseW2(taxCompanyServiceInfoWSDTO.inHouseW2);
            taxServiceInfoDTO.setLastTaxYear(taxCompanyServiceInfoWSDTO.lastTaxYear);
            taxServiceInfoDTO.setClientPacketDeliveryPreferenceCd(DeliveryPreferenceCode.valueOf(taxCompanyServiceInfoWSDTO.clientPacketDeliveryPref));
            taxServiceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.valueOf(taxCompanyServiceInfoWSDTO.w2DeliveryPref));
            taxServiceInfoDTO.setFinalAnnualReturns(taxCompanyServiceInfoWSDTO.finalAnnualReturns);
            taxServiceInfoDTO.setFileAnnualReturns(taxCompanyServiceInfoWSDTO.fileAnnualReturns);
            taxServiceInfoDTO.setLastQuarterToFile(taxCompanyServiceInfoWSDTO.lastQuarterToFile);

            ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateService(SourceSystemCode.valueOf(
                    sourceSystemCD), sourceCompanyID, taxServiceInfoDTO);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            taxService = (TaxCompanyServiceInfo) processResult.getResult();

            response.lastQuarterToFile = taxService.getLastQuarterToFile();
            response.fileAnnualReturns = taxService.getFileAnnualReturns();
            response.finalAnnualReturns = taxService.getFinalAnnualReturns();
            if (taxService.getW2DeliveryPreferenceCd() != null) {
                response.w2DeliveryPref = taxService.getW2DeliveryPreferenceCd().toString();
            }
            if (taxService.getClientPacketDeliveryPreferenceCd() != null) {
                response.clientPacketDeliveryPref = taxService.getClientPacketDeliveryPreferenceCd().toString();
            }
            response.lastTaxYear = taxService.getLastTaxYear();
            if (taxService.getLastPayrollDate() != null) {
                response.lastPayrollDate = CalendarUtils.convertToCalendar(taxService.getLastPayrollDate());
            }
            response.inHouseW2 = taxService.getInHouseW2();
            response.includeOnSsaFile = taxService.getIncludeOnSSAFile();

            PayrollServices.commitUnitOfWork();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }


    @WebMethod
    public String queryServiceStatus(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "serviceCD") String serviceCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (serviceCD == null) {
            throw new RuntimeException("No serviceCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Invalid sourceCompanyID specified");
            }

            DomainEntitySet<CompanyService> companyServices =
                    CompanyService.findCompanyServicesBySourceCompanyId(
                                               SourceSystemCode.valueOf(sourceSystemCD), ServiceCode.valueOf(serviceCD), sourceCompanyID);
            CompanyService companyService = null;
            if (null != companyServices && companyServices.size() > 0) {
                companyService = companyServices.get(0);
            }else {
                throw new RuntimeException("Company not associated with the specified serviceCD");
            }

            String serviceStatus = companyService.getStatusCd().toString();
            PayrollServices.commitUnitOfWork();
            return serviceStatus;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<CompanyDailyLiabilityWSDTO> queryCompanyDailyLiabilities(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            PayrollServices.beginUnitOfWork();

            Expression<CompanyDailyLiability> query = null;

            if (sourceCompanyID!=null && sourceSystemCD!=null && SourceSystemCode.valueOf(sourceSystemCD)!=null) {
                Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

                if (null == company) {
                    throw new RuntimeException("Invalid sourceCompanyID specified");
                }
                
                query =
                    new Query<CompanyDailyLiability>()
                            .Where(CompanyDailyLiability.Company().equalTo(company))
                            .OrderBy(CompanyDailyLiability.Company().Descending(), CompanyDailyLiability.LiabilityDate().Descending(), CompanyDailyLiability.Law().Descending());
            } else {
                query =
                        new Query<CompanyDailyLiability>()
                                .OrderBy(CompanyDailyLiability.Company().Descending(), CompanyDailyLiability.LiabilityDate().Descending(), CompanyDailyLiability.Law().Descending());
            }

            DomainEntitySet<CompanyDailyLiability> cdlList = Application.find(CompanyDailyLiability.class, query);

            Collection<CompanyDailyLiabilityWSDTO> companyDailyLiabilities = buildCompanyDailyLiabilityDTOs(cdlList);
            PayrollServices.commitUnitOfWork();
            return companyDailyLiabilities;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<ATFPayrollToProcessWSDTO> queryATFPayrollsToProcess(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            PayrollServices.beginUnitOfWork();

            Expression<ATFPayrollsToProcess> query = null;

            if (sourceCompanyID!=null && sourceSystemCD!=null && SourceSystemCode.valueOf(sourceSystemCD)!=null) {
                Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

                if (null == company) {
                    throw new RuntimeException("Invalid sourceCompanyID specified");
                }

                query =
                    new Query<ATFPayrollsToProcess>()
                            .Where(ATFPayrollsToProcess.PayrollRun().Company().equalTo(company))
                            .OrderBy(ATFPayrollsToProcess.CreatedDate().Descending());
            } else {
                query =
                        new Query<ATFPayrollsToProcess>()
                                .OrderBy(ATFPayrollsToProcess.CreatedDate().Descending());
            }

            DomainEntitySet<ATFPayrollsToProcess> atfPayrollsList = Application.find(ATFPayrollsToProcess.class, query);

            Collection<ATFPayrollToProcessWSDTO> atfPayrollsWSDTO = buildATFPayrollsToProcessWSDTO(atfPayrollsList);
            PayrollServices.commitUnitOfWork();
            return atfPayrollsWSDTO;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<ATFPayrollToProcessWSDTO> buildATFPayrollsToProcessWSDTO(DomainEntitySet<ATFPayrollsToProcess> atfPayrollsList) {
        Collection<ATFPayrollToProcessWSDTO> p2pWSDTOList = new ArrayList<ATFPayrollToProcessWSDTO>();
        ATFPayrollToProcessWSDTO p2pWSDTO = null;
        for (ATFPayrollsToProcess currentP2P : atfPayrollsList) {
            p2pWSDTO = new ATFPayrollToProcessWSDTO();
            p2pWSDTO.id = currentP2P.getId().toString();
            p2pWSDTO.modifiedDate = CalendarUtils.convertToDate(currentP2P.getModifiedDate().toLocal());
            p2pWSDTO.payrollGUID = currentP2P.getPayrollRun().getId().toString();
            p2pWSDTO.sourcePayrollRunId = currentP2P.getPayrollRun().getSourcePayRunId();
            p2pWSDTOList.add(p2pWSDTO);
        }
        return p2pWSDTOList;
    }

    @WebMethod
    public Collection<OnHoldReasonWSDTO> queryOnHoldReasons(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Invalid sourceCompanyID specified");
            }

            Expression<OnHoldReason> query =
                    new Query<OnHoldReason>()
                            .Where(OnHoldReason.Company().equalTo(company))
                            .OrderBy(OnHoldReason.EffectiveDate().Descending());

            DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);

            Collection<OnHoldReasonWSDTO> onHoldReasons = buildOnHoldReasonDTOs(onHoldReasonList);
            PayrollServices.commitUnitOfWork();
            return onHoldReasons;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<OnHoldReasonWSDTO> addOnHoldReason(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "onHoldReasonCD") String onHoldReasonCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (onHoldReasonCD == null) {
            throw new RuntimeException("No onHoldReasonCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Invalid sourceCompanyID specified");
            }
            ProcessResult<Company> processResult = PayrollServices.companyManager.addOnHoldReason(
                                                        SourceSystemCode.valueOf(sourceSystemCD),
                                                        sourceCompanyID,
                                                        ServiceSubStatusCode.valueOf(onHoldReasonCD));
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            Expression<OnHoldReason> query =
                    new Query<OnHoldReason>()
                            .Where(OnHoldReason.Company().equalTo(company)
                                   .And(OnHoldReason.ExpirationDate().isNull()))
                            .OrderBy(OnHoldReason.EffectiveDate().Descending());

            DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);

            Collection<OnHoldReasonWSDTO> onHoldReasons = buildOnHoldReasonDTOs(onHoldReasonList);
            PayrollServices.commitUnitOfWork();
            return onHoldReasons;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<OnHoldReasonWSDTO> removeOnHoldReason(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "onHoldReasonCD") String onHoldReasonCD) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (onHoldReasonCD == null) {
            throw new RuntimeException("No onHoldReasonCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Invalid sourceCompanyID specified");
            }
            
            ServiceSubStatus serviceSubStatus = PayrollServices.entityFinder.findById(ServiceSubStatus.class, ServiceSubStatusCode.valueOf(onHoldReasonCD));
            if (null == serviceSubStatus) {
                throw new RuntimeException("Invalid onHoldReasonCD specified");
            }
            ProcessResult<Company> processResult = PayrollServices.companyManager.removeOnHoldReason(
                                                                        SourceSystemCode.valueOf(sourceSystemCD),
                                                                        sourceCompanyID,
                                                                        serviceSubStatus.getServiceSubStatusCd());
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            Expression<OnHoldReason> query =
                    new Query<OnHoldReason>()
                            .Where(OnHoldReason.Company().equalTo(company)
                                   .And(OnHoldReason.ExpirationDate().isNull()))
                            .OrderBy(OnHoldReason.EffectiveDate().Descending());

            DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);

            Collection<OnHoldReasonWSDTO> onHoldReasons = buildOnHoldReasonDTOs(onHoldReasonList);
            PayrollServices.commitUnitOfWork();
            return onHoldReasons;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<CompanyWSDTO> queryCompanies() throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        Collection<CompanyWSDTO> companyDTOs=null;
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            companyDTOs = buildCompanyDTOs(companies);
            PayrollServices.commitUnitOfWork();
            return companyDTOs;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<CompanyWSDTO> queryCompaniesByLegalName(@WebParam(name = "pspCompanyID") String pPspCompanyId,
                                                              @WebParam(name = "companyLegalName") String pLegalName,
                                                              @WebParam(name = "shouldReturn401kInfo") Boolean pShouldReturn401kInfo,
                                                              @WebParam(name = "shouldReturnServices") Boolean pShouldReturnServices,
                                                              @WebParam(name = "shouldReturnAssistedInfo") Boolean pShouldReturnAssistedInfo,
                                                              @WebParam(name = "includeLiabilityChecks") Boolean pIncludeLiabilityChecks,
                                                              @WebParam(name = "includeApplicationId") Boolean pIncludeApplicationId) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        Collection<CompanyWSDTO> companyDTOs=null;
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = null;
            if (pShouldReturn401kInfo==null) {
                pShouldReturn401kInfo = false;
            }
            if (pShouldReturnServices == null) {
                pShouldReturnServices = false;
            }
            if (pShouldReturnAssistedInfo == null) {
                pShouldReturnAssistedInfo = false;
            }

            if(pPspCompanyId != null && pPspCompanyId.trim().length() > 0){
                companies = Company.searchCompaniesBySourceCompanyId(pPspCompanyId.trim());
            }else if(pLegalName != null && pLegalName.trim().length() > 0){
                companies = Company.searchCompaniesByLegalName(pLegalName.trim());
            }
            companyDTOs = buildCompanyDTOs(companies, pShouldReturn401kInfo, pShouldReturnServices, pShouldReturnAssistedInfo, pIncludeLiabilityChecks, pIncludeApplicationId);
            PayrollServices.commitUnitOfWork();
            return companyDTOs;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public CompanyWSDTO queryCompany(@WebParam(name = "pspCompanyID") String pPspCompanyId,
                                    @WebParam(name = "shouldReturn401kInfo") Boolean pShouldReturn401kInfo,
                                    @WebParam(name = "shouldReturnAssistedInfo") Boolean pShouldReturnAssistedInfo,
                                    @WebParam(name = "includeLiabilityChecks") Boolean pIncludeLiabilityChecks)  throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
         CompanyWSDTO companyDTO = null;
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(pPspCompanyId));

            if (pShouldReturn401kInfo==null) {
                pShouldReturn401kInfo = false;
            }
            if (pShouldReturnAssistedInfo == null) {
                pShouldReturnAssistedInfo = false;
            }

            if (company != null) {
                companyDTO= buildCompanyDTO(company, pShouldReturn401kInfo, pShouldReturnAssistedInfo, pIncludeLiabilityChecks, true);
            }
            PayrollServices.commitUnitOfWork();

            return companyDTO;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

     @WebMethod
    public CompanyWSDTO queryCompanyBySourceId(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                               @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                               @WebParam(name = "shouldReturn401kInfo") Boolean pShouldReturn401kInfo,
                                               @WebParam(name = "shouldReturnAssistedInfo") Boolean pShouldReturnAssistedInfo,
                                               @WebParam(name = "includeLiabilityChecks") Boolean pIncludeLiabilityChecks) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            if (pShouldReturn401kInfo==null) {
                pShouldReturn401kInfo = false;
            }
            if (pShouldReturnAssistedInfo == null) {
                pShouldReturnAssistedInfo = false;
            }

            CompanyWSDTO companyDTO = buildCompanyDTO(company, pShouldReturn401kInfo, pShouldReturnAssistedInfo, pIncludeLiabilityChecks, true);
            PayrollServices.commitUnitOfWork();
            return companyDTO;

        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<CompanyAgencyWSDTO> queryCompanyAgencies(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                            @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                            @WebParam(name = "includeInvalidDates") Boolean includeInvalidDates) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if(includeInvalidDates == null) {
            includeInvalidDates = false;
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            Expression<CompanyAgency> agencyQuery =
                new Query<CompanyAgency>()
                        .Where(CompanyAgency.Company().equalTo(company))
                        .OrderBy(CompanyAgency.Agency().AgencyId());
            DomainEntitySet<CompanyAgency> agencyList = Application.find(CompanyAgency.class, agencyQuery);
            Collection<CompanyAgencyWSDTO> companyAgencies =  buildCompanyAgencyDTOs(agencyList.sort(CompanyAgency.Agency().AgencyId()), includeInvalidDates);
            PayrollServices.commitUnitOfWork();
            return companyAgencies;

        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<EffectiveDepositFrequencyWSDTO> queryEffectiveDepositFrequencies(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                                                       @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                                                                       @WebParam(name = "paymentTemplateCd") String paymentTemplateCd,
                                                                                       @WebParam(name = "includeInvalidDates") Boolean includeInvalidDates) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (StringUtils.isEmpty(paymentTemplateCd)) {
            throw new RuntimeException("No payment template is specified");
        }

        if(includeInvalidDates == null) {
            includeInvalidDates = false;
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies =
                    Application.find(EffectiveDepositFrequency.class, EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(company)
                                                                                               .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCd)));

            List<EffectiveDepositFrequencyWSDTO> dtoList = new ArrayList<EffectiveDepositFrequencyWSDTO>();
            for (EffectiveDepositFrequency edf : effectiveDepositFrequencies) {
                EffectiveDepositFrequencyWSDTO wsEdf = new EffectiveDepositFrequencyWSDTO();
                wsEdf.depositFrequency = ObjectUtils.toString(edf.getPaymentTemplateFrequency().getPaymentFrequencyId());
                wsEdf.effectiveDate = CalendarUtils.convertToDate(edf.getEffectiveDate());
                wsEdf.paymentTemplate = edf.getPaymentTemplateFrequency().getPaymentTemplate().getPaymentTemplateCd();
                if(includeInvalidDates && edf.getInvalidDate() != null) {
                    wsEdf.invalidDate = CalendarUtils.convertToDate(edf.getInvalidDate());
                }
                dtoList.add(wsEdf);
            }

            return dtoList;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<EmployeeBankAccountWSDTO> queryEmployeeBankAccounts(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                               @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                               @WebParam(name = "sourceEmployeeID") String sourceEmployeeID,
                                               @WebParam(name = "shouldGetAssistedInfo") Boolean pShouldGetAssistedInfo) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        Collection<EmployeeBankAccountWSDTO> employeeBankAccountDTOs=null;
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (sourceEmployeeID == null || sourceEmployeeID.trim().length() == 0) {
            throw new RuntimeException("No sourceEmployeeID is specified");
        }

        if (pShouldGetAssistedInfo == null) {
            pShouldGetAssistedInfo = false;
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            DomainEntitySet<EmployeeBankAccount> ebaCollection = EmployeeBankAccount.findEmployeeBankAccounts
                        (company, sourceEmployeeID, null);

            employeeBankAccountDTOs = buildEmployeeBankAccountDTOs(ebaCollection, pShouldGetAssistedInfo);
            PayrollServices.commitUnitOfWork();
            return employeeBankAccountDTOs;

        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<EmployeeBankAccountWSDTO> buildEmployeeBankAccountDTOs(DomainEntitySet<EmployeeBankAccount> pEmployeeBankAccounts, boolean pShouldGetAssistedInfo) {
        ArrayList<EmployeeBankAccountWSDTO> employeeBankAccountDTOs = new ArrayList();
        for (EmployeeBankAccount employeeBankAccount : pEmployeeBankAccounts) {
            EmployeeBankAccountWSDTO employeeBankAccountWSDTO = buildEmployeeBankAccountDTO(employeeBankAccount, pShouldGetAssistedInfo);
            employeeBankAccountDTOs.add(employeeBankAccountWSDTO);
        }
        return employeeBankAccountDTOs;
    }

    private EmployeeBankAccountWSDTO buildEmployeeBankAccountDTO(EmployeeBankAccount pEmployeeBankAccount, boolean pShouldGetAssistedInfo) {
        EmployeeBankAccountWSDTO employeeBankAccountDTO = new EmployeeBankAccountWSDTO();
        employeeBankAccountDTO.id = pEmployeeBankAccount.getId().toString();
        employeeBankAccountDTO.sourceBankAccountId = pEmployeeBankAccount.getSourceBankAccountId();
        employeeBankAccountDTO.statusCode = ObjectUtils.toString(pEmployeeBankAccount.getStatusCd());
        employeeBankAccountDTO.bankAccount = buildBankAccountWSDTO(pEmployeeBankAccount.getBankAccount());
        if (pShouldGetAssistedInfo) {
            employeeBankAccountDTO.amount = pEmployeeBankAccount.getAmount();
            employeeBankAccountDTO.amountType = ObjectUtils.toString(pEmployeeBankAccount.getAmountType());
        }
        return employeeBankAccountDTO;
    }


    @WebMethod
    public Collection<EmployeeWSDTO> queryEmployeesByCompanyId(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                               @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                               @WebParam(name = "shouldReturn401kInfo") Boolean pShouldReturn401kInfo,
                                               @WebParam(name = "shouldReturnAssistedInfo") Boolean pShouldReturnAssistedInfo) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
         Collection<EmployeeWSDTO> employeeDTOs=null;
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (null == company) {
                throw new RuntimeException("Company not found");
            }
            DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Employee> employees = Employee.findEmployees(company);

            if (pShouldReturn401kInfo == null) {
                pShouldReturn401kInfo = false;
            }
            if (pShouldReturnAssistedInfo == null) {
                pShouldReturnAssistedInfo = false;
            }

            employeeDTOs = buildEmployeeDTOs(employees, pShouldReturn401kInfo, pShouldReturnAssistedInfo);
            PayrollServices.commitUnitOfWork();
            return employeeDTOs;

        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<EmployeeWSDTO> buildEmployeeDTOs(DomainEntitySet<com.intuit.sbd.payroll.psp.domain.Employee> pEmployees, Boolean shouldGet401kInfo, Boolean shouldGetAssistedInfo) throws Exception {
        ArrayList<EmployeeWSDTO> employeeDTOs = new ArrayList();
        if (shouldGetAssistedInfo == null || shouldGetAssistedInfo) {
            pEmployees = pEmployees.sort(Employee.SourceEmployeeId());
        }
        for (com.intuit.sbd.payroll.psp.domain.Employee employee : pEmployees) {
            EmployeeWSDTO employeeDTO = buildEmployeeDTO(employee, shouldGet401kInfo, shouldGetAssistedInfo);
            employeeDTOs.add(employeeDTO);
        }
        return employeeDTOs;
    }

    private EmployeeWSDTO buildEmployeeDTO(Employee pEmployee, boolean pShouldGet401kInfo, boolean pShouldGetAssistedInfo) {
        EmployeeWSDTO employeeDTO = new EmployeeWSDTO();
        employeeDTO.id=pEmployee.getId().toString();
        employeeDTO.sourceEmployeeId = pEmployee.getSourceEmployeeId();
        employeeDTO.statusCode = ObjectUtils.toString(pEmployee.getStatusCd());
        employeeDTO.firstName = pEmployee.getFirstName();
        employeeDTO.lastName = pEmployee.getLastName();
        employeeDTO.middleName = pEmployee.getMiddleName();
        if(pEmployee.getCommunicationTypePreference() != null) {
            employeeDTO.communicationTypePreference = pEmployee.getCommunicationTypePreference().name();
        }
        employeeDTO.email = pEmployee.getEmail();
        if (pEmployee.getEmployeeWagePlanCollection() != null) {
            employeeDTO.employeeWagePlanCollection = buildEEWagePlanCollection(pEmployee.getEmployeeWagePlanCollection());
        }
        if (pEmployee.getEmployeeBankAccountCollection() != null) {
            employeeDTO.employeeBankAccountCollection = buildEmployeeBankAccountDTOs(pEmployee.getEmployeeBankAccountCollection(), pShouldGetAssistedInfo);
        }
        if (pEmployee.getMailingAddress() != null) {
            employeeDTO.mailingAddress = buildAddressWSDTO(pEmployee.getMailingAddress());
        }
        employeeDTO.fedAllowances = pEmployee.getFedAllowances();
        employeeDTO.fedFilingStatus = pEmployee.getFedFilingStatus();
        if (pEmployee.getGenderCd() != null) {
            employeeDTO.genderCode = pEmployee.getGenderCd().name();
        }
        employeeDTO.hasRetirementPlan = pEmployee.getHasRetirementPlan();
        employeeDTO.hasThirdPartySickPay = pEmployee.getHasThirdPartySickPay();
        if (pEmployee.getHireDate() != null) {
            employeeDTO.hireDate = CalendarUtils.convertToDate(pEmployee.getHireDate());
        }
        employeeDTO.isStatutory = pEmployee.getIsStatutory();
        employeeDTO.phone = pEmployee.getPhone();
        if (pEmployee.getReHireDate() != null) {
            employeeDTO.reHireDate = CalendarUtils.convertToDate(pEmployee.getReHireDate());
        }


        for (EmployeeTax employeeTax : pEmployee.getEmployeeTaxCollection()) {
            if(employeeTax.getTaxType() == EmployeeTaxType.SIT) {
                employeeDTO.stateAllowances = employeeTax.getAllowances();
                employeeDTO.stateFilingStatus = employeeTax.getFilingStatus();
                break;
            }
        }        


        if (pEmployee.getStatusEffectiveDate() != null) {
            employeeDTO.statusEffectiveDate = CalendarUtils.convertToDate(pEmployee.getStatusEffectiveDate());
        }
        employeeDTO.suffix = pEmployee.getSuffix();
        employeeDTO.taxId = pEmployee.getTaxId();
        if (pEmployee.getTerminationDate() != null) {
            employeeDTO.terminationDate = CalendarUtils.convertToDate(pEmployee.getTerminationDate());
        }
        employeeDTO.workState = pEmployee.getWorkState();

        if (pShouldGet401kInfo) {
            if (pEmployee.getThirdParty401kInfo()!=null) {
                ThirdParty401kEmployeeInfoWSDTO tp401kInfo = new ThirdParty401kEmployeeInfoWSDTO();
                if (pEmployee.getBirthDate()!=null) {
                    tp401kInfo.birthDate = CalendarUtils.convertToDate(pEmployee.getBirthDate());
                }
                tp401kInfo.isFamilyMember = pEmployee.getThirdParty401kInfo().getIsFamilyMember();
                tp401kInfo.isHighlyCompensatedEmployee = pEmployee.getThirdParty401kInfo().getIsHighlyCompensated();
                tp401kInfo.ownershipPercentage = new BigDecimal(pEmployee.getThirdParty401kInfo().getOwnershipPercentage());
                employeeDTO.tp401kEEInfo = tp401kInfo;
            }
        }

        if (pShouldGetAssistedInfo) {
            setAssistedEmployeeInfo(pEmployee, employeeDTO);        
        }

        return employeeDTO;
    }

    private void setAssistedEmployeeInfo(Employee employee, EmployeeWSDTO wsEmployee) {
        wsEmployee.employeePayrollItemInfo = new ArrayList<EmployeePayrollItemWSDTO>();
        for (EmployeePayrollItem item : employee.getEmployeePayrollItemCollection().sort(EmployeePayrollItem.CompanyPayrollItem().SourcePayrollItemId(), EmployeePayrollItem.Amount())) {
            EmployeePayrollItemWSDTO wsItem = new EmployeePayrollItemWSDTO();
            wsItem.amount = item.getAmount();
            wsItem.amountType = ObjectUtils.toString(item.getAmountType());
            wsItem.limit = item.getItemLimit();
            wsItem.limitType = ObjectUtils.toString(item.getLimitType());
            wsItem.paylineType = ObjectUtils.toString(item.getType().toString());
            wsEmployee.employeePayrollItemInfo.add(wsItem);
        }

        wsEmployee.employeeTaxInfo = new ArrayList<EmployeeTaxWSDTO>();
        for (EmployeeTax tax : employee.getEmployeeTaxCollection().sort(EmployeeTax.CompanyLaw().SourceId(), EmployeeTax.Allowances())) {
            EmployeeTaxWSDTO wsTax = new EmployeeTaxWSDTO();
            wsTax.subjectTo = tax.getSubjectTo();
            wsTax.taxLawVersion = tax.getTaxLawVersion();
            wsTax.taxType = ObjectUtils.toString(tax.getTaxType().toString());
            wsTax.w2Name = tax.getW2Name();
            wsTax.extraWithholding = new BigDecimal(tax.getExtraWithholding());
            wsTax.taxTableMiscData = new ArrayList<String>();
            wsTax.companyLaw = tax.getCompanyLaw().getId().toString();
            for (TaxTableMiscData data : tax.getTaxTableMiscDataCollection()) {
                wsTax.taxTableMiscData.add(data.getValue());
            }
            wsEmployee.employeeTaxInfo.add(wsTax);
        }

        wsEmployee.employeeAccrualInfo = new ArrayList<EmployeeAccrualWSDTO>();
        for (EmployeeAccrual accrual : employee.getEmployeeAccrualCollection().sort(EmployeeAccrual.Hours())){
            EmployeeAccrualWSDTO wsAccrual = new EmployeeAccrualWSDTO();
            wsAccrual.accrualPeriod = ObjectUtils.toString(accrual.getAccrualPeriod());
            wsAccrual.accrualType = ObjectUtils.toString(accrual.getAccrualType());
            wsAccrual.hours = accrual.getHours();
            wsAccrual.hoursPerPeriod = accrual.getHoursPerPeriod();
            wsAccrual.maxHours = accrual.getMaxHours();
            wsAccrual.newYearReset = accrual.getNewYearReset();
            wsEmployee.employeeAccrualInfo.add(wsAccrual);
        }

        wsEmployee.employeeCustomFieldInfo = new ArrayList<EmployeeCustomFieldWSDTO>();
        for (EmployeeCustomField field : employee.getEmployeeCustomFieldCollection().sort(EmployeeCustomField.Name())) {
            EmployeeCustomFieldWSDTO wsField = new EmployeeCustomFieldWSDTO();
            wsField.name = field.getName();
            wsField.value = field.getValue();
            wsEmployee.employeeCustomFieldInfo.add(wsField);
        }

        QbdtEmployeeInfo qbdtInfo = employee.getQbdtEmployeeInfo();
        QBDTEmployeeInfoWSDO wsQbdtInfo = new QBDTEmployeeInfoWSDO();
        wsQbdtInfo.altPhone = qbdtInfo.getAltPhone();
        wsQbdtInfo.billPayAccount = qbdtInfo.getBillPayAccount();
        wsQbdtInfo.employeeType = ObjectUtils.toString(qbdtInfo.getEmployeeType());
        wsQbdtInfo.enforceSubjectTo = qbdtInfo.getEnforceSubjectTo();
        wsQbdtInfo.initials = qbdtInfo.getInitials();
        wsQbdtInfo.isDeleted = qbdtInfo.getIsDeleted();
        wsQbdtInfo.title = qbdtInfo.getTitle();
        wsQbdtInfo.trackingClass = qbdtInfo.getTrackingClass();
        wsQbdtInfo.useTime = qbdtInfo.getUseTime();
        wsEmployee.qbdtEmployeeInfo = wsQbdtInfo;


        AssistedEmployeeInfoWSDTO wsAssistedInfo = new AssistedEmployeeInfoWSDTO();
        wsAssistedInfo.fedExtraWithholding = SpcfUtils.convertToBigDecimal(employee.getFedExtraWithholding());
        wsAssistedInfo.isDeceased = employee.getIsDeceased();
        wsAssistedInfo.liveState = employee.getLiveState();
        wsAssistedInfo.qualifiesForAeic = employee.getQualifiesForAeic();
        wsEmployee.assistedEmployeeInfo = wsAssistedInfo;

        
    }

    private Collection<EmployeeWagePlanWSDTO> buildEEWagePlanCollection(DomainEntitySet<EmployeeWagePlan> pEmployeeWagePlanCollection) {
        return buildEEWagePlanCollection(pEmployeeWagePlanCollection, false);
    }

    private Collection<EmployeeWagePlanWSDTO> buildEEWagePlanCollection(DomainEntitySet<EmployeeWagePlan> pEmployeeWagePlanCollection, boolean pNeedAllDetails) {
        Collection<EmployeeWagePlanWSDTO> eeWagePlans = new ArrayList<EmployeeWagePlanWSDTO>();
        for (EmployeeWagePlan employeeWagePlan:pEmployeeWagePlanCollection) {
            EmployeeWagePlanWSDTO eeWagePlan = new EmployeeWagePlanWSDTO();
            if (employeeWagePlan.getName() != null) {
                eeWagePlan.wagePlanName = employeeWagePlan.getName().name();
            }
            eeWagePlan.state = employeeWagePlan.getState();
            eeWagePlan.wagePlanValue = employeeWagePlan.getWagePlanValue();
            if(pNeedAllDetails) {
                eeWagePlan.description = employeeWagePlan.getDescription();
                eeWagePlan.rulesVersion = employeeWagePlan.getRulesVersion();
                if(employeeWagePlan.getWagePlanDomain() != null) {
                    eeWagePlan.wagePlanDomain = employeeWagePlan.getWagePlanDomain().toString();
                }
                if(employeeWagePlan.getInvalidDate() != null) {
                    eeWagePlan.invalidDate = employeeWagePlan.getInvalidDate().format("MM-dd-yyyy");
                }
            }
            eeWagePlans.add(eeWagePlan);
        }
        return eeWagePlans;
    }

    private Collection<CompanyWSDTO> buildCompanyDTOs(DomainEntitySet<Company> pCompanies) throws Exception {
        return buildCompanyDTOs(pCompanies, false, false, false, false, true);
    }

    private Collection<CompanyWSDTO> buildCompanyDTOs(DomainEntitySet<Company> pCompanies, boolean pShouldInclude401kData, boolean pReturnServices,
                                                      Boolean pShouldReturnAssistedInfo, Boolean pIncludeLiabilityChecks, Boolean pIncludeApplicationId) throws Exception {
        SortedMap<String, CompanyWSDTO> companyDTOs = new TreeMap<String, CompanyWSDTO>();
        String key;
        
        for (Company company : pCompanies) {
            CompanyWSDTO companyDTO = buildCompanyDTO(company, pShouldInclude401kData, pReturnServices, pShouldReturnAssistedInfo, pIncludeLiabilityChecks, pIncludeApplicationId);

            key = companyDTO.fedTaxId + companyDTO.ddStatus + companyDTO.legalName + companyDTO.sourceCompanyID;
            companyDTOs.put(key, companyDTO);
        }

        return companyDTOs.values();
    }

    private CompanyWSDTO buildCompanyDTO(Company pCompany) throws Exception {
        return buildCompanyDTO(pCompany, false, false, false, true);
    }

    private CompanyWSDTO buildCompanyDTO(Company pCompany, boolean pShouldInclude401kData, boolean pShouldIncludeAssistedInfo, Boolean pIncludeLiabilityChecks, Boolean pIncludeApplicationId) throws Exception {
        return buildCompanyDTO(pCompany, pShouldInclude401kData, false, pShouldIncludeAssistedInfo, pIncludeLiabilityChecks, pIncludeApplicationId);
    }

    private CompanyWSDTO buildCompanyDTO(Company pCompany,
                                         boolean pShouldInclude401kData,
                                         boolean pReturnServices,
                                         Boolean pShouldReturnAssistedInfo,
                                         Boolean pIncludeLiabilityChecks,
                                         Boolean pIncludeApplicationId) throws Exception {
        CompanyWSDTO companyDTO = new CompanyWSDTO();
        companyDTO.sourceSystemCD = pCompany.getSourceSystemCd().toString();
        companyDTO.sourceCompanyID = pCompany.getSourceCompanyId();
        companyDTO.pspCompanyID = pCompany.getId().toString();
        companyDTO.legalName = pCompany.getLegalName();
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(pCompany))
                        .OrderBy(OnHoldReason.EffectiveDate().Descending());
        DomainEntitySet<OnHoldReason> onHoldReasonList = Application.find(OnHoldReason.class, query);
        companyDTO.onHoldReasons = buildOnHoldReasonDTOs(onHoldReasonList);
        DDCompanyServiceInfo ddServiceInfo = (DDCompanyServiceInfo) CompanyService.findCompanyService(pCompany, ServiceCode.DirectDeposit);
        if (ddServiceInfo != null) {
            companyDTO.ddStatus = ddServiceInfo.getStatusCd().toString();
        }
        CompanyService taxService = CompanyService.findCompanyService(pCompany, ServiceCode.Tax);
        if (taxService != null) {
            companyDTO.taxStatus = taxService.getStatusCd().toString();    
        }
        companyDTO.offloadGroup = pCompany.getOffloadGroup().getOffloadGroupCd().toString();
        companyDTO.fundingModel = pCompany.getFundingModel().getFundingModelCd();
        companyDTO.fedTaxId = pCompany.getFedTaxId();
        companyDTO.legalAddress = buildAddressWSDTO(pCompany.getLegalAddress());

        if(ddServiceInfo != null && ddServiceInfo.getOverrideCompanyLimitAmount() != null){
            companyDTO.overrideCompanyLimitAmount = SpcfUtils.convertToBigDecimal(ddServiceInfo.getOverrideCompanyLimitAmount());
        } else {
            LimitRule limitRule = LimitRule.findLimitRule(pCompany, ServiceCode.DirectDeposit);
            if(limitRule != null) {
                LimitValue spp = limitRule.findLimitValueByName(LimitValueType.DefaultCompanyLimit);
                if (spp != null) {
                    companyDTO.overrideCompanyLimitAmount = new BigDecimal(spp.getValue());
                }
            }
        }

        if(companyDTO.overrideCompanyLimitAmount != null) {
            companyDTO.overrideCompanyLimitAmount = companyDTO.overrideCompanyLimitAmount.setScale(2);
        }

        if(ddServiceInfo != null && ddServiceInfo.getOverrideEmployeeLimitAmount() != null){
            companyDTO.overrideEmployeeLimitAmount =SpcfUtils.convertToBigDecimal(ddServiceInfo.getOverrideEmployeeLimitAmount());
        } else {
            LimitRule limitRule = LimitRule.findLimitRule(pCompany, ServiceCode.DirectDeposit);
            if(limitRule != null) {
                LimitValue spp = limitRule.findLimitValueByName(LimitValueType.DefaultEmployeeLimit);
                if (spp != null) {
                    companyDTO.overrideEmployeeLimitAmount = new BigDecimal(spp.getValue());
                }
            }
        }

        if(companyDTO.overrideEmployeeLimitAmount != null) {
            companyDTO.overrideEmployeeLimitAmount = companyDTO.overrideEmployeeLimitAmount.setScale(2);
        }

        long lastTransactionToken = new Long(TransactionResponse.getLastTransactionTokenNumber(pCompany)).intValue();
        companyDTO.lastTransactionResponseToken = lastTransactionToken;
        companyDTO.isFlaggedForFraud = pCompany.getIsFlaggedForFraud();

        if (pCompany.getQuickbooksInfo()!=null) {
            CompanyQuickBooksInfoWSDTO quickBooksInfoDTO = new CompanyQuickBooksInfoWSDTO();
            if(pIncludeApplicationId != null && pIncludeApplicationId) {
                quickBooksInfoDTO.applicationId = pCompany.getQuickbooksInfo().getApplicationId();
            }
            quickBooksInfoDTO.appVersion = pCompany.getQuickbooksInfo().getApplicationVersion();
            quickBooksInfoDTO.coaFeeAccountName = pCompany.getQuickbooksInfo().getCoaFeeAccountName();
            quickBooksInfoDTO.coaSalesTaxAccountName = pCompany.getQuickbooksInfo().getCoaSalesTaxAccountName();
            quickBooksInfoDTO.licenseNumber = pCompany.getQuickbooksInfo().getLicenseNumber();
            quickBooksInfoDTO.taxTableId = pCompany.getQuickbooksInfo().getTaxTableId();
            companyDTO.quickBooksInfo=quickBooksInfoDTO;
        }

        if (pShouldInclude401kData) {
            // 401K service info
            Company401kInfoWSDTO company401kInfo = new Company401kInfoWSDTO();
            ThirdParty401kCompanyServiceInfo company401kService = (ThirdParty401kCompanyServiceInfo) CompanyService.findCompanyService(pCompany, ServiceCode.ThirdParty401k);
            if (company401kService != null) {
                company401kInfo.serviceStatus = company401kService.getStatusCd().toString();
                company401kInfo.custodialId = company401kService.getCustodialId();
                company401kInfo.isSafeHarbor = company401kService.getHasSafeHarbor();
                if (company401kService.getServiceStartDate()!=null) {
                    company401kInfo.effectiveDate = CalendarUtils.getDateWithoutSeconds(company401kService.getServiceStartDate().toLocal());
                }
            }
            companyDTO.company401kInfo = company401kInfo;


            // cloud payroll items
            Criterion<CompanyPayrollItem> where = CompanyPayrollItem.Company().equalTo(pCompany);
            Expression<CompanyPayrollItem> companyPayrollItemQuery =
                    new Query<CompanyPayrollItem>()
                           .Where(where)
                           .OrderBy(CompanyPayrollItem.PayrollItem().PayrollItemCode());
            DomainEntitySet<CompanyPayrollItem> companyPayrollItems = Application.find(CompanyPayrollItem.class, companyPayrollItemQuery);

            ArrayList<PayrollItemWSDTO> payrollItemDTOs = new ArrayList<PayrollItemWSDTO>();
            for (CompanyPayrollItem currentItem : companyPayrollItems) {
                PayrollItemWSDTO newPayrollItemDTO = new  PayrollItemWSDTO();
                newPayrollItemDTO.payrollItemCode = currentItem.getPayrollItem() == null ? null : ObjectUtils.toString(currentItem.getPayrollItem().getPayrollItemCode());
                newPayrollItemDTO.sourceDescription = currentItem.getSourceDescription();
                newPayrollItemDTO.sourcePayrollItemId = currentItem.getSourcePayrollItemId();
                payrollItemDTOs.add(newPayrollItemDTO);
            }
            companyDTO.companyPayrollItems = payrollItemDTOs;
        }

        // services collection
        if (pReturnServices) {
            ArrayList<CompanyServiceWSDTO> serviceDTOs = new ArrayList<CompanyServiceWSDTO>();
            for (CompanyService companyService : pCompany.getCompanyServiceCollection().sort(CompanyService.Service().ServiceCd())) {
                CompanyServiceWSDTO serviceDTO = new CompanyServiceWSDTO();
                serviceDTO.name = companyService.getService().getServiceCd().name();
                serviceDTO.status = companyService.getStatusCd().name();
                serviceDTOs.add(serviceDTO);
            }
            companyDTO.services = serviceDTOs;

            companyDTO.nextEmployeeId=pCompany.getNextEmployeeId();
            companyDTO.nextPaycheckId=pCompany.getNextPaycheckId();
            companyDTO.nextPaylineTransactionId=pCompany.getNextPayrollItemId();
            companyDTO.nextPayrollTransactionId=pCompany.getNextPayrollTransactionId();
        }

        if (pShouldReturnAssistedInfo) {
            companyDTO.assistedPayrollItems = new ArrayList<CompanyPayrollItemWSDTO>();
            for (CompanyPayrollItem pi : pCompany.getCompanyPayrollItemCollection().sort(CompanyPayrollItem.SourcePayrollItemId())) {
                CompanyPayrollItemWSDTO wsPi = new CompanyPayrollItemWSDTO();
                wsPi.payrollItemCode = pi.getPayrollItem() == null ? null : ObjectUtils.toString(pi.getPayrollItem().getPayrollItemCode());
                wsPi.payrollItemDescription = pi.getPayrollItem() == null ? null : pi.getPayrollItem().getPayrollItemDescription();
                wsPi.payrollItemType = pi.getPayrollItem() == null ? null : ObjectUtils.toString(pi.getPayrollItem().getPayrollItemType());
                wsPi.sourceDescription = pi.getSourceDescription();
                wsPi.status = ObjectUtils.toString(pi.getStatus());
                wsPi.sourcePayrollItemId = pi.getSourcePayrollItemId();
                wsPi.taxFormLine = ObjectUtils.toString(pi.getTaxFormLine());
                wsPi.qbdtPayrollItemInfo = getPayrollItemInfoWSDTO(pi.getQbdtPayrollItemInfo());
                wsPi.payrollItemTaxableToLawIds = new ArrayList<String>();
                for (PayrollItemTaxableTo taxableTo : pi.getPayrollItemTaxableToCollection().sort(PayrollItemTaxableTo.CompanyLaw().Law().LawId())) {
                    wsPi.payrollItemTaxableToLawIds.add(taxableTo.getCompanyLaw().getLaw().getLawId());
                }
                companyDTO.assistedPayrollItems.add(wsPi);                
            }

            companyDTO.liabilityChecks = new ArrayList<LiabilityCheckWSDTO>();
            if(pIncludeLiabilityChecks != null && pIncludeLiabilityChecks) {
                for (LiabilityCheck lc : Application.<LiabilityCheck>find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(pCompany))) {
                    LiabilityCheckWSDTO wsLc = new LiabilityCheckWSDTO();
                    wsLc.amount = SpcfUtils.convertToBigDecimal(lc.getAmount());
                    wsLc.isVoid = lc.getIsVoid();
                    wsLc.periodEndDate = CalendarUtils.convertToDate(lc.getPeriodEndDate());
                    wsLc.sourceId = lc.getSourceId();
                    wsLc.transactionDate = CalendarUtils.convertToDate(lc.getTransactionDate());
                    wsLc.qbdtTransactionInfo = getQbdtTransactionInfoWSDTO(lc.getQbdtTransactionInfo());
                    wsLc.liabilityCheckLines = new ArrayList<LiabilityCheckLineWSDTO>();
                    for (LiabilityCheckLine lcl : lc.getLiabilityCheckLineCollection()) {
                        LiabilityCheckLineWSDTO wsLcl = new LiabilityCheckLineWSDTO();
                        wsLcl.amount = SpcfUtils.convertToBigDecimal(lcl.getAmount());
                        wsLcl.companyPayrollItem = ObjectUtils.toString(lcl.getCompanyPayrollItem());
                        wsLcl.qbdtTransactionInfoWSDTO = getQbdtTransactionInfoWSDTO(lcl.getQbdtTransactionInfo());
                        wsLc.liabilityCheckLines.add(wsLcl);
                    }
                    companyDTO.liabilityChecks.add(wsLc);
                }
            }

        }

        return companyDTO;
    }

    public static QbdtTransactionInfoWSDTO getQbdtTransactionInfoWSDTO(QbdtTransactionInfo info) {
        if(info == null) {
            return null;
        }

        QbdtTransactionInfoWSDTO wsInfo = new QbdtTransactionInfoWSDTO();
        wsInfo.accountName = info.getAccountName();
        wsInfo.agencyName = info.getAgencyName();
        wsInfo.cleared = info.getCleared();
        wsInfo.isDeleted = info.getIsDeleted();
        wsInfo.memo = info.getMemo();
        wsInfo.onService = info.getOnService();
        wsInfo.referenceNumber = info.getReferenceNumber();
        wsInfo.trackingClass = info.getTrackingClass();
        return wsInfo;
    }

    private Collection<OnHoldReasonWSDTO> buildOnHoldReasonDTOs(DomainEntitySet<OnHoldReason> pOnHoldReasonList)
                                                                                            throws Exception {
        Collection<OnHoldReasonWSDTO> onHoldReasonList = new ArrayList<OnHoldReasonWSDTO>();
        OnHoldReasonWSDTO onHoldReasonWSDTO = null;
        for (OnHoldReason onHoldReason:pOnHoldReasonList) {
            onHoldReasonWSDTO = new OnHoldReasonWSDTO();
            onHoldReasonWSDTO.id = onHoldReason.getId().toString();
            onHoldReasonWSDTO.onHoldReasonCd = onHoldReason.getOnHoldReasonCd().toString();
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, onHoldReason.getOnHoldReasonCd());
            onHoldReasonWSDTO.onHoldReasonNane = serviceSubStatus.getName();
            onHoldReasonWSDTO.effectiveDate = new Date(onHoldReason.getEffectiveDate().toLocal().getTimeInMilliseconds());
            if (null != onHoldReason.getExpirationDate()) {
                onHoldReasonWSDTO.expirationDate = new Date(onHoldReason.getExpirationDate().toLocal().getTimeInMilliseconds());
            }
            onHoldReasonList.add(onHoldReasonWSDTO);
        }
        return onHoldReasonList;
    }

    private Collection<CompanyDailyLiabilityWSDTO> buildCompanyDailyLiabilityDTOs(DomainEntitySet<CompanyDailyLiability> pCDLList)
                                                                                            throws Exception {
        Collection<CompanyDailyLiabilityWSDTO> cdlWSDTOList = new ArrayList<CompanyDailyLiabilityWSDTO>();
        CompanyDailyLiabilityWSDTO cdlWSDTO = null;
        for (CompanyDailyLiability currentCDL : pCDLList) {
            cdlWSDTO = new CompanyDailyLiabilityWSDTO();
            cdlWSDTO.id = currentCDL.getId().toString();
            cdlWSDTO.sourceCompanyID = currentCDL.getCompany().getSourceCompanyId();
            cdlWSDTO.sourceSystemCD = currentCDL.getCompany().getSourceSystemCd().toString();
            cdlWSDTO.lawId = currentCDL.getLaw().getLawId();
            cdlWSDTO.liabilityDate = CalendarUtils.convertToDate(currentCDL.getLiabilityDate().toLocal());
            cdlWSDTO.taxableWagesAmount = SpcfUtils.convertToBigDecimal(currentCDL.getTaxableWages());
            cdlWSDTO.taxAmount = SpcfUtils.convertToBigDecimal(currentCDL.getTaxAmount());
            cdlWSDTOList.add(cdlWSDTO);
        }
        return cdlWSDTOList;
    }



    private Collection<CompanyAgencyWSDTO> buildCompanyAgencyDTOs(DomainEntitySet<CompanyAgency> pCompanyAgencyList, boolean pIncludeInvalidDates)
                                                                                            throws Exception {
        Collection<CompanyAgencyWSDTO> companyAgencyList = new ArrayList<CompanyAgencyWSDTO>();
        for (CompanyAgency companyAgency:pCompanyAgencyList) {
            CompanyAgencyWSDTO companyAgencyWSDTO = buildCompanyAgencyWSDTO(companyAgency, pIncludeInvalidDates);
            companyAgencyList.add(companyAgencyWSDTO);
        }
        return companyAgencyList;
    }

    private CompanyAgencyWSDTO buildCompanyAgencyWSDTO(CompanyAgency pCompanyAgency, boolean pIncludeInvalidDates) {
        CompanyAgencyWSDTO companyAgencyWSDTO = new CompanyAgencyWSDTO();
        companyAgencyWSDTO.agencyId = pCompanyAgency.getAgency().getAgencyId();
        if (null != pCompanyAgency.getIntuitResponsibilityStartDate()) {            
            companyAgencyWSDTO.intuitResponsibilityStartDate = CalendarUtils.convertToDate(CalendarUtils.setTime12AM(pCompanyAgency.getIntuitResponsibilityStartDate()));
        }
        if (null != pCompanyAgency.getIntuitResponsibilityEndDate()) {
            companyAgencyWSDTO.intuitResponsibilityEndDate = CalendarUtils.convertToDate(CalendarUtils.setTime12AM(pCompanyAgency.getIntuitResponsibilityEndDate()));
        }
        companyAgencyWSDTO.companyAgencyStatusCode = "Active";
        companyAgencyWSDTO.finalPayrollDate = pCompanyAgency.getFinalPayrollDate() == null ? null : CalendarUtils.convertToDate(pCompanyAgency.getFinalPayrollDate());
        companyAgencyWSDTO.firstFilingsQuarter = pCompanyAgency.getFirstFilingsQuarter();
        companyAgencyWSDTO.generateAnnualForm = pCompanyAgency.getGenerateAnnualForm();
        companyAgencyWSDTO.lastFilingsQuarter = pCompanyAgency.getLastFilingsQuarter();
        companyAgencyWSDTO.isFinalReturn = pCompanyAgency.getIsFinalReturn();

        companyAgencyWSDTO.companyAgencyPaymentTemplates = new ArrayList<CompanyAgencyPaymentTemplateWSDTO>();
        for (CompanyAgencyPaymentTemplate capt : pCompanyAgency.getCompanyAgencyPaymentTemplateCollection().sort(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd())) {
            CompanyAgencyPaymentTemplateWSDTO wsCapt = new CompanyAgencyPaymentTemplateWSDTO();
            wsCapt.paymentTemplateCd = capt.getPaymentTemplate().getPaymentTemplateCd();
            wsCapt.effectiveDepositFrequencyWSDTOs = new ArrayList<EffectiveDepositFrequencyWSDTO>();
            wsCapt.agencyTaxpayerId = capt.getAgencyTaxpayerId();
            for (EffectiveDepositFrequency edf : capt.getEffectiveDepositFrequencyCollection()) {
                EffectiveDepositFrequencyWSDTO wsEdf = new EffectiveDepositFrequencyWSDTO();
                wsEdf.depositFrequency = ObjectUtils.toString(edf.getPaymentTemplateFrequency().getPaymentFrequencyId());
                wsEdf.effectiveDate = CalendarUtils.convertToDate(edf.getEffectiveDate());
                wsEdf.paymentTemplate = edf.getPaymentTemplateFrequency().getPaymentTemplate().getPaymentTemplateCd();
                if(pIncludeInvalidDates && edf.getInvalidDate() != null) {
                    wsEdf.invalidDate = CalendarUtils.convertToDate(edf.getInvalidDate());
                }
                wsCapt.effectiveDepositFrequencyWSDTOs.add(wsEdf);
            }
            companyAgencyWSDTO.companyAgencyPaymentTemplates.add(wsCapt);
        }

        companyAgencyWSDTO.companyLaws = new ArrayList<CompanyLawWSDTO>();
        for (CompanyLaw cl : pCompanyAgency.getCompanyLawCollection().sort(CompanyLaw.SourceId())) {
            CompanyLawWSDTO wsCl = new CompanyLawWSDTO();
            wsCl.exemptionStatus = ObjectUtils.toString(cl.getExemptionStatus());
            wsCl.lawId = cl.getLaw().getLawId();
            wsCl.sourceDescription = cl.getSourceDescription();
            wsCl.sourceId = cl.getSourceId();
            wsCl.status = ObjectUtils.toString(cl.getStatus());
            wsCl.taxFormLine = ObjectUtils.toString(cl.getTaxFormLine());
            wsCl.qbdtPayrollItemInfo = getPayrollItemInfoWSDTO(cl.getQbdtPayrollItemInfo());
            wsCl.lawRates = new ArrayList<CompanyLawRateWSDTO>();
            for (CompanyLawRate clr : cl.getCompanyLawRateCollection()) {
                CompanyLawRateWSDTO wsClr = new CompanyLawRateWSDTO();
                wsClr.effectiveDate = clr.getEffectiveDate() == null ? null : CalendarUtils.convertToDate(clr.getEffectiveDate());
                wsClr.rate = clr.getRate();
                wsCl.lawRates.add(wsClr);
            }
            companyAgencyWSDTO.companyLaws.add(wsCl);
        }

        companyAgencyWSDTO.companyAgencyOnHoldReasons = new ArrayList<CompanyAgencyOnHoldReasonWSDTO>();

        return companyAgencyWSDTO;
    }

    private QBDTPayrollItemInfoWSDTO getPayrollItemInfoWSDTO(QbdtPayrollItemInfo info) {
        QBDTPayrollItemInfoWSDTO wsInfo = new QBDTPayrollItemInfoWSDTO();
        wsInfo.adjustGross = info.getAdjustsGross();
        wsInfo.agencyId = info.getAgencyId();
        wsInfo.basedOnQuantity = info.getBasedOnQuantity();
        wsInfo.defaultLimit = info.getDefaultLimit() == null ? null : SpcfUtils.convertToBigDecimal(info.getDefaultLimit());
        wsInfo.defaultRate = info.getDefaultRate();
        wsInfo.defaultRateType = ObjectUtils.toString(info.getDefaultRateType());
        wsInfo.earningsTable = info.getEarningsTable();
        wsInfo.expenseAccount = info.getExpenseAccount();
        wsInfo.expenseByJob = info.getExpenseByJob();
        wsInfo.isDeleted = info.getIsDeleted();
        wsInfo.isEmployeePaid = info.getIsEmployeePaid();
        wsInfo.liabilityAccount = info.getLiabilityAccount();
        wsInfo.liabilityAgency = info.getLiabilityAgency();
        wsInfo.onService = info.getOnService();
        wsInfo.payType = ObjectUtils.toString(info.getPayType());
        wsInfo.specialType = ObjectUtils.toString(info.getSpecialType());
        return wsInfo;
    }

    private XMLGregorianCalendar getXMLGregorianCalendar(SpcfCalendar spcfCalendar) throws Exception {
        Date date = new Date(spcfCalendar.getTimeInMilliseconds());
        GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
        cal.setTime(date);
        XMLGregorianCalendar pspXmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

        return pspXmlDate;
    }

    @WebMethod
    public Collection<CompanyBankAccountWSDTO> queryCompanyBankAccounts(@WebParam(name = "pspCompanyID") String pPspCompanyId) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            Collection<CompanyBankAccountWSDTO> companyBankAccountDTOs=null;
            PayrollServices.beginUnitOfWork();
            Company company = Application.findById(Company.class, SpcfUniqueId.createInstance(pPspCompanyId));
            DomainEntitySet<CompanyBankAccount> companyBankAccounts= CompanyBankAccount.findCompanyBankAccounts(company);
            companyBankAccountDTOs = buildCompanyBankAccountDTOs(companyBankAccounts);
            PayrollServices.commitUnitOfWork();
            return companyBankAccountDTOs;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public CompanyBankAccountWSDTO queryCompanyBankAccount(@WebParam(name = "pspCompanyBankAccID") String pPspCompanyBankAccId) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pPspCompanyBankAccId == null) {
            throw new RuntimeException("No Company Bank Account GUID is specified");
        }
        try {
            CompanyBankAccountWSDTO companyBankAccountWSDTO = null;
            PayrollServices.beginUnitOfWork();
            CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(pPspCompanyBankAccId));
            if (companyBankAccount != null) {
                companyBankAccountWSDTO = buildCompanyBankAccountDTO(companyBankAccount);
            }
            PayrollServices.commitUnitOfWork();
            return companyBankAccountWSDTO;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public CompanyBankAccountWSDTO queryCompanyBankAccountBySourceId(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                                     @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                                                     @WebParam(name = "sourceCompanyBankAccountID") String sourceCompanyBankAccountID) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            CompanyBankAccountWSDTO companyBankAccountWSDTO = null;
            PayrollServices.beginUnitOfWork();
            if (sourceSystemCD == null || sourceCompanyID == null
                    || sourceCompanyBankAccountID == null) {
                throw new RuntimeException("Any of Source System Code, Source Company ID or Source Company BankAccount ID"
                        + " can not be null");
            }

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            DomainEntitySet<CompanyBankAccount> companyBankAccounts = CompanyBankAccount
                    .findCompanyBankAccountsIncludingExpired(company, sourceCompanyBankAccountID);
            if (companyBankAccounts == null || companyBankAccounts.size() == 0) {
                throw new RuntimeException("No BankAccount exists for the specified company and sourceCompanyBankAccountId");
            }

            companyBankAccountWSDTO = buildCompanyBankAccountDTO(companyBankAccounts.get(0));

            PayrollServices.commitUnitOfWork();
            return companyBankAccountWSDTO;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateBankAccountStatus(@WebParam(name = "pspCompanyBankAccID") String pPspCompanyBankAccId,
                                        @WebParam(name = "bankAccountStatusD") String pBankAccountStatus) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pPspCompanyBankAccId == null) {
            throw new RuntimeException("No Company Bank Account GUID is specified");
        }
        if (pBankAccountStatus == null) {
            throw new RuntimeException("No Bank Account Status is specified");
        }
        try {
            PayrollServices.beginUnitOfWork();
            CompanyBankAccount companyBankAccount = Application.findById(CompanyBankAccount.class, SpcfUniqueId.createInstance(pPspCompanyBankAccId));
            if (companyBankAccount == null) {
                throw new RuntimeException("CompanyBankAccount with the specified Company Bank Account Id "+ pPspCompanyBankAccId  +" doesn't exists");
            }

            companyBankAccount.updateBankAccountStatus(BankAccountStatus.valueOf(pBankAccountStatus));

            PayrollServices.commitUnitOfWork();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String updateFundingModel(@WebParam(name = "sourceSystemCode")String pSourceSystemCode,
                                     @WebParam(name = "sourceCompanyID")String pSourceCompanyID,
                                     @WebParam(name = "fundingModel")String pFundingModel) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        String result = "";

        if (pSourceSystemCode == null) {
            throw new RuntimeException("No Source System Code is specified");
        }

        if (pSourceCompanyID == null) {
            throw new RuntimeException("No Company Id is specified");
        }

        if (pFundingModel == null) {
            throw new RuntimeException("No Funding Model is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();

            FundingModel fundingModel = Application.findById(FundingModel.class, pFundingModel);
            ProcessResult<Company> processResult = PayrollServices.companyManager.updateCompanyFundingModel(
                    SourceSystemCode.valueOf(pSourceSystemCode),
                    pSourceCompanyID,
                    fundingModel);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            } else {
                result = "Funding model updated.";
            }

            PayrollServices.commitUnitOfWork();
            return result;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String updateDDLimits(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                 @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                 @WebParam(name = "companyDDLimit")String pCompanyDDLimit,
                                 @WebParam(name = "employeeDDLimit")String pEmployeeDDLimit) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        String result = "";
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            SpcfMoney companyLimit = null;
            SpcfMoney employeeLimit = null;

            if(pCompanyDDLimit != null){
                companyLimit = new SpcfMoney(pCompanyDDLimit);
            }

            if(pEmployeeDDLimit != null){
                employeeLimit = new SpcfMoney(pEmployeeDDLimit);
            }

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.companyManager.updateDDLimits(SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, companyLimit, employeeLimit);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            } else {
                if(companyLimit != null){
                    result = "Company limit updated.";
                }

                if(employeeLimit != null){
                    result = "Employee limit updated.";
                }
            }
            PayrollServices.commitUnitOfWork();
            return result;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String updateBPLimits(@WebParam(name = "sourceSystemCD")String sourceSystemCD,
                                 @WebParam(name = "sourceCompanyID")String sourceCompanyID,
                                 @WebParam(name = "companyDDLimit")String pCompanyDDLimit,
                                 @WebParam(name = "payeeDDLimit")String pPayeeDDLimit) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        String result = "";
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            SpcfMoney companyLimit = null;
            SpcfMoney payeeLimit = null;

            if(pCompanyDDLimit != null){
                companyLimit = new SpcfMoney(pCompanyDDLimit);
            }

            if(pPayeeDDLimit != null){
                payeeLimit = new SpcfMoney(pPayeeDDLimit);
            }

            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            SpcfMoney initialCompanyLimitAmount = null;
            SpcfMoney initialPayeeLimitAmount = null;
            if(company != null) {
                BPCompanyServiceInfo bpCompanyServiceInfo = (BPCompanyServiceInfo)company.getService(ServiceCode.BillPayment);
                initialCompanyLimitAmount = bpCompanyServiceInfo.getOverrideCompanyLimitAmount();
                initialPayeeLimitAmount = bpCompanyServiceInfo.getOverridePayeeLimitAmount();
            }

            ProcessResult<BPCompanyServiceInfo> processResult = PayrollServices.companyManager.updateBPLimits(SourceSystemCode.valueOf(sourceSystemCD),
                    sourceCompanyID, companyLimit, payeeLimit);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            } else {
                BPCompanyServiceInfo updatedBpCompanyServiceInfo = processResult.getResult();
                if((initialCompanyLimitAmount == null && updatedBpCompanyServiceInfo.getOverrideCompanyLimitAmount() != null) ||
                        (initialCompanyLimitAmount != null && updatedBpCompanyServiceInfo.getOverrideCompanyLimitAmount() == null) ||
                        ((initialCompanyLimitAmount != null && updatedBpCompanyServiceInfo.getOverrideCompanyLimitAmount() != null) &&
                                initialCompanyLimitAmount.compareTo(updatedBpCompanyServiceInfo.getOverrideCompanyLimitAmount()) != 0)){
                    result += "Company limit updated. ";
                }

                if((initialPayeeLimitAmount == null && updatedBpCompanyServiceInfo.getOverridePayeeLimitAmount() != null) ||
                        (initialPayeeLimitAmount != null && updatedBpCompanyServiceInfo.getOverridePayeeLimitAmount() == null) ||
                        ((initialPayeeLimitAmount != null && updatedBpCompanyServiceInfo.getOverridePayeeLimitAmount() != null) &&
                                initialPayeeLimitAmount.compareTo(updatedBpCompanyServiceInfo.getOverridePayeeLimitAmount()) != 0)) {
                    result += "Payee limit updated.";
                }
            }
            PayrollServices.commitUnitOfWork();
            return result;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void changeCompanySourcePayrollSystem(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "newSourceSystemCD") String newSourceSystemCD,
                                      @WebParam(name = "offeringSkuID") String offeringSkuID) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (newSourceSystemCD == null || newSourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No newSourceSystemCD is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(
                                                            sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD) );
            if (null == company) {
                throw new RuntimeException("No company exists with the specified SourceCompanyID "+ sourceCompanyID +
                " and for the specified SourcePayrollSystem " +sourceSystemCD);
            }
            company.setSourceSystemCd(SourceSystemCode.valueOf(newSourceSystemCD));
            if (SourceSystemCode.valueOf(newSourceSystemCD) == SourceSystemCode.QBOE) {
                // set the default QBOE - Offering
                String sku = offeringSkuID;
                if (null == sku || sku.length() <= 0) {
                    sku = OfferingInfoDTO.DIY_WITH_DD.getSKU();
                }
                Offering offering = Offering.findBySKU(sku);
                if (null == offering) {
                    throw new RuntimeException("No Offering exists for the SKU "+ sku);
                }
                CompanyService companyService = company.getService(ServiceCode.DirectDeposit);
                ServiceInfoDTO serviceInfoDTO = new DTOFactory().create(companyService);

                serviceInfoDTO.setOfferingCode(offering.getOfferingCode());

                ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateService(SourceSystemCode.valueOf(sourceSystemCD), company.getSourceCompanyId(), serviceInfoDTO);
                if(!processResult.isSuccess()) {
                    throw new RuntimeException("Error updating service: " + processResult.toString());
                }
            }
            Application.save(company);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void changeCompanyOffering(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "serviceCd") String serviceCd,
                                      @WebParam(name = "payrollSubTypeCode") String payrollSubTypeCode,
                                      @WebParam(name = "offeringSkuID") String offeringSkuID) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (null == company) {
                throw new RuntimeException("No company exists with the specified SourceCompanyID "+ sourceCompanyID +
                " and for the specified SourcePayrollSystem " +sourceSystemCD);
            }

            Offering offering = Offering.findBySKU(offeringSkuID);
            if (offering == null) {
                throw new RuntimeException("The sku "+offeringSkuID+" is not associated with any offering");
            }

            OfferingInfoDTO offeringInfoDTO = PayrollServices.dtoFactory.create(offering);

            ProcessResult processResult = PayrollServices.companyManager.updateCompanyOffering(SourceSystemCode.valueOf(sourceSystemCD),
                    company.getSourceCompanyId(),
                    offeringInfoDTO); 
            if(!processResult.isSuccess()) {
                throw new RuntimeException("Error updating service: " + processResult.toString());
            }
            Application.save(company);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void claimOfferForCompany(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "offerCode") String offerCode) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(
                                                            sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD) );
            if (null == company) {
                throw new RuntimeException("No company exists with the specified SourceCompanyID "+ sourceCompanyID +
                " and for the specified SourcePayrollSystem " +sourceSystemCD);
            }
            ProcessResult<CompanyOffer> processResult = PayrollServices.companyManager.claimOfferForCompany(offerCode, null, company);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<CompanyBankAccountWSDTO> buildCompanyBankAccountDTOs(DomainEntitySet<CompanyBankAccount> pCompanyBankAccounts) throws Exception {
        ArrayList<CompanyBankAccountWSDTO> companyBankAccountWSDTOs = new ArrayList();
        for (CompanyBankAccount companyBankAccount : pCompanyBankAccounts) {
            CompanyBankAccountWSDTO companyDTO = buildCompanyBankAccountDTO(companyBankAccount);
            companyBankAccountWSDTOs.add(companyDTO);
        }
        return companyBankAccountWSDTOs;
    }

    private CompanyBankAccountWSDTO buildCompanyBankAccountDTO(CompanyBankAccount pCompanyBankAccount) throws Exception {
        CompanyBankAccountWSDTO companyBankAccountDTO = new CompanyBankAccountWSDTO();
        companyBankAccountDTO.sourceBankAccountID = pCompanyBankAccount.getSourceBankAccountId();
        companyBankAccountDTO.statusCode = ObjectUtils.toString(pCompanyBankAccount.getStatusCd());
        companyBankAccountDTO.id = pCompanyBankAccount.getId().toString();
        companyBankAccountDTO.verfyRetryCount = pCompanyBankAccount.getVerifyRetryCount();
        companyBankAccountDTO.bankAccount = buildBankAccountWSDTO(pCompanyBankAccount.getBankAccount());
        return companyBankAccountDTO;
    }

    private BankAccountWSDTO buildBankAccountWSDTO(BankAccount bankAccount) {
        if(bankAccount == null) {
            return null;
        }

        BankAccountWSDTO bankAccountDTO = new BankAccountWSDTO();
        bankAccountDTO.accountNumber = bankAccount.getAccountNumber();
        bankAccountDTO.bankAccountType = bankAccount.getAccountTypeCd().toString();
        bankAccountDTO.bankName = bankAccount.getBankName();
        bankAccountDTO.routingNumber = bankAccount.getRoutingNumber();
        return bankAccountDTO;
    }

    @WebMethod
    public void addStrike(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "strikeReason") String strikeReason,
                                      @WebParam(name = "strikeDate")Calendar strikeDate) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (strikeReason == null) {
            throw new RuntimeException("No strikeReason is specified");
        }

        if (strikeDate == null) {
            throw new RuntimeException("No strikeDate is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyEvent> processResult = PayrollServices.companyManager.addStrikeEvent(
                                                            SourceSystemCode.valueOf(sourceSystemCD),
                                                            sourceCompanyID,
                                                            strikeReason,
                                                            CalendarUtils.convertToSpcfCalendar(strikeDate));
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateAS400PayrollCount(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                      @WebParam(name = "migratedPayrollCount") int migratedPayrollCount) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }else if(!sourceSystemCD.equals("QBDT")){
            throw new RuntimeException("Invalid sourceSystemCD is specified to Update AS400 Payroll Count. Source SystemCD should be QBDT ");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(
                                                            sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD) );
            if (null == company) {
                throw new RuntimeException("No company exists with the specified SourceCompanyID "+ sourceCompanyID +
                " and for the specified SourcePayrollSystem " +sourceSystemCD);
            }
            company.getQuickbooksInfo().setAS400PayrollCount(migratedPayrollCount);
            Application.save(company);
            PayrollServices.commitUnitOfWork();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public long queryPayrollCount(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                  @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {
        long response = 0;

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));

            if (null == company) {
                throw new RuntimeException("No company exists with the specified SourceCompanyID "+ sourceCompanyID +
                " and for the specified SourcePayrollSystem " +sourceSystemCD);
            }

            if (company.getQuickbooksInfo() != null) {
                response = company.getQuickbooksInfo().getAS400PayrollCount();
            }

            PayrollServices.commitUnitOfWork();
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    @WebMethod
    public Collection<CompanyEventWSDTO> queryCompanyEvents(@WebParam(name = "sourceSystemCD") String pSourceSystemCD,
                                      @WebParam(name = "sourceCompanyID") String pSourceCompanyID,
                                      @WebParam(name = "companyEventCd") String pCompanyEventCD,
                                      @WebParam(name = "fromDate") String pFromDate,
                                      @WebParam(name = "toDate") String pToDate) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        Collection<CompanyWSDTO> companyDTOs=null;
        try {
            PayrollServices.beginUnitOfWork();
            if (pSourceSystemCD == null || pSourceSystemCD.trim().length() == 0) {
                throw new RuntimeException("No sourceSystemCD is specified");
            }

            if (pSourceCompanyID == null || pSourceCompanyID.trim().length() == 0) {
                throw new RuntimeException("No sourceCompanyID is specified");
            }

            if (pFromDate != null && pFromDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid from date format" + pFromDate + ".  Correct format: MM/dd/yyyy");
            }

            if (pToDate != null && pToDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid from date format" + pToDate + ".  Correct format: MM/dd/yyyy");
            }

            EventTypeCode eventTypeCd = null;
            if(pCompanyEventCD != null){
                eventTypeCd = EventTypeCode.valueOf(pCompanyEventCD);
                if (eventTypeCd == null) {
                    throw new RuntimeException("Invalid EventType Code: " + eventTypeCd);
                }
            }

            Company company = Company.findCompany(pSourceCompanyID, SourceSystemCode.valueOf(pSourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }
            SpcfCalendar fromDate = null;
            SpcfCalendar toDate = null;
            if (pFromDate != null) {
                fromDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("MM/dd/yyyy");
                SpcfCalendar parsedRunDate = dateFormat.parse(pFromDate);
                fromDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            if (pToDate != null) {
                toDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("MM/dd/yyyy");
                SpcfCalendar parsedRunDate = dateFormat.parse(pToDate);
                toDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            DomainEntitySet<CompanyEvent> companyEvents =  CompanyEvent.findCompanyEvents(company, eventTypeCd, null, fromDate, toDate);
            Collection<CompanyEventWSDTO> companyEventDTOs = buildCompanyEventDTOs(companyEvents);
            PayrollServices.commitUnitOfWork();
            return companyEventDTOs;
        } catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private Collection<CompanyEventWSDTO> buildCompanyEventDTOs(DomainEntitySet<CompanyEvent> pCompanyEvents) throws Exception {
        Collection<CompanyEventWSDTO> companyEventDTOs = new ArrayList<CompanyEventWSDTO>(pCompanyEvents.size());
        for (CompanyEvent companyEvent : pCompanyEvents) {
            CompanyEventWSDTO companyEventDTO = buildCompanyEventDTO(companyEvent);
            companyEventDTOs.add(companyEventDTO);
        }
        return companyEventDTOs;
    }


    private CompanyEventWSDTO buildCompanyEventDTO(CompanyEvent pCompanyEvent) throws Exception {
        CompanyEventWSDTO companyEventDTO = new CompanyEventWSDTO();

        companyEventDTO.id = pCompanyEvent.getId().toString();
        companyEventDTO.eventDate= CalendarUtils.getDateWithoutSeconds(pCompanyEvent.getEventTimeStamp().toLocal());
        companyEventDTO.eventTypeCd = pCompanyEvent.getEventTypeCd();
        EventType eventType = PayrollServices.entityFinder.findById(EventType.class, pCompanyEvent.getEventTypeCd());
        companyEventDTO.eventTypeName = eventType.getName();
        companyEventDTO.eventTypeDescription = eventType.getDescription();
        companyEventDTO.statusCd = pCompanyEvent.getStatusCd();
        companyEventDTO.statusEffectiveDate = CalendarUtils.getDateWithoutSeconds(pCompanyEvent.getStatusEffectiveDate().toLocal());

        Collection<CompanyEventDetailWSDTO> companyEventDetailDTOs = buildCompanyEventDetails(pCompanyEvent.getCompanyEventDetailCollection());

        if(companyEventDetailDTOs != null && companyEventDetailDTOs.size() > 0){
            companyEventDTO.eventDetails = companyEventDetailDTOs;
        }
        
        return companyEventDTO;
    }

    private Collection<CompanyEventDetailWSDTO> buildCompanyEventDetails(DomainEntitySet<CompanyEventDetail> pCompanyEventDetails){
        Collection<CompanyEventDetailWSDTO> companyEventDetailDTOs =
                new ArrayList<CompanyEventDetailWSDTO>(pCompanyEventDetails.size());

        CompanyEventDetailWSDTO eventDetailDTO = null;
        for (CompanyEventDetail eventDetail : pCompanyEventDetails) {
            eventDetailDTO = new CompanyEventDetailWSDTO();
            EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, eventDetail.getEventDetailTypeCd());

            eventDetailDTO.eventDetailTypeCd = eventDetail.getEventDetailTypeCd();
            eventDetailDTO.name = eventDetailType.getName();
            eventDetailDTO.value = eventDetail.getValue();
            eventDetailDTO.valueClassName = eventDetailType.getValueClassName();
            companyEventDetailDTOs.add(eventDetailDTO);
        }

        return companyEventDetailDTOs;
    }
	
    @WebMethod
    public String changeCompanyBankAccount(@WebParam(name = "sourceSystemCD")String pSourceSystemCD,
                                           @WebParam(name = "sourceCompanyID")String pSourceCompanyID,
                                           @WebParam(name = "companyBankAccountDTO")CompanyBankAccountWSDTO pCompanyBankAccountDTO,
                                           @WebParam(name = "shouldAddRandomDebits")boolean pShouldAddRandomDebits,
                                           @WebParam(name = "shouldAllowPendingTransactions")boolean pShouldAllowPendingTransactions,
                                           @WebParam(name = "shouldMovePendingTransactionsToAccount")boolean pShouldMovePendingTransactionsToAccount) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCD == null || pSourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyID == null || pSourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pCompanyBankAccountDTO == null) {
            throw new RuntimeException("No CompanyBankAccount is specified");
        }
        try {
            String result = "";
            PayrollServices.beginUnitOfWork();
            CompanyBankAccountDTO companyBankAccountDTO = new CompanyBankAccountDTO();
            BankAccountDTO bankAccount = new BankAccountDTO();
            bankAccount.setAccountNumber(pCompanyBankAccountDTO.bankAccount.accountNumber);
            bankAccount.setAccountType(BankAccountType.valueOf(pCompanyBankAccountDTO.bankAccount.bankAccountType));
            bankAccount.setBankName(pCompanyBankAccountDTO.bankAccount.bankName);
            bankAccount.setRoutingNumber(pCompanyBankAccountDTO.bankAccount.routingNumber);

            companyBankAccountDTO.setBankAccountDTO(bankAccount);
            companyBankAccountDTO.setCompanyBankAccountID(pCompanyBankAccountDTO.sourceBankAccountID);
            companyBankAccountDTO.setSourceBankAccountName("BofA");

            ProcessResult processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.valueOf(pSourceSystemCD),
                    pSourceCompanyID, companyBankAccountDTO, pShouldAddRandomDebits, pShouldAllowPendingTransactions, pShouldMovePendingTransactionsToAccount);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            } else {
                result = "Company Bank Account information changed.";
            }

            PayrollServices.commitUnitOfWork();
            return result;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }	


//    @WebMethod
//    public Collection<EftpsEnrollmentWSDTO> queryEftpsEnrollments( @WebParam(name = "sourceSystemCD") String pSrcSystemCd,
//                                                     @WebParam(name = "sourceCompanyID")String pSrcCompanyId) throws Exception {
//        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
//        if (pSrcSystemCd == null || pSrcSystemCd.trim().length() == 0) {
//            throw new RuntimeException("No sourceSystemCD is specified");
//        }
//
//        if (pSrcCompanyId == null || pSrcCompanyId.trim().length() == 0) {
//            throw new RuntimeException("No sourceCompanyID is specified");
//        }
//
//         try {
//            PayrollServices.beginUnitOfWork();
//            Company company = Company.findCompany(pSrcCompanyId, SourceSystemCode.valueOf(pSrcSystemCd));
//            if (company == null) {
//                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
//            }
//
//            DomainEntitySet<EftpsEnrollment> enrollmentList = company.getAllEnrollments();
//            Collection<EftpsEnrollmentWSDTO> enrollments =  buildEnrollmentDTOs(enrollmentList);
//            PayrollServices.commitUnitOfWork();
//            return enrollments;
//        } catch(Exception ex) {
//            PayrollServices.rollbackUnitOfWork();
//            throw ex;
//        } finally {
//            PayrollServices.rollbackUnitOfWork();
//        }
//    }
//
//    @WebMethod
//    public EftpsEnrollmentWSDTO updateEftpsEnrollments( @WebParam(name = "EftpsEnrollmentWSDTO") EftpsEnrollmentWSDTO pEftpsEnrollmentWSDTO) throws Exception {
//        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
//        if (pEftpsEnrollmentWSDTO == null) {
//            throw new RuntimeException("No EftpsEnrollmentWSDTO is specified");
//        }
//
//        try {
//            PayrollServices.beginUnitOfWork();
//            EftpsEnrollment domainEnrollment = PayrollServices.entityFinder.findById(EftpsEnrollment.class, SpcfUniqueId.createInstance(pEftpsEnrollmentWSDTO.id));
//            if (domainEnrollment == null) {
//                throw new RuntimeException("No EftpsEnrollment exists for the specified id");
//            }
//            updateEnrollmentFromDTO(domainEnrollment, pEftpsEnrollmentWSDTO);
//            Application.save(domainEnrollment);
//            domainEnrollment = PayrollServices.entityFinder.findById(EftpsEnrollment.class, domainEnrollment.getId());
//            EftpsEnrollmentWSDTO enrollmentWSDTO = buildEnrollmentDTO(domainEnrollment);
//            PayrollServices.commitUnitOfWork();
//            return enrollmentWSDTO;
//        } catch(Exception ex) {
//            PayrollServices.rollbackUnitOfWork();
//            throw ex;
//        } finally {
//            PayrollServices.rollbackUnitOfWork();
//        }
//    }
//
//    private Collection<EftpsEnrollmentWSDTO> buildEnrollmentDTOs(DomainEntitySet<EftpsEnrollment> pEnrollmentList)
//                                                                                            throws Exception {
//        Collection<EftpsEnrollmentWSDTO> enrollmentList = new ArrayList<EftpsEnrollmentWSDTO>();
//        EftpsEnrollmentWSDTO enrollmentWSDTO = null;
//        for (EftpsEnrollment enrollment:pEnrollmentList) {
//            enrollmentWSDTO = buildEnrollmentDTO(enrollment);
//            enrollmentList.add(enrollmentWSDTO);
//        }
//        return enrollmentList;
//    }
//
//    private EftpsEnrollmentWSDTO buildEnrollmentDTO(EftpsEnrollment pEftpsEnrollment) {
//        EftpsEnrollmentWSDTO enrollmentWSDTO = new EftpsEnrollmentWSDTO();
//        enrollmentWSDTO.id = pEftpsEnrollment.getId().toString();
//        enrollmentWSDTO.fedTaxId = pEftpsEnrollment.getFedTaxId();
//        enrollmentWSDTO.addressLine1 = pEftpsEnrollment.getAddressLine1();
//        enrollmentWSDTO.addressLine2 = pEftpsEnrollment.getAddressLine2();
//        enrollmentWSDTO.city = pEftpsEnrollment.getCity();
//        enrollmentWSDTO.state = pEftpsEnrollment.getState();
//        enrollmentWSDTO.zipCode = pEftpsEnrollment.getZipCode();
//        enrollmentWSDTO.efeFilingId = pEftpsEnrollment.getEfeFilingId();
//        enrollmentWSDTO.efeStatus = pEftpsEnrollment.getEfeStatus();
//        if (null != pEftpsEnrollment.getEfeStatusEffectiveDate()) {
//            enrollmentWSDTO.efeStatusEffectiveDate = new Date(pEftpsEnrollment.getEfeStatusEffectiveDate().toLocal().getTimeInMilliseconds());
//        }
//        enrollmentWSDTO.legalName = pEftpsEnrollment.getLegalName();
//        if (null != pEftpsEnrollment.getStatusCd()) {
//            enrollmentWSDTO.statusCd = pEftpsEnrollment.getStatusCd().toString();
//        }
//        if (null != pEftpsEnrollment.getStatusEffectiveDate()) {
//            enrollmentWSDTO.statusEffectiveDate = new Date(pEftpsEnrollment.getStatusEffectiveDate().toLocal().getTimeInMilliseconds());
//        }
//        enrollmentWSDTO.statusReason = pEftpsEnrollment.getStatusReason();
//        if (null != pEftpsEnrollment.getSubmittedDate()) {
//            enrollmentWSDTO.submittedDate = new Date(pEftpsEnrollment.getSubmittedDate().toLocal().getTimeInMilliseconds());
//        }
//        enrollmentWSDTO.contactFirstName = pEftpsEnrollment.getContactFirstName();
//        enrollmentWSDTO.contactLastName = pEftpsEnrollment.getContactLastName();
//        enrollmentWSDTO.contactJobTitle = pEftpsEnrollment.getContactJobTitle();
//        enrollmentWSDTO.contactPhone = pEftpsEnrollment.getContactPhone();
//
//        return enrollmentWSDTO;
//    }
//
//    private void updateEnrollmentFromDTO(EftpsEnrollment pEnrollment, EftpsEnrollmentWSDTO pEftpsEnrollmentDTO) {
//        pEnrollment.setFedTaxId(pEftpsEnrollmentDTO.fedTaxId);
//        pEnrollment.setAddressLine1(pEftpsEnrollmentDTO.addressLine1);
//        pEnrollment.setAddressLine2(pEftpsEnrollmentDTO.addressLine2);
//        pEnrollment.setCity(pEftpsEnrollmentDTO.city);
//        pEnrollment.setState(pEftpsEnrollmentDTO.state);
//        pEnrollment.setZipCode(pEftpsEnrollmentDTO.zipCode);
//        pEnrollment.setEfeFilingId(pEftpsEnrollmentDTO.efeFilingId);
//        pEnrollment.setEfeStatus(pEftpsEnrollmentDTO.efeStatus);
//        if (null != pEftpsEnrollmentDTO.efeStatusEffectiveDate) {
//            pEnrollment.setEfeStatusEffectiveDate(CalendarUtils.convertToSpcfCalendar(pEftpsEnrollmentDTO.efeStatusEffectiveDate));
//        }
//        pEnrollment.setLegalName(pEftpsEnrollmentDTO.legalName);
//        if (null != pEftpsEnrollmentDTO.statusCd) {
//            pEnrollment.setStatusCd(EftpsEnrollmentStatusCode.valueOf(pEftpsEnrollmentDTO.statusCd));
//        }
//        if (null != pEftpsEnrollmentDTO.statusEffectiveDate) {
//             pEnrollment.setStatusEffectiveDate(CalendarUtils.convertToSpcfCalendar(pEftpsEnrollmentDTO.statusEffectiveDate));
//        }
//        pEnrollment.setStatusReason(pEftpsEnrollmentDTO.statusReason);
//        if (null != pEftpsEnrollmentDTO.submittedDate) {
//             pEnrollment.setSubmittedDate(CalendarUtils.convertToSpcfCalendar(pEftpsEnrollmentDTO.submittedDate));
//        }
//
//        pEnrollment.setContactFirstName(pEftpsEnrollmentDTO.contactFirstName);
//        pEnrollment.setContactLastName(pEftpsEnrollmentDTO.contactLastName);
//        pEnrollment.setContactJobTitle(pEftpsEnrollmentDTO.contactJobTitle);
//        pEnrollment.setContactPhone(pEftpsEnrollmentDTO.contactPhone);
//    }

    @WebMethod
    public Collection<EventWSDTO> queryAllEvents() throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            Collection<EventWSDTO> wsEvents = new ArrayList<EventWSDTO>();

            DomainEntitySet<EventType> events = PayrollServices.entityFinder.findObjects(EventType.class);
            for (EventType event : events) {
                EventWSDTO wsEvent = new EventWSDTO();
                wsEvent.eventTypeCd = event.getEventTypeCd();
                wsEvent.description = event.getDescription();
                wsEvents.add(wsEvent);
            }
            return wsEvents;
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @WebMethod
    public Collection<CompanyEventEmailWSDTO> queryCompanyEmailEventParams(@WebParam(name = "sourceSystemCD")String pSourceSystemCD,
                                         @WebParam(name = "sourceCompanyID")String pSourceCompanyID,
                                         @WebParam(name = "companyEventCd")String pCompanyEventCD) throws Exception {

        if (pSourceSystemCD == null || pSourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyID == null || pSourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            Company company = Company.findCompany(
                    pSourceCompanyID,
                    SourceSystemCode.valueOf(pSourceSystemCD));

            EventTypeCode eventTypeCd = null;
            if(pCompanyEventCD != null){
                eventTypeCd = EventTypeCode.valueOf(pCompanyEventCD);
                if (eventTypeCd == null) {
                    throw new RuntimeException("Invalid Event Type Code: " + eventTypeCd);
                }
            }

            DomainEntitySet<CompanyEvent> companyEvents =  CompanyEvent.findCompanyEvents
                    (company, eventTypeCd, null, null, null);

            Collection<CompanyEventEmailWSDTO> companyEventEmailWSDTOS = new ArrayList<CompanyEventEmailWSDTO>();
            for (CompanyEvent companyEvent : companyEvents) {
                Criterion<CompanyEventEmail> where = CompanyEventEmail.CompanyEvent().equalTo(companyEvent);

                Expression<CompanyEventEmail> query =
                        new Query<CompanyEventEmail>()
                               .Where(where)
                               .OrderBy(CompanyEventEmail.EmailTemplateTypeCd(), CompanyEventEmail.StatusCd(), CompanyEventEmail.CreatedDate());

                DomainEntitySet<CompanyEventEmail> companyEventEmails = Application.find(CompanyEventEmail.class, query);

                for (CompanyEventEmail companyEventEmail : companyEventEmails) {
                    CompanyEventEmailWSDTO companyEventEmailWSDTO = new CompanyEventEmailWSDTO();

                    companyEventEmailWSDTO.emailTemplateTypeCd = companyEventEmail.getEmailTemplateTypeCd().toString();

                    companyEventEmailWSDTOS.add(companyEventEmailWSDTO);
                    DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams = companyEventEmail.getEmailParamsForEmailEvent();
                    for (CompanyEventEmailParam companyEventEmailParam : companyEventEmailParams) {
                        if (companyEventEmailWSDTO.eventEmailParams == null) {
                            companyEventEmailWSDTO.eventEmailParams = new ArrayList<CompanyEventEmailParamWSDTO>();
                        }

                        CompanyEventEmailParamWSDTO companyEventEmailParamWSDTO = new CompanyEventEmailParamWSDTO();

                        companyEventEmailParamWSDTO.eventDetailTypeCd = companyEventEmailParam.getParamTypeCd().toString();
                        companyEventEmailParamWSDTO.value = companyEventEmailParam.getValue();

                        companyEventEmailWSDTO.eventEmailParams.add(companyEventEmailParamWSDTO);
                    }
                }
            }

            PayrollServices.commitUnitOfWork();

            return companyEventEmailWSDTOS;
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        }
    }

    @WebMethod
    public Collection<PayeeWSDTO> queryPayeesByCompanyId (@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                               @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            DomainEntitySet<Payee> payees = Payee.findPayees(company);
            ArrayList<PayeeWSDTO> payeeWSDTOs = new ArrayList<PayeeWSDTO>();
            for (Payee payee : payees) {
                payeeWSDTOs.add(buildPayeeWSDTO(payee));
            }
            PayrollServices.rollbackUnitOfWork();

            return payeeWSDTOs;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public Collection<PayeeBankAccountWSDTO> queryPayeeBankAccounts (@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                               @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                               @WebParam(name = "sourcePayeeID") String sourcePayeeID) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (sourcePayeeID == null || sourcePayeeID.trim().length() == 0) {
            throw new RuntimeException("No sourcePayeeID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            Payee payee = Payee.findPayee(company, sourcePayeeID);
            ArrayList<PayeeBankAccountWSDTO> payeeBankAccountWSDTOs = new ArrayList<PayeeBankAccountWSDTO>();
            for (PayeeBankAccount payeeBankAccount : payee.getPayeeBankAccountCollection()) {
                payeeBankAccountWSDTOs.add(buildPayeeBankAccountWSDTO(payeeBankAccount));
            }
            PayrollServices.rollbackUnitOfWork();

            return payeeBankAccountWSDTOs;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void setCompanyDebugLogging(@WebParam(name = "sourceSystemCD")String pSourceSystemCD,
                                         @WebParam(name = "sourceCompanyID")String pSourceCompanyID,
                                         @WebParam(name = "debugLogging")boolean pDebugLogging) throws Exception {

        if (pSourceSystemCD == null || pSourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyID == null || pSourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            Company company = Company.findCompany(
                    pSourceCompanyID,
                    SourceSystemCode.valueOf(pSourceSystemCD));
            CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);

            companyDTO.setDebugLogging(pDebugLogging);

            ProcessResult<Company> updateCompanyPR = PayrollServices.companyManager.updateCompany
                (SourceSystemCode.valueOf(pSourceSystemCD), companyDTO.getCompanyId(), companyDTO);
            if (!updateCompanyPR.isSuccess()) {
                throw new RuntimeException(updateCompanyPR.toString());
            }

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public RAFEnrollmentWSDTO updateRAFEnrollmentStatus(@WebParam(name="psid")String psid, @WebParam(name="newStatus")String status) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            Application.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            RAFEnrollmentStatus newStatus = RAFEnrollmentStatus.valueOf(status);
            if (newStatus == null) {
                throw new RuntimeException("Invalid RAFEnrollmentStatus: " + status);
            }

            ProcessResult processResult = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                newStatus);

            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            company = Company.findCompany(psid, SourceSystemCode.QBDT);
            RAFEnrollmentWSDTO currentRAFEnrollmentWSDTO = buildRAFEnrollmentWSDTO(company.getCurrentRAFEnrollment());
            Application.rollbackUnitOfWork();

            return currentRAFEnrollmentWSDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public RAFEnrollmentWSDTO reinitiateRAFEnrollment(@WebParam(name="psid")String psid) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            Application.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            ProcessResult processResult = PayrollServices.companyManager.reInitiateRAFEnrollment(company.getCurrentRAFEnrollment());
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            company = Company.findCompany(psid, SourceSystemCode.QBDT);
            RAFEnrollmentWSDTO currentRAFEnrollmentWSDTO = buildRAFEnrollmentWSDTO(company.getCurrentRAFEnrollment());
            Application.rollbackUnitOfWork();
            
            return currentRAFEnrollmentWSDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public RAFEnrollmentWSDTO rejectRAFEnrollment(@WebParam(name="psid")String psid, @WebParam(name="rejectReason")String rejectReason) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            Application.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            ProcessResult processResult = PayrollServices.companyManager.rejectRAFEnrollment(company.getCurrentRAFEnrollment(), rejectReason);
            if (!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            company = Company.findCompany(psid, SourceSystemCode.QBDT);
            RAFEnrollmentWSDTO currentRAFEnrollmentWSDTO = buildRAFEnrollmentWSDTO(company.getCurrentRAFEnrollment());
            Application.rollbackUnitOfWork();

            return currentRAFEnrollmentWSDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<RAFEnrollmentWSDTO> getAllRAFEnrollments(@WebParam(name="psid")String psid) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        ArrayList<RAFEnrollmentWSDTO> rafEnrollmentDTOs = new ArrayList<RAFEnrollmentWSDTO>();

        try {
            Application.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (null == company) {
                throw new RuntimeException("Company not found");
            }

            DomainEntitySet<RAFEnrollment> rafEnrollments = company.getAllRAFEnrollments();

            for (RAFEnrollment currentRAFEnrollment : rafEnrollments) {
                RAFEnrollmentWSDTO currentRAFEnrollmentWSDTO = buildRAFEnrollmentWSDTO(currentRAFEnrollment);
                rafEnrollmentDTOs.add(currentRAFEnrollmentWSDTO);
            }
            Application.rollbackUnitOfWork();

            return rafEnrollmentDTOs;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private RAFEnrollmentWSDTO buildRAFEnrollmentWSDTO(RAFEnrollment pRAFEnrollment) {
        RAFEnrollmentWSDTO dto = new RAFEnrollmentWSDTO();
        if (pRAFEnrollment.getRAFEnrollmentDetail()!=null) {
            RAFEnrollmentDetail detail = pRAFEnrollment.getRAFEnrollmentDetail();
            dto.fedTaxId  = detail.getFedTaxid();
            dto.legalName = detail.getLegalName();
            dto.legalCity = detail.getLegalCity();
            dto.legalState = detail.getLegalState();
            dto.legalStreetAddress = detail.getLegalStreetAddress();
            dto.legalZipCode = detail.getLegalZipCode();
            dto.f940TaxPeriod = detail.getF940TaxPeriod();
            dto.f941TaxPeriod = detail.getF941TaxPeriod();
            dto.f94xFTDPeriod = detail.getF94xFTDPeriod();
        }

        dto.status = pRAFEnrollment.getStatus().toString();
        dto.statusReason = pRAFEnrollment.getStatusReason();

        dto.id = pRAFEnrollment.getId().toString();

        return dto;
    }

    @WebMethod
    public void activateAssistedCompany(@WebParam(name = "PSID") String pCompanyId,
                                        @WebParam(name = "AgreementNumber") String pAgreementNumber) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        Application.beginUnitOfWork();
        Company pCompany = Company.findCompany(pCompanyId, SourceSystemCode.QBDT);
        Application.rollbackUnitOfWork();

        DataLoadServices.addCompanyPIN(pCompany, null);
        DataLoadServices.addCompanyBankAccount(pCompany);
        DataLoadServices.updateCompanyService(pCompany, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
    }

    @WebMethod
    public CompanyWSDTO addCompanyAsRep(SAPAddCompany pSAPAddCompany) throws Exception {
        CompanyWSDTO response = new CompanyWSDTO();

        try {
            //Set PspPrincipal so isAgent = true
            PayrollServices.setCurrentPrincipal(new PspPrincipal("101001100010", "Andy Agent"));
            try {
                new CompanyAdapter().addCompany(pSAPAddCompany);
            } catch (Throwable t) {
                throw new Exception(t);
            }
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<EntitlementUnit> entitlementUnits = EntitlementUnit.findEntitlementUnits(
                    pSAPAddCompany.getLegalInfo().getEin(), pSAPAddCompany.getLicenseNumber(), pSAPAddCompany.getEoc());

            if (entitlementUnits.size() > 1) {
                throw new Exception("More then one EntitlementUnit found for EIN, LicenseNumber, and EOC");    
            }

            if (!entitlementUnits.isEmpty()){
                response.sourceSystemCD = entitlementUnits.get(0).getCompany().getSourceSystemCd().toString();
                response.sourceCompanyID = entitlementUnits.get(0).getCompany().getSourceCompanyId();                 
            }            
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return response;
    }

    private PayeeBankAccountWSDTO buildPayeeBankAccountWSDTO(PayeeBankAccount pPayeeBankAccount) {
        PayeeBankAccountWSDTO payeeBankAccountWSDTO = new PayeeBankAccountWSDTO();
        payeeBankAccountWSDTO.sourceBankAccountId = pPayeeBankAccount.getSourceBankAccountId();
        payeeBankAccountWSDTO.statusCd = pPayeeBankAccount.getStatusCd().toString();
        payeeBankAccountWSDTO.bankAccount = buildBankAccountWSDTO(pPayeeBankAccount.getBankAccount());
        return payeeBankAccountWSDTO;
    }

    private PayeeWSDTO buildPayeeWSDTO(Payee pPayee) {
        PayeeWSDTO payeeWSDTO = new PayeeWSDTO();
        payeeWSDTO.email = pPayee.getEmail();
        payeeWSDTO.mailingAddress = buildAddressWSDTO(pPayee.getMailingAddress());
        payeeWSDTO.name = pPayee.getName();
        payeeWSDTO.phone = pPayee.getPhone();
        payeeWSDTO.sourcePayeeId = pPayee.getSourcePayeeId();
        payeeWSDTO.taxId = pPayee.getTaxId();
        return payeeWSDTO;
    }

    private AddressWSDTO buildAddressWSDTO(Address pAddress) {
        if(pAddress == null) {
            return null;
        }

        AddressWSDTO address = new AddressWSDTO();
        address.addressLine1 = pAddress.getAddressLine1();
        address.addressLine2 = pAddress.getAddressLine2();
        address.addressLine3 = pAddress.getAddressLine3();
        address.city = pAddress.getCity();
        address.state = pAddress.getState();
        address.country = pAddress.getCountry();
        address.zipCode = pAddress.getZipCode();
        address.zipCodeExtension = pAddress.getZipCodeExtension();
        return address;
    }

    @WebMethod
    public Collection<QbdtUnprocessedRequestWSDTO> queryUnprocessedRequests(@WebParam(name = "sourceSystemCD")String pSourceSystemCD,
                                                                            @WebParam(name = "sourceCompanyID")String pSourceCompanyID) throws Exception {
        Collection<QbdtUnprocessedRequestWSDTO> qbdtUnprocessedRequestWSDTOs = new ArrayList<QbdtUnprocessedRequestWSDTO>();
        DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests;
        try{
            PayrollServices.beginUnitOfWork();
            if(pSourceSystemCD == null || pSourceCompanyID == null){
                qbdtUnprocessedRequests = Application.find(QbdtUnprocessedRequest.class);
            }else{
                Company company = Company.findCompany(pSourceCompanyID, SourceSystemCode.valueOf(pSourceSystemCD));
                if(company == null) throw  new RuntimeException("No Company for sourceCompanyID and sourceSystemCD is specified");
                qbdtUnprocessedRequests = Application.find(QbdtUnprocessedRequest.class, QbdtUnprocessedRequest.Company().equalTo(company));
            }
            for (QbdtUnprocessedRequest qbdtUnprocessedRequest : qbdtUnprocessedRequests) {
                qbdtUnprocessedRequestWSDTOs.add(buildQbdtUnprocessedRequestWSDTO(qbdtUnprocessedRequest));
            }
        }finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return qbdtUnprocessedRequestWSDTOs;
    }

    private QbdtUnprocessedRequestWSDTO buildQbdtUnprocessedRequestWSDTO(QbdtUnprocessedRequest pQbdtUnprocessedRequest){
        QbdtUnprocessedRequestWSDTO qbdtUnprocessedRequestWSDTO = new QbdtUnprocessedRequestWSDTO();
        qbdtUnprocessedRequestWSDTO.errorMessage = pQbdtUnprocessedRequest.getErrorMessage();
        qbdtUnprocessedRequestWSDTO.status = pQbdtUnprocessedRequest.getStatus().toString();
        return qbdtUnprocessedRequestWSDTO;
    }

    @WebMethod
    public void updateUnprocessedRequestsToQueued(@WebParam(name = "sourceSystemCD")String pSourceSystemCD,
                                                  @WebParam(name = "sourceCompanyID")String pSourceCompanyID) throws Exception {
        DomainEntitySet<QbdtUnprocessedRequest> qbdtUnprocessedRequests;
        Criterion<QbdtUnprocessedRequest> query = QbdtUnprocessedRequest.Status().equalTo(QbdtRequestStatus.Error);
        try{
            PayrollServices.beginUnitOfWork();
            if(pSourceSystemCD != null && pSourceCompanyID != null){
                Company company = Company.findCompany(pSourceCompanyID, SourceSystemCode.valueOf(pSourceSystemCD));
                query = query.And(QbdtUnprocessedRequest.Company().equalTo(company));
                if(company == null) throw  new RuntimeException("No Company for sourceCompanyID and sourceSystemCD is specified");
            }
            qbdtUnprocessedRequests = Application.find(QbdtUnprocessedRequest.class, query);
            for (QbdtUnprocessedRequest qbdtUnprocessedRequest : qbdtUnprocessedRequests) {
                qbdtUnprocessedRequest.setStatus(QbdtRequestStatus.Queued);
                Application.save(qbdtUnprocessedRequest);
            }
            PayrollServices.commitUnitOfWork();
        }finally{
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String offloadStateACHPayments(@WebParam(name = "offloadGroupCd") String offloadGroupCd,
                                          @WebParam(name = "offloadDate") Calendar offloadDate
    ) throws Exception {
        if (offloadGroupCd == null || offloadGroupCd.trim().length() == 0) {
            throw new RuntimeException("No offloadGroupCd specified");
        }
        if (offloadDate == null) {
            throw new RuntimeException("No offloadDate specified");
        }
        String newLine = System.getProperty("line.separator");
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        SpcfCalendar offloadDt = CalendarUtils.convertToSpcfCalendar(offloadDate);
        try {
            offloader.offloadAndPostOffload(offloadGroupCd, offloadDt, ACHFileType.Tax);
        }
        catch (Exception e) {
            return "Exception offloading ACH Payments" + newLine + e.getMessage();
        }
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<NACHAFile> nachaFiles = NACHAFile.getNACHAFilesForOffloadDate(offloadDt, NACHAFileStatus.Finalized);
        PayrollServices.commitUnitOfWork();
        if (nachaFiles.size() == 0) {
            throw new RuntimeException(String.format("No NACHA File for %s", offloadDt.toLocal().toString()));
        }
        if (nachaFiles.size() > 1) {
            throw new RuntimeException(String.format("More than one NACHA Files for %s", offloadDt.toLocal().toString()));
        }
        StringBuffer fileContents = new StringBuffer();
        File offloadedFile = new File(nachaFiles.get(0).getFileName());
        FileInputStream iStreamFile = new FileInputStream(offloadedFile);
        InputStreamReader iStreamReader = new InputStreamReader(iStreamFile);
        BufferedReader reader = new BufferedReader(iStreamReader);
        fileContents.append(newLine);
        try {
            while (reader.ready()) {
                fileContents.append(reader.readLine()).append(newLine);
            }
            fileContents.append(newLine);
        }
        finally {
            reader.close();
        }
        return fileContents.toString();
    }

    @WebMethod
    public void stateACHEnrollCompany(@WebParam(name = "sourceSystemCd") String sourceSystemCd,
                                      @WebParam(name = "sourceCompanyId") String sourceCompanyId,
                                      @WebParam(name = "paymentTemplateCd") String paymentTemplateCd) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCd == null || sourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCode specified");
        }

        if (sourceCompanyId == null || sourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyId specified");
        }

        if (paymentTemplateCd == null || paymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        try {

            PayrollServices.beginUnitOfWork();
            PayrollServices.paymentManager.updatePaymentAgentEnabledCore(SourceSystemCode.valueOf(sourceSystemCd), sourceCompanyId, sourceSystemCd, PaymentMethod.ACHCredit, true);
            PayrollServices.commitUnitOfWork();
        }
        catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void returnStateACHPayment(@WebParam(name = "checkDate") Calendar checkDate,
                                      @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                      @WebParam(name = "returnCd") String returnCd,
                                      @WebParam(name = "returnDescription") String returnDescription) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (checkDate == null) {
            throw new RuntimeException("No check date specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if (returnCd == null || returnCd.trim().length() == 0) {
            throw new RuntimeException("No returnCode specified");
        }

        if (returnDescription == null || returnDescription.trim().length() == 0) {
            throw new RuntimeException("No returnDescription specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            MoneyMovementTransaction.TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments().setNonDirect().setExecutedOrSuccessful().setPaycheckDate(CalendarUtils.convertToSpcfCalendar(checkDate)).setPaymentTemplateCd(pPaymentTemplateCd);
            DomainEntitySet<MoneyMovementTransaction> mmtsToReturn = finder.find();
            ACHReturnsDataLoader.createTransactionReturns(mmtsToReturn, returnCd, returnDescription);
            PayrollServices.commitUnitOfWork();
        }
        catch (Exception e) {
            throw new RuntimeException("Exception returning state ACH payments" + e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void voidACHCheckPayment(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                    @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                    @WebParam(name = "checkDate") Calendar pCheckDate,
                                    @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                    @WebParam(name = "voidReason") String pVoidReason) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pCheckDate == null) {
            throw new RuntimeException("No check date specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if (pVoidReason == null || pVoidReason.trim().length() == 0) {
            throw new RuntimeException("No returnCode specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyId or sourceSystemCode specified.");
            }
            PaymentMethod[] paymentMethods= {PaymentMethod.CheckPayment};
            MoneyMovementTransaction.TaxPaymentsFinder finder = MoneyMovementTransaction.findTaxPayments().setCompany(company).setNonDirect().setReadyToSend().setPaycheckDate(CalendarUtils.convertToSpcfCalendar(pCheckDate)).setPaymentTemplateCd(pPaymentTemplateCd).setExecutedOrSuccessful().setPaymentMethods(paymentMethods);
            DomainEntitySet<MoneyMovementTransaction> mmtsToVoid = finder.find();
            /*  We'll probably be sending one check for a company on a particular check date, but iterating just in case */
            for (MoneyMovementTransaction mmtToVoid : mmtsToVoid) {
                ProcessResult<MoneyMovementTransaction> processResult = PayrollServices.paymentManager.rejectPayment(mmtToVoid.getId().toString(), pVoidReason);
                assertSuccess(processResult);
            }
            PayrollServices.commitUnitOfWork();
        }
        catch (Exception e) {
            throw new RuntimeException("Exception returning state ACH payments" + e.getMessage());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

	@WebMethod
	public void createAssistedCompanyInPendingPinState(@WebParam(name = "sourceSystemCode") String pSourceSystemCode,
                                                       @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                                       @WebParam(name = "fedTaxId") String pFedTaxId,
                                                       @WebParam(name = "licenseNumber") String pLicenseNumber) throws Exception {

        if (pSourceSystemCode == null || pSourceSystemCode.trim().length() == 0) {
            throw new RuntimeException("No Source System Code specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No Source Company Id specified");
        }

        if (pFedTaxId == null || pFedTaxId.trim().length() == 0) {
            throw new RuntimeException("No Federal Tax Id specified");
        }
        
        if (pLicenseNumber == null || pLicenseNumber.trim().length() == 0) {
            throw new RuntimeException("No License Number specified");
        }
        
        /*  Set PspPrincipal so that isAgent = true  */
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal("101001100010", "Some Agent"));
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            PayrollServices.rollbackUnitOfWork();
            if (company != null) {
                throw new RuntimeException("Company already exists");
            }

            DataLoadServices.newCompany(SourceSystemCode.valueOf(pSourceSystemCode), pSourceCompanyId, pFedTaxId, false, ServiceCode.Cloud);
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCode));
            PayrollServices.rollbackUnitOfWork();
            if (company == null) {
                throw new RuntimeException("Error creating company");
            }
            String entitlementOfferingCode = "09876543210987654321";
            DataLoadServices.addAssistedEntitlementUnit(company, pLicenseNumber, entitlementOfferingCode, true);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }
    @WebMethod
    public void updateDepositFrequencyInPSP(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                    @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                    @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                    @WebParam(name = "depositFrequencyCd") String pDepositFreqCd,
                                    @WebParam(name = "effectiveDate") String pEffectiveDate) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if(pDepositFreqCd.isEmpty()) {
            throw new RuntimeException("No depositFrequencyCd specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyId or sourceSystemCode specified.");
            }

            EffectiveDepositFrequencyDTO dto = new EffectiveDepositFrequencyDTO();
            PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplateCd);
            DepositFrequencyCode depositFrequencyCode = DepositFrequencyCode.valueOf(pDepositFreqCd);
            dto.setAgencyId(paymentTemplate.getAgency().getAgencyId());
            dto.setPaymentTemplateCd(pPaymentTemplateCd);
            dto.setPaymentFrequencyId(depositFrequencyCode);

            dto.setEffectiveDate(PSPDate.getPSPTime());

            ProcessResult processResultUf = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.valueOf(pSourceSystemCd), pSourceCompanyId, dto);

            if(!processResultUf.isSuccess()) {
                throw new RuntimeException(processResultUf.getMessages().toString());
            }
            
            PayrollServices.commitUnitOfWork();
        }
        catch (Exception e) {
            throw new RuntimeException("Exception returning state ACH payments" + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateACHEnrollmentFlagInPSP(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                    @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                    @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                    @WebParam(name = "achEnrollmentFlag") Boolean pAchEnrollmentFlag ) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.isEmpty()) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.isEmpty()) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if(pAchEnrollmentFlag == null) {
            throw new RuntimeException("No pAchEnrollmentFlag specified");
        }

        try {
            new TaxAdapter().updateAgentEnabled(pSourceSystemCd, pSourceCompanyId, pPaymentTemplateCd, pAchEnrollmentFlag);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @WebMethod
    public void updateEDIRegistrationFlagInPSP(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                    @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                    @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                    @WebParam(name = "ediRegistrationFlag") Boolean pEdiRegistrationFlag ) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (pSourceCompanyId == null || pSourceCompanyId.isEmpty()) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        if (pPaymentTemplateCd == null || pPaymentTemplateCd.isEmpty()) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }

        if(pEdiRegistrationFlag == null) {
            throw new RuntimeException("No ediRegistrationFlag specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyId or sourceSystemCode specified.");
            }

            PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplateCd);

            ProcessResult processResult = PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), paymentTemplate.getPaymentTemplateCd(), PaymentMethod.EDI, pEdiRegistrationFlag);

            if(!processResult.isSuccess()) {
                throw new RuntimeException(processResult.getMessages().toString());
            }
            PayrollServices.commitUnitOfWork();

        } catch (Exception e) {
            throw new RuntimeException("Exception returning state ACH payments" + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void runComplianceToolkit(@WebParam(name = "arguments") String arguments) {
        ComplianceToolkit.main(arguments.split(" "));
    }

    @WebMethod
    public void addOrUpdateAdditionalAgencyId(@WebParam(name = "sourceSystemCd") String pSourceSystemCd,
                                    @WebParam(name = "sourceCompanyId") String pSourceCompanyId,
                                    @WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                    @WebParam(name = "idName") String pIdName,
                                    @WebParam(name = "agencyTaxpayerId") String pAgencyTaxpayerId) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSourceSystemCd == null || pSourceSystemCd.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }
        if (pSourceCompanyId == null || pSourceCompanyId.isEmpty()) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }
        if (pPaymentTemplateCd == null || pPaymentTemplateCd.isEmpty()) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }
        if(pIdName == null) {
            throw new RuntimeException("No idName specified");
        }
        if(pAgencyTaxpayerId == null) {
            throw new RuntimeException("No agencyTaxpayerId specified");
        }

        SAPCompanyAgencyPaymentTemplateAgencyId sapCompanyAgencyPaymentTemplateAgencyId = new SAPCompanyAgencyPaymentTemplateAgencyId();
        sapCompanyAgencyPaymentTemplateAgencyId.setId(pAgencyTaxpayerId);
        sapCompanyAgencyPaymentTemplateAgencyId.setName(StringUtils.trimToNull(pIdName));
        try {
            new TaxAdapter().updateAgencyIDs(pSourceSystemCd, pSourceCompanyId, pPaymentTemplateCd, Arrays.asList(sapCompanyAgencyPaymentTemplateAgencyId));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @WebMethod
    public void activateBankAccount(@WebParam (name = "sourceSystem") String sourceSystem,
                                    @WebParam (name = "sourceCompanyId") String sourceCompanyId) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystem));
            CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
            ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
            DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
            PayrollServices.rollbackUnitOfWork();

            TransactionsWS transactionsWS = new TransactionsWS();
            for (FinancialTransaction financialTransaction : verificationTransactions) {
                amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
                transactionsWS.offloadTransaction(financialTransaction.getId().toString());
                transactionsWS.processACHTransaction(financialTransaction.getId().toString());
            }

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
            if(!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void addEmployeeBankAccount(@WebParam (name = "sourceSystem") String sourceSystem,
                                       @WebParam (name = "sourceCompanyId") String sourceCompanyId,
                                       @WebParam (name = "sourceEmployeeId") String pSourceEmployeeId,
                                       @WebParam (name = "employeeBankAccount") EmployeeBankAccountWSDTO pEmployeeBankAccount) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {

            if(sourceCompanyId == null || sourceSystem == null) {
                throw new RuntimeException("Company source system and company id required.");
            }

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystem));

            EmployeeBankAccountDTO employeeBankAccountDTO = new EmployeeBankAccountDTO();
            employeeBankAccountDTO.setEmployeeBankAccountId(pEmployeeBankAccount.sourceBankAccountId);
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setAccountNumber(pEmployeeBankAccount.bankAccount.accountNumber);
            bankAccountDTO.setRoutingNumber(pEmployeeBankAccount.bankAccount.routingNumber);
            bankAccountDTO.setAccountType(BankAccountType.valueOf(pEmployeeBankAccount.bankAccount.bankAccountType));
            bankAccountDTO.setBankName(pEmployeeBankAccount.bankAccount.bankName);
            employeeBankAccountDTO.setBankAccount(bankAccountDTO);

            ProcessResult processResult = PayrollServices.employeeManager.addEmployeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), pSourceEmployeeId, employeeBankAccountDTO);
            if(!processResult.isSuccess()) {
                throw new RuntimeException(processResult.toString());
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    @WebResult(name = "SourceCompanyID")
    public String changeSourceCompanyId(@WebParam (name = "sourceSystemCd") String sourceSystemCd,
                                       @WebParam (name = "sourceCompanyId") String sourceCompanyId,
                                       @WebParam (name = "newSourceCompanyId") String newSourceCompanyId) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystemCd));

            company.setSourceCompanyId(newSourceCompanyId);

            PayrollServices.commitUnitOfWork();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return newSourceCompanyId;
    }

    @WebMethod
    public List<LedgerOperationWSDTO> getLedgerOperationsForCompany(@WebParam (name = "sourceSystemCd") String sourceSystemCd,
                                                                    @WebParam (name = "sourceCompanyId") String sourceCompanyId) {
        List<LedgerOperationWSDTO> operationWSDTOs = new ArrayList<LedgerOperationWSDTO>();
        try {

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<LedgerOperation> ledgerOperations = Application.find(LedgerOperation.class, LedgerOperation.SourceSystemCode().equalTo(SourceSystemCode.valueOf(sourceSystemCd))
                                                                                                                       .And(LedgerOperation.SourceCompanyId().equalTo(sourceCompanyId)));

            for (LedgerOperation ledgerOperation : ledgerOperations) {
                LedgerOperationWSDTO wsDTO = new LedgerOperationWSDTO();
                wsDTO.sourceSystemCode = ledgerOperation.getSourceSystemCode().toString();
                wsDTO.sourceCompanyId = ledgerOperation.getSourceCompanyId();
                wsDTO.amount = SpcfUtils.convertToBigDecimal(ledgerOperation.getAmount());
                wsDTO.checkDate = SpcfUtils.convertSpcfCalendarToDate(ledgerOperation.getCheckDate());
                wsDTO.lawId = ledgerOperation.getLaw().getLawId();
                wsDTO.memo = ledgerOperation.getMemo();
                wsDTO.originalLegalName = ledgerOperation.getOriginalLegalName();
                wsDTO.originalIndex = ledgerOperation.getOriginalIndex();
                wsDTO.messages = ledgerOperation.getMessages();
                wsDTO.status = ledgerOperation.getStatus().toString();

                wsDTO.jobType = ledgerOperation.getLedgerOperationJob().getJobType().toString();
                wsDTO.jobStatus = ledgerOperation.getLedgerOperationJob().getStatus().toString();
                wsDTO.startTime = SpcfUtils.convertSpcfCalendarToDate(ledgerOperation.getLedgerOperationJob().getStartTime());
                wsDTO.finishTime = SpcfUtils.convertSpcfCalendarToDate(ledgerOperation.getLedgerOperationJob().getFinishTime());

                operationWSDTOs.add(wsDTO);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            Application.rollbackUnitOfWork();
        }
        return operationWSDTOs;
    }

    @WebMethod
    public Collection<EmployeeWagePlanWSDTO> getEmployeeWagePlans(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                               @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                               @WebParam(name = "sourceEmployeeId") String sourceEmployeeId) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }

            Criterion<EmployeeWagePlan> criterion = EmployeeWagePlan.Employee().Company().equalTo(company);

            if(StringUtils.isNotEmpty(sourceEmployeeId)) {
                criterion.And(EmployeeWagePlan.Employee().SourceEmployeeId().equalTo(sourceEmployeeId));
            }

            DomainEntitySet<EmployeeWagePlan> employeeWagePlans = Application.find(EmployeeWagePlan.class, criterion);
            return buildEEWagePlanCollection(employeeWagePlans, true);

        }catch(Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void updateW2PrintingPreference(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                           @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                           @WebParam(name = "w2DeliveryPreferenceCd") String w2DeliveryPreferenceCd) {
        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode");
            }
            TaxServiceInfoDTO serviceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(company.getService(ServiceCode.Tax));
            serviceInfoDTO.setW2DeliveryPreferenceCd(DeliveryPreferenceCode.valueOf(w2DeliveryPreferenceCd));

            ProcessResult<CompanyService> processResult = PayrollServices.companyManager.updateService(SourceSystemCode.valueOf(sourceSystemCD), sourceCompanyID, serviceInfoDTO);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                throw new RuntimeException(processResult.toString());
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<LawRateRangeWSDTO> getLawRateRanges() {
        try {
            PayrollServices.beginUnitOfWork();

            List<LawRateRangeWSDTO> dtoList = new ArrayList<LawRateRangeWSDTO>();
            for (LawRateRange lawRateRange : Application.findObjects(LawRateRange.class).sort(LawRateRange.Law().LawId())) {
                dtoList.add(new LawRateRangeWSDTO(lawRateRange.getLaw().getLawId(),
                                                  lawRateRange.getMinRate() == null ? null : SpcfUtils.convertToBigDecimal(lawRateRange.getMinRate()).doubleValue() * 100,
                                                  lawRateRange.getMaxRate() == null ? null : SpcfUtils.convertToBigDecimal(lawRateRange.getMaxRate()).doubleValue() * 100,
                                                  lawRateRange.getPrecision()));
            }
            return dtoList;

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @WebMethod
    public void deleteCompany(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                    @WebParam(name = "sourceCompanyID") String sourceCompanyID) {
        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode "+sourceCompanyID);
            }
            Application.deleteCompany(company.getId().toString());

        }catch(Exception ex){
            throw new RuntimeException("Error deleting company: "+sourceCompanyID +ex.getMessage());
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @WebMethod
    public String updateConsumerRealmId(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                        @WebParam(name = "sourceCompanyID") String sourceCompanyID,
                                        @WebParam(name = "consumerRealmId") String consumerRealmId,
                                        @WebParam(name = "sourceEmployeeId")String sourceEmployeeId) throws Exception {

        SpcfLogger logger = Application.getLogger(CompanyWS.class);
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company == null) {
                logger.info("Invalid sourceCompanyID or sourceSystemCode " + sourceCompanyID);
                throw new RuntimeException("Invalid sourceCompanyID or sourceSystemCode " + sourceCompanyID);
            }
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            if (employees != null && employees.size() != 0) {
                Employee employee = null;
                for (Employee ee : employees) {
                    if (ee.getSourceEmployeeId().equals(sourceEmployeeId)) {
                        employee = ee;
                        break;
                    }
                }
                if (employee == null) {
                    logger.info("No Employee record present.");
                    throw new RuntimeException("No Employee record present.");
                }
                Application.refresh(employee);
                employee.setConsumerRealmId(consumerRealmId);
                Application.save(employee);
                Application.commitUnitOfWork();
            }
        }catch (Throwable e) {
            logger.info("Failed to Update the consumer realmid "+consumerRealmId+"  for company : "+sourceCompanyID+"  and employeeid: "+sourceEmployeeId+" Exception: "+e.getMessage());
            throw new RuntimeException(e);
        }
        return consumerRealmId;
    }
}

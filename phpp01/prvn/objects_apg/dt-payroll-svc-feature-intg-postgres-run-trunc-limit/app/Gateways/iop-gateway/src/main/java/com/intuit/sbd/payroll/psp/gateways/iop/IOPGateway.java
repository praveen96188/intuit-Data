package com.intuit.sbd.payroll.psp.gateways.iop;

import com.intuit.onlinepayroll.webservices.v1.ContractorPaymentCompanyModel;
import com.intuit.onlinepayroll.webservices.v1.EMSPManager;
import com.intuit.onlinepayroll.webservices.v1.EMSPManager_Service;
import com.intuit.onlinepayroll.webservices.v1.PayrollCompanyModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.PspCertificateManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.gateways.iop.exceptions.ServiceUnavailableException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.ClientTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.URL;
import java.util.*;

/**
 * @author Jeff Jones
 */
public class IOPGateway implements IIOPGateway {
    private QName qName;
    private EMSPManager_Service mEMSPManager_Service;

    private static SSLSocketFactory sslSocketFactory;
    private static final Logger logger = LoggerFactory.getLogger(IOPGateway.class);

    private static final String IOP_URL;
    private static final String LOCAL_URL;
    private static final String USERNAME;
    private static final String PASSWORD;
    private static final Boolean USE_CERT_AUTH;
    private static final Boolean USE_BASIC_AUTH;
    private static final String CERT_PASSWORD;
    private static final String SERVICE_UNAVAILABLE = "503";
    private static final String LOCAL_PART = "EMSPManager";
    private static final String NAMESPACE_URI = "http://webservices.onlinepayroll.intuit.com/v1";
    private static final String KEYSTORE_ALIAS = "iop.gateway";
    private static final String REQUEST_TIME_OUT_SECONDS;
    private static final String CONNECT_TIME_OUT_SECONDS;


    static {
        try {
            IOP_URL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_serverurl");
            LOCAL_URL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_localurl");
            USERNAME = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_username");
            PASSWORD = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_password");
            USE_BASIC_AUTH = Boolean.valueOf(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_basicauth"));
            USE_CERT_AUTH = Boolean.valueOf(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_certauth"));
            CERT_PASSWORD = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_cert_password");
            CONNECT_TIME_OUT_SECONDS = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_gateway_connect_time_out_second","60");
            REQUEST_TIME_OUT_SECONDS = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_iop_gateway_request_time_out_second","30");
        } catch (Exception e) {
            logger.error("error reading configurations" +e);
            throw new RuntimeException(e);
        }
    }

    public IOPGateway() throws Throwable {
        qName = new QName(NAMESPACE_URI, LOCAL_PART);

        if (USE_CERT_AUTH) {
            sslSocketFactory = PspCertificateManager.getSSLSocketFactory(KEYSTORE_ALIAS,
                                                                         CERT_PASSWORD);
        }

        try {
            mEMSPManager_Service = new EMSPManager_Service(new URL(IOP_URL + "?wsdl"), qName);
        } catch (Throwable t) {
            String path = "file:///" + LOCAL_URL;
            mEMSPManager_Service = new EMSPManager_Service(new URL(path), qName);
        }
    }

    public List<Integer> getCompaniesWithPayrollActivity(SpcfCalendar pStart, SpcfCalendar pEnd) throws Exception {
        List<Integer> companyList;
        try {
            EMSPManager service = mEMSPManager_Service.getEMSPManager();

            setRequestContextProperties(service);

            companyList = service.getCompaniesWithPayrollActivity(createXMLGregorianCalendar(pStart),
                    createXMLGregorianCalendar(pEnd));
        } catch (ClientTransportException cte) {
            String status = String.valueOf(cte.getArguments()[0]);
            if (status.equals(SERVICE_UNAVAILABLE)) {
                throw new ServiceUnavailableException("IOP Web Service Unavailable", cte);
            } else {
                throw cte;
            }
        } catch (Exception e) {
            logger.warn("Exception in getCompaniesWithPayrollActivity()" +e);
            throw e;
        }
        
        return companyList;
    }

    public PayrollCompanyModel getPaychecksEmployeesCompanyDetails(Long pCompanyId,
                                                                   SpcfCalendar pStart,
                                                                   SpcfCalendar pEnd) throws Exception {
        PayrollCompanyModel payrollCompanyModel;
        try {
            EMSPManager service = mEMSPManager_Service.getEMSPManager();

            setRequestContextProperties(service);

            payrollCompanyModel = service.getPayrollCompanyModel(pCompanyId, createXMLGregorianCalendar(pStart),
                    createXMLGregorianCalendar(pEnd));
        } catch (ClientTransportException cte) {
            String status = String.valueOf(cte.getArguments()[0]);
            if (status.equals(SERVICE_UNAVAILABLE)) {
                throw new ServiceUnavailableException("IOP Web Service Unavailable", cte);
            } else {
                throw cte;
            }
        } catch (Exception e) {
            logger.warn("exception in getPaychecksEmployeesCompanyDetails()" +e);
            throw e;
        }
        return payrollCompanyModel;
    }

    public List<Integer> getCompaniesWithContractorPaymentActivity(SpcfCalendar pStart, SpcfCalendar pEnd) throws Exception {
        List<Integer> companyList;
        try {
            EMSPManager service = mEMSPManager_Service.getEMSPManager();

            setRequestContextProperties(service);

            companyList = service.getCompaniesWithContractorPaymentActivity(createXMLGregorianCalendar(pStart),
                    createXMLGregorianCalendar(pEnd));
        } catch (ClientTransportException cte) {
            String status = String.valueOf(cte.getArguments()[0]);
            if (status.equals(SERVICE_UNAVAILABLE)) {
                throw new ServiceUnavailableException("IOP Web Service Unavailable", cte);
            } else {
                throw cte;
            }
        } catch (Exception e) {
            logger.warn("exception in getCompaniesWithContractorPaymentActivity()" +e);
            throw e;
        }

        return companyList;
    }

    public ContractorPaymentCompanyModel getContractorPaymentCompanyModel(Long pCompanyId,
                                                                   SpcfCalendar pStart,
                                                                   SpcfCalendar pEnd) throws Exception {
        ContractorPaymentCompanyModel contractorPaymentCompanyModel;
        try {
            EMSPManager service = mEMSPManager_Service.getEMSPManager();

            setRequestContextProperties(service);

            contractorPaymentCompanyModel = service.getContractorPaymentCompanyModel(pCompanyId, createXMLGregorianCalendar(pStart),
                    createXMLGregorianCalendar(pEnd));
        } catch (ClientTransportException cte) {
            String status = String.valueOf(cte.getArguments()[0]);
            if (status.equals(SERVICE_UNAVAILABLE)) {
                throw new ServiceUnavailableException("IOP Web Service Unavailable", cte);
            } else {
                throw cte;
            }
        } catch (Exception e) {
            logger.warn("exception in getContractorPaymentCompanyModel()" +e);
            throw e;
        }
        return contractorPaymentCompanyModel;
    }

    private void setRequestContextProperties(EMSPManager pService) throws Exception {
            Map<String, Object> requestContext = ((BindingProvider)pService).getRequestContext();
            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, IOP_URL);

            if (!Application.isProdEnvironment()) {
                HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {
                    public boolean verify(String urlHostName,SSLSession session) {
                        return true;
                    }
                };
                requestContext.put(com.sun.xml.ws.developer.JAXWSProperties.HOSTNAME_VERIFIER, hv);
            }

            if (USE_CERT_AUTH) {
                requestContext.put(com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY, sslSocketFactory);
            }

            if (USE_BASIC_AUTH) {
                requestContext.put(BindingProvider.USERNAME_PROPERTY, USERNAME);
                requestContext.put(BindingProvider.PASSWORD_PROPERTY, PASSWORD);
            }


        int connectionTimeoutSeconds = Integer.parseInt(CONNECT_TIME_OUT_SECONDS);
        //setting the connection timeout
        requestContext.put(BindingProviderProperties.CONNECT_TIMEOUT,  connectionTimeoutSeconds * 1000);


        int requestTimeoutSeconds = Integer.parseInt(REQUEST_TIME_OUT_SECONDS);
        //setting the request timeout
        requestContext.put(BindingProviderProperties.REQUEST_TIMEOUT,  requestTimeoutSeconds * 1000);

    }

    public QName getQName() {
        return qName;
    }

    private XMLGregorianCalendar createXMLGregorianCalendar(SpcfCalendar pSpcfCalendar) {
        XMLGregorianCalendar xmlGC = null;

        try {
            DatatypeFactory df = DatatypeFactory.newInstance();
            xmlGC = df.newXMLGregorianCalendar();

            xmlGC.setYear(pSpcfCalendar.getYear());
            xmlGC.setMonth(pSpcfCalendar.getMonth());
            xmlGC.setDay(pSpcfCalendar.getDay());
            xmlGC.setHour(pSpcfCalendar.getHour());
            xmlGC.setMinute(pSpcfCalendar.getMinute());
            xmlGC.setSecond(pSpcfCalendar.getSecond());
            xmlGC.setMillisecond(pSpcfCalendar.getMillisecond());
        } catch (DatatypeConfigurationException e) {
            logger.warn("Error creating XMLGregorianCalendar object. ", e);
        }

        return xmlGC;
    }
}

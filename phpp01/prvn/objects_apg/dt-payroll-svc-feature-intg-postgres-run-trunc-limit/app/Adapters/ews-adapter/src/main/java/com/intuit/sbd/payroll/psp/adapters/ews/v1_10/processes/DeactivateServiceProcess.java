package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageList;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * Created with IntelliJ IDEA.
 * User: srikanthm180
 * Date: 3/6/13
 * Time: 4:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeactivateServiceProcess extends BaseProcess {

    private EwsDeactivateService mRequest;
    private static final SpcfLogger logger;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(DeactivateServiceProcess.class);
    }

    public DeactivateServiceProcess(EwsDeactivateService pRequest) {
       mRequest = pRequest;
       this.mPSID = pRequest.getPsid();
       mTransmissionsList = new TransmissionsList();
       mPSPTransmission = new PSPTransmission();
       mTransmissionsList.add(mPSPTransmission);

       logger.info("Processing Deactivate_Service Request / PSID: " + this.mPSID);
    }

    @Override
    public EwsDeactivateServiceResponse execute() {
        EwsDeactivateServiceResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.DeactivateService);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsDeactivateServiceResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsDeactivateServiceResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();
            try {
                mPSPTransmission.setFinalizeDateTime(PSPDate.getPSPTime());
                mPSPTransmission.setResponse(response);

                LoggingUtils.logTransmissions(mTransmissionsList);
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();

        mPspCompany = PspFactory.findCompany(mPSID);
        mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());

        //Validate the company has an active EntitlementUnit
        if (mPspCompany.getActivePrimaryEntitlementUnit() == null) {
            throw new EwsException(EwsMessages.noActiveEntitlementUnit());
        }

        if (mRequest.getPin() != null) {
            ProcessResult<Company> authenticationPR = PayrollServices.subscriptionManager.verifyCompanyPIN
                    (SourceSystemCode.QBDT, mPSID, mRequest.getPin());
            if (!authenticationPR.isSuccess()) {
                //Commit psp validation logic
                PayrollServices.commitUnitOfWork();
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                MessageList messageList = authenticationPR.getMessages();
                for (Message message : messageList) {
                    logger.info("MessageCode: " + message.getMessageCode() + " Message: " + message.getMessage());
                }
                throw new EwsException(EwsMessages.convertPSPMessage(messageList.get(0)));
            }
            mPspCompany = authenticationPR.getResult();
        }
    }

    @Override
    protected EwsDeactivateServiceResponse process() throws Exception {
        EwsDeactivateServiceResponse response = new EwsDeactivateServiceResponse();
        EwsBaseServices ewsBaseServices = mRequest.getEwsBaseServices();
        EwsServicesResponse ewsServicesResponse  = new EwsServicesResponse();
        ServiceInfoDTO serviceInfoDTO = null;
        CompanyService companyService;

        if(ewsBaseServices.getCloudV2() != null) {
            EwsBaseService cloudV2Service = ewsBaseServices.getCloudV2();
            serviceInfoDTO = PspFactory.createServiceInfoDTO(cloudV2Service, ServiceCode.CloudV2, null);
            companyService = deactivateService(serviceInfoDTO);
            ewsServicesResponse.setCloudV2Response(EwsFactory.createEwsBaseServiceResponse(companyService));
        }

        if(ewsBaseServices.getViewMyPaycheck() != null) {
            EwsBaseService viewMyPaycheckService = ewsBaseServices.getViewMyPaycheck();
            serviceInfoDTO = PspFactory.createServiceInfoDTO(viewMyPaycheckService, ServiceCode.ViewMyPaycheck, null);
            companyService = deactivateService(serviceInfoDTO);
            ewsServicesResponse.setViewMyPaycheckResponse(EwsFactory.createEwsBaseServiceResponse(companyService));
        }

        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setEwsServicesResponse(ewsServicesResponse);

        return response;
    }
}

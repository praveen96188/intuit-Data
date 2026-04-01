package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessage;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400.PSIMessageWSDTO;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsCompanyResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsEntitlementResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsQueryAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp.EwsQueryAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class QueryAccountProcess extends BaseProcess {
    private EwsQueryAccount mRequest;
    private static final SpcfLogger logger;

    private String mSubscriptionNumber;
    private String mRealmId;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(QueryAccountProcess.class);
    }

    public QueryAccountProcess(EwsQueryAccount pRequest) {
        this.mRequest = pRequest;
        this.mPSID = pRequest.getPsid();
        mSubscriptionNumber =  pRequest.getSubscriptionNumber();
        if (pRequest.getEwsBaseCompany() != null) {
            this.mEIN = pRequest.getEwsBaseCompany().getEin();
            mRealmId = pRequest.getEwsBaseCompany().getRealmId();
        }

        mTransmissionsList = new TransmissionsList();
        mPSPTransmission = new PSPTransmission();
        mTransmissionsList.add(mPSPTransmission);

        logger.info("Processing Query_Account Request / PSID: " + this.mPSID  + " SubscriptionNumber: " + mSubscriptionNumber + " RealmId: " + mRealmId);
    }

    @Override
    public EwsQueryAccountResponse execute() {
        EwsQueryAccountResponse response = null;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.QueryAccount);
            mPSPTransmission.setRequest(mRequest);

            validate();
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsQueryAccountResponse();
            processEwsException(e, response);
        } catch (Throwable t){
            response = new EwsQueryAccountResponse();
            processThrowable(t, response);
        } finally {
            PayrollServices.rollbackUnitOfWork();

            try {
                mPSPTransmission.setFinalizeDateTime(PSPDate.getPSPTime());
                mPSPTransmission.setResponse(response);

                if (mPspCompany != null) {
                    LoggingUtils.logTransmissions(mTransmissionsList);
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }

        return response;
    }

    @Override
    protected void validate() throws Exception {
        mRequest.validate();

        //query by EIN and sub num first because of a EWS issue
        if (mEIN != null && mSubscriptionNumber != null) {
            mPspCompany = PspFactory.findCompanyByEin(mEIN, mSubscriptionNumber);
            if (mRequest.getPsid() != null && mRequest.getPsid().length() > 0) {
                if (!mRequest.getPsid().equals(mPspCompany.getSourceCompanyId())) {
                    EwsMessage ewsMessage = EwsMessages.psidMismatch(mEIN, mSubscriptionNumber, mRequest.getPsid(), mPspCompany.getSourceCompanyId());
                    logger.warn(ewsMessage);
                    CompanyEvent.createPSIDMismatchEvent(mPspCompany, mPSID, String.valueOf(ewsMessage.getCode()), ewsMessage.getMessage());
                }
            }
        } else if (mPSID != null) {
            mPspCompany = PspFactory.findCompany(mPSID);
        } else if (mRealmId != null) {
            mPspCompany = PspFactory.findCompanyByRealmId(mRealmId);
        }
        if (mPspCompany != null) {
            mPSID = mPspCompany.getSourceCompanyId();
            mEIN = mPspCompany.getFedTaxId();
            mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());
        }
    }

    @Override
    protected EwsQueryAccountResponse process() throws Exception {
        EwsQueryAccountResponse response = new EwsQueryAccountResponse();

        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setCompanyResponse(EwsFactory.createEwsCompanyResponse(mPspCompany));
        response.setEwsEntitlementUnitResponses(EwsFactory.createEwsEntitlementUnitResponses(mPspCompany.getPrimaryEntitlementUnits()));

        response.setEwsServicesResponse(EwsFactory.createEwsServicesResponse(mPspCompany));

        return response;
    }

}

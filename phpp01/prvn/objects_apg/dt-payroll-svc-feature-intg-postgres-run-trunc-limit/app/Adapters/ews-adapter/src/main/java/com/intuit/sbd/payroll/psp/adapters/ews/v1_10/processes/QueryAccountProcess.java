package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessage;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsQueryAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.EwsFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.factories.PspFactory;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.LoggingUtils;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.PSPTransmission;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.TransmissionsList;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.log.MoneyMovementLogHelper;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

/**
 * @author Jeff Jones
 */
public class QueryAccountProcess extends BaseProcess {
    private EwsQueryAccount mRequest;
    private static final SpcfLogger logger;
    private PSPRequestContextManager pspRequestContextManager;

    private String mSubscriptionNumber;
    private String mRealmId;

    private TransmissionsList mTransmissionsList;
    private PSPTransmission mPSPTransmission;

    static {
        logger = PayrollServices.getLogger(QueryAccountProcess.class);
    }

    public QueryAccountProcess(EwsQueryAccount pRequest) {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
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
        int statusCode;

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            mPSPTransmission.setInitializeDateTime(PSPDate.getPSPTime());
            mPSPTransmission.setTransmissionType(TransmissionType.QueryAccount);
            mPSPTransmission.setRequest(mRequest);

            validate();
            pspRequestContextManager.clearRequestContext();
            pspRequestContextManager.setRequestContext(mPspCompany, RequestType.SOAP, "Query_Account");
            response = process();

            PayrollServices.commitUnitOfWork();
        } catch (EwsException e) {
            response = new EwsQueryAccountResponse();
            processEwsException(e, response);
        } catch (Throwable t) {
            response = new EwsQueryAccountResponse();
            processThrowable(t, response);
        } finally {
            pspRequestContextManager.clearRequestContext();

            if(mPspCompany != null) {
                if (response.getEwsResponseStatus() == null) {
                    statusCode = -1;
                } else {
                    statusCode = response.getEwsResponseStatus().getCode();
                }
                LoggingUtils.logMigrationMsg(mPSID, mPspCompany, statusCode);
            }

            PayrollServices.rollbackUnitOfWork();

            try {
                removeLoggerContext();
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
            logger.info("Query Account EWS Call by realmID "+mRealmId);
            mPspCompany = PspFactory.findCompanyByRealmId(mRealmId);
        }
        if (mPspCompany != null) {
            setLoggerContext();
            mPSID = mPspCompany.getSourceCompanyId();
            mEIN = mPspCompany.getFedTaxId();
            mTransmissionsList.setCompanySeq(mPspCompany.getId().toString());
            moneyMovementTrackingLog(logger,
                    MoneyMovementLogHelper.EventType.QueryAccount,
                    "Completed Validation");
        }
    }

    @Override
    protected EwsQueryAccountResponse process() throws Exception {
        moneyMovementTrackingLog(logger,
                MoneyMovementLogHelper.EventType.QueryAccount,
                "Started process");
        EwsQueryAccountResponse response = new EwsQueryAccountResponse();

        response.setPsid(mPspCompany.getSourceCompanyId());
        response.setCompanyResponse(EwsFactory.createEwsCompanyResponse(mPspCompany));
        response.setEwsEntitlementUnitResponses(EwsFactory.createEwsEntitlementUnitResponses(mPspCompany.getPrimaryEntitlementUnits()));

        response.setEwsServicesResponse(EwsFactory.createEwsServicesResponse(mPspCompany));
        moneyMovementTrackingLog(logger,
                MoneyMovementLogHelper.EventType.QueryAccount,
                "Completed process");
        return response;
    }

}

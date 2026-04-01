package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessages;
import com.intuit.sbd.payroll.psp.iam.HeaderUtils;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.UpdateCompanyFilingFrequencyRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.UpdateCompanyFilingFrequencyResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.handlers.PspUserAuthZHandler;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_8.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 * <p/>
 * Query company event Process
 */
public class PSPUpdateCompanyFilingFrequency extends DISProcessInterface {
    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(PSPUpdateCompanyFilingFrequency.class);
    }

    public static final int MAX_RESULT_CNT = 1000;
    //@TODO Correct class and update operation
    public static final String UPDATE_OPERATION_CLASS_NAME = "com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter";
    public static final String UPDATE_OPERATION_METHOD_NAME = "refundEmployerTransaction";

    public static Class updateAdapterClass;
    static {
        try {
            updateAdapterClass = Class.forName(UPDATE_OPERATION_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.fatal("PSPUpdateCompanyFilingFrequency static loader failed.");
        }
    }

    private UpdateCompanyFilingFrequencyRequestDISDTO requestDISDTO;
    private ResponseDISDTO responseDISDTO;

    /**
     * Constructor
     *
     * @param pUpdateCompanyFilingFrequencyDISDTO
     *
     */
    public PSPUpdateCompanyFilingFrequency(UpdateCompanyFilingFrequencyRequestDISDTO pUpdateCompanyFilingFrequencyDISDTO) {
        requestDISDTO = pUpdateCompanyFilingFrequencyDISDTO;
    }

    @Override
    public Object process() throws Throwable {
        logger.debug("Entering PSPUpdateCompanyFilingFrequencys.process()");

        responseDISDTO = new UpdateCompanyFilingFrequencyResponseDISDTO();

        SourceSystemEnum sourceSystem = requestDISDTO.getSourceSystem();
        String sourceCompanyId = requestDISDTO.getSourceCompanyId();

        doWork(sourceSystem,
                sourceCompanyId);
        logger.debug("Leaving PSPUpdateCompanyFilingFrequencys.process()");
        return responseDISDTO;
    }

    private void doWork(SourceSystemEnum pSourceSystemCd,
                          String pSourceCompanyId) throws Throwable {
        try {

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(pSourceCompanyId, translateSourceSystemCode(pSourceSystemCd));
            if (company == null) {
                PayrollServices.rollbackUnitOfWork();
                DISMessage disMessage = DISMessages.companyDoesNotExist(pSourceCompanyId);
                logger.info(disMessage.getMessage());
                responseDISDTO = createErrorResponse(disMessage);
                return;
            }

            PayrollServices.rollbackUnitOfWork();

            String token = requestDISDTO.getToken();
            String corpId = null;
            if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_DISPSPUSERAUTH_HANDLER, true)) {
                corpId = RequestAttributesUtils.getAttribute(PspUserAuthZHandler.CORP_ID, String.class);
            }
            else{
                corpId = requestDISDTO.getCorpId();
            }
            if(!(HeaderUtils.isOfflineTicket())) {
                try {
                    PSPHelper.validateUserHasPermissionsInSAP(corpId, token, updateAdapterClass, UPDATE_OPERATION_METHOD_NAME);
                } catch (Exception e) {
                    responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                    return;
                }
            }

            // TODO: DO WORK
            // TODO: SET RESPONSE
//            ((UpdateCompanyAgencyIdResponseDISDTO)responseDISDTO).setRefundTransactionId(refundTransactionId);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Override
    public ResponseDISDTO getResponse() {
        return responseDISDTO;
    }

}

package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.psp;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessages;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.UpdateCompanyFilingFrequencyRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.UpdateCompanyFilingFrequencyResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes.DISProcessInterface;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import static com.intuit.sbd.payroll.psp.adapters.dis.v1_7.PSPDISTranslator.translateSourceSystemCode;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/psp/PSPUpdateCompanyFilingFrequency.java $
 * $Revision: #1 $
 * $DateTime: 2012/08/30 22:27:03 $
 * $Author: JChickanosky $
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
            String corpId = requestDISDTO.getCorpId();
            try {
                PSPHelper.validateUserHasPermissionsInSAP(corpId,token,updateAdapterClass, UPDATE_OPERATION_METHOD_NAME);
            } catch (Exception e) {
                responseDISDTO = createErrorResponse(DISMessages.systemError(e.getMessage()));
                return;
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

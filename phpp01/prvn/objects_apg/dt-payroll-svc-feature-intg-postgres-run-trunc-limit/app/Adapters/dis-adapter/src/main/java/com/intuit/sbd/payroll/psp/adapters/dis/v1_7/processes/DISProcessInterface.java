package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.processes;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.DISResponseDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.DISResponseMessageDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.ResponseStatusEnum;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.ResponseDISDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/processes/DISProcessInterface.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Abstract class that all of the web service process class extend.
 */
public abstract class DISProcessInterface {

    /***
     * Fulfills the jax-ws web service call.
     *
     * @return
     * @throws Exception
     */
    public abstract Object process() throws Throwable;


    /***
     * Get the instance of the response object related to the process from
     *    the implementing class.
     *
     * @return
     */
    public abstract ResponseDISDTO getResponse();

    /***
     * Returns the error response to return the jax-ws WebResult.
     * @param disMessages
     * @return
     */
    public Object createErrorResponse(List<DISMessage> disMessages) {
        List<DISResponseMessageDISDTO> disResponseMessageDISDTOList = new ArrayList<DISResponseMessageDISDTO>();
        for (DISMessage disMessage : disMessages) {
            DISResponseMessageDISDTO disResponseMessageDISDTO = new DISResponseMessageDISDTO();
            disResponseMessageDISDTO.setDISMessage(disMessage);
            disResponseMessageDISDTOList.add(disResponseMessageDISDTO);
        }
        DISResponseDISDTO disResponse = new DISResponseDISDTO();
        disResponse.setDisResponseMessageDISDTO(disResponseMessageDISDTOList);
        ResponseDISDTO response = getResponse();
        response.clearElements();
        disResponse.setStatus(ResponseStatusEnum.Failure);
        response.setDISResponse(disResponse);
        return response;
    }

    /***
     * Returns the error response to return the jax-ws WebResult.
     * @param pDISMessage
     * @return
     */
    public ResponseDISDTO createErrorResponse(DISMessage pDISMessage) {
        List<DISResponseMessageDISDTO> disResponseMessageDISDTOList = new ArrayList<DISResponseMessageDISDTO>();
        DISResponseMessageDISDTO disResponseMessageDISDTO = new DISResponseMessageDISDTO();
        disResponseMessageDISDTO.setDISMessage(pDISMessage);
        disResponseMessageDISDTOList.add(disResponseMessageDISDTO);
        DISResponseDISDTO disResponse = new DISResponseDISDTO();
        disResponse.setDisResponseMessageDISDTO(disResponseMessageDISDTOList);
        ResponseDISDTO response = getResponse();
        response.clearElements();
        disResponse.setStatus(ResponseStatusEnum.Failure);
        response.setDISResponse(disResponse);
        return response;
    }
}

package com.intuit.sbd.payroll.psp.adapters.ade.tools;

import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.api.messages.MessageCode;
import com.intuit.ems.cep.api.messages.MessageLevel;
import com.intuit.ems.cep.api.messages.MessageListGen;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 9/29/13
 * Time: 9:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServiceHelper {
    public static <T> void mergeResultIgnoreEntityDoesNotExist(ServiceResult<T> serviceResult, ServiceResult parentServiceResult) {
        MessageListGen errorMessages = serviceResult.getErrorMessages();
        MessageListGen entityDoesNotExistMessages = serviceResult.getMessages(MessageCode.EntityDoesNotExist.getMessageCode());
        //Merge errors if there are additional errors beyond entity does not exist
        if(errorMessages.size() > entityDoesNotExistMessages.size()){
            parentServiceResult.merge(serviceResult);
        }
    }

    public static void mergeServiceResultWithProcessResult(ServiceResult pServiceResult, ProcessResult pProcessResult) {
        for (Message processResultMessage : pProcessResult.getMessages()) {
            pServiceResult.getMessages().addMessage(Message.class,
                                                    processResultMessage.getSourceId(),
                                                    processResultMessage.getMessageCode(),
                                                    MessageLevel.valueOf(processResultMessage.getLevel().name()),
                                                    processResultMessage.getMessage(),
                                                    new HashMap<String, Object>());
        }
    }
}

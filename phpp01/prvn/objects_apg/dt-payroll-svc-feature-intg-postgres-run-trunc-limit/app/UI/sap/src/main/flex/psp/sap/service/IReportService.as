package psp.sap.service {
import mx.rpc.IResponder;

import psp.sap.service.interfaces.IPSPService;

public interface IReportService extends IPSPService {

    function getReportList(responder:IResponder):void;

    function downloadReport(reportType:String,selectedDate:String, responder:IResponder):void;

    function deleteGeneratedReport(decFile:String, responder:IResponder):void;

}


}

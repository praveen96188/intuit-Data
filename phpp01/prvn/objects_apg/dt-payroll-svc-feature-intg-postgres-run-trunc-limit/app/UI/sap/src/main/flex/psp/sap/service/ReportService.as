package psp.sap.service {

import mx.rpc.AsyncToken;

import mx.rpc.IResponder;

import mx.rpc.remoting.RemoteObject;

public class ReportService extends PSPService implements IReportService {

    public function ReportService() {

        remoteObjectPool = new RemoteObjectPool("reportservice", 2);

    }

    public function get reportRemoteService():RemoteObject {

        return remoteObjectPool.nextAvailable();

    }



    public function getReportList(responder:IResponder):void {

        var remoteToken:AsyncToken =

                AsyncToken(reportRemoteService.getReportList());

        remoteToken.addResponder(responder);

    }

    public function downloadReport(reportType:String,selectedDate:String, responder:IResponder):void {
        var remoteToken:AsyncToken =

                AsyncToken(reportRemoteService.downloadReport(reportType,selectedDate));

        remoteToken.addResponder(responder);

    }

    public function deleteGeneratedReport(decrptedFile:String,responder:IResponder):void {
        var remoteToken:AsyncToken =

                AsyncToken(reportRemoteService.deleteGeneratedReport(decrptedFile));

        remoteToken.addResponder(responder);

    }
}
}

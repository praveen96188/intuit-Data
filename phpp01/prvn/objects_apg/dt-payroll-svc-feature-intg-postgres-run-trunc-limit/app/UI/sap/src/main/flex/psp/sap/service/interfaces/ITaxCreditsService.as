package psp.sap.service.interfaces {
    import mx.rpc.IResponder;

    public interface ITaxCreditsService extends IPSPService {

		function find9061Forms(ein:String, ssn:String, startDate:Date, endDate:Date, responder:IResponder):void;

		
    }
}
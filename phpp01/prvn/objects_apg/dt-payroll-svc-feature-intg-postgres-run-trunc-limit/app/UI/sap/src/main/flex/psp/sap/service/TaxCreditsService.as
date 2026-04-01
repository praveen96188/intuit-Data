package psp.sap.service {
    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.remoting.RemoteObject;

    import psp.sap.service.interfaces.ITaxCreditsService;

    public class TaxCreditsService extends PSPService implements ITaxCreditsService {

        private var taxCreditsRemoteService:RemoteObject = new RemoteObject("taxcreditsservice");

		public function find9061Forms(ein:String, ssn:String, startDate:Date, endDate:Date, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(taxCreditsRemoteService.find9061Forms(ein, ssn, startDate, endDate));
			remoteToken.addResponder(responder);
        }

        public function TaxCreditsService() {
        }
    }
}
package test.mock {
    import mx.rpc.IResponder;

    import org.mock4as.Mock;

    import psp.sap.service.interfaces.ITaxCreditsService;

    public class MockTaxCreditsService extends MockAsyncService implements ITaxCreditsService {

        public function MockTaxCreditsService() {
        }

		public function expectsFind9061Forms(ein:String, ssn:String, startDate:Date, endDate:Date):Mock {
            return expects("find9061Forms").withArgs(ein, ssn, startDate, endDate);
        }
		public function find9061Forms(ein:String, ssn:String, startDate:Date, endDate:Date, responder:IResponder):void {
            record("find9061Forms", ein, ssn, startDate, endDate);
			sendAsyncResult(responder,"find9061Forms");
        }


        
    }
}
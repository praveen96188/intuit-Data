package test.mock
{
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;

import org.mock4as.Mock;

import psp.sap.service.interfaces.IPSPSystemInformationService;

	public class MockPSPSystemInformationService extends MockAsyncService implements IPSPSystemInformationService
	{
		public function MockPSPSystemInformationService()
		{
		}

		public function expectsGetSystemInformation():Mock {
            return expects("getSystemInformation").withArgs();
        }
		public function getSystemInformation(responder:IResponder):void {
            record("getSystemInformation");
			sendAsyncResult(responder,"getSystemInformation");
        }
	}
}
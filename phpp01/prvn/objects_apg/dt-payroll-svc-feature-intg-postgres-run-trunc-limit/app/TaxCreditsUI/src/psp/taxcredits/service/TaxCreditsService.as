package psp.taxcredits.service {
    import flash.events.EventDispatcher;

    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.remoting.RemoteObject;

    import psp.taxcredits.dto.EligibilityInfo;
    import psp.taxcredits.dto.EmployeeInfo;
    import psp.taxcredits.dto.EmployerInfo;

    public class TaxCreditsService extends EventDispatcher {

        private var remoteService:RemoteObject = new RemoteObject("taxcreditsservice");

        public function getPSPDate(responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(remoteService.getPSPDate());
			remoteToken.addResponder(responder);
        }

        public function submitApplication(employerInfo:EmployerInfo, employeeInfo:EmployeeInfo, eligibilityInfo:EligibilityInfo, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(remoteService.submitApplication(employerInfo, employeeInfo, eligibilityInfo));
            remoteToken.addResponder(responder);
        }

         public function getCategories(eligibilityInfo:EligibilityInfo, employeeInfo:EmployeeInfo, responder:IResponder):void {
             var remoteToken:AsyncToken =
                 AsyncToken(remoteService.getCategories(eligibilityInfo, employeeInfo));
             remoteToken.addResponder(responder);
         }

        public function isAddressInRCorEZ(address:String, zipCode:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                AsyncToken(remoteService.isAddressInRCorEZ(address, zipCode));
            remoteToken.addResponder(responder);

        }

		public function submitContactRequest(name:String, phone:String, email:String, responder:IResponder):void {
			var remoteToken:AsyncToken =
				AsyncToken(remoteService.submitContactRequest(name, phone, email));
			remoteToken.addResponder(responder);
        }

    }
}
package testTools.service
{
	import flash.events.EventDispatcher;
	import flash.events.IEventDispatcher;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.AsyncToken;
	import mx.rpc.IResponder;
	import mx.rpc.remoting.RemoteObject;

	public class TestService extends EventDispatcher
	{
		private var testService:RemoteObject = new RemoteObject("testtoolsservice");

		private static var mInstance:TestService = null;
		private static var instanceFunction:Boolean = false;
				
		public function TestService(target:IEventDispatcher=null)
		{
			super(target);
			
			if (instanceFunction) {
				instanceFunction = false;
			} else {
				throw new Error("TestService is a singleton class.  Please use TestService.instance instead of new TestService().");
			}
		}
		
		public static function get instance():TestService {
			if (mInstance == null) {
				instanceFunction = true;
				mInstance = new TestService();
			}
			
			return mInstance;
		}
		
		public function setPSPDate(date:Date,                                         
                                        responder:IResponder):void {
            var remoteToken: AsyncToken =
                testService.setPSPDate(date);

            remoteToken.addResponder(responder);
        }
        
		public function findOffloadGroups(responder:IResponder):void {
			var remoteToken:AsyncToken = testService.findOffloadGroups();
			remoteToken.addResponder(responder);
		}
		
		public function saveOffloadGroup(groupCode:String,
									     groupName:String,
									     groupDescription:String,
									     cutoffTime:String,
									     responder:IResponder):void {
			var remoteToken:AsyncToken = testService.saveOffloadGroup(groupCode, groupName, groupDescription, cutoffTime);
			remoteToken.addResponder(responder);								     	
        }
		
		public function addOffloadGroup(groupCode:String,
								        groupName:String,
								        groupDescription:String,
									    cutoffTime:String,
									    responder:IResponder):void {
			var remoteToken:AsyncToken = testService.addOffloadGroup(groupCode, groupName, groupDescription, cutoffTime);
			remoteToken.addResponder(responder);								     	
        }
        
        public function findOffloadBatches(responder:IResponder):void {
        	var remoteToken:AsyncToken = testService.findOffloadBatches();
        	remoteToken.addResponder(responder);
        }
        
        public function generateNACHAFiles(offloadGroup:String, responder:IResponder):void {
        	var remoteToken:AsyncToken = testService.generateNACHAFiles(offloadGroup);
        	remoteToken.addResponder(responder);
        }
        
        public function changeCompanyOffloadGroup(sourceSystemCd:String, companyId:String, offloadGrpCd:String, responder:IResponder):void {
        	var remoteToken:AsyncToken = testService.changeCompanyOffloadGroup(sourceSystemCd, companyId, offloadGrpCd);
        	remoteToken.addResponder(responder);
        }
        
        public function getEntryDetailRecords(fromDate:Date,
                                            toDate:Date,
                                            offloadGroupCd:String,
                                            firstResult:int,
                                            maxResults:int,
                                            responder:IResponder): void {
        	var remoteToken:AsyncToken = testService.getEntryDetailRecords(fromDate,
                                            toDate,
                                            offloadGroupCd,
                                            firstResult,
                                            maxResults);
			remoteToken.addResponder(responder);
		}
		
		public function createBankReturnsForMoneyMovementTransactions(
											bankReturnDTOs:ArrayCollection,
											responder:IResponder):void {
			var remoteToken:AsyncToken = testService.createBankReturnsForMoneyMovementTransactions(bankReturnDTOs);
			remoteToken.addResponder(responder);										
		}
        		
	}
}
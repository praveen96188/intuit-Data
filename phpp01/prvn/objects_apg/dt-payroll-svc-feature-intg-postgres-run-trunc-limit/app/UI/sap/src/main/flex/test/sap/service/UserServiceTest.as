package test.sap.service
{
	import flexunit.framework.TestSuite;
	
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.application.SAP;
	
	import test.sap.application.SAPTestBase;
	
	public class UserServiceTest extends SAPTestBase
	{
		public static function suite():TestSuite {
			return new TestSuite(UserServiceTest);
		}
		
		/**
         * Test the operations for each role match the requirements 
         */
        private var roleCount:int = 0;
        public function testOperationsMappedToNonAuthRoles():void {
        	runDataLoader("Load User Data Only", testOperationsMappedToNonAuthRoles_Step2, 5);        	
        }
        
        private function testOperationsMappedToNonAuthRoles_Step2(e:ResultEvent):void {
			testOperationsMappedToNonAuthRoles_login();
        }
        
        private function testOperationsMappedToNonAuthRoles_login():void {
        	login(testOperationsMappedToNonAuthRoles_Step3, USERNAME, UserRolesData.ROLES.getItemAt(roleCount) as String);
        }
        
        private function testOperationsMappedToNonAuthRoles_Step3(e:ResultEvent):void {
        	var requirementsOperations:ArrayCollection = UserRolesData[UserRolesData.ROLE_OPERATIONS[roleCount]] as ArrayCollection;
        	assertEquals("number of operations matches does not match " + SAP.instance.session.user.roleIds.getItemAt(0), requirementsOperations.length, SAP.instance.session.user.grantedOperations.length);
        	        	
        	for each(var operation:String in requirementsOperations){
        		assertEquals("operation match not found for role: " + SAP.instance.session.user.roleIds.getItemAt(0) + " operation: " + operation,
        						true, SAP.canPerformOperation(operation));        		
        	}
        	
        	roleCount++;
        	if(roleCount < UserRolesData.ROLES.length){        		
        		SAP.instance.session.logout();
        		testOperationsMappedToNonAuthRoles_login();
        	}         	
        }

	}
}
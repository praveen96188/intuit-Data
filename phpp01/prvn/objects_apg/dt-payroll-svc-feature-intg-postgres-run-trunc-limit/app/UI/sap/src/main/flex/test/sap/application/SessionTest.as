package test.sap.application
{
	import flash.events.Event;
	
	import flexunit.framework.TestSuite;
	
	import mx.events.PropertyChangeEvent;
	
	import psp.sap.application.ApplicationSession;
	import psp.sap.application.events.SAPEvent;
	import psp.sap.model.User;
	
	
	public class SessionTest extends SAPTestBase
	{
		override public function setUp():void {
			super.setUp();
			this.asyncTimeout *= 3;
			
			trackedEvents = [
				SAPEvent.SESSION_STARTED,
				SAPEvent.SESSION_ENDED,
				ApplicationSession.USER_CHANGED,
				ApplicationSession.IS_OPEN_CHANGED
			];
			
			trackActionEventsStart(mSAP.session);
		}
		
		override public function tearDown():void {
			super.tearDown();
			trackActionEventsStop(mSAP.session);	
		}
		
		public static function suite(): TestSuite {
			return new TestSuite(SessionTest);
		}
		
		public function testInactiveSession():void {
			
		}
		
		public function testLogin():void {	
			addAsyncVerifier(mSAP.session, ApplicationSession.IS_OPEN_CHANGED, verifySession, null, onRemoteFault, 4);
			mSAP.session.login(USERNAME, PASSWORD);
		}
		
		private function verifySession(e:Event):void {
			assertTrue("session isOpen", mSAP.session.isOpen);
			assertEquals("session username", USERNAME, mSAP.session.user.userName);
			assertEventHistory([SAPEvent.SESSION_STARTED,
								ApplicationSession.USER_CHANGED,
								ApplicationSession.IS_OPEN_CHANGED
								]);
								
			// use lastEventCaught property
			assertEquals("is open - old value", false, PropertyChangeEvent(lastEventCaught).oldValue);
			assertEquals("is open - new value", true, PropertyChangeEvent(lastEventCaught).newValue);
			
			// use mEventTrap member -- stores history of all events caught through handler attached in setUp
			var userChangedEvent:PropertyChangeEvent = mActionEventHistory.getItemAt(1) as PropertyChangeEvent;
			assertNull("user name - old value", userChangedEvent.oldValue);
			assertEquals("user name - new value", USERNAME, User(userChangedEvent.newValue).userName);

								
			// re-login (expect exception? auto-close existing session and login?)
			// const userid2:String = "userid2";
			// mSAP.session.login(userid2, "password");
			// verify session close event fired (probably need an event stack to verify)
			
		}
	}
}
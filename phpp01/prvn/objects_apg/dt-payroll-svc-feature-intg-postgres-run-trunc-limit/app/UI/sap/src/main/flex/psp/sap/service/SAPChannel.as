package psp.sap.service
{
import flash.external.ExternalInterface;

import mx.messaging.MessageAgent;
import mx.messaging.events.MessageAckEvent;
import mx.messaging.events.MessageFaultEvent;
import mx.messaging.messages.IMessage;

import psp.app.util.CookieUtil;

import psp.sap.application.ApplicationSession;
import psp.sap.application.SAP;
import psp.sap.swfaddress.SWFAddress;
import psp.sap.viewmodel.AbstractExplorer;
import psp.sap.viewmodel.SinglePartPageViewModel;

public class SAPChannel
{
	public function SAPChannel()
	{
	}

	/**
	 * Adds the corpId and authToken of the current user onto the request and then calls the super.send() method
	 * to actually do the sending.  The SAPJavaObject.java adapter on the Java-side processes the corpId and
	 * authorizationToken information and validates that the request is allowed.
	 * @param agent
	 * @param message
	 */
	static public function prepareSAPHandshake(agent:MessageAgent, message:IMessage):void {
		if (message != null) {
			if (SAP.instance.session.isOpen) {
				message.headers["corpId"] = SAP.instance.session.user.corpId;
				message.headers["authorizationToken"] = SAP.instance.session.user.authorizationToken;
				message.headers["timezoneOffset"] = new Date().getTimezoneOffset();
				message.headers["sso_param_iv"] =CookieUtil.getCookie("sso_param_iv");
				setIAMMessageHeaders(message);
				var screenPath:String;
				if (SAP.instance != null && SAP.instance.activeExplorer != null) {
					if(SAP.instance.activeExplorer.activeInspector != null &&
							SAP.instance.activeExplorer.activeInspector.activePage != null &&
							SAP.instance.activeExplorer.activeInspector.activePage is SinglePartPageViewModel) {
						screenPath = SinglePartPageViewModel(SAP.instance.activeExplorer.activeInspector.activePage).generatePageFragment();
					} else  {
						screenPath = SAP.instance.activeExplorer.name;
						var explorer:AbstractExplorer = SAP.instance.activeExplorer as AbstractExplorer;
						if (explorer != null && explorer.activeInspector != null && explorer.activeInspector.activePage != null) {
							screenPath = screenPath + ":" + explorer.activeInspector.activePage.label;
						}
					}
					message.headers["screenPath"] = screenPath;
				}
			}
		}

		agent.addEventListener(MessageAckEvent.ACKNOWLEDGE, onAckMessage, false, 0, true);
		agent.addEventListener(MessageFaultEvent.FAULT, onMessageFault, false, int.MAX_VALUE, true);
	}

	/**
	 * Pull the server returned PSPDate out of the message header and update the global
	 * SAP.instance.PSPDate property.
	 *
	 */
	static protected function onAckMessage(e:MessageAckEvent):void {
		if (e != null && e.message != null && e.message.headers != null) {
			var pspTime:String = e.message.headers["PSPTime"];
			var millis:Number = parseInt(pspTime);
			SAP.instance.setPSPDate(millis);
		}
	}

	//TODO: have ApplicationSession hook into class (i.e. reverse the dependency)
	/**
	 * Listen for exceptions that indicate that a session has ended -- either
	 * via explicit termination or expiration.
	 */
	static protected function onMessageFault(e:MessageFaultEvent):void {
		if(e.faultCode == ApplicationSession.SESSION_EXPIRED || e.faultCode == ApplicationSession.SESSION_TERMINATED) {
			ExternalInterface.call("logout");
		}
	}

	static private function setIAMMessageHeaders(message:IMessage):void {
		message.headers["ticket"] = SAP.instance.session.user.ticket;
		message.headers["authId"] = SAP.instance.session.user.authId;
		message.headers["realmId"] = SAP.instance.session.user.realmId;
	}


}
}
package psp.sap.application
{
import flash.events.EventDispatcher;
import flash.external.ExternalInterface;

import intuit.sbd.flex.framework.service.IgnoreResponder;

import mx.events.PropertyChangeEvent;
import mx.events.PropertyChangeEventKind;
import mx.rpc.AsyncResponder;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import psp.app.util.CommonUtil;
import psp.sap.application.events.SAPEvent;
import psp.sap.model.User;

/**
 * A session represents the period between a user logging in and logging out
 * and provides methods for establishing the session (logging in) and ending
 * the session (logging out.)
 */

[Event(name=SAPEvent.SESSION_AUTHENTICATION_SUCCEEDED, type="psp.sap.application.events.SAPEvent")]
[Event(name=SAPEvent.SESSION_AUTHENTICATION_FAILED, type="psp.sap.application.events.SAPEvent")]
[Event(name=FaultEvent.FAULT, type="mx.rpc.events.FaultEvent")]

[Event(name=USER_CHANGED, type="mx.events.PropertyChangeEvent")]
[Event(name=IS_OPEN_CHANGED, type="mx.events.PropertyChangeEvent")]

public class ApplicationSession extends EventDispatcher
{
	public static const USER_CHANGED:String = "userChanged";
	public static const IS_OPEN_CHANGED:String = "isOpenChanged";

	public static const SESSION_USER_CLOSED:String = "1000";
	public static const SESSION_EXPIRED:String = "1001";
	public static const SESSION_TERMINATED:String = "1002";
	public static const SAP_UNAUTHORIZED_REMOTE_CALL:String = "1003";


	private var mUser:User = null;
	private var mSessionStart:Date;
	private var mSessionExitCode:String = "";
	private var mSessionExitDetail:String = "";

	public function ApplicationSession()
	{
		super();
	}


	[Bindable("userChanged")]
	public function get user(): User {
		return mUser;
	}

	public function set user(value:User):void {
		var oldUser:User = mUser;
		mUser = value;

		dispatchUserPropertyChangedEvent(oldUser, value);
	}

	[Bindable("isOpenChanged")]
	public function get isOpen():Boolean {
		return mUser != null;
	}

	private function dispatchOpenPropertyChangedEvent(oldValue:Boolean, newValue:Boolean):void {
		if (oldValue == newValue) return;

		var changeEvent:PropertyChangeEvent =
				new PropertyChangeEvent(IS_OPEN_CHANGED, false, false, PropertyChangeEventKind.UPDATE, "isOpen", oldValue, newValue, this);
		dispatchEvent(changeEvent);
	}

	private function dispatchUserPropertyChangedEvent(oldUser:User, newUser:User):void {
		if (oldUser == newUser) return;

		var changeEvent:PropertyChangeEvent =
				new PropertyChangeEvent(USER_CHANGED, false, false, PropertyChangeEventKind.UPDATE, "user", oldUser, newUser, this);
		dispatchEvent(changeEvent);
	}

	public function login(userid: String, password: String, succeeded:Function = null, failed:Function = null, faulted:Function = null):void {
		var callbackHandlers:Object = {succeededHandler:succeeded, failedHandler:failed, faultedHandler:faulted};
		var asyncResponder:AsyncResponder = new AsyncResponder(onLoginResult, onLoginFaulted, callbackHandlers);
		SAP.instance.authService.login(userid, password, true, asyncResponder);
	}

	public function loginSSO(unAuthenticatedUser:User, succeeded:Function = null, failed:Function = null, faulted:Function = null):void {
		var callbackHandlers:Object = {succeededHandler:succeeded, failedHandler:failed, faultedHandler:faulted};
		var asyncResponder:AsyncResponder = new AsyncResponder(onLoginResult, onLoginFaulted, callbackHandlers);
		SAP.instance.authService.loginSSO(unAuthenticatedUser, asyncResponder);
	}

	private function onLoginResult(e:ResultEvent, token:Object):void {

		var oldUser:User = mUser;
		var oldIsOpen:Boolean = isOpen;

		mUser = e.result as User;

		if (mUser != null) {
			// execute callback
			if (token["succeededHandler"] != null)
				token["succeededHandler"](e);

			// setup session
			mSessionStart = SAP.instance.PSPDate;

			mSessionExitCode = "";
			mSessionExitDetail = "";

			dispatchEvent(new SAPEvent(SAPEvent.SESSION_STARTED));
			dispatchUserPropertyChangedEvent(oldUser, mUser);
			dispatchOpenPropertyChangedEvent(oldIsOpen, isOpen);
		}
		else {
			// execute callback
			if (token["failedHandler"] != null)
				token["failedHandler"](e);
		}
	}

	private function onLoginFaulted(e:FaultEvent, token:Object):void {
		if (token["faultedHandler"] != null)
			token["faultedHandler"](e);
	}

	public function get loginTime():Date {
		return mSessionStart;
	}

	public function get duration():Number {
		return (SAP.instance.PSPDate.time - mSessionStart.time)/1000;
	}

	[Bindable("propertyChange")]
	public function get exitCode():String {
		return mSessionExitCode;
	}

	[Bindable("propertyChange")]
	public function get exitDetail():String {
		return mSessionExitDetail;
	}

	public function logout(exitCode:String = SESSION_USER_CLOSED, exitData:String = ""): void {
		if (!isOpen)
			return;

		// capture audit trail of session end
		SAP.instance.userService.sapLogout(user.corpId, new IgnoreResponder());

		var oldUser:User = mUser;
		var oldIsOpen:Boolean = isOpen;

		mUser = null;

		mSessionExitCode = exitCode;
		mSessionExitDetail = exitData;

		if(CommonUtil.isDTApp()) {
			// closeApplication is javascript method present in SAPAPP.html which is loading swf file inside AIR Application
			// Location : Repo : sapapp-bundler
			// Path : sapapp-bundler/resource-bundler/SAPAPP.html
			ExternalInterface.call("closeApplication");
		} else {
			var url:String = ExternalInterface.call("window.location.href.toString");

			if(url.indexOf("#") < 0) {
				url = url + "#";
			}

			url+="exitDetail="+exitDetail;
			ExternalInterface.call("eval", "window.location.replace('"+url+"')");
			ExternalInterface.call("eval", "window.location.reload()");
		}
	}

	public function logoutSSO(exitCode:String = SESSION_USER_CLOSED,exitData:String = ""):void {
		if (!isOpen)
			return;
		SAP.instance.userService.sapssoLogout( new IgnoreResponder());
		var oldUser:User = mUser;
		var oldIsOpen:Boolean = isOpen;

		mUser = null;

		mSessionExitCode = exitCode;
		mSessionExitDetail = exitData;

		if(CommonUtil.isDTApp()) {
			// closeApplication is javascript method present in SAPAPP.html which is loading swf file inside AIR Application
			// Location : Repo : sapapp-bundler
			// Path : sapapp-bundler/resource-bundler/SAPAPP.html
			ExternalInterface.call("closeApplication");
		} else {
			var url:String  = ExternalInterface.call("logout");
		}

	}
}
}
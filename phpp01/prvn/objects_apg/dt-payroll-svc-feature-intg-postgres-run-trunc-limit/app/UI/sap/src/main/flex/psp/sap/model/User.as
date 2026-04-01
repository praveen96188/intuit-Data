package psp.sap.model
{
    import flash.events.Event;
    import flash.utils.Dictionary;

    import intuit.sbd.flex.framework.model.EntityObject;

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;
    import mx.core.Application;
    import mx.logging.ILogger;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.PartsEnum;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.viewmodel.AbstractExplorer;
    import psp.sap.viewmodel.AbstractInspectorViewModel;
    import psp.sap.viewmodel.CompanyExplorerViewModel;
    import psp.sap.viewmodel.InspectorTopicViewModel;
    import psp.sap.viewmodel.SettingsExplorerViewModel;
    import psp.sap.viewmodel.UniversalSearchViewModel;
    import psp.sap.viewmodel.events.ViewModelEvent;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPUser")]
	public class User extends EntityObject
	{

        private var logger:ILogger = ClientLoggingTarget.getLogger(this);
        
		public var userName:String;
		public var firstName: String;
		public var lastName: String;
		public var corpId: String;
		public var globalUserId: Number;

        // IAM Attributes
        public var ticket: String;
        public var authId: String;
        public var realmId: String;

        [ArrayElementType("String")]
        public var roleIds:ArrayCollection;
		public var uniqueId:String;
        public var emailAddress:String;
		public var authorizationToken:String;
        public var lastRemoteCallTimestamp:Date;
        public var accountLockedUntil:Date;
        public var numberOfFailedLoginAttempts:int;
		
		[ArrayElementType("psp.sap.model.UserOperation")]
		public var grantedOperations: ArrayCollection;


        public var settingsDictionary:Dictionary;

        [ArrayElementType("psp.sap.model.UserSetting")]
        public function set settings(value:ArrayCollection):void {
            settingsDictionary = new Dictionary(true);
            for each (var setting:UserSetting in value) {
                settingsDictionary[setting.key] = setting.value;
            }
            dispatchEvent(new Event("preferencesChanged"));
        }
		
		[Transient]
		[Bindable ("propertyChange")]
		public function get fullName():String {
            var returnString:String = "";
            if (firstName != null && firstName != "") {
                returnString += firstName;
            }
            if (lastName != null && lastName != "") {
                if (returnString != "") {
                    returnString += " ";
                }
                returnString += lastName;
            }
            if (returnString != "") {
                return returnString;
            } else {
                return corpId;
            }
        }

		[Transient]
		public function isUserHelpDesk():Boolean {
			return (roleIds.contains("HELPDESK"));
		}
		
		[Transient]
		public function isUserDataCustodian():Boolean {
			return (roleIds.contains("DATACUST"));
		}
		
		public function gotoLandingPage(gotoLanding:Boolean):void {
			if (!gotoLanding) {
                SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY).activate();
            } else if(agentIsOperator()) {
				//let's just make operators always go here since there isn't any where else to go
                SAP.instance.explorers.getExplorer(ExplorerEnum.OPERATOR).activate();
			} else {
                //check preference
                var initialPage:String = getPreference(SettingsEnum.INITIAL_PAGE);
				if (initialPage == "Company Search") {
                    SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY).activate();
                    var autoCloseSearch:String = Application.application.getQueryStringParameters()["autoclosesearch"]; //for testing, if needed
			        if(autoCloseSearch == null) {
                        var universalSearchVM:UniversalSearchViewModel = (SAP.instance.partHost.findPartByLabel(PartsEnum.UNIVERSAL_SEARCH) as UniversalSearchViewModel);
                        universalSearchVM.addEventListener(ViewModelEvent.ACTIVATED, function(e:ViewModelEvent):void {
                           universalSearchVM.initialSearch(); 
                        });
                    }
                } else {
                    for each (var explorer:AbstractExplorer in SAP.instance.explorers) {
                        if (! (explorer is SettingsExplorerViewModel) && ! (explorer is CompanyExplorerViewModel)) {
                            if (explorer.permissionGranted()) {
                                var inspector:AbstractInspectorViewModel = explorer.inspectors.getInspectorAt(0);
                                for each (var topic:InspectorTopicViewModel in inspector.topics) {
                                    if (topic.label == initialPage) {
                                        topic.activate(null);
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    //default
                    SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY).activate();
                }


			}
		} 
		
		private function agentIsOperator():Boolean {
			return SAP.canPerformOperation(OperationsEnum.VIEW_OPERATOR_TAB)						
					&& !SAP.canPerformOperation(OperationsEnum.ACCESS_APPLICATION);			
		}
		
		public function canAccessApplication():Boolean {
			return SAP.canPerformOperation(OperationsEnum.ACCESS_APPLICATION);
		}

		public function getPreference(key:String):String {
            if (settingsDictionary == null) {
                return null;
            }
            return settingsDictionary[key];
        }

        //convenience
        public function getPreferenceBoolean(key:String):Boolean {
            return getPreference(key) == "true";
        }

        public function setPreference(key:String, value:String):void {
            //we will just assume that this is not changed by an external process for simplicity and performance
            //thus we will save and write to our local copy and not get a new copy from the server            
            SAP.instance.userService.updatePreference(corpId, key, value, new Responder(onUpdatePreferenceSuccess, onUpdatePreferenceFailure));
            settingsDictionary[key] = value;
            dispatchEvent(new Event("preferencesChanged"));
        }

        private function onUpdatePreferenceFailure(e:FaultEvent):void {
            Alert.show("Failed to update preference", "Failure");
            logger.error(e.fault.faultDetail);
        }

        private function onUpdatePreferenceSuccess(e:ResultEvent):void {
            logger.debug("Updated preference");
        }

        public function setPreferenceBoolean(key:String, value:Boolean):void {            
            setPreference(key, value ? "true" : "false");
        }

        [Bindable(event="preferencesChanged")]
        public function get inlineSettingsEnabled():Boolean {
            return getPreferenceBoolean(SettingsEnum.INLINE_SETTINGS);
        }

        public function get roleId():String {
            var roles:String = "";
            if(roleIds == null || roleIds.length == 0){
                roles =  "";
            }else{
                for(var i:int=0; i < roleIds.length; i++){
                    roles = roles + (roleIds.getItemAt(i) as String) + ((i < roleIds.length-1) ? ", " : "");
                }
            }
            return roles;
        }
	}
}

package psp.sap.application {
    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.getQualifiedClassName;

    import intuit.sbd.flex.framework.application.collections.MostRecentlyUsedCollection;

    import mx.controls.Alert;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.formatters.DateFormatter;
    import mx.formatters.NumberBaseRoundType;
    import mx.formatters.NumberFormatter;
    import mx.logging.ILogger;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.core.Application;
    import psp.app.util.CommonUtil;
    import psp.sap.service.interfaces.rtbAutomation.IVMPAutomationService;
    import org.as3commons.reflect.Field;
    import org.as3commons.reflect.MetaData;
    import org.as3commons.reflect.MetaDataArgument;
    import org.as3commons.reflect.Type;
    import psp.sap.service.rtbAutomation.VMPAutomationService;
    import psp.sap.application.collections.ExplorersCollection;
    import psp.sap.application.collections.InspectorCollection;
    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.application.enums.PartsEnum;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.model.Company;
    import psp.sap.model.User;
    import psp.sap.model.UserOperation;
    import psp.sap.service.AccountingService;
    import psp.sap.service.AdministrationService;
    import psp.sap.service.AuthService;
    import psp.sap.service.BankReturnService;
    import psp.sap.service.BillingHistoryService;
    import psp.sap.service.BillingService;
    import psp.sap.service.CompanyService;
    import psp.sap.service.EmployeeService;
    import psp.sap.service.IBillingHistoryService;
    import psp.sap.service.IReportService;
    import psp.sap.service.LookupService;
    import psp.sap.service.PSPSystemInformationService;
    import psp.sap.service.PayrollRunService;
    import psp.sap.service.PropertyAuditService;
    import psp.sap.service.RTBService;
    import psp.sap.service.ReportService;
    import psp.sap.service.TaxCreditsService;
    import psp.sap.service.TaxService;
    import psp.sap.service.UserService;
    import psp.sap.service.ViewMyPaycheckService;
    import psp.sap.service.interfaces.IAccountingService;
    import psp.sap.service.interfaces.IAdministrationService;
    import psp.sap.service.interfaces.IAuthService;
    import psp.sap.service.interfaces.IBankReturnService;
    import psp.sap.service.interfaces.IBillingService;
    import psp.sap.service.interfaces.ICompanyService;
    import psp.sap.service.interfaces.IEmployeeService;
    import psp.sap.service.interfaces.IPSPSystemInformationService;
    import psp.sap.service.interfaces.IPayrollRunService;
    import psp.sap.service.interfaces.IPropertyAuditService;
    import psp.sap.service.interfaces.IRTBService;
    import psp.sap.service.interfaces.ITaxCreditsService;
    import psp.sap.service.interfaces.ITaxService;
    import psp.sap.service.interfaces.IUserService;
    import psp.sap.service.interfaces.IViewMyPaycheckService;
    import psp.sap.service.interfaces.rtbAutomation.IRTBAutomationService;
    import psp.sap.service.rtbAutomation.RTBAutomationService;
    import psp.sap.swfaddress.SWFAddress;
    import psp.sap.swfaddress.SWFAddressEvent;
    import psp.sap.view.SAPIcons;
    import psp.sap.viewmodel.AbstractExplorer;
    import psp.sap.viewmodel.AbstractInspectorViewModel;
    import psp.sap.viewmodel.AbstractPartViewModel;
    import psp.sap.viewmodel.AccountingExplorerViewModel;
    import psp.sap.viewmodel.AdministrationExplorerViewModel;
    import psp.sap.viewmodel.CompanyExplorerViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.CompositePartViewModel;
    import psp.sap.viewmodel.EINManagementExplorerViewModel;
    import psp.sap.viewmodel.EnrollmentsExplorerViewModel;
    import psp.sap.viewmodel.InspectorTopicViewModel;
    import psp.sap.viewmodel.OperatorExplorerViewModel;
    import psp.sap.viewmodel.PaymentsExplorerViewModel;
    import psp.sap.viewmodel.RTBExplorerViewModel;
    import psp.sap.viewmodel.RiskExplorerViewModel;
    import psp.sap.viewmodel.SettingsExplorerViewModel;
    import psp.sap.viewmodel.SinglePartPageViewModel;
    import psp.sap.viewmodel.TaxCreditsExplorerViewModel;
    import psp.sap.viewmodel.UniversalSearchViewModel;

    [Event(name=ACTIVE_EXPLORER_CHANGED, type="mx.events.PropertyChangeEvent")]
    public class SAP extends EventDispatcher {
        private static const REFRESH_WAIT_PERIOD:Number = 1000;
        public static const MAX_OPEN_COMPANIES:Number = 3;
        public static const ACTIVE_EXPLORER_CHANGED:String = "activeExplorerChanged";
        public static const SHOW_KNOWLEDGE_MANAGEMENT:String = "showKnowledgeManagement";

        public static const SYSTEM_ID:String = "DDUI";
        public static const AUTH_SYSTEM_ID:String = "AUTH";

        private var logger:ILogger = ClientLoggingTarget.getLogger(this);

        private var mConfigurationValues:SAPConfigurationValues = new SAPConfigurationValues();
        private var mIcons:SAPIcons = new SAPIcons();
        private var mApplicationTitle:String = "Service Assistance Portal";
        private var mSession:ApplicationSession = new ApplicationSession();

        private var mPSPDate:Date = new Date();
        private var mLastRefresh:Date;

        private static var mInstance:SAP = new SAP();

        [Bindable]
        public var companyService:ICompanyService = new CompanyService();
        [Bindable]
        public var payrollRunService:IPayrollRunService = new PayrollRunService();
        [Bindable]
        public var billingHistoryService:IBillingHistoryService = new BillingHistoryService();
        [Bindable]
        public var authService:IAuthService = new AuthService();
        [Bindable]
        public var userService:IUserService = new UserService();
        [Bindable]
        public var lookupService:LookupService = new LookupService();
        [Bindable]
        public var bankReturnService:IBankReturnService = new BankReturnService();
        [Bindable]
        public var administrationService:IAdministrationService = new AdministrationService();
        [Bindable]
        public var billingService:IBillingService = new BillingService();
        [Bindable]
        public var systemInformationService:IPSPSystemInformationService = new PSPSystemInformationService();
        [Bindable]
        public var taxService:ITaxService = new TaxService();
        [Bindable]
        public var employeeService:IEmployeeService = new EmployeeService();
        [Bindable]
        public var propertyAuditService:IPropertyAuditService = new PropertyAuditService();
        [Bindable]
        public var taxCreditsService:ITaxCreditsService = new TaxCreditsService();
        [Bindable]
        public var accountingService:IAccountingService = new AccountingService();
        [Bindable]
        public var viewMyPaycheckService:IViewMyPaycheckService = new ViewMyPaycheckService();
        [Bindable]
        public var rtbService:IRTBService = new RTBService();
        [Bindable]
        public var rtbAutomationService:IRTBAutomationService = new RTBAutomationService();
        [Bindable]
        public var vmpAutomationService:IVMPAutomationService = new VMPAutomationService();
        [Bindable]
        public var reportService:IReportService = new ReportService();
        // INITIALIZE ALL 'SESSION' RELATED VALUES IN THE INITIALIZE METHOD
        private var mExplorers:ExplorersCollection = new ExplorersCollection();
        private var mActiveExplorer:AbstractExplorer = null;

        private var mExplorersMenu:ExplorersCollection = new ExplorersCollection();

        // Singleton construction
        private static var mInitialized:Boolean = false;
        mInitialized = true;

        public var displayRegistry:Dictionary = new Dictionary(true);

        public var shortDateFormatter:DateFormatter = new DateFormatter();

        private var gotoLanding:Boolean = true;

        public function SAP() {
            super();

            partHost = new CompositePartViewModel();

            logger.debug("constructing SAP");

            if (mInitialized) {
                var msg:String = "ApplicationModel is a singleton.  Use the instance property to access.";
                logger.error(msg);
                throw new Error(msg);
            }

            partHost.addNewPart(UniversalSearchViewModel, PartsEnum.UNIVERSAL_SEARCH);

            mSession.addEventListener(SAPEvent.SESSION_STARTED, onSessionStarted, false, int.MAX_VALUE, true);
            mSession.addEventListener(SAPEvent.SESSION_ENDED, onSessionEnded, false, int.MAX_VALUE, true);

            rateFormatter.precision = 2;
            rateFormatter.rounding = NumberBaseRoundType.NEAREST;

            shortDateFormatter.formatString = configuration.dateFormatShort;

            //SWFAddress.onChange = handleSWFAddress;
            SWFAddress.addEventListener(SWFAddressEvent.EXTERNAL_CHANGE, function (e:Event):void {
                handleSWFAddress();
            });
        }

        [Bindable("propertyChange")]
        public static function get instance():SAP {
            return mInstance;
        }

        private function onSessionStarted(e:SAPEvent):void {
            initialize();
        }

        /**
         * All session related clean-up is executed here.
         *
         * When a user session is ended, this method is run.
         */
        private function onSessionEnded(e:SAPEvent):void {
            try {
                MRUEventHandling.disable();
            } catch (err:Error) {
                if (err.message.substr(0, 33) != "Could not find explorer with name")
                    throw err;
            }

            // future: 	this would be better in an eventing model; i.e. the explorers listen
            //			for when they have been removed from the SAP.explorers collection and run
            //			their own custom close down logic
            try {
                var openInspectors:InspectorCollection = explorers.getExplorer(ExplorerEnum.COMPANY).inspectors;
                for (var i:int = openInspectors.length - 1; i >= 0; i--) {
                    openInspectors.getInspectorAt(i).close();
                }
            } catch (err:Error) {
                if (err.message.substr(0, 33) != "Could not find explorer with name")
                    throw err;
            }

            for each(var explorer:AbstractExplorer in explorers) {
                explorer.inspectors.removeAll();
            }

            explorers.refresh();
            explorers.removeAll();

            explorersMenu.refresh();
            explorersMenu.removeAll();
        }

        /**
         * All session related initialization is executed here.
         *
         * When a user session is started, this method is run.  Any initial
         * setup related to this session (including re-initialization of state)
         * needs to occur within this method.
         *
         * NOTE:
         *        this method is not simply the event handlers since it is
         *        called directly from resetForTesting()
         */
        private function initialize():void {

            // only load the lookup values if we not starting SAP from test cases
            if (!testMode) {
                lookupService.attachListeners();
                lookupService.loadData();
            }

            var companyExplorer:CompanyExplorerViewModel = new CompanyExplorerViewModel();
            mExplorers.addItem(companyExplorer);
            mExplorers.addItem(new RiskExplorerViewModel());
            mExplorers.addItem(new AccountingExplorerViewModel());
            mExplorers.addItem(new AdministrationExplorerViewModel());
            mExplorers.addItem(new OperatorExplorerViewModel());
            mExplorers.addItem(new SettingsExplorerViewModel());
            mExplorers.addItem(new TaxCreditsExplorerViewModel());
            mExplorers.addItem(new EnrollmentsExplorerViewModel());
            mExplorers.addItem(new PaymentsExplorerViewModel());
            mExplorers.addItem(new EINManagementExplorerViewModel());
            mExplorers.addItem(new RTBExplorerViewModel());

            //TODO: move collection list size to configuration
            recentlyInspectedCompanies.removeAll();
            openCompanies.removeAll();
            MRUEventHandling.enable(companyExplorer);

            var menu:ExplorersCollection = new ExplorersCollection(mExplorers.source);
            menu.filterFunction = function (item:AbstractExplorer):Boolean {
                return item.showInMenu;
            };
            menu.refresh();

            explorersMenu = menu;

            // should only be null when testing
            if (session.user != null && !testMode) {
                session.user.gotoLandingPage(gotoLanding);
            }

        }

        /**
         * Create the data provider that will populate the explorer menu bar;
         * only show those items that:
         * 1) should ever be visible
         * 2) that the user has permissions to view
         */
        [Bindable]
        public function get explorersMenu():ExplorersCollection {
            return mExplorersMenu;
        }

        public function set explorersMenu(value:ExplorersCollection):void {
            mExplorersMenu = value;
        }

        /**
         * Return the PSP system date as reported by the server.
         */
        [Bindable("propertyChange")]
        public function get PSPDate():Date {
            // always hand back a copy since Flex's dates are mutable
            var date:Date = new Date();
            date.time = mPSPDate.time;
            return date;
        }

        public function setPSPDate(millisecondsSince1970:Number):Date {
            mPSPDate.setTime(millisecondsSince1970);
            return PSPDate;
        }

        /**
         * System wide *application* configuration values.  These are not
         * domain configuration values.
         *
         * You will find here properties for the standard formatter pattern
         * strings for displaying dates, etc.  You will not find domain
         * configuration values like default DD limits.
         */
        [Bindable("propertyChange")]
        public function get configuration():SAPConfigurationValues {
            return mConfigurationValues;
        }

        /**
         * System wide *application* icons.
         * The icons are embedded in one location to reduce the size of the
         * application and memory usage.
         */
        [Bindable("propertyChange")]
        public function get icons():SAPIcons {
            return mIcons;
        }

        //------------------------------------------------
        // Session Handling
        //------------------------------------------------
        /**
         * The user - system interaction.
         */
        [Bindable("propertyChange")]
        public function get session():ApplicationSession {
            return mSession;
        }


        //------------------------------------------------
        // Explorers
        //------------------------------------------------

        /**
         *
         */
        public function get explorers():ExplorersCollection {
            return mExplorers;
        }

        /**
         *
         */
        [Bindable(event=ACTIVE_EXPLORER_CHANGED)]
        public function get activeExplorer():AbstractExplorer {
            return mActiveExplorer;
        }

        public function set activeExplorer(value:AbstractExplorer):void {
            var explorerName:String = value != null ? getQualifiedClassName(value) : "null";
            logger.info("activeExplorer changed explorer: " + explorerName);
            if (mActiveExplorer != value) {
                var oldValue:AbstractExplorer = mActiveExplorer;

                if (oldValue != null) {
                    oldValue.deactivate();
                }

                mActiveExplorer = value;

                var event:PropertyChangeEvent =
                        new PropertyChangeEvent(ACTIVE_EXPLORER_CHANGED,
                                false,
                                false,
                                PropertyChangeEventKind.UPDATE,
                                "activeExplorer",
                                oldValue,
                                mActiveExplorer,
                                this);

                dispatchEvent(event);

                dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "activeExplorerMenuIndex", null, activeExplorerMenuIndex));
            }

        }

        [Bindable("propertyChange")]
        public function get activeExplorerMenuIndex():int {
            if (activeExplorer == null)
                return -1;

            return mExplorersMenu.getItemIndex(activeExplorer);
        }

        public var testMode:Boolean = false;

        /**
         * MRU list of companies that have been opened for inspection in the current session.
         *
         * TODO: serialize out MRU on application exit (explore Flex serialization options.)
         */
        [Bindable]
        public var recentlyInspectedCompanies:MostRecentlyUsedCollection =
                new MostRecentlyUsedCollection(25);

        [Bindable]
        public var openCompanies:MostRecentlyUsedCollection =
                new MostRecentlyUsedCollection(25);

        [Bindable]
        public function get applicationTitle():String {
            return mApplicationTitle;
        }

        public function set applicationTitle(value:String):void {
            mApplicationTitle = value;
        }

        /**
         * Request to display relevant Knowledge Management topics.
         * Each view may elect to handle this request separately;
         * right now, the main MXML file pops up a window.
         *
         * Notice, this is a different approach than Cairngorm
         */

        [Bindable]
        public var isShowingProgress:Boolean = false;

        public function showKnowledgeManagement():void {
            dispatchEvent(new Event(SAP.SHOW_KNOWLEDGE_MANAGEMENT));
        }


        public function showProgress(message:String = null):void {
            dispatchEvent(new SAPEvent("showProgress", message));
            isShowingProgress = true;
        }

        public function hideProgress():void {
            dispatchEvent(new SAPEvent("hideProgress"));
            isShowingProgress = false;
        }


        /**
         * Calls "refresh()" on the currently active page, if any.
         */
        public function refreshActivePage():void {
            if (mLastRefresh == null) {
                mLastRefresh = new Date();
            }
            else {
                // check to see if the refresh wait period has passed
                // the wait period negates any double firing of the key down event
                if (mLastRefresh.time + REFRESH_WAIT_PERIOD < new Date().time) {
                    mLastRefresh = new Date();
                }
                else {
                    return;
                }
            }

            var explorer:AbstractExplorer = SAP.instance.activeExplorer as AbstractExplorer;
            if (explorer == null)
                return;

            var inspector:AbstractInspectorViewModel = explorer.activeInspector;
            if (inspector != null) {
                //refresh the inspector (e.g. company banner)
                inspector.refresh();
                if (inspector.activePage != null) {
                    inspector.activePage.refresh();
                }
            }
        }

        public function displaySavedMessage():void {

        }

        public function resetForTesting():void {
            if (mSession.isOpen)
                mSession.logout();
            initialize();
        }

        //host to add parts defined on SAPApp
        public var partHost:CompositePartViewModel;

        static public function canPerformOperation(pOperationId:String):Boolean {
            var user:User = SAP.instance.session.user;

            if (user == null)
                return false;

            for each (var operation:UserOperation in user.grantedOperations) {
                if (operation.operationId == pOperationId && (operation.domainId == SAP.SYSTEM_ID || operation.domainId == SAP.AUTH_SYSTEM_ID)) {
                    return true;
                }
            }

            return false;
        }

        private var rateFormatter:NumberFormatter = new NumberFormatter();

        public function formatPercentage(number:Number):String {
            if (isNaN(number)) {
                return "";
            } else {
                return rateFormatter.format(number * 100) + "%";
            }
        }

        public function get currentQuarter():String {
            // Jan is 0 not 1. Duh!
            var month:int = PSPDate.month;
            if (month >= 0 && month <= 2) {
                return "Q1";
            }
            if (month >= 3 && month <= 5) {
                return "Q2";
            }
            if (month >= 6 && month <= 8) {
                return "Q3";
            }
            if (month >= 9 && month <= 11) {
                return "Q4";
            }

            // should never happen. I think there are only 12 months, well 11 in zero based.
            return null;
        }

        private function handleSWFAddress():void {
            CommonUtil.checkIfDTApp()
            var fragment:String = getFragmentValue();
            var fragments:Array = fragment.split("/");
            if (fragments.length != 7) {
                //not a deep link
                return;
            }

            //are we in a session?  if not, wait for login
            if (!session.isOpen) {
                gotoLanding = false;
                mSession.addEventListener(SAPEvent.SESSION_STARTED, function (e:Event):void {
                    handleSWFAddress();
                    session.removeEventListener(SAPEvent.SESSION_STARTED, arguments.callee);
                }, false, 0, false);
                return;
            }


            var explorerName:String = decodeURL(fragments[1]);
            var inspectorName:String = decodeURL(fragments[2]);
            var topicName:String = decodeURL(fragments[3]);
            var pageName:String = decodeURL(fragments[4]);
            var activatorString:String = decodeURL(fragments[5]);

            var explorer:AbstractExplorer = this.explorers.getExplorer(explorerName);
            var inspector:AbstractInspectorViewModel = null;

            if (!(explorer is CompanyExplorerViewModel)) {
                inspector = explorer.inspectors.getInspectorAt(0);
            } else {
                for each (var testInspector:AbstractInspectorViewModel in explorer.inspectors) {
                    if (testInspector.persistentLabel == inspectorName) {
                        inspector = testInspector;
                        break;
                    }
                }
            }
            if (inspector == null) {
                //must be a company inspector
                var inspectorParts:Array = inspectorName.split(":");
                if (inspectorParts.length != 2) {
                    return;
                }
                companyService.findCompany(inspectorParts[0], inspectorParts[1], new Responder(
                        function (e:ResultEvent):void {
                            if (e.result == null) {
                                Alert.show("Failed to find company '" + inspectorName);
                            } else {
                                var inspector:CompanyInspectorViewModel = new CompanyInspectorViewModel(explorer);
                                inspector.initialize(Company(e.result));
                                explorer.inspectors.addItem(inspector);
                                handleSWFAddress();
                            }
                        },
                        function (e:FaultEvent):void {
                            Alert.show("Failed to load company '" + inspectorName + "':" + e.fault.faultString);
                        }
                ));
                return;
            }

            var topic:InspectorTopicViewModel = inspector.topics.getTopic(topicName);
            var page:SinglePartPageViewModel = topic.findPage(pageName);
            var activator:Object = deserializeActivator(page.part, activatorString);

            //make sure we're not already there
            if (activeExplorer != null) {
                var activeInspector:AbstractInspectorViewModel = activeExplorer.activeInspector;
                if (activeInspector != null) {
                    var activeTopic:InspectorTopicViewModel = activeInspector.activeTopic;
                    if (activeTopic != null) {
                        var activePage:SinglePartPageViewModel = activeTopic.activePage as SinglePartPageViewModel;
                        if (activePage != null) {
                            if (activePage == page) {
                                //activator changed?
                                if (page.createCurrentActivatorString() == activatorString) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            //validate that all the context properties are in the activator

            var partType:Type = Type.forInstance(page.part);
            for each (var field:Field in partType.fields) {
                var backingPropertyMetaData:Array = field.getMetaData("BackingProperty");
                if (backingPropertyMetaData.length > 0) {
                    var contextArgument:MetaDataArgument = MetaData(backingPropertyMetaData[0]).getArgument("context");
                    var requiredArgument:MetaDataArgument = MetaData(backingPropertyMetaData[0]).getArgument("required");
                    if (contextArgument != null && contextArgument.value == "true") {
                        if (requiredArgument == null || requiredArgument.value == "true") {
                            if (activator == null || !(field.name in activator) || activator[field.name] == null) {
                                //check to see if already set (i.e. can't link to this page, but is in history)
                                if (page.part[field.name] == null && page.part.contextPopulateFunctions.length == 0) { //may not be sufficient
                                    logger.error("Could not deep link to '" + pageName + "' because " + field.name + " was not set");
                                    //if this is a deep link on a company, then the inspector will be in an odd state
                                    if (inspector.activePageLabel == "") {
                                        inspector.activate();
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            page.respondingToFragmentChange = true;
            page.activatePage(activator);
        }


        private function getFragmentValue():String {
            if(!CommonUtil.isDTApp()) {
                return SWFAddress.getValue();
            }

            var licenseNumber:String = Application.application.parameters.licenseNumber;
            var serviceAccountId:String = Application.application.parameters.serviceAccountId;
            var itemNumber:String = Application.application.parameters.itemNumber;
            var eoc:String = Application.application.parameters.eoc;

            if(licenseNumber && serviceAccountId && itemNumber && eoc)
            {
                 return "/EIN-Management//EINs/EINs/licenseNumber=" + licenseNumber + "&itemNumber=" + itemNumber +
                "&serviceAccountId=" + serviceAccountId+"&eoc=" + eoc+"/";
            }else {
                return SWFAddress.getValue();
            }

        }

        private function decodeURL(t:String):String {
            return t.replace(/\*/g, "/").replace(/-/g, " ").replace(/\+/g, "-");
        }

        private function deserializeActivator(part:AbstractPartViewModel, activatorString:String):Object {
            if (activatorString == "") {
                return null;
            }
            var activator:Object = [];
            var params:Array = activatorString.split("&");
            for (var i:int = 0; i < params.length; i++) {
                var param:String = params[i];
                var pair:Array = param.split("=");
                if (pair.length != 2) {
                    return null;
                }
                var key:String = pair[0];
                var field:Field = Type.forInstance(part).getField(key);
                var backingPropertyMetaData:Array = field.getMetaData("BackingProperty");
                if (backingPropertyMetaData.length == 0) {
                    return null;
                }
                var contextArgument:MetaDataArgument = MetaData(backingPropertyMetaData[0]).getArgument("context");
                if (contextArgument == null) {
                    return null;
                }
                if (contextArgument.value != "true") {
                    return null;
                }

                activator[key] = part.deserializeActivatorValue(pair[1], field.type.clazz, key);
            }
            return activator;
        }


    }
}

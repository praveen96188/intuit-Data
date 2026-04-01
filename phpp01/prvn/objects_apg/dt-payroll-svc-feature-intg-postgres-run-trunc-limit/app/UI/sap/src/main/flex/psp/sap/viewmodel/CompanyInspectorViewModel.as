package psp.sap.viewmodel
{
    import flash.display.Sprite;
    import flash.events.Event;

    import mx.binding.utils.BindingUtils;
    import mx.controls.Alert;
    import mx.core.Application;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectUtil;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorTopicEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.application.enums.PartsEnum;
    import psp.sap.model.Company;
    import psp.sap.model.CompanyKey;
    import psp.sap.model.CompanyNote;
    import psp.sap.viewmodel.events.EntityChangeEvent;
    import psp.sap.viewmodel.events.ViewModelEvent;

    public class CompanyInspectorViewModel
		extends AbstractInspectorViewModel
	{
		private var mCompanyKey:CompanyKey;

		private var logger:ILogger = ClientLoggingTarget.getLogger(this);

		private var mIsLoadingCompany:Boolean=false;

		public var partHost:CompositePartViewModel;

		public function CompanyInspectorViewModel(explorer:AbstractExplorer) {
			super(explorer);

			partHost = new CompositePartViewModel();
			partHost.inspector = this;
			BindingUtils.bindProperty(partHost,"company",this,"company");
			BindingUtils.bindProperty(partHost, "companyKey", this, "companyKey");

			partHost.addNewPart(CompanySummaryBannerViewModel,PartsEnum.COMPANY_BANNER);

			//----------------
			// topics
			//----------------
			var companyInfoTopic:CompanyInformationTopicViewModel = new CompanyInformationTopicViewModel(this);
			topics.addItem(companyInfoTopic);

            var employeesInfoTopic:CompanyEmployeesTopicViewModel = new CompanyEmployeesTopicViewModel(this);
            topics.addItem(employeesInfoTopic);

            if (SAP.canPerformOperation(OperationsEnum.VIEW_PAYROLL_SCREEN)) {
                var companyPayrollsTopic:CompanyPayrollsTopicViewModel = new CompanyPayrollsTopicViewModel(this);
                topics.addItem(companyPayrollsTopic);
            }

			if(SAP.canPerformOperation(OperationsEnum.VIEW_LEDGER))
			{
				var companyLedgerTopic:CompanyLedgerTopicViewModel = new CompanyLedgerTopicViewModel(this);
				topics.addItem(companyLedgerTopic);
			}

			var companyBanksTopic:CompanyBankAccountsTopicViewModel = new CompanyBankAccountsTopicViewModel(this);
			topics.addItem(companyBanksTopic);

			var companyConnectionLogTopic:CompanyConnectionLogTopicViewModel = new CompanyConnectionLogTopicViewModel(this);
			topics.addItem(companyConnectionLogTopic);

			var companyEventLogTopic:CompanyEventLogTopicViewModel = new CompanyEventLogTopicViewModel(this);
			topics.addItem(companyEventLogTopic);

			if(SAP.canPerformOperation(OperationsEnum.VIEW_CHASE_REPORT))
			{
				var companyChaseReportTopic:CompanyChaseReportTopicViewModel = new CompanyChaseReportTopicViewModel(this);
				topics.addItem(companyChaseReportTopic);
			}

            if(SAP.canPerformOperation(OperationsEnum.VIEW_MONEY_MOVEMENT_SCREEN)) {
                var companyMmtTopic:CompanyMoneyMvmtTopicViewModel = new CompanyMoneyMvmtTopicViewModel(this);
                topics.addItem(companyMmtTopic);
            }

            if (canViewTaxesTopic()) {
                var companyTaxesTopic:CompanyTaxesTopicViewModel = new CompanyTaxesTopicViewModel(this);
                topics.addItem(companyTaxesTopic);
            }

            var companyBillingLogTopic:CompanyBillingTopicViewModel = new CompanyBillingTopicViewModel(this);
			topics.addItem(companyBillingLogTopic);

            if(SAP.canPerformOperation(OperationsEnum.VIEW_VMP_DATA)) {
                var companyVmpDataTopic:CompanyVMPDataTopicViewModel = new CompanyVMPDataTopicViewModel(this);
                topics.addItem(companyVmpDataTopic);
            }

			// don't listen for save events from other pages in testing mode
			if(!SAP.instance.testMode){
				// listen for SAP events
				SAP.instance.addEventListener(EntityChangeEvent.ENTITY_SAVED, onCompanySaved, false, 0, true);
				SAP.instance.addEventListener(EntityChangeEvent.PAGE_REFRESH, onPageRefresh, false, 0, true);
			}

			this.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE,onPropertyChange,false,0,true);
		}

        public static function canViewTaxesTopic():Boolean {
            return SAP.canPerformOperation(OperationsEnum.VIEW_COMPANY_TAX_PAYMENTS)
                    || SAP.canPerformOperation(OperationsEnum.VIEW_TAX_LEDGER)
                    || SAP.canPerformOperation(OperationsEnum.VIEW_AGENCY_INFO)
                    || SAP.canPerformOperation(OperationsEnum.CREATE_MANUAL_LEDGER_ENTRY)
        }

		private function onPropertyChange(event:PropertyChangeEvent):void{
			if (event.property == "applicationItem"){
				onApplicationItemChanged();
			}
		}

		private function onApplicationItemChanged():void {
			//esentially when the application item changes, the company is changing
			//so we need to dispatch the event
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this,"company",null,company)); //may not have been null, probably won't matter
			removeTopics();

		}

		private function onCompanySaved(e:EntityChangeEvent):void {
			// todo check entity id
			if(e.entityType == EntityChangeEvent.COMPANY){
				loadModelData();
                partHost.refresh();
			}
		}

		private function onPageRefresh(e:EntityChangeEvent):void {
			logger.info("onPageRefresh called");
			loadModelData();
		}

		public function loadModelData():void {
			if (company != null)
				SAP.instance.companyService.findCompany(company.sourceSystemCd,
																	company.companyId,
																	new Responder(onCompanyResults,onCompanyLoadFaulted));
		}

		public function onCompanyResults(e:ResultEvent):void {
			if (e.result == null) {
                onCompanyNotFound();
                return;
            }
            if (ObjectUtil.compare(this.company,e.result) != 0) {
		 		//only set the company if anything has changed--avoid lots of bindings everywhere
		 		this.company = e.result as Company;
		 	}
		}

        private var notFoundAlert:Boolean = false; //prevent multiple pop-ups if there are two refreshes before the alert is closed
        private function onCompanyNotFound():void {
            var thisInspector:CompanyInspectorViewModel = this;
            if (!notFoundAlert) {
                notFoundAlert = true;
                Alert.show("The company was not found.  It is likely that the Source Company ID has changed.  Please search for the company again.",
                        "Company not found",
                        Alert.OK,
                        Sprite(Application.application),
                        function(e:Event):void {
                            thisInspector.explorer.inspectors.removeItem(thisInspector);
                            SAP.instance.recentlyInspectedCompanies.removeItem(thisInspector.company);
                        },
                        null,
                        Alert.OK);
            }

        }

		public function onCompanyLoadFaulted(e:FaultEvent, token:Object=null):void {
			//TODO: what to do if load faults?
			mIsLoadingCompany = false;
		}

		//-----------------------------
		// ICompanyInspector
		//-----------------------------
		[Bindable]
		public function get company():Company {
			return this.applicationItem as Company;
		}

		public function set company(value:Company):void {
			if (value == this.company)
				return;

			this.applicationItem = value;

			removeTopics();

		}

        private function removeTopics():void {
            //This is to remove topics that are conditional to company information that is not known at initialize time
            if (! company.isQBDTCompany()) {
                remove(CompanyInspectorTopicEnum.CONNECTION_LOG);
            }
            if (!(company.isAssisted || company.isAssistedServiceCancelled)) {
                remove(CompanyInspectorTopicEnum.TAXES);
            }
            if (! company.isVmp) {
                remove(CompanyInspectorTopicEnum.VMP_DATA);
            }
        }


		private function remove(topicToRemoveCd:String):void {
			try {
				var topicToRemove:InspectorTopicViewModel = topics.getTopic(topicToRemoveCd);
				if (topicToRemove != null) {
					topics.removeItem(topicToRemove);
				}
			} catch (errorObject:Error) {
				//Strangely, if the topic is not there, we throw an error. To prevent this if a
				//user does not have permissions to view OFX, or this gets called multiple
				//times (e.g. company refresh), we will catch the error here.
				//eat it (the error), eat it, just in case you repeat it.
			}
		}

		[Bindable]
		public function get companyKey():CompanyKey {
			return mCompanyKey;
		}

		public function set companyKey(value:CompanyKey):void {
			mCompanyKey = value;
		}

		public function initialize(value:Company):void {
			applicationItem = value;
			companyKey = CompanyKey.create(applicationItem);
		}

		//-----------------------------
		// ViewModel specific
		//-----------------------------
		[Bindable("propertyChange")]
		public function get label():String {
			if (company == null) return "";

			return company.legalName;
		}



		[Bindable] public var noteViewState:String = "addNoteCollapsed";
        [Bindable] public var noteText:String;
        [Bindable] public var alert:Boolean;


		public function minimizeNote():void{
			noteViewState = "addNoteCollapsed";
		}

		public function openNote():void {
			noteViewState = "addNoteExpanded";
		}

		public function openOrCloseNote():void {
			noteViewState = (noteViewState == "addNoteExpanded" ? "addNoteCollapsed" : "addNoteExpanded" );
		}

		public function clearNote():void {
			noteText = "";
            alert = false;
			minimizeNote();
		}

		public function saveNote():void {
			var newNote:CompanyNote = new CompanyNote();
			newNote.notes = noteText;
			newNote.insertUserId = SAP.instance.session.user.corpId;
			newNote.createdDate = SAP.instance.PSPDate;
            newNote.alert = alert;

			SAP.instance.companyService.addCompanyNote(company.sourceSystemCd,
											 company.companyId, null, null,
											 newNote,
											 new Responder(onSaveNoteSucceeded, onSaveNoteFaulted));
			clearNote();
		}


		private var mNoteScreenHeight:int=100;

		[Bindable]
		public function get noteScreenHeight():int {
			return mNoteScreenHeight;
		}

		public function set noteScreenHeight(value:int):void{
			mNoteScreenHeight = value;
		}

		protected function onSaveNoteSucceeded(e:ResultEvent):void {
            refresh();
		}

		protected function onSaveNoteFaulted(e:FaultEvent):void {
			Alert.show("Error saving note: "+e.message,"Error");
		}



		override public function refresh():void {
			//many parts may call this at the same time, but we don't want to reload over and over
			if (! mIsLoadingCompany && !notFoundAlert) {
				mIsLoadingCompany = true;
				SAP.instance.companyService.findCompany(company.sourceSystemCd,
																		company.companyId,
																		new Responder(onCompanyRefresh,onCompanyLoadFaulted));
			}
			partHost.refresh();
		}

		public function onCompanyRefresh(event:ResultEvent):void {
            if (event.result == null) {
                onCompanyNotFound();
                return;
            }
            if (ObjectUtil.compare(this.company,event.result) != 0) {
		 		//only set the company if anything has changed--avoid lots of bindings everywhere
		 		this.company = event.result as Company;
		 	}
		 	mIsLoadingCompany = false;
		 	dispatchEvent(ViewModelEvent.createDataLoadResulted());
		}

        override public function get persistentLabel():String {
            if (companyKey != null) {
                return companyKey.sourceSystemCd + ":" + companyKey.companyId;
            } else {
                return super.persistentLabel;
            }
        }

	}
}

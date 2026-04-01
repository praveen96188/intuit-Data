package psp.sap.viewmodel
{
    import flash.events.Event;
    import flash.events.IOErrorEvent;
    import flash.net.FileFilter;
    import flash.net.FileReference;

    import mx.collections.ArrayCollection;
    import mx.events.PropertyChangeEvent;
    import mx.formatters.NumberFormatter;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.CompanyDdLimits;
    import psp.sap.model.CompanyServiceStatus;
    import psp.sap.model.CompanyStatus;
    import psp.sap.model.DisplayStatus;
    import psp.sap.model.ServiceCodeEnum;
    import psp.sap.model.ServiceStatus;
    import psp.sap.model.ServiceSubStatus;
    import psp.sap.validators.SAPValidators;
    import psp.sap.viewmodel.events.EntityChangeEvent;

    public class CompanyEditSubscriptionStatusViewModel
	extends AbstractPartViewModel
	{
        private static const signatureFileType:FileFilter = new FileFilter("Signatures (*.png)", "*.png");

        private var mSelectedStatus:ServiceStatus = null;
        private var mSelectedCompanyServiceStatus:CompanyServiceStatus;
        private var mNumberFormatter:NumberFormatter = new NumberFormatter();
        private var mFormFileRef:FileReference = new FileReference();

        public var selectedServiceCode:String = null;

        [Bindable] [BackingProperty] public var companyStatus:CompanyStatus;
        [Bindable] public var currentStatus:DisplayStatus;
		[Bindable] public var originalSelectedStatus:ServiceStatus;
        [Bindable] public var companyIsOnHold:Boolean = false;
        [Bindable] public var fraudFlag:Boolean = false;
        [Bindable] public var isMultipleSubStatusSelectionAllowed:Boolean = false;
        [Bindable] public var isEEGreaterThanER:Boolean = false;
        [Bindable] public var hasCheckPrintingService:Boolean = true;
        [Bindable] public var hasOpenLedgerOperationFTs:Boolean = false;

        // operations
        [Bindable] public var canEditSubscriptionStatus:Boolean = false;
        [Bindable] public var canEditDdLimits:Boolean = false;
        [Bindable] public var canAddUpdateCheckPrintSignature:Boolean = false;

        [Bindable] [BackingProperty] public var perPayrollLimit:String = "";
        [Bindable] [BackingProperty] public var perEmployeeLimit:String = "";


		public function CompanyEditSubscriptionStatusViewModel()
		{
			this.label = CompanyInspectorPageEnum.SUBSCRIPTION_STATUS;	
			this.reloadOnSave = true;

            mNumberFormatter.precision = 2;
			mNumberFormatter.useThousandsSeparator = false;
			
			SAP.instance.addEventListener(EntityChangeEvent.ENTITY_SAVED, onCompanySavedExternally, false, 0, true);

            this.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, onLimitChanged, false, int.MAX_VALUE, true);

            mFormFileRef.addEventListener(IOErrorEvent.IO_ERROR, onUploadFailure, false, 0, true);
            mFormFileRef.addEventListener(Event.SELECT, onSelect, false, 0, false);
            mFormFileRef.addEventListener(Event.COMPLETE, onUploadComplete, false, 0, true);
		}

        public function openFileDialog():void {
            mFormFileRef.browse([signatureFileType]);
        }
		
		private function onCompanySavedExternally(e:EntityChangeEvent):void {
			if (e.entityType != EntityChangeEvent.COMPANY)
				return;
				
			if (companyKey == null)
				return;
				
			if (companyKey.equals(e.entityId)) {
				refresh();
			}
		}

        public function useDefaultPayrollLimit():void {
			perPayrollLimit = "";
		}

		public function useDefaultEmployeeLimit():void {
			perEmployeeLimit = "";
		}

		[Bindable("propertyChange")]
		public function get serviceName():String {
			return selectedCompanyServiceStatus.serviceCodeEnum.label;
		}

		[Bindable("propertyChange")]
		public function get serviceStatuses():ArrayCollection {
			return selectedCompanyServiceStatus.allowedTransitions;
		}

        protected function get findTransitions():Boolean {
            return true;
        }
		
		[Bindable]
		public function get selectedServiceStatus():ServiceStatus {
			return mSelectedStatus;
		}
		
		public function set selectedServiceStatus(value:ServiceStatus):void {
			var newSelectedStatus:ServiceStatus = value as ServiceStatus; 
			if (newSelectedStatus == null)
				return;
				
			if (mSelectedStatus != null) {
				// clear out changes
				if (mSelectedStatus != originalSelectedStatus) {
					for each (var subStatus:ServiceSubStatus in mSelectedStatus.serviceSubStatusList)
						subStatus.selected = false;
				}
				// reset to original
				else {
					initializedSelectedValues(mSelectedStatus.serviceSubStatusList, selectedCompanyServiceStatus.status.serviceSubStatusList);
				}
			}

			mSelectedStatus = newSelectedStatus;
			
			isMultipleSubStatusSelectionAllowed = (mSelectedStatus.serviceStatusCd == "OnHold");
			if(mSelectedStatus.serviceSubStatusList.length == 1) {
				var serviceSubStatus:ServiceSubStatus = mSelectedStatus.serviceSubStatusList.getItemAt(0) as ServiceSubStatus;
				serviceSubStatus.selected = true;	
			}
			updateCanSave();
		}
		
		[Bindable("propertyChange")]
		public function get selectedServiceSubStatuses():ArrayCollection {
			if (mSelectedStatus == null)
				return null;
				
			var selectedSubStatuses:ArrayCollection = new ArrayCollection(mSelectedStatus.serviceSubStatusList.source);
			selectedSubStatuses.filterFunction = function(item:ServiceSubStatus):Boolean {
				return item.selected;
			};
			selectedSubStatuses.refresh();
			
			return selectedSubStatuses;
		}

		[Bindable("propertyChange")]
		public function get selectedServiceSubStatusesLabel():String {
			
			var label:String = "";
			var selectedSubStatuses:ArrayCollection = this.selectedServiceSubStatuses;

			if (selectedSubStatuses == null)
				return label;
			
			for (var i:int = 0; i < selectedSubStatuses.length - 1; i++) {								
				var subStatus:ServiceSubStatus = selectedSubStatuses.getItemAt(i) as ServiceSubStatus;
				label += (subStatus.subStatusName + ", "); 
			}
			
			if (selectedSubStatuses.length > 0) {
				var lastSubStatus:ServiceSubStatus = selectedSubStatuses.getItemAt(selectedSubStatuses.length - 1) as ServiceSubStatus;
				label += lastSubStatus.subStatusName;
			}
			
			return label;
		}
		
		//use only when only one can be selected (radio buttons)		
		public function set selectedSubStatus(value:ServiceSubStatus):void {
			for each (var subStatus:ServiceSubStatus in this.selectedServiceStatus.serviceSubStatusList) {
				subStatus.selected = (value == subStatus);
			}	
			updateCanSave();
		}
		
		
		[Bindable("propertyChange")]
		public function get canUpdateStatus():Boolean {
			return canEditSubscriptionStatus && selectedCompanyServiceStatus.canUpdateStatus;
		}

        [Bindable]
        public function get selectedCompanyServiceStatus():CompanyServiceStatus {
            return mSelectedCompanyServiceStatus;
        }

        public function set selectedCompanyServiceStatus(value:CompanyServiceStatus):void {
            mSelectedCompanyServiceStatus = value;
            currentStatus = mSelectedCompanyServiceStatus.displayStatus;
			originalSelectedStatus = mSelectedCompanyServiceStatus.status;
			companyIsOnHold = mSelectedCompanyServiceStatus.isServiceOnHold;
            if(mSelectedCompanyServiceStatus.ddLimits != null){
                perEmployeeLimit = mSelectedCompanyServiceStatus.ddLimits.isUsingDefaultEmployeeLimit ? "" : mNumberFormatter.format(mSelectedCompanyServiceStatus.ddLimits.perEmployeeLimit);
                perPayrollLimit = mSelectedCompanyServiceStatus.ddLimits.isUsingDefaultPayrollLimit ? "" : mNumberFormatter.format(mSelectedCompanyServiceStatus.ddLimits.perPayrollLimit);
            }

			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "canUpdateStatus", null, mSelectedCompanyServiceStatus.canUpdateStatus));

			// order of events is important -- must set the list before setting the selectedServiceStatus item below
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "serviceStatuses", null, mSelectedCompanyServiceStatus.canUpdateStatus));

			for each (var serviceStatus:ServiceStatus in mSelectedCompanyServiceStatus.allowedTransitions) {
				if (serviceStatus.serviceStatusCd == mSelectedCompanyServiceStatus.status.serviceStatusCd) {
					// initialize the UI backing property for selected service status
					selectedServiceStatus = serviceStatus;

					// turn on the selection indicator for the current sub-status value(s)
					initializedSelectedValues(serviceStatus.serviceSubStatusList, mSelectedCompanyServiceStatus.status.serviceSubStatusList);
				}
			}

			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "selectedServiceSubStatuses", null, null));
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "selectedServiceSubStatusesLabel", null, null));
			dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "serviceName", null, null));
        }

		override protected function loadModelData():void {
			if (companyKey == null) 
				return;

            loadCount=2;
			SAP.instance.companyService.getCompanyStatus(	companyKey.sourceSystemCd, companyKey.companyId, findTransitions, false, 
															createLoadModelDataResponder(onStatusLoaded));
            SAP.instance.companyService.checkIfOpenLedgerOperationFTsExist(companyKey, createLoadModelDataResponder(onLedgerOperationsChecked));
					
		}

        private function onLedgerOperationsChecked(e:ResultEvent):void {
            hasOpenLedgerOperationFTs=e.result as Boolean;
        }

		private function onStatusLoaded(e:ResultEvent):void {
            companyStatus = e.result as CompanyStatus;
            hasCheckPrintingService = false;
            for each(var service:CompanyServiceStatus in companyStatus.serviceStatusCollection) {
                if(service.serviceCodeEnum == ServiceCodeEnum.CHECK_DISTRIBUTION) {
                    hasCheckPrintingService = true;
                }
            }

            if(companyStatus.serviceStatusCollection.length > 0){
                if(selectedServiceCode != null) {
			        for each(var serviceStatus:CompanyServiceStatus in companyStatus.serviceStatusCollection) {
			          if(serviceStatus.serviceCd == selectedServiceCode) {
                          selectedCompanyServiceStatus = serviceStatus;
                          selectedServiceCode = null;
                      }
			        }
                    if(selectedServiceCode != null) {
                        selectedCompanyServiceStatus = companyStatus.serviceStatusCollection.getItemAt(0) as CompanyServiceStatus;
                        selectedServiceCode = null;
                    }
                }
                else {
                    selectedCompanyServiceStatus = companyStatus.serviceStatusCollection.getItemAt(0) as CompanyServiceStatus;
                }
            }

			fraudFlag = companyStatus.flaggedForFraud;
		}
		
		private function initializedSelectedValues(targetSubStatusList:ArrayCollection, selectedSubStatusList:ArrayCollection):void {
			// turn on the selection indicator for the current sub-status value(s) 
			for each (var serviceSubStatus:ServiceSubStatus in targetSubStatusList) {
				for each (var selectedSubStatus:ServiceSubStatus in selectedSubStatusList) {
					if (serviceSubStatus.subStatusCd == selectedSubStatus.subStatusCd) {
						serviceSubStatus.selected = true;
						break;
					}
				}
			}
		}

        private function get perPayrollLimitValueForCompare():Number {
			return isEmpty(perPayrollLimit) ? selectedCompanyServiceStatus.ddLimits.defaultPayrollLimit : parseFloat(mNumberFormatter.format(perPayrollLimit));
		}

		private function get perEmployeeLimitValueForCompare():Number {
			return isEmpty(perEmployeeLimit) ? selectedCompanyServiceStatus.ddLimits.defaultEmployeeLimit : parseFloat(mNumberFormatter.format(perEmployeeLimit));
		}

		private function get perPayrollLimitValue():Number {
			return isEmpty(perPayrollLimit) ? SAP.instance.configuration.specialNumberForDefault : parseFloat(mNumberFormatter.format(perPayrollLimit));
		}

		private function get perEmployeeLimitValue():Number {
			return isEmpty(perEmployeeLimit) ? SAP.instance.configuration.specialNumberForDefault : parseFloat(mNumberFormatter.format(perEmployeeLimit));
		}

        private function onLimitChanged(e:PropertyChangeEvent):void {
			if ((selectedCompanyServiceStatus == null || selectedCompanyServiceStatus.ddLimits == null) ||
                (e.property != "perPayrollLimit" && e.property != "perEmployeeLimit")) {
        		return;
            }

			isEEGreaterThanER = perEmployeeLimitValueForCompare > perPayrollLimitValueForCompare;
		}

        public function onSelect(e:Event):void {
            SAP.instance.showProgress("Uploading File...");
            mFormFileRef.load();
        }

        public function onUploadComplete(e:Event):void {            
            // save the signature
            SAP.instance.companyService.uploadSignatureFile(companyKey.sourceSystemCd,
                                                            companyKey.companyId,
                                                            mFormFileRef.data,
                                                            createSaveResponder(onSaveSucceeded));
        }

        public function onUploadFailure(e:IOErrorEvent):void {
            SAP.instance.hideProgress();
            saveFaulted = true;
            saveMsg = "Error uploading file: " + e.text;
        }
		
		override protected function evaluateCanSave():Boolean {
			return findTransitions && (companyKey != null)
					// allow a company that is already on hold to save w/no selected subStatuses --> removes hold 
					&& (companyIsOnHold || atLeastOneSelected()) 
					&& super.evaluateCanSave()
                    && !isEEGreaterThanER;
		}	
		
		
		protected function atLeastOneSelected():Boolean {
            if(selectedServiceStatus != null){
                for each (var subStatus:ServiceSubStatus in selectedServiceStatus.serviceSubStatusList) {
                    if(subStatus.selected)
                        return true;
                }
            }
			return false;
		}

        public function fraudReasonSelected():Boolean {
            if(mSelectedStatus.serviceStatusCd == "OnHold"){
                for each (var subStatus:ServiceSubStatus in mSelectedStatus.serviceSubStatusList) {
                    if(subStatus.selected)
                        return true;
                }
            }
            return false;
        }

		override protected function initializeBackingProperties():void {
            clearValidators();
            validators.push(SAPValidators.createNumberValidator(this, "perPayrollLimit", false, 0, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));
            validators.push(SAPValidators.createNumberValidator(this, "perEmployeeLimit", false, 0, SAP.instance.configuration.maxAllowedCurrencyValue, false, 2));

            canEditSubscriptionStatus = SAP.canPerformOperation(OperationsEnum.STATUS_UPDATE);
            canEditDdLimits = SAP.canPerformOperation(OperationsEnum.LIMITS_UPDATE);
            canAddUpdateCheckPrintSignature = SAP.canPerformOperation(OperationsEnum.ADD_UPDATE_CHECK_PRINT_SIGNATURE); 
		}

		override public function get hasChanged():Boolean {
            return hasDdLimitsChanged || hasServiceStatusChanged;
		}

        private function get hasDdLimitsChanged():Boolean {
            if(selectedCompanyServiceStatus == null){
                return false;
            }

            return selectedCompanyServiceStatus.ddLimits != null &&
                   (perEmployeeLimitValue != selectedCompanyServiceStatus.ddLimits.perEmployeeLimit ||
                    perPayrollLimitValue != selectedCompanyServiceStatus.ddLimits.perPayrollLimit);
        }

        private function get hasServiceStatusChanged():Boolean {
            if(!findTransitions || originalSelectedStatus == null || selectedServiceStatus == null){
                return false;
            }

			if (selectedServiceStatus.serviceStatusCd != originalSelectedStatus.serviceStatusCd)
				return true;

			var selectedCount:int = 0;
			for each (var subStatus:ServiceSubStatus in selectedServiceStatus.serviceSubStatusList) {
				// verify in original list
				if (subStatus.selected) {
					selectedCount++;
					if (getSubStatus(subStatus.subStatusCd, originalSelectedStatus.serviceSubStatusList) == null)
						return true;
				}
			}

			return selectedCount != originalSelectedStatus.serviceSubStatusList.length;
        }
		
		private function getSubStatus(subStatusCd:String, subStatusList:ArrayCollection):ServiceSubStatus {
			for each (var subStatus:ServiceSubStatus in subStatusList) {
				if (subStatus.subStatusCd == subStatusCd)
					return subStatus;
			}
			
			return null;
		}

		override protected function executeSave():void {
			var selectedSubStatuses:ArrayCollection = null;
            if(hasServiceStatusChanged){
                selectedSubStatuses = new ArrayCollection();
                for each (var subStatus:ServiceSubStatus in selectedServiceStatus.serviceSubStatusList) {
                    if (subStatus.selected)
                        selectedSubStatuses.addItem(subStatus);
                }
            }

            var ddLimits:CompanyDdLimits = null;
            if(hasDdLimitsChanged) {
                ddLimits = new CompanyDdLimits();
                ddLimits.perEmployeeLimit = perEmployeeLimitValue;
                ddLimits.perPayrollLimit = perPayrollLimitValue;
            }

			SAP.instance.companyService.saveCompanyService(
					companyKey.sourceSystemCd,
                    companyKey.companyId,
                    selectedCompanyServiceStatus.serviceCd,
					selectedSubStatuses,
                    ddLimits,

                    null,
					createSaveResponder(onSaveSucceeded));
		}
		
		protected function onSaveSucceeded(e:ResultEvent):void {
			dispatchCompanySavedEvent();
		}

		public function removeFraudFlag():void {
			// show progress bar
			SAP.instance.showProgress("Removing Fraud Flag...");
			// reset error string
			saveMsg = "";
			SAP.instance.companyService.removeFraudFlag(companyKey.sourceSystemCd,
														  companyKey.companyId,
														  createSaveResponder(onFraudFlagRemoved));
		}

		protected function onFraudFlagRemoved(e:ResultEvent=null):void {
			dispatchEvent(new Event("FraudFlagRemoved"));

			dispatchCompanySavedEvent();

			// it will be updated during refresh, but just to make the UI look responsive..
			fraudFlag = false;
			refresh();
		}
		
		protected function dispatchCompanySavedEvent():void {
			SAP.instance.dispatchEvent(EntityChangeEvent.createEvent(
											EntityChangeEvent.ENTITY_SAVED, 
											EntityChangeEvent.COMPANY, 
											companyKey.toString()));			
		}

        public function isEmpty(str:String):Boolean {
			return (str == null || str.replace(" ", "").length == 0);
		}

        [Bindable(event="backingPropertyChanged")]
        public function get cloudService():CompanyServiceStatus {
            return getService(ServiceCodeEnum.CLOUD);
        }

        [Bindable(event="backingPropertyChanged")]
        public function get cloudV2Service():CompanyServiceStatus {
            return getService(ServiceCodeEnum.CLOUD_V2);
        }

        private function getService(service:ServiceCodeEnum):CompanyServiceStatus {
            for each (var serviceStatus:CompanyServiceStatus in companyStatus.serviceStatusCollection) {
                if (serviceStatus.serviceCodeEnum == service) {
                    return serviceStatus;
                }
            }
            return null;
        }
	}
}

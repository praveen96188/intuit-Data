package psp.sap.viewmodel
{
    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;
    import mx.validators.DateValidator;

    import psp.sap.application.CompanyEventHandler;
    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.CompanyEventGroup;
    import psp.sap.model.CompanyEventGroupItem;
    import psp.sap.model.CompanyEventItem;
    import psp.sap.model.CompanyEventQueryReturn;
    import psp.sap.model.User;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.viewmodel.events.ViewModelEvent;
    import mx.collections.HierarchicalData;
    import mx.controls.advancedDataGridClasses.AdvancedDataGridColumn;

    public class CompanyEventLogViewModel extends CompositePartViewModel
	{

		[Bindable] [BackingProperty(context=true, required=false, linkable=false)] public var startupEventId:String;
		
		//components
		private var addNotePup:PopUpPartViewModel;
		private var addNoteViewModel:CompanyEventLogAddNoteViewModel;
				
		//data
        private var creatorReturn:ArrayCollection = null;
        private var eventGroupReturn:ArrayCollection = null;
		private var companyEventQueryReturn:CompanyEventQueryReturn;		
		private var companyEvents:ArrayCollection = new ArrayCollection();
		
		//backing properties
		//Only these two have validators and there is no hasChanged logic, so they are the only ones with the meta
		[Bindable] [BackingProperty] public var startDate:String;
		[Bindable] [BackingProperty] public var endDate:String;

        [ArrayElementType("psp.sap.model.User")]
		[Bindable] public var creators:ArrayCollection = new ArrayCollection();
		[Bindable] public var eventGroups:ArrayCollection = new ArrayCollection();
		private var mCreator:User = null;
		[Bindable] public var companyEventsHierarchicalData:HierarchicalData = new HierarchicalData(new ArrayCollection());		
		
		//validators
		[Bindable] public var startDateValidator:DateValidator;
		[Bindable] public var endDateValidator:DateValidator;
		[Bindable] public var dateRangeValidator:SAPStartEndDateValidator;		
				
		//transients
		//These are used because validation cannot block us from F9 reloading the page with what was there.
		private var lastSearchedStartDate:String;
		private var lastSearchedEndDate:String;

        //preferences
        [Bindable] [BackingProperty] public var typeColumnVisible:Boolean;
        [Bindable] [BackingProperty] public var descriptionColumnVisible:Boolean;
        [Bindable] [BackingProperty] public var creatorColumnVisible:Boolean;
        [Bindable] [BackingProperty] public var notesColumnVisible:Boolean;
        [Bindable] [BackingProperty] public var includeAS400Events:Boolean;

		
		public function CompanyEventLogViewModel()
		{	
			super();
			
			// init subparts			
			addNotePup = addPopUpPart(CompanyInspectorPageEnum.EVENT_LOG_ADD_NOTE);			
			addNoteViewModel = addNotePup.addNewPart(CompanyEventLogAddNoteViewModel, CompanyInspectorPageEnum.EVENT_LOG_ADD_NOTE) as CompanyEventLogAddNoteViewModel;			
			addNotePup.closeOnSave = true;
			addNoteViewModel.addEventListener(ViewModelEvent.CLOSE,onAddNoteClosed,false,0,true);
			addNoteViewModel.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, showNoteSaved, false, 0, true);

								
			initDateDefaults();

			initValidators();
			
		}

        public function set creator(selection:Object):void{
            //the user is bound if there is one, or else it binds text--this will set the text inputs to null
            mCreator = selection as User;
        }

        public function get creator():Object{
            return mCreator;
        }

        public static function createActivator(startupEventId:String):Object {
            return {"startupEventId":startupEventId};
        }

		
		private function initDateDefaults():void {
			var today:Date = SAP.instance.PSPDate;				
			var oneYearAgo:Date = SAP.instance.PSPDate;
			oneYearAgo.setFullYear(	today.fullYear - 1, 
									today.month, 
									today.month == 1 && today.date == 29 ? 28 : today.date);	
																	
			startDate = SAPDateFormatters.dateFormatShort.format(oneYearAgo);
			endDate = SAPDateFormatters.dateFormatShort.format(today);
										
			lastSearchedStartDate = startDate;
			lastSearchedEndDate = endDate;						
		}
		
		private function initValidators():void {
			startDateValidator = new DateValidator();
			startDateValidator.source = this;
			startDateValidator.property = "startDate";
			startDateValidator.required = false;
			startDateValidator.trigger = this;
			validators.push(startDateValidator);

			endDateValidator = new DateValidator();
			endDateValidator.source = this;
			endDateValidator.property = "endDate";
			endDateValidator.required = false;
			endDateValidator.trigger = this;
			validators.push(endDateValidator);
							 
			dateRangeValidator = new SAPStartEndDateValidator();
			dateRangeValidator.source = this;
			dateRangeValidator.trigger = this;
			dateRangeValidator.startDateProperty = "startDate";
			dateRangeValidator.endDateProperty = "endDate";
			dateRangeValidator.required = false;
			validators.push(dateRangeValidator);
		}

        override protected function onActivating():void{
            setFromPreferences();
        }

		override protected function onActivated():void {
			dispatchEvent(new Event("expandStartup",false,false));
		}		
	
		override protected function loadModelData():void {
            if (creatorReturn == null) {
                loadCount++;
                SAP.instance.companyService.findCompanyEventCreators(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onCompanyEventCreatorsLoaded));
            }
            if (eventGroupReturn == null) {
                loadCount++;
                SAP.instance.companyService.findCompanyEventGroups(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onCompanyEventGroupsLoaded));
            }

			//this will be a no-op on a manual search, but will happen on an F9
			startDate = lastSearchedStartDate;
			endDate = lastSearchedEndDate;
									
			SAP.instance.companyService.findCompanyEvents( 
											company.sourceSystemCd,
                                            company.companyId,
											parseDate(startDate),
											parseDate(endDate),
											(creator != null && creator.corpId != "") ? creator.corpId : null,
											selectedEventGroupItems,
                                            includeAS400Events,
											createLoadModelDataResponder(onCompanyEventsLoaded));

		}	

		public function search():void {			
			lastSearchedStartDate = startDate;
			lastSearchedEndDate = endDate;

			refresh();
		}

        private function onCompanyEventCreatorsLoaded(e:ResultEvent):void {
            creatorReturn = ArrayCollection(e.result);
            creators = creatorReturn;
            creator = null;
        }

        private function onCompanyEventGroupsLoaded(e:ResultEvent):void {
            eventGroupReturn = ArrayCollection(e.result);

            var sort:Sort = new Sort();
            sort.fields = [new SortField("name",true,false,false)];
            eventGroupReturn.sort = sort;
            eventGroupReturn.refresh();

            eventGroups = eventGroupReturn;
        }
		
		private function onCompanyEventsLoaded(e:ResultEvent):void {
			companyEventQueryReturn = e.result as CompanyEventQueryReturn;
		}

		
		override protected function initializeBackingProperties():void {
            companyEvents = companyEventQueryReturn.events;

            var sortDate:Sort = new Sort();
            var sortDateField:SortField = new SortField("eventDate",false,true);
            sortDate.fields = [sortDateField];
            companyEvents.sort = sortDate;
            companyEvents.refresh();

            var eventHandler:CompanyEventHandler = new CompanyEventHandler(inspector as CompanyInspectorViewModel);
            for each (var eventItem:CompanyEventItem in companyEvents) {
                eventItem.eventHandler = eventHandler;
            }

            companyEventsHierarchicalData = new HierarchicalData(companyEvents);

            if (companyEventQueryReturn.moreEventsExistForQuery) {
                saveMsg = "Results limited to last 500 events. Please update your search criteria.";
                saveFaulted = true;
            }
		}



		public function get eventsShowing():Boolean {
			return companyEvents.length > 0;
		}						
		
		//We don't care if it has changed.
		override public function get hasChanged():Boolean {
			//this event just for preferences and may not be true, but won't hurt
            this.dispatchEvent(new Event("preferencesChanged"));
            return true;
		}

		/**
		 * Return date representation or null
		 */
		private function parseDate(dateString:String):Date {
			if(dateString == ""){
				return null;
			}
			var formattedDate:String = SAPDateFormatters.dateFormatShort.format(dateString);
			var txDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			txDate.setTime(time);
			return txDate;			
		}
				
		/**
		 * Return a list of event type codes that are selected
		 * or null if no events types are selected
		 */
		private function get selectedEventGroupItems():ArrayCollection {
			var selectedEventGroups:ArrayCollection = new ArrayCollection();
			
			if(companyEventsHierarchicalData == null) return null;
			
			for each(var dataEventGroup:CompanyEventGroup in this.eventGroups) {
				for(var i:int = 0; i < dataEventGroup.children.length; i++)
				{
					var companyEventGroupItem:CompanyEventGroupItem = dataEventGroup.children.getItemAt(i) as CompanyEventGroupItem;
					if(companyEventGroupItem.checked) {
						selectedEventGroups.addItem(companyEventGroupItem.eventTypeCd);
					}
				}
			}

			if(selectedEventGroups.length == 0) return null;
			return selectedEventGroups;
		}

		

		
		public function getFilterString():String {
			var newFilterString:String;

			if (startDate != "" && endDate != "") {
				newFilterString = "From " + startDate + " to " + endDate;
			} else if (startDate != "") {
				newFilterString = "After " + startDate;
			} else if (endDate != "") {
				newFilterString = "Before " + endDate;
			} else {
				newFilterString = "All dates";
			}
			
			if (creator != null && creator.corpId != "") {
				newFilterString = newFilterString + "; by " + creator.fullName;
			}
			var eventGroupItems:ArrayCollection = selectedEventGroupItems;
			if (eventGroupItems != null && eventGroupItems.length > 0) {
				newFilterString = newFilterString + "; with type ";
				for each(var item:String in eventGroupItems){
					newFilterString = newFilterString + item + ", ";
				}
				newFilterString = newFilterString.substring(0,newFilterString.length-2);					
			}
			return newFilterString;		
		}
		
		//notes
		//Invoke the add note screen (pass in null if it doesn't have a parent event)
		public function addNewNoteToEvent(event:CompanyEventItem):void {
			addNoteViewModel.selectedEvent = event;			
			addNotePup.displayPopUp();
		}						
		
		private function onAddNoteClosed(e:ViewModelEvent):void{
			this.saveMsg = "";
			this.saveFaulted = false;
		}
						
		//This is called as a result of a save on the add note screen to pass along the success message
		public function showNoteSaved(e:Event):void {			
			saveFaulted = false;			
			saveMsg = SAP.instance.configuration.defaultSaveSucceededMsg;
			refresh(false);
		}
		
		public function creatorFilterFunction(item:Object, text:String):Boolean {
			var testText:String = (text != null) ? text.toLowerCase() : null;
		    var regExp:RegExp = new RegExp(testText,"");
            var lookupReturn:String = null;
            if(item.fullName != null){
                lookupReturn = String(item.fullName);
            } else{
                lookupReturn = String(item.cordId);
            }
		    lookupReturn = (lookupReturn != null) ? lookupReturn.toLowerCase() : null;
		    return regExp.test(lookupReturn);
		}

		public function eventGroupLabel(value:Object):String {
			if (value is CompanyEventGroupItem) {
				return (value as CompanyEventGroupItem).eventTypeName;
			}
			
			var groupName:String = (value as CompanyEventGroup).name;
			
			//Camel case to spaces				
			if(groupName != null && groupName.indexOf(" ") == -1)
			{
				var newGroupName:String = "";
				for(var i:int = 0; i < groupName.length; i++)
				{
					if(groupName.substr(i, 1).toUpperCase() == groupName.substr(i, 1) && i+1 != (groupName.length) && groupName.substr(i+1, 1).toUpperCase() != groupName.substr(i+1, 1))
					{
						newGroupName += " " + groupName.substr(i, 1);
					} else {
						newGroupName += groupName.substr(i, 1);
					}
				}
				return newGroupName;
			} else {
				return groupName;
			}
		}
		
		public function formatDate(item:Object, dgc:AdvancedDataGridColumn):String{
			if (item is String) {
				return (item as String);
			} else if (item is CompanyEventItem) {
				return SAPDateFormatters.dateTimeFormatShort.format((item as CompanyEventItem).eventDate);
			} else {
				//when does this happen?
				return "";
			}
			
		}

        //preferences
        private static const columnProperties:Array = ["typeColumnVisible", "descriptionColumnVisible", "creatorColumnVisible", "notesColumnVisible"];

        [Bindable(event="preferencesChanged")]
        public function get preferencesChanged():Boolean {
            return columnBits != SAP.instance.session.user.getPreference(SettingsEnum.EVENT_LOG_COLUMNS);
        }

        [Bindable(event="preferencesChanged")]
        public function get as400PreferencesChanged():Boolean {
            return includeAS400Events != SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_AS400_EVENTS);
        }

        public function get columnBits():String {
            var bits:String = "";
            for (var i:int=0; i<columnProperties.length; i++) {
                bits += this[columnProperties[i]] ? "1" : "0";
            }
            return bits;
        }

        private function setFromPreferences():void {
            var bits:String = SAP.instance.session.user.getPreference(SettingsEnum.EVENT_LOG_COLUMNS);
            if (bits != null) {
                for (var i:int=0; i<columnProperties.length; i++) {
                    this[columnProperties[i]] = bits.charAt(i) == "1";
                }
            }

            includeAS400Events = SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_AS400_EVENTS);
        }

        public function savePreferences():void {
            SAP.instance.session.user.setPreference(SettingsEnum.EVENT_LOG_COLUMNS, columnBits);
            dispatchEvent(new Event("preferencesChanged"));
        }

        public function saveAS400Preferences():void {
            SAP.instance.session.user.setPreferenceBoolean(SettingsEnum.INCLUDE_AS400_EVENTS, includeAS400Events);
            dispatchEvent(new Event("preferencesChanged"));
        }



	}
}

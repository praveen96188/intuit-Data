package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.events.PropertyChangeEvent;
    import mx.formatters.DateFormatter;
    import mx.rpc.events.ResultEvent;
    import mx.validators.StringValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.CompanyStrike;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class CompanyManageStrikesViewModel
	extends AbstractPartViewModel
	{
		public static const MAX_STRIKE_REASON:Number = 200;
		private static const DEFAULT_STRIKE_REASON:String = "";
		private var DEFAULT_STRIKE_DATE:String;

		private var mStrikeDate:String = null;
		private var mStrikeReason:String;
		
		private var mCanAddStrikes:Boolean = false;
		[Bindable] public var canCancelStrikes:Boolean = false;
		
		private var dateFormatterForInput:DateFormatter = new DateFormatter();

		[Bindable]
		public var strikeDateValidator:SAPDateValidator;
		
		[Bindable]
		public var strikeDateRequiredValidator:Validator;
		
		[Bindable]
		public var strikeReasonValidator:StringValidator;
		
		[Bindable]
		public var strikeReasonRequiredValidator:Validator;
		
		[Bindable]
		public var charactersRemaining:Number = MAX_STRIKE_REASON;
		
		[Bindable]
		public var strikes:ArrayCollection = new ArrayCollection();
	
		
		public function CompanyManageStrikesViewModel()
		{
			this.label = CompanyInspectorPageEnum.STRIKES;	
			this.reloadOnSave = true;
			
			// strike sorting
			var sort:Sort = new Sort();
		    sort.fields = [new SortField("strikeDate", false, true)];
		    strikes.sort = sort;					
			
			dateFormatterForInput.formatString = "MM/DD/YYYY";
					
			strikeDateValidator = SAPValidators.createDateValidator(this, "strikeDate", false, 365, 0, SAP.instance.PSPDate);
			validators.push(strikeDateValidator);
			strikeDateRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "strikeDate", true);
			validators.push(strikeDateRequiredValidator);
			
			strikeReasonValidator = SAPValidators.createStringValidator(this, "strikeReason", false, 0, MAX_STRIKE_REASON);
			validators.push(strikeReasonValidator);
			strikeReasonRequiredValidator = SAPValidators.createRequiredFieldValidator(this, "strikeReason", true);
			validators.push(strikeReasonRequiredValidator);
		}		
		
	
		[Bindable]
		public function get strikeReason():String {
			return mStrikeReason;
		}
		
		public function set strikeReason(value:String):void {
			mStrikeReason = value;
			charactersRemaining = MAX_STRIKE_REASON - mStrikeReason.length;
			updateCanSave();
		} 

		[Bindable]
		public function get strikeDate():String {
			return mStrikeDate;
		}
		
		public function set strikeDate(value:String):void {
			mStrikeDate = dateFormatterForInput.format(value);
			updateCanSave();			
		}
						
		[Bindable]
		public function get canAddStrikes():Boolean {
			return mCanAddStrikes;
		}

        public function set canAddStrikes(value:Boolean):void {
			mCanAddStrikes = value;
			updateCanSave();
		}

		override public function get hasChanged():Boolean {
			return strikeReason != DEFAULT_STRIKE_REASON;
		}
		
		override protected function evaluateCanSave():Boolean {
			return super.evaluateCanSave() && mCanAddStrikes;
		}
		
		override protected function onActivated():void {
			mCanAddStrikes = SAP.canPerformOperation(OperationsEnum.STRIKE_ADD);
			canCancelStrikes = SAP.canPerformOperation(OperationsEnum.STRIKE_CANCEL);
		}

        override protected function loadModelData():void {
            SAP.instance.companyService.getStrikeInfo(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onStrikeInfoLoaded));
        }

        private function onStrikeInfoLoaded(e:ResultEvent):void {
            strikes.removeAll();
            for each (var strike:CompanyStrike in ArrayCollection(e.result)) {
                strikes.addItem(strike);
            }
            strikes.refresh();
            dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "twelveMonthStrikeCount", null, null));
        }

        [Bindable("propertyChange")]
        public function get twelveMonthStrikeCount():Number {
            return getTwelveMonthStrikeCount(strikes);
        }

        public static function getTwelveMonthStrikeCount(strikes:ArrayCollection):Number {
            var lastYear:Date = SAP.instance.PSPDate;
            var count:Number = 0;

            var newTime:Number = lastYear.setFullYear(lastYear.getFullYear() - 1);
            lastYear.setTime(newTime);

            for each(var strikeItem:CompanyStrike in strikes) {
                if (strikeItem.strikeDate >= lastYear && !strikeItem.cancelled) {
                    count++;
                }
            }

            return count;
        }

        override protected function initializeDefaults():void {
			var today:Date = SAP.instance.PSPDate;
			DEFAULT_STRIKE_DATE = dateFormatterForInput.format(today);
		}

		override protected function initializeBackingProperties():void {
			strikeReason = DEFAULT_STRIKE_REASON;
			strikeDate = DEFAULT_STRIKE_DATE;
		}

		public function saveNewStrike():void {
            SAP.instance.companyService.addCompanyStrike(
                    company.sourceSystemCd,
                    company.companyId,
                    new Date(Date.parse(this.strikeDate)),
                    this.strikeReason,
                    createSaveResponder());

		}


		public function cancelStrike(c:CompanyStrike):void {
            SAP.instance.companyService.cancelCompanyStrike(
                    company.sourceSystemCd,
                    company.companyId,
                    c.spcfUniqueId,
                    createSaveResponder());
		}

		public function changePage(newPageEnum:String):void {
			inspector.getPage(newPageEnum).activate();
		}
		 
	}
}
package psp.sap.viewmodel
{
    import flash.events.EventDispatcher;

    import mx.events.PropertyChangeEvent;
    import mx.validators.DateValidator;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.DateRangeEnum;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;

    public class DateSelectionViewModel extends EventDispatcher
	{
		private var mDateRange:DateRangeEnum;		
		private var mStartDate:String;
		private var mEndDate:String;
		private var mInspectorPage:AbstractPartViewModel;
		private var mIsUpdatingDateRange:Boolean = false;
		
		[Bindable] public var dateRanges:Array;
		[Bindable] public var defaultDateRange:DateRangeEnum; 
		[Bindable] public var startAndEndValidator:SAPStartEndDateValidator;
		[Bindable] public var startDateValidator:DateValidator;
		[Bindable] public var endDateValidator:DateValidator;		


		public function DateSelectionViewModel(inspectorPage:AbstractPartViewModel)
		{
			this.inspectorPage = inspectorPage;

			dateRanges = DateRangeEnum.list;			

			startDateValidator = SAPValidators.createDefaultDateValidator(this, "startDate", false);
			startDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
			startDateValidator.trigger = this;
			mInspectorPage.validators.push(startDateValidator);

			endDateValidator = SAPValidators.createDefaultDateValidator(this, "endDate", false);
			endDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
			endDateValidator.trigger = this;
			mInspectorPage.validators.push(endDateValidator);
			
			startAndEndValidator = new SAPStartEndDateValidator();
			startAndEndValidator.source = this;
			startAndEndValidator.trigger = this;
			startAndEndValidator.startDateProperty = "startDate";
			startAndEndValidator.endDateProperty = "endDate";
			startAndEndValidator.required = false;
			mInspectorPage.validators.push(startAndEndValidator);			
		}

		// getters and setters		
		[Bindable]
		public function get dateRange():DateRangeEnum {
			return mDateRange;
		}

		public function set dateRange(value:DateRangeEnum):void {
			if(value == null){
				value = defaultDateRange;
			}
			if(value != null){
				mDateRange = value;
				// update date range
				if(!mDateRange.isCustom){
					// keep from going into an infinte loop
					mIsUpdatingDateRange = true;
					startDate = mDateRange.startDate;
					endDate = mDateRange.endDate;
					mIsUpdatingDateRange = false;
				}
				// call validators
				mInspectorPage.updateCanSave();
			}
		}		

		[Bindable]
		public function get startDate():String {
			return mStartDate;
		}

		public function set startDate(value:String):void {
			mStartDate = value;
			// set date range
			if(!mIsUpdatingDateRange){
				dateRange = DateRangeEnum.findEnumFromDateRange(mStartDate, mEndDate);
			}
			mInspectorPage.updateCanSave();
		}

		public function get startDateValue():Date {
			if(mStartDate == null || mStartDate == ""){
				return null;
			}
			var formattedDate:String = SAPDateFormatters.dateFormatShort.format(mStartDate);
			var txDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			txDate.setTime(time);
			return txDate;
		}

		[Bindable]
		public function get endDate():String {
			return mEndDate;
		}

		public function set endDate(value:String):void {
			mEndDate = value;
			// set date range
			if(!mIsUpdatingDateRange){
				dateRange = DateRangeEnum.findEnumFromDateRange(mStartDate, mEndDate);
			}
			mInspectorPage.updateCanSave();
		}

		public function get endDateValue():Date {
			if(mEndDate == null|| mEndDate == ""){
				return null;
			}
			var formattedDate:String = SAPDateFormatters.dateFormatShort.format(mEndDate);
			var txDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			txDate.setTime(time);
			return txDate;
		}
		
		public function set inspectorPage(value:AbstractPartViewModel):void {
			mInspectorPage = value;
		}

        public function get inspectorPage():AbstractPartViewModel {
            return mInspectorPage;
        }

	}
}

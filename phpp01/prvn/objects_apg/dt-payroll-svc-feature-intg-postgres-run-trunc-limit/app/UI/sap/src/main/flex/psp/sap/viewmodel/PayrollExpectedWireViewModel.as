package psp.sap.viewmodel
{
	import mx.events.PropertyChangeEvent;
	import mx.formatters.DateFormatter;
	import mx.rpc.Responder;
	import mx.validators.Validator;
	
	import psp.sap.application.SAP;
	import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.CollectionStageEnum;
	import psp.sap.model.PayrollRun;
	import psp.sap.validators.SAPDateValidator;
	import psp.sap.validators.SAPValidators;
				
	public class PayrollExpectedWireViewModel
	extends AbstractPartViewModel
	{

        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;
        [Bindable] [BackingProperty (context=true)] public var action:ActionEvent;

		private var DEFAULT_TX_DATE:Date;
		private static const millisecondsPerDay:int = 1000 * 60 * 60 * 24;
        private const DAYSTOADD:int = 5;
        private const SATURDAY:String = "6";
        private const SUNDAY:String = "0";
        
		private var mDateValidator:SAPDateValidator;	
		private var mExpectedDateRequiredValidator:Validator;
		private var mDateFormatter:DateFormatter = new DateFormatter();
		private var mExpectedDate:String;
		private var mCollectionTypes:Array;
		private var mCollectionType:CollectionStageEnum;
		private var mCollectionIndex:int;
		private var mSendLastEmail:Boolean;
		
		public function PayrollExpectedWireViewModel()
		{
			mDateFormatter.formatString = SAP.instance.configuration.dateFormatShort;
			
			mDateValidator = SAPValidators.createDateValidator(this, "expectedDate", true, 0, 365);
			mDateValidator.daysBeforeAllowedErrorMessage = "Expected date must not be in the past";
			mDateValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
			mDateValidator.trigger = this;
			validators.push(mDateValidator);			
			
			mExpectedDateRequiredValidator = new Validator();
			mExpectedDateRequiredValidator.source = this;
			mExpectedDateRequiredValidator.property = "expectedDate";
			mExpectedDateRequiredValidator.required = true;
			validators.push(mExpectedDateRequiredValidator);	
			
			mCollectionTypes = CollectionStageEnum.values;							
		}

        public static function createActivator(payrollRun:PayrollRun, action:ActionEvent):Object {
            return {"payrollRun":payrollRun,"action":action};
        }

		
		[Bindable]
		public function get sendLastEmail():Boolean {
			return mSendLastEmail
		}	        
	        
	    public function set sendLastEmail(value:Boolean):void {
	    	mSendLastEmail = value;
	    }    				       
		
		[Bindable]
		public function get expectedDate():String {
			return mExpectedDate;
		}
		
		public function set expectedDate(value:String):void {
			mExpectedDate = value;
			mDateValidator.validate();
			updateCanSave();
		}		
		
		public function get expectedDateValue():Date {
			var formattedDate:String = mDateFormatter.format(mExpectedDate);
			var txDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			txDate.setTime(time);
			return txDate;
		}		
		
		public function get expectedDateRequiredValidator():Validator {
			return mExpectedDateRequiredValidator;
		}

		public function get expectedDateValidator():SAPDateValidator {
			return mDateValidator;
		}		
			
		[Bindable]
		public function get collectionTypes():Array {
			return mCollectionTypes;
		}

		public function set collectionTypes(value:Array):void {
			mCollectionTypes = value;
		}			
			
		[Bindable]
		public function get coCollType():CollectionStageEnum {
			return mCollectionType;
		}
		
		public function set coCollType(value:CollectionStageEnum):void {
			if(value == null){
				value = getDefaultCollectionStage(); 
			}
			mCollectionType = value;			
			updateCanSave();
		}
		
		public function getDefaultCollectionStage():CollectionStageEnum {
			if (payrollRun.collectionStage != null) {
           	   if (payrollRun.collectionStage == CollectionStageEnum.FIRST.code) {
           	      return CollectionStageEnum.FIRST;
           	   }
           	   if (payrollRun.collectionStage == CollectionStageEnum.SECOND.code) {
           	   	  return CollectionStageEnum.SECOND;
           	   }
           	   if (payrollRun.collectionStage == CollectionStageEnum.TERM.code) {
           	   	  return CollectionStageEnum.TERM;
           	   }
           	}          
           	return CollectionStageEnum.FIRST;
		}    			
		
		public function getFutureBankingDate():Date {
			var tempDate:Date = SAP.instance.PSPDate;
			tempDate.setHours(0, 0, 0, 0);
			return addBusinessDays(tempDate, DAYSTOADD);
		}
			
        private function addBusinessDays(value:Date, numDays:int):Date {
        	for (var i:int = 0; i < numDays; i++) {
        		value.setTime(value.getTime() + millisecondsPerDay);
        		if (value.day.toString() == SATURDAY || value.day.toString() == SUNDAY) {  
        		   i--;
        		}
        	}
        	return value;        	
        }			
				
		public function get wireDate():String {
			return mDateFormatter.format(payrollRun.wireExpectedDate);
		}		
		
        override protected function loadModelData():void {
        	initializeBackingProperties();	        
			if (payrollRun.wireExpectedDate != null) {
				expectedDate = wireDate;
			}
			else{
				// one day in the future
				var date:Date = SAP.instance.PSPDate;
				date.setTime(date.getTime() + millisecondsPerDay);
				expectedDate = mDateFormatter.format(date);
			}
   			coCollType = getDefaultCollectionStage();
   			modelDataLoaded();	       
        }
        
        override protected function initializeBackingProperties():void {
        	sendLastEmail = false;
        }						
		
		override protected function evaluateCanSave():Boolean {
			return (company != null) && super.evaluateCanSave(); 
		} 
		
		override public function get hasChanged():Boolean {
			return true;
     	}
	
		override protected function executeSave():void {
			
			var strCollectionCode:String = coCollType.code;
			var dateExpectedDate:Date = expectedDateValue;
			var boolLastEmail:Boolean = sendLastEmail;   		
			
			SAP.instance.payrollRunService.addWireExpectedDateTransaction(
										company.sourceSystemCd, 
										company.companyId,
										payrollRun.sourcePayRunId,
										strCollectionCode,
										action.code.code,
										dateExpectedDate,
										sendLastEmail,
										createSaveResponder());
		}		
		
		public function goToAddRedebit():void {
          	topic.findPage(CompanyInspectorPageEnum.PAYROLL_ADD_REDEBIT).activatePage(PayrollMultiItemEnteringPageViewModel.createActivator(payrollRun, action));
       	}		
	}
}
package psp.sap.viewmodel
{
import mx.formatters.NumberFormatter;
import mx.rpc.events.ResultEvent;
import mx.validators.NumberValidator;

import psp.sap.application.SAP;
import psp.sap.formatters.SAPDateFormatters;
import psp.sap.model.FeeDetail;
import psp.sap.model.PayrollRun;
import psp.sap.model.PayrollTransaction;
import psp.sap.validators.SAPValidators;

public class PayrollRefundRebillViewModel
		extends AbstractPartViewModel
	{

        [Bindable] [BackingProperty(context=true)] public var payrollTransaction:PayrollTransaction;
        [Bindable] [BackingProperty (context=true)] public var payrollRun:PayrollRun;


		// defaults
		private static const DEFAULT_IS_REBILL:Boolean = false;
		
		// members
		private var mIsRebill:Boolean = false;
		private var mFeeDetail:FeeDetail;
		private var mRebillUnitPrice:String;
        private var mRebillUnitQuantity:String;
		private var mRebillTotalPrice:Number;
		private var mRefundTotalPrice:Number;
		private var mRebillRefundTotalPrice:Number;
		
		// formatter
		private var mNumberFormatter:NumberFormatter = new NumberFormatter();
		
		// validator
		public var rebillPriceValidator:NumberValidator;
        public var rebillQuantityValidator:NumberValidator;
		
		public function PayrollRefundRebillViewModel()
        {    
        	reloadOnSave = true;

            this.shallowCopyFields = ["id", "txnDate"];
        	                    
			mNumberFormatter.useThousandsSeparator = false;
			mNumberFormatter.precision = 2;

            rebillPriceValidator = SAPValidators.createNumberValidator(this, "rebillUnitPrice", true, "0.01", SAP.instance.configuration.maxAllowedCurrencyValue, false, 2);
			validators.push(rebillPriceValidator);

            rebillQuantityValidator = SAPValidators.createNumberValidator(this, "rebillUnitQuantity", true, 1, null,  false, 0);
            validators.push(rebillQuantityValidator);
        }

        public static function createActivator(payrollTransaction:PayrollTransaction, payrollRun:PayrollRun):Object {
            return {"payrollTransaction":payrollTransaction, "payrollRun":payrollRun};
        }
        
        [Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {
            var transactionDate:String = (payrollTransaction == null || payrollTransaction.txnDate == null) ? "" : SAPDateFormatters.dateTimeFormatDateOverTime.format(payrollTransaction.txnDate);
            return "Refund/Rebill For Transaction Date: " + transactionDate;
        }
		
		[Bindable]
		public function set isRebill(value:Boolean):void {
            rebillPriceValidator.enabled = value;
            rebillQuantityValidator.enabled = value;
			mIsRebill = value;
			updateCanSave();
		}
		
		public function get isRebill():Boolean {
			return mIsRebill;
		}
		
		[Bindable]
		public function set feeDetail(value:FeeDetail):void {
			mFeeDetail = value;
		}
		
		public function get feeDetail():FeeDetail {
			return mFeeDetail;
		}
		
		[Bindable]
		public function set rebillUnitPrice(value:String):void {
			mRebillUnitPrice = value;
			// set total rebill
			rebillTotalPrice = rebillUnitPriceValue * rebillUnitQuantityValue;
			updateCanSave();
		}
		
		public function get rebillUnitPrice():String {
			return mRebillUnitPrice;
		}
		
		public function get rebillUnitPriceValue():Number {
			return parseFloat(mNumberFormatter.format( mRebillUnitPrice != "" ? mRebillUnitPrice : "0.00"));
		}

        [Bindable]
        public function set rebillUnitQuantity(value:String):void {
            mRebillUnitQuantity = value;
            rebillTotalPrice = rebillUnitPriceValue * rebillUnitQuantityValue;
            updateCanSave();
        }

        public function get rebillUnitQuantity():String {
            return mRebillUnitQuantity;
        }

        public function get rebillUnitQuantityValue():int {
            var quantity:Number = parseInt(mRebillUnitQuantity);
            if (isNaN(quantity)) {
                return 0;
            }
            return quantity;
        }

		[Bindable]
		public function set rebillTotalPrice(value:Number):void {
			mRebillTotalPrice = value;
			// update rebill/refund total
			rebillRefundTotal = -refundTotal + rebillTotalPrice;
		}
		
		public function get rebillTotalPrice():Number {
			return mRebillTotalPrice;
		}
		
		[Bindable]
		public function set refundTotal(value:Number):void {
			mRefundTotalPrice = value;
			// update rebill/refund total
			rebillRefundTotal = -refundTotal + rebillTotalPrice;
		}
		
		public function get refundTotal():Number {
			return mRefundTotalPrice;
		}
		
		[Bindable]
		public function set rebillRefundTotal(value:Number):void {
			mRebillRefundTotalPrice = value;
		}
		
		public function get rebillRefundTotal():Number {
			return mRebillRefundTotalPrice;
		}

		// override functions
		override protected function loadModelData():void {
			SAP.instance.billingService.findFeeDetail(payrollTransaction.id,
				createLoadModelDataResponder(onResults),companyKey.companyId);
			
		}
		
		override protected function initializeBackingProperties():void {
			isRebill = DEFAULT_IS_REBILL;			
		}
		
		override public function get hasChanged():Boolean {
			return true;
		}
		
		override protected function evaluateCanSave():Boolean {
			// do not allow a save of $0.00
			var temp:Boolean = true;
			if(isRebill){
				temp = rebillRefundTotal != 0.00;
			}
			else{
				temp = refundTotal != 0.00;
			}
			
			return super.evaluateCanSave() && temp;
		}
		
		
		private function onResults(e:ResultEvent):void {
			feeDetail = e.result as FeeDetail;
			if(feeDetail.currentUnitPrice != SAP.instance.configuration.specialNumberForDefault){
				rebillUnitPrice = mNumberFormatter.format(feeDetail.currentUnitPrice);
			} else{
				rebillUnitPrice = mNumberFormatter.format(feeDetail.unitPrice);
			}
            rebillUnitQuantity = feeDetail.units.toString();
			refundTotal = feeDetail.totalPrice;
		}
		
		override protected function executeSave():void {
			SAP.instance.payrollRunService.addRefundRebillTransaction(company.sourceSystemCd,
										company.companyId,
										payrollRun.sourcePayRunId,
										refundTotal,
										SAP.instance.PSPDate,
										isRebill,
										payrollTransaction.id,
										rebillUnitPriceValue,
                                        rebillUnitQuantityValue,
										createSaveResponder());
		}				
	}
}
package psp.sap.model
{
    import flash.events.EventDispatcher;

    import mx.collections.ArrayCollection;

    import mx.events.PropertyChangeEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPDateFormatters;

    [Bindable]
	[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompanyServiceStatus")]
	public class CompanyServiceStatus extends EventDispatcher
	{
		public var serviceCd:String;
		public var displayStatus:DisplayStatus;
        public var status:ServiceStatus;
		public var canUpdateStatus:Boolean;
        public var hasSignatureFile:Boolean;
        public var serviceStartDate:Date;
        public var offering:String;
        public var offeringDetails:Offering;
        public var canEditOffering:Boolean;
        public var offer:String;
        public var offerExpirationDate:Date;
        public var canEditOffer:Boolean;
        public var firstTaxQuarter:Date;
        public var isAssistedActive:Boolean;
        public var w2PrintingPreference:String;

        // service specific additional information for banner
        public var directDepositAdditionalInfo:DirectDepositServiceInformation;
        public var billPaymentAdditionalInfo:BillPaymentServiceInformation;

		[ArrayElementType("psp.sap.model.ServiceStatus")]
		public var allowedTransitions:ArrayCollection;

        // for dd and bp services
        public var ddLimits:CompanyDdLimits = null;                
        private var mFundingModelCd: String;
		private var mFundingModel:FundingModel;

        //for 401k service
        public var custodialId:String = null;
        public var isSafeHarbor:Boolean = false;

		public function get fundingModelCd():String {
			return mFundingModelCd;
		}

		public function set fundingModelCd(value:String):void {
			mFundingModelCd = value;

			var oldValue:FundingModel = mFundingModel;
			mFundingModel = lookupFundingModel();
			dispatchEvent( PropertyChangeEvent.createUpdateEvent(this, "fundingModel", oldValue, mFundingModel) );
		}

        [Transient]
		[Bindable("propertyChange")]
		public function get fundingModel():FundingModel {
			return mFundingModel;
		}

		private function lookupFundingModel():FundingModel {
			if (fundingModelCd == null)
				return null;

			return SAP.instance.lookupService.fundingModels.getItemByKey(fundingModelCd) as FundingModel;
		}

        [Transient]
        public function get isServiceOnHold():Boolean {
            if(status != null){
                return status.serviceStatusCd == "OnHold";
            }
            return false;
        }

        [Transient]
        [Bindable("propertyChange")]
		public function get serviceCodeEnum():ServiceCodeEnum {
			return ServiceCodeEnum.valueOf(serviceCd);
		}

        public function strFirstTaxQuarter():String{
            var strYear:String = "";
            var strQuarter:String = "";
            if(firstTaxQuarter != null){
                strYear = firstTaxQuarter.fullYear.toString();
                if (firstTaxQuarter.month <= 2) {
                    strQuarter= " Q1";
                } else if (firstTaxQuarter.month <= 5) {
                    strQuarter = " Q2";
                } else if (firstTaxQuarter.month <= 8) {
                    strQuarter = " Q3";
                } else {
                    strQuarter = " Q4";
                }
            }
            return (strYear + strQuarter);
        }

        public function get offerText():String {
            var text:String = offer;
            if (offerExpirationDate != null) {
                text += " (Expires " + SAPDateFormatters.dateFormatShort.format(offerExpirationDate) + ")";
            }
            return text;
        }

        public function get displayInTile():Boolean {
            return serviceCodeEnum != ServiceCodeEnum.CLOUD && serviceCodeEnum != ServiceCodeEnum.CLOUD_V2;
        }
	}
}

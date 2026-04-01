package psp.sap.viewmodel
{
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.formatters.DateFormatter;
    import mx.formatters.NumberFormatter;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.CompanyServiceState;
    import psp.sap.model.Offering;

    public class CompanyOfferingsViewModel extends AbstractPartViewModel
	{
		private var mOfferings:ArrayCollection = new ArrayCollection();
		private var mSelectedOffering:String = null;
		private var mCanChangeOffering:Boolean = false;
		private var mDateFormatter:DateFormatter = new DateFormatter();
		private var mPriceFormatter:NumberFormatter = new NumberFormatter();

        [Bindable] [BackingProperty(context=true)] public var serviceCd:String;
        [Bindable] public var currentOffering:Offering = null;
		
		public function CompanyOfferingsViewModel()
		{
			this.label =  CompanyInspectorPageEnum.COMPANY_OFFERINGS;
			this.reloadOnSave = true;
			
			mDateFormatter.formatString = SAP.instance.configuration.dateFormatMedium;
			mPriceFormatter.precision = 2;	
		}

        public static function createActivator(serviceCd:String):Object {
            return {"serviceCd":serviceCd};
        }

		[Bindable]
		public function get offerings():ArrayCollection {
			return mOfferings;
		}
		
		public function set offerings(value:ArrayCollection):void {
			mOfferings = value;		

            var sortCode:Sort = new Sort();
			var sortCodeField:SortField = new SortField("SKU",false,true,true);
			sortCode.fields = [sortCodeField]; 
			mOfferings.sort = sortCode;
			mOfferings.refresh();
		}
		
		[Bindable]
		public function get selectedOffering():String {
			return mSelectedOffering;
		}
		
		public function set selectedOffering(value:String):void {
			mSelectedOffering = value;
			
			for each (var offering:Offering in this.offerings) {
				offering.selected = false;
			}
			
			for each (offering in this.offerings) {
				if (value == offering.SKU) {
					offering.selected = true;
				}
			}
			
			canSave = evaluateCanSave();
		}
		
		[Bindable]
		public function get canChangeOffering():Boolean {
			return mCanChangeOffering;
		} 
		
		public function set canChangeOffering(value:Boolean):void {
			mCanChangeOffering = value;
			evaluateCanSave();
		}
		
		public function get dateFormatter():DateFormatter {
			return mDateFormatter;
		}

		public function get priceFormatter():NumberFormatter {
			return mPriceFormatter;
		}

		override protected function loadModelData():void {
			loadCount = 2;
            SAP.instance.billingService.getCurrentOffering(companyKey.sourceSystemCd, companyKey.companyId, serviceCd, createLoadModelDataResponder(onCurrentOfferingLoaded));

			SAP.instance.billingService.findOfferings(serviceCd, companyKey.companyId, companyKey.sourceSystemCd, createLoadModelDataResponder(onOfferingsLoaded));
		}

        public function onCurrentOfferingLoaded(e:ResultEvent):void {
            currentOffering = e.result as Offering;
        }
		
		public function onOfferingsLoaded(e:ResultEvent):void {
			offerings = e.result as ArrayCollection;
			
			for (var i:int = this.offerings.length - 1; i >= 0; i--) {
				var offering:Offering = Offering(this.offerings.getItemAt(i));
				if (offering.SKU == currentOffering.SKU) {
					this.offerings.removeItemAt(i);
				}
			}
			
			modelDataLoaded();
		}
		
		public function get offeringSelected():Boolean {
			return hasChanged;
		}
		
		override protected function evaluateCanSave():Boolean {
			return mCanChangeOffering && offeringSelected && super.evaluateCanSave();
		}
		
		override protected function onActivated():void {
            if(company.companyServiceState == CompanyServiceState.AssistedActive) {
                mCanChangeOffering = SAP.canPerformOperation(OperationsEnum.ADD_ASSISTED_OFFERING_POST_BALF) ;
            } else if (company.companyServiceState == CompanyServiceState.AssistedPending) {
                mCanChangeOffering = SAP.canPerformOperation(OperationsEnum.ADD_ASSISTED_OFFERING_PRE_BALF) ;
            } else {
                mCanChangeOffering = SAP.canPerformOperation(OperationsEnum.ADD_OFFERING) ;
            }
		}
		
		public function changeOffering():void {
			SAP.instance.billingService.addOfferingToCompany(
					selectedOffering,
					companyKey.companyId,
					companyKey.sourceSystemCd,
					createSaveResponder());
		}


		
		override public function get hasChanged():Boolean {
			return (mSelectedOffering != null) && (mSelectedOffering != currentOffering.SKU);
		}

		

	}
}

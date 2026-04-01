package psp.sap.viewmodel
{
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.events.PropertyChangeEvent;
    import mx.formatters.DateFormatter;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;
    import mx.utils.StringUtil;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.model.Offer;
    import psp.sap.model.OfferEndEventEnum;
    import psp.sap.validators.SAPDateValidator;
    import psp.sap.validators.SAPValidators;

    public class CompanyOffersViewModel extends AbstractPartViewModel
	{
		private var mSelectedOffer:Offer = null;
		private var mDateFormatter:DateFormatter = new DateFormatter();
		private var mShowClaimedExpirationDate:Boolean = false;
		private var mCanUpdateValidators:Array = [];
        private var mSortByCode:Sort;

        [Bindable] [BackingProperty(context=true)] public var serviceCd:String;
		[Bindable] public var expirationDateRangeValidator:SAPDateValidator;
		[Bindable] public var claimedExpirationDateRangeValidator:SAPDateValidator;
		[Bindable] public var canUpdate:Boolean = false;
        [Bindable] public var hasCurrentOffer:Boolean = false;
        [Bindable] [ArrayElementType ("psp.sap.model.Offer")] public var offers:ArrayCollection = new ArrayCollection();

        [Bindable] [BackingProperty] public var claimedExpirationDate:String;
        [Bindable] [BackingProperty] public var expirationDate:String;
        [Bindable] [BackingProperty] public var showExpirationDate:Boolean = false;
        [Bindable] [BackingProperty] public var showClaimedExpirationDate:Boolean = true;
        [Bindable] [BackingProperty] [ArrayElementType ("psp.sap.model.Offer")] public var companyOffers:ArrayCollection = null;
		
		public function CompanyOffersViewModel()
		{
			this.label = CompanyInspectorPageEnum.COMPANY_OFFERS;
			this.reloadOnSave = true;

            mSortByCode = new Sort();
			mSortByCode.fields = [new SortField("offerCd",false,true,true)];
			
			mDateFormatter.formatString = SAP.instance.configuration.dateFormatMedium;
			expirationDateRangeValidator = SAPValidators.createDateValidator(this, "expirationDate", true, 0, 365);
            expirationDateRangeValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            expirationDateRangeValidator.trigger = this;
            // enable/disable is set in property set of showExpirationData
            expirationDateRangeValidator.enabled = showExpirationDate;
            BindingUtils.bindProperty(expirationDateRangeValidator, "enabled", this, "showExpirationDate");
            validators.push(expirationDateRangeValidator);		                        
            
			mDateFormatter.formatString = SAP.instance.configuration.dateFormatMedium;
			claimedExpirationDateRangeValidator = SAPValidators.createDateValidator(this, "claimedExpirationDate", true, 0, 365);
            claimedExpirationDateRangeValidator.triggerEvent = PropertyChangeEvent.PROPERTY_CHANGE;
            claimedExpirationDateRangeValidator.trigger = this;
            claimedExpirationDateRangeValidator.enabled = showClaimedExpirationDate;
            BindingUtils.bindProperty(expirationDateRangeValidator, "enabled", this, "showClaimedExpirationDate");
            mCanUpdateValidators.push(claimedExpirationDateRangeValidator);
		}

        public static function createActivator(serviceCd:String):Object {
            return {"serviceCd":serviceCd};
        }

		
		private function updateCanUpdate():void {
			canUpdate = SAPValidators.validateAll(mCanUpdateValidators, false).length == 0;
		}
		
		[Bindable]
		public function get selectedOffer():Offer {
			return mSelectedOffer;
		}
		
		public function set selectedOffer(value:Offer):void {
			mSelectedOffer = value;			
			if (mSelectedOffer != null && OfferEndEventEnum.DATEEVENT.toString() == mSelectedOffer.offerEndEvent) {
            	expirationDate = "";
            	showExpirationDate = true;		            	
            } else {		            	  
                expirationDate = "";
                showExpirationDate = false;
            }
					
			updateCanSave();
		}

        protected function get expirationDateValue():Date {
            return getDateValue(expirationDate);
        }    
        
        protected function get claimedExpirationDateValue():Date {
            return getDateValue(claimedExpirationDate);
        }
        
       	protected function getDateValue(value:String):Date {
            if (value != null && StringUtil.trim(value).length > 0) {
                var formattedDate:String = mDateFormatter.format(value);
                var txDate:Date = SAP.instance.PSPDate;
                var time:Number = Date.parse(formattedDate);
                txDate.setTime(time);
                return txDate;
            }
            else {
                return null;
            }
        }
		
		public function get dateFormatter():DateFormatter {
			return mDateFormatter;
		}

		override protected function loadModelData():void {
            SAP.instance.companyService.getCompanyOffers(companyKey.sourceSystemCd, companyKey.companyId, serviceCd, new Responder(onCompanyOffersLoaded, onLoadModelDataFaulted));
		}

        private function onCompanyOffersLoaded(e:ResultEvent):void {
            companyOffers = e.result as ArrayCollection;
            hasCurrentOffer = (companyOffers != null) && (companyOffers.length > 0);

            SAP.instance.billingService.findOffers(companyKey.sourceSystemCd, companyKey.companyId, serviceCd, createLoadModelDataResponder(onOffersLoaded));
        }
		
		public function onOffersLoaded(e:ResultEvent):void {
			offers = e.result as ArrayCollection;
			offers.sort = mSortByCode;
			offers.refresh();

			if (hasCurrentOffer) {
				var currentOffer:Offer = Offer(companyOffers.getItemAt(0));
				for (var i:int = offers.length - 1; i >= 0; i--) {
					var offer:Offer = Offer(offers.getItemAt(i));
					if (offer.offerCd == currentOffer.offerCd) {
						offers.removeItemAt(i);
					}
				}

				if (currentOffer.offerEndEvent != OfferEndEventEnum.PAYROLLUSAGEEVENT.toString()) {
					showClaimedExpirationDate = true;
					claimedExpirationDate = "";
				}
			} else {
				showClaimedExpirationDate = false;
			}
		}
		
		public function get offerSelected():Boolean {
			return hasChanged;
		}
		
		override protected function evaluateCanSave():Boolean {
			return super.evaluateCanSave() && offerSelected;
		}				
		
		override protected function executeSave():void {
			SAP.instance.billingService.claimOfferWithExpirationForCompany(
					this.selectedOffer.offerCd,
					this.company.companyId,
					this.company.sourceSystemCd,
					this.expirationDateValue,
					createSaveResponder());
		}
		
		
		override public function get hasChanged():Boolean {
			var currentOffer:Offer = null;
			if ((companyOffers != null) && (companyOffers.length > 0)) {
				currentOffer = Offer(companyOffers.getItemAt(0));
            }
				
			return (selectedOffer != null) && ((currentOffer == null) || (selectedOffer.offerCd != currentOffer.offerCd));
		}
		
		override protected function initializeBackingProperties():void {
			selectedOffer = null;
		}
		
		public function updateOffer():void {
			var offer:Offer = companyOffers.getItemAt(0) as Offer;
			selectedOffer = null;
			saveMsg = "";				
			SAP.instance.showProgress();
			SAP.instance.billingService.claimOfferWithExpirationForCompany(
					offer.offerCd,
					companyKey.companyId,
					companyKey.sourceSystemCd,
					this.claimedExpirationDateValue,
					createSaveResponder());
		}		
				
		public function removeOffer(offerCd:String):void {
			selectedOffer = null;
			saveMsg = "";				
			SAP.instance.showProgress();
			SAP.instance.billingService.cancelOfferForCompany(
					offerCd,
					this.company.companyId,
					this.company.sourceSystemCd,
					createSaveResponder());
			this.showClaimedExpirationDate = false;
		}				
	}
}

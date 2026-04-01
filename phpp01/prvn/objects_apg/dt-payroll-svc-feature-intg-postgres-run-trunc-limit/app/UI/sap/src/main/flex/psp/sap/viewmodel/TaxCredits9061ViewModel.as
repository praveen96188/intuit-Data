package psp.sap.viewmodel {
    import flash.net.URLRequest;

    import mx.collections.ArrayCollection;

    import mx.controls.dataGridClasses.DataGridColumn;
    import mx.rpc.events.ResultEvent;

    import mx.validators.DateValidator;
    import mx.validators.SocialSecurityValidator;
    import mx.validators.Validator;

    import psp.sap.application.SAP;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.TaxCredits9061;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;

    public class TaxCredits9061ViewModel extends AbstractPartViewModel {

        [ArrayElementType("psp.sap.model.TaxCredits9061")]
        [Bindable] public var forms:ArrayCollection = new ArrayCollection();

        [Bindable] [BackingProperty] public var ein:String;
        [Bindable] [BackingProperty] public var ssn:String;
        [Bindable] [BackingProperty] public var startDate:String="";
        [Bindable] [BackingProperty] public var endDate:String="";
        
        [Bindable] public var einValidator:Validator;
        [Bindable] public var ssnValidator:Validator;
        [Bindable] public var startDateValidator:DateValidator;
        [Bindable] public var endDateValidator:DateValidator;
        [Bindable] public var dateRangeValidator:SAPStartEndDateValidator;

        public function TaxCredits9061ViewModel() {
            super();

            einValidator = SAPValidators.createEinValidator(this, "ein", false);
            validators.push(einValidator);

            ssnValidator = new SocialSecurityValidator();
            ssnValidator.source = this;
            ssnValidator.property = "ssn";
            ssnValidator.required = false;
            validators.push(ssnValidator);

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

        override protected function loadModelData():void {
            SAP.instance.taxCreditsService.find9061Forms(einOrNull, ssnOrNull, parseDate(startDate), parseDate(endDate), createLoadModelDataResponder(onFormsLoaded))
        }

        private function onFormsLoaded(e:ResultEvent):void {
            forms = e.result as ArrayCollection;
        }

        private function get einOrNull():String {
            return (!ein || ein == "") ? null : ein.replace(/-/g,"");
        }

        private function get ssnOrNull():String {
            return (!ssn || ssn == "") ? null : ssn.replace(/-/g,"");
        }

        override public function get hasChanged():Boolean {
            return true;
        }
        
        public function search():void {
            refresh();
        }


        public function formatCreatedDate(item:Object, dgc:DataGridColumn):String {
            return SAPDateFormatters.dateTimeFormatDateOverTime.format(item[dgc.dataField]);
        }

        public function getViewFormURL(form:TaxCredits9061, type:String):URLRequest {
			return new URLRequest("TaxCreditsForms?formId="+form.formId+"&type="+type+"&token="+SAP.instance.session.user.authorizationToken);
		}

        public function getViewApplicationURL(form:TaxCredits9061, type:String):URLRequest {
			return new URLRequest("TaxCreditsForms?formId="+form.applicationId+"&type="+type+"&token="+SAP.instance.session.user.authorizationToken);
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
    }
}
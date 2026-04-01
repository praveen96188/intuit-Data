/**
 * Created with IntelliJ IDEA.
 * User: ihannur
 * Date: 6/24/13
 * Time: 11:31 AM
 */
package psp.sap.viewmodel {
    import flexlib.scheduling.util.DateUtil;

    import mx.collections.ArrayCollection;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;
    import mx.validators.DateValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.OperationsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.Paystub;
    import psp.sap.model.PaystubDetails;
    import psp.sap.model.VMPEmployeeInfo;
    import psp.sap.validators.SAPStartEndDateValidator;

    public class VMPEmployeeViewModel extends AbstractPartViewModel {

        protected const EMPTY_STRING:String = "";

        [Bindable]
        [BackingProperty]
        public var employeeInfo:VMPEmployeeInfo;

        [Bindable]
        [BackingProperty(context=true)]
        public var employeeId:String;

        [Bindable]
        [BackingProperty]
        public var fromDate:String = EMPTY_STRING;
        [Bindable]
        [BackingProperty]
        public var toDate:String = EMPTY_STRING;

        [Bindable]
        public var fromDateValidator:DateValidator;
        [Bindable]
        public var toDateValidator:DateValidator;
        [Bindable]
        public var fromToDateRangeValidator:SAPStartEndDateValidator;

        [Bindable]
        public var searchButtonClicked:Boolean = false;

        [Bindable]
        public var paystubs:ArrayCollection;

        [Bindable]
        public var selectedPaystubDetails:PaystubDetails;

        private var mSelectedPaystub:Paystub;

        public function VMPEmployeeViewModel() {
            super();
            this.reloadOnSave = true;

            fromDateValidator = new DateValidator();
            fromDateValidator.source = this;
            fromDateValidator.property = "fromDate";
            fromDateValidator.required = false;
            fromDateValidator.trigger = this;
            validators.push(fromDateValidator);

            toDateValidator = new DateValidator();
            toDateValidator.source = this;
            toDateValidator.property = "toDate";
            toDateValidator.required = false;
            toDateValidator.trigger = this;
            validators.push(toDateValidator);

            fromToDateRangeValidator = new SAPStartEndDateValidator();
            fromToDateRangeValidator.source = this;
            fromToDateRangeValidator.trigger = this;
            fromToDateRangeValidator.startDateProperty = "fromDate";
            fromToDateRangeValidator.endDateProperty = "toDate";
            fromToDateRangeValidator.required = false;
            validators.push(fromToDateRangeValidator);
        }

        public static function createActivator(employeeSeq:String):Object {
            return {"employeeId": employeeSeq};
        }

        [Bindable (event="backingPropertyChanged")]
        public function get pageLabel():String {
            return employeeInfo == null ? CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_EMPLOYEE_INFO : CompanyInspectorPageEnum.VIEW_MY_PAYCHECK_EMPLOYEE_INFO + " for " + employeeInfo.fullNameForward;
        }

        override protected function executeSave():void {
            SAP.instance.viewMyPaycheckService.removeConsumerId(employeeInfo.employeeSeq, createSaveResponder());
        }

        public function removeConsumerId():void {
            forceSave();
        }

        override protected function onActivating():void {
            var today:Date = SAP.instance.PSPDate;
            var lastMonthDate:Date = DateUtil.addTime(today, -1 * DateUtil.MONTH_IN_MILLISECONDS);
            fromDate = SAPDateFormatters.dateFormatShort.format(lastMonthDate);
            toDate = EMPTY_STRING;
            searchButtonClicked = true;
        }

        override protected function initializeBackingProperties():void {
            selectedPaystub = null;
        }

        [Bindable (event="backingPropertyChanged")]
        public function canRemoveConsumerId():Boolean {
            return employeeInfo != null && employeeInfo.consumerId != null && SAP.canPerformOperation(OperationsEnum.EDIT_VMP_DATA);
        }

        override protected function loadModelData():void {
            paystubs = null;
            SAP.instance.viewMyPaycheckService.getEmployeeInfo(employeeId, createLoadModelDataResponder(onEmployeeInfoFetch));

            if(searchButtonClicked) {
                loadCount++;
                SAP.instance.viewMyPaycheckService.findPaystubs(employeeId, fromDate != EMPTY_STRING ? new Date(fromDate) : null, toDate != EMPTY_STRING ? new Date(toDate) : null, createLoadModelDataResponder(onPaystubSearchResult));
            }
        }

        override public function refresh(resetSaveMessage:Boolean = true):void {
            selectedPaystub = null;
            super.refresh(resetSaveMessage);
        }

        private function onEmployeeInfoFetch(e:ResultEvent):void {
            if (e.result != null) {
                employeeInfo = VMPEmployeeInfo(e.result);
            }
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        private function onPaystubSearchResult(e:ResultEvent):void{
            paystubs = e.result as ArrayCollection;
        }

        [Bindable]
        public function get selectedPaystub():Paystub {
            return mSelectedPaystub;
        }

        public function set selectedPaystub(value:Paystub):void {
            mSelectedPaystub = value;
            selectedPaystubDetails = null;
            if (mSelectedPaystub != null) {
                saveMsg = "";
                SAP.instance.viewMyPaycheckService.getPaystubDetails(mSelectedPaystub.paystubSeq, new Responder(onSelectedPaystubDetailsLoaded, onSaveFaulted_internal),companyKey.companyId);
            }
        }

        private function onSelectedPaystubDetailsLoaded(e:ResultEvent):void {
            selectedPaystubDetails = e.result as PaystubDetails;
        }
    }
}

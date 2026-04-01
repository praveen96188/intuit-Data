package psp.sap.viewmodel
{

    import flash.events.Event;

    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.application.enums.SettingsEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.ActionEvent;
    import psp.sap.model.ActionEventCode;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.Paycheck;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;

    public class PayrollTransactionsListViewModel
    extends AbstractPartViewModel
    {
        [Bindable] [BackingProperty (context=true)] public var sourcePayrollRunId:String;
        [Bindable] [BackingProperty (context=true)] public var paycheckDate:Date;

        [Bindable] public var payrollRun:PayrollRun;

        [Bindable] public var payrollType:String = "Payroll"; //used only for the page label

        public function PayrollTransactionsListViewModel()
        {
            this.reloadOnSave = true;
        }

        public static function createActivator(sourcePayrollRunId:String, paycheckDate:Date):Object {
            return {"sourcePayrollRunId":sourcePayrollRunId, "paycheckDate":paycheckDate};
        }

		[Bindable] public var showEmployeeTransactions:Boolean = false;

        [ArrayElementType("psp.sap.model.PayrollEmployeeTransaction")]
        private var mEmployeeTransactions:ArrayCollection =  null;

        [Bindable]
        public function get employeeTransactions():ArrayCollection {
            return mEmployeeTransactions;
        }

        public function set employeeTransactions(value:ArrayCollection):void {
            mEmployeeTransactions = value;
            showEmployeeTransactions = mEmployeeTransactions != null && mEmployeeTransactions.length > 0;
        }
        
        [Bindable] public var showEmployeePaychecks:Boolean = false;

        [ArrayElementType("psp.sap.model.Paycheck")]
        private var mEmployeePaychecks:ArrayCollection =  null;

        [Bindable]
        public function get employeePaychecks():ArrayCollection {
            return mEmployeePaychecks;
        }

        public function set employeePaychecks(value:ArrayCollection):void {
            mEmployeePaychecks = value;
            showEmployeePaychecks = mEmployeePaychecks != null && mEmployeePaychecks.length > 0;
        }

        [Bindable] public var showEmployerTransactions:Boolean = false;

        [ArrayElementType("psp.sap.model.PayrollTransaction")]
        private var mEmployerTransactions:ArrayCollection =  null;

        [Bindable]
        public function get employerTransactions():ArrayCollection {
            return mEmployerTransactions;
        }

        public function set employerTransactions(value:ArrayCollection):void {
            mEmployerTransactions = value;
            showEmployerTransactions = mEmployerTransactions != null && mEmployerTransactions.length > 0;
        }
        
        [Bindable] public var showIntuitTransactions:Boolean = false;

        [ArrayElementType("psp.sap.model.PayrollTransaction")]
        private var mIntuitTransactions:ArrayCollection = null;

        [Bindable]
        public function get intuitTransactions():ArrayCollection {
            return mIntuitTransactions;
        }

        public function set intuitTransactions(value:ArrayCollection):void {
            mIntuitTransactions = value;
            showIntuitTransactions = mIntuitTransactions != null && mIntuitTransactions.length > 0;
        }
        
        [Bindable] public var showAgencyTransactions:Boolean = false;

        [ArrayElementType("psp.sap.model.AgencyTransaction")]
        private var mAgencyTransactions:ArrayCollection = null;
        
        [Bindable]
        public function get agencyTransactions():ArrayCollection {
            return mAgencyTransactions;
        }

        public function set agencyTransactions(value:ArrayCollection):void {
            mAgencyTransactions = value;
            showAgencyTransactions = mAgencyTransactions != null && mAgencyTransactions.length > 0;
        }

        [Bindable] public var showVendorTransactions:Boolean = false;

        [ArrayElementType("psp.sap.model.PayrollEmployeeTransaction")]
        private var mVendorTransactions:ArrayCollection = null;

        [Bindable]
        public function get vendorTransactions():ArrayCollection {
            return mVendorTransactions;
        }

        public function set vendorTransactions(value:ArrayCollection):void {
            mVendorTransactions = value;
            showVendorTransactions = mVendorTransactions != null && mVendorTransactions.length > 0;
        }

        override protected function onActivating():void {
            includeCancelledTransactions = SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_CANCELLED_TRANSACTIONS);
        }

        override protected function loadModelData():void {            
         

            employerTransactions = new ArrayCollection();            
            intuitTransactions = new ArrayCollection();
            employeeTransactions = new ArrayCollection();
            employeePaychecks = new ArrayCollection();
            agencyTransactions = new ArrayCollection();
            vendorTransactions = new ArrayCollection();            
			
            loadCount = 7;
            SAP.instance.payrollRunService.findPayrollRun(companyKey.sourceSystemCd, companyKey.companyId, sourcePayrollRunId,
                    createLoadModelDataResponder(onPayrollRunLoadResults));

            SAP.instance.payrollRunService.findEmployerTransactions(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    sourcePayrollRunId,
                    null,
                    null,
                    createLoadModelDataResponder(onEmployerTransactionResults));

            SAP.instance.payrollRunService.findIntuitTransactions(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    sourcePayrollRunId,
                    null,
                    null,
                    createLoadModelDataResponder(onIntuitTransactionResults));

            SAP.instance.payrollRunService.findEmployeeTransactions(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    sourcePayrollRunId,
                    null,
                    null,
                    createLoadModelDataResponder(onEmployeeTransactionResults));

            SAP.instance.payrollRunService.findPaychecks(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    sourcePayrollRunId,
                    createLoadModelDataResponder(onEmployeePaycheckResults));

            SAP.instance.payrollRunService.findAgencyTransactions(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    sourcePayrollRunId,
                    null,
                    null,
                    createLoadModelDataResponder(onAgencyTransactionResults));

            SAP.instance.payrollRunService.findVendorTransactions(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    sourcePayrollRunId,
                    null,
                    null,
                    createLoadModelDataResponder(onVendorTransactionResults));

        }    
        
        public function onPayrollRunLoadResults(e:ResultEvent):void {
        	payrollRun = e.result as PayrollRun;
        }            

        public function onEmployeeTransactionResults(e:ResultEvent):void {
            var result:ArrayCollection = ArrayCollection(e.result);
            result.sort = employeeTransactionSort;
            result.filterFunction = cancelledFilterFunction;
            result.refresh();
            employeeTransactions = result;
        }
        
        public function onEmployeePaycheckResults(e:ResultEvent):void {
            var result:ArrayCollection = ArrayCollection(e.result);
            result.sort = employeeTransactionSort;
            result.refresh();
            employeePaychecks = result;
        }

        public function onEmployerTransactionResults(e:ResultEvent):void {
            var result:ArrayCollection = ArrayCollection(e.result);
            result.sort = normalTransactionSort;
            result.filterFunction = cancelledFilterFunction;
            result.refresh();
            employerTransactions = result;                        
        }

        public function onIntuitTransactionResults(e:ResultEvent):void {
            var result:ArrayCollection = ArrayCollection(e.result);
            result.sort = normalTransactionSort;
            result.filterFunction = cancelledFilterFunction;
            result.refresh();
            intuitTransactions = result;
        }
        
        public function onAgencyTransactionResults(e:ResultEvent):void {
            var result:ArrayCollection = ArrayCollection(e.result);
            result.sort = agencyTransactionSort;
            result.filterFunction = cancelledFilterFunction;
            result.refresh();
            agencyTransactions = result;
        }

        public function onVendorTransactionResults(e:ResultEvent):void {
            var result:ArrayCollection = ArrayCollection(e.result);
            result.sort = normalTransactionSort;
            result.filterFunction = cancelledFilterFunction;
            result.refresh();
            vendorTransactions = result;
        }

		private function cancelledFilterFunction (obj:Object):Boolean {
            return includeCancelledTransactions ? true : PayrollTransaction(obj).status != "Cancelled";
		}

        private function get normalTransactionSort():Sort {
            var sort:Sort = new Sort();
            sort.fields = [new SortField("txnDate", false, true), new SortField("createdDate", false, true)];
            return sort;
        }

        private function get employeeTransactionSort():Sort {
            var sort:Sort = new Sort();
            sort.fields = [new SortField("employeeName")];
            return sort;
        }

        private function get agencyTransactionSort():Sort {
            var sort:Sort = new Sort();
            var agencySortField:SortField = new SortField("agencyAbbreviation", true);
            agencySortField.compareFunction = CompareUtils.compareAgenciesTransactions;
            sort.fields = [new SortField("txnDate", false, true), agencySortField, new SortField("taxAbbreviation")];
            return sort;
        }

        public function cancelTransaction(payrollTransaction:PayrollTransaction):void {
            SAP.instance.showProgress();
            SAP.instance.payrollRunService.cancelTransaction(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    payrollTransaction.id,
                    createSaveResponder());
        }

        public function voidTransaction(payrollTransaction:PayrollTransaction):void {
            SAP.instance.showProgress();
            SAP.instance.payrollRunService.voidTransaction(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    payrollTransaction.id,
                    createSaveResponder());
        }

        public function reissueFee(payrollTransaction:PayrollTransaction):void {
            SAP.instance.showProgress();
            SAP.instance.payrollRunService.addFeeRedebitTransaction(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    payrollTransaction.id,
                    createSaveResponder());
        }

        public function reissuePayrollTaxPayment(payrollTransaction:PayrollTransaction):void {
            SAP.instance.showProgress();
            SAP.instance.payrollRunService.reissuePayrollTaxPayment(
                    companyKey.companyId,
                    companyKey.sourceSystemCd,
                    payrollTransaction.sourcePayRunId,
                    payrollTransaction.id,
                    createSaveResponder());
        }


		public function performPayrollTransactionAction(action:ActionEvent, payrollTransaction:PayrollTransaction):void {
            action.performPayrollTransactionAction(inspector, payrollRun, payrollTransaction);
        }

        public function performPaycheckAction(action:ActionEvent, paycheck:Paycheck):void {
            if (action.code == ActionEventCode.VIEW_PAYCHECK_LINE_ITEMS) {
                topic.findPage(CompanyInspectorPageEnum.PAYCHECK_LINE_ITEMS).activatePage(EmployeeLineItemsViewModel.createActivator(paycheck));
            }
		}


        //preferences

        private var mIncludeCancelledTransactions:Boolean=false;
        [Bindable] [BackingProperty] public function get includeCancelledTransactions():Boolean {
            return mIncludeCancelledTransactions;
        }

        public function set includeCancelledTransactions(value:Boolean):void {
            mIncludeCancelledTransactions = value;

            nullSafeRefresh(employeeTransactions);
            nullSafeRefresh(employerTransactions);
            nullSafeRefresh(intuitTransactions);
            nullSafeRefresh(agencyTransactions);
            nullSafeRefresh(vendorTransactions);

            showEmployeeTransactions = mEmployeeTransactions != null && mEmployeeTransactions.length > 0;
            showEmployerTransactions = mEmployerTransactions != null && mEmployerTransactions.length > 0;
            showIntuitTransactions = mIntuitTransactions != null && mIntuitTransactions.length > 0;
            showAgencyTransactions = mAgencyTransactions != null && mAgencyTransactions.length > 0;
            showVendorTransactions = mVendorTransactions != null && mVendorTransactions.length > 0;

            dispatchEvent(new Event("preferencesChanged"));
        }

        private function nullSafeRefresh(collection:ArrayCollection):void {
            if (collection != null) {
                collection.refresh();
            }
        }

        public function savePreferences():void {
            SAP.instance.session.user.setPreferenceBoolean(SettingsEnum.INCLUDE_CANCELLED_TRANSACTIONS, includeCancelledTransactions);
            dispatchEvent(new Event("preferencesChanged"));
        }

        [Bindable(event="preferencesChanged")]
        public function get preferencesChanged():Boolean {
            return includeCancelledTransactions != SAP.instance.session.user.getPreferenceBoolean(SettingsEnum.INCLUDE_CANCELLED_TRANSACTIONS);
        }

		[Bindable(event="contextPropertyChanged")]
        public function get pageLabel():String {            
            var paycheckDateString:String = paycheckDate == null ? "" : SAPDateFormatters.dateFormatMedium.format(paycheckDate);
            if (payrollType == "Payroll") {
                return "Payroll Transactions for Payroll Check Date: " + paycheckDateString;
            } else if (payrollType == "Vendor") {
                return "Payment Transactions for Payment Date: " + paycheckDateString;
            } else {
                return "Transactions";
			}
		}

        public function removeInvalidFlagOnEmailId(email:String):void {
            SAP.instance.companyService.removeInvalidFlagOnEmailAddresses(companyKey.sourceSystemCd, companyKey.companyId, email, createSaveResponder());
            forceSave();
        }
    }
}

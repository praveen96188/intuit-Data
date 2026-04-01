package psp.sap.viewmodel {
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    import mx.rpc.events.ResultEvent;
    import mx.validators.DateValidator;

    import psp.sap.application.SAP;
    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.formatters.SAPDateFormatters;
    import psp.sap.model.EmployeeInfo;
    import psp.sap.model.EmployeeLineItemGroup;
    import psp.sap.model.EmployeeLineItemPaycheck;
    import psp.sap.model.EmployeeLineItemQuarter;
    import psp.sap.model.EmployeeLineItemYear;
    import psp.sap.model.EmployeePaycheckCollection;
    import psp.sap.model.LineItemValue;
    import psp.sap.model.RotatedPaycheck;
    import psp.sap.validators.SAPStartEndDateValidator;
    import psp.sap.validators.SAPValidators;

    public class EmployeeInfoTabViewModel extends CompositePartViewModel {

        [Bindable]
        public var employeeInfo:EmployeeInfo;

        [Bindable]
        public var employeePaycheckCollection:EmployeePaycheckCollection;
        [Bindable] [BackingProperty]
        public var paychecksFound:Boolean = false;

        [Bindable]
        [ArrayElementType("psp.sap.model.EmployeeLineItemYear")]
        public var employeeProfileQTDYTDYears:ArrayCollection = new ArrayCollection();

        [Bindable]
        [ArrayElementType("psp.sap.model.RotatedPaycheck")]
        public var rotatedPaychecks:ArrayCollection = new ArrayCollection();

        [Bindable]
        public var paycheckFirstVisibleColumn:int=0;
        [Bindable]
        public var totalVisibleColumns:int;


        public var mSearchType:String  = "PAYCHECK"; /*PAYCHECK | QTDYTD */

        [Bindable]
        public function get searchType():String {
            return mSearchType;
        }
        public function set searchType(val:String):void {
            if (val != "null") {
                mSearchType = val;
            }
        }

        [Bindable]
        public var searchRequested:Boolean = false;

        [Bindable]
        [BackingProperty]
        public var fromDateString:String = "";

        [Bindable]
        [BackingProperty]
        public var toDateString:String = "";

        private var mIncludeWages:Boolean;
        [Bindable]
        public function get includeWages():Boolean {
            return mIncludeWages;
        }

        public function set includeWages(value:Boolean):void {
            mIncludeWages = value;
            if (searchRequested) {
                if (searchType == "PAYCHECK") {
                    rotatePaychecks();
                } else {
                    rotateQTD();
                }
            }
        }

        [Bindable]
        public var startEndDateValidator:SAPStartEndDateValidator;
        [Bindable]
        public var fromDateValidator:DateValidator;
        [Bindable]
        public var toDateValidator:DateValidator;


        private var profileExpander:ExpanderViewModel;

        [Bindable]
        public var employeeProfileInfoViewModel:EmployeeProfileInfoViewModel;
        [Bindable]
        public var employeeProfileInfoHistoryViewModel:EmployeeProfileHistoryViewModel;
        [Bindable]
        public var employeeTaxabilityInfoViewModel:EmployeeTaxabilityViewModel;
        [Bindable]
        public var employeeProfileDEH:DisplayEditHistoryViewModel;
        [Bindable]
        public var employeeComplianceInfoViewModel:EmployeeComplianceViewModel;


        public function EmployeeInfoTabViewModel() {
            super();
            this.reloadOnActivate = false;

            profileExpander = addExpander(CompanyInspectorPageEnum.EMPLOYEE_PROFILE_TABS);
            var tabNavigator:PartsTabNavigatorViewModel = profileExpander.addPartsTabNavigator(CompanyInspectorPageEnum.EMPLOYEE_PROFILE_TABS);
            employeeProfileDEH = tabNavigator.addNewPart(DisplayEditHistoryViewModel, CompanyInspectorPageEnum.EMPLOYEE_PROFILE) as DisplayEditHistoryViewModel;

            employeeProfileInfoViewModel = employeeProfileDEH.addDisplay(EmployeeProfileInfoViewModel) as EmployeeProfileInfoViewModel;
            employeeProfileInfoHistoryViewModel = employeeProfileDEH.addHistory(EmployeeProfileHistoryViewModel) as EmployeeProfileHistoryViewModel;

            employeeTaxabilityInfoViewModel = tabNavigator.addNewPart(EmployeeTaxabilityViewModel,
                    CompanyInspectorPageEnum.EMPLOYEE_TAXABILITY) as EmployeeTaxabilityViewModel;
            employeeComplianceInfoViewModel = tabNavigator.addNewPart(EmployeeComplianceViewModel,
                    CompanyInspectorPageEnum.EMPLOYEE_COMPLIANCE) as EmployeeComplianceViewModel;
            tabNavigator.defaultSinglePart = employeeProfileDEH;

            BindingUtils.bindProperty(employeeProfileInfoViewModel, "employeeInfo", this, "employeeInfo");
            BindingUtils.bindProperty(employeeProfileInfoHistoryViewModel, "employeeInfo", this, "employeeInfo");
            BindingUtils.bindProperty(employeeTaxabilityInfoViewModel, "employeeInfo", this, "employeeInfo");
            BindingUtils.bindProperty(employeeComplianceInfoViewModel,"employeeInfo", this, "employeeInfo");

            startEndDateValidator = SAPValidators.createSAPStartEndDateValidator(this, this, "fromDateString", "toDateString", false);
            validators.push(startEndDateValidator);

            fromDateValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "fromDateString", false);
            this.validators.push(fromDateValidator);

            toDateValidator = SAPValidators.createDefaultDatePropertyChangeValidator(this, "toDateString", false);
            this.validators.push(toDateValidator);

        }


        override protected function onActivating():void {
            if (!searchRequested)  {
                var last2Weeks:Date = SAP.instance.PSPDate;
                last2Weeks.setDate(last2Weeks.date - 14);
                fromDateString = SAPDateFormatters.dateFormatShort.format(last2Weeks);
                toDateString = SAPDateFormatters.dateFormatShort.format(SAP.instance.PSPDate);
                includeWages = false;
            }
        }

        override protected function loadModelData():void {
            if (!searchRequested) {
                modelDataLoaded();
            } else {
                if(searchType == "PAYCHECK"){
                    paychecksFound = false;
                    SAP.instance.employeeService.getEmployeeProfilePaycheckDetail(company.sourceSystemCd, company.companyId, employeeInfo.employeeId, fromDateValue, toDateValue, createLoadModelDataResponder(onEmployeePaycheckDetailsLoaded));
                } else{
                    SAP.instance.employeeService.getEmployeeProfileQTDYTDDetails(company.sourceSystemCd, company.companyId, employeeInfo.employeeId, createLoadModelDataResponder(onEmployeeQTDYTDDetailsLoaded));
                }
            }
        }

        public function get fromDateValue():Date {
            if (fromDateString == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(fromDateString);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        public function get toDateValue():Date {
            if (toDateString == "") {
                return null;
            }
            var formattedDate:String = SAPDateFormatters.dateFormatShort.format(toDateString);
            var txDate:Date = SAP.instance.PSPDate;
            var time:Number = Date.parse(formattedDate);
            txDate.setTime(time);
            return txDate;
        }

        override public function get hasChanged():Boolean {
            return true;
        }

        public function search():void{
            searchRequested = true;
            profileExpander.collapse();
            refresh();
        }

        public function onEmployeePaycheckDetailsLoaded(e:ResultEvent):void{
            paycheckFirstVisibleColumn=0;
            employeePaycheckCollection = e.result as EmployeePaycheckCollection;
            if(employeePaycheckCollection != null && employeePaycheckCollection.paychecks != null && employeePaycheckCollection.paychecks.length > 0){
                paychecksFound = true;
            }
            rotatePaychecks();
        }

        public function onEmployeeQTDYTDDetailsLoaded(e:ResultEvent):void{
            employeeProfileQTDYTDYears = e.result as ArrayCollection;
            rotateQTD();
        }


        private function rotatePaychecks():void {
            var tempRotatedPaychecks:ArrayCollection = new ArrayCollection();

            rotate(tempRotatedPaychecks, "Compensation", employeePaycheckCollection.compensationItems, "compensations", true, false, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "Addition (Taxable)", employeePaycheckCollection.taxableAdditionItems, "taxableAdditions", false, false, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "Deduction (Pre-tax)", employeePaycheckCollection.preTaxDeductionItems, "preTaxDeductions", false, false, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "Employee Taxes", employeePaycheckCollection.employeeTaxItems, "employeeTaxes", false, includeWages, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "Deduction (No Tax Affect)", employeePaycheckCollection.postTaxDeductionItems, "postTaxDeductions", false, false, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "Addition (No Tax Affect)", employeePaycheckCollection.noTaxAffectAdditionItems, "noTaxAffectAdditions", false, false, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "Direct Deposit", employeePaycheckCollection.directDepositItems, "directDeposits", false, false, employeePaycheckCollection.paychecks);

            var netPay:RotatedPaycheck = new RotatedPaycheck();
            netPay.isColumnHeader = true;
            netPay.columnHeader = "Net Pay";
            for each (var paycheck:EmployeeLineItemPaycheck in employeePaycheckCollection.paychecks) {
                if (!paycheck.isELA) {
                    netPay.amounts.addItem(paycheck.netPay);
                } else {
                    netPay.amounts.addItem(NaN);
                }
            }
            netPay.amounts.addItem(employeePaycheckCollection.netPay);
            tempRotatedPaychecks.addItem(netPay);

            rotate(tempRotatedPaychecks, "Employer Taxes", employeePaycheckCollection.employerTaxItems, "employerTaxes", false, includeWages, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "ER Contribution (Taxable)", employeePaycheckCollection.taxableEmployerContributionItems, "taxableEmployerContributions", false, false, employeePaycheckCollection.paychecks);
            rotate(tempRotatedPaychecks, "ER Contribution (No Tax Affect)", employeePaycheckCollection.noTaxAffectEmployerContributionItems, "noTaxAffectEmployerContributions", false, false, employeePaycheckCollection.paychecks);
            rotatedPaychecks = tempRotatedPaychecks;
        }

        private function rotateQTD():void {
            for each (var year:EmployeeLineItemYear in this.employeeProfileQTDYTDYears) {
                var tempRotatedPaychecks:ArrayCollection = new ArrayCollection();

                rotate(tempRotatedPaychecks, "Compensation", year.compensationItems, "compensations", true, false, year.quarters);
                rotate(tempRotatedPaychecks, "Addition (Taxable)", year.taxableAdditionItems, "taxableAdditions", false, false, year.quarters);
                rotate(tempRotatedPaychecks, "Deduction (Pre-tax)", year.preTaxDeductionItems, "preTaxDeductions", false, false, year.quarters);
                rotate(tempRotatedPaychecks, "Employee Taxes", year.employeeTaxItems, "employeeTaxes", false, includeWages, year.quarters);
                rotate(tempRotatedPaychecks, "Deduction (No Tax Affect)", year.postTaxDeductionItems, "postTaxDeductions", false, false, year.quarters);
                rotate(tempRotatedPaychecks, "Addition (No Tax Affect)", year.noTaxAffectAdditionItems, "noTaxAffectAdditions", false, false, year.quarters);
                rotate(tempRotatedPaychecks, "Direct Deposit", year.directDepositItems, "directDeposits", false, false, year.quarters);


                var netPay:RotatedPaycheck = new RotatedPaycheck();
                netPay.isColumnHeader = true;
                netPay.columnHeader = "Net Pay";
                for each (var quarter:EmployeeLineItemQuarter in year.quarters) {
                    netPay.amounts.addItem(quarter.netPay);
                }
                tempRotatedPaychecks.addItem(netPay);

                rotate(tempRotatedPaychecks, "Employer Taxes", year.employerTaxItems, "employerTaxes", false, includeWages, year.quarters);
                rotate(tempRotatedPaychecks, "Employer Contribution (Taxable)", year.taxableEmployerContributionItems, "taxableEmployerContributions", false, false, year.quarters);
                rotate(tempRotatedPaychecks, "Employer Contribution (No Tax Affect)", year.noTaxAffectEmployerContributionItems, "noTaxAffectEmployerContributions", false, false, year.quarters);

                year.rotatedPaychecks = tempRotatedPaychecks;
            }
        }


        private function rotate(rotatedPaychecks:ArrayCollection, columnHeader:String,  masterCollection:ArrayCollection, paycheckCollectionProperty:String, addHours:Boolean, addWages:Boolean, columnCollection:ArrayCollection):void {
            var headerPaycheck:RotatedPaycheck = new RotatedPaycheck();
            headerPaycheck.isColumnHeader = true;
            headerPaycheck.columnHeader = columnHeader;
            rotatedPaychecks.addItem(headerPaycheck);

            for each (var masterItem:LineItemValue in masterCollection) {
                var itemRow:RotatedPaycheck = new RotatedPaycheck();
                itemRow.columnTooltip  = masterItem.taxFormLine != null ? masterItem.taxFormLine : masterItem.itemName;
                itemRow.columnHeader = masterItem.sourceDescription;
                if (masterItem.itemId == "compTotal") {
                    itemRow.isSubTotal = true;
                }

                if (addHours) {
                    var hoursRow:RotatedPaycheck = new RotatedPaycheck();
                    hoursRow.columnHeader = "   Hours Worked";

                    if (masterItem.itemId == "compTotal") {
                        hoursRow.isSubTotal = true;
                    }
                }
                if (addWages) {
                    var totalWages:RotatedPaycheck = new RotatedPaycheck();
                    totalWages.columnHeader ="   Total Wages";
                    var taxableWages:RotatedPaycheck = new RotatedPaycheck();
                    taxableWages.columnHeader = "   Taxable Wages";
                }

                for each (var paycheck:EmployeeLineItemGroup in columnCollection) {
                    var item:LineItemValue = findItem(paycheck[paycheckCollectionProperty], masterItem.itemId);
                    if (item != null) {
                        itemRow.amounts.addItem(item.amount);
                        if (addHours) {
                            if (paycheck is EmployeeLineItemQuarter || (paycheck is EmployeeLineItemPaycheck && !EmployeeLineItemPaycheck(paycheck).isELA)) {
                                hoursRow.amounts.addItem(item.hoursWorked);
                            } else {
                                hoursRow.amounts.addItem(NaN);
                            }
                        }

                        if (addWages) totalWages.amounts.addItem(item.totalWages);
                        if (addWages) taxableWages.amounts.addItem(item.taxableWages);
                    } else {
                        itemRow.amounts.addItem(NaN);
                        if (addHours) hoursRow.amounts.addItem(NaN);
                        if (addWages) totalWages.amounts.addItem(NaN);
                        if (addWages) taxableWages.amounts.addItem(NaN);
                    }
                }
                itemRow.amounts.addItem(masterItem.amount);
                if (addHours) hoursRow.amounts.addItem(masterItem.hoursWorked);
                if (addWages) totalWages.amounts.addItem(masterItem.totalWages);
                if (addWages) taxableWages.amounts.addItem(masterItem.taxableWages);

                rotatedPaychecks.addItem(itemRow);
                if (addHours) rotatedPaychecks.addItem(hoursRow);
                if (addWages) rotatedPaychecks.addItem(taxableWages);
                if (addWages) rotatedPaychecks.addItem(totalWages);
            }

        }

        private function findItem(collection:ArrayCollection, itemId:String):LineItemValue {
            for each (var item:LineItemValue in collection) {
                if (item.itemId == itemId) {
                    return item;
                }
            }
            return null;
        }

        public function prevPage():void {
            paycheckFirstVisibleColumn = Math.max(0, paycheckFirstVisibleColumn - totalVisibleColumns);
        }

        public function prev():void {
            paycheckFirstVisibleColumn--;
        }

        public function next():void {
            paycheckFirstVisibleColumn++;
        }
        public function nextPage():void {
            paycheckFirstVisibleColumn = Math.min(employeePaycheckCollection.paychecks.length + 1 - totalVisibleColumns, paycheckFirstVisibleColumn + totalVisibleColumns);
        }



        public function goToPayrollTransactions(sourcePayrollRunId:String , paycheckDate:Date):void {
            inspector.findPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST).activatePage(PayrollTransactionsListViewModel.createActivator(sourcePayrollRunId, paycheckDate));
        }


    }
}
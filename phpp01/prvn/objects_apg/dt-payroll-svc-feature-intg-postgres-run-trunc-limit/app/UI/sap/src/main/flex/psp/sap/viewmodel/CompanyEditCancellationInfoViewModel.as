package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.controls.DateField;
import mx.rpc.events.ResultEvent;
import mx.validators.DateValidator;
import mx.validators.Validator;

import psp.sap.application.SAP;
import psp.sap.application.enums.CompanyInspectorPageEnum;
import psp.sap.model.TaxCompanyServiceInfo;
import psp.sap.validators.SAPValidators;

public class CompanyEditCancellationInfoViewModel extends AbstractPartViewModel{

    public function CompanyEditCancellationInfoViewModel() {
        this.label = CompanyInspectorPageEnum.EDIT_CANCELLATION_INFO;

        lastPayrollDateValidator = new DateValidator();
        lastPayrollDateValidator.source = this;
        lastPayrollDateValidator.property = "lastPayrollDate";
        lastPayrollDateValidator.required = false;
        lastPayrollDateValidator.trigger = this;
        validators.push(lastPayrollDateValidator);

        lastTaxQuarterRequiredValidator = SAPValidators.createRegExValidator(this, "selectedLastTaxQuarter", true, "[^0].*", this, "This field is required");
        validators.push(lastTaxQuarterRequiredValidator);
    }

    [Bindable] [BackingProperty]
    public var cancellationInfo:TaxCompanyServiceInfo;

    [Bindable] [BackingProperty]
    public var lstQuarters:ArrayCollection = new ArrayCollection(["", "Q1", "Q2", "Q3", "Q4"]);

    [Bindable] public var lastPayrollDateValidator:DateValidator;

    [Bindable] [BackingProperty]
    public var lstYears:ArrayCollection;

    [Bindable] [BackingProperty]
    public var fileAnnualReturns:Boolean;

    [Bindable] [BackingProperty]
    public var isFinal:Boolean;

    [Bindable] [BackingProperty]
    public var lastPayrollDate:String;

    [Bindable] public var lastTaxQuarterRequiredValidator:Validator;

    private var mSelectedYear:String;
    [Bindable] [BackingProperty]
    public function get selectedYear():String {
        return mSelectedYear;
    }

    public function set selectedYear(value:String):void {
        mSelectedYear = value;
        if(mSelectedYear != null && mSelectedYear!= ""){
            doNotFile = false;
            initYearAndQuarter();
        }

    }

    private var mSelectedQuarter:String;
    [Bindable] [BackingProperty]
    public function get selectedQuarter():String {
        return mSelectedQuarter;
    }

    public function set selectedQuarter(value:String):void {
        mSelectedQuarter = value;
        if(mSelectedQuarter != null && mSelectedQuarter != ""){
            doNotFile = false;
            initYearAndQuarter();
        }

    }

    private var mDoNotFile:Boolean;
    [Bindable] [BackingProperty]
    public function get doNotFile():Boolean {
        return mDoNotFile;
    }

    public function set doNotFile(value:Boolean):void {
        mDoNotFile = value;
        if(mDoNotFile){
            blankYearAndQuarter();
        } else {
            initYearAndQuarter();
        }
    }


    private function initYearAndQuarter():void{
        if(selectedYear == null || selectedYear == ""){
            selectedYear = lstYears.getItemAt(0).toString();
        }
        if(selectedQuarter == null ||selectedQuarter == "" ){
            selectedQuarter = lstQuarters.getItemAt(0).toString();
        }
    }

    private function blankYearAndQuarter():void{
        selectedQuarter = "";
        selectedYear = "";

    }

    [Bindable("propertyChange")]
    public function get selectedLastTaxQuarter():String {
        if (doNotFile) {
            return TaxCompanyServiceInfo.DO_NOT_FILE.toString();
        }
        if (selectedYear == null || selectedYear == "" || selectedQuarter == null || selectedQuarter == "") {
            return TaxCompanyServiceInfo.LAST_QUARTER_UNSET.toString();
        }
        return mSelectedYear + mSelectedQuarter.charAt(1);
    }

    override protected function loadModelData():void {
        SAP.instance.companyService.getCompanyCancellationInfo(companyKey.sourceSystemCd,
                                                               companyKey.companyId,
                                                               createLoadModelDataResponder(onCompanyCancellationInfoLoaded));
    }

    public function onCompanyCancellationInfoLoaded(e:ResultEvent):void {
        cancellationInfo = e.result as TaxCompanyServiceInfo;
    }

    override protected function initializeBackingProperties():void {
        if(cancellationInfo !=null){
            var lastTaxYear:int =  cancellationInfo.lastTaxQuarter != null && cancellationInfo.lastTaxQuarter.length > 4 ?
                    parseInt(cancellationInfo.lastTaxQuarter.substr(0,4)) : new Date().getFullYear();
            lstYears = new ArrayCollection();
            lstYears.addItem("");

            for(var i:int = -2 ; i < 2 ; i = i+1) {
                lstYears.addItem((lastTaxYear + i).toString());
            }

            if(cancellationInfo.lastTaxQuarter == TaxCompanyServiceInfo.DO_NOT_FILE.toString()){
                doNotFile = true;
            }

            if(cancellationInfo.lastTaxQuarter != null && cancellationInfo.lastTaxQuarter.length > 4 )  {
                selectedYear = cancellationInfo.lastTaxQuarter.substr(0,4);
                selectedQuarter = "Q" + cancellationInfo.lastTaxQuarter.substr(4,1);
            } else {
                selectedYear = "";
                selectedQuarter = "";
            }

            lastPayrollDate = cancellationInfo.lastPayrollDate != null ? DateField.dateToString(cancellationInfo.lastPayrollDate, 'MM/DD/YYYY') : "";
            fileAnnualReturns = cancellationInfo.fileAnnualReturns;
            isFinal = cancellationInfo.isFinal;
        }
    }

    override protected function executeSave():void {

        var taxCompanyServiceInfo:TaxCompanyServiceInfo = new TaxCompanyServiceInfo();

        taxCompanyServiceInfo.fileAnnualReturns = fileAnnualReturns;
        taxCompanyServiceInfo.isFinal = isFinal;
        taxCompanyServiceInfo.lastPayrollDate = (lastPayrollDate != null && lastPayrollDate !="") ? new Date(lastPayrollDate) : null;
        taxCompanyServiceInfo.lastTaxQuarter = selectedLastTaxQuarter;

        SAP.instance.companyService.updateCompanyCancellationInfo(companyKey.sourceSystemCd,
                                                                  companyKey.companyId,
                                                                  taxCompanyServiceInfo,
                                                                  createSaveResponder());
    }
}
}
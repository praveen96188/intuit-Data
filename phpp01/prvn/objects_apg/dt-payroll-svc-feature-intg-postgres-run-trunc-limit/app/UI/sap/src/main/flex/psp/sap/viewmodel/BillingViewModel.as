package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.logging.ILogger;
import mx.rpc.events.ResultEvent;
import mx.validators.DateValidator;

import psp.sap.application.ClientLoggingTarget;
import psp.sap.application.SAP;
import psp.sap.application.enums.CompanyInspectorPageEnum;
import psp.sap.application.enums.PartsEnum;
import psp.sap.formatters.SAPDateFormatters;
import psp.sap.model.UsageBillingInvoice;
import psp.sap.model.UsageBillingInvoiceDetail;
import psp.sap.service.PSPService;
import psp.sap.validators.SAPStartEndDateValidator;

public class BillingViewModel extends CompositePartViewModel {

    private var logger:ILogger = ClientLoggingTarget.getLogger(this);
    private var mBillingHistory:ArrayCollection = new ArrayCollection();
    [Bindable]
    public var hasBillingHistory:Boolean = false;
    private var resultsPup:PopUpPartViewModel;
    [Bindable]
    public var billingDetailsViewModel:BillingDetailsViewModel;
    public var showInvoiceDetails:Boolean = false;
    public var selectedInvoiceIndex:int = -1;

    [Bindable]
    public var subscriptionNumberCollection:ArrayCollection = new ArrayCollection();
    [Bindable] [BackingProperty] public var subscriptionNumberSelected:String = "";

    //Billing start date
    [Bindable] [BackingProperty] public var startDate:String = "";
    [Bindable] [BackingProperty] public var endDate:String = "";
    public var loadBySubscriptionAndDate:Boolean = false;

    //validators
    [Bindable]
    public var startDateValidator:DateValidator;
    [Bindable]
    public var endDateValidator:DateValidator;
    [Bindable]
    public var dateRangeValidator:SAPStartEndDateValidator;


    public function BillingViewModel() {
        super();
        this.label = CompanyInspectorPageEnum.BILLING;
        resultsPup = addPopUpPart(PartsEnum.BILLING_DETAILS);
        billingDetailsViewModel = resultsPup.addNewPart(BillingDetailsViewModel, PartsEnum.BILLING_DETAILS) as BillingDetailsViewModel;

        initValidators();
    }

    [Bindable]
    public function get billingHistory():ArrayCollection {
        return mBillingHistory;
    }

    public function set billingHistory(value:ArrayCollection):void {
        mBillingHistory = value;
        if (mBillingHistory != null) {
            mBillingHistory.refresh();
        }
    }

    private function initValidators():void {
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
        if (this.showInvoiceDetails) {
            logger.info(" loadModelData called.");
            var invoice:UsageBillingInvoice = mBillingHistory.getItemAt(selectedInvoiceIndex) as UsageBillingInvoice;
            SAP.instance.billingHistoryService.findInvoiceDetails(
                    this.company.companyId,
                    this.company.sourceSystemCd,
                    this.subscriptionNumberSelected,
                    invoice.billPOID,
                    createLoadModelDataResponder(onInvoiceDetailsResults));
        } else if (this.loadBySubscriptionAndDate) {

            SAP.instance.billingHistoryService.findBillingHistoryBySubscriptionAndDate(this.company.companyId,
                    this.company.sourceSystemCd,
                    this.subscriptionNumberSelected,
                    parseDate(this.startDate),
                    parseDate(this.endDate),
                    createLoadModelDataResponder(onBillingHistoryResults));
        } else {
            SAP.instance.billingHistoryService.findSymphonySubscriptionNumbers(
                    this.company.companyId,
                    this.company.sourceSystemCd,
                    createLoadModelDataResponder(onSubscriptionNumberResults)
            );
            SAP.instance.billingHistoryService.findBillingHistoryByDate(
                    this.company.companyId,
                    this.company.sourceSystemCd,
                    createLoadModelDataResponder(onBillingHistoryResults));
        }
    }

    public function getBillingDetailsByDateAndSubscription():void {
        loadBySubscriptionAndDate = true;
        refresh();
    }

    public function onBillingHistoryResults(e:ResultEvent):void {
        billingHistory = e.result as ArrayCollection;
        hasBillingHistory = (billingHistory != null && billingHistory.length > 0);
        if (billingHistory != null) {
            logger.info(" onBillingHistoryResults completed " + billingHistory.length + " billing in the collection");
        } else {
            logger.info(" onBillingHistoryResults completed - no billing history");
        }
    }

    public function onInvoiceDetailsResults(e:ResultEvent):void {
        var invoiceDetail:UsageBillingInvoiceDetail = e.result as UsageBillingInvoiceDetail;
        var invoice:UsageBillingInvoice = mBillingHistory.getItemAt(selectedInvoiceIndex) as UsageBillingInvoice;
        invoice.invoiceDetail = invoiceDetail;
        showInvoiceDetails = false;
        selectedInvoiceIndex = -1;

    }

    public function onSubscriptionNumberResults(e:ResultEvent):void {
        subscriptionNumberCollection = e.result as ArrayCollection;
        if (subscriptionNumberCollection != null) {
            logger.info(" onSubscriptionNumberResults completed subscription_number_length =" + subscriptionNumberCollection.length);
        }
    }

    public function displayItemCharges(index:int):void {
        showInvoiceDetails = true;
        selectedInvoiceIndex = index;
        refresh();
    }

    public function displayBillingDetails(data:UsageBillingInvoice):void {
        billingDetailsViewModel.billingHistoryInfo = data;
        resultsPup.displayPopUp();
    }

    public function updateDateSuffix(currInvoice:UsageBillingInvoice, prevInvoice:UsageBillingInvoice):void {
        var currBillDate:String = SAPDateFormatters.dateFormatShort.format(currInvoice.statementDate);
        var prevBillDate:String = SAPDateFormatters.dateFormatShort.format(prevInvoice.statementDate);
        if (currBillDate == prevBillDate) {
            currInvoice.statementDateCounter = (prevInvoice.statementDateCounter) + 1;
        }
    }

    public function getBillingInfoByDateAndSubsNum():void {
        SAP.instance.billingHistoryService.findBillingHistoryByDate(
                this.company.companyId,
                this.company.sourceSystemCd,
                createLoadModelDataResponder(onBillingHistoryResults));
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

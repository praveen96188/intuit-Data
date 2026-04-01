/**
 * Created by aagarwal14 on 5/31/2017.
 */
package psp.sap.viewmodel {
import flash.events.Event;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.events.CollectionEvent;
import mx.events.CollectionEventKind;
import mx.rpc.events.ResultEvent;
import mx.utils.StringUtil;

import psp.sap.application.SAP;
import psp.sap.application.collections.PaginationCollection;

import psp.sap.application.collections.PaginationCollection;

import psp.sap.application.enums.FraudInspectorPageEnum;
import psp.sap.application.enums.RiskInspectorPageEnum;
import psp.sap.model.DateRangeEnum;
import psp.sap.model.SearchResults;

public class IPBasedFraudSearchViewModel extends AbstractPartViewModel {
    private const EMPTY_STRING:String = "";

    [Bindable]
    [ArrayElementType("psp.sap.model.Transmission")]
    public var searchResults:PaginationCollection = new PaginationCollection();

    [Bindable]
    [BackingProperty(hasChanged=false)]
    public var mStartDate:String;
    [Bindable]
    [BackingProperty(hasChanged=false)]
    public var mEndDate:String;
    [Bindable]
    [BackingProperty(hasChanged=false)]
    public var ipAddress:String;

    // component
    private var mDateSelectionViewModel:DateSelectionViewModel;

    [Bindable]
    public var canUpdateBatchStatus:Boolean = false;

    public function IPBasedFraudSearchViewModel() {
        this.label = RiskInspectorPageEnum.IP_BASED_FRUAD_FILTERING;
        this.reloadOnActivate = false;
        this.loadOnActivate = false;
        this.watchedEntities = []; //this keeps this from loading on activate when an external entity changes
        this.reloadOnSave = true;

        initDefaults();
    }

    override public function get hasChanged():Boolean {
        return true;
    }

    public function search():void {
        if (searchResults) {
            searchResults.reset();
        }
        refresh();
    }

    [Bindable ("propertyChange")]
    public function get dateSelectionViewModel():DateSelectionViewModel {
        return mDateSelectionViewModel;
    }

    override protected function evaluateCanSave():Boolean {
       return super.evaluateCanSave() && (StringUtil.trim(ipAddress).length > 0 );
    }

    override protected function loadModelData():void {
        // change inputs back to last search inputs for F9 refresh

        SAP.instance.companyService.findTransmissionByIPAndDate(
                ipAddress,
                dateSelectionViewModel.startDateValue ,
                dateSelectionViewModel.endDateValue ,
                createLoadModelDataResponder(onSearchCompleted));
    }

    private function initDefaults():void {

        // init component
        mDateSelectionViewModel = new DateSelectionViewModel(this);
        mDateSelectionViewModel.defaultDateRange = DateRangeEnum.LAST_3_MONTHS;
        dateSelectionViewModel.dateRange = dateSelectionViewModel.defaultDateRange;
        searchResults = new PaginationCollection();
        ipAddress = EMPTY_STRING;
        mStartDate = EMPTY_STRING;
        mEndDate = EMPTY_STRING;

    }

    protected function onSearchCompleted(e:ResultEvent):void {
        var searchReturn:SearchResults = e.result as SearchResults;
        searchResults.totalRecords = searchReturn.totalRecords;
        searchResults.source = searchReturn.returnsList.source;


    }
}
}
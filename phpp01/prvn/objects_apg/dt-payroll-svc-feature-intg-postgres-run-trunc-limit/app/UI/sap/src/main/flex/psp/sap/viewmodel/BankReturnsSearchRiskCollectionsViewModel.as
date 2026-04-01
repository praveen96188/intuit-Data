package psp.sap.viewmodel
{
	import mx.rpc.Responder;
	
	import psp.sap.application.SAP;

	public class BankReturnsSearchRiskCollectionsViewModel extends BankReturnsSearchBaseViewModel
	{
		public function BankReturnsSearchRiskCollectionsViewModel()
		{
			super();
		}
		
		override public function searchBankReturns(useLastCriteria:Boolean = false):void {
			setupSearch();
			
			// save search criteria
			if(!useLastCriteria){
				lastEin = ein;
				lastStartDate = startDate;
				lastEndDate = endDate;
                lastAchReturn = achReturn;               
			}
			// reload search criteria
			else {
				ein = lastEin;
				startDate = lastStartDate;
				endDate = lastEndDate;
                achReturn = lastAchReturn; 
			}

			SAP.instance.bankReturnService.findCompanyBankReturnsByComplexSearch(
                                               (ein.length > 0) ? ein : null,
                                               startDateValue,
                                               endDateValue,
                                               achReturnValue,
                                               getReportTypeCode(),
                                               searchResults.sortBy,
                                               searchResults.sortDesc,
                                               searchResults.startIndex,
                                               searchResults.pageSize,
                                               false,
                                               new Responder(onSearchCompleted, onSearchFaulted));
		}
		
		override public function getReportTypeCode():String {
			return "RiskCollections";
		}
		
	}
}

package psp.sap.application
{
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;

    import psp.sap.application.enums.ExplorerEnum;
    import psp.sap.model.Company;
    import psp.sap.viewmodel.AbstractInspectorViewModel;
    import psp.sap.viewmodel.CompanyExplorerViewModel;

    /**
	 * Event handling code for watching the CompanyExplorerViewModel::inspectors
	 * collection.  The MRUList should always contain the following:
	 * 
	 * Any company that has been opened in an Inspector but is currently closed.
	 *
	 *  
	 * If a company is re-opened, it is removed from the MRU list until it is
	 * closed again.
	 */
	internal class MRUEventHandling
	{
		public function MRUEventHandling()
		{
			throw new Error("MRUEventHandling is a static class that should not be instantiated");
		}
		
		public static function enable(companyExplorer:CompanyExplorerViewModel = null):Boolean {
			if (companyExplorer == null)
				companyExplorer = getCompanyExplorer();
			
			if (companyExplorer == null || companyExplorer.inspectors == null) {
				trace("Failed to enable MRUEventHandling");
				return false;
			}

			companyExplorer.inspectors.addEventListener(CollectionEvent.COLLECTION_CHANGE, 
														onInspectorCollectionChanged, false, 0, true);
														
			return true;			
		}

		private static function getCompanyExplorer():CompanyExplorerViewModel {
            return SAP.instance.explorers.getExplorer(ExplorerEnum.COMPANY)
            as CompanyExplorerViewModel;
		}
		
		public static function disable():void {
			var companyExplorer:CompanyExplorerViewModel = getCompanyExplorer();
			
			companyExplorer.inspectors.removeEventListener(CollectionEvent.COLLECTION_CHANGE, 
														   onInspectorCollectionChanged);
		}
		
		public static function onInspectorCollectionChanged(e:CollectionEvent):void {

			if (e.kind == CollectionEventKind.ADD) {
				onInspectorsAdded(e.items);
			}
			else if (e.kind == CollectionEventKind.REMOVE) {
				onInspectorsRemoved(e.items);
			}
		
		}

		
		//When inspector is ADDed, REMOVE from MRU		
		private static function onInspectorsAdded(inspectors:Array):void {

			for each (var inspector:AbstractInspectorViewModel in inspectors) {
				var applicationItem:IApplicationItem = inspector.applicationItem;
				if (applicationItem != null && SAP.instance.recentlyInspectedCompanies.contains(applicationItem)) {
					SAP.instance.recentlyInspectedCompanies.removeItem(applicationItem);					
				}
				if(applicationItem != null && !SAP.instance.openCompanies.contains(applicationItem)){
					var company:Company = Company(applicationItem); 
					SAP.instance.openCompanies..addItem(company);
				}
			}
									
		}
		
		//When inspector is REMOVEd, ADD to MRU
		private static function onInspectorsRemoved(inspectors:Array):void {

			for each (var inspector:AbstractInspectorViewModel in inspectors) {
				var applicationItem:IApplicationItem = inspector.applicationItem;
				if (applicationItem != null) {
					var company:Company = Company(applicationItem);
					SAP.instance.recentlyInspectedCompanies.addItem(company);
				}
				if(applicationItem != null && SAP.instance.openCompanies.contains(applicationItem)){
					SAP.instance.openCompanies.removeItem(applicationItem);
				}
			}
					
		}
	}
}
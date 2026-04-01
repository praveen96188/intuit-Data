package psp.sap.viewmodel
{
	import mx.collections.ArrayCollection;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	
	import psp.sap.viewmodel.events.ViewModelEvent;
	
	import testTools.model.BankReturn;
	import testTools.model.EntryDetailRecord;
	import testTools.model.OffloadGroup;
	import testTools.service.TestService;
		
	public class TTBankSimulatorViewModel extends CompositePartViewModel
	{
		public static const BANK_RETURN_CODES:Array = new Array(
			"<select return code>",
		    "R01 - Insufficient Funds",
		    "R02 - Account Closed",
		    "R03 - No Account/Unab...",
		    "R04 - Invalid Account...",
		    "R05 - Unauthorized De...",
		    "R06 - Returned per OD...",
		    "R07 - Authorization R...",
		    "R08 - Payment Stopped",
		    "R09 - Uncollected Funds",
		    "R09 - Uncollected Funds",
		    "R10 - Customer Advise...",
		    "R11 - Check Truncatio...",
		    "R12 - Account Sold to...",
		    "R13 - RDFI Not Qualif...",
		    "R14 - Representative ...",
		    "R15 - Beneficiary or ...",
		    "R16 - Account Frozen",
		    "R17 - File Record Edi...",
		    "R18 - Improper Effect...",
		    "R19 - Amount Field Error",
		    "R20 - Non-Transaction...",
		    "R21 - Invalid Company...",
		    "R22 - Invalid Individ...",
		    "R23 - Credit Entry Re...",
		    "R24 - Duplicate Entry",
		    "R25 - Addenda Error",
		    "R26 - Mandatory Field...",
		    "R27 - Trace Number Error",
		    "R28 - Routing Number ...",
		    "R29 - Corporate Custo...",
		    "R30 - RDFI Not Partic...",
		    "R31 - Permissible Ret...",
		    "R32 - RDFI Non-Settle...",
		    "R33 - Return of XCK E...",
		    "R35 - Return of Impro...",
		    "R36 - Return of Impro...",
		    "R37 - Source Document...",
		    "R38 - Stop Payment on...",
		    "R39 - Improper Source...",
		    "R40 - Return of ENR E...",
		    "R41 - Invalid Transac...",
		    "R42 - Routing Number/...",
		    "R43 - Invalid DFI Acc...",
		    "R44 - Invalid Individ...",
		    "R45 - Invalid Individ...",
		    "R46 - Invalid Represe...",
		    "R47 - Duplicate Enrol...",
		    "R50 - State Law Affec...",
		    "R51 - Item is Ineligi...",
		    "R52 - Stop Payment on...",
		    "R53 - Item and ACH En...",
		    "R61 - Misrouted Return",
		    "R62 - Incorrect Trace...",
		    "R63 - Incorrect Dolla...",
		    "R64 - Incorrect Indiv...",
		    "R65 - Incorrect Trans...",
		    "R66 - Incorrect Compa...",
		    "R67 - Duplicate Return",
		    "R68 - Untimely Return",
		    "R69 - Multiple Errors...",
		    "R70 - Permissible Ret...",
		    "R71 - Misrouted Disho...",
		    "R72 - Untimely Dishon...",
		    "R73 - Timely Original...",
		    "R74 - Corrected Return",
		    "R75 - Original Return...",
		    "R76 - No Errors Found",
		    "R80 - Cross-Border Pa...",
		    "R81 - Non-Participant...",
		    "R82 - Invalid Foreign...",
		    "R83 - Foreign Receivi...",
		    "R84 - Entry Not Proce...",
		    "C01 - Incorrect DFI A...",
		    "C02 - Incorrect Routi...",
		    "C03 - Incorrect Routi...",
		    "C04 - Incorrect Indiv...",
		    "C05 - Incorrect Trans...",
		    "C06 - Incorrect DFI A...",
		    "C07 - Incorrect Routi...",
		    "C08 - Incorrect Forei...",
		    "C09 - Incorrect Indiv...",
		    "C13 - Addenda Format ...",
		    "C61 - Misrouted Notif...",
		    "C62 - Incorrect Trace...",
		    "C63 - Incorrect Compa...",
		    "C64 - Incorrect Indiv...",
		    "C65 - Incorrectly For...",
		    "C66 - Incorrect Discr...",
		    "C67 - Routing Number ...",
		    "C68 - DFI Account Num...",
		    "C69 - Incorrect Trans...");

		public function TTBankSimulatorViewModel () {
			super();
			
			//nameValidator = SAPValidators.createStringValidator(this, "groupName", true);
			//validators.push(nameValidator);

			this.addEventListener(ViewModelEvent.SAVE_SUCCEEDED, refreshData);
		}
		
		[Bindable]
		public var entryDetailRecords:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var startDate:Date = new Date();
		
		[Bindable]
		public var endDate:Date = new Date();
		
		[Bindable]
		public var selectedOffloadGrpIndex:int = 0;
		
		[Bindable]
		public var offloadGroupList:ArrayCollection = new ArrayCollection();
		
		[Bindable]
		public var isSearching:Boolean = false;
		
		override protected function loadModelData():void {
			TestService.instance.findOffloadGroups(this.createLoadModelDataResponder(onLoadSucceeded));
		}
		
		protected function onLoadSucceeded(e:ResultEvent):void {
			offloadGroupList = e.result as ArrayCollection;
		}
			
		public function searchTransactions():void {
			isSearching = true;
			showGrid = false;
			showNoResults = false;
			TestService.instance.getEntryDetailRecords(startDate, endDate, OffloadGroup(offloadGroupList.getItemAt(selectedOffloadGrpIndex)).groupCode, 0, 2000,
				this.createLoadModelDataResponder(onSearchTransactionsSucceeded, onSearchFault));
		}
		
        protected function onSearchTransactionsSucceeded(e:ResultEvent):void{
        	entryDetailRecords = e.result as ArrayCollection;
        	if (entryDetailRecords.length > 0) {
        		var testRec:EntryDetailRecord = entryDetailRecords.getItemAt(0) as EntryDetailRecord;
        		if (testRec.bankReturnsExists) {
        			var testReturn:BankReturn = testRec.bankReturns.getItemAt(0) as BankReturn;
        		}
        	}
        	isSearching = false;
        	if (entryDetailRecords.length > 0) {
        		showGrid = true;
        		showNoResults = false;
        	} else {
        		showGrid = false;
        		showNoResults = true;
        	}
     	}
     	
     	protected function onSearchFault(e:FaultEvent):void {
     		isSearching = false;
     		showGrid = false;
     		showNoResults = false;
     	}
    	
    	private function createBankReturnDTOs():ArrayCollection {
    		var returnList:ArrayCollection = new ArrayCollection();
			for each (var entry:EntryDetailRecord in entryDetailRecords) {
				if (entry.bankReturnCd != "" && entry.bankReturnCd != BANK_RETURN_CODES[0]) {
					var newReturn:BankReturn = new BankReturn();
					newReturn.accountNumber = entry.accountNumber;
					newReturn.accountType = entry.bankAccountType;
					newReturn.bankReturnCd = entry.bankReturnCd.substr(0, 3);
					newReturn.employeeDisplayName = entry.individualName;
					newReturn.routingNumber = entry.routingNumber;
					newReturn.traceNumber = entry.traceNumber;
					newReturn.transactionId = entry.mmTransactionId;
					returnList.addItem(newReturn);
				}	
			}    		
			return returnList;
    	}
        
        override protected function executeSave():void {
        	var returnDTOs:ArrayCollection = createBankReturnDTOs();
        	if (returnDTOs.length > 0) {
	        	TestService.instance.createBankReturnsForMoneyMovementTransactions(
	        		returnDTOs,        	
	                createSaveResponder(onSaveSucceeded));
	        } else {
	        	createSaveResponder().result(null);
	        }
        }
        
        public function onSaveSucceeded(e:Object):void {
        	searchTransactions();
        }
        
        private function refreshData(e:ViewModelEvent):void {
        	loadModelData();
        }
        
        override public function get hasChanged():Boolean {
        	return true;
        }
        
        [Bindable]
        public var showGrid:Boolean = false;
        
        [Bindable]
        public var showNoResults:Boolean = true;
        
	}
}
package psp.sap.viewmodel {
	import mx.collections.ArrayCollection;
	import mx.rpc.events.ResultEvent;
	import mx.validators.Validator;

	import psp.sap.application.SAP;
	import psp.sap.application.collections.PaginationCollection;
	import psp.sap.application.enums.AccountingInspectorPageEnum;
	import psp.sap.application.enums.TransactionPageEnum;
	import psp.sap.formatters.SAPDateFormatters;
	import psp.sap.model.BookTransferTransaction;
	import psp.sap.model.SearchResults;
	import psp.sap.validators.SAPNotSameValidator;
	import psp.sap.validators.SAPValidators;

	public class BookTransfersViewModel extends CompositePartViewModel {

		static public const MAX_AMOUNT:Number = 1999999.99;

		private const DEFAULT_PAGE_SIZE:int = 10;
		private const EMPTY_STRING:String = "";

		[Bindable]
		public var searchResults:PaginationCollection = new PaginationCollection(null, DEFAULT_PAGE_SIZE);

		[Bindable]
		[BackingProperty]
		public var fromAccount:String = EMPTY_STRING;
		[Bindable]
		[BackingProperty]
		public var toAccount:String = EMPTY_STRING;
		[Bindable]
		[BackingProperty]
		public var amount:String = EMPTY_STRING;

		[Bindable]
		public var fromDateSearch:String = EMPTY_STRING;
		[Bindable]
		public var toDateSearch:String = EMPTY_STRING;
		[Bindable]
		public var accountSearch:String = EMPTY_STRING;

		[Bindable]
		[BackingProperty]
		public var intuitAccounts:ArrayCollection = new ArrayCollection();

		/*Validators*/
		[Bindable]
		public var fromRequiredValidator:Validator;
		[Bindable]
		public var toRequiredValidator:Validator;
		[Bindable]
		public var amountValidator:Validator;
		[Bindable]
		public var sameFromAndToAccountValidator:SAPNotSameValidator;

		//History pop up
		private var mTransactionHistoryPopUp:PopUpPartViewModel;
		private var mTransactionHistoryPopUpViewModel:TransactionHistoryPopUpViewModel;

		private var action:String;
		private var txnId:String;

		public function BookTransfersViewModel() {
			this.label = AccountingInspectorPageEnum.BOOK_TRANSFERS;

			reloadOnSave = true;

			mTransactionHistoryPopUp = addPopUpPart(TransactionPageEnum.TRANSACTION_HISTORY);
			mTransactionHistoryPopUpViewModel = mTransactionHistoryPopUp.addNewPart(TransactionHistoryPopUpViewModel, TransactionPageEnum.TRANSACTION_HISTORY) as TransactionHistoryPopUpViewModel;

			fromRequiredValidator = SAPValidators.createStringValidator(this, "fromAccount", true, 1);
			validators.push(fromRequiredValidator);
			toRequiredValidator = SAPValidators.createStringValidator(this, "toAccount", true, 1);
			validators.push(toRequiredValidator);
			amountValidator = SAPValidators.createNumberValidator(this, "amount", true, 0.01, MAX_AMOUNT, false, 2);
			validators.push(amountValidator);
			sameFromAndToAccountValidator = SAPValidators.createSAPNotSameValidator(this, this, "fromAccount", "toAccount", false);
			validators.push(sameFromAndToAccountValidator);

		}

		override protected function loadModelData():void {
			if (intuitAccounts == null || intuitAccounts.length == 0) {
				loadCount++;
				SAP.instance.accountingService.getIntuitAccountsDescription(createLoadModelDataResponder(onIntuitAccountsLoaded));
			}
			SAP.instance.accountingService.findBookTransferTransactions(fromDate, toDate, accountSearch, searchResults.startIndex, searchResults.pageSize, searchResults.sortBy, searchResults.sortDesc, createLoadModelDataResponder(onSearchResults));
		}

		private function onIntuitAccountsLoaded(e:ResultEvent):void {
			intuitAccounts = e.result as ArrayCollection;
			intuitAccounts.addItemAt(EMPTY_STRING, 0); // Adding empty string at first position in combo box
		}

		public function onSearchResults(e:ResultEvent):void {
			searchResults.searchResults = SearchResults(e.result);
		}

		override protected function initializeBackingProperties():void {

			fromAccount = intuitAccounts.length > 0 ? intuitAccounts.getItemAt(0) as String : null;
			toAccount = intuitAccounts.length > 0 ? intuitAccounts.getItemAt(0) as String : null;
			amount = EMPTY_STRING;

		}

		override protected function executeSave():void {
			if (action == "CancelTxn") {
				SAP.instance.accountingService.cancelBookTransfer(txnId, createSaveResponder());
			} else if (action == "CreateBookTransfer") {
				SAP.instance.accountingService.createBookTransfer(fromAccount, toAccount, parseFloat(amount), createSaveResponder());
			}
		}

		public function pageDataGridLoadDataFunction():void {
			refresh();
		}

		public function search():void {
			searchResults.reset();
			refresh();
		}

		public function viewHistory(data:BookTransferTransaction):void {
			mTransactionHistoryPopUpViewModel.bookTransferTxn = data;
			mTransactionHistoryPopUp.displayPopUp();
		}

		public function createBookTransfer():void {
			action = "CreateBookTransfer";
			forceSave();
		}

		public function cancelTransaction(transactionId:String):void {
			action = "CancelTxn";
			txnId = transactionId;
			forceSave();
		}

		public function get fromDate():Date {
			if (fromDateSearch == EMPTY_STRING) {
				return null;
			}
			var formattedDate:String = SAPDateFormatters.dateFormatShort.format(fromDateSearch);
			var fromDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			fromDate.setTime(time);
			return fromDate;
		}

		public function get toDate():Date {
			if (toDateSearch == EMPTY_STRING) {
				return null;
			}
			var formattedDate:String = SAPDateFormatters.dateFormatShort.format(toDateSearch);
			var toDate:Date = SAP.instance.PSPDate;
			var time:Number = Date.parse(formattedDate);
			toDate.setTime(time);
			return toDate;
		}

	}

}

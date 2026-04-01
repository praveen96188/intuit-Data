package psp.sap.application
{

    import mx.controls.Alert;
    import mx.events.CloseEvent;
    import mx.logging.ILogger;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.enums.CompanyInspectorPageEnum;
    import psp.sap.model.PayrollRun;
    import psp.sap.model.PayrollTransaction;
    import psp.sap.model.PayrollTypeEnum;
    import psp.sap.viewmodel.CompanyEditSubscriptionStatusViewModel;
    import psp.sap.viewmodel.CompanyInspectorViewModel;
    import psp.sap.viewmodel.FinancialLedgerAdjustmentViewModel;
    import psp.sap.viewmodel.PayrollTransactionHistoryViewModel;
    import psp.sap.viewmodel.PayrollTransactionsListViewModel;

    /**
	 * this handles the absurd amount of crap to link from a page to the payroll pages
	 * I just copied this from the event log, so who I don't really know what it is doing or if it 
	 * is needed, but it seems to work.
	 */
	public class CompanyInspectorLinkHandler
	{
		
		private var inspector:CompanyInspectorViewModel;
		
		private var logger:ILogger = ClientLoggingTarget.getLogger(this);
		
		public function CompanyInspectorLinkHandler(inspector:CompanyInspectorViewModel)
		{
			this.inspector = inspector;
		}
		
		public function goToPayrollTransaction(transactionId:String):void {
			SAP.instance.showProgress("Loading...");			
			SAP.instance.payrollRunService.findPayrollTransactionById(transactionId, inspector.company.companyId,
															new Responder(onTransactionLoadResults, onFinTxnFail));
		}	
		
		protected final function onPayrollFail(e:FaultEvent=null, token:Object=null):void {
			onFail("The selected payroll is no longer available", e);				
		}

        protected final function onFinTxnFail(e:FaultEvent=null, token:Object=null):void {
            onFail("The selected financial transaction is no longer available", e);
		}

        private function onFail(message:String, e:FaultEvent):void {
			SAP.instance.hideProgress();
            Alert.show(message);
			logger.error("Failed to load: " + e.fault.faultString);
        }
		
		private function onTransactionLoadResults(e:ResultEvent):void {
			var payrollTransaction:PayrollTransaction = e.result as PayrollTransaction;
			if(payrollTransaction != null){
				if(payrollTransaction.sourcePayRunId != null){
					goToSourcePayrollRun(payrollTransaction.sourcePayRunId);
				}
				else{
					SAP.instance.hideProgress();
					// there is no payroll associated with this transaction ** should only happen with verification debits
					// goto the transaction history page
					inspector.findPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_HISTORY).activatePage(PayrollTransactionHistoryViewModel.createActivator(payrollTransaction));
				}
			}
			else{
				SAP.instance.hideProgress();			
			}
		}	
		
		public function goToSourcePayrollRun(sourcePayRunId:String):void {
			if(!SAP.instance.isShowingProgress){
				SAP.instance.showProgress("Loading...");
			}						
			SAP.instance.payrollRunService.findPayrollRun(inspector.company.sourceSystemCd, inspector.company.companyId, sourcePayRunId,
															new Responder(onPayrollRunLoadResults, onPayrollFail));
		}
		
		public function goToPayrollRun(payrollRunId:String):void {
			if(!SAP.instance.isShowingProgress){
				SAP.instance.showProgress("Loading...");
			}						
			SAP.instance.payrollRunService.findPayrollRunByPayrollRunId(payrollRunId,
															new Responder(onPayrollRunLoadResults, onPayrollFail));
		}
		
		private function onPayrollRunLoadResults(event:ResultEvent):void {
			SAP.instance.hideProgress();
			var payrollRun:PayrollRun = event.result as PayrollRun;

			if(payrollRun != null){
				// goto the transactions page
                if (payrollRun.payrollType == PayrollTypeEnum.BILL_PAYMENT.toString()) {
                    inspector.findPage(CompanyInspectorPageEnum.PAYROLL_VENDOR_TRANSACTION_LIST).activatePage(PayrollTransactionsListViewModel.createActivator(payrollRun.sourcePayRunId, payrollRun.paycheckDate));
                }
                else {
                    inspector.findPage(CompanyInspectorPageEnum.PAYROLL_TRANSACTION_LIST).activatePage(PayrollTransactionsListViewModel.createActivator(payrollRun.sourcePayRunId, payrollRun.paycheckDate));
                }
			}
			else {
				// goto payrolls page
                inspector.getPage(CompanyInspectorPageEnum.PAYROLL).activatePage();
			}					
		}			
		
		public function goToBanks():void {
            inspector.getPage(CompanyInspectorPageEnum.COMPANY_BANK).activatePage();
		}

        public function goToPayrolls():void {
            inspector.getPage(CompanyInspectorPageEnum.PAYROLL).activatePage();
        }
				
        public function resendEmail(emailId:String):void {
            Alert.show("Are you sure you want to resend this email?",
                    "Resend Email Confirmation",
                    Alert.OK | Alert.CANCEL,
                    null,
                    function(evt_obj:CloseEvent):void {
                        if(evt_obj.detail == Alert.OK)
                        {
                            SAP.instance.companyService.resendEmail(inspector.companyKey.sourceSystemCd,
                                    inspector.companyKey.companyId,
                                    emailId, new Responder(onResendSuccess, onResendFail));
                        }
                    },
                    null,
                    Alert.OK);
        }

        private function onResendSuccess(e:ResultEvent):void {
            Alert.show("New email regenerated.  Email will be resent within 5 minutes.", "Success");            
        }

        private function onResendFail(e:FaultEvent):void {
            Alert.show("Email failed to resend: " + e.fault.faultDetail);
        }

        public function sendEmailToMtl(emailSeqId:String):void {
            var sessionUserEmailAddress:String = SAP.instance.session.user.emailAddress;
            sessionUserEmailAddress = (sessionUserEmailAddress == "") ? null : sessionUserEmailAddress;
            Alert.show("This payroll email will be regenerated and sent to your email ID (SSO login). Do you want to proceed?",
                    "Send Email Confirmation",
                    Alert.OK | Alert.CANCEL,
                    null,
                    function(evt_obj:CloseEvent):void {
                        if(evt_obj.detail == Alert.OK)
                        {
                            SAP.instance.companyService.sendEmailToMtl(inspector.companyKey.sourceSystemCd,
                                    inspector.companyKey.companyId, emailSeqId,
                                    sessionUserEmailAddress, new Responder(onSendSuccess, onSendFail));
                        }
                    },
                    null,
                    Alert.OK);
        }

        private function onSendSuccess(e:ResultEvent):void {
            Alert.show("Sent to your email ID (SSO login email). You should receive this email in 5 minutes.", "Success");
        }

        private function onSendFail(e:FaultEvent):void {
            Alert.show("Email failed to send: " + e.fault.faultDetail);
        }

        public function goToEditSubscriptionStatus(serviceCode:String):void {
			CompanyEditSubscriptionStatusViewModel(inspector.findPart(CompanyInspectorPageEnum.SUBSCRIPTION_STATUS)).selectedServiceCode = serviceCode;
            inspector.getPage(CompanyInspectorPageEnum.SUBSCRIPTION_STATUS).activatePage();
		}

        public function goToTaxOverpayments():void {
            inspector.getPage(CompanyInspectorPageEnum.TAX_OVERPAYMENT).activate();
        }

        public function goToTaxOverpaymentsRefunds():void {
			inspector.getPage(CompanyInspectorPageEnum.TAX_OVERPAYMENT_REFUND_LIST).activatePage();            
        }

        public function goToDataSyncSearch():void {
			inspector.getPage(CompanyInspectorPageEnum.DATA_SYNC_SEARCH_VIEW).activatePage();
        }

        public function goToDataSyncManage():void {
			inspector.getPage(CompanyInspectorPageEnum.DATA_SYNC_MANAGE_VIEW).activatePage();
        }

        public function goToFinancialLedgerAdjustment(sourcePayrollRunId:String = null):void {
            if (sourcePayrollRunId == null) {
                inspector.getPage(CompanyInspectorPageEnum.FINANCIAL_LEDGER_ADJUSTMENT).activatePage();
            } else {
                inspector.getPage(CompanyInspectorPageEnum.FINANCIAL_LEDGER_ADJUSTMENT_PAYROLL).activatePage(FinancialLedgerAdjustmentViewModel.createActivator(sourcePayrollRunId));
            }
        }

        public function goToPAndIRefunds():void {
            inspector.getPage(CompanyInspectorPageEnum.TAX_PENALTIES_AND_INTEREST).activatePage();
        }
    }

}

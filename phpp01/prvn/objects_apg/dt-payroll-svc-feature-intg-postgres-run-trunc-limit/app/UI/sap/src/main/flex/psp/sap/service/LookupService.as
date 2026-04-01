package psp.sap.service {
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.logging.ILogger;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.ClientLoggingTarget;
    import psp.sap.application.SAP;
    import psp.sap.application.collections.LookupCollection;
    import psp.sap.application.events.SAPEvent;
    import psp.sap.model.Agency;
    import psp.sap.model.CompanyEventType;
    import psp.sap.model.CompareUtils;
    import psp.sap.model.FundingModel;
    import psp.sap.model.PaymentTemplate;
    import psp.sap.model.PayrollStatus;
    import psp.sap.model.ServiceStatus;
    import psp.sap.model.ServiceSubStatus;
    import psp.sap.model.SourceSystem;
    import psp.sap.model.TransactionType;
    import psp.sap.viewmodel.events.EntityChangeEvent;

    public class LookupService extends PSPService {
        private var logger:ILogger = ClientLoggingTarget.getLogger(this);

        public function LookupService() {
        }

        public function attachListeners():void {
            // remove old listeners
            SAP.instance.removeEventListener(EntityChangeEvent.ENTITY_SAVED, onEntityChange);
            SAP.instance.removeEventListener(EntityChangeEvent.PAGE_REFRESH, onPageRefresh);
            // add new
            SAP.instance.addEventListener(EntityChangeEvent.ENTITY_SAVED, onEntityChange, false, 0, true);
            SAP.instance.addEventListener(EntityChangeEvent.PAGE_REFRESH, onPageRefresh, false, 0, true);
        }

        private var mOutstandingRemoteRequests:int = 0;

        public function get outstandingRequests():int {
            return mOutstandingRemoteRequests;
        }

        public function set outstandingRequests(value:int):void {
            if (value < 0)
                throw new Error("outstanding requests is negative!  How do we have more methods returning than being called?!");

            var fireEvent:Boolean = (mOutstandingRemoteRequests > 0 && value == 0);
            mOutstandingRemoteRequests = value;
            if (fireEvent)
                dispatchEvent(SAPEvent.createDataLoadCompletedEvent());
        }

        public function loadData():void {
            var mSAP:SAP = SAP.instance;

            // todo this should be an enum
            loadPayrollStatus();

            outstandingRequests += 7;

            mSAP.companyService.getFundingModelList(
                    new Responder(fundingModelResultHandler, faultHandler));

            mSAP.companyService.getSourceSystemList(
                    new Responder(sourceSystemResultHandler, faultHandler));

            mSAP.companyService.getServiceStatusList(
                    new Responder(serviceStatusResultHandler, faultHandler));

            mSAP.companyService.getServiceSubStatusList(
                    new Responder(serviceSubStatusResultHandler, faultHandler));

            mSAP.payrollRunService.getTransactionTypeList(
                    new Responder(transactionTypeResultHandler, faultHandler));

            // tax stuff
            mSAP.taxService.getAgencyList(
                    new Responder(agencyListHandler, faultHandler));

            mSAP.companyService.getFraudEventTypes(new Responder(fraudEventTypesHandler, faultHandler));
        }

        private function agencyListHandler(e:ResultEvent):void {
            mAgencyList = new LookupCollection(FundingModel, ArrayCollection(e.result).toArray(), "agencyAbbrev");

            // agency sort (IRS first the alphabetical
            var agencySort:Sort = new Sort();
            var agencySortField:SortField = new SortField("agencyName");
            agencySortField.compareFunction = CompareUtils.compareAgencies;
            agencySort.fields = [agencySortField];

            // payment template sort (IRS first then alphabetical)
            var paymentTemplateSort:Sort = new Sort();
            var paymentTemplateSortField:SortField = new SortField("paymentTemplateName");
            paymentTemplateSortField.compareFunction = CompareUtils.comparePaymentTemplate;
            paymentTemplateSort.fields = [paymentTemplateSortField];

            // law item sort
            var lawItemSort:Sort = new Sort();
            lawItemSort.fields = [new SortField("name")];

            for each(var agency:Agency in mAgencyList) {
                for each(var paymentTemplate:PaymentTemplate in agency.paymentTemplates) {
                    paymentTemplate.lawItems.sort = lawItemSort;
                    paymentTemplate.lawItems.refresh();
                }
                agency.paymentTemplates.sort = paymentTemplateSort;
                agency.paymentTemplates.refresh();
            }

            var paymentTemplate:PaymentTemplate = new PaymentTemplate();
            paymentTemplate.paymentTemplateName = "ALL";
            paymentTemplate.paymentTemplateCd = "ALL";
            paymentTemplate.agencyName = "ALL";

            var paymentTemplateArray:ArrayCollection = new ArrayCollection();
            paymentTemplateArray.addItem(paymentTemplate);

            var agency:Agency = new Agency();
            agency.agencyId = "ALL";
            agency.agencyAbbrev = "ALL";
            agency.paymentTemplates = paymentTemplateArray;

            mAgencyList.source.push(agency);
            mAgencyList.sort = agencySort;
            mAgencyList.refresh();

            outstandingRequests--;
        }

        private function fundingModelResultHandler(e:ResultEvent):void {
            mFundingModels = new LookupCollection(FundingModel, ArrayCollection(e.result).toArray(), "fundingModelCd");
            outstandingRequests--;
        }

        private function sourceSystemResultHandler(e:ResultEvent):void {
            mSourceSystems = new LookupCollection(SourceSystem, ArrayCollection(e.result).toArray(), "sourceSystemCd");
            outstandingRequests--;
        }

        private function serviceStatusResultHandler(e:ResultEvent):void {
            mServiceStatuses = new LookupCollection(ServiceStatus, ArrayCollection(e.result).toArray(), "serviceStatusCd");
            outstandingRequests--;
        }

        private function serviceSubStatusResultHandler(e:ResultEvent):void {
            mServiceSubStatuses = new LookupCollection(ServiceSubStatus, ArrayCollection(e.result).toArray(), "subStatusCd");
            outstandingRequests--;
        }

        private function fraudEventTypesHandler(e:ResultEvent):void {
            mFraudEventTypes = new LookupCollection(CompanyEventType, ArrayCollection(e.result).source, "eventTypeCode");
            outstandingRequests--;
        }

        private function faultHandler(e:FaultEvent):void {
            outstandingRequests--;
            logger.error("An error occured while loading a look up service: " + e.message);
            SAP.instance.dispatchEvent(new SAPEvent(SAPEvent.DATA_LOAD_FAULTED));
        }

        private function transactionTypeResultHandler(e:ResultEvent):void {
            mTransactionTypes = new LookupCollection(TransactionType, ArrayCollection(e.result).toArray(), "transactionTypeCd");
            outstandingRequests--;
        }

        private function lookupResultHandler(e:ResultEvent, token:Object):void {
            this[token['collection']] = new LookupCollection(token['type'], ArrayCollection(e.result).toArray(), token['lookupKey'], token['defaultPropertyName']);
            outstandingRequests--;
        }


        private var mTaxFilingDisplayNames:LookupCollection = null;
        private var mFundingModels:LookupCollection = null;
        private var mSourceSystems:LookupCollection = null;
        private var mServiceStatuses:LookupCollection = null;
        private var mServiceSubStatuses:LookupCollection = null;
        private var mTransactionTypes:LookupCollection = null;
        private var mPayrollStatus:LookupCollection = null;
        private var mAgencyList:LookupCollection = null;
        private var mFraudEventTypes:LookupCollection = null;
        private var mRTBJobList:LookupCollection = null;

        [Bindable("propertyChange")]
        public function get taxFilingDisplayNames():LookupCollection {
            return mTaxFilingDisplayNames;
        }

        [Bindable("propertyChange")]
        public function get agencyList():LookupCollection {
            return mAgencyList;
        }

        [Bindable("propertyChange")]
        public function get fundingModels():LookupCollection {
            return mFundingModels;
        }

        [Bindable("propertyChange")]
        public function get sourceSystems():LookupCollection {
            return mSourceSystems;
        }

        [Bindable("propertyChange")]
        public function get serviceStatuses():LookupCollection {
            return mServiceStatuses;
        }

        [Bindable("propertyChange")]
        public function get serviceSubStatuses():LookupCollection {
            return mServiceSubStatuses;
        }

        [Bindable("propertyChange")]
        public function get transactionTypes():LookupCollection {
            return mTransactionTypes;
        }

        [Bindable("propertyChange")]
        public function get payrollStatus():LookupCollection {
            return mPayrollStatus;
        }

        [Bindable("propertyChange")]
        public function get fraudEventTypes():LookupCollection {
            return mFraudEventTypes;
        }

        private function loadPayrollStatus():void {
            [ArrayElementType("psp.sap.model.PayrollStatus")] var tempArrayCollection:ArrayCollection = new ArrayCollection();
            var status:PayrollStatus;

            status = createPayrollStatus("Complete", "Complete");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("Canceled", "Canceled");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("DebitReturnedCanceled", "Debit Returned Canceled");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("DebitReturned", "Debit Returned");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("NSFCanceled", "NSF Canceled");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("OffloadedAll", "Offloaded All");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("OffloadedDebit", "Offloaded Debit");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("Pending", "Pending");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("WrittenOff", "Written Off");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("PendingReversals", "Pending Reversals");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("PendingAutoRedebit", "Pending Auto Redebit");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("AutoRedebitOffloaded", "Auto Redebit Offloaded");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("PendingRedebit", "Pending Redebit");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("RedebitOffloaded", "Redebit Offloaded");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("PendingWire", "Pending Wire");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("ReversalsOffloaded", "Reversals Offloaded");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("ReversalsFinished", "Reversals Finished");
            tempArrayCollection.addItem(status);

            status = createPayrollStatus("ReturnedTwice", "Returned Twice");
            tempArrayCollection.addItem(status);

            mPayrollStatus = new LookupCollection(PayrollStatus, tempArrayCollection.toArray(), "code");
        }

        public function createPayrollStatus(code:String, label:String):PayrollStatus {
            var status:PayrollStatus = new PayrollStatus();
            status.code = code;
            status.label = label;
            return status;
        }

        private function onEntityChange(e:EntityChangeEvent):void {
            if (e.entityType == EntityChangeEvent.SETTINGS) {
                loadData();
            }
        }

        private function onPageRefresh(e:EntityChangeEvent):void {
            loadData();
        }
    }
}

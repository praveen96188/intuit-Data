/**
 * User: dweinberg
 * Date: 10/11/12
 * Time: 11:19 AM
 */
package psp.sap.viewmodel {
    import mx.collections.ArrayCollection;
    import mx.rpc.Responder;
    import mx.rpc.events.ResultEvent;

    import psp.sap.application.SAP;
    import psp.sap.model.QuickBooksFileId;
    import psp.sap.validators.SAPValidators;

    public class CompanyQuickBooksInfoEditViewModel extends CompanyQuickBooksInfoViewModel {

        [ArrayElementType("psp.sap.model.QuickBooksFileId")]
        [Bindable] public var fileIds:ArrayCollection;

        private var mSelectedFileId:QuickBooksFileId;

        [Bindable]
        public var caseId:String;

        public var fileIdInitially:String;

        [Bindable]
        [BackingProperty (hasChanged=false)]
        public function get selectedFileId():QuickBooksFileId {
            return mSelectedFileId;
        }

        public function set selectedFileId(value:QuickBooksFileId):void {
            mSelectedFileId = value;
            if (value != null && value.fileId != null && value.fileId != "") {
                quickbooksInfo.fileId = value.fileId;
            }
        }

        override protected function loadModelData():void {
            super.loadModelData();
            loadCount = loadCount + 1;
            SAP.instance.companyService.getAvailableQuickBooksFileIds(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onFileIdsLoaded));
        }

        private function onFileIdsLoaded(e:ResultEvent):void {
            var tempFileIds:ArrayCollection = ArrayCollection(e.result);
            tempFileIds.addItemAt(new QuickBooksFileId(), 0);
            fileIds = tempFileIds;
        }

        override protected function initializeBackingProperties():void {
            super.initializeBackingProperties();
            validators.length = 0;
            validators.push(SAPValidators.createRequiredFieldValidator(quickbooksInfo, "coaFeeAccountName", true));
            validators.push(SAPValidators.createRequiredFieldValidator(quickbooksInfo, "coaSalesTaxAccountName", true));
            onFileIdChanged();
            fileIdInitially= quickbooksInfo.fileId;
        }

        public function onFileIdChanged():void {
            for each (var fileId:QuickBooksFileId in fileIds) {
                if (this.quickbooksInfo.fileId == fileId.fileId) {
                    selectedFileId = fileId;
                    return;
                }
            }
            selectedFileId = QuickBooksFileId(fileIds.getItemAt(0));

        }

        override protected function executeSave():void {
            SAP.instance.companyService.updateQuickbooksInfo(
                    companyKey.sourceSystemCd,
                    companyKey.companyId,
                    quickbooksInfo.coaFeeAccountName,
                    quickbooksInfo.coaSalesTaxAccountName,
                    quickbooksInfo.fileId, caseId,
                    createSaveResponder());
        }

       public  function clearVmpData():void{
            SAP.instance.companyService.deleteVMPData(
                   companyKey.companyId,
                   companyKey.sourceSystemCd,
                   createSaveResponder(vmpDeleteResponderOnSuccess));
        }

       public function vmpDeleteResponderOnSuccess(e:ResultEvent):void{
           saveMsg = "VMP data deleted successfully.";
       }

       public function isOnVmpService():Boolean{
           if (company == null){
               return false;
           }
         return  company.isVmp;
       }
    }
}

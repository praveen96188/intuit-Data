/**
 * Created by anandp233 on 2/11/14.
 */
package psp.sap.viewmodel {
import flash.events.Event;
import flash.events.TimerEvent;
import flash.net.FileReference;
import flash.utils.Timer;
import flash.utils.clearInterval;
import flash.utils.setInterval;
import mx.collections.ArrayCollection;
import mx.events.CloseEvent;
import mx.rpc.events.ResultEvent;
import psp.sap.application.SAP;
import psp.sap.application.enums.OperationsEnum;
import psp.sap.application.enums.RTBPageEnum;
import psp.sap.model.RTBJob;
import psp.sap.model.RTBAutomationJob;
import psp.sap.model.ReportJob;
import psp.sap.validators.SAPValidators;
import flash.net.URLRequest;
import flash.net.navigateToURL;
import flash.net.URLRequestMethod;
import mx.utils.URLUtil;
import mx.core.Application;
import psp.app.util.CookieUtil;
import psp.app.util.CommonUtil;
import mx.controls.Alert;

public class RTBViewModel extends CompositePartViewModel {
        public function RTBViewModel() {
            super();
            this.label = RTBPageEnum.RUN_THE_BUSINESS;
            validators.push(SAPValidators.createRequiredFieldValidator(this, "fileName"));
            validators.push(SAPValidators.createRequiredFieldValidator(this, "selectedRTBJob"));
            rtbJobViewModel = RTBJobViewModel(this.addNewPart(RTBJobViewModel));
        }

        [Bindable]
        [BackingProperty]
        public var queryConsoleUrl:String = "";

        [Bindable]
        [BackingProperty]
        public var fileName:String = "";
        [Bindable]
        [BackingProperty]
        public var description:String = "";
        [Bindable]
        [BackingProperty]
        public var currentFileUploaded:Boolean = false;
        [Bindable]
        [BackingProperty]
        public var processedLogs:String = "You will see processed logs for your job after execution is complete.";
        public var fileRef:FileReference = new FileReference();
        [ArrayElementType("psp.sap.model.RTBJob")]
        [Bindable]
        public var rtbJobList:ArrayCollection;
        [Bindable]
        public var rtbJobViewModel:RTBJobViewModel;
        [Bindable]
        [BackingProperty]
        private var mSelectedRTBJob:RTBJob;
        [Bindable]
        [BackingProperty]
        private var mSelectedAutomationJob:RTBAutomationJob;
        [ArrayElementType("psp.sap.model.RTBAutomationJob")]
        [Bindable]
        public var automationJobList:ArrayCollection;
        [Bindable]
        [BackingProperty]
        private var mSelectedReportJob:ReportJob;
        [ArrayElementType("psp.sap.model.ReportJob")]
        [Bindable]
        public var reportJobList:ArrayCollection;
        [Bindable]
        [BackingProperty]
        public var selectedDate:String="";
        [BackingProperty]
        public var reportName:String = "";
        private var timer;
        [ArrayElementType("String")]
        [Bindable]
        public var processMassCanFileList:ArrayCollection;
        [Bindable]
        [BackingProperty]
        private var mSelectedMassCnFile:String;
        [Bindable]
        [BackingProperty]
         public var isAutomateMassCanFeautureEnable:Boolean=false;
    public var urlRequest:URLRequest;

        override public function get hasChanged():Boolean {
            return currentFileUploaded;
        }
        
        public function launchQueryConsole():void {
       		var completeUrl:String = mx.core.Application.application.url;
        	var serverName:String = mx.utils.URLUtil.getServerName(completeUrl);

            CookieUtil.setCookie("C", SAP.instance.session.user.corpId, 30, "/", serverName);
            CookieUtil.setCookie("A", SAP.instance.session.user.authorizationToken, 30, "/", serverName);

            if (queryConsoleUrl != "")
            {
                serverName = queryConsoleUrl;
            }
        	var url:String = "https://" + serverName + "/queryengine";


			var request:URLRequest = new URLRequest();
			request.method = URLRequestMethod.GET;
			
			request.url = url;
            navigateToURL(request, '_blank');
        }


        [Bindable]
        public function get selectedRTBJob():RTBJob {
            return mSelectedRTBJob;
        }

        [Bindable]
        public function set selectedRTBJob(value:RTBJob):void {
            if (value != mSelectedRTBJob && value != null) {
                mSelectedRTBJob = value;
                this.refresh();
            }
        }

        [Bindable]
        public function get selectedRTBJobName():String {
            return (selectedRTBJob != null) ? selectedRTBJob.jobName : null;
        }


        [Bindable]
        public function get selectedJob():RTBAutomationJob {
            return mSelectedAutomationJob;
        }


        [Bindable]
        public function set selectedJob(value:RTBAutomationJob):void {
            if (value != mSelectedAutomationJob && value != null) {
                mSelectedAutomationJob = value;
                this.refresh();
            }
        }

        [Bindable]
        public function get selectedJobName():String {
            return (selectedJob != null) ? selectedJob.jobName : null;
        }

        [Bindable]
        public function get selectedReportJob():ReportJob {
            return mSelectedReportJob;
        }


        [Bindable]
        public function set selectedReportJob(value:ReportJob):void {
            if (value != mSelectedReportJob && value != null) {
                mSelectedReportJob = value;
                this.refresh();
            }
        }

        [Bindable]
        public function get selectedReportJobName():String {
            return (selectedReportJob != null) ? selectedReportJob.reportName : null;
        }

        override protected function loadModelData():void {
            //SAP.instance.rtbService.getAutoMassCanFlag(createLoadModelDataResponder(onLoadFilesSucceeded));
            if(SAP.canPerformOperation(OperationsEnum.REPORT_FILE_DOWNLOAD)){

                SAP.instance.reportService.getReportList(createLoadModelDataResponder(onReportDataLoadSucceeded));
            }
            if(SAP.canPerformOperation(OperationsEnum.EXECUTE_RTB_AUTOMATION_JOB)){
                if(automationJobList == null){
                    SAP.instance.rtbAutomationService.getAutomationJobList(createLoadModelDataResponder(onDataLoadSucceeded));
                }
            }
            if(SAP.canPerformOperation(OperationsEnum.EXECUTE_RTB_JOB)){
                if(rtbJobList ==null){
                    SAP.instance.rtbService.getRTBJobList(createLoadModelDataResponder(onLoadSucceeded));
                }
                SAP.instance.rtbService.getQueryConsoleIksUrl(createLoadModelDataResponder(onUrlDataLoadSucceeded));
                if (processMassCanFileList == null) {
                    SAP.instance.rtbService.getProcessMassCanFileList(createLoadModelDataResponder(onLoadFilesSucceeded));
                }
            }

        }

    override protected function executeSave():void {
        if(mSelectedReportJob != null) {
            var month:String = selectedDate.toString().split("/")[0];
            var day:String = selectedDate.toString().split("/")[1];
            var year:String = selectedDate.toString().split("/")[2];
            urlRequest = new URLRequest("DownloadReport?reportType="+mSelectedReportJob.shortDescription+"&date="+selectedDate.toString()+"&token="+SAP.instance.session.user.authorizationToken);
            //SAP.instance.reportService.downloadReport(mSelectedReportJob.shortDescription,selectedDate.toString(), createSaveResponder(onReportSuccess, onReportFailed));
            mSelectedReportJob = null;
            if(CommonUtil.isDTApp()) {
                CommonUtil.downloadFromSAPAppURL(urlRequest.url, this, "AML_Data_File_" + year + month + day + ".txt");
            } else {
                navigateToURL(urlRequest);
            }

            SAP.instance.hideProgress();
        } else {
            if(mSelectedMassCnFile!=null){
                SAP.instance.rtbService.executeMassCancellation(mSelectedMassCnFile,createSaveResponder(onSuccess, onFailed));
                this.refresh();
            }else
            {
                SAP.instance.rtbService.uploadAndExecuteRTBJob(mSelectedRTBJob.jobName, fileRef.data,
                        createSaveResponder(onSuccess, onFailed));
                currentFileUploaded = false;
            }
        }
    }
        protected function onLoadSucceeded(e:ResultEvent):void {
            rtbJobList = e.result as ArrayCollection;
        }
        protected function onLoadFilesSucceeded(e:ResultEvent):void {
           // isAutomateMassCanFeautureEnable = e.result as Boolean;
            processMassCanFileList = e.result as ArrayCollection;
        }

    protected function onSuccess(e:ResultEvent):void {
        processedLogs = e.result as String;
        SAP.instance.rtbService.getProcessMassCanFileList(createLoadModelDataResponder(onLoadFilesSucceeded));
        saveMsg = "Your job executed successfully.";
        SAP.instance.hideProgress();
    }

        protected function onFailed(e:ResultEvent):void {
            saveFaulted = true;
            saveMsg = "Error while executing your job : " + e.toString();
            SAP.instance.hideProgress();
        }

        protected function onDataLoadSucceeded(e:ResultEvent):void {
            automationJobList = e.result as ArrayCollection;
        }

        protected function onReportDataLoadSucceeded(e:ResultEvent):void {
            reportJobList = e.result as ArrayCollection;
        }

        protected  function onUrlDataLoadSucceeded(e:ResultEvent):void {
            queryConsoleUrl = e.result as String;
        }

    /*
        protected function onDeleteSuccess(e:ResultEvent):void {
            saveFaulted = false;
            saveMsg = "";
            SAP.instance.hideProgress();
        }

        protected function onDeleteFailure(e:ResultEvent):void {
            saveFaulted = true;
            saveMsg = "";
            SAP.instance.hideProgress();
        }

        protected function onReportSuccess(e:ResultEvent):void {
            var reportFileName:String = e.result as String;
            reportName = reportFileName;
            urlRequest = new URLRequest("DownloadReport?fileName=" + reportFileName + "&token=" + SAP.instance.session.user.authorizationToken);
            Alert.show("Download "+reportFileName,
                    "Download Confirmation",
                    Alert.OK | Alert.CANCEL,
                    null,
                    confirmVerifyHandler,
                    null,
                    Alert.OK);
        }

    private function confirmVerifyHandler(event:CloseEvent):void {
        if (event.detail == Alert.OK) {
            if(CommonUtil.isDTApp()) {
                CommonUtil.downloadFromSAPURL(urlRequest.url, reportName);
            } else {
                navigateToURL(urlRequest);
            }

            saveMsg = "Your report downloaded successfully.";
            SAP.instance.hideProgress();
        } else {
            delayDelOperation();
        }
    }

        protected function delayDelOperation():void{
            SAP.instance.reportService.deleteGeneratedReport(reportName,createSaveResponder(onDeleteSuccess,onDeleteFailure));
        }

        protected function onReportFailed(e:ResultEvent):void {
            saveFaulted = true;
            saveMsg = "Error while downloading the report : " + e.toString();
            SAP.instance.hideProgress();
        }
        */
        [Bindable]
        public function get selectedMassCnFile():String {
            return mSelectedMassCnFile;
         }
        [Bindable]
        public function set selectedMassCnFile(value:String):void {
            mSelectedMassCnFile = value;
        }
}
}

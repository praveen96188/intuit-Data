package psp.app.util {


import flash.utils.clearInterval;
import flash.utils.setInterval;
import flash.utils.setTimeout;

import mx.core.Application;

    import flash.external.ExternalInterface;
    import flash.net.FileReference;
    import flash.net.URLRequest;
import flash.events.IOErrorEvent;
import flash.events.Event;

import psp.sap.viewmodel.AbstractPartViewModel;

public class CommonUtil {

        public static var DTApp:Boolean;
        private static var domainURL:String;
    private static var abstractPart:AbstractPartViewModel;
    private static var timer;

        public static function isDTApp() {
            return DTApp;
        }

        public static function checkIfDTApp() {
            if(Application.application.parameters.serverName != null) {
                DTApp = true;
                domainURL = Application.application.parameters.serverName;
            } else {
                DTApp = false;
            }
        }

        public static function showPrintWindow(query, filename="Print") {
            downloadFromURL(domainURL + "Print" + query, filename);
        }

        public static function downloadFromSAPURL(query, filename="Print") {
            downloadFromURL(domainURL + query, filename);
        }

        private static function downloadFromURL(downloadURL, filename) {
            var fileReference:FileReference = new FileReference();
            var urlRequest:URLRequest = new URLRequest(downloadURL);
            fileReference.download(urlRequest, filename);
        }

    public static function downloadFromSAPAppURL(query, abstractPartViewModel, filename="Print") {
        downloadFileURL(domainURL + query, abstractPartViewModel, filename);
    }

    private static function downloadFileURL(downloadURL, abstractPartViewModel, filename) {
        abstractPart = abstractPartViewModel;
        var urlRequest:URLRequest = new URLRequest(downloadURL);
        var fileReference:FileReference = new FileReference();
        fileReference.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
        fileReference.addEventListener(Event.COMPLETE, completeHandler);
        fileReference.download(urlRequest, filename);
    }

    private static function ioErrorHandler(event:IOErrorEvent):void {
        abstractPart.saveFaulted = true;
        abstractPart.saveMsg = "Error while downloading the file";
        timer = setInterval(hideMsg, 5000);
    }

    private static function completeHandler(event:Event):void {
        abstractPart.saveMsg = "Your report downloaded successfully";
        timer = setInterval(hideMsg, 5000);
    }

    private static function hideMsg() {
        flash.utils.clearInterval(timer);
        abstractPart.refresh();
    }

        public static function openForgotPasswordURL(query) {
            ExternalInterface.call("enableNavigateInSystemBrowser");
            ExternalInterface.call("window.open", query, '_blank');
            ExternalInterface.call("disableNavigateInSystemBrowser");
        }
    }
}
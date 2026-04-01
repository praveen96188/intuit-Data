/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/17/12
 * Time: 10:53 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.service {

    import mx.rpc.IResponder;

    import psp.sap.service.interfaces.IPSPService;

    public interface IBillingHistoryService extends IPSPService {
        function findBillingHistoryByDate(companyId:String, sourceSystemCd:String, responder:IResponder):void;

        function findBillingHistoryBySubscriptionAndDate(companyId:String, sourceSystemCd:String, subscriptionNumber:String, fromDate:Date, toDate:Date, responder:IResponder):void;

        function findInvoiceDetails(companyId:String, sourceSystemCd:String,subscriptionNumber:String, billPOID: String, responder:IResponder):void;

        function findBillingDetails(companyId:String, sourceSystemCd:String, billDate:Date,viewAll:Boolean, responder:IResponder):void;

        function findSymphonySubscriptionNumbers(companyId:String, sourceSystemCd:String, responder:IResponder):void;

    }
}

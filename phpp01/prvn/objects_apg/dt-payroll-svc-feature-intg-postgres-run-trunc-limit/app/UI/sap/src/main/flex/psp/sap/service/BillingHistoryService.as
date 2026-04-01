/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 8/17/12
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.service {

    import mx.rpc.AsyncToken;
    import mx.rpc.IResponder;
    import mx.rpc.remoting.RemoteObject;


    public class BillingHistoryService extends PSPService implements IBillingHistoryService {

        public function BillingHistoryService() {
            super();
            remoteObjectPool = new RemoteObjectPool("billinghistoryservice");
        }

        public function get billingHistoryRemoteService():RemoteObject {
            return remoteObjectPool.nextAvailable();
        }

        public function findBillingHistoryByDate(companyId:String, sourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(billingHistoryRemoteService.findBillingHistoryByDate(companyId, sourceSystemCd));
            remoteToken.addResponder(responder);
        }

        public function findBillingDetails(companyId:String, sourceSystemCd:String, billDate:Date, viewAll:Boolean, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(billingHistoryRemoteService.findBillingDetails(companyId, sourceSystemCd, billDate, viewAll));
            remoteToken.addResponder(responder);
        }

        public function findInvoiceDetails(companyId:String, sourceSystemCd:String,subscriptionNumber:String, billPOID:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(billingHistoryRemoteService.findInvoiceDetails(companyId, sourceSystemCd,subscriptionNumber, billPOID));
            remoteToken.addResponder(responder);
        }

        public function findSymphonySubscriptionNumbers(companyId:String, sourceSystemCd:String, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(billingHistoryRemoteService.findSymphonySubscriptionNumbers(companyId, sourceSystemCd));
            remoteToken.addResponder(responder);
        }

        public function findBillingHistoryBySubscriptionAndDate(companyId:String, sourceSystemCd:String, subscriptionNumber:String, fromDate:Date, toDate:Date, responder:IResponder):void {
            var remoteToken:AsyncToken =
                    AsyncToken(billingHistoryRemoteService.findBillingHistoryBySubscriptionAndDate(companyId, sourceSystemCd, subscriptionNumber, fromDate, toDate));
            remoteToken.addResponder(responder);
        }
    }


}

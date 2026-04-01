/**
 * Created by IntelliJ IDEA.
 * User: dweinberg
 * Date: 1/29/11
 * Time: 8:08 PM
 * To change this template use File | Settings | File Templates.
 */
package psp.sap.model {
    [Bindable]
    [RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQTDYTDs")]
    public class QTDYTDs {
        public var qtdLiability:Number=0;
        public var qtdWages:Number=0;
        public var ytdLiability:Number=0;
        public var ytdWages:Number=0;
        public var taxBalance:Number=0;
    }
}

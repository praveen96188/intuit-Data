package psp.sap.model
{
import mx.collections.ArrayCollection;

[Bindable]
[RemoteClass(alias="com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPriceTypeOffer")]
public class PriceTypeOffer
{

    public var priceType:String;
    [ArrayElementType("psp.sap.model.Offer")]
    public var offerList:ArrayCollection;

}
}
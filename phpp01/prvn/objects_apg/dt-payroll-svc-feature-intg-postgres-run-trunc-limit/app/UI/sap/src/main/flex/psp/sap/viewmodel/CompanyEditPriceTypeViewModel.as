package psp.sap.viewmodel {
import mx.collections.ArrayCollection;
import mx.controls.Alert;
import mx.rpc.events.ResultEvent;
import psp.sap.application.SAP;
import psp.sap.model.Offer;
import psp.sap.model.PriceTypeOffer;
import psp.sap.validators.SAPValidators;

public class CompanyEditPriceTypeViewModel extends AbstractPartViewModel {

    [Bindable] [BackingProperty(context=true)] public var isAssisted:Boolean = false;

    [Bindable] [BackingProperty] public var itemNumber:String;

    [Bindable] public var mPriceTypes:ArrayCollection;

    [Bindable] public var priceTypes:ArrayCollection;

    [ArrayElementType("psp.sap.model.Offer")]
    [Bindable] [BackingProperty] public var assistedOffers:ArrayCollection;

    [Bindable] [BackingProperty] public var priceType:String;
    [Bindable]  public var offer:Offer;

    [Bindable] public var showSaveButtons:Boolean = true;
    [Bindable] public var priceTypeOffers:ArrayCollection;

    private var localPriceType:String;

    override protected function loadModelData():void {
        loadCount = 0;

        if (companyKey != null) {
            loadCount = 4;
            SAP.instance.companyService.getAvailablePriceTypesByCompanyKey(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onPriceTypesLoaded));
            SAP.instance.companyService.getPriceType(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onPriceTypeLoaded));
            SAP.instance.companyService.getOfferByCompanyKey(companyKey, createLoadModelDataResponder(onSpecialOfferLoaded));
            SAP.instance.companyService.getAssistedOffer(companyKey.sourceSystemCd, companyKey.companyId, createLoadModelDataResponder(onOfferLoaded));
        } else if (itemNumber != null) {
            loadCount = 2;
            //SAP.instance.companyService.getAvailablePriceTypesByItemNumber(itemNumber, createLoadModelDataResponder(onPriceTypesLoaded));
            SAP.instance.companyService.getOfferByItemNumber(itemNumber, createLoadModelDataResponder(onSpecialOfferLoaded));
            SAP.instance.companyService.getOfferByPriceType(itemNumber, createLoadModelDataResponder(onPriceTypeOfferLoaded));

        }
    }
    private function onPriceTypeOfferLoaded(e:ResultEvent):void {
        if(e.result) {
            priceTypeOffers = ArrayCollection(e.result);
            mPriceTypes = new ArrayCollection();
            assistedOffers=ArrayCollection(priceTypeOffers.getItemAt(0).offerList);
            for(var i=0;i<priceTypeOffers.length;i++){
                if(priceTypeOffers.getItemAt(i)!=null){
                   mPriceTypes.addItem(priceTypeOffers.getItemAt(i).priceType);
                }

            }

            priceTypes = ArrayCollection(mPriceTypes);
            priceType = String(priceTypes.getItemAt(0));

        }
    }

    private function onPriceTypesLoaded(e:ResultEvent):void {
        if (e.result) {
            priceTypes = ArrayCollection(e.result);
        }
    }

    private function onSpecialOfferLoaded(e:ResultEvent):void {
        if (e.result) {
            assistedOffers=ArrayCollection(e.result);
        }
    }

    private function onPriceTypeLoaded(e:ResultEvent):void {
        if (e.result) {
            localPriceType = String(e.result);
        }
    }

    private function onOfferLoaded(e:ResultEvent):void {
        if (e.result) {
            offer = getDisplayOffer(Offer(e.result).offerCd);
        } else {
            offer = Offer(assistedOffers.getItemAt(0));
        }
    }

    //convert the "real" offer into one that we use on this view
    private function getDisplayOffer(offerCode:String):Offer {
        for each (var offer:Offer in assistedOffers) {
            if (offer.offerCd == offerCode) {
                return offer;
            }
        }
        //else return the empty one
        return Offer(assistedOffers.getItemAt(0));
    }


    override protected function initializeBackingProperties():void {
        validators.length = 0;

        validators.push(SAPValidators.createRequiredFieldValidator(this, "priceType", isAssisted));


        if (localPriceType == null) {
            priceType = String(priceTypes.getItemAt(0));
        } else {
            priceType = localPriceType;
        }

        if (companyKey == null) {
            offer = Offer(assistedOffers.getItemAt(0));
        }


    }

    override protected function executeSave():void {
        SAP.instance.companyService.setAssistedPriceTypeAndOffer(companyKey.sourceSystemCd, companyKey.companyId, priceType, offer.offerCd, createSaveResponder());
    }

}
}
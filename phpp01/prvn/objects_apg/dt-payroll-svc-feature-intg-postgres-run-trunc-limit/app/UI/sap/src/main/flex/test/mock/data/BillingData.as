package test.mock.data
{
    import mx.collections.ArrayCollection;

    import psp.sap.model.FeeDetail;
    import psp.sap.model.Offer;

    public class BillingData
	{
		public static function getFeeDetail():FeeDetail {
			var feeDetail:FeeDetail = new FeeDetail();
			feeDetail.currentUnitPrice = 10;
			feeDetail.units = 2;
			feeDetail.totalPrice = 20;
			feeDetail.isPayrollFee = false;
			
			return feeDetail;
		}

        public static function getOffers():ArrayCollection {
            var offers:ArrayCollection = new ArrayCollection();
            for(var i:int; i < 5; i++){
                var offer:Offer = new Offer();
                offer.offerCd = "cd" + i;
                offers.addItem(offer);
            }

            return offers;
        }

	}
}
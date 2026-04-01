package com.intuit.sbd.payroll.psp.gateways.amo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/23/13
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class AMOWSMockGateway implements IAMOWSGateway {

    private static Map<String, GetCustomerAssetResponseTypeDTO> mGetCustomerAssetResponseTypeDTO = new HashMap<String, GetCustomerAssetResponseTypeDTO>();

    public static void clear() {
        mGetCustomerAssetResponseTypeDTO.clear();
    }

    public static void setGetCustomerAssetResponseTypeDTO(GetCustomerAssetResponseTypeDTO pGetCustomerAssetResponseTypeDTO) {
        mGetCustomerAssetResponseTypeDTO.put(pGetCustomerAssetResponseTypeDTO.getLicenseNumber() + pGetCustomerAssetResponseTypeDTO.getEntitlementOfferingCode(), pGetCustomerAssetResponseTypeDTO);
    }

    public GetCustomerAssetResponseTypeDTO getCustomerAsset(String pLicenseNumber, String pEntitlementOfferingCode, IAMOGatewayListener pListener) throws Exception {
        return mGetCustomerAssetResponseTypeDTO.get(pLicenseNumber + pEntitlementOfferingCode);
    }

}

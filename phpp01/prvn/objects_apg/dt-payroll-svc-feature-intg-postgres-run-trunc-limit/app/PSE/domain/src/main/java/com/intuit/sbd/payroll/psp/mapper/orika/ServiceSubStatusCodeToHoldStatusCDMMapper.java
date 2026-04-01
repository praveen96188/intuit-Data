package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.company.model.HoldStatusCDM;
import com.intuit.payroll.api.company.model.HoldStatusType;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

/**
 * Map ServiceSubStatusCode(psp) to HoldStatusCDM(payroll)
 *
 * @author dchoudhary1
 */
@Component
public class ServiceSubStatusCodeToHoldStatusCDMMapper extends BeanMapper<ServiceSubStatusCode, HoldStatusCDM> {


    @Override
    public void mapAtoB(ServiceSubStatusCode serviceSubStatusCode,
                        HoldStatusCDM holdStatusCDM,
                        MappingContext mappingContext) {
        switch (serviceSubStatusCode) {
            case AchRejectOther:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_NSF);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.AchRejectOther.toString());
                break;
            case AchRejectR1R9:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_NSF);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.AchRejectR1R9.toString());
                break;
            case Fraud:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_FRAUD);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.Fraud.toString());
                break;
            case IntuitCollections:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_NSF);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.IntuitCollections.toString());
                break;
            case RiskAssessment:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_NSF);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.RiskAssessment.toString());
                break;
            case RiskCollections:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_NSF);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.RiskCollections.toString());
                break;
            case AMLHold:
                holdStatusCDM.setHoldName(HoldStatusType.PAYMENT_FRAUD);
                holdStatusCDM.setHoldValue(ServiceSubStatusCode.AMLHold.toString());
                break;
            default:
                holdStatusCDM.setHoldName(HoldStatusType.OTHER_HOLD);
                holdStatusCDM.setHoldValue(serviceSubStatusCode.toString());
        }
    }

    @Override
    public void directFieldToFieldMapping() {

        // TODO Auto-generated method stub

    }
}

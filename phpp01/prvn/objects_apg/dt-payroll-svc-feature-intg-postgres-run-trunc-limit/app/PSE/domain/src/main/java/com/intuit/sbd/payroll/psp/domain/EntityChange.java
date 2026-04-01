package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Query;

import java.util.List;

/**
 * Hand-written business logic
 */
public class EntityChange extends BaseEntityChange {
    public static String OldEinKeyName="Entity_Chng_OldEIN";
    public static String NewEinKeyName="Entity_Chng_NewEIN";

    /**
     * Default constructor.
     */
    public EntityChange() {
        super();
    }

    /**
     * If the company's current EIN is a result of an Entity change,  return the Entity Change object
     * otherwise return null
     */
    public static EntityChange findMostRecentEntityChangeForCompany(Company pCompany) {
        Query<EntityChange> einQuery = new Query<EntityChange>();
        List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntityChange.NewEinKeyName, pCompany.getFedTaxId());
        einQuery.Where(EntityChange.Company().equalTo(pCompany).And(EntityChange.NewEinEnc().in(einEncList)));
        DomainEntitySet<EntityChange> entityChanges = Application.find(EntityChange.class, einQuery.OrderBy(EntityChange.EffectiveDate().Descending()));
        if (entityChanges != null && entityChanges.size() > 0) {
            return entityChanges.get(0);
        } else {
            return null;
        }
    }

    /**
     * If the company's current EIN is a result of an Entity change which is not to remove an erroneous EIN,
     * return the Entity Change object
     * otherwise return null
     */
    public static EntityChange findMostRecentEntityChangeForCompanyWithoutError(Company pCompany) {
        Query<EntityChange> einQuery = new Query<EntityChange>();
        List<String> einEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntityChange.NewEinKeyName, pCompany.getFedTaxId());
        einQuery.Where(EntityChange.Company().equalTo(pCompany)
                .And(EntityChange.NewEinEnc().in(einEncList))
                .And(EntityChange.IsError().equalTo(false)));

        DomainEntitySet<EntityChange> entityChanges = Application.find(EntityChange.class, einQuery.OrderBy(EntityChange.EffectiveDate().Descending()));
        if (entityChanges != null && entityChanges.size() > 0) {
            return entityChanges.get(0);
        } else {
            return null;
        }
    }

    /**
     * Get Entity Change History corresponding to the passed oldEin, New RIn and Company objects
     */
    public static EntityChange findEntityChange(Company pCompany, String pOldEin, String pNewEin) {
        Query<EntityChange> einQuery = new Query<EntityChange>();
        List<String> newEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntityChange.NewEinKeyName, pNewEin);
        List<String> oldEinEncList = EncryptionUtils.deterministicEncryptWithAllKeys(EntityChange.OldEinKeyName, pOldEin);
        einQuery.Where(EntityChange.Company().equalTo(pCompany).And(EntityChange.OldEinEnc().in(oldEinEncList).And(EntityChange.NewEinEnc().in(newEinEncList))));
        DomainEntitySet<EntityChange> entityChanges = Application.find(EntityChange.class, einQuery.OrderBy(EntityChange.EffectiveDate().Descending()));
        if (entityChanges != null && entityChanges.size() > 0) {
            return entityChanges.get(0);
        } else {
            return null;
        }
    }

    public void setOldEIN(String pOldEIN) {
        super.setOldEinEnc(EncryptionUtils.deterministicEncrypt(OldEinKeyName,pOldEIN));
    }


    public String getOldEIN() {
        return EncryptionUtils.deterministicDecrypt(OldEinKeyName,getOldEinEnc());
    }

    public void setNewEIN(String pNewEIN) {
        super.setNewEinEnc(EncryptionUtils.deterministicEncrypt(NewEinKeyName,pNewEIN));
    }


    public String getNewEIN() {
        return EncryptionUtils.deterministicDecrypt(NewEinKeyName,getNewEinEnc());
    }
}
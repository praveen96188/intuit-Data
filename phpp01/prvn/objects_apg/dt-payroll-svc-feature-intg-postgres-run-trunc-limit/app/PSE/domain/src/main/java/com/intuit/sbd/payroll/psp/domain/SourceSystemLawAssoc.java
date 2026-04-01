package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.HashMap;

/**
 * Hand-written business logic
 */
public class SourceSystemLawAssoc extends BaseSourceSystemLawAssoc {

    private static HashMap<NaturalKey, SpcfUniqueId> naturalKeyToPrimaryKeyMap = null;

    public static synchronized HashMap<NaturalKey, SpcfUniqueId> getNaturalKeyToPrimaryKeyMap() {
        if (naturalKeyToPrimaryKeyMap == null) {
            naturalKeyToPrimaryKeyMap = new HashMap<NaturalKey, SpcfUniqueId>();
            DomainEntitySet<SourceSystemLawAssoc> sourceSystemLawAssocs = Application.find(SourceSystemLawAssoc.class);

            for (SourceSystemLawAssoc currSSLA : sourceSystemLawAssocs) {
                NaturalKey naturalKey = new NaturalKey(SourceSystemLawAssoc.class, currSSLA.getSourceSystem().getSourceSystemCd(), currSSLA.getSourceLawCode());
                naturalKeyToPrimaryKeyMap.put(naturalKey, currSSLA.getId());
            }
        }
        return naturalKeyToPrimaryKeyMap;
    }

    private static HashMap<NaturalKey, SpcfUniqueId> sourceIdNaturalKeyToPrimaryKeyMap = null;

    public static synchronized HashMap<NaturalKey, SpcfUniqueId> getSourceIdNaturalKeyToPrimaryKeyMap() {
        if (sourceIdNaturalKeyToPrimaryKeyMap == null) {
            sourceIdNaturalKeyToPrimaryKeyMap = new HashMap<NaturalKey, SpcfUniqueId>();
            DomainEntitySet<SourceSystemLawAssoc> sourceSystemLawAssocs = Application.find(SourceSystemLawAssoc.class, new Query<SourceSystemLawAssoc>().EagerLoad(SourceSystemLawAssoc.Law()));

            for (SourceSystemLawAssoc currSSLA : sourceSystemLawAssocs) {
                NaturalKey naturalKey = new NaturalKey(SourceSystemLawAssoc.class, currSSLA.getSourceSystem().getSourceSystemCd(), currSSLA.getLaw());
                sourceIdNaturalKeyToPrimaryKeyMap.put(naturalKey, currSSLA.getId());
            }
        }
        return sourceIdNaturalKeyToPrimaryKeyMap;
    }


    /**
	 * Default constructor.
	 */
	public SourceSystemLawAssoc() {
		super();
	}

    public static Law findLawBySourceSystemAndSourceId(SourceSystemCode pSourceSystemCode, String pSourceId) {
        SourceSystemLawAssoc foundLawAssoc = null;

        NaturalKey naturalKey = new NaturalKey(SourceSystemLawAssoc.class, pSourceSystemCode, pSourceId);
        SpcfUniqueId primaryKey = getNaturalKeyToPrimaryKeyMap().get(naturalKey);

        if (primaryKey != null) {
            foundLawAssoc = Application.findById(SourceSystemLawAssoc.class, primaryKey);
        } else {
            throw new RuntimeException(pSourceSystemCode + " : " + pSourceId + " source system law association was not found.");
        }

        return foundLawAssoc.getLaw();
    }

    public static String findSourceIdBySourceSystemAndLaw(SourceSystemCode pSourceSystemCode, Law pLaw) {
        SourceSystemLawAssoc foundLawAssoc = null;

        NaturalKey naturalKey = new NaturalKey(SourceSystemLawAssoc.class, pSourceSystemCode, pLaw);
        SpcfUniqueId primaryKey = getSourceIdNaturalKeyToPrimaryKeyMap().get(naturalKey);

        if (primaryKey != null) {
            foundLawAssoc = Application.findById(SourceSystemLawAssoc.class, primaryKey);
        } else {
            throw new RuntimeException(pSourceSystemCode + " : " + pLaw.getLawId() + " source system law association was not found.");
        }

        return foundLawAssoc.getSourceLawCode();
    }

}
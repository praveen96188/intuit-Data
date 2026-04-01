package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Hand-written business logic
 */
public class OwnershipType extends BaseOwnershipType {

	static String cacheKey = "CachedOwnershipTypes";

	private static SpcfLogger logger = Application.getLogger(OwnershipType.class);

	/**
	 * Default constructor.
	 */
	public OwnershipType()
	{
		super();
	}

	public static String findOwnership(String ownership) {
		OwnershipType ownershipType = findOwnershipType(ownership);
		if(Objects.nonNull(ownershipType)) {
			return ownershipType.getOwnership();
		}
		return null;
	}

	/**
	 * Utility function to return OwnershipType
	 *
	 * @param ownershipName String representation of ownership
	 * @return OwnershipType object
	 * @see OwnershipType
	 */
	public static OwnershipType findOwnershipType(String ownershipName) {

		DomainEntitySet<OwnershipType> ownershipTypes;
		if (Application.getSessionCache().isDataObjectCollectionCached(OwnershipType.class, cacheKey)) {
			ownershipTypes = Application.getSessionCache().getDataObjectCollection(OwnershipType.class, cacheKey);
		} else {
			ownershipTypes = Application.findObjects(OwnershipType.class);
			Application.getSessionCache().addDataObjectCollection(OwnershipType.class, cacheKey, ownershipTypes);
		}
		ownershipTypes = ownershipTypes.find(OwnershipType.Ownership().equalTo(ownershipName));
		//
		if (ownershipTypes.size() != 1) {
			// TODO: Setup alert in splunk and analyse later for a custom exception to be thrown.
			// throw new RuntimeException("More than one OwnershipType is found for Ownership:" + ownershipName);
			logger.error("Zero or more than one ownershipType objects found for ownership: " + ownershipName);
			return null;
		}
		return ownershipTypes.getFirst();
	}
}
package com.intuit.spc.foundations.portability;

/**
 * A portable interface for accessing SpcfIncident members.
 *
 */
public interface ISpcfIncident {

	/**
     *  A string that is used by computers or humans to identify that
	 *  a particular incident has occurred, allowing them to take
	 *  prescribed action on that id. 
	 *
	 *  For example, there might be a TOPS policy saying that
	 *  a "1b5" incident should result in the lead getting paged. 
	 */
	String getOperationalId();

	//TBD - System.String UniqueId {get;} 
	//to uniquely identify this incident across space and time

	/**
     *  Gets a detailed message that describes the incident 
	 *  (which may be null)
	 */
	String getMessage();

	/**
     *  Gets the priority associated with this incident
	 */
	SpcfPriority getPriority();
}

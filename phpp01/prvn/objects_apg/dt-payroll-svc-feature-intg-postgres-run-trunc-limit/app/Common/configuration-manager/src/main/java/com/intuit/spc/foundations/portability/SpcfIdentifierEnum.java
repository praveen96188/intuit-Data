package com.intuit.spc.foundations.portability;

/**
 * The base abstract class for the error enum classes for the projects
 */
public abstract class SpcfIdentifierEnum {
	
	/**
	 * the id encapuslated by the enum instance
	 */
	private int mId;

	/**
	 * the id encapuslated by the enum instance
	 */
	public int getId()
	{
		return mId;
	}

	/**
	 * the one and only public constructor
	 * @param id the id for this error instance
	 */
	protected SpcfIdentifierEnum (int id) { mId = id; }

	/**
	 * we're serious about that one and only public constructor thing
	 */
	private SpcfIdentifierEnum() {}
}

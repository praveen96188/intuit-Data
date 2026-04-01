package com.intuit.spc.foundations.portability;

/**
 * The base class for the Foundations architecture which contains 
 * the Foundations architecture ID.
 */
public abstract class SpcfArchitectureException extends SpcfRuntimeException {

	private static final long serialVersionUID = 5488048048149393845L;
	private static final int ArchitectureIdForDefiningTypeConst = 100;
	
	/**
	 * Class constructor
	 */
	public SpcfArchitectureException() {}

	/**
	 * Class constructor
	 * @param message An exception message
	 */
	public SpcfArchitectureException(String message)
	{
		super(message);
	}

	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 */
	public SpcfArchitectureException(String message, Throwable e)
	{
		super(message, e);
	}

	/**
	 * Class constructor
	 * @param e A throwable exception
	 */
	public SpcfArchitectureException(Throwable e)
	{
		super(e);
	}
	
	/**
	 * Class constructor
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArchitectureException(int applicationId, int architectureId, int moduleId) 
	{
		super(applicationId, architectureId, moduleId);
	}

	/**
	 * Class constructor
	 * @param message An exception message
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArchitectureException(String message, int applicationId, int architectureId, int moduleId)
	{
		super(message, applicationId, architectureId, moduleId);
	}

	/**
	 * Class constructor
	 * @param message An exception message
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArchitectureException(String message, Throwable e, int applicationId, int architectureId, int moduleId)
	{
		super(message, e, applicationId, architectureId, moduleId);
	}

	/**
	 * Class constructor
	 * @param e A throwable exception
	 * @param applicationId Application ID override. Use zero to avoid override.
	 * @param architectureId Architecture ID override. Use zero to avoid override.
	 * @param moduleId	Module ID override. Use zero to avoid override
	 */
	public SpcfArchitectureException(Throwable e, int applicationId, int architectureId, int moduleId)
	{
		super(e, applicationId, architectureId, moduleId);
	}

	/**
	 * Your SpcfXXXModule must add a unique entry to the 
	 * SpcfModuleEnum and return it when it overrides this method. This
	 * is how we ensure our modules are using unique ids in a type safe
	 * manner.
	 */
	protected abstract SpcfArchitectureModuleEnum getSpcfArchitectureModule();

	/**
	 * Override the generic, error prone int, to call the typesafe
	 * SpcfModule property we're going to make everyone implement 
	 */
	protected int getModuleIdForDefiningType()
	{
		return getSpcfArchitectureModule().getId();
	}

	/**
	 * Provide our ArchitectureId, which is unique to Intuit - as 
	 * controlled here: 
	 * https://www.quickbase.com/db/bas7j8774?a=q&amp;qid=1
	 */
	protected int getArchitectureIdForDefiningType()
	{
		return ArchitectureIdForDefiningTypeConst;
	}
}

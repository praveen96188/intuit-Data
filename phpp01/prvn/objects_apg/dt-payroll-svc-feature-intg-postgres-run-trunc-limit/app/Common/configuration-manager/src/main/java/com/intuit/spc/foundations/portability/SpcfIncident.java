
package com.intuit.spc.foundations.portability;

/**
 * Standard implementation of an ISpcfIncident, which defaults the priority to "Error"
 */
public class SpcfIncident implements ISpcfIncident{
	
	private SpcfPriority mPriority = SpcfPriority.Error;
	private String mOperationalId = null;
	
	private String mMessage = null;
	private int mArchitectureId = 0;
	private int mIncidentId = 0;
	private int mModuleId = 0;

	private final int HashConst = 31;
	
	/**
	 * Create an instance of this class with the given info
	 * @param architectureId
	 * @param moduleId
	 * @param incidentId
	 */
	public SpcfIncident(int architectureId, int moduleId,
		int incidentId)
	{
		mArchitectureId = architectureId;
		mModuleId = moduleId;
		mIncidentId = incidentId;
	}

	/**
	 * Create an instance with the given info
	 * @param architectureId
	 * @param moduleId
	 * @param incidentId
	 * @param message
	 */
	public SpcfIncident(int architectureId, int moduleId,
		int incidentId, String message)
	{
		mArchitectureId = architectureId;
		mModuleId = moduleId;
		mIncidentId = incidentId;
		mMessage = message;
	}

	/**
	 * Indicates whether the specified object is equal to this
	 * @param obj
	 * @return
	 */
	/**
	 * Determines whether this object is equal to object obj.
	 * @param obj Object to compare with
	 * @return true, only if obj is an instance of SpcfIncident and the incidentId, moduleId,
	 * architectureId and message are the same.
	 */
	public boolean equals(Object obj) 
	{
		if (obj instanceof SpcfIncident) 
		{
			SpcfIncident code = (SpcfIncident) obj;

			if ((code.getIncidentId() == this.getIncidentId())
				&& (code.getModuleId() == this.getModuleId())
				&& (code.getArchitectureId() == this.getArchitectureId())
				&& (stringsAreEquals(code.getMessage(),this.getMessage()))) 
			{
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Returns the hash code for the current SpcfIncident class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	public int hashCode()
	{
		int hash = this.getIncidentId();
		hash = (hash * HashConst) + this.getModuleId();
		hash = (hash * HashConst) + this.getArchitectureId();
		hash = (hash * HashConst) + this.getMessage().hashCode();
		return hash;
	}
	
	/**
	 * Decides if strings are equal
	 * @param s1
	 * @param s2
	 * @return true if s1 equals s2 or both are null
	 */
	private boolean stringsAreEquals(String s1, String s2)
	{
		if (s1 == null)
		{
			return s2 == null;
		}
		else
		{
			return s1.equals(s2);
		}
	}

	/**
	 * An identifier that indicates which Architecture defined
	 * this exception. 
	 */
	public int getArchitectureId()
	{
		return mArchitectureId;
	}
	

	/**
	 *  An identifier that indicates which Module defined
	 * this exception. It should be unique to the architecture.
	 */
	public int getModuleId()
	{
		return mModuleId;
	}
	
	/**
	 *  An identifier that uniquely identifies this incident
	 *  within the module
	 */
	public int getIncidentId()
	{
		return mIncidentId;
	}

	/**
	 * Convert given info to an operational id
	 * @param architectureId the first integer in the return operational id
	 * @param moduleId the second integer in the return operational id
	 * @param incidentId the third integer in the return operational id
	 * @return a string which is the concatination of the 
	 * architectureId, moduleId, and incidentId separated by a '.'
	 */
	public static String calculateOperationalId(int architectureId, 
		int moduleId, int incidentId)
	{
		return architectureId + "." + moduleId + "." + incidentId;
	}

	/**
	 * Return the given message or null if not set
	 */
	public String getMessage()
	{
		return mMessage;
	}

	/**
	 * The level of this incident.. your code probably 
	 * isn't smart enough to really know that something should be "Error" 
	 * vs "Critical" for the life of your app, so you probably shouldn't 
	 * worry about setting this. The default is Error.
	 */
	public SpcfPriority getPriority()
	{
		return mPriority;
	}
	public void setPriority(SpcfPriority val)
	{
		mPriority = val;
	}

	/**
	 * Implementation of that uses ArchId, ModuleId, and IncidentId to generate
	 * the operationalId (if not overridden).
	 */
	public String getOperationalId()
	{
		if (mOperationalId == null)
		{
			return SpcfIncident.calculateOperationalId(getArchitectureId(), 
					getModuleId(), getIncidentId());
		}
		else
		{
			return mOperationalId;
		}
	}
	
	/**
	 * Override toString to include OperationalID and message
	 */
	public String toString()
	{
		String message = getMessage();
		if (message == null)
		{
			return getOperationalId();
		}
		else
		{
			return getOperationalId() + ":" + message;
		}
	}
}

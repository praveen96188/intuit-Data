package com.intuit.spc.foundations.portability;

/**
 * Enumeration of common directories. 
 */
public class SpcfCommonDirectoryEnum
{	
	/**
	 * Location to store application data for all users.<br/><br/>
	 * 
	 * On Windows (.Net or Java), this would be "%ALLUSERSPROFILE%\\Application Data".<br/><br/>
	 * 
	 * In platform-agnostic Java, there is no system property for this at all. Requesting this
	 * 		on a non-Windows platform may throw an exception.
	 */
    public static final SpcfCommonDirectoryEnum 
    AllUsersApplicationData = new SpcfCommonDirectoryEnum (1, "AllUsersApplicationData");

	/**
	 * Location to store user-specific application data.<br/><br/>
	 * 
	 * On Windows (.Net or Java), this would be "%APPDATA%".<br/><br/>
	 * 
	 * In platform-agnostic Java, there is no system property for this,
	 * 		but we we fabricate one by suffixing an "AppData" directory beneath 
	 * 		the "user.home" directory.
	 */
    public static final SpcfCommonDirectoryEnum 
    UserApplicationData = new SpcfCommonDirectoryEnum (2, "UserApplicationData");
    
	/**
	 * Location to store user-specific temporary data files.<br/><br/>
	 * 
	 * On Windows (.Net or Java), this would be "%TEMP%".<br/><br/>
	 * 
	 * In Java, this would be system property "java.io.tmpdir".
	 */
    public static final SpcfCommonDirectoryEnum 
    Temporary = new SpcfCommonDirectoryEnum (3, "Temporary");

	/**
	 * Location to store user-specific documents.<br/><br/>
	 * 
	 * On Windows (.Net or Java), this would be "%USERPROFILE%".<br/><br/>
	 * 
	 * In Java, this would be system property "home.dir".
	 */
    public static final SpcfCommonDirectoryEnum 
    UserDocuments = new SpcfCommonDirectoryEnum (4, "UserDocuments");

    /**
     * Initializes a new instance of this class
     *
     * @param id Unique id
     * @param name Name
     */
    private SpcfCommonDirectoryEnum(int id, String name)
    {
        setId(id);
        setName(name);
    }
	
	/**
     * The Id associated with this class
     */
    private int mId;

    /**
     * The Id associated with this class
     */
    public int getId()
    {
        return mId;
    }
    
    /**
     * The Id associated with this class
     */
    private void setId(int val)
    {
        mId = val;
    }
    
    /**
     * The Name associated with this class
     */
    private String mName;

    /**
     * The Name associated with this class
     */
    public String getName()
    {
        return mName;
    }
    
    /**
     * The Name associated with this class
     */
    private void setName(String name)
    {
        mName = name;
    }
    
    /**
     * converts text to instance
     * @param val
     * @return
     */
    public static SpcfCommonDirectoryEnum parse(String val)
    {
    	SpcfParamValidator.checkIsNotNull(val, "val");
        String lowerCase = val.toLowerCase();
        if (lowerCase.equals("allusersapplicationdata"))
        {
            return SpcfCommonDirectoryEnum.AllUsersApplicationData;
        }
        else if (lowerCase.equals("userapplicationdata"))
        {
            return SpcfCommonDirectoryEnum.UserApplicationData;
        }  
        if (lowerCase.equals("temporary"))
        {
            return SpcfCommonDirectoryEnum.Temporary;
        }
        else if (lowerCase.equals("userdocuments"))
        {
            return SpcfCommonDirectoryEnum.UserDocuments;
        }  
        else
        {
            return null;
        }
    }
    
    /**
     * To convert into a string.
     */
    public String toString()
    {
    	return this.getName();
    }
}
